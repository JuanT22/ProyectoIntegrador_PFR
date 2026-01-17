package utilities

import models.{Movie, CrewMember, TestUser}
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import scala.io.Source

object AnalysisUtils {

  // ================= AVANCE 1: ESTADÍSTICAS Y LIMPIEZA =================

  // 1. Estadísticas básicas (Promedio, Máx, Mín)
  def calculateNumericStats(data: List[Double], colName: String): Unit = {
    val count = data.size
    val mean = if (count > 0) data.sum / count else 0.0
    val max = if (count > 0) data.max else 0.0
    val min = if (count > 0) data.min else 0.0

    println(f"--- Estadísticas: $colName ---")
    println(f"Total: $count | Promedio: $$${mean}%,.2f | Máx: $$${max}%,.2f | Mín: $$${min}%,.2f")
  }

  // 2. Análisis Texto: Frecuencia (Ej: Géneros)
  def analyzeCategoricalColumn(movies: List[Movie]): Unit = {
    val genres = movies.map(_.genres)
    val freq = genres.groupBy(identity).mapValues(_.size).toMap

    println("--- Distribución de Géneros (Top 5) ---")
    freq.toList.sortBy(-_._2).take(5).foreach { case (g, c) => println(s"$g: $c películas") }
  }

  // 3. Limpieza de Datos: Rellenar ceros con el promedio
  def cleanBudget(movies: List[Movie]): List[Movie] = {
    // Calculamos promedio ignorando los ceros
    val validBudgets = movies.map(_.budget).filter(_ > 0)
    val meanBudget = if (validBudgets.nonEmpty) validBudgets.sum / validBudgets.size else 0.0

    println(f"\n[Limpieza] Reemplazando presupuestos '0' con el promedio: $$${meanBudget}%,.2f")

    movies.map { m =>
      if (m.budget == 0.0) m.copy(budget = meanBudget) else m
    }
  }

  // ================= AVANCE 2: CIRCE Y JSON =================

  // 1. Aprender Circe con JSON pequeño
  def learnCirce(jsonPath: String): Unit = {
    val jsonContent = Source.fromFile(jsonPath).mkString
    val decodeResult = decode[List[TestUser]](jsonContent)

    println("\n--- Avance 2: Prueba de Aprendizaje Circe ---")
    decodeResult match {
      case Right(users) =>
        users.foreach(u => println(s"Usuario: ${u.user_name}, Roles: ${u.roles.mkString(", ")}"))
      case Left(error) => println(s"Error parseando JSON pequeño: $error")
    }
  }

  // 2. Solución Columna Crew: Extraer Directores
  def extractDirectors(movies: List[Movie]): Unit = {
    println("\n--- Avance 2: Análisis Columna 'Crew' (JSON Complejo) ---")

    movies.take(10).foreach { movie =>
      // Parseamos el string JSON a lista de objetos
      val crewDecoded = decode[List[CrewMember]](movie.crewJson)

      crewDecoded match {
        case Right(crewList) =>
          // Filtramos solo el director
          val directors = crewList.filter(_.job == "Director").map(_.name).mkString(", ")
          println(s"Película: ${movie.title} -> Director(es): $directors")
        case Left(_) =>
          println(s"Película: ${movie.title} -> Error leyendo crew")
      }
    }
  }
}