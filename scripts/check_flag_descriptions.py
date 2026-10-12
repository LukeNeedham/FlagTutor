#!/usr/bin/env python3
"""Lints the flag descriptions (`symbolism`) in country_data.json against scripts/FLAG_DESCRIPTION_RULES.md.

Errors (exit code 1): a flag without a description, a description for a country without a flag, a year or date, a text that
is too long, or a country name spelled differently from that country's `name`.

Warnings (never fail): a sentence with no word that gives a meaning (it may only say what is on the flag), and wording that
compares the flag with another one. These need a person to look; they are the rules a script cannot check for certain.

Run from the repository root: python3 scripts/check_flag_descriptions.py
"""
import importlib.util
import json
import re
import sys

DATA = "composeApp/src/commonMain/composeResources/files/country_data.json"
MAX_LENGTH = 500

# A sentence should say what something means; these words are how the descriptions say it.
MEANING = re.compile(
    r"stand|symbol|represent|recall|evoke|mean|signif|reflect|honou?r|link|tie|alluding|allude|associated|read|said|believed|echo|"
    r"connection|heritage|origin|emblem|celebrat|commemor|derive|inspir|come from|comes from|because|for |"
    r"national|official|legend|tradition|colours of|taken from|based on|identif",
    re.I,
)
COMPARES = re.compile(r"\b(similar to|resembl\w*|identical|looks? like|copied|modelled on|same as)\b", re.I)
YEAR = re.compile(r"\b(1[0-9]{3}|20[0-9]{2})\b")


def main() -> int:
    spec = importlib.util.spec_from_file_location("names", "scripts/check_country_names.py")
    names = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(names)

    data = json.load(open(DATA))
    errors = list(names.find_problems())
    warnings = []
    for code, entry in data.items():
        text = (entry.get("symbolism") or "").strip()
        label = f"{code} ({entry['name']})"
        if entry["flagImage"] is None:
            if text:
                errors.append(f"{label}: has a description but no flag")
            continue
        if not text:
            errors.append(f"{label}: has a flag but no description")
            continue
        if len(text) > MAX_LENGTH:
            errors.append(f"{label}: {len(text)} characters, the limit is {MAX_LENGTH}")
        if YEAR.search(text):
            errors.append(f"{label}: mentions a year ({YEAR.search(text).group()})")
        for sentence in re.split(r"(?<=[.;])\s+", text):
            if not MEANING.search(sentence):
                warnings.append(f"{label}: no meaning in: {sentence}")
            if COMPARES.search(sentence):
                warnings.append(f"{label}: compares the flag with another: {sentence}")

    for line in warnings:
        print("warning:", line)
    for line in errors:
        print("error:", line)
    print(f"{len(data)} countries, {len(errors)} errors, {len(warnings)} warnings")
    return 1 if errors else 0


if __name__ == "__main__":
    sys.exit(main())
