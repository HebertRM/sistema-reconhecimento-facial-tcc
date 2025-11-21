package com.example.faceclient.controller;

import com.example.faceclient.Alert.AlertaUtil;
import com.example.faceclient.repository.ClienteCSV;
import com.example.faceclient.service.CameraDetectorWindows;
import com.example.faceclient.model.DispositivoDTO;
import com.example.faceclient.service.HttpClientService;
import com.example.faceclient.service.Reconhecimento;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.w3c.dom.ls.LSOutput;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;


public class FormDispositivoController {



    // COMPONENTES RODAPE [FXML]
    @FXML private Button deleteBtn;
    @FXML private Button canceleBtn;
    @FXML private Label validarFormSalvar;


    // CAMPOS DO FORMULARIO [FXML]
    @FXML private Button cameraBtn;
    @FXML private TextField cameraNameField;
    @FXML public  ComboBox cameraCombo;
    @FXML public TextField arduinoIpField;
    @FXML public TextField arduinoPortField;
    @FXML  public Label arduinoStatusLabel;

    // COMPONENTE DO CORPO [FXML]
    @FXML private ScrollPane root; // nó raiz do seu FXML (ScrollPane)
    @FXML private ImageView cameraView;
    @FXML private Pane overlay;
    @FXML private Rectangle roiRect;

    // Lista com os dados que serão retornados ao MonitoramentoController
    Optional<DispositivoDTO> result = Optional.empty();

    // Vai ligar a camera e executar o algotirmo de reconhecimento facial
    Reconhecimento reconhecimento = new Reconhecimento();

    // Teste chamada ao microcontrolador
    HttpClientService httpClientService = new HttpClientService();



    // enum de modos para aux na manipulação do ROI
    private enum Mode { NONE, DRAW, MOVE }
    private Mode mode = Mode.NONE;


    // pontos auxiliares
    private double pressX, pressY; // ponto inicial do mouse no overlay
    private double rectStartX, rectStartY; // pos inicial do retângulo ao mover


    @FXML // Metodo que inicializa ao carregar(load) o FXML
    private void initialize() {
        // carregar cameras da maquina no comboBox
        carregarCamerasWindows();

        // Alterando style dos botoes/checks do formulario
        styleButtonsChecks();

        // garante foco inicial
        root.setFocusTraversable(true);
        root.requestFocus();

        // Eventos do mouse no overlay
        eventMouseOverlay();
    }

    private void eventMouseOverlay() {

        // Eventos do mouse
        overlay.setOnMousePressed(e -> {
            System.out.println("Clicou na tela do ImageView");
            // coordenadas no overlay
            pressX = e.getX();
            pressY = e.getY();

            if (roiRect.isVisible() && inside(pressX, pressY, roiRect)) {
                // mover retângulo
                mode = Mode.MOVE;
                rectStartX = roiRect.getX();
                rectStartY = roiRect.getY();
            } else {
                // iniciar novo retângulo
                mode = Mode.DRAW;
                roiRect.setX(pressX);
                roiRect.setY(pressY);
                roiRect.setWidth(0);
                roiRect.setHeight(0);
                roiRect.setVisible(true);
            }
        });

        overlay.setOnMouseDragged(e -> {
            System.out.println("Moveu o mouse");
            if (mode == Mode.DRAW) {
                double x = Math.min(pressX, e.getX());
                double y = Math.min(pressY, e.getY());
                double w = Math.abs(e.getX() - pressX);
                double h = Math.abs(e.getY() - pressY);

                // limitar às bordas do overlay
                x = clamp(x, 0, overlay.getWidth());
                y = clamp(y, 0, overlay.getHeight());
                w = clamp(x + w, 0, overlay.getWidth()) - x;
                h = clamp(y + h, 0, overlay.getHeight()) - y;

                roiRect.setX(x);
                roiRect.setY(y);
                roiRect.setWidth(w);
                roiRect.setHeight(h);
            } else if (mode == Mode.MOVE) {
                double dx = e.getX() - pressX;
                double dy = e.getY() - pressY;

                double newX = clamp(rectStartX + dx, 0, overlay.getWidth() - roiRect.getWidth());
                double newY = clamp(rectStartY + dy, 0, overlay.getHeight() - roiRect.getHeight());

                roiRect.setX(newX);
                roiRect.setY(newY);
            }
        });

        overlay.setOnMouseReleased(e -> {
            mode = Mode.NONE;
            System.out.printf("Retirou do click com o ROI: x=%.2f, y=%.2f, w=%.2f, h=%.2f, área=%.2f%n",
                    roiRect.getX(), roiRect.getY(), roiRect.getWidth(), roiRect.getHeight(),
                    (roiRect.getWidth() * roiRect.getHeight()));
        });

        // manter overlay do tamanho do ImageView
        overlay.minWidthProperty().bind(cameraView.fitWidthProperty());
        overlay.minHeightProperty().bind(cameraView.fitHeightProperty());
        overlay.maxWidthProperty().bind(cameraView.fitWidthProperty());
        overlay.maxHeightProperty().bind(cameraView.fitHeightProperty());


    }


