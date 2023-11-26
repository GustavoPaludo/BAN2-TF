package com.compra.model;

import java.time.LocalDate;
import java.util.Map;

public class CompraModel {
    private LocalDate dataPedido;
    private String matriculaFuncionario;
    private String cnpjFornecedor;
    private Map<String, Integer> produtosQuantidades;

    public CompraModel(LocalDate dataPedido, Map<String, Integer> produtosQuantidades, String matriculaFuncionario, String cnpjFornecedor) {
        this.dataPedido = dataPedido;
        this.matriculaFuncionario = matriculaFuncionario;
        this.cnpjFornecedor = cnpjFornecedor;
        this.produtosQuantidades = produtosQuantidades;
    }

    public LocalDate getDataPedido() {
        return dataPedido;
    }

    public void setDataPedido(LocalDate dataPedido) {
        this.dataPedido = dataPedido;
    }

    public String getMatriculaFuncionario() {
        return matriculaFuncionario;
    }

    public void setMatriculaFuncionario(String matriculaFuncionario) {
        this.matriculaFuncionario = matriculaFuncionario;
    }

    public String getCnpjFornecedor() {
        return cnpjFornecedor;
    }

    public void setCnpjFornecedor(String cnpjFornecedor) {
        this.cnpjFornecedor = cnpjFornecedor;
    }

    public Map<String, Integer> getProdutosQuantidades() {
        return produtosQuantidades;
    }

    public void setProdutosQuantidades(Map<String, Integer> produtosQuantidades) {
        this.produtosQuantidades = produtosQuantidades;
    }
}