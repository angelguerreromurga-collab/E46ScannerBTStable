package com.angel.e46scannerbt;

public class BackupValidator {
    public static boolean isValidRawBackup(String raw) {
        if (raw == null) return false;
        String upper = raw.toUpperCase();
        boolean hasElm = upper.contains("ELM") || upper.contains("ATI") || upper.contains("ATZ");
        boolean hasProtocol = upper.contains("ATDP") || upper.contains("ATDPN") || upper.contains("ISO") || upper.contains("KWP");
        boolean hasPid = upper.contains("0100") || upper.contains("0120") || upper.contains("4100") || upper.contains("4120");
        boolean hasDtc = upper.contains("03") || upper.contains("07") || upper.contains("43") || upper.contains("47");
        return hasElm && hasProtocol && hasPid && hasDtc;
    }

    public static String validationMessage(String raw) {
        if (isValidRawBackup(raw)) return "Backup valido para preparar pruebas en simulacion.";
        return "Backup insuficiente: falta ELM, protocolo, PIDs o DTC. No se permite preparar coding.";
    }
}
