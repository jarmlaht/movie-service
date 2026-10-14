INSERT INTO movie (id, title, vote_count, release_date, genres)
SELECT g, 'Generated Movie ' || g, g, DATE '2000-01-01' + g, ARRAY['Drama']
FROM generate_series(1001, 1110) AS g;
