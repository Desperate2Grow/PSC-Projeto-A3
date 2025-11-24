package dao;

import model.Usuario;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe de acesso a dados (DAO) para a entidade Usuario.
 * Responsável por todas as operações de banco de dados relacionadas a usuários.
 */
public class UsuarioDAO {

    /**
     * Tenta obter um usuário a partir de um ResultSet.
     * @param rs O ResultSet posicionado no registro do usuário.
     * @return Um objeto Usuario ou null em caso de erro.
     * @throws SQLException Se ocorrer um erro de SQL.
     */
    Usuario mapResultSetToUsuario(ResultSet rs) throws SQLException {
        // Mapeia diretamente para o construtor do POJO imutável (sem setters).
        int id = rs.getInt("id");
        String nome = rs.getString("nome");
        String email = rs.getString("email");
        String senha = rs.getString("senha");
        boolean isAdmin = rs.getBoolean("is_admin");

        return new Usuario(id, nome, email, senha, isAdmin);
    }

    /**
     * Insere um novo usuário no banco de dados.
     * @param usuario O objeto Usuario a ser criado (ID é -1 no objeto, será gerado pelo DB).
     * @return O ID gerado para o novo usuário ou -1 em caso de falha.
     */
    public int criarUsuario(Usuario usuario) {
        String sql = "INSERT INTO usuario (nome, email, senha, is_admin) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, usuario.nome());
            stmt.setString(2, usuario.email());
            stmt.setString(3, usuario.senha());
            stmt.setBoolean(4, usuario.isAdmin());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao criar usuário: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Obtém um usuário do banco de dados pelo ID.
     * @param id O ID do usuário.
     * @return O objeto Usuario correspondente ou null se não for encontrado.
     */
    public Usuario getUsuarioPorId(int id) {
        String sql = "SELECT * FROM usuario WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUsuario(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar usuário por ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Obtém um usuário do banco de dados pelo email (usado para login).
     * @param email O email do usuário.
     * @return O objeto Usuario correspondente ou null se não for encontrado.
     */
    public Usuario getUsuarioPorEmail(String email) {
        String sql = "SELECT * FROM usuario WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUsuario(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar usuário por email: " + e.getMessage());
        }
        return null;
    }

    /**
     * Lista todos os usuários presentes no banco de dados, ordenados pelo ID.
     * @return Uma lista de objetos Usuario. Retorna uma lista vazia em caso de erro.
     */
    public List<Usuario> listarTodosUsuarios() {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM usuario ORDER BY id";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                usuarios.add(mapResultSetToUsuario(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar todos os usuários: " + e.getMessage());
        }
        return usuarios;
    }

    /**
     * Deleta um usuário do banco de dados pelo ID.
     * A exclusão em cascata (ON DELETE CASCADE) garante que todos os registros
     * de 'participacao' e 'eventos' (organizados por ele) sejam removidos,
     * conforme configurado em DatabaseConnection.java.
     * @param id O ID do usuário a ser deletado.
     * @return true se a exclusão for bem-sucedida, false caso contrário.
     */
    public boolean deletarUsuario(int id) {
        String sql = "DELETE FROM usuario WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();

            // Retorna true se exatamente uma linha foi afetada.
            return affectedRows == 1;
        } catch (SQLException e) {
            System.err.println("Erro ao deletar usuário: " + e.getMessage());
            return false;
        }
    }

    public boolean atualizarStatusAdmin(boolean isAdmin) {
        return isAdmin;
    }
}