package com.example.faceclient.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HttpClientService {

    private final HttpClient client;

    public HttpClientService() {
        // HttpClient é thread-safe e pode ser reutilizado
        this.client = HttpClient.newHttpClient();
    }

    /**
     * Envia uma requisição GET para: http://ip:porta/client?<paramName>=<paramValue>
     */
    public String enviarComandoESP32(String ip, String porta, String paramNameValue) throws Exception {

        // Monta a URL
        String url = String.format(
                "http://%s:%s/acao?nome=%s",
                ip,
                porta,
                paramNameValue
        );

        // Cria a requisição
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        // Envia e espera resposta
        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        String dataAtual = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd-HH:mm:ss"));
        // Valida status
        if (response.statusCode() != 200) {
            System.out.println("Falha ao comunicar com ESP32. Status = " + response.statusCode());
            return "Cliente "+paramNameValue+" não foi liberado. Falha ao comunicar com ESP32. Status = " +
                    response.statusCode()+" | Data=" + dataAtual;
        }

        // Retorno do ESP32 (texto simples normalmente)
        return "Acesso liberado ao cliente "+paramNameValue+", segue o retorno ESP32. Status = "+
                response.statusCode()+" | Data=" + dataAtual;
    }
}