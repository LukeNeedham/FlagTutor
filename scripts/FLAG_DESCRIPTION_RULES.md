# Flag description rules

These rules **must be followed for every flag description**, i.e. the `symbolism` field of each country in
`composeApp/src/commonMain/composeResources/files/country_data.json`. Check every new or changed text against them.

## Process

- Write each description individually, and look at the flag image actually used in the app
  (`composeApp/src/commonMain/composeResources/files/flags/<code>.png`) before writing. Some countries have more than one flag
  (state / civil flag, old / new design), so describe the one in the image, not the one you remember.
- The facts (what a colour, symbol or emblem stands for) come from the country's flag article on Wikipedia (`flagWikipediaUrl`).
  Do not state anything you cannot source there, and do not describe an element that is not in the image.
- When the flag image changes, rewrite its text.

## Style

1. **Keep it short.** One to three sentences.
2. **No dates**, such as when the flag was introduced or adopted.
3. **Do not refer to other flags or countries**, unless the link is deliberate. Saying that Australia's flag carries the Union Jack
   because of its ties to the UK is fine. Saying that two flags look alike is not, and neither is saying a flag was copied from another.
4. **Do not mention historical flags.** Describe the current flag shown in the image.
5. **No literal descriptions.** Focus on symbolism and meaning, and work any literal description into it. Write
   "The red stripe stands for ...", not "Two horizontal stripes of red and blue. Red stands for ...".
6. **Every sentence must carry a meaning.** A sentence that only says what is on the flag, for example "A white crescent and
   star sit at the centre of the red stripe", or "The coat of arms shows a tree and a ship", is a literal description even when
   it is short, so it does not belong. For every colour, shape and emblem you mention, say what it stands for. If the flag
   article gives no meaning for an element, leave that element out; do not invent one, and do not describe it instead. A flag whose
   elements have no recorded meaning gets a shorter text (or only what its article does say, such as "the colours have no official
   meaning").

7. **Use each country's name exactly as it is in the `name` field**, for the country itself and for any other country mentioned,
   so the spelling is the same everywhere ("Nauru", not "Naoero"; "United States", not "US" or "America"). The names are set in
   `country_data.json` (from the ISO list, overridden by `names` in `scripts/country_data_overrides.json`). Run
   `python3 scripts/check_country_names.py` after editing; it lists any text that uses another spelling.

## Sourcing and fact-checking

A description is only as good as its source, and every claim in it has to be checked against that source, not remembered.

- **The source is the flag article on Wikipedia** (`flagWikipediaUrl`). Run `python3 scripts/flag_sources.py <codes>` (or `--all`) to
  print what the article says each colour, symbol and emblem means, next to the current description. It reads the article's
  wikitext, because the plain-text view silently drops lists, tables and quotations, which is where many articles put the meanings.
- **Look at the flag** before writing: `python3 scripts/flag_sheets.py <dir> [codes]` makes labelled contact sheets of the images
  in the app. Describe what the image shows (a civil flag with no arms, a territory's own flag, a parent's flag).
- **Check every sentence, claim by claim.** For each colour, shape and emblem, find the passage that gives its meaning. If none
  does, delete the claim; do not keep it because it is widely repeated or you remember it. Typical failures found this way: a
  meaning that is in no article, one historian's reading stated as general, "pan-Arab" meanings attached to the wrong colour, and
  a detail described the wrong way round (which element sits above which).
- **When the article gives no meaning for a flag** (Bulgaria, Türkiye), write only what it does say, even if that is one sentence.
- **Where an article quotes more than one reading** (official, popular, a government's, the CIA World Factbook's), say which, or
  use the official one.
- **When the flag image changes** (the update workflow lists them as "Flag image changed"), the description may no longer be
  true: check it again.

## Linting

`python3 scripts/check_flag_descriptions.py` checks what a script can: every flag has a description and no other country does, no
year or date, a length limit, and country names (rule 7). It only warns about a sentence with no word that gives a meaning (rule 6)
or one that compares the flag with another (rule 3), because a script cannot be sure; look at each warning. The same check runs on
every pull request that touches the data or the scripts (`.github/workflows/check_country_data.yml`).

## Checking a text

Read each sentence in turn and ask "what does this tell the reader the thing means?". If the answer is only "what it looks like",
rewrite it with the meaning from the flag article, or remove it.
