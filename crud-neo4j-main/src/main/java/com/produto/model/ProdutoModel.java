package com.produto.model;

public class ProdutoModel {

    private String codigo;
    private String nome;
    private Long preco;

    public ProdutoModel(String codigo, String nome, Long preco) {
        this.setCodigo(codigo);
        this.nome = nome;
        this.setPreco(preco);
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

	public Long getPreco() {
		return preco;
	}

	public void setPreco(Long preco) {
		this.preco = preco;
	}

	@Override
	public String toString() {
	    return "Código: " + this.getCodigo() + ", Nome: " + this.getNome() + ", Preço: " + this.getPreco();
	}
}