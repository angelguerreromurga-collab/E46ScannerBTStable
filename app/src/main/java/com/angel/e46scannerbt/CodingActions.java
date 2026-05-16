package com.angel.e46scannerbt;

public class CodingActions {
    public static final String LSZ_LED_COLD_CHECK = "LSZ LED cold check";
    public static final String LSZ_LED_WARM_CHECK = "LSZ LED warm check";
    public static final String GM5_COMFORT_CLOSE = "GM5 comfort close";
    public static final String GM5_BLINK_UNLOCK = "GM5 blink unlock";
    public static final String COUPE_REAR_WINDOWS = "Coupe rear windows comfort";

    public static String description(String action) {
        if (LSZ_LED_COLD_CHECK.equals(action)) return "Desactivar comprobación fría LED para evitar parpadeos.";
        if (LSZ_LED_WARM_CHECK.equals(action)) return "Desactivar comprobación caliente LED para evitar aviso de bombilla.";
        if (GM5_COMFORT_CLOSE.equals(action)) return "Activar cierre confort de ventanillas con mando.";
        if (GM5_BLINK_UNLOCK.equals(action)) return "Activar confirmación visual al abrir con mando.";
        if (COUPE_REAR_WINDOWS.equals(action)) return "Ajustar funciones confort de ventanillas traseras coupe.";
        return "Acción de coding no documentada.";
    }
}
