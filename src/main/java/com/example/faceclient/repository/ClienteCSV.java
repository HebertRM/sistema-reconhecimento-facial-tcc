package com.example.faceclient.repository;

import com.example.faceclient.Alert.AlertaUtil;
import com.example.faceclient.model.ClienteDTO;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class ClienteCSV {

    private static final String ARQUIVO = "data/clientes_v1.csv";
    private static final String CABECALHO = "ID;NOME;CPF;TEL;EMAIL;DATA;ENDERECO;STATUS";




    /**
     * #1 - Cria o arquivo CSV se não existir
     */
    public static void criarOuVerificarCSV() {
        try {
            File file = new File(ARQUIVO);
            if (!file.exists()) {
                try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(ARQUIVO), StandardCharsets.UTF_8)) {
                    writer.write(CABECALHO);
                    writer.newLine();
                    System.out.println("Arquivo CSV criado: " + ARQUIVO);
                }
            } else {
                System.out.println("Arquivo CSV já existe: " + ARQUIVO);
            }
        } catch (IOException e) {
            e.printStackTrace();
            AlertaUtil.mostrarAviso("Falha na confirmação do arquivo "+ARQUIVO, "Houve uma falha ao consulta o arquivo CSV responsável pelo armazenamento de clientes.");
        }
    }

    /**
     * #2 - Busca um cliente pelo ID e retorna as colunas
     */
    public static ClienteDTO buscarPorId(int id) {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(ARQUIVO), StandardCharsets.UTF_8)) {
            String linha;
            reader.readLine(); // pula cabeçalho
            while ((linha = reader.readLine()) != null) {
                String[] partes = linha.split(";", -1);
                if (partes.length >= 8) {
                    int idLido = Integer.parseInt(partes[0].trim());
                    if (idLido == id) {
                        System.out.println("Cliente com ID "+id+", encontrado.");
                        return new ClienteDTO(partes[0],partes[1],partes[2],partes[3],partes[4],partes[5],partes[6],partes[7]);
                    }
                }
            }
        } catch (IOException e) {
            AlertaUtil.mostrarAviso("Falha ao consultar o cliente no CSV : "+ARQUIVO, "Houve uma falha ao consulta o ID "+id+", necessário solicitar apoio ao suporte.");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * #3 - Inclui um novo registro
     */
    public static Boolean incluirNovoCliente(ClienteDTO clienteDTO) {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(ARQUIVO),
                StandardCharsets.UTF_8, StandardOpenOption.APPEND)) {
            writer.write(clienteDTO.toString());
            writer.newLine();
            System.out.println("Cliente adicionado: " + clienteDTO.getTxtNome());
        } catch (IOException e) {
            AlertaUtil.mostrarErro("Falha ao incluir o cliente no CSV : "+ARQUIVO, "Houve uma falha ao incluir o cliente "+clienteDTO.getTxtNome()
                    +", necessário solicitar apoio ao suporte.");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Atualiza um cliente existente no CSV com base no ID informado.
     * Pode alterar qualquer coluna sem ser o ID
     * @return true se o cliente foi atualizado, false caso não encontrado
     */
    public static boolean atualizarCliente(ClienteDTO clienteDTO) {
        List<String> linhas = new ArrayList<>();
        boolean atualizado = false;

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(ARQUIVO), StandardCharsets.UTF_8)) {
            String linha = reader.readLine(); // lê cabeçalho
            if (linha != null) {
                linhas.add(linha); // mantém o cabeçalho
            }

            while ((linha = reader.readLine()) != null) {
                String[] partes = linha.split(";", -1);
                if (partes.length >= CABECALHO.split(";").length) {
                    int idLido = Integer.parseInt(partes[0].trim());
                    if (idLido == clienteDTO.getId()) {
                        partes[1] = clienteDTO.getTxtNome();
                        partes[2] = clienteDTO.getTxtCpf();
                        partes[3] = clienteDTO.getTxtTelefone();
                        partes[4] = clienteDTO.getTxtEmail();
                        partes[5] = clienteDTO.getTxtDataNascimento();
                        partes[6] = clienteDTO.getTxtEndereco();
                        partes[7] = clienteDTO.isStatus() ? "1" : "0";
                        linha = String.join(";", partes);
                        atualizado = true;
                    }
                }
                linhas.add(linha);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        // Regrava o arquivo completo
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(ARQUIVO),
                StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (String l : linhas) {
                writer.write(l);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (atualizado) {
            System.out.println("Cliente ID " + clienteDTO.getId() + " atualizado com sucesso!");
        } else {
            System.out.println("Cliente ID " + clienteDTO.getId() + " não encontrado.");
        }

        return atualizado;
    }

    /**
     * Remove um cliente do CSV com base no ID informado.
     *
     * @param idRemover ID do cliente que deve ser removido
     * @return true se o cliente foi removido, false se não encontrado
     */
    public static boolean removerCliente(int idRemover) {
        List<String> linhas = new ArrayList<>();
        boolean removido = false;

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(ARQUIVO), StandardCharsets.UTF_8)) {
            String linha = reader.readLine(); // lê cabeçalho
            if (linha != null) {
                linhas.add(linha); // mantém o cabeçalho
            }

            while ((linha = reader.readLine()) != null) {
                String[] partes = linha.split(";", -1); // use ";" se for o separador do seu CSV
                if (partes.length >= CABECALHO.split(";").length) {
                    int idLido = Integer.parseInt(partes[0].trim());
                    if (idLido == idRemover) {
                        removido = true;
                        continue; // pula a linha (não adiciona à lista)
                    }
                }
                linhas.add(linha);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (!removido) {
            System.out.println("Cliente ID " + idRemover + " não encontrado.");
            return false;
        }

        // Regrava o arquivo sem a linha removida
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(ARQUIVO),
                StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (String l : linhas) {
                writer.write(l);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        System.out.println("Cliente ID " + idRemover + " removido com sucesso!");
        return true;
    }



    /**
     * #4 - Retorna o último ID existente no CSV
     */
    public static int obterUltimoId() {
        int ultimoId = 0;
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(ARQUIVO), StandardCharsets.UTF_8)) {
            String linha;
            reader.readLine(); // pula cabeçalho
            while ((linha = reader.readLine()) != null) {
                String[] partes = linha.split(";", -1);
                if (partes.length > 0 && !partes[0].trim().isEmpty()) {
                    try {
                        int idLido = Integer.parseInt(partes[0].trim());
                        if (idLido > ultimoId) {
                            ultimoId = idLido;
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ultimoId;
    }


    public static List<ClienteDTO> listarTodosClientesCSV() {
        List<ClienteDTO> clientes = new ArrayList<>(10000);

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(ARQUIVO), StandardCharsets.UTF_8)) {
            String linha;
            reader.readLine(); // pula o cabeçalho (ID;CPF;NOME)

            while ((linha = reader.readLine()) != null) {
                String[] partes = linha.split(";", -1); // use ";" se o CSV usa ponto e vírgula
                if (partes.length >= 3) {
                    try {
                        System.out.println("Encontrado cliente com ID : "+ partes[0]);
                        clientes.add(new ClienteDTO(partes[0],partes[1],partes[2],partes[7]));
                    } catch (NumberFormatException e) {
                        System.err.println("Erro ao converter ID em número: " + partes[0]);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            AlertaUtil.mostrarErro("Falha ao carregar os clientes do CSV : "+ARQUIVO, "Houve uma falha ao carregar a lista de todos os clientes"
                    +", necessário solicitar apoio ao suporte.");
            return null;
        }

        return clientes;
    }

    /**
     * Exemplo de uso
     */
    public static void main(String[] args) {
        criarOuVerificarCSV();

        // Adicionar novo cliente com ID automático
        //int novoId = obterUltimoId() + 1;
        //incluirNovoCliente(novoId, "54245327453754", "Ronaldinho Novo");
        // Buscar cliente por ID
       // System.out.println(buscarPorId(novoId));
        //atualizarCliente(2,"40000000","Weber Rodrigues Moreira");
        //removerCliente(2);
    }
}
