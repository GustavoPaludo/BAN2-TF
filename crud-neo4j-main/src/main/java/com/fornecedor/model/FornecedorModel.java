package com.fornecedor.model;

import java.util.List;

import com.produto.model.ProdutoModel;

public class FornecedorModel {

    private String cnpj;
    private String nome;
    private List<ProdutoModel> produtos;
    
    public FornecedorModel(String cnpj, String nome, List<ProdutoModel> produtos) {
        this.setCnpj(cnpj);
        this.nome = nome;
        this.setProdutos(produtos);
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

	public String getCnpj() {
		return cnpj;
	}

	public void setCnpj(String cnpj) {
		this.cnpj = cnpj;
	}

	public List<ProdutoModel> getProdutos() {
		return produtos;
	}

	public void setProdutos(List<ProdutoModel> produtos) {
		this.produtos = produtos;
	}
	
	public void adicionarProduto(ProdutoModel produto) {
        this.produtos.add(produto);
    }

	public void removerProduto(ProdutoModel produto) {
	    this.produtos.removeIf(p -> p.getCodigo().equals(produto.getCodigo()));
	}
}