package com.app.gerador_de_orcamento.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.gerador_de_orcamento.model.Auditoria;
import com.app.gerador_de_orcamento.model.Usuario;
import com.app.gerador_de_orcamento.repository.AuditoriaRepository;
import com.app.gerador_de_orcamento.util.UserContext;

@Service
public class AuditoriaService {

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    @Transactional
    public void registrar(String acao, String entidade, Long entidadeId, String detalhes) {
        Usuario usuarioAtual = UserContext.getCurrentUser();
        
        Auditoria auditoria = new Auditoria();
        auditoria.setUsuario(usuarioAtual);
        auditoria.setAcao(acao);
        auditoria.setEntidade(entidade);
        auditoria.setEntidadeId(entidadeId);
        auditoria.setDetalhes(detalhes);
        auditoria.setDataHora(LocalDateTime.now());

        auditoriaRepository.save(auditoria);
    }

    public List<Auditoria> listarTodos() {
        return auditoriaRepository.findAllByOrderByDataHoraDesc();
    }
}
