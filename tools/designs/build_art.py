#!/usr/bin/env python3
"""Draws the muted backdrop pictures of the app designs as Android vector drawables.

Run from the repository root:  python3 tools/designs/build_art.py [--preview out.html]
Every picture is one colour on a 360 x 360 canvas; the app tints it with the text colour of the design and
lays it over the background at a low opacity. The script writes art_<key>.xml and backdrop_<key>.xml into
app/src/main/res/drawable. With --preview it also writes an SVG sheet for looking at the pictures.
"""
import math
import os
import sys

RES = os.path.join(os.path.dirname(__file__), "..", "..", "app", "src", "main", "res", "drawable")
SIZE = 360


# ---- path helpers: each returns an (element) tuple (kind, path data, stroke width, opacity) ----

def f(v):
    return ("%.1f" % v).rstrip("0").rstrip(".")


def line(d, w=2.5, a=1.0):
    return ("line", d, w, a)


def fill(d, a=1.0):
    return ("fill", d, 0, a)


def circle_d(cx, cy, r):
    return "M%s,%s a%s,%s 0 1,0 %s,0 a%s,%s 0 1,0 %s,0 Z" % (f(cx - r), f(cy), f(r), f(r), f(2 * r), f(r), f(r), f(-2 * r))


def ellipse_d(cx, cy, rx, ry):
    return "M%s,%s a%s,%s 0 1,0 %s,0 a%s,%s 0 1,0 %s,0 Z" % (f(cx - rx), f(cy), f(rx), f(ry), f(2 * rx), f(rx), f(ry), f(-2 * rx))


def circle(cx, cy, r, w=2.5, a=1.0):
    return line(circle_d(cx, cy, r), w, a)


def disc(cx, cy, r, a=1.0):
    return fill(circle_d(cx, cy, r), a)


def seg(x1, y1, x2, y2, w=2.5, a=1.0):
    return line("M%s,%s L%s,%s" % (f(x1), f(y1), f(x2), f(y2)), w, a)


def polar(cx, cy, r, deg):
    t = math.radians(deg)
    return cx + r * math.cos(t), cy + r * math.sin(t)


def spokes(cx, cy, r1, r2, count, w=1.5, a=1.0, start=0):
    out = []
    for i in range(count):
        x1, y1 = polar(cx, cy, r1, start + 360 * i / count)
        x2, y2 = polar(cx, cy, r2, start + 360 * i / count)
        out.append(seg(x1, y1, x2, y2, w, a))
    return out


def star4(cx, cy, r, a=1.0):
    k = r * 0.22
    pts = [(cx, cy - r), (cx + k, cy - k), (cx + r, cy), (cx + k, cy + k), (cx, cy + r), (cx - k, cy + k), (cx - r, cy), (cx - k, cy - k)]
    return fill("M" + " L".join("%s,%s" % (f(x), f(y)) for x, y in pts) + " Z", a)


def burst(cx, cy, r1, r2, points, rot=0):
    pts = []
    for i in range(points * 2):
        r = r2 if i % 2 == 0 else r1
        pts.append(polar(cx, cy, r, rot + 180 * i / points))
    return "M" + " L".join("%s,%s" % (f(x), f(y)) for x, y in pts) + " Z"


def rect_d(x, y, w, h):
    return "M%s,%s h%s v%s h%s Z" % (f(x), f(y), f(w), f(h), f(-w))


# ---- the pictures ----

def paper_ink():
    e = [
        line("M60,330 C100,300 120,320 150,296 C176,276 196,300 228,270 C258,242 280,262 318,226", 2),
        line("M28,350 H332", 1.2, 0.7), line("M28,332 H332", 1.2, 0.7), line("M28,314 H332", 1.2, 0.7),
        # fountain pen lying across the page
        line("M96,190 L292,86 L306,112 L110,216 Z", 2.5),
        line("M292,86 L318,72 L330,96 L306,112", 2.5),
        seg(250, 108, 266, 136, 2), seg(228, 120, 244, 148, 2),
        line("M96,190 L110,216 L70,262 Z", 2.5),
        seg(70, 262, 92, 218, 1.5),
        disc(102, 207, 3),
    ]
    return e


