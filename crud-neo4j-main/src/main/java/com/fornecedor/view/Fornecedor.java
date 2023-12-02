package com.fornecedor.view;

import com.Main;
import com.Neo4jConnection;
import com.fornecedor.model.FornecedorModel;
import com.produto.model.ProdutoModel;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import static org.neo4j.driver.Values.parameters;

public class Fornecedor {

	private Neo4jConnection neo4jConnection;
	private Scanner scanner;

	public Fornecedor() {
		this.neo4jConnection = new Neo4jConnection();
		this.scanner = new Scanner(System.in);
	}

	public void exibirMenu() {
		int opcao;
		do {
			System.out.println("\nSelecione uma ação:");
			System.out.println("1. Incluir fornecedor");
			System.out.println("2. Editar fornecedor");
			System.out.println("3. Listar fornecedores");
			System.out.println("4. Excluir fornecedor");
			System.out.println("5. Sair");

			opcao = scanner.nextInt();
			scanner.nextLine();

			switch (opcao) {
			case 1:
				incluirFornecedor();
				break;
			case 2:
				editarFornecedor();
				break;
			case 3:
				listarFornecedores();
				break;
			case 4:
				excluirFornecedor();
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

	private void incluirFornecedor() {
		System.out.println("\nDigite o CNPJ:");
		String cnpj = scanner.nextLine();

		System.out.println("Digite o nome:");
		String nome = scanner.nextLine();

		System.out.println("\nLista de produtos disponíveis:");
		List<ProdutoModel> produtos = buscarTodosProdutos();
		for (ProdutoModel produto : produtos) {
			System.out.println(produto.getCodigo() + " - " + produto.getNome() + " - Preço de Compra: "
					+ produto.getPrecoCompra() + " - Preço de Venda: " + produto.getPrecoVenda());
		}

		System.out.println("Digite os produtos associados (códigos separados por vírgula):");
		String produtosInput = scanner.nextLine();
		String[] produtosArray = produtosInput.split(",");
		List<ProdutoModel> produtosSelecionados = new ArrayList<>();

		for (String codigoProduto : produtosArray) {
			ProdutoModel produto = buscarProdutoPorCodigo(codigoProduto.trim());
			if (produto != null) {
				produtosSelecionados.add(produto);
			}
		}

		FornecedorModel novoFornecedor = new FornecedorModel(cnpj, nome, produtosSelecionados);

		System.out.println("\nConfirme os dados:");
		System.out.println("CNPJ: " + novoFornecedor.getCnpj());
		System.out.println("Nome: " + novoFornecedor.getNome());
		System.out.println("Produtos associados: " + novoFornecedor.getProdutos());

		System.out.println("Deseja salvar? (S/N)");
		String confirmacao = scanner.nextLine().toUpperCase();

		if (confirmacao.equals("S")) {
			this.adicionarFornecedor(novoFornecedor);
			System.out.println("Fornecedor adicionado com sucesso!");
		} else {
			System.out.println("Operação cancelada.");
		}
	}

	private void listarFornecedores() {
		List<FornecedorModel> fornecedores = this.buscarTodosFornecedores();
		System.out.println("\nLista de fornecedores:");
		for (FornecedorModel fornecedor : fornecedores) {
			System.out.println(
					fornecedor.getCnpj() + " - " + fornecedor.getNome() + " - Produtos: " + fornecedor.getProdutos());
		}
	}

	private void excluirFornecedor() {
		System.out.println("\nLista de fornecedores:");
		List<FornecedorModel> fornecedores = this.buscarTodosFornecedores();
		for (FornecedorModel fornecedor : fornecedores) {
			System.out.println(fornecedor.getCnpj() + " - " + fornecedor.getNome());
		}

		System.out.println("\nDigite o CNPJ do fornecedor que deseja excluir:");
		String cnpj = scanner.nextLine();

		FornecedorModel fornecedor = this.buscarFornecedorPorCNPJ(cnpj);

		if (fornecedor != null) {
			this.excluirFornecedor(cnpj);
			System.out.println("Fornecedor excluído com sucesso!");
		} else {
			System.out.println("Fornecedor não encontrado!");
		}
	}

	private void sair() {
		Main.exibirMenuPrincipal();
	}

	public void adicionarFornecedor(FornecedorModel fornecedor) {
		try (Session session = neo4jConnection.getSession()) {
			List<String> produtosCodigos = fornecedor.getProdutos().stream().map(ProdutoModel::getCodigo)
					.collect(Collectors.toList());

			Result result = session.run(
					"CREATE (f:Fornecedor {cnpj: $cnpj, nome: $nome, produtos: $produtos}) "
							+ "WITH f UNWIND $produtos AS produtoCodigo " + "MATCH (p:Produto {codigo: produtoCodigo}) "
							+ "MERGE (f)-[:Fornece]->(p)",
					parameters("cnpj", fornecedor.getCnpj(), "nome", fornecedor.getNome(), "produtos",
							produtosCodigos));
			System.out.println(result.consume().counters().nodesCreated());
		}
	}

	public void editarFornecedor(FornecedorModel fornecedor) {
		try (Session session = neo4jConnection.getSession()) {
			List<String> produtosCodigos = fornecedor.getProdutos().stream().map(ProdutoModel::getCodigo)
					.collect(Collectors.toList());

			session.run("MATCH (f:Fornecedor {cnpj: $cnpj}) SET f.nome = $nome, f.produtos = $produtos", parameters(
					"cnpj", fornecedor.getCnpj(), "nome", fornecedor.getNome(), "produtos", produtosCodigos));
			System.out.println("Fornecedor atualizado com sucesso!");
		}
	}

	public void excluirFornecedor(String cnpj) {
		try (Session session = neo4jConnection.getSession()) {
			Result result = session.run("MATCH (f:Fornecedor {cnpj: $cnpj}) DETACH DELETE f", parameters("cnpj", cnpj));
			System.out.println(result.consume().counters().nodesDeleted());
		}
	}

	public List<FornecedorModel> buscarTodosFornecedores() {
		try (Session session = neo4jConnection.getSession()) {
			Result result = session.run("MATCH (f:Fornecedor) RETURN f.cnpj, f.nome, f.produtos");
			return result.stream().map(record -> {
				String cnpj = record.get(0).asString();
				String nome = record.get(1).asString();
				List<String> produtosCodigos = record.get(2).asList(Value::asString);
				List<ProdutoModel> produtos = produtosCodigos.stream().map(this::buscarProdutoPorCodigo)
						.filter(produto -> produto != null).collect(Collectors.toList());
				return new FornecedorModel(cnpj, nome, produtos);
			}).collect(Collectors.toList());
		}
	}

	public FornecedorModel buscarFornecedorPorCNPJ(String cnpj) {
		try (Session session = neo4jConnection.getSession()) {
			Result result = session.run("MATCH (f:Fornecedor {cnpj: $cnpj}) RETURN f.nome, f.produtos",
					parameters("cnpj", cnpj));

			if (result.hasNext()) {
				Record record = result.next();
				String nome = record.get("f.nome").asString();
				List<String> produtosCodigos = record.get("f.produtos").asList(Value::asString);
				List<ProdutoModel> produtos = produtosCodigos.stream().map(this::buscarProdutoPorCodigo)
						.filter(produto -> produto != null).collect(Collectors.toList());
				return new FornecedorModel(cnpj, nome, produtos);
			}
		}
		return null;
	}

	public ProdutoModel buscarProdutoPorCodigo(String codigo) {
		try (Session session = neo4jConnection.getSession()) {
			Result result = session.run(
					"MATCH (p:Produto {codigo: $codigo}) RETURN p.nome, p.precocompra, p.precovenda",
					parameters("codigo", codigo));

			if (result.hasNext()) {
				Record record = result.next();
				String nome = record.get("p.nome").asString();
				Long precocompra = record.get("p.precocompra").asLong();
				Long precovenda = record.get("p.precovenda").asLong();
				return new ProdutoModel(codigo, nome, precocompra, precovenda);
			}
		}
		return null;
	}

	private void editarFornecedor() {
		System.out.println("\nLista de fornecedores:");
		List<FornecedorModel> fornecedores = buscarTodosFornecedores();
		for (FornecedorModel fornecedor : fornecedores) {
			System.out.println(fornecedor.getCnpj() + " - " + fornecedor.getNome());
		}

		System.out.println("\nDigite o CNPJ do fornecedor que deseja editar:");
		String cnpj = scanner.nextLine();

		FornecedorModel fornecedor = buscarFornecedorPorCNPJ(cnpj);

		if (fornecedor != null) {
			System.out.println("Fornecedor encontrado:");
			System.out.println("Nome: " + fornecedor.getNome());
			System.out.println("Produtos associados: " + fornecedor.getProdutos());

			System.out.println("\nLista de produtos disponíveis:");
			List<ProdutoModel> produtos = buscarTodosProdutos();
			for (ProdutoModel produto : produtos) {
				System.out.println(produto.getCodigo() + " - " + produto.getNome() + " - Preço de Compra: "
						+ produto.getPrecoCompra() + " - Preço de Compra: " + produto.getPrecoCompra());
			}

			System.out.println("Digite os produtos a serem adicionados (códigos separados por vírgula):");
			String produtosAdicionados = scanner.nextLine();
			String[] produtosAdicionadosArray = produtosAdicionados.split(",");
			List<ProdutoModel> produtosSelecionados = new ArrayList<>();

			for (String codigoProduto : produtosAdicionadosArray) {
				ProdutoModel produto = buscarProdutoPorCodigo(codigoProduto.trim());
				if (produto != null && !fornecedor.getProdutos().contains(produto)) {
					produtosSelecionados.add(produto);
				}
			}

			System.out.println("Digite os produtos a serem removidos (códigos separados por vírgula):");
			String produtosRemovidos = scanner.nextLine();
			String[] produtosRemovidosArray = produtosRemovidos.split(",");

			for (String codigoProduto : produtosRemovidosArray) {
				ProdutoModel produto = buscarProdutoPorCodigo(codigoProduto.trim());
				if (produto != null && fornecedor.getProdutos().contains(produto)) {
					fornecedor.removerProduto(produto);
				}
			}

			for (ProdutoModel produto : produtosSelecionados) {
				fornecedor.adicionarProduto(produto);
			}

			this.editarFornecedor(fornecedor);
			System.out.println("Fornecedor editado com sucesso!");
		} else {
			System.out.println("Fornecedor não encontrado!");
		}
	}

	private List<ProdutoModel> buscarTodosProdutos() {
		try (Session session = neo4jConnection.getSession()) {
			Result result = session.run("MATCH (p:Produto) RETURN p.codigo, p.nome, p.precocompra, p.precovenda");
			return result.stream().map(record -> new ProdutoModel(record.get(0).asString(), record.get(1).asString(),
					record.get(2).asLong(), record.get(3).asLong())).collect(Collectors.toList());
		}
	}
}