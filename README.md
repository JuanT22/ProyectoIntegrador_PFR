```markdown
# Proyecto Integrador: Análisis de Datos de Películas con Scala

Este repositorio contiene el desarrollo del Proyecto Integrador de Programación Funcional. El objetivo es procesar, limpiar y analizar un dataset de películas (`movies_dataset.csv`) utilizando Scala y librerías del ecosistema funcional como **Circe** para el manejo de JSON.

## 📂 Estructura del Proyecto

El código fuente se ha organizado siguiendo una arquitectura modular dentro de `src/main/scala`, separando la lógica de negocio, los modelos de datos y la ejecución principal.

```text
Nombre_Del_Repositorio/
├── build.sbt                <-- Dependencias (circe, scala-csv)
├── src/
│   ├── main/
│   │   ├── resources/       <-- Datos de entrada (csv y json)
│   │   └── scala/
│   │       ├── data/        <-- DataLoader.scala (Lectura robusta de CSV)
│   │       ├── models/      <-- ProjectModels.scala (Case Classes)
│   │       ├── utilities/   <-- AnalysisUtils.scala (Lógica de negocio y limpieza)
│   │       └── Main.scala   <-- Punto de entrada de la aplicación
└── README.md                <-- Documentación del proyecto

```

---

## 📅 Avance 1: Análisis Descriptivo y Limpieza Básica

### 1. Diccionario de Datos

A continuación se detallan las columnas del dataset procesado:

| Nombre Columna | Tipo de Dato (Scala) | Propósito | Observaciones |
| --- | --- | --- | --- |
| `id` | `Int` | Identificador único | Clave primaria de la película. |
| `title` | `String` | Título de la película | Se utiliza para reportes y visualización. |
| `budget` | `Double` | Presupuesto de producción | **Dato Crítico:** Contiene múltiples valores en `0` que requieren limpieza. |
| `revenue` | `Double` | Ingresos generados | Usado para análisis financiero. |
| `genres` | `String` | Categoría | Texto plano. Se usa para análisis de frecuencia. |
| `crew` | `String` (JSON) | Equipo de producción | Contiene una estructura JSON compleja anidada dentro del CSV. |

### 2. Lectura y Análisis Numérico

Se implementó la lectura del archivo mediante la librería `scala-csv` para manejar correctamente los caracteres de escape en columnas complejas.

* **Estadísticas calculadas:** Promedio, Máximo y Mínimo.
* **Columnas analizadas:** `budget` (Presupuesto) y `revenue` (Ingresos).

### 3. Análisis de Texto (Frecuencia)

Se realizó un análisis de distribución de frecuencia sobre la columna `genres`. El algoritmo agrupa las películas por género idéntico y cuenta las ocurrencias para identificar las categorías predominantes en la industria.

### 4. Limpieza de Datos

Se detectó una inconsistencia grave en la columna `budget`: existen registros con valor `0.0`, lo cual distorsiona el análisis estadístico.

* **Estrategia de Limpieza:** Se implementó una función pura que:
1. Filtra los valores válidos (`budget > 0`).
2. Calcula el promedio real de la industria.
3. Reemplaza los valores `0.0` por este promedio calculado (Imputación de datos).



---

## 🚀 Avance 2: Manejo de JSON con Circe

Para este avance se integró la librería **Circe** (`io.circe`), el estándar en el ecosistema Scala para el manejo funcional de JSON.

### 1. Investigación e Implementación de Circe

Se eligió Circe por su capacidad de derivación automática de decodificadores (`generic.auto`), lo que evita escribir código repetitivo (boilerplate) y garantiza seguridad de tipos en tiempo de compilación.

* **Prueba de Concepto:** Se creó un archivo `test_data.json` y una case class `TestUser`. El sistema lee y decodifica exitosamente este archivo para demostrar la correcta configuración de la librería.

### 2. Solución a la Columna "Crew"

La columna `crew` presenta un desafío técnico: es un **String dentro de un CSV que contiene un JSON Array**.

* **El Problema:** Al leer el CSV, Scala interpreta `crew` como una cadena de texto larga.
* **La Solución:**
1. Se modeló la estructura interna con la case class `CrewMember(credit_id, job, name)`.
2. Se utilizó `io.circe.parser.decode` para transformar el String en una `List[CrewMember]`.
3. **Filtrado Funcional:** Una vez convertido a objetos, se aplicó un filtro (`.filter(_.job == "Director")`) para extraer únicamente los directores de cada película, descartando otros roles técnicos.



### 3. Documentación del Proceso de Limpieza Completa

El flujo final de datos (`pipeline`) ejecutado en `Main.scala` es:

1. **Ingesta:** Carga cruda desde `movies_dataset.csv`.
2. **Sanitización:** Detección y corrección de presupuestos en cero.
3. **Enriquecimiento:** Parseo de la columna `crew` para extraer información de directores.
4. **Reporte:** Impresión en consola de estadísticas limpias y datos estructurados.

---

## 🛠️ Cómo ejecutar el proyecto

Requisitos previos: tener instalado `sbt` y `Java SDK` (versión 11 o superior).

1. Clonar el repositorio o descargar los archivos.
2. Navegar a la carpeta raíz.
3. Ejecutar el siguiente comando en la terminal:

```bash
sbt run

```

El sistema compilará las dependencias, procesará los archivos en `src/main/resources` y mostrará los resultados del análisis en la consola.

```

***

### 💡 Un último consejo (Next Step)
Si en tu presentación el profesor te pregunta **"¿Por qué usaste `Option` o `Try` (o por qué no los usaste si quitamos el manejo de errores para simplificar)?"**, puedes responder:

> *"En la carga de datos (`DataLoader`), aunque podríamos haber usado `Option` para cada campo, decidí usar `try/catch` bloqueante dentro del `flatMap` para descartar filas corruptas silenciosamente y asegurarme de que solo entren al sistema datos que cumplan con la estructura de la case class `Movie`. Esto garantiza que las funciones de análisis posteriores sean puras y no fallen."*

¡Mucho éxito en la entrega, te va a ir genial!

```
