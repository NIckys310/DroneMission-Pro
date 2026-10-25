package com.dronemission.unit;

import com.dronemission.common.PasswordPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Política de contraseñas seguras. */
class PasswordPolicyTest {

    @Test
    @DisplayName("Acepta una contraseña con mayúscula, minúscula, número y 8+ caracteres")
    void acceptsStrongPassword() {
        assertThat(PasswordPolicy.problems("Vuelo2026Seguro")).isEmpty();
    }

    @Test
    @DisplayName("Indica cada requisito que falta")
    void listsProblems() {
        assertThat(PasswordPolicy.problems("abc")).contains("al menos 8 caracteres", "una letra mayúscula", "un número");
        assertThat(PasswordPolicy.problems("SOLOMAYUS1")).contains("una letra minúscula");
    }

    @Test
    @DisplayName("No permite contener el usuario o el nombre")
    void rejectsPersonalData() {
        assertThat(PasswordPolicy.problems("Laura2026x", "laura")).contains("no contener tu nombre, usuario o correo");
    }
}
