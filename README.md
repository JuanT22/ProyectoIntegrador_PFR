# Proyecto Integrador: Análisis de Datos de Películas con Scala

Este repositorio contiene el desarrollo del Proyecto Integrador de Programación Funcional. El objetivo es procesar, limpiar y analizar un dataset de películas (`pi_movies_complete.csv`) utilizando Scala y librerías del ecosistema funcional como **FS2**, **Circe** y **Doobie** para la lectura de datos, el manejo de estructuras JSON y la carga en base de datos relacional.

## Estructura del Proyecto

El código fuente se ha organizado de forma modular dentro de `src/main/scala`, separando la lectura de datos, los modelos, la lógica de procesamiento y la carga a base de datos.

```text
Proyecto_Integrador/
├── build.sbt                    <-- Dependencias del proyecto (fs2, circe, doobie, cats-effect)
├── src/
│   ├── main/
│   │   ├── resources/           <-- Archivos de entrada (CSV y JSON de prueba)
│   │   └── scala/
│   │       ├── data/            <-- Lectura y preprocesamiento del CSV
│   │       │   └── CSVParser.scala
│   │       ├── models/          <-- Modelos del dominio (case classes)
│   │       │   └── Models.scala
│   │       ├── loader/          <-- Lógica de carga a base de datos (Doobie)
│   │       │   └── ProyectoIntegradorLoader.scala
│   │       ├── utilities/       <-- Limpieza, análisis y transformaciones
│   │       └── Main.scala       <-- Punto de entrada de la aplicación
└── README.md                    <-- Documentación del proyecto

```
---

#  Avance 1:  Análisis Descriptivo y Limpieza Básica

**Fecha de entrega:** Semana 11 (19 de diciembre de 2025)

Este avance corresponde a la fase inicial del Proyecto Integrador y se enfoca en el **Análisis Exploratorio de Datos (EDA)** y la **limpieza básica del dataset de películas**, aplicando principios de **programación funcional en Scala**.

---

## 1. Diccionario de Datos

Se realizó un análisis inicial de las principales columnas del archivo `movies_dataset.csv`, identificando su tipo, propósito y posibles inconsistencias.

| Columna             | Tipo (Scala)    | Descripción           | Observaciones               |
| ------------------- | --------------- | --------------------- | --------------------------- |
| `id`                | `Int`           | Identificador único   | Clave primaria, sin nulos   |
| `title`             | `String`        | Título de la película | Texto limpio                |
| `budget`            | `Option[Long]`  | Presupuesto           | Muchos valores `0`          |
| `revenue`           | `Option[Long]`  | Ingresos              | Valores válidos             |
| `original_language` | `String`        | Idioma original       | Texto plano                 |
| `status`            | `String`        | Estado de producción  | Texto plano                 |
| `genres`            | `String (JSON)` | Géneros               | No analizado en este avance |

> Las columnas que contienen **JSON embebido** (`genres`, `crew`, `cast`, etc.) se excluyen explícitamente de este avance, según las instrucciones.

---

## 2. Lectura Funcional del CSV

La lectura del dataset se realizó utilizando **FS2**, permitiendo:

* Procesamiento eficiente de archivos grandes.
* Manejo funcional de errores.
* Conversión segura de tipos.

Durante la lectura se aplicaron funciones puras para transformar los datos sin efectos colaterales.

---

## 3. Análisis de Datos Numéricos

Se analizaron las columnas numéricas:

* `budget`
* `revenue`

Para cada columna se calcularon:

* Promedio
* Valor mínimo
* Valor máximo

### Uso de Programación Funcional

El análisis se implementó usando **funciones de orden superior**, tales como:

* `map`: transformación de valores numéricos.
* `filter`: selección de valores válidos.
* `count`: conteo de registros válidos.
* `foldLeft`: acumulación de métricas estadísticas.

Ejemplo conceptual del proceso aplicado:

* Filtrar valores mayores a cero.
* Transformar los datos a tipos numéricos seguros.
* Agregar valores para obtener estadísticas descriptivas.

Este enfoque evita estados mutables y garantiza resultados reproducibles.

---

## 4. Análisis de Datos Textuales (Frecuencia)

Se realizó un análisis de frecuencia sobre columnas de texto plano:

* `original_language`
* `status`

### Metodología

1. Agrupar valores idénticos.
2. Contar ocurrencias.
3. Identificar valores dominantes.

### Funciones Funcionales Utilizadas

* `groupBy`
* `map`
* `count`

Este análisis permitió identificar patrones de distribución, como el idioma más común y el estado predominante de las películas.

---

## 5. Limpieza Básica de Datos

### Problema Detectado

La columna `budget` presenta numerosos valores iguales a `0`, los cuales generan distorsión en los análisis estadísticos.

