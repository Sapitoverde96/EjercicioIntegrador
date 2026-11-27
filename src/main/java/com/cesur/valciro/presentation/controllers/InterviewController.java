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
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.util.StringConverter;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controlador de la pantalla de entrevistas: muestra la tabla de entrevistas y el
 * formulario para registrarlas o editarlas.
 */
public class InterviewController implements Initializable {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    private TableView<Interview> tvInterviewsTable;
    @FXML
    private TableColumn<Interview, Long> tcInterviewId;
    @FXML
    private TableColumn<Interview, String> tcInterviewOffer;
    @FXML
    private TableColumn<Interview, String> tcInterviewCompany;
    @FXML
    private TableColumn<Interview, LocalDate> tcInterviewDate;
    @FXML
    private TableColumn<Interview, InterviewType> tcInterviewType;
    @FXML
    private TableColumn<Interview, InterviewOutcome> tcInterviewOutcome;
    @FXML
    private TableColumn<Interview, String> tcInterviewNotes;
    @FXML
    private ChoiceBox<Offer> cbInterviewOffer;
    @FXML
    private DatePicker dpInterviewDate;
    @FXML
    private ChoiceBox<InterviewType> cbInterviewType;
    @FXML
    private ChoiceBox<InterviewOutcome> cbInterviewOutcome;
    @FXML
    private TextArea taInterviewNotes;
    @FXML
    private Label lbInterviewFormTitle;
    @FXML
    private Label lbInterviewFormSubtitle;
    @FXML
    private Label lbInterviewCount;

    private final BasicDAO<Offer> offerDAO;
    private final BasicDAO<Interview> interviewDAO;
    private final Offer contextOffer;
    private final Interview interviewToEdit;
    private final boolean returnToDetail;
    private Interview editingInterview;

    /**
     * Crea el controlador con los datos que necesita la pantalla de entrevistas.
     *
     * @param offerDAO DAO de candidaturas, para rellenar la lista de ofertas
     * @param interviewDAO DAO de entrevistas, para consultarlas y guardarlas
     * @param contextOffer candidatura desde la que se abre la pantalla
     * @param interviewToEdit entrevista que se quiere editar
     */
    public InterviewController(BasicDAO<Offer> offerDAO, BasicDAO<Interview> interviewDAO,
                               Offer contextOffer, Interview interviewToEdit, boolean returnToDetail) {
        this.offerDAO = offerDAO;
        this.interviewDAO = interviewDAO;
        this.contextOffer = contextOffer;
        this.interviewToEdit = interviewToEdit;
        this.returnToDetail = returnToDetail;
    }

    /**
     * Prepara la tabla y el formulario, carga las opciones y muestra las entrevistas.
     * @throws RuntimeException si no se pueden cargar las candidaturas o las entrevistas
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setUpTable();
        loadOfferChoices();
        cbInterviewType.getItems().setAll(InterviewType.values());
        cbInterviewOutcome.getItems().setAll(InterviewOutcome.values());
        dpInterviewDate.setValue(LocalDate.now());

        tvInterviewsTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        fillForm(newValue);
                    }
                });

        if (interviewToEdit != null) {
            fillForm(interviewToEdit);
        } else if (contextOffer != null) {
            selectOffer(contextOffer.getId());
        }
        loadInterviews();
    }

    @FXML
    public void newInterview(ActionEvent actionEvent) {
        editingInterview = null;
        tvInterviewsTable.getSelectionModel().clearSelection();
        dpInterviewDate.setValue(LocalDate.now());
        cbInterviewType.setValue(null);
        cbInterviewOutcome.setValue(InterviewOutcome.PENDING);
        taInterviewNotes.clear();
        if (contextOffer == null) {
            cbInterviewOffer.setValue(null);
        } else {
            selectOffer(contextOffer.getId());
        }
        lbInterviewFormTitle.setText("Nueva entrevista");
        lbInterviewFormSubtitle.setText("Registra una entrevista asociada a una candidatura.");
    }

    /**
     * Guarda la entrevista del formulario y refresca la tabla.
     * Si faltan datos obligatorios, muestra un aviso de error.
     *
     */
    @FXML
    public void saveInterview(ActionEvent actionEvent) {
        try {
            Offer offer = cbInterviewOffer.getValue();
            LocalDate date = dpInterviewDate.getValue();
            InterviewType type = cbInterviewType.getValue();
            InterviewOutcome outcome = cbInterviewOutcome.getValue();

            if (offer == null) {
                throw new IllegalArgumentException("Debes seleccionar una candidatura.");
            }
            if (date == null) {
                throw new IllegalArgumentException("La fecha de la entrevista es obligatoria.");
            }
            if (type == null) {
                throw new IllegalArgumentException("Debes seleccionar un tipo de entrevista.");
            }
            if (outcome == null) {
                throw new IllegalArgumentException("Debes seleccionar un resultado.");
            }

            Interview interview = editingInterview == null ? new Interview() : editingInterview;
            interview.setOffer(offer);
            interview.setDate(date);
            interview.setType(type);
            interview.setOutcome(outcome);
            interview.setNotes(taInterviewNotes.getText().isBlank() ? null : taInterviewNotes.getText().trim());

            if (interview.getId() == null) {
                interviewDAO.save(interview);
            } else {
                interviewDAO.update(interview);
            }

            if (returnToDetail && contextOffer != null) {
                ViewManager.openOfferDetail(contextOffer);
            } else {
                loadInterviews();
                newInterview(null);
            }
        } catch (RuntimeException exception) {
            showError(exception.getMessage());
        }
    }

