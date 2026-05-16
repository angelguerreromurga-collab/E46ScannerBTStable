package com.angel.e46scannerbt;

public class SafeCommandBatch {
    public static boolean isAllowed(String[] commands) {
        if (commands == null || commands.length == 0) return false;
        for (String command : commands) {
            if (!SafeCommandPolicy.isAllowedReadOnly(command)) return false;
        }
        return true;
    }

    public static String report(String[] commands) {
        StringBuilder out = new StringBuilder();
        out.append("SAFE COMMAND BATCH CHECK\n");
        if (commands == null || commands.length == 0) {
            out.append("BLOQUEADO: lote vacio.\n");
            return out.toString();
        }
        for (String command : commands) {
            if (SafeCommandPolicy.isAllowedReadOnly(command)) {
                out.append("OK ").append(command).append("\n");
            } else {
                out.append("BLOCK ").append(command).append(" - ").append(SafeCommandPolicy.blockReason(command)).append("\n");
            }
        }
        return out.toString();
    }

    public static String firstBlockedReason(String[] commands) {
        if (commands == null || commands.length == 0) return "Lote vacio bloqueado.";
        for (String command : commands) {
            if (!SafeCommandPolicy.isAllowedReadOnly(command)) {
                return SafeCommandPolicy.blockReason(command);
            }
        }
        return "Sin bloqueos.";
    }
}
