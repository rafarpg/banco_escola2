package escola;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

public class Relatorios implements HttpHandler {

    @Override
    // Decide qual dos 3 relatorios mostrar, de acordo com a pagina acessada
    public void handle(HttpExchange exchange) {
        try {
            String caminho = exchange.getRequestURI().getPath();
            Map<String, String> parametros = Pagina.parametrosDaUrl(exchange);

            if (caminho.equals("/relatorios/boletim")) {
                Pagina.responder(exchange, Pagina.montar(exchange, "Boletim por Aluno", boletim(parametros.get("id_aluno"))));
            } else if (caminho.equals("/relatorios/ranking")) {
                Pagina.responder(exchange, Pagina.montar(exchange, "Ranking por Materia", ranking(parametros.get("id_disciplina"))));
            } else if (caminho.equals("/relatorios/mais-velhos")) {
                Pagina.responder(exchange, Pagina.montar(exchange, "Alunos Mais Velhos", maisVelhos()));
            } else {
                exchange.sendResponseHeaders(404, -1);
                exchange.close();
            }
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    // usa as views e monta as opcoes de um <select> (dropdown)
    private String montarOpcoes(String sql, String idSelecionado) throws SQLException {
        StringBuilder opcoes = new StringBuilder();
        opcoes.append("<option value=\"\">Selecione...</option>");
        try (Connection con = Banco.conectar();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String id = rs.getString("id");
                boolean selecionado = id.equals(idSelecionado);
                opcoes.append("<option value=\"").append(id).append("\"")
                    .append(selecionado ? " selected" : "")
                    .append(">").append(rs.getString("label")).append("</option>");
            }
        }
        return opcoes.toString();
    }

    // media
    private String boletim(String idAluno) throws SQLException {
        StringBuilder html = new StringBuilder();
        html.append("<form method=\"get\" action=\"/relatorios/boletim\">");
        html.append("<label>Aluno: <select name=\"id_aluno\" onchange=\"this.form.submit()\">")
            .append(montarOpcoes("SELECT id, label FROM vw_autocomplete_aluno ORDER BY label", idAluno))
            .append("</select></label>");
        html.append(" <button type=\"submit\">Ver Boletim</button>");
        html.append("</form>");

        if (idAluno == null || idAluno.isEmpty()) {
            return html.toString();
        }

        html.append("<h2>Notas Detalhadas</h2>");
        String sqlNotas = "SELECT d.nome AS disciplina, av.titulo, av.peso, av.nota_maxima, n.valor_obtido "
            + "FROM nota n "
            + "JOIN avaliacao av ON n.id_avaliacao = av.id_avaliacao "
            + "JOIN turma_disciplina td ON av.id_turma_disciplina = td.id_turma_disciplina "
            + "JOIN disciplina d ON td.id_disciplina = d.id_disciplina "
            + "JOIN matricula m ON n.id_matricula = m.id_matricula "
            + "WHERE m.id_aluno = ? "
            + "ORDER BY d.nome, av.data_realizacao";
        try (Connection con = Banco.conectar(); PreparedStatement stmt = con.prepareStatement(sqlNotas)) {
            stmt.setInt(1, Integer.parseInt(idAluno));
            try (ResultSet rs = stmt.executeQuery()) {
                String disciplinaAtual = null;
                while (rs.next()) {
                    String disciplina = rs.getString("disciplina");
                    if (!disciplina.equals(disciplinaAtual)) {
                        if (disciplinaAtual != null) {
                            html.append("</table>");
                        }
                        html.append("<h3>").append(disciplina).append("</h3>");
                        html.append("<table><tr><th>Avaliacao</th><th>Peso</th><th>Nota Maxima</th><th>Valor Obtido</th></tr>");
                        disciplinaAtual = disciplina;
                    }
                    html.append("<tr>")
                        .append("<td>").append(rs.getString("titulo")).append("</td>")
                        .append("<td>").append(rs.getBigDecimal("peso")).append("</td>")
                        .append("<td>").append(rs.getBigDecimal("nota_maxima")).append("</td>")
                        .append("<td>").append(rs.getBigDecimal("valor_obtido")).append("</td>")
                        .append("</tr>");
                }
                if (disciplinaAtual != null) {
                    html.append("</table>");
                }
            }
        }

        html.append("<h2>Media por Disciplina</h2>");
        html.append("<table><tr><th>Disciplina</th><th>Media</th></tr>");
        String sqlMedias = "SELECT d.nome AS disciplina, ROUND(SUM(n.valor_obtido * av.peso) / SUM(av.peso), 2) AS media "
            + "FROM nota n "
            + "JOIN avaliacao av ON n.id_avaliacao = av.id_avaliacao "
            + "JOIN turma_disciplina td ON av.id_turma_disciplina = td.id_turma_disciplina "
            + "JOIN disciplina d ON td.id_disciplina = d.id_disciplina "
            + "JOIN matricula m ON n.id_matricula = m.id_matricula "
            + "WHERE m.id_aluno = ? "
            + "GROUP BY d.id_disciplina, d.nome "
            + "ORDER BY d.nome";
        try (Connection con = Banco.conectar(); PreparedStatement stmt = con.prepareStatement(sqlMedias)) {
            stmt.setInt(1, Integer.parseInt(idAluno));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    html.append("<tr>")
                        .append("<td>").append(rs.getString("disciplina")).append("</td>")
                        .append("<td>").append(rs.getBigDecimal("media")).append("</td>")
                        .append("</tr>");
                }
            }
        }
        html.append("</table>");
        return html.toString();
    }

    // ranking
    private String ranking(String idDisciplina) throws SQLException {
        StringBuilder html = new StringBuilder();
        html.append("<form method=\"get\" action=\"/relatorios/ranking\">");
        html.append("<label>Disciplina: <select name=\"id_disciplina\" onchange=\"this.form.submit()\">")
            .append(montarOpcoes("SELECT id_disciplina AS id, nome AS label FROM disciplina ORDER BY nome", idDisciplina))
            .append("</select></label>");
        html.append(" <button type=\"submit\">Ver Ranking</button>");
        html.append("</form>");

        if (idDisciplina == null || idDisciplina.isEmpty()) {
            return html.toString();
        }

        html.append("<h2>Ranking</h2>");
        html.append("<table><tr><th>Posicao</th><th>Aluno</th><th>Matricula</th><th>Media</th></tr>");
        String sql = "SELECT a.nome, a.matricula_registro, ROUND(SUM(n.valor_obtido * av.peso) / SUM(av.peso), 2) AS media "
            + "FROM nota n "
            + "JOIN avaliacao av ON n.id_avaliacao = av.id_avaliacao "
            + "JOIN turma_disciplina td ON av.id_turma_disciplina = td.id_turma_disciplina "
            + "JOIN matricula m ON n.id_matricula = m.id_matricula "
            + "JOIN aluno a ON m.id_aluno = a.id_aluno "
            + "WHERE td.id_disciplina = ? "
            + "GROUP BY a.id_aluno, a.nome, a.matricula_registro "
            + "ORDER BY media DESC";
        try (Connection con = Banco.conectar(); PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(idDisciplina));
            try (ResultSet rs = stmt.executeQuery()) {
                int posicao = 1;
                while (rs.next()) {
                    html.append("<tr>")
                        .append("<td>").append(posicao++).append("</td>")
                        .append("<td>").append(rs.getString("nome")).append("</td>")
                        .append("<td>").append(rs.getString("matricula_registro")).append("</td>")
                        .append("<td>").append(rs.getBigDecimal("media")).append("</td>")
                        .append("</tr>");
                }
            }
        }
        html.append("</table>");
        return html.toString();
    }

    // alunos mais velhos
    private String maisVelhos() throws SQLException {
        StringBuilder html = new StringBuilder();
        html.append("<table><tr><th>Nome</th><th>Matricula</th><th>Data de Nascimento</th><th>Idade</th></tr>");
        String sql = "SELECT nome, matricula_registro, data_nascimento, "
            + "TIMESTAMPDIFF(YEAR, data_nascimento, CURDATE()) AS idade "
            + "FROM aluno ORDER BY data_nascimento ASC";
        try (Connection con = Banco.conectar();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                html.append("<tr>")
                    .append("<td>").append(rs.getString("nome")).append("</td>")
                    .append("<td>").append(rs.getString("matricula_registro")).append("</td>")
                    .append("<td>").append(rs.getDate("data_nascimento")).append("</td>")
                    .append("<td>").append(rs.getInt("idade")).append("</td>")
                    .append("</tr>");
            }
        }
        html.append("</table>");
        return html.toString();
    }
}
