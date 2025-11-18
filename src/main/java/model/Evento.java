package model;

import java.time.LocalDateTime;
// Importa a classe da View para acessar o formatador de data estático
import view.SistemaEventosApp;

// Classe que representa a entidade Evento.
public class Evento {

    private int id;
    private String nome;
    private String endereco;
    private CategoriaEvento categoria;
    private LocalDateTime dataHora; // Usa o tipo moderno de data e hora
    private String descricao;

    // Construtor completo (usado ao carregar do BD)
    public Evento(int id, String nome, String endereco, CategoriaEvento categoria, LocalDateTime dataHora, String descricao) {
        this.id = id;
        this.nome = nome;
        this.endereco = endereco;
        this.categoria = categoria;
        this.dataHora = dataHora;
        this.descricao = descricao;
    }

    // Construtor sem ID (usado ao cadastrar novo evento)
    public Evento(String nome, String endereco, CategoriaEvento categoria, LocalDateTime dataHora, String descricao) {
        this.id = 0;
        this.nome = nome;
        this.endereco = endereco;
        this.categoria = categoria;
        this.dataHora = dataHora;
        this.descricao = descricao;
    }

    // Métodos Getters
    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getEndereco() { return endereco; }
    public CategoriaEvento getCategoria() { return categoria; }
    public LocalDateTime getDataHora() { return dataHora; }
    public String getDescricao() { return descricao; }

    @Override
    public String toString() {
        // Usa o formatador estático da View para garantir a formatação de data consistente
        return "ID: " + id +
                " | Nome: " + nome +
                " | Local: " + endereco +
                " | Categoria: " + categoria +
                " | Horário: " + dataHora.format(SistemaEventosApp.USER_FORMATTER_FOR_OUTPUT) +
                " | Descrição: " + descricao;
    }
}