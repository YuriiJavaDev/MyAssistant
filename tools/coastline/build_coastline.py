#!/usr/bin/env python3
"""Packs the Natural Earth land polygons (public domain) into the compact asset the route map reads.

Usage: build_coastline.py ne_10m_land.geojson app/src/main/assets/coastline.bin

Format: the bytes "CL1", then the number of rings, then for every ring its number of points and the points.
Every number is a zigzag varint. Points are stored in thousandths of a degree, each as the change from the
previous point of the same ring (the first point from 0, 0), longitude first.
"""
import json
import sys

SCALE = 1000


def varint(out, number):
    number = (number << 1) ^ (number >> 63) if number < 0 else number << 1
    while number > 0x7F:
        out.append((number & 0x7F) | 0x80)
        number >>= 7
    out.append(number)


def unsigned(out, number):
    while number > 0x7F:
        out.append((number & 0x7F) | 0x80)
        number >>= 7
    out.append(number)


def rings_of(geometry):
    polygons = geometry["coordinates"] if geometry["type"] == "MultiPolygon" else [geometry["coordinates"]]
    for polygon in polygons:
        for ring in polygon:
            yield ring


def simplified(ring):
    points = []
    for lon, lat in ring[:-1]:
        point = (round(lon * SCALE), round(lat * SCALE))
        if not points or point != points[-1]:
            points.append(point)
    return points


def main(source, target):
    with open(source, encoding="utf-8") as file:
        data = json.load(file)
    rings = []
    for feature in data["features"]:
        for ring in rings_of(feature["geometry"]):
            points = simplified(ring)
            if len(points) >= 3:
                rings.append(points)
    out = bytearray(b"CL1")
    unsigned(out, len(rings))
    for points in rings:
        unsigned(out, len(points))
        previous = (0, 0)
        for point in points:
            varint(out, point[0] - previous[0])
            varint(out, point[1] - previous[1])
            previous = point
    with open(target, "wb") as file:
        file.write(out)
    print(len(rings), "rings,", sum(len(r) for r in rings), "points,", len(out), "bytes")


if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2])
