package escola;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class Professor implements HttpHandler {

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
                    Pagina.responder(exchange, Pagina.montar(exchange, "Professores", Pagina.erro(e.getMessage()) + montarCorpo(null)));
                    return;
                }
                Pagina.redirecionar(exchange, "/professores");
                return;
            }

            Map<String, String> parametros = Pagina.parametrosDaUrl(exchange);
            if ("excluir".equals(parametros.get("acao"))) {
                excluir(parametros.get("id"));
                Pagina.redirecionar(exchange, "/professores");
                return;
            }

            String idEmEdicao = "editar".equals(parametros.get("acao")) ? parametros.get("id") : null;
            Pagina.responder(exchange, Pagina.montar(exchange, "Professores", montarCorpo(idEmEdicao)));
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    // crud
    private String montarCorpo(String idEmEdicao) throws SQLException {
        String nome = "", cpf = "", email = "", formacao = "";

        if (idEmEdicao != null) {
            try (Connection con = Banco.conectar();
                 PreparedStatement stmt = con.prepareStatement("SELECT * FROM professor WHERE id_professor = ?")) {
                stmt.setInt(1, Integer.parseInt(idEmEdicao));
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        nome = rs.getString("nome");
                        cpf = rs.getString("cpf");
                        email = rs.getString("email");
                        formacao = Pagina.vazioSeNulo(rs.getString("formacao"));
                    }
                }
            }
        }

        StringBuilder html = new StringBuilder();
        html.append("<h2>").append(idEmEdicao != null ? "Editar Professor" : "Novo Professor").append("</h2>");
        html.append("<form method=\"post\" action=\"/professores\">");
        html.append("<input type=\"hidden\" name=\"acao\" value=\"").append(idEmEdicao != null ? "atualizar" : "criar").append("\">");
        if (idEmEdicao != null) {
            html.append("<input type=\"hidden\" name=\"id\" value=\"").append(idEmEdicao).append("\">");
        }
        html.append("<label>Nome: <input name=\"nome\" value=\"").append(nome).append("\" required></label>");
        html.append("<label>CPF: <input name=\"cpf\" value=\"").append(cpf).append("\" required></label>");
        html.append("<label>Email: <input type=\"email\" name=\"email\" value=\"").append(email).append("\" required></label>");
        html.append("<label>Formacao: <input name=\"formacao\" value=\"").append(formacao).append("\" required></label>");
        html.append("<button type=\"submit\">Salvar</button>");
        html.append("</form>");

        html.append("<h2>Lista de Professores</h2>");
        html.append("<table><tr><th>Nome</th><th>CPF</th><th>Email</th><th>Formacao</th><th>Acoes</th></tr>");
        try (Connection con = Banco.conectar();
             PreparedStatement stmt = con.prepareStatement("SELECT * FROM professor ORDER BY nome");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id_professor");
                html.append("<tr>")
                    .append("<td>").append(rs.getString("nome")).append("</td>")
                    .append("<td>").append(rs.getString("cpf")).append("</td>")
                    .append("<td>").append(rs.getString("email")).append("</td>")
                    .append("<td>").append(Pagina.vazioSeNulo(rs.getString("formacao"))).append("</td>")
                    .append("<td>")
                    .append("<a href=\"/professores?acao=editar&id=").append(id).append("\">Editar</a> ")
                    .append("<a href=\"/professores?acao=excluir&id=").append(id).append("\">Excluir</a>")
                    .append("</td></tr>");
            }
        }
        html.append("</table>");
        return html.toString();
    }

    // Cadastra um novo professor
    private void criar(Map<String, String> dados) throws SQLException {
        String sql = "INSERT INTO professor (nome, cpf, email, formacao) VALUES (?, ?, ?, ?)";
        try (Connection con = Banco.conectar(); PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, dados.get("nome"));
            stmt.setString(2, dados.get("cpf"));
            stmt.setString(3, dados.get("email"));
            stmt.setString(4, dados.get("formacao"));
            stmt.executeUpdate();
        }
    }

    // Atualiza os dados de um professor existente
    private void atualizar(Map<String, String> dados) throws SQLException {
        String sql = "UPDATE professor SET nome=?, cpf=?, email=?, formacao=? WHERE id_professor=?";
        try (Connection con = Banco.conectar(); PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, dados.get("nome"));
            stmt.setString(2, dados.get("cpf"));
            stmt.setString(3, dados.get("email"));
            stmt.setString(4, dados.get("formacao"));
            stmt.setInt(5, Integer.parseInt(dados.get("id")));
            stmt.executeUpdate();
        }
    }

    // pra apagar professor tem que excluir antes os vinculos (turma+disciplina) dele
    private void excluir(String id) throws SQLException {
        try (Connection con = Banco.conectar()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = con.prepareStatement("DELETE FROM turma_disciplina WHERE id_professor=?")) {
                    stmt.setInt(1, Integer.parseInt(id));
                    stmt.executeUpdate();
                }
                try (PreparedStatement stmt = con.prepareStatement("DELETE FROM professor WHERE id_professor=?")) {
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