### Estrategia de Limpieza

Se aplicó una **estrategia de imputación funcional**, consistente en:

1. Filtrar presupuestos válidos (`> 0`).
2. Calcular el promedio real.
3. Reemplazar los valores inconsistentes (`0`) por dicho promedio.

### Beneficios

* Reducción de sesgo estadístico.
* Conservación de registros completos.
* Preparación del dataset para transformaciones posteriores.

Todas las operaciones se realizaron sin modificar directamente los datos originales, respetando principios funcionales.

---

# Avance 2 Manejo de JSON y Limpieza Completa de Datos con Circe

**Fecha:** Según cronograma de la asignatura

Este avance profundiza en el **manejo de estructuras JSON embebidas en archivos CSV**, utilizando la librería **Circe**, y consolida una **estrategia de limpieza completa de datos**, manteniendo un enfoque funcional y tipado en Scala.

---

## 1. Investigación sobre la Librería Circe

Para el procesamiento de datos en formato JSON se seleccionó la librería **Circe (`io.circe`)**, estándar dentro del ecosistema Scala para el manejo funcional de JSON.

### Justificación Técnica

Circe fue elegida por:

* Seguridad de tipos en tiempo de compilación.
* Integración natural con *case classes*.
* Uso de decodificadores automáticos y semiautomáticos.
* Manejo explícito de errores (`Either`, `Option`).
* Compatibilidad con estructuras JSON complejas y anidadas.

---

## 2. Aprendizaje Inicial con JSON Simple

Como prueba de aprendizaje, se utilizó un archivo JSON pequeño (`test_data.json`) para validar el correcto uso de Circe.

### Ejemplo conceptual

```json
{
  "id": 1,
  "name": "Test User",
  "role": "Admin"
}
```

Este archivo fue decodificado exitosamente hacia una `case class`, permitiendo verificar:

* Correcta configuración de dependencias.
* Uso de `Decoder` de Circe.
* Manejo funcional de errores durante el parseo.

---

## 3. Uso de Circe en Columnas JSON del Dataset

El dataset principal contiene múltiples columnas que almacenan **JSON embebido como texto dentro del CSV**, entre ellas:

* `genres`
* `crew`
* `cast`
* `keywords`
* `production_companies`
* `production_countries`
* `spoken_languages`

### Proceso Aplicado

1. Limpieza previa del `String`:

   * Eliminación de comillas duplicadas.
   * Normalización de valores vacíos.
2. Conversión del texto a JSON válido.
3. Decodificación con Circe hacia listas de *case classes*.

Este proceso se realizó de forma funcional, utilizando:

* `map`
* `flatMap`
* `filter`
* `traverse`

---

## 4. Solución al Manejo de la Columna `crew`

### Problema Identificado

La columna `crew` representa uno de los mayores desafíos técnicos del dataset:

* Se almacena como un `String`.
* Contiene un **array JSON** con múltiples roles técnicos.
* Incluye estructuras anidadas y valores opcionales.

### Solución Implementada

1. **Modelado Tipado**

```scala
case class CrewMember(
  id: Long,
  name: String,
  job: Option[String],
  department: Option[String]
)
```

2. **Decodificación Funcional**

* Conversión del texto limpio a `List[CrewMember]` usando Circe.
* Manejo seguro de errores sin interrumpir el flujo.

3. **Filtrado Funcional**

Se aplicó un filtrado funcional para extraer información relevante:

* Ejemplo: miembros cuyo `job` es `"Director"`.

Funciones utilizadas:

* `filter`
* `map`
* `exists`

Esto permitió trabajar únicamente con los datos relevantes para el modelo relacional.

---

## 5. Limpieza Completa de Datos

Durante este avance se consolidó una estrategia de **limpieza completa**, aplicada de manera sistemática.

### Acciones de Limpieza Implementadas

* Conversión segura de tipos numéricos.
* Manejo explícito de valores `None`.
* Normalización de textos.
* Imputación de presupuestos en `0`.
* Validación de estructuras JSON mal formateadas.
* Truncamiento de campos largos para cumplir restricciones de BD.

Todas las transformaciones se realizaron usando funciones puras, evitando estados mutables.

---

## 6. Pipeline de Procesamiento Documentado

El flujo de datos del proyecto quedó definido de la siguiente manera:

1. **Ingesta**

   * Lectura funcional del CSV.
2. **Sanitización**

   * Limpieza de valores inconsistentes.
3. **Enriquecimiento**

   * Parseo de JSON con Circe.
4. **Transformación**

   * Conversión a estructuras tipadas.
5. **Preparación**

   * Organización de entidades y relaciones.
6. **Salida**

   * Dataset limpio y listo para carga en base de datos.

---

## 7. Resultado del Avance

Este avance deja los datos:

