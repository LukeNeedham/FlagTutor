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
import html, json, os, re, sys, time, urllib.parse, urllib.request, urllib.error

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
LINKS_PATH = os.path.join(SCRIPT_DIR, "../composeApp/src/commonMain/composeResources/files/wikipedia_links.json")
OUT_DIR = os.path.join(SCRIPT_DIR, "../composeApp/src/commonMain/composeResources/files/maps")

USER_AGENT = "FlagTutorMapFetcher/1.0 (https://github.com/lukeneedham/flagtutor; contact via GitHub)"
THUMB_WIDTH = 600
REQUEST_DELAY_SECONDS = 2.5
MAX_RETRIES = 3
MAX_RETRY_DELAY_SECONDS = 20
# Wikimedia's rate-limit response (HTTP 429) names how long to wait before retrying via
# a Retry-After header. Respect it (capped, so one file can't stall the whole run) instead
# of a generic backoff that's too short to ever clear the cooldown.
MAX_RETRY_AFTER_SECONDS = 60
# If this many requests in a row fail, Wikimedia is almost certainly rate-limiting/blocking
# this runner rather than each file being individually broken. Stop early instead of burning
# hours retrying every remaining country - re-running later (once the block lifts) is cheaper.
CIRCUIT_BREAKER_THRESHOLD = 10

# Many overseas territories' Wikipedia pages name their coat-of-arms/logo file in French,
# Dutch, German, Spanish or Portuguese rather than English, e.g. "Blason_St_Barthélémy" or
# "Aruba_wapen" - those don't contain any of the English terms below, so they'd otherwise slip
# through and get picked over the page's actual locator map.
NON_MAP_NAME_PATTERNS = re.compile(
    r"flag_of|coat_of_arms|national_emblem|state_emblem|seal_of|emblem_of|banner_of|logo"
    r"|blason|armoiries|wapen|wappen|escudo|bras[aã]o|stemma",
    re.IGNORECASE,
)
MAP_KEYWORD_SCORES = [
    (re.compile(r"orthographic", re.IGNORECASE), 2),
    (re.compile(r"globe", re.IGNORECASE), 1),
]
# A genuine cartographic locator/orthographic map is essentially never distributed on
# Wikipedia as a JPEG - that format is used for photographs. Excluding it up front stops a
# lead-section photo (e.g. a satellite photo of the country) from being picked over the page's
# actual locator map elsewhere in the lead.
NON_MAP_EXTENSIONS = re.compile(r"\.jpe?g$", re.IGNORECASE)

FORCE = os.environ.get("FORCE_REDOWNLOAD") == "1"

os.makedirs(OUT_DIR, exist_ok=True)

with open(LINKS_PATH) as f:
    links = json.load(f)


def retry_delay_for(error, default_delay):
    """How long to wait before retrying after `error`. Honours a 429's Retry-After header,
    since that's Wikimedia telling us exactly when its rate limit will clear - a shorter,
    unrelated backoff just retries before the cooldown ends and fails every time."""
    if isinstance(error, urllib.error.HTTPError) and error.code == 429:
        retry_after = error.headers.get("Retry-After")
        if retry_after is not None:
            try:
                return min(float(retry_after), MAX_RETRY_AFTER_SECONDS)
            except ValueError:
                pass
    return default_delay


def describe_error(error):
    """Human-readable reason for a failed request, so failure logs say *why* rather than
    just *that* a download failed - critical for telling a transient blip apart from a
    persistent block (e.g. a 403 from every request means the runner's IP is blocked,
    not that any individual file is broken)."""
    if isinstance(error, urllib.error.HTTPError):
        return f"HTTP {error.code} {error.reason}"
    if isinstance(error, urllib.error.URLError):
        return f"URLError: {error.reason}"
    return f"{type(error).__name__}: {error}"


