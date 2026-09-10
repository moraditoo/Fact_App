package ni.edu.uam.fact_app.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public final class SceneManager {

    private SceneManager() { }

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