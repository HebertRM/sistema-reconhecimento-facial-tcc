package com.example.faceclient.controller;

import com.example.faceclient.Alert.AlertaUtil;
import jakarta.mail.*;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.Properties;

public class SuporteController {

    @FXML
    private TextField txtAssunto;

    @FXML
    private TextArea txtMensagem;

    private final String EMAIL_DESTINO = "hebertehrm@gmail.com";

    // Coloca dados SMTP do Gmail aqui
    private final String SMTP_EMAIL = "SMTP_PROTOTIPO@gmail.com";
    private final String SMTP_SENHA = "SENHA_DE_PROTOTIPO";

    @FXML
    private void onEnviarEmail() {

        String assunto = txtAssunto.getText();
        String mensagem = txtMensagem.getText();

        if (assunto.isBlank() || mensagem.isBlank()) {
            AlertaUtil.mostrarAlerta("Erro", "Preencha o assunto e a mensagem.");
            return;
        }

        try {
            enviarEmailSMTP(assunto, mensagem);
            AlertaUtil.mostrarAlerta("Sucesso", "Mensagem enviada com sucesso!");

            txtAssunto.clear();
            txtMensagem.clear();

        } catch (Exception e) {
            e.printStackTrace();
            AlertaUtil.mostrarAlerta("Erro", "Falha ao enviar email: " + e.getMessage());
        }
    }

    private void enviarEmailSMTP(String assunto, String texto) throws Exception {

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session sessao = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_EMAIL, SMTP_SENHA);
            }
        });

        Message message = new MimeMessage(sessao);
        message.setFrom(new InternetAddress(SMTP_EMAIL));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(EMAIL_DESTINO));
        message.setSubject(assunto);
        message.setText(texto);

        Transport.send(message);
    }


}
