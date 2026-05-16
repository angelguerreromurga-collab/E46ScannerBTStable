# Build trigger with body module read only base

This commit intentionally triggers GitHub Actions after adding the first Java base classes for body modules.

New classes added:

- ModuleState.java
- BodyModuleProfile.java
- BodyModuleReadOnlyManager.java

Purpose:

- Prepare LSZ lights module phase.
- Prepare GM5 comfort module phase.
- Keep everything READ ONLY.
- No real coding.
- No writing.
- No erase.
- No reset.

Current verified car capability:

- ELM Bluetooth OK.
- ISO 9141-2 detected.
- Engine PIDs readable.
- DTC readable.
- Backup READ ONLY OK.
- P0401 decoded from OBD generic.

Next app development:

1. Wire Modules screen to BodyModuleReadOnlyManager.
2. Add LSZ card.
3. Add GM5 card.
4. Add module RAW export area.
5. Add popups for blocked write/coding attempts.

Expected artifact name:

E46ScannerBT-V4.2-premium-decoded-diagnostics-debug-apk
