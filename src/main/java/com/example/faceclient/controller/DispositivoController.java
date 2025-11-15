package com.example.faceclient.controller;


import com.example.faceclient.Alert.AlertaUtil;
import com.example.faceclient.model.DispositivoDTO;
import com.example.faceclient.repository.ClienteCSV;
import com.example.faceclient.service.CameraDetectorWindows;
import com.example.faceclient.service.Reconhecimento;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.event.ActionEvent;


public class DispositivoController {
    @FXML private Label lblNomeDispositivo;
    @FXML private Label lblCamera;
    @FXML private Label lblArduino;
    @FXML private Button btLigarDispositivo;

    private String nomeDispositivo, ipArduino, portaArduino, cameraCombo;
    private Reconhecimento reconhecimento;

    private DispositivoDTO dispositivoDTO;

    Stage stageEditFormDispositivo, stageExibirCameraReconhecimento;

    public DispositivoController setData (DispositivoDTO dispositivoDTO) {
        this.dispositivoDTO = dispositivoDTO;
        this.lblNomeDispositivo.setText("Nome: "+dispositivoDTO.getnomeDispositivo());
        this.lblCamera.setText("Camera: "+dispositivoDTO.getCameraCombo());
        this.lblArduino.setText("Tipo: "+dispositivoDTO.getIpArduino()+":"+dispositivoDTO.getPortaArduino());
        this.ipArduino = dispositivoDTO.getIpArduino();
        this.portaArduino = dispositivoDTO.getPortaArduino();
        this.cameraCombo = dispositivoDTO.getCameraCombo();
        this.reconhecimento = dispositivoDTO.getReconhecimento();
        return this;
    }

    @FXML
    public void onEditarDispositivo(ActionEvent event){
        try {
            if (stageEditFormDispositivo != null && stageEditFormDispositivo.isShowing()) {
                stageEditFormDispositivo.toFront();   // traz para frente
                stageEditFormDispositivo.requestFocus(); // foca na janela
                return;
            }

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/faceclient/formularios/form_dispositivo-view.fxml"));
            Parent root = fxmlLoader.load();

            FormDispositivoController formDispositivoController = fxmlLoader.getController(); //Exemplo usar o controller do FXML
            formDispositivoController.formEditarDispositivo(dispositivoDTO); // Possibidade de desabilitar os campos antes de abrir formulario.
            stageEditFormDispositivo = new Stage(); // Criando janela nova
            stageEditFormDispositivo.setTitle("Formulário de Dispositivo");
            stageEditFormDispositivo.setScene(new Scene(root));
            stageEditFormDispositivo.initOwner(((Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow()));                      // define a janela dona
            stageEditFormDispositivo.setResizable(true);                              // Controla o tamanho da janela(opcional)
            stageEditFormDispositivo.show();  // ou showAndWait (bloqueia as outras até o usuário fechar essa stage)

            // Esse pode manter as outras janelas rodando com stage.show()
            stageEditFormDispositivo.setOnHidden(evt -> {
                formDispositivoController.getResult().ifPresent(dispositivoDTO -> {
                    this.setData(dispositivoDTO);
                });
            });

        } catch (Exception e) {
            e.printStackTrace();
            // opcional: mostrar um Alert de erro
        }
    }

    @FXML
    public void onLigardispositivo(Event event)  {
        if(btLigarDispositivo.getText().equals("Ligar")){
            int index = CameraDetectorWindows.consultandoIndexDispositivoWindows(cameraCombo);
            reconhecimento.startCameraReconhecimentoFacial(index,ClienteCSV.listarTodosClientesCSV());
            btLigarDispositivo.setText("Desligar");
        }else{
            reconhecimento.stopCameraReconhecimentoFacial();
            btLigarDispositivo.setText("Ligar");
        }
    }

    @FXML
    public void onVerCameraDispositivo(ActionEvent actionEvent) {
        try{
            if (stageExibirCameraReconhecimento != null && stageExibirCameraReconhecimento.isShowing()) {
                stageExibirCameraReconhecimento.toFront();   // traz para frente
                stageExibirCameraReconhecimento.requestFocus(); // foca na janela
                return;
            }

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/faceclient/cam-view.fxml"));
            Parent root = fxmlLoader.load();

            CamController camController = fxmlLoader.getController(); //Exemplo usar o controller do FXML

            camController.iniciarVerCameraReconhecimentoFacial(reconhecimento); // Possibidade de desabilitar os campos antes de abrir formulario.

            stageExibirCameraReconhecimento = new Stage();
            stageExibirCameraReconhecimento.setTitle("Camera Captura");
            stageExibirCameraReconhecimento.setScene(new Scene(root));
            stageExibirCameraReconhecimento.initOwner(((Stage) ((javafx.scene.Node) actionEvent.getSource()).getScene().getWindow()));                      // define a janela dona
            stageExibirCameraReconhecimento.setResizable(true);                              // Controla o tamanho da janela(opcional)
            stageExibirCameraReconhecimento.show();  // ou showAndWait (bloqueia as outras até o usuário fechar essa stage)

            stageExibirCameraReconhecimento.setOnHidden(evt -> {
                camController.encerrarCapturaFacial();
            });
        } catch (Exception e) {
            e.printStackTrace();
            AlertaUtil.mostrarErro("Falha ao abrir tela de captura", "Houve uma falha ao abrir a tela da camera, necessário solicitar apoio ao suporte.");
        }
    }

    public DispositivoDTO getDispositivoDTO() {
        return dispositivoDTO;
    }

    public void setDispositivoDTO(DispositivoDTO dispositivoDTO) {
        this.dispositivoDTO = dispositivoDTO;
    }


}
