package com.estoque.model;

public class EstoqueModel {

    private String codigoProduto;
    private Integer quantidade;
    private Long precoCompra;
    private Long precoVenda;

    public EstoqueModel(String codigoProduto, Integer quantidade, Long precoCompra, Long precoVenda) {
        this.setCodigoProduto(codigoProduto);
        this.setQuantidade(quantidade);
        this.setPrecoCompra(precoCompra);
        this.setPrecoVenda(precoVenda);
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

	public Long getPrecoCompra() {
		return precoCompra;
	}

	public void setPrecoCompra(Long precoCompra) {
		this.precoCompra = precoCompra;
	}

	public Long getPrecoVenda() {
		return precoVenda;
	}

	public void setPrecoVenda(Long precoVenda) {
		this.precoVenda = precoVenda;
	}
}