package org.schabi.newpipe.extractor.services.youtube.stream;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeStreamExtractor;
import org.schabi.newpipe.extractor.stream.Card;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertIsSecureUrl;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;

public class YoutubeCardsTest {

    public static class pollNvideos {

        private static final String expectedCard1title = "CORONAVIRUS : faut-il vraiment s'inquiéter ?";
        private static final String expectedCard2title = "Immersion au championnat de France de lecture rapide (ils me donnent des conseils)";
        private static final String expectedCard3title = "Trump sera-t-il réelu ?";
        private static YoutubeStreamExtractor extractor;
        private static Card card1;
        private static Card card2;
        private static Card card3;

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (YoutubeStreamExtractor) YouTube
                    .getStreamExtractor("https://www.youtube.com/watch?v=ZCN5oquez9Q");
            extractor.fetchPage();
            List<Card> cards = extractor.getCards();
            card1 = cards.get(0);
            card2 = cards.get(1);
            card3 = cards.get(2);
        }

        @Test
        public void test() throws ParsingException {
            System.out.println(extractor.getCards());
        }

        @Test
        public void testGetCards() throws ParsingException {
            assertEquals(3, extractor.getCards().size());
        }

        @Test
        public void testGetName() {
            assertEquals(expectedCard1title, card1.getTitle());
            assertEquals(expectedCard2title, card2.getTitle());
            assertEquals(expectedCard3title, card3.getTitle());
        }

        @Test
        public void testGetChoices() {
            assertNull(card1.getChoices());
            assertNull(card2.getChoices());
            assertFalse(card3.getChoices().isEmpty());
        }

        @Test
        public void testGetChannel() {
            assertEquals("HugoDécrypte", card1.getChannel());
            assertEquals("HugoDécrypte", card2.getChannel());
            assertNull(card3.getChannel());
        }

        @Test
        public void testGetThumbnailUrl() {
            assertIsSecureUrl(card1.getThumbnailUrl());
            assertIsSecureUrl(card2.getThumbnailUrl());
            assertNull(card3.getThumbnailUrl());
        }

        @Test
        public void testGetLength() {
            assertEquals(411, card1.getLength());
            assertEquals(626, card2.getLength());
            assertEquals(-1, card3.getLength());
        }

        @Test
        public void testGetCount() {
            assertTrue(card1.getCount() >= 413148);
            assertTrue(card2.getCount() >= 929599);
            assertTrue(card3.getCount() >= 11709);
        }

        @Test
        public void testChoice() {
            assertNotNull(card3);
        }

        @Test
        public void testGetUrl() {
            assertEquals("https://www.youtube.com/watch?v=0hvbqSDqoyY", card1.getUrl());
            assertEquals("https://www.youtube.com/watch?v=nDvtI7mSYtA", card2.getUrl());
            assertNull(card3.getUrl());
        }
    }

    public static class noCardsVideo {

        private static YoutubeStreamExtractor extractor;

        @BeforeClass
        public static void setUp() throws ExtractionException, IOException {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (YoutubeStreamExtractor) YouTube
                    .getStreamExtractor("https://www.youtube.com/watch?v=SMRVFIrPevA");
            extractor.fetchPage();
        }

        @Test
        public void testGetCards() throws ParsingException {
            assertNull(extractor.getCards());
        }
    }

    public static class playlistNlink {
        private static YoutubeStreamExtractor extractor;

        @BeforeClass
        public static void setUp() throws ExtractionException, IOException {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (YoutubeStreamExtractor) YouTube
                    .getStreamExtractor("https://www.youtube.com/watch?v=8BQqX3bUqtY");
            extractor.fetchPage();
        }

        @Test
        public void test() throws ParsingException {
            System.out.println(extractor.getCards());
        }

        @Test
        public void testTeaser() throws ParsingException {
            assertEquals("Suggested: GIVE ME FUTURE SOUNDTRACK", extractor.getCards().get(0).getTeaserMessage());
        }

        @Test
        public void testGetUrl() throws ParsingException {
            assertEquals("https://www.youtube.com/watch?v=XRuxxuBVLmY&list=PLMx-oj8tIII3xecvoOhmTeVjSI9tQVteg",
                    extractor.getCards().get(0).getUrl());
            assertEquals("https://itunes.apple.com/us/album/get-it-right-feat-m%C3%B8/1306515320?i=1306515338",
                    extractor.getCards().get(1).getUrl());
            assertEquals("itunes.apple.com", extractor.getCards().get(1).getHost());
        }
    }

    public static class channel {
        private static YoutubeStreamExtractor extractor;
        private static Card card;

        @BeforeClass
        public static void setUp() throws ExtractionException, IOException {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (YoutubeStreamExtractor) YouTube
                    .getStreamExtractor("https://www.youtube.com/watch?v=CZP72J8Osa8");
            extractor.fetchPage();
            card = extractor.getCards().get(1);
        }

        @Test
        public void test() throws ParsingException {
            System.out.println(card);
        }

        @Test
        public void testGetName() throws ParsingException {
            assertIsSecureUrl(card.getThumbnailUrl());
        }

        @Test
        public void testGetUrl() {
            assertIsSecureUrl(card.getUrl());
        }

        @Test
        public void testGetChannel() {
            assertEquals("The Maltbie Bunch", card.getChannel());
        }

        @Test
        public void testGetSubCount() {
            assertTrue(card.getCount() >= 36800);
        }
    }
}
