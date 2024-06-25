package com.example.iot_dash;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.util.Objects;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private Label WarningLogin;

    @FXML
    public void initialize() {
        passwordField.setOnAction(e -> handleLoginButtonAction());
    }

    @FXML
    public void handleKeyRelease() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        boolean isDisabled = (username.isEmpty() || password.isEmpty());
        loginButton.setVisible(!isDisabled);
    }

    @FXML
    public void handleLoginButtonAction() {

        try {
            ApiClient apiClient = new ApiClient();

            String enteredPassword = passwordField.getText();
            String correctPassword = apiClient.getCredentials(usernameField.getText());

            if (usernameField.getText().isEmpty() || passwordField.getText().isEmpty()) {
                WarningLogin.setVisible(true);
                WarningLogin.setText("Vul beide velden in.");
                WarningLogin.setStyle("-fx-text-fill: red");
            } else if (!Objects.equals(enteredPassword, correctPassword) || correctPassword == null) {
                WarningLogin.setVisible(true);
                WarningLogin.setText("Onjuiste gebruikersnaam of wachtwoord.");
                WarningLogin.setStyle("-fx-text-fill: red");
            } else if (enteredPassword.equals(correctPassword)) {
                Stage currentStage = (Stage) loginButton.getScene().getWindow();

                Dashboard dashboard = new Dashboard();
                dashboard.start(currentStage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}