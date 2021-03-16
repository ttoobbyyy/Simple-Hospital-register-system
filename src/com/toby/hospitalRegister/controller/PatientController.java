package com.toby.hospitalRegister.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXSlider;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

public class PatientController {
    // 登录界面记录的基本信息
    static public String patientName;
    static public String patientNumber;
    static public Double patientBalance;
    public JFXButton buttonRegister;
    public Label labelStatus;
    public JFXSlider sliderPay;
    public JFXCheckBox checkBoxAddToBalance;
    public JFXButton buttonExit;
    public JFXCheckBox checkBoxUseBalance;

    public void buttonRegisterPressed(MouseEvent mouseEvent) {
    }

    public void sliderPayDragged(MouseEvent mouseEvent) {
    }

    public void useBalanceClicked(MouseEvent mouseEvent) {
    }

    public void buttonExitClicked(MouseEvent mouseEvent) {
    }
}
