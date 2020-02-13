package org.schabi.newpipe.extractor.utils;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.AbbreviationHelper;
import org.schabi.newpipe.extractor.localization.Localization;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.schabi.newpipe.extractor.localization.AbbreviationHelper.abbreviationSubscribersCount;

public class Utils {

    public static final String HTTP = "http://";
    public static final String HTTPS = "https://";
    private static final String regexNumberWithDotOrComma = "([\\d]+([\\.,][\\d]+)?)";
    public static final String regexWhiteSpaces = "(\\s| | | )"; //for some reasons \\s isn't enough, so I added
    //the failing spaces

    private Utils() {
        //no instance
    }

    /**
     * Remove all non-digit characters from a string.<p>
     * Examples:<p>
     * <ul><li>1 234 567 views -&gt; 1234567</li>
     * <li>$31,133.124 -&gt; 31133124</li></ul>
     *
     * @param toRemove string to remove non-digit chars
     * @return a string that contains only digits
     */
    public static String removeNonDigitCharacters(String toRemove) {
        return toRemove.replaceAll("\\D+", "");
    }

    /**
     * <p>Remove a number from a string.</p>
     * <p>Examples:</p>
     * <ul>
     *     <li>"123" -&gt; ""</li>
     *     <li>"1.23K" -&gt; "K"</li>
     *     <li>"1.23 M" -&gt; " M"</li>
     * </ul>
     * Pay attention, it may remove the final dot.
     * eg: "8,93 хил." -> " хил"
     *
     * @param toRemove string to remove a number
     * @return a string that contains only not a number
     */
    public static String removeNumber(String toRemove) {
        return toRemove.replaceAll("[0-9,.]", "");
    }

    /**
     * {@link #removeNonDigitCharacters(String)} but keep dot and comma in between
     *
     * @param string
     * @return a string that only has a number, with maybe a dot or a comma
     */
    public static String getNumber(String string) {
        String notNumber = string.replaceAll(regexNumberWithDotOrComma, "");
        return string.replaceAll(notNumber, "");
    }

    /**
     * <p>Convert a mixed number word to a long.</p>
     * <p>Examples:</p>
     * <ul>
     *     <li>123 -&gt; 123</li>
     *     <li>1.23K -&gt; 1230</li>
     *     <li>1.23M -&gt; 1230000</li>
     * </ul>
     *
     * @param numberWord string to be converted to a long
     * @return a long
     * @throws NumberFormatException
     * @throws ParsingException
     */
    public static long mixedNumberWordToLong(String numberWord) throws NumberFormatException, ParsingException {
        String multiplier = "";
        try {
            multiplier = Parser.matchGroup("[\\d]+([\\.,][\\d]+)?([KMBkmb万লক億])+", numberWord, 2);
        } catch (ParsingException ignored) {
        }
        double count = Double.parseDouble(Parser.matchGroup1(regexNumberWithDotOrComma, numberWord)
                .replace(",", "."));
        switch (multiplier.toUpperCase()) {
            case "K":
                return (long) (count * 1000);
            case "万": //10K, used by east-asian languages
                return (long) (count * 10_000);
            case "ল": //100K, used by indo-arabic languages
                return (long) (count * 100_000);
            case "M":
                return (long) (count * 1_000_000);
            case "ক": //10M, used by indo-arabic languages
                return (long) (count * 10_000_000);
            case "億": //100M, used by east-asian languages
                return (long) (count * 100_000_000);
            case "B":
                return (long) (count * 1_000_000_000);
            default:
                return (long) (count);
        }
    }

    public static String removeWhiteSpaces(String s) {
        return s.replaceAll(regexWhiteSpaces, "");
    }

    /**
     * Does the same as {@link #mixedNumberWordToLong(String)}, but for the 80 languages supported by YouTube.
     *
     * @param numberWord string to be converted to a long
     * @param loc        a {@link Localization}
     * @return a long
     * @throws ParsingException
     */
    public static long mixedNumberWordToLong(String numberWord, Localization loc) throws ParsingException {
        String langCode = loc.getLanguageCode();

        if (langCode.equals("en")) {
            return mixedNumberWordToLong(numberWord);
            //next two: the number has a dot or a space in it, but is a full number (for thousands only)
        } else if (langCode.equals("eu") && numberWord.split(regexWhiteSpaces).length == 2) {
            return mixedNumberWordToLong(removeNonDigitCharacters(numberWord));
        } else if (langCode.equals("sv") && numberWord.matches("\\d+" + regexWhiteSpaces + "\\d+.*")) {
            return mixedNumberWordToLong(removeNonDigitCharacters(numberWord));
        }
        //special case for language written right to left
        else if (langCode.equals("sw") && numberWord.contains("elfu")) {
            numberWord = moveAtRight("elfu", numberWord);
        }

        try { //special case where it gives a number directly for some languages, or with a dot or a comma
            String maybeAlreadyNumber = removeWhiteSpaces(numberWord).replaceAll("([.,])", "");
            return Long.parseLong(maybeAlreadyNumber);
        } catch (NumberFormatException ignored) {
            //the number had an abbreviation, so it will be handled below
        }

        String[] abbreviation = getAbbreviation(numberWord, loc);
        //because it matches "something number something"
        if (AbbreviationHelper.typeTwo.contains(loc)) {
            String number = numberWord.replaceAll("[^0-9.,]", "");
            if (number.endsWith(".")) number = number.substring(0, number.length() - 1);
            return mixedNumberWordToLong(number + abbreviation[0]);
        }
        //the thousand abbreviation for catalan / portugal are the same as the million abbreviation in many more languages
        else if ((langCode.equals("pt")) && numberWord.contains("mil") || langCode.equals("ca") && numberWord.contains("m")) {
            abbreviation[0] = "K";
        }

        if (abbreviation[1].equals("1")) {
            return mixedNumberWordToLong(getNumber(numberWord) + abbreviation[0]);
        } else if (abbreviation[1].equals("2")) {
            return mixedNumberWordToLong(numberWord.replace(removeNumber(numberWord), abbreviation[0]));
        }
        throw new ParsingException("Could not extract number from \"" + numberWord + "\", " + loc.toString());
    }

