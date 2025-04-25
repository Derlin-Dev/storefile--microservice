# 📁 Servicio de Almacenamiento de Archivos

Este proyecto es un **servicio RESTful** desarrollado en **Spring Boot** que permite subir, descargar, listar, eliminar archivos y crear carpetas dentro de un directorio raíz configurable. Es útil para sistemas que requieren gestión básica de archivos del lado del servidor.

## ⚙️ Configuración

En tu archivo `application.properties`, define la siguiente propiedad:

```properties
media.location=/ruta/del/directorio/base
```

Este será el directorio base en el sistema de archivos donde se almacenarán todos los archivos y carpetas.

---

## 📦 Funcionalidades principales

- Subida de múltiples archivos a una subcarpeta.
- Descarga de archivos mediante URL.
- Listado del contenido de un directorio (archivos y carpetas).
- Creación de nuevas carpetas.
- Eliminación de archivos o carpetas (recursiva).

---

## 🔗 Endpoints disponibles

| Endpoint                                           | Método HTTP | Parámetros                                      | Descripción                                               |
|----------------------------------------------------|-------------|--------------------------------------------------|-----------------------------------------------------------|
| `/media/upload`                                    | `POST`      | `MultipartFile[] files`, `String dir`           | Sube múltiples archivos a una subcarpeta específica.     |
| `/media/download-file`                             | `GET`       | `filename`, `directory`                         | Descarga un archivo almacenado.                          |
| `/media/list-directory`                            | `GET`       | `path`                                          | Lista el contenido inmediato de un directorio.           |
| `/media/create-directory`                          | `POST`      | `folderName`, `parentDir`                       | Crea una nueva carpeta dentro de un directorio padre.    |
| `/media/delete`                                    | `DELETE`    | `fileName`, `dir`                               | Elimina un archivo o una carpeta recursivamente.         |

---

## 🧱 Estructura del almacenamiento

- Todos los archivos y carpetas se almacenan dentro de un directorio raíz (`media.location`).
- Las rutas se normalizan y validan para prevenir accesos fuera del directorio base.
- Las URLs para descarga y exploración de archivos se devuelven codificadas en UTF-8 para evitar errores de caracteres especiales.

---

## 🛡️ Seguridad básica

- Se valida que el archivo o carpeta esté dentro del directorio raíz antes de guardar o acceder.
- No se permite almacenar archivos vacíos.
- El sistema genera URLs seguras para cada archivo o carpeta.

---

## 📌 Ejemplo de respuesta

### Subida de archivo exitosa:

```json
[
  {
    "filename": "documento.pdf",
    "directory": "proyectos2025",
    "url": "http://localhost:8080/media/download-file?filename=documento.pdf&directory=proyectos2025"
  }
]
```

### Listado de directorio:

```json
[
  {
    "name": "documento.pdf",
    "url": "http://localhost:8080/media/download-file?filename=documento.pdf&directory=proyectos2025"
  },
  {
    "name": "subcarpeta/",
    "url": "http://localhost:8080/media/list-directory?path=proyectos2025%2Fsubcarpeta"
  }
]
```

---

## 📂 Estructura del servicio principal

El servicio está implementado en la clase `StorageServices`:

- `storeFiles(MultipartFile[], String)`: Sube varios archivos.
- `store(MultipartFile, String)`: Sube un archivo individual.
- `loadsAsResource(String, String)`: Descarga un archivo como recurso.
- `loadAllFilenames(String)`: Lista contenido de una carpeta.
- `createDirectory(String, String)`: Crea una subcarpeta.
- `deleteFile(String, String)`: Elimina un archivo o carpeta.

---

## ✅ Requisitos

- Java 11 o superior
- Spring Boot
- Maven o Gradle

---

## 🚀 Ejecución

```bash
./mvnw spring-boot:run
```

---

## 📤 Contacto

Si tienes dudas o sugerencias, no dudes en abrir un *issue* o contribuir al repositorio.

---
