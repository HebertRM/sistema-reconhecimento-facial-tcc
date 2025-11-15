package com.example.faceclient.controller;


import com.example.faceclient.Alert.AlertaUtil;
import com.example.faceclient.model.ClienteDTO;
import com.example.faceclient.repository.ClienteCSV;
import com.example.faceclient.service.Treinamento;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLOutput;
import java.time.LocalDate;


public class CadastrosController {

    @FXML
    public Button btnDeletar, btnLimpar;
    @FXML
    private TextField txtNome, txtCpf, txtTelefone, txtEmail, txtEndereco;
    @FXML
    private DatePicker dateNascimento;
    @FXML
    private ComboBox<String> comboStatus;
    @FXML
    private HBox boxFotos;

    private int id, qtdFotosCliente;

    private boolean cliente_novo = true;

    ClienteDTO clienteDTO;

    Stage stageJanelaCamera;

    private static final String CAMINHO_FOTOS = "captures";
    private static final String CAMINHO_FOTOS_TMP = "captures/tmp";




    @FXML
    private void initialize() {
        ClienteCSV.criarOuVerificarCSV();
        carregarFotos();
    }

    @FXML
    public void onSalvar(ActionEvent actionEvent) {
        System.out.println("Salvando/Atualizando o cliente: " + txtNome.getText());
        ClienteDTO cliente = new ClienteDTO();
        cliente.setTxtNome(txtNome.getText());
        cliente.setTxtCpf(txtCpf.getText());
        cliente.setTxtTelefone(txtTelefone.getText());
        cliente.setTxtEmail(txtEmail.getText());
        cliente.setTxtDataNascimento(dateNascimento.getValue().toString());
        cliente.setTxtEndereco(txtEndereco.getText());
        cliente.setStatus(comboStatus.getValue().equals("Ativo"));
        if (cliente_novo) {
            cliente.setId(ClienteCSV.obterUltimoId()+1);
            if (ClienteCSV.incluirNovoCliente(cliente)) {
                AlertaUtil.mostrarAviso("Cliente Cadastro", "O cliente " + txtNome.getText() + " foi cadastrado com sucesso!");
                encaminharTodasFotosTmp();
                onLimpar();
                carregarFotos();
            }
        }else {
            if(AlertaUtil.mostrarConfirmar("Atualizar Cliente", "Tem certeza que deseja alterar as informações do cliente " + clienteDTO.getTxtNome())) {
                cliente.setId(clienteDTO.getId());
                if (ClienteCSV.atualizarCliente(cliente)) {
                    AlertaUtil.mostrarAviso("Cliente Atualizado", "O cliente " + txtNome.getText() + " foi atualizado com sucesso!");
                    Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
                    stage.close();
                }
            }
        }
        Treinamento.startTreinamentoAlgotirmos().start();
    }

    @FXML
    private void onLimpar() {
        txtNome.clear();
        txtCpf.clear();
        txtTelefone.clear();
        txtEmail.clear();
        txtEndereco.clear();
        dateNascimento.setValue(null);
        comboStatus.setValue(null);
        if (cliente_novo) {
            deletarTodasFotos();
            carregarFotos();
        }
    }

    @FXML
    public void onLigarCamera(ActionEvent actionEvent) {
        try{
            if (stageJanelaCamera != null && stageJanelaCamera.isShowing()) {
                stageJanelaCamera.toFront();   // traz para frente
                stageJanelaCamera.requestFocus(); // foca na janela
                return;
            }

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/faceclient/cam-view.fxml"));
            Parent root = fxmlLoader.load();

            CamController camController = fxmlLoader.getController(); //Exemplo usar o controller do FXML

            int idCliente = cliente_novo ? (ClienteCSV.obterUltimoId() + 1) : clienteDTO.getId();
            camController.iniciarCapturaFacial(idCliente,cliente_novo,qtdFotosCliente); // Possibidade de desabilitar os campos antes de abrir formulario.

            stageJanelaCamera = new Stage();
            stageJanelaCamera.setTitle("Camera Captura");
            stageJanelaCamera.setScene(new Scene(root));
            stageJanelaCamera.initOwner(((Stage) ((javafx.scene.Node) actionEvent.getSource()).getScene().getWindow()));                      // define a janela dona
            stageJanelaCamera.setResizable(true);                              // Controla o tamanho da janela(opcional)
            stageJanelaCamera.show();  // ou showAndWait (bloqueia as outras até o usuário fechar essa stage)
            System.out.println("Exibindo janela da camera.");
            stageJanelaCamera.setOnHidden(evt -> {
                carregarFotos();
                camController.encerrarCapturaFacial();
            });
        } catch (Exception e) {
            AlertaUtil.mostrarErro("Falha ao abrir tela de captura", "Houve uma falha ao abrir a tela da camera, necessário solicitar apoio ao suporte.");
            e.printStackTrace();
        }
    }

    @FXML
    public void onDeletar(ActionEvent actionEvent) {
        if (AlertaUtil.mostrarConfirmar("Remover Cliente", "Tem certeza que deseja excluir o cliente " + clienteDTO.getTxtNome())) {
            if (ClienteCSV.removerCliente(clienteDTO.getId())&&deletarTodasFotos())
                AlertaUtil.mostrarAviso("Cliente Deletado", "Cliente com o ID " + clienteDTO.getId() + " foi removido com sucesso!");
            onLimpar();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.close();
        }
    }

