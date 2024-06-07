package com.ltm2022client.application;

import com.ltm2022client.models.Film;
import com.ltm2022client.models.Review;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.crypto.SealedObject;

import static com.ltm2022client.application.MainController.objectIn;

public class SearchFilmController implements Initializable {
    private static final ObservableList<Film> searchList = FXCollections.observableArrayList();
    private static final ObservableList<Review> reviewFilmList = FXCollections.observableArrayList();
    private static final String errorCharacter = "'\"@#^&*\\{}[]|`~<>/;()+";

    @FXML
    private TextField searchTxtField;

    @FXML
    private Button searchBtn;

    @FXML
    private AnchorPane mainPane;

    @FXML
    private AnchorPane filmField;

    private static Stage primaryStage;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Handler();
    }

    private boolean checkRegex(String value, String regex){
        for(int i=0; i<value.length(); i++){
            for(int j=0; j<regex.length(); j++)
                if(value.charAt(i) == regex.charAt(j))
                    return true;
        }
        return false;
    }

    public void Handler() {
        searchBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                String rawValue = searchTxtField.getText().trim();
                if(checkRegex(rawValue, errorCharacter) || rawValue.length() == 0){
                    errorAlert("Lỗi", "Bạn đã nhập kí tự đặc biêt hoặc không nhập gì, vui lòng kiểm tra lại.");
                }
                else{
                    try {
                        if (searchList.size() != 0){
                            searchList.clear();
                        }

                        if (reviewFilmList.size() != 0){
                            reviewFilmList.clear();
                        }
                        String key = Cryption.RandomKey() + ";;1";
                        SealedObject encryptedValue = (SealedObject) Cryption.AES.EncryptionObject(rawValue, key);
                        key = Cryption.RSA.Encryption(key, MainController.serverPublicKey);
                        MainController.objectOut.writeObject(encryptedValue);
                        MainController.objectOut.writeObject(key);
                        MainController.objectOut.flush();

                        Object responseFromServer;
                        SealedObject message = null;
                        String content;
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
                            if(keyPart[1].equalsIgnoreCase("1")){
                                content = (String) Cryption.AES.DecryptionObject(message, key);
                                String resultValues = content;
                                String[] tmp = resultValues.split(";;");
                                if(tmp.length != 1){
                                    Film film = new Film();
                                    film.setName(tmp[0]);
                                    film.setYear(tmp[1]);
                                    film.setActor(tmp[2]);
                                    film.setImdb(tmp[3]);
                                    film.setPoster(tmp[4]);
                                    film.setTrailer(tmp[5]);
                                    film.setGern(tmp[6]);
                                    film.setDescription(tmp[7]);
                                    film.setDirector(tmp[8]);
                                    JSONArray reviewList = new JSONArray(tmp[9]);

                                    for (Object review : reviewList) {
                                        Review rv = new Review();
                                        JSONObject reviewToJson = new JSONObject(review.toString());
                                        rv.setTitle(reviewToJson.getString("title"));
                                        rv.setContent(reviewToJson.getString("content"));
                                        rv.setUserName(reviewToJson.getString("username"));
                                        reviewFilmList.add(rv);
                                    }
                                    searchList.add(film);
                                    loadFilmFromSearchList();
                                }
                                else{
                                    errorAlert("Thông báo", tmp[0]);
                                }
                            }
                        }
                    } catch (Exception error) {
                        error.printStackTrace();
                    }
                }
            }
        });
    }

    private void errorAlert(String title, String Message){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        DialogPane root = alert.getDialogPane();
//        root.getStylesheets().add((getClass().getResource("main.css")).toExternalForm());
        root.getScene().setFill(Color.TRANSPARENT);
        Stage dialogStage = (Stage) root.getScene().getWindow();
        dialogStage.initStyle(StageStyle.TRANSPARENT);
        alert.setContentText(Message);
        alert.setHeaderText(null);
        ButtonType okBtn = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(okBtn);
        alert.show();
    }

    public void loadFilmFromSearchList() throws IOException {
        if(searchList.size() > 0){
            for (Film film : searchList) {
                FXMLLoader filmLoader = new FXMLLoader();
                filmLoader.setLocation(getClass().getResource("film-item.fxml"));
                AnchorPane filmBox = filmLoader.load();
                FilmItemCotroller filmItemCotroller = filmLoader.getController();
                filmItemCotroller.setValue(film, reviewFilmList);
                mainPane.getChildren().clear();
                mainPane.getChildren().add(filmBox);
                AnchorPane.setTopAnchor(filmBox,0.0);
                AnchorPane.setBottomAnchor(filmBox,0.0);
                AnchorPane.setLeftAnchor(filmBox,0.0);
                AnchorPane.setRightAnchor(filmBox,0.0);
            }
        }
    }
}
