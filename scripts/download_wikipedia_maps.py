"""
Downloads each country's "globe" locator map (the orthographic-projection image shown
at the top of its Wikipedia infobox, e.g. https://en.wikipedia.org/wiki/France#/media/File:EU-France_(orthographic_projection).svg)
from Wikimedia, and saves it as a PNG per country.

These images are sourced from Wikimedia Commons and are almost always licensed
CC BY-SA or GFDL, which requires attribution - see the in-app credits screen
(ui/feature/credits) for the attribution text shown alongside these maps.

Requires: Python 3 (stdlib only). Run once after checkout: python3 scripts/download_wikipedia_maps.py
Skips countries that have already been downloaded. Re-run to retry failures.
"""
import json, os, re, sys, time, urllib.request, urllib.error

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
LINKS_PATH = os.path.join(SCRIPT_DIR, "../composeApp/src/commonMain/composeResources/files/wikipedia_links.json")
OUT_DIR = os.path.join(SCRIPT_DIR, "../composeApp/src/commonMain/composeResources/files/maps")

USER_AGENT = "FlagTutorMapFetcher/1.0 (https://github.com/lukeneedham/flagtutor; contact via GitHub)"
THUMB_WIDTH = 600
REQUEST_DELAY_SECONDS = 2.5
MAX_RETRIES = 6

NON_MAP_NAME_PATTERNS = re.compile(
    r"flag_of|coat_of_arms|national_emblem|state_emblem|seal_of|emblem_of|banner_of|logo_of",
    re.IGNORECASE,
)
MAP_KEYWORD_SCORES = [
    (re.compile(r"orthographic", re.IGNORECASE), 2),
    (re.compile(r"globe", re.IGNORECASE), 1),
]

FORCE = os.environ.get("FORCE_REDOWNLOAD") == "1"

os.makedirs(OUT_DIR, exist_ok=True)

with open(LINKS_PATH) as f:
    links = json.load(f)


def fetch_json(url):
    req = urllib.request.Request(url, headers={"User-Agent": USER_AGENT, "Accept": "application/json"})
    delay = REQUEST_DELAY_SECONDS
    for attempt in range(MAX_RETRIES):
        try:
            with urllib.request.urlopen(req, timeout=30) as resp:
                body = resp.read()
            return json.loads(body)
        except (urllib.error.HTTPError, urllib.error.URLError, json.JSONDecodeError):
            time.sleep(delay)
            delay *= 2
    return None


def fetch_bytes(url):
    if url.startswith("//"):
        url = "https:" + url
    req = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
    delay = REQUEST_DELAY_SECONDS
    for attempt in range(MAX_RETRIES):
        try:
            with urllib.request.urlopen(req, timeout=30) as resp:
                return resp.read()
        except (urllib.error.HTTPError, urllib.error.URLError):
            time.sleep(delay)
            delay *= 2
    return None


def pick_map_item(media_list_items):
    """Pick the globe/orthographic map image among a page's lead-section images."""
    candidates = [
        item
        for item in media_list_items
        if item.get("section_id") == 0
        and item.get("type") == "image"
        and item.get("srcset")
        and not NON_MAP_NAME_PATTERNS.search(item.get("title", ""))
    ]
    if not candidates:
        return None, False

    best_item, best_score = None, -1
    for item in candidates:
        score = 0
        for pattern, points in MAP_KEYWORD_SCORES:
            if pattern.search(item["title"]):
                score = max(score, points)
        if score > best_score:
            best_item, best_score = item, score

    return best_item, best_score > 0


def thumb_url_at_width(item, width):
    src = item["srcset"][-1]["src"]
    return re.sub(r"/\d+px-", f"/{width}px-", src)


downloaded = 0
skipped = 0
guessed = []
failed = []

codes = sorted(links.keys())
for code in codes:
    out_path = os.path.join(OUT_DIR, f"{code}.png")
    if os.path.exists(out_path) and not FORCE:
        skipped += 1
        continue

    url = links[code]
    title = url.rstrip("/").rsplit("/wiki/", 1)[-1]
    media_list_url = f"https://en.wikipedia.org/api/rest_v1/page/media-list/{title}"

    data = fetch_json(media_list_url)
    time.sleep(REQUEST_DELAY_SECONDS)
    if data is None:
        print(f"  [{code}] FAILED: could not fetch media list", file=sys.stderr)
        failed.append(code)
        continue

    item, confident = pick_map_item(data.get("items", []))
    if item is None:
        print(f"  [{code}] FAILED: no candidate map image found", file=sys.stderr)
        failed.append(code)
        continue

    image_bytes = fetch_bytes(thumb_url_at_width(item, THUMB_WIDTH))
    time.sleep(REQUEST_DELAY_SECONDS)
    if image_bytes is None:
        print(f"  [{code}] FAILED: could not download {item['title']}", file=sys.stderr)
        failed.append(code)
        continue

    with open(out_path, "wb") as f:
        f.write(image_bytes)
    downloaded += 1
    if not confident:
        guessed.append((code, item["title"]))
    if downloaded % 25 == 0:
        print(f"  {downloaded} maps downloaded…")

print(f"\nDone: {downloaded} downloaded, {skipped} already present, {len(failed)} failed.")
if guessed:
    print(f"\n{len(guessed)} countries had no 'orthographic'/'globe' match in the filename - picked the first lead image, please verify:")
    for code, title in guessed:
        print(f"  {code}: {title}")
if failed:
    print(f"\n{len(failed)} countries need a manually-sourced map image:")
    for code in failed:
        print(f"  {code}: {links[code]}")
