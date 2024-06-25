package com.example.iot_dash;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import java.io.IOException;
import java.util.Objects;

public class Login extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Login.class.getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 960 , 720);
        scene.getStylesheets().add(Objects.requireNonNull(Login.class.getResource("style.css")).toExternalForm());

        Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Images/GCicon.png")));
        stage.getIcons().add(icon);
        stage.setTitle("IoT Login");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}