package com.ltm2022client.application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.Objects;

public class PlayTrailerMain extends Application {

    public static Scene scene;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        setRoot("play-trailer");
        setStylesheets("main");
        primaryStage.setScene(scene);
        primaryStage.initStyle(StageStyle.TRANSPARENT);
//        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public static void setRoot(String name) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource(name + ".fxml"));
        scene = new Scene(fxmlLoader.load());
    }

    public static void setStylesheets(String name) throws IOException {
        scene.getStylesheets().add(Objects.requireNonNull(Main.class.getResource(name + ".css")).toExternalForm());
    }
}