    public  void exibirFormularioDetalhesCliente(ClienteDTO cliente){
        clienteDTO = cliente;
        cliente_novo = false;

        if (!cliente.getTxtDataNascimento().isEmpty()) {
            LocalDate data = LocalDate.parse(cliente.getTxtDataNascimento());
            dateNascimento.setValue(data);
        }
        String status = cliente.isStatus() ? "Ativo" : "Inativo";
        System.out.println("Verificando o status : "+status+ " boolean : "+cliente.isStatus());
        txtNome.setText(cliente.getTxtNome());
        txtCpf.setText(cliente.getTxtCpf());
        txtTelefone.setText(cliente.getTxtTelefone());
        txtEmail.setText(cliente.getTxtEmail());
        txtEndereco.setText(cliente.getTxtEndereco());
        comboStatus.setValue(status);
        // habilitando/exibindo o botao delete ao alterar cliente
        btnDeletar.setDisable(false);
        btnDeletar.setVisible(true);
        // Desabilitando/Ocultando o botao de limpar ao alterar cliente
        btnLimpar.setVisible(false);
        btnLimpar.setDisable(true);
        //carregar as fotos
        carregarFotos();
    }

    private void carregarFotos() {
        System.out.println("Carregando a lista de fotos.");
        String caminho = cliente_novo ? CAMINHO_FOTOS_TMP : CAMINHO_FOTOS;

        File pasta = new File(caminho);

        if (!pasta.exists()) {
            System.out.println("Não foi encontrado o diretorio com as fotos.");
            AlertaUtil.mostrarErro("Falha ao carregar fotos","Não foi encontrado o diretorio para armazenamento das fotos, por gentileza, solicitar analise ao suporte");
            return;
        }

        String nomeFotos = cliente_novo ? ".png" : "_" + clienteDTO.getId() + ".png";

        File[] arquivos = pasta.listFiles((dir, nome) ->
                nome.toLowerCase().endsWith(nomeFotos)
        );

        if (arquivos == null) return;

        qtdFotosCliente = arquivos.length;
        System.out.println("Foram encontrados " + qtdFotosCliente + " para o cliente " + (cliente_novo ? "novo" : clienteDTO.getTxtNome()));

        boxFotos.getChildren().clear();

        for (File imgFile : arquivos) {
            ImageView img = new ImageView(imgFile.toURI().toString());
            img.setFitHeight(120);
            img.setPreserveRatio(true);

            //botao deletar (lixeira)
            Button btnDel = new Button("Excluir");
            btnDel.setStyle("-fx-background-color: #1E3A5F; -fx-text-fill: white; -fx-font-size: 11px;");
            btnDel.setOnAction(e -> deletarFoto(imgFile));

            // Layout da foto + botão
            VBox box = new VBox(5);
            box.getChildren().addAll(img, btnDel);

            boxFotos.getChildren().add(box);
        }
    }

    private void encaminharTodasFotosTmp() {
        System.out.println("Iniciando encaminhamento das fotos do diretorio /tmp");
        File pastaOrigem = new File(CAMINHO_FOTOS_TMP);
        File pastaDestino = new File(CAMINHO_FOTOS);

// cria a pasta destino se não existir
        if (!pastaDestino.exists()) {
            pastaDestino.mkdirs();
        }

// filtro igual ao seu
        File[] arquivos = pastaOrigem.listFiles((dir, nome) ->
                nome.toLowerCase().endsWith(".png")
        );

        if (arquivos != null) {
            for (File arquivo : arquivos) {
                File novoLocal = new File(pastaDestino, arquivo.getName());
                try {
                    // copiar
                    Files.copy(
                            arquivo.toPath(),
                            novoLocal.toPath(),
                            StandardCopyOption.REPLACE_EXISTING
                    );
                    // excluir o original
                    boolean deletado = arquivo.delete();
                    if (!deletado) {
                        System.out.println("Não foi possível excluir: " + arquivo.getName());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // MENSAGEM DE FALHA AO ENCAMINHAR FOTOS DO CLIENTE
                }
            }
        }

    }

    private boolean deletarTodasFotos() {
        String caminho = cliente_novo ? CAMINHO_FOTOS_TMP : CAMINHO_FOTOS;
        File pasta = new File(caminho);

        String nomeFotos = cliente_novo ? ".png" : "_" + clienteDTO.getId() + ".png";
        File[] arquivos = pasta.listFiles((dir, nome) ->
                nome.toLowerCase().endsWith(nomeFotos)
        );

        if (arquivos == null) return true;

        boxFotos.getChildren().clear();

        for (File imgFile : arquivos) {
            if (!imgFile.delete()) {
                AlertaUtil.mostrarErro("Falha ao remover foto", "Houve uma falha ao excluir a foto " + imgFile.getName() + ", necessário solicitar apoio ao suporte.");
                return false;
            }
        }
        Treinamento.startTreinamentoAlgotirmos().start();
        return true;
    }

    private void deletarFoto(File f) {
        boolean confirm = AlertaUtil.mostrarConfirmar("Remover Fotos", "Deseja realmente excluir a foto do cliente" + (cliente_novo?".":" "+clienteDTO.getTxtNome()));
        if (confirm) {
            if (f.delete()) {
                carregarFotos(); // atualiza carrossel
            } else {
                AlertaUtil.mostrarErro("Falha ao remover foto", "Houve uma falha ao excluir a foto "+f.getName()+", necessário solicitar apoio ao suporte.");
            }
        }
    }



}