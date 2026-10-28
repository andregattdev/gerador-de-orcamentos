package com.app.gerador_de_orcamento.service;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.app.gerador_de_orcamento.model.StatusServico;

@Component
public class StatusTransicaoValidator {

    private static final Map<StatusServico, Set<StatusServico>> TRANSICOES = Map.of(
            StatusServico.RASCUNHO, EnumSet.of(StatusServico.APROVADO, StatusServico.CANCELADO, StatusServico.REJEITADO),
            StatusServico.APROVADO, EnumSet.of(StatusServico.EM_ANDAMENTO, StatusServico.CANCELADO),
            StatusServico.EM_ANDAMENTO, EnumSet.of(StatusServico.FINALIZADO, StatusServico.CANCELADO),
            StatusServico.FINALIZADO, EnumSet.of(StatusServico.CONCLUIDO),
            StatusServico.CONCLUIDO, EnumSet.noneOf(StatusServico.class),
            StatusServico.CANCELADO, EnumSet.of(StatusServico.RASCUNHO),
            StatusServico.REJEITADO, EnumSet.of(StatusServico.RASCUNHO, StatusServico.CANCELADO)
    );

    public void validar(StatusServico atual, StatusServico novo) {
        if (atual == null || novo == null) {
            throw new IllegalArgumentException("Status inválido");
        }
        if (atual == novo) {
            throw new IllegalArgumentException("O orçamento já está com status " + novo);
        }
        Set<StatusServico> permitidos = TRANSICOES.getOrDefault(atual, Set.of());
        if (!permitidos.contains(novo)) {
            throw new IllegalArgumentException(
                    "Transição de " + atual + " para " + novo + " não é permitida");
        }
    }
}
