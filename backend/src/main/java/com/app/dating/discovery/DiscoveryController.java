package com.app.dating.discovery;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class DiscoveryController {

	private final DiscoveryService discoveryService;
	private final TopPickService topPickService;

	public DiscoveryController(DiscoveryService discoveryService, TopPickService topPickService) {
		this.discoveryService = discoveryService;
		this.topPickService = topPickService;
	}

	@GetMapping("/api/discover")
	public DiscoveryResponse discover(
		Authentication authentication,
		@RequestParam(required = false) Integer radiusKm,
		@RequestParam(defaultValue = "0") int page
	) {
		UUID me = (UUID) authentication.getPrincipal();
		return discoveryService.discover(me, radiusKm, page);
	}

	@GetMapping("/api/discover/top-pick")
	public ResponseEntity<DiscoveryResultDto> topPick(Authentication authentication) {
		UUID me = (UUID) authentication.getPrincipal();
		return topPickService.getTodaysTopPick(me)
			.map(ResponseEntity::ok)
			.orElseGet(() -> ResponseEntity.noContent().build());
	}

}
