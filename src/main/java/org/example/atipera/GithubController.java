package org.example.atipera;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Validated
public class GithubController {

    private final GithubService githubService;

    @GetMapping("/{username}/repositories")
    public ResponseEntity<List<Repository>> getUserRepositories(
            @PathVariable @NotBlank(message = "Username cannot be empty") String username) {
        var repositories = githubService.getUserRepositories(username);
        return ResponseEntity.ok(repositories);
    }
}
