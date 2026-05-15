# Vehicle behavior baseline

Vehicle: BMW E46 coupe restyling 320d/320Cd M47N.
Owner-observed behavior before any app coding.

## Lights / LSZ

- Dashboard bulb warning appears for front position lights only.
- Low beam LED lights are not the current warning target.
- Priority LSZ target: front position light LED false warning.
- Do not apply LED check changes to low beam unless later verified.

Current lock/unlock light behavior:
- Lights/intermittents illuminate when locking the car.
- Lights do not illuminate when unlocking the car.

Coding target:
- Preserve/verify lock confirmation.
- Add unlock confirmation only if LSZ/GM5 supports it safely.

## Windows / GM5-ZKE

Current remote comfort behavior:
- Holding remote close raises front windows only.
- Holding remote close does not close rear coupe windows.

Current interior switch behavior:
- Front windows: one-touch works up and down.
- Rear coupe windows: one-touch/open auto works for opening/down.
- Rear coupe windows: one-touch close/up does not work.
- Rear coupe windows require holding the buttons to close.

Coding target priority:
1. Rear coupe windows close by holding remote close.
2. Rear coupe windows one-touch close from interior buttons.

Important interpretation:
- Rear windows already have automatic opening behavior, so hardware has at least partial automatic logic.
- Missing function is automatic closing/up for rear coupe windows.
- One-touch close may still depend on GM5/ZKE, anti-trap logic, initialization, or body variant restrictions.

Safety notes:
- EINKLEMMSCHUTZ must not be changed blindly.
- Front windows already work correctly; avoid touching front window logic unless needed for comparison.
- Test rear window functions one step at a time.