def pastel_cloud():
    e = []
    # soft clouds built from circles
    for cx, cy, s, a in [(110, 300, 1.0, 1.0), (250, 240, 0.8, 0.9), (95, 150, 0.55, 0.8)]:
        for dx, dy, r in [(-34, 0, 26), (0, -14, 34), (38, -2, 28), (68, 8, 20)]:
            e.append(circle(cx + dx * s, cy + dy * s, r * s, 2.5, a))
        e.append(seg(cx - 56 * s, cy + 24 * s, cx + 86 * s, cy + 24 * s, 2.5, a))
    for x, y, r in [(300, 110, 16), (220, 60, 10), (40, 80, 9), (330, 330, 9), (170, 190, 7)]:
        e.append(star4(x, y, r))
    for x, y in [(268, 150), (60, 230), (200, 345)]:
        e.append(disc(x, y, 3.5))
    # a small heart
    e.append(line("M300,200 c-14,-18 -34,2 -14,18 l14,12 l14,-12 c20,-16 0,-36 -14,-18 Z", 2.2))
    return e


def neon_arcade():
    e = []
    # striped retro sun: a disc cut into bands that get thinner towards the top
    cx, cy, r = 180, 170, 100
    bands = [(-100, -62), (-52, -22), (-14, 6), (14, 30), (38, 52), (60, 70), (78, 86)]
    for y0, y1 in bands:
        xa = math.sqrt(r * r - y0 * y0)
        xb = math.sqrt(r * r - y1 * y1)
        e.append(fill("M%s,%s L%s,%s A%s,%s 0 0,1 %s,%s L%s,%s A%s,%s 0 0,1 %s,%s Z" % (
            f(cx - xa), f(cy + y0), f(cx + xa), f(cy + y0), f(r), f(r), f(cx + xb), f(cy + y1),
            f(cx - xb), f(cy + y1), f(r), f(r), f(cx - xa), f(cy + y0)), 0.9))
    # horizon and perspective grid
    e.append(seg(0, 262, 360, 262, 2.5))
    for x in range(-360, 721, 60):
        e.append(seg(180 + (x - 180) * 0.12, 262, x, 360, 1.4, 0.8))
    for y, w in [(274, 1.4), (292, 1.6), (318, 1.8), (352, 2)]:
        e.append(seg(0, y, 360, y, w, 0.8))
    # stars
    for x, y, r in [(50, 60, 8), (310, 50, 6), (270, 130, 4), (90, 120, 4)]:
        e.append(star4(x, y, r))
    # an arcade joystick
    e.append(line(ellipse_d(300, 100, 40, 12), 3))
    e.append(line("M262,100 v-6 a38,12 0 0 1 76,0 v6", 3))
    e.append(seg(300, 94, 300, 48, 6))
    e.append(disc(300, 36, 16))
    e.append(disc(244, 96, 5))
    return e


