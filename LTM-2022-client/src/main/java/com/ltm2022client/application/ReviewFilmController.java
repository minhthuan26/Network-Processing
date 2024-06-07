package com.ltm2022client.application;

import com.ltm2022client.models.Review;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.ResourceBundle;

public class ReviewFilmController implements Initializable {

    @FXML
    private TextArea reviewContentTxf;

    @FXML
    private AnchorPane reviewPane;

    @FXML
    private Label reviewTitleLbl;

    @FXML
    private Label userLbl;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void setValue(Review review){
        reviewContentTxf.setText(review.getContent());
        reviewTitleLbl.setText(review.getTitle());
        userLbl.setText(review.getUserName());
    }
}
