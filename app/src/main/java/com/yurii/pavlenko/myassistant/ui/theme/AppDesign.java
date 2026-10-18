package com.yurii.pavlenko.myassistant.ui.theme;

import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;

import com.yurii.pavlenko.myassistant.R;

/** The looks the owner can choose from. Classic is the base theme (overlay 0); every other design is an overlay on top of it. */
public enum AppDesign {
    CLASSIC("classic", 0, R.string.design_classic_name, R.string.design_classic_about),
    PAPER_INK("paper_ink", R.style.Design_PaperInk, R.string.design_paper_ink_name, R.string.design_paper_ink_about),
    PASTEL_CLOUD("pastel_cloud", R.style.Design_PastelCloud, R.string.design_pastel_cloud_name, R.string.design_pastel_cloud_about),
    NEON_ARCADE("neon_arcade", R.style.Design_NeonArcade, R.string.design_neon_arcade_name, R.string.design_neon_arcade_about),
    TOON("toon", R.style.Design_Toon, R.string.design_toon_name, R.string.design_toon_about),
    BIKER("biker", R.style.Design_Biker, R.string.design_biker_name, R.string.design_biker_about),
    COCKPIT("cockpit", R.style.Design_Cockpit, R.string.design_cockpit_name, R.string.design_cockpit_about),
    BEACH("beach", R.style.Design_Beach, R.string.design_beach_name, R.string.design_beach_about),
    FOREST("forest", R.style.Design_Forest, R.string.design_forest_name, R.string.design_forest_about),
    MONO_BLOCK("mono_block", R.style.Design_MonoBlock, R.string.design_mono_block_name, R.string.design_mono_block_about);

    public final String id;
    @StyleRes
    public final int overlay;
    @StringRes
    public final int nameRes;
    @StringRes
    public final int aboutRes;

    AppDesign(String id, @StyleRes int overlay, @StringRes int nameRes, @StringRes int aboutRes) {
        this.id = id;
        this.overlay = overlay;
        this.nameRes = nameRes;
        this.aboutRes = aboutRes;
    }

    public boolean hasOverlay() {
        return overlay != 0;
    }

    /** The design saved under this id; Classic for a missing or unknown id. */
    public static AppDesign fromId(String id) {
        for (AppDesign design : values()) {
            if (design.id.equals(id)) {
                return design;
            }
        }
        return CLASSIC;
    }
}
