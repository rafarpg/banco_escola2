package escola;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

public class Nota implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) {
        try {
            if (exchange.getRequestMethod().equals("POST")) {
                Map<String, String> dados = Pagina.parametrosDoFormulario(exchange);
                String acao = dados.get("acao");
                if ("criar".equals(acao) || "atualizar".equals(acao)) {
                    int idAvaliacao = Integer.parseInt(dados.get("id_avaliacao"));
                    int idMatricula = Integer.parseInt(dados.get("id_matricula"));
                    if (!alunoEstaNaTurmaDaAvaliacao(idAvaliacao, idMatricula)) {
                        String erro = Pagina.erro("o aluno dessa matricula nao esta matriculado na turma em que "
                            + "essa avaliacao foi criada (disciplina com outro professor/turma).");
                        Pagina.responder(exchange, Pagina.montar(exchange, "Notas", erro + montarCorpo(null)));
                        return;
                    }
                    try {
                        if ("criar".equals(acao)) {
                            criar(dados);
                        } else {
                            atualizar(dados);
                        }
                    } catch (SQLException e) {
                        Pagina.responder(exchange, Pagina.montar(exchange, "Notas", Pagina.erro(e.getMessage()) + montarCorpo(null)));
                        return;
                    }
                }
                Pagina.redirecionar(exchange, "/notas");
                return;
            }

            Map<String, String> parametros = Pagina.parametrosDaUrl(exchange);
            if ("excluir".equals(parametros.get("acao"))) {
                excluir(parametros.get("id"));
                Pagina.redirecionar(exchange, "/notas");
                return;
            }

            String idEmEdicao = "editar".equals(parametros.get("acao")) ? parametros.get("id") : null;
            Pagina.responder(exchange, Pagina.montar(exchange, "Notas", montarCorpo(idEmEdicao)));
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    private String montarCorpo(String idEmEdicao) throws SQLException {
        String idAvaliacao = "", idMatricula = "", valorObtido = "", observacao = "";

        if (idEmEdicao != null) {
            try (Connection con = Banco.conectar();
                 PreparedStatement stmt = con.prepareStatement("SELECT * FROM nota WHERE id_nota = ?")) {
                stmt.setInt(1, Integer.parseInt(idEmEdicao));
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        idAvaliacao = String.valueOf(rs.getInt("id_avaliacao"));
                        idMatricula = String.valueOf(rs.getInt("id_matricula"));
                        valorObtido = rs.getBigDecimal("valor_obtido").toString();
                        observacao = Pagina.vazioSeNulo(rs.getString("observacao"));
                    }
                }
            }
        }

        StringBuilder html = new StringBuilder();
        html.append("<h2>").append(idEmEdicao != null ? "Editar Nota" : "Nova Nota").append("</h2>");
        html.append("<form method=\"post\" action=\"/notas\">");
        html.append("<input type=\"hidden\" name=\"acao\" value=\"").append(idEmEdicao != null ? "atualizar" : "criar").append("\">");
        if (idEmEdicao != null) {
            html.append("<input type=\"hidden\" name=\"id\" value=\"").append(idEmEdicao).append("\">");
        }
        html.append("<label>Avaliacao: <select name=\"id_avaliacao\" required>")
            .append(montarOpcoes("SELECT id, label FROM vw_autocomplete_avaliacao ORDER BY label", idAvaliacao))
            .append("</select></label>");
        html.append("<label>Matricula (Aluno na Turma): <select name=\"id_matricula\" required>")
            .append(montarOpcoes("SELECT id, label FROM vw_autocomplete_matricula ORDER BY label", idMatricula))
            .append("</select></label>");
        html.append("<label>Valor Obtido: <input type=\"number\" step=\"0.01\" name=\"valor_obtido\" value=\"").append(valorObtido).append("\" required></label>");
        html.append("<label>Observacao: <textarea name=\"observacao\">").append(observacao).append("</textarea></label>");
        html.append("<button type=\"submit\">Salvar</button>");
        html.append("</form>");

