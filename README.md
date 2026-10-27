# MazdaSpeedDefs

Definition files for Mazda engine control computers, for use with the RomRaider ECU Editor.

These definition files are experimental and provided without warranty.

Definition filenames identify the ECU software they support. ROM files are not included.

## How to use

1. Install [RomRaider](https://github.com/RomRaider/RomRaider/releases).
2. Download or clone this repository. Copy its files into the RomRaider installation folder, beside `RomRaider.jar`. Keep the `checksum` folder intact.
3. Double-click `Open_RomRaider_Mazda.cmd`.
4. Add the definition through RomRaider's ECU Definition Manager.
5. Open your ROM. Save your changes under a new filename.

The launcher uses Java from the RomRaider folder, `JAVA_HOME`, or `PATH`. It prints a message if RomRaider, Java, or a checksum module is missing.

## Checksum support

Use the supplied launcher so RomRaider can load the included checksum modules. They validate supported ROMs when opened and apply the supported checksum behavior when saved. See the [support notes](support) for details.

Java source is included under `checksum/source`. To rebuild the modules, run `checksum/Build_Checksum.cmd` with a JDK on `PATH`, after placing the repository files in the RomRaider folder.

## License

Released under the [MIT License](LICENSE).
