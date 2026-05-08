package com.cse241.hotel.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

import java.util.Optional;


public final class Dialogs {
    private Dialogs() {
    }

    public static void info(String header, String content) {
        show(AlertType.INFORMATION, "Information", header, content);
    }

    public static void warning(String header, String content) {
        show(AlertType.WARNING, "Warning", header, content);
    }

    public static void error(String header, String content) {
        show(AlertType.ERROR, "Error", header, content);
    }

    public static boolean confirm(String header, String content) {
        Alert alert = new Alert(AlertType.CONFIRMATION, content, ButtonType.YES, ButtonType.NO);
        alert.setTitle("Confirm");
        alert.setHeaderText(header);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.YES;
    }

    private static void show(AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
