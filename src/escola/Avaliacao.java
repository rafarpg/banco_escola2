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

public class Avaliacao implements HttpHandler {

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
                    Pagina.responder(exchange, Pagina.montar(exchange, "Avaliacoes", Pagina.erro(e.getMessage()) + montarCorpo(null)));
                    return;
                }
                Pagina.redirecionar(exchange, "/avaliacoes");
                return;
            }

            Map<String, String> parametros = Pagina.parametrosDaUrl(exchange);
            if ("excluir".equals(parametros.get("acao"))) {
                excluir(parametros.get("id"));
                Pagina.redirecionar(exchange, "/avaliacoes");
                return;
            }

            String idEmEdicao = "editar".equals(parametros.get("acao")) ? parametros.get("id") : null;
            Pagina.responder(exchange, Pagina.montar(exchange, "Avaliacoes", montarCorpo(idEmEdicao)));
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String montarCorpo(String idEmEdicao) throws SQLException {
        String idVinculo = "", titulo = "", peso = "1.00", notaMaxima = "10.00", dataRealizacao = "";

        if (idEmEdicao != null) {
            try (Connection con = Banco.conectar();
                 PreparedStatement stmt = con.prepareStatement("SELECT * FROM avaliacao WHERE id_avaliacao = ?")) {
                stmt.setInt(1, Integer.parseInt(idEmEdicao));
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        idVinculo = String.valueOf(rs.getInt("id_turma_disciplina"));
                        titulo = rs.getString("titulo");
                        peso = rs.getBigDecimal("peso").toString();
                        notaMaxima = rs.getBigDecimal("nota_maxima").toString();
                        dataRealizacao = rs.getDate("data_realizacao") != null ? rs.getDate("data_realizacao").toString() : "";
                    }
                }
            }
        }

        StringBuilder html = new StringBuilder();
        html.append("<h2>").append(idEmEdicao != null ? "Editar Avaliacao" : "Nova Avaliacao").append("</h2>");
        html.append("<form method=\"post\" action=\"/avaliacoes\">");
        html.append("<input type=\"hidden\" name=\"acao\" value=\"").append(idEmEdicao != null ? "atualizar" : "criar").append("\">");
        if (idEmEdicao != null) {
            html.append("<input type=\"hidden\" name=\"id\" value=\"").append(idEmEdicao).append("\">");
        }
        html.append("<label>Disciplina/Turma/Professor: <select name=\"id_turma_disciplina\" required>")
            .append(montarOpcoes("SELECT id, label FROM vw_autocomplete_vinculo_disciplina ORDER BY label", idVinculo))
            .append("</select></label>");
        html.append("<label>Titulo: <input name=\"titulo\" value=\"").append(titulo).append("\" required></label>");
        html.append("<label>Peso: <input type=\"number\" step=\"0.01\" name=\"peso\" value=\"").append(peso).append("\" required></label>");
        html.append("<label>Nota Maxima: <input type=\"number\" step=\"0.01\" name=\"nota_maxima\" value=\"").append(notaMaxima).append("\" required></label>");
        html.append("<label>Data de Realizacao: <input type=\"date\" name=\"data_realizacao\" value=\"").append(dataRealizacao).append("\" required></label>");
        html.append("<button type=\"submit\">Salvar</button>");
        html.append("</form>");

        html.append("<h2>Lista de Avaliacoes</h2>");
        html.append("<table><tr><th>Disciplina</th><th>Turma</th><th>Titulo</th><th>Peso</th><th>Nota Maxima</th><th>Data</th><th>Acoes</th></tr>");
        String sqlListagem = "SELECT av.id_avaliacao, d.nome AS disciplina, t.sigla AS turma, av.titulo, av.peso, av.nota_maxima, av.data_realizacao "
            + "FROM avaliacao av "
            + "JOIN turma_disciplina td ON av.id_turma_disciplina = td.id_turma_disciplina "
            + "JOIN disciplina d ON td.id_disciplina = d.id_disciplina "
            + "JOIN turma t ON td.id_turma = t.id_turma "
            + "ORDER BY av.data_realizacao DESC";
        try (Connection con = Banco.conectar();
             PreparedStatement stmt = con.prepareStatement(sqlListagem);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id_avaliacao");
                html.append("<tr>")
                    .append("<td>").append(rs.getString("disciplina")).append("</td>")
                    .append("<td>").append(rs.getString("turma")).append("</td>")
                    .append("<td>").append(rs.getString("titulo")).append("</td>")
                    .append("<td>").append(rs.getBigDecimal("peso")).append("</td>")
                    .append("<td>").append(rs.getBigDecimal("nota_maxima")).append("</td>")
                    .append("<td>").append(rs.getDate("data_realizacao")).append("</td>")
                    .append("<td>")
                    .append("<a href=\"/avaliacoes?acao=editar&id=").append(id).append("\">Editar</a> ")
                    .append("<a href=\"/avaliacoes?acao=excluir&id=").append(id).append("\">Excluir</a>")
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

    private void criar(Map<String, String> dados) throws SQLException {
        String sql = "INSERT INTO avaliacao (id_turma_disciplina, titulo, peso, nota_maxima, data_realizacao) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = Banco.conectar(); PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(dados.get("id_turma_disciplina")));
            stmt.setString(2, dados.get("titulo"));
            stmt.setBigDecimal(3, new java.math.BigDecimal(dados.get("peso")));
            stmt.setBigDecimal(4, new java.math.BigDecimal(dados.get("nota_maxima")));
            stmt.setString(5, dados.get("data_realizacao"));
            stmt.executeUpdate();
        }
    }

    private void atualizar(Map<String, String> dados) throws SQLException {
        String sql = "UPDATE avaliacao SET id_turma_disciplina=?, titulo=?, peso=?, nota_maxima=?, data_realizacao=? WHERE id_avaliacao=?";
        try (Connection con = Banco.conectar(); PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(dados.get("id_turma_disciplina")));
            stmt.setString(2, dados.get("titulo"));
            stmt.setBigDecimal(3, new java.math.BigDecimal(dados.get("peso")));
            stmt.setBigDecimal(4, new java.math.BigDecimal(dados.get("nota_maxima")));
            stmt.setString(5, dados.get("data_realizacao"));
            stmt.setInt(6, Integer.parseInt(dados.get("id")));
            stmt.executeUpdate();
        }
    }

    private void excluir(String id) throws SQLException {
        try (Connection con = Banco.conectar();
             PreparedStatement stmt = con.prepareStatement("DELETE FROM avaliacao WHERE id_avaliacao=?")) {
            stmt.setInt(1, Integer.parseInt(id));
            stmt.executeUpdate();
        }
    }
}
