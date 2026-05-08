package com.cse241.hotel.ui.controller;

import com.cse241.hotel.net.ChatRole;
import com.cse241.hotel.net.ChatServer;
import com.cse241.hotel.net.client.ChatClient;
import com.cse241.hotel.ui.Navigator;
import com.cse241.hotel.ui.Session;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ChatController implements Initializable {

    private static final int DEFAULT_PORT = 5555;

    @FXML
    private Label statusLabel;
    @FXML
    private TextField hostField;
    @FXML
    private TextField portField;
    @FXML
    private ChoiceBox<ChatRole> roleChoice;
    @FXML
    private TextField usernameField;

    @FXML
    private ListView<String> messagesList;
    @FXML
    private TextField messageField;
    @FXML
    private Button sendButton;
    @FXML
    private Button connectButton;
    @FXML
    private Button disconnectButton;

    private ChatClient client;
    private ChatServer localServer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        hostField.setText("127.0.0.1");
        portField.setText(String.valueOf(DEFAULT_PORT));

        roleChoice.getItems().setAll(ChatRole.GUEST, ChatRole.RECEPTIONIST);
        roleChoice.setValue(ChatRole.GUEST);

        if (Session.getCurrentGuest() != null) {
            usernameField.setText(Session.getCurrentGuest().getUsername());
        } else {
            usernameField.setText("guest");
        }

        roleChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV == ChatRole.RECEPTIONIST && (usernameField.getText() == null || usernameField.getText().isBlank())) {
                usernameField.setText("reception");
            }
        });

        messageField.textProperty().addListener((obs, oldV, newV) -> updateSendState());
        updateUiState(false);
        updateSendState();
        setStatus("Not connected.");
    }

    @FXML
    private void onBack() {
        if (client != null) {
            client.disconnect();
            client = null;
        }
        Navigator.goTo(Navigator.DASHBOARD);
    }

    @FXML
    private void onStartLocalServer() {
        if (localServer != null && localServer.isRunning()) {
            setStatus("Server already running on port " + localServer.getPort());
            return;
        }
        int port = parsePort();
        if (port <= 0) {
            setStatus("Invalid port.");
            return;
        }
        localServer = new ChatServer(port);
        Thread t = new Thread(() -> {
            try {
                localServer.start();
                Platform.runLater(() -> setStatus("Local server started on port " + port));
            } catch (IOException e) {
                Platform.runLater(() -> setStatus("Failed to start server: " + e.getMessage()));
            }
        }, "chat-server-ui-starter");
        t.setDaemon(true);
        t.start();
    }

    @FXML
    private void onConnect() {
        if (client != null && client.isConnected()) {
            return;
        }

        String host = hostField.getText();
        int port = parsePort();
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        ChatRole role = roleChoice.getValue();

        if (host == null || host.isBlank()) {
            setStatus("Host is required.");
            return;
        }
        if (port <= 0) {
            setStatus("Invalid port.");
            return;
        }
        if (username.isEmpty()) {
            setStatus("Username is required.");
            return;
        }
        if (role == null) {
            setStatus("Role is required.");
            return;
        }

        setStatus("Connecting...");
        updateUiState(true);

        client = new ChatClient(host.trim(), port, username, role, new ChatClient.Listener() {
            @Override
            public void onSystem(String text) {
                Platform.runLater(() -> addMessage("[system] " + text));
            }

            @Override
            public void onMessage(String fromUsername, ChatRole fromRole, String text) {
                Platform.runLater(() -> addMessage(fromRole + " " + fromUsername + ": " + text));
            }

            @Override
            public void onDisconnected(String reason) {
                Platform.runLater(() -> {
                    addMessage("[system] " + reason);
                    setStatus("Disconnected.");
                    updateUiState(false);
                });
            }
        });

        Thread t = new Thread(() -> {
            try {
                client.connect();
                Platform.runLater(() -> {
                    setStatus("Connected to " + host.trim() + ":" + port);
                    updateUiState(false);
                    updateSendState();
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    setStatus("Connection failed: " + e.getMessage());
                    updateUiState(false);
                });
            }
        }, "chat-client-connect");
        t.setDaemon(true);
        t.start();
    }

    @FXML
    private void onDisconnect() {
        if (client != null) {
            client.disconnect();
            client = null;
        }
        setStatus("Disconnected.");
        updateUiState(false);
    }

    @FXML
    private void onSend() {
        if (client == null || !client.isConnected()) {
            return;
        }
        String text = messageField.getText();
        if (text == null || text.isBlank()) {
            return;
        }
        client.sendMessage(text);
        messageField.clear();
        updateSendState();
    }

    private void addMessage(String msg) {
        messagesList.getItems().add(msg);
        messagesList.scrollTo(messagesList.getItems().size() - 1);
    }

    private void setStatus(String text) {
        statusLabel.setText(text == null ? "" : text);
    }

    private void updateUiState(boolean busy) {
        boolean connected = client != null && client.isConnected();

        boolean lockConnectionFields = busy || connected;
        hostField.setDisable(lockConnectionFields);
        portField.setDisable(lockConnectionFields);
        roleChoice.setDisable(lockConnectionFields);
        usernameField.setDisable(lockConnectionFields);

        connectButton.setDisable(busy || connected);
        disconnectButton.setDisable(!(busy || connected));

        messageField.setDisable(!connected);
    }

    private void updateSendState() {
        boolean connected = client != null && client.isConnected();
        boolean hasText = messageField.getText() != null && !messageField.getText().isBlank();
        sendButton.setDisable(!connected || !hasText);
    }

    private int parsePort() {
        try {
            return Integer.parseInt(portField.getText().trim());
        } catch (RuntimeException e) {
            return -1;
        }
    }
}

