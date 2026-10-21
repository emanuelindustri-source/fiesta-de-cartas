# AdMob: cómo activar los anuncios reales

La cuenta de AdMob ya está aprobada. El ID de editor es:

```text
pub-7330148989868278
```

La app todavía usa los **IDs de prueba** de Google. Los IDs de prueba muestran
anuncios de ejemplo y **no generan ingresos**. Para cobrar hay que crear la app y
los bloques en AdMob y pegar sus IDs.

## 1. Crear la app en AdMob

1. Entra a <https://admob.google.com/> con la cuenta del proyecto.
2. **Apps → Añadir app → Android**.
3. Nombre: `Fiesta de Cartas`.
4. Paquete: `com.fiestadecartas.app`.
5. AdMob te dará un **App ID** con este formato:

```text
ca-app-pub-7330148989868278~1234567890
```

## 2. Crear los bloques

Crea dos bloques para esa app:

- **Banner** (para la pantalla de inicio y el perfil).
- **Intersticial** (cada 3 partidas, como máximo).

Cada bloque tendrá un ID parecido a:

```text
ca-app-pub-7330148989868278/1111111111
ca-app-pub-7330148989868278/2222222222
```

## 3. Pegar los IDs en el proyecto

Edita `app/src/main/java/com/fiestadecartas/app/ads/Ads.kt`:

```kotlin
object AdsConfig {
    const val PUBLISHER_ID = "pub-7330148989868278"
    const val APP_ID = "ca-app-pub-7330148989868278~1234567890"
    const val BANNER_ID = "ca-app-pub-7330148989868278/1111111111"
    const val INTERSTITIAL_ID = "ca-app-pub-7330148989868278/2222222222"
}
```

Y el `APPLICATION_ID` en `app/src/main/AndroidManifest.xml`:

```xml
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="ca-app-pub-7330148989868278~1234567890" />
```

No cambies el `PUBLISHER_ID` por el App ID: son valores distintos.

## 4. Compilar y probar

```powershell
.\gradlew.bat :app:assembleRelease -x lintVitalRelease --no-daemon --console=plain
```

Instala la APK en un dispositivo real, acepta el consentimiento y comprueba que
aparecen anuncios. Los primeros pueden tardar unas horas en servirse.

## 5. Verificación de dominio (`app-ads.txt`)

El archivo `site/app-ads.txt` ya contiene:

```text
google.com, pub-7330148989868278, DIRECT, f08c47fec0942fa0
```

Para que AdMob lo valide debe estar en la **raíz de un dominio propio**, por ejemplo:

```text
https://fiestadecartas.com/app-ads.txt
```

GitHub Pages sirve el sitio en `https://emanuelindustri-source.github.io/fiesta-de-cartas/`,
que no es un dominio raíz. Cuando conectes un dominio propio, coloca el archivo en
la raíz y declara el dominio en AdMob.

## 6. Consentimiento

La app tiene un diálogo de consentimiento propio para anuncios. Para tráfico de la
UE/Reino Unido, Google exige el **UMP SDK** o un CMP certificado. Antes de publicar
en esas regiones, integra UMP y prueba el flujo de consentimiento.

## 7. Qué no hacer

- No compartas la contraseña de la cuenta de Google; usa solo los IDs públicos.
- No hagas clic en tus propios anuncios ni pidas a amigos que lo hagan.
- No muestres intersticiales durante una jugada; la frecuencia actual es cada 3
  partidas terminadas.
- No uses IDs de prueba en producción: no pagan.
