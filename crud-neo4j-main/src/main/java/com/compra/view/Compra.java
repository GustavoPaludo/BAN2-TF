package com.compra.view;

import com.Main;
import com.Neo4jConnection;
import com.compra.model.CompraModel;
import com.fornecedor.model.FornecedorModel;
import com.funcionario.model.FuncionarioModel;
import com.produto.model.ProdutoModel;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import static org.neo4j.driver.Values.parameters;

public class Compra {

    private Neo4jConnection neo4jConnection;
    private Scanner scanner;

    public Compra() {
        this.neo4jConnection = new Neo4jConnection();
        this.scanner = new Scanner(System.in);
    }

    public void exibirMenu() {
        int opcao;
        do {
            System.out.println("\nSelecione uma ação:");
            System.out.println("1. Novo pedido");
            System.out.println("2. Listar histórico de pedidos");
            System.out.println("3. Sair");

            opcao = scanner.nextInt();
            scanner.nextLine();

            switch (opcao) {
                case 1:
                    fazerNovoPedido();
                    break;
                case 2:
                    listarHistoricoPedidos();
                    break;
                case 3:
                    sair();
                    break;
                default:
                    System.out.println("Opção inválida!");
                    break;
            }
        } while (opcao != 3);
    }

    private void fazerNovoPedido() {
    	
    	List<FornecedorModel> fornecedores = buscarTodosFornecedores();
        if (fornecedores.isEmpty()) {
            System.out.println("Não existem fornecedores disponíveis. Não é possível fazer um pedido.");
            return;
        }

        System.out.println("\nLista de fornecedores disponíveis:");
        for (FornecedorModel fornecedor : fornecedores) {
            System.out.println(fornecedor.getCnpj() + " - " + fornecedor.getNome());
        }

        System.out.println("\nDigite o CNPJ do fornecedor:");
        String cnpjFornecedor = scanner.nextLine();

        FornecedorModel fornecedorSelecionado = fornecedores.stream()
                .filter(fornecedor -> fornecedor.getCnpj().equals(cnpjFornecedor))
                .findFirst()
                .orElse(null);

        if (fornecedorSelecionado == null) {
            System.out.println("Fornecedor não encontrado.");
            return;
        }

        List<ProdutoModel> produtosAssociados = buscarProdutosAssociados(cnpjFornecedor);

        if (produtosAssociados.isEmpty()) {
            System.out.println("Não existem produtos associados a este fornecedor. Não é possível fazer um pedido.");
            return;
        }

        System.out.println("\nProdutos disponíveis para o fornecedor selecionado:");
        for (ProdutoModel produto : produtosAssociados) {
            System.out.println(produto.getCodigo() + " - " + produto.getNome() + " - Preço: " + produto.getPreco());
        }
    	
    	List<ProdutoModel> produtos = buscarTodosProdutos();
        if (produtos.isEmpty()) {
            System.out.println("Não existem produtos disponíveis. Não é possível fazer um pedido.");
            return;
        }

        List<FuncionarioModel> funcionarios = buscarTodosFuncionarios();
        if (funcionarios.isEmpty()) {
            System.out.println("Não existem funcionários disponíveis. Não é possível fazer um pedido.");
            return;
        }

        System.out.println("\nLista de funcionários disponíveis:");
        for (FuncionarioModel funcionario : funcionarios) {
            System.out.println(funcionario.getMatricula() + " - " + funcionario.getNome());
        }

        System.out.println("Digite a matrícula do funcionário:");
        String matriculaFuncionario = scanner.nextLine();

        System.out.println("\nProdutos disponíveis para o fornecedor selecionado:");
        for (ProdutoModel produto : produtosAssociados) {
            System.out.println(produto.getCodigo() + " - " + produto.getNome() + " - Preço: " + produto.getPreco());
        }

        System.out.println("Digite os produtos para o pedido (código, quantidade separados por vírgula):");
        Map<String, Integer> produtosQuantidades = new HashMap<>();

        boolean adicionarOutroProduto = true;

        while (adicionarOutroProduto) {
            String produtosInput = scanner.nextLine();
            String[] produtosArray = produtosInput.split(",");
            if (produtosArray.length != 2) {
                System.out.println("Formato inválido. Utilize 'código, quantidade'.");
                continue;
            }

            String codigoProduto = produtosArray[0].trim();
            int quantidade = Integer.parseInt(produtosArray[1].trim());

            if (quantidade <= 0) {
                System.out.println("Quantidade inválida. Deve ser um valor positivo.");
                continue;
            }

            produtosQuantidades.put(codigoProduto, quantidade);

            System.out.println("Deseja adicionar outro produto? (S/N)");
            String resposta = scanner.nextLine().toUpperCase();

            adicionarOutroProduto = resposta.equals("S");
        }

        CompraModel novoPedido = new CompraModel(LocalDate.now(), produtosQuantidades, matriculaFuncionario, cnpjFornecedor);

        System.out.println("\nConfirme os dados:");
        System.out.println("CNPJ do fornecedor: " + novoPedido.getCnpjFornecedor());
        System.out.println("Matrícula do funcionário: " + novoPedido.getMatriculaFuncionario());
        System.out.println("Produtos e quantidades: " + novoPedido.getProdutosQuantidades());

        System.out.println("Deseja fazer o pedido? (S/N)");
        String confirmacao = scanner.nextLine().toUpperCase();

        if (confirmacao.equals("S")) {
            adicionarPedido(novoPedido);
            System.out.println("Pedido realizado com sucesso!");
        } else {
            System.out.println("Operação cancelada.");
        }
    }

