package com.vinsguru.netflux.movie.dto;

import jakarta.validation.constraints.AssertTrue;

import java.util.Objects;

public record SearchRequest(String title,
                            String genre,
                            Integer limit) {

    public SearchRequest {
        title = blankToNull(title);
        genre = blankToNull(genre);
    }

    @AssertTrue(message = "At least one of title or genre is required")
    public boolean isAtLeastOnePresent() {
        return Objects.nonNull(title) || Objects.nonNull(genre);
    }

    private static String blankToNull(String value) {
        return Objects.isNull(value) || value.isBlank() ? null : value;
    }
}
