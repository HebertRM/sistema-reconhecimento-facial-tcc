package com.example.faceclient.controller;


import com.example.faceclient.model.ClienteDTO;
import com.example.faceclient.repository.ClienteCSV;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ClientesController {

    @FXML private TextField txtPesquisa;
    @FXML private VBox listaClientes;

    private List<ClienteDTO> clientes = new ArrayList<>();
    Stage stageFormCadastros;

    @FXML
    public void initialize() {
        carregarClientes();
        atualizarLista(clientes);
    }

    @FXML
    public void atualizarListaCarregarClientes(ActionEvent actionEvent) {
        carregarClientes();
        atualizarLista(clientes);
    }

    @FXML
    private void buscarClientes() {
        String termo = txtPesquisa.getText().toLowerCase().trim();
        List<ClienteDTO> filtrados;
        if (termo.matches("\\d+")){
            filtrados = clientes.stream()
                    .filter(c -> c.getTxtCpf().toLowerCase().contains(termo))
                    .collect(Collectors.toList());
        }else{
            filtrados = clientes.stream()
                    .filter(c -> c.getTxtNome().toLowerCase().contains(termo))
                    .collect(Collectors.toList());
        }
        atualizarLista(filtrados);
    }

    private void atualizarLista(List<ClienteDTO> lista) {
        listaClientes.getChildren().clear();

        for (ClienteDTO c : lista) {
            Button btn = new Button("ID: " + c.getId() + " | Nome: " + c.getTxtNome() + " | CPF: " + c.getTxtCpf());
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setWrapText(true);
            btn.setStyle("-fx-alignment: TOP_LEFT; -fx-font-size: 13px;");
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setOnAction(e -> abrirDetalhesCliente(e,c));
            listaClientes.getChildren().add(btn);
        }
    }

    private void carregarClientes() {
        System.out.println("Carregando a lista de todos os clientes do CSV");
        clientes = ClienteCSV.listarTodosClientesCSV();
    }

    private void abrirDetalhesCliente(ActionEvent event,ClienteDTO cliente) {
        try{
        if (stageFormCadastros != null && stageFormCadastros.isShowing()) {
            stageFormCadastros.toFront();   // traz para frente
            stageFormCadastros.requestFocus(); // foca na janela
            return;
        }

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/faceclient/principal/cadastros-view.fxml"));
        Parent root = fxmlLoader.load();

            CadastrosController cadastrosController = fxmlLoader.getController(); //Exemplo usar o controller do FXML
            cadastrosController.exibirFormularioDetalhesCliente(ClienteCSV.buscarPorId(cliente.getId())); // Possibidade de desabilitar os campos antes de abrir formulario.

            stageFormCadastros = new Stage();
            stageFormCadastros.setTitle("ID: "+cliente.getId()+" Cliente: " + cliente.getTxtNome());
            stageFormCadastros.setScene(new Scene(root));
            stageFormCadastros.initOwner(((Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow()));                      // define a janela dona
            stageFormCadastros.setResizable(true);                              // Controla o tamanho da janela(opcional)
            stageFormCadastros.show();  // ou showAndWait (bloqueia as outras até o usuário fechar essa stage)

            stageFormCadastros.setOnHidden(evt -> {
                carregarClientes();
                atualizarLista(clientes);
            });
        } catch (Exception e) {
            e.printStackTrace();
            // opcional: mostrar um Alert de erro
        }

    }


}