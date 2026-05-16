# Acceptance checklist V4.1

Target version: `4.1.0-premium-guided-tests`

## UI checks

- App opens directly in `Coding Lab E46`.
- It does not open the old `MainActivity` screen.
- Header shows the premium dark layout.
- Hero BMW is visible at the top right.
- Main menu cards are visible and tappable.
- Android back button returns to the previous app screen.
- Bottom navigation works.
- Coding Lab tabs scroll horizontally.
- Switches can be touched but remain blocked until backup.

## Bluetooth / ELM checks

- `Backup seguro > 1 Conectar Bluetooth` finds paired ELM327.
- If no ELM is paired, app shows an error in logs.
- `2 Inicializar ELM327` runs AT commands.
- `3 Backup OBD ECU READ ONLY` runs read-only commands.
- The app does not send clear-DTC or write commands.

## Safe test checks

- Test 1 connects and initializes ELM.
- Test 2 reads protocol.
- Test 3 reads motor PIDs.
- Test 4 reads DTC.
- Test 5 LSZ remains blocked or simulated.
- Test 6 GM5 remains blocked or simulated.

## Safety acceptance

- No command `04` is sent.
- No coding command is sent.
- No LSZ write is sent.
- No GM5 write is sent.
- Backup must exist before any advanced preparation.

## Required user report after test

Share the full session log from the app after running:

1. Backup seguro complete flow.
2. Tests 1 to 4.
3. Tests 5 and 6 to confirm they stay safe.
