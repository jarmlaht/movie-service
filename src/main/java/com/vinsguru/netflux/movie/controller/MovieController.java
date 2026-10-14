package com.vinsguru.netflux.movie.controller;

import com.vinsguru.netflux.movie.dto.MovieDetails;
import com.vinsguru.netflux.movie.dto.MovieSummary;
import com.vinsguru.netflux.movie.dto.SearchRequest;
import com.vinsguru.netflux.movie.service.MovieService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService service;

    public MovieController(MovieService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public MovieDetails getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping("/batch")
    public List<MovieSummary> getByIds(@RequestParam @NotEmpty @Size(max = 100) List<@NotNull Long> ids) {
        return service.findByIds(ids);
    }

    @GetMapping("/popular")
    public List<MovieSummary> getPopular(@RequestParam(required = false) Integer limit) {
        return service.findPopular(limit);
    }

    @GetMapping("/latest")
    public List<MovieSummary> getLatest(@RequestParam(required = false) Integer limit) {
        return service.findLatest(limit);
    }

    @GetMapping("/search")
    public List<MovieSummary> search(@Valid SearchRequest request) {
        return service.search(request);
    }
}
