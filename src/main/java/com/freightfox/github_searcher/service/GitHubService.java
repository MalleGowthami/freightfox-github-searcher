
package com.freightfox.github_searcher.service;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.freightfox.github_searcher.dto.GitHubSearchResponse;
import com.freightfox.github_searcher.dto.GitHubSearchResponse.RepositoryItem;
import com.freightfox.github_searcher.entity.GitHubRepository;
import com.freightfox.github_searcher.repository.GitHubRepositoryRepository;

@Service
public class GitHubService {

    private final RestClient restClient;
    private final GitHubRepositoryRepository repository;

    public GitHubService(GitHubRepositoryRepository repository) {

        this.repository = repository;

        this.restClient = RestClient.builder()
                .baseUrl("https://api.github.com")
                .defaultHeader("Accept", "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .build();
    }

    // Search repositories from GitHub API
    public GitHubSearchResponse searchRepositories(
            String keyword, String language, String sort) {

        if (keyword == null || keyword.isBlank()) {
            throw new IllegalArgumentException(
                    "Search keyword cannot be empty");
        }

        String query = keyword.trim();

        if (language != null && !language.isBlank()) {
            query += " language:" + language.trim();
        }

        String selectedSort = (sort == null || sort.isBlank())
                ? "stars" : sort.trim().toLowerCase();

        if (!selectedSort.equals("stars")
                && !selectedSort.equals("forks")
                && !selectedSort.equals("updated")) {

            throw new IllegalArgumentException(
                    "Sort must be stars, forks, or updated");
        }

        final String searchQuery = query;
        final String sortOrder = selectedSort;

        GitHubSearchResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search/repositories")
                        .queryParam("q", searchQuery)
                        .queryParam("sort", sortOrder)
                        .queryParam("order", "desc")
                        .queryParam("per_page", 100)
                        .build())
                .retrieve()
                .body(GitHubSearchResponse.class);

        // Sort by latest updated date
        if (response != null
                && response.getItems() != null
                && "updated".equals(sortOrder)) {

            response.getItems().sort(
                    Comparator.comparing(
                            RepositoryItem::getUpdatedAt,
                            Comparator.nullsLast(
                                    Comparator.reverseOrder())));
        }

        return response;
    }

    // Save or update repositories in PostgreSQL using batch operations
    public void saveRepositories(List<RepositoryItem> items) {

        if (items == null || items.isEmpty()) {
            return;
        }

        // Collect GitHub IDs from the API response
        List<Long> githubIds = items.stream()
                .map(RepositoryItem::getId)
                .toList();

        // Fetch all existing repositories in one database query
        List<GitHubRepository> existingRepositories =
                repository.findAllByGithubIdIn(githubIds);

        // Store existing repositories in a map for quick lookup
        Map<Long, GitHubRepository> repositoryMap = new HashMap<>();

        for (GitHubRepository existing : existingRepositories) {
            repositoryMap.put(existing.getGithubId(), existing);
        }

        // Prepare repositories for saving
        for (RepositoryItem item : items) {

            GitHubRepository gitRepo =
                    repositoryMap.computeIfAbsent(
                            item.getId(),
                            id -> new GitHubRepository());

            gitRepo.setGithubId(item.getId());
            gitRepo.setName(item.getName());
            gitRepo.setFullName(item.getFullName());
            gitRepo.setDescription(item.getDescription());

            if (item.getOwner() != null) {
                gitRepo.setOwner(item.getOwner().getLogin());
            }

            gitRepo.setLanguage(item.getLanguage());
            gitRepo.setHtmlUrl(item.getHtmlUrl());
            gitRepo.setStars(item.getStars());
            gitRepo.setForks(item.getForks());

            if (item.getUpdatedAt() != null
                    && !item.getUpdatedAt().isBlank()) {

                gitRepo.setUpdatedAt(
                        Instant.parse(item.getUpdatedAt()));
            }
        }

        // Save all repositories together
        repository.saveAll(repositoryMap.values());
    }

    // Search GitHub and save repositories
    public GitHubSearchResponse searchAndSaveRepositories(
            String keyword, String language, String sort) {

        GitHubSearchResponse response =
                searchRepositories(keyword, language, sort);

        if (response != null) {
            saveRepositories(response.getItems());
        }

        return response;
    }

    // Filter and sort saved repositories
    public List<GitHubRepository> getFilteredRepositories(
            String language, Integer minStars, String sort) {

        if (minStars != null && minStars < 0) {
            throw new IllegalArgumentException(
                    "minStars cannot be negative");
        }

        String selectedSort = (sort == null || sort.isBlank())
                ? "stars" : sort.trim().toLowerCase();

        if (!selectedSort.equals("stars")
                && !selectedSort.equals("forks")
                && !selectedSort.equals("updated")) {

            throw new IllegalArgumentException(
                    "Sort must be stars, forks, or updated");
        }

        String sortField = selectedSort.equals("updated")
                ? "updatedAt" : selectedSort;

        Sort sorting = Sort.by(
                Sort.Direction.DESC, sortField);

        String selectedLanguage =
                (language == null || language.isBlank())
                        ? null : language.trim();

        return repository.findFilteredRepositories(
                selectedLanguage, minStars, sorting);
    }

    // Retrieve all saved repositories
    public List<GitHubRepository> getSavedRepositories() {
        return repository.findAll();
    }
}
