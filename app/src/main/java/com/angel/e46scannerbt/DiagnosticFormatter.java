package com.angel.e46scannerbt;

public class DiagnosticFormatter {
    public static String format(ObdSnapshot s) {
        if (s == null) return empty();
        StringBuilder b = new StringBuilder();
        b.append("INFORME MECANICO\n");
        b.append(s.liveText()).append("\n");
        b.append("Voltaje: ").append(value(s.voltage)).append("\n");
        b.append("Protocolo: ").append(value(s.protocol)).append("\n");
        b.append(conclusion(s));
        return b.toString();
    }

    public static String empty() {
        return "INFORME MECANICO\n" +
                "RPM --     TEMP --\n" +
                "MAP --     MAF --\n" +
                "VEL --     IAT --\n" +
                "DTC --\n" +
                "Voltaje: --\n" +
                "Protocolo: --\n" +
                "Conclusion: sin lectura suficiente.";
    }

    private static String conclusion(ObdSnapshot s) {
        if (s.dtc != null && s.dtc.contains("P0401")) {
            return "Conclusion: EGR anulada/desconectada o flujo insuficiente detectado.";
        }
        if (s.coolantC != Integer.MIN_VALUE && s.coolantC < 80) {
            return "Conclusion: temperatura baja; revisar termostatos si ocurre en marcha.";
        }
        if (s.rpm == 0 && s.mapKpa > 90 && s.mapKpa < 110) {
            return "Conclusion: contacto dado o motor parado; MAP atmosferico coherente.";
        }
        return "Conclusion: sin anomalía clara con los datos actuales.";
    }

    private static String value(String s) {
        if (s == null || s.trim().length() == 0) return "--";
        return s.trim();
    }
}
