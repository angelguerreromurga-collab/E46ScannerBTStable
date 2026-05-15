package com.angel.e46scannerbt;

public class WindowComfortProfile {
    public final String module = "GM5/ZKE";
    public final String vehicleBody = "E46 Coupe";

    public final String remoteComfortCloseRearWindowsKey = "GM5_REMOTE_COMFORT_CLOSE_REAR_WINDOWS";
    public final String interiorOneTouchRearCloseKey = "GM5_INTERIOR_ONE_TOUCH_REAR_CLOSE";
    public final String interiorOneTouchRearOpenKey = "GM5_INTERIOR_ONE_TOUCH_REAR_OPEN";

    public String remoteComfortCloseDescription() {
        return "Cerrar ventanillas traseras manteniendo pulsado cerrar en el mando/llave, si GM5/ZKE y elevalunas traseros lo soportan.";
    }

    public String interiorOneTouchDescription() {
        return "Permitir subida automática de ventanillas traseras desde el botón interior con pulsación tipo doble clic/one-touch, si el módulo lo soporta.";
    }

    public String safetyRequirement() {
        return "Requiere leer GM5/ZKE, guardar backup, comprobar antipinzamiento/inicialización y probar primero una sola ventanilla.";
    }

    public boolean isSafetyCritical() {
        return true;
    }
}
