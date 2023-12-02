package com.relatorio.view;

import com.Main;
import com.Neo4jConnection;
import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.neo4j.driver.*;
import org.neo4j.driver.Record;

public class Relatorio {

    private Neo4jConnection neo4jConnection;
    private Scanner scanner;

    public Relatorio() {
        this.neo4jConnection = new Neo4jConnection();
        this.scanner = new Scanner(System.in);
    }

    public void exibirMenu() {
        int opcao;
        do {
            System.out.println("\nSelecione uma ação:");
            System.out.println("1. Relatorio de Compras");
            System.out.println("2. Relatorio de Vendas");
            System.out.println("3. Relatorio de Movimentações de Estoque");
            System.out.println("4. Sair");

            opcao = scanner.nextInt();
            scanner.nextLine();

            switch (opcao) {
                case 1:
                    relatorioCompra();
                    break;
                case 2:
                    relatorioVendas();
                    break;
                case 3:
                	relatorioMovimentacoes();
                	break;
                case 4:
                    sair();
                    break;
                default:
                    System.out.println("Opção inválida!");
                    break;
            }
        } while (opcao != 4);
    }

    private void exportarParaCSV(String nomeArquivo, List<String[]> linhas) {
        String caminhoRelativo = "src/main/java/com/relatorio/relatorios/";
        String caminhoCompleto = caminhoRelativo + nomeArquivo;
        try (CSVWriter writer = new CSVWriter(new FileWriter(caminhoCompleto))) {
            writer.writeAll(linhas);
        } catch (IOException e) {
            System.out.println("Erro ao exportar para CSV: " + e.getMessage());
        }
    }

    public void relatorioCompra() {
        try (Session session = neo4jConnection.getSession()) {
            Result result = session.run("MATCH (c:Compra)-[contem:Contem]->(p:Produto) "
                    + "RETURN ID(c) AS CompraID, c.dataCompra AS Data, p.nome AS Produto, p.precocompra AS PrecoCompra, contem.quantidade AS Quantidade");

            System.out.println("Relatório de Compras:");
            double totalCompras = 0.0;
            List<String[]> linhas = new ArrayList<>();
            linhas.add(new String[]{"ID", "Data", "Produto", "Preço Compra", "Quantidade", "Total do Produto"});

            while (result.hasNext()) {
                Record record = result.next();
                long compraID = record.get("CompraID").asLong();
                LocalDate data = LocalDate.parse(record.get("Data").asString());
                String produto = record.get("Produto").asString();
                double precocompra = record.get("PrecoCompra").asDouble();
                int quantidade = record.get("Quantidade").asInt();
                double totalProduto = precocompra * quantidade;
                totalCompras += totalProduto;

                System.out.println("ID: " + compraID + " - Data: " + data + " - Produto: " + produto + " - Preço de Compra: " + precocompra +
                        " - Quantidade: " + quantidade + " - Total do Produto: " + totalProduto);

                linhas.add(new String[]{String.valueOf(compraID), data.toString(), produto, String.valueOf(precocompra), String.valueOf(quantidade), String.valueOf(totalProduto)});
            }
            System.out.println("Total de todas as compras: " + totalCompras);

            LocalDateTime agora = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
            String dataFormatada = agora.format(formatter);

            String nomeArquivo = "relatorio_compras_" + dataFormatada + ".csv";

            exportarParaCSV(nomeArquivo, linhas);
        } catch (Exception e) {
            System.out.println("Erro ao gerar relatório de compras: " + e.getMessage());
        }
    }

