package com.toby.hospitalRegister.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXSlider;
import com.toby.hospitalRegister.util.Constants;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

abstract class ListItem{
    public String pronunciation;

    @Override
    public abstract String toString();

    public abstract void forSqlResultSet(ResultSet resultSet) throws SQLException;

    public String getPronunciation(){return pronunciation;}
}

class ListItemDepartment extends ListItem{
    public String number;
    public String name;

    @Override
    public String toString() {
        return number+" "+name+" ";
    }

    @Override
    public void forSqlResultSet(ResultSet resultSet) throws SQLException{
        number = resultSet.getString(Constants.NameTableColumnDepartmentNumber);
        name = resultSet.getString(Constants.NameTableColumnDepartmentName);
        pronunciation = resultSet.getString(Constants.NameTableColumnDepartmentPronounce);
    }
}

class ListItemDoctor extends ListItem{
    public String number;
    public String name;
    public String departmentNumber;
    public String password;
    public boolean isSpecialList;
    public Timestamp lastLoginTime;
    @Override
    public String toString() {
        return number+" "+name+" "+(isSpecialList?"专家号":"普通号");
    }

    @Override
    public void forSqlResultSet(ResultSet resultSet) throws SQLException {
        number = resultSet.getString(Constants.NameTableColumnDoctorNumber);
        name = resultSet.getString(Constants.NameTableColumnDoctorName);
        departmentNumber = resultSet.getString(Constants.NameTableColumnDoctorDepartmentNumber);
        password = resultSet.getString(Constants.NameTableColumnDoctorPassword);
        isSpecialList = resultSet.getBoolean(Constants.NameTableColumnDoctorIsSpecialist);
        lastLoginTime = resultSet.getTimestamp(Constants.NameTableColumnDoctorIsSpecialist);
        pronunciation = resultSet.getString(Constants.NameTableColumnDoctorPronounce);
    }
}


class ListItemisSpecial extends ListItem{
    public boolean isSpecial;
    @Override
    public String toString() {
        return isSpecial?"专家号":"普通号";
    }

    @Override
    public void forSqlResultSet(ResultSet resultSet) throws SQLException {
        isSpecial = resultSet.getBoolean(Constants.NameTableColumnCategoryRegisterIsSpecialist);
    }
}

class  ListItemNameRegister extends ListItem{
    public String number;
    public String name;
    public boolean isSpecial;
    public int maxRegNumber;
    public float fee;

    @Override
    public String toString() {
        return number+" "+name+" "+(isSpecial?"专家号":"普通号")+" "+fee;
    }

    @Override
    public void forSqlResultSet(ResultSet resultSet) throws SQLException {
        number = resultSet.getString(Constants.NameTableColumnCategoryRegisterNumber);
        name = resultSet.getString(Constants.NameTableColumnCategoryRegisterName);
        isSpecial = resultSet.getBoolean(Constants.NameTableColumnCategoryRegisterIsSpecialist);
        maxRegNumber = resultSet.getInt(Constants.NameTableColumnCategoryRegisterMaxRegisterNumber);
        fee = resultSet.getInt(Constants.NameTableColumnCategoryRegisterFee);
    }
}
public class PatientController {
    // 登录界面记录的基本信息
    static public String patientName;
    static public String patientNumber;
    static public Double patientBalance;

    @FXML private JFXButton buttonRegister;
    @FXML private Label labelStatus;
    @FXML private JFXSlider sliderPay;
    @FXML private JFXCheckBox checkBoxAddToBalance;
    @FXML private JFXButton buttonExit;
    @FXML private JFXCheckBox checkBoxUseBalance;
    @FXML private Label labelFee;
    @FXML private Label labelRefund;
    @FXML private Label labelWelcome;
    @FXML private JFXComboBox<String> inputNameDepartment;
    @FXML private JFXComboBox<String> inputNameDoctor;
    @FXML private JFXComboBox<String> inputTypeRegister;
    @FXML private JFXComboBox<String> inputNameRegister;

    private int lastIndexInputNameDepartment = -1;
    private int lastIndexInputNameDoctor = -1;
    private int lastIndexInputTypeRegister = -1;
    private int lastIndexInputNameRegister = -1;

    private ObservableList<ListItemDepartment> departments = FXCollections.observableArrayList();
    private ObservableList<ListItemDoctor> doctors = FXCollections.observableArrayList();
    private ObservableList<ListItemisSpecial> isSpecials = FXCollections.observableArrayList();
    private ObservableList<ListItemNameRegister> registers = FXCollections.observableArrayList();

    private ObservableList<ListItemDepartment> departmentsFilter = FXCollections.observableArrayList();
    private ObservableList<ListItemDoctor> doctorsFilter = FXCollections.observableArrayList();
    private ObservableList<ListItemisSpecial> isSpecialsFilter = FXCollections.observableArrayList();
    private ObservableList<ListItemNameRegister> registersFilter = FXCollections.observableArrayList();

    //该函数会自动执行
    @FXML
    public void initialize(){
        userInfoDisplay();
    }

    private void userInfoDisplay() {
        labelWelcome.setText(String.format("欢迎，%s   余额：%.2f",patientName,patientBalance));
    }

    public void buttonRegisterPressed(MouseEvent mouseEvent) {
    }

    public void sliderPayDragged(MouseEvent mouseEvent) {
    }

    public void useBalanceClicked(MouseEvent mouseEvent) {
    }

    public void buttonExitClicked(MouseEvent mouseEvent) {
    }
}
