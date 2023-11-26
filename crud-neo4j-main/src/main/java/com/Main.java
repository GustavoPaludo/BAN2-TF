package com;

import com.cliente.view.Cliente;
import com.compra.view.Compra;
import com.estoque.view.Estoque;
import com.fornecedor.view.Fornecedor;
//import com.funcionario.view.Funcionario;
import com.funcionario.view.Funcionario;
import com.produto.view.Produto;
import com.relatorio.view.Relatorio;
import com.venda.view.Venda;

import java.util.Scanner;

public class Main {

    private static Scanner scanner;
    private static Cliente cliente;
    private static Funcionario funcionario;
    private static Fornecedor fornecedor;
    private static Estoque estoque;
    private static Produto produto;
    private static Compra compra;
    private static Venda venda;
    private static Relatorio relatorio;

    public Main() {
        scanner = new Scanner(System.in);
        cliente = new Cliente();
        funcionario = new Funcionario();
        fornecedor = new Fornecedor();
        estoque = new Estoque();
        produto = new Produto();
        compra = new Compra();
        venda = new Venda();
        relatorio = new Relatorio();
    }

    public static void main(String[] args) {

        @SuppressWarnings("unused")
		Main main = new Main();
        Main.exibirMenuPrincipal();
    }

    public static void exibirMenuPrincipal() {
        int opcao;
        do {
            System.out.println("\nSelecione uma ação:");
            System.out.println("1. Clientes");
            System.out.println("2. Funcionarios");
            System.out.println("3. Fornecedores");
            System.out.println("4. Estoque");
            System.out.println("5. Produtos");
            System.out.println("6. Compra");
            System.out.println("7. Venda");
            System.out.println("8. Relatorios");
            System.out.println("9. Sair");

            opcao = scanner.nextInt();
            scanner.nextLine();

            switch (opcao) {
                case 1:
                    cliente.exibirMenu();
                    break;
                case 2:
             	   	funcionario.exibirMenu();
                    break;
                case 3:
             	    fornecedor.exibirMenu();
                    break;
                case 4:
             	   	estoque.exibirMenu();
                    break;
                case 5:
             	    produto.exibirMenu();
                    break;
                case 6:
             	   	compra.exibirMenu();
                    break;
                case 7:
                	venda.exibirMenu();
                    break;
                case 8:
                   	relatorio.exibirMenu();
                    break;
                case 9:
                    sair();
                    break;
                default:
                    System.out.println("Opção inválida!");
                    break;
            }
        } while (opcao != 9);
    }

    public static void sair() {
        System.out.println("Encerrando o programa.");
        System.exit(0);
    }
}