        html.append("<h2>Lista de Notas</h2>");
        html.append("<table><tr><th>Aluno</th><th>Avaliacao</th><th>Valor Obtido</th><th>Nota Maxima</th><th>Data</th><th>Observacao</th><th>Acoes</th></tr>");
        String sqlListagem = "SELECT n.id_nota, a.nome AS aluno, av.titulo AS avaliacao, n.valor_obtido, av.nota_maxima, n.data_lancamento, n.observacao "
            + "FROM nota n "
            + "JOIN matricula m ON n.id_matricula = m.id_matricula "
            + "JOIN aluno a ON m.id_aluno = a.id_aluno "
            + "JOIN avaliacao av ON n.id_avaliacao = av.id_avaliacao "
            + "ORDER BY n.data_lancamento DESC";
        try (Connection con = Banco.conectar();
             PreparedStatement stmt = con.prepareStatement(sqlListagem);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id_nota");
                html.append("<tr>")
                    .append("<td>").append(rs.getString("aluno")).append("</td>")
                    .append("<td>").append(rs.getString("avaliacao")).append("</td>")
                    .append("<td>").append(rs.getBigDecimal("valor_obtido")).append("</td>")
                    .append("<td>").append(rs.getBigDecimal("nota_maxima")).append("</td>")
                    .append("<td>").append(rs.getDate("data_lancamento")).append("</td>")
                    .append("<td>").append(Pagina.vazioSeNulo(rs.getString("observacao"))).append("</td>")
                    .append("<td>")
                    .append("<a href=\"/notas?acao=editar&id=").append(id).append("\">Editar</a> ")
                    .append("<a href=\"/notas?acao=excluir&id=").append(id).append("\">Excluir</a>")
                    .append("</td></tr>");
            }
        }
        html.append("</table>");
        return html.toString();
    }

    private String montarOpcoes(String sql, String idSelecionado) throws SQLException {
        StringBuilder opcoes = new StringBuilder();
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

    // Confere se o aluno da matricula pertence a turma dona dessa avaliacao
    private boolean alunoEstaNaTurmaDaAvaliacao(int idAvaliacao, int idMatricula) throws SQLException {
        String sql = "SELECT 1 "
            + "FROM avaliacao av "
            + "JOIN turma_disciplina td ON av.id_turma_disciplina = td.id_turma_disciplina "
            + "JOIN matricula m ON m.id_turma = td.id_turma "
            + "WHERE av.id_avaliacao = ? AND m.id_matricula = ?";
        try (Connection con = Banco.conectar(); PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, idAvaliacao);
            stmt.setInt(2, idMatricula);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    // lança a nota
    private void criar(Map<String, String> dados) throws SQLException {
        String sql = "INSERT INTO nota (id_avaliacao, id_matricula, valor_obtido, observacao) VALUES (?, ?, ?, ?)";
        try (Connection con = Banco.conectar(); PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(dados.get("id_avaliacao")));
            stmt.setInt(2, Integer.parseInt(dados.get("id_matricula")));
            stmt.setBigDecimal(3, new BigDecimal(dados.get("valor_obtido")));
            stmt.setString(4, dados.get("observacao"));
            stmt.executeUpdate();
        }
    }

    private void atualizar(Map<String, String> dados) throws SQLException {
        String sql = "UPDATE nota SET id_avaliacao=?, id_matricula=?, valor_obtido=?, observacao=? WHERE id_nota=?";
        try (Connection con = Banco.conectar(); PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(dados.get("id_avaliacao")));
            stmt.setInt(2, Integer.parseInt(dados.get("id_matricula")));
            stmt.setBigDecimal(3, new BigDecimal(dados.get("valor_obtido")));
            stmt.setString(4, dados.get("observacao"));
            stmt.setInt(5, Integer.parseInt(dados.get("id")));
            stmt.executeUpdate();
        }
    }

    // Exclui uma nota
    private void excluir(String id) throws SQLException {
        try (Connection con = Banco.conectar();
             PreparedStatement stmt = con.prepareStatement("DELETE FROM nota WHERE id_nota=?")) {
            stmt.setInt(1, Integer.parseInt(id));
            stmt.executeUpdate();
        }
    }
}
