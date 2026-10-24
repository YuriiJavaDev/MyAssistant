package com.yurii.pavlenko.myassistant.weather.logic;

/**
 * Reads the weather symbols of MET Norway, such as "lightrainshowers_day" or "heavysnowandthunder", and
 * gives the matching WMO weather code the rest of the weather screen is built on.
 */
public final class MetSymbol {

    private static final String THUNDER = "andthunder";

    private MetSymbol() {
    }

    /** The symbol without its "_day", "_night" or "_polartwilight" ending. */
    public static String base(String symbol) {
        int cut = symbol.indexOf('_');
        return cut < 0 ? symbol : symbol.substring(0, cut);
    }

    /** True or false when the symbol says whether it is day; null when it does not (such as "cloudy"). */
    public static Boolean isDay(String symbol) {
        if (symbol.endsWith("_night")) {
            return Boolean.FALSE;
        }
        if (symbol.endsWith("_day") || symbol.endsWith("_polartwilight")) {
            return Boolean.TRUE;
        }
        return null;
    }

    /** The closest WMO code; 3 (overcast) for a symbol that is not known. */
    public static int wmoCode(String symbol) {
        String base = base(symbol);
        if (base.contains(THUNDER)) {
            return 95;
        }
        switch (base) {
            case "clearsky": return 0;
            case "fair": return 1;
            case "partlycloudy": return 2;
            case "cloudy": return 3;
            case "fog": return 45;
            case "lightrain": return 61;
            case "rain": return 63;
            case "heavyrain": return 65;
            case "lightrainshowers": return 80;
            case "rainshowers": return 81;
            case "heavyrainshowers": return 82;
            case "lightsleet":
            case "lightsleetshowers": return 66;
            case "sleet":
            case "heavysleet":
            case "sleetshowers":
            case "heavysleetshowers": return 67;
            case "lightsnow": return 71;
            case "snow": return 73;
            case "heavysnow": return 75;
            case "lightsnowshowers": return 85;
            case "snowshowers":
            case "heavysnowshowers": return 86;
            default: return 3;
        }
    }
}
