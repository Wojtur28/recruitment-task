package org.example.atipera;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Service
@RequiredArgsConstructor
class GithubService {

    private final GithubClient githubClient;
    private final ExecutorService virtualThreadExecutor;

    List<Repository> getUserRepositories(String username) {
        List<GithubRepository> repositories;
        try {
            repositories = githubClient.getUserRepositories(username);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 404) {
                throw new UserNotFoundException(username);
            }
            throw e;
        }

        List<CompletableFuture<Repository>> futures = repositories.stream()
                .filter(repo -> !repo.fork())
                .map(repo -> CompletableFuture.supplyAsync(
                        () -> getRepositoryBranches(repo),
                        virtualThreadExecutor
                ))
                .toList();

        return futures.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    private Repository getRepositoryBranches(GithubRepository githubRepo) {
        String owner = githubRepo.owner().login();
        String repoName = githubRepo.name();

        List<Branch> branches = githubClient.getRepositoryBranches(owner, repoName).stream()
                .map(githubBranch -> new Branch(
                        githubBranch.name(),
                        githubBranch.commit().sha()
                ))
                .toList();

        return new Repository(repoName, owner, branches);
    }
}
