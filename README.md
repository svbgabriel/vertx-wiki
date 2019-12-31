# Vert.x Wiki

Esse projeto foi feito seguindo o guia disponível:

[A gentle guide to asynchronous programming with Eclipse Vert.x for Java developers](https://vertx.io/docs/guide-for-java-devs)

## Pré-requisitos

* Apache Maven
* JDK 8+

## Desenvolvimento

Faça o clone do projeto:

	https://github.com/svbgabriel/vertx-wiki.git

Crie a chave para acesso via HTTPS (exemplo):

	keytool -genkey \
	-alias test \
	-keyalg RSA \
	-keystore server-keystore.jks \
	-keysize 2048 \
	-validity 360 \
	-dname CN=localhost \
	-keypass secret \
	-storepass secret

## Executando o projeto

Você pode executar o projeto com o comando:

	mvn test exec:java

Esse comando compila o projeto e executa os testes, então a aplicação estará disponível por padrão em http://localhost:8080. Você deve ver o a página inicial da Wiki.

## Compilando o projeto

Você pode compilar o projeto com o comando:

	mvn clean package

Isso irá gerar um *fat-jar* no diretório `target`.
