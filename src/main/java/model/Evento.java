package model;

import java.time.LocalDateTime;

/**
 * Classe de modelo (POJO) que representa um evento.
 * Implementada como um objeto imutável (campos final e sem setters)
 * para garantir a consistência dos dados após a criação ou recuperação.
 *
 * @param organizadorId ID do usuário que criou o evento
 */
public record Evento(int id, String nome, CategoriaEvento categoria, LocalDateTime dataHora, String local,
                     int capacidade, int organizadorId, String descricao) {
    /**
     * Construtor completo para mapeamento de resultados do banco de dados.
     *
     * @param id            O ID único do evento.
     * @param nome          O nome do evento.
     * @param categoria     A categoria do evento (usa o enum CategoriaEvento).
     * @param dataHora      A data e hora do evento.
     * @param local         O local onde o evento ocorrerá.
     * @param capacidade    O número máximo de participantes.
     * @param organizadorId O ID do usuário organizador.
     * @param descricao     A descrição detalhada do evento.
     */
    public Evento {
    }

    // --- Getters (Padrão POJO/Java) ---
}