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

public class Matricula implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) {
        try {
            if (exchange.getRequestMethod().equals("POST")) {
                Map<String, String> dados = Pagina.parametrosDoFormulario(exchange);
                try {
                    if ("criar".equals(dados.get("acao"))) {
                        criar(dados);
                    } else if ("atualizar".equals(dados.get("acao"))) {
                        atualizar(dados);
                    }
                } catch (SQLException e) {
                    Pagina.responder(exchange, Pagina.montar(exchange, "Matriculas", Pagina.erro(e.getMessage()) + montarCorpo(null)));
                    return;
                }
                Pagina.redirecionar(exchange, "/matriculas");
                return;
            }

            Map<String, String> parametros = Pagina.parametrosDaUrl(exchange);
            if ("excluir".equals(parametros.get("acao"))) {
                excluir(parametros.get("id"));
                Pagina.redirecionar(exchange, "/matriculas");
                return;
            }

            String idEmEdicao = "editar".equals(parametros.get("acao")) ? parametros.get("id") : null;
            Pagina.responder(exchange, Pagina.montar(exchange, "Matriculas", montarCorpo(idEmEdicao)));
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String montarCorpo(String idEmEdicao) throws SQLException {
        String idAluno = "", idTurma = "", dataMatricula = "", status = "ATIVA";

        if (idEmEdicao != null) {
            try (Connection con = Banco.conectar();
                 PreparedStatement stmt = con.prepareStatement("SELECT * FROM matricula WHERE id_matricula = ?")) {
                stmt.setInt(1, Integer.parseInt(idEmEdicao));
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        idAluno = String.valueOf(rs.getInt("id_aluno"));
                        idTurma = String.valueOf(rs.getInt("id_turma"));
                        dataMatricula = rs.getDate("data_matricula").toString();
                        status = rs.getString("status");
                    }
                }
            }
        }

        StringBuilder html = new StringBuilder();
        html.append("<h2>").append(idEmEdicao != null ? "Editar Matricula" : "Nova Matricula").append("</h2>");
        html.append("<form method=\"post\" action=\"/matriculas\">");
        html.append("<input type=\"hidden\" name=\"acao\" value=\"").append(idEmEdicao != null ? "atualizar" : "criar").append("\">");
        if (idEmEdicao != null) {
            html.append("<input type=\"hidden\" name=\"id\" value=\"").append(idEmEdicao).append("\">");
        }
        html.append("<label>Aluno: <select name=\"id_aluno\" required>")
            .append(montarOpcoes("SELECT id, label FROM vw_autocomplete_aluno ORDER BY label", idAluno))
            .append("</select></label>");
        html.append("<label>Turma: <select name=\"id_turma\" required>")
            .append(montarOpcoes("SELECT id, label FROM vw_autocomplete_turma ORDER BY label", idTurma))
            .append("</select></label>");
        html.append("<label>Data da Matricula: <input type=\"date\" name=\"data_matricula\" value=\"").append(dataMatricula).append("\" required></label>");
        html.append("<label>Status: <input name=\"status\" value=\"").append(status).append("\" required></label>");
        html.append("<button type=\"submit\">Salvar</button>");
        html.append("</form>");

        html.append("<h2>Lista de Matriculas</h2>");
        html.append("<table><tr><th>Aluno</th><th>Turma</th><th>Data</th><th>Status</th><th>Acoes</th></tr>");
        String sqlListagem = "SELECT m.id_matricula, a.nome AS aluno, t.sigla AS turma, m.data_matricula, m.status "
            + "FROM matricula m JOIN aluno a ON m.id_aluno = a.id_aluno JOIN turma t ON m.id_turma = t.id_turma "
            + "ORDER BY m.id_matricula DESC";
        try (Connection con = Banco.conectar();
             PreparedStatement stmt = con.prepareStatement(sqlListagem);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id_matricula");
                html.append("<tr>")
                    .append("<td>").append(rs.getString("aluno")).append("</td>")
                    .append("<td>").append(rs.getString("turma")).append("</td>")
                    .append("<td>").append(rs.getDate("data_matricula")).append("</td>")
                    .append("<td>").append(rs.getString("status")).append("</td>")
                    .append("<td>")
                    .append("<a href=\"/matriculas?acao=editar&id=").append(id).append("\">Editar</a> ")
                    .append("<a href=\"/matriculas?acao=excluir&id=").append(id).append("\">Excluir</a>")
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

    private void criar(Map<String, String> dados) throws SQLException {
        String sql = "INSERT INTO matricula (id_aluno, id_turma, data_matricula, status) VALUES (?, ?, ?, ?)";
        try (Connection con = Banco.conectar(); PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(dados.get("id_aluno")));
            stmt.setInt(2, Integer.parseInt(dados.get("id_turma")));
            stmt.setString(3, dados.get("data_matricula"));
            stmt.setString(4, dados.get("status"));
            stmt.executeUpdate();
        }
    }

    private void atualizar(Map<String, String> dados) throws SQLException {
        String sql = "UPDATE matricula SET id_aluno=?, id_turma=?, data_matricula=?, status=? WHERE id_matricula=?";
        try (Connection con = Banco.conectar(); PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(dados.get("id_aluno")));
            stmt.setInt(2, Integer.parseInt(dados.get("id_turma")));
            stmt.setString(3, dados.get("data_matricula"));
            stmt.setString(4, dados.get("status"));
            stmt.setInt(5, Integer.parseInt(dados.get("id")));
            stmt.executeUpdate();
        }
    }

    private void excluir(String id) throws SQLException {
        try (Connection con = Banco.conectar();
             PreparedStatement stmt = con.prepareStatement("DELETE FROM matricula WHERE id_matricula=?")) {
            stmt.setInt(1, Integer.parseInt(id));
            stmt.executeUpdate();
        }
    }
}
