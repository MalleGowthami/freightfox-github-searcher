
package com.freightfox.github_searcher.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.freightfox.github_searcher.entity.GitHubRepository;

public interface GitHubRepositoryRepository
        extends JpaRepository<GitHubRepository, Long> {

    Optional<GitHubRepository> findByGithubId(Long githubId);

    boolean existsByGithubId(Long githubId);
}
