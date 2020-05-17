package org.schabi.newpipe.extractor.services.youtube;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import org.junit.Ignore;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.localization.Localization;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeChannelExtractor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getJsonResponse;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getTextFromObject;
import static org.schabi.newpipe.extractor.utils.Utils.mixedNumberWordToLong;

/**
 * A class that tests abbreviations and subscriber counts for all the languages YouTube supports.
 * Create public JsonObject getInitialData() {return this.initialData;} in YoutubeChannelExtractor to test
 */
@Ignore("Should be ran manually from time to time, as it's too time consuming.")
public class YoutubeSubscriberTest {

    private static final String guideBuilderUrl = "https://www.youtube.com/feed/guide_builder?pbj=1";
    private static final int PAUSE_DURATION = 250;

    @Test
    // only check if extracting abbreviation is supported, it doesn't compare with english.
    // But it tests 112 times for every supported languages
    public void testAllAbbreviations() throws IOException, ExtractionException, InterruptedException {
        boolean verbose = true; //change this for more prints

        List<String> failed = new ArrayList<>();
        int totalCount = 0;
        for (Localization loc : YouTube.getSupportedLocalizations()) {
            System.out.println(totalCount + ";" + loc.toString());
            try {
                totalCount += runTest(loc, verbose);
                if (verbose) System.out.println();
            } catch (AssertionError | ParsingException e) {
                e.printStackTrace();
                failed.add(loc.getLocalizationCode());
            }
            Thread.sleep(PAUSE_DURATION);
        }
        if (!failed.isEmpty()) {
            throw new RuntimeException("Tests failed for these localizations:\n" +
                    failed.toString());
        } else {
            System.out.println("\nThe test is successful!!!");
        }
    }

    @Test
    public void testAbbreviationsOneLanguage() throws IOException, ExtractionException {
        runTest(new Localization("fr"), true);
    }

    @Test
    public void testFailedLanguages() throws ParsingException, ReCaptchaException, IOException {
        // edit the list below if you want to test some languages but not all
        List<Localization> failed = Localization.listFrom(
                "km", "ko", "lo", "my", "sw", "th", "uk"
        );
        for (Localization loc : failed) {
            try {
                runTest(loc, true);
            } catch (AssertionError | ParsingException ignored) {
                System.err.println("this one particularly failed, " + loc);
            }
        }
    }

    private int runTest(Localization loc, boolean verbose) throws ParsingException, IOException {
        int totalCount = 0;
        try {
            JsonObject guide = getGuideBuilderResponse(loc);
            JsonArray content = guide.getObject("contents").getObject("twoColumnBrowseResultsRenderer").getArray("tabs")
                    .getObject(0).getObject("tabRenderer").getObject("content").getObject("sectionListRenderer")
                    .getArray("contents");

            JsonArray items;
            for (Object object : content) {
                items = ((JsonObject) object).getObject("itemSectionRenderer").getArray("contents")
                        .getObject(0).getObject("shelfRenderer").getObject("content").getObject("horizontalListRenderer")
                        .getArray("items");
                for (Object obj : items) {
                    JsonObject renderer = ((JsonObject) obj).getObject("gridChannelRenderer");
                    String wordCount = getTextFromObject(renderer.getObject("subscriberCountText"));
                    String channelName = renderer.getObject("title").getString("simpleText");
                    String url = "https://www.youtube.com" + renderer.getObject("navigationEndpoint")
                            .getObject("commandMetadata").getObject("webCommandMetadata").getString("url");
                    if (verbose)
                        System.out.println(channelName + ":\"" + wordCount + "\" --> " + mixedNumberWordToLong(wordCount, loc) + ";url= " + url);
                    try {
                        assertTrue(mixedNumberWordToLong(wordCount, loc) >= 1000);
                    } catch (AssertionError e) {
                        throw new AssertionError("subCount < 1000, " + loc.toString(), e);
                    }
                    totalCount++;
                }
            }
        } catch (NullPointerException | ExtractionException npe) {
            //--> failed request
        }
        return totalCount;
    }

    private JsonObject getGuideBuilderResponse(Localization loc) throws ExtractionException, IOException {
        return getJsonResponse(guideBuilderUrl, loc).getObject(1).getObject("response");
    }

