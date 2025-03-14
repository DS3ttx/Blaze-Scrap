# 🔥 BlazeScrap  

BlazeScrap é um projeto desenvolvido para analisar usuários de um modelo de aposta online.  
A ferramenta, construída em **Java**, é um **WebScraper** que utiliza **Selenium** e um **observer JavaScript** para monitorar as entradas que são rapidamente substituídas na tela, assim como os resultados de cada rodada.  

Os dados coletados são armazenados em um banco de dados **SQLite** para análise posterior.  

---

## 📌 Índice  

1. [📖 Sobre](#-blazescrap)  
2. [⚙️ Pré-requisitos](#️-pré-requisitos)  
3. [📥 Instalação](#-instalação)  
4. [🚀 Uso](#-uso)  
5. [📝 Licença](#-licença)  

---

## ⚙️ Pré-requisitos  

Antes de instalar, certifique-se de ter os seguintes requisitos:  

- Os arquivos **JARs** do **Selenium** e **SQLite** compatíveis com o seu sistema operacional, instalados via **Maven** ou manualmente.  
- **Java Development Kit (JDK)** instalado.  
- **javac** (compilador Java) disponível no seu sistema.  

---

## 📥 Instalação  

1. Clone este repositório:  
   ```sh
   git clone https://github.com/DS3ttx/Blaze-Scrap/BlazeScrap.git
   ```  
2. Entre no diretório do projeto:  
   ```sh
   cd Blaze-Scrap
   ```  
3. Compile o código-fonte:  
   ```sh
   javac -cp "caminho/para/selenium.jar:caminho/para/sqlite.jar:." Main.java
   ```  
4. Execute o programa:  
   ```sh
   java -cp "caminho/para/selenium.jar:caminho/para/sqlite.jar:." Main
   ```  

---

## 🚀 Uso  

Após a execução do executável, **não é necessário realizar nenhuma ação adicional**.  
O WebScraper capturará os dados automaticamente.  

📌 **Para encerrar a captura de dados**, basta **fechar o executável e o navegador**.  

---

## 📝 Licença  

Este projeto está sob uma **licença de código aberto**.  

---
