# Required checksum release check

**Every supported checksum must correct automatically when an edited ROM is saved in RomRaider.** Validation alone does not satisfy this requirement. No separate repair step is required from the user.

Run this check before publishing a new definition or changes to definitions, checksum modules or the launcher:

```text
python checksum/tests/check_save.py --romraider <installation-folder> --roms <private-reference-folder> --output <private-results-folder> --javac <JDK-javac>
```

The private reference folder must contain a matching `<definition-name>.bin` for every XML in the repository. Missing images or missing checksum test profiles fail the check. No images are distributed with this repository. The test requires Python 3, a JDK to compile the test, and Java from the supplied RomRaider installation or `--java`.

The check loads the published XML and packaged checksum modules with RomRaider's actual model. Repairs must occur through `Rom.saveFile()`; the test never calls a checksum update method itself. An independent calculation checks the saved bytes.

Required checks:

- No-edit saves preserve the reference image exactly.
- Every included editable table, switch and bitfield saves with a valid checksum, preserves unrelated bytes, and restores exactly.
- Changes in every covered checksum region trigger correction on save. Changes outside coverage leave the correction word alone.
- Saved files reopen with valid checksums; repeated saves are identical and restoring the edited bytes reproduces the reference image.
- Software identifiers, source images, published definitions, checksum JARs and RomRaider itself remain unchanged.
- A validation-only replacement module must fail the save check. This negative control prevents a validator from being mistaken for working automatic repair.

Reports and temporary images stay in the requested private output folder. These are offline editor checks; they do not flash or validate operation on a physical ECU.
