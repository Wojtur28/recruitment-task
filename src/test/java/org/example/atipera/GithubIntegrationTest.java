package org.example.atipera;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GithubIntegrationTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @LocalServerPort
    private int port;

    private RestClient restClient;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("github.api.base-url", wireMock::baseUrl);
    }

    @BeforeEach
    void setUp() {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    void shouldReturnRepositoriesWithoutForks() {
        wireMock.stubFor(get(urlEqualTo("/users/testuser/repos"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(GithubTestFixtures.twoRepositoriesJson(
                                "my-repo", "testuser", false,
                                "forked-repo", "testuser", true,
                                wireMock.baseUrl()))));

        wireMock.stubFor(get(urlEqualTo("/repos/testuser/my-repo/branches"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(GithubTestFixtures.branchesJson(
                                "main", "abc123def456",
                                "develop", "xyz789uvw012"))));

        ResponseEntity<List<Repository>> response = restClient.get()
                .uri(GithubTestFixtures.userRepositoriesUrl("testuser"))
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .hasSize(1)
                .extracting(Repository::repositoryName)
                .containsExactly("my-repo");
        assertThat(response.getBody().getFirst().branches())
                .hasSize(2)
                .extracting("name")
                .containsExactly("main", "develop");

        wireMock.verify(1, getRequestedFor(urlEqualTo("/users/testuser/repos")));
        wireMock.verify(1, getRequestedFor(urlEqualTo("/repos/testuser/my-repo/branches")));
        wireMock.verify(0, getRequestedFor(urlEqualTo("/repos/testuser/forked-repo/branches")));
    }

    @Test
    void shouldReturn404WhenUserNotFound() {
        wireMock.stubFor(get(urlEqualTo("/users/nonexistent/repos"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody(GithubTestFixtures.userNotFoundJson())));

        ResponseEntity<String> response = restClient.get()
                .uri(GithubTestFixtures.userRepositoriesUrl("nonexistent"))
                .retrieve()
                .onStatus(status -> status.value() == 404, (_, _) -> {})
                .toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody())
                .contains("\"status\":404")
                .contains("User 'nonexistent' not found");

        wireMock.verify(1, getRequestedFor(urlEqualTo("/users/nonexistent/repos")));
    }

    @Test
    void shouldReturnEmptyListWhenAllReposAreForks() {
        wireMock.stubFor(get(urlEqualTo("/users/forker/repos"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(GithubTestFixtures.twoRepositoriesJson(
                                "forked-repo-1", "forker", true,
                                "forked-repo-2", "forker", true,
                                wireMock.baseUrl()))));

        ResponseEntity<List<Repository>> response = restClient.get()
                .uri(GithubTestFixtures.userRepositoriesUrl("forker"))
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();

        wireMock.verify(1, getRequestedFor(urlEqualTo("/users/forker/repos")));
        wireMock.verify(0, getRequestedFor(urlPathMatching("/repos/forker/.*/branches")));
    }

    @Test
    void shouldHandleRepositoryWithNoBranches() {
        wireMock.stubFor(get(urlEqualTo("/users/testuser/repos"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(GithubTestFixtures.repositoriesJson(
                                "empty-repo", "testuser", false, wireMock.baseUrl()))));

        wireMock.stubFor(get(urlEqualTo("/repos/testuser/empty-repo/branches"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(GithubTestFixtures.emptyArrayJson())));

        ResponseEntity<List<Repository>> response = restClient.get()
                .uri(GithubTestFixtures.userRepositoriesUrl("testuser"))
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .hasSize(1)
                .first()
                .satisfies(repo -> {
                    assertThat(repo.repositoryName()).isEqualTo("empty-repo");
                    assertThat(repo.ownerLogin()).isEqualTo("testuser");
                    assertThat(repo.branches()).isEmpty();
                });

        wireMock.verify(1, getRequestedFor(urlEqualTo("/users/testuser/repos")));
        wireMock.verify(1, getRequestedFor(urlEqualTo("/repos/testuser/empty-repo/branches")));
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoRepositories() {
        wireMock.stubFor(get(urlEqualTo("/users/newuser/repos"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(GithubTestFixtures.emptyArrayJson())));

        ResponseEntity<List<Repository>> response = restClient.get()
                .uri(GithubTestFixtures.userRepositoriesUrl("newuser"))
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().isEmpty();

        wireMock.verify(1, getRequestedFor(urlEqualTo("/users/newuser/repos")));
        wireMock.verify(0, getRequestedFor(urlPathMatching("/repos/.*/branches")));
    }

    @Test
    void shouldReturn400WhenUsernameIsEmpty() {
        ResponseEntity<String> response = restClient.get()
                .uri(GithubTestFixtures.userRepositoriesUrl(" "))
                .retrieve()
                .onStatus(status -> status.value() == 400, (_, _) -> {})
                .toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .contains("\"status\":400");

        wireMock.verify(0, getRequestedFor(urlPathMatching("/.*")));
    }
}
