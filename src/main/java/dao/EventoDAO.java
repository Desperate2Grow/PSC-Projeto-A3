package dao;

import model.Evento;
import model.CategoriaEvento;
import model.Usuario;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

// Data Access Object (DAO) responsável pela comunicação direta com o banco de dados SQLite.
public class EventoDAO {

    // Formatador auxiliar para lidar com formatos de data SQL legados ou padrão
    private static final DateTimeFormatter OLD_SQL_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Método que estabelece a conexão com o banco de dados (database/banco.db)
    private Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:sqlite:database/banco.db");
        // SOLUÇÃO CRUCIAL CONTRA [SQLITE_BUSY]: Força o commit automático para liberar o lock imediatamente.
        conn.setAutoCommit(true);
        return conn;
    }

    // Cria as tabelas se elas ainda não existirem.
    public void inicializarBD() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Criação da tabela Eventos
            stmt.execute("CREATE TABLE IF NOT EXISTS Eventos (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "nome TEXT NOT NULL, " +
                    "endereco TEXT NOT NULL, " +
                    "categoria TEXT NOT NULL, " +
                    "horario TEXT NOT NULL, " +
                    "descricao TEXT)");

            // Tabela Usuarios (email com restrição UNIQUE)
            stmt.execute("CREATE TABLE IF NOT EXISTS Usuarios (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "nome TEXT NOT NULL, " +
                    "email TEXT NOT NULL UNIQUE, " +
                    "senha TEXT NOT NULL)");

            // Tabela N:M para participação
            stmt.execute("CREATE TABLE IF NOT EXISTS Participacao (" +
                    "usuario_id INTEGER NOT NULL, " +
                    "evento_id INTEGER NOT NULL, " +
                    "PRIMARY KEY (usuario_id, evento_id)," +
                    "FOREIGN KEY (usuario_id) REFERENCES Usuarios(id)," +
                    "FOREIGN KEY (evento_id) REFERENCES Eventos(id))");

            System.out.println("-> [DAO] Estrutura do BD verificada/criada com sucesso.");

        } catch (SQLException e) {
            System.err.println("Erro ao inicializar o DAO e as tabelas: " + e.getMessage());
        }
    }

    // Insere um novo Evento
    public void inserirEvento(Evento evento) {
        String sql = "INSERT INTO Eventos (nome, endereco, categoria, horario, descricao) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, evento.getNome());
            pstmt.setString(2, evento.getEndereco());
            pstmt.setString(3, evento.getCategoria().toString());
            pstmt.setString(4, evento.getDataHora().toString());
            pstmt.setString(5, evento.getDescricao());

            pstmt.executeUpdate();
            System.out.println("-> [DAO] Evento cadastrado com sucesso!");

        } catch (SQLException e) {
            System.err.println("Erro ao salvar evento no BD: " + e.getMessage());
        }
    }

    // Lista todos os eventos
    public List<Evento> listarTodosEventos() {
        List<Evento> eventos = new ArrayList<>();
        String sql = "SELECT * FROM Eventos";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {

                String horarioString = rs.getString("horario");
                LocalDateTime dataHora;

                // Trata possíveis formatos de data existentes no banco
                try {
                    dataHora = LocalDateTime.parse(horarioString, OLD_SQL_FORMATTER);
                } catch (DateTimeParseException e) {
                    dataHora = LocalDateTime.parse(horarioString);
                }

                Evento evento = new Evento(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getString("endereco"),
                        CategoriaEvento.valueOf(rs.getString("categoria")),
                        dataHora,
                        rs.getString("descricao")
                );
                eventos.add(evento);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao carregar eventos do BD: " + e.getMessage());
        }
        return eventos;
    }

    // Insere um novo Usuário
    public void inserirUsuario(Usuario usuario) {
        String sql = "INSERT INTO Usuarios (nome, email, senha) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, usuario.getNome());
            pstmt.setString(2, usuario.getEmail());
            pstmt.setString(3, usuario.getSenha());
            pstmt.executeUpdate();
            System.out.println("-> [DAO] Usuário cadastrado com sucesso!");
        } catch (SQLException e) {
            System.err.println("Erro ao salvar usuário no BD: " + e.getMessage());
        }
    }

    // Busca um usuário pelo email (essencial para a função de login)
    public Usuario buscarUsuarioPorEmail(String email) {
        String sql = "SELECT id, nome, email, senha FROM Usuarios WHERE email = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Usuário encontrado
                    return new Usuario(
                            rs.getInt("id"),
                            rs.getString("nome"),
                            rs.getString("email"),
                            rs.getString("senha")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar usuário por email: " + e.getMessage());
        }
        return null; // Retorna null se não encontrar
    }

    // Registra a participação
    public void confirmarParticipacao(int eventoId, Usuario usuario) {
        String sql = "INSERT INTO Participacao (usuario_id, evento_id) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, usuario.getId());
            pstmt.setInt(2, eventoId);
            pstmt.executeUpdate();
            System.out.println("-> [DAO] Participação confirmada para Evento ID: " + eventoId + " pelo usuário " + usuario.getNome());

        } catch (SQLException e) {
            System.err.println("Erro ao confirmar participação: " + e.getMessage());
        }
    }

    // Cancela a participação
    public void cancelarParticipacao(int eventoId, int usuarioId) {
        String sql = "DELETE FROM Participacao WHERE usuario_id = ? AND evento_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, usuarioId);
            pstmt.setInt(2, eventoId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("-> [DAO] Participação cancelada com sucesso!");
            } else {
                System.out.println("-> [DAO] Participação não encontrada ou já cancelada.");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao cancelar participação: " + e.getMessage());
        }
    }

    // Exclusão de Evento (remove participações e o evento)
    public void deletarEvento(int eventoId) {
        // 1. Excluir todas as participações primeiro (chave estrangeira)
        String sqlDeleteParticipacao = "DELETE FROM Participacao WHERE evento_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmtParticipacao = conn.prepareStatement(sqlDeleteParticipacao)) {

            pstmtParticipacao.setInt(1, eventoId);
            pstmtParticipacao.executeUpdate();
            System.out.println("-> [DAO] Participações associadas ao Evento ID " + eventoId + " excluídas.");
        } catch (SQLException e) {
            System.err.println("Erro ao excluir participações do evento: " + e.getMessage());
            return;
        }

        // 2. Excluir o Evento
        String sqlDeleteEvento = "DELETE FROM Eventos WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmtEvento = conn.prepareStatement(sqlDeleteEvento)) {

            pstmtEvento.setInt(1, eventoId);
            int rowsAffected = pstmtEvento.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ [DAO] Evento ID " + eventoId + " excluído com sucesso!");
            } else {
                System.out.println("-> [DAO] Evento ID " + eventoId + " não encontrado.");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao deletar evento: " + e.getMessage());
        }
    }

    // Exclusão de Usuário (remove participações e o usuário)
    public void deletarUsuario(int usuarioId) {
        // 1. Excluir todas as participações
        String sqlDeleteParticipacao = "DELETE FROM Participacao WHERE usuario_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmtParticipacao = conn.prepareStatement(sqlDeleteParticipacao)) {

            pstmtParticipacao.setInt(1, usuarioId);
            pstmtParticipacao.executeUpdate();
            System.out.println("-> [DAO] Participações do usuário ID " + usuarioId + " excluídas.");
        } catch (SQLException e) {
            System.err.println("Erro ao excluir participações do usuário: " + e.getMessage());
            return;
        }

        // 2. Excluir o usuário
        String sqlDeleteUsuario = "DELETE FROM Usuarios WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmtUsuario = conn.prepareStatement(sqlDeleteUsuario)) {

            pstmtUsuario.setInt(1, usuarioId);
            int rowsAffected = pstmtUsuario.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("-> [DAO] Usuário ID " + usuarioId + " excluído com sucesso!");
            } else {
                System.out.println("-> [DAO] Usuário ID " + usuarioId + " não encontrado.");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao deletar usuário: " + e.getMessage());
        }
    }
}