    public static String moveAtRight(String toMove, String whole) {
        whole = whole.replace(toMove, "");
        whole += toMove;
        return whole;
    }

    public static String[] getAbbreviation(String numberWord, Localization loc) throws ParsingException {
        //gets the abbreviation out of a mixed number string
        //and return {equivalentAbbreviation, type};

        String abbreviation;
        String nonDigitCharacters = removeNumber(numberWord);
        String[] words = nonDigitCharacters.split(regexWhiteSpaces);
        for (String word : words) {
            abbreviation = abbreviationSubscribersCount.get(word);
            if (abbreviation != null) {
                return new String[]{abbreviation, "1"};
            }
        }
        //some languages like japan don't use space everywhere but single characters
        for (int i = 0; i < nonDigitCharacters.length(); i++) {
            abbreviation = abbreviationSubscribersCount.get(String.valueOf(nonDigitCharacters.charAt(i)));
            if (abbreviation != null) {
                return new String[]{abbreviation, "2"};
            }
        }
        throw new ParsingException("Could not extract number from \"" + numberWord + "\", " + loc.toString());
    }

    /**
     * Check if the url matches the pattern.
     *
     * @param pattern the pattern that will be used to check the url
     * @param url     the url to be tested
     */
    public static void checkUrl(String pattern, String url) throws ParsingException {
        if (isNullOrEmpty(url)) {
            throw new IllegalArgumentException("Url can't be null or empty");
        }

        if (!Parser.isMatch(pattern, url.toLowerCase())) {
            throw new ParsingException("Url don't match the pattern");
        }
    }

    public static void printErrors(List<Throwable> errors) {
        for (Throwable e : errors) {
            e.printStackTrace();
            System.err.println("----------------");
        }
    }

    public static String replaceHttpWithHttps(final String url) {
        if (url == null) return null;

        if (!url.isEmpty() && url.startsWith(HTTP)) {
            return HTTPS + url.substring(HTTP.length());
        }
        return url;
    }

    /**
     * get the value of a URL-query by name.
     * if a url-query is give multiple times, only the value of the first query is returned
     *
     * @param url           the url to be used
     * @param parameterName the pattern that will be used to check the url
     * @return a string that contains the value of the query parameter or null if nothing was found
     */
    public static String getQueryValue(URL url, String parameterName) {
        String urlQuery = url.getQuery();

        if (urlQuery != null) {
            for (String param : urlQuery.split("&")) {
                String[] params = param.split("=", 2);

                String query;
                try {
                    query = URLDecoder.decode(params[0], "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    System.err.println("Cannot decode string with UTF-8. using the string without decoding");
                    e.printStackTrace();
                    query = params[0];
                }

                if (query.equals(parameterName)) {
                    try {
                        return URLDecoder.decode(params[1], "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        System.err.println("Cannot decode string with UTF-8. using the string without decoding");
                        e.printStackTrace();
                        return params[1];
                    }
                }
            }
        }

        return null;
    }

    /**
     * converts a string to a URL-Object.
     * defaults to HTTP if no protocol is given
     *
     * @param url the string to be converted to a URL-Object
     * @return a URL-Object containing the url
     */
    public static URL stringToURL(String url) throws MalformedURLException {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            // if no protocol is given try prepending "https://"
            if (e.getMessage().equals("no protocol: " + url)) {
                return new URL(HTTPS + url);
            }

            throw e;
        }
    }

    public static boolean isHTTP(URL url) {
        // make sure its http or https
        String protocol = url.getProtocol();
        if (!protocol.equals("http") && !protocol.equals("https")) {
            return false;
        }

        boolean usesDefaultPort = url.getPort() == url.getDefaultPort();
        boolean setsNoPort = url.getPort() == -1;

        return setsNoPort || usesDefaultPort;
    }

    public static String removeUTF8BOM(String s) {
        if (s.startsWith("\uFEFF")) {
            s = s.substring(1);
        }
        if (s.endsWith("\uFEFF")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    public static String getBaseUrl(String url) throws ParsingException {
        URL uri;
        try {
            uri = stringToURL(url);
        } catch (MalformedURLException e) {
            throw new ParsingException("Malformed url: " + url, e);
        }
        return uri.getProtocol() + "://" + uri.getAuthority();
    }

    public static boolean isNullOrEmpty(final String str) {
        return str == null || str.isEmpty();
    }

    // can be used for JsonArrays
    public static boolean isNullOrEmpty(final Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    // can be used for JsonObjects
    public static boolean isNullOrEmpty(final Map map) {
        return map == null || map.isEmpty();
    }
}