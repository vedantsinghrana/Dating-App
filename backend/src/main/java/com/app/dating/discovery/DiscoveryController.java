package com.app.dating.discovery;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class DiscoveryController {

	private final DiscoveryService discoveryService;

	public DiscoveryController(DiscoveryService discoveryService) {
		this.discoveryService = discoveryService;
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

}
