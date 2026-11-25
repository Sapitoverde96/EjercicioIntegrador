package com.cesur.valciro.presentation;

import com.cesur.valciro.EjercicioIntegradorRunnable;
import com.cesur.valciro.business.models.Company;
import com.cesur.valciro.business.models.Interview;
import com.cesur.valciro.business.models.Offer;
import com.cesur.valciro.data.dao.BasicDAO;
import com.cesur.valciro.presentation.controllers.CompanyController;
import com.cesur.valciro.presentation.controllers.CreateOfferController;
import com.cesur.valciro.presentation.controllers.InterviewController;
import com.cesur.valciro.presentation.controllers.OfferController;
import com.cesur.valciro.presentation.controllers.OfferDetailController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ViewManager {
    private static final String VIEW_PATH = "views/";
    private static final String VIEW_EXTENSION = ".fxml";
    private static Stage stage;

    private static BasicDAO<Company> companyDAO;
    private static BasicDAO<Offer> offerDAO;
    private static BasicDAO<Interview> interviewDAO;

    private static Offer selectedOffer;
    private static Offer offerToEdit;
    private static Offer interviewOffer;
    private static Interview interviewToEdit;
    private static boolean returnToOfferDetail;

    private ViewManager() {
    }

    public static void setStage(Stage stage) {
        ViewManager.stage = stage;
    }

    public static void setDAOs(
            BasicDAO<Company> companyDAO,
            BasicDAO<Offer> offerDAO,
            BasicDAO<Interview> interviewDAO
    ) {
        ViewManager.companyDAO = companyDAO;
        ViewManager.offerDAO = offerDAO;
        ViewManager.interviewDAO = interviewDAO;
    }

    public static void openOfferDetail(Offer offer) {
        selectedOffer = offer;
        changeToWithInjection("offer-detail-view", "Jobflow: Detalle de candidatura");
    }

    public static void openOfferForm(Offer offer) {
        offerToEdit = offer;
        changeToWithInjection("create-offer-view",
                offer == null ? "Jobflow: Nueva candidatura" : "Jobflow: Editar candidatura");
    }

    public static void openInterviewManager(Offer offer, Interview interview, boolean returnToDetail) {
        interviewOffer = offer;
        interviewToEdit = interview;
        returnToOfferDetail = returnToDetail;
        changeToWithInjection("interview-view", "Jobflow: Entrevistas");
    }

    public static void changeToWithInjection(String viewName, String windowTitle) {
        FXMLLoader fxLoader = new FXMLLoader(EjercicioIntegradorRunnable.class.getResource(
                VIEW_PATH + viewName + VIEW_EXTENSION)
        );
        fxLoader.setControllerFactory(controller -> switch (viewName) {
            case "offer-view" -> new OfferController(offerDAO, interviewDAO, companyDAO);
            case "create-offer-view" -> new CreateOfferController(offerDAO, companyDAO, offerToEdit);
            case "offer-detail-view" -> new OfferDetailController(offerDAO, interviewDAO, selectedOffer);
            case "company-view" -> new CompanyController(companyDAO, offerDAO);
            case "interview-view" -> new InterviewController(
                    offerDAO, interviewDAO, interviewOffer, interviewToEdit, returnToOfferDetail);
            default -> throw new IllegalArgumentException("Invalid view name: " + viewName);
        });
        try {
            Scene scene = new Scene(fxLoader.load());
            stage.setTitle(windowTitle);
            stage.setScene(scene);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}
