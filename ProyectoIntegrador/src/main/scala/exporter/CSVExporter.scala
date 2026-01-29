package exporter

import cats.effect.{IO, Ref}
import cats.syntax.all._
import models._
import fs2.{Stream, text}
import fs2.io.file.{Files, Path, Flags}

class CSVExporter(outputDir: String) {

  private val baseDir = Path(outputDir)

  // USAR ; COMO DELIMITADOR PARA EXCEL
  private val delimiter = ";"

  // ==================== CREAR DIRECTORIO DE SALIDA ====================
  private def ensureOutputDir(): IO[Unit] = {
    Files[IO].createDirectories(baseDir)
  }

  // ==================== ESCAPAR VALORES CSV ====================
  private def escapeCSV(value: String): String = {
    val cleaned = if (value == null) "" else value
    // Escapar con ; como delimitador
    if (cleaned.contains(delimiter) || cleaned.contains("\"") || cleaned.contains("\n")) {
      "\"" + cleaned.replace("\"", "\"\"") + "\""
    } else {
      cleaned
    }
  }

  private def escapeOpt(opt: Option[String]): String =
    escapeCSV(opt.getOrElse(""))

  private def escapeLong(opt: Option[Long]): String =
    opt.map(_.toString).getOrElse("")

  private def escapeInt(opt: Option[Int]): String =
    opt.map(_.toString).getOrElse("")

  private def escapeDate(opt: Option[java.sql.Date]): String =
    opt.map(_.toString).getOrElse("")

  // ==================== ESCRITURA OPTIMIZADA CON BUFFER ====================
  private def writeToFile(filePath: Path, header: String, dataStream: Stream[IO, String]): IO[Long] = {
    val headerStream = Stream.emit(header + "\n")
    val fullStream = headerStream ++ dataStream.map(_ + "\n")

    fullStream
      .through(text.utf8.encode)
      .through(Files[IO].writeAll(filePath, Flags.Write))
      .compile
      .count
  }

  // ==================== EXPORTAR MOVIES ====================
  def exportMovies(movies: Stream[IO, MovieComplete]): IO[Long] = {
    val filePath = baseDir / "movies.csv"
    val header = List(
      "movie_id", "title", "original_title", "original_language", "budget", "revenue", "runtime",
      "popularity", "vote_average", "vote_count", "release_date", "status", "adult", "homepage",
      "imdb_id", "poster_path", "tagline", "overview", "video"
    ).mkString(delimiter)

    val dataStream = movies.map { mc =>
      val m = mc.movie
      List(
        m.movieId.toString,
        escapeCSV(m.title),
        escapeCSV(m.originalTitle),
        escapeCSV(m.originalLanguage),
        escapeLong(m.budget),
        escapeLong(m.revenue),
        escapeInt(m.runtime),
        m.popularity.toString,
        m.voteAverage.toString,
        escapeInt(m.voteCount),
        escapeDate(m.releaseDate),
        escapeCSV(m.status),
        m.adult.toString,
        escapeOpt(m.homepage),
        escapeCSV(m.imdbId),
        escapeOpt(m.posterPath),
        escapeOpt(m.tagline),
        escapeCSV(m.overview),
        m.video.toString
      ).mkString(delimiter)
    }

    writeToFile(filePath, header, dataStream)
  }

  // ==================== EXPORTAR GENRES ====================
  def exportGenres(movies: Stream[IO, MovieComplete]): IO[Long] = {
    val filePath = baseDir / "movie_genres.csv"
    val header = List("movie_id", "genre_id", "genre_name").mkString(delimiter)

    val dataStream = movies.flatMap { mc =>
      Stream.emits(mc.genres.map { g =>
        List(mc.movie.movieId.toString, g.id.toString, escapeCSV(g.name)).mkString(delimiter)
      })
    }

    writeToFile(filePath, header, dataStream)
  }

