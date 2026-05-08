package com.cse241.hotel.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.function.Consumer;

 // Centralized scene/screen switcher.

public final class Navigator {

    public static final String LOGIN = "/fxml/login.fxml";
    public static final String REGISTER = "/fxml/register.fxml";
    public static final String DASHBOARD = "/fxml/dashboard.fxml";
    public static final String STAFF_DASHBOARD = "/fxml/staff-dashboard.fxml";
    public static final String ROOMS = "/fxml/rooms.fxml";
    public static final String RESERVATIONS = "/fxml/reservations.fxml";
    public static final String ADMIN_ROOMS = "/fxml/admin-rooms.fxml";
    public static final String RECEPTIONIST_RESERVATIONS = "/fxml/receptionist-reservations.fxml";
    public static final String CHECKOUT = "/fxml/checkout.fxml";
    public static final String CHAT = "/fxml/chat.fxml";

    private static final String STYLESHEET = "/style.css";

    private static Stage stage;
    private static Scene scene;

    private Navigator() {
    }

    public static void init(Stage primary) {
        stage = Objects.requireNonNull(primary, "Primary stage is required.");
    }

    public static Stage stage() {
        if (stage == null) {
            throw new IllegalStateException("Navigator has not been initialised.");
        }
        return stage;
    }

    /**
     * Loads the supplied FXML and replaces the current scene root.
     *
     * @return the controller instance (so callers can pass context)
     */
    public static <T> T goTo(String fxmlPath) {
        return goTo(fxmlPath, null);
    }

    /**
     * Loads the supplied FXML and invokes {@code initializer} on the controller
     * after construction (useful for passing data such as a selected reservation).
     */
    public static <T> T goTo(String fxmlPath, Consumer<T> initializer) {
        Objects.requireNonNull(fxmlPath, "FXML path is required.");
        try {
            URL fxmlUrl = Navigator.class.getResource(fxmlPath);
            if (fxmlUrl == null) {
                throw new IllegalStateException("FXML not found on classpath: " + fxmlPath);
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            if (scene == null) {
                scene = new Scene(root, 1000, 660);
                stage().setScene(scene);
            } else {
                scene.setRoot(root);
            }

            URL cssUrl = Navigator.class.getResource(STYLESHEET);
            scene.getStylesheets().clear();
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            T controller = loader.getController();
            if (initializer != null && controller != null) {
                initializer.accept(controller);
            }
            return controller;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load FXML: " + fxmlPath, e);
        }
    }
}
