package com.app.gerador_de_orcamento.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.gerador_de_orcamento.model.SessaoUsuario;
import com.app.gerador_de_orcamento.model.Usuario;
import com.app.gerador_de_orcamento.repository.SessaoUsuarioRepository;

@Service
public class AuthService {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private SessaoUsuarioRepository sessaoUsuarioRepository;

    @Transactional
    public SessaoUsuario login(String email, String senha) {
        Usuario usuario = usuarioService.buscarPorEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("E-mail ou senha inválidos!"));

        if (!usuario.isAtivo()) {
            throw new IllegalStateException("Usuário inativo. Entre em contato com o administrador.");
        }

        if (!usuarioService.verificarSenha(senha, usuario.getSenha())) {
            throw new IllegalArgumentException("E-mail ou senha inválidos!");
        }

        // Limpar sessões anteriores do mesmo usuário
        sessaoUsuarioRepository.deleteByUsuario(usuario);

        // Criar nova sessão válida por 8 horas
        SessaoUsuario sessao = new SessaoUsuario();
        sessao.setToken(UUID.randomUUID().toString());
        sessao.setUsuario(usuario);
        sessao.setDataExpiracao(LocalDateTime.now().plusHours(8));

        return sessaoUsuarioRepository.save(sessao);
    }

    @Transactional
    public void logout(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (token != null) {
            sessaoUsuarioRepository.deleteById(token);
        }
    }
}
