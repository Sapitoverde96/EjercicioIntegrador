package com.cesur.valciro.presentation.controllers;

import com.cesur.valciro.business.models.Company;
import com.cesur.valciro.business.models.Offer;
import com.cesur.valciro.business.models.OfferStatus;
import com.cesur.valciro.data.dao.BasicDAO;
import com.cesur.valciro.presentation.ViewManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

/**
 * Pantalla del formulario de candidaturas
 */
public class CreateOfferController implements Initializable {
    @FXML
    private Label lbTitle;
    @FXML
    private TextField tfOfferTitle;
    @FXML
    private ChoiceBox<Company> cbOfferCompany;
    @FXML
    private ChoiceBox<OfferStatus> cbOfferStatus;
    @FXML
    private DatePicker dpOfferDate;
    @FXML
    private TextField tfOfferLink;
    @FXML
    private TextField tfOfferSalary;
    @FXML
    private TextArea taOfferDescription;
    @FXML
    private TextArea taOfferNotes;
    @FXML
    private Label lbFormSubtitle;

    private final BasicDAO<Offer> offerDAO;
    private final BasicDAO<Company> companyDAO;
    private final Offer offerToEdit;

    /**
     * Prepara el controlador con los datos necesarios y la candidatura que se va a editar si la hay.
     *
     * @param offerDAO    acceso a los datos de las candidaturas
     * @param companyDAO  acceso a los datos de las empresas
     * @param offerToEdit candidatura que se está editando, o {@code null} si es nueva
     */
    public CreateOfferController(BasicDAO<Offer> offerDAO, BasicDAO<Company> companyDAO, Offer offerToEdit) {
        this.offerDAO = offerDAO;
        this.companyDAO = companyDAO;
        this.offerToEdit = offerToEdit;
    }

    /**
     * Prepara el formulario al abrirlo: carga las empresas y los estados disponibles
     *
     * @throws RuntimeException si no se pueden cargar las empresas
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cbOfferCompany.getItems().setAll(companyDAO.findAll());
        cbOfferStatus.getItems().setAll(OfferStatus.values());

        if (offerToEdit == null) {
            lbTitle.setText("Nueva candidatura");
            lbFormSubtitle.setText("Añade una oportunidad y comienza a seguir su progreso.");
            dpOfferDate.setValue(LocalDate.now());
            cbOfferStatus.setValue(OfferStatus.PENDING);
            return;
        }

        lbTitle.setText("Editar candidatura");
        lbFormSubtitle.setText("Actualiza los datos de la candidatura seleccionada.");
        tfOfferTitle.setText(offerToEdit.getTitle());
        cbOfferCompany.getItems().stream()
                .filter(company -> company.getId().equals(offerToEdit.getCompany().getId()))
                .findFirst()
                .ifPresent(cbOfferCompany::setValue);
        cbOfferStatus.setValue(offerToEdit.getStatus());
        dpOfferDate.setValue(offerToEdit.getDate());
        tfOfferLink.setText(offerToEdit.getLink() == null ? "" : offerToEdit.getLink());
        tfOfferSalary.setText(offerToEdit.getSalary() == null ? "" : String.valueOf(offerToEdit.getSalary()));
        taOfferDescription.setText(offerToEdit.getDescription() == null ? "" : offerToEdit.getDescription());
        taOfferNotes.setText(offerToEdit.getNotes() == null ? "" : offerToEdit.getNotes());
    }

    /**
     * Guarda la candidatura del formulario, creándola o actualizándola según corresponda,
     * y vuelve a la lista de candidaturas. Muestra un aviso si faltan datos obligatorios
     * o  si el salario no es unnúmero valido.
     *
     */
    @FXML
    public void saveOffer(ActionEvent actionEvent) {
        try {
            String title = tfOfferTitle.getText().trim();
            Company company = cbOfferCompany.getValue();
            OfferStatus status = cbOfferStatus.getValue();
            LocalDate date = dpOfferDate.getValue();

            if (title.isEmpty()) {
                throw new IllegalArgumentException("El título del puesto es obligatorio.");
            }
            if (company == null) {
                throw new IllegalArgumentException("Debes seleccionar una empresa.");
            }
            if (status == null) {
                throw new IllegalArgumentException("Debes seleccionar un estado.");
            }
            if (date == null) {
                throw new IllegalArgumentException("La fecha de postulación es obligatoria.");
            }

            Double salary = null;
            String salaryText = tfOfferSalary.getText().trim();
            if (!salaryText.isEmpty()) {
                salary = Double.parseDouble(salaryText.replace(',', '.'));
                if (salary < 0) {
                    throw new IllegalArgumentException("El salario no puede ser negativo.");
                }
            }

            Offer offer = offerToEdit == null ? new Offer() : offerToEdit;
            offer.setTitle(title);
            offer.setCompany(company);
            offer.setStatus(status);
            offer.setDate(date);
            offer.setLink(emptyToNull(tfOfferLink.getText()));
            offer.setDescription(emptyToNull(taOfferDescription.getText()));
            offer.setNotes(emptyToNull(taOfferNotes.getText()));
            offer.setSalary(salary);

            if (offer.getId() == null) {
                offerDAO.save(offer);
            } else {
                offerDAO.update(offer);
            }
            ViewManager.changeToWithInjection("offer-view", "Jobflow: Candidaturas");
        } catch (NumberFormatException exception) {
            showError("El salario debe ser un número válido.");
        } catch (RuntimeException exception) {
            showError(exception.getMessage());
        }
    }

    /**
     * Cierra el formulario sin guardar y vuelve a la lista de candidaturas
     *
     * @param actionEvent evento que lanza la acción
     */
    @FXML
    public void cancel(ActionEvent actionEvent) {
        ViewManager.changeToWithInjection("offer-view", "Jobflow: Candidaturas");
    }

    /**
     * Convierte un texto vacío en {@code null} para no guardar campos sin datos.
     *
     * @param value texto que se quiere comprobar
     * @return el texto recortado o {@code null} si estaba vacío
     */
    private String emptyToNull(String value) {
        String trimmed = value == null ? "" : value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
    }
}
