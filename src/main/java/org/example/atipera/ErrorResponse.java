package org.example.atipera;

public record ErrorResponse(
        int status,
        String message
) {
}
