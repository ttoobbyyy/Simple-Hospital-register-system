package com.toby.hospitalRegister;

import com.toby.hospitalRegister.dao.DBConnector;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.SQLException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        try {
            DBConnector.getInstance().connectDatabase("localhost",3306,"hospitalManage","hospital","654321");

        }catch (SQLException e){
            System.out.println("can't connect database!");
            e.printStackTrace();
        }

        Parent root = FXMLLoader.load(getClass().getResource("ui/Login.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