  // ==================== EXPORTAR COLLECTIONS ====================
  def exportCollections(movies: Stream[IO, MovieComplete]): IO[Long] = {
    val filePath = baseDir / "movie_collections.csv"
    val header = List("movie_id", "collection_id", "collection_name", "poster_path", "backdrop_path").mkString(delimiter)

    val dataStream = movies.flatMap { mc =>
      Stream.emits(mc.collections.map { c =>
        List(
          mc.movie.movieId.toString,
          c.id.toString,
          escapeCSV(c.name),
          escapeCSV(c.poster_path),
          escapeCSV(c.backdrop_path)
        ).mkString(delimiter)
      })
    }

    writeToFile(filePath, header, dataStream)
  }

  // ==================== EXPORTAR COMPANIES ====================
  def exportCompanies(movies: Stream[IO, MovieComplete]): IO[Long] = {
    val filePath = baseDir / "movie_companies.csv"
    val header = List("movie_id", "company_id", "company_name").mkString(delimiter)

    val dataStream = movies.flatMap { mc =>
      Stream.emits(mc.companies.map { c =>
        List(mc.movie.movieId.toString, c.id.toString, escapeCSV(c.name)).mkString(delimiter)
      })
    }

    writeToFile(filePath, header, dataStream)
  }

  // ==================== EXPORTAR COUNTRIES ====================
  def exportCountries(movies: Stream[IO, MovieComplete]): IO[Long] = {
    val filePath = baseDir / "movie_countries.csv"
    val header = List("movie_id", "country_id", "country_name").mkString(delimiter)

    val dataStream = movies.flatMap { mc =>
      Stream.emits(mc.country.map { c =>
        List(mc.movie.movieId.toString, c.id.toString, escapeCSV(c.name)).mkString(delimiter)
      })
    }

    writeToFile(filePath, header, dataStream)
  }

  // ==================== EXPORTAR LANGUAGES ====================
  def exportLanguages(movies: Stream[IO, MovieComplete]): IO[Long] = {
    val filePath = baseDir / "movie_languages.csv"
    val header = List("movie_id", "language_iso", "language_name").mkString(delimiter)

    val dataStream = movies.flatMap { mc =>
      Stream.emits(mc.spoken_lenguages.map { l =>
        List(mc.movie.movieId.toString, l.iso.toString, escapeCSV(l.name)).mkString(delimiter)
      })
    }

    writeToFile(filePath, header, dataStream)
  }

  // ==================== EXPORTAR KEYWORDS ====================
  def exportKeywords(movies: Stream[IO, MovieComplete]): IO[Long] = {
    val filePath = baseDir / "movie_keywords.csv"
    val header = List("movie_id", "keyword_id", "keyword_name").mkString(delimiter)

    val dataStream = movies.flatMap { mc =>
      Stream.emits(mc.keywords.map { k =>
        List(mc.movie.movieId.toString, k.id.toString, escapeCSV(k.name)).mkString(delimiter)
      })
    }

    writeToFile(filePath, header, dataStream)
  }

  // ==================== EXPORTAR CAST ====================
  def exportCast(movies: Stream[IO, MovieComplete]): IO[Long] = {
    val filePath = baseDir / "movie_cast.csv"
    val header = List(
      "movie_id", "person_id", "person_name", "character", "order",
      "gender", "cast_id", "credit_id", "profile_path"
    ).mkString(delimiter)

    val dataStream = movies.flatMap { mc =>
      Stream.emits(mc.cast.map { c =>
        List(
          mc.movie.movieId.toString,
          c.id.toString,
          escapeCSV(c.name),
          escapeOpt(c.character),
          escapeInt(c.order),
          escapeInt(c.gender),
          escapeInt(c.castId),
          escapeOpt(c.creditId),
          escapeOpt(c.profilePath)
        ).mkString(delimiter)
      })
    }

    writeToFile(filePath, header, dataStream)
  }

  // ==================== EXPORTAR CREW ====================
  def exportCrew(movies: Stream[IO, MovieComplete]): IO[Long] = {
    val filePath = baseDir / "movie_crew.csv"
    val header = List(
      "movie_id", "person_id", "person_name", "job",
      "department", "credit_id", "gender", "profile_path"
    ).mkString(delimiter)

    val dataStream = movies.flatMap { mc =>
      Stream.emits(mc.crew.map { c =>
        List(
          mc.movie.movieId.toString,
          c.id.toString,
          escapeCSV(c.name),
          escapeOpt(c.job),
          escapeOpt(c.department),
          escapeOpt(c.creditId),
          escapeInt(c.gender),
          escapeOpt(c.profilePath)
        ).mkString(delimiter)
      })
    }

    writeToFile(filePath, header, dataStream)
  }

