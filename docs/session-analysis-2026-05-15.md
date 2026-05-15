# Session analysis 2026-05-15

Captured from E46 Scanner BT.

## Connection
- Adapter: OBDII / 01:23:45:67:89:BA
- ELM: ELM327 v2.1 clone
- Voltage: 14.6 V
- Protocol auto: ATDP AUTO, ATDPN A0

## Raw reads
- 0100: SEARCHING...
- 0120: 486B124100983B0011EA
- 0140: NO DATA
- 010C: 486B12410C000012
- 0105: 486B1241056570
- 010D: 486B12410D0013
- 010B: 486B12410B6273
- 010F: 486B12410F5D72
- 0110: 486B124110008298
- 0142: NO DATA
- 03: 486B12430401000000000D
- 07: 486B12470000000000000C

## Decoded useful values
BMW/KWP responses contain a header before the OBD payload. The app must search for 41 <PID> and parse bytes after it.

- RPM 010C: 41 0C 00 00 -> 0 rpm
- Coolant 0105: 41 05 65 -> 0x65 - 40 = 61 C
- Speed 010D: 41 0D 00 -> 0 km/h
- MAP 010B: 41 0B 62 -> 98 kPa / 980 mbar absolute
- IAT 010F: 41 0F 5D -> 53 C
- MAF 0110: 41 10 00 82 -> 1.30 g/s
- DTC 03: 43 04 01 00 00 00 00 -> P0401, then zeros
- Pending DTC 07: no pending DTC

## App fixes required for v1.0
1. Add parser that finds OBD payload inside BMW/KWP header.
2. Decode common live values into human-readable cards.
3. Decode DTC P0401 and known BMW M47N hints.
4. Avoid protocol spam: manual ATSP3/4/5 attempts caused UNABLE TO CONNECT with this adapter.
5. Add button to share complete session from app.
6. Keep write/coding locked.
