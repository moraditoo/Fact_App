module ni.edu.uam.fact_app {
    requires javafx.controls;
    requires javafx.fxml;
    requires static lombok;

    opens ni.edu.uam.fact_app to javafx.fxml;
    exports ni.edu.uam.fact_app;
}