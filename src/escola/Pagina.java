package escola;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Pagina {

    private static final String[][] MENU = {
        {"/alunos", "Alunos"},
        {"/professores", "Professores"},
        {"/disciplinas", "Disciplinas"},
        {"/turmas", "Turmas"},
        {"/matriculas", "Matriculas"},
        {"/vinculos", "Vinculos"},
        {"/avaliacoes", "Avaliacoes"},
        {"/notas", "Notas"},
        {"/relatorios/boletim", "Boletim"},
        {"/relatorios/ranking", "Ranking"},
        {"/relatorios/mais-velhos", "Mais Velhos"}
    };

    public static String montar(HttpExchange exchange, String titulo, String corpo) {
        String caminhoAtual = exchange.getRequestURI().getPath();
        StringBuilder nav = new StringBuilder();
        for (String[] item : MENU) {
            boolean atual = item[0].equals(caminhoAtual);
            nav.append("<a href=\"").append(item[0]).append("\"")
                .append(atual ? " class=\"atual\"" : "")
                .append(">").append(item[1]).append("</a>");
        }
        return "<!DOCTYPE html>"
            + "<html lang=\"pt-br\">"
            + "<head><meta charset=\"UTF-8\"><title>" + titulo + "</title>"
            + "<link rel=\"stylesheet\" href=\"/style.css\"></head>"
            + "<body>"
            + "<nav>" + nav + "</nav>"
            + "<main><h1>" + titulo + "</h1>" + corpo + "</main>"
            + "</body></html>";
    }

    // le os parâmetros da URL
    public static Map<String, String> parametrosDaUrl(HttpExchange exchange) {
        return interpretar(exchange.getRequestURI().getRawQuery());
    }

    // le os campos enviados
    public static Map<String, String> parametrosDoFormulario(HttpExchange exchange) throws IOException {
        String corpo = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        return interpretar(corpo);
    }

    // 
    private static Map<String, String> interpretar(String textoCodificado) {
        Map<String, String> parametros = new HashMap<>();
        if (textoCodificado == null || textoCodificado.isEmpty()) {
            return parametros;
        }
        for (String par : textoCodificado.split("&")) {
            String[] partes = par.split("=", 2);
            String chave = URLDecoder.decode(partes[0], StandardCharsets.UTF_8);
            String valor = partes.length > 1 ? URLDecoder.decode(partes[1], StandardCharsets.UTF_8) : "";
            parametros.put(chave, valor);
        }
        return parametros;
    }

    // envia pg HTML
    public static void responder(HttpExchange exchange, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream saida = exchange.getResponseBody()) {
            saida.write(bytes);
        }
    }

    public static void redirecionar(HttpExchange exchange, String caminho) throws IOException {
        exchange.getResponseHeaders().add("Location", caminho);
        exchange.sendResponseHeaders(302, -1);
        exchange.close();
    }

    public static String vazioSeNulo(String valor) {
        return valor == null ? "" : valor;
    }

    public static String erro(String mensagem) {
        return "<p class=\"erro\">Erro ao salvar: " + mensagem + "</p>";
    }
}
