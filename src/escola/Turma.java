package escola;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class Turma implements HttpHandler {

    @Override
    //requisicoes da tela de Turmas
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
                    Pagina.responder(exchange, Pagina.montar(exchange, "Turmas", Pagina.erro(e.getMessage()) + montarCorpo(null)));
                    return;
                }
                Pagina.redirecionar(exchange, "/turmas");
                return;
            }

            Map<String, String> parametros = Pagina.parametrosDaUrl(exchange);
            if ("excluir".equals(parametros.get("acao"))) {
                excluir(parametros.get("id"));
                Pagina.redirecionar(exchange, "/turmas");
                return;
            }

            String idEmEdicao = "editar".equals(parametros.get("acao")) ? parametros.get("id") : null;
            Pagina.responder(exchange, Pagina.montar(exchange, "Turmas", montarCorpo(idEmEdicao)));
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    // crud turmas
    private String montarCorpo(String idEmEdicao) throws SQLException {
        String sigla = "", anoLetivo = "", semestre = "", turno = "";

        if (idEmEdicao != null) {
            try (Connection con = Banco.conectar();
                 PreparedStatement stmt = con.prepareStatement("SELECT * FROM turma WHERE id_turma = ?")) {
                stmt.setInt(1, Integer.parseInt(idEmEdicao));
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        sigla = rs.getString("sigla");
                        anoLetivo = String.valueOf(rs.getInt("ano_letivo"));
                        semestre = String.valueOf(rs.getInt("semestre"));
                        turno = rs.getString("turno");
                    }
                }
            }
        }

        StringBuilder html = new StringBuilder();
        html.append("<h2>").append(idEmEdicao != null ? "Editar Turma" : "Nova Turma").append("</h2>");
        html.append("<form method=\"post\" action=\"/turmas\">");
        html.append("<input type=\"hidden\" name=\"acao\" value=\"").append(idEmEdicao != null ? "atualizar" : "criar").append("\">");
        if (idEmEdicao != null) {
            html.append("<input type=\"hidden\" name=\"id\" value=\"").append(idEmEdicao).append("\">");
        }
        html.append("<label>Sigla: <input name=\"sigla\" value=\"").append(sigla).append("\" required></label>");
        html.append("<label>Ano Letivo: <input type=\"number\" name=\"ano_letivo\" value=\"").append(anoLetivo).append("\" required></label>");
        html.append("<label>Semestre: <input type=\"number\" name=\"semestre\" value=\"").append(semestre).append("\" required></label>");
        html.append("<label>Turno: <input name=\"turno\" value=\"").append(turno).append("\" required></label>");
        html.append("<button type=\"submit\">Salvar</button>");
        html.append("</form>");

        html.append("<h2>Lista de Turmas</h2>");
        html.append("<table><tr><th>Sigla</th><th>Ano Letivo</th><th>Semestre</th><th>Turno</th><th>Acoes</th></tr>");
        try (Connection con = Banco.conectar();
             PreparedStatement stmt = con.prepareStatement("SELECT * FROM turma ORDER BY ano_letivo DESC, sigla");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id_turma");
                html.append("<tr>")
                    .append("<td>").append(rs.getString("sigla")).append("</td>")
                    .append("<td>").append(rs.getInt("ano_letivo")).append("</td>")
                    .append("<td>").append(rs.getInt("semestre")).append("</td>")
                    .append("<td>").append(rs.getString("turno")).append("</td>")
                    .append("<td>")
                    .append("<a href=\"/turmas?acao=editar&id=").append(id).append("\">Editar</a> ")
                    .append("<a href=\"/turmas?acao=excluir&id=").append(id).append("\">Excluir</a>")
                    .append("</td></tr>");
            }
        }
        html.append("</table>");
        return html.toString();
    }

    // cadastra turma
    private void criar(Map<String, String> dados) throws SQLException {
        String sql = "INSERT INTO turma (sigla, ano_letivo, semestre, turno) VALUES (?, ?, ?, ?)";
        try (Connection con = Banco.conectar(); PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, dados.get("sigla"));
            stmt.setInt(2, Integer.parseInt(dados.get("ano_letivo")));
            stmt.setInt(3, Integer.parseInt(dados.get("semestre")));
            stmt.setString(4, dados.get("turno"));
            stmt.executeUpdate();
        }
    }

    // atualizar turma
    private void atualizar(Map<String, String> dados) throws SQLException {
        String sql = "UPDATE turma SET sigla=?, ano_letivo=?, semestre=?, turno=? WHERE id_turma=?";
        try (Connection con = Banco.conectar(); PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, dados.get("sigla"));
            stmt.setInt(2, Integer.parseInt(dados.get("ano_letivo")));
            stmt.setInt(3, Integer.parseInt(dados.get("semestre")));
            stmt.setString(4, dados.get("turno"));
            stmt.setInt(5, Integer.parseInt(dados.get("id")));
            stmt.executeUpdate();
        }
    }

    // exclui uma turma apagando antes matriculas, vinculos e o que depende deles
    private void excluir(String id) throws SQLException {
        try (Connection con = Banco.conectar()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = con.prepareStatement("DELETE FROM matricula WHERE id_turma=?")) {
                    stmt.setInt(1, Integer.parseInt(id));
                    stmt.executeUpdate();
                }
                try (PreparedStatement stmt = con.prepareStatement("DELETE FROM turma_disciplina WHERE id_turma=?")) {
                    stmt.setInt(1, Integer.parseInt(id));
                    stmt.executeUpdate();
                }
                try (PreparedStatement stmt = con.prepareStatement("DELETE FROM turma WHERE id_turma=?")) {
                    stmt.setInt(1, Integer.parseInt(id));
                    stmt.executeUpdate();
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }
}
