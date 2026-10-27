# Gen2 definition support notes

This experimental definition uses the stock calibration layout identified by its filename. Year and market have not been independently established. ROM images are not included.

Use the supplied launcher with the ordinary RomRaider installation. The definition and checksum module must be installed together.

## Verification

The definition contains 287 verified tuning entries and 155 diagnostic reporting controls. Addresses, native lookup dimensions, axes, units, bank selection and relevant time bases were checked against the matching image. RomRaider load, all cells and axis labels, numeric edits, reporting presets, save and restore were checked. A no-edit save is byte-identical. Hardware operation and flashing have not been validated.

The existing 295-entry reference scope was reviewed in full: 275 entries have native coverage, with shared tables consolidated and additional native banks retained. The other 20 reference entries are not exposed. An exclusion means support is not established; it does not prove the ECU has no comparable function.

## Checksum behavior

The module validates the native application code sum and application markers. Editable calibration addresses are outside that code sum. Saving a valid calibration edit leaves the protected code unchanged. The module rejects an invalid code sum or marker and does not repair arbitrary code changes. Additional calibration integrity rules and compatibility with a particular flasher have not been established.

Code patches and code-backed sensor scaling constants are not exposed. Keep the original image and save edits under a new filename.

## Differences from the other definitions

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
| Deceleration fuel-cut code patch | Unsupported code edit. Factory deceleration thresholds and hold timer are included. |
| Nine separate MAP scaling entries | Identified control/logging coefficients are code-backed; no independently verified third diagnostic set. These entries are not exposed. |

## Diagnostic controls

Each included DTC has four native reporting choices: Disabled, Internal Reporting, OBD Reporting with MIL Suppressed, and OBD Reporting with MIL Permitted. Reporting choices do not guarantee that all fault detection or failsafe behavior stops. MIL permission still depends on the fault state.

P1260 and U3000 use runtime-controlled reporting levels and have no editable ROM level in this definition. Independent monitor/readiness forcing and maturation/handling-class edits are not provided. Reporting controls do not force readiness complete.
