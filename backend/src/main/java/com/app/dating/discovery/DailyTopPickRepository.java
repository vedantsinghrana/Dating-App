package com.app.dating.discovery;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface DailyTopPickRepository extends JpaRepository<DailyTopPick, UUID> {

	Optional<DailyTopPick> findByUserIdAndPickDate(UUID userId, LocalDate pickDate);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("DELETE FROM DailyTopPick p WHERE p.userId = :userId AND p.pickDate = :pickDate")
	void deleteByUserIdAndPickDate(@Param("userId") UUID userId, @Param("pickDate") LocalDate pickDate);

}
