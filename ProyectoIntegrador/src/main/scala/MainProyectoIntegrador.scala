import cats.effect._
import cats.syntax.all._
import doobie.hikari.HikariTransactor
import fs2.io.file.{Files, Path}
import models._
import parser.CSVParcer
import exporter.CSVExporter
import loader.ProyectoIntegradorLoader
import scala.concurrent.ExecutionContext

object MainProyectoIntegrador extends IOApp.Simple {

  // ==================== CONFIGURACIÓN ====================
  val config = AppConfig(
    inputCSV = "C:\\Users\\juani\\Downloads\\pi_movies_complete.csv",
    outputDir = "output_ProyectoIntegrador",
    dbDriver = "com.mysql.cj.jdbc.Driver",
    dbUrl = "jdbc:mysql://localhost:3306/ProyectoIntegrador?useSSL=false&serverTimezone=UTC&rewriteBatchedStatements=true&allowPublicKeyRetrieval=true",
    dbUser = "root",
    dbPassword = "Chupapi22.",
    batchSize = 1000
  )

  // ==================== MENÚ INTERACTIVO ====================
  def showMenu(): IO[Int] = {
    IO.println(
      """
        |╔═══════════════════════════════════════════════════════╗
        |║     ETL ProyectoIntegrador - SISTEMA DE PELÍCULAS     ║
        |╠═══════════════════════════════════════════════════════╣
        |║                                                       ║
        |║  1. Solo Exportar a CSV                               ║
        |║  2. Solo Cargar a MySQL (ProyectoIntegrador)          ║
        |║  3. Full ETL (CSV + MySQL)                            ║
        |║  4. Ver Estadísticas de MySQL                         ║
        |║  0. Salir                                             ║
        |║                                                       ║
        |╚═══════════════════════════════════════════════════════╝
        |
        |➜ Seleccione una opción: """.stripMargin
    ) *> IO(scala.io.StdIn.readInt())
  }

  // ==================== SOLO EXPORTAR CSV ====================
  def exportToCSV(): IO[Unit] = {
    for {
      _ <- IO.println("\n🎬 EXPORTANDO A CSV...")
      startTime <- IO(System.currentTimeMillis())
      errorsRef <- Ref[IO].of(List.empty[String])
      successRef <- Ref[IO].of(0L)

      moviesStream = Files[IO]
        .readUtf8Lines(Path(config.inputCSV))
        .drop(1)
        .zipWithIndex
        .evalMap { case (line, idx) =>
          CSVParcer.parseRow(line, idx.toInt + 2).flatMap {
            case Right(movie) =>
              successRef.update(_ + 1).as(Some(movie))
            case Left(error) =>
              errorsRef.update(error :: _).as(None)
          }
        }
        .collect { case Some(movie) => movie }

      exporter = new CSVExporter(config.outputDir)
      stats <- exporter.exportAll(moviesStream)

      errors <- errorsRef.get
      totalSuccess <- successRef.get
      _ <- exporter.saveErrors(errors)

      endTime <- IO(System.currentTimeMillis())
      duration = (endTime - startTime) / 1000.0

      _ <- IO.println(s"\n Exportación completada en $duration segundos")
      _ <- IO.println(s" Total procesadas: ${totalSuccess + errors.size} líneas")
      _ <- IO.println(s" Exitosas: $totalSuccess películas")
      _ <- IO.println(s" Errores: ${errors.size}")
      _ <- IO.println(s"\n Revisa los archivos CSV en: ${config.outputDir}/")
    } yield ()
  }

