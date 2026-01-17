package data

import com.github.tototoshi.csv._
import models.Movie
import java.io.File

object DataLoader {

  def loadMovies(filePath: String): List[Movie] = {
    // Usamos tototoshi porque maneja excelente las comillas dobles dentro del CSV
    val reader = CSVReader.open(new File(filePath))

    // Convertimos cada fila del CSV en un objeto Movie
    val movies = reader.allWithHeaders().flatMap { row =>
      try {
        Some(Movie(
          id = row("id").toInt,
          title = row("title"),
          budget = row("budget").toDouble,
          revenue = row("revenue").toDouble,
          genres = row("genres"),
          crewJson = row("crew")
        ))
      } catch {
        case e: Exception =>
          println(s"Error leyendo fila: $row. Error: $e")
          None
      }
    }
    reader.close()
    movies
  }
}