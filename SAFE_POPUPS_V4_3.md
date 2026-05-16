# Safe Popups V4.3

## Objective

Add native Android warning and explanation popups to Coding Lab E46 before any blocked, simulated or advanced operation.

The app must remain safe:

- No write operation without backup.
- No module coding without explicit confirmation.
- No dangerous OBD command.
- No LSZ/GM5 action outside simulation.

## Popup 1: Backup required

Trigger:

- User toggles a coding switch before backup.
- User presses Save before backup.
- User opens an advanced test before backup.
- User attempts LSZ/GM5 preparation before backup.

Title:

`Backup obligatorio`

Message:

`Esta función está bloqueada porque todavía no existe un backup seguro de la sesión. Para proteger el módulo, primero conecta el ELM327, inicializa el adaptador y ejecuta Backup OBD/ECU READ ONLY. No se escribirá nada en el coche.`

Steps shown:

1. Conectar Bluetooth.
2. Inicializar ELM327.
3. Leer protocolo.
4. Leer motor/DTC.
5. Ejecutar Backup seguro.
6. Exportar sesión.

Buttons:

- `Ir a Backup`
- `Cancelar`

Expected action:

- `Ir a Backup` opens `showBackup()` or `go("backup", true)`.
- `Cancelar` closes popup only.

## Popup 2: Safe mode explanation

Trigger:

- User presses an advanced simulated action after backup.
- User presses Save after backup.

Title:

`Modo seguro activo`

Message:

`La app está en modo seguro. La operación se preparará solo en simulación. No se escribirá en LSZ, GM5, DDE ni KOMBI hasta que exista una fase de escritura validada y reversible.`

Buttons:

- `Entendido`

Expected action:

- Add a timestamped log entry.
- Do not transmit write commands.

## Popup 3: Busy operation

Trigger:

- User taps while an ELM operation is running.
- User tries Android Back during operation.

Title:

`Operación en curso`

Message:

`Espera a que termine la lectura actual. Cortar una comunicación OBD a medias puede dejar datos incompletos o hacer que el adaptador ELM327 se quede bloqueado hasta reiniciar.`

Buttons:

- `Aceptar`

Expected action:

- No navigation.
- No second command.

## Popup 4: Dangerous command blocked

Trigger:

- SafeCommandBatch blocks a dangerous service or write-like command.

Title:

`Comando bloqueado`

Message:

`Este comando no se enviará al vehículo. Está bloqueado por seguridad porque podría borrar información, escribir datos o iniciar una rutina no reversible.`

Buttons:

- `Ver pasos seguros`
- `Cerrar`

Expected action:

- `Ver pasos seguros` opens Backup/Tests safe screen.

## Implementation target in PremiumActivity

Add:

```java
import android.app.AlertDialog;
```

Add helper:

```java
private void popup(String title, String message) {
    runOnUiThread(() -> new AlertDialog.Builder(this)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton("Entendido", null)
        .show());
}
```

Add backup popup helper:

```java
private void requireBackupPopup(String actionName) {
    runOnUiThread(() -> new AlertDialog.Builder(this)
        .setTitle("Backup obligatorio")
        .setMessage("Bloqueado: " + actionName + "\n\nPasos seguros:\n1. Conectar Bluetooth.\n2. Inicializar ELM327.\n3. Leer protocolo.\n4. Leer motor/DTC.\n5. Ejecutar Backup seguro.\n6. Exportar sesión.")
        .setPositiveButton("Ir a Backup", (d, w) -> go("backup", true))
        .setNegativeButton("Cancelar", null)
        .show());
}
```

Replace blocked code paths:

- Switch blocked path should call `requireBackupPopup(title)`.
- Save blocked path should call `requireBackupPopup("Guardar cambios")`.
- Advanced test blocked path should call `requireBackupPopup(name)`.
- Busy navigation should call `popup("Operación en curso", ...)`.

## Compile note

Current `PremiumActivity.java` uses `ColorStateList` in `switchRow()` but lacks the explicit import. Either add:

```java
import android.content.res.ColorStateList;
```

or remove manual switch tinting to avoid the dependency.
