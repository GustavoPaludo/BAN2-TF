package com.produto.model;

public class ProdutoModel {

    private String codigo;
    private String nome;
    private Long precocompra;
    private Long precovenda;
    private Integer qtdEstoque;

    public ProdutoModel(String codigo, String nome, Long precocompra, Long precovenda) {
        this.setCodigo(codigo);
        this.nome = nome;
        this.setPrecoCompra(precocompra);
        this.setPrecoVenda(precovenda);
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public Long getPrecoCompra() {
		return precocompra;
	}

	public void setPrecoCompra(Long precocompra) {
		this.precocompra = precocompra;
	}

	public Long getPrecoVenda() {
		return precovenda;
	}

	public void setPrecoVenda(Long precovenda) {
		this.precovenda = precovenda;
	}

	@Override
	public String toString() {
	    return "Código: " + this.getCodigo() + ", Nome: " + this.getNome() + ", Preço Compra: " + this.getPrecoCompra() + ", Preço Venda: " + this.getPrecoVenda();
	}

	public Integer getQtdEstoque() {
		return qtdEstoque;
	}

	public void setQtdEstoque(Integer qtdEstoque) {
		this.qtdEstoque = qtdEstoque;
	}
}