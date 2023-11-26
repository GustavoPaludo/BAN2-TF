package com.estoque.view;

import com.Main;
import com.Neo4jConnection;
import com.estoque.model.EstoqueModel;
import com.produto.model.ProdutoModel;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.util.Scanner;

import static org.neo4j.driver.Values.parameters;

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
            System.out.println("1. Adicionar produto ao estoque");
            System.out.println("2. Remover produto do estoque");
            System.out.println("3. Editar preço de venda");
            System.out.println("4. Listar estoque");
            System.out.println("5. Sair");

            opcao = scanner.nextInt();
            scanner.nextLine();

            switch (opcao) {
                case 1:
                    adicionarProdutoEstoque();
                    break;
                case 2:
                    removerProdutoEstoque();
                    break;
                case 3:
                    editarPrecoVenda();
                    break;
                case 4:
                    listarEstoque();
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

    private void adicionarProdutoEstoque() {
    	this.exibirListaProdutos();
        System.out.println("\nDigite o código do produto:");
        String codigoProduto = scanner.nextLine();

        System.out.println("Digite a quantidade:");
        int quantidade = scanner.nextInt();
        
        System.out.println("Digite o preço de venda:");
        Long precoVenda = scanner.nextLong();

        ProdutoModel produto = buscarProdutoPorCodigo(codigoProduto);
        if (produto != null) {
            Long precoCompra = buscarPrecoCompraProduto(codigoProduto);

            if (precoCompra != null) {
                EstoqueModel novoItemEstoque = new EstoqueModel(codigoProduto, quantidade, precoCompra, precoVenda);

                System.out.println("\nConfirme os dados:");
                System.out.println("Código do produto: " + novoItemEstoque.getCodigoProduto());
                System.out.println("Quantidade: " + novoItemEstoque.getQuantidade());
                System.out.println("Preço de compra: " + novoItemEstoque.getPrecoCompra());

                System.out.println("Deseja adicionar ao estoque? (S/N)");
                String confirmacao = scanner.next().toUpperCase();

                if (confirmacao.equals("S")) {
                    this.adicionarItemEstoque(novoItemEstoque);
                    System.out.println("Produto adicionado ao estoque com sucesso!");
                } else {
                    System.out.println("Operação cancelada.");
                }
            } else {
                System.out.println("Erro ao buscar preço de compra do produto.");
            }
        } else {
            System.out.println("Produto não encontrado!");
        }
    }

    private Long buscarPrecoCompraProduto(String codigoProduto) {
        try (Session session = neo4jConnection.getSession()) {
            Result result = session.run("MATCH (p:Produto {codigo: $codigo}) RETURN p.preco",
                    parameters("codigo", codigoProduto));

            if (result.hasNext()) {
                Record record = result.next();
                return record.get("p.preco").asLong();
            }
        } catch (Exception e) {
            System.out.println("Erro ao buscar preço de compra do produto: " + e.getMessage());
        }
        return null;
    }

    private void removerProdutoEstoque() {
    	this.exibirListaProdutos();
        System.out.println("\nDigite o código do produto a ser removido:");
        String codigoProduto = scanner.nextLine();

        System.out.println("Digite a quantidade a ser removida:");
        int quantidade = scanner.nextInt();

        try (Session session = neo4jConnection.getSession()) {
            String query = "MATCH (e:Estoque {codigo_produto: $codigo}) "
                         + "WHERE e.quantidade >= $quantidade "
                         + "SET e.quantidade = e.quantidade - $quantidade "
                         + "RETURN e";
            Result result = session.run(query, parameters("codigo", codigoProduto, "quantidade", quantidade));
            
            if (result.hasNext()) {
                System.out.println("Produto removido do estoque com sucesso!");
            } else {
                System.out.println("Não há quantidade suficiente desse produto no estoque.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao remover produto do estoque: " + e.getMessage());
        }
    }

    private void editarPrecoVenda() {
    	this.exibirListaProdutos();
        System.out.println("\nDigite o código do produto para editar o preço de venda:");
        String codigoProduto = scanner.nextLine();

        System.out.println("Digite o novo preço de venda:");
        Long novoPrecoVenda = scanner.nextLong();

        try (Session session = neo4jConnection.getSession()) {
            String query = "MATCH (e:Estoque {codigo_produto: $codigo}) "
                         + "SET e.preco_venda = $precoVenda "
                         + "RETURN e";
            Result result = session.run(query, parameters("codigo", codigoProduto, "precoVenda", novoPrecoVenda));
            
            if (result.hasNext()) {
                System.out.println("Preço de venda do produto atualizado com sucesso!");
            } else {
                System.out.println("Produto não encontrado no estoque.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao editar preço de venda: " + e.getMessage());
        }
    }

    private void listarEstoque() {
        try (Session session = neo4jConnection.getSession()) {
            Result result = session.run("MATCH (e:Estoque) RETURN e.codigo_produto, e.quantidade, e.preco_compra, e.preco_venda");
            while (result.hasNext()) {
                Record record = result.next();
                System.out.println("Código do produto: " + record.get("e.codigo_produto").asString());
                System.out.println("Quantidade: " + record.get("e.quantidade").asInt());
                System.out.println("Preço de compra: " + record.get("e.preco_compra").asLong());
                System.out.println("Preço de venda: " + record.get("e.preco_venda").asLong());
            }
        } catch (Exception e) {
            System.out.println("Erro ao listar estoque: " + e.getMessage());
        }
    }

    private void sair() {
        Main.exibirMenuPrincipal();
    }

    public void adicionarItemEstoque(EstoqueModel itemEstoque) {
        try (Session session = neo4jConnection.getSession()) {
            Result result = session.run("MATCH (p:Produto {codigo: $codigo}) "
                    + "MERGE (e:Estoque {codigo_produto: $codigo}) "
                    + "SET e.quantidade = e.quantidade + $quantidade, e.preco_compra = $precoCompra, e.preco_venda = $precoVenda "
                    + "RETURN e",
                    parameters("codigo", itemEstoque.getCodigoProduto(), "quantidade", itemEstoque.getQuantidade(),
                            "precoCompra", itemEstoque.getPrecoCompra(), "precoVenda", itemEstoque.getPrecoVenda()));
            System.out.println(result.consume().counters().nodesCreated());
        } catch (Exception e) {
            System.out.println("Erro ao adicionar produto ao estoque: " + e.getMessage());
        }
    }

    public ProdutoModel buscarProdutoPorCodigo(String codigo) {
        try (Session session = neo4jConnection.getSession()) {
            Result result = session.run("MATCH (p:Produto {codigo: $codigo}) RETURN p.nome, p.preco",
                    parameters("codigo", codigo));

            if (result.hasNext()) {
                Record record = result.next();
                String nome = record.get("p.nome").asString();
                Long preco = record.get("p.preco").asLong();
                return new ProdutoModel(codigo, nome, preco);
            }
        } catch (Exception e) {
            System.out.println("Erro ao buscar produto no estoque: " + e.getMessage());
        }
        return null;
    }
    
    private void exibirListaProdutos() {
        try (Session session = neo4jConnection.getSession()) {
            Result result = session.run("MATCH (p:Produto) RETURN p.codigo, p.nome, p.preco");
            System.out.println("\nLista de produtos:");
            while (result.hasNext()) {
                Record record = result.next();
                System.out.println("Código: " + record.get("p.codigo").asString() + " - Nome: " + record.get("p.nome").asString() + " - Preço de compra: " + record.get("p.preco").asString());
            }
        } catch (Exception e) {
            System.out.println("Erro ao listar produtos: " + e.getMessage());
        }
    }
}