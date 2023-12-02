package com.produto.view;

import com.Main;
import com.Neo4jConnection;
import com.produto.model.ProdutoModel;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import static org.neo4j.driver.Values.parameters;

public class Produto {

	private Neo4jConnection neo4jConnection;
	private Scanner scanner;

	public Produto() {
		this.neo4jConnection = new Neo4jConnection();
		this.scanner = new Scanner(System.in);
	}

	public void exibirMenu() {
		int opcao;
		do {
			System.out.println("\nSelecione uma ação:");
			System.out.println("1. Incluir produto");
			System.out.println("2. Editar produto");
			System.out.println("3. Listar produtos");
			System.out.println("4. Excluir produto");
			System.out.println("5. Sair");

			opcao = scanner.nextInt();
			scanner.nextLine();

			switch (opcao) {
			case 1:
				incluirProduto();
				break;
			case 2:
				editarProduto();
				break;
			case 3:
				listarProdutos();
				break;
			case 4:
				excluirProduto();
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

	private void incluirProduto() {
		System.out.println("\nLista de produtos cadastrados:");
		List<ProdutoModel> produtos = this.buscarTodosProdutos();
		for (ProdutoModel produto : produtos) {
			System.out.println(produto.getCodigo() + " - " + produto.getNome());
		}

		System.out.println("\nDigite o código que deseja adicionar:");
		String codigo = scanner.nextLine();

		System.out.println("Digite o nome:");
		String descricao = scanner.nextLine();

		System.out.println("Digite o preço de compra:");
		Long precocompra = scanner.nextLong();
		scanner.nextLine();

		System.out.println("Digite o preço de venda:");
		Long precovenda = scanner.nextLong();
		scanner.nextLine();

		ProdutoModel novoProduto = new ProdutoModel(codigo, descricao, precocompra, precovenda);

		System.out.println("\nConfirme os dados:");
		System.out.println("Código: " + novoProduto.getCodigo());
		System.out.println("Nome: " + novoProduto.getNome());
		System.out.println("Preço de compra: " + novoProduto.getPrecoCompra());
		System.out.println("Preço de venda: " + novoProduto.getPrecoVenda());

		System.out.println("Deseja salvar? (S/N)");
		String confirmacao = scanner.nextLine().toUpperCase();

		if (confirmacao.equals("S")) {
			this.adicionarProduto(novoProduto);
			System.out.println("Produto adicionado com sucesso!");
		} else {
			System.out.println("Operação cancelada.");
		}
	}

	private void editarProduto() {
		System.out.println("\nLista de produtos:");
		List<ProdutoModel> produtos = this.buscarTodosProdutos();
		for (ProdutoModel produto : produtos) {
			System.out.println(produto.getCodigo() + " - " + produto.getNome());
		}

		System.out.println("\nDigite o código do produto que deseja editar:");
		String codigo = scanner.nextLine();

		ProdutoModel produto = this.buscarProdutoPorCodigo(codigo);

		if (produto != null) {
			System.out.println("Produto encontrado:");
			System.out.println("Nome: " + produto.getNome());
			System.out.println("Novo nome:");
			String novoNome = scanner.nextLine();

			System.out.println("Preço de compra: " + produto.getPrecoCompra());
			System.out.println("Novo preço de compra:");
			Long precocompra = scanner.nextLong();
			scanner.nextLine();

			System.out.println("Preço de venda: " + produto.getPrecoVenda());
			System.out.println("Novo preço de Venda:");
			Long precovenda = scanner.nextLong();
			scanner.nextLine();

			produto.setNome(novoNome);
			produto.setPrecoCompra(precocompra);
			produto.setPrecoVenda(precovenda);
			this.editarProduto(produto);
			System.out.println("Produto editado com sucesso!");
		} else {
			System.out.println("Produto não encontrado!");
		}
	}

	private void listarProdutos() {
		List<ProdutoModel> produtos = this.buscarTodosProdutos();
		System.out.println("\nLista de produtos:");
		for (ProdutoModel produto : produtos) {
			System.out.println(produto.getCodigo() + " - " + produto.getNome() + " - Preço de Compra: "
					+ produto.getPrecoCompra() + " - Preço de Venda: " + produto.getPrecoVenda());
		}
	}

	private void excluirProduto() {
		System.out.println("\nLista de produtos:");
		List<ProdutoModel> produtos = this.buscarTodosProdutos();
		for (ProdutoModel produto : produtos) {
			System.out.println(produto.getCodigo() + " - " + produto.getNome());
		}

		System.out.println("\nDigite o código do produto que deseja excluir:");
		String codigo = scanner.nextLine();

		ProdutoModel produto = this.buscarProdutoPorCodigo(codigo);

		if (produto != null) {
			this.excluirProduto(codigo);
			System.out.println("Produto excluído com sucesso!");
		} else {
			System.out.println("Produto não encontrado!");
		}
	}

	private void sair() {
		Main.exibirMenuPrincipal();
	}

	public void adicionarProduto(ProdutoModel produto) {
		try (Session session = neo4jConnection.getSession()) {
			Result result = session.run(
					"CREATE (p:Produto {codigo: $codigo, nome: $nome, precocompra: $precocompra, precovenda: $precovenda}) "
							+ "WITH p " + "CREATE (e:Estoque {codigo_produto: $codigo, quantidade: 0}) "
							+ "CREATE (p)-[:POSSUI]->(e) " + "RETURN p",
					parameters("codigo", produto.getCodigo(), "nome", produto.getNome(), "precocompra",
							produto.getPrecoCompra(), "precovenda", produto.getPrecoVenda()));
			System.out.println(result.consume().counters().nodesCreated());
		}
	}

	public void editarProduto(ProdutoModel produto) {
		try (Session session = neo4jConnection.getSession()) {
			session.run(
					"MATCH (p:Produto {codigo: $codigo}) SET p.nome = $nome, p.precocompra = $precocompra, p.precovenda = $precovenda",
					parameters("codigo", produto.getCodigo(), "nome", produto.getNome(), "precocompra",
							produto.getPrecoCompra(), "precovenda", produto.getPrecoVenda()));
			System.out.println("Produto atualizado com sucesso!");
		}
	}

	public void excluirProduto(String codigo) {
		try (Session session = neo4jConnection.getSession()) {
			Result resultproduto = session.run("MATCH (p:Produto {codigo: $codigo}) DETACH DELETE p",
					parameters("codigo", codigo));
			Result resultestoque = session.run("MATCH (e:Estoque {codigo_produto: $codigo_produto}) DETACH DELETE e",
					parameters("codigo_produto", codigo));
			System.out.println(resultproduto.consume().counters().nodesDeleted() + resultestoque.consume().counters().nodesDeleted());
		}
	}
	
	public List<ProdutoModel> buscarTodosProdutos() {
		try (Session session = neo4jConnection.getSession()) {
			Result result = session.run("MATCH (p:Produto) RETURN p.codigo, p.nome, p.precocompra, p.precovenda");
			return result.stream().map(record -> new ProdutoModel(record.get(0).asString(), record.get(1).asString(),
					record.get(2).asLong(), record.get(3).asLong())).collect(Collectors.toList());
		}
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
}