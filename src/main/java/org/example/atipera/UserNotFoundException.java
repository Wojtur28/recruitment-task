package org.example.atipera;

class UserNotFoundException extends RuntimeException {
    private static final String MESSAGE_TEMPLATE = "User '%s' not found";

    UserNotFoundException(String username) {
        super(MESSAGE_TEMPLATE.formatted(username));
    }
}
