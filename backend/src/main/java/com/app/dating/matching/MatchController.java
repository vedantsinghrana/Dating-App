package com.app.dating.matching;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

	private final MatchService matchService;

	public MatchController(MatchService matchService) {
		this.matchService = matchService;
	}

	@GetMapping
	public MatchesResponse listMyMatches(Authentication authentication) {
		UUID me = (UUID) authentication.getPrincipal();
		return matchService.listMyMatches(me);
	}

}
