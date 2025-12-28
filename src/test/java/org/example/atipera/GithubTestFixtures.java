package org.example.atipera;

class GithubTestFixtures {

    private static final String API_BASE_URL = "/api/v1/users";

    static String userRepositoriesUrl(String username) {
        return API_BASE_URL + "/" + username + "/repositories";
    }

    static String repositoryJson(String name, String owner, boolean fork, String baseUrl) {
        return """
                {
                  "name": "%s",
                  "owner": {
                    "login": "%s"
                  },
                  "fork": %s,
                  "branches_url": "%s/repos/%s/%s/branches{/branch}"
                }
                """.formatted(name, owner, fork, baseUrl, owner, name);
    }

    static String repositoriesJson(String name, String owner, boolean fork, String baseUrl) {
        return "[" + repositoryJson(name, owner, fork, baseUrl) + "]";
    }

    static String twoRepositoriesJson(String name1, String owner1, boolean fork1,
                                       String name2, String owner2, boolean fork2,
                                       String baseUrl) {
        return "[" + repositoryJson(name1, owner1, fork1, baseUrl) + "," +
                repositoryJson(name2, owner2, fork2, baseUrl) + "]";
    }

    static String branchJson(String name, String sha) {
        return """
                {
                  "name": "%s",
                  "commit": {
                    "sha": "%s"
                  }
                }
                """.formatted(name, sha);
    }

    static String branchesJson(String name1, String sha1, String name2, String sha2) {
        return "[" + branchJson(name1, sha1) + "," + branchJson(name2, sha2) + "]";
    }

    static String singleBranchJson(String name, String sha) {
        return "[" + branchJson(name, sha) + "]";
    }

    static String userNotFoundJson() {
        return """
                {
                  "message": "Not Found"
                }
                """;
    }

    static String emptyArrayJson() {
        return "[]";
    }
}
