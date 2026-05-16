package com.angel.e46scannerbt;

public class SafeTestWorkflow {
    public static final int STEP_CONNECT = 1;
    public static final int STEP_INIT_ELM = 2;
    public static final int STEP_PROTOCOL = 3;
    public static final int STEP_MOTOR = 4;
    public static final int STEP_DTC = 5;
    public static final int STEP_BACKUP = 6;
    public static final int STEP_LSZ_PREPARE = 7;
    public static final int STEP_GM5_PREPARE = 8;

    public static String title(int step) {
        switch (step) {
            case STEP_CONNECT: return "Conectar Bluetooth";
            case STEP_INIT_ELM: return "Inicializar ELM327";
            case STEP_PROTOCOL: return "Identificar protocolo ISO/KWP";
            case STEP_MOTOR: return "Leer motor/PIDs";
            case STEP_DTC: return "Leer DTC sin borrar";
            case STEP_BACKUP: return "Crear backup READ ONLY";
            case STEP_LSZ_PREPARE: return "Preparar LSZ LED";
            case STEP_GM5_PREPARE: return "Preparar GM5 confort";
            default: return "Paso desconocido";
        }
    }

    public static boolean requiresBackup(int step) {
        return step == STEP_LSZ_PREPARE || step == STEP_GM5_PREPARE;
    }

    public static boolean isWriteStep(int step) {
        return step == STEP_LSZ_PREPARE || step == STEP_GM5_PREPARE;
    }

    public static String guardMessage(int step, boolean backupDone) {
        String name = title(step);
        if (requiresBackup(step) && !backupDone) {
            return "BLOQUEADO: " + name + " requiere Backup seguro. No se ha modificado nada.";
        }
        if (isWriteStep(step)) {
            return name + " preparado en SIMULACIÓN. Escritura real deshabilitada.";
        }
        return name + " permitido en modo READ ONLY.";
    }
}
