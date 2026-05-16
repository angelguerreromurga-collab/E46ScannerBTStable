package com.angel.e46scannerbt;

public class BackupChecklist {
    public static String format(boolean bluetooth, boolean elm, boolean protocol, boolean motor, boolean dtc, BackupState backupState) {
        StringBuilder b = new StringBuilder();
        b.append(mark(bluetooth)).append(" Bluetooth\n");
        b.append(mark(elm)).append(" ELM inicializado\n");
        b.append(mark(protocol)).append(" Protocolo identificado\n");
        b.append(mark(motor)).append(" Motor/PIDs leidos\n");
        b.append(mark(dtc)).append(" DTC leidos\n");
        boolean validBackup = backupState != null && backupState.isValidForCodingPrep();
        b.append(mark(validBackup)).append(" ").append(backupState == null ? "Sin backup" : backupState.label());
        return b.toString();
    }

    private static String mark(boolean ok) {
        return ok ? "[OK]" : "[--]";
    }
}
