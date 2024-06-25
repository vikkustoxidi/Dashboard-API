package com.example.iot_dash;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class ApiClient {
    private static final String BASE_URL = "http://vhoebee.gcmsi.nl/api/";
    private static final String API_KEY = "123456";
    private final HttpClient client;

    public ApiClient() {
        this.client = HttpClient.newHttpClient();
    }

    public String getCredentials(String username) throws Exception {
        String url = BASE_URL + "users/" + username;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .header("Authorization", API_KEY)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JSONObject users = new JSONObject(response.body());
            if (!users.isEmpty()) {
                return users.getString("Password");
            }
        } else {
            throw new RuntimeException("Error: " + response.statusCode() + " - " + response.body());
        }
        return null;
    }

    public Float getLastValueFromSensor(int sensorID) throws Exception {
        String url = BASE_URL + "metingen/filters?aantal=1&sorteerVolgorde=ASC&sensorID=" + sensorID;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .header("Authorization", API_KEY)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JSONArray metingen = new JSONArray(response.body());
            if (!metingen.isEmpty()) {
                JSONObject meting = metingen.getJSONObject(0);
                return meting.getFloat("Waarde");
            }
        } else {
            throw new RuntimeException("Error: " + response.statusCode() + " - " + response.body());
        }
        return null;
    }

    public List<JSONObject> getLast24hFromSensor(int sensorID) throws Exception {
        String url = BASE_URL + "metingen/filters?aantal=48&sorteerVolgorde=DESC&sensorID=" + sensorID;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .header("Authorization", API_KEY)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JSONArray metingen = new JSONArray(response.body());
            List<JSONObject> result = new ArrayList<>();
            for (int i = 0; i < metingen.length(); i++) {
                JSONObject meting = metingen.getJSONObject(i);
                JSONObject filteredMeting = new JSONObject();
                filteredMeting.put("Waarde", meting.getFloat("Waarde"));
                filteredMeting.put("Timestamp", meting.getString("Timestamp"));
                result.add(filteredMeting);
            }
            return result;
        } else {
            throw new RuntimeException("Error: " + response.statusCode() + " - " + response.body());
        }
    }

    public List<JSONObject> getLast6hFromSensor(int sensorID) throws Exception {
        String url = BASE_URL + "metingen/filters?aantal=12&sorteerVolgorde=DESC&sensorID=" + sensorID;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .header("Authorization", API_KEY)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JSONArray metingen = new JSONArray(response.body());
            List<JSONObject> result = new ArrayList<>();
            for (int i = 0; i < metingen.length(); i++) {
                JSONObject meting = metingen.getJSONObject(i);
                JSONObject filteredMeting = new JSONObject();
                filteredMeting.put("Waarde", meting.getFloat("Waarde"));
                filteredMeting.put("Timestamp", meting.getString("Timestamp"));
                result.add(filteredMeting);
            }
            return result;
        } else {
            throw new RuntimeException("Error: " + response.statusCode() + " - " + response.body());
        }
    }

    public JSONArray getAllSensors() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(BASE_URL + "sensoren"))
                .header("Authorization", API_KEY)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return new JSONArray(response.body());
        } else {
            throw new RuntimeException("Error: " + response.statusCode() + " - " + response.body());
        }
    }

    public JSONObject addNewSensor(String type, String locatieBeschrijving, int diepte) throws Exception {
        JSONObject newSensor = new JSONObject();
        newSensor.put("Type", type);
        newSensor.put("LocatieBeschrijving", locatieBeschrijving);
        newSensor.put("Diepte", diepte);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(BASE_URL + "sensoren"))
                .header("Authorization", API_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(newSensor.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 201) {
            return new JSONObject(response.body());
        } else {
            throw new RuntimeException("Error: " + response.statusCode() + " - " + response.body());
        }
    }
}