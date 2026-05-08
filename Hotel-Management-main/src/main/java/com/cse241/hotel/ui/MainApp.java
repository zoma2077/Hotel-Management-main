package com.cse241.hotel.ui;

import com.cse241.hotel.db.HotelDatabase;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * JavaFX bootstrap entry point. Seeds dummy data and shows the login screen.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        // Ensure Phase 1 in-memory data is available to all controllers.
        HotelDatabase.seedDummyData();

        Navigator.init(stage);
        stage.setTitle("Hotel Management System");
        stage.setMinWidth(900);
        stage.setMinHeight(620);
        Navigator.goTo(Navigator.LOGIN);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
