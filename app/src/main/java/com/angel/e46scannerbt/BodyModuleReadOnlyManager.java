package com.angel.e46scannerbt;

public class BodyModuleReadOnlyManager {
    private final ModuleState dde = new ModuleState("DDE", "DDE Motor");
    private final ModuleState lsz = new ModuleState("LSZ", "LSZ Luces");
    private final ModuleState gm5 = new ModuleState("GM5", "GM5 Confort");
    private final ModuleState kombi = new ModuleState("KOMBI", "KOMBI Cuadro");
    private final ModuleState ihka = new ModuleState("IHKA", "IHKA Clima");

    public ModuleState dde() { return dde; }
    public ModuleState lsz() { return lsz; }
    public ModuleState gm5() { return gm5; }
    public ModuleState kombi() { return kombi; }
    public ModuleState ihka() { return ihka; }

    public void markDde(String raw) { dde.update("OBD GENERICO OK", raw); }
    public void markLszNoTest() { lsz.update("PENDIENTE FASE 1", ""); }
    public void markGm5NoTest() { gm5.update("PENDIENTE FASE 1", ""); }

    public String summary() {
        return dde.display() + "\n\n" +
                lsz.display() + "\n\n" +
                gm5.display() + "\n\n" +
                kombi.display() + "\n\n" +
                ihka.display();
    }

    public String safetyText() {
        return "MODULOS CARROCERIA - FASE 1\n" +
                "Modo: READ ONLY\n" +
                "LSZ y GM5 aun sin escritura real.\n" +
                "Primero hay que identificar modulo, leer RAW, exportar backup y validar rollback.";
    }
}