  // ==================== SOLO CARGAR A MYSQL ====================
  def loadToMySQL(): IO[Unit] = {
    HikariTransactor.newHikariTransactor[IO](
      config.dbDriver,
      config.dbUrl,
      config.dbUser,
      config.dbPassword,
      ExecutionContext.global
    ).use { xa =>
      val loader = new ProyectoIntegradorLoader(xa)

      for {
        _ <- IO.println("\n CARGANDO A MYSQL (ProyectoIntegrador)...")
        _ <- IO.println(" IMPORTANTE: Asegúrate de haber ejecutado el script SQL 'create database ProyectoIntegrador'")
        startTime <- IO(System.currentTimeMillis())

        successRef <- Ref[IO].of(0)
        errorsRef <- Ref[IO].of(List.empty[String])
        batchBuffer <- Ref[IO].of(List.empty[MovieComplete])
        lineCountRef <- Ref[IO].of(0L)
        lastProgressRef <- Ref[IO].of(0L)

        _ <- Files[IO]
          .readUtf8Lines(Path(config.inputCSV))
          .drop(1)
          .zipWithIndex
          .evalMap { case (line, idx) =>
            for {
              _ <- lineCountRef.update(_ + 1)
              result <- CSVParcer.parseRow(line, idx.toInt + 2)

              _ <- result match {
                case Right(movie) =>
                  for {
                    buffer <- batchBuffer.updateAndGet(movie :: _)
                    _ <- if (buffer.size >= config.batchSize) {
                      for {
                        count <- loader.loadBatch(buffer)
                        _ <- successRef.update(_ + count)
                        _ <- batchBuffer.set(List.empty)
                        totalSuccess <- successRef.get
                        _ <- IO.println(s" Procesadas: ${idx + 1} líneas | Insertadas: $totalSuccess películas")
                        _ <- lastProgressRef.set(idx.toLong)
                      } yield ()
                    } else {
                      // Mostrar progreso cada 500 líneas aunque no se complete un lote
                      lastProgressRef.get.flatMap { lastProgress =>
                        if (idx - lastProgress >= 500) {
                          successRef.get.flatMap { totalSuccess =>
                            IO.println(s" Leyendo: ${idx + 1} líneas | En memoria: ${buffer.size} películas | Total insertadas: $totalSuccess") *>
                              lastProgressRef.set(idx.toLong)
                          }
                        } else IO.unit
                      }
                    }
                  } yield ()

                case Left(error) =>
                  errorsRef.update(error :: _)
              }
            } yield ()
          }
          .compile
          .drain
          .handleErrorWith { error =>
            IO.println(s" Error durante el procesamiento: ${error.getMessage}") *>
              IO.println(s"Stack trace:") *>
              IO(error.printStackTrace()) *>
              IO.raiseError(error)
          }

        // Procesar lote final
        finalBatch <- batchBuffer.get
        _ <- if (finalBatch.nonEmpty) {
          loader.loadBatch(finalBatch).flatMap { count =>
            successRef.update(_ + count) *>
              IO.println(s" Lote final: $count películas insertadas")
          }
        } else IO.unit

        totalSuccess <- successRef.get
        errors <- errorsRef.get
        totalLines <- lineCountRef.get

        endTime <- IO(System.currentTimeMillis())
        duration = (endTime - startTime) / 1000.0

        _ <- IO.println(s"\n" + "=" * 60)
        _ <- IO.println(s" Carga completada en $duration segundos")
        _ <- IO.println(s" Total procesadas: $totalLines líneas")
        _ <- IO.println(s" Insertadas: $totalSuccess películas")
        _ <- IO.println(s" Errores: ${errors.size}")
        _ <- IO.println("=" * 60)

      } yield ()
    }
  }

  // ==================== FULL ETL ====================
  def fullETL(): IO[Unit] = {
    for {
      _ <- IO.println("\n INICIANDO FULL ETL (CSV + MYSQL)...")
      _ <- exportToCSV()
      _ <- IO.println("\n" + "=" * 60)
      _ <- loadToMySQL()
      _ <- IO.println("\n ETL COMPLETO FINALIZADO")
    } yield ()
  }

