package com.cesur.valciro.presentation.controllers;

import com.cesur.valciro.business.models.Company;
import com.cesur.valciro.business.models.Interview;
import com.cesur.valciro.business.models.Offer;
import com.cesur.valciro.business.models.OfferStatus;
import com.cesur.valciro.data.dao.BasicDAO;
import com.cesur.valciro.presentation.ViewManager;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class OfferController implements Initializable {
    private static final String ALL_COMPANIES = "Todas las empresas";
    private static final String ALL_STATUSES = "Todos los estados";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public Pane pnOffers;
    public Label lbOffers;
    public HBox hbCard01;
    public Pane pnPendings;
    public Label lbPendings;
    public HBox hbCard02;
    public Pane pnInterviews;
    public Label lbInterviews;
    public HBox hbCard03;
    public Pane pnOfferSpacer;
    public Button btClearFilters;
    public Button btViewOffer;
    public ImageView ivEditOffer;
    public Button btEditOffer;
    public ImageView ivDeleteOffer;
    public Button btDeleteOffer;
    public BorderPane bpInteriorPane;
    public Pane pnLogo;
    public Label lbTitle;
    public Pane pnSpacer;
    public ImageView ivAddIcon;
    public Button btNewOffer;
    public HBox hbUpperBar;
    public ImageView ivOffers;
    public Button btOffers;
    public ImageView ivCompanies;
    public Button btCompanies;
    public ImageView ivInterviews;
    public Button btInterviews;
    public ImageView ivStats;
    public Button btStats;
    public ImageView ivAI;
    public Button btAI;
    public HBox hbLowerBar;
    public BorderPane bpExteriorPane;

    @FXML
    private TableView<Offer> tvOffersTable;
    @FXML
    private TableColumn<Offer, Long> tcOfferId;
    @FXML
    private TableColumn<Offer, String> tcOfferTitle;
    @FXML
    private TableColumn<Offer, String> tcOfferCompany;
    @FXML
    private TableColumn<Offer, OfferStatus> tcOfferStatus;
    @FXML
    private TableColumn<Offer, LocalDate> tcOfferDate;
    @FXML
    private TableColumn<Offer, Double> tcOfferSalary;
    @FXML
    private TextField tfOfferTitle;
    @FXML
    private ChoiceBox<String> cbOfferCompany;
    @FXML
    private ChoiceBox<String> cbOfferStatus;
    @FXML
    private DatePicker dpOfferDate;
    @FXML
    private Slider slOfferSalary;
    @FXML
    private Label lbDynamicSalary;
    @FXML
    private Label lbDynamicOffers;
    @FXML
    private Label lbDynamicPendings;
    @FXML
    private Label lbDynamicInterviews;

    private final BasicDAO<Offer> offerDAO;
    private final BasicDAO<Interview> interviewDAO;
    private final BasicDAO<Company> companyDAO;
    private final ObservableList<Offer> allOffers = FXCollections.observableArrayList();

    public OfferController(BasicDAO<Offer> offerDAO, BasicDAO<Interview> interviewDAO,
                           BasicDAO<Company> companyDAO) {
        this.offerDAO = offerDAO;
        this.interviewDAO = interviewDAO;
        this.companyDAO = companyDAO;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setUpTable();
        refreshFilterOptions();
        setUpFilters();
        loadOffers();
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

    @FXML
    public void addOffer(ActionEvent actionEvent) {
        ViewManager.openOfferForm(null);
    }

    @FXML
    public void showDetail(ActionEvent actionEvent) {
        Offer selected = tvOffersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Selecciona una candidatura de la tabla.");
            return;
        }
        ViewManager.openOfferDetail(selected);
    }

    @FXML
    public void enableEdit(ActionEvent actionEvent) {
        Offer selected = tvOffersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Selecciona una candidatura de la tabla.");
            return;
        }
        ViewManager.openOfferForm(selected);
    }

    @FXML
    public void enableDelete(ActionEvent actionEvent) {
        Offer selected = tvOffersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Selecciona una candidatura de la tabla.");
            return;
        }

        long interviewCount = interviewDAO.findAll().stream()
                .filter(interview -> interview.getOffer().getId().equals(selected.getId()))
                .count();

        if (interviewCount > 0) {
            ButtonType back = new ButtonType("Volver", ButtonBar.ButtonData.CANCEL_CLOSE);
            ButtonType proceed = new ButtonType("Continuar", ButtonBar.ButtonData.OK_DONE);
            Alert alert = new Alert(Alert.AlertType.WARNING,
                    "Esta oferta tiene " + interviewCount + " entrevista(s) asociada(s) que también se eliminarán.",
                    back, proceed);
            alert.setTitle("Eliminar oferta");
            alert.setHeaderText("Se eliminarán datos en cascada");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isEmpty() || result.get() != proceed) {
                return;
            }
        }

        offerDAO.deleteById(selected.getId());
        loadOffers();
    }

    @FXML
    public void clearFilters(ActionEvent actionEvent) {
        tfOfferTitle.clear();
        cbOfferCompany.setValue(ALL_COMPANIES);
        cbOfferStatus.setValue(ALL_STATUSES);
        dpOfferDate.setValue(null);
        slOfferSalary.setValue(0);
        applyFilters();
    }

    private void setUpTable() {
        tvOffersTable.setEditable(true);
        tcOfferId.setCellValueFactory(row -> new SimpleObjectProperty<>(row.getValue().getId()));
        tcOfferTitle.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getTitle()));
        tcOfferCompany.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getCompany().getName()));
        tcOfferStatus.setCellValueFactory(row -> new SimpleObjectProperty<>(row.getValue().getStatus()));
        tcOfferStatus.setCellFactory(ComboBoxTableCell.forTableColumn(OfferStatus.values()));
        tcOfferStatus.setOnEditCommit(event -> {
            Offer offer = event.getRowValue();
            offer.setStatus(event.getNewValue());
            offerDAO.update(offer);
            updateStats();
        });
        tcOfferDate.setCellValueFactory(row -> new SimpleObjectProperty<>(row.getValue().getDate()));
        tcOfferDate.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setText(empty || date == null ? null : date.format(DATE_FORMAT));
            }
        });
        tcOfferSalary.setCellValueFactory(row -> new SimpleObjectProperty<>(row.getValue().getSalary()));
        tcOfferSalary.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double salary, boolean empty) {
                super.updateItem(salary, empty);
                setText(empty ? null : (salary == null ? "-" : String.format("%.2f €", salary)));
            }
        });
        tvOffersTable.setRowFactory(table -> {
            TableRow<Offer> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    ViewManager.openOfferDetail(row.getItem());
                }
            });
            return row;
        });
    }

    private void setUpFilters() {
        tfOfferTitle.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        cbOfferCompany.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        cbOfferStatus.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        dpOfferDate.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        slOfferSalary.valueProperty().addListener((observable, oldValue, newValue) -> {
            lbDynamicSalary.setText(newValue.doubleValue() <= 0
                    ? "Cualquier salario"
                    : String.format("≥ %.0f €", newValue.doubleValue()));
            applyFilters();
        });
        lbDynamicSalary.setText("Cualquier salario");
    }

    private void refreshFilterOptions() {
        String selectedCompany = cbOfferCompany.getValue();
        String selectedStatus = cbOfferStatus.getValue();

        cbOfferCompany.getItems().setAll(ALL_COMPANIES);
        companyDAO.findAll().stream()
                .map(Company::getName)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .forEach(cbOfferCompany.getItems()::add);
        cbOfferCompany.setValue(cbOfferCompany.getItems().contains(selectedCompany)
                ? selectedCompany : ALL_COMPANIES);

        cbOfferStatus.getItems().setAll(ALL_STATUSES);
        for (OfferStatus status : OfferStatus.values()) {
            cbOfferStatus.getItems().add(status.toString());
        }
        cbOfferStatus.setValue(cbOfferStatus.getItems().contains(selectedStatus)
                ? selectedStatus : ALL_STATUSES);
    }

    private void loadOffers() {
        allOffers.setAll(offerDAO.findAll());
        applyFilters();
        updateStats();
    }

    private void applyFilters() {
        String titleFilter = tfOfferTitle.getText() == null
                ? "" : tfOfferTitle.getText().trim().toLowerCase();
        String companyFilter = cbOfferCompany.getValue();
        String statusFilter = cbOfferStatus.getValue();
        LocalDate dateFilter = dpOfferDate.getValue();
        double salaryFilter = slOfferSalary.getValue();

        List<Offer> filtered = allOffers.stream()
                .filter(offer -> titleFilter.isEmpty()
                        || offer.getTitle().toLowerCase().contains(titleFilter))
                .filter(offer -> companyFilter == null || ALL_COMPANIES.equals(companyFilter)
                        || offer.getCompany().getName().equalsIgnoreCase(companyFilter))
                .filter(offer -> statusFilter == null || ALL_STATUSES.equals(statusFilter)
                        || offer.getStatus().toString().equals(statusFilter))
                .filter(offer -> dateFilter == null || dateFilter.equals(offer.getDate()))
                .filter(offer -> salaryFilter <= 0
                        || (offer.getSalary() != null && offer.getSalary() >= salaryFilter))
                .toList();
        tvOffersTable.getItems().setAll(filtered);
    }

    private void updateStats() {
        lbDynamicOffers.setText(String.valueOf(allOffers.size()));
        lbDynamicPendings.setText(String.valueOf(allOffers.stream()
                .filter(offer -> offer.getStatus() == OfferStatus.PENDING)
                .count()));
        lbDynamicInterviews.setText(String.valueOf(interviewDAO.findAll().size()));
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
    }
}
