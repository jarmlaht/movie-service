package com.vinsguru.netflux.movie;

import com.vinsguru.netflux.movie.dto.MovieDetails;
import com.vinsguru.netflux.movie.dto.MovieSummary;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ProblemDetail;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
@AutoConfigureRestTestClient
@Sql(scripts = "/sql/insert-movies.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/delete-movies.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
class MovieApiTest {

    @Autowired
    RestTestClient restTestClient;

    // Get by ID

    @Test
    @DisplayName("Should return full movie details by ID")
    void shouldReturnMovieDetails() {
        var movie = restTestClient.get()
                .uri("/api/movies/{id}", 1)
                .exchange()
                .expectStatus().isOk()
                .expectBody(MovieDetails.class)
                .returnResult()
                .getResponseBody();

        var expected = new MovieDetails(
                1L,
                "The Dark Knight",
                8.5,
                30000,
                LocalDate.of(2008, 7, 16),
                1004558444L,
                152,
                "/dark-knight-backdrop.jpg",
                185000000L,
                "https://www.thedarkknight.com",
                "Batman faces the Joker.",
                123.4,
                "/dark-knight-poster.jpg",
                List.of("Action", "Crime", "Drama"),
                List.of("Chaos", "Justice")
        );
        assertThat(movie).isEqualTo(expected);
    }

    @Test
    @DisplayName("Should return 404 ProblemDetail when ID does not exist")
    void shouldReturn404WhenNotFound() {
        var problem = restTestClient.get()
                .uri("/api/movies/{id}", 999)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(ProblemDetail.class)
                .returnResult()
                .getResponseBody();

        assertThat(problem.getStatus()).isEqualTo(404);
        assertThat(problem.getDetail()).isEqualTo("Movie not found: 999");
    }

    @Test
    @DisplayName("Should return 400 ProblemDetail when ID is not numeric")
    void shouldReturn400WhenIdNotNumeric() {
        assertBadRequest("/api/movies/abc");
    }

    // Batch

    @Test
    @DisplayName("Should return batch in request order, omitting unknown and duplicate IDs")
    void shouldReturnBatchInRequestOrder() {
        var ids = getIds("/api/movies/batch?ids=4,999,2,4");

        assertThat(ids).containsExactly(4L, 2L);
    }

    @Test
    @DisplayName("Should return movie summary fields in batch")
    void shouldReturnSummaryFields() {
        var movies = getSummaries("/api/movies/batch?ids=2");

        var expected = new MovieSummary(
                2L,
                "Inception",
                "/inception-poster.jpg",
                "/inception-backdrop.jpg",
                8.4,
                LocalDate.of(2010, 7, 15),
                List.of("Action", "Science Fiction")
        );
        assertThat(movies).containsExactly(expected);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/movies/batch", "/api/movies/batch?ids=", "/api/movies/batch?ids=1,abc"})
    @DisplayName("Should return 400 when batch IDs are missing, empty or not numeric")
    void shouldReturn400WhenBatchIdsInvalid(String uri) {
        assertBadRequest(uri);
    }

    @Test
    @DisplayName("Should return 400 when more than 100 batch IDs are requested")
    void shouldReturn400WhenTooManyBatchIds() {
        var ids = LongStream.rangeClosed(1, 101)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining(","));

        assertBadRequest("/api/movies/batch?ids=" + ids);
    }

    // Popular

    @Test
    @DisplayName("Should return popular movies by vote count with ID tie-breaker and nulls last")
    void shouldReturnPopularMovies() {
        var ids = getIds("/api/movies/popular");

        assertThat(ids).containsExactly(2L, 3L, 1L, 4L, 7L, 5L, 6L);
    }

    @Test
    @DisplayName("Should limit popular movies")
    void shouldLimitPopularMovies() {
        var ids = getIds("/api/movies/popular?limit=3");

        assertThat(ids).containsExactly(2L, 3L, 1L);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/movies/popular", "/api/movies/popular?limit=0", "/api/movies/popular?limit=-5"})
    @Sql(scripts = "/sql/insert-many-movies.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @DisplayName("Should use default limit of 20 when limit is omitted or below 1")
    void shouldUseDefaultLimit(String uri) {
        assertThat(getIds(uri)).hasSize(20);
    }

    @Test
    @Sql(scripts = "/sql/insert-many-movies.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @DisplayName("Should clamp limit to 100 when above maximum")
    void shouldClampLimitToMax() {
        assertThat(getIds("/api/movies/popular?limit=500")).hasSize(100);
    }

    @Test
    @DisplayName("Should return 400 when limit is not numeric")
    void shouldReturn400WhenLimitNotNumeric() {
        assertBadRequest("/api/movies/popular?limit=abc");
    }

    // Latest

    @Test
    @DisplayName("Should return latest movies excluding future and undated, with ID tie-breaker")
    void shouldReturnLatestMovies() {
        var ids = getIds("/api/movies/latest");

        assertThat(ids).containsExactly(3L, 2L, 1L, 4L, 7L);
    }

    @Test
    @DisplayName("Should limit latest movies")
    void shouldLimitLatestMovies() {
        var ids = getIds("/api/movies/latest?limit=2");

        assertThat(ids).containsExactly(3L, 2L);
    }

    // Search

    @Test
    @DisplayName("Should search by partial case-insensitive title")
    void shouldSearchByTitle() {
        var ids = getIds("/api/movies/search?title=KNIGHT");

        assertThat(ids).containsExactly(1L);
    }

    @Test
    @DisplayName("Should sort search results by vote count with nulls last")
    void shouldSortSearchResults() {
        var ids = getIds("/api/movies/search?title=movie");

        assertThat(ids).containsExactly(7L, 5L, 6L);
    }

    @Test
    @DisplayName("Should search by exact genre")
    void shouldSearchByGenre() {
        var ids = getIds("/api/movies/search?genre=Action");

        assertThat(ids).containsExactly(2L, 1L);
    }

    @ParameterizedTest
    @ValueSource(strings = {"action", "Act"})
    @DisplayName("Should not match genre by different case or partial value")
    void shouldNotMatchPartialOrDifferentCaseGenre(String genre) {
        var ids = getIds("/api/movies/search?genre=" + genre);

        assertThat(ids).isEmpty();
    }

    @Test
    @DisplayName("Should combine title and genre with AND")
    void shouldCombineSearchParams() {
        var ids = getIds("/api/movies/search?title=movie&genre=Drama");

        assertThat(ids).containsExactly(7L, 5L);
    }

    @Test
    @DisplayName("Should limit search results")
    void shouldLimitSearchResults() {
        var ids = getIds("/api/movies/search?genre=Drama&limit=2");

        assertThat(ids).containsExactly(3L, 1L);
    }

    @Test
    @DisplayName("Should ignore blank search param when another is present")
    void shouldIgnoreBlankSearchParam() {
        var ids = getIds("/api/movies/search?title={title}&genre=Action", " ");

        assertThat(ids).containsExactly(2L, 1L);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/movies/search", "/api/movies/search?limit=5"})
    @DisplayName("Should return 400 when no search param is provided")
    void shouldReturn400WhenNoSearchParam(String uri) {
        assertBadRequest(uri);
    }

    @Test
    @DisplayName("Should return 400 when all search params are blank")
    void shouldReturn400WhenSearchParamsBlank() {
        assertBadRequest("/api/movies/search?title={title}&genre=", " ");
    }

    private List<MovieSummary> getSummaries(String uri, Object... uriVariables) {
        return restTestClient.get()
                .uri(uri, uriVariables)
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<MovieSummary>>() {})
                .returnResult()
                .getResponseBody();
    }

    private List<Long> getIds(String uri, Object... uriVariables) {
        return getSummaries(uri, uriVariables).stream()
                .map(MovieSummary::id)
                .toList();
    }

    private void assertBadRequest(String uri, Object... uriVariables) {
        var problem = restTestClient.get()
                .uri(uri, uriVariables)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ProblemDetail.class)
                .returnResult()
                .getResponseBody();

        assertThat(problem.getStatus()).isEqualTo(400);
    }
}
