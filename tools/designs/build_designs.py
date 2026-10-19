#!/usr/bin/env python3
"""Generates the colour resources and the theme overlays of the app designs.

Run from the repository root:  python3 tools/designs/build_designs.py
It rewrites design_colors.xml (day and night) and design_styles.xml in app/src/main/res.
Classic is not generated: it is the base theme and keeps its hand-written colours.
"""
import os

RES = os.path.join(os.path.dirname(__file__), "..", "..", "app", "src", "main", "res")

TRANSPARENT = "#00000000"

# Per design: resource key, font family, corner radius (dp), border width (dp),
# button border (dp, colour token) and day / night colour tokens.
DESIGNS = [
    dict(key="paper_ink", font="sans-serif", r=6, sw=1, btn=(0, "line"),
         light=dict(bg="#F4F2EE", text="#1F2933", sub="#5F6B76", line="#C9C4BA", accent="#2F5D8A", tab="#E3E8EE",
                    track="#B8C7D6", b1="#2F5D8A", b2="#2F5D8A", bl="#FFFFFF", od="#B3261E", imp="#B25E00", ok="#1E7B4A"),
         dark=dict(bg="#15191E", text="#E6E8EB", sub="#9AA5B1", line="#3A444E", accent="#7FA7CF", tab="#232B34",
                   track="#3C4E60", b1="#5B8DBF", b2="#5B8DBF", bl="#0B1620", od="#FF8A80", imp="#FFB74D", ok="#66D19E")),
    dict(key="pastel_cloud", font="sans-serif", r=28, sw=1, btn=(0, "line"),
         light=dict(bg="#FFF5F8", text="#5B4B6E", sub="#6F6285", line="#E3C6D8", accent="#9B7BE0", tab="#F3E3FA",
                    track="#E1D3F5", b1="#F7A8C4", b2="#B79CED", bl="#4A3A63", od="#C2415D", imp="#C26A1B", ok="#2E9E6B"),
         dark=dict(bg="#231C2E", text="#F0E6FA", sub="#BCAFD0", line="#4E4066", accent="#D6BFFF", tab="#34294A",
                   track="#54466E", b1="#B78CE8", b2="#8E6FC9", bl="#1C1229", od="#FF8FA3", imp="#FFB86B", ok="#7FE0AE")),
    dict(key="neon_arcade", font="sans-serif-condensed", r=10, sw=2, btn=(2, "line"),
         light=dict(bg="#F4F0FF", text="#14102B", sub="#4B4673", line="#C400A8", accent="#00778A", tab="#E4DAFF",
                    track="#CDB8FF", b1="#E000C0", b2="#6A1BFF", bl="#FFFFFF", od="#C4163B", imp="#9A6B00", ok="#007A3D"),
         dark=dict(bg="#0B0B14", text="#F2F2FF", sub="#A3A3C9", line="#FF2BD6", accent="#00E5FF", tab="#1B1B33",
                   track="#3A2A66", b1="#FF2BD6", b2="#7A2BFF", bl="#FFFFFF", od="#FF5C7A", imp="#FFC400", ok="#2BFF88")),
    dict(key="toon", font="casual", r=16, sw=3, btn=(3, "line"),
         light=dict(bg="#FFF1B8", text="#1A1A1A", sub="#4D4D4D", line="#1A1A1A", accent="#3DA9FC", tab="#FFD23F",
                    track="#FFFFFF", b1="#FF5A5F", b2="#FF5A5F", bl="#1A1A1A", od="#D90429", imp="#E85D04", ok="#2B9348"),
         dark=dict(bg="#2B2559", text="#FFF6D8", sub="#D8D0F0", line="#FFF6D8", accent="#5CC8FF", tab="#5B4FD6",
                   track="#FFFFFF", b1="#FF6B6B", b2="#FF6B6B", bl="#1A1A1A", od="#FF8A8A", imp="#FFB703", ok="#5EE08A")),
    dict(key="biker", font="sans-serif-condensed", r=3, sw=2, btn=(0, "line"),
         light=dict(bg="#D8D3CA", text="#1B1B1B", sub="#504B44", line="#2B2B2B", accent="#D9480F", tab="#BFB8AA",
                    track="#8A8478", b1="#3A3A3A", b2="#1B1B1B", bl="#FF8A2A", od="#A4161A", imp="#B34700", ok="#2B6A2F"),
         dark=dict(bg="#121212", text="#ECE6DA", sub="#A8A194", line="#4A4A4A", accent="#FF7A1A", tab="#2A2A2A",
                   track="#5A5348", b1="#FF7A1A", b2="#C2410C", bl="#121212", od="#FF6B5E", imp="#FFB020", ok="#6BD36F")),
    dict(key="cockpit", font="monospace", r=3, sw=1, btn=(1, "accent"),
         light=dict(bg="#E7ECEF", text="#0C1116", sub="#43525E", line="#9FB0BC", accent="#8A5200", tab="#D3DDE3",
                    track="#9FB0BC", b1=TRANSPARENT, b2=TRANSPARENT, bl="#8A5200", od="#B3261E", imp="#8A5200",
                    ok="#17803D", bpe="#338A5200"),
         dark=dict(bg="#0C1116", text="#D7E3EA", sub="#8AA0AF", line="#2E4352", accent="#FFB000", tab="#16212A",
                   track="#2E4352", b1=TRANSPARENT, b2=TRANSPARENT, bl="#FFB000", od="#FF5A4F", imp="#FFB000",
                   ok="#35E27D", bpe="#33FFB000")),
    dict(key="beach", font="sans-serif", r=24, sw=1.5, btn=(0, "line"),
         light=dict(bg="#FFF4DC", text="#0B4F6C", sub="#4F7F95", line="#7FD1D9", accent="#0FA3B1", tab="#CFF1F0",
                    track="#9BDDE0", b1="#FF8A65", b2="#FF5C58", bl="#4A1209", od="#C62828", imp="#B8590A", ok="#1B8F5A"),
         dark=dict(bg="#10283A", text="#FFE9C7", sub="#A9C3D1", line="#2E5F78", accent="#3ED6D6", tab="#1C3F57",
                   track="#2E5F78", b1="#FF7A5C", b2="#D6455D", bl="#1A0B10", od="#FF8A80", imp="#FFC266", ok="#5EE0A0")),
    dict(key="forest", font="serif", r=12, sw=2, btn=(0, "line"),
         light=dict(bg="#F1E6C8", text="#2B3A2F", sub="#5E5A3F", line="#A98B3D", accent="#1F6F54", tab="#E0D2A8",
                    track="#BFAF7A", b1="#2E8B6A", b2="#1B4D3E", bl="#F2D27A", od="#A31D1D", imp="#8A5A00", ok="#1F6F3A"),
         dark=dict(bg="#0F1F1A", text="#EAD9A4", sub="#A8B59A", line="#8A7332", accent="#D8B24A", tab="#1B3329",
                   track="#3E5A4B", b1="#2E8B6A", b2="#14382D", bl="#F2D27A", od="#FF8A7A", imp="#E8B84A", ok="#6FD39A")),
    dict(key="mono_block", font="sans-serif-black", r=0, sw=3, btn=(0, "line"),
         light=dict(bg="#FFFFFF", text="#000000", sub="#333333", line="#000000", accent="#000000", tab="#FFE600",
                    track="#000000", b1="#000000", b2="#000000", bl="#FFFFFF", od="#D00000", imp="#B35900", ok="#007A33",
                    tabtext="#000000"),
         dark=dict(bg="#000000", text="#FFFFFF", sub="#CCCCCC", line="#FFFFFF", accent="#FFE600", tab="#FFE600",
                   track="#FFFFFF", b1="#FFE600", b2="#FFE600", bl="#000000", od="#FF5C5C", imp="#FFB000", ok="#4DFF88",
                   tabtext="#000000")),
]


