
package com.freightfox.github_searcher.service;

import java.time.Instant;
import java.util.List;
import java.util.Comparator;

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

        String query = keyword;

        if (language != null && !language.isBlank()) {
            query += " language:" + language;
        }

        String selectedSort = (sort == null || sort.isBlank())
                ? "stars" : sort.toLowerCase();

        if (!selectedSort.equals("stars")
                && !selectedSort.equals("forks")
                && !selectedSort.equals("updated")) {

            throw new IllegalArgumentException(
                    "Sort must be stars, forks, or updated");
        }

        final String searchQuery = query;
        final String sortOrder = selectedSort;
        System.out.println("GitHub API sort parameter: " + sortOrder);

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

        // Ensure updated-date sorting is in descending order
        if (response != null
                && response.getItems() != null
                && "updated".equals(sortOrder)) {

            response.getItems().sort(
                    Comparator.comparing(
                            RepositoryItem::getUpdatedAt,
                            Comparator.nullsLast(Comparator.reverseOrder())
                    )
            );
        }

        return response;
    }

    // Save or update GitHub repositories in PostgreSQL
    public void saveRepositories(List<RepositoryItem> items) {

        if (items == null || items.isEmpty()) {
            return;
        }

        for (RepositoryItem item : items) {

            GitHubRepository gitRepo = repository
                    .findByGithubId(item.getId())
                    .orElseGet(GitHubRepository::new);

            gitRepo.setGithubId(item.getId());
            gitRepo.setName(item.getName());
            gitRepo.setFullName(item.getFullName());
            gitRepo.setDescription(item.getDescription());
            gitRepo.setLanguage(item.getLanguage());
            gitRepo.setHtmlUrl(item.getHtmlUrl());
            gitRepo.setStars(item.getStars());
            gitRepo.setForks(item.getForks());

            if (item.getUpdatedAt() != null
                    && !item.getUpdatedAt().isBlank()) {
                gitRepo.setUpdatedAt(Instant.parse(item.getUpdatedAt()));
            }

            repository.save(gitRepo);
        }
    }

    // Search and save repositories in one operation
    public GitHubSearchResponse searchAndSaveRepositories(
            String keyword, String language, String sort) {

        GitHubSearchResponse response =
                searchRepositories(keyword, language, sort);

        if (response != null) {
            saveRepositories(response.getItems());
        }

        return response;
    }

    // Retrieve all saved repositories from PostgreSQL
    public List<GitHubRepository> getSavedRepositories() {
        return repository.findAll();
    }
}
