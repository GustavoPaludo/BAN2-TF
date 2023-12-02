package com.estoque.model;

public class EstoqueModel {

    private String codigoProduto;
    private Integer quantidade;

    public EstoqueModel(String codigoProduto, Integer quantidade) {
        this.setCodigoProduto(codigoProduto);
        this.setQuantidade(quantidade);
    }

	public String getCodigoProduto() {
		return codigoProduto;
	}

	public void setCodigoProduto(String codigoProduto) {
		this.codigoProduto = codigoProduto;
	}

	public Integer getQuantidade() {
		return quantidade;
	}

	public void setQuantidade(Integer quantidade) {
		this.quantidade = quantidade;
	}
}