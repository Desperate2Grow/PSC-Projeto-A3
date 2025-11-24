package view;

import controller.AppController;
import model.CategoriaEvento;
import model.EventoComDetalhes;
import model.Usuario;
import util.DatabaseConnection;

import java.time.format.DateTimeFormatter;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

/**
 * Classe principal da aplicação (View) que lida com a interface de console
 * e delega toda a lógica de negócio e acesso a dados para o AppController.
 */
public class SistemaEventosApp {

    // Instância do Controller para acesso a toda a lógica.
    private static final AppController controller = new AppController();
    private static final Scanner scanner = new Scanner(System.in);
    // Variável de estado que armazena o usuário logado, controlando a sessão.
    private static Usuario usuarioLogado = null;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");


    /**
     * Método principal da aplicação (Ponto de entrada).
     */
   public static void main(String[] ignoredArgs) {
        // Garante que o banco de dados e as tabelas sejam criados e que o
        // usuário administrador padrão seja inserido.
        DatabaseConnection.initializeDatabase();

        try {
            // Inicia o fluxo de autenticação/menu principal.
            exibirMenuLogin();
        } finally {
            // Garante que o Scanner seja fechado ao encerrar a aplicação.
            scanner.close();
        }
    }

    /**
     * Exibe o menu de login e gerencia a autenticação.
     */
    private static void exibirMenuLogin() {
        System.out.println("==================================================");
        System.out.println("  Bem-vindo ao Sistema de Gestão de Eventos!");
        System.out.println("==================================================");

        while (usuarioLogado == null) {
            System.out.println("\n--- Menu de Acesso ---");
            System.out.println("1. Fazer Login");
            System.out.println("2. Criar Nova Conta");
            System.out.println("0. Sair");
            System.out.print("Escolha uma opção: ");

            int opcao = lerOpcao();

            switch (opcao) {
                case 1:
                    fazerLogin();
                    break;
                case 2:
                    criarNovaConta();
                    break;
                case 0:
                    System.out.println("Encerrando a aplicação. Até logo!");
                    return;
                default:
                    System.out.println("Opção inválida. Tente novamente.");
            }

            if (usuarioLogado != null) {
                // Se o login foi bem-sucedido, exibe o menu principal/admin
                if (usuarioLogado.isAdmin()) {
                    exibirMenuAdmin();
                } else {
                    exibirMenuPrincipal();
                }
            }
        }
    }

    private static void fazerLogin() {
        System.out.println("\n--- Login ---");
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Senha: ");
        String senha = scanner.nextLine();

        usuarioLogado = controller.fazerLogin(email, senha);

        if (usuarioLogado != null) {
            System.out.println("\nLogin bem-sucedido! Bem-vindo(a), " + usuarioLogado.nome() + ".");
        } else {
            System.out.println("\nFalha no login. Verifique seu email e senha.");
        }
    }

    private static void criarNovaConta() {
        System.out.println("\n--- Criar Nova Conta ---");
        System.out.print("Nome: ");
        String nome = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Senha: ");
        String senha = scanner.nextLine();

        if (controller.criarNovaConta(nome, email, senha)) {
            System.out.println("\nConta criada com sucesso! Você já pode fazer login.");
        }
    }

    // --- MENUS DE USUÁRIO COMUM ---

    private static void exibirMenuPrincipal() {
        while (usuarioLogado != null && !usuarioLogado.isAdmin()) {
            System.out.println("\n==================================================");
            System.out.println("  Menu Principal (Usuário: " + usuarioLogado.nome() + " - ID: " + usuarioLogado.id() + ")");
            System.out.println("==================================================");
            System.out.println("1. Visualizar Eventos Disponíveis");
            System.out.println("2. Inscrever-se em Evento");
            System.out.println("3. Meus Eventos Inscritos"); // Novo método de listagem
            System.out.println("4. Cancelar Inscrição");
            System.out.println("5. Meus Eventos Organizados");
            System.out.println("6. Criar Novo Evento");
            System.out.println("7. Deletar Evento (Apenas os que você organizou)");
            System.out.println("8. Deletar Minha Conta");
            System.out.println("0. Fazer Logout");
            System.out.print("Escolha uma opção: ");

            int opcao = lerOpcao();

            switch (opcao) {
                case 1:
                    listarEventosDisponiveis(controller.listarTodosEventosComDetalhes());
                    break;
                case 2:
                    inscreverEmEvento();
                    break;
                case 3:
                    listarMeusEventosInscritos(); // Nova chamada
                    break;
                case 4:
                    cancelarInscricao();
                    break;
                case 5:
                    listarEventosOrganizados();
                    break;
                case 6:
                    criarEvento();
                    break;
                case 7:
                    deletarEvento();
                    break;
                case 8:
                    deletarConta(usuarioLogado.id());
                    break;
                case 0:
                    fazerLogout();
                    break;
                default:
                    System.out.println("Opção inválida. Tente novamente.");
            }
        }
    }