def toon():
    e = []
    cx, cy = 140, 250
    e.append(circle(cx, cy, 82, 4))
    e.append(line("M%s,%s L%s,%s L%s,%s" % (f(cx - 76), f(cy - 36), f(cx - 70), f(cy - 112), f(cx - 20), f(cy - 76)), 4))
    e.append(line("M%s,%s L%s,%s L%s,%s" % (f(cx + 76), f(cy - 36), f(cx + 70), f(cy - 112), f(cx + 20), f(cy - 76)), 4))
    e.append(fill(ellipse_d(cx - 30, cy - 8, 11, 15)))
    e.append(fill(ellipse_d(cx + 30, cy - 8, 11, 15)))
    e.append(fill("M%s,%s l12,0 l-6,9 Z" % (f(cx - 6), f(cy + 14))))
    e.append(line("M%s,%s c-6,16 -22,16 -28,2 M%s,%s c6,16 22,16 28,2" % (f(cx), f(cy + 23), f(cx), f(cy + 23)), 3.5))
    for s in (-1, 1):
        e.append(seg(cx + s * 56, cy + 14, cx + s * 112, cy + 2, 3))
        e.append(seg(cx + s * 56, cy + 26, cx + s * 110, cy + 36, 3))
    # comic burst with a bolt, a star and a speech bubble
    e.append(line(burst(272, 112, 38, 62, 9, -90), 4))
    e.append(fill("M276,78 L252,118 L270,118 L262,146 L292,104 L274,104 Z"))
    e.append(line("M214,330 h120 a14,14 0 0 0 14,-14 v-28 a14,14 0 0 0 -14,-14 h-120 a14,14 0 0 0 -14,14 v28 a14,14 0 0 0 14,14 Z M236,330 l-12,20 l30,-20", 4))
    for i in range(3):
        e.append(fill(circle_d(240 + i * 26, 302, 5)))
    e.append(star4(320, 200, 14))
    e.append(star4(40, 90, 12))
    return e


def biker():
    e = []
    rear, front = (92, 276), (288, 276)
    for cx, cy in (rear, front):
        e.append(circle(cx, cy, 46, 7))
        e.append(circle(cx, cy, 32, 2))
        e += spokes(cx, cy, 7, 32, 14, 1.3)
        e.append(disc(cx, cy, 7))
    # fenders
    e.append(line("M40,262 A56,56 0 0 1 128,228", 4))
    e.append(line("M236,248 A56,56 0 0 1 300,222", 4))
    # frame, seat, tank
    e.append(line("M92,276 L150,236 L214,246", 4))
    e.append(line("M92,276 L170,270", 3))
    e.append(line("M66,236 C98,222 140,222 168,224 L166,236 C132,236 100,240 78,250 Z", 3))
    e.append(line("M162,222 C170,194 220,188 240,210 C246,218 242,226 232,228 L174,232 C162,232 158,228 162,222 Z", 3))
    # engine block with fins
    e.append(line("M148,244 h66 v40 h-66 Z", 3))
    for x in range(156, 212, 10):
        e.append(seg(x, 248, x, 280, 1.5))
    e.append(line("M160,244 L146,214 L166,208 L178,242", 2.5))
    e.append(line("M192,244 L196,212 L216,214 L208,244", 2.5))
    # fork, bars, headlamp
    e.append(seg(288, 276, 252, 192, 5))
    e.append(seg(298, 272, 262, 188, 3))
    e.append(line("M252,192 L248,164 C246,152 234,148 226,156", 4))
    e.append(circle(270, 200, 11, 3))
    e.append(seg(214, 246, 252, 200, 3))
    # exhaust pipes
    e.append(line("M162,282 C130,296 90,304 40,300", 5))
    e.append(line("M176,286 C142,306 100,312 50,310", 4))
    # road
    for x in range(10, 360, 44):
        e.append(seg(x, 336, x + 24, 336, 3))
    # speed lines and a wing behind the rider
    for y, x1, x2 in ((150, 20, 86), (172, 40, 100), (194, 12, 70)):
        e.append(seg(x1, y, x2, y, 2.5, 0.8))
    gx, gy = 312, 84
    e.append(circle(gx, gy, 24, 4))
    e.append(circle(gx, gy, 9, 3))
    for i in range(8):
        x1, y1 = polar(gx, gy, 28, i * 45)
        x2, y2 = polar(gx, gy, 38, i * 45)
        e.append(seg(x1, y1, x2, y2, 9))
    e.append(star4(250, 40, 9))
    e.append(star4(40, 70, 7))
    return e


