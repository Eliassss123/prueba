# 🔥 Proyecto Firewall

Sistema de respuesta ciudadana con microservicios Spring Boot, API Gateway, Eureka y frontend Angular. Todo el stack se levanta con **Docker Compose** en un solo comando.

---

## 📋 Tabla de contenidos

1. [Arquitectura](#-arquitectura)
2. [Requisitos previos](#-requisitos-previos)
3. [Estructura del proyecto](#-estructura-del-proyecto)
4. [Instalación y ejecución rápida](#-instalación-y-ejecución-rápida)
5. [Variables de entorno](#-variables-de-entorno)
6. [Puertos y URLs de acceso](#-puertos-y-urls-de-acceso)
7. [Descripción de cada servicio](#-descripción-de-cada-servicio)
8. [Comandos útiles](#-comandos-útiles)
9. [Solución de problemas](#-solución-de-problemas)

---

## 🏗 Arquitectura

```
                         ┌──────────────────┐
                         │   firewall-web   │
                         │  (Angular + Nginx)│
                         │    puerto: 4200   │
                         └────────┬─────────┘
                                  │  /api/*  /alerts/*
                                  ▼
                         ┌──────────────────┐
                         │   API Gateway    │
                         │  (Spring Cloud)  │
                         │   puerto: 8080   │
                         └────────┬─────────┘
                                  │
                    ┌─────────────┼─────────────┐
                    │             │             │
              ┌─────▼─────┐ ┌────▼────┐ ┌──────▼──────┐
              │ms-usuarios│ │ms-report│ │ms-geolocali │
              │  :8084    │ │  :8081  │ │   :8083     │
              └─────┬─────┘ └────┬────┘ └──────┬──────┘
                    │            │              │
              ┌─────▼─────┐ ┌───▼─────┐ ┌──────▼──────┐
              │mysql-usuar│ │mysql-rep │ │mysql-geoloc │
              │  :3309    │ │  :3307  │ │   :3308     │
              └───────────┘ └─────────┘ └─────────────┘
                                  │
                           ┌──────▼──────┐
                           │ ms-alertas  │
                           │   :8082     │
                           └──────┬──────┘
                           ┌──────▼──────┐
                           │mysql-alerts │
                           │   :3306     │
                           └─────────────┘

              Todos registrados en ──► Eureka :8761
```

---

## ✅ Requisitos previos

Antes de ejecutar el proyecto en otro equipo, asegúrate de tener instalado:

| Herramienta       | Versión mínima | Descarga                                           |
|--------------------|---------------|-----------------------------------------------------|
| **Docker Desktop** | 4.x           | https://www.docker.com/products/docker-desktop/     |
| **Docker Compose** | v2 (incluido) | Viene integrado en Docker Desktop                   |
| **Git** (opcional) | 2.x           | https://git-scm.com/downloads                       |

> **Nota:** Docker Desktop para Windows requiere **WSL 2** habilitado. Al instalarlo, el propio instalador te guiará para activarlo.

### Verificar instalación

```bash
docker --version          # Docker version 24.x o superior
docker compose version    # Docker Compose version v2.x
```

---

## 📁 Estructura del proyecto

```
proyecto firewall/
├── docker-compose.yml          ← Orquestador principal (9 servicios)
├── pom.xml                     ← POM padre Maven (multi-módulo)
├── app.jar                     ← JAR general de la aplicación
│
├── eureka-service/             ← Servidor de descubrimiento (Eureka)
│   ├── Dockerfile
│   ├── pom.xml
│   ├── src/
│   └── target/
│       └── eureka-service-0.0.1-SNAPSHOT.jar
│
├── api-gateway/                ← API Gateway (Spring Cloud Gateway)
│   ├── Dockerfile
│   ├── pom.xml
│   ├── src/
│   └── target/
│       └── api-gateway-0.0.1-SNAPSHOT.jar
│
├── usuarios/                   ← Microservicio de usuarios
│   ├── Dockerfile
│   ├── pom.xml
│   ├── src/
│   └── target/
│       └── ms-usuarios-0.0.1-SNAPSHOT.jar
│
├── reportes/                   ← Microservicio de reportes
│   ├── Dockerfile
│   ├── pom.xml
│   ├── src/
│   └── target/
│       └── ms-reportes-0.0.1-SNAPSHOT.jar
│
├── alertas/                    ← Microservicio de alertas
│   ├── Dockerfile
│   ├── pom.xml
│   ├── src/
│   └── target/
│       └── firewall-alerta-0.0.1-SNAPSHOT.jar
│
├── geolocalizacion/            ← Microservicio de geolocalización
│   ├── Dockerfile
│   ├── pom.xml
│   ├── src/
│   └── target/
│       └── geolocalizacion-0.0.1-SNAPSHOT.jar
│
└── firewall-web/               ← Frontend Angular 17 + Tailwind + Leaflet
    ├── Dockerfile
    ├── package.json
    ├── nginx/
    │   └── default.conf        ← Proxy /api/ y /alerts/ → API Gateway
    └── src/
```

---

## 🚀 Instalación y ejecución rápida

### Paso 1 — Clonar o copiar el proyecto

```bash
# Opción A: clonar desde Git
git clone <URL_DEL_REPOSITORIO>
cd "proyecto firewall"

# Opción B: copiar la carpeta manualmente al equipo nuevo
# Copiar toda la carpeta "proyecto firewall" a cualquier ubicación
```

### Paso 2 — Abrir Docker Desktop

Asegúrate de que **Docker Desktop** esté abierto y en ejecución (icono de ballena en la barra de tareas).

### Paso 3 — Levantar todo el stack

Abre una terminal (PowerShell, CMD o Git Bash) en la raíz del proyecto:

```bash
cd "proyecto firewall"
docker compose up -d --build
```

> **¿Qué hace este comando?**
> - `--build` : Construye las imágenes Docker de cada microservicio y del frontend  
> - `-d` : Ejecuta todo en segundo plano (detached)  
> - Los Dockerfiles usan **multi-stage build**: primero compilan con Maven/Node y luego copian solo el `.jar`/`dist` al contenedor final

### Paso 4 — Esperar a que todo esté listo

La primera vez tarda **5-10 minutos** aproximadamente (descarga de imágenes base + compilación Maven). Puedes monitorear el progreso:

```bash
# Ver logs en tiempo real de todos los servicios
docker compose logs -f

# Ver solo un servicio específico
docker compose logs -f ms-usuarios
```

**¿Cómo saber que está listo?**  
Cuando veas en los logs mensajes como:
- `Started EurekaServiceApplication` (Eureka)
- `Started ApiGatewayApplication` (Gateway)
- `Started MsUsuariosApplication` (Usuarios)
- etc.

### Paso 5 — Verificar los servicios

```bash
# Ver el estado de todos los contenedores
docker compose ps
```

Todos deben estar en estado **"running"**. Ejemplo:

```
NAME                  STATUS
eureka                Up
mysql-alerts          Up (healthy)
mysql-reports         Up (healthy)
mysql-geolocation     Up (healthy)
mysql-usuarios        Up (healthy)
ms-usuarios           Up
ms-reportes           Up
ms-alertas            Up
ms-geolocalizacion    Up
api-gateway           Up
firewall-web          Up
```

### Paso 6 — Abrir la aplicación

Abre tu navegador y visita:

🌐 **http://localhost:4200**

---

## 🔐 Variables de entorno

El proyecto utiliza variables de entorno con valores por defecto. Puedes personalizarlas creando un archivo `.env` en la raíz del proyecto:

```env
# .env (opcional — los valores por defecto ya funcionan)
MYSQL_ROOT_PASSWORD=devroot
MYSQL_ALERTS_PASSWORD=alerts_pass
MYSQL_REPORTS_PASSWORD=reports_pass
MYSQL_GEOLOCATION_PASSWORD=geo_pass
MYSQL_USUARIOS_PASSWORD=usuarios_pass

# Necesario para recuperar contrasenas por correo (Gmail)
MAIL_USERNAME=tu_correo@gmail.com
MAIL_APP_PASSWORD=tu_contrasena_de_aplicacion
```

> **Nota:** Si no creas el archivo `.env`, se usarán los valores por defecto definidos en el `docker-compose.yml`.
> La recuperacion de contrasena requiere `MAIL_USERNAME` y `MAIL_APP_PASSWORD`; sin esas variables el endpoint devuelve un error de configuracion y no cambia la clave del usuario.

---

## 🌐 Puertos y URLs de acceso

| Servicio             | URL                          | Puerto | Descripción                           |
|----------------------|------------------------------|--------|---------------------------------------|
| **Frontend**          | http://localhost:4200         | 4200   | Aplicación web Angular (nginx)         |
| **API Gateway**       | http://localhost:8080         | 8080   | Punto de entrada único a las APIs      |
| **Eureka Dashboard**  | http://localhost:8761         | 8761   | Panel de descubrimiento de servicios   |
| **ms-usuarios**       | http://localhost:8084         | 8084   | API de usuarios (acceso directo)       |
| **ms-reportes**       | http://localhost:8081         | 8081   | API de reportes (acceso directo)       |
| **ms-alertas**        | http://localhost:8082         | 8082   | API de alertas (acceso directo)        |
| **ms-geolocalizacion**| http://localhost:8083         | 8083   | API de geolocalización (acceso directo)|
| **MySQL Alertas**     | localhost:3306                | 3306   | Base de datos `alerts_db`              |
| **MySQL Reportes**    | localhost:3307                | 3307   | Base de datos `reports_db`             |
| **MySQL Geolocation** | localhost:3308                | 3308   | Base de datos `geolocation_db`         |
| **MySQL Usuarios**    | localhost:3309                | 3309   | Base de datos `usuarios_db`            |

### Rutas de la API (a través del frontend o Gateway)

```
Frontend (nginx) proxifica automáticamente:
  /api/*      →  API Gateway :8080/api/*
  /alerts/*   →  API Gateway :8080/alerts/*

Rutas disponibles:
  /api/usuarios     →  ms-usuarios
  /api/reportes     →  ms-reportes
  /api/geolocation  →  ms-geolocalizacion
  /alerts/*         →  ms-alertas
```

---

## 📦 Descripción de cada servicio

### 🔎 Eureka Service (Descubrimiento)
- **Tecnología:** Spring Cloud Netflix Eureka Server
- **Puerto:** 8761
- **Función:** Registro y descubrimiento de microservicios. Todos los demás servicios se registran aquí automáticamente.

### 🚪 API Gateway
- **Tecnología:** Spring Cloud Gateway
- **Puerto:** 8080
- **Función:** Punto de entrada único que enruta las peticiones a los microservicios correspondientes según la URL.

### 👤 ms-usuarios
- **Puerto:** 8084
- **Base de datos:** MySQL `usuarios_db` (puerto 3309)
- **Función:** Gestión de usuarios, autenticación (login/registro con RUT + contraseña).

### 📋 ms-reportes
- **Puerto:** 8081
- **Base de datos:** MySQL `reports_db` (puerto 3307)
- **Función:** Creación y consulta de reportes ciudadanos con coordenadas GPS.

### 🚨 ms-alertas
- **Puerto:** 8082
- **Base de datos:** MySQL `alerts_db` (puerto 3306)
- **Función:** Sistema de alertas. Consume el servicio de reportes internamente.

### 📍 ms-geolocalizacion
- **Puerto:** 8083
- **Base de datos:** MySQL `geolocation_db` (puerto 3308)
- **Función:** Gestión de datos de geolocalización.

### 🌐 firewall-web (Frontend)
- **Tecnología:** Angular 17, Angular Material, Tailwind CSS 3, Leaflet (mapas)
- **Puerto:** 4200 (nginx sirve los archivos estáticos)
- **Función:** Interfaz web con login, mapa de incidentes en vivo, y formulario de reporte ciudadano.

---

## 🛠 Comandos útiles

### Gestión del stack

```bash
# Levantar todo (primera vez o después de cambios)
docker compose up -d --build

# Levantar sin reconstruir (uso normal)
docker compose up -d

# Detener todos los servicios
docker compose down

# Detener y eliminar volúmenes (BORRA las bases de datos)
docker compose down -v

# Reiniciar un servicio específico
docker compose restart ms-usuarios

# Reconstruir solo un servicio
docker compose up -d --build ms-reportes
```

### Monitoreo y logs

```bash
# Ver logs de todos los servicios
docker compose logs -f

# Ver logs de un servicio específico
docker compose logs -f api-gateway

# Ver estado de los contenedores
docker compose ps

# Entrar a un contenedor (ej: MySQL)
docker exec -it mysql-usuarios mysql -u root -pdevroot usuarios_db
```

### Limpieza

```bash
# Eliminar contenedores, redes y volúmenes del proyecto
docker compose down -v --rmi local

# Limpiar imágenes huérfanas
docker image prune -f
```

---

## ❗ Solución de problemas

### 1. `docker compose` no se reconoce como comando

> **Solución:** Asegúrate de tener Docker Desktop abierto. Si usas una versión antigua, prueba con `docker-compose` (con guion).

### 2. Error de puertos en uso

```
Error: bind: address already in use
```

> **Solución:** Otro programa usa ese puerto. Ciérralo o cambia el mapeo de puertos en `docker-compose.yml`.
> 
> Para verificar qué usa un puerto en Windows:
> ```powershell
> netstat -ano | findstr :4200
> ```

### 3. Los microservicios no aparecen en Eureka

> **Solución:** Espera 30-60 segundos después de que Eureka esté levantado. Los microservicios se registran gradualmente. Revisa los logs:
> ```bash
> docker compose logs -f ms-usuarios
> ```

### 4. La base de datos no está lista (healthcheck falla)

> **Solución:** Las bases de datos MySQL tienen healthchecks configurados. Los microservicios esperarán automáticamente hasta que MySQL esté saludable. Si el problema persiste:
> ```bash
> docker compose down -v
> docker compose up -d --build
> ```

### 5. Error de memoria en Docker

> **Solución:** El stack completo puede necesitar 4-6 GB de RAM. Ajusta la memoria en Docker Desktop:
> - **Settings** → **Resources** → **Memory** → Aumentar a 6 GB mínimo

### 6. La primera compilación es muy lenta

> **Solución:** Es normal. Maven descarga todas las dependencias la primera vez. Las ejecuciones siguientes serán mucho más rápidas gracias al cache de Docker.

---

## 📐 Tecnologías utilizadas

| Componente      | Tecnología                                    |
|-----------------|-----------------------------------------------|
| Backend         | Java 21, Spring Boot, Spring Cloud            |
| Build           | Maven 3.9.9                                   |
| Service Discovery| Spring Cloud Netflix Eureka                  |
| API Gateway     | Spring Cloud Gateway                          |
| Base de datos   | MySQL 8.0 (4 instancias independientes)       |
| Frontend        | Angular 17, Angular Material, Tailwind CSS 3  |
| Mapas           | Leaflet.js                                    |
| Servidor web    | Nginx 1.27                                    |
| Contenedores    | Docker + Docker Compose                       |
| Runtime Java    | Eclipse Temurin 21 (JRE)                      |
| Runtime Node    | Node 20 Alpine (solo para build)              |

---

> **Proyecto Firewall** — Red de respuesta · Chile 🇨🇱
