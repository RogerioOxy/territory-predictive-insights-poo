package br.com.fiap.territorio;

import java.util.Locale;

/**
 * Padroniza os numeros do console com Locale pt-BR, sem depender do computador.
 */
public final class Formatador {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private Formatador() {
    }

    public static String f(String formato, Object... argumentos) {
        return String.format(PT_BR, formato, argumentos);
    }
}
