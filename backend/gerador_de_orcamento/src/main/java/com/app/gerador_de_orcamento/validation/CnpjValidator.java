package com.app.gerador_de_orcamento.validation;

import com.app.gerador_de_orcamento.util.DocumentoUtil;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CnpjValidator implements ConstraintValidator<Cnpj, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return DocumentoUtil.cnpjValido(value);
    }
}
