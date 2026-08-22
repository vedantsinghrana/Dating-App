package com.app.dating.matching;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/swipes")
public class SwipeController {

	private final SwipeService swipeService;

	public SwipeController(SwipeService swipeService) {
		this.swipeService = swipeService;
	}

	@PostMapping
	public SwipeResponse swipe(Authentication authentication, @Valid @RequestBody SwipeRequest request) {
		UUID me = (UUID) authentication.getPrincipal();
		return swipeService.swipe(me, request);
	}

}
