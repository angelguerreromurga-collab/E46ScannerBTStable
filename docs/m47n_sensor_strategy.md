# M47N sensor strategy

Vehicle: BMW E46 320d/320Cd M47N/M47TUD20.
Current interface: Bluetooth ELM327 clone.
Goal: read more useful BMW-specific data than a generic Torque-style dashboard where possible.

## Strategy

1. Read standard OBD-II PIDs first.
2. Detect supported PIDs dynamically.
3. Log unsupported PIDs cleanly.
4. Try extended/BMW-specific probes only in read-only mode.
5. Save all raw responses for ChatGPT analysis.
6. Prioritize M47N-relevant diagnosis over generic sensor quantity.

## Standard OBD priority PIDs

Core:
- 0100: supported PIDs 00-20
- 0120: supported PIDs 21-40
- 0140: supported PIDs 41-60
- 0101: MIL/readiness/DTC count
- 0104: calculated load
- 0105: coolant temperature
- 010B: intake manifold absolute pressure/MAP
- 010C: RPM
- 010D: vehicle speed
- 010F: intake air temperature
- 0110: MAF airflow
- 0111: throttle position if exposed
- 011F: time since engine start
- 0121: distance with MIL on
- 0123: fuel rail pressure if supported
- 012F: fuel level if supported
- 0130: warmups since clear
- 0131: distance since clear
- 0133: barometric pressure
- 0142: control module voltage if supported
- 0146: ambient air temperature if supported
- 015C: engine oil temperature if supported

DTC:
- 03: confirmed DTC
- 07: pending DTC
- 0A: permanent DTC if supported

Vehicle info:
- 0902: VIN if supported
- 0904: calibration ID if supported

## M47N diagnostic dashboards

### Cooling / thermostat dashboard
- coolant temperature
- intake air temperature
- vehicle speed
- RPM
- engine load
- voltage
- warm-up time
- temperature stability over time

### Turbo / vacuum / boost dashboard
- MAP absolute pressure
- calculated boost = MAP - barometric pressure
- barometric pressure
- MAF
- RPM
- load
- speed
- DTC group: 4191, 4521, 4530, P1252-like symptoms if present

### EGR dashboard
- MAF
- MAP
- RPM
- load
- coolant temperature
- DTC group: 4501/P0401-like symptoms

### Glow plug dashboard
- DTC group: 4212, 4222, 4232, 4242
- battery/charging voltage
- coolant temperature at startup

### Coding readiness dashboard
- voltage
- ignition state if inferable
- protocol
- adapter identity
- module response stability
- LSZ/GM5 probe status

## Important limitation

With the current fake ELM327 v2.1 clone:
- Generic OBD reading is realistic.
- Some BMW extended DTC may work.
- Deep module data is uncertain.
- No write operations in first APK.

## App behavior

The app should present:
- Supported sensors
- Unsupported sensors
- Last raw response
- Refresh rate
- Stability score
- Export report button

The app should not hide failures. If a PID or module does not respond, it must say so clearly.
