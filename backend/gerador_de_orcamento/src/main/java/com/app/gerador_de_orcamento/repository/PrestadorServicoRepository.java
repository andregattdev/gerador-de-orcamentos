package com.app.gerador_de_orcamento.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.app.gerador_de_orcamento.model.PrestadorServico;

public interface PrestadorServicoRepository extends JpaRepository<PrestadorServico, Long> {
    
   // Busca o único cadastro ativo do sistema
    PrestadorServico findByAtivoTrue();

    // Busca por CNPJ (para validação de duplicidade)
    PrestadorServico findByCnpj(String cnpj);

    // Lista apenas os ATIVOS
    List<PrestadorServico> findByAtivoTrueOrderByRazaoSocialAsc();

    // Lista apenas os INATIVOS
    List<PrestadorServico> findByAtivoFalseOrderByRazaoSocialAsc();
}