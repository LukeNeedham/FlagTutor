"""
Generates a map PNG for every country in country_boundaries.json.
Mirrors the viewport/rendering logic from CountryMapHighlight.kt.
"""
import json, math, os, sys
from PIL import Image, ImageDraw

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
BOUNDARIES_PATH = os.path.join(SCRIPT_DIR, "../composeApp/src/commonMain/composeResources/files/country_boundaries.json")
OUT_DIR = os.path.join(SCRIPT_DIR, "../composeApp/src/commonMain/composeResources/files/maps")

W, H = 800, 500
PADDING = 0.05
PAD_X = int(W * PADDING)
PAD_Y = int(H * PADDING)
DW = W - 2 * PAD_X
DH = H - 2 * PAD_Y
ASPECT = W / H  # ~1.6

MIN_FRAC = 1.0 / 3.0
MAX_FRAC = 5.0 / 6.0
LARGE_THRESHOLD = 20.0
LARGE_CAP = 120.0

HIGHLIGHT = (121, 101, 175, 255)   # purple
LAND = (192, 188, 197, 255)        # muted gray
BORDER = (112, 108, 117, 96)       # semi-transparent gray

os.makedirs(OUT_DIR, exist_ok=True)

print("Parsing country_boundaries.json …")
with open(BOUNDARIES_PATH) as f:
    raw = f.read()

# Custom parser matching CountryBoundaries.kt
boundaries = {}
i = raw.index('{') + 1
n = len(raw)
while i < n:
    ks = raw.find('"', i)
    if ks < 0: break
    ke = raw.find('"', ks + 1)
    code = raw[ks+1:ke]
    i = raw.index('[', ke) + 1
    polygons = []
    while i < n and raw[i] != ']':
        if raw[i] == '[':
            i += 1
            ring = []
            while i < n and raw[i] != ']':
                if raw[i] == '[':
                    i += 1
                    lon_end = raw.index(',', i)
                    lon = float(raw[i:lon_end])
                    i = lon_end + 1
                    j = i
                    while j < n and raw[j] not in (']', ','): j += 1
                    lat = float(raw[i:j])
                    i = raw.index(']', j) + 1
                    ring.append((lon, lat))
                else:
                    i += 1
            if i < n: i += 1
            polygons.append(ring)
        else:
            i += 1
    if i < n: i += 1
    boundaries[code] = polygons

print(f"Parsed {len(boundaries)} countries.")

def compute_viewport(code):
    polys = boundaries.get(code)
    if not polys:
        return (-180, 180, -60, 85)
    min_lon = min_lat = float('inf')
    max_lon = max_lat = float('-inf')
    for ring in polys:
        for lon, lat in ring:
            if lon < min_lon: min_lon = lon
            if lon > max_lon: max_lon = lon
            if lat < min_lat: min_lat = lat
            if lat > max_lat: max_lat = lat
    span_lon = max_lon - min_lon
    if span_lon > 180:
        wmin = wmax = None
        for ring in polys:
            for lon, lat in ring:
                wlon = lon + 360 if lon < 0 else lon
                if wmin is None or wlon < wmin: wmin = wlon
                if wmax is None or wlon > wmax: wmax = wlon
        span_lon = wmax - wmin
        c_lon = (wmin + wmax) / 2
        if c_lon > 180: c_lon -= 360
    else:
        c_lon = (min_lon + max_lon) / 2
    span_lon = max(span_lon, 2.0)
    span_lat = max(max_lat - min_lat, 2.0)
    c_lat = (min_lat + max_lat) / 2
    max_span = max(span_lon, span_lat)
    t = max(0, min(1, (max_span - LARGE_THRESHOLD) / (LARGE_CAP - LARGE_THRESHOLD)))
    fraction = MIN_FRAC + t * (MAX_FRAC - MIN_FRAC)
    need_lon = span_lon / fraction
    need_lat = span_lat / fraction
    if need_lon / need_lat > ASPECT:
        final_lon = need_lon
        final_lat = need_lon / ASPECT
    else:
        final_lat = need_lat
        final_lon = need_lat * ASPECT
    return (c_lon - final_lon/2, c_lon + final_lon/2, c_lat - final_lat/2, c_lat + final_lat/2)

def ring_to_pixels(ring, vp):
    min_lon, max_lon, min_lat, max_lat = vp
    range_x = max_lon - min_lon
    range_y = max_lat - min_lat
    vp_center = (min_lon + max_lon) / 2
    ref_lon = ring[0][0] if ring else 0
    if ref_lon < vp_center - 180:
        shift = 360
    elif ref_lon > vp_center + 180:
        shift = -360
    else:
        shift = 0
    pts = []
    for lon, lat in ring:
        x = PAD_X + ((lon + shift - min_lon) / range_x) * DW
        y = PAD_Y + ((max_lat - lat) / range_y) * DH
        pts.append((x, y))
    return pts

generated = 0
skipped = 0
errors = 0
codes = sorted(boundaries.keys())

for target_code in codes:
    out_path = os.path.join(OUT_DIR, f"{target_code}.png")
    if os.path.exists(out_path):
        skipped += 1
        continue
    try:
        vp = compute_viewport(target_code)
        img = Image.new("RGBA", (W, H), (0, 0, 0, 0))
        draw = ImageDraw.Draw(img, "RGBA")

        # Draw all non-target countries (skip Antarctica)
        for code, polys in boundaries.items():
            if code == "aq" or code == target_code:
                continue
            for ring in polys:
                pts = ring_to_pixels(ring, vp)
                if len(pts) >= 3:
                    draw.polygon(pts, fill=LAND, outline=BORDER)

        # Draw target country on top
        for ring in boundaries.get(target_code, []):
            pts = ring_to_pixels(ring, vp)
            if len(pts) >= 3:
                draw.polygon(pts, fill=HIGHLIGHT, outline=BORDER)

        img.save(out_path, "PNG")
        generated += 1
        if generated % 25 == 0:
            print(f"  {generated} maps generated…")
    except Exception as e:
        print(f"  Error for {target_code}: {e}", file=sys.stderr)
        errors += 1

print(f"Done: {generated} generated, {skipped} skipped, {errors} errors.")
