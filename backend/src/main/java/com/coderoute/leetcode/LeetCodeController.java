package com.coderoute.leetcode;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.coderoute.dto.leetcode.LeetCodeStatsResponse;

import jakarta.validation.constraints.NotBlank;

/**
 * Read-only proxy for basic public LeetCode stats. Requires a valid JWT like
 * every other private endpoint; the profile link comes from the caller.
 */
@RestController
@RequestMapping("/api/leetcode")
@Validated
public class LeetCodeController {
	private final LeetCodeService leetCodeService;

	public LeetCodeController(LeetCodeService leetCodeService) {
		this.leetCodeService = leetCodeService;
	}

	@GetMapping("/stats")
	public LeetCodeStatsResponse stats(@RequestParam @NotBlank String profileUrl) {
		return leetCodeService.stats(profileUrl);
	}
}
