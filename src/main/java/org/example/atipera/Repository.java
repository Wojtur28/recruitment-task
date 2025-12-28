package org.example.atipera;

import java.util.List;

public record Repository(
        String repositoryName,
        String ownerLogin,
        List<Branch> branches
) {
    public Repository {
        branches = List.copyOf(branches);
    }
}
