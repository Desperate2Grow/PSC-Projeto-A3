package controller;

import dao.EventoDAO;
import model.CategoriaEvento;
import model.Evento;
import model.Usuario;
import java.time.LocalDateTime;
import java.util.List;

// O Controller atua como intermediário entre a View e o DAO, gerenciando a lógica de negócio e o estado de login.
public class EventoController {

    private final EventoDAO eventoDAO;

    // O usuário logado, mutável após o login.
    private Usuario usuarioLogado;

    // Usuário de teste fixo para fallback e inicialização.
    public final Usuario USUARIO_TESTE = new Usuario(1, "João Teste", "joao.teste@app.com", "1234");

    // Construtor
    public EventoController() {
        this.eventoDAO = new EventoDAO();
        this.eventoDAO.inicializarBD();

        // Define o usuário de teste como padrão ao iniciar.
        this.usuarioLogado = USUARIO_TESTE;

        System.out.println("-> [Controller] Dados de inicialização carregados.");
    }

    // Método que retorna o usuário atualmente logado (GETTER)
    public Usuario getUsuarioLogado() {
        return this.usuarioLogado;
    }

    // Lógica de login
    public boolean fazerLogin(String email, String senha) {
        Usuario usuario = eventoDAO.buscarUsuarioPorEmail(email);

        if (usuario != null && usuario.getSenha().equals(senha)) {
            this.usuarioLogado = usuario; // Define o novo usuário logado
            return true;
        }
        return false;
    }

    // Cadastra um evento.
    public void cadastrarEvento(String nome, String endereco, CategoriaEvento categoria,
                                LocalDateTime dataHora, String descricao) {
        Evento evento = new Evento(0, nome, endereco, categoria, dataHora, descricao);
        eventoDAO.inserirEvento(evento);
    }

    // Lista todos os eventos.
    public List<Evento> listarTodosEventos() {
        return eventoDAO.listarTodosEventos();
    }

    // Cadastra um usuário.
    public void cadastrarUsuario(Usuario usuario) {
        this.eventoDAO.inserirUsuario(usuario);
    }

    // Confirma a presença (usa o usuário logado interno).
    public void confirmarPresenca(int eventoId) {
        this.eventoDAO.confirmarParticipacao(eventoId, this.usuarioLogado);
    }

    // Cancela a participação (usa o ID do usuário logado interno).
    public void cancelarParticipacao(int eventoId) {
        if (this.usuarioLogado.getId() <= 0) {
            System.err.println("-> [Controller] Usuário inválido para cancelar participação.");
            return;
        }
        eventoDAO.cancelarParticipacao(eventoId, this.usuarioLogado.getId());
    }

    // Remove o evento.
    public void removerEvento(int eventoId) {
        eventoDAO.deletarEvento(eventoId);
    }

    // 🆕 CORRIGIDO: Deleta a conta do usuário logado.
    public void desinscreverUsuario() {
        if (this.usuarioLogado.getId() <= 0) {
            System.err.println("-> [Controller] Não é possível desinscrever um usuário sem ID.");
            return;
        }
        // Chamada ao DAO
        eventoDAO.deletarUsuario(this.usuarioLogado.getId());

        // Retorna o estado de login para o usuário de teste após a exclusão
        this.usuarioLogado = USUARIO_TESTE;
    }
}