#!/usr/bin/env python3
"""Write a Markdown test summary to GitHub Actions job summary."""

from __future__ import annotations

import os
import sys
import xml.etree.ElementTree as ET
from dataclasses import dataclass, field
from pathlib import Path


@dataclass
class Case:
    suite: str
    classname: str
    name: str
    status: str
    detail: str = ""


@dataclass
class Totals:
    tests: int = 0
    passed: int = 0
    failed: int = 0
    skipped: int = 0
    errors: int = 0
    cases: list[Case] = field(default_factory=list)


def parse_suite(path: Path, totals: Totals) -> None:
    root = ET.parse(path).getroot()
    suite_name = root.attrib.get("name", path.stem)

    for testcase in root.findall("testcase"):
        classname = testcase.attrib.get("classname", "")
        name = testcase.attrib.get("name", "")
        failure = testcase.find("failure")
        error = testcase.find("error")
        skipped = testcase.find("skipped")

        totals.tests += 1
        if failure is not None:
          totals.failed += 1
          status = "failed"
          detail = (failure.attrib.get("message") or failure.text or "").strip()
        elif error is not None:
          totals.errors += 1
          status = "error"
          detail = (error.attrib.get("message") or error.text or "").strip()
        elif skipped is not None:
          totals.skipped += 1
          status = "skipped"
          detail = (skipped.attrib.get("message") or skipped.text or "").strip()
        else:
          totals.passed += 1
          status = "passed"
          detail = ""

        totals.cases.append(Case(suite_name, classname, name, status, detail))


def main() -> int:
    root = Path(sys.argv[1] if len(sys.argv) > 1 else ".").resolve()
    xml_files = sorted(root.glob("**/build/test-results/test/TEST-*.xml"))
    totals = Totals()

    for xml_file in xml_files:
        parse_suite(xml_file, totals)

    out = os.environ.get("GITHUB_STEP_SUMMARY")
    stream = open(out, "a", encoding="utf-8") if out else sys.stdout

    with stream:
        stream.write("## Test results\n\n")
        if not xml_files:
            stream.write("_No JUnit XML reports found under `**/build/test-results/test/`._\n")
            return 1

        stream.write("| Metric | Count |\n")
        stream.write("|--------|------:|\n")
        stream.write(f"| Total | {totals.tests} |\n")
        stream.write(f"| Passed | {totals.passed} |\n")
        stream.write(f"| Failed | {totals.failed} |\n")
        stream.write(f"| Errors | {totals.errors} |\n")
        stream.write(f"| Skipped | {totals.skipped} |\n\n")

        not_ok = [c for c in totals.cases if c.status != "passed"]
        if not_ok:
            stream.write("### Failed, errored, or skipped tests\n\n")
            for case in not_ok:
                label = f"{case.classname}.{case.name}" if case.classname else case.name
                stream.write(f"- **{case.status.upper()}** `{label}` ({case.suite})")
                if case.detail:
                    one_line = " ".join(case.detail.split())
                    if len(one_line) > 240:
                        one_line = one_line[:237] + "..."
                    stream.write(f": {one_line}")
                stream.write("\n")
        else:
            stream.write("All tests passed.\n")

    return 0 if totals.failed == 0 and totals.errors == 0 else 1


if __name__ == "__main__":
    raise SystemExit(main())
