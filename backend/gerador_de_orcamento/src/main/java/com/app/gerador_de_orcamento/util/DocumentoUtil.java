package com.app.gerador_de_orcamento.util;

public final class DocumentoUtil {

    private DocumentoUtil() {}

    public static String somenteDigitos(String valor) {
        if (valor == null) return "";
        return valor.replaceAll("\\D", "");
    }

    public static boolean cpfValido(String cpf) {
        String digits = somenteDigitos(cpf);
        if (digits.length() != 11 || digits.chars().distinct().count() == 1) {
            return false;
        }
        int d1 = calcularDigitoCpf(digits, 9, 10);
        int d2 = calcularDigitoCpf(digits, 10, 11);
        return digits.charAt(9) - '0' == d1 && digits.charAt(10) - '0' == d2;
    }

    public static boolean cnpjValido(String cnpj) {
        String digits = somenteDigitos(cnpj);
        if (digits.length() != 14 || digits.chars().distinct().count() == 1) {
            return false;
        }
        int d1 = calcularDigitoCnpj(digits, 12);
        int d2 = calcularDigitoCnpj(digits, 13);
        return digits.charAt(12) - '0' == d1 && digits.charAt(13) - '0' == d2;
    }

    private static int calcularDigitoCpf(String cpf, int tamanho, int pesoInicial) {
        int soma = 0;
        for (int i = 0; i < tamanho; i++) {
            soma += (cpf.charAt(i) - '0') * (pesoInicial - i);
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private static int calcularDigitoCnpj(String cnpj, int posicao) {
        int[] pesos = posicao == 12
                ? new int[] { 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2 }
                : new int[] { 6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2 };
        int soma = 0;
        for (int i = 0; i < pesos.length; i++) {
            soma += (cnpj.charAt(i) - '0') * pesos[i];
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
