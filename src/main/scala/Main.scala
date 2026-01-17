import data.DataLoader
import utilities.AnalysisUtils

object Main extends App {
  println("=========================================")
  println("      PROYECTO INTEGRADOR - SCALA")
  println("=========================================")

  // RUTAS
  val csvPath = "src/main/resources/movies_dataset.csv"
  val jsonPath = "src/main/resources/test_data.json"

  // 1. CARGA DE DATOS
  println(s"Cargando datos desde $csvPath ...")
  val rawMovies = DataLoader.loadMovies(csvPath)
  println(s"Registros cargados: ${rawMovies.size}")

  // ---------------- AVANCE 1 ----------------
  println("\n>>> EJECUTANDO AVANCE 1")

  // Análisis Numérico
  AnalysisUtils.calculateNumericStats(rawMovies.map(_.budget), "Presupuesto (Original)")
  AnalysisUtils.calculateNumericStats(rawMovies.map(_.revenue), "Ingresos (Revenue)")

  // Análisis Texto
  AnalysisUtils.analyzeCategoricalColumn(rawMovies)

  // Limpieza de Datos (Valores nulos/ceros)
  val cleanMovies = AnalysisUtils.cleanBudget(rawMovies)
  AnalysisUtils.calculateNumericStats(cleanMovies.map(_.budget), "Presupuesto (Limpio)")


  // ---------------- AVANCE 2 ----------------
  println("\n>>> EJECUTANDO AVANCE 2")

  // Parte A: Aprender Circe (JSON Pequeño)
  AnalysisUtils.learnCirce(jsonPath)

  // Parte B: Solución Columna Crew (Usar Circe en columnas JSON)
  AnalysisUtils.extractDirectors(cleanMovies)

  println("\n=========================================")
  println("      PROCESO FINALIZADO CON ÉXITO")
  println("=========================================")
}