package com.angel.e46scannerbt;

public class SafeCommandPolicy {
    public static boolean isAllowedReadOnly(String command) {
        if (command == null) return false;
        String c = command.trim().toUpperCase();
        if (c.length() == 0) return false;

        if (c.equals("04")) return false;
        if (c.startsWith("2E")) return false;
        if (c.startsWith("3B")) return false;
        if (c.startsWith("14")) return false;
        if (c.startsWith("30")) return false;
        if (c.startsWith("31")) return false;
        if (c.startsWith("34")) return false;
        if (c.startsWith("36")) return false;
        if (c.startsWith("37")) return false;

        if (c.startsWith("AT")) return true;
        if (c.equals("0100")) return true;
        if (c.equals("0120")) return true;
        if (c.equals("0140")) return true;
        if (c.equals("010C")) return true;
        if (c.equals("0105")) return true;
        if (c.equals("010D")) return true;
        if (c.equals("010B")) return true;
        if (c.equals("010F")) return true;
        if (c.equals("0110")) return true;
        if (c.equals("03")) return true;
        if (c.equals("07")) return true;
        if (c.equals("0A")) return true;

        // BMW KWP read-only services. 21 reads local identifiers. 1A reads identification data.
        if (c.startsWith("21")) return true;
        if (c.startsWith("1A")) return true;

        return false;
    }

    public static String blockReason(String command) {
        if (command == null) return "Comando nulo bloqueado.";
        String c = command.trim().toUpperCase();
        if (c.equals("04")) return "Bloqueado: 04 borra DTC.";
        if (c.startsWith("2E")) return "Bloqueado: 2E puede escribir datos.";
        if (c.startsWith("3B")) return "Bloqueado: 3B puede escribir memoria.";
        if (c.startsWith("14")) return "Bloqueado: 14 puede borrar informacion diagnostica.";
        if (c.startsWith("30") || c.startsWith("31")) return "Bloqueado: servicio de rutina/control no permitido.";
        if (c.startsWith("34") || c.startsWith("36") || c.startsWith("37")) return "Bloqueado: transferencia/programacion no permitida.";
        return "Comando no incluido en whitelist READ ONLY.";
    }
}
