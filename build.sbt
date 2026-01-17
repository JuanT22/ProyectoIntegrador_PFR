name := "B2-TRABAJOINTEGRADOR"
version := "0.1"
scalaVersion := "2.13.12"

libraryDependencies ++= Seq(
  // Para leer CSV robustamente (maneja las comillas del JSON automágicamente)
  "com.github.tototoshi" %% "scala-csv" % "1.3.10",

  // Para Avance 2: Manejo de JSON con Circe
  "io.circe" %% "circe-core" % "0.14.6",
  "io.circe" %% "circe-generic" % "0.14.6",
  "io.circe" %% "circe-parser" % "0.14.6"
)