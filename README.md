MANUAL DE INSTALAÇÃO DO AMBIENTE – COMO RODAR O SISTEMA (PASSO A PASSO SIMPLES)


Siga os passos na ordem e tudo vai funcionar.

1. Instalar o Java (JDK Zulu 21)

O Java é obrigatório para rodar o projeto.

Baixe aqui:
👉 https://www.azul.com/downloads/?package=jdk#download-openjdk

- Escolha Java 21 (LTS)
- Escolha Windows – 64 bits
- Baixe o instalador MSI (Clique Next → Next → Install)

Quando terminar, o Java já estará configurado.

2. Instalar o IntelliJ IDEA Community (IDE)

É o programa que abre e roda o projeto.

Baixe aqui:
👉 https://www.jetbrains.com/idea/download/

Baixe a versão Community
Instale normalmente

Abra o IntelliJ no final

3. Baixar o projeto (maneira mais fácil – sem Git Bash)

Você pode baixar direto pelo IntelliJ:

Abra o IntelliJ → Tela inicial → clique em Get from VCS

(⭐ Caso o IntelliJ não esteja na tela inicial:
Vá em File → New → Project from Version Control)

No campo do link, cole:

https://github.com/HebertRM/sistema-reconhecimento-facial-tcc.git

Escolha a pasta onde salvar

Clique Clone

O IntelliJ vai baixar tudo automaticamente.


4. Aguardar o IntelliJ baixar as dependências

O projeto usa Maven, então o IntelliJ vai baixar tudo automaticamente:

JavaFX 21 / JavaCV 1.5.12 / OpenCV / jakarta

Você só precisa esperar alguns minutos até o canto inferior direito parar de mostrar “Loading…” ou “Indexing”.

5. Configurar o Java no IntelliJ (caso apareça erro)

Se o IntelliJ pedir o Java:

Vá em File → Project Structure

Clique em "Project"

Em SDK, selecione Java 21 (Zulu)

Clique em OK

Pronto.

6. Rodar o sistema

Tudo pronto!

Para rodar:

No IntelliJ, abra a classe principal 
(src/main/java/com/example/faceclient/AppPlay.java)

Clique no botão verde ▶ RUN no canto superior direito

ou

Clique com botão direito na classe principal

Escolha Run

O sistema vai abrir a janela JavaFX normalmente.

7. Dicas importantes sobre a câmera

Antes de testar:

- Feche o OBS, Teams, Zoom ou qualquer aplicativo que esteja usando a câmera

- Abra o app Câmera do Windows para confirmar que ela funciona

- Se houver mais de uma câmera conectada, o sistema mostra a lista.

8. Caso prefira baixar o projeto sem o IntelliJ

Método opcional, só se quiser usar Git Bash (não é necessário):

Baixe e instale o Git: https://git-scm.com/download/win

Abra o Git Bash

Rode:

git clone https://github.com/HebertRM/sistema-reconhecimento-facial-tcc.git


Mas repito: o método mais fácil é pelo IntelliJ diretamente, sem Git.