# LSZ LED and lock/unlock visual confirmation targets

Vehicle: BMW E46 coupe restyling 320d/320Cd M47N.
Phase: Bluetooth first APK remains read-only. These are later validation targets.

## LSZ / LCM / ALSZ - LED front position checks

Primary target if present:
- KALTUEBERWACHUNG_SL_V: aktiv -> nicht_aktiv
- WARMUEBERWACHUNG_SL_V: aktiv -> nicht_aktiv

Side-specific variants:
- KALTUEBERWACHUNG_SL_L: aktiv -> nicht_aktiv
- KALTUEBERWACHUNG_SL_R: aktiv -> nicht_aktiv
- WARMUEBERWACHUNG_SL_L: aktiv -> nicht_aktiv
- WARMUEBERWACHUNG_SL_R: aktiv -> nicht_aktiv

Standlicht variants:
- KALTUEBERWACHUNG_STANDLICHT: aktiv -> nicht_aktiv
- WARMUEBERWACHUNG_STANDLICHT: aktiv -> nicht_aktiv
- KALTUEBERWACHUNG_STANDLICHT_L/R: aktiv -> nicht_aktiv
- WARMUEBERWACHUNG_STANDLICHT_L/R: aktiv -> nicht_aktiv

Meaning:
- KALTUEBERWACHUNG: cold check, startup/off-state pulse check.
- WARMUEBERWACHUNG: warm check, live consumption monitoring while active.
- SL: Standlicht / position light.
- V: vorne/front.
- L/R: left/right depending daten variant.

Risk model:
- Cold check off: low risk; removes LED flash/false bulb warning.
- Warm check off: low/medium risk; may remove bulb-out warning for that circuit.

Validation order:
1. Confirm exact parameter name from read response or imported trace.
2. Start with cold check for front position only.
3. If warning remains while light is active, evaluate warm check.
4. Never disable unrelated lamp checks blindly.

## LSZ / GM5 - visual confirmation on lock/unlock

Primary LSZ candidates:
- QUIT_BLK_SCHAERF: nicht_aktiv -> aktiv
- QUIT_BLK_ENTSCH: nicht_aktiv -> aktiv

Related candidates:
- QUIT_OPT_SCHAERF: nicht_aktiv -> aktiv
- QUIT_OPT_ENTSCH: nicht_aktiv -> aktiv
- BLINKEN_BEI_SCHAERF
- BLINKEN_BEI_ENTSCH

Possible GM5/ZKE related candidates:
- QUIT_OPT_SCHAERF
- QUIT_OPT_ENTSCH
- DWA
- DWA_QUITTIERUNG

DWA rule:
- Do not activate DWA by default if alarm hardware is absent.
- First test visual confirmation with DWA kept inactive.
- Avoid acoustic confirmation parameters unless siren/hardware is verified.

Risk model:
- Visual confirmation: low risk.
- DWA activation without hardware: medium risk, can create strange alarm behavior or stored faults.

Test order:
1. Read LSZ/GM5 state.
2. Keep DWA inactive if no real alarm.
3. Try lock/unlock visual parameters only.
4. Verify with remote open/close.
5. If no effect, check equivalent GM5 parameter availability before considering DWA-related logic.

## First APK behavior

The first APK must not write these values.
It should display them as locked targets and collect:
- adapter info,
- protocol,
- voltage,
- DTC,
- raw responses,
- user-observed LED warning behavior,
- exportable ChatGPT report.
