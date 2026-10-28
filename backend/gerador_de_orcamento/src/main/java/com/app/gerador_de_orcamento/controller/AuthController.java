package com.app.gerador_de_orcamento.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.gerador_de_orcamento.model.SessaoUsuario;
import com.app.gerador_de_orcamento.service.AuthService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            SessaoUsuario sessao = authService.login(request.getEmail(), request.getSenha());
            return ResponseEntity.ok(new LoginResponse(
                    sessao.getToken(),
                    sessao.getUsuario().getId(),
                    sessao.getUsuario().getNome(),
                    sessao.getUsuario().getEmail(),
                    sessao.getUsuario().getTipo().name()
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        authService.logout(token);
        return ResponseEntity.ok().build();
    }

    @Data
    public static class LoginRequest {
        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        private String email;

        @NotBlank(message = "Senha é obrigatória")
        private String senha;
    }

    @Data
    public static class LoginResponse {
        private final String token;
        private final Long id;
        private final String nome;
        private final String email;
        private final String tipo;
    }

    @Data
    public static class ErrorResponse {
        private final String message;
    }
}
