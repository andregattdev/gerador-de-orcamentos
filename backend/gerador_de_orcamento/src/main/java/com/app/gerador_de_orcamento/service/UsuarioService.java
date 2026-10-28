package com.app.gerador_de_orcamento.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.gerador_de_orcamento.model.TipoUsuario;
import com.app.gerador_de_orcamento.model.Usuario;
import com.app.gerador_de_orcamento.repository.UsuarioRepository;

import jakarta.annotation.PostConstruct;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostConstruct
    @Transactional
    public void seedAdminUser() {
        if (usuarioRepository.count() == 0) {
            Usuario admin = new Usuario();
            admin.setNome("Administrador (Dono)");
            admin.setEmail("admin@comercio.com");
            admin.setSenha(passwordEncoder.encode("admin"));
            admin.setTipo(TipoUsuario.DONO);
            admin.setAtivo(true);
            usuarioRepository.save(admin);
            System.out.println(">>> Usuário administrador inicial criado com sucesso: admin@comercio.com / admin");
        }
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    @Transactional
    public Usuario criar(Usuario usuario) {
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new IllegalArgumentException("E-mail já cadastrado!");
        }
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario atualizar(Long id, Usuario dadosAtualizados) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado!"));

        if (!usuario.getEmail().equals(dadosAtualizados.getEmail()) && 
            usuarioRepository.existsByEmail(dadosAtualizados.getEmail())) {
            throw new IllegalArgumentException("E-mail já cadastrado por outro usuário!");
        }

        usuario.setNome(dadosAtualizados.getNome());
        usuario.setEmail(dadosAtualizados.getEmail());
        usuario.setTipo(dadosAtualizados.getTipo());

        // Só atualiza a senha se uma nova senha for fornecida
        if (dadosAtualizados.getSenha() != null && !dadosAtualizados.getSenha().trim().isEmpty()) {
            usuario.setSenha(passwordEncoder.encode(dadosAtualizados.getSenha()));
        }

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void inativar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado!"));
        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void reativar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado!"));
        usuario.setAtivo(true);
        usuarioRepository.save(usuario);
    }

    public boolean verificarSenha(String senhaDigitada, String senhaCriptografada) {
        return passwordEncoder.matches(senhaDigitada, senhaCriptografada);
    }
}
