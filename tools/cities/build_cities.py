#!/usr/bin/env python3
"""Packs the GeoNames cities dump (CC BY 4.0, https://download.geonames.org/export/dump/) into the compact asset the weather search reads.

Usage: build_cities.py cities5000.txt app/src/main/assets/cities.tsv [--alternates alternates_filtered.txt] [--natural-earth ne_10m_populated_places.geojson]

One city per line, tab separated: display name, country code, latitude, longitude, population,
the searchable names, and the names to show by language. The searchable names are lower case, without
accents, each between bars, for example "|kharkiv|харьков|"; the app normalizes what the user types the same
way (see CityIndex.normalize). The names to show look like "he=חרקוב;el=Χάρκοβο".

The names to show come from two sources, the second filling what the first lacks:
  --alternates: alternates_filtered.txt, made by filter_alternates.py from the GeoNames alternate names, which say
    which language every name is in;
  --natural-earth: the Natural Earth populated places (public domain, names from Wikidata) for about 7,300 big
    cities in 26 languages. A place is matched with the city of the list that has the same name within 25 km.
Without either that column stays empty and the app shows the English name: the cities dump lists names without their language, and guessing by script gives wrong names
(for Seoul it picks the historical "경성").
"""
import json
import re
import sys
import unicodedata


def normalize(text):
    text = unicodedata.normalize("NFD", text.lower())
    return "".join(ch for ch in text if unicodedata.category(ch) != "Mn").strip()


LANGUAGE_ALIASES = {"iw": "he", "in": "id", "no": "nb"}
SHOWN = {"uk", "ru", "he", "ar", "es", "fr", "de", "it", "pt", "pl", "nl", "tr", "cs", "sk", "ro", "hu", "bg", "el",
         "sr", "hr", "sv", "da", "fi", "nb", "hi", "bn", "ur", "fa", "zh-CN", "zh-TW", "ja", "ko", "vi", "th", "id",
         "ka", "hy"}


def usable(name):
    return name and not re.search(r"[;=\t|]", name)


def labelled(path):
    """geonameid -> {language: name}, taking the preferred name of a language when there is one."""
    found, preferred = {}, set()
    with open(path, encoding="utf-8") as src:
        for line in src:
            f = line.rstrip("\n").split("\t")
            if len(f) < 4:
                continue
            geoid, lang, name, is_preferred = f[0], f[1], f[2], f[3] == "1"
            lang = LANGUAGE_ALIASES.get(lang, lang)
            if lang == "zh" or lang.startswith("zh-") or lang.startswith("zh_"):
                tag = "zh-TW" if re.search(r"(TW|HK|MO|Hant)", lang) else "zh-CN"
            else:
                tag = lang
            if tag not in SHOWN or not usable(name):
                continue
            key = (geoid, tag)
            if key in preferred and not is_preferred:
                continue
            if is_preferred:
                preferred.add(key)
            names = found.setdefault(geoid, {})
            if is_preferred or tag not in names:
                names[tag] = name
    return found


NATURAL_EARTH_FIELDS = {
    "NAME_UK": "uk", "NAME_RU": "ru", "NAME_HE": "he", "NAME_AR": "ar", "NAME_ES": "es", "NAME_FR": "fr",
    "NAME_DE": "de", "NAME_IT": "it", "NAME_PT": "pt", "NAME_PL": "pl", "NAME_NL": "nl", "NAME_TR": "tr",
    "NAME_HU": "hu", "NAME_EL": "el", "NAME_SV": "sv", "NAME_HI": "hi", "NAME_BN": "bn", "NAME_UR": "ur",
    "NAME_FA": "fa", "NAME_ZH": "zh-CN", "NAME_ZHT": "zh-TW", "NAME_JA": "ja", "NAME_KO": "ko", "NAME_VI": "vi",
    "NAME_ID": "id",
}
MATCH_KM = 25.0


def natural_earth(path, cities):
    """geonameid -> {language: name} from the Natural Earth places that match a city of the list."""
    import math
    index = {}
    for geoid, (names, lat, lon) in cities.items():
        index.setdefault((int(lat), int(lon)), []).append((geoid, names, lat, lon))
    found = {}
    matched = 0
    with open(path, encoding="utf-8") as src:
        places = json.load(src)["features"]
    for place in places:
        props = place["properties"]
        lon, lat = place["geometry"]["coordinates"]
        wanted = {normalize(props[k]) for k in ("NAME", "NAMEASCII", "NAME_EN") if props.get(k)}
        best, best_km = None, MATCH_KM
        for di in (-1, 0, 1):
            for dj in (-1, 0, 1):
                for geoid, names, clat, clon in index.get((int(lat) + di, int(lon) + dj), []):
                    if not wanted & names:
                        continue
                    km = math.hypot(clat - lat, (clon - lon) * math.cos(math.radians(lat))) * 111.2
                    if km < best_km:
                        best, best_km = geoid, km
        if best is None:
            continue
        matched += 1
        shown = {}
        for field, tag in NATURAL_EARTH_FIELDS.items():
            name = props.get(field)
            if usable(name) and name != props.get("NAME_EN"):
                shown[tag] = name
        found.setdefault(best, {}).update(shown)
    print(matched, "of", len(places), "Natural Earth places matched")
    return found


def main(source, target, alternates_file=None, natural_earth_file=None):
    exact = labelled(alternates_file) if alternates_file else None
    if natural_earth_file:
        known = {}
        with open(source, encoding="utf-8") as src:
            for line in src:
                f = line.rstrip("\n").split("\t")
                names = {normalize(c) for c in [f[1], f[2]] + f[3].split(",")}
                known[f[0]] = (names, float(f[4]), float(f[5]))
        extra = natural_earth(natural_earth_file, known)
        exact = exact if exact is not None else {}
        for geoid, names in extra.items():
            merged = dict(names)
            merged.update(exact.get(geoid, {}))
            exact[geoid] = merged
    count = 0
    with open(source, encoding="utf-8") as src, open(target, "w", encoding="utf-8", newline="\n") as out:
        for line in src:
            f = line.rstrip("\n").split("\t")
            name, ascii_name, alternates = f[1], f[2], f[3].split(",")
            display = ascii_name or name
            names = []
            for candidate in [name, ascii_name] + alternates:
                n = normalize(candidate)
                if n and "|" not in n and not re.match(r"^(https?:|www\.)", n) and n not in names:
                    names.append(n)
            shown = exact.get(f[0], {}) if exact is not None else {}
            shown_text = ";".join(lang + "=" + text for lang, text in sorted(shown.items()))
            out.write("\t".join([display, f[8], f[4], f[5], f[14], "|" + "|".join(names) + "|", shown_text]) + "\n")
            count += 1
    print(count, "cities")


def parse(arguments):
    positional, options = [], {}
    it = iter(arguments)
    for argument in it:
        if argument in ("--alternates", "--natural-earth"):
            options[argument] = next(it)
        else:
            positional.append(argument)
    return positional[0], positional[1], options.get("--alternates"), options.get("--natural-earth")


main(*parse(sys.argv[1:]))
