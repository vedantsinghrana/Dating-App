package com.app.dating.matching;

import com.app.dating.auth.UserRepository;
import com.app.dating.common.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SwipeServiceTest {

	@Mock
	private SwipeRepository swipeRepository;

	@Mock
	private MatchRepository matchRepository;

	@Mock
	private UserRepository userRepository;

	private SwipeService swipeService;
	private UUID me;
	private UUID them;

	@BeforeEach
	void setUp() {
		swipeService = new SwipeService(swipeRepository, matchRepository, userRepository, 48);
		me = UUID.randomUUID();
		them = UUID.randomUUID();
	}

	@Test
	void rejectsSwipingOnSelf() {
		assertThatThrownBy(() -> swipeService.swipe(me, new SwipeRequest(me, SwipeDirection.LIKE)))
			.isInstanceOf(ApiException.class)
			.hasMessage("Cannot swipe on yourself");
	}

	@Test
	void rejectsUnknownTarget() {
		when(userRepository.existsById(them)).thenReturn(false);

		assertThatThrownBy(() -> swipeService.swipe(me, new SwipeRequest(them, SwipeDirection.LIKE)))
			.isInstanceOf(ApiException.class)
			.hasMessage("User not found");
	}

	@Test
	void rejectsDuplicateSwipe() {
		when(userRepository.existsById(them)).thenReturn(true);
		when(swipeRepository.existsBySwiperIdAndSwipeeId(me, them)).thenReturn(true);

		assertThatThrownBy(() -> swipeService.swipe(me, new SwipeRequest(them, SwipeDirection.LIKE)))
			.isInstanceOf(ApiException.class)
			.hasMessage("Already swiped on this user");
	}

	@Test
	void passDoesNotCreateMatch() {
		when(userRepository.existsById(them)).thenReturn(true);
		when(swipeRepository.existsBySwiperIdAndSwipeeId(me, them)).thenReturn(false);

		SwipeResponse response = swipeService.swipe(me, new SwipeRequest(them, SwipeDirection.PASS));

		assertThat(response.matched()).isFalse();
		assertThat(response.matchId()).isNull();
		verify(matchRepository, never()).save(any());
	}

	@Test
	void likeWithoutMutualDoesNotCreateMatch() {
		when(userRepository.existsById(them)).thenReturn(true);
		when(swipeRepository.existsBySwiperIdAndSwipeeId(me, them)).thenReturn(false);
		when(swipeRepository.findBySwiperIdAndSwipeeIdAndDirection(them, me, SwipeDirection.LIKE))
			.thenReturn(Optional.empty());

		SwipeResponse response = swipeService.swipe(me, new SwipeRequest(them, SwipeDirection.LIKE));

		assertThat(response.matched()).isFalse();
		verify(matchRepository, never()).save(any());
	}

	@Test
	void mutualLikeCreatesMatchWithOrderedPair() {
		when(userRepository.existsById(them)).thenReturn(true);
		when(swipeRepository.existsBySwiperIdAndSwipeeId(me, them)).thenReturn(false);
		when(swipeRepository.findBySwiperIdAndSwipeeIdAndDirection(them, me, SwipeDirection.LIKE))
			.thenReturn(Optional.of(new Swipe(them, me, SwipeDirection.LIKE)));
		UUID expectedA = me.compareTo(them) < 0 ? me : them;
		UUID expectedB = me.compareTo(them) < 0 ? them : me;
		when(matchRepository.existsByUserAIdAndUserBId(expectedA, expectedB)).thenReturn(false);
		// GenerationType.UUID assigns the id during a real persist(); assign it here
		// the way Hibernate would, since the mock repository skips that.
		when(matchRepository.save(any(Match.class))).thenAnswer(inv -> {
			Match match = inv.getArgument(0);
			match.setId(UUID.randomUUID());
			return match;
		});

		SwipeResponse response = swipeService.swipe(me, new SwipeRequest(them, SwipeDirection.LIKE));

		assertThat(response.matched()).isTrue();
		assertThat(response.matchId()).isNotBlank();
	}

}
