package models

import io.circe.Decoder
import java.sql.Date

// ==================== ENTIDADES MAESTRAS ====================
case class Genre(id: Long, name: String)
case class ProductionCompany(id: Long, name: String)

// CORREGIDO: Countries usa iso_3166_1 en lugar de id
case class ProductionCountry(iso_3166_1: String, name: String) {
  def id: Long = iso_3166_1.hashCode.toLong.abs
}

case class Keyword(id: Long, name: String)
case class Collection(id: Long, name: String, poster_path: String, backdrop_path: String)

// CORREGIDO: Languages usa iso_639_1 en lugar de iso
case class SpokenLanguage(iso_639_1: String, name: String) {
  def iso: Long = iso_639_1.hashCode.toLong.abs
}

case class CastMember(
                       id: Long,
                       name: String,
                       character: Option[String],
                       order: Option[Int],
                       gender: Option[Int],
                       cast_id: Option[Int],
                       credit_id: Option[String],
                       profile_path: Option[String]
                     ) {
  // Usar cast_id con snake_case del JSON
  def castId: Option[Int] = cast_id
  def creditId: Option[String] = credit_id
  def profilePath: Option[String] = profile_path
}

case class CrewMember(
                       id: Long,
                       name: String,
                       job: Option[String],
                       department: Option[String],
                       credit_id: Option[String],
                       gender: Option[Int],
                       profile_path: Option[String]
                     ) {
  // Propiedades derivadas
  def creditId: Option[String] = credit_id
  def profilePath: Option[String] = profile_path
}

// CORREGIDO: Rating usa userId en lugar de userid
case class Rating(userId: Long, rating: Double, timestamp: Long)

// ==================== PELÍCULA ====================
case class MovieBase(
                      movieId: Int,
                      title: String,
                      originalTitle: String,
                      originalLanguage: String,
                      budget: Option[Long],
                      revenue: Option[Long],
                      runtime: Option[Int],
                      popularity: Double,
                      video: Boolean,
                      voteAverage: Double,
                      voteCount: Option[Int],
                      releaseDate: Option[Date],
                      status: String,
                      adult: Boolean,
                      homepage: Option[String],
                      imdbId: String,
                      posterPath: Option[String],
                      tagline: Option[String],
                      overview: String
                    )

case class MovieComplete(
                          movie: MovieBase,
                          collections: List[Collection],
                          genres: List[Genre],
                          companies: List[ProductionCompany],
                          country: List[ProductionCountry],
                          keywords: List[Keyword],
                          spoken_lenguages: List[SpokenLanguage],
                          cast: List[CastMember],
                          crew: List[CrewMember],
                          ratings: List[Rating]
                        )

// ==================== ESTADÍSTICAS ====================
case class Stats(
                  total: Long = 0,
                  success: Long = 0,
                  errors: Long = 0
                )

// ==================== CONFIGURACIÓN ====================
case class AppConfig(
                      inputCSV: String,
                      outputDir: String,
                      dbDriver: String,
                      dbUrl: String,
                      dbUser: String,
                      dbPassword: String,
                      batchSize: Int = 1000
                    )

// ==================== DECODERS ====================
object Decoders {

  implicit val genreDecoder: Decoder[Genre] = Decoder.instance { h =>
    for {
      id <- h.get[Long]("id")
      name <- h.get[String]("name")
    } yield Genre(id, name)
  }

  // CORREGIDO: Countries usa iso_3166_1
  implicit val productionCountryDecoder: Decoder[ProductionCountry] = Decoder.instance { h =>
    for {
      iso <- h.get[String]("iso_3166_1")
      name <- h.get[String]("name")
    } yield ProductionCountry(iso, name)
  }

  implicit val productionCompany: Decoder[ProductionCompany] = Decoder.instance { h =>
    for {
      id <- h.get[Long]("id")
      name <- h.get[String]("name")
    } yield ProductionCompany(id, name)
  }

  implicit val keywordDecoder: Decoder[Keyword] = Decoder.instance { h =>
    for {
      id <- h.get[Long]("id")
      name <- h.get[String]("name")
    } yield Keyword(id, name)
  }

  implicit val collectionDecoder: Decoder[Collection] = Decoder.instance { h =>
    for {
      id <- h.get[Long]("id")
      name <- h.get[String]("name")
      poster_path <- h.get[String]("poster_path")
      backdrop_path <- h.get[String]("backdrop_path")
    } yield Collection(id, name, poster_path, backdrop_path)
  }

  // CORREGIDO: Languages usa iso_639_1
  implicit val spokenLanguageDecoder: Decoder[SpokenLanguage] = Decoder.instance { h =>
    for {
      iso <- h.get[String]("iso_639_1")
      name <- h.get[String]("name")
    } yield SpokenLanguage(iso, name)
  }

  // CORREGIDO: Cast usa snake_case
  implicit val castDecoder: Decoder[CastMember] = Decoder.instance { h =>
    for {
      id <- h.get[Long]("id")
      name <- h.getOrElse[String]("name")("Unknown")
      character <- h.get[Option[String]]("character")
      order <- h.get[Option[Int]]("order")
      gender <- h.get[Option[Int]]("gender")
      cast_id <- h.get[Option[Int]]("cast_id")
      credit_id <- h.get[Option[String]]("credit_id")
      profile_path <- h.get[Option[String]]("profile_path")
    } yield CastMember(id, name, character, order, gender, cast_id, credit_id, profile_path)
  }

  // CORREGIDO: Crew usa snake_case
  implicit val crewDecoder: Decoder[CrewMember] = Decoder.instance { h =>
    for {
      id <- h.get[Long]("id")
      name <- h.getOrElse[String]("name")("Unknown")
      job <- h.get[Option[String]]("job")
      department <- h.get[Option[String]]("department")
      credit_id <- h.get[Option[String]]("credit_id")
      gender <- h.get[Option[Int]]("gender")
      profile_path <- h.get[Option[String]]("profile_path")
    } yield CrewMember(id, name, job, department, credit_id, gender, profile_path)
  }

  // CORREGIDO: Rating usa userId (camelCase)
  implicit val ratingDecoder: Decoder[Rating] = Decoder.instance { h =>
    for {
      userId <- h.get[Long]("userId")
      rating <- h.get[Double]("rating")
      timestamp <- h.get[Long]("timestamp")
    } yield Rating(userId, rating, timestamp)
  }
}