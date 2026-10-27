# SPDX-License-Identifier: MIT
# Copyright (c) 2026 mrphyslaw
"""Fail the release check unless every definition repairs checksums on editor save."""
from pathlib import Path
import argparse
import hashlib
import json
import os
import re
import shutil
import subprocess
import sys
import xml.etree.ElementTree as ET
import zipfile

REPO = Path(__file__).resolve().parents[2]
# Expected native coverage, exclusive ends, and permitted four-byte repair word.
# Keep these independent of the implementation being checked. New definitions
# must supply a verified profile here; they cannot silently skip the save test.
PROFILES = {
    "L33XEH000": (0x48000, [(0x48000, 0x100000)]),
    "L35HEH000": (0x48000, [(0x48000, 0x100000)]),
    "L38KEH000": (0x48000, [(0x48000, 0x100000)]),
    "L39SED000": (0x48000, [(0x48000, 0x100000)]),
    "L39SEE000": (0x48000, [(0x48000, 0x100000)]),
    "L39SEF000": (0x48000, [(0x48000, 0x100000)]),
    "L3CGEE000": (0x3FFC0, [(0x4000, 0x1C000), (0x20000, 0xC0000)]),
    "L3M7EF000": (0xFFFF4, [(0x3F000, 0x100000)]),
    "L3P4EM000": (0x48000, [(0x48000, 0x100000)]),
}


def need(condition, message):
    if not condition:
        raise RuntimeError(message)


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def run(command, log, *, expected_failure=False):
    p = subprocess.run(list(map(str, command)), capture_output=True, text=True, timeout=600)
    log.write_text(p.stdout + p.stderr, encoding="utf-8")
    if expected_failure:
        need(p.returncode != 0 and "AUTOMATIC_CHECKSUM_SAVE_FAILED" in log.read_text(encoding="utf-8"),
             "Validation-only negative control was not detected by the save check: " + str(log))
    else:
        need(p.returncode == 0, "Checksum release check failed: " + str(log) + "\n" + (p.stdout+p.stderr)[-2500:])
        if p.stdout.strip():
            print(p.stdout.strip().splitlines()[-1], flush=True)


