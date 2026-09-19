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

public class Vinculo implements HttpHandler {

    @Override
    // Recebe as requisicoes da tela de Vinculos
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
                    Pagina.responder(exchange, Pagina.montar(exchange, "Vinculos (Turma + Disciplina + Professor)", Pagina.erro(e.getMessage()) + montarCorpo(null)));
                    return;
                }
                Pagina.redirecionar(exchange, "/vinculos");
                return;
            }

            Map<String, String> parametros = Pagina.parametrosDaUrl(exchange);
            if ("excluir".equals(parametros.get("acao"))) {
                excluir(parametros.get("id"));
                Pagina.redirecionar(exchange, "/vinculos");
                return;
            }

            String idEmEdicao = "editar".equals(parametros.get("acao")) ? parametros.get("id") : null;
            Pagina.responder(exchange, Pagina.montar(exchange, "Vinculos (Turma + Disciplina + Professor)", montarCorpo(idEmEdicao)));
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Monta o formulario de cadastro ou edicao e a lista de vinculos cadastrados
    private String montarCorpo(String idEmEdicao) throws SQLException {
        String idTurma = "", idDisciplina = "", idProfessor = "", salaAula = "";

        if (idEmEdicao != null) {
            try (Connection con = Banco.conectar();
                 PreparedStatement stmt = con.prepareStatement("SELECT * FROM turma_disciplina WHERE id_turma_disciplina = ?")) {
                stmt.setInt(1, Integer.parseInt(idEmEdicao));
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        idTurma = String.valueOf(rs.getInt("id_turma"));
                        idDisciplina = String.valueOf(rs.getInt("id_disciplina"));
                        idProfessor = String.valueOf(rs.getInt("id_professor"));
                        salaAula = Pagina.vazioSeNulo(rs.getString("sala_aula"));
                    }
                }
            }
        }

        StringBuilder html = new StringBuilder();
        html.append("<h2>").append(idEmEdicao != null ? "Editar Vinculo" : "Novo Vinculo").append("</h2>");
        html.append("<form method=\"post\" action=\"/vinculos\">");
        html.append("<input type=\"hidden\" name=\"acao\" value=\"").append(idEmEdicao != null ? "atualizar" : "criar").append("\">");
        if (idEmEdicao != null) {
            html.append("<input type=\"hidden\" name=\"id\" value=\"").append(idEmEdicao).append("\">");
        }
        html.append("<label>Turma: <select name=\"id_turma\" required>")
            .append(montarOpcoes("SELECT id, label FROM vw_autocomplete_turma ORDER BY label", idTurma))
            .append("</select></label>");
        html.append("<label>Disciplina: <select name=\"id_disciplina\" required>")
            .append(montarOpcoes("SELECT id_disciplina AS id, nome AS label FROM disciplina ORDER BY nome", idDisciplina))
            .append("</select></label>");
        html.append("<label>Professor: <select name=\"id_professor\" required>")
            .append(montarOpcoes("SELECT id_professor AS id, nome AS label FROM professor ORDER BY nome", idProfessor))
            .append("</select></label>");
        html.append("<label>Sala de Aula: <input name=\"sala_aula\" value=\"").append(salaAula).append("\" required></label>");
        html.append("<button type=\"submit\">Salvar</button>");
        html.append("</form>");

        html.append("<h2>Lista de Vinculos</h2>");
        html.append("<table><tr><th>Turma</th><th>Disciplina</th><th>Professor</th><th>Sala</th><th>Acoes</th></tr>");
        String sqlListagem = "SELECT td.id_turma_disciplina, t.sigla AS turma, d.nome AS disciplina, p.nome AS professor, td.sala_aula "
            + "FROM turma_disciplina td "
            + "JOIN turma t ON td.id_turma = t.id_turma "
            + "JOIN disciplina d ON td.id_disciplina = d.id_disciplina "
            + "JOIN professor p ON td.id_professor = p.id_professor "
            + "ORDER BY t.sigla, d.nome";
        try (Connection con = Banco.conectar();
             PreparedStatement stmt = con.prepareStatement(sqlListagem);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id_turma_disciplina");
                html.append("<tr>")
                    .append("<td>").append(rs.getString("turma")).append("</td>")
                    .append("<td>").append(rs.getString("disciplina")).append("</td>")
                    .append("<td>").append(rs.getString("professor")).append("</td>")
                    .append("<td>").append(Pagina.vazioSeNulo(rs.getString("sala_aula"))).append("</td>")
                    .append("<td>")
                    .append("<a href=\"/vinculos?acao=editar&id=").append(id).append("\">Editar</a> ")
                    .append("<a href=\"/vinculos?acao=excluir&id=").append(id).append("\">Excluir</a>")
                    .append("</td></tr>");
            }
        }
        html.append("</table>");
        return html.toString();
    }

    // Consulta o banco e monta as opcoes de um <select> (dropdown)
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

    // Liga uma disciplina e um professor a uma turma
    private void criar(Map<String, String> dados) throws SQLException {
        String sql = "INSERT INTO turma_disciplina (id_turma, id_disciplina, id_professor, sala_aula) VALUES (?, ?, ?, ?)";
        try (Connection con = Banco.conectar(); PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(dados.get("id_turma")));
            stmt.setInt(2, Integer.parseInt(dados.get("id_disciplina")));
            stmt.setInt(3, Integer.parseInt(dados.get("id_professor")));
            stmt.setString(4, dados.get("sala_aula"));
            stmt.executeUpdate();
        }
    }

    // Atualiza os dados de um vinculo existente
    private void atualizar(Map<String, String> dados) throws SQLException {
        String sql = "UPDATE turma_disciplina SET id_turma=?, id_disciplina=?, id_professor=?, sala_aula=? WHERE id_turma_disciplina=?";
        try (Connection con = Banco.conectar(); PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(dados.get("id_turma")));
            stmt.setInt(2, Integer.parseInt(dados.get("id_disciplina")));
            stmt.setInt(3, Integer.parseInt(dados.get("id_professor")));
            stmt.setString(4, dados.get("sala_aula"));
            stmt.setInt(5, Integer.parseInt(dados.get("id")));
            stmt.executeUpdate();
        }
    }

    // Exclui um vinculo e as avaliacoes e notas ligadas a ele
    private void excluir(String id) throws SQLException {
        try (Connection con = Banco.conectar();
             PreparedStatement stmt = con.prepareStatement("DELETE FROM turma_disciplina WHERE id_turma_disciplina=?")) {
            stmt.setInt(1, Integer.parseInt(id));
            stmt.executeUpdate();
        }
    }
}
