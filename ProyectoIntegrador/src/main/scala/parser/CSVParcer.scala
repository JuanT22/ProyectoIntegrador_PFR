package parser

import cats.effect.IO
import io.circe.Decoder
import io.circe.parser
import models._
import models.Decoders._
import java.sql.Date
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object CSVParcer {

  // ==================== FUNCIONES DE PARSEO DE FECHAS ====================
  def parseDate(dateStr: String): Option[Date] = {
    if (dateStr == null || dateStr.trim.isEmpty || dateStr == "None") {
      None
    } else {
      val formats = List(
        "M/d/yyyy",      // 5/3/1998
        "yyyy-MM-dd",
        "dd/MM/yyyy",
        "MM/dd/yyyy",
        "yyyy/MM/dd"
      )

      formats.foldLeft[Option[Date]](None) { (result, format) =>
        result.orElse {
          try {
            val formatter = DateTimeFormatter.ofPattern(format)
            val localDate = LocalDate.parse(dateStr.trim, formatter)
            Some(Date.valueOf(localDate))
          } catch {
            case _: Exception => None
          }
        }
      }
    }
  }

  // ==================== LIMPIEZA DE JSON ====================
  def cleanJsonString(raw: String): String = {
    if (raw.trim.isEmpty || raw.trim == "[]" || raw.trim == "\"[]\"") {
      "[]"
    } else {
      var cleaned = raw.trim

      // ✅ Remover comillas externas múltiples veces si es necesario
      while (cleaned.startsWith("\"") && cleaned.endsWith("\"") && cleaned.length > 1) {
        cleaned = cleaned.substring(1, cleaned.length - 1)
      }

      // ✅ CRÍTICO: Primero reemplazar comillas dobles escapadas
      cleaned = cleaned
        .replace("\"\"", "\"")  // Primero convertir "" a "
        .replace("\\\"", "\"")  // Luego \" a "
        .replace("'", "\"")     // Finalmente ' a "
        .replace("None", "null")
        .replace("True", "true")
        .replace("False", "false")

      cleaned
    }
  }

  // ==================== PARSEO DE LISTAS JSON ====================
  def parseJsonList[A: Decoder](raw: String): IO[List[A]] = {
    if (raw.trim.isEmpty || raw.trim == "[]" || raw.trim == "\"[]\"" || raw.trim == "\"\"") {
      IO.pure(List.empty[A])
    } else {
      val cleaned = cleanJsonString(raw)

      // ✅ Si después de limpiar queda vacío, retornar lista vacía
      if (cleaned == "[]" || cleaned.isEmpty) {
        IO.pure(List.empty[A])
      } else {
        IO.fromEither(parser.parse(cleaned).flatMap(_.as[List[A]]))
          .handleErrorWith { error =>
            // ✅ Log del error para debugging
            IO.println(s"⚠️  Error parseando JSON: ${error.getMessage}") *>
              IO.println(s"   JSON raw: ${raw.take(100)}") *>
              IO.println(s"   JSON cleaned: ${cleaned.take(100)}") *>
              IO.pure(List.empty[A])
          }
      }
    }
  }

  // ✅ NUEVO: Parsear Collection individual (no array)
  def parseJsonSingle[A: Decoder](raw: String): IO[Option[A]] = {
    if (raw.trim.isEmpty || raw.trim == "{}" || raw.trim == "\"{}\"" || raw.trim == "\"\"") {
      IO.pure(None)
    } else {
      val cleaned = cleanJsonString(raw)

      if (cleaned == "{}" || cleaned.isEmpty) {
        IO.pure(None)
      } else {
        IO.fromEither(parser.parse(cleaned).flatMap(_.as[A]))
          .map(Some(_))
          .handleErrorWith { error =>
            IO.println(s"⚠️  Error parseando JSON single: ${error.getMessage}") *>
              IO.println(s"   JSON raw: ${raw.take(100)}") *>
              IO.println(s"   JSON cleaned: ${cleaned.take(100)}") *>
              IO.pure(None)
          }
      }
    }
  }

  // ==================== CONVERSIONES SEGURAS ====================
  def safeInt(s: String): Option[Int] =
    if (s.trim.isEmpty || s.trim == "None") None
    else scala.util.Try(s.trim.toInt).toOption

  def safeLong(s: String): Option[Long] =
    if (s.trim.isEmpty || s.trim == "None") None
    else scala.util.Try(s.trim.toLong).toOption

  def safeDouble(s: String): Double =
    scala.util.Try(s.trim.toDouble).getOrElse(0.0)

  def safeBoolean(s: String): Boolean =
    s.trim.toLowerCase match {
      case "true" | "t" | "1" => true
      case _ => false
    }

  def safeString(s: String): String =
    if (s == null || s.trim.isEmpty || s.trim == "None") "" else s.trim

  // ✅ NUEVO: String truncado para campos con límite
  def safeTruncatedString(s: String, maxLength: Int): String = {
    val cleaned = safeString(s)
    if (cleaned.length > maxLength) cleaned.take(maxLength) else cleaned
  }

  // ==================== PARSEO DE FILA CSV ====================
  def parseRow(row: String, lineNum: Int): IO[Either[String, MovieComplete]] = {
    try {
      // ✅ Split con límite -1 para preservar columnas vacías al final
      val cols = row.split(";", -1).map(_.trim)

      if (cols.length < 28) {
        IO.pure(Left(s"Línea $lineNum: Columnas incorrectas (${cols.length}, esperadas 28)"))
      } else {
        for {
          // ✅ Collection puede ser objeto individual, no array
          collectionOpt <- parseJsonSingle[Collection](cols(1))
          collections = collectionOpt.toList

          genres <- parseJsonList[Genre](cols(3))

          // ✅ Countries usa iso_3166_1, no id
          countries <- parseJsonList[ProductionCountry](cols(13))

          companies <- parseJsonList[ProductionCompany](cols(12))

          // ✅ Languages usa iso_639_1, no iso
          languages <- parseJsonList[SpokenLanguage](cols(17))

          keywords <- parseJsonList[Keyword](cols(24))
          cast <- parseJsonList[CastMember](cols(25))
          crew <- parseJsonList[CrewMember](cols(26))

          // ✅ Ratings con doble escape de comillas
          ratings <- parseJsonList[Rating](cols(27))
        } yield {
          val releaseDate = parseDate(cols(14))

          val movie = MovieBase(
            movieId = safeInt(cols(5)).getOrElse(0),
            title = safeString(cols(20)),
            originalTitle = safeString(cols(8)),
            originalLanguage = safeString(cols(7)),
            budget = safeLong(cols(2)),
            revenue = safeLong(cols(15)),
            runtime = safeInt(cols(16)),
            popularity = safeDouble(cols(10)),
            video = safeBoolean(cols(21)),
            voteAverage = safeDouble(cols(22)),
            voteCount = safeInt(cols(23)),
            releaseDate = releaseDate,
            // ✅ CRÍTICO: Truncar status a 50 caracteres
            status = safeTruncatedString(cols(18), 50),
            adult = safeBoolean(cols(0)),
            homepage = Option(cols(4)).filter(s => s.nonEmpty && s != "None"),
            imdbId = safeString(cols(6)),
            posterPath = Option(cols(11)).filter(s => s.nonEmpty && s != "None"),
            tagline = Option(cols(19)).filter(s => s.nonEmpty && s != "None"),
            overview = safeString(cols(9))
          )

          Right(MovieComplete(
            movie = movie,
            collections = collections,
            genres = genres,
            companies = companies,
            country = countries,
            keywords = keywords,
            spoken_lenguages = languages,
            cast = cast,
            crew = crew,
            ratings = ratings
          ))
        }
      }
    } catch {
      case e: Exception =>
        IO.pure(Left(s"Línea $lineNum: ${e.getMessage.take(200)}"))
    }
  }
}