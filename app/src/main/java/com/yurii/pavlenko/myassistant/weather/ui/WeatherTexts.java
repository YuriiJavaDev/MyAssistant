package com.yurii.pavlenko.myassistant.weather.ui;

import androidx.annotation.StringRes;

import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.weather.logic.MoonPhase;
import com.yurii.pavlenko.myassistant.weather.logic.WeatherDescription;
import com.yurii.pavlenko.myassistant.weather.viewmodel.WeatherState;

/** The wording of the language-neutral weather values. */
final class WeatherTexts {

    private WeatherTexts() {
    }

    @StringRes
    static int description(WeatherDescription description) {
        switch (description) {
            case CLEAR_SKY: return R.string.weather_description_clear_sky;
            case MAINLY_CLEAR: return R.string.weather_description_mainly_clear;
            case PARTLY_CLOUDY: return R.string.weather_description_partly_cloudy;
            case OVERCAST: return R.string.weather_description_overcast;
            case FOG: return R.string.weather_description_fog;
            case RIME_FOG: return R.string.weather_description_rime_fog;
            case LIGHT_DRIZZLE: return R.string.weather_description_light_drizzle;
            case DRIZZLE: return R.string.weather_description_drizzle;
            case DENSE_DRIZZLE: return R.string.weather_description_dense_drizzle;
            case LIGHT_FREEZING_DRIZZLE: return R.string.weather_description_light_freezing_drizzle;
            case FREEZING_DRIZZLE: return R.string.weather_description_freezing_drizzle;
            case SLIGHT_RAIN: return R.string.weather_description_slight_rain;
            case RAIN: return R.string.weather_description_rain;
            case HEAVY_RAIN: return R.string.weather_description_heavy_rain;
            case LIGHT_FREEZING_RAIN: return R.string.weather_description_light_freezing_rain;
            case FREEZING_RAIN: return R.string.weather_description_freezing_rain;
            case SLIGHT_SNOW: return R.string.weather_description_slight_snow;
            case SNOW: return R.string.weather_description_snow;
            case HEAVY_SNOW: return R.string.weather_description_heavy_snow;
            case SNOW_GRAINS: return R.string.weather_description_snow_grains;
            case LIGHT_RAIN_SHOWERS: return R.string.weather_description_light_rain_showers;
            case RAIN_SHOWERS: return R.string.weather_description_rain_showers;
            case VIOLENT_RAIN_SHOWERS: return R.string.weather_description_violent_rain_showers;
            case LIGHT_SNOW_SHOWERS: return R.string.weather_description_light_snow_showers;
            case HEAVY_SNOW_SHOWERS: return R.string.weather_description_heavy_snow_showers;
            case THUNDERSTORM: return R.string.weather_description_thunderstorm;
            case THUNDERSTORM_WITH_HAIL: return R.string.weather_description_thunderstorm_with_hail;
            case THUNDERSTORM_WITH_HEAVY_HAIL: return R.string.weather_description_thunderstorm_with_heavy_hail;
            case UNKNOWN: return R.string.weather_description_unknown;
            default: throw new IllegalArgumentException(description.name());
        }
    }

    @StringRes
    static int moonPhase(MoonPhase.Phase phase) {
        switch (phase) {
            case NEW_MOON: return R.string.weather_moon_phase_new_moon;
            case WAXING_CRESCENT: return R.string.weather_moon_phase_waxing_crescent;
            case FIRST_QUARTER: return R.string.weather_moon_phase_first_quarter;
            case WAXING_GIBBOUS: return R.string.weather_moon_phase_waxing_gibbous;
            case FULL_MOON: return R.string.weather_moon_phase_full_moon;
            case WANING_GIBBOUS: return R.string.weather_moon_phase_waning_gibbous;
            case LAST_QUARTER: return R.string.weather_moon_phase_last_quarter;
            case WANING_CRESCENT: return R.string.weather_moon_phase_waning_crescent;
            default: throw new IllegalArgumentException(phase.name());
        }
    }

    @StringRes
    static int error(WeatherState.Error error) {
        switch (error) {
            case NO_LOCATION: return R.string.weather_error_no_location;
            case CITY_NOT_FOUND: return R.string.weather_error_city_not_found;
            case NO_NETWORK: return R.string.weather_error_no_network;
            case CHOOSE_CITY: return R.string.weather_error_choose_city;
            default: throw new IllegalArgumentException(error.name());
        }
    }
}
