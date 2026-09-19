package escola;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {

    // Inicia o servidor e registra cada rota do sistema
    public static void main(String[] args) throws IOException {
        HttpServer servidor = HttpServer.create(new InetSocketAddress(8080), 0);

        servidor.createContext("/", exchange -> {
            if (!exchange.getRequestURI().getPath().equals("/")) {
                exchange.sendResponseHeaders(404, -1);
                exchange.close();
                return;
            }
            String corpo = "<ul>"
                + "<li><a href=\"/alunos\">Alunos</a></li>"
                + "<li><a href=\"/professores\">Professores</a></li>"
                + "<li><a href=\"/disciplinas\">Disciplinas</a></li>"
                + "<li><a href=\"/turmas\">Turmas</a></li>"
                + "<li><a href=\"/matriculas\">Matriculas</a></li>"
                + "<li><a href=\"/vinculos\">Vinculos (Turma + Disciplina + Professor)</a></li>"
                + "<li><a href=\"/avaliacoes\">Avaliacoes</a></li>"
                + "<li><a href=\"/notas\">Notas</a></li>"
                + "<li><a href=\"/relatorios/boletim\">Relatorio: Boletim por Aluno</a></li>"
                + "<li><a href=\"/relatorios/ranking\">Relatorio: Ranking por Materia</a></li>"
                + "<li><a href=\"/relatorios/mais-velhos\">Relatorio: Alunos Mais Velhos</a></li>"
                + "</ul>";
            Pagina.responder(exchange, Pagina.montar(exchange, "Sistema de Avaliacoes Escolares", corpo));
        });

        servidor.createContext("/style.css", exchange -> {
            byte[] css = Files.readAllBytes(Paths.get("web/style.css"));
            exchange.getResponseHeaders().add("Content-Type", "text/css; charset=UTF-8");
            exchange.sendResponseHeaders(200, css.length);
            try (OutputStream saida = exchange.getResponseBody()) {
                saida.write(css);
            }
        });

        servidor.createContext("/alunos", new Aluno());
        servidor.createContext("/professores", new Professor());
        servidor.createContext("/disciplinas", new Disciplina());
        servidor.createContext("/turmas", new Turma());
        servidor.createContext("/matriculas", new Matricula());
        servidor.createContext("/vinculos", new Vinculo());
        servidor.createContext("/avaliacoes", new Avaliacao());
        servidor.createContext("/notas", new Nota());
        servidor.createContext("/relatorios", new Relatorios());

        servidor.setExecutor(null);
        servidor.start();
        System.out.println("Servidor rodando em http://localhost:8080/");
    }
}
