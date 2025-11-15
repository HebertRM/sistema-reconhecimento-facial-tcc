package com.example.faceclient.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

public class HelloController {

    @FXML private Label InstrucaoEntraApp;
    @FXML private Label textIntrodWelcome;

    @FXML
    private void initialize() {
        exibirMensagemIntrodWelcome(textIntrodWelcome);
    }

    @FXML
    protected void onHelloButtonClick2(ActionEvent event) {
        InstrucaoEntraApp.setText("Iniciando Aplicativo FaceClient...");
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Pegando o stage atual
        abrirJanelaMenu(stage);
    }


    public  void abrirJanelaMenu (Stage stage){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/faceclient/principal/main-view.fxml"));
            Scene loginScene = new Scene(fxmlLoader.load());
            stage.setScene(loginScene);
            stage.setTitle("Menu do Sistema");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error ao abrir a tela de Login! " + e.getMessage());
        }
    }

    private void exibirMensagemIntrodWelcome(Label IntrodWelcome) {
        String mensagem = "Este sistema utiliza tecnologia de reconhecimento facial para auxiliar na segurança e controle de acesso de ambientes.\n" +
                "Nosso objetivo é oferecer uma solução moderna, confiável e prática para a identificação segura de clientes, garantindo proteção e agilidade no processo de autenticação.";
        IntrodWelcome.setText(mensagem);
    }


}