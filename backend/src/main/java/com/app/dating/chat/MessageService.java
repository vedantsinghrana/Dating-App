package com.app.dating.chat;

import com.app.dating.common.ApiException;
import com.app.dating.matching.Match;
import com.app.dating.matching.MatchRepository;
import com.app.dating.profile.Profile;
import com.app.dating.profile.ProfilePrompt;
import com.app.dating.profile.ProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class MessageService {

	private final MessageRepository messageRepository;
	private final MatchRepository matchRepository;
	private final ProfileRepository profileRepository;
	private final SimpMessagingTemplate messagingTemplate;

	public MessageService(
		MessageRepository messageRepository,
		MatchRepository matchRepository,
		ProfileRepository profileRepository,
		SimpMessagingTemplate messagingTemplate
	) {
		this.messageRepository = messageRepository;
		this.matchRepository = matchRepository;
		this.profileRepository = profileRepository;
		this.messagingTemplate = messagingTemplate;
	}

	@Transactional(readOnly = true)
	public MessagesResponse listMessages(UUID me, UUID matchId) {
		requireParticipant(me, matchId);
		return new MessagesResponse(
			messageRepository.findByMatchIdOrderBySentAtAsc(matchId).stream().map(MessageResponse::from).toList()
		);
	}

	@Transactional
	public MessageResponse sendMessage(UUID me, UUID matchId, SendMessageRequest request) {
		Match match = requireParticipant(me, matchId);

		if (!match.isOpeningMoveDone() && Instant.now().isAfter(match.getExpiresAt())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "This match has expired");
		}

		boolean isFirstMessage = !messageRepository.existsByMatchId(matchId);
		if (isFirstMessage) {
			validateOpeningMove(match, me, request.promptId());
		}

		Message message = new Message(matchId, me, request.content(), request.promptId());
		messageRepository.save(message);

		if (isFirstMessage) {
			match.setOpeningMoveDone(true);
			matchRepository.save(match);
		}

		MessageResponse response = MessageResponse.from(message);
		messagingTemplate.convertAndSend("/topic/matches/" + matchId, response);
		return response;
	}

	private void validateOpeningMove(Match match, UUID me, UUID promptId) {
		if (promptId == null) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "The first message must reference one of the other person's prompts");
		}
		UUID otherUserId = match.otherUserId(me);
		Profile otherProfile = profileRepository.findById(otherUserId)
			.orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "promptId must reference one of the other person's prompts"));
		boolean promptBelongsToOther = otherProfile.getPrompts().stream()
			.map(ProfilePrompt::getId)
			.anyMatch(promptId::equals);
		if (!promptBelongsToOther) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "promptId must reference one of the other person's prompts");
		}
	}

	private Match requireParticipant(UUID me, UUID matchId) {
		Match match = matchRepository.findById(matchId)
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Match not found"));
		if (!match.getUserAId().equals(me) && !match.getUserBId().equals(me)) {
			throw new ApiException(HttpStatus.NOT_FOUND, "Match not found");
		}
		return match;
	}

}
