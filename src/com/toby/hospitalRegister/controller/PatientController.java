package com.toby.hospitalRegister.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXSlider;
import com.toby.hospitalRegister.dao.DBConnector;
import com.toby.hospitalRegister.util.Constants;
import com.toby.hospitalRegister.util.RegisterException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
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
    public String department;
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
        department = resultSet.getString(Constants.NameTableColumnCategoryRegisterDepartment);
        pronunciation = resultSet.getString(Constants.NameTableColumnCategoryRegisterPronounce);
    }
}
public class PatientController {
    // 登录界面记录的基本信息
    static public String patientName;
    static public String patientNumber;
    static public Double patientBalance;

    @FXML private GridPane mainPane;
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

    private ObservableList<ListItemDepartment> listDepartments = FXCollections.observableArrayList();
    private ObservableList<ListItemDoctor> listDoctors = FXCollections.observableArrayList();
    private ObservableList<ListItemisSpecial> listIsSpecials = FXCollections.observableArrayList();
    private ObservableList<ListItemNameRegister> listRegisters = FXCollections.observableArrayList();

    private ObservableList<ListItemDepartment> listDepartmentsFilter = FXCollections.observableArrayList();
    private ObservableList<ListItemDoctor> listDoctorsFilter = FXCollections.observableArrayList();
    private ObservableList<ListItemisSpecial> listIsSpecialsFilter = FXCollections.observableArrayList();
    private ObservableList<ListItemNameRegister> listRegistersFilter = FXCollections.observableArrayList();

