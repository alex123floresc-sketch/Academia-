# Guía para ejecutar el proyecto con IntelliJ IDEA, MySQL Workbench y VS Code

Vas a usar tres programas, cada uno para una parte:

| Programa | Para qué |
|---|---|
| **MySQL Workbench** | Crear y ver la base de datos `Academia` |
| **IntelliJ IDEA** | Ejecutar el **backend** (Spring Boot / API REST) |
| **VS Code** | Ejecutar el **frontend** (Angular) |

Orden de arranque siempre: **1) MySQL → 2) backend (IntelliJ) → 3) frontend (VS Code)**.

---

## ⚠️ Antes de empezar: tu versión de Java

Tienes instalada la **JDK 26**, pero este proyecto está hecho para **Java 21**. Spring Boot 3.5
no está probado con la JDK 26 y puede dar errores. La solución es fácil: le dices a IntelliJ que
use una JDK 21 (la puede descargar solo). Lo vemos en el paso 2.B — no necesitas desinstalar la 26.

Node (24) y npm (11) están bien, no hay que tocar nada.

---

## 1. MySQL Workbench — preparar la base de datos

1. Abre **MySQL Workbench**.
2. En la pantalla de inicio, haz clic en tu conexión local (normalmente
   **"Local instance MySQL80"**). Si te pide contraseña, es la de tu usuario `root`.
   - ¿No tienes conexión? Haz clic en el **+** junto a "MySQL Connections", pon
     Hostname `127.0.0.1`, Port `3306`, Username `root`, y guarda.
3. Ya conectado, abre una pestaña de consulta (el ícono de hoja con un rayo, o `Ctrl+T`).
4. Escribe y ejecuta (con el ícono del rayo ⚡ o `Ctrl+Enter`):
   ```sql
   CREATE DATABASE IF NOT EXISTS Academia CHARACTER SET utf8mb4;
   ```
5. En el panel izquierdo, pulsa el botón de refrescar en "SCHEMAS" y verás **Academia**.
   Las tablas aparecerán solas después de arrancar el backend la primera vez.

> **Anota tu usuario y contraseña de MySQL.** Los necesitarás en IntelliJ. El proyecto asume por
> defecto usuario `root` y contraseña `mysql123@`; si la tuya es distinta, lo configuras en el paso 2.C.

Para **ver los datos** más adelante: en SCHEMAS → Academia → Tables, clic derecho sobre una tabla
→ *Select Rows*.

---

## 2. IntelliJ IDEA — ejecutar el backend (API REST)

### 2.A Abrir el proyecto
1. Abre **IntelliJ IDEA** → **File → Open**.
2. Navega a `C:\Programing\JAVA\Academia\migracion-angular\backend` y selecciona la carpeta
   `backend` (la que contiene `pom.xml`). Abre.
3. IntelliJ detectará que es un proyecto Maven y empezará a descargar dependencias
   (barra de progreso abajo a la derecha). **Espera a que termine** la primera vez.

### 2.B Configurar la JDK 21 (importante por tu Java 26)
1. **File → Project Structure** (`Ctrl+Alt+Shift+S`).
2. En **Project Settings → Project**:
   - **SDK:** despliega la lista. Si aparece un "21", elígelo. Si no:
     - clic en **Add SDK → Download JDK…**
     - Version: **21**, Vendor: **Eclipse Temurin (Adoptium)** → **Download**.
   - **Language level:** *21*.
3. Aplica con **OK**.
4. (Opcional pero recomendado) **File → Settings → Build, Execution, Deployment → Build Tools →
   Maven → Importing / Runner** y asegúrate de que el **JRE** sea la 21.

### 2.C Poner tus credenciales de MySQL (si no son las de por defecto)
Si tu MySQL usa usuario `root` y contraseña `mysql123@`, **salta este paso**.

Si son distintas, defínelas como variables de entorno en la configuración de ejecución:
1. Arriba a la derecha, abre el desplegable de configuraciones → **Edit Configurations…**
   (si aún no existe, la creas al hacer el primer Run del paso 2.D y luego la editas).
2. Busca el campo **Environment variables** y pega (ajustando tu contraseña):
   ```
   DB_USERNAME=root;DB_PASSWORD=TU_CONTRASEÑA
   ```
