
package com.freightfox.github_searcher.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.freightfox.github_searcher.dto.GitHubSearchRequest;
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

    // Search GitHub repositories and save them
    @PostMapping("/search")
    public GitHubSearchResponse searchRepositories(
            @RequestBody GitHubSearchRequest request) {

        return gitHubService.searchAndSaveRepositories(
                request.getQuery(),
                request.getLanguage(),
                request.getSort());
    }

    // Get all saved repositories
    @GetMapping("/saved")
    public List<GitHubRepository> getSavedRepositories() {
        return gitHubService.getSavedRepositories();
    }

    // Filter and sort saved repositories
    @GetMapping("/repositories")
    public List<GitHubRepository> getFilteredRepositories(
            @RequestParam(required = false) String language,
            @RequestParam(required = false) Integer minStars,
            @RequestParam(required = false, defaultValue = "stars") String sort) {

        return gitHubService.getFilteredRepositories(
                language, minStars, sort);
    }
}
