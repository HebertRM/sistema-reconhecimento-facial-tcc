package com.example.faceclient.service;


import java.io.File;
import java.io.FilenameFilter;
import java.nio.IntBuffer;
import static org.bytedeco.opencv.global.opencv_core.*;

import com.example.faceclient.Alert.AlertaUtil;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_face.*;
import static org.bytedeco.opencv.global.opencv_imgcodecs.*;
import static org.bytedeco.opencv.global.opencv_imgproc.resize;

/**
 *
 * @author Jones
 */
public class Treinamento {

    private static final String CAMINHO_FOTOS = "captures";
    private static final String CAMINHO_CLASSIFICADORES = "models_training";


    public static Thread startTreinamentoAlgotirmos() {
        return Thread.ofVirtual().unstarted(() -> {
            try {
                File diretorio = new File(CAMINHO_FOTOS); // Diretorio com as imagens de treino
                FilenameFilter filtroImagem = new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String nome) {
                        return nome.endsWith(".png"); // nome.endsWith(".jpg") || nome.endsWith(".gif")
                    }
                };


                if (diretorio == null || diretorio.length() == 0) { // interessante incluir uma verificação de diretorio e nao esta vazio
                    AlertaUtil.mostrarErro("Falha no Treinamento de Reconhecimento Facial", "Não foi encontrado fotos no caminho : " +
                            CAMINHO_FOTOS + " para seguir com a classificação e o treinamento de reconhecimento facial doc sclientes.");
                    return;
                }

                File[] arquivos = diretorio.listFiles(filtroImagem);

                MatVector fotos = new MatVector(arquivos.length); // E tipo uma lista para armazenar as mat das fotos
                Mat rotulos = new Mat(arquivos.length, 1, CV_32SC1); // Esse são informacoes das fotos
                IntBuffer rotulosBuffer = rotulos.createBuffer();

                int contador = 0;
                for (File imagem : arquivos) {
                    Mat foto = imread(imagem.getAbsolutePath(), IMREAD_GRAYSCALE); // converte imagem pra cinza
                    int classe = Integer.parseInt(imagem.getName().split("_")[3].replace(".png", "")); // Separa nome por ponto e puxa a classe
                    System.out.println("Coletando o ID " + imagem.getName().split("_")[3].replace(".png", "") + " da imagem " + imagem.getName());
                    resize(foto, foto, new Size(160, 160)); // Dimensionar a imagem para 160 para ter o tamanho padrão
                    fotos.put(contador, foto);
                    rotulosBuffer.put(contador, classe);
                    contador++;
                }

                // Gerando o modelo de treinamento para o classificador eigenfaces
                FaceRecognizer eigenfaces = EigenFaceRecognizer.create(); //criando modelo EigenFaceRecognizer
                eigenfaces.train(fotos, rotulos);
                eigenfaces.save(CAMINHO_CLASSIFICADORES + "/classificadorEigenFaces.yml");

                // Gerando o modelo de treinamento para o classificador fisherfaces
                FaceRecognizer fisherfaces = FisherFaceRecognizer.create();
                fisherfaces.train(fotos, rotulos);
                fisherfaces.save(CAMINHO_CLASSIFICADORES + "/classificadorFisherFaces.yml");

                // Gerando o modelo de treinamento para o classificador LBPH
                FaceRecognizer lbph = LBPHFaceRecognizer.create(2, 9, 9, 9, 1);
                lbph.train(fotos, rotulos);
                lbph.save(CAMINHO_CLASSIFICADORES + "/classificadorLBPH.yml");
            } catch (Exception e) {
                e.printStackTrace();
                AlertaUtil.mostrarAviso("Falha no Treinamento de Algoritmos de Reconhecimento", "Houve uma falha ao realizar o treinamento dos algoritmos com a lista de clientes atualizada." +
                        " Por gentileza, entrar em contato com o suporte.");

            }
        });
    }


    /**
     * ############# [ DEPOIS ] ##########
     * Use var e try-with-resources para deixar o código mais limpo e seguro.
     *
     * Valide se o diretório existe e se há imagens antes de treinar.
     *
     * Normalize brilho e contraste antes de treinar (ajuda na precisão).
     */

}