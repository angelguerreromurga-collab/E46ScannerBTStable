package com.angel.e46scannerbt;

public class BlockedWritePlan {
    public final String moduleId;
    public final String featureName;
    public final String currentState;
    public final String requestedState;
    public final String reason;

    public BlockedWritePlan(String moduleId, String featureName, String currentState, String requestedState, String reason) {
        this.moduleId = moduleId;
        this.featureName = featureName;
        this.currentState = currentState;
        this.requestedState = requestedState;
        this.reason = reason;
    }

    public String title() {
        return moduleId + " - " + featureName;
    }

    public String summary() {
        return "MODULO: " + moduleId + "\n" +
                "FUNCION: " + featureName + "\n" +
                "ACTUAL: " + currentState + "\n" +
                "SOLICITADO: " + requestedState + "\n" +
                "ESTADO: BLOQUEADO\n" +
                "MOTIVO: " + reason;
    }

    public static BlockedWritePlan lszLedColdCheck(boolean enable) {
        return new BlockedWritePlan(
                "LSZ",
                "LED cold check",
                "SIN LEER",
                enable ? "ACTIVAR" : "DESACTIVAR",
                "Hace falta leer codificacion original LSZ y guardar backup RAW antes de cualquier escritura."
        );
    }

    public static BlockedWritePlan lszLedWarmCheck(boolean enable) {
        return new BlockedWritePlan(
                "LSZ",
                "LED warm check",
                "SIN LEER",
                enable ? "ACTIVAR" : "DESACTIVAR",
                "Hace falta identificar modulo LSZ, leer codificacion y verificar byte/bit exacto."
        );
    }

    public static BlockedWritePlan gm5ComfortClose(boolean enable) {
        return new BlockedWritePlan(
                "GM5",
                "Comfort close",
                "SIN LEER",
                enable ? "ACTIVAR" : "DESACTIVAR",
                "Hace falta backup GM5, lectura actual y plan de rollback antes de escribir."
        );
    }
}