def rgb(h):
    h = h.lstrip("#")
    return tuple(int(h[i:i + 2], 16) for i in (len(h) - 6, len(h) - 4, len(h) - 2))


def hexed(c):
    return "#" + "".join("%02X" % round(v) for v in c)


def mix(a, b, t):
    """Colour a moved the fraction t towards colour b."""
    ca, cb = rgb(a), rgb(b)
    return hexed(tuple(x + (y - x) * t for x, y in zip(ca, cb)))


def luminance(h):
    def lin(v):
        v /= 255
        return v / 12.92 if v <= 0.03928 else ((v + 0.055) / 1.055) ** 2.4
    r, g, b = (lin(v) for v in rgb(h))
    return 0.2126 * r + 0.7152 * g + 0.0722 * b


def on_colour(h):
    return "#FFFFFF" if luminance(h) < 0.35 else "#000000"


def colours(t):
    """All colour resources of one design in one mode, keyed by the resource suffix."""
    out = dict(
        background=t["bg"], text=t["text"], text_secondary=t["sub"], text_muted=t["sub"], outline=t["line"],
        tab_selected=t["tab"], switch_track=t["track"], accent=t["accent"], button_start=t["b1"], button_end=t["b2"],
        button_label=t["bl"], overdue=t["od"], important=t["imp"], ok=t["ok"],
        button_pressed_end=t.get("bpe") or mix(t["b2"], t["b1"] if t["b1"] != t["b2"] else t["text"], 0.5),
        tab_text=t.get("tabtext", t["text"]),
        md_on_primary=on_colour(t["accent"]),
        md_surface_container_lowest=t["bg"],
        md_surface_container_low=mix(t["bg"], t["text"], 0.03),
        md_surface_container=mix(t["bg"], t["text"], 0.06),
        md_surface_container_high=mix(t["bg"], t["text"], 0.09),
        md_surface_container_highest=mix(t["bg"], t["text"], 0.12),
    )
    return out


