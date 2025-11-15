package com.example.faceclient.model;

import com.example.faceclient.service.Captura;
import com.example.faceclient.service.Reconhecimento;
import javafx.scene.control.SingleSelectionModel;

import java.util.List;

public class DispositivoDTO {

    private String nomeDispositivo, ipArduino,portaArduino, cameraCombo;
    private Reconhecimento reconhecimento;

    public DispositivoDTO() {}

    public DispositivoDTO(String nomeDispositivo, String ipArduino, String portaArduino, String cameraCombo, Reconhecimento reconhecimento) {
        this.nomeDispositivo = nomeDispositivo;
        this.ipArduino = ipArduino;
        this.portaArduino = portaArduino;
        this.cameraCombo = cameraCombo;
        this.reconhecimento = reconhecimento;
    }

    public String getnomeDispositivo() {
        return nomeDispositivo;
    }

    public void setnomeDispositivo(String nomeDispositivo) {
        this.nomeDispositivo = nomeDispositivo;
    }

    public String getNomeDispositivo() {
        return nomeDispositivo;
    }

    public void setNomeDispositivo(String nomeDispositivo) {
        this.nomeDispositivo = nomeDispositivo;
    }

    public String getIpArduino() {
        return ipArduino;
    }

    public void setIpArduino(String ipArduino) {
        this.ipArduino = ipArduino;
    }

    public String getPortaArduino() {
        return portaArduino;
    }

    public void setPortaArduino(String portaArduino) {
        this.portaArduino = portaArduino;
    }

    public String getCameraCombo() {
        return cameraCombo;
    }

    public void setCameraCombo(String cameraCombo) {
        this.cameraCombo = cameraCombo;
    }

    public Reconhecimento getReconhecimento() {
        return reconhecimento;
    }

    public void setReconhecimento(Reconhecimento reconhecimento) {
        this.reconhecimento = reconhecimento;
    }
}