  // ==================== EXPORTAR RATINGS ====================
  def exportRatings(movies: Stream[IO, MovieComplete]): IO[Long] = {
    val filePath = baseDir / "movie_ratings.csv"
    val header = List("movie_id", "user_id", "rating", "timestamp").mkString(delimiter)

    val dataStream = movies.flatMap { mc =>
      Stream.emits(mc.ratings.map { r =>
        List(
          mc.movie.movieId.toString,
          r.userId.toString,
          r.rating.toString,
          r.timestamp.toString
        ).mkString(delimiter)
      })
    }

    writeToFile(filePath, header, dataStream)
  }

  // ==================== EXPORTAR TODO ====================
  def exportAll(movies: Stream[IO, MovieComplete]): IO[Stats] = {
    // Convertir stream a lista para reutilizar
    movies.compile.toList.flatMap { movieList =>
      val movieStream = Stream.emits(movieList)

      for {
        _ <- ensureOutputDir()
        _ <- IO.println("Iniciando exportación de CSVs normalizados...")

        moviesCount <- exportMovies(movieStream)
        _ <- IO.println(s" movies.csv: ${moviesCount - 1} registros")

        genresCount <- exportGenres(movieStream)
        _ <- IO.println(s" movie_genres.csv: ${genresCount - 1} registros")

        collectionsCount <- exportCollections(movieStream)
        _ <- IO.println(s" movie_collections.csv: ${collectionsCount - 1} registros")

        companiesCount <- exportCompanies(movieStream)
        _ <- IO.println(s" movie_companies.csv: ${companiesCount - 1} registros")

        countriesCount <- exportCountries(movieStream)
        _ <- IO.println(s" movie_countries.csv: ${countriesCount - 1} registros")

        languagesCount <- exportLanguages(movieStream)
        _ <- IO.println(s" movie_languages.csv: ${languagesCount - 1} registros")

        keywordsCount <- exportKeywords(movieStream)
        _ <- IO.println(s" movie_keywords.csv: ${keywordsCount - 1} registros")

        castCount <- exportCast(movieStream)
        _ <- IO.println(s" movie_cast.csv: ${castCount - 1} registros")

        crewCount <- exportCrew(movieStream)
        _ <- IO.println(s" movie_crew.csv: ${crewCount - 1} registros")

        ratingsCount <- exportRatings(movieStream)
        _ <- IO.println(s" movie_ratings.csv: ${ratingsCount - 1} registros")

        _ <- IO.println(s"\n Exportación completada en: $outputDir")
        _ <- IO.println(s"RESUMEN DE REGISTROS:")
        _ <- IO.println(s"   • Películas: ${moviesCount - 1}")
        _ <- IO.println(s"   • Géneros: ${genresCount - 1}")
        _ <- IO.println(s"   • Colecciones: ${collectionsCount - 1}")
        _ <- IO.println(s"   • Productoras: ${companiesCount - 1}")
        _ <- IO.println(s"   • Países: ${countriesCount - 1}")
        _ <- IO.println(s"   • Idiomas: ${languagesCount - 1}")
        _ <- IO.println(s"   • Palabras clave: ${keywordsCount - 1}")
        _ <- IO.println(s"   • Cast: ${castCount - 1}")
        _ <- IO.println(s"   • Crew: ${crewCount - 1}")
        _ <- IO.println(s"   • Ratings: ${ratingsCount - 1}")

      } yield Stats(
        total = movieList.size,
        success = movieList.size,
        errors = 0
      )
    }
  }

  // ==================== GUARDAR ERRORES ====================
  def saveErrors(errors: List[String]): IO[Unit] = {
    if (errors.isEmpty) IO.unit
    else {
      val filePath = baseDir / "errors.log"
      val errorStream = Stream.emits(errors).map(_ + "\n")

      errorStream
        .through(text.utf8.encode)
        .through(Files[IO].writeAll(filePath, Flags.Write))
        .compile
        .drain
        .flatMap(_ => IO.println(s" ${errors.size} errores guardados en errors.log"))
    }
  }
}