# Definition requirements

- Every supported definition must automatically correct its implemented ECU checksums when an edited ROM is saved through the normal RomRaider save path. A validation-only checksum handler is not sufficient. Users must not need a separate checksum command or repair step.
- Treat this as a mandatory release check for new definitions and changes to definitions, checksum modules or launchers. Run `checksum/tests/check_save.py` against every supported definition and its matching private reference ROM before publishing.
- Verify no-edit saves, edits inside each checksum region, ordinary table and switch edits, disk save/reopen, repeated saves, and exact restoration. Check the saved bytes with an independent checksum calculation and allow checksum repair to change only the documented correction word. Edits outside checksum coverage must not cause unnecessary checksum changes.
- Keep software-ID, image-size, layout and integrity-marker guards. Reject unsupported images instead of disabling ECU integrity checks.
- Never publish ROM images, private research, commercial ECU-product references or machine-specific information. Use the existing MIT license and author identity.
