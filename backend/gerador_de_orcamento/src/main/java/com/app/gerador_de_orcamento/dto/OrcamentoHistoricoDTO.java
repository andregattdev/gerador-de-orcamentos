package com.app.gerador_de_orcamento.dto;

import java.time.LocalDateTime;

import com.app.gerador_de_orcamento.model.StatusServico;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrcamentoHistoricoDTO {
    private Long id;
    private Long orcamentoId;
    private StatusServico statusAnterior;
    private StatusServico statusNovo;
    private String observacao;
    private LocalDateTime dataAlteracao;
}
