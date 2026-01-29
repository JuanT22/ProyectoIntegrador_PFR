package loader

import cats.effect.IO
import cats.syntax.all._
import doobie._
import doobie.implicits._
import doobie.implicits.javasql._
import models._

class ProyectoIntegradorLoader(xa: Transactor[IO]) {

  // ==================== INSERTAR TABLAS MAESTRAS ====================

  def insertGenero(genres: List[Genre]): ConnectionIO[Int] = {
    if (genres.isEmpty) 0.pure[ConnectionIO]
    else Update[(Int, String)](
      "INSERT INTO genero (genre_id, name) VALUES (?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name)"
    ).updateMany(genres.map(g => (g.id.toInt, g.name.take(100))))
  }

  def insertProductora(companies: List[ProductionCompany]): ConnectionIO[Int] = {
    if (companies.isEmpty) 0.pure[ConnectionIO]
    else Update[(Int, String)](
      "INSERT INTO compania_productora (company_id, name) VALUES (?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name)"
    ).updateMany(companies.map(c => (c.id.toInt, c.name.take(255))))
  }

  def insertPalabraClave(keywords: List[Keyword]): ConnectionIO[Int] = {
    if (keywords.isEmpty) 0.pure[ConnectionIO]
    else Update[(Int, String)](
      "INSERT INTO palabra_clave (keyword_id, name) VALUES (?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name)"
    ).updateMany(keywords.map(k => (k.id.toInt, k.name.take(100))))
  }

  def insertPais(countries: List[ProductionCountry]): ConnectionIO[Int] = {
    if (countries.isEmpty) 0.pure[ConnectionIO]
    else Update[(Int, String)](
      "INSERT INTO pais (country_id, name) VALUES (?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name)"
    ).updateMany(countries.map(c => (c.id.toInt, c.name.take(100))))
  }

  def insertIdioma(languages: List[SpokenLanguage]): ConnectionIO[Int] = {
    if (languages.isEmpty) 0.pure[ConnectionIO]
    else Update[(Int, String, String)](
      "INSERT INTO idioma (language_id, name, iso_code) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name), iso_code=VALUES(iso_code)"
    ).updateMany(languages.map(l => (l.iso.toInt, l.name.take(100), l.iso_639_1.take(10))))
  }

  def insertColeccion(collections: List[Collection]): ConnectionIO[Int] = {
    if (collections.isEmpty) 0.pure[ConnectionIO]
    else Update[(Int, String)](
      "INSERT INTO coleccion (collection_id, name) VALUES (?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name)"
    ).updateMany(collections.map(c => (c.id.toInt, c.name.take(255))))
  }

  def insertActor(cast: List[CastMember]): ConnectionIO[Int] = {
    if (cast.isEmpty) 0.pure[ConnectionIO]
    else Update[(Int, String)](
      "INSERT INTO actor (actor_id, name) VALUES (?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name)"
    ).updateMany(cast.map(c => (c.id.toInt, c.name.take(255))))
  }

  def insertEquipoTecnico(crew: List[CrewMember]): ConnectionIO[Int] = {
    if (crew.isEmpty) 0.pure[ConnectionIO]
    else Update[(Int, String)](
      "INSERT INTO equipo_tecnico (crew_id, name) VALUES (?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name)"
    ).updateMany(crew.map(c => (c.id.toInt, c.name.take(255))))
  }

  // ==================== INSERTAR PELÍCULA ====================

  def insertPelicula(m: MovieBase, collectionId: Option[Int]): ConnectionIO[Int] = {
    sql"""INSERT INTO pelicula (
      id, title, original_title, original_language, overview,
      release_date, runtime, budget, revenue, popularity, vote_average, vote_count,
      status, tagline, homepage, imdb_id, adult, video, poster_path, collection_id
    ) VALUES (
      ${m.movieId}, ${m.title.take(255)}, ${m.originalTitle.take(255)}, ${m.originalLanguage.take(10)}, ${m.overview},
      ${m.releaseDate}, ${m.runtime}, ${m.budget}, ${m.revenue},
      ${m.popularity}, ${m.voteAverage}, ${m.voteCount}, ${m.status.take(50)},
      ${m.tagline.map(_.take(255))}, ${m.homepage.map(_.take(255))}, ${m.imdbId.take(20)},
      ${if (m.adult) 1 else 0}, ${if (m.video) 1 else 0}, ${m.posterPath.map(_.take(255))}, $collectionId
    ) ON DUPLICATE KEY UPDATE title=VALUES(title), vote_average=VALUES(vote_average)
    """.update.run
  }

  // ==================== INSERTAR RELACIONES ====================

  def insertPeliculaGenero(movieId: Int, genres: List[Genre]): ConnectionIO[Int] = {
    if (genres.isEmpty) 0.pure[ConnectionIO]
    else Update[(Int, Int)](
      "INSERT INTO pelicula_genero (movie_id, genre_id) VALUES (?, ?) ON DUPLICATE KEY UPDATE genre_id=VALUES(genre_id)"
    ).updateMany(genres.map(g => (movieId, g.id.toInt)))
  }

  def insertPeliculaCompania(movieId: Int, companies: List[ProductionCompany]): ConnectionIO[Int] = {
    if (companies.isEmpty) 0.pure[ConnectionIO]
    else Update[(Int, Int)](
      "INSERT INTO pelicula_compania (movie_id, company_id) VALUES (?, ?) ON DUPLICATE KEY UPDATE company_id=VALUES(company_id)"
    ).updateMany(companies.map(c => (movieId, c.id.toInt)))
  }