def cockpit():
    e = []
    # a round instrument
    cx, cy, r = 250, 112, 76
    e.append(circle(cx, cy, r, 4))
    e.append(circle(cx, cy, r - 12, 1.5, 0.8))
    for i in range(36):
        major = i % 3 == 0
        x1, y1 = polar(cx, cy, r - (24 if major else 18), i * 10)
        x2, y2 = polar(cx, cy, r - 12, i * 10)
        e.append(seg(x1, y1, x2, y2, 2.4 if major else 1.3))
    nx, ny = polar(cx, cy, r - 24, -50)
    e.append(seg(cx, cy, nx, ny, 3.5))
    hx, hy = polar(cx, cy, r - 40, 70)
    e.append(seg(cx, cy, hx, hy, 5))
    e.append(disc(cx, cy, 6))
    # a glider seen from above
    gx, gy = 150, 262
    e.append(line("M%s,%s L18,%s L14,%s L%s,%s Z" % (f(gx - 6), f(gy - 8), f(gy - 3), f(gy + 9), f(gx - 6), f(gy + 14)), 3))
    e.append(line("M%s,%s L342,%s L346,%s L%s,%s Z" % (f(gx + 6), f(gy - 8), f(gy - 3), f(gy + 9), f(gx + 6), f(gy + 14)), 3))
    e.append(line("M%s,%s C%s,%s %s,%s %s,%s L%s,%s L%s,%s L%s,%s C%s,%s %s,%s %s,%s Z" % (
        f(gx), f(gy - 70), f(gx + 8), f(gy - 60), f(gx + 10), f(gy - 36), f(gx + 8), f(gy - 8),
        f(gx + 5), f(gy + 70), f(gx - 5), f(gy + 70), f(gx - 8), f(gy - 8),
        f(gx - 10), f(gy - 36), f(gx - 8), f(gy - 60), f(gx), f(gy - 70)), 3))
    e.append(line("M%s,%s h56 l-4,9 h-48 Z" % (f(gx - 28), f(gy + 62)), 2.5))
    e.append(line(ellipse_d(gx, gy - 44, 5, 14), 2))
    for x in (60, 100, 200, 240, 280):
        e.append(seg(x, gy + 2, x, gy + 4, 1.5))
    # contrail
    e.append(line("M160,340 C200,334 250,338 330,326", 2, 0.8))
    e.append(line("M20,60 C60,56 110,64 150,58", 2, 0.8))
    return e


def beach():
    e = []
    # sun
    e.append(circle(80, 92, 34, 3.5))
    for i in range(16):
        x1, y1 = polar(80, 92, 44, i * 22.5)
        x2, y2 = polar(80, 92, 56 if i % 2 == 0 else 52, i * 22.5)
        e.append(seg(x1, y1, x2, y2, 3))
    # palm
    e.append(line("M286,340 C282,290 288,240 270,190", 8))
    e.append(line("M294,340 C292,290 298,240 280,188", 3, 0.7))
    top = (272, 186)
    fronds = [
        "M%s,%s C250,150 206,146 176,176", "M%s,%s C256,138 224,114 192,116", "M%s,%s C270,128 262,96 246,76",
        "M%s,%s C290,134 308,106 336,98", "M%s,%s C300,156 336,150 352,178", "M%s,%s C282,170 316,184 330,214",
        "M%s,%s C262,172 236,186 220,214",
    ]
    for d in fronds:
        e.append(line(d % (f(top[0]), f(top[1])), 5))
    e.append(disc(266, 196, 7)); e.append(disc(280, 200, 7))
    # waves
    for y, a in ((300, 1.0), (322, 0.8), (344, 0.6)):
        e.append(line("M0,%s q22,-14 45,0 t45,0 t45,0 t45,0 t45,0 t45,0 t45,0 t45,0 t45,0" % f(y), 3, a))
    # gulls and a shell
    for x, y, s in ((150, 70, 1.0), (196, 100, 0.7), (118, 120, 0.6)):
        e.append(line("M%s,%s q%s,%s %s,%s q%s,%s %s,%s" % (f(x), f(y), f(10 * s), f(-12 * s), f(20 * s), f(2 * s), f(10 * s), f(-12 * s), f(20 * s), f(-2 * s)), 2.8))
    e.append(line("M40,262 c-22,-6 -26,-34 0,-44 c26,10 22,38 0,44 Z", 2.5))
    for dx in (-10, -4, 2, 8):
        e.append(seg(40, 262, 40 + dx * 1.2, 224, 1.5))
    return e


