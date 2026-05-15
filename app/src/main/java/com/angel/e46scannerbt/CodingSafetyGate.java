package com.angel.e46scannerbt;

public class CodingSafetyGate {
    public static class Result {
        public final boolean allowed;
        public final String message;

        public Result(boolean allowed, String message) {
            this.allowed = allowed;
            this.message = message;
        }
    }

    public Result canWrite(
            boolean moduleIdentified,
            boolean backupAvailable,
            boolean restoreAvailable,
            boolean stableCommunication,
            Double voltage,
            String moduleName,
            boolean userDoubleConfirmed
    ) {
        if (moduleName == null || moduleName.trim().isEmpty()) {
            return block("Módulo no identificado. Escritura bloqueada.");
        }

        String upper = moduleName.toUpperCase();
        if (upper.contains("DDE") || upper.contains("EWS") || upper.contains("AIRBAG") || upper.contains("SRS")) {
            return block("Módulo crítico prohibido: " + moduleName + ". La app no escribirá aquí.");
        }

        if (!moduleIdentified) return block("No se ha identificado el módulo exacto.");
        if (!backupAvailable) return block("No existe backup válido de la codificación original.");
        if (!restoreAvailable) return block("No está verificado el método de restauración.");
        if (!stableCommunication) return block("Comunicación inestable. Reintenta con contacto estable y adaptador fiable.");
        if (voltage == null) return block("Voltaje desconocido. Escritura bloqueada.");
        if (voltage < 12.3) return block("Voltaje bajo: " + voltage + " V. Mantén alimentación estable antes de codificar.");
        if (!userDoubleConfirmed) return block("Falta confirmación doble del usuario.");

        return new Result(true, "Condiciones seguras cumplidas. Escritura permitida solo para módulo de confort/carrocería.");
    }

    private Result block(String msg) {
        return new Result(false, msg);
    }
}
