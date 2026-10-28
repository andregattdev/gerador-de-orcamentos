package com.app.gerador_de_orcamento.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import com.app.gerador_de_orcamento.model.StatusServico;

@Data
@Getter 
@Setter
public class AgendaItemDTO {
    private Long id;
    private String clienteNome;
    private String descricaoCurta;
    private LocalDateTime dataAgendamento;
    private StatusServico status;

    public AgendaItemDTO(Long id, String clienteNome, String descricaoCurta, LocalDateTime dataAgendamento, StatusServico status) {
        this.id = id;
        this.clienteNome = clienteNome;
        this.descricaoCurta = descricaoCurta;
        this.dataAgendamento = dataAgendamento;
        this.status = status;
    }

}
