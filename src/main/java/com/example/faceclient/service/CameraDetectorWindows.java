package com.example.faceclient.service;

import org.bytedeco.ffmpeg.global.avdevice;
import org.bytedeco.ffmpeg.avdevice.AVDeviceInfoList;
import org.bytedeco.ffmpeg.avdevice.AVDeviceInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bytedeco.javacv.VideoInputFrameGrabber;

public class CameraDetectorWindows {

    private static List<String> procurarEntradaDispositivoCameraWindows() {
        List<String> listDispositivosCamera = new ArrayList<>();
        String[] devices = null;
        try {
            // Este método retorna os nomes dos dispositivos de vídeo
            devices = VideoInputFrameGrabber.getDeviceDescriptions();

            if (devices == null || devices.length == 0) {
                System.out.println("Nenhum dispositivo de câmera encontrado.");
                return listDispositivosCamera;
            }

            System.out.println("Câmeras conectadas:");
            for (int i = 0; i < devices.length; i++) {
                // O índice (i) é o 'deviceNumber' que você usará para abrir a câmera
                listDispositivosCamera.add(i+";"+devices[i]);
                System.out.println("Dispositivo " + i + ": " + devices[i]);
            }
        } catch (Exception e) {
            System.err.println("Erro ao listar dispositivos de vídeo:");
            e.printStackTrace();
        }finally {
            return listDispositivosCamera;
        }
    }

    public static List<String> consultarDispositivosWindows (){
        return procurarEntradaDispositivoCameraWindows();
    };

    public static String consultandoNomeDispositivoWindows(int index) {
       return procurarEntradaDispositivoCameraWindows().stream()
                .filter(s -> s.split(";")[1].equals(String.valueOf(index)))
                .findFirst()
                .orElse("");
    }

    public static int consultandoIndexDispositivoWindows(String nome) {
        return procurarEntradaDispositivoCameraWindows().stream()
                .filter(s -> s.split(";")[1].equals(nome)) // compara pelo nome
                .map(s -> Integer.parseInt(s.split(";")[0])) // converte índice para int
                .findFirst()
                .orElse(-1); // valor padrão caso não encontre
    }
}