def fetch_json(url):
    req = urllib.request.Request(url, headers={"User-Agent": USER_AGENT, "Accept": "application/json"})
    delay = REQUEST_DELAY_SECONDS
    last_error = None
    for attempt in range(MAX_RETRIES):
        try:
            with urllib.request.urlopen(req, timeout=30) as resp:
                body = resp.read()
            return json.loads(body), None
        except (urllib.error.HTTPError, urllib.error.URLError, json.JSONDecodeError) as e:
            last_error = describe_error(e)
            time.sleep(retry_delay_for(e, delay))
            delay = min(delay * 2, MAX_RETRY_DELAY_SECONDS)
    return None, last_error


def fetch_bytes(url):
    if url.startswith("//"):
        url = "https:" + url
    req = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
    delay = REQUEST_DELAY_SECONDS
    last_error = None
    for attempt in range(MAX_RETRIES):
        try:
            with urllib.request.urlopen(req, timeout=30) as resp:
                return resp.read(), None
        except (urllib.error.HTTPError, urllib.error.URLError) as e:
            last_error = describe_error(e)
            time.sleep(retry_delay_for(e, delay))
            delay = min(delay * 2, MAX_RETRY_DELAY_SECONDS)
    return None, last_error


def map_keyword_score(item):
    score = 0
    for pattern, points in MAP_KEYWORD_SCORES:
        if pattern.search(item["title"]):
            score = max(score, points)
    return score


def pick_map_candidates(media_list_items):
    """Rank a page's lead-section images best-first (globe/orthographic keyword match, then
    everything else), so that if the top pick's file turns out to be persistently broken/
    blocked on Wikimedia's side, the caller can fall back to the next best lead image instead
    of failing the whole country."""
    candidates = [
        item
        for item in media_list_items
        if item.get("section_id") == 0
        and item.get("type") == "image"
        and item.get("srcset")
        and not NON_MAP_NAME_PATTERNS.search(item.get("title", ""))
        and not NON_MAP_EXTENSIONS.search(item.get("title", ""))
    ]
    return sorted(candidates, key=map_keyword_score, reverse=True)


def thumb_url_near_width(item, target_width):
    """Pick the srcset entry closest to `target_width`, rather than requesting an arbitrary
    size. Wikimedia's thumbnail servers only pre-render a per-file whitelist of widths (the
    ones already offered in `srcset`) and reject any other width with an HTTP 400 ("Use
    thumbnail sizes listed on ...") - rewriting the URL to a fixed width like the old code did
    is exactly what triggered that, on effectively every file."""
    def width_of(entry):
        match = re.search(r"/(\d+)px-", entry["src"])
        return int(match.group(1)) if match else None

    candidates = [(w, entry["src"]) for entry in item["srcset"] if (w := width_of(entry)) is not None]
    if not candidates:
        return item["srcset"][-1]["src"]
    return min(candidates, key=lambda c: abs(c[0] - target_width))[1]


def fetch_media_list_fallback(title):
    """Fallback for when the REST media-list endpoint returns a hard HTTP 500 for a page - a
    real, persistent Wikimedia-side bug confirmed for a handful of articles (reproduces from
    completely different networks, not just this runner, and doesn't clear on retry).

    Parses the classic action=parse HTML of the lead section (section 0 - the same scope the
    REST endpoint uses) and turns each <img> there into an item shaped like a media-list entry,
    so pick_map_item/thumb_url_near_width work on it unchanged."""
    api_url = (
        "https://en.wikipedia.org/w/api.php?action=parse&format=json&formatversion=2"
        f"&prop=text&section=0&page={title}"
    )
    data, error = fetch_json(api_url)
    if data is None:
        return None, error

    page_html = data.get("parse", {}).get("text", "")
    items = []
    for tag_match in re.finditer(r"<img[^>]*>", page_html):
        tag = tag_match.group(0)
        src_match = re.search(r'src="([^"]+)"', tag)
        name_match = re.search(r"/thumb/[0-9a-f]/[0-9a-f]{2}/([^/]+)/\d+px-", tag)
        if not src_match or not name_match:
            continue

        srcset = [{"src": html.unescape(src_match.group(1))}]
        srcset_match = re.search(r'srcset="([^"]+)"', tag)
        if srcset_match:
            for entry in html.unescape(srcset_match.group(1)).split(","):
                src = entry.strip().split(" ")[0]
                if src:
                    srcset.append({"src": src})

        items.append({
            "title": f"File:{urllib.parse.unquote(name_match.group(1))}",
            "section_id": 0,
            "type": "image",
            "srcset": srcset,
        })
    return items, None


