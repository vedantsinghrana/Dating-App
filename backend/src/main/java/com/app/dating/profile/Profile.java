package com.app.dating.profile;

import com.app.dating.auth.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implements Persistable so Spring Data JPA's save() calls persist() for a brand-new
 * Profile instead of merge(): the id here is assigned manually via @MapsId in the
 * constructor (not @GeneratedValue), so it's never null even for an unsaved instance —
 * the default "id == null means new" heuristic can't tell new from existing. createdAt is
 * only ever set by @PrePersist, so its nullness is an honest signal of newness instead.
 */
@Entity
@Table(name = "profiles")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Profile implements Persistable<UUID> {

	@Id
	private UUID id;

	@OneToOne(fetch = FetchType.LAZY)
	@MapsId
	@JoinColumn(name = "id")
	private User user;

	@Column(nullable = false, length = 120)
	private String name;

	@Column(nullable = false)
	private LocalDate birthdate;

	@Column(columnDefinition = "text")
	private String bio;

	@Column(columnDefinition = "geography(Point,4326)")
	private Point location;

	@Column(name = "search_radius_km", nullable = false)
	private Integer searchRadiusKm = 25;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@OrderBy("position ASC")
	private List<ProfilePhoto> photos = new ArrayList<>();

	@OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@OrderBy("position ASC")
	private List<ProfilePrompt> prompts = new ArrayList<>();

	public Profile(User user, String name, LocalDate birthdate) {
		this.user = user;
		this.id = user.getId();
		this.name = name;
		this.birthdate = birthdate;
	}

	@PrePersist
	void onCreate() {
		Instant now = Instant.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		this.updatedAt = Instant.now();
	}

	@Override
	public boolean isNew() {
		return createdAt == null;
	}

}
