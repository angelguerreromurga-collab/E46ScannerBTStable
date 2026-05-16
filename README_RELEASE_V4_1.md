# Coding Lab E46 V4.1 Premium Guided Tests

## Estado

Versión: `4.1.0-premium-guided-tests`  
Modo: `SAFE / READ ONLY`  
Coche objetivo: BMW E46 320d/320Cd M47N

## Incluido

- Interfaz premium nueva arrancando desde `PremiumActivity`.
- Menú principal estilo Coding Lab.
- Secciones: Coding Lab, Backup seguro, Pruebas, Luces, Ventanillas, Confort, Diagnóstico, Información y Logs.
- Botón Android atrás vuelve a la pantalla anterior.
- Tabs horizontales en Coding Lab.
- Switches funcionales bloqueados hasta backup.
- Bottom navigation.
- `ElmClient` para conexión Bluetooth SPP con ELM327.
- Comandos ELM READ ONLY:
  - `ATZ`
  - `ATE0`
  - `ATL0`
  - `ATS0`
  - `ATH1`
  - `ATI`
  - `ATRV`
  - `ATSP0`
  - `ATDP`
  - `ATDPN`
  - `0100`
  - `0120`
  - `0140`
  - `010C`
  - `0105`
  - `010D`
  - `010B`
  - `010F`
  - `0110`
  - `03`
  - `07`
  - `0A`
- Backup seguro READ ONLY.
- Exportar sesión completa.
- `ObdSnapshot` preparado.
- `ObdParser` preparado.
- `SafetyGate` preparado.
- `CodingActions` preparado.
- `SafeTestWorkflow` preparado.

## Bloqueos de seguridad

- No borra DTC.
- No escribe módulos.
- No codifica LSZ.
- No codifica GM5.
- No permite guardar cambios sin backup.
- LSZ/GM5 quedan en simulación aunque exista backup.

## Prueba recomendada en coche

1. Instalar APK del artifact `E46ScannerBT-V4.1-premium-guided-tests-debug-apk`.
2. Abrir app.
3. Confirmar que arranca la interfaz nueva, no `MainActivity` vieja.
4. Entrar en `Backup seguro`.
5. Pulsar `1 · Conectar Bluetooth`.
6. Pulsar `2 · Inicializar ELM327`.
7. Pulsar `3 · Backup OBD/ECU READ ONLY`.
8. Compartir sesión completa.
9. Entrar en `Pruebas`.
10. Ejecutar Test 1, Test 2, Test 3 y Test 4.
11. Confirmar que Test 5 y Test 6 no escriben nada.

## Siguiente paso técnico

Conectar `ObdParser` al flujo de `ElmClient` para mostrar valores vivos decodificados en pantalla, además del RAW.
