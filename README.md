# RentCar — Parcial 2 de PGII

## 1. Integrantes

**Miguel Ángel Bocanegra** — <u>1095179036</u>
**Jhoan Esteban Avilés Sánchez** — <u>1094953060</u>

## 2. Descripción de la solución

RentCar es una aplicación de escritorio para alquilar vehículos. Permite armar una solicitud de alquiler, elegir un plan (Basic, Standard o Premium), agregar servicios extra o paquetes, verificar al cliente, cobrar con una API de pagos externa y registrar la reserva con un estado claro: `CONFIRMED`, `PENDING` o `FAILED`.

Está hecha en Java 21 con Gradle, JavaFX y FXML, siguiendo MVC. No usa base de datos: todo se guarda en memoria mientras la aplicación está abierta.

## 3. Requisitos y funcionalidades implementadas

- **RF-001 Solicitud de alquiler:** `RentalRequestBuilder` arma y valida la solicitud (Builder).
- **RF-002 Proceso de reserva:** `RentalFacade` coordina validación de fechas, disponibilidad, costo, pago y registro (Facade).
- **RF-003 Planes de alquiler:** Basic, Standard y Premium, cada uno con su fábrica (Abstract Factory).
- **RF-004 Pago con API externa:** `PaymentApiAdapter` traduce entre la API de pagos y la aplicación (Adapter).
- **RF-005 Servicios adicionales:** GPS, silla para niños y conductor adicional (Decorator).
- **RF-006 Verificación del cliente:** `CustomerVerificationProxy` verifica al cliente antes de cobrar (Proxy).
- **RF-007 Paquetes de servicios:** `ServiceBundle` agrupa servicios y puede contener otros paquetes (Composite).

**Reparto del trabajo:**

- **Parte A:** Builder, Abstract Factory, Decorator y Composite.
- **Parte B:** Facade, Adapter, Proxy, repositorio de reservas y pantalla de reservas.

## 4. Arquitectura y organización del código

La aplicación sigue MVC:

- **Vista:** archivos FXML en `src/main/resources`.
- **Controlador:** clases en `edu.rentcar.controller`. Solo reciben lo que hace el usuario, llaman a los servicios y muestran el resultado.
- **Modelo y lógica:** el resto de los paquetes.

Paquetes principales dentro de `src/main/java/edu/rentcar`:

- `app`: arranque de la aplicación y `ApplicationServices`, que entrega los servicios a los controladores.
- `controller`: controladores de las pantallas.
- `model`: `Customer`, `Vehicle`, `RentalRequest`, `Reservation` y `ReservationStatus`.
- `builder`: `RentalRequestBuilder` (RF-001).
- `service`: `RentalFacade` (RF-002) y `VehicleService`.
- `factory`: planes de alquiler, sus fábricas y `PlanCatalog` (RF-003).
- `payment`: `PaymentGateway`, `PaymentApiAdapter` y los objetos de pago (RF-004).
- `extras`: `ServiceComponent`, decoradores, `SingleService`, `ServiceBundle`, `ExtraOption` y `ExtrasAssembler` (RF-005 y RF-007).
- `proxy`: `CustomerVerificationProxy` y `CustomerVerifier` (RF-006).
- `repository`: `ReservationRepository` y `VehicleRepository`.
- `data`: datos de ejemplo.
- `client`: ejemplo de uso del cliente de la API de pagos.

## 5. Patrones de diseño

- **Builder (RF-001):** una solicitud lleva muchos datos y debe revisarse antes de existir. `RentalRequestBuilder` los arma paso a paso y `build()` valida todo. Clases: `RentalRequestBuilder`, `RentalRequest`.
- **Facade (RF-002):** el proceso de reserva tiene varios pasos que no deben vivir en la pantalla. `RentalFacade.processRental()` los esconde en un solo método. Clases: `RentalFacade`, `ReservationRepository`, `PaymentGateway`.
- **Abstract Factory (RF-003):** cada plan tiene su fábrica y quien la usa pide `createPlan()` sin conocer la clase concreta. Clases: `RentalPlan`, `RentalPlanFactory`, `BasicRentalPlan`, `StandardRentalPlan`, `PremiumRentalPlan`, sus tres fábricas y `PlanCatalog`.
- **Adapter (RF-004):** la API de pagos usa su propio formato. `PaymentApiAdapter` lo traduce al formato de la aplicación y convierte los errores en `TECHNICAL_ERROR`. Clases: `PaymentGateway`, `PaymentApiAdapter`, `PaymentApiClient`.
- **Decorator (RF-005):** los extras se van envolviendo uno sobre otro y suman su costo, sin una clase por cada combinación. Clases: `ServiceComponent`, `NoExtraServices`, `ExtraServiceDecorator`, `GpsDecorator`, `ChildSeatDecorator`, `ExtraDriverDecorator`.
- **Proxy (RF-006):** se verifica al cliente antes de cobrar. Si no está autorizado, no se llama a la API. Clases: `PaymentGateway`, `CustomerVerificationProxy`, `CustomerVerifier`.
- **Composite (RF-007):** un paquete y un servicio suelto se usan igual, y un paquete puede contener otros paquetes. Clases: `ServiceComponent`, `SingleService`, `ServiceBundle`.

