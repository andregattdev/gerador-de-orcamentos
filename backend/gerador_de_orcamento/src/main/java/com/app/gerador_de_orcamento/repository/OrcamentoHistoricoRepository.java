package com.app.gerador_de_orcamento.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.gerador_de_orcamento.model.OrcamentoHistorico;

public interface OrcamentoHistoricoRepository extends JpaRepository<OrcamentoHistorico, Long> {

    List<OrcamentoHistorico> findByOrcamentoIdOrderByDataAlteracaoDesc(Long orcamentoId);
}