    private boolean inside(double x, double y, Rectangle r) {
        return x >= r.getX() && x <= r.getX() + r.getWidth()
                && y >= r.getY() && y <= r.getY() + r.getHeight();
    }

    private double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    // ------ Conversão ROI preview -> ROI no frame OpenCV (Mat) ------
    // Chame isso quando precisar aplicar a ROI no processamento real.
    public java.awt.Rectangle getRoiOnSource(int srcWidth, int srcHeight) {
        // Tamanho efetivo da imagem desenhada no ImageView, respeitando preserveRatio
        double ivFitW = cameraView.getFitWidth();
        double ivFitH = cameraView.getFitHeight();

        // fator de escala para "letterbox" (barras laterais ou superior/inferior)
        double scale = Math.min(ivFitW / srcWidth, ivFitH / srcHeight);
        double drawnW = srcWidth * scale;
        double drawnH = srcHeight * scale;

        // offsets (imagem centralizada dentro do ImageView)
        double xOffset = (ivFitW - drawnW) / 2.0;
        double yOffset = (ivFitH - drawnH) / 2.0;

        // coords do retângulo na área “desenhada” (tirando offsets)
        double xInDrawn = roiRect.getX() - xOffset;
        double yInDrawn = roiRect.getY() - yOffset;

        // converter para coordenadas do frame original
        double sx = xInDrawn / scale;
        double sy = yInDrawn / scale;
        double sw = roiRect.getWidth() / scale;
        double sh = roiRect.getHeight() / scale;

        // clamp para ficar dentro do frame
        int rx = (int) Math.max(0, Math.min(srcWidth - 1, Math.round(sx)));
        int ry = (int) Math.max(0, Math.min(srcHeight - 1, Math.round(sy)));
        int rw = (int) Math.max(1, Math.min(srcWidth - rx, Math.round(sw)));
        int rh = (int) Math.max(1, Math.min(srcHeight - ry, Math.round(sh)));

        return new java.awt.Rectangle(rx, ry, rw, rh);
    }

    @FXML
    public void onConectarCamera() throws InterruptedException {
        if (cameraCombo.getSelectionModel().isEmpty()){
            AlertaUtil.mostrarAviso("Dispositivo Camera Invalido","Necessário selecionar uma camera para conectar!");
            cameraCombo.setStyle("-fx-background-color: #fcdde0; -fx-text-fill: #8a1c24; -fx-font-weight: bold;");
            return;
        }
        cameraBtn.setDisable(true);
        if (cameraBtn.getText().equals("Ativar Camera")) {
            int index = CameraDetectorWindows.consultandoIndexDispositivoWindows(cameraCombo.getValue().toString());
            reconhecimento.startCameraReconhecimentoFacial(index, ClienteCSV.listarTodosClientesCSV());
            Thread.sleep(3000);
            exibirCameraReconhecimento(cameraView);
        } else {
            reconhecimento.stopCameraReconhecimentoFacial();
            Thread.sleep(3000);
            reconhecimento.stopExibirCameraImageView();
            cameraBtn.setText("Ativar Camera");
        }
        cameraBtn.setDisable(false);
        cameraCombo.setStyle("");
    }

