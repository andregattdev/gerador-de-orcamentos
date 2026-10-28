package com.app.gerador_de_orcamento.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.gerador_de_orcamento.model.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    List<Cliente> findByAtivoTrue();
    List<Cliente> findByAtivoFalse();
    // Busca por CPF ou CNPJ (para evitar duplicidade)
    Cliente findByCpf(String cpf);
    Cliente findByCnpj(String cnpj);

}
