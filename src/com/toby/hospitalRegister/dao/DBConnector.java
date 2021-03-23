package com.toby.hospitalRegister.dao;

import com.toby.hospitalRegister.util.Constants;
import com.toby.hospitalRegister.util.RegisterException;

import java.sql.*;

public class DBConnector {
    private static DBConnector instance = null;
    private Connection connection;
    private Connection transactionConnection;
    private Statement statement;
    private Statement transactionStatement;

    public DBConnector() throws ClassNotFoundException{
        //用来测试能不能找到driver
        Class.forName("com.mysql.cj.jdbc.Driver");
    }

    public static DBConnector getInstance(){
        try {
            if(instance == null){
                instance = new DBConnector();
            }
        }catch (ClassNotFoundException e){
            System.err.println("cannot load sql driver");
            e.printStackTrace();
            System.exit(1);
        }
        return instance;
    }

    public void connectDatabase(
            String hostName,
            Integer port,
            String dbName,
            String userName,
            String password
    )throws SQLException {
        String url = "jdbc:mysql://" + hostName
                +":" +port
                +"/"+dbName
                +"?zeroDateTimeBehavior=convertToNull&autoReconnect=true&characterEncoding=UTF-8&characterSetResults=UTF-8&serverTimezone=Asia/Shanghai";
        connection = DriverManager.getConnection(url,userName,password);
        statement = connection.createStatement();
        transactionConnection = DriverManager.getConnection(url,userName,password);
        // 将自动提交关闭才可以启动事务
        transactionConnection.setAutoCommit(false);
        transactionStatement = transactionConnection.createStatement();
    }

    public void disconnectDatabase()throws SQLException{
        connection.close();
    }

    public ResultSet getWholeTable(String tableName){
        try {
            return statement.executeQuery("select * from "+tableName);
        } catch (SQLException throwables) {
            return null;
        }
    }

    public String getPatientPasswordById(String id){
        try {
            ResultSet res = statement.executeQuery(
                    "select "
                            + Constants.NameTableColumnPatientPassword
                            +" from "
                            +Constants.NameTablePatient
                            +" where "
                            +Constants.NameTableColumnPatientNumber +"="+ id
            );
            if(!res.next())
                return null;
            return res.getString(Constants.NameTableColumnPatientPassword);
        }catch (SQLException e){
            e.printStackTrace();
            return null;
        }
    }

    public ResultSet getPatientInfoById(String id){
        try {
            return statement.executeQuery(
                    "select * from "
                            +Constants.NameTablePatient
                            +" where "+Constants.NameTableColumnPatientNumber +"="+id
            );
        }catch (SQLException e){
            e.printStackTrace();
            return null;
        }
    }

    public ResultSet getDoctorInfoById(String id){
        try {
            return statement.executeQuery(
                    "select * from "
                            +Constants.NameTableDoctor
                            +" where " + Constants.NameTableColumnDoctorNumber +"="+id
            );
        }catch (SQLException e){
            e.printStackTrace();
            return null;
        }
    }

