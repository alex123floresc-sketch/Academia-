# Cómo ejecutar el proyecto (backend Spring Boot + frontend Angular)

Guía completa para levantar la migración en tu computadora (Windows).
Todo vive en `C:\Programing\JAVA\Academia\migracion-angular\`:

```
migracion-angular/
├── backend/     ← API REST + JWT (Spring Boot, Java 21)
└── frontend/    ← aplicación Angular (SPA)
```

Necesitas **dos terminales abiertas a la vez**: una para el backend y otra para el frontend.
El orden correcto es: 1) base de datos, 2) backend, 3) frontend.

---

## 0. Requisitos previos (instalar una sola vez)

| Programa | Versión | Para qué | Cómo verificar |
|---|---|---|---|
| **JDK** | 21 | Compilar/ejecutar el backend | `java -version` |
| **MySQL** | 8.x | Base de datos `Academia` | `mysql --version` |
| **Node.js** | 20.19+ o 22+ | Ejecutar Angular | `node -v` |
| **npm** | 10+ (viene con Node) | Instalar dependencias Angular | `npm -v` |

No necesitas instalar Maven ni Angular CLI: el backend trae `mvnw.cmd` (Maven Wrapper) y el
CLI de Angular se instala solo con `npm install`.

**Verifica todo** abriendo PowerShell y ejecutando:
```powershell
java -version
node -v
npm -v
mysql --version
```
Si algún comando falla, instala ese programa antes de continuar:
- JDK 21: https://adoptium.net (Temurin 21)
- Node.js: https://nodejs.org (versión LTS)
- MySQL: https://dev.mysql.com/downloads/installer/

---

## 1. Preparar la base de datos MySQL

El backend espera una base de datos llamada `Academia`. Según tu `application.yml`, por defecto
se conecta con:

- URL: `jdbc:mysql://localhost:3306/Academia`
- Usuario: `root`
- Contraseña: `mysql123@`

Asegúrate de que MySQL esté encendido y que exista la base. Abre una terminal y ejecuta:

```powershell
mysql -u root -p
# (te pedirá la contraseña de root)
```
Ya dentro de MySQL:
```sql
CREATE DATABASE IF NOT EXISTS Academia CHARACTER SET utf8mb4;
EXIT;
```

> Las tablas se crean solas al arrancar el backend (`ddl-auto=update`). Si ya venías usando esta
> base con el proyecto original, no borres nada: es la misma.

**¿Tu contraseña de root NO es `mysql123@`?** No edites el `application.yml`. Mejor define variables
de entorno antes de arrancar el backend (ver paso 2, opción B).

---

## 2. Ejecutar el BACKEND (API REST)

Abre una terminal en la carpeta del backend:
```powershell
cd C:\Programing\JAVA\Academia\migracion-angular\backend
```

### Opción A — tu MySQL usa usuario `root` y contraseña `mysql123@`
```powershell
.\mvnw.cmd clean spring-boot:run
```

### Opción B — usar tus propias credenciales de MySQL (sin tocar archivos)
En PowerShell:
```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="TU_CONTRASEÑA"
$env:DB_URL="jdbc:mysql://localhost:3306/Academia?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
.\mvnw.cmd clean spring-boot:run
```

**La primera vez** descargará las dependencias (Maven), incluidas las nuevas de JWT; puede tardar
unos minutos. Cuando veas en la consola algo como:

```
Tomcat started on port 9090 (http)
Started ProjectApplication in X.XXX seconds
```

el backend está listo en **http://localhost:9090**.

### Comprobar que la API responde
Abre **otra** terminal y prueba el login (usuario sembrado automáticamente `admin/admin123`):
```powershell
curl.exe -X POST http://localhost:9090/api/auth/login -H "Content-Type: application/json" -d "{\"username\":\"admin\",\"password\":\"admin123\"}"
```
Debe devolver un JSON con un `token`. Si lo ves, ¡la API funciona!

> Deja esta terminal abierta y corriendo. NO la cierres mientras uses la app.

---

## 3. Ejecutar el FRONTEND (Angular)

Abre una **segunda terminal** en la carpeta del frontend:
```powershell
cd C:\Programing\JAVA\Academia\migracion-angular\frontend
```

