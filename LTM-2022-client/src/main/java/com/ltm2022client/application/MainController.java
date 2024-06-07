package com.ltm2022client.application;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    private Button closeBtn;

    @FXML
    private Button resizeBtn;

    @FXML
    private Button minimizeBtn;

    @FXML
    private Button searchFilmBtn;

    @FXML
    private Button imageHandleBtn;

    @FXML
    private AnchorPane wrapPane;

    @FXML
    private AnchorPane headerPane;

    @FXML
    private AnchorPane contentPane;

    private Stage primaryStage;

    private static double offsetX;
    private static double offsetY;

    public static Socket socket = null;
    public static BufferedReader in = null;
    public static BufferedWriter out = null;
    private static BufferedReader stdIn = null;
    public static ObjectInputStream objectIn = null;
    public static ObjectOutputStream objectOut = null;
    public static byte[] serverPublicKey = null;
    public static byte[] publicKey = null;
    public static byte[] privateKey = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        connect();
        Handler();
    }

    public void connect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket("localhost", 3000);
                    System.out.println("Connected to server...");
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    stdIn = new BufferedReader(new InputStreamReader(System.in));
                    objectIn = new ObjectInputStream(socket.getInputStream());
                    objectOut = new ObjectOutputStream(socket.getOutputStream());
                    //read public key from server
                    serverPublicKey = (byte[]) objectIn.readObject();

                    //create client key
                    ArrayList<byte[]> keyList = Cryption.RSA.KeyPairGenerate();
                    publicKey = keyList.get(0);
                    privateKey = keyList.get(1);

                    objectOut.writeObject(publicKey);
                    objectOut.flush();
                } catch (Exception error) {
                    error.printStackTrace();
                    closeConnect();
                }
            }
        }).start();

    }

    public void closeConnect() {
        try {
            if (in != null)
                in.close();
            if (out != null)
                out.close();
            if (stdIn != null)
                stdIn.close();
            if (socket != null)
                socket.close();
        } catch (Exception error) {
            error.printStackTrace();
        }
    }

    public void Handler() {
        closeBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                primaryStage = (Stage) wrapPane.getScene().getWindow();
                primaryStage.close();
            }
        });

        resizeBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                primaryStage = (Stage) wrapPane.getScene().getWindow();
                primaryStage.setMaximized(!primaryStage.isMaximized());
            }
        });

        minimizeBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                primaryStage = (Stage) wrapPane.getScene().getWindow();
                primaryStage.setIconified(true);
            }
        });

        headerPane.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                primaryStage = (Stage) wrapPane.getScene().getWindow();
                offsetX = primaryStage.getX() - event.getScreenX();
                offsetY = primaryStage.getY() - event.getScreenY();
            }
        });

        headerPane.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                primaryStage = (Stage) wrapPane.getScene().getWindow();
                primaryStage.setX(event.getScreenX() + offsetX);
                primaryStage.setY(event.getScreenY() + offsetY);
            }
        });

        searchFilmBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    contentPane.getChildren().clear();
                    contentPane.getChildren().add(FXMLLoader.load(Objects.requireNonNull(getClass().getResource("search-film.fxml"))));
                    AnchorPane content = (AnchorPane) contentPane.getChildren().get(0);
                    AnchorPane.setBottomAnchor(content, 0.0);
                    AnchorPane.setTopAnchor(content, 0.0);
                    AnchorPane.setLeftAnchor(content, 0.0);
                    AnchorPane.setRightAnchor(content, 0.0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        imageHandleBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    contentPane.getChildren().clear();
                    contentPane.getChildren().add(FXMLLoader.load(Objects.requireNonNull(getClass().getResource("image-processing.fxml"))));
                    AnchorPane content = (AnchorPane) contentPane.getChildren().get(0);
                    AnchorPane.setBottomAnchor(content, 0.0);
                    AnchorPane.setTopAnchor(content, 0.0);
                    AnchorPane.setLeftAnchor(content, 0.0);
                    AnchorPane.setRightAnchor(content, 0.0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
