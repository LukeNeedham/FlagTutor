#!/usr/bin/env python3
"""Checks that country names are spelled the same everywhere in country_data.json.

Every country has one name (`name`), and the flag descriptions (`symbolism`) and notes (`flagNote`) must use exactly that name
whenever they mention a country, not a variant such as the formal ISO name, a short form or an older spelling ("Nauru" and not
"Naoero", "United States" and not "US" or "America"). See scripts/FLAG_DESCRIPTION_RULES.md.

Run from the repository root: python3 scripts/check_country_names.py   (exits 1 and lists the problems if any)
"""
import json
import re
import sys
import urllib.parse

DATA = "app/src/commonMain/composeResources/files/country_data.json"
OVERRIDES = "scripts/country_data_overrides.json"

# Spellings that are wrong in prose, mapped to the country they mean. The Wikipedia article title of each country is added too.
VARIANTS = {
    "UK": "gb", "Britain": "gb", "Great Britain": "gb", "US": "us", "USA": "us", "United States of America": "us",
    "Russian Federation": "ru", "Czech Republic": "cz", "Ivory Coast": "ci", "East Timor": "tl", "Burma": "mm",
    "Swaziland": "sz", "Holland": "nl", "Naoero": "nr", "Macau": "mo", "Turkey": "tr", "Cape Verde": "cv", "Cote d'Ivoire": "ci", "Viet Nam": "vn",
    "Korea, Republic of": "kr", "Korea, Democratic People's Republic of": "kp", "Iran, Islamic Republic of": "ir",
    "Syrian Arab Republic": "sy", "Taiwan, Province of China": "tw", "Tanzania, United Republic of": "tz",
    "Lao People's Democratic Republic": "la", "Brunei Darussalam": "bn", "Holy See": "va", "Sao Tome and Principe": "st",
    "Moldova, Republic of": "md", "Palestine, State of": "ps", "Congo, Democratic Republic of the": "cd",
    "Venezuela, Bolivarian Republic of": "ve", "Bolivia, Plurinational State of": "bo", "Pitcairn": "pn",
    "Micronesia, Federated States of": "fm", "Netherlands, Kingdom of the": "nl", "Åland": "ax",
}


def find_problems() -> list[str]:
    """Every text in the data file that spells a country name differently from that country's `name`."""
    data = json.load(open(DATA))
    names = {code: entry["name"] for code, entry in data.items()}
    variants = dict(VARIANTS)
    for code, entry in data.items():
        if entry["wikipediaUrl"]:
            variants.setdefault(urllib.parse.unquote(entry["wikipediaUrl"].rsplit("/wiki/", 1)[1]).replace("_", " "), code)
    for code, name in json.load(open(OVERRIDES)).get("names", {}).items():
        assert names[code] == name, f"{code}: data says {names[code]!r}, overrides say {name!r}"

    problems = []
    for code, entry in data.items():
        for field in ("symbolism", "flagNote"):
            text = entry.get(field) or ""
            for variant, other in variants.items():
                if variant == names[other] or variant in names[other]:
                    continue  # the correct name, or part of it (e.g. "Pitcairn" inside "Pitcairn Islands")
                if re.search(r"(?<![\w])" + re.escape(variant) + r"(?![\w])", text):
                    problems.append(f"{code} {field}: uses {variant!r}, the name is {names[other]!r}")
    return problems


def main() -> int:
    problems = find_problems()
    print("\n".join(problems) if problems else "Country names are consistent.")
    return 1 if problems else 0


if __name__ == "__main__":
    sys.exit(main())
