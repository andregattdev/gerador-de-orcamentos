package com.app.gerador_de_orcamento.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TipoCliente tipo;

    private String nome;
    private String endereco;
    private String telefone;
    private String email;
    private Boolean ativo = true;

    // Pessoa Física
    private String cpf;
    private String rg;

    // Pessoa Jurídica
    private String cnpj;
    private String razaoSocial;
    private String inscricaoEstadual;
}