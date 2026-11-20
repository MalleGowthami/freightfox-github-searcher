
package com.freightfox.github_searcher.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.freightfox.github_searcher.entity.GitHubRepository;

public interface GitHubRepositoryRepository
        extends JpaRepository<GitHubRepository, Long> {

    // Find repository using GitHub ID
    Optional<GitHubRepository> findByGithubId(Long githubId);

    // Check whether repository already exists
    boolean existsByGithubId(Long githubId);

    // Find multiple repositories using GitHub IDs
    List<GitHubRepository> findAllByGithubIdIn(
            Collection<Long> githubIds);

    // Filter repositories by language and minimum stars
    @Query("SELECT r FROM GitHubRepository r " +
           "WHERE (COALESCE(:language, '') = '' " +
           "OR LOWER(r.language) = LOWER(:language)) " +
           "AND r.stars >= COALESCE(:minStars, 0)")
    List<GitHubRepository> findFilteredRepositories(
            @Param("language") String language,
            @Param("minStars") Integer minStars,
            Sort sort);
}
