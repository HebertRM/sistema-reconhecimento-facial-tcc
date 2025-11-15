package com.example.faceclient.controller;


import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Map;

public class MainController {

    @FXML
    private AnchorPane contentPane;

    @FXML
    private void handleMonitoramento() throws IOException {
        // Dessa maneira você pode instancia mais de um objeto MonitoramentoController (ou Threads)
        loadUI("monitoramento-view.fxml");
    }

    @FXML
    private void handleClientes() throws IOException {
        loadUI("clientes-view.fxml");
    }

    @FXML
    private void handleCadastros() throws IOException {
        loadUI("cadastros-view.fxml");
    }

    @FXML
    private void handleSuporte() throws IOException {
        loadUI("suporte-view.fxml");
    }

    @FXML
    private void handleSobre() throws IOException {
        loadUI("sobre-view.fxml");
    }

    // [ BUG ] - Esse metodo instacian um conttoller novo toda vez que é chamado
   /* private void loadUI2(String fxml) throws IOException {
        AnchorPane pane = FXMLLoader.load(getClass().getResource(fxml));
        contentPane.getChildren().setAll(pane);
    }*/


    // [ FIX ] - Reutiliza a mesma instância do AnchorPane/Controller por FXML
    private void loadUI(String fxml) throws IOException {
        @SuppressWarnings("unchecked")
        Map<String, AnchorPane> cache = (Map<String, AnchorPane>) contentPane.getProperties()
                .computeIfAbsent("viewCache", k -> new java.util.HashMap<String, AnchorPane>());

        AnchorPane pane = cache.get(fxml);
        if (pane == null) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/faceclient/principal/"+fxml));
            pane = loader.load();                 // controller é criado apenas na primeira vez
            cache.put(fxml, pane);                // guarda para reutilizar nas próximas chamadas
        }

        contentPane.getChildren().setAll(pane);   // reaproveita o mesmo nó/controller
    }




    public  void startSceneMain (Stage stage){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main-view.fxml"));
            Scene loginScene = new Scene(fxmlLoader.load());
            stage.setScene(loginScene);
            stage.setTitle("Menu Principal");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error ao abrir o Menu Principal! " + e.getMessage());
        }
    }


}
