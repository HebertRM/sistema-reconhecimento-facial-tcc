package com.example.faceclient.model;

import java.util.List;
import java.util.Objects;

public class ClienteDTO {

    private int id;
    private String  txtNome, txtCpf, txtTelefone, txtEmail, txtDataNascimento, txtEndereco;
    private boolean status;

    public ClienteDTO() {};

    public ClienteDTO(int id, String txtNome, String txtCpf, String txtTelefone, String txtEmail, String dataNascimentoFormatada, String txtEndereco, boolean status) {
        this.id = id;
        this.txtNome = txtNome;
        this.txtCpf = txtCpf;
        this.txtTelefone = txtTelefone;
        this.txtEmail = txtEmail;
        this.txtDataNascimento = dataNascimentoFormatada;
        this.txtEndereco = txtEndereco;
        this.status = status;
    }

    public ClienteDTO(String id, String txtNome, String txtCpf, String txtTelefone, String txtEmail, String dataNascimentoFormatada, String txtEndereco, String status) {
        this.id = Integer.parseInt(id) ;
        this.txtNome = txtNome;
        this.txtCpf = txtCpf;
        this.txtTelefone = txtTelefone;
        this.txtEmail = txtEmail;
        this.txtDataNascimento = dataNascimentoFormatada;
        this.txtEndereco = txtEndereco;
        System.out.println("Verificando retorno do status no construtor, onde esta recebendo o valor do CSV - status : "+status);
        this.status = (status.equals("1")) ? true : false;
    }

    public ClienteDTO(String id, String txtNome, String txtCpf, String status) {
        this.id = Integer.parseInt(id) ;
        this.txtNome = txtNome;
        this.txtCpf = txtCpf;
        this.status = (status.equals("1")) ? true : false;
    }

    public static ClienteDTO getClientePorId(List<ClienteDTO> clientes, int id) {
        return clientes.stream()
                .filter(c -> c.getId() == id)
                .findFirst()
                .orElse(null);
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTxtNome() {
        return txtNome;
    }

    public void setTxtNome(String txtNome) {
        this.txtNome = txtNome;
    }

    public String getTxtCpf() {
        return txtCpf;
    }

    public void setTxtCpf(String txtCpf) {
        this.txtCpf = txtCpf;
    }

    public String getTxtTelefone() {
        return txtTelefone;
    }

    public void setTxtTelefone(String txtTelefone) {
        this.txtTelefone = txtTelefone;
    }

    public String getTxtEmail() {
        return txtEmail;
    }

    public void setTxtEmail(String txtEmail) {
        this.txtEmail = txtEmail;
    }

    public String getTxtEndereco() {
        return txtEndereco;
    }

    public void setTxtEndereco(String txtEndereco) {
        this.txtEndereco = txtEndereco;
    }

    public String getTxtDataNascimento() {
        return txtDataNascimento;
    }

    public void setTxtDataNascimento(String txtDataNascimento) {
        this.txtDataNascimento = txtDataNascimento;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return  id + ";"
                + txtNome + ";"
                + txtCpf + ";"
                + txtTelefone + ";"
                + txtEmail + ";"
                + txtDataNascimento + ";"
                + txtEndereco + ";"
                + (status ? "1" : "0");
    }
}
