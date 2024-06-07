package com.ltm2022client.application;

import com.ltm2022client.models.Film;
import com.ltm2022client.models.Review;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.crypto.SealedObject;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;

import static com.ltm2022client.application.MainController.objectIn;
import static com.ltm2022client.application.MainController.objectOut;

public class ImageProcessingController implements Initializable {
    public static File inputImageFile = null;
    public static File outputImageFile = null;
    public static FileOutputStream fos = null;
    public static FileInputStream fis = null;

    @FXML
    private Button changeExtensionBtn;

    @FXML
    private Button choosePictureBtn;

    @FXML
    private Button compressBtn;

    @FXML
    private Button findWithGoogleBtn;

    @FXML
    private Button greyScaleBtn;

    @FXML
    private AnchorPane mainPane;

    @FXML
    private Button objectDetectBtn;

    @FXML
    private ImageView originImageView;

    @FXML
    private ImageView processedImageView;

    @FXML
    private Button refreshBtn;
    @FXML
    private AnchorPane subContentPane;

    private Stage primaryStage;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setDefault();
        Handler();
    }

    public void Handler() {
        choosePictureBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setTitle("Chọn 1 ảnh để xử lý");
                    fileChooser.getExtensionFilters().addAll(
                            new FileChooser.ExtensionFilter("PNG", "*.png"),
                            new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                            new FileChooser.ExtensionFilter("JPEG", "*.jpeg")
                    );
                    primaryStage = (Stage) mainPane.getScene().getWindow();
                    inputImageFile = fileChooser.showOpenDialog(primaryStage);
                    if (inputImageFile != null) {
                        Image img = new Image(inputImageFile.toURI().toURL().toString());
                        originImageView.setImage(img);
                        setDefault();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        refreshBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                inputImageFile = null;
                outputImageFile = null;
                originImageView.setImage(null);
                processedImageView.setImage(null);
                setDefault();
            }
        });

        greyScaleBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try{
                    BufferedImage imgIn = ImageIO.read(inputImageFile);
                    outputImageFile = new File(inputImageFile.getParentFile() + "/" + getFileName(inputImageFile) + "_greyscale." + getFileExtension(inputImageFile));
                    ImageIO.write(imgIn, getFileExtension(inputImageFile), outputImageFile);
                    outputImageFile = sendImage(outputImageFile, "grey-scale", getFileExtension(outputImageFile));

                    Image processedImage = new Image(outputImageFile.toURI().toURL().toString());
                    processedImageView.setImage(processedImage);
                }
                catch(Exception error){
                    error.printStackTrace();
                }
            }
        });

        objectDetectBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try{
                    BufferedImage imgIn = ImageIO.read(inputImageFile);
                    outputImageFile = new File(inputImageFile.getParentFile() + "/" + getFileName(inputImageFile) + "_objectDectect." + getFileExtension(inputImageFile));
                    ImageIO.write(imgIn, getFileExtension(inputImageFile), outputImageFile);
                    outputImageFile = sendImage(outputImageFile, "object-detect", getFileExtension(outputImageFile));

                    Image processedImage = new Image(outputImageFile.toURI().toURL().toString());
                    processedImageView.setImage(processedImage);
                }
                catch(Exception error){
                    error.printStackTrace();
                }
            }
        });

        changeExtensionBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    Stage newStage = new Stage();
                    ImageFormatMain imageFormat = new ImageFormatMain();
                    //ngăn tương tác với dashboard
                    Stage oldStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    newStage.initModality(Modality.WINDOW_MODAL);
                    newStage.initOwner(oldStage);
                    //chạy newStage
                    imageFormat.start(newStage);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        compressBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                try {
                    BufferedImage imgIn = ImageIO.read(inputImageFile);
                    outputImageFile = new File(inputImageFile.getParentFile() + "/" + getFileName(inputImageFile) + "_compress." + getFileExtension(inputImageFile));
                    ImageIO.write(imgIn, getFileExtension(inputImageFile), outputImageFile);
                    outputImageFile = sendImage(outputImageFile, "compress", getFileExtension(outputImageFile));

                    Image processedImage = new Image(outputImageFile.toURI().toURL().toString());
                    processedImageView.setImage(processedImage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
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

    public String getFileName(File file) {
        return file.getName().split("\\.")[0];
    }

    public String getFileExtension(File file) {
        return file.getName().split("\\.")[1];
    }

    public void setDefault() {
        if (inputImageFile == null) {
            changeExtensionBtn.setDisable(true);
            compressBtn.setDisable(true);
            findWithGoogleBtn.setDisable(true);
            greyScaleBtn.setDisable(true);
            objectDetectBtn.setDisable(true);
            refreshBtn.setDisable(true);
            choosePictureBtn.setDisable(false);
        } else {
            changeExtensionBtn.setDisable(false);
            compressBtn.setDisable(false);
            findWithGoogleBtn.setDisable(false);
            greyScaleBtn.setDisable(false);
            objectDetectBtn.setDisable(false);
            refreshBtn.setDisable(false);
            choosePictureBtn.setDisable(true);
        }
    }
}