downloaded = 0
skipped = 0
guessed = []
failed = []
not_attempted = []
consecutive_failures = 0
last_network_error = None

codes = sorted(links.keys())
for i, code in enumerate(codes):
    out_path = os.path.join(OUT_DIR, f"{code}.png")
    if os.path.exists(out_path) and not FORCE:
        skipped += 1
        continue

    if consecutive_failures >= CIRCUIT_BREAKER_THRESHOLD:
        not_attempted.extend(codes[i:])
        print(
            f"\nAborting early: {consecutive_failures} downloads in a row failed "
            f"(most recent error: {last_network_error}) - likely rate-limited/blocked "
            f"rather than a per-file problem. {len(not_attempted)} countries not attempted, "
            f"re-run later to retry them.",
            file=sys.stderr,
        )
        break

    url = links[code]
    title = url.rstrip("/").rsplit("/wiki/", 1)[-1]
    media_list_url = f"https://en.wikipedia.org/api/rest_v1/page/media-list/{title}"

    data, error = fetch_json(media_list_url)
    time.sleep(REQUEST_DELAY_SECONDS)
    if data is None:
        fallback_items, fallback_error = fetch_media_list_fallback(title)
        time.sleep(REQUEST_DELAY_SECONDS)
        if fallback_items is None:
            print(
                f"  [{code}] FAILED: could not fetch media list ({error}); "
                f"fallback also failed ({fallback_error})",
                file=sys.stderr,
            )
            failed.append(code)
            consecutive_failures += 1
            last_network_error = fallback_error
            continue
        data = {"items": fallback_items}

    candidates = pick_map_candidates(data.get("items", []))
    if not candidates:
        print(f"  [{code}] FAILED: no candidate map image found", file=sys.stderr)
        failed.append(code)
        consecutive_failures = 0
        continue

    # Try candidates best-first, falling back to the next lead image if one's file turns
    # out to be persistently broken/blocked on Wikimedia's side (seen in practice: a
    # specific SVG failing the same way across separate runs hours apart) rather than
    # failing the whole country over one bad file.
    image_bytes, error, item = None, None, None
    for item in candidates:
        image_bytes, error = fetch_bytes(thumb_url_near_width(item, THUMB_WIDTH))
        time.sleep(REQUEST_DELAY_SECONDS)
        if image_bytes is not None:
            break

    if image_bytes is None:
        print(f"  [{code}] FAILED: could not download {item['title']} ({error})", file=sys.stderr)
        failed.append(code)
        consecutive_failures += 1
        last_network_error = error
        continue

    with open(out_path, "wb") as f:
        f.write(image_bytes)
    downloaded += 1
    consecutive_failures = 0
    if map_keyword_score(item) == 0:
        guessed.append((code, item["title"]))
    if downloaded % 25 == 0:
        print(f"  {downloaded} maps downloaded…")

print(f"\nDone: {downloaded} downloaded, {skipped} already present, {len(failed)} failed"
      + (f", {len(not_attempted)} not attempted" if not_attempted else "") + ".")
if guessed:
    print(f"\n{len(guessed)} countries had no 'orthographic'/'globe' match in the filename - picked the first lead image, please verify:")
    for code, title in guessed:
        print(f"  {code}: {title}")
if failed:
    print(f"\n{len(failed)} countries need a manually-sourced map image:")
    for code in failed:
        print(f"  {code}: {links[code]}")
