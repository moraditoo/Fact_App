package ni.edu.uam.fact_app.application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ni.edu.uam.fact_app.util.SceneManager;

public class FacturacionApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        SceneManager.setPrimaryStage(stage);

        // La aplicación arranca en el Login
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/ni/edu/uam/fact_app/fxml/login-view.fxml")
        );
        stage.setTitle("Facturacion App - Iniciar Sesión");
        stage.setScene(new Scene(loader.load()));
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}