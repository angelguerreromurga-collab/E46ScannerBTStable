# GM5/ZKE rear coupe windows remote close test order - final

Vehicle: BMW E46 coupe restyling 320d/320Cd M47N.
Issue: holding remote open opens all four windows, but holding remote close only closes front windows. Rear coupe quarter windows do not move.

## Interpretation

- KOMFORTOEFFNUNG_FB is working or equivalent opening logic is active.
- GM5/ZKE can command rear coupe quarter windows for opening.
- FH_HINTEN or equivalent rear window recognition is probably active.
- The likely missing branch is comfort close, not rear window recognition.

## Module

Possible module names:
- GM5
- ZKE
- ZKE5

## Priority candidates

### First target
- KOMFORTSCHLIESSUNG_FB: likely nicht_aktiv -> aktiv

Purpose:
- Enables comfort close via remote/key when holding close.

### Second target if first is insufficient
- KOMFORTSCHLIESSUNG: variable -> aktiv

Purpose:
- General comfort close enable/condition.

### Keep as-is if already working
- KOMFORTOEFFNUNG_FB: keep aktiv/current

Purpose:
- Comfort opening via remote/key already works.

### Confirm only, do not change unless clearly wrong
- FH_HINTEN: likely aktiv/current if rear windows can open electrically

Purpose:
- Rear window/quarter window recognition.

## Do not touch initially

- MAUT_FH_HINTEN
- MAUT_FH_HI
- EINKLEMMSCHUTZ
- KINDERSICHERUNG
- FH_SPERRUNG
- FH_TUEREN_HINTEN

Reason:
- MAUT relates more to one-touch/automatic movement, not primary remote comfort close.
- EINKLEMMSCHUTZ is safety/anti-trap and must remain original.
- KINDERSICHERUNG and FH_SPERRUNG are not likely cause because rear windows open correctly.
- FH_TUEREN_HINTEN likely refers to sedan/touring rear doors, not coupe quarter windows.

## Safe future test order

1. Read GM5/ZKE/ZKE5 and save current state/baseline.
2. Identify exact module and available parameters.
3. Verify current KOMFORTOEFFNUNG_FB/opening branch.
4. First future test: KOMFORTSCHLIESSUNG_FB = aktiv.
5. Test holding remote close.
6. If rear windows still do not close, evaluate KOMFORTSCHLIESSUNG = aktiv.
7. Initialize rear quarter windows if needed.
8. Do not touch MAUT/EINKLEMMSCHUTZ until remote close is resolved.

## Initialization after future window-related change

1. Ignition position 2.
2. Fully open rear quarter windows.
3. Hold open button 3-5 seconds.
4. Fully close rear quarter windows.
5. Hold close button 3-5 seconds.
6. Test remote comfort close again.

## Minimal future target set

GM5/ZKE/ZKE5:
- KOMFORTOEFFNUNG_FB: keep current/aktiv
- KOMFORTSCHLIESSUNG_FB: aktiv
- KOMFORTSCHLIESSUNG: aktiv if present and needed
- FH_HINTEN: keep/confirm current

Keep original:
- EINKLEMMSCHUTZ
- KINDERSICHERUNG
- FH_SPERRUNG
- MAUT_FH_HINTEN
- MAUT_FH_HI

## First APK behavior

First APK remains read-only.
It should show remote rear close as locked until:
- GM5/ZKE is detected,
- current state is captured,
- stable voltage is confirmed,
- and a restore path exists.
