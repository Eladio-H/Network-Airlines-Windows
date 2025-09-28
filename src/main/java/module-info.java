module com.example.networkairlines {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.example.networkairlines to javafx.fxml;
    exports com.example.networkairlines;
}