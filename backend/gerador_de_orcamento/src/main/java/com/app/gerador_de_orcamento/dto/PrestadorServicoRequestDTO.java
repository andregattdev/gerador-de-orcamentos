package com.app.gerador_de_orcamento.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PrestadorServicoRequestDTO {

    @NotBlank(message = "Razão Social é obrigatória")
    private String razaoSocial;

    private String nomeFantasia;

    @NotBlank(message = "CNPJ é obrigatório")
    private String cnpj;

    private String inscricaoMunicipal;
    private String inscricaoEstadual;

    @NotBlank(message = "Endereço é obrigatório")
    private String endereco;

    @NotBlank(message = "Telefone é obrigatório")
    private String telefone;

    private String email;
    private String site;
    private String nomeResponsavel;
    private String observacaoRodape;
}