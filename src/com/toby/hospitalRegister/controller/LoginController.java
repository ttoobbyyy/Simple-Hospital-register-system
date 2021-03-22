package com.toby.hospitalRegister.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import com.toby.hospitalRegister.dao.DBConnector;
import com.toby.hospitalRegister.util.Constants;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoginController {
    @FXML Label labelStatus;
    @FXML JFXButton buttonLoginDoctor;
    @FXML JFXButton buttonLoginPatient;
    @FXML JFXButton buttonExit;
    @FXML JFXPasswordField inputPassword;
    @FXML JFXTextField inputUsername;

    @FXML
    void initialize(){
        buttonLoginDoctor.setOnKeyReleased(keyEvent -> {
            if(keyEvent.getCode() == KeyCode.ENTER){
                try {
                    doctorLogin();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
        buttonLoginPatient.setOnKeyReleased(keyEvent ->{
            if(keyEvent.getCode() == KeyCode.ENTER){
                patientLogin();
            }
        });
        buttonExit.setOnKeyReleased(keyEvent ->{
            if(keyEvent.getCode() == KeyCode.ENTER){
                System.exit(0);
            }
        });
    }

    @FXML
    public void patientLogin() {
        if(!validateUsernameAndPassword())
                return;
        ResultSet resultSet =  DBConnector.getInstance().getPatientInfoById(inputUsername.getText().trim());
        if(resultSet == null){
            labelStatus.setText("数据库读取错误，请联系系统管理员！");
            labelStatus.setStyle("-fx-text-fill: red");
        }
        try {
            if(!resultSet.next()){
                labelStatus.setText("用户不存在");
                labelStatus.setStyle("-fx-text-fill: red");
                return;
            }else if(!resultSet.getString(Constants.NameTableColumnPatientPassword).equals(inputPassword.getText().trim()))
            {
                labelStatus.setText("密码输入错误！");
                labelStatus.setStyle("-fx-text-fill: red");
                return;
            }

            PatientController.patientNumber = resultSet.getString(Constants.NameTableColumnPatientNumber);
            PatientController.patientName = resultSet.getString(Constants.NameTableColumnPatientName);
            PatientController.patientBalance = resultSet.getDouble(Constants.NameTableColumnPatientBalance);

            //更新登录时间
            DBConnector.getInstance().updatePatientLoginTime(inputUsername.getText().trim(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            //获取当前的窗口，执行关闭，然后开启新的窗口
            Stage primaryStage = (Stage) buttonLoginPatient.getScene().getWindow();
            primaryStage.close();

            Stage newStage = new Stage();
            Scene newScene = new Scene(FXMLLoader.load(getClass().getResource("../ui/Patient.fxml")));
            newStage.setTitle("病人挂号");
            newStage.setScene(newScene);
            newStage.show();

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private boolean validateUsernameAndPassword() {
        if(inputUsername.getText().isEmpty()){
            inputUsername.setStyle("-fx-background-color: #a29aee");
            labelStatus.setText("请输入用户名！");
            labelStatus.setStyle("-fx-text-fill: #ff0000");
            return false;
        }
        if(inputPassword.getText().isEmpty()){
            inputPassword.setStyle("-fx-background-color: #a39af5");
            labelStatus.setText("请输入密码！");
            labelStatus.setStyle("-fx-text-fill: red");
            return false;
        }
        labelStatus.setText("登录中...");
        return true;
    }

    @FXML
    public void doctorLogin() throws SQLException {
        if(!validateUsernameAndPassword())
            return;
        ResultSet resultSet =  DBConnector.getInstance().getDoctorInfoById(inputUsername.getText().trim());
        if(resultSet == null){
            labelStatus.setText("数据库读取错误，请联系系统管理员！");
            labelStatus.setStyle("-fx-text-fill: red");
        }
        try {
            if(!resultSet.next()){
                labelStatus.setText("用户不存在");
                labelStatus.setStyle("-fx-text-fill: red");
                return;
            }else if(!resultSet.getString(Constants.NameTableColumnDoctorPassword).equals(inputPassword.getText().trim()))
            {
                labelStatus.setText("密码输入错误！");
                labelStatus.setStyle("-fx-text-fill: red");
                return;
            }

            DoctorController.doctorName = resultSet.getString(Constants.NameTableColumnDoctorName);
            DoctorController.doctorNumber = resultSet.getString(Constants.NameTableColumnDoctorNumber);

            //更新登录时间
            DBConnector.getInstance().updateDoctorLoginTime(inputUsername.getText().trim(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            //获取当前的窗口，执行关闭，然后开启新的窗口
            Stage primaryStage = (Stage) buttonLoginPatient.getScene().getWindow();
            primaryStage.close();

            Stage newStage = new Stage();
            Scene newScene = new Scene(FXMLLoader.load(getClass().getResource("../ui/Doctor.fxml")));
            newStage.setTitle("医生界面");
            newStage.setScene(newScene);
            newStage.show();

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @FXML
    public void onInputUsernameAction() {
    }
    @FXML
    public void onInputPasswordAction() {
    }

    @FXML
    public void buttonExitClicked() {
        System.exit(0);
    }
}
