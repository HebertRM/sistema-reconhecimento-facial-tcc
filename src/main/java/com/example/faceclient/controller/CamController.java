package com.example.faceclient.controller;

import com.example.faceclient.Alert.AlertaUtil;
import com.example.faceclient.service.Captura;
import com.example.faceclient.service.Reconhecimento;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;

public class CamController {

    public VBox vboxBtnCam;
    public Button cameraBtn;
    public Label cameraStatusLabel;

    Captura captura = new Captura();
    Reconhecimento reconhecimento;

    int idCliente, qtdFotosClientes;

    boolean clienteNovo, exibindoReconhecimento;

    public ImageView cameraView;

    @FXML
    public void iniciarCapturaFacial(int idCliente, boolean clienteNovo, int qtdFotos) {
        System.out.println("iniciando camera de captura facial");
        this.clienteNovo = clienteNovo;
        this.idCliente = idCliente;
        this.qtdFotosClientes = qtdFotos;
        captura.startCameraCapturaFacial(0);
        captura.startExibirCameraImageView(cameraView);
    }

    public void iniciarVerCameraReconhecimentoFacial(Reconhecimento reconhecimento){
        this.reconhecimento = reconhecimento;
        reconhecimento.startExibirCameraImageView(cameraView);
        vboxBtnCam.setVisible(false);
        cameraStatusLabel.setVisible(false);
        cameraStatusLabel.setVisible(false);
        exibindoReconhecimento = true;
    }

    public void encerrarCapturaFacial(){
        System.out.println("Encerrando janela da camera!");
        captura.stopReconhecerFaceCamera();
        captura.stopExibirCameraImageView();
        if(exibindoReconhecimento) reconhecimento.stopExibirCameraImageView();
    }

    @FXML
    public void onTirarFoto(ActionEvent actionEvent) {
        String dataAtual = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssms"));
        boolean excedeuQtdDeFotos = (qtdFotosClientes >= 30);
        System.out.println("Quantidade de fotos atuais : "+ qtdFotosClientes);
        if (captura.getFaceCapturada() != null && !excedeuQtdDeFotos) {
            if (clienteNovo) {
                imwrite("captures/tmp/capture_" + dataAtual + "_face_" + idCliente + ".png", captura.getFaceCapturada());
            } else {
                imwrite("captures/capture_" + dataAtual + "_face_" + idCliente + ".png", captura.getFaceCapturada());
            }
            qtdFotosClientes++;
            System.out.println("Foto capturada do cliente "+idCliente+" com sucesso!");
        } else if (excedeuQtdDeFotos) {
            AlertaUtil.mostrarAviso("Excedeu o limite de fotos","O cliente excedeu o limite de 30 fotos.");
            fecharJanelaCaptura(actionEvent);
        } else {
            AlertaUtil.mostrarAviso("Registro facial não capturado","Não foi capturado nenhum registro facial através da camera, por favor, verifique o dispositivo.");
        }
    }

    public void fecharJanelaCaptura(ActionEvent event){
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}