def forest():
    e = []
    # fern frond curving up from the lower right
    pts = []
    for i in range(0, 41):
        t = i / 40
        x = 330 - 190 * t - 40 * math.sin(t * math.pi)
        y = 350 - 250 * t
        pts.append((x, y))
    e.append(line("M" + " L".join("%s,%s" % (f(x), f(y)) for x, y in pts), 3.5))
    for i in range(3, 40, 2):
        x, y = pts[i]
        t = i / 40
        length = 56 * (1 - t) ** 0.7 + 8
        for s in (-1, 1):
            ang = -90 + s * 62 - 28
            ex, ey = polar(x, y, length, ang if s < 0 else ang + 0)
            e.append(line("M%s,%s L%s,%s" % (f(x), f(y), f(ex), f(ey)), 2.6, 0.9))
    # mushrooms
    for x, y, s in ((70, 340, 1.0), (120, 348, 0.7)):
        e.append(line("M%s,%s a%s,%s 0 0 1 %s,0 Z" % (f(x - 34 * s), f(y - 40 * s), f(34 * s), f(30 * s), f(68 * s)), 3))
        e.append(line("M%s,%s v%s q%s,%s %s,0 v%s" % (f(x - 12 * s), f(y - 40 * s), f(36 * s), f(12 * s), f(8 * s), f(24 * s), f(-36 * s)), 3))
        for dx, dy in ((-14, -58), (6, -64), (18, -52)):
            e.append(disc(x + dx * s, y + dy * s + 8 * s, 3.5 * s))
    # two leaves with veins
    for lx, ly, k in ((78, 150, 1.0), (250, 66, 0.6)):
        e.append(line("M%s,%s C%s,%s %s,%s %s,%s C%s,%s %s,%s %s,%s Z" % (
            f(lx), f(ly + 64 * k), f(lx - 44 * k), f(ly + 30 * k), f(lx - 38 * k), f(ly - 34 * k), f(lx), f(ly - 66 * k),
            f(lx + 38 * k), f(ly - 34 * k), f(lx + 44 * k), f(ly + 30 * k), f(lx), f(ly + 64 * k)), 3))
        e.append(seg(lx, ly + 90 * k, lx, ly - 52 * k, 2.5))
        for dy in (-34, -12, 10, 32):
            e.append(seg(lx, ly + dy * k, lx - 22 * k, ly + (dy - 16) * k, 1.8))
            e.append(seg(lx, ly + dy * k, lx + 22 * k, ly + (dy - 16) * k, 1.8))
    # fireflies
    for x, y, r in ((250, 90, 5), (300, 140, 4), (190, 60, 3.5), (330, 60, 3), (40, 40, 4), (220, 200, 3)):
        e.append(disc(x, y, r))
    return e


def mono_block():
    e = []
    e.append(line(rect_d(150, 70, 190, 190), 8))
    e.append(line(rect_d(110, 110, 190, 190), 8, 0.8))
    e.append(fill(rect_d(70, 150, 190, 190), 0.55))
    e.append(fill(circle_d(300, 300, 36)))
    for i in range(7):
        e.append(seg(20 + i * 18, 60, 4 + i * 18, 100, 6))
    e.append(fill("M40,24 h120 v-18 l60,40 l-60,40 v-18 h-120 Z", 0.8))
    return e