    //该函数会自动执行
    @FXML
    public void initialize() throws Exception {
        // 欢迎提示语+余额显示
        userInfoDisplay();
        // 初始化列表的数据
        // 1.先将列表的全部数据加载处理
        initComboBoxData();
        // 2.对下拉列表置空
        inputNameDepartment.setItems(FXCollections.observableArrayList());
        inputNameDoctor.setItems(FXCollections.observableArrayList());
        inputNameRegister.setItems(FXCollections.observableArrayList());
        inputTypeRegister.setItems(FXCollections.observableArrayList());
        // 3.对列表进行筛选
        reFilterDepartment(false);
        reFilterDoctor(false);
        reFilterRegisterType(false);
        reFilterRegisterName(false);


        // 初始化挂号按钮
        updateRegisterButton();

        //根据键盘事件重新筛选数据
        inputNameDepartment.getEditor().setOnKeyReleased(keyEvent ->{
            // 过滤 up/down and enter 键
            if (shouldSupressKeyCode(keyEvent.getCode()))
                return;
            reFilterDepartment(true);
            reFilterDoctor(false);
            reFilterRegisterType(false);
            reFilterRegisterName(false);
            if (!inputNameDepartment.isShowing()) {
                inputNameDepartment.show();
            } else {
                inputNameDepartment.hide();
                inputNameDepartment.show();
            }
        });
        // 添加监听
        inputNameDepartment.addEventHandler(ComboBox.ON_HIDDEN, event -> {
            int index;
            //如果每次和上次的选择不一样，则需要重新筛选其他三个下拉列表的数据
            if((index = inputNameDepartment.getSelectionModel().getSelectedIndex()) != lastIndexInputNameDepartment){
                lastIndexInputNameDepartment = index;
                reFilterDoctor(false);
                reFilterRegisterType(false);
                reFilterRegisterName(false);
            }
            // 删除事件
            event.consume();
        });

        inputNameDoctor.getEditor().setOnKeyReleased(keyEvent -> {
            // 过滤 up/down and enter 键
            if (shouldSupressKeyCode(keyEvent.getCode()))
                return;

            reFilterDoctor(true);
            reFilterDepartment(false);
            reFilterRegisterType(false);
            reFilterRegisterName(false);
            if (!inputNameDoctor.isShowing()) {
                inputNameDoctor.show();
            } else {
                inputNameDoctor.hide();
                inputNameDoctor.show();
            }
        });
        inputNameDoctor.addEventHandler(ComboBox.ON_HIDDEN, e->{
            int index;
            if((index = inputNameDoctor.getSelectionModel().getSelectedIndex())
                    != lastIndexInputNameDoctor) {
                lastIndexInputNameDoctor = index;
                reFilterDepartment(false);
                reFilterRegisterType(false);
                reFilterRegisterName(false);
            }
            inputNameDoctor.setStyle("");
            updateRegisterButton();
            e.consume();
        });
        inputNameDoctor.setOnMousePressed(mouseEvent -> {
            inputNameDoctor.setStyle("");
        });

        inputTypeRegister.getEditor().setOnKeyReleased(keyEvent -> {
            // 过滤 up/down and enter 键
            if (shouldSupressKeyCode(keyEvent.getCode()))
                return;

            reFilterRegisterType(true);
            reFilterDepartment(false);
            reFilterDoctor(false);
            reFilterRegisterName(false);
            if (!inputTypeRegister.isShowing()) {
                inputTypeRegister.show();
            } else {
                inputTypeRegister.hide();
                inputTypeRegister.show();
            }
        });
        inputTypeRegister.addEventHandler(ComboBox.ON_HIDDEN, e-> {
            int index;
            if ((index = inputTypeRegister.getSelectionModel().getSelectedIndex())
                    != lastIndexInputTypeRegister) {
                lastIndexInputTypeRegister = index;
                reFilterDepartment(false);
                reFilterDoctor(false);
                reFilterRegisterName(false);
            }
            updateRegisterButton();
            e.consume();
        });

        inputNameRegister.getEditor().setOnKeyReleased(keyEvent -> {
            // 过滤 up/down and enter 键
            if (shouldSupressKeyCode(keyEvent.getCode()))
                return;

            reFilterRegisterName(true);
            reFilterDepartment(false);
            reFilterDoctor(false);
            reFilterRegisterType(false);
            if (!inputNameRegister.isShowing()) {
                inputNameRegister.show();
            } else {
                inputNameRegister.hide();
                inputNameRegister.show();
            }
        });
        inputNameRegister.addEventHandler(ComboBox.ON_HIDDEN, e->{
            int index;
            if((index = inputNameRegister.getSelectionModel().getSelectedIndex())
                    != lastIndexInputNameRegister) {
                lastIndexInputNameRegister = index;
                reFilterDepartment(false);
                reFilterDoctor(false);
                reFilterRegisterType(false);
            }
            inputNameRegister.setStyle("");
            if(index != -1){
                float fee = listRegistersFilter.get(index).fee;
                labelFee.setText("" + fee + " ¥");
            }
            updateUseBalance();
            updateRefund();
            updateRegisterButton();
            e.consume();
        });
        inputNameRegister.setOnMousePressed(mouseEvent -> {
            inputNameRegister.setStyle("");
        });

        buttonRegister.setOnKeyReleased(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER)
                buttonRegisterPressed();
        });

        buttonExit.setOnKeyReleased(keyEvent -> {
            try {
                if (keyEvent.getCode() == KeyCode.ENTER)
                    buttonExitClicked();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        checkBoxUseBalance.setOnKeyReleased(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.SPACE)
                useBalanceClicked();
            else
                keyEvent.consume();
        });




    }

    /**
     * @brief 更新余额
     */
    private void updateRefund(){
        int index = inputNameRegister.getSelectionModel().getSelectedIndex();
        if(index != -1 && checkBoxUseBalance.isSelected()){
            labelRefund.setText("0¥");
            labelRefund.setStyle("");
            return;
        }

        if(index != -1 && sliderPay.getValue() > listRegistersFilter.get(index).fee) {
            labelRefund.setText(String.format("%.2f¥", sliderPay.getValue() - listRegistersFilter.get(index).fee));
            labelRefund.setStyle("");
        } else if (index != -1) {
            labelRefund.setText("交款金额不足");
            labelRefund.setStyle("-fx-text-fill: red;");
        }
    }

    // 判断是否可以使用余额，余额不足会禁用选项
    private void updateUseBalance() {
        int index = inputNameRegister.getSelectionModel().getSelectedIndex();
        if(index != -1 && patientBalance < listRegistersFilter.get(index).fee){
            checkBoxUseBalance.setSelected(false);
            sliderPay.setDisable(false);
            checkBoxUseBalance.setText("余额不足");
            checkBoxUseBalance.setDisable(true);
        }else {
            checkBoxUseBalance.setDisable(false);
            checkBoxUseBalance.setText("使用余额付款");
            checkBoxUseBalance.setSelected(true);
            sliderPay.setDisable(true);
        }
    }

    private boolean shouldSupressKeyCode(KeyCode code) {
        return code == KeyCode.DOWN ||
                code == KeyCode.UP ||
                code == KeyCode.ENTER;
    }

    private void updateRegisterButton() {
        buttonRegister.setVisible(false);
        int index;
        //满足输入条件才可以挂号
        if(inputNameDoctor.getSelectionModel().getSelectedIndex() != -1||
            (index = inputNameRegister.getSelectionModel().getSelectedIndex()) != -1 ||
                    (checkBoxUseBalance.isSelected() && patientBalance >= listRegistersFilter.get(index).fee)||
                    (!checkBoxUseBalance.isSelected() && sliderPay.getValue() >= listRegistersFilter.get(index).fee)){
            buttonRegister.setVisible(true);
        }
    }

    private void reFilterRegisterName(boolean withoutSelect) {
        int index;
        String previousKey = "";
        if((index = inputNameRegister.getSelectionModel().getSelectedIndex()) != -1)
            previousKey = listRegistersFilter.get(index).number;

        ObservableList<ListItemNameRegister> list0 = FXCollections.observableArrayList();
        ObservableList<ListItemNameRegister> list1 = FXCollections.observableArrayList();

        for (ListItemNameRegister listItemNameRegister : listRegisters)
            if (listItemNameRegister.toString().contains(inputNameRegister.getEditor().getText().trim()) ||
                    listItemNameRegister.getPronunciation().contains(inputNameRegister.getEditor().getText().trim()))
                list0.add(listItemNameRegister);

        // 根据科室过滤
        if ((index = inputNameDepartment.getSelectionModel().getSelectedIndex()) != -1) {
            for (ListItemNameRegister listItemNameRegister : list0)
                if (listItemNameRegister.department.equals(listDepartmentsFilter.get(index).number))
                    list1.add(listItemNameRegister);
            list0 = list1;
        }

        // 根据医生过滤
        list1 = FXCollections.observableArrayList();
        if ((index= inputNameDoctor.getSelectionModel().getSelectedIndex()) != -1) {
            for (ListItemNameRegister listItemNameRegister : list0)
                if (!listItemNameRegister.isSpecial || listDoctorsFilter.get(index).isSpecialList)
                    list1.add(listItemNameRegister);
            list0 = list1;
        }

        // 根据是否是专家号过滤
        list1 = FXCollections.observableArrayList();
        if ((index= inputTypeRegister.getSelectionModel().getSelectedIndex()) != -1) {
            for (ListItemNameRegister listItemNameRegister : list0)
                if (listItemNameRegister.isSpecial == listIsSpecialsFilter.get(index).isSpecial)
                    list1.add(listItemNameRegister);
            list0 = list1;
        }

        boolean isCurrentInputLegal = false;
        int counter = 0, newSelection = -1;
        listRegistersFilter.clear();
        inputNameRegister.getItems().clear();
        for (ListItemNameRegister listItemNameRegister : list0) {
            listRegistersFilter.add(listItemNameRegister);
            inputNameRegister.getItems().add(listItemNameRegister.toString());
            if(listItemNameRegister.toString().contains(inputNameRegister.getEditor().getText().trim()) ||
                    listItemNameRegister.getPronunciation().contains(inputNameRegister.getEditor().getText().trim()))
                isCurrentInputLegal = true;
            if(previousKey.equals(listItemNameRegister.number))
                newSelection = counter;
            ++counter;
        }

        if (!withoutSelect) {
            if (!isCurrentInputLegal)
                inputNameRegister.getEditor().clear();
            if (newSelection != -1) {
                inputNameRegister.getSelectionModel().clearAndSelect(newSelection);
                inputNameRegister.getEditor().setText(inputNameRegister.getItems().get(newSelection));
            }
        }
    }

    private void reFilterRegisterType(boolean withoutSelect) {
        int index;
        String previousKey = "";
        if((index = inputTypeRegister.getSelectionModel().getSelectedIndex()) != -1)
            previousKey = listIsSpecialsFilter.get(index).pronunciation;

        ObservableList<ListItemisSpecial> list0 = FXCollections.observableArrayList();
        ObservableList<ListItemisSpecial> list1 = FXCollections.observableArrayList();

        for (ListItemisSpecial listItemTypeRegister : listIsSpecials)
            if (listItemTypeRegister.toString().contains(inputTypeRegister.getEditor().getText().trim()) ||
                    listItemTypeRegister.getPronunciation().contains(inputTypeRegister.getEditor().getText().trim()))
                list0.add(listItemTypeRegister);

        // 根据医生过滤
        if ((index= inputNameDoctor.getSelectionModel().getSelectedIndex()) != -1) {
            for (ListItemisSpecial listItemTypeRegister : list0)
                if (listDoctorsFilter.get(index).isSpecialList || !listItemTypeRegister.isSpecial)
                    list1.add(listItemTypeRegister);
            list0 = list1;
        }

        // 根据挂号种类过滤
        list1 = FXCollections.observableArrayList();
        if((index = inputNameRegister.getSelectionModel().getSelectedIndex()) != -1) {
            for (ListItemisSpecial register : list0)
                if (register.isSpecial == listRegistersFilter.get(index).isSpecial)
                    list1.add(register);
            list0 = list1;
        }

        boolean isCurrentInputLegal = false;
        int counter = 0, newSelection = -1;
        listIsSpecialsFilter.clear();
        inputTypeRegister.getItems().clear();
        for (ListItemisSpecial register : list0) {
            listIsSpecialsFilter.add(register);
            inputTypeRegister.getItems().add(register.toString());
            if(register.toString().contains(inputTypeRegister.getEditor().getText().trim()) ||
                    register.getPronunciation().contains(inputTypeRegister.getEditor().getText().trim()))
                isCurrentInputLegal = true;
            if(previousKey.equals(register.pronunciation))
                newSelection = counter;
            ++counter;
        }

        if(!withoutSelect) {
            if (!isCurrentInputLegal)
                inputTypeRegister.getEditor().clear();
            if (newSelection != -1) {
                inputTypeRegister.getSelectionModel().clearAndSelect(newSelection);
                inputTypeRegister.getEditor().setText(inputTypeRegister.getItems().get(newSelection));
            }
        }
    }

    private void reFilterDoctor(boolean withoutSelect) {
        int index;
        String previousKey = "";
        if((index = inputNameDoctor.getSelectionModel().getSelectedIndex()) != -1)
            previousKey = listDoctorsFilter.get(index).number;

        ObservableList<ListItemDoctor> list0 = FXCollections.observableArrayList();
        ObservableList<ListItemDoctor> list1 = FXCollections.observableArrayList();


        for (ListItemDoctor listItemNameDoctor : listDoctors)
            if (listItemNameDoctor.toString().contains(inputNameDoctor.getEditor().getText().trim()) ||
                    listItemNameDoctor.getPronunciation().contains(inputNameDoctor.getEditor().getText().trim())){
                list0.add(listItemNameDoctor);
                listDoctorsFilter.add(listItemNameDoctor);
            }

        // 根据科室名称过滤
        if ((index = inputNameDepartment.getSelectionModel().getSelectedIndex() )!= -1) {
            for (ListItemDoctor listItemNameDoctor : list0)
                if (listItemNameDoctor.departmentNumber.equals(listDepartmentsFilter.get(index).number))
                    list1.add(listItemNameDoctor);
            list0 = list1;
        }

        // 根据是否是专家号过滤
        list1 = FXCollections.observableArrayList();
        if ((index = inputTypeRegister.getSelectionModel().getSelectedIndex()) != -1) {
            for (ListItemDoctor doctor : list0)
                if (doctor.isSpecialList || !listIsSpecialsFilter.get(index).isSpecial)
                    list1.add(doctor);
            list0 = list1;
        }

        // 根据挂号种类过滤
        list1 = FXCollections.observableArrayList();
        if ((index = inputNameRegister.getSelectionModel().getSelectedIndex()) != -1) {
            for (ListItemDoctor doctor : list0)
                if (doctor.departmentNumber.equals(listRegistersFilter.get(index).department))
                    list1.add(doctor);
            list0 = list1;
        }

        boolean isCurrentInputLegal = false;
        int counter = 0, newSelection = -1;
        inputNameDoctor.getItems().clear();
        listDoctorsFilter.clear();
        for (ListItemDoctor doctor : list0) {
            listDoctorsFilter.add(doctor);
            inputNameDoctor.getItems().add(doctor.toString());
            if(doctor.toString().contains(inputNameDoctor.getEditor().getText().trim()) ||
                    doctor.getPronunciation().contains(inputNameDoctor.getEditor().getText().trim()))
                isCurrentInputLegal = true;
            if(previousKey.equals(doctor.number))
                newSelection = counter;
            ++counter;
        }

        if(!withoutSelect) {
            if (!isCurrentInputLegal)
                inputNameDoctor.getEditor().clear();
            if (newSelection != -1) {
                inputNameDoctor.getSelectionModel().clearAndSelect(counter);
                inputNameDoctor.getEditor().setText(inputNameDoctor.getItems().get(newSelection));
            }
        }
    }

    private void reFilterDepartment(boolean withOutSelect) {
        int index;
        String previousKey = "";
        if((index = inputNameDepartment.getSelectionModel().getSelectedIndex()) != -1){
            previousKey = listDepartmentsFilter.get(index).number;
        }

        ObservableList<ListItemDepartment> list0 = FXCollections.observableArrayList();
        ObservableList<ListItemDepartment> list1 = FXCollections.observableArrayList();

        // 用户输入或者已经选择了科室，则过滤科室名字
        for (ListItemDepartment listItemDepartment: listDepartments) {
            if(listItemDepartment.toString().contains(inputNameDepartment.getEditor().getText().trim()) ||
                    listItemDepartment.getPronunciation().contains(inputNameDepartment.getEditor().getText().trim())){
                // listDepartmentsFilter对外的选择的科室集合
                // list0当前下拉列表的过滤后的集合
                listDepartmentsFilter.add(listItemDepartment);
                list0.add(listItemDepartment);
            }

        }
        // 用户如果已经选择了医生，则需要根据医生过滤科室
        if((index = inputNameDoctor.getSelectionModel().getSelectedIndex()) != -1){
            for (ListItemDepartment department: list0) {
                if(department.number.equals(listDoctorsFilter.get(index).departmentNumber)){
                    // list1其他的过滤条件
                    list1.add(department);
                }
            }
            list0 = list1;
        }

        boolean isCurrentInputLegal = false;
        int counter = 0, newSelection = -1;
        inputNameDepartment.getItems().clear();
        listDepartmentsFilter.clear();
        for (ListItemDepartment department: list0){
            inputNameDepartment.getItems().add(department.toString());
            listDepartmentsFilter.add(department);
            if(department.toString().contains(inputNameDepartment.getEditor().getText().trim()) ||
                    department.getPronunciation().contains(inputNameDepartment.getEditor().getText().trim()))
                isCurrentInputLegal = true;
            if(previousKey.equals(department.number))
                    newSelection = counter;
            counter++;
        }

        // 清除不合法的输入格式
        if(!withOutSelect){
            if(!isCurrentInputLegal){
                inputNameDepartment.getEditor().clear();
            }
            if(newSelection != -1){
                inputNameDepartment.getSelectionModel().clearAndSelect(newSelection);
                inputNameDepartment.getEditor().setText(inputNameDepartment.getItems().get(newSelection));
            }
        }

    }

    private void initComboBoxData() throws Exception {
        updateOneSetOfData(Constants.NameTableDepartment, listDepartments,ListItemDepartment.class);
        updateOneSetOfData(Constants.NameTableDoctor,listDoctors,ListItemDoctor.class);
        updateOneSetOfData(Constants.NameTableRegister,listRegisters,ListItemNameRegister.class);

        //关于listIsSpecials，因为只有两个对象，就手动添加了
        ListItemisSpecial special = new ListItemisSpecial();
        ListItemisSpecial normal = new ListItemisSpecial();
        special.isSpecial = true;
        special.pronunciation = "zhuanjiahao";
        normal.isSpecial = false;
        normal.pronunciation = "putonghao";
        listIsSpecials.add(special);
        listIsSpecials.add(normal);
    }

    private <TypeItem extends ListItem>boolean updateOneSetOfData(String tableName, ObservableList<TypeItem> list, Class<TypeItem> clazz) throws Exception {
        // 获得整张表
        ResultSet resultSet = DBConnector.getInstance().getWholeTable(tableName);

        if (resultSet != null){
            ObservableList<TypeItem> tempList = FXCollections.observableArrayList();
            try {
                while (resultSet.next()){
                    TypeItem item = clazz.newInstance();
                    item.forSqlResultSet(resultSet);
                    tempList.add(item);
                }
            }catch (Exception e){
                e.printStackTrace();
                System.exit(-1);
            }
            // 无问题就可以添加了
            list.clear();
            list.addAll(tempList);
            return true;
        }else {
            throw new Exception("数据库出问题了，请联系管理员!");
        }
    }


    private void userInfoDisplay() {
        labelWelcome.setText(String.format("欢迎，%s   余额：%.2f",patientName,patientBalance));
    }

    public void buttonRegisterPressed() {
        // 判断挂号前的必要条件
        if (inputNameDoctor.getSelectionModel().getSelectedIndex() == -1){
            statusError("请选择医生姓名");
            inputNameDoctor.setStyle("-fx-background-color: pink;");
            return;
        }
        if (inputNameRegister.getSelectionModel().getSelectedIndex() == -1) {
            statusError("请选择号种名称");
            inputNameRegister.setStyle("-fx-background-color: pink;");
            return;
        }

        int index;
        if ( !( (index = inputNameRegister.getSelectionModel().getSelectedIndex()) != -1 &&
                inputNameDoctor.getSelectionModel().getSelectedIndex() != -1 && (
                (checkBoxUseBalance.isSelected() && patientBalance >= listRegistersFilter.get(index).fee) ||
                        (!checkBoxUseBalance.isSelected() && sliderPay.getValue() >= listRegistersFilter.get(index).fee)))) {
            statusError("缴费金额不足或余额不足");
            return;
        }

        // 等待sql的结果
        disableEverything();
        RegisterService service = new RegisterService(
                listRegistersFilter.get(inputNameRegister.getSelectionModel().getSelectedIndex()).number,
                listDoctorsFilter.get(inputNameDoctor.getSelectionModel().getSelectedIndex()).number,
                patientNumber,
                listRegistersFilter.get(inputNameRegister.getSelectionModel().getSelectedIndex()).fee,
                ((checkBoxAddToBalance.isSelected() && !checkBoxUseBalance.isSelected()) ?
                        sliderPay.getValue()-listRegistersFilter.get(inputNameRegister.getSelectionModel().getSelectedIndex()).fee:0),
                checkBoxUseBalance.isSelected()
        );
        service.setOnSucceeded(workerStateEvent -> {
            switch (service.returnCode){
                case registerIdExceeded:
                    statusError("此号已达到人数上限。");
                    break;
                case registerCategoryNotFound:
                case sqlException:
                    statusError("数据库错误, 请联系管理员");
                    break;
                case retryTimeExceeded:
                    statusError("系统繁忙，请稍候再试");
                    break;
                case noError:
                    labelStatus.setText("挂号成功，挂号号码：" + service.registerNumber);
                    patientBalance = service.updateBalance;
                    userInfoDisplay();
                    break;
            }
            enableEverythingI();
        });
        service.start();

    }

    private void enableEverythingI() {
        mainPane.setDisable(false);
    }

    private void disableEverything() {
        mainPane.setDisable(true);
    }

    private void statusError(String error) {
        labelStatus.setText(error);
        labelStatus.setStyle("-fx-text-fill: red");
    }

    @FXML
    public void sliderPayDragged() {
        updateRefund();
        updateRegisterButton();
    }

    @FXML
    void useBalanceClicked(){
        if (checkBoxUseBalance.isSelected()) {
            sliderPay.setDisable(true);
            updateRefund();
        } else {
            sliderPay.setDisable(false);
            updateRefund();
        }
        updateRegisterButton();
    }

    @FXML
    public void buttonExitClicked() throws IOException {
        Scene scene = new Scene(FXMLLoader.load(getClass().getResource("../ui/Login.fxml")));

        Stage primaryStage = (Stage) buttonExit.getScene().getWindow();
        primaryStage.close();
        Stage secondStage = new Stage();
        secondStage.setScene(scene);
        secondStage.show();
    }

}
class RegisterService extends Service {
    String registerCategoryNumber;
    String doctorNumber;
    String patientNumber;
    double registerFee;
    double addToBalance;
    boolean deductFromBalance;
    // 注册数据库失败尝试次数
    int retry = 3;
    int registerNumber;
    RegisterException.ErrorCode returnCode;
    double updateBalance;

