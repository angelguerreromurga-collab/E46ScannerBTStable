# Next steps

Current target version: `4.1.0-premium-guided-tests`.

## Added now

- `TestResult.java`
  - Standard OK output.
  - Standard BLOCKED output.
  - Standard ERROR output.
  - Standard INFO output.

## Pending integration

1. Replace raw log messages in `PremiumActivity` with `TestResult` formatting.
2. Connect `ObdParser` to `ElmClient` response flow.
3. Show decoded live data in the Diagnostic screen.
4. Keep all LSZ/GM5 actions blocked until backup exists.
5. Keep real write operations disabled until explicit dedicated implementation.
