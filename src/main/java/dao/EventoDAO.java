package dao;

import model.CategoriaEvento;
import model.Evento;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) para a entidade Evento.
 * Gerencia a comunicação entre a aplicação e a tabela 'Eventos' no banco de dados.
 */
public class EventoDAO {

    /**
     * Cria um novo evento no banco de dados.
     * @param evento O objeto Evento a ser criado (ID é -1 no objeto, será gerado pelo DB).
     * @return O ID gerado para o novo evento ou -1 em caso de falha.
     */
    public int criarEvento(Evento evento) {
        String sql = "INSERT INTO Eventos (nome, categoria, data_hora, local, capacidade, organizador_id, descricao) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Uso dos getters do POJO
            stmt.setString(1, evento.nome());
            stmt.setString(2, evento.categoria().name()); // Salva o nome da enum
            stmt.setTimestamp(3, Timestamp.valueOf(evento.dataHora()));
            stmt.setString(4, evento.local());
            stmt.setInt(5, evento.capacidade());
            stmt.setInt(6, evento.organizadorId());
            stmt.setString(7, evento.descricao());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1); // Retorna o ID gerado
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao criar evento: " + e.getMessage());
        }
        return -1; // Falha na criação
    }

    /**
     * Busca um evento pelo seu ID.
     * @param id O ID do evento.
     * @return O objeto Evento, ou null se não for encontrado.
     */
    public Evento getEventoPorId(int id) {
        String sql = "SELECT * FROM Eventos WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEvento(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar evento por ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Lista todos os eventos.
     * @return Uma lista de objetos Evento.
     */
    public List<Evento> listarTodosEventos() {
        List<Evento> eventos = new ArrayList<>();
        // Ordena por data_hora (os mais próximos/futuros primeiro)
        String sql = "SELECT * FROM Eventos ORDER BY data_hora ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                eventos.add(mapResultSetToEvento(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar todos os eventos: " + e.getMessage());
        }
        return eventos;
    }

    /**
     * Lista todos os eventos organizados por um usuário específico.
     * @param organizadorId O ID do usuário organizador.
     * @return Uma lista de objetos Evento.
     */
    public List<Evento> listarEventosPorOrganizador(int organizadorId) {
        List<Evento> eventos = new ArrayList<>();
        String sql = "SELECT * FROM Eventos WHERE organizador_id = ? ORDER BY data_hora ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, organizadorId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    eventos.add(mapResultSetToEvento(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar eventos por organizador: " + e.getMessage());
        }
        return eventos;
    }

    /**
     * NOVO: Lista todos os eventos nos quais um usuário está inscrito.
     * @param userId O ID do usuário inscrito.
     * @return Uma lista de objetos Evento.
     */
    public List<Evento> listarEventosInscritosPorUsuario(int userId) {
        List<Evento> eventos = new ArrayList<>();
        // Query de junção entre Eventos e a tabela de participação
        String sql = "SELECT e.* FROM Eventos e " +
                "JOIN participacao p ON e.id = p.evento_id " +
                "WHERE p.usuario_id = ? " +
                "ORDER BY e.data_hora ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    eventos.add(mapResultSetToEvento(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar eventos inscritos por usuário: " + e.getMessage());
        }
        return eventos;
    }


    /**
     * Deleta um evento do banco de dados.
     * A deleção em cascata (ON DELETE CASCADE) na tabela 'participacao' garante
     * que todas as inscrições relacionadas a este evento sejam removidas.
     * @param id O ID do evento a ser deletado.
     * @return true se a deleção foi bem sucedida, false caso contrário.
     */
    public boolean deletarEvento(int id) {
        String sql = "DELETE FROM Eventos WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();

            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Erro ao deletar evento: " + e.getMessage());
        }
        return false;
    }


    /**
     * Mapeia um ResultSet para um objeto Evento, usando o construtor completo.
     * @param rs ResultSet contendo os dados do evento.
     * @return Objeto Evento populado.
     * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
     */
    private Evento mapResultSetToEvento(ResultSet rs) throws SQLException {
        // Uso do construtor completo do POJO para criar a instância.
        return new Evento(
                rs.getInt("id"),
                rs.getString("nome"),
                CategoriaEvento.valueOf(rs.getString("categoria")),
                rs.getTimestamp("data_hora").toLocalDateTime(),
                rs.getString("local"),
                rs.getInt("capacidade"),
                rs.getInt("organizador_id"),
                rs.getString("descricao")
        );
    }
}