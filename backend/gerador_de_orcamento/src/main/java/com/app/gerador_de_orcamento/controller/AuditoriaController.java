package com.app.gerador_de_orcamento.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.gerador_de_orcamento.config.RequerDono;
import com.app.gerador_de_orcamento.model.Auditoria;
import com.app.gerador_de_orcamento.service.AuditoriaService;

@RestController
@RequestMapping("/api/auditoria")
@RequerDono // A visualização dos logs de auditoria é exclusiva do Dono
public class AuditoriaController {

    @Autowired
    private AuditoriaService auditoriaService;

    @GetMapping
    public ResponseEntity<List<Auditoria>> listarTodos() {
        return ResponseEntity.ok(auditoriaService.listarTodos());
    }
}