El diagrama UML está en `docs/uml/` y la explicación completa de cada patrón, en el documento técnico.

## 6. Ejecución de la aplicación

### Requisitos

- JDK 21.
- Conexión a Internet la primera vez que Gradle necesite descargar la distribución o dependencias.

### Ejecutar

Desde la raíz del proyecto:

- **Windows PowerShell:** `.\gradlew.bat run`
- **Git Bash / macOS / Linux:** `./gradlew run`

En IntelliJ IDEA también se puede ejecutar la tarea `application > run` desde la ventana Gradle.

### Ejecutar las pruebas

- **Windows PowerShell:** `.\gradlew.bat test`
- **Git Bash / macOS / Linux:** `./gradlew test`

## 7. Integración de pagos

Los pagos se hacen con la API externa `payment-api.jar`, que está en la carpeta `external`.

**Cómo iniciarla** (en otra terminal, antes de hacer una reserva real):

1. Entrar a la carpeta `external`.
2. Ejecutar `java -jar payment-api.jar`.
3. Comprobar que responde en `http://localhost:8080/health`.

**Cómo se conecta con la aplicación:**

- `PaymentApiAdapter` envía el pago a `POST http://localhost:8080/api/payments`.
- `CustomerVerificationProxy` va delante del adaptador y verifica al cliente primero.
- `RentalFacade` solo conoce `PaymentGateway`, no la API.

**Tokens de prueba:**

- `tok_approved`: pago aprobado, la reserva queda `CONFIRMED`.
- `tok_declined`: pago rechazado, la reserva queda `FAILED`.
- `tok_api_error`: la API responde con error 503, la reserva queda `PENDING`.

No se guardan claves ni datos reales de pago.

## 8. Pruebas realizadas

Las pruebas son unitarias, con JUnit 5, y están en `src/test/java/edu/rentcar/service`. Las dependencias externas (pago y verificación) se reemplazan por versiones falsas, o por un servidor local de prueba, para no depender de la API real.

- `RentalRequestBuilderTest` (RF-001): solicitud válida y fechas al revés.
- `RentalFacadeTest` (RF-002): alquiler exitoso y alquiler fallido.
- `RentalFacadeStatusTest` (RF-002): estado de la reserva (`CONFIRMED`, `PENDING` o `FAILED`) según el resultado del pago.
- `RentalPlanFactoryTest` (RF-003): costo del plan Premium y búsqueda en el catálogo.
- `PaymentApiAdapterTest` (RF-004): respuestas de la API de pagos convertidas al formato de la aplicación.
- `ExtraServiceDecoratorTest` (RF-005): extras apilados y extra sin servicio base.
- `ExtrasAssemblerTest` (RF-005): armado de los extras a partir de las opciones elegidas.
- `CustomerVerificationProxyTest` (RF-006): cliente autorizado y no autorizado, con o sin cobro.
- `ServiceBundleTest` (RF-007): descuento del paquete y paquete dentro de otro.

Para ejecutarlas: `.\gradlew.bat test`. Resultado: `[completar, por ejemplo: todas las pruebas pasan]`.

## 9. Limitaciones y decisiones de diseño

**Decisiones:**

- Estados de la reserva: `CONFIRMED` si todo sale bien; `FAILED` si la solicitud es inválida, el vehículo no está libre, el cliente no está autorizado o el pago es rechazado; `PENDING` si hay un fallo técnico (API caída o error de verificación), que puede reintentarse.
- Solo las reservas `CONFIRMED` bloquean al vehículo. Dos reservas se cruzan si `nuevoInicio < finExistente` y `inicioExistente < nuevoFin`; la fecha de fin es el día de devolución.
- El costo total es el costo base del plan más el costo de los servicios, calculado con los días de alquiler.
- `RentalRequest` es inmutable y el Builder es el único lugar donde se valida la solicitud.
- El proxy y el adaptador implementan la misma interfaz, `PaymentGateway`, así que `RentalFacade` no cambia si se agrega o se quita el proxy.

**Limitaciones:**

- Todo se guarda en memoria: al cerrar la aplicación se pierden las reservas.
- No hay cancelación ni modificación de reservas, porque el enunciado no las pide.
- Cada fábrica de planes crea un solo producto, el plan, que ya incluye cobertura, asistencia y costo.
- Las pruebas automáticas usan pagos y verificaciones simulados; el pago real solo se probó a mano con la API.

## 10. Evidencias

- Diagrama UML: `docs/uml/[nombre del archivo].png`.
- Documento técnico: `[enlace o nombre del archivo]`.
- Video de sustentación: `[enlace]`.
- Historial de commits: en el repositorio, con las ramas y los pull requests de cada integrante.