    public void relatorioVendas() {
        try (Session session = neo4jConnection.getSession()) {
            Result result = session.run("MATCH (v:Venda)-[contem:Contem]->(p:Produto) "
                    + "RETURN ID(v) AS VendaID, v.dataVenda AS Data, p.nome AS Produto, p.precovenda AS PrecoVenda, contem.quantidade AS Quantidade");

            System.out.println("Relatório de Vendas:");
            double totalVendas = 0.0;
            List<String[]> linhas = new ArrayList<>();
            linhas.add(new String[]{"ID", "Data", "Produto", "Preco Venda", "Quantidade", "Total do Produto"});

            while (result.hasNext()) {
                Record record = result.next();
                long vendaID = record.get("VendaID").asLong();
                LocalDate data = LocalDate.parse(record.get("Data").asString());
                String produto = record.get("Produto").asString();
                double precovenda = record.get("PrecoVenda").asDouble();
                int quantidade = record.get("Quantidade").asInt();
                double totalProduto = precovenda * quantidade;
                totalVendas += totalProduto;

                System.out.println("ID: " + vendaID + " - Data: " + data + " - Produto: " + produto + " - Preço de Venda: " + precovenda +
                        " - Quantidade: " + quantidade + " - Total do Produto: " + totalProduto);

                linhas.add(new String[]{String.valueOf(vendaID), data.toString(), produto, String.valueOf(precovenda), String.valueOf(quantidade), String.valueOf(totalProduto)});
            }
            System.out.println("Total de todas as vendas: " + totalVendas);
            LocalDateTime agora = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
            String dataFormatada = agora.format(formatter);

            String nomeArquivo = "relatorio_vendas_" + dataFormatada + ".csv";

            exportarParaCSV(nomeArquivo, linhas);
        } catch (Exception e) {
            System.out.println("Erro ao gerar relatório de vendas: " + e.getMessage());
        }
    }

    public void relatorioMovimentacoes() {
        try (Session session = neo4jConnection.getSession()) {
        	Result resultCompra = session.run("MATCH (mov:Compra)-[contem:Contem]->(p:Produto) "
                    + "RETURN ID(mov) AS MovimentacaoID, mov.dataCompra AS Data, p.nome AS Produto, p.precocompra AS PrecoCompra, contem.quantidade AS Quantidade");

            Result resultVenda = session.run("MATCH (mov:Venda)-[contem:Contem]->(p:Produto) "
                    + "RETURN ID(mov) AS MovimentacaoID, mov.dataVenda AS Data, p.nome AS Produto, p.precovenda AS PrecoVenda, contem.quantidade AS Quantidade");

            System.out.println("Relatorio de Movimentacoes de Estoque:");
            double totalEntrada = 0.0;
            double totalSaida = 0.0;
            List<String[]> linhas = new ArrayList<>();
            linhas.add(new String[]{"ID", "Tipo", "Data", "Produto", "Preco", "Quantidade", "Total do Produto"});

            while (resultCompra.hasNext()) {
                Record record = resultCompra.next();
                long movimentacaoID = record.get("MovimentacaoID").asLong();
                LocalDate data = LocalDate.parse(record.get("Data").asString());
                String produto = record.get("Produto").asString();
                double precocompra = record.get("PrecoCompra").asDouble();
                int quantidade = record.get("Quantidade").asInt();
                double totalProduto = precocompra * quantidade;

                totalEntrada += totalProduto;
                System.out.println("ID: " + movimentacaoID + " - Entrada - Data: " + data + " - Produto: " + produto + " - Preco de Compra: " + precocompra +
                        " - Quantidade: " + quantidade + " - Total do Produto: " + totalProduto);

                linhas.add(new String[]{String.valueOf(movimentacaoID), "Entrada", data.toString(), produto, String.valueOf(precocompra), String.valueOf(quantidade), String.valueOf(totalProduto)});
            }

            while (resultVenda.hasNext()) {
                Record record = resultVenda.next();
                long movimentacaoID = record.get("MovimentacaoID").asLong();
                LocalDate data = LocalDate.parse(record.get("Data").asString());
                String produto = record.get("Produto").asString();
                double precovenda = record.get("PrecoVenda").asDouble();
                int quantidade = record.get("Quantidade").asInt();
                double totalProduto = precovenda * quantidade;

                totalSaida += totalProduto;
                System.out.println("ID: " + movimentacaoID + " - Saida - Data: " + data + " - Produto: " + produto + " - Preco: " + precovenda +
                        " - Quantidade: " + quantidade + " - Total do Produto: " + totalProduto);

                linhas.add(new String[]{String.valueOf(movimentacaoID), "Saida", data.toString(), produto, String.valueOf(precovenda), String.valueOf(quantidade), String.valueOf(totalProduto)});
            }

            System.out.println("Total de Entrada no Estoque: " + totalEntrada);
            System.out.println("Total de Saída no Estoque: " + totalSaida);

            LocalDateTime agora = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
            String dataFormatada = agora.format(formatter);

            String nomeArquivo = "relatorio_movimentacoes_" + dataFormatada + ".csv";

            exportarParaCSV(nomeArquivo, linhas);
        } catch (Exception e) {
            System.out.println("Erro ao gerar relatório de movimentações de estoque: " + e.getMessage());
        }
    }

    private void sair() {
        Main.exibirMenuPrincipal();
    }
}