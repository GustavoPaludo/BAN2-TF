package com.cliente.view;

import com.Main;
import com.Neo4jConnection;
import com.cliente.model.ClienteModel;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import static org.neo4j.driver.Values.parameters;

public class Cliente {

    private Neo4jConnection neo4jConnection;
    private Scanner scanner;

    public Cliente() {
        this.neo4jConnection = new Neo4jConnection();
        this.scanner = new Scanner(System.in);
    }

    public void exibirMenu() {
        int opcao;
        do {
            System.out.println("\nSelecione uma ação:");
            System.out.println("1. Incluir cliente");
            System.out.println("2. Editar cliente");
            System.out.println("3. Listar clientes");
            System.out.println("4. Excluir cliente");
            System.out.println("5. Sair");

            opcao = scanner.nextInt();
            scanner.nextLine();

            switch (opcao) {
                case 1:
                    incluirCliente();
                    break;
                case 2:
                    editarCliente();
                    break;
                case 3:
                    listarClientes();
                    break;
                case 4:
                    excluirCliente();
                    break;
                case 5:
                    sair();
                    break;
                default:
                    System.out.println("Opção inválida!");
                    break;
            }
        } while (opcao != 5);
    }

    private void incluirCliente() {
        System.out.println("\nDigite o nome:");
        String nome = scanner.nextLine();

        System.out.println("Digite o CPF:");
        String cpf = scanner.nextLine();

        ClienteModel novoCliente = new ClienteModel(cpf, nome);

        System.out.println("\nConfirme os dados:");
        System.out.println("Nome: " + novoCliente.getNome());
        System.out.println("CPF: " + novoCliente.getCpf());

        System.out.println("Deseja salvar? (S/N)");
        String confirmacao = scanner.nextLine().toUpperCase();

        if (confirmacao.equals("S")) {
            this.adicionarCliente(novoCliente);
            System.out.println("Cliente adicionado com sucesso!");
        } else {
            System.out.println("Operação cancelada.");
        }
    }

    private void editarCliente() {
        System.out.println("\nLista de clientes:");
        List<ClienteModel> clientes = this.buscarTodosClientes();
        for (ClienteModel cliente : clientes) {
            System.out.println(cliente.getCpf() + " - " + cliente.getNome());
        }

        System.out.println("\nDigite o CPF do cliente que deseja editar:");
        String cpf = scanner.nextLine();

        ClienteModel cliente = this.buscarClientePorCPF(cpf);

        if (cliente != null) {
            System.out.println("Cliente encontrado:");
            System.out.println("Nome: " + cliente.getNome());
            System.out.println("Novo nome:");
            String novoNome = scanner.nextLine();

            cliente.setNome(novoNome);
            this.editarCliente(cliente);
            System.out.println("Cliente editado com sucesso!");
        } else {
            System.out.println("Cliente não encontrado!");
        }
    }

    private void listarClientes() {
        List<ClienteModel> clientes = this.buscarTodosClientes();
        System.out.println("\nLista de clientes:");
        for (ClienteModel cliente : clientes) {
            System.out.println(cliente.getCpf() + " - " + cliente.getNome());
        }
    }

    private void excluirCliente() {
        System.out.println("\nLista de clientes:");
        List<ClienteModel> clientes = this.buscarTodosClientes();
        for (ClienteModel cliente : clientes) {
            System.out.println(cliente.getCpf() + " - " + cliente.getNome());
        }

        System.out.println("\nDigite o CPF do cliente que deseja excluir:");
        String cpf = scanner.nextLine();

        ClienteModel cliente = this.buscarClientePorCPF(cpf);

        if (cliente != null) {
            this.excluirCliente(cpf);
            System.out.println("Cliente excluído com sucesso!");
        } else {
            System.out.println("Cliente não encontrado!");
        }
    }

    private void sair() {
//        this.neo4jConnection.close();
        Main.exibirMenuPrincipal();
    }
    
    public void adicionarCliente(ClienteModel cliente) {
        try (Session session = neo4jConnection.getSession()) {
            Result result = session.run("CREATE (p:Cliente {cpf: $cpf, nome: $nome})",
                    parameters("cpf", cliente.getCpf(), "nome", cliente.getNome()));
            System.out.println(result.consume().counters().nodesCreated());
        }
    }

    public void editarCliente(ClienteModel cliente) {
        try (Session session = neo4jConnection.getSession()) {
            session.run("MATCH (p:Cliente {cpf: $cpf}) SET p.nome = $nome",
                    parameters("cpf", cliente.getCpf(), "nome", cliente.getNome()));
            System.out.println("Cliente atualizado com sucesso!");
        }
    }

    public void excluirCliente(String cpf) {
        try (Session session = neo4jConnection.getSession()) {
            Result result = session.run("MATCH (p:Cliente {cpf: $cpf}) DETACH DELETE p",
                    parameters("cpf", cpf));
            System.out.println(result.consume().counters().nodesDeleted());
        }
    }

    public List<ClienteModel> buscarTodosClientes() {
        try (Session session = neo4jConnection.getSession()) {
            Result result = session.run("MATCH (p:Cliente) RETURN p.cpf, p.nome");
            return result.stream().map(record ->
                    new ClienteModel(record.get(0).asString(), record.get(1).asString()))
                    .collect(Collectors.toList());
        }
    }

    public ClienteModel buscarClientePorCPF(String cpf) {
        try (Session session = neo4jConnection.getSession()) {
            Result result = session.run("MATCH (p:Cliente {cpf: $cpf}) RETURN p.nome",
                    parameters("cpf", cpf));

            if (result.hasNext()) {
                Record record = result.next();
                String nome = record.get("p.nome").asString();
                return new ClienteModel(cpf, nome);
            }
        }
        return null;
    }
}