    /**
     * Asserts that the subscriber count gathered by the extractor in every supported languages matches the english count
     *
     * @param channelUrl the channel to test
     * @param verbose    whether we should display more info. REQUIRES getInitialData() in YoutubeChannelExtractor.
     * @throws ExtractionException
     * @throws IOException
     * @throws InterruptedException
     */
    public static void assertEqualsWithEnglish(String channelUrl, boolean verbose) throws ExtractionException, IOException, InterruptedException {
        Set<Localization> failedLocalisations = new HashSet<>();

        NewPipe.init(DownloaderTestImpl.getInstance(), Localization.DEFAULT);
        YoutubeChannelExtractor extractorEnglish = (YoutubeChannelExtractor) YouTube
                .getChannelExtractor(channelUrl);
        extractorEnglish.fetchPage();
        long englishSubCount = extractorEnglish.getSubscriberCount();
        Localization localization;
        String toDisplay;

        for (int z = 0; z < YouTube.getSupportedLocalizations().size(); z++) {
            localization = YouTube.getSupportedLocalizations().get(z);
            toDisplay = "Current localization: " + localization.toString();
            NewPipe.init(DownloaderTestImpl.getInstance(), localization);
            YoutubeChannelExtractor extractor = (YoutubeChannelExtractor) YouTube
                    .getChannelExtractor(channelUrl);
            extractor.fetchPage();

            long subscriberCount = -2;
            try {
                subscriberCount = extractor.getSubscriberCount();
            } catch (ParsingException e) {
                System.err.println(e.getMessage());
                failedLocalisations.add(localization);
            }

            if (verbose) {
                //displays the gathered text from youtube, uncomment and create getinitialdata temporarily for it
//                toDisplay += ": \"" + getTextFromObject(extractor.getInitialData().getObject("header")
//                        .getObject("c4TabbedHeaderRenderer").getObject("subscriberCountText")) + "\"";
            }
            System.out.println(toDisplay);
            if (subscriberCount == -1) {
                System.err.println("Subscriber count for " + localization.toString() + " was -1;\n" +
                        "If the channel doesn't have the subscribers disabled, it was probably a failed request");
            } else {
                try {
                    assertEquals("Language that failed:" + localization.toString() + ".\nWe", englishSubCount, subscriberCount, 10);
                } catch (AssertionError e) {
                    System.err.println(e.getMessage());
                    failedLocalisations.add(localization);
                }
            }
            Thread.sleep(PAUSE_DURATION);
        }

        System.err.println("failed Localisations: " + failedLocalisations);
    }

    public static void assertEqualsWithEnglish(String channelUrl, Localization loc) throws ExtractionException, IOException {
        //for only one language
        NewPipe.init(DownloaderTestImpl.getInstance(), new Localization("en"));
        YoutubeChannelExtractor extractorEnglish = (YoutubeChannelExtractor) YouTube
                .getChannelExtractor(channelUrl);
        extractorEnglish.fetchPage();
        long englishSubCount = extractorEnglish.getSubscriberCount();

        NewPipe.init(DownloaderTestImpl.getInstance(), loc);
        YoutubeChannelExtractor extractor = (YoutubeChannelExtractor) YouTube
                .getChannelExtractor(channelUrl);
        extractor.fetchPage();
        //displays the gathered text from youtube, uncomment and create getinitialdata temporarily for it
//        System.out.println(getTextFromObject(extractor.getInitialData().getObject("header").getObject("c4TabbedHeaderRenderer").getObject("subscriberCountText")));
        assertEquals(englishSubCount, extractor.getSubscriberCount());
    }

    public static void assertEqualsWithEnglish(String channelUrl, String languageCode) throws ExtractionException, IOException {
        assertEqualsWithEnglish(channelUrl, new Localization(languageCode));

    }

    @Test
    public void testDisabled() throws IOException, ExtractionException, InterruptedException {
        //every languages should give -1
        Localization localization;
        for (int z = 0; z < YouTube.getSupportedLocalizations().size(); z++) {
            localization = YouTube.getSupportedLocalizations().get(z);
            System.out.println("Current localization: " + localization);
            NewPipe.init(DownloaderTestImpl.getInstance(), localization);
            YoutubeChannelExtractor extractor = (YoutubeChannelExtractor) YouTube
                    .getChannelExtractor("https://www.youtube.com/user/EminemVEVO/");
            extractor.fetchPage();

            long subscriberCount = extractor.getSubscriberCount();
            assertEquals("Language that failed: " + localization.toString() + "\n We ", -1, subscriberCount);
            Thread.sleep(PAUSE_DURATION);
        }
    }

    // don't use invidious links, it takes more time and the tests have greater chances to fail
    private static final String highestSubsUrl = "https://www.youtube.com/user/tseries";
    private static final String selenaGomezUrl = "https://www.youtube.com/channel/UCPNxhDvTcytIdvwXWAm43cA";
    private static final String franjoUrl = "https://www.youtube.com/channel/UC53gfTiWvslLPNuoDcoxmVg";

    public static void assertEqualsWithEnglish(String channelUrl) throws InterruptedException, ExtractionException, IOException {
        assertEqualsWithEnglish(channelUrl, true); //change verbose to false if you don't want
    }

    @Test
    public void testOneLanguageExtractor() throws ExtractionException, IOException {
        assertEqualsWithEnglish(franjoUrl, "pt");
    }

    @Test
    public void testHighestSubsOnYoutube() throws ExtractionException, IOException, InterruptedException {
        assertEqualsWithEnglish(highestSubsUrl);
    }

    @Test
    public void testSelenaGomez() throws InterruptedException, ExtractionException, IOException {
        assertEqualsWithEnglish(selenaGomezUrl);
    }

    @Test
    public void testFranjo() throws InterruptedException, ExtractionException, IOException {
        assertEqualsWithEnglish(franjoUrl);
    }

    @Test
    public void testLowSubscriberCount() throws InterruptedException, ExtractionException, IOException {
        assertEqualsWithEnglish("https://youtube.com/channel/UCUaQMQS9lY5lit3vurpXQ6w");
    }
}
