package com.angel.e46scannerbt;

public class SafetyGate {
    private boolean backupDone;
    private boolean writeEnabled;

    public void setBackupDone(boolean done) {
        backupDone = done;
        if (!done) writeEnabled = false;
    }

    public boolean isBackupDone() {
        return backupDone;
    }

    public boolean isWriteEnabled() {
        return backupDone && writeEnabled;
    }

    public String enableWrite() {
        if (!backupDone) return "BLOQUEADO: primero hay que completar Backup seguro.";
        writeEnabled = true;
        return "ESCRITURA ARMADA: requiere confirmación explícita antes de modificar módulos.";
    }

    public String disableWrite() {
        writeEnabled = false;
        return "ESCRITURA DESACTIVADA: modo seguro.";
    }

    public String guard(String actionName) {
        if (!backupDone) return "BLOQUEADO: " + actionName + " requiere Backup seguro. No se ha modificado nada.";
        if (!writeEnabled) return actionName + " preparado en simulación. Escritura real deshabilitada.";
        return actionName + " permitido por SafetyGate.";
    }
}
