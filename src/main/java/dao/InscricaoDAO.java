package dao;

import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Data Access Object (DAO) para a entidade de ligação Inscricao/Participacao.
 * Gerencia as inscrições e cancelamentos de usuários em eventos, utilizando a tabela 'participacao'.
 */
public class InscricaoDAO {

    /**
     * Insere um novo registro de inscrição. Corresponde ao `criarInscricao` no Controller.
     * @param usuarioId ID do usuário.
     * @param eventoId ID do evento.
     * @return true se inserido, false se duplicado ou erro.
     */
    public boolean criarInscricao(int usuarioId, int eventoId) {
        String sql = "INSERT INTO participacao (usuario_id, evento_id) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, usuarioId);
            stmt.setInt(2, eventoId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            // Em caso de erro (ex: chave duplicada se já estiver inscrito), retorna false.
            System.err.println("Erro ao criar inscrição (usuário ID " + usuarioId + ", evento ID " + eventoId + "): " + e.getMessage());
        }
        return false;
    }

    /**
     * Verifica se um usuário está inscrito em um evento.
     * @param usuarioId ID do usuário.
     * @param eventoId ID do evento.
     * @return true se inscrito, false caso contrário.
     */
    public boolean isInscrito(int usuarioId, int eventoId) {
        String sql = "SELECT COUNT(*) FROM participacao WHERE usuario_id = ? AND evento_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, usuarioId);
            stmt.setInt(2, eventoId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao verificar inscrição: " + e.getMessage());
        }
        return false;
    }

    /**
     * Remove um registro de participação (cancela a presença). Corresponde ao `cancelarPresenca` no Controller.
     * @param usuarioId ID do usuário.
     * @param eventoId ID do evento.
     * @return true se removido, false se não existir ou erro.
     */
    public boolean removerInscricao(int usuarioId, int eventoId) {
        String sql = "DELETE FROM participacao WHERE usuario_id = ? AND evento_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, usuarioId);
            stmt.setInt(2, eventoId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Erro ao remover inscrição: " + e.getMessage());
        }
        return false;
    }

    /**
     * Conta o número total de participantes inscritos em um evento.
     * @param eventoId ID do evento.
     * @return O número de participantes.
     */
    public int contarParticipantes(int eventoId) {
        String sql = "SELECT COUNT(*) FROM participacao WHERE evento_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, eventoId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao contar participantes: " + e.getMessage());
        }
        return 0;
    }


}