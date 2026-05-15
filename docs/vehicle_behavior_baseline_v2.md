# Vehicle behavior baseline v2

Vehicle: BMW E46 coupe restyling 320d/320Cd M47N.

## Equipment

- No sunroof.
- Rear side windows are coupe rear pop-out/quarter windows, not normal rear door windows.
- No electric folding mirrors.
- No electric memory seats.

## LSZ / lights

- Dashboard warning is for front position lights only.
- Low beam is not the warning target.
- Bulb warning appears when ignition/contact is switched on.
- This points primarily to cold check for front position lights.
- Warm check may still be evaluated only if warning remains while position lights are on.

Current remote light behavior:
- Opening the car now illuminates position lights.
- Locking behavior must be verified/preserved.

Priority LSZ target:
1. Front position LED cold check handling.
2. Front position LED warm check only if needed.
3. Preserve or refine lock/unlock visual confirmation.

## GM5 / ZKE - rear coupe windows

Remote behavior:
- Holding open on remote opens all four windows.
- Holding close on remote closes only the front windows.
- Rear coupe quarter windows remain open when holding close.

Interior switch behavior:
- Front windows: one-touch works up and down.
- Rear coupe quarter windows: automatic opening/down behavior works.
- Rear coupe quarter windows: automatic closing/up does not work; button must be held.

Important interpretation:
- No sunroof simplifies comfort close logic.
- Rear coupe windows have partial automated behavior on opening.
- Missing function is rear automatic closing/up and rear inclusion in comfort close.
- Because coupe rear windows are pop-out/quarter windows, sedan/touring rear-door parameters must be treated with suspicion.

Do not touch first:
- FH_TUEREN_HINTEN unless proven relevant to coupe rear quarter windows.
- EINKLEMMSCHUTZ unless exact role is known.

Priority GM5/ZKE targets:
1. Rear quarter windows close when holding remote close.
2. Rear quarter windows one-touch close from interior switch if supported.

Test model:
- One function at a time.
- Read current behavior and module state first.
- Preserve front window behavior.
- No sunroof-related functions needed.