    @FXML
    public void onConectarArduino(ActionEvent actionEvent) {
        Thread.ofVirtual().unstarted(() -> {
            Platform.runLater(() -> {
            Thread.yield();
            arduinoStatusLabel.setText("Iniciando testes com microcontrolador...");
            System.out.println("Entrou na thread para testar a conexao com microcontrolador...");

            String retornoESP32;
            try {
                retornoESP32 = httpClientService.enviarComandoESP32(arduinoIpField.getText().trim(), arduinoPortField.getText().trim(), "Teste");
                if (retornoESP32.contains("200")) {
                    arduinoStatusLabel.setText("ESP32 comunicado com sucesso!");
                } else {
                    arduinoStatusLabel.setText("Falha ao comunicar ESP32!");
                }
            } catch (Exception e) {
                arduinoStatusLabel.setText("Falha ao comunicar ESP32!");
            }
            });
        }).start();
    }


    public void exibirCameraReconhecimento(ImageView cameraView){
        reconhecimento.startExibirCameraImageView(cameraView);
        cameraBtn.setText("Desativar Camera");
    }

    // Pode ser usado para a IA reconhecer a face apenas de uma aréa recotada
    private void recortaFramecapturado(BufferedImage bimg) {
        String dataAtual = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssms"));
        if (bimg != null) {
            imwrite("captures/capture_" + cameraNameField.getText() + "_" + dataAtual + "_face.png", reconhecimento.getFaceCapturada());
            System.out.println("Gerando imagem da face : " + cameraNameField.getText());
        } else {
            System.out.println("## IMAGEM DA CAMERA NÃO ENCONTRADA ###");
        }
    }

    // [ UTILIZAR FUTURAMENTE PARA EXIBIR A FOTO COMPLETA COM RETAGANGULO DO ROI  ]
    public BufferedImage desenharRetanguloROIImagemOriginal(java.awt.Rectangle roi) {
        BufferedImage frameCapturado = reconhecimento.getFrameCapturado();
        if (frameCapturado != null) {
            if (areaMinimaRetanguloCamera()) {
                BufferedImage copy = new BufferedImage(
                        frameCapturado.getWidth(), frameCapturado.getHeight(), frameCapturado.getType()
                ); // Gerar uma copia de imagem em branco com o mesmo tamanho que o original
                Graphics2D g = copy.createGraphics(); // Objeto para desenhar na imagem
                g.drawImage(frameCapturado, 0, 0, null); // desenha a imagem original na copia
                g.setColor(Color.BLUE); // Define cor linha para o retângulo retangulo
                g.setStroke(new BasicStroke(3)); // Definir a espessura da linha do retangulo
                g.drawRect(roi.x, roi.y, roi.width, roi.height); // desenha o retângulo
                g.dispose(); // Libera os recursos do Graphics2D (Para n ocupar memoria)
                return copy; // Retorna a imagem resultante com o retângulo desenhado
            } else {
                System.out.println("## ARÉA DE RETANGulo SELECIONADA MUITO PEQUENA ###");
            }
        } else {
            System.out.println("## IMAGEM DA CAMERA NÃO ENCONTRADA ###");
        }
        return null;
    }

    private boolean areaMinimaRetanguloCamera() {
        return (roiRect.getWidth() * roiRect.getHeight()) > 20;
    }


// SEGUE ABAIXO UM EXEMPLO DE MANIPULAÇÃO DE DADOS APÓS SALVAR O FORMULARIO

    public void styleButtonsChecks(){
        canceleBtn.setOnMouseEntered(e ->
                canceleBtn.setStyle("-fx-background-color: #fcdde0; -fx-text-fill: #8a1c24; -fx-font-weight: bold;")
        );
        canceleBtn.setOnMouseExited(e ->
                canceleBtn.setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #8a1c24; -fx-font-weight: bold;")
        );
        deleteBtn.setOnMouseEntered(e ->
                canceleBtn.setStyle("-fx-background-color: #fcdde0; -fx-text-fill: #8a1c24; -fx-font-weight: bold;")
        );
        deleteBtn.setOnMouseExited(e ->
                canceleBtn.setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #8a1c24; -fx-font-weight: bold;")
        );
    }

