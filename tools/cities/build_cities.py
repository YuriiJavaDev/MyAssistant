#!/usr/bin/env python3
"""Packs the GeoNames cities dump (CC BY 4.0, https://download.geonames.org/export/dump/) into the compact asset the weather search reads.

Usage: build_cities.py cities5000.txt app/src/main/assets/cities.tsv [alternates_filtered.txt]

One city per line, tab separated: display name, country code, latitude, longitude, population,
the searchable names, and the names to show by language. The searchable names are lower case, without
accents, each between bars, for example "|kharkiv|харьков|"; the app normalizes what the user types the same
way (see CityIndex.normalize). The names to show look like "he=חרקוב;el=Χάρκοβο".

With alternates_filtered.txt (made by filter_alternates.py from the GeoNames alternate names, which say which
language every name is in) the names to show are exact. Without it that column stays empty and the app shows the
English name: the cities dump lists names without their language, and guessing by script gives wrong names
(for Seoul it picks the historical "경성").
"""
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


def main(source, target, alternates_file=None):
    exact = labelled(alternates_file) if alternates_file else None
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


main(*sys.argv[1:4])
