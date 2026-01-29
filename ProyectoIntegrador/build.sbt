ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.7"

lazy val root = (project in file("."))
  .settings(
    name := "ProyectoIntegrador",
    libraryDependencies ++= Seq(
      // Cats y Cats Effect
      "org.typelevel" %% "cats-core" % "2.9.0",
      "org.typelevel" %% "cats-effect" % "3.4.8",

      // FS2 para streaming
      "co.fs2" %% "fs2-core" % "3.6.1",
      "co.fs2" %% "fs2-io" % "3.6.1",

      // Doobie para MySQL
      "org.tpolecat" %% "doobie-core" % "1.0.0-RC2",
      "org.tpolecat" %% "doobie-hikari" % "1.0.0-RC2",
      "mysql" % "mysql-connector-java" % "8.0.33",
      // Circe para parsing JSON
      "io.circe" %% "circe-core" % "0.14.6",
      "io.circe" %% "circe-generic" % "0.14.6",
      "io.circe" %% "circe-parser" % "0.14.6"
    )
  )
