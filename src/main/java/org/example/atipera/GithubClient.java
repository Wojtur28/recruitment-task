package org.example.atipera;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

@HttpExchange
interface GithubClient {

    @GetExchange("/users/{username}/repos")
    List<GithubRepository> getUserRepositories(@PathVariable String username);

    @GetExchange("/repos/{owner}/{repo}/branches")
    List<GithubBranch> getRepositoryBranches(@PathVariable String owner, @PathVariable String repo);
}
