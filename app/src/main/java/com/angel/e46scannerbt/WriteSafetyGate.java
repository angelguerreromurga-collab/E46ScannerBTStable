package com.angel.e46scannerbt;

public class WriteSafetyGate {
    public static boolean canWrite(boolean backupDone, boolean moduleIdentified, boolean codingRead, boolean rollbackReady, boolean expertUnlock) {
        return backupDone && moduleIdentified && codingRead && rollbackReady && expertUnlock;
    }

    public static String blockReason(boolean backupDone, boolean moduleIdentified, boolean codingRead, boolean rollbackReady, boolean expertUnlock) {
        if (!backupDone) return "Bloqueado: falta backup seguro exportado.";
        if (!moduleIdentified) return "Bloqueado: falta identificar modulo.";
        if (!codingRead) return "Bloqueado: falta leer codificacion actual.";
        if (!rollbackReady) return "Bloqueado: falta rollback verificable.";
        if (!expertUnlock) return "Bloqueado: escritura real no desbloqueada en esta version.";
        return "Permitido.";
    }

    public static String requiredSteps() {
        return "PASOS PARA DESBLOQUEAR ESCRITURA FUTURA\n" +
                "1. Conectar ELM.\n" +
                "2. Identificar modulo.\n" +
                "3. Leer codificacion original.\n" +
                "4. Exportar backup RAW.\n" +
                "5. Confirmar byte y bit exacto.\n" +
                "6. Preparar rollback.\n" +
                "7. Ejecutar escritura solo si expertUnlock esta activo.\n" +
                "8. Leer despues de escribir y comparar.";
    }
}
