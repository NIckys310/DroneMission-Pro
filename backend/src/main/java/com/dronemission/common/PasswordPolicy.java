package com.dronemission.common;

import com.dronemission.common.exception.BusinessRuleException;

import java.util.ArrayList;
import java.util.List;

/**
 * Política de contraseñas seguras. Se aplica al registrarse, al crear usuarios,
 * al cambiar la contraseña y al crear el administrador inicial.
 */
public final class PasswordPolicy {

    public static final int MIN_LENGTH = 8;
    public static final int MAX_LENGTH = 64;
    public static final String DESCRIPTION =
            "Mínimo 8 caracteres, con al menos una mayúscula, una minúscula y un número";

    private PasswordPolicy() {
    }

    /** Devuelve la lista de requisitos que la contraseña NO cumple (vacía si es válida). */
    public static List<String> problems(String password, String... personalData) {
        List<String> problems = new ArrayList<>();
        if (password == null || password.length() < MIN_LENGTH) problems.add("al menos " + MIN_LENGTH + " caracteres");
        if (password != null && password.length() > MAX_LENGTH) problems.add("máximo " + MAX_LENGTH + " caracteres");
        if (password == null || !password.matches(".*[A-ZÁÉÍÓÚÑ].*")) problems.add("una letra mayúscula");
        if (password == null || !password.matches(".*[a-záéíóúñ].*")) problems.add("una letra minúscula");
        if (password == null || !password.matches(".*\\d.*")) problems.add("un número");
        if (password != null) {
            String lower = password.toLowerCase();
            for (String data : personalData) {
                if (data != null && data.length() >= 3 && lower.contains(data.toLowerCase())) {
                    problems.add("no contener tu nombre, usuario o correo");
                    break;
                }
            }
        }
        return problems;
    }

    public static void validate(String password, String... personalData) {
        List<String> problems = problems(password, personalData);
        if (!problems.isEmpty()) {
            throw new BusinessRuleException("La contraseña no es segura. Debe tener: " + String.join(", ", problems));
        }
    }
}
