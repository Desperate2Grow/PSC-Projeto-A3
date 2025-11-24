package model;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) usado na camada Controller para listar eventos
 * com informações adicionais, como o nome do organizador.
 * Este DTO melhora a comunicação de dados ao combinar Evento e informações extras,
 * evitando a delegação desnecessária de lógica para a View.
 * É um objeto imutável, garantindo que o estado não seja alterado durante a transferência.
 *
 * @param evento          Composição: Contém o objeto Evento original
 * @param nomeOrganizador Campo adicional que será buscado no Controller/Service.
 */
public record EventoComDetalhes(Evento evento, String nomeOrganizador) {
    /**
     * Construtor do DTO.
     *
     * @param evento          O objeto Evento base.
     * @param nomeOrganizador O nome do organizador do evento.
     */
    public EventoComDetalhes {
    }

    // --- Getters Delegados (acesso aos dados do evento) ---

    public int getId() {
        return evento.id();
    }

    public String getNome() {
        return evento.nome();
    }

    public CategoriaEvento getCategoria() {
        return evento.categoria();
    }

    public LocalDateTime getDataHora() {
        return evento.dataHora();
    }

    public int getCapacidade() {
        return evento.capacidade();
    }

    // --- Getter Próprio (Nome do Organizador) ---

    /**
     * Retorna o nome do organizador do evento.
     */
    @Override
    public String nomeOrganizador() {
        return nomeOrganizador;
    }

}