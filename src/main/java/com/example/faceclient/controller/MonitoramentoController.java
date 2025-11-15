package com.example.faceclient.controller;

import com.example.faceclient.Alert.AlertaUtil;
import com.example.faceclient.model.DispositivoDTO;
import com.example.faceclient.service.CameraDetectorWindows;
import com.example.faceclient.service.Captura;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MonitoramentoController {

    // [ TESTE ] - Formato de data/hora dos logs
    DateTimeFormatter HHMMSS =  DateTimeFormatter.ofPattern("'['dd/MM/yyyy - HH:mm:ss']'");


    @FXML private TextArea logArea;
    @FXML private ScrollPane scroll;
    @FXML private VBox listaDispositivos;

    Stage stageFormDispositivo;


    /**
     * Botão "Criar Dispositivo" (exemplo simples)
     */
    @FXML
    private void handleCriarDispositivo(ActionEvent event) {
        System.out.println("Dispositivos listados : " + listaDispositivos.getChildren().size()
                + ". Detectados nesse windows : " + CameraDetectorWindows.consultarDispositivosWindows().size());
        if (listaDispositivos.getChildren().size() >= CameraDetectorWindows.consultarDispositivosWindows().size()) {
            AlertaUtil.mostrarAviso("Limite de Dispositivos Cadastrados", "Você excedeu seu limite de dispositivos cadastrados, pois foram detectados apenas "
                    + CameraDetectorWindows.consultarDispositivosWindows().size() + " dispositivos de camera nessa maquina.");
            return;
        }
        try {
            if (stageFormDispositivo != null && stageFormDispositivo.isShowing()) {
                stageFormDispositivo.toFront();   // traz para frente
                stageFormDispositivo.requestFocus(); // foca na janela
                return;
            }

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/faceclient/formularios/form_dispositivo-view.fxml"));
            Parent root = fxmlLoader.load();

            FormDispositivoController formDispositivoController = fxmlLoader.getController(); //Exemplo usar o controller do FXML
            formDispositivoController.formSalvarNovoDispositivo(); // Possibidade de desabilitar os campos antes de abrir formulario.
            stageFormDispositivo = new Stage(); // Criando janela nova
            stageFormDispositivo.setTitle("Formulário de Dispositivo");
            stageFormDispositivo.setScene(new Scene(root));

            //stageFormDispositivo.initModality(Modality.WINDOW_MODAL);              // trava a janela mãe (opcional)
            stageFormDispositivo.initOwner(((Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow()));                      // define a janela dona
            stageFormDispositivo.setResizable(true);                              // Controla o tamanho da janela(opcional)
            stageFormDispositivo.show();  // ou showAndWait (bloqueia as outras até o usuário fechar essa stage)

             // Esse pode manter as outras janelas rodando com stage.show()
            stageFormDispositivo.setOnHidden(evt -> {
                formDispositivoController.getResult().ifPresent(dispositivoDTO -> {
                    dispositivoDTO.getReconhecimento().setLogAreaMonitoramento(logArea);
                    listaDispositivos.getChildren().add(criarItem(dispositivoDTO));
                });
            });
        } catch (Exception e) {
            e.printStackTrace();
            // opcional: mostrar um Alert de erro
        }
    }


    //  Implementar Logs de inicializacao
    private void log(String msg) {
        Platform.runLater(() -> { // Alterando UI usando o proprio JavaFX 'Platform', para não ter erro.
            String[] lines = logArea.getText().split("\n");

            if (lines.length >= 1000) {
                int removeIndex = logArea.getText().indexOf("\n", 0);
                if (removeIndex > 0) {
                    logArea.deleteText(0, removeIndex + 1); // Remove primeira linha
                }
            }
        logArea.appendText(msg + "\n");  // Adiciona ao final + nova linha
        //logArea.positionCaret(logArea.getLength()); // Força a rola pro fim (auto-scroll)
        });
    }

    // [ TESTE ] - Teste de estresse na caixa de log - ( Vamos criar Thereads para rodar as cameras )
    public Thread logsTest () {
         Thread logThread = new Thread(() -> {
            try {
                for (int i = 0; i <= 3000; i++) {
                    Thread.sleep(1000);
                    log(LocalDateTime.now().format(HHMMSS) + " - Inicializando teste de logs - " + i);
                }
            } catch(Exception e){
                e.printStackTrace();
            }
        });
         //   logThread.setDaemon(true); // opcional: encerra junto com o app
        return logThread;
    }



    // Carregar dispositivos inicias pode ser por aqui
    private final List<DispositivoDTO> dispositivosIniciais = List.of(
            // new DispositivoDTO("502.468.202.50","Entrada Principal Teste","Teste_ARD-01",true),
    );

    @FXML
    private void initialize() {
        log("Aplicação de monitoramento iniciada");
        // Garantir que os itens ocupem a largura do Scroll
        listaDispositivos.setFillWidth(true);
        scroll.setFitToWidth(true);

        // Carregar dispositivos
        listaDispositivos.getChildren().clear();
        for (DispositivoDTO dispositivoDTO : dispositivosIniciais) {
            listaDispositivos.getChildren().add(criarItem(dispositivoDTO));
            log(LocalDateTime.now().format(HHMMSS)+" - Inicializando a monitoração das cameras : "+ dispositivoDTO.getnomeDispositivo()); //teste
        }


    }


    /** Carrega o FXML do item e injeta os dados */
    private Node criarItem(DispositivoDTO dispositivoDTO) {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/faceclient/principal/dispositivo-view.fxml"));
            VBox itemRoot = loader.load();
            DispositivoController dispositivoController = loader.getController();
            dispositivoController.setData(dispositivoDTO);

            // Para ocupar a largura do ScrollPane
            itemRoot.setMaxWidth(Double.MAX_VALUE);
            return itemRoot;
        } catch (IOException e) {
            throw new RuntimeException("Erro ao carregar ItemDispositivo.fxml", e);
        }
    }
}
