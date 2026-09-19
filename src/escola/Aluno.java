package escola;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class Aluno implements HttpHandler {

    // Recebe as requisicoes da tela de Alunos: listar, cadastrar, editar e excluir
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
                    Pagina.responder(exchange, Pagina.montar(exchange, "Alunos", Pagina.erro(e.getMessage()) + montarCorpo(null)));
                    return;
                }
                Pagina.redirecionar(exchange, "/alunos");
                return;
            }

            Map<String, String> parametros = Pagina.parametrosDaUrl(exchange);
            if ("excluir".equals(parametros.get("acao"))) {
                excluir(parametros.get("id"));
                Pagina.redirecionar(exchange, "/alunos");
                return;
            }

            String idEmEdicao = "editar".equals(parametros.get("acao")) ? parametros.get("id") : null;
            Pagina.responder(exchange, Pagina.montar(exchange, "Alunos", montarCorpo(idEmEdicao)));
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Monta o formulario de cadastro/edicao e a lista de alunos cadastrados
    private String montarCorpo(String idEmEdicao) throws SQLException {
        String matriculaRegistro = "", nome = "", cpf = "", nascimento = "", email = "", telefone = "";

        if (idEmEdicao != null) {
            try (Connection con = Banco.conectar();
                 PreparedStatement stmt = con.prepareStatement("SELECT * FROM aluno WHERE id_aluno = ?")) {
                stmt.setInt(1, Integer.parseInt(idEmEdicao));
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        matriculaRegistro = rs.getString("matricula_registro");
                        nome = rs.getString("nome");
                        cpf = rs.getString("cpf");
                        nascimento = rs.getDate("data_nascimento").toString();
                        email = Pagina.vazioSeNulo(rs.getString("email"));
                        telefone = Pagina.vazioSeNulo(rs.getString("telefone"));
                    }
                }
            }
        }

        StringBuilder html = new StringBuilder();
        html.append("<h2>").append(idEmEdicao != null ? "Editar Aluno" : "Novo Aluno").append("</h2>");
        html.append("<form method=\"post\" action=\"/alunos\">");
        html.append("<input type=\"hidden\" name=\"acao\" value=\"").append(idEmEdicao != null ? "atualizar" : "criar").append("\">");
        if (idEmEdicao != null) {
            html.append("<input type=\"hidden\" name=\"id\" value=\"").append(idEmEdicao).append("\">");
        }
        html.append("<label>Matricula: <input name=\"matricula_registro\" value=\"").append(matriculaRegistro).append("\" required></label>");
        html.append("<label>Nome: <input name=\"nome\" value=\"").append(nome).append("\" required></label>");
        html.append("<label>CPF: <input name=\"cpf\" value=\"").append(cpf).append("\" required></label>");
        html.append("<label>Data de Nascimento: <input type=\"date\" name=\"data_nascimento\" value=\"").append(nascimento).append("\" required></label>");
        html.append("<label>Email: <input type=\"email\" name=\"email\" value=\"").append(email).append("\" required></label>");
        html.append("<label>Telefone: <input name=\"telefone\" value=\"").append(telefone).append("\" required></label>");
        html.append("<button type=\"submit\">Salvar</button>");
        html.append("</form>");

        html.append("<h2>Lista de Alunos</h2>");
        html.append("<table><tr><th>Matricula</th><th>Nome</th><th>CPF</th><th>Nascimento</th><th>Email</th><th>Telefone</th><th>Acoes</th></tr>");
        try (Connection con = Banco.conectar();
             PreparedStatement stmt = con.prepareStatement("SELECT * FROM aluno ORDER BY nome");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id_aluno");
                html.append("<tr>")
                    .append("<td>").append(rs.getString("matricula_registro")).append("</td>")
                    .append("<td>").append(rs.getString("nome")).append("</td>")
                    .append("<td>").append(rs.getString("cpf")).append("</td>")
                    .append("<td>").append(rs.getDate("data_nascimento")).append("</td>")
                    .append("<td>").append(Pagina.vazioSeNulo(rs.getString("email"))).append("</td>")
                    .append("<td>").append(Pagina.vazioSeNulo(rs.getString("telefone"))).append("</td>")
                    .append("<td>")
                    .append("<a href=\"/alunos?acao=editar&id=").append(id).append("\">Editar</a> ")
                    .append("<a href=\"/alunos?acao=excluir&id=").append(id).append("\">Excluir</a>")
                    .append("</td></tr>");
            }
        }
        html.append("</table>");
        return html.toString();
    }

    // Cadastra um novo aluno
    private void criar(Map<String, String> dados) throws SQLException {
        String sql = "INSERT INTO aluno (matricula_registro, nome, cpf, data_nascimento, email, telefone) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = Banco.conectar(); PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, dados.get("matricula_registro"));
            stmt.setString(2, dados.get("nome"));
            stmt.setString(3, dados.get("cpf"));
            stmt.setString(4, dados.get("data_nascimento"));
            stmt.setString(5, dados.get("email"));
            stmt.setString(6, dados.get("telefone"));
            stmt.executeUpdate();
        }
    }

    // Atualiza os dados de um aluno existente
    private void atualizar(Map<String, String> dados) throws SQLException {
        String sql = "UPDATE aluno SET matricula_registro=?, nome=?, cpf=?, data_nascimento=?, email=?, telefone=? WHERE id_aluno=?";
        try (Connection con = Banco.conectar(); PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, dados.get("matricula_registro"));
            stmt.setString(2, dados.get("nome"));
            stmt.setString(3, dados.get("cpf"));
            stmt.setString(4, dados.get("data_nascimento"));
            stmt.setString(5, dados.get("email"));
            stmt.setString(6, dados.get("telefone"));
            stmt.setInt(7, Integer.parseInt(dados.get("id")));
            stmt.executeUpdate();
        }
    }

    // excluir matricula e nota também
    private void excluir(String id) throws SQLException {
        try (Connection con = Banco.conectar()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = con.prepareStatement("DELETE FROM matricula WHERE id_aluno=?")) {
                    stmt.setInt(1, Integer.parseInt(id));
                    stmt.executeUpdate();
                }
                try (PreparedStatement stmt = con.prepareStatement("DELETE FROM aluno WHERE id_aluno=?")) {
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
