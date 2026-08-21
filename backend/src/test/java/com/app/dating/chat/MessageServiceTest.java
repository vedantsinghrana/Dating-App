package com.app.dating.chat;

import com.app.dating.auth.User;
import com.app.dating.common.ApiException;
import com.app.dating.matching.Match;
import com.app.dating.matching.MatchRepository;
import com.app.dating.profile.Profile;
import com.app.dating.profile.ProfilePrompt;
import com.app.dating.profile.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

	@Mock
	private MessageRepository messageRepository;

	@Mock
	private MatchRepository matchRepository;

	@Mock
	private ProfileRepository profileRepository;

	@Mock
	private SimpMessagingTemplate messagingTemplate;

	private MessageService messageService;
	private UUID me;
	private UUID them;
	private UUID matchId;

	@BeforeEach
	void setUp() {
		messageService = new MessageService(messageRepository, matchRepository, profileRepository, messagingTemplate);
		me = UUID.randomUUID();
		them = UUID.randomUUID();
		matchId = UUID.randomUUID();
	}

	private void stubSaveAssignsId() {
		// GenerationType.UUID assigns the id during a real persist(); assign it here the
		// way Hibernate would, since the mock repository skips that.
		when(messageRepository.save(any(Message.class))).thenAnswer(inv -> {
			Message message = inv.getArgument(0);
			message.setId(UUID.randomUUID());
			return message;
		});
	}

	private Match activeMatch() {
		UUID userAId = me.compareTo(them) < 0 ? me : them;
		UUID userBId = me.compareTo(them) < 0 ? them : me;
		Match match = new Match(userAId, userBId, Instant.now().plus(48, ChronoUnit.HOURS));
		match.setId(matchId);
		return match;
	}

	@Test
	void listMessagesThrowsWhenMatchMissing() {
		when(matchRepository.findById(matchId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> messageService.listMessages(me, matchId))
			.isInstanceOf(ApiException.class)
			.hasMessage("Match not found");
	}

	@Test
	void listMessagesThrowsWhenNotParticipant() {
		Match match = activeMatch();
		when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));

		assertThatThrownBy(() -> messageService.listMessages(UUID.randomUUID(), matchId))
			.isInstanceOf(ApiException.class)
			.hasMessage("Match not found");
	}

	@Test
	void sendThrowsWhenExpiredWithoutOpeningMove() {
		UUID userAId = me.compareTo(them) < 0 ? me : them;
		UUID userBId = me.compareTo(them) < 0 ? them : me;
		Match match = new Match(userAId, userBId, Instant.now().minus(1, ChronoUnit.HOURS));
		match.setId(matchId);
		when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));

		assertThatThrownBy(() -> messageService.sendMessage(me, matchId, new SendMessageRequest("hi", null)))
			.isInstanceOf(ApiException.class)
			.hasMessage("This match has expired");
	}

	@Test
	void firstMessageWithoutPromptIdIsRejected() {
		Match match = activeMatch();
		when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
		when(messageRepository.existsByMatchId(matchId)).thenReturn(false);

		assertThatThrownBy(() -> messageService.sendMessage(me, matchId, new SendMessageRequest("hi", null)))
			.isInstanceOf(ApiException.class)
			.hasMessage("The first message must reference one of the other person's prompts");
	}

	@Test
	void firstMessageWithPromptIdNotBelongingToOtherIsRejected() {
		Match match = activeMatch();
		when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
		when(messageRepository.existsByMatchId(matchId)).thenReturn(false);

		User themUser = new User("them@example.com", "hashed");
		themUser.setId(them);
		Profile themProfile = new Profile(themUser, "Them", LocalDate.of(1996, 1, 1));
		themProfile.getPrompts().add(new ProfilePrompt(themProfile, "Q", "A", 0));
		when(profileRepository.findById(them)).thenReturn(Optional.of(themProfile));

		UUID randomPromptId = UUID.randomUUID();
		assertThatThrownBy(() -> messageService.sendMessage(me, matchId, new SendMessageRequest("hi", randomPromptId)))
			.isInstanceOf(ApiException.class)
			.hasMessage("promptId must reference one of the other person's prompts");
	}

	@Test
	void firstMessageWithValidPromptIdSucceedsAndSetsOpeningMoveDone() {
		Match match = activeMatch();
		when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
		when(messageRepository.existsByMatchId(matchId)).thenReturn(false);

		User themUser = new User("them@example.com", "hashed");
		themUser.setId(them);
		Profile themProfile = new Profile(themUser, "Them", LocalDate.of(1996, 1, 1));
		ProfilePrompt prompt = new ProfilePrompt(themProfile, "Q", "A", 0);
		prompt.setId(UUID.randomUUID());
		themProfile.getPrompts().add(prompt);
		when(profileRepository.findById(them)).thenReturn(Optional.of(themProfile));
		stubSaveAssignsId();

		MessageResponse response = messageService.sendMessage(me, matchId, new SendMessageRequest("hi", prompt.getId()));

		assertThat(response.senderId()).isEqualTo(me.toString());
		assertThat(response.content()).isEqualTo("hi");
		assertThat(match.isOpeningMoveDone()).isTrue();
		verify(matchRepository).save(match);
		verify(messagingTemplate).convertAndSend(anyString(), any(MessageResponse.class));
	}

	@Test
	void subsequentMessageDoesNotRequirePromptId() {
		Match match = activeMatch();
		match.setOpeningMoveDone(true);
		when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
		when(messageRepository.existsByMatchId(matchId)).thenReturn(true);
		stubSaveAssignsId();

		MessageResponse response = messageService.sendMessage(me, matchId, new SendMessageRequest("second message", null));

		assertThat(response.content()).isEqualTo("second message");
		verify(matchRepository, org.mockito.Mockito.never()).save(any());
	}

}
