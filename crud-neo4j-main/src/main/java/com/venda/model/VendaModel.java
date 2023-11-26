package com.venda.model;

import java.time.LocalDate;
import java.util.Map;

public class VendaModel {
    private LocalDate dataPedido;
    private String matriculaFuncionario;
    private String cpfCliente;
    private Map<String, Integer> produtosQuantidades;

    public VendaModel(LocalDate dataPedido, Map<String, Integer> produtosQuantidades, String matriculaFuncionario, String cpfCliente) {
        this.dataPedido = dataPedido;
        this.matriculaFuncionario = matriculaFuncionario;
        this.cpfCliente = cpfCliente;
        this.produtosQuantidades = produtosQuantidades;
    }

    public LocalDate getDataVenda() {
        return dataPedido;
    }

    public void setDataVenda(LocalDate dataPedido) {
        this.dataPedido = dataPedido;
    }

    public String getMatriculaFuncionario() {
        return matriculaFuncionario;
    }

    public void setMatriculaFuncionario(String matriculaFuncionario) {
        this.matriculaFuncionario = matriculaFuncionario;
    }

    public String getCpfCliente() {
        return cpfCliente;
    }

    public void setCpfCliente(String cpfCliente) {
        this.cpfCliente = cpfCliente;
    }

    public Map<String, Integer> getProdutosQuantidades() {
        return produtosQuantidades;
    }

    public void setProdutosQuantidades(Map<String, Integer> produtosQuantidades) {
        this.produtosQuantidades = produtosQuantidades;
    }
}
