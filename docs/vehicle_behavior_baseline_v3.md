# Vehicle behavior baseline v3 - final pre-APK notes

Vehicle: BMW E46 coupe restyling 320d/320Cd M47N.

## Rear coupe quarter windows / GM5-ZKE

Remote comfort behavior:
- Holding open on remote opens front and rear coupe windows.
- User must keep remote open button pressed; if released, all windows stop.
- Holding close on remote closes front windows only.
- Rear coupe quarter windows do not move, make no obvious sound, and show no closing attempt when remote close is held.

Interior switch behavior:
- Front windows work normally.
- Rear coupe quarter windows work from interior switches.
- Rear windows remain usable for around 10-15 minutes after ignition is turned off, similar to front windows.
- Rear quarter windows require holding button for close/up.
- Automatic rear open/down behavior exists, but automatic rear close/up does not.

Interpretation:
- GM5/ZKE can command rear quarter windows for opening.
- Rear comfort close command is not being sent/executed for rear quarter windows.
- This makes remote rear comfort close a real target, not a fantasy feature.
- Interior rear one-touch close remains more experimental.

Priority GM5/ZKE target order:
1. Rear quarter windows close when holding remote close.
2. Rear quarter windows one-touch close from interior buttons.

Do not touch first:
- EINKLEMMSCHUTZ.
- FH_TUEREN_HINTEN.
- Any unrelated sedan/touring rear-door logic.

## Lights / LSZ

LED position light behavior:
- LED front position lights trigger dashboard warning on both sides.
- On ignition/contact before engine start, LED position lights perform strange flashing/pulses and then stop.
- This strongly indicates cold check activity.
- The bulb warning remains fixed on the cluster even when position lights are switched on.
- This suggests warm check / consumption monitoring may also need evaluation.

Priority LSZ target order:
1. Front position LED cold check handling, both sides.
2. If warning remains, front position warm check handling, both sides.
3. Do not target low beam LED/cross beam because user says warning is only position lights.

Likely parameter groups to search later:
- KALTUEBERWACHUNG_SL_V or SL_L/SL_R or STANDLICHT variants.
- WARMUEBERWACHUNG_SL_V or SL_L/SL_R or STANDLICHT variants.

## Lock/unlock visual confirmation

Current behavior:
- Car flashes indicators when locking.
- Car does not flash indicators when unlocking.
- When locking, front LED position lights remain on for about one minute.
- Alarm/DWA presence is unknown.

Priority target:
- Keep current lock behavior intact.
- Add unlock visual confirmation only if safely supported.
- Do not activate DWA by default.
- Avoid acoustic confirmation unless alarm hardware is verified.

## Unknowns

- DWA/alarm equipment unknown.
- Exact LSZ version unknown.
- Exact GM5/ZKE version unknown.
- ELM327 ability to reach LSZ/GM5 unknown.

## First APK requirement

The first APK must collect:
- ELM identity.
- Protocol and voltage.
- Live OBD data.
- DTC.
- Raw responses.
- User-observed behavior notes.
- Any possible LSZ/GM5 probe responses.
- Exportable ChatGPT report.

No coding writes in first APK.
