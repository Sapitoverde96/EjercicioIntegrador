package com.cesur.valciro.presentation.controllers;

import com.cesur.valciro.business.models.Interview;
import com.cesur.valciro.business.models.InterviewOutcome;
import com.cesur.valciro.business.models.InterviewType;
import com.cesur.valciro.business.models.Offer;
import com.cesur.valciro.data.dao.BasicDAO;
import com.cesur.valciro.presentation.ViewManager;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

public class OfferDetailController implements Initializable {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    private Label lbDetailTitle;
    @FXML
    private Label lbDetailCompany;
    @FXML
    private Label lbDetailIndustry;
    @FXML
    private Label lbDetailWebsite;
    @FXML
    private Label lbDetailStatus;
    @FXML
    private Label lbDetailDate;
    @FXML
    private Label lbDetailSalary;
    @FXML
    private Label lbDetailLink;
    @FXML
    private Label lbDetailDescription;
    @FXML
    private Label lbDetailNotes;
    @FXML
    private Label lbDetailInterviewCount;
    @FXML
    private TableView<Interview> tvDetailInterviews;
    @FXML
    private TableColumn<Interview, Long> tcDetailInterviewId;
    @FXML
    private TableColumn<Interview, LocalDate> tcDetailInterviewDate;
    @FXML
    private TableColumn<Interview, InterviewType> tcDetailInterviewType;
    @FXML
    private TableColumn<Interview, InterviewOutcome> tcDetailInterviewOutcome;
    @FXML
    private TableColumn<Interview, String> tcDetailInterviewNotes;

    private final BasicDAO<Offer> offerDAO;
    private final BasicDAO<Interview> interviewDAO;
    private Offer offer;

    public OfferDetailController(BasicDAO<Offer> offerDAO, BasicDAO<Interview> interviewDAO, Offer offer) {
        this.offerDAO = offerDAO;
        this.interviewDAO = interviewDAO;
        this.offer = offer;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setUpTable();
        if (offer == null) {
            ViewManager.changeToWithInjection("offer-view", "Jobflow: Candidaturas");
            return;
        }
        offerDAO.findById(offer.getId()).ifPresent(found -> offer = found);
        renderOffer();
        loadInterviews();
    }

    @FXML
    public void editOffer(ActionEvent actionEvent) {
        ViewManager.openOfferForm(offer);
    }

    @FXML
    public void addInterview(ActionEvent actionEvent) {
        ViewManager.openInterviewManager(offer, null, true);
    }

    @FXML
    public void editInterview(ActionEvent actionEvent) {
        Interview selected = tvDetailInterviews.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Selecciona una entrevista de la tabla.");
            return;
        }
        ViewManager.openInterviewManager(offer, selected, true);
    }

    @FXML
    public void deleteInterview(ActionEvent actionEvent) {
        Interview selected = tvDetailInterviews.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Selecciona una entrevista de la tabla.");
            return;
        }

        ButtonType cancel = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType delete = new ButtonType("Eliminar", ButtonBar.ButtonData.OK_DONE);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar la entrevista del " + selected.getDate().format(DATE_FORMAT) + "?",
                cancel, delete);
        alert.setTitle("Eliminar entrevista");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isEmpty() || result.get() != delete) {
            return;
        }

        interviewDAO.deleteById(selected.getId());
        loadInterviews();
    }

    @FXML
    public void back(ActionEvent actionEvent) {
        ViewManager.changeToWithInjection("offer-view", "Jobflow: Candidaturas");
    }

    @FXML
    public void goToOffers(ActionEvent actionEvent) {
        ViewManager.changeToWithInjection("offer-view", "Jobflow: Candidaturas");
    }

    @FXML
    public void goToCompanies(ActionEvent actionEvent) {
        ViewManager.changeToWithInjection("company-view", "Jobflow: Empresas");
    }

    @FXML
    public void goToInterviews(ActionEvent actionEvent) {
        ViewManager.openInterviewManager(null, null, false);
    }

    private void setUpTable() {
        tcDetailInterviewId.setCellValueFactory(row -> new SimpleObjectProperty<>(row.getValue().getId()));
        tcDetailInterviewDate.setCellValueFactory(row -> new SimpleObjectProperty<>(row.getValue().getDate()));
        tcDetailInterviewDate.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setText(empty || date == null ? null : date.format(DATE_FORMAT));
            }
        });
        tcDetailInterviewType.setCellValueFactory(row ->
                new SimpleObjectProperty<>(row.getValue().getType()));
        tcDetailInterviewOutcome.setCellValueFactory(row ->
                new SimpleObjectProperty<>(row.getValue().getOutcome()));
        tcDetailInterviewNotes.setCellValueFactory(row -> new SimpleStringProperty(
                row.getValue().getNotes() == null ? "-" : row.getValue().getNotes()));
    }

    private void renderOffer() {
        lbDetailTitle.setText(offer.getTitle());
        lbDetailCompany.setText(offer.getCompany().getName());
        lbDetailIndustry.setText(offer.getCompany().getIndustry());
        lbDetailWebsite.setText(valueOrDash(offer.getCompany().getWebsite()));
        lbDetailStatus.setText(offer.getStatus().toString());
        lbDetailDate.setText(offer.getDate().format(DATE_FORMAT));
        lbDetailSalary.setText(offer.getSalary() == null ? "-" : String.format("%.2f €", offer.getSalary()));
        lbDetailLink.setText(valueOrDash(offer.getLink()));
        lbDetailDescription.setText(valueOrDash(offer.getDescription()));
        lbDetailNotes.setText(valueOrDash(offer.getNotes()));
    }

    private void loadInterviews() {
        var interviews = interviewDAO.findAll().stream()
                .filter(interview -> offer.getId().equals(interview.getOffer().getId()))
                .toList();
        tvDetailInterviews.getItems().setAll(interviews);
        lbDetailInterviewCount.setText(String.valueOf(interviews.size()));
    }

    private String valueOrDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
    }
}
