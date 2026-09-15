"""
Downloads flag SVGs from hampusborgos/country-flags on GitHub and converts
them to 320px-wide PNGs. Requires: pip install cairosvg
Run once after checkout: python3 scripts/download_flags.py
Skips flags that have already been downloaded.
"""
import json, os, sys, urllib.request
import cairosvg

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
COUNTRIES_PATH = os.path.join(SCRIPT_DIR, "../composeApp/src/commonMain/composeResources/files/countries.json")
OUT_DIR = os.path.join(SCRIPT_DIR, "../composeApp/src/commonMain/composeResources/files/flags")
BASE_URL = "https://raw.githubusercontent.com/hampusborgos/country-flags/main/svg"

os.makedirs(OUT_DIR, exist_ok=True)

with open(COUNTRIES_PATH) as f:
    codes = list(json.load(f).keys())

downloaded = 0
skipped = 0
errors = 0

for code in codes:
    out_path = os.path.join(OUT_DIR, f"{code}.png")
    if os.path.exists(out_path):
        skipped += 1
        continue
    url = f"{BASE_URL}/{code}.svg"
    try:
        svg_data = urllib.request.urlopen(url).read()
        cairosvg.svg2png(bytestring=svg_data, write_to=out_path, output_width=320)
        downloaded += 1
        if downloaded % 25 == 0:
            print(f"  {downloaded} flags downloaded…")
    except Exception as e:
        print(f"  Error for {code}: {e}", file=sys.stderr)
        errors += 1

print(f"Done: {downloaded} downloaded, {skipped} already present, {errors} errors.")
