package com.app.gerador_de_orcamento.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.gerador_de_orcamento.model.SessaoUsuario;
import com.app.gerador_de_orcamento.model.Usuario;

@Repository
public interface SessaoUsuarioRepository extends JpaRepository<SessaoUsuario, String> {
    Optional<SessaoUsuario> findByTokenAndDataExpiracaoAfter(String token, LocalDateTime now);
    void deleteByUsuario(Usuario usuario);
    void deleteByDataExpiracaoBefore(LocalDateTime now);
}
