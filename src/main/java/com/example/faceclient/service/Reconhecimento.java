package com.example.faceclient.service;


import com.example.faceclient.Alert.AlertaUtil;
import com.example.faceclient.model.ClienteDTO;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.IntPointer;
import static org.bytedeco.opencv.global.opencv_imgproc.FONT_HERSHEY_PLAIN;

import org.bytedeco.javacv.*;
import org.bytedeco.javacv.Frame;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_face.*;

import static org.bytedeco.opencv.global.opencv_imgproc.COLOR_BGRA2GRAY;
import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;
import static org.bytedeco.opencv.global.opencv_imgproc.putText;
import static org.bytedeco.opencv.global.opencv_imgproc.rectangle;
import static org.bytedeco.opencv.global.opencv_imgproc.resize;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javafx.scene.control.TextArea;


/**
 *
 * @author Jones
 */
public class Reconhecimento {
    BufferedImage frameCapturado;
    Mat faceCapturada;
    Thread threadStartCameraReconhecimentoFacial, threadStartExibirCameraImageView;
    boolean rodandoCameraImageView, exibiuMensagemFalhaLog;
    OpenCVFrameGrabber camera;
    TextArea logAreaMonitoramento;
    int idRepetido, contadorLiberar;
    String arduinoIP, arduinoPorta;
    HttpClientService httpClientService = new HttpClientService();


    public void startCameraReconhecimentoFacial(int idOpenCVCamera, List<ClienteDTO> clientes) {
        threadStartCameraReconhecimentoFacial = Thread.ofVirtual().unstarted(() -> {
            OpenCVFrameConverter.ToMat converteMat = new OpenCVFrameConverter.ToMat(); // converte para Matriz
            camera = new OpenCVFrameGrabber(idOpenCVCamera); // Acessa a webcam padrao
            try {
                camera.start(); // inicializando webcam
                // Haar Cascade  detecta as faces da camera
                CascadeClassifier detectorFace = new CascadeClassifier("models_training/haarcascade_frontalface_alt.xml");

                FaceRecognizer reconhecedor = EigenFaceRecognizer.create();             // inicia o objeto EigenFaceRecognizer();
                reconhecedor.read("models_training/classificadorEigenFaces.yml");        // declarar a path do classificador treinado
                //reconhecedor.setThreshold(0);

                Frame frame = null; //
                Mat imagemColorida = new Mat();

                while ((frame = camera.grab())!=null) {
                    imagemColorida = converteMat.convert(frame); // convertendo Frame para matriz colorida
                    Mat imagemCinza = new Mat();
                    cvtColor(imagemColorida, imagemCinza, COLOR_BGRA2GRAY); // converte para escala de Cinza
                    RectVector facesDetectadas = new RectVector();
                    // (aqui esta dectando os rostos das frames) - tambem define tamanho do rosto que será detectado
                    detectorFace.detectMultiScale(imagemCinza, facesDetectadas, 1.1, 1, 0, new Size(150, 150), new Size(500, 500));

                    for (int i = 0; i < facesDetectadas.size(); i++) {
                        Rect dadosFace = facesDetectadas.get(i); // A localização dos retangulos nas faces detectadas
                        rectangle(imagemColorida, dadosFace, new Scalar(0, 0, 255, 0));
                        faceCapturada = new Mat(imagemCinza, dadosFace);

                        IntPointer rotulo = new IntPointer(1);
                        DoublePointer confianca = new DoublePointer(1);

                        if ((faceCapturada.size(0) == 160) || (faceCapturada.size(1) == 160)){
                            continue; // Aqui filtra as micros faces que podem ser encontradas pela IA
                        }
                        resize(faceCapturada, faceCapturada, new Size(160, 160));

                        reconhecedor.predict(faceCapturada, rotulo, confianca); // Realiza o reconhecimento com base na fazecapturada

                        int predicao = rotulo.get(0);
                        String nome;
                        if (predicao == -1) {
                            nome = "Desconhecido";
                        } else {
                            ClienteDTO clienteDTO =  ClienteDTO.getClientePorId(clientes,predicao);
                            nome = clienteDTO.getTxtNome().split(" ")[0].trim() + " - " + confianca.get(0);
                            liberarEntradaCliente(clienteDTO);
                        }

                        int x = Math.max(dadosFace.tl().x() - 10, 0);
                        int y = Math.max(dadosFace.tl().y() - 10, 0);
                        putText(imagemColorida, nome, new Point(x, y), FONT_HERSHEY_PLAIN, 1.4, new Scalar(0,255,0,0));

                    }
                    Java2DFrameConverter toJava2D = new Java2DFrameConverter();
                    frameCapturado  = toJava2D.convert(frame);
                    Thread.yield();
                }
            } catch (FrameGrabber.Exception e) {
                if (!(Thread.currentThread().isInterrupted() || (e.getMessage() != null && e.getMessage().contains("Could not grab frame")))) {
                    e.printStackTrace();
                    AlertaUtil.mostrarErro("ERROR NO DISPOSITIVO 1", "Houve uma falha ao executar a camera com reconhecimento facial.");
                }
                System.out.println("Falha esperada ao desativar a camera.");
            } catch (Exception ex) {
                ex.printStackTrace();
                // RuntimeException pode travar fxml
                AlertaUtil.mostrarErro("ERROR NO DISPOSITIVO 2", "Houve uma falha ao executar a camera com reconhecimento facial.");
            } finally {
                if (camera != null) {
                    try {
                        camera.stop();
                        camera.release(); // liberar recurso nativo também
                    } catch (Exception e) {
                        AlertaUtil.mostrarErro("ERROR NO DISPOSITIVO 3", "Houve uma falha ao desligar a camera.");
                        e.printStackTrace();
                    }
                }
            }
        });
        threadStartCameraReconhecimentoFacial.start();
        exibiuMensagemFalhaLog = false;
        contadorLiberar = 0;
    }

