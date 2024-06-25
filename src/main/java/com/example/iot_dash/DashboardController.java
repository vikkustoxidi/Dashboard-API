package com.example.iot_dash;

import javafx.collections.ListChangeListener;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.control.Button;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

public class DashboardController {
    @FXML
    private Button LogoutButton;
    @FXML
    private Button RefreshButton;
    @FXML
    private WebView Buienradar;
    @FXML
    private LineChart<String, Number> GrondTempChart;
    @FXML
    private LineChart<String, Number> LuchtTempChart;
    @FXML
    private LineChart<String, Number> GrondVochtChart;
    @FXML
    private LineChart<String, Number> LuchtVochtChart;
    @FXML
    private BarChart<String, Number> RegenChart;
    @FXML
    private TextField SensorType;
    @FXML
    private TextField SensorLocDescr;
    @FXML
    private TextField SensorDiep;
    @FXML
    private Button AddButton;
    @FXML
    private Label WarningLabel;
    @FXML
    private Label Begroeting;

    @FXML
    public void initialize() throws Exception {

        LocalTime currentTime = LocalTime.now();
        int currentHour = currentTime.getHour();
        if (currentHour >= 5 && currentHour < 12) {
            Begroeting.setText("Goedemorgen!");
        } else if (currentHour >= 12 && currentHour < 17) {
            Begroeting.setText("Goedemiddag!");
        } else if (currentHour >= 17 && currentHour < 21) {
            Begroeting.setText("Goedeavond!");
        } else {
            Begroeting.setText("Goedenacht!");
        }



        RefreshButton.setOnAction(event -> {
            try {
                refreshCharts();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });



        LogoutButton.setOnAction(e -> handleLogoutButtonAction());



        WebEngine webEngine = Buienradar.getEngine();
        webEngine.load("https://gadgets.buienradar.nl/gadget/zoommap/?lat=51.965&lng=6.28889&overname=2&zoom=10&naam=Doetinchem&size=3&voor=1");
        Buienradar.getChildrenUnmodifiable().addListener((ListChangeListener<Node>) change -> {
            Set<Node> deadSeaScrolls = Buienradar.lookupAll(".scroll-bar");
            for (Node scroll : deadSeaScrolls) {
                scroll.setVisible(false);
            }
        });
        Buienradar.setMaxSize(456, 405);
        Buienradar.addEventHandler(Event.ANY, Event::consume);
        Buienradar.addEventFilter(Event.ANY, Event::consume);
        webEngine.setOnAlert(Event::consume);



        ApiClient apiClient = new ApiClient();

        Map<Integer, LineChart<String, Number>> sensorChartMap = new HashMap<>();
        sensorChartMap.put(23, GrondTempChart);
        sensorChartMap.put(26, GrondVochtChart);
        sensorChartMap.put(21, LuchtTempChart);
        sensorChartMap.put(22, LuchtVochtChart);

        for (Map.Entry<Integer, LineChart<String, Number>> entry : sensorChartMap.entrySet()) {
            int sensorID = entry.getKey();
            LineChart<String, Number> chart = entry.getValue();

            List<JSONObject> data = apiClient.getLast24hFromSensor(sensorID);

            XYChart.Series<String, Number> series = new XYChart.Series<>();

            for (int i = data.size() - 1; i >= 0; i--) {
                JSONObject jsonObject = data.get(i);
                String timestamp = jsonObject.getString("Timestamp");

                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("HH:mm");
                LocalDateTime dateTime = LocalDateTime.parse(timestamp, inputFormatter);
                String formattedTimestamp = dateTime.format(outputFormatter);

                Float waarde = jsonObject.getFloat("Waarde");
                series.getData().add(new XYChart.Data<>(formattedTimestamp, waarde));
            }

            chart.getData().add(series);
        }

        Map<Integer, BarChart<String,Number>> sensorBarChartMap = new HashMap<>();
        sensorBarChartMap.put(30, RegenChart);

        for (Map.Entry<Integer, BarChart<String, Number>> entry : sensorBarChartMap.entrySet()) {
            int sensorID = entry.getKey();
            BarChart<String, Number> chart = entry.getValue();

            List<JSONObject> data = apiClient.getLast6hFromSensor(sensorID);

            XYChart.Series<String, Number> series = new XYChart.Series<>();

            for (int i = data.size() - 1; i >= 0; i--) {
                JSONObject jsonObject = data.get(i);
                String timestamp = jsonObject.getString("Timestamp");

                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("HH:mm");
                LocalDateTime dateTime = LocalDateTime.parse(timestamp, inputFormatter);
                String formattedTimestamp = dateTime.format(outputFormatter);

                Float waarde = jsonObject.getFloat("Waarde");
                series.getData().add(new XYChart.Data<>(formattedTimestamp, waarde));
            }

            chart.getData().add(series);
        }



        AddButton.setOnAction(event -> {
            String type = SensorType.getText();
            String locatieBeschrijving = SensorLocDescr.getText();
            String diepteStr = SensorDiep.getText();

            if (type.isEmpty() || locatieBeschrijving.isEmpty() || diepteStr.isEmpty()) {
                WarningLabel.setText("Alle velden moeten gevuld zijn.");
                WarningLabel.setStyle("-fx-text-fill: #ff0000;");
            } else if (!diepteStr.matches("\\d+")) {
                WarningLabel.setText("Sensordiepte moet een getal zijn.");
                WarningLabel.setStyle("-fx-text-fill: #ff0000;");
            } else {
                try {
                    int diepte = Integer.parseInt(diepteStr);

                    apiClient.addNewSensor(type, locatieBeschrijving, diepte);

                    WarningLabel.setText("Sensor toegevoegd!");
                    WarningLabel.setStyle("-fx-text-fill: #07ba07;");
                    SensorType.clear();
                    SensorLocDescr.clear();
                    SensorDiep.clear();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    private void handleLogoutButtonAction() {
        try {
            // Get the current Stage
            Stage currentStage = (Stage) LogoutButton.getScene().getWindow();

            Login login = new Login();
            login.start(currentStage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshCharts() throws Exception {
        ApiClient apiClient = new ApiClient();

        Map<Integer, LineChart<String, Number>> sensorChartMap = new HashMap<>();
        sensorChartMap.put(23, GrondTempChart);
        sensorChartMap.put(26, GrondVochtChart);
        sensorChartMap.put(21, LuchtTempChart);
        sensorChartMap.put(22, LuchtVochtChart);

        for (Map.Entry<Integer, LineChart<String, Number>> entry : sensorChartMap.entrySet()) {
            int sensorID = entry.getKey();
            LineChart<String, Number> chart = entry.getValue();
            chart.getData().clear(); // Clear old data

            List<JSONObject> data = apiClient.getLast24hFromSensor(sensorID);

            XYChart.Series<String, Number> series = new XYChart.Series<>();

            for (int i = data.size() - 1; i >= 0; i--) {
                JSONObject jsonObject = data.get(i);
                String timestamp = jsonObject.getString("Timestamp");

                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("HH:mm");
                LocalDateTime dateTime = LocalDateTime.parse(timestamp, inputFormatter);
                String formattedTimestamp = dateTime.format(outputFormatter);

                Float waarde = jsonObject.getFloat("Waarde");
                series.getData().add(new XYChart.Data<>(formattedTimestamp, waarde));
            }

            chart.getData().add(series);
        }

        Map<Integer, BarChart<String,Number>> sensorBarChartMap = new HashMap<>();
        sensorBarChartMap.put(30, RegenChart);

        for (Map.Entry<Integer, BarChart<String, Number>> entry : sensorBarChartMap.entrySet()) {
            int sensorID = entry.getKey();
            BarChart<String, Number> chart = entry.getValue();
            chart.getData().clear(); // Clear old data

            List<JSONObject> data = apiClient.getLast6hFromSensor(sensorID);

            XYChart.Series<String, Number> series = new XYChart.Series<>();

            for (int i = data.size() - 1; i >= 0; i--) {
                JSONObject jsonObject = data.get(i);
                String timestamp = jsonObject.getString("Timestamp");

                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("HH:mm");
                LocalDateTime dateTime = LocalDateTime.parse(timestamp, inputFormatter);
                String formattedTimestamp = dateTime.format(outputFormatter);

                Float waarde = jsonObject.getFloat("Waarde");
                series.getData().add(new XYChart.Data<>(formattedTimestamp, waarde));
            }

            chart.getData().add(series);
        }
    }


}