ART = [
    ("paper_ink", paper_ink, 0.10),
    ("pastel_cloud", pastel_cloud, 0.14),
    ("neon_arcade", neon_arcade, 0.12),
    ("toon", toon, 0.12),
    ("biker", biker, 0.12),
    ("cockpit", cockpit, 0.12),
    ("beach", beach, 0.13),
    ("forest", forest, 0.13),
    ("mono_block", mono_block, 0.08),
]


def vector_xml(elements, alpha):
    lines = ['<?xml version="1.0" encoding="utf-8"?>',
             '<!-- Generated by tools/designs/build_art.py; do not edit by hand -->',
             '<vector xmlns:android="http://schemas.android.com/apk/res/android"',
             '    android:width="%ddp"' % SIZE, '    android:height="%ddp"' % SIZE,
             '    android:viewportWidth="%d"' % SIZE, '    android:viewportHeight="%d"' % SIZE,
             '    android:alpha="%s"' % alpha, '    android:tint="?attr/maText">']
    for kind, d, w, a in elements:
        if kind == "line":
            lines.append('    <path android:pathData="%s" android:strokeColor="@color/black" android:strokeWidth="%s"'
                         ' android:strokeLineCap="round" android:strokeLineJoin="round"%s />'
                         % (d, f(w), (' android:strokeAlpha="%s"' % a) if a != 1.0 else ""))
        else:
            lines.append('    <path android:pathData="%s" android:fillColor="@color/black"%s />'
                         % (d, (' android:fillAlpha="%s"' % a) if a != 1.0 else ""))
    lines.append("</vector>")
    return "\n".join(lines) + "\n"


def backdrop_xml(key):
    return ('<?xml version="1.0" encoding="utf-8"?>\n'
            '<!-- Generated by tools/designs/build_art.py; the background colour with a muted picture in the corner -->\n'
            '<layer-list xmlns:android="http://schemas.android.com/apk/res/android">\n'
            '    <item>\n        <shape android:shape="rectangle">\n            <solid android:color="?attr/maBackground" />\n'
            '        </shape>\n    </item>\n'
            '    <item\n        android:drawable="@drawable/art_%s"\n        android:gravity="bottom|end" />\n'
            '</layer-list>\n' % key)


def plain_backdrop_xml():
    return ('<?xml version="1.0" encoding="utf-8"?>\n'
            '<shape xmlns:android="http://schemas.android.com/apk/res/android"\n    android:shape="rectangle">\n'
            '    <solid android:color="?attr/maBackground" />\n</shape>\n')


def svg(elements, alpha=1.0):
    parts = ['<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 %d %d" width="%d" height="%d">' % (SIZE, SIZE, SIZE, SIZE)]
    for kind, d, w, a in elements:
        if kind == "line":
            parts.append('<path d="%s" fill="none" stroke="#000" stroke-width="%s" stroke-linecap="round" stroke-linejoin="round" opacity="%s"/>' % (d, f(w), a))
        else:
            parts.append('<path d="%s" fill="#000" opacity="%s"/>' % (d, a))
    parts.append("</svg>")
    return "\n".join(parts)


def write(name, text):
    with open(os.path.join(RES, name), "w", encoding="utf-8", newline="\n") as out:
        out.write(text)


if __name__ == "__main__":
    write("backdrop_classic.xml", plain_backdrop_xml())
    write("backdrop_paper_ink.xml", backdrop_xml("paper_ink"))
    for key, make, alpha in ART:
        write("art_%s.xml" % key, vector_xml(make(), alpha))
        if key != "paper_ink":
            write("backdrop_%s.xml" % key, backdrop_xml(key))
    if "--preview" in sys.argv:
        out = sys.argv[sys.argv.index("--preview") + 1]
        cells = "".join('<div style="display:inline-block;margin:6px;text-align:center;font:12px sans-serif">%s<br>%s</div>'
                        % (svg(make()), key) for key, make, _ in ART)
        with open(out, "w") as sheet:
            sheet.write('<html><body style="background:#fff">%s</body></html>' % cells)
