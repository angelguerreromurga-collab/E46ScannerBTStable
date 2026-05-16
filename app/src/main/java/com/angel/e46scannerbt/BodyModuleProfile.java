package com.angel.e46scannerbt;

public class BodyModuleProfile {
    public final String id;
    public final String title;
    public final String subtitle;
    public final String mode;

    public BodyModuleProfile(String id, String title, String subtitle) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.mode = "READ ONLY";
    }

    public static BodyModuleProfile dde() {
        return new BodyModuleProfile("DDE", "DDE Motor", "Motor, datos OBD, DTC y backup seguro");
    }

    public static BodyModuleProfile lsz() {
        return new BodyModuleProfile("LSZ", "LSZ Luces", "Luces, check frio/caliente y alumbrado");
    }

    public static BodyModuleProfile gm5() {
        return new BodyModuleProfile("GM5", "GM5 Confort", "Cierre, apertura, elevalunas y confort");
    }

    public static BodyModuleProfile kombi() {
        return new BodyModuleProfile("KOMBI", "KOMBI Cuadro", "Cuadro, avisos y datos de instrumento");
    }

    public static BodyModuleProfile ihka() {
        return new BodyModuleProfile("IHKA", "IHKA Clima", "Climatizacion, AUC y sensores auxiliares");
    }
}
