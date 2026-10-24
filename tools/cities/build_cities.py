#!/usr/bin/env python3
"""Packs the GeoNames cities dump (CC BY 4.0, https://download.geonames.org/export/dump/) into the compact asset the weather search reads.

Usage: build_cities.py cities5000.txt app/src/main/assets/cities.tsv

One city per line, tab separated: display name, country code, latitude, longitude, population,
and the searchable names. The names are lower case, without accents, each between bars, for example
"|kharkiv|харьков|". The app normalizes what the user types the same way (see CityIndex.normalize).
"""
import re
import sys
import unicodedata


def normalize(text):
    text = unicodedata.normalize("NFD", text.lower())
    return "".join(ch for ch in text if unicodedata.category(ch) != "Mn").strip()


def main(source, target):
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
            out.write("\t".join([display, f[8], f[4], f[5], f[14], "|" + "|".join(names) + "|"]) + "\n")
            count += 1
    print(count, "cities")


main(sys.argv[1], sys.argv[2])
