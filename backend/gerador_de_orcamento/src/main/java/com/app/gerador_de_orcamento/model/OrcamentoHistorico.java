package com.app.gerador_de_orcamento.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import lombok.Data;

@Data
@Entity
public class OrcamentoHistorico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "orcamento_id", nullable = false)
    private Orcamento orcamento;

    @Enumerated(EnumType.STRING)
    private StatusServico statusAnterior;

    @Enumerated(EnumType.STRING)
    private StatusServico statusNovo;

    @Column(length = 500)
    private String observacao;

    private LocalDateTime dataAlteracao;

    @PrePersist
    protected void onCreate() {
        dataAlteracao = LocalDateTime.now();
    }
}
