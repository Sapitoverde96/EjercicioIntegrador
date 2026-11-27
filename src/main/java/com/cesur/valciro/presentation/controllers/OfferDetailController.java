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

    /**
     * Preapara el controlador con la candidtura
     *
     * @param offerDAO     acceso a los datos de las candidaturas
     * @param interviewDAO acceso a los datos de las entrevistas
     * @param offer        candidatura que se quiere ver
     */
    public OfferDetailController(BasicDAO<Offer> offerDAO, BasicDAO<Interview> interviewDAO, Offer offer) {
        this.offerDAO = offerDAO;
        this.interviewDAO = interviewDAO;
        this.offer = offer;
    }

    /**
     * Cyarga la candidatura y sus entrevistas y muestra sus datos en pantalla.
     *
     * @throws RuntimeException si no se pueden cargar los datos
     */
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

    /**
     * Abre el formulario para editar la candidatura que se está viendo.
     *
     * @param actionEvent evento que dispara la acción
     */
    @FXML
    public void editOffer(ActionEvent actionEvent) {
        ViewManager.openOfferForm(offer);
    }

    /**
     * Abre el formulario para añadir una entrevista a la candidatura.
     *
     */
    @FXML
    public void addInterview(ActionEvent actionEvent) {
        ViewManager.openInterviewManager(offer, null, true);
    }

    /**
     * Abre el formulario para editar la entrevista seleccionada en la tabla.
     * Si no hay ninguna seleccionada muestra un aviso.
     *
     * @param actionEvent evento que lanza la acción
     */
    @FXML
    public void editInterview(ActionEvent actionEvent) {
        Interview selected = tvDetailInterviews.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Selecciona una entrevista de la tabla.");
            return;
        }
        ViewManager.openInterviewManager(offer, selected, true);
    }

    /**
     * Pide confirmación y borra la entrevista seleccionada en la tabla.
     * Si no hay ninguna seleccionada, muestra un aviso.
     * @throws RuntimeException si no se puede borrar la entrevista
     */
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

    /**
     * Vuelve a la pantalla de candidaturas.
     *
     * @param actionEvent evento que dispara la acción
     */
    @FXML
    public void back(ActionEvent actionEvent) {
        ViewManager.changeToWithInjection("offer-view", "Jobflow: Candidaturas");
    }

    /**
     * Abre la pantalla de candidaturas.
     *
     * @param actionEvent evento que dispara la acción
     */
    @FXML
    public void goToOffers(ActionEvent actionEvent) {
        ViewManager.changeToWithInjection("offer-view", "Jobflow: Candidaturas");
    }

    /**
     * Abre la pantalla de empresas.
     *
     * @param actionEvent evento que dispara la acción
     */
    @FXML
    public void goToCompanies(ActionEvent actionEvent) {
        ViewManager.changeToWithInjection("company-view", "Jobflow: Empresas");
    }

    /**
     * Abre la pantalla de entrevistas.
     *
     */
    @FXML
    public void goToInterviews(ActionEvent actionEvent) {
        ViewManager.openInterviewManager(null, null, false);
    }

    private void setUpTable() {
        tcDetailInterviewId.setCellValueFactory(row -> new SimpleObjectProperty<>(row.getValue().getId()));
        tcDetailInterviewDate.setCellValueFactory(row -> new SimpleObjectProperty<>(row.getValue().getDate()));
        tcDetailInterviewDate.setCellFactory(column -> new TableCell<>() {
            /**
             * Muestra la fecha con formato dd/MM/yyyy, o nada si la celda está vacía.
             *
             * @param date  fecha que se va a mostrar
             * @param empty indica si la celda está vacía
             */
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

    /**
     * Muestra en pantalla los datos de la candidatura: empresa, estado, fecha, salario,
     * enlace, descripción y notas.
     */
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

    /**
     * Carga en la tabla las entrevistas de la candidatura y actualiza el contador
     *
     * @throws RuntimeException si no se pueden cargar las entrevistas
     */
    private void loadInterviews() {
        var interviews = interviewDAO.findAll().stream()
                .filter(interview -> offer.getId().equals(interview.getOffer().getId()))
                .toList();
        tvDetailInterviews.getItems().setAll(interviews);
        lbDetailInterviewCount.setText(String.valueOf(interviews.size()));
    }

    /**
     * Devuelve un guion cuando el texto está vacío, para que la pantalla no muestre
     * espacios en blanco.
     *
     * @param value texto que se quiere mostrar
     * @return el texto indicado, o {@code "-"} si está vacío
     */
    private String valueOrDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    /**
     * Muestra un aviso de error en pantalla con el mensaje indicado.
     *
     * @param message texto que se quiere mostrar
     */
    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
    }
}