    public void setRetry(int retry){
        this.retry = retry;
    }

    public RegisterService(String registerCategoryNumber, String doctorNumber, String patientNumber, double registerFee, double addToBalance, boolean deductFromBalance) {
        this.registerCategoryNumber = registerCategoryNumber;
        this.doctorNumber = doctorNumber;
        this.patientNumber = patientNumber;
        this.registerFee = registerFee;
        this.addToBalance = addToBalance;
        this.deductFromBalance = deductFromBalance;
    }



    @Override
    protected Task createTask() {
        return new Task() {
            @Override
            protected Object call() throws Exception {
                boolean retryFlag = false;
                while (!retryFlag && retry-- > 0){
                    try {
                        registerNumber = DBConnector.getInstance().tryRegister(
                                registerCategoryNumber,
                                doctorNumber,
                                patientNumber,
                                registerFee,
                                deductFromBalance,
                                addToBalance
                        );
                    }catch (RegisterException e){
                        retryFlag = true;
                        switch (e.error){
                            case sqlException:
                                returnCode = RegisterException.ErrorCode.sqlException;
                                break;
                            case registerIdExceeded:
                            case registerCategoryNotFound:
                            case patientNotExist:
                                returnCode =e.error;
                                return  null;
                        }
                    }
                }
                if(retry == 0){
                    returnCode = RegisterException.ErrorCode.retryTimeExceeded;
                }else {
                    returnCode = RegisterException.ErrorCode.noError;
                    try {
                        ResultSet patientInfo =DBConnector.getInstance().getPatientInfoById(patientNumber);
                        if(!patientInfo.next()){
                            returnCode = RegisterException.ErrorCode.patientNotExist;
                        }
                        updateBalance = patientInfo.getDouble(Constants.NameTableColumnPatientBalance);
                    }catch (SQLException e){
                        returnCode = RegisterException.ErrorCode.sqlException;
                        return null;
                    }
                }
                return null;

            }
        };
    }
}
