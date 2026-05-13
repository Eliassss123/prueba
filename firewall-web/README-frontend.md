# Firewall Web — Frontend Angular

Interfaz ciudadana del sistema Firewall. Permite reportar incidentes con geolocalización GPS, visualizar el mapa de alertas activas en tiempo real y gestionar sesión por RUT. Sirve archivos estáticos vía **Nginx** y proxifica las llamadas a la API al Gateway.

---

## 📋 Tabla de contenidos

1. [Arquitectura del frontend](#-arquitectura-del-frontend)
2. [Requisitos previos](#-requisitos-previos)
3. [Estructura del proyecto](#-estructura-del-proyecto)
4. [Instalación y ejecución](#-instalación-y-ejecución)
5. [Variables y configuración](#-variables-y-configuración)
6. [Rutas de la aplicación](#-rutas-de-la-aplicación)
7. [Proxy y conexión al backend](#-proxy-y-conexión-al-backend)
8. [Comandos útiles](#-comandos-útiles)
9. [Solución de problemas](#-solución-de-problemas)

---

## 🏗 Arquitectura del frontend

```
Navegador
    │
    ▼
┌─────────────────────────┐
│     Nginx :80 / :4200   │  ← Sirve los archivos estáticos del build
│  (Angular 17 compilado) │
└────────────┬────────────┘
             │ /api/*  → proxy
             │ /alerts/* → proxy
             ▼
┌─────────────────────────┐
│      API Gateway :8080  │
└─────────────────────────┘
```

### Vistas principales

| Vista | Ruta | Descripción |
|---|---|---|
| Landing / Login | `/` | Autenticación con RUT y contraseña |
| Dashboard | `/app` | Contenedor principal (requiere sesión) |
| Mapa de operador | `/app/mapa` | Mapa Leaflet con incidentes en vivo |
| Reporte ciudadano | `/app/reporte` | Formulario de reporte con GPS y fotos |

---

## ✅ Requisitos previos

### Para desarrollo local

| Herramienta | Versión mínima | Descarga |
|---|---|---|
| **Node.js** | 20.x LTS | https://nodejs.org |
| **npm** | 10.x (incluido con Node) | — |
| **Angular CLI** | 17.x | `npm install -g @angular/cli` |

### Para producción con Docker

| Herramienta | Versión mínima | Descarga |
|---|---|---|
| **Docker Desktop** | 4.x | https://www.docker.com/products/docker-desktop/ |
| **Docker Compose** | v2 (incluido) | Viene integrado en Docker Desktop |

> **Importante:** El frontend en Docker se integra con el stack completo del backend. Consulta el README principal del proyecto para levantarlo todo junto.

### Verificar instalación

```bash
node --version      # v20.x o superior
npm --version       # 10.x o superior
ng version          # Angular CLI 17.x
```

---

## 📁 Estructura del proyecto

```
firewall-web/
├── Dockerfile              ← Build multi-stage: Node (build) + Nginx (serve)
├── package.json            ← Dependencias y scripts npm
├── angular.json            ← Configuración de Angular CLI (configs: dev, prod, docker)
├── proxy.conf.json         ← Proxy para desarrollo local → API Gateway :8080
├── tailwind.config.js      ← Configuración de Tailwind CSS
│
├── nginx/
│   └── default.conf        ← Configuración de Nginx: proxy /api/ y /alerts/
│
└── src/
    ├── index.html          ← HTML base (incluye div.bg-image para el fondo)
    ├── main.ts             ← Bootstrap de la aplicación
    ├── styles.scss         ← Estilos globales, fondo animado, botones
    │
    └── app/
        ├── app.routes.ts   ← Rutas con lazy loading y authGuard
        │
        ├── core/
        │   ├── auth.guard.ts       ← Protege rutas que requieren sesión
        │   └── api-config.service.ts
        │
        └── views/
            ├── landing/            ← Login con RUT + contraseña
            ├── dashboard/          ← Contenedor principal post-login
            ├── operator-map/       ← Mapa Leaflet de incidentes
            └── reporte-ciudadano/  ← Formulario de reporte GPS
```

---

## 🚀 Instalación y ejecución

### Opción A — Desarrollo local (con backend corriendo)

#### Paso 1 — Instalar dependencias

```bash
cd firewall-web
npm install
```

#### Paso 2 — Asegurarte de que el backend está levantado

El frontend necesita el API Gateway corriendo en `localhost:8080`. Desde la carpeta raíz del proyecto:

```bash
cd "proyecto firewall"
docker compose up -d
```

#### Paso 3 — Levantar el frontend en modo desarrollo

```bash
npm start
```

El servidor arrancará en **http://localhost:4200** con hot-reload activado.

> El proxy (`proxy.conf.json`) redirige automáticamente `/api/*` al Gateway en `:8080`.

---

### Opción B — Producción con Docker (recomendado)

El frontend se incluye en el `docker-compose.yml` principal. Para levantarlo junto a todo el stack:

```bash
cd "proyecto firewall"
docker compose up -d --build
```

Accede en: **http://localhost:4200**

> Nginx sirve los archivos estáticos en el puerto 4200 y proxifica `/api/` y `/alerts/` al API Gateway automáticamente.

---

## ⚙️ Variables y configuración

### Proxy de desarrollo (`proxy.conf.json`)

```json
{
  "/api": {
    "target": "http://localhost:8080",
    "secure": false,
    "changeOrigin": true,
    "logLevel": "info"
  }
}
```

Todas las llamadas a `/api/*` desde el frontend en desarrollo se redirigen al API Gateway local.

### Nginx en producción (`nginx/default.conf`)

```nginx
location /api/ {
    proxy_pass http://api-gateway:8080/api/;
}
location /alerts/ {
    proxy_pass http://api-gateway:8080/alerts/;
}
location / {
    try_files $uri $uri/ /index.html;   ← Soporte para rutas Angular (SPA)
}
```

### Imagen de fondo (`src/assets/`)

La aplicación usa una imagen de fondo en:
```
src/assets/background.jpg
```
Si no existe, el fondo será el color oscuro por defecto. Para cambiarla, reemplaza el archivo y reinicia el servidor.

---

## 🗺 Rutas de la aplicación

| URL | Componente | Auth requerida |
|---|---|---|
| `/` | `LandingComponent` | No |
| `/app` | `DashboardComponent` | ✅ Sí (`authGuard`) |
| `/app/mapa` | `OperatorMapComponent` | ✅ Sí |
| `/app/reporte` | `ReporteCiudadanoComponent` | ✅ Sí |
| `/**` | Redirige a `/` | — |

> Las rutas protegidas usan `authGuard`. Si el usuario no tiene sesión activa, es redirigido automáticamente al login.

---

## 🔌 Proxy y conexión al backend

### En desarrollo

El proxy de Angular (`proxy.conf.json`) intercepta las llamadas:

```
Frontend :4200 → /api/usuarios/login → API Gateway :8080 → ms-usuarios :8084
Frontend :4200 → /api/reportes       → API Gateway :8080 → ms-reportes :8081
Frontend :4200 → /api/geolocation    → API Gateway :8080 → ms-geolocalizacion :8083
```

### En producción (Docker)

Nginx hace el mismo trabajo sin necesidad de proxy de Angular:

```
Nginx :80 → /api/* → api-gateway:8080
Nginx :80 → /alerts/* → api-gateway:8080
```

---

## 🛠 Comandos útiles

### Desarrollo

```bash
# Instalar dependencias
npm install

# Servidor de desarrollo con hot-reload
npm start

# Limpiar caché de Angular (cuando los cambios no se reflejan)
Remove-Item -Recurse -Force .angular\cache   # PowerShell (Windows)
rm -rf .angular/cache                         # Bash (Linux/Mac)

# Build de producción
npm run build

# Build en modo watch
npm run watch
```

### Docker

```bash
# Reconstruir solo el frontend
docker compose up -d --build firewall-web

# Ver logs del frontend
docker compose logs -f firewall-web

# Reiniciar el frontend sin reconstruir
docker compose restart firewall-web
```

### Base de datos de usuarios (referencia rápida)

```bash
# Entrar a MySQL de usuarios
docker exec -it mysql-usuarios mysql -u usuarios_user -pusuarios_pass usuarios_db

# Ver usuarios registrados
SELECT rut, nombre, categoria, password_hash FROM usuario;

# Resetear contraseña de un usuario
UPDATE usuario SET password_hash = 'nueva_clave' WHERE rut = '21.479.015-7';
```

---

## ❗ Solución de problemas

### 1. Los cambios en el código no se reflejan

El caché de Angular puede guardar versiones antiguas. Límpialo antes de reiniciar:

```powershell
# PowerShell (Windows)
Remove-Item -Recurse -Force .angular\cache
Remove-Item -Recurse -Force dist
npm start
```

### 2. Error `ECONNREFUSED` al hacer login o cargar reportes

El backend no está corriendo. Levanta los contenedores:

```bash
cd "proyecto firewall"
docker compose up -d
docker ps   # Verifica que todos los contenedores estén en "Up"
```

### 3. La imagen de fondo no aparece

Verifica que el archivo existe en la ruta correcta:
```
firewall-web/src/assets/background.jpg
```
Y que el `index.html` tiene:
```html
<body>
  <div class="bg-image"></div>
  <app-root></app-root>
</body>
```

### 4. Error 404 al cargar rutas directamente (ej: `/app/mapa`)

En producción con Nginx esto está resuelto por `try_files $uri $uri/ /index.html`. Si ocurre en desarrollo, asegúrate de acceder siempre desde la raíz (`/`) o que el servidor esté corriendo con `npm start`.

### 5. Error de compilación por módulos faltantes

```bash
rm -rf node_modules
npm install
```

### 6. `rm -rf` no funciona en PowerShell

PowerShell usa sintaxis diferente a Bash:

```powershell
# En vez de: rm -rf .angular/cache
Remove-Item -Recurse -Force .angular\cache
```

---

## 📐 Tecnologías utilizadas

| Componente | Tecnología |
|---|---|
| Framework | Angular 17 |
| UI Components | Angular Material |
| Estilos | Tailwind CSS 3, SCSS |
| Mapas | Leaflet.js |
| HTTP | Angular HttpClient con proxy |
| Servidor de producción | Nginx 1.27 |
| Runtime de build | Node 20 Alpine |
| Contenedor | Docker (multi-stage build) |

---

> **Proyecto Firewall** — Red de respuesta · Chile 🇨🇱
