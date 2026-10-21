# Honeygain SDK (opcional)

Fiesta de Cartas incluye una integración **opcional** con el SDK de Honeygain para
que quien quiera pueda contribuir a mantener la app gratuita compartiendo una parte
del internet que no está usando mientras juega.

> **Estado de esta compilación:** el SDK propietario `hgsdk.aar` y la clave de API
> **no están incluidos en el repositorio**. La app funciona perfectamente sin ellos.
> La tarjeta de apoyo aparece como “no disponible” hasta que se añadan localmente.

## Cómo funciona en la app

- La función está **desactivada por defecto**.
- En **Ajustes → Apoyar la app** se explica qué hace y se abre el consentimiento
  oficial de Honeygain.
- Solo se inicia si el usuario acepta. Se puede desactivar en cualquier momento;
  al hacerlo se llama a `optOut()` y se detiene.
- No se ejecuta en segundo plano ni al arrancar el teléfono (`isBackground = false`,
  `launchOnBoot = false`).
- Si no hay SDK o clave, no se llama a nada y el resto de la app sigue igual.

## Requisitos para activarlo

1. Crear una cuenta en <https://sdk.honeygain.com/dashboard>.
2. Generar una **API key** del SDK.
3. Descargar `hgsdk.aar` desde el panel de desarrolladores.
4. Colocar el archivo en `app/libs/hgsdk.aar` (ignorado por Git).
5. Añadir la clave a `local.properties` (ignorado por Git):

```properties
honeygain.apiKey=TU_CLAVE_AQUI
```

6. Recompilar:

```powershell
.\gradlew.bat :app:assembleRelease --no-daemon --console=plain
```

También se puede pasar la clave por variable de entorno:

```powershell
$env:HONEYGAIN_API_KEY = "TU_CLAVE_AQUI"
```

La AAR se añade como dependencia solo si existe; el proyecto compila sin ella.

## Restricciones importantes

- Honeygain **prohíbe** integrar su SDK en apps distribuidas por **Google Play**.
  Esta app se distribuye como APK directa; si algún día se publica en Play, hay que
  quitar el SDK y volver a firmar.
- El consentimiento debe ser claro y revocable. La app usa la pantalla oficial de
  Honeygain; si se hiciera una pantalla propia, Honeygain exige aprobarla.
- El usuario debe entender que se comparte ancho de banda, no datos personales.
  Enlaza la política de privacidad de Honeygain.
- No se debe activar en dispositivos de menores ni sin consentimiento.
- El SDK necesita internet; la app sigue funcionando offline sin él.

## Implementación técnica

- `data/HoneygainSupport.kt` es un puente por reflexión para que el proyecto compile
  sin la AAR. Detecta la clase `com.honeygain.hgsdk.HgSdk`, inicializa el SDK y llama
  a `requestConsent()`, `start()`, `stop()` y `optOut()`.
- `FiestaApp` inicializa el puente.
- `MainActivity.onResume()` reanuda el SDK si el usuario ya dio su consentimiento.
- `SettingsScreen` muestra el estado, el interruptor y los enlaces legales.
- El SDK se mantiene en primer plano (`isBackground = false`).

## Verificación

Con el AAR y la clave configurados:

1. Abre Ajustes → Apoyar la app.
2. Activa el interruptor.
3. Acepta la pantalla de Honeygain.
4. Vuelve a la app: debe mostrar “Apoyo activo”.
5. Desactívalo: debe detenerse y mostrar “Apoyo desactivado”.
6. Comprueba en el panel de Honeygain que el dispositivo aparece conectado.

## Privacidad

La app no recibe ni almacena datos de Honeygain. El SDK y la red de Honeygain tratan
la conexión según sus propias políticas:

- <https://www.honeygain.com/sdk-privacy-policy/>
- <https://www.honeygain.com/sdk-tos/>

El correo de contacto del proyecto es `emanuel.industri@gmail.com`.
