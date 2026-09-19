package escola;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class Disciplina implements HttpHandler {

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
                    Pagina.responder(exchange, Pagina.montar(exchange, "Disciplinas", Pagina.erro(e.getMessage()) + montarCorpo(null)));
                    return;
                }
                Pagina.redirecionar(exchange, "/disciplinas");
                return;
            }

            Map<String, String> parametros = Pagina.parametrosDaUrl(exchange);
            if ("excluir".equals(parametros.get("acao"))) {
                excluir(parametros.get("id"));
                Pagina.redirecionar(exchange, "/disciplinas");
                return;
            }

            String idEmEdicao = "editar".equals(parametros.get("acao")) ? parametros.get("id") : null;
            Pagina.responder(exchange, Pagina.montar(exchange, "Disciplinas", montarCorpo(idEmEdicao)));
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String montarCorpo(String idEmEdicao) throws SQLException {
        String codigo = "", nome = "", cargaHoraria = "", ementa = "";

        if (idEmEdicao != null) {
            try (Connection con = Banco.conectar();
                 PreparedStatement stmt = con.prepareStatement("SELECT * FROM disciplina WHERE id_disciplina = ?")) {
                stmt.setInt(1, Integer.parseInt(idEmEdicao));
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        codigo = rs.getString("codigo");
                        nome = rs.getString("nome");
                        cargaHoraria = String.valueOf(rs.getInt("carga_horaria"));
                        ementa = Pagina.vazioSeNulo(rs.getString("ementa"));
                    }
                }
            }
        }

        StringBuilder html = new StringBuilder();
        html.append("<h2>").append(idEmEdicao != null ? "Editar Disciplina" : "Nova Disciplina").append("</h2>");
        html.append("<form method=\"post\" action=\"/disciplinas\">");
        html.append("<input type=\"hidden\" name=\"acao\" value=\"").append(idEmEdicao != null ? "atualizar" : "criar").append("\">");
        if (idEmEdicao != null) {
            html.append("<input type=\"hidden\" name=\"id\" value=\"").append(idEmEdicao).append("\">");
        }
        html.append("<label>Codigo: <input name=\"codigo\" value=\"").append(codigo).append("\" required></label>");
        html.append("<label>Nome: <input name=\"nome\" value=\"").append(nome).append("\" required></label>");
        html.append("<label>Carga Horaria: <input type=\"number\" name=\"carga_horaria\" value=\"").append(cargaHoraria).append("\" required></label>");
        html.append("<label>Ementa: <textarea name=\"ementa\" required>").append(ementa).append("</textarea></label>");
        html.append("<button type=\"submit\">Salvar</button>");
        html.append("</form>");

        html.append("<h2>Lista de Disciplinas</h2>");
        html.append("<table><tr><th>Codigo</th><th>Nome</th><th>Carga Horaria</th><th>Acoes</th></tr>");
        try (Connection con = Banco.conectar();
             PreparedStatement stmt = con.prepareStatement("SELECT * FROM disciplina ORDER BY nome");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id_disciplina");
                html.append("<tr>")
                    .append("<td>").append(rs.getString("codigo")).append("</td>")
                    .append("<td>").append(rs.getString("nome")).append("</td>")
                    .append("<td>").append(rs.getInt("carga_horaria")).append("</td>")
                    .append("<td>")
                    .append("<a href=\"/disciplinas?acao=editar&id=").append(id).append("\">Editar</a> ")
                    .append("<a href=\"/disciplinas?acao=excluir&id=").append(id).append("\">Excluir</a>")
                    .append("</td></tr>");
            }
        }
        html.append("</table>");
        return html.toString();
    }

    private void criar(Map<String, String> dados) throws SQLException {
        String sql = "INSERT INTO disciplina (codigo, nome, carga_horaria, ementa) VALUES (?, ?, ?, ?)";
        try (Connection con = Banco.conectar(); PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, dados.get("codigo"));
            stmt.setString(2, dados.get("nome"));
            stmt.setInt(3, Integer.parseInt(dados.get("carga_horaria")));
            stmt.setString(4, dados.get("ementa"));
            stmt.executeUpdate();
        }
    }

    private void atualizar(Map<String, String> dados) throws SQLException {
        String sql = "UPDATE disciplina SET codigo=?, nome=?, carga_horaria=?, ementa=? WHERE id_disciplina=?";
        try (Connection con = Banco.conectar(); PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, dados.get("codigo"));
            stmt.setString(2, dados.get("nome"));
            stmt.setInt(3, Integer.parseInt(dados.get("carga_horaria")));
            stmt.setString(4, dados.get("ementa"));
            stmt.setInt(5, Integer.parseInt(dados.get("id")));
            stmt.executeUpdate();
        }
    }

    private void excluir(String id) throws SQLException {
        try (Connection con = Banco.conectar()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = con.prepareStatement("DELETE FROM turma_disciplina WHERE id_disciplina=?")) {
                    stmt.setInt(1, Integer.parseInt(id));
                    stmt.executeUpdate();
                }
                try (PreparedStatement stmt = con.prepareStatement("DELETE FROM disciplina WHERE id_disciplina=?")) {
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
