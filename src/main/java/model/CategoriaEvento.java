package model;

/**
 * Define as categorias fixas e válidas para qualquer objeto Evento na aplicação.
 * O uso de ENUM garante tipagem forte e evita erros de strings.
 * Adiciona uma descrição amigável para exibição na View.
 */
public enum CategoriaEvento {
    // Definimos as categorias com uma descrição para facilitar a exibição.
    TECNOLOGIA("Tecnologia e Inovação"),
    CULTURA("Arte, Cultura e Lazer"),
    ESPORTES("Esportes e Competições"),
    ACADEMICO("Acadêmico e Científico"),
    OUTROS("Outros / Diversos");

    private final String descricao;

    CategoriaEvento(String descricao) {
        this.descricao = descricao;
    }

    /**
     * Retorna a descrição amigável da categoria para exibição.
     */
    public String getDescricao() {
        return descricao;
    }
}