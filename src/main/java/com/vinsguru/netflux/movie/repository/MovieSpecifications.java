package com.vinsguru.netflux.movie.repository;

import com.vinsguru.netflux.movie.entity.Movie;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Objects;

public class MovieSpecifications {

    public static Specification<Movie> titleContains(String title) {
        return (root, query, cb) ->
                Objects.isNull(title) ? cb.conjunction() :
                cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    }

    public static Specification<Movie> hasGenre(String genre) {
        return (root, query, cb) ->
                Objects.isNull(genre) ? cb.conjunction() :
                cb.greaterThan(cb.function("array_position", Integer.class, root.get("genres"), cb.literal(genre)), 0);
    }

    // excludes null and future release dates
    public static Specification<Movie> releasedOnOrBefore(LocalDate date) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("releaseDate"), date);
    }
}
