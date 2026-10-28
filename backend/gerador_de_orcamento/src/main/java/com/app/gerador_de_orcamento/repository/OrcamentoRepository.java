package com.app.gerador_de_orcamento.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.app.gerador_de_orcamento.dto.AgendaItemDTO;
import com.app.gerador_de_orcamento.model.Orcamento;
import com.app.gerador_de_orcamento.model.StatusServico;

public interface OrcamentoRepository extends JpaRepository<Orcamento, Long> {

       @Query("SELECT o FROM Orcamento o JOIN FETCH o.cliente")
       List<Orcamento> findAll();

       List<Orcamento> findByAtivoTrue();

       List<Orcamento> findByAtivoFalse();

       @Query("SELECT o FROM Orcamento o WHERE " +
                     "(:clienteNome IS NULL OR :clienteNome = '' OR LOWER(o.cliente.nome) LIKE LOWER(CONCAT('%', CAST(:clienteNome AS string), '%'))) AND "
                     +
                     "(:status IS NULL OR o.statusServico = :status) AND " +
                    // "(:dataInicio IS NULL OR o.dataCriacao >= CAST(:dataInicio AS timestamp)) AND " +
                    // "(:dataFim IS NULL OR o.dataCriacao <= CAST(:dataFim AS timestamp)) AND " +
                     "(:apenasAtivos IS NULL OR o.ativo = CAST(:apenasAtivos AS boolean))")
       Page<Orcamento> buscarComFiltros(
                     @Param("clienteNome") String clienteNome,
                     @Param("status") StatusServico status,
                     @Param("dataInicio") LocalDateTime dataInicio,
                     @Param("dataFim") LocalDateTime dataFim,
                     @Param("apenasAtivos") Boolean apenasAtivos,
                     Pageable pageable);

       @Query("SELECT SUM(o.valorTotal) FROM Orcamento o WHERE o.statusServico IN ('FINALIZADO', 'CONCLUIDO') AND o.ativo = true")
       Double calcularFaturamentoBruto();

       @Query("SELECT SUM(o.valorTotal - COALESCE(o.valorCustos, 0)) FROM Orcamento o WHERE o.statusServico IN ('FINALIZADO', 'CONCLUIDO') AND o.ativo = true")
       Double calcularLucroLiquido();

       @Query("SELECT COUNT(o) FROM Orcamento o WHERE o.statusServico IN ('APROVADO', 'EM_ANDAMENTO') AND o.ativo = true")
       Long contarServicosAtivos();

       @Query("SELECT new com.app.gerador_de_orcamento.dto.AgendaItemDTO(o.id, o.cliente.nome, o.observacoes, o.dataAgendamento, o.statusServico) "
                     +
                     "FROM Orcamento o WHERE o.dataAgendamento >= CURRENT_TIMESTAMP AND o.ativo = true " +
                     "ORDER BY o.dataAgendamento ASC")
       List<AgendaItemDTO> buscarProximosAgendamentos(Pageable pageable);

       @Query("SELECT new com.app.gerador_de_orcamento.dto.AgendaItemDTO(o.id, o.cliente.nome, o.observacoes, o.dataAgendamento, o.statusServico) "
                     +
                     "FROM Orcamento o WHERE o.dataAgendamento BETWEEN :inicio AND :fim AND o.ativo = true " +
                     "ORDER BY o.dataAgendamento ASC")
       List<AgendaItemDTO> buscarAgendamentosEntre(
                     @Param("inicio") LocalDateTime inicio,
                     @Param("fim") LocalDateTime fim);
}
