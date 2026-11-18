
package com.freightfox.github_searcher.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.freightfox.github_searcher.dto.GitHubSearchResponse;
import com.freightfox.github_searcher.entity.GitHubRepository;
import com.freightfox.github_searcher.service.GitHubService;

@RestController
@RequestMapping("/api/github")
public class GitHubController {

    private final GitHubService gitHubService;

    public GitHubController(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    // Search GitHub repositories and save results
    @GetMapping("/search")
    public GitHubSearchResponse searchRepositories(
            @RequestParam String keyword,
            @RequestParam(required = false) String language,
            @RequestParam(required = false, defaultValue = "stars") String sort) {

        return gitHubService.searchAndSaveRepositories(
                keyword, language, sort);
    }

    // Retrieve all saved repositories
    @GetMapping("/saved")
    public List<GitHubRepository> getSavedRepositories() {
        return gitHubService.getSavedRepositories();
    }
}
