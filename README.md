# Sistema de Gestão de Eventos (Console + SQLite)

Projeto da disciplina **Programação de Sistemas para Computação (PSC)**.  
Aplicação console em **Java 17**, usando **Maven** e **SQLite** para gerenciar:

- Usuários (cadastro, login, exclusão de conta)
- Eventos (criação, listagem, inscrição, cancelamento)
- Diferenciação entre **usuário comum** e **administrador**

---

## Objetivo do sistema

O sistema permite que pessoas usuárias:

- Criem uma conta com nome, e-mail e senha
- Façam login no sistema
- Visualizem eventos disponíveis
- Criem novos eventos
- Inscrevam-se e cancelem inscrição em eventos
- Visualizem os próprios eventos organizados e inscrições

E, para administradores, há um menu específico com funções de gestão mais amplas
(listagem de todos os usuários, todos os eventos, etc.).

---

## Tecnologias utilizadas

- **Linguagem:** Java 17  
- **Build/gerenciador de dependências:** Maven  
- **Banco de dados:** SQLite (via `sqlite-jdbc`)  
- **Logging simples:** SLF4J (binding `slf4j-nop`)  
- **IDE recomendada:** IntelliJ IDEA

As dependências estão declaradas no arquivo `pom.xml`.

---

## Arquitetura e organização do código

O projeto segue uma separação em camadas dentro de `src/main/java`:

- `view`  
  - Contém a classe **`SistemaEventosApp`**, que é a classe principal.  
  - Responsável pela interface de console (menus, leitura de entrada, impressão de tabelas).  
  - Não acessa o banco diretamente: sempre chama o `AppController`.

- `controller`  
  - Contém o **`AppController`**.  
  - Concentra a **regra de negócio** e faz a ponte entre a `view` e os DAOs.  
  - Implementa operações como: login, criação de usuário, criação e listagem de eventos, inscrição/cancelamento, etc.

- `dao`  
  - **`UsuarioDAO`**: operações de banco relacionadas a usuários  
    (inserir, buscar por e-mail/ID, listar, atualizar status de admin, deletar).  
  - **`EventoDAO`**: operações da tabela de eventos  
    (criar, listar todos, listar por organizador, deletar, buscar por ID).  
  - **`InscricaoDAO`**: operações da relação usuário-evento  
    (registrar inscrição, verificar se já existe, listar inscrições, cancelar).

- `model`  
  - **`Usuario`**: modelo de usuário (id, nome, e-mail, senha, flag de admin).  
  - **`Evento`**: modelo de evento (id, nome, categoria, data/hora, local, capacidade, organizador).  
  - **`EventoComDetalhes`**: DTO para exibição, inclui dados do evento e nome do organizador.  
  - **`CategoriaEvento`**: enum com as categorias de evento e descrições legíveis.

- `util`  
  - **`DatabaseConnection`**: centraliza a conexão com o SQLite.  
    - Método `initializeDatabase()` cria as tabelas necessárias (se não existirem)  
      e garante a criação do usuário administrador padrão.  
    - Fornece o método para obter `Connection` usado pelos DAOs.

Também existe um pacote `org.example` com uma classe `Main` gerada automaticamente na criação do projeto, que não é utilizada na versão final.  
A **classe principal oficial** do sistema é `view.SistemaEventosApp`.

---

## Classe principal (ponto de entrada)

```java
package view;

public class SistemaEventosApp {
    public static void main(String[] args) {
        // Inicialização do banco e início do fluxo de menus
    }
}

É executada ao iniciar o programa.

Chama DatabaseConnection.initializeDatabase() logo no começo.

Depois chama exibirMenuLogin(), que controla o fluxo de login/criação de conta.

A partir do usuário logado (usuarioLogado), redireciona para:

exibirMenuPrincipal() para usuário comum, ou

exibirMenuAdmin() para administradores.

Fluxos principais implementados
Usuário comum

Criar nova conta

Fazer login

Ver eventos disponíveis

Inscrever-se em evento

Ver “Meus eventos inscritos”

Cancelar inscrição em evento

Ver “Meus eventos organizados”

Criar novos eventos

Deletar eventos que ele mesmo organizou

Deletar a própria conta

Administrador

O menu de admin foi projetado para:

Listar todos os eventos

Listar todos os usuários

Criar eventos

Deletar eventos

Alterar status de admin de usuários

Deletar contas de outros usuários

Na versão atual, uma parte das funcionalidades de administrador ainda está em refinamento
(por exemplo, deleção global de eventos e alteração/persistência do status de admin).
Apesar disso, toda a estrutura de menus, controller e DAOs está pronta e o fluxo de
usuário comum está completamente funcional.

Banco de dados (SQLite)

Banco de dados local em arquivo SQLite.

A conexão é feita via sqlite-jdbc.

DatabaseConnection.initializeDatabase() cria as tabelas necessárias
e garante o usuário administrador padrão.

Dependência principal:

<dependency>
    <groupId>org.xerial</groupId>
    <artifactId>sqlite-jdbc</artifactId>
    <version>3.45.1.0</version>
</dependency>


Observação: em versões mais novas da JDK podem aparecer warnings sobre acesso nativo
do SQLite. Eles não impedem a execução do programa.

Como executar o projeto
1. Via IntelliJ IDEA (recomendado)

Abrir o IntelliJ IDEA.

File > Open... e selecionar a pasta do projeto
(onde está o arquivo pom.xml).

Aguardar a importação do projeto Maven (download das dependências).

No painel de projeto, abrir:

src/main/java/view/SistemaEventosApp.java


Clicar com o botão direito sobre o método:

public static void main(String[] args)


e escolher Run 'SistemaEventosApp.main()'.

O console do IntelliJ será aberto e o sistema de menus ficará disponível
para interação.

2. Via Maven (linha de comando)

Pré-requisitos:

Java JDK 17 ou superior instalado

Maven instalado

Comandos na raiz do projeto (onde está o pom.xml):

mvn clean compile exec:java


O plugin exec-maven-plugin está configurado para usar:

mainClass = view.SistemaEventosApp


Portanto, o comando acima compila e executa diretamente a classe principal.

Usuário administrador padrão

Na inicialização do banco, é criado um usuário administrador padrão
(os dados podem ser verificados no código de DatabaseConnection).

Esse usuário pode ser usado para acessar o menu de administração,
listar usuários e eventos e testar as funções administrativas.

Estrutura de diretórios (resumo)
src/
  main/
    java/
      controller/
        AppController.java
      dao/
        EventoDAO.java
        InscricaoDAO.java
        UsuarioDAO.java
      model/
        CategoriaEvento.java
        Evento.java
        EventoComDetalhes.java
        Usuario.java
      org/example/
        Main.java   (não utilizado na versão final)
      util/
        DatabaseConnection.java
      view/
        SistemaEventosApp.java

Observações finais

O foco principal foi entregar um sistema funcional em console, com
persistência real em banco de dados e separação em camadas
(view, controller, dao, model, util).

O fluxo de usuário comum está plenamente implementado e testado.

O fluxo de administrador já tem menus e estrutura prontos, com algumas
operações em funcionamento e outras ainda em refinamento de regra de negócio
(permissões e persistência do status de admin).

Qualquer dúvida sobre a execução ou sobre uma parte específica do código,
basta abrir a classe relacionada nos pacotes descritos acima.