    public int tryRegister(
            String registerCategoryId,
            String doctorId,
            String patientId,
            Double registerFee,
            boolean deductFromBanlance,
            Double addToBanlance
    )throws RegisterException {
        try {
            ResultSet resultSet = transactionStatement.executeQuery(
                    " select * from "
                            +Constants.NameTableRegister
                            +" order by "+Constants.NameTableColumnRegisterNumber
                            +" desc limit 1"
            );

            //拿到最后可以挂号的id和当前已经挂该种类的数量
            int regId, currentCount;
            if(!resultSet.next()){
                regId = 0;
            }else {
                regId = Integer.parseInt(resultSet.getString(Constants.NameTableColumnRegisterNumber))+1;
            }

            resultSet = transactionStatement.executeQuery(
                    "select * from "
                            +Constants.NameTableRegister
                            +" where "+Constants.NameTableColumnRegisterCategoryNumber + "="+registerCategoryId
                            +" order by "+Constants.NameTableColumnRegisterNumber
                            +" desc limit 1"
            );
            if(!resultSet.next()){
                currentCount = 0;
            }else
                currentCount = resultSet.getInt(Constants.NameTableColumnRegisterCurrentRegisterCount);

            //处理病人id
            resultSet = transactionStatement.executeQuery(
                    "select * from "+Constants.NameTablePatient
                            +" where "+Constants.NameTableColumnPatientNumber +"="+patientId
            );

            if(!resultSet.next()){
                throw new RegisterException("patient dones't exist ",RegisterException.ErrorCode.patientNotExist);
            }
            double balance = resultSet.getDouble(Constants.NameTableColumnPatientBalance);

            //处理是否超过当前最大注册数量
            resultSet = transactionStatement.executeQuery(
                    "select * from "+Constants.NameTableCategoryRegister
                            +" where "+Constants.NameTableColumnCategoryRegisterNumber+"="+registerCategoryId
            );
            int maxRegNumber;
            if(!resultSet.next()){
                throw new RegisterException("registerCategoryNotFound", RegisterException.ErrorCode.registerCategoryNotFound);
            }else
                maxRegNumber = resultSet.getInt(Constants.NameTableColumnCategoryRegisterMaxRegisterNumber);
            if(currentCount >= maxRegNumber){
                throw new RegisterException("max register number reached", RegisterException.ErrorCode.registerIdExceeded);
            }

            //插入数据
            transactionStatement.executeUpdate(
                    String.format("insert into %s values(\"%06d\",\"%s\",\"%s\",\"%s\",%d,false,%.2f,current_timestamp)",
                            Constants.NameTableRegister,
                            regId,
                            registerCategoryId,
                            doctorId,
                            patientId,
                            (currentCount+1),
                            registerFee)
            );
            //从余额中减去此次挂号的费用
            if(deductFromBanlance){
                transactionStatement.executeUpdate(
                        String.format("update %s set %s=%.2f where %s=%s",
                                Constants.NameTablePatient,
                                Constants.NameTableColumnPatientBalance,
                                (balance -= registerFee),
                                Constants.NameTableColumnPatientNumber,
                                patientId)
                );
            }
            if(!deductFromBanlance){
                transactionStatement.executeUpdate(
                        String.format("update %s set %s=%.2f where %s=%s",
                                Constants.NameTablePatient,
                                Constants.NameTableColumnPatientBalance,
                                (balance += addToBanlance),
                                Constants.NameTableColumnPatientNumber,
                                patientId)
                );
            }

            //没有问题出现的话
            transactionConnection.commit();
            return regId;

        } catch (SQLException e) {
            try {
                transactionConnection.rollback();
            }catch (SQLException ee){
                ee.printStackTrace();
            }
            throw new RegisterException("sql exception occur", RegisterException.ErrorCode.sqlException);
        }
    }

    // 选择一定时间段的挂号人数！
    public ResultSet getRegisterFromDoctor(String docId, String startTime, String endTime){
        try {
            String sql = "select reg." + Constants.NameTableColumnRegisterNumber +
                    ",pat." + Constants.NameTableColumnPatientName +
                    ",reg." + Constants.NameTableColumnRegisterDateTime +
                    ",cat." + Constants.NameTableColumnCategoryRegisterIsSpecialist + (
                    " from (select " + Constants.NameTableColumnRegisterNumber +
                            "," + Constants.NameTableColumnRegisterPatientNumber +
                            "," + Constants.NameTableColumnRegisterDateTime +
                            "," + Constants.NameTableColumnRegisterCategoryNumber +
                            " from " + Constants.NameTableRegister +
                            " where " + Constants.NameTableColumnRegisterDoctorNumber +
                            "=" + docId +
                            " and " + Constants.NameTableColumnRegisterDateTime +
                            ">=\"" + startTime +
                            "\" and " + Constants.NameTableColumnRegisterDateTime +
                            "<=\"" + endTime +
                            "\") as reg" ) + (
                    " inner join (select " + Constants.NameTableColumnPatientNumber +
                            "," + Constants.NameTableColumnPatientName +
                            " from " + Constants.NameTablePatient +
                            ") as pat" ) +
                    " on reg." + Constants.NameTableColumnRegisterPatientNumber +
                    "=pat." + Constants.NameTableColumnPatientNumber + (
                    " inner join (select " + Constants.NameTableColumnCategoryRegisterNumber +
                            "," + Constants.NameTableColumnCategoryRegisterIsSpecialist +
                            " from " + Constants.NameTableCategoryRegister +
                            ") as cat" ) +
                    " on reg." + Constants.NameTableColumnRegisterCategoryNumber +
                    "=cat." + Constants.NameTableColumnCategoryRegisterNumber;
            return statement.executeQuery(sql);
        }catch (SQLException e){
            e.printStackTrace();
            return null;
        }
    }