    private static void listarMeusEventosInscritos() {
        System.out.println("\n--- Meus Eventos Inscritos ---");
        // Uso do getter POJO: getId()
        List<EventoComDetalhes> eventos = controller.listarInscricoesDoUsuario(usuarioLogado.id());
        listarEventosDisponiveis(eventos);
        System.out.println("\nTotal de eventos inscritos: " + eventos.size());
    }

    private static void inscreverEmEvento() {
        listarEventosDisponiveis(controller.listarTodosEventosComDetalhes());

        System.out.print("\nDigite o ID do evento para se inscrever: ");
        int eventoId = lerOpcao();

        if (eventoId != -1) {
            // Uso do getter POJO: getId()
            if (controller.inscreverEmEvento(usuarioLogado.id(), eventoId)) {
                System.out.println("\nInscrição realizada com sucesso no evento ID " + eventoId + "!");
            } else {
                // O Controller já exibe a mensagem de erro específica.
                System.out.println("Falha na inscrição. Verifique as regras do evento.");
            }
        }
    }

    private static void cancelarInscricao() {
        listarMeusEventosInscritos(); // Lista eventos inscritos

        System.out.print("\nDigite o ID do evento para CANCELAR a inscrição: ");
        int eventoId = lerOpcao();

        if (eventoId != -1) {
            // Uso do getter POJO: getId()
            if (controller.cancelarPresenca(usuarioLogado.id(), eventoId)) {
                System.out.println("\nInscrição cancelada com sucesso no evento ID " + eventoId + ".");
            } else {
                // O Controller já exibe a mensagem de erro específica.
                System.out.println("Falha ao cancelar inscrição.");
            }
        }
    }

    private static void listarEventosOrganizados() {
        System.out.println("\n--- Meus Eventos Organizados ---");
        // Uso do getter POJO: getId()
        List<EventoComDetalhes> eventos = controller.listarEventosOrganizados(usuarioLogado.id());

        if (eventos.isEmpty()) {
            System.out.println("Você ainda não organizou nenhum evento.");
            return;
        }

        listarEventosDisponiveis(eventos);
    }

    private static void criarEvento() {
        System.out.println("\n--- Criar Novo Evento ---");
        System.out.print("Nome do Evento: ");
        String nome = scanner.nextLine();

        System.out.println("Categorias disponíveis: ");
        for (CategoriaEvento cat : CategoriaEvento.values()) {
            System.out.printf(" - %s (%s)\n", cat.name(), cat.getDescricao());
        }
        System.out.print("Categoria (Ex: TECNOLOGIA): ");
        String categoriaStr = scanner.nextLine();

        System.out.print("Data e Hora (Formato DD/MM/AAAA HH:MM): ");
        String dataHoraStr = scanner.nextLine();

        System.out.print("Local: ");
        String local = scanner.nextLine();

        System.out.print("Capacidade Máxima: ");
        int capacidade = lerOpcao();
        if (capacidade == -1) return;

        System.out.print("Descrição do Evento: ");
        String descricao = scanner.nextLine();

        // Uso do getter POJO: getId()
        int novoId = controller.criarNovoEvento(usuarioLogado.id(), nome, categoriaStr, dataHoraStr, local, capacidade, descricao);

        if (novoId != -1) {
            System.out.println("\nEvento criado com sucesso! ID: " + novoId);
        } else {
            // O Controller já exibe a mensagem de erro específica.
            System.out.println("Falha ao criar evento.");
        }
    }

