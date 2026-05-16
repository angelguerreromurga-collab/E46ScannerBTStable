package com.angel.e46scannerbt;

public class TestResult {
    public static String ok(String title, String details) {
        return "OK - " + title + "\n" + clean(details);
    }

    public static String blocked(String title, String reason) {
        return "BLOQUEADO - " + title + "\n" + clean(reason) + "\nNo se ha modificado nada.";
    }

    public static String error(String title, String details) {
        return "ERROR - " + title + "\n" + clean(details);
    }

    public static String info(String title, String details) {
        return "INFO - " + title + "\n" + clean(details);
    }

    private static String clean(String value) {
        if (value == null || value.trim().length() == 0) return "Sin detalles.";
        return value.trim();
    }
}
