package com.app.gerador_de_orcamento.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrestadorServicoResponseDTO {
    private Long id;
    private String razaoSocial;
    private String nomeFantasia;
    private String cnpj;
    private String inscricaoMunicipal;
    private String inscricaoEstadual;
    private String endereco;
    private String telefone;
    private String email;
    private String site;
    private String nomeResponsavel;
    private String observacaoRodape;
    private Boolean ativo;
}