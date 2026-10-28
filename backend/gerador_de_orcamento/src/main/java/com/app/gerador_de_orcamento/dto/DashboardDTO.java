package com.app.gerador_de_orcamento.dto;

import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter 
@Setter
public class DashboardDTO {
    private Double faturamentoBruto;
    private Double lucroLiquido;
    private Long servicosAtivos;
    private List<AgendaItemDTO> proximosServicos;

    // Construtor, Getters e Setters
    public DashboardDTO(Double faturamentoBruto, Double lucroLiquido, Long servicosAtivos, List<AgendaItemDTO> proximosServicos) {
        this.faturamentoBruto = faturamentoBruto != null ? faturamentoBruto : 0.0;
        this.lucroLiquido = lucroLiquido != null ? lucroLiquido : 0.0;
        this.servicosAtivos = servicosAtivos != null ? servicosAtivos : 0L;
        this.proximosServicos = proximosServicos;
    }
}