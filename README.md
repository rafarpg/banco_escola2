# Sistema de Avaliações Escolares

Aplicação simples (trabalho de faculdade) para simular um sistema de avaliações de uma escola.

## Tecnologias Utilizadas

- **Linguagem**: Java, com servidor HTTP embutido (`com.sun.net.httpserver`) 
- **Banco de Dados**: MySQL, via XAMPP.
- **Driver JDBC**: `com.mysql.cj.jdbc.Driver` (MySQL Connector/J), já incluso em `lib/`.
- **Frontend**: HTML e CSS

##  Requisitos

- **JDK**
- **XAMPP**


## 1. Ligar o MySQL

1. Abra o **XAMPP Control Panel**.
2. Clique em **Start** na linha do **MySQL**. 

## 2. Criar o banco de dados pelo phpMyAdmin

1. Com o MySQL ligado, abra o navegador em **<http://localhost/phpmyadmin/>**.
2. No menu à esquerda, clique em **Novo**.
3. Em "Nome do banco de dados", digite `banco_escola` e clique em **Criar**.

## 3. Importar as tabelas

1. Ainda no phpMyAdmin, clique no banco `banco_escola` que acabou de criar (menu à esquerda).
2. Clique na aba **Importar** (no topo da tela).
3. Clique em **Escolher arquivo** e selecione o arquivo `database\banco_escola.sql` desta pasta do projeto.
4. Role até o final da página e clique em **Executar** (ou **Go**).

Ou copie o script SQL completo no arquivo banco_escola

## 4. Abrir e rodar o projeto (pelo VSCode)

1. Abra a pasta do projeto inteira no VSCode (`Arquivo > Abrir Pasta`).
2. Abra o arquivo `src/escola/Main.java`.
3. Clique no ícone de **Run**.
4. Vai abrir um terminal integrado mostrando `Servidor rodando em http://localhost:8080/`.

> Precisa ter o MySQL rodando.

### Alternativa por terminal

```
javac -encoding UTF-8 -d out src\escola\*.java
java -cp "out;lib\mysql-connector-j-9.1.0.jar" escola.Main
```

## 5. Usar o sistema

Abra **<http://localhost:8080/>** no navegador.

## Ajustes (se necessário)

Se a senha do `root` do seu MySQL não for vazia, ou a porta não for `3306`, abra o arquivo `src\escola\Banco.java` em qualquer editor e ajuste estas linhas:

```java
private static final String URL = "jdbc:mysql://localhost:3306/banco_escola";
private static final String USUARIO = "root";
private static final String SENHA = "";
```

Salve o arquivo e rode de novo.

## O que a aplicação tem

Cadastro completo (CRUD) para:
- Alunos, Professores, Disciplinas, Turmas
- Matrículas (aluno em turma)
- Vínculos (disciplina + professor dentro de uma turma)
- Avaliações (provas/trabalhos de um vínculo)
- Notas (nota de um aluno em uma avaliação)

E 3 relatórios:
- **Boletim por Aluno**: notas e média ponderada de um aluno escolhido agrupadas por disciplina.
- **Ranking por Matéria**: alunos de uma disciplina ordenados pela média ponderada, do maior para o menor.
- **Alunos Mais Velhos**: todos os alunos ordenados por data de nascimento, com a idade calculada.

## Estrutura do projeto

```
database/banco_escola.sql -> script SQL com tabelas e views
src/escola/*.java         -> código Java
web/style.css             -> estilização
lib/                      -> ja contem o driver JDBC do MySQL
out/                      -> gerado pela compilação (.class)
```