    //统计医生的收入
    public ResultSet getIncomeInfo(String startTime, String endTime){
        try {
            String sql = "select dep." + Constants.NameTableColumnDepartmentName +
                    " as depname,reg." + Constants.NameTableColumnRegisterDoctorNumber +
                    ",doc." + Constants.NameTableColumnDoctorName +
                    " as docname,cat." + Constants.NameTableColumnCategoryRegisterIsSpecialist +
                    ",reg." + Constants.NameTableColumnRegisterCurrentRegisterCount +
                    ",SUM(reg." + Constants.NameTableColumnRegisterFee +
                    ") as sum from" + (
                    " (select * from " + Constants.NameTableRegister +
                            " where " + Constants.NameTableColumnRegisterDateTime +
                            ">=\"" + startTime +
                            "\" and " + Constants.NameTableColumnRegisterDateTime +
                            "<=\"" + endTime +
                            "\") as reg") +
                    " inner join" + (
                    " (select " + Constants.NameTableColumnDoctorNumber +
                            "," + Constants.NameTableColumnDoctorName +
                            "," + Constants.NameTableColumnDoctorDepartmentNumber +
                            " from " + Constants.NameTableDoctor +
                            ") as doc") +
                    " on reg." + Constants.NameTableColumnRegisterDoctorNumber +
                    "=doc." + Constants.NameTableColumnDoctorNumber +
                    " inner join" + (
                    " (select " + Constants.NameTableColumnDepartmentNumber +
                            "," + Constants.NameTableColumnDepartmentName +
                            " from " + Constants.NameTableDepartment +
                            ") as dep") +
                    " on doc." + Constants.NameTableColumnDoctorDepartmentNumber +
                    "=dep." + Constants.NameTableColumnDepartmentNumber +
                    " inner join" + (
                    " (select " + Constants.NameTableColumnCategoryRegisterNumber +
                            "," + Constants.NameTableColumnCategoryRegisterIsSpecialist +
                            " from " + Constants.NameTableCategoryRegister +
                            ") as cat" ) +
                    " on reg." + Constants.NameTableColumnRegisterCategoryNumber +
                    "=cat." + Constants.NameTableColumnCategoryRegisterNumber +
                    " group by reg." + Constants.NameTableColumnRegisterDoctorNumber +
                    ",cat." + Constants.NameTableColumnCategoryRegisterIsSpecialist;
            return statement.executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void updatePatientLoginTime(String paId, String time){
        try {
            statement.executeUpdate(
                    "update "+Constants.NameTablePatient
                            +" set "+Constants.NameTableColumnPatientLastLogin
                            +"=\""+time
                            +"\" where "+Constants.NameTableColumnPatientNumber
                            +"="+paId
            );
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void updateDoctorLoginTime(String doctorId, String time){
        try {
            statement.executeUpdate(
                    "update " + Constants.NameTableDoctor +
                            " set " + Constants.NameTableColumnDoctorLastLogin +
                            "=\"" + time +
                            " \"where " + Constants.NameTableColumnRegisterDoctorNumber+
                            "=" + doctorId
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
