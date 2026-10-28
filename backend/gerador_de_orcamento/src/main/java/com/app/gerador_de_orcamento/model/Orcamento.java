package com.app.gerador_de_orcamento.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Data;

@Data
@Entity
public class Orcamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    private LocalDateTime dataCriacao;
    private String observacoes;
    private Boolean ativo = true;

    @Column(precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @Column(precision = 10, scale = 2)
    private BigDecimal valorCustos = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal valorDesconto = BigDecimal.ZERO;

    @Column(precision = 5, scale = 2)
    private BigDecimal porcentagemImposto = BigDecimal.ZERO;

    private LocalDateTime dataAgendamento;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "orcamento_id")
    private List<ItemOrcamento> itens = new ArrayList<>();

    @Column(name = "documento_assinado", columnDefinition = "BYTEA")
    private byte[] documentoAssinado;

    private String statusAssinatura = "PENDENTE";

    @Enumerated(EnumType.STRING)
    private StatusServico statusServico = StatusServico.RASCUNHO;

    // Métodos de ciclo de vida e auxiliares agrupados ao final do arquivo:
    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
        calcularValorTotal();
    }

    @PreUpdate
    protected void onUpdate() {
        calcularValorTotal();
    }

    public void calcularValorTotal() {
        BigDecimal subtotal = itens.isEmpty()
                ? BigDecimal.ZERO
                : itens.stream()
                        .map(item -> item.getValorUnitario().multiply(BigDecimal.valueOf(item.getQuantidade())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal desconto = valorDesconto != null ? valorDesconto : BigDecimal.ZERO;
        BigDecimal bruto = subtotal.subtract(desconto);
        this.valorTotal = bruto.max(BigDecimal.ZERO);
    }
}