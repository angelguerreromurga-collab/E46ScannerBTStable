# Comfort targets - Bluetooth phase

Primary vehicle: BMW E46 320Cd / 320d M47N.
Current interface: Bluetooth ELM327 clone.
First APK goal: read-only, data capture, RAW report and locked Coding Lab.

## Active target modules

### LSZ / LCM
Purpose:
- LED false bulb warnings.
- Front position LED check handling.
- Visual lock/unlock confirmation.

Relevant public NCS keywords to map later:
- KALTUEBERWACHUNG
- WARMUEBERWACHUNG
- SL
- BLK
- AL
- FL
- KZL
- QUIT
- SCHAERF
- ENTSCH
- OPT
- BLK

Potential functions:
1. LED front position false warning handling.
2. Lock visual confirmation.
3. Unlock visual confirmation.
4. Follow-me-home only if safely supported.

## GM5 / ZKE
Purpose:
- Rear window comfort close.
- Rear window one-touch behavior.
- Locking comfort features.

Relevant public NCS keywords to map later:
- KOMFORTSCHLIESSUNG
- KOMFORTOEFFNUNG
- FB
- FH
- HINTEN
- VORNE
- MAUT
- AUTOMATIK
- EINKLEMMSCHUTZ

Potential functions:
1. Rear windows close when holding remote/key close.
2. Rear windows one-touch close from interior switch if hardware supports it.
3. Automatic lock while driving as secondary target.

## Hard safety limits

Blocked modules:
- DDE coding.
- EWS.
- ABS/DSC.
- SRS/Airbag.
- Alarm functions unless explicitly verified.

Blocked actions in first APK:
- No write operations.
- No security access.
- No routine control.
- No irreversible coding.

## Unlock model

Every comfort feature starts locked.
A feature can only move forward after:
1. Read module response.
2. Identify module/version.
3. Save current state or raw baseline.
4. Verify restore path.
5. Stable voltage.
6. One single controlled test.
7. User validates result.
8. Mark feature safe for this car.

## First APK implementation rule

The app will not try to perform coding yet.
It will collect:
- ELM identity.
- Protocol.
- Voltage.
- Supported PIDs.
- DTC.
- Live OBD values.
- RAW responses.
- Any LSZ/GM5 probe responses if available.
- Export report for ChatGPT.