    private static void deletarEvento() {
        listarEventosOrganizados(); // Lista eventos para que o usuário possa escolher o ID.

        System.out.print("\nDigite o ID do evento para DELETAR: ");
        int eventoId = lerOpcao();

        if (eventoId != -1) {
            System.out.print("Tem certeza que deseja DELETAR o evento ID " + eventoId + "? (S/N): ");
            String confirmacao = scanner.nextLine().toUpperCase();

            if (confirmacao.equals("S")) {
                // Uso do getter POJO: getId()
                if (controller.deletarEvento(eventoId, usuarioLogado.id())) {
                    System.out.println("\nEvento ID " + eventoId + " deletado com sucesso.");
                } else {
                    // O Controller já exibe a mensagem de erro específica.
                    System.out.println("Falha ao deletar evento. Verifique a permissão ou o ID.");
                }
            } else {
                System.out.println("Operação cancelada.");
            }
        }
    }

    private static void deletarConta(int idParaDeletar) {
        boolean isSelfDeletion = idParaDeletar == usuarioLogado.id();
        String idParaDeletarStr = String.valueOf(idParaDeletar);

        if (isSelfDeletion) {
            System.out.println("\nATENÇÃO: Você está prestes a deletar sua conta permanentemente (ID: " + usuarioLogado.id() + ").");
        } else {
            System.out.println("\nATENÇÃO: Você está prestes a deletar a conta do usuário ID " + idParaDeletar + " permanentemente.");
        }

        System.out.println("Isso removerá todas as participações e, se for organizador, todos os eventos criados.");
        System.out.print("Digite 'DELETAR' para confirmar a exclusão da conta ID " + idParaDeletarStr + ": ");
        String confirmacao = scanner.nextLine(); // Confirmação de segurança via texto.

        if (confirmacao.equals("DELETAR")) {
            // Chamada ao controller com ID do logado e ID do alvo (controller verifica permissão).
            // Uso do getter POJO: getId()
            if (controller.deletarConta(usuarioLogado.id(), idParaDeletar)) {
                if (isSelfDeletion) {
                    System.out.println("\nSua conta foi deletada com sucesso. Sentiremos sua falta!");
                    usuarioLogado = null; // Limpa o estado para forçar retorno ao menu de login.
                } else {
                    System.out.println("\nConta do usuário ID " + idParaDeletar + " deletada com sucesso pelo Admin.");
                }
            } else {
                // O Controller é responsável por exibir a mensagem de erro detalhada.
                System.out.println("Falha ao deletar conta. Verifique se você tem permissão ou se o ID está correto.");
            }
        } else {
            System.out.println("\nExclusão de conta cancelada.");
        }
    }

    private static void fazerLogout() {
        if (usuarioLogado != null) {
            System.out.println("\nUsuário " + usuarioLogado.nome() + " deslogado.");
            usuarioLogado = null; // Limpa o estado
        }
    }


    // --- MENUS DE ADMIN ---

    private static void exibirMenuAdmin() {
        while (usuarioLogado != null && usuarioLogado.isAdmin()) {
            System.out.println("\n==================================================");
            System.out.println("  Menu ADMIN (Usuário: " + usuarioLogado.nome() + " - ID: " + usuarioLogado.id() + ")");
            System.out.println("==================================================");
            System.out.println("1. Visualizar TODOS os Eventos");
            System.out.println("2. Visualizar TODOS os Usuários");
            System.out.println("3. Criar Novo Evento");
            System.out.println("4. Deletar Evento (Qualquer um)");
            System.out.println("5. Alterar Status de Admin de Usuário");
            System.out.println("6. Deletar Conta de Usuário (Qualquer um)");
            System.out.println("0. Fazer Logout");
            System.out.print("Escolha uma opção: ");

            int opcao = lerOpcao();

            switch (opcao) {
                case 1:
                    listarEventosDisponiveis(controller.listarTodosEventosComDetalhes());
                    break;
                case 2:
                    listarTodosUsuarios();
                    break;
                case 3:
                    criarEvento();
                    break;
                case 4:
                    deletarEvento();
                    break;
                case 5:
                    alterarStatusAdmin();
                    break;
                case 6:
                    deletarContaDeTerceiros();
                    break;
                case 0:
                    fazerLogout();
                    break;
                default:
                    System.out.println("Opção inválida. Tente novamente.");
            }
        }
    }

