package model;

// Classe que representa a entidade Usuario no sistema.
public class Usuario {

    // Atributos privados do usuário
    private int id;
    private String nome;
    private String email;
    private String senha;

    // Construtor completo, usado geralmente ao recuperar dados do BD
    public Usuario(int id, String nome, String email, String senha) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
    }

    // Construtor sem ID, usado ao criar um novo usuário (o BD gera o ID)
    public Usuario(String nome, String email, String senha) {
        this.id = 0;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
    }

    // Métodos Getters (Acesso de leitura aos atributos)
    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getSenha() { return senha; }

    // Representação do objeto em string
    @Override
    public String toString() {
        return "Usuário: " + nome + " (" + email + "), ID: " + id;
    }
}