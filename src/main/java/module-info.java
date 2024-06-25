module com.example.iot_dash {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.net.http;
    requires org.json;
    requires java.datatransfer;
    requires javafx.base;

    opens com.example.iot_dash to javafx.fxml;
    exports com.example.iot_dash;
}