    private static void listarTodosUsuarios() {
        System.out.println("\n--- Lista de Todos os Usuários ---");
        List<Usuario> usuarios = controller.listarTodosUsuarios();

        if (usuarios.isEmpty()) {
            System.out.println("Nenhum usuário cadastrado (exceto o admin inicial).");
            return;
        }

        // Tabela formatada para exibição
        System.out.printf("%-5s | %-20s | %-30s | %-5s\n", "ID", "Nome", "Email", "Admin");
        System.out.println("----------------------------------------------------------------------");

        for (Usuario u : usuarios) {
            // Uso dos getters POJO: getId(), getNome(), getEmail(), isAdmin()
            System.out.printf("%-5d | %-20s | %-30s | %-5s\n",
                    u.id(),
                    truncate(u.nome(), 20),
                    truncate(u.email(), 30),
                    u.isAdmin() ? "SIM" : "NÃO");
        }
        System.out.println("----------------------------------------------------------------------");
    }

    private static void alterarStatusAdmin() {
        listarTodosUsuarios();

        System.out.print("\nDigite o ID do usuário para alterar o status de Admin: ");
        int userIdAlvo = lerOpcao();

        if (userIdAlvo == -1) return;

        System.out.print("Tornar Admin? (S/N): ");
        String statusStr = scanner.nextLine().toUpperCase();
        boolean novoStatus = statusStr.equals("S");

        // Uso do getter POJO: getId()
        if (controller.toggleAdminStatus(usuarioLogado.id(), userIdAlvo, novoStatus)) {
            System.out.println("\nStatus de Admin do usuário ID " + userIdAlvo + " alterado para: " + (novoStatus ? "SIM" : "NÃO") + ".");
        } else {
            // O Controller já exibe a mensagem de erro específica.
            System.out.println("Falha ao alterar status de admin. Verifique o ID e as regras de negócio.");
        }
    }

    private static void deletarContaDeTerceiros() {
        listarTodosUsuarios();
        System.out.print("\nDigite o ID do usuário cuja conta deseja DELETAR: ");
        int idParaDeletar = lerOpcao();

        if (idParaDeletar != -1) {
            deletarConta(idParaDeletar);
        }
    }


    // --- UTILS DA VIEW ---

    /**
     * Exibe uma lista de eventos formatada como tabela.
     * @param eventos A lista de EventoComDetalhes a ser exibida.
     */
    private static void listarEventosDisponiveis(List<EventoComDetalhes> eventos) {
        if (eventos.isEmpty()) {
            System.out.println("Nenhum evento encontrado.");
            return;
        }

        // Tabela formatada para exibição (ajustada para EventoComDetalhes)
        System.out.println("-------------------------------------------------------------------------------------------------------");
        System.out.printf("%-5s | %-25s | %-12s | %-16s | %-15s | %-10s\n", "ID", "Nome do Evento", "Categoria", "Data e Hora", "Organizador", "Capacidade");
        System.out.println("-------------------------------------------------------------------------------------------------------");

        for (EventoComDetalhes e : eventos) {
            // Uso dos getters delegados
            String dataFormatada = e.getDataHora().format(FORMATTER);
            System.out.printf("%-5d | %-25s | %-12s | %-16s | %-15s | %-10d\n",
                    e.getId(),
                    truncate(e.getNome(), 25),
                    truncate(e.getCategoria().name(), 12),
                    dataFormatada,
                    truncate(e.nomeOrganizador(), 15), // Nome do organizador do DTO
                    e.getCapacidade());
        }
        System.out.println("-------------------------------------------------------------------------------------------------------");
    }

    /**
     * Função utilitária para ler uma opção inteira do usuário, tratando erros.
     * @return A opção válida ou -1 em caso de erro.
     */
    private static int lerOpcao() {
        try {
            // Verifica se há um inteiro para ser lido.
            if (scanner.hasNextInt()) {
                int opcao = scanner.nextInt();
                scanner.nextLine(); // Consome a quebra de linha pendente.
                return opcao;
            } else {
                // Se não for um inteiro, consome a linha e retorna -1 (erro).
                System.out.println("Entrada inválida. Por favor, digite um número.");
                scanner.nextLine(); // Limpa o buffer
                return -1;
            }
        } catch (InputMismatchException e) {
            // Deve ser pego pelo `if (scanner.hasNextInt())`, mas mantido por segurança.
            System.out.println("Erro: Entrada inválida. Por favor, digite um número.");
            scanner.nextLine(); // Limpa o buffer.
            return -1;
        }
    }

    /**
     * Função utilitária para truncar strings para exibição em tabela.
     * @param value A string a ser truncada.
     * @param length O comprimento máximo.
     * @return A string truncada com "..." se necessário.
     */
    private static String truncate(String value, int length) {
        if (value != null && value.length() > length) {
            return value.substring(0, length - 3) + "...";
        }
        return value;
    }
}