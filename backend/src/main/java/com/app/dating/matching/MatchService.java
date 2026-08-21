package com.app.dating.matching;

import com.app.dating.profile.Profile;
import com.app.dating.profile.ProfilePhoto;
import com.app.dating.profile.ProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class MatchService {

	private final MatchRepository matchRepository;
	private final ProfileRepository profileRepository;

	public MatchService(MatchRepository matchRepository, ProfileRepository profileRepository) {
		this.matchRepository = matchRepository;
		this.profileRepository = profileRepository;
	}

	@Transactional(readOnly = true)
	public MatchesResponse listMyMatches(UUID me) {
		List<Match> matches = matchRepository.findAllForUser(me);

		List<UUID> otherUserIds = matches.stream().map(m -> m.otherUserId(me)).toList();
		Map<UUID, Profile> profilesById = profileRepository.findAllById(otherUserIds).stream()
			.collect(java.util.stream.Collectors.toMap(Profile::getId, p -> p));

		List<MatchSummaryDto> summaries = matches.stream()
			.map(match -> toSummary(match, me, profilesById.get(match.otherUserId(me))))
			.toList();

		return new MatchesResponse(summaries);
	}

	private MatchSummaryDto toSummary(Match match, UUID viewerId, Profile otherProfile) {
		OtherUserDto otherUser = otherProfile == null
			? new OtherUserDto(match.otherUserId(viewerId).toString(), "Unknown", List.of())
			: new OtherUserDto(
				otherProfile.getId().toString(),
				otherProfile.getName(),
				otherProfile.getPhotos().stream().map(ProfilePhoto::getUrl).toList()
			);
		return new MatchSummaryDto(
			match.getId().toString(),
			otherUser,
			match.getCreatedAt(),
			match.getExpiresAt(),
			match.isOpeningMoveDone()
		);
	}

}
