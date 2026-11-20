
package com.freightfox.github_searcher.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.freightfox.github_searcher.dto.GitHubSearchResponse.RepositoryItem;
import com.freightfox.github_searcher.entity.GitHubRepository;
import com.freightfox.github_searcher.repository.GitHubRepositoryRepository;

@ExtendWith(MockitoExtension.class)
class GitHubServiceTest {

    @Mock
    private GitHubRepositoryRepository repository;

    @InjectMocks
    private GitHubService githubService;

    @Test
    void shouldRejectEmptyKeyword() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> githubService.searchRepositories("", "Java", "stars"));

        assertEquals("Search keyword cannot be empty", exception.getMessage());
    }

    @Test
    void shouldRejectInvalidSort() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> githubService.searchRepositories("springboot", "Java", "invalid"));

        assertEquals("Sort must be stars, forks, or updated", exception.getMessage());
    }

    @Test
    void shouldNotSaveWhenRepositoryListIsEmpty() {
        githubService.saveRepositories(new ArrayList<>());

        verify(repository, never()).saveAll(any());
    }

    @Test
    void shouldSaveRepository() {
        RepositoryItem item = new RepositoryItem();
        item.setId(12345L);
        item.setName("spring-boot");
        item.setFullName("example/spring-boot");
        item.setDescription("Spring Boot example");
        item.setLanguage("Java");
        item.setHtmlUrl("https://github.com/example/spring-boot");
        item.setStars(100);
        item.setForks(20);
        item.setUpdatedAt("2026-11-18T10:00:00Z");

        when(repository.findAllByGithubIdIn(anyCollection()))
                .thenReturn(List.of());

        githubService.saveRepositories(List.of(item));

        verify(repository).findAllByGithubIdIn(anyCollection());
        verify(repository).saveAll(any());
    }

    @Test
    void shouldUpdateExistingRepository() {
        GitHubRepository existing = new GitHubRepository();
        existing.setGithubId(12345L);
        existing.setName("old-name");

        RepositoryItem item = new RepositoryItem();
        item.setId(12345L);
        item.setName("spring-boot");
        item.setFullName("example/spring-boot");
        item.setDescription("Updated description");
        item.setLanguage("Java");
        item.setStars(200);
        item.setForks(50);
        item.setUpdatedAt("2026-11-19T10:00:00Z");

        when(repository.findAllByGithubIdIn(anyCollection()))
                .thenReturn(List.of(existing));

        githubService.saveRepositories(List.of(item));

        assertEquals("spring-boot", existing.getName());
        assertEquals("Updated description", existing.getDescription());
        assertEquals(200, existing.getStars());

        verify(repository).saveAll(any());
    }
}
