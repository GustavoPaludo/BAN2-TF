package com.estoque.view;

import com.Main;
import com.Neo4jConnection;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.util.Scanner;

public class Estoque {

	private Neo4jConnection neo4jConnection;
	private Scanner scanner;

	public Estoque() {
		this.neo4jConnection = new Neo4jConnection();
		this.scanner = new Scanner(System.in);
	}

	public void exibirMenu() {
		int opcao;
		do {
			System.out.println("\nSelecione uma ação:");
			System.out.println("1. Listar estoque");
			System.out.println("2. Sair");

			opcao = scanner.nextInt();
			scanner.nextLine();

			switch (opcao) {
			case 1:
				listarEstoque();
				break;
			case 2:
				sair();
				break;
			default:
				System.out.println("Opção inválida!");
				break;
			}
		} while (opcao != 2);
	}

	private void listarEstoque() {
		try (Session session = neo4jConnection.getSession()) {
			Result result = session.run("MATCH (e:Estoque) RETURN e.codigo_produto, e.quantidade");
			while (result.hasNext()) {
				Record record = result.next();
				System.out.println("Código do produto: " + record.get("e.codigo_produto").asString());
				System.out.println("Quantidade: " + record.get("e.quantidade").asInt());
			}
		} catch (Exception e) {
			System.out.println("Erro ao listar estoque: " + e.getMessage());
		}
	}

	private void sair() {
		Main.exibirMenuPrincipal();
	}
}