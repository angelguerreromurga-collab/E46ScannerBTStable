# V4.2 ready to test

APK artifact to download:

`E46ScannerBT-V4.2-premium-decoded-diagnostics-debug-apk`

App version in Gradle:

`4.2.0-premium-decoded-diagnostics`

## What is done

- Premium UI starts by default.
- ELM327 real client is wired to the premium UI.
- Safe command batch guard is active before transmission.
- OBD parser is connected to ELM responses.
- Diagnostic screen shows decoded diagnostic text.
- Backup and guided tests are present.
- Coding writes remain blocked.

## Test in the car

1. Install APK from the V4.2 artifact.
2. Open app.
3. Go to Diagnostico.
4. Press Conectar ELM327.
5. Press Inicializar ELM.
6. Press Leer motor basico.
7. Press Leer DTC.
8. Press Backup seguro READ ONLY.
9. Share full session.

## Do not do yet

- Do not enable real LSZ writing.
- Do not enable real GM5 writing.
- Do not clear DTC from this app.
- Do not add non-whitelisted commands before reviewing logs.
