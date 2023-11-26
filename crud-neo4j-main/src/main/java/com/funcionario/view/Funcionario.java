package com.funcionario.view;

import com.Main;
import com.Neo4jConnection;
import com.funcionario.model.FuncionarioModel;

import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import static org.neo4j.driver.Values.parameters;

public class Funcionario {

    private Neo4jConnection neo4jConnection;
    private Scanner scanner;

    public Funcionario() {
        this.neo4jConnection = new Neo4jConnection();
        this.scanner = new Scanner(System.in);
    }

    public void exibirMenu() {
        int opcao;
        do {
            System.out.println("\nSelecione uma ação:");
            System.out.println("1. Incluir funcionario");
            System.out.println("2. Editar funcionario");
            System.out.println("3. Listar funcionario");
            System.out.println("4. Excluir funcionario");
            System.out.println("5. Sair");

            opcao = scanner.nextInt();
            scanner.nextLine();

            switch (opcao) {
                case 1:
                    incluirFuncionario();
                    break;
                case 2:
                    editarFuncionario();
                    break;
                case 3:
                    listarFuncionarios();
                    break;
                case 4:
                    excluirFuncionario();
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

    private void incluirFuncionario() {
        System.out.println("\nDigite o nome:");
        String nome = scanner.nextLine();

        System.out.println("Digite a Matricula:");
        String matricula = scanner.nextLine();

        FuncionarioModel novoFuncionario = new FuncionarioModel(matricula, nome);

        System.out.println("\nConfirme os dados:");
        System.out.println("Nome: " + novoFuncionario.getNome());
        System.out.println("Matricula: " + novoFuncionario.getMatricula());

        System.out.println("Deseja salvar? (S/N)");
        String confirmacao = scanner.nextLine().toUpperCase();

        if (confirmacao.equals("S")) {
            this.adicionarFuncionario(novoFuncionario);
            System.out.println("Funcionarios adicionado com sucesso!");
        } else {
            System.out.println("Operação cancelada.");
        }
    }

    private void editarFuncionario() {
        System.out.println("\nLista de funcionarios:");
        List<FuncionarioModel> funcionarios = this.buscarTodosFuncionarios();
        for (FuncionarioModel funcionario : funcionarios) {
            System.out.println(funcionario.getMatricula() + " - " + funcionario.getNome());
        }

        System.out.println("\nDigite a matricula do funcionario que deseja editar:");
        String matricula = scanner.nextLine();

        FuncionarioModel funcionario = this.buscarFuncionarioPorMatricula(matricula);

        if (funcionario != null) {
            System.out.println("Funcionario encontrado:");
            System.out.println("Nome: " + funcionario.getNome());
            System.out.println("Novo nome:");
            String novoNome = scanner.nextLine();

            funcionario.setNome(novoNome);
            this.editarFuncionario(funcionario);
            System.out.println("Funcionario editado com sucesso!");
        } else {
            System.out.println("Funcionario não encontrado!");
        }
    }

    private void listarFuncionarios() {
        List<FuncionarioModel> funcionarios = this.buscarTodosFuncionarios();
        System.out.println("\nLista de funcionarios:");
        for (FuncionarioModel funcionario : funcionarios) {
            System.out.println(funcionario.getMatricula() + " - " + funcionario.getNome());
        }
    }

    private void excluirFuncionario() {
        System.out.println("\nLista de funcionarios:");
        List<FuncionarioModel> funcionarios = this.buscarTodosFuncionarios();
        for (FuncionarioModel funcionario : funcionarios) {
            System.out.println(funcionario.getMatricula() + " - " + funcionario.getNome());
        }

        System.out.println("\nDigite a matricula do funcionario que deseja excluir:");
        String matricula = scanner.nextLine();

        FuncionarioModel funcionario = this.buscarFuncionarioPorMatricula(matricula);

        if (funcionario != null) {
            this.excluirFuncionario(matricula);
            System.out.println("Funconario excluído com sucesso!");
        } else {
            System.out.println("Funcionario não encontrado!");
        }
    }

    private void sair() {
//        this.neo4jConnection.close();
        Main.exibirMenuPrincipal();
    }
    
    public void adicionarFuncionario(FuncionarioModel funcionario) {
        try (Session session = neo4jConnection.getSession()) {
            Result result = session.run("CREATE (p:Funcionario {matricula: $matricula, nome: $nome})",
                    parameters("matricula", funcionario.getMatricula(), "nome", funcionario.getNome()));
            System.out.println(result.consume().counters().nodesCreated());
        }
    }

    public void editarFuncionario(FuncionarioModel funcionario) {
        try (Session session = neo4jConnection.getSession()) {
            session.run("MATCH (p:Funcionario {matricula: $matricula}) SET p.nome = $nome",
                    parameters("matricula", funcionario.getMatricula(), "nome", funcionario.getNome()));
            System.out.println("Funcionario atualizado com sucesso!");
        }
    }

    public void excluirFuncionario(String matricula) {
        try (Session session = neo4jConnection.getSession()) {
            Result result = session.run("MATCH (p:Funcionario {matricula: $matricula}) DETACH DELETE p",
                    parameters("matricula", matricula));
            System.out.println(result.consume().counters().nodesDeleted());
        }
    }

    public List<FuncionarioModel> buscarTodosFuncionarios() {
        try (Session session = neo4jConnection.getSession()) {
            Result result = session.run("MATCH (p:Funcionario) RETURN p.matricula, p.nome");
            return result.stream().map(record ->
                    new FuncionarioModel(record.get(0).asString(), record.get(1).asString()))
                    .collect(Collectors.toList());
        }
    }

    public FuncionarioModel buscarFuncionarioPorMatricula(String matricula) {
        try (Session session = neo4jConnection.getSession()) {
            Result result = session.run("MATCH (p:Funcionario {matricula: $matricula}) RETURN p.nome",
                    parameters("matricula", matricula));

            if (result.hasNext()) {
                Record record = result.next();
                String nome = record.get("p.nome").asString();
                return new FuncionarioModel(matricula, nome);
            }
        }
        return null;
    }
}