def validator_only(source):
    """Change only update() in a private copy, leaving identity/validation intact."""
    match = re.search(r"public\s+int\s+update\s*\(\s*byte\[\]\s+data\s*\)\s*\{", source)
    need(match is not None, "Cannot construct validation-only negative control")
    start = match.end()
    end, depth = start, 1
    while depth:
        need(end < len(source), "Unclosed update method in negative control")
        if source[end] == "{": depth += 1
        elif source[end] == "}": depth -= 1
        end += 1
    return source[:start] + "\n        return 0; // Deliberately broken private test fixture.\n    }" + source[end:]


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--romraider", type=Path, required=True)
    parser.add_argument("--roms", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--javac", default=shutil.which("javac"))
    parser.add_argument("--java", type=Path)
    args = parser.parse_args()
    rr, roms, out = (x.resolve() for x in (args.romraider, args.roms, args.output))
    need(not out.is_relative_to(REPO), "Reports and test images must stay outside the public repository")
    out.mkdir(parents=True, exist_ok=True)
    report_path = out/"Checksum_Save_Audit.json"
    # Invalidate an older success before any preflight or test can fail.
    report_path.write_text(json.dumps(dict(passed=False, status="incomplete"))+"\n", encoding="utf-8")
    need(args.javac, "A JDK javac is required; use --javac")
    need((rr/"RomRaider.jar").is_file(), "RomRaider.jar is missing")
    java = args.java
    if not java:
        bundled = sorted(rr.glob("*/bin/java.exe")) + sorted(rr.glob("*/bin/java"))
        need(len(bundled) == 1, "Select the intended RomRaider Java runtime with --java")
        java = bundled[0]
    need(java.is_file(), "Java runtime is missing")
    definitions = sorted(REPO.glob("*.xml"))
    need({p.stem for p in definitions} == set(PROFILES), "Every definition needs an independent checksum save-test profile")
    private_roms = [roms/(p.stem+".bin") for p in definitions]
    need(all(p.is_file() for p in private_roms), "A matching private .bin reference is required for every definition")
    jars = sorted((REPO/"checksum").glob("*.jar"))
    protected = definitions + private_roms + jars + [rr/"RomRaider.jar", REPO/"Open_RomRaider_Mazda.cmd",
        REPO/"checksum/tests/check_save.py", REPO/"checksum/tests/VerifyChecksumOnSave.java"]
    before = {p: sha(p) for p in protected}
    launcher = (REPO/"Open_RomRaider_Mazda.cmd").read_text(encoding="utf-8")
    for jar in jars:
        need("%RR_HOME%checksum\\"+jar.name in launcher, "Launcher omits checksum module " + jar.name)
    for line in (REPO/"SHA256SUMS.txt").read_text(encoding="utf-8").splitlines():
        digest, rel = line.split("  ", 1)
        need(sha(REPO/rel) == digest, "Published manifest mismatch: " + rel)
    classes = out/"classes"; classes.mkdir(exist_ok=True)
    classpath = os.pathsep.join(map(str, [classes, *jars, rr/"RomRaider.jar", rr/"lib/common/*",
                                        rr/"lib/windows/*", rr/"lib/*", rr/"i18n"]))
    run([args.javac, "--release", "11", "-cp", classpath, "-d", classes,
         REPO/"checksum/tests/VerifyChecksumOnSave.java"], out/"compile.log")
    results = []
    for xml, rom in zip(definitions, private_roms):
        correction, ranges = PROFILES[xml.stem]
        area = out/xml.stem; area.mkdir(exist_ok=True)
        common = [java, "-Xmx1024M", "--add-opens=java.desktop/java.awt=ALL-UNNAMED",
                  "-Djava.awt.headless=false", "-Duser.home="+str(area/"isolated-home")]
        arguments = [xml, rom, area, hex(correction),
                     ",".join(hex(a)+":"+hex(b) for a, b in ranges)]
        run([*common, "-cp", classpath, "VerifyChecksumOnSave", *arguments], area/"save.log")
        result = json.loads((area/"result.json").read_text(encoding="utf-8"))
        need(result["passed"] and result["correctedCases"] > 0 and result["diskSaveReopenCases"] > 0,
             "Missing automatic repair or disk save coverage: " + xml.stem)
        checksum = ET.parse(xml).find("./rom/checksum")
        need(checksum is not None, "Missing definition checksum configuration")
        class_name = "Checksum"+checksum.get("type")
        class_file = "com/romraider/maps/checksum/"+class_name+".class"
        matching_jars = []
        for jar in jars:
            with zipfile.ZipFile(jar) as z:
                if class_file in z.namelist(): matching_jars.append(jar)
        need(len(matching_jars) == 1, "Missing or duplicate packaged checksum class")
        # Prove the check rejects a handler that validates but never repairs.
        negative = area/"validation-only"; negative.mkdir(exist_ok=True)
        source = negative/(class_name+".java")
        source.write_text(validator_only((REPO/"checksum/source"/source.name).read_text(encoding="utf-8")), encoding="utf-8")
        run([args.javac, "--release", "11", "-cp", classpath, "-d", negative, source], negative/"compile.log")
        run([*common, "-cp", str(negative)+os.pathsep+classpath, "VerifyChecksumOnSave",
             *arguments, "negative-control"], negative/"save.log", expected_failure=True)
        result.update(definition=xml.name, definitionSHA256=before[xml], sourceROMSHA256=before[rom],
                      checksumJARSHA256=before[matching_jars[0]], validationOnlyModuleRejected=True)
        results.append(result)
    need(all(sha(p) == h for p, h in before.items()), "A protected input file changed during the audit")
    report = dict(passed=True, definitionsChecked=len(results),
                  controlsChecked=sum(r["tables"] for r in results),
                  saveCases=sum(r["saveCases"] for r in results),
                  automaticCorrectionCases=sum(r["correctedCases"] for r in results),
                  diskSaveReopenCases=sum(r["diskSaveReopenCases"] for r in results),
                  validationOnlyModulesRejected=len(results),
                  sourceImagesAndPublishedFilesUnchanged=True, hardwareFlashed=False, results=results)
    report.update(testRunnerSHA256=sha(REPO/"checksum/tests/check_save.py"),
                  testJavaSHA256=sha(REPO/"checksum/tests/VerifyChecksumOnSave.java"),
                  romraiderJARSHA256=before[rr/"RomRaider.jar"],
                  launcherSHA256=before[REPO/"Open_RomRaider_Mazda.cmd"])
    report_path.write_text(json.dumps(report, indent=2)+"\n", encoding="utf-8")
    print("PASS: all " + str(len(results)) + " definitions automatically correct checksums on save.", flush=True)


if __name__ == "__main__":
    try:
        main()
    except Exception as error:
        print(str(error), file=sys.stderr)
        sys.exit(1)
