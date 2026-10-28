package com.app.gerador_de_orcamento.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrcamentoRequestDTO {

    @NotNull(message = "ID do cliente é obrigatório")
    private Long clienteId;

    private String observacoes;

    private BigDecimal valorCustos;
    private BigDecimal valorDesconto;
    private BigDecimal porcentagemImposto;
    private LocalDateTime dataAgendamento;

    @NotEmpty(message = "O orçamento deve ter pelo menos um item")
    private List<ItemOrcamentoDTO> itens;
}