def colour_file(mode):
    lines = ['<?xml version="1.0" encoding="utf-8"?>', "<resources>",
             "    <!-- Generated by tools/designs/build_designs.py; do not edit by hand -->"]
    for d in DESIGNS:
        lines.append("")
        for name, value in colours(d[mode]).items():
            argb = value if len(value.lstrip("#")) == 8 else "#FF" + value.lstrip("#")
            lines.append('    <color name="%s_%s">%s</color>' % (d["key"], name, argb))
    lines.append("</resources>")
    return "\n".join(lines) + "\n"


def dp(v):
    return ("%g" % v) + "dp"


def style(d):
    k = d["key"]
    c = lambda n: "@color/%s_%s" % (k, n)
    panel = round(d["r"] * 0.8)
    tab = min(d["r"], 18)
    bw, bcolour = d["btn"]
    items = [
        ("maBackground", c("background")), ("maText", c("text")), ("maTextSecondary", c("text_secondary")),
        ("maTextMuted", c("text_muted")), ("maOutline", c("outline")), ("maTabSelected", c("tab_selected")),
        ("maTabText", c("tab_text")), ("maSwitchTrack", c("switch_track")), ("maAccent", c("accent")),
        ("maButtonLabel", c("button_label")), ("maButtonStart", c("button_start")), ("maButtonEnd", c("button_end")),
        ("maButtonPressedEnd", c("button_pressed_end")), ("maOverdue", c("overdue")),
        ("maImportant", c("important")), ("maOk", c("ok")),
        ("maCorner", dp(d["r"])), ("maCornerPanel", dp(panel)), ("maCornerTab", dp(tab)),
        ("maStroke", dp(d["sw"])), ("maButtonStroke", dp(bw)),
        ("maButtonStrokeColor", c("outline" if bcolour == "line" else "accent")),
        ("maBackdrop", "@drawable/backdrop_%s" % k),
        ("android:fontFamily", d["font"]),
        ("colorPrimary", c("accent")), ("colorOnPrimary", c("md_on_primary")),
        ("android:colorBackground", c("background")), ("colorSurface", c("background")),
        ("colorSurfaceContainerLowest", c("md_surface_container_lowest")),
        ("colorSurfaceContainerLow", c("md_surface_container_low")),
        ("colorSurfaceContainer", c("md_surface_container")),
        ("colorSurfaceContainerHigh", c("md_surface_container_high")),
        ("colorSurfaceContainerHighest", c("md_surface_container_highest")),
        ("colorOnSurface", c("text")), ("colorSurfaceVariant", c("tab_selected")),
        ("colorOnSurfaceVariant", c("text_secondary")), ("colorOutline", c("outline")),
    ]
    name = "".join(p.capitalize() for p in k.split("_"))
    lines = ["    <style name=\"Design.%s\" parent=\"\">" % name]
    lines += ['        <item name="%s">%s</item>' % item for item in items]
    lines.append("    </style>")
    return "\n".join(lines)


def style_file():
    body = "\n\n".join(style(d) for d in DESIGNS)
    return ('<?xml version="1.0" encoding="utf-8"?>\n<resources>\n'
            "    <!-- Generated by tools/designs/build_designs.py; each overlay is applied over Theme.MyAssistant -->\n"
            + body + "\n</resources>\n")


def write(path, text):
    with open(os.path.join(RES, path), "w", encoding="utf-8", newline="\n") as f:
        f.write(text)


if __name__ == "__main__":
    write("values/design_colors.xml", colour_file("light"))
    write("values-night/design_colors.xml", colour_file("dark"))
    write("values/design_styles.xml", style_file())