* Correctamente parseados desde JSON.
* Limpiados y normalizados.
* Tipados de forma segura.
* Listos para ser cargados en una base de datos relacional.

---

#  Avance 3: Poblado y Explotación de Base de Datos desde Scala con Doobie


Este avance corresponde a la etapa final del proyecto integrador, donde se implementa el **poblado completo de una base de datos relacional MySQL** a partir de los datos previamente limpiados y transformados, utilizando **Scala**, **Doobie** y principios de **programación funcional**.

---

## 1. Enfoque Seleccionado para el Poblado de Datos

De las opciones propuestas, se implementó la siguiente:

### ✔ Opción 2: Inserciones Directas desde Scala mediante Librería

Se utilizó la librería **Doobie**, la cual permite:

* Ejecución de sentencias SQL tipadas.
* Manejo seguro de transacciones.
* Integración directa con `cats-effect`.
* Prevención de inyección SQL mediante parámetros.

La opción de generación de scripts SQL externos fue descartada, ya que Doobie ofrece mayor control transaccional y escalabilidad.

---

## 2. Arquitectura del Proceso de Carga

El proceso de carga se centraliza en la clase:

```
loader/ProyectoIntegradorLoader.scala
```

Esta clase recibe un `Transactor[IO]` y define **operaciones desacopladas** para cada entidad del modelo relacional.

### Ventajas del diseño

* Separación de responsabilidades.
* Reutilización de funciones.
* Código legible y mantenible.
* Facilidad de extensión.

---

## 3. Inserción de Tablas Maestras

Se implementaron funciones específicas para poblar las tablas maestras:

* `genero`
* `compania_productora`
* `palabra_clave`
* `pais`
* `idioma`
* `coleccion`
* `actor`
* `equipo_tecnico`

### Características Técnicas

* Uso de `Update.updateMany`
* Inserción por lotes
* Prevención de duplicados mediante
  `ON DUPLICATE KEY UPDATE`

### Funciones Funcionales Utilizadas

* `map`
* `filter`
* `traverse`
* `pure`

---

## 4. Inserción de la Entidad Principal `pelicula`

La tabla `pelicula` se inserta mediante una sentencia SQL parametrizada que incluye:

* Identificadores
* Metadatos
* Información financiera
* Fechas
* Indicadores booleanos
* Relación opcional con colecciones

En caso de conflicto por clave primaria, se actualizan campos relevantes sin duplicar registros.

---

## 5. Inserción de Relaciones (Tablas Intermedias)

Dado el modelo altamente normalizado, se implementaron relaciones **muchos a muchos**, entre ellas:

* `pelicula_genero`
* `pelicula_compania`
* `pelicula_palabra_clave`
* `pelicula_idioma`
* `pelicula_pais`
* `pelicula_actor`
* `pelicula_equipo_tecnico`

Cada relación:

* Se inserta solo si existen datos.
* Respeta integridad referencial.
* Evita duplicación.

---

## 6. Manejo de Datos Especiales

### Equipo Técnico (`crew`)

* Solo se insertan miembros con rol (`job` definido).
* Se almacenan `job` y `department`.
* Se respetan las restricciones del esquema SQL.

### Calificaciones (`rating`)

* Se genera un identificador único por calificación.
* Se evita duplicación con `ON DUPLICATE KEY UPDATE`.
* Se mantiene la trazabilidad por película.

---

## 7. Carga de Película Completa (Transaccional)

El método `loadMovie` ejecuta el flujo completo para una película:

1. Inserción de tablas maestras.
2. Inserción de la película.
3. Inserción de relaciones.
4. Inserción de calificaciones.

Todo el proceso se ejecuta dentro del contexto `ConnectionIO`, garantizando **atomicidad y consistencia**.

---

## 8. Procesamiento por Lotes

Para mejorar el rendimiento, se implementó carga por lotes:

```scala
loadBatch(movies: List[MovieComplete]): IO[Int]
```

### Beneficios

* Menor número de conexiones.
* Mayor eficiencia.
* Escalabilidad.

---

## 9. Consultas SQL desde Scala (Explotación de Datos)

Además del poblado, se ejecutan consultas SQL desde Scala para la explotación de datos, tales como:

* Películas con mayor presupuesto.
* Ingresos promedio por género.
* Idiomas más frecuentes.
* Directores con mayor número de películas.

Estas consultas permiten validar la correcta carga y explotar la información almacenada.

---

## 10. Resultado Final del Proyecto

 Con este avance se concreta la carga de la información del dataset en la base de datos, verificando que los datos limpiados y transformados en los avances anteriores sean compatibles con el modelo relacional definido.
 El proceso permitió comprobar la integridad de los datos y el correcto funcionamiento de las inserciones realizadas desde Scala.

---



