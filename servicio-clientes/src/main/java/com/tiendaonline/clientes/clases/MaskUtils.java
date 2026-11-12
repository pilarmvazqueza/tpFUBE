package com.tiendaonline.clientes.clases;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class MaskUtils {
    private MaskUtils() {}

    public static String maskNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return nombre;
        }
        return Arrays.stream(nombre.trim().split("\\s+"))
                .map(p -> p.charAt(0) + "***")
                .collect(Collectors.joining(" "));
    }

    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        String dominio = email.substring(email.indexOf("@") + 1);
        int punto = dominio.lastIndexOf('.');
        String tld = punto >= 0 ? dominio.substring(0, punto) : "";
        return "***@***" + tld;
    }

}
