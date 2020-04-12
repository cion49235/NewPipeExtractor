package org.schabi.newpipe.extractor.localization;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;

public class Localization implements Serializable {
    public static final Localization DEFAULT = new Localization("en", "GB");

    @Nonnull
    private final String languageCode;
    @Nullable
    private final String countryCode;

    /**
     * @param localizationCodeList a list of localization code, formatted like {@link #getLocalizationCode()}
     */
    public static List<Localization> listFrom(String... localizationCodeList) {
        final List<Localization> toReturn = new ArrayList<>();
        for (String localizationCode : localizationCodeList) {
            toReturn.add(fromLocalizationCode(localizationCode));
        }
        return Collections.unmodifiableList(toReturn);
    }

    /**
     * @param localizationCode a localization code, formatted like {@link #getLocalizationCode()}
     */
    public static Localization fromLocalizationCode(String localizationCode) {
        final int indexSeparator = localizationCode.indexOf("-");

        final String languageCode, countryCode;
        if (indexSeparator != -1) {
            languageCode = localizationCode.substring(0, indexSeparator);
            countryCode = localizationCode.substring(indexSeparator + 1);
        } else {
            languageCode = localizationCode;
            countryCode = null;
        }

        return new Localization(languageCode, countryCode);
    }

    public Localization(@Nonnull String languageCode, @Nullable String countryCode) {
        this.languageCode = languageCode;
        this.countryCode = countryCode;
    }

    public Localization(@Nonnull String languageCode) {
        this(languageCode, null);
    }

    public String getLanguageCode() {
        return languageCode;
    }

    @Nonnull
    public String getCountryCode() {
        return countryCode == null ? "" : countryCode;
    }

    public Locale asLocale() {
        return new Locale(getLanguageCode(), getCountryCode());
    }

    public static Localization fromLocale(@Nonnull Locale locale) {
        return new Localization(locale.getLanguage(), locale.getCountry());
    }

    /**
     * Return a formatted string in the form of: {@code language-Country}, or
     * just {@code language} if country is {@code null}.
     */
    public String getLocalizationCode() {
        return languageCode + (countryCode == null ? "" : "-" + countryCode);
    }

    @Override
    public String toString() {
        return "Localization[" + getLocalizationCode() + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Localization)) return false;

        Localization that = (Localization) o;

        if (!languageCode.equals(that.languageCode)) return false;
        return countryCode != null ? countryCode.equals(that.countryCode) : that.countryCode == null;
    }

    @Override
    public int hashCode() {
        int result = languageCode.hashCode();
        result = 31 * result + (countryCode != null ? countryCode.hashCode() : 0);
        return result;
    }

    /**
     * Converts a three letter language code (ISO 639-3) to a Locale
     * in the limit of Java Locale class.
     * It can also handle 639-2/T but may return a wrong locale.
     * from https://stackoverflow.com/a/674122/12680950
     * <p>
     * It should not be used with 639-2/B, since java already handle it.
     * (Just use new Locale("an ISO 639-2/B code")).
     *
     * @param code a three letter language code
     * @return the Locale corresponding
     */
    public static Locale getLocaleFromThreeLetterCode(String code) {
        String[] languages = Locale.getISOLanguages();
        Map<String, Locale> localeMap = new HashMap<>(languages.length);
        for (String language : languages) {
            Locale locale = new Locale(language);
            localeMap.put(locale.getISO3Language(), locale);
        }
        if (localeMap.containsKey(code)) {
            return localeMap.get(code);
        } else {
            return new Locale(code.substring(0, 2));
        }
    }
}
