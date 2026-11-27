# Jobflow — Gestor de candidaturas laborales

Aplicación desarrollada por **Valentin Leyton Golla** y **Ciro Álvarez Garrido** (2DAM, Grupo 1) para el Ejercicio Integrador del primer trimestre.

El proyecto evoluciona en dos entregas:

| Versión | Estado | Contenido principal |
|---|---|---|
| **v1.0** | Etiqueta `v1.0` | Persistencia en archivos CSV e interfaz de línea de comandos (CLI). |
| **v2.0** | Rama actual / etiqueta `v2.0` | Persistencia JDBC sobre MySQL e interfaz gráfica JavaFX + FXML. |
| v3.0 | Fuera del alcance de esta entrega | Hibernate, estadísticas avanzadas y asistente con Ollama4j. |

---

## Tecnologías

| Tecnología | Uso |
|---|---|
| Java 25 | Lenguaje y runtime. |
| Maven | Gestión de dependencias y compilación. |
| MySQL 8 | Base de datos relacional de la versión 2.0. |
| JDBC | Acceso a datos, consultas con `JOIN`, transacciones y CRUD. |
| JavaFX 25 + FXML | Interfaz gráfica de la versión 2.0. |
| Scene Builder | Maquetación de las vistas `.fxml`. |
| JUnit 5 + H2 | Pruebas automatizadas de la importación CSV. |
| Figma | Prototipo y manual de estilo de la versión 1.0. |

---

## Requisitos

- **Java 25** o superior.
- **Maven 3.9** o superior.
- **MySQL 8** para ejecutar la versión 2.0.
- No se necesita MySQL para ejecutar las pruebas: utilizan H2 en memoria.

---

## Instalación

```bash
git clone https://github.com/Sapitoverde96/EjercicioIntegradorT1.git
cd EjercicioIntegradorT1
mvn clean install
```

Las entregas están separadas por etiquetas de Git:

```bash
git tag
git checkout v1.0   # versión CLI + CSV
git checkout v2.0   # versión JDBC + JavaFX
```

---

## Versión 1.0 — Archivos + CLI

La versión 1.0 trabaja directamente sobre un archivo CSV cuyo nombre se guarda en `config.txt`. El formato de cada línea es:

```text
id;titulo;estado;empresa;fechaPostulacion
```

Para ejecutarla:

```bash
git checkout v1.0
mvn clean package
java -cp target/classes com.cesur.valciro.Run listar
```

Operaciones disponibles:

```bash
java -cp target/classes com.cesur.valciro.Run listar
java -cp target/classes com.cesur.valciro.Run listar --estado pendiente --empresa Indra
java -cp target/classes com.cesur.valciro.Run añadir --titulo "Dev Java" --empresa "Indra" --estado pendiente --fecha 02/03/2026
java -cp target/classes com.cesur.valciro.Run ver 13
java -cp target/classes com.cesur.valciro.Run borrar 13
java -cp target/classes com.cesur.valciro.Run importar candidaturas.csv
```

Los estados admitidos son `pendiente`, `entrevista`, `rechazado` y `aceptado`. La versión 1.0 no utiliza `Scanner` ni menús interactivos: cada ejecución realiza una operación y termina.

---

## Versión 2.0 — JDBC + JavaFX

### 1. Preparar MySQL

Crea una base de datos llamada `database` (o cambia la URL de conexión). La aplicación ejecuta `schema.sql` automáticamente al arrancar.

La configuración está externalizada. Puedes usar, por orden de prioridad:

1. Variables de entorno:
   ```powershell
   $env:DB_URL="jdbc:mysql://localhost:3306/database?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
   $env:DB_USER="root"
   $env:DB_PSWD="tu_contraseña"
   ```
2. Un archivo `database.properties` en la raíz del proyecto:
   ```properties
   db.url=jdbc:mysql://localhost:3306/database?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   db.username=root
   db.password=tu_contraseña
   ```
3. Los valores por defecto de `src/main/resources/database.properties` (MySQL en el puerto `32768`, usuario `usr` y contraseña `pwd`).

