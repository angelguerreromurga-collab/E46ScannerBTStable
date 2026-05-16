package com.angel.e46scannerbt;

public class E46ModuleProfile {
    public final String key;
    public final String name;
    public final String header;
    public final String[] headerVariants;
    public final String[] safeReadCommands;

    public E46ModuleProfile(String key, String name, String header, String[] safeReadCommands) {
        this(key, name, header, new String[]{header}, safeReadCommands);
    }

    public E46ModuleProfile(String key, String name, String header, String[] headerVariants, String[] safeReadCommands) {
        this.key = key;
        this.name = name;
        this.header = header;
        this.headerVariants = headerVariants;
        this.safeReadCommands = safeReadCommands;
    }

    public String describe() {
        StringBuilder out = new StringBuilder();
        out.append(name).append("\n");
        out.append("ELM header principal: ").append(header).append("\n");
        out.append("Variantes: ");
        for (int i = 0; i < headerVariants.length; i++) {
            if (i > 0) out.append(", ");
            out.append(headerVariants[i]);
        }
        out.append("\nLecturas read-only: ");
        for (int i = 0; i < safeReadCommands.length; i++) {
            if (i > 0) out.append(", ");
            out.append(safeReadCommands[i]);
        }
        return out.toString();
    }
}
