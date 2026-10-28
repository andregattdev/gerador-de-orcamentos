package com.app.gerador_de_orcamento.util;

import com.app.gerador_de_orcamento.model.Usuario;

public class UserContext {
    private static final ThreadLocal<Usuario> currentUser = new ThreadLocal<>();

    public static void setCurrentUser(Usuario user) {
        currentUser.set(user);
    }

    public static Usuario getCurrentUser() {
        return currentUser.get();
    }

    public static void clear() {
        currentUser.remove();
    }
}
