create database ProyectoIntegrador;
use ProyectoIntegrador;

-- 1. TABLAS INDEPENDIENTES (MAESTRAS)

CREATE TABLE pais (
    country_id INT PRIMARY KEY,
    name VARCHAR(100)
);

CREATE TABLE idioma (
    language_id INT PRIMARY KEY,
    name VARCHAR(100),
    iso_code VARCHAR(10)
);

CREATE TABLE genero (
    genre_id INT PRIMARY KEY,
    name VARCHAR(100)
);

CREATE TABLE palabra_clave (
    keyword_id INT PRIMARY KEY,
    name VARCHAR(100)
);

CREATE TABLE actor (
    actor_id INT PRIMARY KEY,
    name VARCHAR(255)
);

CREATE TABLE equipo_tecnico (
    crew_id INT PRIMARY KEY,
    name VARCHAR(255)
);

CREATE TABLE compania_productora (
    company_id INT PRIMARY KEY,
    name VARCHAR(255)
);

CREATE TABLE coleccion (
    collection_id INT PRIMARY KEY,
    name VARCHAR(255)
);

-- 2. TABLA PRINCIPAL

CREATE TABLE pelicula (
    id INT PRIMARY KEY,
    title VARCHAR(255),
    original_title VARCHAR(255),
    original_language VARCHAR(10),
    overview TEXT,
    release_date DATE,
    runtime INT,
    budget BIGINT,
    revenue BIGINT,
    popularity DECIMAL(10,2),
    vote_average DECIMAL(3,1),
    vote_count INT,
    status VARCHAR(50),
    tagline VARCHAR(255),
    homepage VARCHAR(255),
    imdb_id VARCHAR(20),
    adult TINYINT(1),
    video TINYINT(1),
    poster_path VARCHAR(255),
    collection_id INT,
    CONSTRAINT fk_pelicula_coleccion FOREIGN KEY (collection_id) REFERENCES coleccion(collection_id)
);

-- 3. TABLAS DE RELACIÓN (INTERMEDIAS Y DEPENDIENTES)

CREATE TABLE pelicula_idioma (
    movie_id INT,
    language_id INT,
    PRIMARY KEY (movie_id, language_id),
    CONSTRAINT fk_idioma_movie FOREIGN KEY (movie_id) REFERENCES pelicula(id),
    CONSTRAINT fk_idioma_lang FOREIGN KEY (language_id) REFERENCES idioma(language_id)
);

CREATE TABLE pelicula_pais (
    movie_id INT,
    country_id INT,
    PRIMARY KEY (movie_id, country_id),
    CONSTRAINT fk_pais_movie FOREIGN KEY (movie_id) REFERENCES pelicula(id),
    CONSTRAINT fk_pais_country FOREIGN KEY (country_id) REFERENCES pais(country_id)
);

CREATE TABLE pelicula_genero (
    movie_id INT,
    genre_id INT,
    PRIMARY KEY (movie_id, genre_id),
    CONSTRAINT fk_genero_movie FOREIGN KEY (movie_id) REFERENCES pelicula(id),
    CONSTRAINT fk_genero_id FOREIGN KEY (genre_id) REFERENCES genero(genre_id)
);

CREATE TABLE pelicula_palabra_clave (
    movie_id INT,
    keyword_id INT,
    PRIMARY KEY (movie_id, keyword_id),
    CONSTRAINT fk_kw_movie FOREIGN KEY (movie_id) REFERENCES pelicula(id),
    CONSTRAINT fk_kw_id FOREIGN KEY (keyword_id) REFERENCES palabra_clave(keyword_id)
);

CREATE TABLE pelicula_compania (
    movie_id INT,
    company_id INT,
    PRIMARY KEY (movie_id, company_id),
    CONSTRAINT fk_comp_movie FOREIGN KEY (movie_id) REFERENCES pelicula(id),
    CONSTRAINT fk_comp_id FOREIGN KEY (company_id) REFERENCES compania_productora(company_id)
);

CREATE TABLE pelicula_actor (
    movie_id INT,
    actor_id INT,
    character_name VARCHAR(255),
    PRIMARY KEY (movie_id, actor_id),
    CONSTRAINT fk_actor_movie FOREIGN KEY (movie_id) REFERENCES pelicula(id),
    CONSTRAINT fk_actor_id FOREIGN KEY (actor_id) REFERENCES actor(actor_id)
);

CREATE TABLE pelicula_equipo_tecnico (
    movie_id INT,
    crew_id INT,
    job VARCHAR(100),
    department VARCHAR(100),
    PRIMARY KEY (movie_id, crew_id),
    CONSTRAINT fk_equipo_movie FOREIGN KEY (movie_id) REFERENCES pelicula(id),
    CONSTRAINT fk_equipo_id FOREIGN KEY (crew_id) REFERENCES equipo_tecnico(crew_id)
);

CREATE TABLE rating (
    rating_id INT PRIMARY KEY,
    source VARCHAR(100),
    value DECIMAL(3,1),
    movie_id INT,
    CONSTRAINT fk_rating_movie FOREIGN KEY (movie_id) REFERENCES pelicula(id)
);
