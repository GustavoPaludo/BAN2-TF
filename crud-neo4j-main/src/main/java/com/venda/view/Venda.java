package com.venda.view;

import com.Main;
import com.Neo4jConnection;
import com.venda.model.VendaModel;
import com.cliente.model.ClienteModel;
import com.funcionario.model.FuncionarioModel;
import com.produto.model.ProdutoModel;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import static org.neo4j.driver.Values.parameters;

public class Venda {

	private Neo4jConnection neo4jConnection;
	private Scanner scanner;

	public Venda() {
		this.neo4jConnection = new Neo4jConnection();
		this.scanner = new Scanner(System.in);
	}

	public void exibirMenu() {
		int opcao;
		do {
			System.out.println("\nSelecione uma ação:");
			System.out.println("1. Nova venda");
			System.out.println("2. Listar histórico de vendas");
			System.out.println("3. Sair");

			opcao = scanner.nextInt();
			scanner.nextLine();

			switch (opcao) {
			case 1:
				fazerNovaVenda();
				break;
			case 2:
				listarHistoricoVendas();
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

	private void fazerNovaVenda() {
		List<ClienteModel> clientes = buscarTodosClientes();
		if (clientes.isEmpty()) {
			System.out.println("Não existem clientes disponíveis. Não é possível fazer uma venda.");
			return;
		}

		System.out.println("\nLista de clientes disponíveis:");
		for (ClienteModel cliente : clientes) {
			System.out.println(cliente.getCpf() + " - " + cliente.getNome());
		}

		System.out.println("\nDigite o CPF do cliente:");
		String cpfCliente = scanner.nextLine();

		ClienteModel clienteSelecionado = clientes.stream().filter(cliente -> cliente.getCpf().equals(cpfCliente))
				.findFirst().orElse(null);

		if (clienteSelecionado == null) {
			System.out.println("Cliente não encontrado.");
			return;
		}

		List<FuncionarioModel> funcionarios = buscarTodosFuncionarios();

		System.out.println("\nLista de funcionários disponíveis:");
		for (FuncionarioModel funcionario : funcionarios) {
			System.out.println(funcionario.getMatricula() + " - " + funcionario.getNome());
		}

		System.out.println("Digite a matrícula do funcionário:");
		String matriculaFuncionario = scanner.nextLine();

		List<ProdutoModel> produtos = buscarTodosProdutos();
		if (produtos.isEmpty()) {
			System.out.println("Não existem produtos disponíveis. Não é possível fazer uma venda.");
			return;
		}

		System.out.println("\nProdutos disponíveis:");
		for (ProdutoModel produto : produtos) {
			System.out.println(produto.getCodigo() + " - " + produto.getNome() + " - Preço de Compra: "
					+ produto.getPrecoCompra() + " - Preço de Venda: " + produto.getPrecoVenda() + " - Estoque: " + produto.getQtdEstoque());
		}

		System.out.println("Digite os produtos para a venda (código, quantidade separados por vírgula):");
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

		VendaModel novaVenda = new VendaModel(LocalDate.now(), produtosQuantidades, matriculaFuncionario, cpfCliente);

		System.out.println("\nConfirme os dados:");
		System.out.println("CPF do cliente: " + novaVenda.getCpfCliente());
		System.out.println("Matricula do funcionario: " + novaVenda.getMatriculaFuncionario());
		System.out.println("Produtos e quantidades: " + novaVenda.getProdutosQuantidades());

		System.out.println("Deseja fazer a venda? (S/N)");
		String confirmacao = scanner.nextLine().toUpperCase();

		if (confirmacao.equals("S")) {
			adicionarVenda(novaVenda);
		} else {
			System.out.println("Operação cancelada.");
		}
	}

	private List<FuncionarioModel> buscarTodosFuncionarios() {
		try (Session session = neo4jConnection.getSession()) {
			Result result = session.run("MATCH (f:Funcionario) RETURN f.matricula, f.nome");
			return result.stream()
					.map(record -> new FuncionarioModel(record.get(0).asString(), record.get(1).asString()))
					.collect(Collectors.toList());
		} catch (Exception e) {
			System.out.println("Erro ao buscar funcionários: " + e.getMessage());
			return Collections.emptyList();
		}
	}

	private void listarHistoricoVendas() {
		try (Session session = neo4jConnection.getSession()) {
			Result result = session.run(
					"MATCH (v:Venda)-[contem:Contem]->(p:Produto) RETURN v.cpfCliente, v.matriculaFuncionario, v.dataVenda, p.codigo, p.nome, contem.quantidade");

			while (result.hasNext()) {
				Record record = result.next();
				System.out.println("CPF do cliente: " + record.get("v.cpfCliente").asString());
				System.out.println("Matrícula do funcionário: " + record.get("v.matriculaFuncionario").asString());
				System.out.println("Data da venda: " + record.get("v.dataVenda").asString());
				System.out.println("Produto: " + record.get("p.nome").asString());
				System.out.println("Código do produto: " + record.get("p.codigo").asString());
				System.out.println("Quantidade: " + record.get("contem.quantidade").asInt());
				System.out.println();
			}
		} catch (Exception e) {
			System.out.println("Erro ao listar histórico de vendas: " + e.getMessage());
		}
	}

	private void sair() {
		Main.exibirMenuPrincipal();
	}

	private void adicionarVenda(VendaModel venda) {
	    try (Session session = neo4jConnection.getSession()) {
	        List<String> produtosSemEstoque = new ArrayList<>();

	        for (Map.Entry<String, Integer> entry : venda.getProdutosQuantidades().entrySet()) {
	            String codigoProduto = entry.getKey();
	            int quantidadeVendida = entry.getValue();

	            Result findStock = session.run("MATCH (e:Estoque {codigo_produto: $codigo}) RETURN e.quantidade AS estoque",
	                    parameters("codigo", codigoProduto));

	            if (findStock.hasNext()) {
	                Record stockRecord = findStock.single();
	                int quantidadeEmEstoque = stockRecord.get("estoque").asInt();

	                if (quantidadeEmEstoque < quantidadeVendida) {
	                    produtosSemEstoque.add(codigoProduto);
	                }
	            } else {
	                produtosSemEstoque.add(codigoProduto);
	            }
	        }

	        if (!produtosSemEstoque.isEmpty()) {
	            for (String codigoProduto : produtosSemEstoque) {
	                System.out.println("Erro ao realizar a venda: Não há estoque suficiente para o produto de código " + codigoProduto);
	            }
	            return;
	        }

	        Result createVenda = session.run(
	                "CREATE (v:Venda {cpfCliente: $cpf, matriculaFuncionario: $matricula, dataVenda: $data}) RETURN v",
	                parameters("cpf", venda.getCpfCliente(), "matricula", venda.getMatriculaFuncionario(), "data",
	                        venda.getDataVenda().toString()));

	        Node vendaNode = createVenda.single().get("v").asNode();

	        session.run(
	                "MATCH (f:Funcionario {matricula: $matricula}), (v:Venda {cpfCliente: $cpf, dataVenda: $data}) "
	                        + "CREATE (f)-[:Realizou]->(v)",
	                parameters("matricula", venda.getMatriculaFuncionario(), "cpf", venda.getCpfCliente(), "data",
	                        venda.getDataVenda().toString()));

	        session.run(
	                "MATCH (c:Cliente {cpf: $cpf}), (v:Venda {cpfCliente: $cpf, dataVenda: $data}) "
	                        + "CREATE (c)-[:Realizou]->(v)",
	                parameters("cpf", venda.getCpfCliente(), "data", venda.getDataVenda().toString()));

	        for (Map.Entry<String, Integer> entry : venda.getProdutosQuantidades().entrySet()) {
	            String codigoProduto = entry.getKey();
	            int quantidadeVendida = entry.getValue();

	            session.run(
	                    "MATCH (v:Venda {cpfCliente: $cpf, matriculaFuncionario: $matricula, dataVenda: $data}), (p:Produto {codigo: $codigo}) "
	                            + "CREATE (v)-[:Contem {quantidade: $quantidade}]->(p)",
	                    parameters("cpf", venda.getCpfCliente(), "matricula", venda.getMatriculaFuncionario(), "data",
	                            venda.getDataVenda().toString(), "codigo", codigoProduto, "quantidade", quantidadeVendida));

	            session.run(
	                    "MATCH (e:Estoque {codigo_produto: $codigo}) "
	                            + "SET e.quantidade = e.quantidade - toInteger($quantidade) RETURN e",
	                    parameters("codigo", codigoProduto, "quantidade", quantidadeVendida));
	        }

	        System.out.println("Venda realizada com sucesso!");
	    } catch (Exception e) {
	        System.out.println("Erro ao adicionar venda: " + e.getMessage());
	    }
	}


	private List<ProdutoModel> buscarTodosProdutos() {
	    try (Session session = neo4jConnection.getSession()) {
	        Result result = session.run("MATCH (p:Produto) RETURN p.codigo, p.nome, p.precocompra, p.precovenda");

	        List<ProdutoModel> produtos = result.stream()
	                .map(record -> new ProdutoModel(
	                        record.get("p.codigo").asString(),
	                        record.get("p.nome").asString(),
	                        record.get("p.precocompra").asLong(),
	                        record.get("p.precovenda").asLong()))
	                .collect(Collectors.toList());

	        for (ProdutoModel produto : produtos) {
	            Result estoqueResult = session.run(
	                    "MATCH (e:Estoque {codigo_produto: $codigo}) RETURN e.quantidade AS quantidade",
	                    parameters("codigo", produto.getCodigo()));

	            if (estoqueResult.hasNext()) {
	                Record estoqueRecord = estoqueResult.single();
	                int quantidadeEmEstoque = estoqueRecord.get("quantidade").asInt();
	                produto.setQtdEstoque(quantidadeEmEstoque);
	            } else {
	                produto.setQtdEstoque(0);
	            }
	        }

	        return produtos;
	    } catch (Exception e) {
	        System.out.println("Erro ao buscar produtos: " + e.getMessage());
	        return Collections.emptyList();
	    }
	}


	private List<ClienteModel> buscarTodosClientes() {
		try (Session session = neo4jConnection.getSession()) {
			Result result = session.run("MATCH (c:Cliente) RETURN c.cpf, c.nome");
			return result.stream().map(record -> new ClienteModel(record.get(0).asString(), record.get(1).asString()))
					.collect(Collectors.toList());
		} catch (Exception e) {
			System.out.println("Erro ao buscar clientes: " + e.getMessage());
			return Collections.emptyList();
		}
	}
}