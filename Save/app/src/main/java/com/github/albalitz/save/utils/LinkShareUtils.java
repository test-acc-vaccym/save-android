package com.github.albalitz.save.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by albalitz on 11/3/17.
 */

public class LinkShareUtils {

    // stolen from https://stackoverflow.com/questions/3809401/what-is-a-good-regular-expression-to-match-a-url#3809435
    private static final String URL_REGEX = "(https?|ftp|mailto|file)://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_+.~#?&/=]*)";
    private static final String PATTERN_STRING = "(.+)?(" + URL_REGEX + ")(.+)?";
    private static final int PATTERN_GROUP_URL = 2;
    private static final int PATTERN_GROUP_ANNOTATION_BEFORE = 1;
    private static final int PATTERN_GROUP_ANNOTATION_AFTER = 6;

    /**
     * Extract the URL from a given string.
     * This is used when sharing something from another app,
     * since some apps may include a page title or description before and/or after the URL.
     */
    public static String extractUrl(String s) {
        Pattern pattern = Pattern.compile(PATTERN_STRING);
        Matcher matcher = pattern.matcher(s);
        while (matcher.find()) {
            String match = matcher.group(PATTERN_GROUP_URL);
            if (match != null) {
                return match;
            }
        }
        return "";
    }

    /**
     * Extract the annotation from a given string.
     * This is used when sharing something from another app,
     * since some apps may include a page title or description before and/or after the URL.
     */
    public static String extractAnnotation(String s) {
        Pattern pattern = Pattern.compile(PATTERN_STRING);
        Matcher matcher = pattern.matcher(s);

        StringBuilder annotationsBefore = new StringBuilder();
        StringBuilder annotationsAfter = new StringBuilder();
        while (matcher.find()) {
            String matchBefore = matcher.group(PATTERN_GROUP_ANNOTATION_BEFORE);
            if (matchBefore != null) {
                annotationsBefore.append(" ").append(matchBefore);
            }

            String matchAfter = matcher.group(PATTERN_GROUP_ANNOTATION_AFTER);
            if (matchAfter != null) {
                annotationsAfter.append(" ").append(matchAfter);
            }
        }

        StringBuilder annotations = new StringBuilder();
        annotations.append(new StringBuilder(annotationsBefore.toString().replaceAll("- *$", "")));
        annotations.append(new StringBuilder(annotationsAfter.toString().replaceAll("^ *-", "")));
        return annotations.toString().trim();
    }
}
