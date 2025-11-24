package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;

/**
 * Utilitário para gerenciar a conexão com o banco de dados SQLite
 * e garantir que as tabelas necessárias existam e que a integridade
 * referencial (FOREIGN KEYs) esteja ativa.
 */
public class DatabaseConnection {
    // URL de conexão com o banco de dados SQLite. O arquivo será criado na pasta raiz do projeto.
    private static final String URL = "jdbc:sqlite:eventos.db";

    /**
     * Estabelece e retorna uma conexão com o banco de dados.
     * @return Objeto Connection.
     * @throws SQLException Se a conexão falhar ou o driver não estiver disponível.
     */
    public static Connection getConnection() throws SQLException {
        // Driver JDBC para SQLite é carregado automaticamente (Java 6+).
        return DriverManager.getConnection(URL);
    }

    /**
     * Inicializa o banco de dados, criando as tabelas se não existirem.
     * Garante que o esquema do banco de dados está sincronizado com as entidades (Models).
     */
    public static void initializeDatabase() {
        // Uso de try-with-resources para garantir o fechamento de Connection e Statement
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Ativa a integridade referencial no SQLite (deve ser a primeira coisa feita na conexão).
            // ESSENCIAL para que as FOREIGN KEYs funcionem, especialmente o ON DELETE CASCADE.
            stmt.execute("PRAGMA foreign_keys = ON;");

            // 1. Tabela USUARIO
            String sqlUsuario = "CREATE TABLE IF NOT EXISTS usuario (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "nome TEXT NOT NULL," +
                    "email TEXT NOT NULL UNIQUE," +
                    "senha TEXT NOT NULL," + // Senha em texto simples, para simplificar.
                    "is_admin BOOLEAN NOT NULL DEFAULT FALSE" +
                    ")";
            stmt.execute(sqlUsuario);

            // 2. Tabela EVENTOS
            String sqlEventos = "CREATE TABLE IF NOT EXISTS eventos (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "nome TEXT NOT NULL," +
                    "categoria TEXT NOT NULL," + // Salvo como string (TECNOLOGIA, CULTURA, etc.)
                    "data_hora TIMESTAMP NOT NULL," +
                    "local TEXT NOT NULL," +
                    "capacidade INTEGER NOT NULL," +
                    "organizador_id INTEGER NOT NULL," +
                    "descricao TEXT," +
                    // Chave estrangeira para o organizador. Se o usuário for deletado, seus eventos também são.
                    "FOREIGN KEY (organizador_id) REFERENCES usuario(id) ON DELETE CASCADE" +
                    ")";
            stmt.execute(sqlEventos);

            // 3. Tabela PARTICIPACAO (Tabela de Junção/Inscrição)
            String sqlParticipacao = "CREATE TABLE IF NOT EXISTS participacao (" +
                    "usuario_id INTEGER NOT NULL," +
                    "evento_id INTEGER NOT NULL," +
                    "PRIMARY KEY (usuario_id, evento_id)," +
                    // Se o usuário ou o evento for deletado, a inscrição deve ser removida.
                    "FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (evento_id) REFERENCES eventos(id) ON DELETE CASCADE" +
                    ")";
            stmt.execute(sqlParticipacao);

            // 4. Insere o admin inicial se não existir.
            insertInitialAdmin(conn);

        } catch (SQLException e) {
            System.err.println("Erro ao inicializar o banco de dados: " + e.getMessage());
        }
    }

    /**
     * Insere um usuário administrador inicial se a tabela estiver vazia (ou o admin não existir).
     * @param conn A conexão de banco de dados ativa.
     */
    private static void insertInitialAdmin(Connection conn) {
        // Verifica se o usuário admin padrão já existe.
        String countSql = "SELECT COUNT(*) FROM usuario WHERE email = 'admin@eventos.com'";
        String insertSql = "INSERT INTO usuario (nome, email, senha, is_admin) VALUES (?, ?, ?, ?)";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(countSql)) {

            // Se a contagem for 0, o admin não existe.
            if (rs.next() && rs.getInt(1) == 0) {
                // Senha em texto puro: 'admin123'
                try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                    pstmt.setString(1, "Admin Principal");
                    pstmt.setString(2, "admin@eventos.com");
                    pstmt.setString(3, "admin123");
                    pstmt.setBoolean(4, true); // TRUE para is_admin
                    pstmt.executeUpdate();
                    System.out.println("Usuário Admin padrão inserido: admin@eventos.com / admin123");
                }
            }
        } catch (SQLException e) {
            // Este erro não deve interromper a inicialização.
            System.err.println("Erro ao inserir admin inicial: " + e.getMessage());
        }
    }
}