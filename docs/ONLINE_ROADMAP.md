# Fiesta de Cartas — notas para el modo online y dinero real

> Documento de diseño para el futuro. **La versión actual no mueve dinero real.**
> Las fichas que se muestran en el Perfil son virtuales, no se compran, no se
> cambian por bolívares ni por criptomonedas y no tienen valor monetario.

## 1. Por qué el dinero real no está activo todavía

Integrar apuestas con dinero real no es solo programar una billetera. Implica:

- **Licencia de juego**: en Venezuela la explotación de juegos de azar y apuestas
  requiere autorización del organismo competente. Cada país tiene su propio
  regulador y sus requisitos (edad mínima, límites, reportes, auditoría).
- **Cumplimiento de Google Play**: las apps de apuestas reales solo se permiten en
  países habilitados, con licencia declarada, verificación de edad y políticas
  estrictas. AdMob también restringe contenido de gambling.
- **KYC / AML**: identificación de usuarios, prevención de lavado, listas de
  sanciones, límites de depósito y de pérdida, autoexclusión.
- **Procesamiento de pagos**: Pago Móvil no ofrece una API pública de cobro para
  apps de terceros; requiere convenio bancario. Binance Pay, Bybit y otras
  plataformas tienen APIs, pero implican custodia, comisiones y riesgo legal.
- **Impuestos y contabilidad**: cada premio y comisión debe quedar registrado.
- **Resolución de disputas**: quién valida el resultado, qué pasa si se cae la
  conexión, cómo se devuelven los depósitos.

Mientras no exista esa estructura legal y operativa, la app debe seguir siendo
**100 % recreativa y sin valor monetario**. Esto protege al creador y a los
jugadores.

## 2. Lo que sí está listo hoy

- Perfil local con puntos de experiencia, niveles 1 a 100 y avatares.
- Fichas de práctica (virtuales) para familiarizarse con la idea de una mesa con
  apuesta simbólica.
- Reglas configurables por partida: jugadores, puntos objetivo, fichas para ganar
  y cartas iniciales.
- Salas LAN por WiFi con QR y código; el anfitrión es la autoridad.
- Niveles y progreso guardados en el teléfono.

## 3. Billetera virtual (siguiente paso seguro)

Antes de hablar de dinero real se puede implementar una billetera **sin valor de
cambio**:

1. Saldo de fichas por perfil, con recarga diaria gratuita y bonos por nivel.
2. Mesas con apuesta en fichas; el anfitrión fija el monto y el modo.
3. Fichas bloqueadas al sentarse y liquidadas al terminar la partida.
4. Historial de movimientos (solo local, sin servidor).
5. Aviso visible: “Las fichas no se compran ni se cambian por dinero”.

Esto permite probar la economía del juego sin riesgo legal.

## 4. Arquitectura propuesta para dinero real (futuro)

### 4.1 Separar el dinero del juego

- El cliente **nunca** guarda saldo real ni claves de pago.
- Un backend autorizado mantiene el libro contable (ledger) con partida doble:
  cada débito tiene un crédito y toda operación es idempotente.
- La app solo muestra saldos y solicita operaciones firmadas.

### 4.2 Flujo de depósito con QR

1. El anfitrión crea una mesa con apuesta en bolívares (o USDT) y un identificador
   de partida firmado.
2. El QR contiene `sala_id`, `monto`, `moneda`, `nonce` y la firma del servidor.
3. El jugador paga por el canal habilitado (Pago Móvil con convenio, Binance Pay,
   Bybit, USDT TRC-20/BEP-20) indicando la referencia.
4. El backend verifica el pago (webhook o conciliación) y acredita la ficha de
   entrada a la mesa.
5. Al terminar, el servidor calcula posiciones, comisiones y devuelve el saldo.
6. Los retiros pasan por KYC y se envían al mismo canal verificado.

### 4.3 Reglas técnicas imprescindibles

- Servidor autoritativo; el cliente no decide resultados ni pagos.
- Semilla y barajado verificables; registro de cada acción.
- Webhooks firmados, reintentos y conciliación diaria.
- Límites de depósito, pérdida y tiempo; autoexclusión.
- Verificación de edad y geolocalización para bloquear jurisdicciones no
  autorizadas.
- Panel de soporte y disputas con evidencia de la partida.
- Contraseñas nunca en la app ni en el repositorio; usar OAuth y secretos en el
  backend.

### 4.4 Criptomonedas

USDT puede simplificar pagos internacionales, pero:

- Elegir una red (TRC-20 suele ser barata) y confirmar número de confirmaciones.
- Definir quién custodia los fondos (exchange o custodio regulado).
- Fijar tasa de conversión y momento de la cotización (BCV u oracle).
- Cumplir con las obligaciones de reporte de activos virtuales del país.
- No prometer “sin comisiones”: la red y el exchange cobran.

## 5. Torneos y temporadas (futuro)

- Torneos con inscripción en fichas primero; dinero real solo con licencia.
- Brackets, relojes, desempates y reglas publicadas antes de empezar.
- Ranking global separado por juego y por región.
- Premios patrocinados por marcas (publicidad) en lugar de apuestas.

## 6. Checklist antes de habilitar dinero real

- [ ] Constituir la figura legal y obtener licencia del regulador.
- [ ] Revisar políticas de Google Play y AdMob para la región.
- [ ] Contratar pasarela de pago con API y soporte de Pago Móvil.
- [ ] Implementar KYC/AML y verificación de edad.
- [ ] Backend con ledger, auditoría, idempotencia y respaldos.
- [ ] Términos y condiciones, política de privacidad y reglas de reembolso.
- [ ] Límites de juego responsable y autoexclusión.
- [ ] Pruebas con dinero simulado y auditoría externa.
- [ ] Soporte 24/7 y proceso de disputas.

## 7. Nota sobre AdMob y credenciales

- La app usa IDs de prueba. Para generar ingresos hay que crear la app y los
  bloques en AdMob y pegar los IDs reales en `Ads.kt` y en el manifiesto.
- **No** se deben guardar contraseñas de Google en el proyecto, en capturas ni en
  conversaciones. Usa la verificación en dos pasos y una contraseña única.
- El correo de contacto del proyecto es `emanuel.industri@gmail.com`; no es una
  credencial.
