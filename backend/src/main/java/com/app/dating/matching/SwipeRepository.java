package com.app.dating.matching;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SwipeRepository extends JpaRepository<Swipe, UUID> {

	boolean existsBySwiperIdAndSwipeeId(UUID swiperId, UUID swipeeId);

	Optional<Swipe> findBySwiperIdAndSwipeeIdAndDirection(UUID swiperId, UUID swipeeId, SwipeDirection direction);

}
