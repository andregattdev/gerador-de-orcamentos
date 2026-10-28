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

import com.app.gerador_de_orcamento.dto.ClienteRequestDTO;
import com.app.gerador_de_orcamento.dto.ClienteResponseDTO;
import com.app.gerador_de_orcamento.service.ClienteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "Gerenciamento de clientes para orçamentos")
public class ClienteController {

    private final ClienteService clienteService;

    @PostMapping
    @Operation(summary = "Criar um novo cliente", description = "Cria um cliente com os dados fornecidos")
    public ResponseEntity<?> criar(@Validated @RequestBody ClienteRequestDTO dto) {
        try {
            ClienteResponseDTO novo = clienteService.criar(dto);
            return new ResponseEntity<>(novo, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    @Operation(summary = "Listar clientes ativos", description = "Retorna uma lista de clientes ativos")
    public ResponseEntity<List<ClienteResponseDTO>> listarAtivos() {
        return ResponseEntity.ok(clienteService.listarAtivos());
    }

    @GetMapping("/inativos")
    @Operation(summary = "Listar clientes inativos", description = "Retorna uma lista de clientes que estão inativos")
    public ResponseEntity<List<ClienteResponseDTO>> listarInativos() {
        return ResponseEntity.ok(clienteService.listarInativos());
    }

    @GetMapping("/todos")
    @Operation(summary = "Listar todos os clientes", description = "Retorna uma lista de todos os clientes, incluindo ativos e inativos")
    public ResponseEntity<List<ClienteResponseDTO>> listarTodos() {
        return ResponseEntity.ok(clienteService.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cliente por ID", description = "Retorna os dados de um cliente específico com base no ID fornecido")
    public ResponseEntity<ClienteResponseDTO> buscarPorId(@PathVariable Long id) {
        ClienteResponseDTO cliente = clienteService.buscarPorId(id);
        return cliente != null ? ResponseEntity.ok(cliente) : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar cliente", description = "Atualiza os dados de um cliente existente com base no ID fornecido")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @Validated @RequestBody ClienteRequestDTO dto) {
        try {
            ClienteResponseDTO atualizado = clienteService.atualizar(id, dto);
            return atualizado != null ? ResponseEntity.ok(atualizado) : ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Inativar cliente", description = "Marca um cliente como inativo com base no ID fornecido")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        return clienteService.inativar(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/reativar")
    @Operation(summary = "Reativar cliente", description = "Reativa um cliente inativo com base no ID fornecido")
    public ResponseEntity<Void> reativar(@PathVariable Long id) {
        return clienteService.reativar(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}