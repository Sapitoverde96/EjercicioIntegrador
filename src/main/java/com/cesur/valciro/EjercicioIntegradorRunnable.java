package com.cesur.valciro;

import com.cesur.valciro.data.config.DatabaseInitializer;
import com.cesur.valciro.data.config.DatasourceFactory;
import com.cesur.valciro.data.dao.CompanyDAO;
import com.cesur.valciro.data.dao.InterviewDAO;
import com.cesur.valciro.data.dao.OfferDAO;
import com.cesur.valciro.presentation.ViewManager;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * Clase principal de la aplicación del Ejercicio Integrador, ahora con JavaFX.
 * @author Ciro Álvarez Garrido
 * @version v2.0
 */
public class EjercicioIntegradorRunnable extends Application {

    /**
     * Función principal de la aplicación. Inicializa todo lo necesario para la ejecución de la aplicación.
     * @param stage Es la ventana principal de la aplicación por la que inicia al ejecutar.
     * @author Ciro Álvarez Garrido
     * @version v2.0
     */
    @Override
    public void start(Stage stage) throws IOException {
        DatabaseInitializer.init(DatasourceFactory.getDataSource());
        Image appIcon = new Image(Objects
                .requireNonNull(getClass().getResourceAsStream("/com/cesur/valciro/JobflowLogo.png"))
        );
        stage.getIcons().add(appIcon);
        ViewManager.setStage(stage);
        ViewManager.setDAOs(
                new CompanyDAO(DatasourceFactory.getDataSource()),
                new OfferDAO(DatasourceFactory.getDataSource()),
                new InterviewDAO(DatasourceFactory.getDataSource())
        );
        ViewManager.changeToWithInjection("offer-view", "Jobflow: Candidaturas");
        stage.show();
    }
}