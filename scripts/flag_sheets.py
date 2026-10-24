#!/usr/bin/env python3
"""Makes contact sheets of the flag images, four to a sheet and labelled, so each flag can be looked at before its description is
written or checked (scripts/FLAG_DESCRIPTION_RULES.md). Needs Pillow.

    python3 scripts/flag_sheets.py build/flag_sheets            # every flag
    python3 scripts/flag_sheets.py build/flag_sheets fr de it   # only these

Sheet 000.png has the first four flags in alphabetical order of code, and so on.
"""
import json
import os
import sys

from PIL import Image, ImageDraw

DATA = "app/src/commonMain/composeResources/files/country_data.json"
FLAGS = "app/src/commonMain/composeResources/files/"
PER_SHEET = 4


def main(argv: list[str]) -> int:
    if not argv:
        print(__doc__)
        return 1
    out_dir, only = argv[0], [a.lower() for a in argv[1:]]
    data = json.load(open(DATA))
    codes = [c for c, e in data.items() if e["flagImage"] and (not only or c in only)]
    os.makedirs(out_dir, exist_ok=True)
    for start in range(0, len(codes), PER_SHEET):
        sheet = Image.new("RGB", (700, 580), (200, 200, 200))
        draw = ImageDraw.Draw(sheet)
        for i, code in enumerate(codes[start:start + PER_SHEET]):
            flag = Image.open(FLAGS + data[code]["flagImage"]).convert("RGBA")
            flag.thumbnail((330, 250))
            backdrop = Image.new("RGBA", flag.size, (255, 0, 255, 255))  # magenta, so transparent parts show
            backdrop.alpha_composite(flag)
            x, y = (i % 2) * 350 + 10, (i // 2) * 290 + 30
            sheet.paste(backdrop.convert("RGB"), (x, y))
            draw.text((x, y - 20), f"{code} {data[code]['name']}", fill=(0, 0, 0))
        sheet.save(os.path.join(out_dir, f"{start // PER_SHEET:03d}.png"))
    print(f"Wrote {-(-len(codes) // PER_SHEET)} sheets for {len(codes)} flags to {out_dir}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
