package com.app.gerador_de_orcamento.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.app.gerador_de_orcamento.model.StatusServico; // Import do Enum de controle

import lombok.Data;

@Data
public class OrcamentoResponseDTO {
    private Long id;
    private ClienteResponseDTO cliente;
    private LocalDateTime dataCriacao;
    private String observacoes;
    private Boolean ativo;
    private BigDecimal valorTotal;
    
    private BigDecimal valorCustos;
    private BigDecimal valorDesconto;
    private BigDecimal porcentagemImposto;
    private BigDecimal valorImpostoCalculado;
    private BigDecimal lucroLiquidoEstimado;
    private LocalDateTime dataAgendamento;
    private StatusServico statusServico;
    
    private List<ItemOrcamentoDTO> itens;
    private String statusAssinatura;
    private boolean temDocumentoAssinado; 
}