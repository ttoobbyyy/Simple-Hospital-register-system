package com.toby.hospitalRegister.controller;

import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import com.toby.hospitalRegister.dao.DBConnector;
import com.toby.hospitalRegister.util.Constants;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DoctorController {

    private static final class Register extends RecursiveTreeObject<Register> {
        public StringProperty number;
        public StringProperty namePatient;
        public StringProperty dateTimeDisplay;
        public StringProperty isSpecialistDisplay;
        public Register(String number, String namePatient, Timestamp dateTime, boolean isSpecialist){
            this.number = new SimpleStringProperty(number);
            this.namePatient = new SimpleStringProperty(namePatient);
            this.dateTimeDisplay = new SimpleStringProperty(dateTime.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            this.isSpecialistDisplay = new SimpleStringProperty(isSpecialist ? "专家号" : "普通号");
        }
    }

    private static final class Income extends RecursiveTreeObject<Income> {
        public StringProperty departmentName;
        public StringProperty doctorNumber;
        public StringProperty doctorName;
        public StringProperty registerType;
        public StringProperty registerPopulation;
        public StringProperty incomeSum;
        public Income(String depName, String docNum, String docName, boolean isSpec, int regNumPeople, Double incomSum){
            this.departmentName = new SimpleStringProperty(depName);
            this.doctorNumber = new SimpleStringProperty(docNum);
            this.doctorName = new SimpleStringProperty(docName);
            this.registerType = new SimpleStringProperty(isSpec ? "专家号" : "普通号");
            this.registerPopulation = new SimpleStringProperty(Integer.toString(regNumPeople));
            this.incomeSum = new SimpleStringProperty(String.format("%.2f", incomSum));
        }
    }

    @FXML private Label labelWelcome;
    @FXML private DatePicker pickerDateStart;
    @FXML private DatePicker pickerDateEnd;
    @FXML private CheckBox checkBoxAllTime;
    @FXML private CheckBox checkBoxToday;
    @FXML private Button buttonFilter;
    @FXML private Button buttonExit;

    @FXML private TabPane mainPane;
    @FXML private Tab tabRegister;
    @FXML private Tab tabIncome;

    @FXML private TreeTableView<Register> tableRegister;
    @FXML private TreeTableColumn<Register, String> columnRegisterNumber;
    @FXML private TreeTableColumn<Register, String> columnRegisterPatientName;
    @FXML private TreeTableColumn<Register, String> columnRegisterDateTime;
    @FXML private TreeTableColumn<Register, String> columnRegisterType;
    private TreeItem<Register> rootRegister;

    @FXML private TreeTableView<Income> tableIncome;
    @FXML private TreeTableColumn<Income, String> columnIncomeDepartmentName;
    @FXML private TreeTableColumn<Income, String> columnIncomeDoctorNumber;
    @FXML private TreeTableColumn<Income, String> columnIncomeDoctorName;
    @FXML private TreeTableColumn<Income, String> columnIncomeRegisterPopulation;
    @FXML private TreeTableColumn<Income, String> columnIncomeRegisterType;
    @FXML private TreeTableColumn<Income, String> columnIncomeSum;
    private TreeItem<Income> rootIncome;

    public static String doctorName;
    public static String doctorNumber;

    private ObservableList<Register> listRegister = FXCollections.observableArrayList();
    private ObservableList<Income> listIncome = FXCollections.observableArrayList();

    @FXML
    void initialize(){
        labelWelcome.setText(String.format("欢迎，%s！", doctorName));

        pickerDateStart.setConverter(new DateConverter());
        pickerDateEnd.setConverter(new DateConverter());
        pickerDateStart.setValue(LocalDate.now());
        pickerDateEnd.setValue(LocalDate.now());

        // 初始化listRegister和listIncome列表
        initAllData();

        // 展示信息
        displayRegisterData();
        displayIncomeData();

        buttonExit.setOnKeyReleased(keyEvent -> {
            try {
                if (keyEvent.getCode() == KeyCode.ENTER)
                    buttonExitClicked();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        buttonFilter.setOnKeyReleased(keyEvent -> {
            try {
                if (keyEvent.getCode() == KeyCode.ENTER)
                    buttonFilterPressed();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void displayIncomeData() {
        columnIncomeDepartmentName.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<Income, String> param) -> param.getValue().getValue().departmentName);
        columnIncomeDoctorNumber.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<Income, String> param) -> param.getValue().getValue().doctorNumber);
        columnIncomeDoctorName.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<Income, String> param) -> param.getValue().getValue().doctorName);
        columnIncomeRegisterType.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<Income, String> param) -> param.getValue().getValue().registerType);
        columnIncomeRegisterPopulation.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<Income, String> param) -> param.getValue().getValue().registerPopulation);
        columnIncomeSum.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<Income, String> param) -> param.getValue().getValue().incomeSum);
        rootIncome = new RecursiveTreeItem<>(listIncome, RecursiveTreeObject::getChildren);
        tableIncome.setRoot(rootIncome);
    }

    private void displayRegisterData() {
        columnRegisterNumber.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<Register, String> param) -> param.getValue().getValue().number);
        columnRegisterPatientName.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<Register, String> param) -> param.getValue().getValue().namePatient );
        columnRegisterDateTime.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<Register, String> param) -> param.getValue().getValue().dateTimeDisplay );
        columnRegisterType.setCellValueFactory(
                (TreeTableColumn.CellDataFeatures<Register, String> param) -> param.getValue().getValue().isSpecialistDisplay );
        rootRegister = new RecursiveTreeItem<>(listRegister, RecursiveTreeObject::getChildren);
        tableRegister.setRoot(rootRegister);
    }

    private void initAllData() {
        ResultSet result;
        result = DBConnector.getInstance().getRegisterFromDoctor(
                doctorNumber,
                "0000-00-00 00:00:00",
                "2050-00-00 00:00:00"
        );
        try {
            listRegister.clear();
            while (result.next()) {
                listRegister.add(new Register(
                        result.getString(Constants.NameTableColumnRegisterNumber),
                        result.getString(Constants.NameTableColumnPatientName),
                        result.getTimestamp(Constants.NameTableColumnRegisterDateTime),
                        result.getBoolean(Constants.NameTableColumnCategoryRegisterIsSpecialist)
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        result = DBConnector.getInstance().getIncomeInfo(
                "0000-00-00 00:00:00",
                "2050-00-00 00:00:00");

        try {
            listIncome.clear();
            while (result.next()) {
                listIncome.add(new Income(
                                result.getString("depname"),
                                result.getString(Constants.NameTableColumnDoctorNumber),
                                result.getString("docname"),
                                result.getBoolean(Constants.NameTableColumnCategoryRegisterIsSpecialist),
                                result.getInt(Constants.NameTableColumnRegisterCurrentRegisterCount),
                                result.getDouble("sum")
                        )
                );
            }
            System.out.println("income numbers is: "+listIncome.size());
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
    }

    @FXML
    private void buttonFilterPressed() {
        if(mainPane.getSelectionModel().getSelectedItem() == tabRegister) {
            ResultSet result;
            if (checkBoxAllTime.isSelected()) {
                result = DBConnector.getInstance().getRegisterFromDoctor(
                        doctorNumber,
                        "0000-00-00 00:00:00",
                        "2050-00-00 00:00:00"
                );
                System.out.println(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            } else if (checkBoxToday.isSelected()) {
                result = DBConnector.getInstance().getRegisterFromDoctor(
                        doctorNumber,
                        LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 00:00:00",
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                );
            } else {
                result = DBConnector.getInstance().getRegisterFromDoctor(
                        doctorNumber,
                        pickerDateStart.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) +
                                " 00:00:00",
                        pickerDateEnd.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) +
                                " 00:00:00"
                );
            }

            try {
                listRegister.clear();
                while (result.next()) {
                    listRegister.add(new Register(
                            result.getString(Constants.NameTableColumnRegisterNumber),
                            result.getString(Constants.NameTableColumnPatientName),
                            result.getTimestamp(Constants.NameTableColumnRegisterDateTime),
                            result.getBoolean(Constants.NameTableColumnCategoryRegisterIsSpecialist)
                    ));
                }
                displayRegisterData();
            } catch (SQLException e) {
                e.printStackTrace();
                return;
            }
        } else if (mainPane.getSelectionModel().getSelectedItem() == tabIncome) {
            ResultSet result;
            if (checkBoxAllTime.isSelected()) {
                result = DBConnector.getInstance().getIncomeInfo(
                        "0000-00-00 00:00:00",
                        "2050-00-00 00:00:00"
                );
            } else if (checkBoxToday.isSelected()) {
                result = DBConnector.getInstance().getIncomeInfo(
                        LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 00:00:00",
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                );
            } else {
                result = DBConnector.getInstance().getIncomeInfo(
                        pickerDateStart.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) +
                                " 00:00:00",
                        pickerDateEnd.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) +
                                " 00:00:00"
                );
            }

            try {
                listIncome.clear();
                while (result.next()) {
                    listIncome.add(new Income(
                                    result.getString("depname"),
                                    result.getString(Constants.NameTableColumnDoctorNumber),
                                    result.getString("docname"),
                                    result.getBoolean(Constants.NameTableColumnCategoryRegisterIsSpecialist),
                                    result.getInt(Constants.NameTableColumnRegisterCurrentRegisterCount),
                                    result.getDouble("sum")
                            )
                    );
                }
                displayIncomeData();
            } catch (SQLException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    @FXML
    private void tabSelectionChanged(Event event) {
        if(((Tab)(event.getTarget())).isSelected());
    }

    @FXML
    private void buttonExitClicked() throws IOException {
        Scene scene = new Scene(FXMLLoader.load(getClass().getResource("../ui/Login.fxml")));

        Stage primaryStage = (Stage) buttonExit.getScene().getWindow();
        primaryStage.close();
        Stage secondStage = new Stage();
        secondStage.setScene(scene);
        secondStage.show();
    }

    @FXML
    void checkBoxAllTimeSelected(){
        if (checkBoxAllTime.isSelected()) {
            checkBoxToday.setSelected(false);
            pickerDateStart.setDisable(true);
            pickerDateEnd.setDisable(true);
        } else if (!checkBoxToday.isSelected()) {
            pickerDateStart.setDisable(false);
            pickerDateEnd.setDisable(false);
        }
    }

    @FXML
    void checkBoxTodaySelected(){
        if (checkBoxToday.isSelected()) {
            checkBoxAllTime.setSelected(false);
            pickerDateStart.setDisable(true);
            pickerDateEnd.setDisable(true);
        } else if(!checkBoxAllTime.isSelected()){
            pickerDateStart.setDisable(false);
            pickerDateEnd.setDisable(false);
        }
    }


}

class DateConverter extends StringConverter<LocalDate> {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    @Override
    public String toString(LocalDate localDate) {
        if(localDate != null){
            return formatter.format(localDate);
        } else {
            return "";
        }
    }

    @Override
    public LocalDate fromString(String s) {
        if(s != null && !s.isEmpty()) {
            return LocalDate.parse(s, formatter);
        } else {
            return null;
        }
    }
}