# Key remote baseline

Vehicle: BMW E46 coupe restyling 320d/320Cd M47N.

Remote key type:
- 3-button BMW remote key.
- Buttons: lock, unlock, trunk.

Current observed behavior:
- Holding unlock opens all four windows while button is held.
- Holding lock closes only front windows while button is held.
- Rear coupe quarter windows do not close with remote lock hold.
- Lock confirmation works with indicators.
- Unlock confirmation does not flash indicators.

Relevant comfort targets:
- KOMFORTOEFFNUNG_FB: appears functionally active because remote comfort opening works.
- KOMFORTSCHLIESSUNG_FB: primary suspect for missing rear remote close.
- QUIT_BLK_SCHAERF: lock visual confirmation appears active.
- QUIT_BLK_ENTSCH: candidate for missing unlock visual confirmation.

No action in first APK:
- Read-only only.
- Collect remote behavior in report.
- Coding remains locked until module detection and baseline capture.