    /**
     * Pide confirmación antes de borrar la entrevista seleccionada
     */
    @FXML
    public void deleteInterview(ActionEvent actionEvent) {
        Interview selected = tvInterviewsTable.getSelectionModel().getSelectedItem();
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
        newInterview(null);
    }

    /**
     * Vuelve al detalle de la candidatura
     *
     * @param actionEvent evento que ejecuta la acción
     */
    @FXML
    public void back(ActionEvent actionEvent) {
        if (returnToDetail && contextOffer != null) {
            ViewManager.openOfferDetail(contextOffer);
        } else {
            ViewManager.changeToWithInjection("offer-view", "Jobflow: Candidaturas");
        }
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

    /**
     * Configura las columnas de la tabla de entrevistas y el formato de la fecha.
     */
    private void setUpTable() {
        tcInterviewId.setCellValueFactory(row -> new SimpleObjectProperty<>(row.getValue().getId()));
        tcInterviewOffer.setCellValueFactory(row ->
                new SimpleStringProperty(row.getValue().getOffer().getTitle()));
        tcInterviewCompany.setCellValueFactory(row ->
                new SimpleStringProperty(row.getValue().getOffer().getCompany().getName()));
        tcInterviewDate.setCellValueFactory(row -> new SimpleObjectProperty<>(row.getValue().getDate()));
        tcInterviewDate.setCellFactory(column -> new TableCell<>() {
            /**
             * Muestra la fecha de la entrevista en formato dd/MM/yyyy, o nada si la celda está vacía.
             *
             * @param date fecha de la entrevista
             * @param empty indica si la celda está vacía
             */
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setText(empty || date == null ? null : date.format(DATE_FORMAT));
            }
        });
        tcInterviewType.setCellValueFactory(row ->
                new SimpleObjectProperty<>(row.getValue().getType()));
        tcInterviewOutcome.setCellValueFactory(row ->
                new SimpleObjectProperty<>(row.getValue().getOutcome()));
        tcInterviewNotes.setCellValueFactory(row -> new SimpleStringProperty(
                row.getValue().getNotes() == null ? "-" : row.getValue().getNotes()));
    }

    /**
     * Carga las candidaturas en el selector ordenadas por titulo
     *
     * @throws RuntimeException si no se pueden consultar las candidaturas
     */
    private void loadOfferChoices() {
        cbInterviewOffer.getItems().setAll(offerDAO.findAll().stream()
                .sorted(Comparator.comparing(Offer::getTitle, String.CASE_INSENSITIVE_ORDER))
                .toList());
        cbInterviewOffer.setConverter(new StringConverter<>() {

            @Override
            public String toString(Offer offer) {
                return offer == null ? "" : offer.getTitle() + " · " + offer.getCompany().getName();
            }

            @Override
            public Offer fromString(String string) {
                return null;
            }
        });
    }

    /**
     * Carga las entrevistas en la tabla y actualiza el contador.
     *
     * @throws RuntimeException si no se pueden consultar las entrevistas
     */
    private void loadInterviews() {
        List<Interview> interviews = interviewDAO.findAll();
        if (returnToDetail && contextOffer != null) {
            interviews = interviews.stream()
                    .filter(interview -> contextOffer.getId().equals(interview.getOffer().getId()))
                    .toList();
        }
        tvInterviewsTable.getItems().setAll(interviews);
        lbInterviewCount.setText(String.valueOf(interviews.size()));
    }

    private void fillForm(Interview interview) {
        editingInterview = interview;
        selectOffer(interview.getOffer().getId());
        dpInterviewDate.setValue(interview.getDate());
        cbInterviewType.setValue(interview.getType());
        cbInterviewOutcome.setValue(interview.getOutcome());
        taInterviewNotes.setText(interview.getNotes() == null ? "" : interview.getNotes());
        lbInterviewFormTitle.setText("Editar entrevista");
        lbInterviewFormSubtitle.setText("Actualiza los datos de la entrevista seleccionada.");
    }

    /**
     * Selecciona en el desplegable la candidatura con el identificador indicado.
     *
     * @param offerId identificador de la candidatura
     */
    private void selectOffer(Long offerId) {
        cbInterviewOffer.getItems().stream()
                .filter(offer -> offer.getId().equals(offerId))
                .findFirst()
                .ifPresent(cbInterviewOffer::setValue);
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
    }
}
