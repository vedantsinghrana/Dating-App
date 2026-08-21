package com.app.dating.matching;

import com.app.dating.auth.UserRepository;
import com.app.dating.common.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class SwipeService {

	private final SwipeRepository swipeRepository;
	private final MatchRepository matchRepository;
	private final UserRepository userRepository;
	private final Duration matchExpiry;

	public SwipeService(
		SwipeRepository swipeRepository,
		MatchRepository matchRepository,
		UserRepository userRepository,
		@Value("${app.match.expiry-hours}") long matchExpiryHours
	) {
		this.swipeRepository = swipeRepository;
		this.matchRepository = matchRepository;
		this.userRepository = userRepository;
		this.matchExpiry = Duration.ofHours(matchExpiryHours);
	}

	@Transactional
	public SwipeResponse swipe(UUID me, SwipeRequest request) {
		UUID toUserId = request.toUserId();
		if (toUserId.equals(me)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot swipe on yourself");
		}
		if (!userRepository.existsById(toUserId)) {
			throw new ApiException(HttpStatus.NOT_FOUND, "User not found");
		}
		if (swipeRepository.existsBySwiperIdAndSwipeeId(me, toUserId)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Already swiped on this user");
		}

		swipeRepository.save(new Swipe(me, toUserId, request.direction()));

		if (request.direction() != SwipeDirection.LIKE) {
			return new SwipeResponse(false, null);
		}

		boolean theyLikedMe = swipeRepository
			.findBySwiperIdAndSwipeeIdAndDirection(toUserId, me, SwipeDirection.LIKE)
			.isPresent();
		if (!theyLikedMe) {
			return new SwipeResponse(false, null);
		}

		UUID userAId = me.compareTo(toUserId) < 0 ? me : toUserId;
		UUID userBId = me.compareTo(toUserId) < 0 ? toUserId : me;
		if (matchRepository.existsByUserAIdAndUserBId(userAId, userBId)) {
			return new SwipeResponse(false, null);
		}

		Match match = new Match(userAId, userBId, Instant.now().plus(matchExpiry));
		matchRepository.save(match);
		return new SwipeResponse(true, match.getId().toString());
	}

}
