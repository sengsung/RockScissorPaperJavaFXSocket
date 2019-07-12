package kr.rsp.java;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("main.fxml"));
        primaryStage.setTitle("가위바위보!");
        primaryStage.setScene(new Scene(root, 294, 97));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
