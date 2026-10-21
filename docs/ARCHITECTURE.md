# Arquitectura

Fiesta de Cartas es una app Android nativa en Kotlin con Jetpack Compose. No usa
backend: todo el estado vive en el dispositivo y la partida viaja por la red local.

## Capas

```text
┌────────────────────────────────────────────────────────────┐
│ UI (Compose)                                               │
│  Home · Lobby · Scanner · Share · Profile · Review         │
│  CaidaScreen · LudoScreen · FiestaScreen · Kemix · Nervioso│
├────────────────────────────────────────────────────────────┤
│ Motores de juego (Kotlin puro, deterministas por semilla)  │
│  CaidaGame · LudoGame · FiestaGame · KemixGame · Nervioso  │
├────────────────────────────────────────────────────────────┤
│ Red local                                                  │
│  LanSession (rol host/guest) · LanServer · LanClient       │
│  ShareServer (HTTP temporal para la APK) · QR              │
├────────────────────────────────────────────────────────────┤
│ Datos locales                                              │
│  Prefs (ajustes/estadísticas) · Profile (XP/nivel/avatar)  │
│  GameConfig (reglas)                                       │
└────────────────────────────────────────────────────────────┘
```

### Por qué los motores son independientes de Compose

Cada juego recibe una semilla (`Long`) y genera su propio mazo con `kotlin.random.Random`.
Las pantallas solo llaman a métodos como `playCard(index)`, `roll()` o `flip(index)`.
Esto permite:

- Reiniciar una partida con la misma semilla y obtener el mismo estado.
- Replicar acciones en el invitado sin enviar el estado completo.
- Probar la lógica sin emulador.

## Multijugador LAN

El anfitrión es la autoridad. El invitado envía acciones; el anfitrión las aplica,
ejecuta los bots y reemite las acciones necesarias.

```text
Invitado                         Anfitrión
   │  HELLO|nombre|código  ────────▶ │
   │  ◀──────── WELCOME|seed|juego|jugadores|código
   │  ◀──────── START|seed|juego|jugadores|host|guest
   │  ACTION|PLAY:3        ────────▶ │
   │  ◀──────── ACTION|PLAY:1        │  (bots / turno del host)
   │  BYE                  ────────▶ │
```

Detalles importantes:

- Los sockets se abren y escriben en hilos de trabajo, nunca en el hilo de UI.
- Las escrituras se serializan en un `Executors.newSingleThreadExecutor()`.
- Los callbacks de red se publican al hilo principal con `Handler(Looper.getMainLooper())`.
- El código de sala es una comprobación ligera, no autenticación fuerte: el modo
  está pensado para una red WiFi de confianza.
- El servidor de compartición de APK es temporal y se apaga al salir de la pantalla.

## Estado y recomposición

- `Prefs`, `Profile` y `GameConfig` son objetos con propiedades `mutableStateOf`
  respaldadas por `SharedPreferences`.
- Las pantallas mantienen un contador `revision` que se incrementa después de cada
  mutación del motor; así Compose vuelve a leer el estado mutable del juego.
- Las animaciones (reparto, vuelo de carta, temblor de mesa, dado) son estado local
  de la pantalla y no afectan la lógica ni la sincronización.

## Diseño y temas

- `Festival.current()` calcula la temporada con la fecha del dispositivo; `Prefs`
  permite fijarla manualmente.
- `FiestaTheme` combina la paleta de temporada con el color dinámico de Android 12+
  y el color dominante del fondo de pantalla.
- Los textos viven en `values`, `values-en`, `values-pt`, `values-it` y `values-zh`.
- `DeviceInfo` decide teléfono/tablet/TV/doble pantalla y limita el ancho de lectura.

## Arte

Las cartas españolas, los naipes de colores, las fichas de Ludo, los dados y los
avatares se dibujan con Canvas y Compose. No se incluyen imágenes de barajas
comerciales ni logotipos de marcas. Los avatares son personajes originales
inspirados en fauna venezolana.

## Calidad y entrega

- `:app:assembleDebug` se ejecuta en GitHub Actions en cada push/PR.
- El workflow de release usa un keystore cifrado en secrets y publica la APK.
- GitHub Pages publica `site/` en cada cambio.
- El APK firmado se verifica con `apksigner` antes de publicarse.
- El SHA-256 se publica en el README y en la página de descarga.

## Deuda técnica conocida

- Un solo invitado por sala; el resto son bots.
- Kemix y Nervioso aún no tienen modo WiFi.
- Los mensajes internos de los motores están en español; la interfaz sí está
  traducida.
- El modo con dinero real no está implementado; ver `ONLINE_ROADMAP.md`.
