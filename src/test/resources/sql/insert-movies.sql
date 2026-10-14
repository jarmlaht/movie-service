INSERT INTO movie (id, title, vote_average, vote_count, release_date, revenue, runtime, backdrop_path, budget, homepage, overview, popularity, poster_path, genres, themes)
VALUES
    (1, 'The Dark Knight', 8.5, 30000, '2008-07-16', 1004558444, 152, '/dark-knight-backdrop.jpg', 185000000, 'https://www.thedarkknight.com', 'Batman faces the Joker.', 123.4, '/dark-knight-poster.jpg', ARRAY['Action', 'Crime', 'Drama'], ARRAY['Chaos', 'Justice']),
    (2, 'Inception', 8.4, 35000, '2010-07-15', 836836967, 148, '/inception-backdrop.jpg', 160000000, 'https://www.inception.com', 'A thief steals secrets through dreams.', 110.2, '/inception-poster.jpg', ARRAY['Action', 'Science Fiction'], ARRAY['Dreams']),
    (3, 'Interstellar', 8.4, 32000, '2014-11-05', 701729206, 169, '/interstellar-backdrop.jpg', 165000000, 'https://www.interstellar.com', 'Explorers travel through a wormhole.', 140.8, '/interstellar-poster.jpg', ARRAY['Adventure', 'Drama', 'Science Fiction'], ARRAY['Space', 'Time']),
    (4, 'Pulp Fiction', 8.5, 27000, '1994-09-10', 213928762, 154, '/pulp-fiction-backdrop.jpg', 8000000, 'https://www.pulpfiction.com', 'Intertwined crime stories.', 80.1, '/pulp-fiction-poster.jpg', ARRAY['Thriller', 'Crime'], ARRAY['Crime']),
    (5, 'Upcoming Movie', NULL, 10, '2999-01-01', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, ARRAY['Drama'], ARRAY[]::text[]),
    (6, 'Undated Movie', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, ARRAY['Comedy'], ARRAY[]::text[]),
    (7, 'Tie Movie', 7.0, 27000, '1994-09-10', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, ARRAY['Drama'], ARRAY[]::text[]);
