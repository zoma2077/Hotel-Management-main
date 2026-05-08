# Hotel Management System — Report

## Overview
This project is a Maven-based Java 17 application with a JavaFX (FXML) user interface that implements a hotel management / reservation workflow. The application uses an **in-memory database** for data storage and demonstrates role-based flows for **Guests**, **Receptionists**, and an **Admin**, plus a **socket-based chat** feature.

## Architecture (MVC)
The codebase follows an MVC-style organization:

- **Model**: Domain objects under `com.cse241.hotel.model.*` (e.g., `Room`, `Reservation`, `Guest`, `Invoice`) plus supporting `enums/`, `exceptions/`, and `interfaces/`.
- **View**: JavaFX FXML screens under `src/main/resources/fxml/` and a shared stylesheet `src/main/resources/style.css` (applied globally).
- **Controller**: JavaFX controllers under `com.cse241.hotel.ui.controller` (one controller per FXML screen).
- **Services**: Stateless service utilities under `com.cse241.hotel.services` that implement business rules on top of the in-memory database.
- **Persistence (in-memory)**: `com.cse241.hotel.db.HotelDatabase` stores collections for guests, staff, rooms, reservations, and invoices and provides seeding + lookup helpers.

Screen navigation is centralized in `com.cse241.hotel.ui.Navigator`, which owns the primary JavaFX `Stage` and swaps the scene root when changing screens.

## Domain model (key entities)
- **Users**
  - `Guest`: end-user who browses rooms and creates reservations.
  - `Staff`: base staff type; concrete roles include `Admin` and `Receptionist`.
  - `Role` enum: distinguishes roles for authentication/authorization logic.
- **Property**
  - `Room`: room number + type + amenities.
  - `RoomType`: name, price per night, capacity.
  - `Amenity`: add-on with optional cost.
- **Transactions**
  - `Reservation`: guest + room + date range + `ReservationStatus` (e.g., pending/confirmed/cancelled).
  - `Invoice`: generated for checkout/payment workflows.
- **Business rules**
  - Overlap/availability checks prevent booking a room for overlapping dates in blocking statuses.

## Services (business logic layer)
Services are implemented as stateless utility classes:

- **Authentication / registration**: `com.cse241.hotel.services.AuthService`
  - Guest registration with uniqueness checks via `HotelDatabase.requireUniqueGuestUsername`.
  - Guest and staff login checks against the in-memory collections.
- **Reservations**: `com.cse241.hotel.services.ReservationService`
  - Creates reservations and enforces date validation and room availability (overlap detection).
  - Supports cancelling and status updates.
- **Rooms / payments**: `RoomService` and `PaymentService`
  - Manage room list operations and checkout/payment validations (see service classes and corresponding tests).

## Database and seeding
All data is stored in-memory via static collections in `com.cse241.hotel.db.HotelDatabase`.

- **Seeding**: `HotelDatabase.seedDummyData()` populates:
  - Staff accounts (Admin, Receptionist)
  - Room types, amenities, and rooms
  - Example guests and initial reservations
- **Bootstrap**: `com.cse241.hotel.ui.MainApp` calls `seedDummyData()` at application startup before showing the login screen.
- **Testing support**: `HotelDatabase.resetForTests()` clears all collections and reseeds, enabling repeatable unit tests.

## JavaFX UI (screens)
FXML screens are located under `src/main/resources/fxml/` and loaded by `Navigator`:

- `login.fxml` / `LoginController`
- `register.fxml` / `RegisterController`
- `dashboard.fxml` / `DashboardController`
- `rooms.fxml` / `RoomBrowseController`
- `reservations.fxml` / `ReservationManagementController`
- `checkout.fxml` / `CheckoutController`
- `admin-rooms.fxml` / `AdminRoomsController`
- `receptionist-reservations.fxml` / `ReceptionistReservationsController`
- `staff-dashboard.fxml` / `StaffDashboardController`
- `chat.fxml` / `ChatController`

Navigation is done via `Navigator.goTo(<FXML_PATH>)`, which loads the FXML, swaps the `Scene` root, and reapplies the global stylesheet.

## Concurrency (Phase 3)
The UI layer uses standard JavaFX concurrency patterns:

- **Background threads for blocking work**: e.g., network/server startup and client connection in `ChatController` are started on daemon threads to keep the UI responsive.
- **UI-thread confinement**: UI updates from background threads use `Platform.runLater(...)`.
- **Reusable executors**: `com.cse241.hotel.ui.concurrent.FxExecutors` provides a daemon-threaded single executor via `DaemonThreadFactory` for safe background task execution when needed.

## Socket chat (Phase 4)
The chat feature uses plain TCP sockets with a simple line-based protocol:

- **Server**: `com.cse241.hotel.net.ChatServer`
  - Uses `ServerSocket` to accept clients on a background accept loop.
  - Maintains a thread-safe client set (concurrent set) and broadcasts messages.
- **Per-client handler**: `com.cse241.hotel.net.ClientHandler`
  - Each client runs in a dedicated daemon thread.
  - Expects an initial join line: `JOIN|username|role`
  - Subsequent messages: `MSG|text`
- **Client**: `com.cse241.hotel.net.client.ChatClient`
  - Connects to server and listens on a background thread.
  - Parses:
    - `SYS|text` for system messages
    - `FROM|username|role|text` for chat messages
- **UI**: `com.cse241.hotel.ui.controller.ChatController`
  - Can start a local server instance.
  - Connects/disconnects and sends messages, updating UI via `Platform.runLater`.

## How to run
From the project root:

```bash
mvn clean javafx:run
```

### Seeded credentials (demo)
- Admin: `admin` / `Admin1234`
- Receptionist: `reception` / `Reception1`

## How to test
Run unit tests from the project root:

```bash
mvn test
```

## Project structure (high level)
- `pom.xml`: Java 17 + JavaFX dependencies and `javafx-maven-plugin` configuration (`mainClass`: `com.cse241.hotel.ui.MainApp`)
- `src/main/java`: application code (models, services, UI, networking)
- `src/main/resources`: FXML + CSS
- `src/test/java`: JUnit 5 tests (reservation overlap, password validation, payment tests)

