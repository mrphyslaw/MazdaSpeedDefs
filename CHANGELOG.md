# Changelog

## 0.16 - 2026-10-20

- Renamed the accumulator-reset option to Disable Fuel Correction Accumulators in L33XEH, L35HEH and L39SED/SEE/SEF.
- Retained Disable Decel Fuel Cut in L38KEH and L3M7EF, where the native switch controls the deceleration fuel-cut ramp; clarified that other fuel-cut protections remain active.
- Verified each ROM independently; preserved switch addresses, encoded byte states, table data, axes and checksum support.

## 0.15 - 2026-10-19

- Documented verified stock lookup ranges and edge-holding behavior in tuner help.
- Clarified fuel, ignition, load and throttle bank priorities and blending.
- Renamed temperature-retard banks to Normal Mode and Abnormal Mode to match ignition selection.
- Clarified VVT target weighting and state/high-RPM overrides.
- Distinguished additive duty points from percentages of base duty; clarified error-axis signs and selector values.
- Preserved numeric conversions and breakpoints; gave boost-mode selectors integer editing increments.
- Passed native-code and RomRaider display/edit/save/checksum checks; approved for publication.

## 0.14 - 2026-10-19

- Added separate accumulated wastegate duty-correction limits across all seven definitions.
- Clarified the existing combined wastegate correction limits and final duty range.
- Corrected the injector-capacity margin unit label to load; numeric scaling is unchanged.
- Clarified overall requested-load arbitration and the corrected-load input to the three throttle maps.
- Renamed the base overboost pressure threshold to match its verified function.
- Passed native-code and RomRaider load/edit/save/checksum checks; approved for publication.

## 0.13 - 2026-10-19

- Added pressure-target temperature correction and its RPM/load multiplier where supported.
- Added the stock deceleration pressure target and verified its qualification and RPM hysteresis.
- Added three pump base-phase banks, pressure-blending anchors and valve delay/peak/hold timing controls.
- Corrected pressure-feedback scaling and help to additive fuel-demand units.
- Preserved the earlier stock strategy, including voltage-only pump delay and peak-duration curves.
- Pump phase uses native counts and fuel demand uses the ECU lookup index; absolute crank-angle and physical fuel-quantity units remain unconfirmed.
- Passed native-code and RomRaider load/edit/save/checksum checks; approved for publication.

## 0.12 - 2026-10-19

- Confirmed the nominal electrical end-of-injection reference before compression TDC; updated phase units and tuner help.
- Added the injector-capacity load margin and verified its link to common load and boost control.
- Added fuel-pressure controller, cold-limit hold/recovery, pressure-monitor, sensor-fault substitute and target-blending controls.
- Added the separate after-start pressure target where present; preserved each stock selector.
- Corrected the cold pressure-limit temperature label to stored intake-air temperature.
- Passed native-code and RomRaider edit/save/checksum checks; approved after local manual review.

## 0.11 - 2026-10-18

- Added verified stock injection-window limits and coolant/after-start phase corrections across all seven definitions.
- Added warm-mode temperature and hysteresis controls where supported; preserved the earlier ROM strategy.
- Converted verified timers and rates to seconds-based units; documented engine-event timing and RPM dependence.
- Clarified phase selection, blending, pulsewidth interaction and tuner help.
- Passed native-code and RomRaider edit/save/checksum checks; approved after local manual review.
- Injection phase retains ECU degrees; the absolute BTDC/ATDC reference remains unconfirmed.

## 0.10 - 2026-10-18

- Extended stock injection-phase controls to the remaining five definitions.
- Preserved each stock strategy, including the earlier two-map phase calculation.
- Passed target-native code, axis/unit and RomRaider edit/save/checksum checks; approved for publication.
- Injection phase uses ECU degrees; the absolute BTDC/ATDC reference remains unconfirmed.

## 0.9 - 2026-10-18

- Added stock injection-phase maps, temperature correction, blending and fixed-value controls to two definitions.
- Retained existing injector characterization and checksum support.
- Passed native-code and RomRaider edit/save checks, followed by user manual review.
- Injection phase uses ECU degrees; the absolute BTDC/ATDC reference remains unconfirmed.

## 0.8 - 2026-10-18

- Added the L39SED000 first-draft definition and checksum module.
- Updated the portable launcher and build command for all seven definitions.
- Passed native-code and RomRaider validation, followed by user manual review.

Vehicle and flashing validation have not been performed.

## 0.7 - 2026-10-18

- Added the L35HEH000 and L33XEH000 first-draft definitions and checksum modules.
- Updated the portable launcher and build command for all six definitions.
- Passed native-code and RomRaider validation, followed by user manual review.

Vehicle and flashing validation have not been performed.

## 0.6 - 2026-10-18

- Added the L39SEF000 first-draft definition and checksum module.
- Updated the portable launcher and build command for all four definitions.
- Passed native-code and RomRaider validation, followed by user manual review.

Vehicle and flashing validation have not been performed.

## 0.5 - 2026-10-18

- Added the L39SEE000 first-draft definition and checksum module.
- Included the verified minimum-lambda RPM curve and base deceleration-RPM offset.
- Renamed Injector Pulsewidth Floor to Injector Pulsewidth Minimum in all definitions.
- Updated the portable launcher and build command for all three definitions.
- Passed software validation; the initial L39 draft also passed manual RomRaider spot checks.

Vehicle and flashing validation have not been performed.

## 0.4 - 2026-10-18

- Added the L3M7EF000 first-draft definition and checksum module.
- Updated the portable launcher and build command to support both definitions.
- Passed software validation and manual RomRaider map spot checks.

Vehicle and flashing validation have not been performed.

## 0.3 - 2026-10-18

Initial repository publication of the L38KEH000 first draft.

- 243 tuning tables and 592 DTC control tables.
- Injector base pulsewidth and related injector characterization controls.
- Calibration identity and reference SHA-256 for matching a user-supplied ROM.
- RomRaider checksum module with source and build instructions.
- Portable Windows launcher and installation instructions.

The draft has passed software validation against the reference image. Vehicle
and flashing validation have not been performed.
