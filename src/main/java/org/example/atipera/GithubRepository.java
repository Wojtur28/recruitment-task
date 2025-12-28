package org.example.atipera;

record GithubRepository(
        String name,
        Owner owner,
        boolean fork
) {
    record Owner(String login) {

    }
}
