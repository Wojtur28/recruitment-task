package org.example.atipera;

record GithubBranch(
        String name,
        Commit commit
) {
    record Commit(String sha) {

    }
}
