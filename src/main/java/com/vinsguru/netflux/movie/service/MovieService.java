package com.vinsguru.netflux.movie.service;

import com.vinsguru.netflux.movie.dto.MovieDetails;
import com.vinsguru.netflux.movie.dto.MovieSummary;
import com.vinsguru.netflux.movie.dto.SearchRequest;
import com.vinsguru.netflux.movie.entity.Movie;
import com.vinsguru.netflux.movie.exceptions.MovieNotFoundException;
import com.vinsguru.netflux.movie.mapper.MovieMapper;
import com.vinsguru.netflux.movie.repository.MovieRepository;
import com.vinsguru.netflux.movie.repository.MovieSpecifications;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MovieService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;
    private static final Sort BY_VOTE_COUNT = Sort.by(Sort.Order.desc("voteCount").nullsLast(), Sort.Order.asc("id"));
    private static final Sort BY_RELEASE_DATE = Sort.by(Sort.Order.desc("releaseDate"), Sort.Order.asc("id"));

    private final MovieRepository repository;

    public MovieService(MovieRepository repository) {
        this.repository = repository;
    }

    public MovieDetails findById(Long id) {
        return repository.findById(id)
                .map(MovieMapper::toDetails)
                .orElseThrow(() -> new MovieNotFoundException(id));
    }

    // distinct ids in request order, unknown ids omitted
    public List<MovieSummary> findByIds(List<Long> ids) {
        var distinctIds = ids.stream()
                .distinct()
                .toList();
        var moviesById = repository.findAllById(distinctIds)
                .stream()
                .collect(Collectors.toMap(Movie::getId, Function.identity()));
        return distinctIds.stream()
                .filter(moviesById::containsKey)
                .map(moviesById::get)
                .map(MovieMapper::toSummary)
                .toList();
    }

    public List<MovieSummary> findPopular(Integer limit) {
        return find(Specification.unrestricted(), BY_VOTE_COUNT, limit);
    }

    public List<MovieSummary> findLatest(Integer limit) {
        return find(MovieSpecifications.releasedOnOrBefore(LocalDate.now()), BY_RELEASE_DATE, limit);
    }

    public List<MovieSummary> search(SearchRequest request) {
        var spec = MovieSpecifications.titleContains(request.title())
                .and(MovieSpecifications.hasGenre(request.genre()));
        return find(spec, BY_VOTE_COUNT, request.limit());
    }

    private List<MovieSummary> find(Specification<Movie> spec, Sort sort, Integer limit) {
        return repository.findBy(spec, query -> query.sortBy(sort).limit(resolveLimit(limit)).all())
                .stream()
                .map(MovieMapper::toSummary)
                .toList();
    }

    // null or < 1 -> default, > max -> max
    private static int resolveLimit(Integer limit) {
        if (Objects.isNull(limit) || limit < 1) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }
}
