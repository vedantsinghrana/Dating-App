package com.app.dating.chat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {

	List<Message> findByMatchIdOrderBySentAtAsc(UUID matchId);

	boolean existsByMatchId(UUID matchId);

}
