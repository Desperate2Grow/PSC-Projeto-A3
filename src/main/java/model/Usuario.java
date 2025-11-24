package model;

/**
 * Representa um usuário no sistema (participante, organizador ou administrador).
 * Implementada como um POJO imutável (todos os campos são final e não há setters)
 * para maior segurança, exceto pelo ID (que é final, sendo ajustado apenas via construtor).
 *
 * @param senha Armazenada como texto simples para simplificar (idealmente seria um hash)
 */
public record Usuario(int id, String nome, String email, String senha, boolean isAdmin) {

    /**
     * Construtor completo.
     *
     * @param id      O ID único do usuário.
     * @param nome    O nome completo do usuário.
     * @param email   O email único (usado para login).
     * @param senha   A senha do usuário (texto simples).
     * @param isAdmin Verdadeiro se o usuário for administrador.
     */
    public Usuario {
    }

    // --- Getters (Padrão POJO/Java) ---

    /**
     * Acessor no padrão Java para booleanos (método isXyz()).
     *
     * @return true se o usuário for administrador.
     */
    @Override
    public boolean isAdmin() {
        return isAdmin;
    }
}