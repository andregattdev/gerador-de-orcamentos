package com.app.gerador_de_orcamento.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "auditorias")
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = true) // Pode ser null se for uma ação do sistema/automática
    private Usuario usuario;

    @Column(nullable = false)
    private String acao; // Ex: ADICAO, ALTERACAO, EXCLUSAO

    @Column(nullable = false)
    private String entidade; // Ex: Cliente, Orcamento, PrestadorServico

    @Column(name = "entidade_id")
    private Long entidadeId; // ID do registro afetado

    @Column(length = 2000)
    private String detalhes;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @PrePersist
    protected void onCreate() {
        if (dataHora == null) {
            dataHora = LocalDateTime.now();
        }
    }
}