### 2. Ejecutar la aplicación

```bash
mvn clean javafx:run
```

También puedes generar el proyecto y ejecutar el lanzador:

```bash
mvn clean package
mvn javafx:run
```

La primera vez que arranque con la tabla `offer` vacía, la aplicación importa automáticamente [`candidaturas.csv`](candidaturas.csv) dentro de una transacción. Si el CSV no existe, utiliza `data.sql` como datos de ejemplo.

### 3. Funcionalidades de la interfaz

- **Listado de candidaturas** en `TableView` con ordenación por columnas y selección de filas.
- **Filtros combinables** por puesto, empresa, estado, fecha y salario mínimo.
- **Cambio de estado** directamente desde el desplegable de la columna «Estado».
- **Alta y edición de candidaturas** con validación de campos.
- **Detalle de candidatura** con todos los datos, empresa asociada y entrevistas.
- **Panel de entrevistas dentro del detalle**, con alta, edición y borrado.
- **Gestión de empresas** con tabla, formulario de alta/edición y borrado en cascada.
- **Gestión de entrevistas** con tabla, formulario y filtro por candidatura cuando se accede desde el detalle.
- **Navegación** entre candidaturas, empresas y entrevistas mediante `FXMLLoader`.
- **Estadísticas rápidas** en el listado: total de candidaturas, pendientes y entrevistas.

### 4. Estructura de la base de datos

```text
company(id, name, industry, website, date)
offer(id, title, company_id FK, status, date, link, description, notes, salary)
interview(id, offer_id FK, date, notes, type, outcome)
```

Las relaciones utilizan `ON DELETE CASCADE`: al borrar una empresa se eliminan sus ofertas y las entrevistas asociadas. Al borrar una oferta se eliminan sus entrevistas.

### 5. Pruebas

```bash
mvn test
```

Las pruebas comprueban la importación del CSV, la creación automática de empresas y el rollback completo cuando una fila es inválida.

---

## Estructura del código

```text
src/main/java/com/cesur/valciro/
├── EjercicioIntegradorLauncher.java
├── EjercicioIntegradorRunnable.java
├── business/models/          # Company, Offer, Interview y enums
├── data/config/              # Datasource, esquema e importador CSV
├── data/dao/                 # DAOs JDBC (company, offer, interview)
└── presentation/
    ├── ViewManager.java      # Navegación y carga de FXML
    └── controllers/          # Controladores JavaFX
```

Los archivos FXML, el CSS y los recursos gráficos están en `src/main/resources/com/cesur/valciro/`.

---

## Datos de prueba

- CSV inicial: [`candidaturas.csv`](candidaturas.csv).
- Esquema SQL: [`src/main/resources/schema.sql`](src/main/resources/schema.sql).
- Datos de ejemplo alternativos: [`src/main/resources/data.sql`](src/main/resources/data.sql).

---

## Prototipo y manual de estilo

El prototipo interactivo de Figma y el manual de estilo se entregaron junto con la versión 1.0. El enlace público de solo lectura se encuentra en la entrega del aula; los recursos gráficos utilizados por la aplicación están incluidos en `src/main/resources/com/cesur/valciro/`.

---

## Contribuciones

| Miembro | v1.0 | v2.0 |
|---|---|---|
| **Ciro Álvarez Garrido** | Persistencia CSV en Repository, conversión de entidades a CSV, listar/añadir/ver en Service, generación de ID y controladores de ver/borrar/importar. Prototipo Figma. | Diseño visual JavaFX, vistas FXML y revisión de la interfaz. |
| **Valentin Leyton Golla** | Entidad y enum, Config y su Repository, Repository/Service/Controller restantes, refactorización, Javadoc y README. | Capa JDBC (DAOs y conexión), importación del CSV a MySQL, pruebas y documentación. |

El detalle de las aportaciones está disponible en el historial de commits del repositorio.

---

## Etiquetas de entrega

```bash
git tag v1.0   # CLI + archivos + diseño
git tag v2.0   # JDBC + JavaFX
```

La versión 3.0 no forma parte de esta entrega.
