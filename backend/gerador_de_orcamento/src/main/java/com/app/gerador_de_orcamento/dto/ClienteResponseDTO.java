package com.app.gerador_de_orcamento.dto;

import com.app.gerador_de_orcamento.model.TipoCliente;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteResponseDTO {
    private Long id;
    private TipoCliente tipo;
    private String nome;
    private String endereco;
    private String telefone;
    private String email;
    private Boolean ativo;

    // Só preenche se for Física
    private String cpf;
    private String rg;

    // Só preenche se for Jurídica
    private String cnpj;
    private String razaoSocial;
    private String inscricaoEstadual;
}