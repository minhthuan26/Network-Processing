package com.ltm2022client.application;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;

public class PlayTrailerController implements Initializable {

    @FXML
    private AnchorPane subContentPane;

    @FXML
    private WebView webView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        WebEngine engine = webView.getEngine();
        engine.setJavaScriptEnabled(true);
        engine.load("http://google.com");
    }
}
