# Monetización: anuncios, tiendas y Honeygain

> Documento de trabajo. No es asesoría legal ni financiera. Las políticas de cada
> plataforma cambian; verifica siempre sus términos vigentes.

## 1. Honeygain: SDK de ancho de banda, no red de anuncios

Honeygain ofrece un **SDK** para apps de escritorio y móviles (Android, Windows,
Linux, macOS, etc.), pero no es una red publicitaria: el usuario comparte parte de
su ancho de banda sin usar y el integrador recibe ingresos según el tráfico. La app
puede mostrar anuncios por separado con AdMob.

Requisitos y límites:

- Necesitas una cuenta en <https://sdk.honeygain.com/dashboard>, una **API key** y
  descargar `hgsdk.aar`.
- La AAR es propietaria y **no se puede subir al repositorio**; se coloca en
  `app/libs/` (ignorada por Git) y la clave en `local.properties`.
- **No se permite integrarlo en apps de Google Play.** La distribución directa por
  APK sí es compatible, pero si publicas en Play hay que quitar el SDK.
- El consentimiento debe ser explícito y revocable. La app usa la pantalla oficial
  de Honeygain y no lo arranca en segundo plano.
- Puede consumir batería/datos y afectar la reputación de la IP del usuario; hay que
  explicarlo con claridad.
- Los ingresos dependen de la demanda por país y no están garantizados.

La integración de este proyecto está documentada en [`HONEYGAIN.md`](HONEYGAIN.md).
En la compilación pública actual el SDK no está incluido porque falta la AAR y la
clave; la tarjeta de Ajustes aparece como no disponible.

## 2. Proveedores de anuncios para Android

| Red | Fortaleza | Requisito práctico |
|---|---|---|
| **Google AdMob** | Mayor inventario, mediación, SDK maduro, buen soporte para LATAM | Cuenta aprobada; idealmente app publicada en una tienda |
| **AppLovin MAX** | Mediación potente, buen rendimiento en juegos, muchas redes conectadas | Cuenta y revisión; suele pedir tienda o volumen |
| **Unity LevelPlay (ironSource)** | Fuerte en juegos y anuncios recompensados | SDK/adaptadores y cuenta aprobada |
| **Mintegral** | Buen fill en Asia y juegos casuales | Revisión de cuenta y tráfico |
| **Liftoff / Vungle** | Vídeo recompensado de calidad | Cuenta y app con tráfico |
| **Huawei Ads** | Imprescindible en dispositivos Huawei sin Google Play | HMS Core y cuenta Huawei |

### Recomendación práctica

1. **Empieza con AdMob** (banner + intersticial + rewarded cuando exista una
   recompensa real). Es el camino más simple y ya está integrado.
2. Cuando tengas tráfico constante, migra a **mediación con AppLovin MAX** o
   **Unity LevelPlay** y conecta AdMob, Unity Ads, Mintegral y Liftoff.
3. **No apiles varios SDK de anuncios sueltos** en la misma APK: aumenta el
   tamaño, los ANR, el consumo de batería y los riesgos de privacidad. La mediación
   existe precisamente para eso.
4. Usa **UMP / consentimiento** (GDPR, CCPA, políticas de Google) y declara el
   `AD_ID` en el manifiesto y en la ficha de la tienda.
5. Publica `app-ads.txt` en el dominio del sitio cuando la red lo pida.

## 3. El problema de distribuir solo la APK

Las redes premium suelen pedir que la app esté en una tienda. Sin tienda:

- El fill rate y el eCPM bajan mucho.
- Algunas cuentas no se aprueban.
- AdMob puede servir anuncios, pero con inventario limitado y más riesgo de
  invalid traffic.

Alternativas de distribución, además de Google Play:

- **Amazon Appstore** (Fire OS y Android; proceso más accesible).
- **Samsung Galaxy Store**.
- **Huawei AppGallery** (si preparas HMS).
- **Aptoide / Uptodown** (menos ingresos, más alcance).
- **GitHub Releases** (perfecto para amigos y pruebas, no para monetizar a escala).

Para Google Play, una cuenta personal nueva suele exigir **pruebas cerradas con
un grupo de testers** antes de producción. Planea esa fase con 12–20 personas
reales durante al menos 14 días.

## 4. Anuncios en la web

- **Google AdSense** es la opción inicial; requiere dominio propio, contenido
  original, política de privacidad y aprobación.
- **Ezoic / Mediavine / Raptive** piden tráfico mínimo (miles de sesiones/mes).
- **PropellerAds** y redes similares tienen fill alto pero calidad baja; pueden
  dañar la marca.
- La opción más rentable al principio suele ser **patrocinio directo** de marcas
  venezolanas o latinas y enlaces de afiliado relevantes.

El sitio de Fiesta de Cartas ya incluye privacidad, términos, contacto y contenido
descriptivo, que son la base para solicitar AdSense o una red premium.

## 5. Pagos y crypto

- AdMob paga por transferencia bancaria o métodos disponibles según el país; no
  todas las formas están disponibles en Venezuela.
- Bybit y Google Pay pueden servir para **recibir** pagos de otros servicios, pero
  no sustituyen la cuenta de anuncios ni resuelven la fiscalidad.
- Si en el futuro se aceptan USDT, define red (TRC-20/BEP-20), confirmaciones,
  tasa de cambio, custodia y obligaciones de reporte.
- Lleva contabilidad separada de ingresos publicitarios, comisiones y premios.

## 6. Antes de activar anuncios reales

- [ ] Sustituir los IDs de prueba en `Ads.kt` y en el manifiesto.
- [ ] Publicar la app en al menos una tienda o aceptar el menor fill de la APK.
- [ ] Implementar el consentimiento UMP correctamente.
- [ ] Revisar la política de contenido y edad.
- [ ] Medir: impresiones, fill rate, eCPM, retención y ANR.
- [ ] No interrumpir una partida con un intersticial; mantener la frecuencia actual
      (cada 3 partidas) o mejorarla con rewarded optativo.
- [ ] No usar anuncios engañosos ni clics forzados: la cuenta puede ser suspendida.

## 7. Qué NO hacer

- Comprar instalaciones o usar bots para inflar impresiones.
- Mostrar anuncios encima de botones de juego o durante una jugada.
- Integrar SDK de dudosa procedencia o de redes sin política de privacidad.
- Prometer ganancias con Honeygain o con apuestas reales sin licencia.
- Guardar credenciales de AdMob, Google o GitHub en la app o en el repositorio.
