# MCP Builder IA

MCP Builder IA es una aplicacion Android nativa para generar conectores MCP orientados a negocio. Desde un formulario permite definir un proyecto, elegir un conector, describir que debe hacer la IA, previsualizar los archivos generados y exportar un ZIP listo para revisar o ejecutar.

## Funcionalidades

- Generacion de proyectos MCP en Node.js con `@modelcontextprotocol/sdk`.
- Conectores base para WooCommerce, WhatsApp Business, WordPress, Google Sheets, Supabase, Notion, CRM propio y ERP pequeno.
- Inferencia sencilla de tools segun el conector y la intencion escrita por el usuario.
- Vista previa por secciones: tools, variables, codigo, documentacion y prompts.
- Guardado y carga local de proyectos mediante `SharedPreferences`.
- Exportacion a ZIP en el directorio de documentos de la app.
- Prueba de conexion para endpoints WordPress y WooCommerce usando autenticacion Basic.
- Prueba de conexion para WhatsApp Business usando Bearer token contra Graph API.

## Estructura

```text
.
├── app/
│   ├── build.gradle
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/mcpbuilder/ia/
│       │   ├── MainActivity.java
│       │   ├── ProjectGenerator.java
│       │   ├── ConnectionTester.java
│       │   ├── ProjectStorage.java
│       │   └── ZipExporter.java
│       └── res/
├── build.gradle
├── settings.gradle
└── local.properties
```

## Requisitos

- Android Studio o Gradle instalado localmente.
- Android Gradle Plugin `9.2.1`.
- `compileSdk` 36, `targetSdk` 36 y `minSdk` 26.
- Java compatible con la version de Android Studio/AGP usada.

El repositorio no incluye `gradlew`, asi que la forma mas directa de abrirlo es desde Android Studio.

## Ejecutar en Android Studio

1. Abre la carpeta del proyecto en Android Studio.
2. Espera a que Gradle sincronice las dependencias.
3. Selecciona un emulador o dispositivo fisico con Android 8.0 o superior.
4. Ejecuta el modulo `app`.

## Compilar por consola

Si tienes Gradle instalado en el sistema:

```bash
gradle :app:assembleDebug
```

El APK debug se genera en:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Skills del proyecto

Los archivos `skills.sh` y `skills.ps1` documentan las capacidades tecnicas instaladas y las siguientes recomendadas para la app. Tambien ejecutan una compilacion debug para validar el entorno.

```bash
bash skills.sh
```

En Windows PowerShell:

```powershell
.\skills.ps1
```

## Uso de la app

1. Escribe el nombre del proyecto.
2. Selecciona el conector.
3. Rellena URL y credenciales si quieres probar la conexion.
4. Describe que quieres que haga la IA.
5. Pulsa `Generar MCP` para actualizar la vista previa.
6. Usa las pestanas para revisar tools, variables, codigo, documentacion y prompts.
7. Pulsa `Guardar` para almacenar la configuracion localmente.
8. Pulsa `Exportar ZIP` para crear el proyecto MCP.

## Proyecto MCP generado

Cada ZIP incluye estos archivos:

```text
package.json
.gitignore
.env.example
src/server.js
src/client.js
docs/README.md
prompts/usage.md
mcp.config.example.json
```

## WhatsApp Business

El conector de WhatsApp Business genera tools MCP para:

- Enviar mensajes de texto con `send_whatsapp_text`.
- Enviar plantillas aprobadas con `send_whatsapp_template`.
- Consultar datos del numero con `get_whatsapp_phone_number` cuando la intencion lo pide.

Para usarlo necesitas datos de WhatsApp Business Platform / Cloud API:

```text
WHATSAPP_API_VERSION=v25.0
WHATSAPP_PHONE_NUMBER_ID=123456789012345
WHATSAPP_ACCESS_TOKEN=EAAG...
```

La app no automatiza WhatsApp personal; usa la API oficial de Meta para cuentas Business.

Dentro del proyecto exportado, el flujo esperado es:

```bash
npm install
cp .env.example .env
npm run check
npm start
```

## Seguridad

- No compartas ni subas archivos `.env` con credenciales reales.
- Revisa manualmente las tools que modifican datos antes de usarlas con clientes reales.
- Las credenciales guardadas se almacenan localmente en `SharedPreferences`; si necesitas proteccion reforzada, conviene migrarlas a almacenamiento cifrado.

## Permisos Android

La app declara el permiso:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

Se usa para probar conexiones contra APIs externas.

## Estado del proyecto

Version actual: `0.1.0`.

El proyecto es un MVP funcional: genera plantillas MCP utiles para prototipado y deja puntos claros para completar integraciones especificas de cada cliente.
