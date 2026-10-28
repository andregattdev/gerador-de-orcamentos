package com.app.gerador_de_orcamento.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class PrestadorServico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Dados da empresa/prestador
    private String razaoSocial;
    private String nomeFantasia;
    private String cnpj;
    private String inscricaoMunicipal;
    private String inscricaoEstadual;
    private String endereco;
    private String telefone;
    private String email;
    private String site;

    // Dados para o PDF
    private String nomeResponsavel; // Nome de quem assina
    private String observacaoRodape; // Texto que vai no final do orçamento
    private Boolean ativo = true;

    
}