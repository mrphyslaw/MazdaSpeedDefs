# Gen2 definition support notes

This experimental definition uses the stock calibration layout identified by its filename. Year and market have not been independently established. ROM images are not included.

Use the supplied launcher with the ordinary RomRaider installation. Update the definition and checksum module together.

## Verification

The definition contains 293 verified tuning entries and 155 diagnostic reporting controls. Addresses, native lookup dimensions, axes, units, bank selection and relevant time bases were checked against the matching image. RomRaider load, cells, axis labels, numeric edits, reporting presets, save and restore were checked. A no-edit save is byte-identical. Hardware operation and flashing have not been validated.

The existing 295-entry reference scope was reviewed in full: 284 entries have native coverage, with shared tables consolidated and additional native banks retained. The other 11 reference entries are not exposed. An exclusion means support is not established; it does not prove the ECU has no comparable function.

## Checksum behavior

RomRaider checks the native application code sum and validity markers when opening the image. When an edit changes the code sum, saving automatically updates the existing big-endian correction word at `0x3FFC0`. Already-valid images are left unchanged. Invalid markers, software identifiers or checksum-routine profiles are rejected.

The native sum covers `0x4000` through `0xBFFFF`, excluding `0x1C000` through `0x1FFFF`, and must equal `0x5AA55AA5`. Calibration edits at `0xC0000` through `0xFFFFD` are outside that sum and do not require changing its correction word. The final validity marker remains protected.

The transfer CRC and runtime diagnostic CVN are separate calculated values. The module does not replace the flasher's transfer verification. No additional stored calibration checksum rule is claimed.

## MAP sensor scaling

Six scalars are listed under **Sensor Calibration**:

- MAP Scale Base Multiplier - Control and Diagnostics
- MAP Scale Base Multiplier - Logging
- MAP Scale Second Multiplier - Control and Diagnostics
- MAP Scale Second Multiplier - Logging
- MAP Scale Subtractive Offset - Control and Diagnostics
- MAP Scale Subtractive Offset - Logging

The primary control conversion, second sampled pressure channel and startup initialization share one set of three coefficients. Logging uses a separate set. Keep corresponding control/diagnostic and logging coefficients matched to the sensor calibration.

The conversion is `pressure (kPa) = max(0, voltage × base multiplier × second multiplier − offset)`. The offset is subtracted. Standard OBD pressure output remains limited to 255 kPa; these controls do not extend that byte's range.

These are code-backed constants. Their application window is `0x40000` through `0x5FFFF`; saving also updates the checksum word in the `0x30000` through `0x3FFFF` window. A writer must include both affected application areas. A calibration-only write cannot apply MAP scaling edits.

All 1,024 ADC codes were checked with stock and matched edited coefficients, along with independent coefficient changes, zero clamping, pressure packing, logging saturation and fallback behavior. Complete native checksum sweeps accept corrected MAP edits and detect uncorrected ones. Seven RomRaider disk save/reopen tests cover each coefficient and a combined MAP/calibration edit; every restoration reproduces the original file exactly.

## Other differences from the definitions

| Reference control group | Support in this definition |
| --- | --- |
| Two numbered absolute RPM entries | Included under Fueling as **Fuel Correction Ramp - Minimum RPM** and **Maximum RPM**. They gate a correction ramp and are not the main engine rev limit. |
| Third numbered absolute RPM entry | No verified corresponding injector-path predicate; not exposed. |
| Pedal barometer multiplier | No independent multiplier established in the reviewed pedal paths. The native mapped-pedal/speed grid is included. |
| Load estimate hold activation | Reviewed retained-load selection uses different filter/max logic; no matching editable activation threshold established. |
| Alternate load target | Reviewed wastegate load-error target takes final requested load directly. No matching alternate-load table established. |
| Transition correction load threshold and hysteresis | Reviewed qualifiers use RPM/state/timer logic. The native correction amount and decay scalars are included. |
| Fuel pressure temperature additive | No matching additive established. The verified startup temperature multiplier is included with its actual axes. |
| Knock recovery steps 1 and 2 | Shared code constant also affects knock attack; independent edits are not supported. Step 3 and both recovery intervals are included. |
| Supplemental spark RPM cap | No verified corresponding independent cap. The load-retard request and final timing clamps are included. |
| Deceleration fuel-cut code patch | No target-specific instruction patch verified. Factory deceleration thresholds and hold timer are included. |

## Diagnostic controls

Each included DTC has four native reporting choices: Disabled, Internal Reporting, OBD Reporting with MIL Suppressed, and OBD Reporting with MIL Permitted. Reporting choices do not guarantee that all fault detection or failsafe behavior stops. MIL permission still depends on the fault state.

P1260 and U3000 use runtime-controlled reporting levels and have no editable ROM level in this definition. Independent monitor/readiness forcing and maturation/handling-class edits are not provided. Reporting controls do not force readiness complete.