    //Alterando os botoes do formulario para salvar um novo
    public void formSalvarNovoDispositivo() {
        deleteBtn.setVisible(false);
        deleteBtn.setCancelButton(false);
    }

    //Alterando os botoes do formulario para alterar ou apagar o dispositivo
    public void formEditarDispositivo(DispositivoDTO dispositivoDTO) {
        cameraCombo.setValue(dispositivoDTO.getCameraCombo());
        arduinoIpField.setText(dispositivoDTO.getIpArduino());
        arduinoPortField.setText(dispositivoDTO.getPortaArduino());
        cameraNameField.setText(dispositivoDTO.getnomeDispositivo());
        reconhecimento = dispositivoDTO.getReconhecimento();
        deleteBtn.setVisible(true);
        // Verificar se a camera de reconhecimentoe esta ligada
        if(reconhecimento.getThreadStartCameraReconhecimentoFacial().isAlive()) exibirCameraReconhecimento(cameraView);
    }

    public void carregarCamerasWindows() {
        System.out.println("Carregando a lista de dispositivos cameras no Windows");
        List<String> listDispositivosNomes = CameraDetectorWindows.consultarDispositivosWindows().stream()
                .map(s -> s.split(";")[1])   // pega o campo de índice 1
                .collect(Collectors.toList());
        cameraCombo.getItems().addAll(listDispositivosNomes);
    }


    private boolean validarCampos() {
        // Lista para marcar quais campos estão inválidos (pode ser usada futuramente)
        List<TextField> camposInvalidos = new ArrayList<>();

        if (arduinoIpField.getText() == null || arduinoIpField.getText().trim().isEmpty())
            camposInvalidos.add(arduinoIpField);

        if (arduinoPortField.getText() == null || arduinoPortField.getText().trim().isEmpty())
            camposInvalidos.add(arduinoPortField);

        if (cameraNameField.getText() == null || cameraNameField.getText().trim().isEmpty())
            camposInvalidos.add(cameraNameField);

        // Exemplo: marcar campos inválidos em vermelho
        if (!camposInvalidos.isEmpty()||cameraCombo.getSelectionModel().isEmpty()) {
            validarFormSalvar.setVisible(true);
            for (TextField campo : camposInvalidos) {
                campo.setStyle("-fx-border-color: red;");
                validarFormSalvar.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                validarFormSalvar.setText("Preencher todos os campos obrigatorios...");
            }
            if(cameraCombo.getSelectionModel().isEmpty()){
                cameraCombo.setStyle("-fx-border-color: red;");
            }
            return false;
        }


        return true;
    }

@FXML
    private void onSalvar(ActionEvent event) {
        if (validarCampos()) { // Validar todos os campos
            // aqui onde salvara todas as informações preenchidas no formulario
            System.out.println("Salvando o dispositivo com o nome  : " + cameraNameField.getText());
            reconhecimento.setDadosArduinoURL(arduinoIpField.getText(),arduinoPortField.getText());
            // Séra retornato as informacoes do dispositivo criado no result
            result = Optional.ofNullable(new DispositivoDTO(cameraNameField.getText(),arduinoIpField.getText(),arduinoPortField.getText(),cameraCombo.getValue().toString(),reconhecimento));
            fecharJanela(event);
        }
    }

@FXML
    private void onCancelar(ActionEvent event) {
        result = Optional.empty(); // nada salvo
        fecharJanela(event);
    }

    @FXML
    private void onDeletar(ActionEvent event) {
    }



    // Fecha a janela atual
    private void fecharJanela(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    // Para a tela chamadora pegar o resultado
    public Optional<DispositivoDTO> getResult() {
        System.out.println("### getResult foi chamado");
        return result;
    }



}

