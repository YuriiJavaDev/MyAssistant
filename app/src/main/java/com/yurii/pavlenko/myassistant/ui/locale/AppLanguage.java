package com.yurii.pavlenko.myassistant.ui.locale;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** A language the app is translated into, shown by its own name so that it can be found in any language. */
public final class AppLanguage {

    private static final List<AppLanguage> ALL = Collections.unmodifiableList(Arrays.asList(
            new AppLanguage("en", "English"),
            new AppLanguage("uk", "Українська"),
            new AppLanguage("ru", "Русский"),
            new AppLanguage("he", "עברית"),
            new AppLanguage("ar", "العربية"),
            new AppLanguage("es", "Español"),
            new AppLanguage("fr", "Français"),
            new AppLanguage("de", "Deutsch"),
            new AppLanguage("it", "Italiano"),
            new AppLanguage("pt", "Português"),
            new AppLanguage("pl", "Polski"),
            new AppLanguage("nl", "Nederlands"),
            new AppLanguage("tr", "Türkçe"),
            new AppLanguage("cs", "Čeština"),
            new AppLanguage("sk", "Slovenčina"),
            new AppLanguage("ro", "Română"),
            new AppLanguage("hu", "Magyar"),
            new AppLanguage("bg", "Български"),
            new AppLanguage("el", "Ελληνικά"),
            new AppLanguage("sr", "Српски"),
            new AppLanguage("hr", "Hrvatski"),
            new AppLanguage("sv", "Svenska"),
            new AppLanguage("da", "Dansk"),
            new AppLanguage("nb", "Norsk bokmål"),
            new AppLanguage("fi", "Suomi"),
            new AppLanguage("hi", "हिन्दी"),
            new AppLanguage("bn", "বাংলা"),
            new AppLanguage("ur", "اردو"),
            new AppLanguage("fa", "فارسی"),
            new AppLanguage("zh-CN", "简体中文"),
            new AppLanguage("zh-TW", "繁體中文"),
            new AppLanguage("ja", "日本語"),
            new AppLanguage("ko", "한국어"),
            new AppLanguage("vi", "Tiếng Việt"),
            new AppLanguage("th", "ไทย"),
            new AppLanguage("id", "Bahasa Indonesia"),
            new AppLanguage("ka", "ქართული"),
            new AppLanguage("hy", "Հայերեն")
    ));

    private final String tag;
    private final String nativeName;

    private AppLanguage(String tag, String nativeName) {
        this.tag = tag;
        this.nativeName = nativeName;
    }

    /** The languages to choose from; it must match res/xml/locales_config.xml. */
    @NonNull
    public static List<AppLanguage> all() {
        return ALL;
    }

    /** The BCP 47 tag, such as "en" or "zh-CN". */
    @NonNull
    public String getTag() {
        return tag;
    }

    @NonNull
    public String getNativeName() {
        return nativeName;
    }
}
