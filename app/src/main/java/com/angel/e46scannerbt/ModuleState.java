package com.angel.e46scannerbt;

public class ModuleState {
    public final String id;
    public final String name;
    public String status;
    public String raw;

    public ModuleState(String id, String name) {
        this.id = id;
        this.name = name;
        this.status = "SIN PROBAR";
        this.raw = "";
    }

    public void update(String status, String raw) {
        this.status = status == null ? "SIN ESTADO" : status;
        this.raw = raw == null ? "" : raw;
    }

    public String display() {
        return name + "\nEstado: " + status + "\nRAW: " + (raw.length() == 0 ? "--" : raw);
    }
}
