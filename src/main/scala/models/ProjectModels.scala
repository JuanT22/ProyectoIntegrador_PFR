package models

// 1. Clase para el CSV de Películas
case class Movie(
                  id: Int,
                  title: String,
                  budget: Double,
                  revenue: Double,
                  genres: String,
                  crewJson: String // Leemos esto como String crudo primero
                )

// 2. Clase para parsear el JSON de la columna 'crew' (Avance 2)
case class CrewMember(
                       credit_id: String,
                       job: String,
                       name: String
                     )

// 3. Clase para el ejercicio de aprendizaje de Circe (Avance 2)
case class TestUser(
                     id: Int,
                     user_name: String,
                     roles: List[String],
                     is_active: Boolean
                   )