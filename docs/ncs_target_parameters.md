# NCS target parameters for E46 comfort features

Vehicle: BMW E46 Coupe restyling 320d/320Cd M47N.
Current first phase: Bluetooth ELM327 read/capture/report only.
These parameters are not written in first APK. They are target names for later validation.

## LSZ / LCM targets

### LED front position false bulb warning
Main candidates:
- KALTUEBERWACHUNG_SL_V = nicht_aktiv
- WARMUEBERWACHUNG_SL_V = nicht_aktiv

Possible variants depending LSZ/daten:
- KALTUEBERWACHUNG_SL_L
- KALTUEBERWACHUNG_SL_R
- WARMUEBERWACHUNG_SL_L
- WARMUEBERWACHUNG_SL_R
- KALTUEBERWACHUNG_STANDLICHT
- WARMUEBERWACHUNG_STANDLICHT

Notes:
- KALT handles cold check / startup pulses.
- WARM handles bulb monitoring while active.
- First real target is front position LED warning, not random LED circuits.

### Lock/unlock visual confirmation
Main candidates:
- QUIT_BLK_SCHAERF = aktiv
- QUIT_BLK_ENTSCH = aktiv

Possible related candidates:
- QUIT_OPT_SCHAERF
- QUIT_OPT_ENTSCH
- BLINKEN_BEI_SCHAERF
- BLINKEN_BEI_ENTSCH

Safety note:
- Do not activate DWA as first approach if alarm hardware is absent.
- First try visual confirmation without pretending DWA exists.

## GM5 / ZKE targets

### Rear windows comfort close by remote/key
Main candidate:
- KOMFORTSCHLIESSUNG_FB = aktiv

Related candidates:
- KOMFORTSCHLIESSUNG
- FH_KOMFORT_SCHLIESSEN
- FH_HINTEN
- FH_TUEREN_HINTEN

### Rear windows comfort open by remote/key
Secondary candidate:
- KOMFORTOEFFNUNG_FB = aktiv

This is secondary, not first priority.

### Rear windows one-touch close from interior switch
Main candidates:
- MAUT_FH_HINTEN = aktiv

Possible variants:
- MAUT_FH_HI
- FH_AUTOMATIK_HINTEN
- FH_HINTEN
- FH_TUEREN_HINTEN
- FH_AUTOMATIK

Safety/hardware notes:
- Coupe rear windows are not normal rear door windows.
- Feature may appear in daten but not work due to GM5, hardware, anti-trap or body variant.
- After any window-related validation, window initialization may be required.

## Test order for future Coding Lab

1. Read LSZ/GM5 identifiers and raw responses.
2. Export report to ChatGPT.
3. Verify if LSZ/GM5 can be reached with Bluetooth ELM.
4. First coding candidate if reachable: LSZ LED check handling.
5. Second: LSZ visual lock/unlock confirmation.
6. Third: GM5 remote rear window comfort close.
7. Fourth: GM5 rear one-touch interior close.

## Locked until validated

All functions remain locked until:
- module detected,
- exact variant identified,
- current state captured,
- restore path defined,
- stable voltage confirmed,
- one single change tested,
- user validates result.
