package com.example.faceclient.service;

import java.awt.image.BufferedImage;

import com.example.faceclient.Alert.AlertaUtil;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.*;

import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;


public class Captura {

     BufferedImage frameCapturado;
     Mat faceCapturada;
     Thread threadStartCameraCapturaFacial, threadStartExibirCameraImageView;
     boolean rodandoCameraImageView;
     OpenCVFrameGrabber camera;




    public void startCameraCapturaFacial(int idOpenCVCamera) {
          threadStartCameraCapturaFacial = Thread.ofVirtual().unstarted(() -> {
            OpenCVFrameConverter.ToMat converteMat = new OpenCVFrameConverter.ToMat(); // converte para Matriz
            camera = new OpenCVFrameGrabber(idOpenCVCamera); // acessa a webcam padrão

            try {
                camera.start(); // ligando a camera
                CascadeClassifier detectorFace = new CascadeClassifier("models_training/haarcascade_frontalface_alt.xml"); //Haar Cascade
                Frame frame = null;
                Mat imagemColorida = new Mat();

                while ((frame = camera.grab())!=null) { // passando todos os frames da camera pelo loop
                    imagemColorida = converteMat.convert(frame); // convertendo Frame para matriz colorida
                    Mat imagemCinza = new Mat();
                    cvtColor(imagemColorida, imagemCinza, COLOR_BGRA2GRAY); // convertendo a imagem em escala de cinza
                    RectVector facesDetectadas = new RectVector();
                    // (aqui define tamanho do rosto detectadp)
                    detectorFace.detectMultiScale(imagemCinza, facesDetectadas, 1.1, 1, 0, new Size(150, 150), new Size(500, 500));

                    for (int i = 0; i < facesDetectadas.size(); i++) {
                        Rect dadosFace = facesDetectadas.get(0); // A localização dos retangulos nas faces detectadas
                        rectangle(imagemColorida, dadosFace, new Scalar(0, 0, 255, 0));
                        faceCapturada = new Mat(imagemCinza, dadosFace);
                        resize(faceCapturada, faceCapturada, new Size(160, 160)); // recortanto
                    }
                    Java2DFrameConverter toJava2D = new Java2DFrameConverter();
                    frameCapturado = toJava2D.convert(frame); // essa variavel guarda a frame capturada
                    Thread.yield();
                }
            } catch (FrameGrabber.Exception e) {
                if (!(Thread.currentThread().isInterrupted()|| (e.getMessage() != null && e.getMessage().contains("Could not grab frame")))) {
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
        threadStartCameraCapturaFacial.start();
    }

    public boolean stopReconhecerFaceCamera() {
        if (threadStartCameraCapturaFacial != null && threadStartCameraCapturaFacial.isAlive()) {
            try {
                // interrompe o fluxo de captura antes do interrupt
                if (camera != null) {
                    System.out.println("Executando o stop da camera");
                    camera.stop();    // para o fluxo de vídeo
                    camera.release(); // libera o dispositivo
                }
                threadStartCameraCapturaFacial.interrupt();
                /**
                for(int r = 0;threadReconhecerFaceCamera.isAlive();r ++) {
                    System.out.println("Thread da câmera ainda não encerrou totalmente.");
                    threadReconhecerFaceCamera.join(2000);
                    if(r > 15){
                        camera.stop();    // para o fluxo de vídeo
                        camera.release();
                        threadReconhecerFaceCamera.interrupt();
                        AlertaUtil.mostrarErro("ERROR AO DESATIVAR A CAMERA", "Dispositivo esta demorando muito para desativar.");
                        return false;
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); */
            } catch (Exception e) {
                AlertaUtil.mostrarErro("ERROR NO DISPOSITIVO 4", "Houve uma falha ao desativar a camera.");
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

    public BufferedImage getFrameCapturado() {
        return frameCapturado;
    }

    public void setThreadStartCameraCapturaFacial(Thread threadStartCameraCapturaFacial) {
        this.threadStartCameraCapturaFacial = threadStartCameraCapturaFacial;
    }

    public Mat getFaceCapturada() {
        return faceCapturada;
    }
}
