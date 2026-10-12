#!/usr/bin/env python3
"""Shows what a flag's Wikipedia article says it means, next to the current description, so a description can be fact-checked.

    python3 scripts/flag_sources.py fr de          # these countries
    python3 scripts/flag_sources.py --all          # every country with a flag

For each country it fetches the wikitext of the flag article (`flagWikipediaUrl` in country_data.json) and prints the sentences that
say what something means. It reads the wikitext, not the plain-text extract, because the extract silently drops lists, tables and
quotations, which is where many articles put the colours' meanings (Bolivia, Ethiopia, Kuwait, Lesotho, Kenya, Cambodia, ...).
Articles are cached in build/flag_sources/. Wikimedia rate limits, so a full run takes a while; it waits as the server asks.

The article is the only source the rules allow (scripts/FLAG_DESCRIPTION_RULES.md), so a claim in the description that none of the
printed passages supports has to be removed or re-sourced. Where an article gives no meaning for an element, leave the element out.
"""
import json
import os
import re
import sys
import time
import urllib.error
import urllib.parse
import urllib.request

DATA = "composeApp/src/commonMain/composeResources/files/country_data.json"
CACHE = "build/flag_sources"
USER_AGENT = "FlagTutorBot/1.0 (https://github.com/LukeNeedham/FlagTutor; flag description fact check)"
MEANING = re.compile(
    r"symboli[sz]|represent|stand[s]? for|signif|meaning|reflect|honou?r|tribute|interpret|said to|believed|recall|evoke|allude|"
    r"denote|epitomi[sz]e|emblem of|motto",
    re.I,
)


def fetch_wikitext(title: str) -> str:
    cached = os.path.join(CACHE, re.sub(r"[^\w.-]", "_", title) + ".wiki")
    if os.path.exists(cached):
        return open(cached, encoding="utf-8").read()
    url = "https://en.wikipedia.org/w/api.php?" + urllib.parse.urlencode(
        {"action": "query", "prop": "revisions", "rvprop": "content", "rvslots": "main", "titles": title, "format": "json", "redirects": 1}
    )
    for _ in range(30):
        try:
            page = next(iter(json.load(urllib.request.urlopen(urllib.request.Request(url, headers={"User-Agent": USER_AGENT}), timeout=30))["query"]["pages"].values()))
            text = page["revisions"][0]["slots"]["main"]["*"]
            os.makedirs(CACHE, exist_ok=True)
            open(cached, "w", encoding="utf-8").write(text)
            return text
        except urllib.error.HTTPError as e:
            time.sleep(int(e.headers.get("retry-after", "10")) + 2)
        except Exception:
            time.sleep(10)
    raise RuntimeError(f"could not fetch {title}")


def plain(wikitext: str) -> str:
    """Wikitext to readable text, keeping the content of {{legend}}, quotation and table templates."""
    t = re.sub(r"<ref[^>]*?/>|<ref[^>]*>.*?</ref>", "", wikitext, flags=re.S)
    t = re.sub(r"<!--.*?-->", "", t, flags=re.S)
    t = re.sub(r"\{\{legend\|[^|}]*\|(.*?)\}\}", r"\1", t, flags=re.S)
    t = re.sub(r"\[\[(?:File|Image):[^\]]*\]\]", "", t)
    t = re.sub(r"\[\[(?:[^\]|]*\|)?([^\]]*)\]\]", r"\1", t)
    t = re.sub(r"'''?", "", t)
    t = re.sub(r"\{\{[^{}]*\}\}", "", t)
    return t


def passages(wikitext: str) -> list[str]:
    out = []
    for block in re.split(r"\n+", plain(wikitext)):
        block = block.strip(" *|!:-")
        if len(block) < 30 or block.startswith(("{|", "|}", "==", "[[Category")):
            continue
        for sentence in re.split(r"(?<=[.;])\s+", block):
            if MEANING.search(sentence) and sentence not in out:
                out.append(sentence.strip())
    return out


def main(argv: list[str]) -> int:
    data = json.load(open(DATA))
    codes = [c for c, e in data.items() if e["flagImage"]] if "--all" in argv else [a.lower() for a in argv]
    if not codes:
        print(__doc__)
        return 1
    for code in codes:
        entry = data[code]
        title = urllib.parse.unquote(entry["flagWikipediaUrl"].rsplit("/wiki/", 1)[1])
        print(f"##### [{code}] {entry['name']}  ({entry['flagWikipediaUrl']})")
        print("DESCRIPTION:", entry["symbolism"])
        for passage in passages(fetch_wikitext(title))[:25]:
            print("  -", passage[:500])
        print()
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
