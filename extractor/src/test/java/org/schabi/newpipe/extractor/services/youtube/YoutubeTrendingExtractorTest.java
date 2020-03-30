package org.schabi.newpipe.extractor.services.youtube;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.services.BaseListExtractorTest;
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeTrendingExtractor;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeTrendingExtractor;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeTrendingLinkHandlerFactory;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;

public class YoutubeTrendingExtractorTest implements BaseListExtractorTest {

    private static YoutubeTrendingExtractor extractor;

    @BeforeClass
    public static void setUp() throws ExtractionException, IOException {
        NewPipe.init(DownloaderTestImpl.getInstance());
        extractor = (YoutubeTrendingExtractor) YouTube.getKioskList().getExtractorById("News", null);
        extractor.fetchPage();
    }

    @Override
    public void testRelatedItems() throws Exception {

    }

    @Override
    public void testMoreRelatedItems() throws Exception {

    }

    @Test
    public void testServiceId() throws Exception {
        assertEquals(YouTube.getServiceId(), extractor.getServiceId());
    }


    @Test
    public void testName() throws Exception {
        System.out.println(extractor.getName());
//        System.out.println(extractor.getUrl());
    }

    @Override
    public void testId() throws Exception {

    }

    @Override
    public void testUrl() throws Exception {

    }

    @Override
    public void testOriginalUrl() throws Exception {

    }
}
