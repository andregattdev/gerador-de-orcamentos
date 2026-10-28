package com.app.gerador_de_orcamento.config;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.app.gerador_de_orcamento.model.SessaoUsuario;
import com.app.gerador_de_orcamento.model.TipoUsuario;
import com.app.gerador_de_orcamento.model.Usuario;
import com.app.gerador_de_orcamento.repository.SessaoUsuarioRepository;
import com.app.gerador_de_orcamento.util.UserContext;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AutenticacaoInterceptor implements HandlerInterceptor {

    @Autowired
    private SessaoUsuarioRepository sessaoRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Ignorar requisições OPTIONS (CORS preflight)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();
        
        // Endpoints públicos
        if (path.startsWith("/api/auth/login") || path.startsWith("/api/auth/logout")) {
            return true;
        }
        
        // Ignorar swagger/docs
        if (path.contains("/v3/api-docs") || path.contains("/swagger-ui") || path.contains("/swagger-resources")) {
            return true;
        }

        // Apenas intercepta rotas da API
        if (!path.startsWith("/api")) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token de autorização não enviado.");
            return false;
        }

        String token = authHeader.substring(7);
        Optional<SessaoUsuario> sessaoOpt = sessaoRepository.findByTokenAndDataExpiracaoAfter(token, LocalDateTime.now());

        if (sessaoOpt.isEmpty()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expirado ou inválido.");
            return false;
        }

        Usuario usuario = sessaoOpt.get().getUsuario();
        if (!usuario.isAtivo()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Usuário inativo.");
            return false;
        }

        // Vincular ao ThreadLocal
        UserContext.setCurrentUser(usuario);

        // Verificar anotação @RequerDono
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            boolean requerDono = handlerMethod.getMethodAnnotation(RequerDono.class) != null 
                    || handlerMethod.getBeanType().getAnnotation(RequerDono.class) != null;

            if (requerDono && usuario.getTipo() != TipoUsuario.DONO) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso restrito ao administrador (Dono).");
                return false;
            }
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.clear();
    }
}
