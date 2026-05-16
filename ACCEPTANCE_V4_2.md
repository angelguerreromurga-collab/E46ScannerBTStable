# Acceptance checklist V4.2

Target version: `4.2.0-premium-decoded-diagnostics`

Artifact: `E46ScannerBT-V4.2-premium-decoded-diagnostics-debug-apk`

## Main purpose

This build validates the new premium UI with real ELM communication, safe command batching, OBD parsing and decoded diagnostic display.

## Must pass

- App opens in `Coding Lab E46` premium UI.
- `Diagnóstico` screen shows `INFORME MECANICO` instead of only raw placeholders.
- `Conectar ELM327` connects to paired OBD adapter.
- `Inicializar ELM` runs AT commands.
- `Leer motor básico` updates decoded data when ELM responses are valid.
- `Leer DTC` updates DTC text when a diagnostic response is present.
- `Backup seguro READ ONLY` runs only whitelisted commands.
- The session log still includes RAW commands and responses.
- `Compartir sesión completa` exports the full log.

## Safety requirements

The following commands must never be sent by this build:

- `04`
- `14`
- `2E*`
- `3B*`
- `30*`
- `31*`
- `34*`
- `36*`
- `37*`

`ElmClient.run()` is guarded by `SafeCommandBatch` before Bluetooth transmission.

## Test order in the car

1. Open app.
2. Go to `Diagnóstico`.
3. Press `Conectar ELM327`.
4. Press `Inicializar ELM`.
5. Press `Leer motor básico`.
6. Press `Leer DTC`.
7. Press `Backup seguro READ ONLY`.
8. Press `Compartir sesión completa`.
9. Send the exported session for analysis.

## Expected decoded values on previous E46 sample data

- RPM: 0 rpm
- TEMP: 49 C
- VEL: 19 km/h
- MAP: 98 kPa
- IAT: 44 C
- MAF: about 1.27 g/s
- DTC: P0401 EGR insuficiente

## If something fails

Send the full exported session. Do not retry coding or advanced tests until the log is reviewed.
