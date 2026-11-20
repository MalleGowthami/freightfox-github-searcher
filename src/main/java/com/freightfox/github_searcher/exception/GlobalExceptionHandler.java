
package com.freightfox.github_searcher.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handle invalid input
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(
            IllegalArgumentException exception) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(exception.getMessage());
    }

    // Handle GitHub API errors
    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<String> handleGitHubApiException(
            RestClientResponseException exception) {

        if (exception.getStatusCode().value() == 403) {
            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("GitHub API rate limit may have been exceeded "
                            + "or access was denied.");
        }

        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body("GitHub API request failed with status: "
                        + exception.getStatusCode().value());
    }

    // Handle network connection errors
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<String> handleNetworkException(
            ResourceAccessException exception) {

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Unable to connect to GitHub API. "
                        + "Please check your internet connection.");
    }
}
