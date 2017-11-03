package com.github.albalitz.save;

import org.junit.Test;

import static org.junit.Assert.*;

import static com.github.albalitz.save.utils.LinkShareUtils.extractAnnotation;
import static com.github.albalitz.save.utils.LinkShareUtils.extractUrl;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class LinkShareUtilsTest {
    @Test
    public void testExtractUrl() throws Exception {
        assertEquals("https://reddit.com", extractUrl("https://reddit.com - The front page of the internet."));
        assertEquals("https://reddit.com", extractUrl("The front page of the internet. - https://reddit.com"));
    }

    @Test
    public void testExtractAnnotation() throws Exception {
        assertEquals("The front page of the internet.", extractAnnotation("https://reddit.com - The front page of the internet."));
        assertEquals("The front page of the internet.", extractAnnotation("The front page of the internet. - https://reddit.com"));
    }
}