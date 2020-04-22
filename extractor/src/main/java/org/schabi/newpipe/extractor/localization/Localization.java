package org.schabi.newpipe.extractor.localization;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

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
        return Objects.equals(countryCode, that.countryCode);
    }

    @Override
    public int hashCode() {
        int result = languageCode.hashCode();
        result = 31 * result + (countryCode != null ? countryCode.hashCode() : 0);
        return result;
    }

    /**
     * Converts a three letter language code (ISO 639-2/T) to a Locale
     * in the limit of Java Locale class.
     *
     * @param code a three letter language code
     * @return the Locale corresponding
     */
    public static Locale getLocaleFromThreeLetterCode(@NonNull String code) throws ParsingException {
        String[] languages = Locale.getISOLanguages();
        Map<String, Locale> localeMap = new HashMap<>(languages.length);
        for (String language : languages) {
            final Locale locale = new Locale(language);
            localeMap.put(locale.getISO3Language(), locale);
        }
        if (localeMap.containsKey(code)) {
            return localeMap.get(code);
        } else {
            throw new ParsingException("Could not get Locale from this three letter language code" + code);
        }
    }
}
