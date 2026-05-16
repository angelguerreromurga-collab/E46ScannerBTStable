package com.angel.e46scannerbt;

import java.util.LinkedHashMap;
import java.util.Map;

public class E46ModuleRegistry {
    private static final Map<String, E46ModuleProfile> modules = new LinkedHashMap<>();

    static {
        modules.put("LSZ", new E46ModuleProfile(
                "LSZ",
                "LSZ / LCM luces",
                "ATSH 80 12 F1",
                new String[]{"ATSH 80 12 F1", "2100", "2101", "2103"}
        ));

        modules.put("GM5", new E46ModuleProfile(
                "GM5",
                "GM5 / ZKE confort",
                "ATSH 00 00 F1",
                new String[]{"ATSH 00 00 F1", "2100", "2101", "2105"}
        ));
    }

    public static E46ModuleProfile get(String key) {
        return modules.get(key);
    }

    public static String describeAll() {
        StringBuilder out = new StringBuilder();
        for (E46ModuleProfile profile : modules.values()) {
            out.append(profile.describe()).append("\n\n");
        }
        return out.toString().trim();
    }
}
