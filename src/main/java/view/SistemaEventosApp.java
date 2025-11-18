package view;

import controller.EventoController;
import model.CategoriaEvento;
import model.Evento;
import model.Usuario;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

// Classe principal que contém o método main() e a lógica de interação com o usuário (View).
public class SistemaEventosApp {

    // Padrão de data e hora esperado do usuário (ex: dd/MM/yyyy HH:mm)
    private static final String DATE_TIME_FORMAT_PATTERN = "dd/MM/yyyy HH:mm";

    // Formatador usado para ler a entrada do usuário
    private static final DateTimeFormatter USER_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT_PATTERN);
    // Formatador estático e público para ser usado no Evento.toString() e em outros pontos de saída.
    public static final DateTimeFormatter USER_FORMATTER_FOR_OUTPUT = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT_PATTERN);

    private static Scanner scanner;
    private static EventoController controller; // Instância do Controller para acesso à lógica

    // Método principal: Ponto de entrada da aplicação.
    public static void main(String[] args) {

        System.out.println("==================================================");
        System.out.println("|| Iniciando Sistema de Eventos (SQLite/Maven) ||");
        System.out.println("==================================================");

        controller = new EventoController(); // Inicializa o controlador (que inicializa o BD)
        scanner = new Scanner(System.in);

        exibirMenuPrincipal();

        scanner.close();
        System.out.println("\n==================================================");
        System.out.println("|| Sistema de Eventos encerrado. Até mais! ||");
        System.out.println("==================================================");
    }

    // Exibe e gerencia o loop do menu principal
    private static void exibirMenuPrincipal() {
        int opcao = -1;

        while (opcao != 0) {

            // Exibe o usuário logado no topo
            Usuario usuarioAtual = controller.getUsuarioLogado();
            System.out.println("\n--- MENU PRINCIPAL ---");
            System.out.println(">>> Usuário Atual: " + usuarioAtual.getNome() + " (ID: " + usuarioAtual.getId() + ") <<<");

            System.out.println("1. Cadastrar Novo Usuário");
            System.out.println("2. Cadastrar Novo Evento");
            System.out.println("3. Listar Todos Eventos");
            System.out.println("4. Confirmar Participação");
            System.out.println("5. Cancelar Participação");
            System.out.println("6. RETIRAR/EXCLUIR Evento");
            System.out.println("7. DESINSCREVER/EXCLUIR Usuário (Atual)"); // Exclui a própria conta
            System.out.println("8. FAZER LOGIN");
            System.out.println("0. Sair");
            System.out.print("Escolha uma opção: ");

            try {
                // Lê a opção
                String input = scanner.nextLine();
                if (input.isEmpty()) { continue; } // Ignora entrada vazia
                opcao = Integer.parseInt(input);

                switch (opcao) {
                    case 1: cadastrarNovoUsuarioInterativo(); break;
                    case 2: cadastrarNovoEventoInterativo(); break;
                    case 3: listarTodosEventos(); break;
                    case 4: confirmarParticipacaoInterativo(); break;
                    case 5: cancelarParticipacaoInterativo(); break;
                    case 6: removerEventoInterativo(); break;
                    case 7: desinscreverUsuarioInterativo(); break;
                    case 8: fazerLoginInterativo(); break;
                    case 0: break;
                    default: System.err.println("Opção inválida. Tente novamente."); break;
                }
            } catch (NumberFormatException e) {
                System.err.println("Entrada inválida. Digite um número.");
                opcao = -1;
            }
        }
    }

    // Coleta dados para cadastro de novo usuário.
    private static void cadastrarNovoUsuarioInterativo() {
        System.out.println("\n--- CADASTRO DE NOVO USUÁRIO ---");
        System.out.print("Digite o Nome: ");
        String nome = scanner.nextLine();
        System.out.print("Digite o E-mail (deve ser único!): ");
        String email = scanner.nextLine();
        System.out.print("Digite a Senha: ");
        String senha = scanner.nextLine();
        Usuario novoUsuario = new Usuario(nome, email, senha); // Construtor sem ID
        controller.cadastrarUsuario(novoUsuario);
        System.out.println("-> Usuário '" + nome + "' solicitado para cadastro.");
    }

    // Coleta dados para cadastro de novo evento.
    private static void cadastrarNovoEventoInterativo() {
        System.out.println("\n--- CADASTRO DE NOVO EVENTO ---");
        System.out.print("Nome do Evento: ");
        String nome = scanner.nextLine();
        System.out.print("Endereço/Local: ");
        String endereco = scanner.nextLine();

        System.out.println("Categorias disponíveis: " + java.util.Arrays.asList(CategoriaEvento.values()));
        System.out.print("Categoria (ex: MUSICA): ");
        String categoriaStr = scanner.nextLine().toUpperCase();
        CategoriaEvento categoria;
        try {
            categoria = CategoriaEvento.valueOf(categoriaStr);
        } catch (IllegalArgumentException e) {
            System.err.println("Categoria inválida. Usando MUSICA como padrão.");
            categoria = CategoriaEvento.MUSICA;
        }

        LocalDateTime dataHora = null;
        boolean dataValida = false;
        while (!dataValida) {
            System.out.print("Data e Hora (" + DATE_TIME_FORMAT_PATTERN + "): ");
            String dataHoraStr = scanner.nextLine();
            try {
                dataHora = LocalDateTime.parse(dataHoraStr, USER_FORMATTER);
                dataValida = true;
            } catch (DateTimeParseException e) {
                System.err.println("Formato de data e hora inválido. Use " + DATE_TIME_FORMAT_PATTERN + ".");
            }
        }

        System.out.print("Descrição do Evento: ");
        String descricao = scanner.nextLine();
        controller.cadastrarEvento(nome, endereco, categoria, dataHora, descricao);
        System.out.println("-> Evento '" + nome + "' solicitado para cadastro.");
    }

    // Exibe todos os eventos.
    private static void listarTodosEventos() {
        System.out.println("\n--- LISTA DE EVENTOS ---");
        List<Evento> eventos = controller.listarTodosEventos();

        if (eventos.isEmpty()) {
            System.out.println("Nenhum evento cadastrado.");
        } else {
            eventos.forEach(System.out::println);
        }
    }

    // Interação de login
    private static void fazerLoginInterativo() {
        System.out.println("\n--- FAZER LOGIN ---");
        System.out.print("E-mail: ");
        String email = scanner.nextLine();
        System.out.print("Senha: ");
        String senha = scanner.nextLine();

        if (controller.fazerLogin(email, senha)) {
            System.out.println("✅ Login realizado com sucesso! Bem-vindo(a), " + controller.getUsuarioLogado().getNome() + ".");
        } else {
            System.err.println("❌ E-mail ou senha inválidos.");
        }
    }

    // Confirma a participação
    private static void confirmarParticipacaoInterativo() {
        // Bloqueia a ação se o usuário não tiver um ID válido (usuário de teste sem login)
        if (controller.getUsuarioLogado().getId() <= 0) {
            System.err.println("Faça login (Opção 8) antes de confirmar a participação.");
            return;
        }
        listarTodosEventos();
        Usuario usuario = controller.getUsuarioLogado();
        System.out.println("\n--- CONFIRMAR PARTICIPAÇÃO ---");
        System.out.println("Participando como: " + usuario.getNome() + " (ID: " + usuario.getId() + ")");
        System.out.print("Digite o ID do Evento que deseja participar: ");
        try {
            int eventoId = Integer.parseInt(scanner.nextLine());
            controller.confirmarPresenca(eventoId); // Usa o usuário logado internamente
        } catch (NumberFormatException e) {
            System.err.println("ID inválido. Digite um número inteiro.");
        }
    }

    // Cancela a participação
    private static void cancelarParticipacaoInterativo() {
        if (controller.getUsuarioLogado().getId() <= 0) {
            System.err.println("Faça login (Opção 8) antes de cancelar a participação.");
            return;
        }
        listarTodosEventos();
        Usuario usuario = controller.getUsuarioLogado();
        System.out.println("\n--- CANCELAR PARTICIPAÇÃO ---");
        System.out.println("Cancelando como: " + usuario.getNome() + " (ID: " + usuario.getId() + ")");
        System.out.print("Digite o ID do Evento que deseja cancelar a participação: ");
        try {
            int eventoId = Integer.parseInt(scanner.nextLine());
            controller.cancelarParticipacao(eventoId); // Usa o usuário logado internamente
        } catch (NumberFormatException e) {
            System.err.println("ID inválido. Digite um número inteiro.");
        }
    }

    // Interação para remover um evento
    private static void removerEventoInterativo() {
        listarTodosEventos();
        System.out.println("\n--- RETIRAR/EXCLUIR EVENTO ---");
        System.out.print("Digite o ID do Evento que deseja excluir permanentemente: ");
        try {
            int eventoId = Integer.parseInt(scanner.nextLine());
            controller.removerEvento(eventoId);
        } catch (NumberFormatException e) {
            System.err.println("ID inválido. Digite um número inteiro.");
        }
    }

    // Permite que o USUARIO ATUAL exclua sua própria conta.
    private static void desinscreverUsuarioInterativo() {
        Usuario usuario = controller.getUsuarioLogado();

        // Proteção contra a exclusão do usuário de teste padrão
        if (usuario.getId() == controller.USUARIO_TESTE.getId()) {
            System.err.println("\nNão é possível excluir o usuário de teste padrão. Faça login (Opção 8) com um usuário cadastrado.");
            return;
        }

        System.out.println("\n--- DESINSCREVER/EXCLUIR USUÁRIO ---");
        System.out.println("ATENÇÃO: Você irá excluir a conta de: " + usuario.getNome() + " (ID: " + usuario.getId() + ")");
        System.out.print("Tem certeza que deseja continuar? (s/n): ");
        String confirmacao = scanner.nextLine().trim().toLowerCase();

        if (confirmacao.equals("s")) {
            controller.desinscreverUsuario(); // Chama a exclusão do usuário logado
            System.out.println("\nConta excluída. Você foi automaticamente deslogado. O usuário atual é: " + controller.getUsuarioLogado().getNome());
        } else {
            System.out.println("Operação cancelada.");
        }
    }
}