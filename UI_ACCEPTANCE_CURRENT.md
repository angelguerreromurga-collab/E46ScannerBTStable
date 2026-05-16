# Current UI acceptance checklist

## Build under test

Use the latest artifact generated after commit:

`c1cfccc6c9e0d41d44af1933afc4950be2c3849e`

## Visual checks

### Home

- Header shows `Coding Lab E46`.
- Status pill shows LISTO or EJECUTANDO.
- Hero shows BMW E46 image or fallback text.
- Quick chips scroll horizontally.
- Status cards show BT, ELM, BACKUP and MODE.
- Main menu cards are readable and aligned.

### Navigation

- Android Back returns to the previous screen.
- Android Back does not close the app from internal screens.
- Navigation is blocked while EJECUTANDO.
- Bottom nav highlights the active item.

### Backup

- CHECKLIST section appears.
- ACCIONES section appears.
- REGISTRO section appears.
- Buttons include a right arrow.

### Tests

- LECTURA SEGURA and PRUEBAS AVANZADAS are separated.
- Advanced actions remain simulated until backup.

### Diagnostic

- INFORME MECANICO section appears.
- ACCIONES section appears.
- REGISTRO section appears.
- Decoded diagnostics remain visible after reading motor/DTC.

### Logs

- EXPORTAR section appears.
- Compartir session completa appears.
- Limpiar registro visual appears.
- REGISTRO entries include `[HH:mm:ss]` timestamps.

## Safety checks

- No write action is enabled.
- Save remains blocked.
- LSZ/GM5 remains simulated.
- SafeCommandBatch still blocks dangerous commands.