    private void listarHistoricoPedidos() {
        try (Session session = neo4jConnection.getSession()) {
            Result result = session.run("MATCH (c:Compra)-[contem:Contem]->(p:Produto) RETURN c.cnpjFornecedor, c.matriculaFuncionario, c.dataCompra, p.codigo, p.nome, contem.quantidade");
            
            while (result.hasNext()) {
                Record record = result.next();
                System.out.println("CNPJ do fornecedor: " + record.get("c.cnpjFornecedor").asString());
                System.out.println("Matrícula do funcionário: " + record.get("c.matriculaFuncionario").asString());
                System.out.println("Data da compra: " + record.get("c.dataCompra").asString());
                System.out.println("Produto: " + record.get("p.nome").asString());
                System.out.println("Código do produto: " + record.get("p.codigo").asString());
                System.out.println("Quantidade: " + record.get("contem.quantidade").asInt());
                System.out.println();
            }
        } catch (Exception e) {
            System.out.println("Erro ao listar histórico de pedidos: " + e.getMessage());
        }
    }

    private void sair() {
        Main.exibirMenuPrincipal();
    }

    private void adicionarPedido(CompraModel pedido) {
        try (Session session = neo4jConnection.getSession()) {
            Result createCompra = session.run("CREATE (c:Compra {cnpjFornecedor: $cnpj, matriculaFuncionario: $matricula, dataCompra: $data}) RETURN c",
                    parameters("cnpj", pedido.getCnpjFornecedor(), "matricula", pedido.getMatriculaFuncionario(),
                            "data", pedido.getDataPedido().toString()));

            @SuppressWarnings("unused")
			Node compraNode = createCompra.single().get("c").asNode();

            session.run("MATCH (f:Funcionario {matricula: $matricula}), (c:Compra {cnpjFornecedor: $cnpj, dataCompra: $data}) "
                    + "CREATE (f)-[:Realizou]->(c)",
                    parameters("matricula", pedido.getMatriculaFuncionario(), "cnpj", pedido.getCnpjFornecedor(),
                            "data", pedido.getDataPedido().toString()));

            for (Map.Entry<String, Integer> entry : pedido.getProdutosQuantidades().entrySet()) {
                String codigoProduto = entry.getKey();
                int quantidade = entry.getValue();

                Result findProduct = session.run("MATCH (p:Produto {codigo: $codigo}) RETURN p", parameters("codigo", codigoProduto));
                @SuppressWarnings("unused")
				Node produtoNode;
                if (findProduct.hasNext()) {
                    produtoNode = findProduct.single().get("p").asNode();
                } else {
                    Result createProduct = session.run("CREATE (p:Produto {codigo: $codigo}) RETURN p", parameters("codigo", codigoProduto));
                    produtoNode = createProduct.single().get("p").asNode();
                }

                session.run("MATCH (c:Compra {cnpjFornecedor: $cnpj, matriculaFuncionario: $matricula, dataCompra: $data}), (p:Produto {codigo: $codigo}) "
                        + "CREATE (c)-[:Contem {quantidade: $quantidade}]->(p)",
                        parameters("cnpj", pedido.getCnpjFornecedor(), "matricula", pedido.getMatriculaFuncionario(),
                                "data", pedido.getDataPedido().toString(), "codigo", codigoProduto, "quantidade", quantidade));

                session.run("MATCH (f:Fornecedor {cnpj: $cnpj}), (p:Produto {codigo: $codigo}) "
                        + "CREATE (f)-[:Fornece]->(p)",
                        parameters("cnpj", pedido.getCnpjFornecedor(), "codigo", codigoProduto));

                session.run("MATCH (e:Estoque {codigo_produto: $codigo}) "
                        + "SET e.quantidade = e.quantidade + toInteger($quantidade) "
                        + "RETURN e",
                        parameters("codigo", codigoProduto, "quantidade", quantidade));
            }
            System.out.println("Pedido realizado com sucesso!");
        } catch (Exception e) {
            System.out.println("Erro ao adicionar pedido: " + e.getMessage());
        }
    }

    private List<ProdutoModel> buscarTodosProdutos() {
        try (Session session = neo4jConnection.getSession()) {
            Result result = session.run("MATCH (p:Produto) RETURN p.codigo, p.nome, p.preco");
            return result.stream().map(record ->
                    new ProdutoModel(record.get(0).asString(), record.get(1).asString(), record.get(2).asLong()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.out.println("Erro ao buscar produtos: " + e.getMessage());
            return null;
        }
    }
    
    private List<FornecedorModel> buscarTodosFornecedores() {
        try (Session session = neo4jConnection.getSession()) {
            Result result = session.run("MATCH (f:Fornecedor) RETURN f.cnpj, f.nome");
            return result.stream().map(record ->
                    new FornecedorModel(record.get(0).asString(), record.get(1).asString(), null))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.out.println("Erro ao buscar fornecedores: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<FuncionarioModel> buscarTodosFuncionarios() {
        try (Session session = neo4jConnection.getSession()) {
            Result result = session.run("MATCH (f:Funcionario) RETURN f.matricula, f.nome");
            return result.stream().map(record ->
                    new FuncionarioModel(record.get(0).asString(), record.get(1).asString()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.out.println("Erro ao buscar funcionários: " + e.getMessage());
            return Collections.emptyList();
        }
    }
    
    private List<ProdutoModel> buscarProdutosAssociados(String cnpjFornecedor) {
        try (Session session = neo4jConnection.getSession()) {
            Result result = session.run("MATCH (f:Fornecedor {cnpj: $cnpj})-[:Fornece]->(p:Produto) RETURN p.codigo, p.nome, p.preco",
                    parameters("cnpj", cnpjFornecedor));

            return result.stream().map(record ->
                    new ProdutoModel(record.get("p.codigo").asString(), record.get("p.nome").asString(), record.get("p.preco").asLong()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.out.println("Erro ao buscar produtos associados ao fornecedor: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}