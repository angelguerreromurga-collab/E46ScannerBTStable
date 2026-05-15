package com.angel.e46scannerbt;

public class VehicleEquipmentProfile {
    public final String vehicle = "BMW E46 320Cd / 320d M47N";
    public final boolean electricFoldingMirrors = false;
    public final boolean electricMemorySeats = false;
    public final boolean manualMirrors = true;
    public final boolean sportSeatsManual = true;

    public boolean supportsFeature(String featureKey) {
        if (featureKey == null) return false;
        String key = featureKey.toUpperCase();
        if (key.contains("FOLDING_MIRROR")) return false;
        if (key.contains("MEMORY_SEAT")) return false;
        if (key.contains("SEAT_MEMORY")) return false;
        return true;
    }

    public String excludedFeaturesReason(String featureKey) {
        if (featureKey == null) return "Función no identificada.";
        String key = featureKey.toUpperCase();
        if (key.contains("FOLDING_MIRROR")) return "Bloqueado: este coche tiene espejos manuales, sin plegado eléctrico.";
        if (key.contains("MEMORY_SEAT") || key.contains("SEAT_MEMORY")) return "Bloqueado: este coche no tiene asientos eléctricos con memoria.";
        return "Función permitida según equipamiento declarado.";
    }
}
