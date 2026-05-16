# Build trigger for V4.3 final visual polish

This commit intentionally triggers GitHub Actions after the latest premium visual pass.

Included visual polish:

- Reduced visual noise.
- Softer card borders.
- Cleaner header.
- Compact status pill.
- Hero with dedicated car image area.
- ImageView integration for `bmw_e46_user_hero`.
- Smaller premium quick chips.
- Home menu without cheap Unicode icons.
- Accent bars instead of noisy icons.
- Reduced Home clutter.
- Safe mode and no-write guard remain active.

Expected artifact name:

`E46ScannerBT-V4.2-premium-decoded-diagnostics-debug-apk`

Validation priority:

1. APK compiles.
2. App opens without crash.
3. Home does not visually reject at first glance.
4. Hero does not overlap text.
5. Car image is centered and contained.
6. Chips are not cut.
7. Bottom navigation remains usable.
8. Backup, Tests, Diagnostics and Logs still work.
