# GM5/ZKE rear window comfort targets

Vehicle: BMW E46 coupe restyling 320d/320Cd M47N.
Phase: Bluetooth first APK remains read-only. These are later validation targets.

## Module names

Possible module names:
- GM5
- ZKE
- ZKE5

## Priority 1 - rear windows comfort close by remote/key

Primary candidate:
- KOMFORTSCHLIESSUNG_FB: likely nicht_aktiv -> aktiv

Related candidates:
- KOMFORTSCHLIESSUNG: variable -> aktiv
- FH_HINTEN: should remain aktiv if rear electric windows are present

Secondary/opening candidate:
- KOMFORTOEFFNUNG_FB: variable -> aktiv

Notes:
- Applies to coupe if GM5/hardware supports it.
- Rear coupe windows are not normal rear door windows.
- FH_TUEREN_HINTEN likely refers to rear doors on sedan/touring and should not be touched first in coupe.

Initial safe test order:
1. Probe/read GM5/ZKE response.
2. Capture current state/raw baseline.
3. If later coding becomes available, test KOMFORTSCHLIESSUNG_FB alone first.
4. If no effect and present, evaluate KOMFORTSCHLIESSUNG.
5. Only evaluate FH_HINTEN if present and clearly tied to electric rear windows.
6. Do not touch FH_TUEREN_HINTEN initially.

## Priority 2 - rear interior one-touch close

Main candidates:
- MAUT_FH_HINTEN: likely nicht_aktiv -> aktiv if present

Possible variants:
- MAUT_FH_HI
- FH_AUTOMATIK_HINTEN
- AUTOMATIKLAUF_FH_HINTEN
- FH_AUTOMATIKLAUF_HINTEN

Notes:
- Much more uncertain than comfort close by remote.
- Parameter may exist but not execute on E46 coupe due to GM5 logic, anti-trap, hardware or body variant.
- Treat as experimental and locked until module and hardware behavior are confirmed.

## Anti-trap / EINKLEMMSCHUTZ

Critical rule:
- Do not modify EINKLEMMSCHUTZ blindly.
- Keep original value.

Reason:
- It may be a condition for automatic close.
- Disabling or altering it can create unsafe window behavior.

## Initialization after any future window coding

Recommended after any window-related change:
1. Ignition position 2.
2. Lower the window fully.
3. Hold button down 3-5 seconds.
4. Raise the window fully.
5. Hold button up 3-5 seconds.
6. Repeat for each relevant window.
7. Test remote comfort close and interior one-touch separately.

## Risk ranking

Low:
- KOMFORTSCHLIESSUNG_FB
- KOMFORTSCHLIESSUNG

Low/medium:
- FH_HINTEN if clearly present and tied to rear electric windows
- MAUT_FH_HINTEN / MAUT_FH_HI / FH_AUTOMATIK_HINTEN

Medium/high:
- EINKLEMMSCHUTZ changes
- FH_TUEREN_HINTEN on coupe without clear confirmation
- multiple changes at once

## First APK behavior

The first APK must not write these values.
It should show these as locked targets and collect:
- ELM identity
- protocol
- voltage
- OBD live data
- DTC
- raw command responses
- GM5/ZKE probe attempts if supported
- user notes on current rear window behavior
- exportable report for ChatGPT