3. Aplica.

### 2.D Ejecutar
1. En el panel de proyecto, abre:
   `src/main/java/com/unaj/project/ProjectApplication.java`.
2. Verás un triángulo verde ▶ junto a la clase (o al método `main`). Haz clic → **Run
   'ProjectApplication'**.
3. En la consola de abajo, espera hasta ver:
   ```
   Tomcat started on port 9090 (http)
   Started ProjectApplication in X.X seconds
   ```
   ✅ El backend está corriendo en **http://localhost:9090**.

> Deja IntelliJ ejecutando. Para detener el backend usa el cuadrado rojo ⏹ de la consola.

### 2.E Comprobar la API (opcional)
En un navegador o en la terminal:
```
http://localhost:9090/api/cursos
```
Sin token te dará **401/403** (es lo correcto: la API está protegida). El login real lo hará
Angular en el siguiente paso.

---

## 3. VS Code — ejecutar el frontend (Angular)

1. Abre **VS Code** → **File → Open Folder…**
2. Selecciona `C:\Programing\JAVA\Academia\migracion-angular\frontend`.
3. Abre una terminal integrada: menú **Terminal → New Terminal** (`Ctrl + Ñ` o `Ctrl+``).
4. Instala dependencias (solo la primera vez):
   ```powershell
   npm install
   ```
   Tarda un par de minutos y crea la carpeta `node_modules`.
5. Levanta la aplicación:
   ```powershell
   npm start
   ```
6. Cuando veas `Local: http://localhost:4200/`, abre esa dirección en el navegador
   (o `Ctrl+clic` sobre el enlace en la terminal).
7. Inicia sesión con:
   - Usuario: **admin**  ·  Contraseña: **admin123**

Verás el módulo **Cursos** funcionando contra la API del backend.

> Para detener Angular: `Ctrl + C` en la terminal de VS Code.
> Extensiones útiles (opcionales): *Angular Language Service* y *ESLint*.

---

## 4. Flujo de trabajo diario (ya instalado todo)

1. Abre **MySQL Workbench** y conéctate (asegura que el servidor MySQL esté encendido).
2. En **IntelliJ**: ▶ Run `ProjectApplication` → espera "Tomcat started on port 9090".
3. En **VS Code**: terminal → `npm start` → abre http://localhost:4200.
4. Trabaja. Al terminar: ⏹ en IntelliJ y `Ctrl+C` en VS Code.

---

## 5. Solución de problemas

**IntelliJ: errores raros al compilar o arrancar (clases, bytecode, Hibernate)**
Casi siempre es por usar la JDK 26. Repite el paso 2.B y deja la **JDK 21** como SDK del proyecto.
Luego **Build → Rebuild Project**.

**"Communications link failure" / no conecta a la base**
El servidor MySQL no está encendido o la contraseña no coincide. Abre Workbench y conéctate para
confirmar; ajusta las credenciales en el paso 2.C.

**El puerto 9090 está ocupado**
En *Edit Configurations* agrega en Environment variables: `PORT=9095`. Si lo cambias, edita
`frontend/src/environments/environment.ts` → `apiUrl: 'http://localhost:9095/api'`.

**El puerto 4200 está ocupado**
En VS Code: `npm start -- --port 4300`.

**Error de CORS en el navegador**
El frontend debe estar en `http://localhost:4200`. Si usas otro puerto, en IntelliJ agrega la
variable `CORS_ORIGINS=http://localhost:4300` en Environment variables y reinicia el backend.

**Login devuelve 401**
Usuario/contraseña incorrectos. Los válidos: `admin/admin123`, `cajero/cajero123`,
`auxiliar/auxiliar123`. Con `admin` puedes crear, editar y eliminar cursos.

**`npm install` falla**
Borra y reinstala:
```powershell
Remove-Item -Recurse -Force node_modules, package-lock.json
npm install
```

---

## 6. ¿Qué hace cada parte? (para ubicarte)

- **MySQL Workbench** solo guarda/consulta datos. No ejecuta la app.
- **IntelliJ** corre el backend: expone la API en `/api/**` y valida el login con JWT.
- **VS Code** corre Angular: la interfaz que ves en el navegador, que llama a esa API.

Los tres trabajan a la vez mientras desarrollas.