Instala dependencias (solo la primera vez, o cuando cambie `package.json`):
```powershell
npm install
```

Levanta la aplicación:
```powershell
npm start
```
Cuando veas:
```
➜  Local:   http://localhost:4200/
```
abre tu navegador en **http://localhost:4200**.

Inicia sesión con:
- Usuario: **admin**  ·  Contraseña: **admin123**

Verás el módulo **Cursos** funcionando: listar, filtrar, crear, editar y eliminar,
consumiendo la API del backend.

---

## 4. Resumen: arranque diario (cuando ya instalaste todo)

1. Asegúrate de que MySQL esté encendido.
2. Terminal 1:
   ```powershell
   cd C:\Programing\JAVA\Academia\migracion-angular\backend
   .\mvnw.cmd spring-boot:run
   ```
3. Terminal 2:
   ```powershell
   cd C:\Programing\JAVA\Academia\migracion-angular\frontend
   npm start
   ```
4. Navegador: http://localhost:4200

Para detener cada servidor: pulsa `Ctrl + C` en su terminal.

---

## 5. (Opcional) Ejecutar desde un IDE

- **IntelliJ IDEA / Eclipse (backend):** abre la carpeta `backend` como proyecto Maven y ejecuta
  la clase `ProjectApplication` (botón ▶). Es equivalente a `mvnw spring-boot:run`.
- **VS Code (frontend):** abre la carpeta `frontend`, abre una terminal integrada y usa
  `npm install` + `npm start`.

---

## 6. Solución de problemas frecuentes

**"JAVA_HOME not found" / no arranca el backend**
Instala JDK 21 y reinicia la terminal. Verifica con `java -version` que diga 21.

**El backend falla con error de conexión a la base de datos**
MySQL no está encendido o la contraseña no coincide. Enciende MySQL y usa la Opción B del paso 2
con tu contraseña real. Confirma que la base `Academia` existe.

**El puerto 9090 ya está en uso**
Arranca el backend en otro puerto:
```powershell
$env:PORT="9095"; .\mvnw.cmd spring-boot:run
```
Si cambias el puerto, actualiza también `frontend/src/environments/environment.ts`
(`apiUrl: 'http://localhost:9095/api'`).

**El puerto 4200 ya está en uso**
```powershell
npm start -- --port 4300
```

**En el navegador: error de CORS**
El frontend debe correr en `http://localhost:4200` (origen permitido en el backend). Si usas otro
puerto, añádelo en `backend/src/main/resources/application.yml` → `app.cors.allowed-origins`, o
define la variable `CORS_ORIGINS` antes de arrancar el backend, por ejemplo:
```powershell
$env:CORS_ORIGINS="http://localhost:4300"; .\mvnw.cmd spring-boot:run
```

**Login devuelve 401**
Usuario o contraseña incorrectos. Los sembrados son `admin/admin123`, `cajero/cajero123`,
`auxiliar/auxiliar123`. Con `admin` tienes acceso completo (crear/editar/eliminar cursos).

**`npm install` falla**
Verifica que Node sea 20.19+ (`node -v`). Borra `node_modules` y `package-lock.json` y reintenta:
```powershell
Remove-Item -Recurse -Force node_modules, package-lock.json
npm install
```

**`'mvnw.cmd' no se reconoce…`**
Estás en la carpeta equivocada. Debes estar dentro de `...\migracion-angular\backend`. Usa
`.\mvnw.cmd` (con el `.\` delante) en PowerShell.

---

## 7. Generar versión de producción (más adelante)

- **Frontend:** `npm run build` genera archivos estáticos en `frontend/dist/academia-frontend/`.
- **Backend:** `.\mvnw.cmd clean package` genera un `.jar` ejecutable en `backend/target/`.
  Se corre con `java -jar target\project-1.3.1.jar`.

Para el despliegue puedes servir el frontend con Nginx/Netlify apuntando a la API, o copiar el
build de Angular dentro de `backend/src/main/resources/static/` para servir todo desde un solo
`.jar`. (Detalle en `LEEME-MIGRACION.md`.)
