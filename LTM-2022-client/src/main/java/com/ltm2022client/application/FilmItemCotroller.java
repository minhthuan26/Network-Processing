package com.ltm2022client.application;

import com.ltm2022client.models.Film;
import com.ltm2022client.models.Review;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class FilmItemCotroller implements Initializable {

    @FXML
    private Label actorLbl;

    @FXML
    private Label directorLbl;

    @FXML
    private Label gernLbl;

    @FXML
    private Label imdbLbl;

    @FXML
    private Label nameLbl;

    @FXML
    private ImageView posterImg;

    @FXML
    private Button trailerBtn;

    @FXML
    private TextArea desTxf;

    @FXML
    private Label yearLbl;

    @FXML
    private GridPane gridPane;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Handler();
    }

    public void setValue(Film film, ObservableList<Review> reviews) throws IOException {
        yearLbl.setText(film.getYear());
//        trailerBtn.;
        if(film.getTrailer().equals("None"))
            trailerBtn.setDisable(true);
        String path = System.getProperty("user.dir");
        Image img = new Image(new File(path + "/src/main/resources/com/ltm2022client/application/Icons/film_icon.png").toURI().toURL().toString());
        if(!film.getPoster().equals("None"))
            img = new Image(film.getPoster());
        posterImg.setImage(img);
        nameLbl.setText(film.getName());
        imdbLbl.setText(film.getImdb());
        gernLbl.setText(film.getGern());
        desTxf.setText(film.getDescription());
        directorLbl.setText(film.getDirector());
        actorLbl.setText(film.getActor());
        for(int i=0; i<reviews.size(); i++){
            FXMLLoader reviewLoader = new FXMLLoader();
            reviewLoader.setLocation(getClass().getResource("review-film.fxml"));
            AnchorPane reviewBox = reviewLoader.load();
            ReviewFilmController reviewFilmController = reviewLoader.getController();
            reviewFilmController.setValue(reviews.get(i));
            gridPane.add(reviewBox, 1, i+1);
            GridPane.setMargin(reviewBox, new Insets(10));
        }
    }

    public void Handler(){
        trailerBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    Stage newStage = new Stage();
                    PlayTrailerMain playTrailerMain = new PlayTrailerMain();

                    Stage oldStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    newStage.initModality(Modality.WINDOW_MODAL);
                    newStage.initOwner(oldStage);

                    playTrailerMain.start(newStage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
