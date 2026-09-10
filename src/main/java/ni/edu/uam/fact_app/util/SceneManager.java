package ni.edu.uam.fact_app.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public final class SceneManager {

    private static Stage primaryStage;

    private SceneManager() { }

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void cambiarEscena(String recurso, String titulo) throws IOException {
        var url = SceneManager.class.getResource(recurso);
        if (url == null) {
            throw new IOException("FXML no encontrado: " + recurso);
        }
        FXMLLoader loader = new FXMLLoader(url);
        primaryStage.setTitle(titulo);
        primaryStage.setScene(new Scene(loader.load()));
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void abrirVentana(String recurso, String titulo) throws IOException {
        var url = SceneManager.class.getResource(recurso);
        if (url == null) {
            throw new IOException("FXML no encontrado: " + recurso);
        }
        Stage stage = new Stage();
        stage.setTitle(titulo);
        stage.setScene(new Scene(new FXMLLoader(url).load()));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }
}