    public boolean stopCameraReconhecimentoFacial() {
        if (threadStartCameraReconhecimentoFacial != null && threadStartCameraReconhecimentoFacial.isAlive()) {
            try {
                // interrompe o fluxo de captura antes do interrupt
                if (camera != null) {
                    System.out.println("Executando o stop da camera");
                    camera.stop();    // para o fluxo de vídeo
                    camera.release(); // libera o dispositivo
                }
                threadStartCameraReconhecimentoFacial.interrupt();
            } catch (Exception e) {
                AlertaUtil.mostrarErro("Error encerrar a camera", "Houve uma falha ao desativar a camera.");
            }

            return true;
        }
        return false;
    }

    public void startExibirCameraImageView(ImageView imageView) {
        threadStartExibirCameraImageView = Thread.ofVirtual().unstarted(() -> {
            try {
                System.out.println("Executando a exibição da camera no ImageView ");
                rodandoCameraImageView = true;
                while (rodandoCameraImageView) {
                    if (frameCapturado != null) {
                        Image fx = SwingFXUtils.toFXImage(frameCapturado, null);
                        Platform.runLater(() -> imageView.setImage(fx));
                    }
                    Thread.yield();
                }
            }catch (Exception e){
                System.out.println("Houve uma falha ao exibir a camera"+e.getMessage());
            }finally {
                System.out.println("Deixando o cameraView nulo");
                Platform.runLater(() -> imageView.setImage(null));
            }
        });
        threadStartExibirCameraImageView.start();
    }

    public void stopExibirCameraImageView() {
        rodandoCameraImageView = false;
        threadStartExibirCameraImageView.interrupt();
    }


    private void log(String msg) {
        try {
            Platform.runLater(() -> { // Alterando UI usando o proprio JavaFX 'Platform', para não ter erro.
                String[] lines = logAreaMonitoramento.getText().split("\n");

                if (lines.length >= 1000) {
                    int removeIndex = logAreaMonitoramento.getText().indexOf("\n", 0);
                    if (removeIndex > 0) {
                        logAreaMonitoramento.deleteText(0, removeIndex + 1); // Remove primeira linha
                    }
                }
                logAreaMonitoramento.appendText(msg + "\n");  // Adiciona ao final + nova linha
                //logArea.positionCaret(logArea.getLength()); // Força a rola pro fim (auto-scroll)
            });
        } catch (Exception e) {
            if (!exibiuMensagemFalhaLog) {
                AlertaUtil.mostrarErro("Falha Logs", "Não foi possível gerar logs desse dispositivo. " +
                        "Caso o error persista, por gentileza, verificar com o time de suporte.");
                exibiuMensagemFalhaLog = true;
                e.printStackTrace();
            }
        }
    }

    private void liberarEntradaCliente(ClienteDTO clienteDTO) {
        int id = clienteDTO.getId();
        String primeiroNome = clienteDTO.getTxtNome().split(" ")[0].trim();
        if (logAreaMonitoramento != null) {
            contadorLiberar++;
            if(idRepetido!=id||contadorLiberar>100) {
                System.out.println("Iniciando a liberação da entrada para o cliente "+primeiroNome);
                idRepetido = id;
                Thread.ofVirtual().unstarted(() -> {
                    String dataAtual = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd-HH:mm:ss"));
                    contadorLiberar = 0;
                    Thread.yield();
                    log("Cliente identificado <ID=" + id + " | Nome=" + primeiroNome + " | Data=" + dataAtual + ">");
                    if (!clienteDTO.isStatus()){
                        log("Cliente "+primeiroNome+" não foi liberado devido ao status inativo. Data=" + dataAtual);
                        return;
                    }
                    try {
                        String retornoArduino = httpClientService.enviarComandoESP32(arduinoIP,arduinoPorta,primeiroNome);
                        log(retornoArduino);
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    } catch (Exception e) {
                        e.printStackTrace();
                        log("Cliente "+primeiroNome+" não foi liberado. Falha ao comunicar com ESP32.Data=" + dataAtual);
                    }
                }).start();
            }
        }
    }

    public void setDadosArduinoURL(String ip, String porta){
        this.arduinoIP = ip.trim();
        this.arduinoPorta = porta.trim();
    }


    public TextArea getLogAreaMonitoramento() {
        return logAreaMonitoramento;
    }

    public void setLogAreaMonitoramento(TextArea logAreaMonitoramento) {
        this.logAreaMonitoramento = logAreaMonitoramento;
    }

    public BufferedImage getFrameCapturado() {
        return frameCapturado;
    }

    public Thread getThreadStartCameraReconhecimentoFacial() {
        return threadStartCameraReconhecimentoFacial;
    }

    public void setThreadStartCameraReconhecimentoFacial(Thread threadStartCameraReconhecimentoFacial) {
        this.threadStartCameraReconhecimentoFacial = threadStartCameraReconhecimentoFacial;
    }

    public Thread getThreadStartExibirCameraImageView() {
        return threadStartExibirCameraImageView;
    }

    public void setThreadStartExibirCameraImageView(Thread threadStartExibirCameraImageView) {
        this.threadStartExibirCameraImageView = threadStartExibirCameraImageView;
    }


    public Mat getFaceCapturada() {
        return faceCapturada;
    }

}
