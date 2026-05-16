# Safe final test build for car connection

This commit intentionally triggers GitHub Actions for the first real car connection test.

Mode for this APK:

- READ ONLY.
- SAFE MODE.
- No real coding.
- No module writing.
- No erase DTC command.
- No LSZ/GM5/DDE/KOMBI write action.

Allowed during first car test:

1. Open app.
2. Connect Bluetooth ELM327.
3. Initialize ELM327.
4. Read protocol.
5. Read motor/basic PIDs.
6. Read DTC.
7. Run Backup OBD/ECU READ ONLY.
8. Share/export full session log.

Do not use as real coding tool yet.

Expected artifact name:

E46ScannerBT-V4.2-premium-decoded-diagnostics-debug-apk

Critical validation:

1. APK compiles.
2. App opens without crash.
3. Home loads.
4. Backup screen opens.
5. Tests screen opens.
6. Diagnostics screen opens.
7. Logs screen opens.
8. Android Back returns to previous screen.
9. Export/share works.
10. No write action is possible.
