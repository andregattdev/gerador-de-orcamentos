package com.app.gerador_de_orcamento.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.app.gerador_de_orcamento.dto.PrestadorServicoRequestDTO;
import com.app.gerador_de_orcamento.dto.PrestadorServicoResponseDTO;
import com.app.gerador_de_orcamento.service.PrestadorServicoService;
import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/prestador")
@RequiredArgsConstructor
@Tag(name = "Prestador de Serviço", description = "Gerenciamento dos dados do prestador de serviço para orçamentos")
public class PrestadorServicoController {

    private final PrestadorServicoService service;

    // Cadastrar novo
    @PostMapping
    @Operation(summary = "Cadastrar novo prestador de serviço", description = "Cadastra um novo prestador de serviço com os dados fornecidos")
    public ResponseEntity<?> salvar(@Validated @RequestBody PrestadorServicoRequestDTO dto) {
        try {
            PrestadorServicoResponseDTO salvo = service.salvar(dto);
            return new ResponseEntity<>(salvo, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Buscar dados ATUAIS (o cadastro que está em uso)
    @GetMapping("/atual")
    @Operation(summary = "Buscar dados atuais", description = "Retorna os dados do prestador de serviço que está em uso")
    public ResponseEntity<PrestadorServicoResponseDTO> getDadosAtuais() {
        try {
            return ResponseEntity.ok(service.getDadosAtuais());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Buscar por ID (apenas se estiver ativo)
    @GetMapping("/{id}")
    @Operation(summary = "Buscar prestador por ID", description = "Retorna os dados de um prestador de serviço específico com base no ID fornecido")
    public ResponseEntity<PrestadorServicoResponseDTO> buscarPorId(@PathVariable Long id) {
        PrestadorServicoResponseDTO prestador = service.buscarPorId(id);
        return prestador != null ? ResponseEntity.ok(prestador) : ResponseEntity.notFound().build();
    }

    // Listar apenas ATIVOS
    @GetMapping
    @Operation(summary = "Listar prestadores ativos", description = "Retorna uma lista de prestadores de serviço que estão ativos")
    public ResponseEntity<List<PrestadorServicoResponseDTO>> listarAtivos() {
        return ResponseEntity.ok(service.listarAtivos());
    }

    // Listar apenas INATIVOS
    @GetMapping("/inativos")
    @Operation(summary = "Listar prestadores inativos", description = "Retorna uma lista de prestadores de serviço que estão inativos")
    public ResponseEntity<List<PrestadorServicoResponseDTO>> listarInativos() {
        return ResponseEntity.ok(service.listarInativos());
    }

    // Listar TODOS
    @GetMapping("/todos")
    @Operation(summary = "Listar todos os prestadores", description = "Retorna uma lista de todos os prestadores de serviço, incluindo ativos e inativos")
    public ResponseEntity<List<PrestadorServicoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    // Atualizar cadastro
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar prestador", description = "Atualiza os dados de um prestador de serviço existente com base no ID fornecido")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @Validated @RequestBody PrestadorServicoRequestDTO dto) {
        try {
            PrestadorServicoResponseDTO atualizado = service.atualizar(id, dto);
            return atualizado != null ? ResponseEntity.ok(atualizado) : ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Inativar
    @DeleteMapping("/{id}")
    @Operation(summary = "Inativar prestador", description = "Marca um prestador de serviço como inativo com base no ID fornecido")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        return service.inativar(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    // Reativar
    @PutMapping("/{id}/reativar")
    @Operation(summary = "Reativar prestador", description = "Reativa um prestador de serviço inativo com base no ID fornecido")
    public ResponseEntity<Void> reativar(@PathVariable Long id) {
        return service.reativar(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}