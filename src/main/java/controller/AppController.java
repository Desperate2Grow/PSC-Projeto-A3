package controller;

import dao.EventoDAO;
import dao.InscricaoDAO; // Mantido o nome InscricaoDAO
import dao.UsuarioDAO;
import model.CategoriaEvento;
import model.Evento;
import model.EventoComDetalhes;
import model.Usuario;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AppController é a camada de lógica de negócios (Service Layer) que interage
 * com as DAOs (Data Access Objects) e manipula as regras de negócio e validações.
 */
public class AppController {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final EventoDAO eventoDAO = new EventoDAO();
    private final InscricaoDAO inscricaoDAO = new InscricaoDAO(); // Usa o novo DAO

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // --- UTILS ---

    /**
     * Converte um objeto Evento em um EventoComDetalhes (DTO) buscando o nome do organizador.
     * @param evento O objeto Evento base.
     * @return EventoComDetalhes com o nome do organizador, ou null se o organizador não for encontrado.
     */
    private EventoComDetalhes toEventoComDetalhes(Evento evento) {
        // Busca o usuário organizador pelo ID.
        Usuario organizador = usuarioDAO.getUsuarioPorId(evento.organizadorId());

        if (organizador != null) {
            return new EventoComDetalhes(evento, organizador.nome());
        }
        // Se o organizador não for encontrado, ainda retorna o evento com um nome padrão.
        // Isso não deve ocorrer se as FKs estiverem corretas.
        return new EventoComDetalhes(evento, "Organizador Desconhecido");
    }

