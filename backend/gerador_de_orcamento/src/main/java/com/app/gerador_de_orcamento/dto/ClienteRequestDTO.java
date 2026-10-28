package com.app.gerador_de_orcamento.dto;

import com.app.gerador_de_orcamento.model.TipoCliente;
import com.app.gerador_de_orcamento.validation.Cnpj;
import com.app.gerador_de_orcamento.validation.Cpf;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClienteRequestDTO {

    @NotNull(message = "O tipo de cliente é obrigatório")
    private TipoCliente tipo;

    @NotBlank(message = "O nome é obrigatório")
    private String nome;

    private String endereco;

    @NotBlank(message = "O telefone é obrigatório")
    private String telefone;

    @Email(message = "E-mail inválido")
    private String email;

    @Cpf
    private String cpf;

    private String rg;

    @Cnpj
    private String cnpj;

    private String razaoSocial;
    private String inscricaoEstadual;
}