  // ==================== ESTADÍSTICAS MYSQL ====================
  def showStats(): IO[Unit] = {
    HikariTransactor.newHikariTransactor[IO](
      config.dbDriver,
      config.dbUrl,
      config.dbUser,
      config.dbPassword,
      ExecutionContext.global
    ).use { xa =>
      import doobie._
      import doobie.implicits._

      for {
        _ <- IO.println("\n ESTADÍSTICAS DE BASE DE DATOS ProyectoIntegrador")
        _ <- IO.println("=" * 60)

        //EXACTAMENTE las tablas de tu script SQL
        movies <- sql"SELECT COUNT(*) FROM pelicula".query[Long].unique.transact(xa)
        genres <- sql"SELECT COUNT(*) FROM genero".query[Long].unique.transact(xa)
        actors <- sql"SELECT COUNT(*) FROM actor".query[Long].unique.transact(xa)
        crew <- sql"SELECT COUNT(*) FROM equipo_tecnico".query[Long].unique.transact(xa)
        ratings <- sql"SELECT COUNT(*) FROM rating".query[Long].unique.transact(xa)
        collections <- sql"SELECT COUNT(*) FROM coleccion".query[Long].unique.transact(xa)
        companies <- sql"SELECT COUNT(*) FROM compania_productora".query[Long].unique.transact(xa)
        keywords <- sql"SELECT COUNT(*) FROM palabra_clave".query[Long].unique.transact(xa)
        countries <- sql"SELECT COUNT(*) FROM pais".query[Long].unique.transact(xa)
        languages <- sql"SELECT COUNT(*) FROM idioma".query[Long].unique.transact(xa)

        // Relaciones (tablas intermedias)
        pelicula_genero <- sql"SELECT COUNT(*) FROM pelicula_genero".query[Long].unique.transact(xa)
        pelicula_actor <- sql"SELECT COUNT(*) FROM pelicula_actor".query[Long].unique.transact(xa)
        pelicula_equipo <- sql"SELECT COUNT(*) FROM pelicula_equipo_tecnico".query[Long].unique.transact(xa)
        pelicula_compania <- sql"SELECT COUNT(*) FROM pelicula_compania".query[Long].unique.transact(xa)
        pelicula_pais <- sql"SELECT COUNT(*) FROM pelicula_pais".query[Long].unique.transact(xa)
        pelicula_idioma <- sql"SELECT COUNT(*) FROM pelicula_idioma".query[Long].unique.transact(xa)
        pelicula_palabra <- sql"SELECT COUNT(*) FROM pelicula_palabra_clave".query[Long].unique.transact(xa)

        _ <- IO.println(f" Películas:              $movies%,10d")
        _ <- IO.println(f" Géneros:                $genres%,10d")
        _ <- IO.println(f" Actores:                $actors%,10d")
        _ <- IO.println(f" Equipo Técnico:         $crew%,10d")
        _ <- IO.println(f" Calificaciones:         $ratings%,10d")
        _ <- IO.println(f" Colecciones:            $collections%,10d")
        _ <- IO.println(f" Compañías Productoras:  $companies%,10d")
        _ <- IO.println(f" Palabras Clave:         $keywords%,10d")
        _ <- IO.println(f" Países:                 $countries%,10d")
        _ <- IO.println(f"️  Idiomas:                $languages%,10d")
        _ <- IO.println("\n RELACIONES:")
        _ <- IO.println(f"   • Película-Género:      $pelicula_genero%,10d")
        _ <- IO.println(f"   • Película-Actor:       $pelicula_actor%,10d")
        _ <- IO.println(f"   • Película-Equipo:      $pelicula_equipo%,10d")
        _ <- IO.println(f"   • Película-Compañía:    $pelicula_compania%,10d")
        _ <- IO.println(f"   • Película-País:        $pelicula_pais%,10d")
        _ <- IO.println(f"   • Película-Idioma:      $pelicula_idioma%,10d")
        _ <- IO.println(f"   • Película-Palabra:     $pelicula_palabra%,10d")
        _ <- IO.println("=" * 60)

      } yield ()
    }
  }

  // ==================== LOOP PRINCIPAL ====================
  override def run: IO[Unit] = {
    def loop(): IO[Unit] = {
      for {
        option <- showMenu()
        _ <- option match {
          case 1 =>
            exportToCSV() *>
              IO.println("\n Exportación completa. Presione Enter para continuar...") *>
              IO(scala.io.StdIn.readLine()) *>
              loop()

          case 2 =>
            loadToMySQL() *>
              IO.println("\n Carga a MySQL completa. Presione Enter para continuar...") *>
              IO(scala.io.StdIn.readLine()) *>
              loop()

          case 3 =>
            fullETL() *>
              IO.println("\n ETL completo finalizado. Presione Enter para continuar...") *>
              IO(scala.io.StdIn.readLine()) *>
              loop()

          case 4 =>
            showStats() *>
              IO.println("\n Estadísticas mostradas. Presione Enter para continuar...") *>
              IO(scala.io.StdIn.readLine()) *>
              loop()

          case 0 =>
            IO.println("\n Muchas gracias a todos los que estan viendo" +
              "\n Estuvo de locos")

          case _ =>
            IO.println(" Opción inválida") *> loop()
        }
      } yield ()
    }

    loop().handleErrorWith { error =>
      IO.println(s"\n ERROR CRÍTICO: ${error.getMessage}") *>
        IO.println(s"\nStack Trace Completo:") *>
        IO(error.printStackTrace()) *>
        IO.println(s"\n  Verifica:") *>
        IO.println(s"   1. Que ejecutaste el script SQL 'create database ProyectoIntegrador'") *>
        IO.println(s"   2. Que MySQL está corriendo en el puerto 3306") *>
        IO.println(s"   3. Que las credenciales son correctas (usuario: root, contraseña: CALPQ25*)") *>
        IO.println(s"   4. Que el archivo CSV existe en: ${config.inputCSV}") *>
        IO.println(s"\nPresione Enter para volver al menú...") *>
        IO(scala.io.StdIn.readLine()) *>
        run
    }
  }
}