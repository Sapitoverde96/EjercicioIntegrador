package com.cesur.valciro.presentation.controllers;

import com.cesur.valciro.business.models.Company;
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
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;


public class CompanyController implements Initializable {
    /** Formato en el que se muestran las fechas en la tabla de empresas. */
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    private TableView<Company> tvCompaniesTable;
    @FXML
    private TableColumn<Company, Long> tcCompanyId;
    @FXML
    private TableColumn<Company, String> tcCompanyName;
    @FXML
    private TableColumn<Company, String> tcCompanyIndustry;
    @FXML
    private TableColumn<Company, String> tcCompanyWebsite;
    @FXML
    private TableColumn<Company, LocalDate> tcCompanyDate;
    @FXML
    private TextField tfCompanyName;
    @FXML
    private TextField tfCompanyIndustry;
    @FXML
    private TextField tfCompanyWebsite;
    @FXML
    private DatePicker dpCompanyDate;
    @FXML
    private Label lbCompanyFormTitle;
    @FXML
    private Label lbCompanyFormSubtitle;

    private final BasicDAO<Company> companyDAO;
    private final BasicDAO<Offer> offerDAO;
    private Company editingCompany;


    public CompanyController(BasicDAO<Company> companyDAO, BasicDAO<Offer> offerDAO) {
        this.companyDAO = companyDAO;
        this.offerDAO = offerDAO;
    }

    /**
     * Tabla y formulario al abrir la pantalla,esta carga las empresas guardadas
     * y deja la fecha de hoy por defecto.
     * @throws RuntimeException si no se pueden cargar las empresas
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setUpTable();
        dpCompanyDate.setValue(LocalDate.now());
        tvCompaniesTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        fillForm(newValue);
                    }
                });
        loadCompanies();
    }

    /**
     * Limpia el formulario y lo prepara para dar de alta una empresa nueva.
     *
     * @param actionEvent evento que eejecuta la acción
     */
    @FXML
    public void newCompany(ActionEvent actionEvent) {
        editingCompany = null;
        tvCompaniesTable.getSelectionModel().clearSelection();
        tfCompanyName.clear();
        tfCompanyIndustry.clear();
        tfCompanyWebsite.clear();
        dpCompanyDate.setValue(LocalDate.now());
        lbCompanyFormTitle.setText("Nueva empresa");
        lbCompanyFormSubtitle.setText("Registra una empresa para asociarle candidaturas.");
        tfCompanyName.requestFocus();
    }

    /**
     * Guarda la empresa que haya en el formulario.
     * Si falta algún dato obligatorio, muestra un aviso en pantalla.
     *
     * @param actionEvent evento que dispara la acción
     */
    @FXML
    public void saveCompany(ActionEvent actionEvent) {
        try {
            String name = tfCompanyName.getText().trim();
            String industry = tfCompanyIndustry.getText().trim();
            String website = tfCompanyWebsite.getText().trim();
            LocalDate date = dpCompanyDate.getValue();

            if (name.isEmpty()) {
                throw new IllegalArgumentException("El nombre de la empresa es obligatorio.");
            }
            if (industry.isEmpty()) {
                throw new IllegalArgumentException("El sector de la empresa es obligatorio.");
            }
            if (date == null) {
                throw new IllegalArgumentException("La fecha de registro es obligatoria.");
            }

            Company company = editingCompany == null ? new Company() : editingCompany;
            company.setName(name);
            company.setIndustry(industry);
            company.setWebsite(website.isEmpty() ? null : website);
            company.setDate(date);

            if (company.getId() == null) {
                companyDAO.save(company);
            } else {
                companyDAO.update(company);
            }
            loadCompanies();
            newCompany(null);
        } catch (RuntimeException exception) {
            showError(exception.getMessage());
        }
    }

    /**
     * Borra la empresa seleccionada en la tabla. Si tiene candidaturas asociadas,
     * muestra un aviso antes de borrarla para que el usuario confirme la accion
     * @throws RuntimeException si no se pueden consultar las candidaturas o borrar la empresa
     */
    @FXML
    public void deleteCompany(ActionEvent actionEvent) {
        Company selected = tvCompaniesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Selecciona una empresa de la tabla.");
            return;
        }

        long offerCount = offerDAO.findAll().stream()
                .filter(offer -> selected.getId().equals(offer.getCompany().getId()))
                .count();
        if (offerCount > 0) {
            ButtonType cancel = new ButtonType("Volver", ButtonBar.ButtonData.CANCEL_CLOSE);
            ButtonType delete = new ButtonType("Eliminar", ButtonBar.ButtonData.OK_DONE);
            Alert alert = new Alert(Alert.AlertType.WARNING,
                    "Esta empresa tiene " + offerCount + " candidatura(s) asociada(s) que también se eliminarán.",
                    cancel, delete);
            alert.setTitle("Eliminar empresa");
            alert.setHeaderText("Se eliminarán datos en cascada");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isEmpty() || result.get() != delete) {
                return;
            }
        }

        companyDAO.deleteById(selected.getId());
        loadCompanies();
        newCompany(null);
    }

    /**
     * Abre la pantalla de candidaturas.
     *
     */
    @FXML
    public void goToOffers(ActionEvent actionEvent) {
        ViewManager.changeToWithInjection("offer-view", "Jobflow: Candidaturas");
    }

    /**
     * Abre la pantalla de empresas.
     *
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

    /**
     * Configura las columnas de la tabla de empresas, con la fecha en formato dd/MM/yyyy.
     */
    private void setUpTable() {
        tcCompanyId.setCellValueFactory(row -> new SimpleObjectProperty<>(row.getValue().getId()));
        tcCompanyName.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getName()));
        tcCompanyIndustry.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getIndustry()));
        tcCompanyWebsite.setCellValueFactory(row -> new SimpleStringProperty(
                row.getValue().getWebsite() == null ? "-" : row.getValue().getWebsite()));
        tcCompanyDate.setCellValueFactory(row -> new SimpleObjectProperty<>(row.getValue().getDate()));
        tcCompanyDate.setCellFactory(column -> new TableCell<>() {
            /**
             * Muestra la fecha con formato dd/MM/yyyy
             *
             * @param date  fecha que se va a mostrar
             */
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setText(empty || date == null ? null : date.format(DATE_FORMAT));
            }
        });
    }

    /**
     * Vuelve a cargar el listado de empresas para que la tabla muestre los datos actuales.
     *
     * @throws RuntimeException si no se pueden obtener las empresas
     */
    private void loadCompanies() {
        tvCompaniesTable.getItems().setAll(companyDAO.findAll());
    }

    /**
     * Rellena el formulario con la empresa indicada y lo prepara para editarla.
     *
     * @param company empresa cuyos datos se van a mostrar
     */
    private void fillForm(Company company) {
        editingCompany = company;
        tfCompanyName.setText(company.getName());
        tfCompanyIndustry.setText(company.getIndustry());
        tfCompanyWebsite.setText(company.getWebsite() == null ? "" : company.getWebsite());
        dpCompanyDate.setValue(company.getDate());
        lbCompanyFormTitle.setText("Editar empresa");
        lbCompanyFormSubtitle.setText("Modifica los datos de la empresa seleccionada.");
    }

    /**
     * Muestra un aviso de error en pantalla con el mensaje indicado
     *
     * @param message texto que se quiere mostrar
     */
    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
    }
}