    /**
     * Tenta converter uma string para um Enum CategoriaEvento.
     * @param categoriaStr A string de categoria.
     * @return A CategoriaEvento correspondente ou null.
     */
    private CategoriaEvento parseCategoria(String categoriaStr) {
        try {
            return CategoriaEvento.valueOf(categoriaStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // --- AUTENTICAÇÃO E USUÁRIOS ---

    public Usuario fazerLogin(String email, String senha) {
        if (email == null || senha == null || email.trim().isEmpty() || senha.trim().isEmpty()) {
            System.out.println("Erro: Email e senha são obrigatórios.");
            return null;
        }

        Usuario usuario = usuarioDAO.getUsuarioPorEmail(email);

        if (usuario != null && usuario.senha().equals(senha)) {
            return usuario;
        }
        return null;
    }

    public boolean criarNovaConta(String nome, String email, String senha) {
        // Validação simples
        if (nome == null || email == null || senha == null ||
                nome.trim().isEmpty() || email.trim().isEmpty() || senha.trim().isEmpty()) {
            System.out.println("Erro: Todos os campos (nome, email, senha) são obrigatórios.");
            return false;
        }

        if (usuarioDAO.getUsuarioPorEmail(email) != null) {
            System.out.println("Erro: O email " + email + " já está em uso. Tente fazer login.");
            return false;
        }

        // Cria o novo usuário (não-administrador por padrão)
        // O ID é -1 no POJO, será gerado no DAO.
        Usuario novoUsuario = new Usuario(-1, nome, email, senha, false);

        // Delegação para o DAO.
        return usuarioDAO.criarUsuario(novoUsuario) != -1;
    }

    /**
     * Deleta a conta de um usuário (apenas o próprio ou um Admin pode deletar).
     * O Admin tem regras adicionais.
     * @param userIdLogado O ID do usuário logado que está executando a ação.
     * @param idParaDeletar O ID do usuário alvo da deleção.
     * @return true se a deleção foi bem sucedida.
     */
    public boolean deletarConta(int userIdLogado, int idParaDeletar) {
        boolean isSelfDeletion = userIdLogado == idParaDeletar;

        // 1. Verificar a permissão
        Usuario usuarioLogado = usuarioDAO.getUsuarioPorId(userIdLogado);
        if (usuarioLogado == null) {
            System.out.println("Erro: Usuário logado não encontrado.");
            return false;
        }

        // Se não for auto-deleção E o logado não for admin, nega.
        if (!isSelfDeletion && !usuarioLogado.isAdmin()) {
            System.out.println("Erro: Você não tem permissão para deletar a conta de outro usuário.");
            return false;
        }

        // Não permite que o admin se auto-delete (para garantir que sempre haja um admin).
        if (isSelfDeletion && usuarioLogado.isAdmin()) {
            List<Usuario> admins = usuarioDAO.listarTodosUsuarios().stream()
                    .filter(Usuario::isAdmin)
                    .toList();

            if (admins.size() == 1 && admins.get(0).id() == userIdLogado) {
                System.out.println("Erro: Você é o único administrador. Não pode deletar sua própria conta.");
                return false;
            }
        }

        // 1. Limpa todas as participações (usando o método do InscricaoDAO)
        // A deleção em cascata (FOREIGN KEY ON DELETE CASCADE) no banco de dados já cuidaria disso,
        // mas é bom ter o método no DAO para clareza da operação.
        // inscricaoDAO.deletarInscricoesPorUsuario(idParaDeletar); // Comentado, pois a FK fará isso.

        // 2. Transfere a organização de eventos ou deixa a FK fazer a deleção em cascata
        // Como o `DatabaseConnection` está configurado com `ON DELETE CASCADE` para o organizador_id,
        // todos os eventos organizados por este usuário serão deletados automaticamente ao deletar o usuário,
        // o que é um comportamento aceitável para um MVP.

        // 3. Deleta o usuário.
        boolean sucesso = usuarioDAO.deletarUsuario(idParaDeletar);

        if (!sucesso) {
            System.out.println("Erro: Falha no processo de deleção do usuário.");
        }

        return sucesso;
    }

    // --- EVENTOS ---

    public List<EventoComDetalhes> listarTodosEventosComDetalhes() {
        List<Evento> eventos = eventoDAO.listarTodosEventos();
        List<EventoComDetalhes> detalhesList = new ArrayList<>();

        for (Evento evento : eventos) {
            EventoComDetalhes dto = toEventoComDetalhes(evento);
            detalhesList.add(dto);
        }
        return detalhesList;
    }

    public List<EventoComDetalhes> listarEventosOrganizados(int organizadorId) {
        List<Evento> eventos = eventoDAO.listarEventosPorOrganizador(organizadorId);
        List<EventoComDetalhes> detalhesList = new ArrayList<>();

        for (Evento evento : eventos) {
            // Reutiliza toEventoComDetalhes. O nome do organizador será o do usuário logado.
            EventoComDetalhes dto = toEventoComDetalhes(evento);
            detalhesList.add(dto);
        }
        return detalhesList;
    }

    public int criarNovoEvento(int organizadorId, String nome, String categoriaStr, String dataHoraStr, String local, int capacidade, String descricao) {
        // 1. Validação de formato da Categoria
        CategoriaEvento categoria = parseCategoria(categoriaStr);
        if (categoria == null) {
            System.out.println("Erro: Categoria '" + categoriaStr + "' inválida. Use uma das opções: " +
                    Arrays.stream(CategoriaEvento.values()).map(CategoriaEvento::name).collect(Collectors.joining(", ")));
            return -1;
        }

        // 2. Validação de formato de Data e Hora
        LocalDateTime dataHora;
        try {
            dataHora = LocalDateTime.parse(dataHoraStr, FORMATTER);
        } catch (DateTimeParseException e) {
            System.out.println("Erro: Formato de data e hora inválido. Use o padrão 'dd/MM/yyyy HH:mm'.");
            return -1;
        }

        // 3. Validação de Negócio: Data no futuro
        if (dataHora.isBefore(LocalDateTime.now())) {
            System.out.println("Erro: Não é possível criar eventos que já ocorreram.");
            return -1;
        }

        // 4. Validação de Capacidade
        if (capacidade <= 0) {
            System.out.println("Erro: A capacidade deve ser um número positivo.");
            return -1;
        }

        // 5. Validação de campos obrigatórios
        if (nome.trim().isEmpty() || local.trim().isEmpty() || descricao.trim().isEmpty()) {
            System.out.println("Erro: Nome, Local e Descrição do evento são obrigatórios.");
            return -1;
        }

        // Cria o objeto Evento
        Evento novoEvento = new Evento(-1, nome, categoria, dataHora, local, capacidade, organizadorId, descricao);

        // Delegação para o DAO
        return eventoDAO.criarEvento(novoEvento);
    }

    public boolean deletarEvento(int eventoId, int userIdLogado) {
        // 1. Buscar o evento para validação de posse
        Evento evento = eventoDAO.getEventoPorId(eventoId);

        if (evento == null) {
            System.out.println("Erro: Evento ID " + eventoId + " não encontrado.");
            return false;
        }

        // 2. Verificar permissão: Somente o organizador ou um Admin pode deletar.
        Usuario usuarioLogado = usuarioDAO.getUsuarioPorId(userIdLogado);
        if (usuarioLogado == null) {
            System.out.println("Erro: Usuário logado não encontrado.");
            return false;
        }

        if (evento.organizadorId() != userIdLogado && !usuarioLogado.isAdmin()) {
            System.out.println("Erro: Você não tem permissão para deletar este evento.");
            return false;
        }

        // 3. Deletar inscrições (Opcional, pois a FK deve fazer isso - boa prática para clareza)
        // inscricaoDAO.deletarInscricoesPorEvento(eventoId); // Comentado, pois a FK fará isso.

        // 4. Delegação para o DAO (deletar o evento)
        return eventoDAO.deletarEvento(eventoId);
    }


    // --- INSCRIÇÕES ---

    /**
     * Tenta inscrever um usuário em um evento, aplicando as regras de negócio.
     * @param userId ID do usuário.
     * @param eventoId ID do evento.
     * @return true se a inscrição foi bem sucedida.
     */
    public boolean inscreverEmEvento(int userId, int eventoId) {
        Evento evento = eventoDAO.getEventoPorId(eventoId);

        if (evento == null) {
            System.out.println("Erro: Evento ID " + eventoId + " não encontrado.");
            return false;
        }

        // Regra de Negócio: Não pode se inscrever em eventos que já ocorreram.
        if (evento.dataHora().isBefore(LocalDateTime.now())) {
            System.out.println("Erro: Não é possível se inscrever em eventos que já ocorreram.");
            return false;
        }

        // Regra de Negócio: Não pode se inscrever no próprio evento.
        if (evento.organizadorId() == userId) { // USO: evento.getOrganizadorId()
            System.out.println("Erro: O organizador não precisa se inscrever no próprio evento.");
            return false;
        }

        // Regra de Negócio: Não pode se inscrever se já estiver inscrito.
        if (inscricaoDAO.isInscrito(userId, eventoId)) {
            System.out.println("Erro: Você já está inscrito neste evento.");
            return false;
        }

        // Regra de Negócio: Capacidade máxima atingida.
        if (inscricaoDAO.contarParticipantes(eventoId) >= evento.capacidade()) { // USO: evento.getCapacidade()
            System.out.println("Erro: O evento ID " + eventoId + " atingiu sua capacidade máxima.");
            return false;
        }

        // Delegação para o DAO (criarInscricao na nova InscricaoDAO).
        return inscricaoDAO.criarInscricao(userId, eventoId);
    }

    /**
     * Tenta cancelar a inscrição de um usuário em um evento, aplicando as regras de negócio.
     * @param userId ID do usuário.
     * @param eventoId ID do evento.
     * @return true se o cancelamento foi bem sucedido.
     */
    public boolean cancelarPresenca(int userId, int eventoId) {
        if (!inscricaoDAO.isInscrito(userId, eventoId)) {
            System.out.println("Erro: Você não está inscrito neste evento para poder cancelar.");
            return false;
        }

        // Delegação para o DAO (removerInscricao na nova InscricaoDAO).
        return inscricaoDAO.removerInscricao(userId, eventoId);
    }

    /**
     * Lista todos os eventos em que um usuário está inscrito.
     * @param userId ID do usuário.
     * @return Uma lista de EventoComDetalhes.
     */
    public List<EventoComDetalhes> listarInscricoesDoUsuario(int userId) {
        // O DAO de Evento precisa de um método para buscar eventos por ID de usuário inscrito.
        List<Evento> eventosInscritos = eventoDAO.listarEventosInscritosPorUsuario(userId);
        List<EventoComDetalhes> detalhesList = new ArrayList<>();

        for (Evento evento : eventosInscritos) {
            EventoComDetalhes dto = toEventoComDetalhes(evento);
            detalhesList.add(dto);
        }
        return detalhesList;
    }

    // --- ADMIN ---

    /**
     * Lista todos os usuários do sistema.
     * @return Uma lista de todos os objetos Usuario.
     */
    public List<Usuario> listarTodosUsuarios() {
        return usuarioDAO.listarTodosUsuarios();
    }

    /**
     * Tenta promover/despromover um usuário a admin (apenas para admins logados).
     * @param userIdLogado ID do admin que faz a ação.
     * @param userIdAlvo ID do usuário a ser alterado.
     * @param isAdmin Novo status de admin.
     * @return true se a alteração foi bem sucedida.
     */
    public boolean toggleAdminStatus(int userIdLogado, int userIdAlvo, boolean isAdmin) {
        Usuario usuarioLogado = usuarioDAO.getUsuarioPorId(userIdLogado);

        if (usuarioLogado == null || !usuarioLogado.isAdmin()) {
            System.out.println("Erro: Apenas administradores podem alterar o status de admin.");
            return false;
        }

        if (userIdLogado == userIdAlvo) {
            System.out.println("Erro: Administradores não podem alterar o próprio status.");
            return false;
        }

        // Regra de Negócio: Não permite remover o último admin.
        if (!isAdmin) {
            List<Usuario> admins = usuarioDAO.listarTodosUsuarios().stream()
                    .filter(Usuario::isAdmin)
                    .toList();

            // Se for o penúltimo admin e estiver tentando despromover
            if (admins.size() == 2 && admins.stream().anyMatch(u -> u.id() == userIdAlvo)) {
                System.out.println("Erro: Não é possível despromover, pois restaria apenas um administrador.");
                return false;
            }
        }

        return usuarioDAO.atualizarStatusAdmin(isAdmin);
    }

}