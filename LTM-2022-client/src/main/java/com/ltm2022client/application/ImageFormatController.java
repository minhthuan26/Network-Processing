package com.ltm2022client.application;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.crypto.SealedObject;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

import static com.ltm2022client.application.ImageProcessingController.inputImageFile;
import static com.ltm2022client.application.ImageProcessingController.outputImageFile;
import static com.ltm2022client.application.MainController.objectIn;

public class ImageFormatController implements Initializable {

    @FXML
    private Button cancelBtn;

    @FXML
    private Button confirmBtn;

    @FXML
    private VBox contentPane;

    @FXML
    private HBox formatGroup;

    @FXML
    private ToggleGroup imageFormatGroup;

    @FXML
    private RadioButton jpegBtn;

    @FXML
    private RadioButton jpgBtn;

    @FXML
    private RadioButton pngBtn;

    @FXML
    private AnchorPane wrapPane;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setDefault();
        Handler();
    }

    public File sendImage(File image, String option, String fileExt){
        try{
            String key = Cryption.RandomKey() + ";;2" + ";;" + option + ";;" + fileExt;
            SealedObject imageObject = (SealedObject) Cryption.AES.EncryptionObject(image, key);
            key = Cryption.RSA.Encryption(key, MainController.serverPublicKey);
            MainController.objectOut.writeObject(imageObject);
            MainController.objectOut.writeObject(key);
            MainController.objectOut.flush();

            Object responseFromServer;
            SealedObject message = null;
            while((responseFromServer = (Object) objectIn.readObject()) != null){

                if(responseFromServer.getClass().getName().equalsIgnoreCase("javax.crypto.SealedObject")){
                    message = (SealedObject) responseFromServer;
                }
                else{
                    String checkValue = (String) responseFromServer;
                    if(!checkValue.equalsIgnoreCase("done"))
                        key = (String) responseFromServer;
                    else{
                        break;
                    }
                }
            }
            if(message != null && key != null){
                key = Cryption.RSA.Decryption(key, MainController.privateKey);
                String[] keyPart = key.split(";;");
                if(keyPart[1].equalsIgnoreCase("2")){
                    image = (File) Cryption.AES.DecryptionObject(message, key);
                    return image;
                }
            }
        }
        catch(Exception error){
            error.printStackTrace();
        }
        return null;
    }

    public void Handler() {
        cancelBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Stage primaryStage = (Stage) wrapPane.getScene().getWindow();
                primaryStage.close();
            }
        });

        confirmBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                RadioButton selectedBtn = (RadioButton) imageFormatGroup.getSelectedToggle();
                try {

                    BufferedImage imgIn = ImageIO.read(inputImageFile);
                    outputImageFile = new File(inputImageFile.getParentFile() + "/" + getFileName(inputImageFile) + "." + getFileExtension(inputImageFile));
                    ImageIO.write(imgIn, getFileExtension(inputImageFile), outputImageFile);
                    outputImageFile = sendImage(outputImageFile, "change-extension", selectedBtn.getText().toLowerCase());
                    inputImageFile.delete();
                    inputImageFile = outputImageFile;
                    outputImageFile = null;
                    Stage primaryStage = (Stage) wrapPane.getScene().getWindow();
                    primaryStage.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void setDefault(){
        setSelected(getFileExtension(inputImageFile));
    }

    public String getFileName(File file){
        return file.getName().split("\\.")[0];
    }

    public String getFileExtension(File file){
        return file.getName().split("\\.")[1];
    }

    public void setSelected(String imgFormat){
        if(imgFormat.equalsIgnoreCase("png"))
            pngBtn.setSelected(true);
        else if(imgFormat.equalsIgnoreCase("jpg"))
            jpgBtn.setSelected(true);
        else
            jpegBtn.setSelected(true);
    }
}