  def insertPeliculaPalabraClave(movieId: Int, keywords: List[Keyword]): ConnectionIO[Int] = {
    if (keywords.isEmpty) 0.pure[ConnectionIO]
    else Update[(Int, Int)](
      "INSERT INTO pelicula_palabra_clave (movie_id, keyword_id) VALUES (?, ?) ON DUPLICATE KEY UPDATE keyword_id=VALUES(keyword_id)"
    ).updateMany(keywords.map(k => (movieId, k.id.toInt)))
  }

  def insertPeliculaIdioma(movieId: Int, languages: List[SpokenLanguage]): ConnectionIO[Int] = {
    if (languages.isEmpty) 0.pure[ConnectionIO]
    else Update[(Int, Int)](
      "INSERT INTO pelicula_idioma (movie_id, language_id) VALUES (?, ?) ON DUPLICATE KEY UPDATE language_id=VALUES(language_id)"
    ).updateMany(languages.map(l => (movieId, l.iso.toInt)))
  }

  def insertPeliculaPais(movieId: Int, countries: List[ProductionCountry]): ConnectionIO[Int] = {
    if (countries.isEmpty) 0.pure[ConnectionIO]
    else Update[(Int, Int)](
      "INSERT INTO pelicula_pais (movie_id, country_id) VALUES (?, ?) ON DUPLICATE KEY UPDATE country_id=VALUES(country_id)"
    ).updateMany(countries.map(c => (movieId, c.id.toInt)))
  }

  def insertPeliculaActor(movieId: Int, cast: List[CastMember]): ConnectionIO[Int] = {
    if (cast.isEmpty) 0.pure[ConnectionIO]
    else {
      Update[(Int, Int, Option[String])](
        """INSERT INTO pelicula_actor (movie_id, actor_id, character_name)
           VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE character_name=VALUES(character_name)"""
      ).updateMany(cast.map(c => (
        movieId,
        c.id.toInt,
        c.character.map(_.take(255))
      )))
    }
  }

  def insertPeliculaEquipoTecnico(movieId: Int, crew: List[CrewMember]): ConnectionIO[Int] = {
    if (crew.isEmpty) 0.pure[ConnectionIO]
    else {
      // CORREGIDO: Usar "department" (como en tu script SQL), NO "departamento"
      val crewWithJob = crew.filter(_.job.isDefined)

      if (crewWithJob.isEmpty) 0.pure[ConnectionIO]
      else Update[(Int, Int, String, Option[String])](
        """INSERT INTO pelicula_equipo_tecnico (movie_id, crew_id, job, department)
           VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE job=VALUES(job), department=VALUES(department)"""
      ).updateMany(crewWithJob.map(c => (
        movieId,
        c.id.toInt,
        c.job.get.take(100),  // Sabemos que existe porque filtramos
        c.department.map(_.take(100))
      )))
    }
  }

  def insertRating(movieId: Int, ratings: List[Rating]): ConnectionIO[Int] = {
    if (ratings.isEmpty) 0.pure[ConnectionIO]
    else {
      // Según tu script SQL: rating(rating_id, source, value, movie_id)
      // Generamos rating_id único basado en movieId + userId
      val ratingsWithId = ratings.zipWithIndex.map { case (r, idx) =>
        val ratingId = (movieId * 100000 + r.userId).toInt
        (ratingId, s"user_${r.userId}", r.rating, movieId)
      }

      Update[(Int, String, Double, Int)](
        """INSERT INTO rating (rating_id, source, value, movie_id)
           VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE value=VALUES(value)"""
      ).updateMany(ratingsWithId)
    }
  }

  // ==================== CARGAR PELÍCULA COMPLETA ====================

  def loadMovie(mc: MovieComplete): ConnectionIO[Unit] = {
    for {
      // 1. Insertar tablas maestras (EXACTAMENTE como en tu script SQL)
      _ <- insertGenero(mc.genres)
      _ <- insertProductora(mc.companies)
      _ <- insertPalabraClave(mc.keywords)
      _ <- insertPais(mc.country)
      _ <- insertIdioma(mc.spoken_lenguages)
      _ <- insertColeccion(mc.collections)
      _ <- insertActor(mc.cast)
      _ <- insertEquipoTecnico(mc.crew)

      // 2. Insertar película
      collectionId = mc.collections.headOption.map(_.id.toInt)
      _ <- insertPelicula(mc.movie, collectionId)

      // 3. Insertar relaciones (EXACTAMENTE como en tu script SQL)
      _ <- insertPeliculaGenero(mc.movie.movieId, mc.genres)
      _ <- insertPeliculaCompania(mc.movie.movieId, mc.companies)
      _ <- insertPeliculaPalabraClave(mc.movie.movieId, mc.keywords)
      _ <- insertPeliculaIdioma(mc.movie.movieId, mc.spoken_lenguages)
      _ <- insertPeliculaPais(mc.movie.movieId, mc.country)
      _ <- insertPeliculaActor(mc.movie.movieId, mc.cast)
      _ <- insertPeliculaEquipoTecnico(mc.movie.movieId, mc.crew)
      _ <- insertRating(mc.movie.movieId, mc.ratings)
    } yield ()
  }

  // ==================== CARGAR LOTE ====================

  def loadBatch(movies: List[MovieComplete]): IO[Int] = {
    movies.traverse(loadMovie).transact(xa).as(movies.size)
  }
}