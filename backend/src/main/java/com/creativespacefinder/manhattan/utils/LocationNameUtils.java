package com.creativespacefinder.manhattan.utils;

public class LocationNameUtils {

    public static String shortenLocationName(String originalName) {
        if (originalName == null || originalName.length() <= 25) {
            return originalName;
        }

        String shortened = originalName;

        // Handle duplicated plaza names (data quality issue)
        shortened = removeDuplicatedPlazaNames(shortened);

        // Handle named plazas with street intersections
        if (shortened.contains(": ") && (shortened.contains("Plaza") || shortened.contains("Square"))) {
            shortened = shortenNamedPlaza(shortened);
        }

        // Handle street intersections with "between"
        if (shortened.contains(" between ")) {
            shortened = shortenStreetIntersection(shortened);
        }

        // Apply general abbreviations
        shortened = applyGeneralAbbreviations(shortened);

        // Smart length management - cut at natural boundaries
        shortened = smartTruncate(shortened, 28);

        return shortened;
    }

    private static String smartTruncate(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }

        // Try to cut at natural breakpoints in order of preference
        String[] breakpoints = {": ", " - ", " (", " between ", " and ", " "};

        for (String breakpoint : breakpoints) {
            int lastBreakpoint = text.lastIndexOf(breakpoint, maxLength);
            if (lastBreakpoint > 15) { // Don't cut too early
                String truncated = text.substring(0, lastBreakpoint);
                // Clean up any hanging punctuation
                return cleanHangingPunctuation(truncated);
            }
        }

        // Last resort: cut at word boundary without "..."
        int lastSpace = text.lastIndexOf(" ", maxLength);
        if (lastSpace > 15) {
            String truncated = text.substring(0, lastSpace);
            return cleanHangingPunctuation(truncated);
        }

        // If no good breakpoint found, just truncate cleanly
        String truncated = text.substring(0, maxLength);
        return cleanHangingPunctuation(truncated);
    }

    private static String cleanHangingPunctuation(String text) {
        // Remove hanging geographical suffixes first
        text = text.replaceAll(",\\s*New$", "")
                .replaceAll(",\\s*North$", "")
                .replaceAll(",\\s*South$", "")
                .replaceAll(",\\s*East$", "")
                .replaceAll(",\\s*West$", "")
                .replaceAll(",\\s*Manhattan$", "")
                .replaceAll(",\\s*NY$", "")
                .replaceAll(",\\s*Brooklyn$", "")
                .replaceAll(",\\s*Queens$", "");

        // Remove hanging punctuation at the end
        text = text.replaceAll("[(:,\\-\\s]++$", "");

        // Check for unmatched opening parentheses and remove them
        long openParens = text.chars().filter(ch -> ch == '(').count();
        long closeParens = text.chars().filter(ch -> ch == ')').count();

        // If we have unmatched opening parentheses, remove the last one(s)
        while (openParens > closeParens && text.contains("(")) {
            int lastOpenParen = text.lastIndexOf("(");
            text = text.substring(0, lastOpenParen).trim();
            openParens = text.chars().filter(ch -> ch == '(').count();
            closeParens = text.chars().filter(ch -> ch == ')').count();
        }

        return text.trim();
    }

    private static String removeDuplicatedPlazaNames(String name) {
        if (name.contains("Plaza (") && name.contains("): ") && name.contains("Plaza (")) {
            int firstPlazaEnd = name.indexOf("): ");
            if (firstPlazaEnd > 0) {
                String beforeDuplication = name.substring(0, firstPlazaEnd + 1);
                String afterDuplication = name.substring(firstPlazaEnd + 3);

                String[] parts = afterDuplication.split(" between ", 2);
                if (parts.length == 2) {
                    return beforeDuplication + " " + parts[1];
                }
            }
        }
        return name;
    }

    private static String shortenNamedPlaza(String name) {
        String[] mainParts = name.split(": ", 2);
        if (mainParts.length == 2) {
            String plazaName = mainParts[0];
            String intersection = mainParts[1];

            // Shorten plaza name
            plazaName = plazaName.replaceAll("Pedestrian Plaza", "Plaza")
                    .replaceAll(" Plaza Plaza", " Plaza")
                    .replaceAll("\\(Herald Square.*?\\)", "(Herald Sq)")
                    .replaceAll("Pershing Square Plaza", "Pershing Sq");

            // If there's a street intersection after the plaza name, shorten it
            if (intersection.contains(" between ")) {
                intersection = shortenStreetIntersection(intersection);
            }

            return plazaName + ": " + intersection;
        }
        return name;
    }

    private static String shortenStreetIntersection(String streetName) {
        String[] parts = streetName.split(" between ", 2);
        if (parts.length != 2) return streetName;

        String mainStreet = parts[0].trim();
        String intersectionPart = parts[1].trim();

        // Shorten the main street name
        mainStreet = shortenStreetName(mainStreet);

        // Handle the intersection part with aggressive abbreviation
        String shortenedIntersection = shortenIntersectionDescription(intersectionPart);

        // Build result
        return mainStreet + " (" + shortenedIntersection + ")";
    }

    private static String shortenStreetName(String streetName) {
        return streetName
                // Aggressive abbreviation for very long boulevard names
                .replaceAll("\\bADAM CLAYTON POWELL JR BOULEVARD\\b", "ACP Jr Blvd")
                .replaceAll("\\bADAM CLAYTON POWELL BOULEVARD\\b", "ACP Blvd")
                .replaceAll("\\bFREDERICK DOUGLASS BOULEVARD\\b", "F Douglass Blvd")
                .replaceAll("\\bFREDERICK DOUGLAS BOULEVARD\\b", "F Douglas Blvd")
                .replaceAll("\\bFREDRICK DOUGLASS BOULEVARD\\b", "F Douglass Blvd")
                .replaceAll("\\bFREDRICK DOUGLAS BOULEVARD\\b", "F Douglas Blvd")
                .replaceAll("\\bFRED DOUGLAS BOULEVARD\\b", "F Douglas Blvd")
                .replaceAll("\\bMALCOLM X BOULEVARD\\b", "Malcolm X Blvd")
                .replaceAll("\\bST NICHOLAS AVENUE\\b", "St Nicholas Ave")
                .replaceAll("\\bSAINT NICHOLAS AVENUE\\b", "St Nicholas Ave")
                .replaceAll("\\bSAINT NICHOLAS TERRACE\\b", "St Nicholas Ter")
                .replaceAll("\\bFORT WASHINGTON AVENUE\\b", "Ft Washington Ave")
                .replaceAll("\\bFT WASHINGTON AVENUE\\b", "Ft Washington Ave")
                .replaceAll("\\bWASHINGTON SQUARE NORTH\\b", "Washington Sq N")
                .replaceAll("\\bWASHINGTON SQUARE SOUTH\\b", "Washington Sq S")
                .replaceAll("\\bWASHINGTON SQUARE WEST\\b", "Washington Sq W")
                .replaceAll("\\bAVENUE OF THE AMERICAS\\b", "6th Ave")
                .replaceAll("\\bHARLEM RIVER DRIVE\\b", "Harlem River Dr")
                .replaceAll("\\bMOUNT MORRIS PARK WEST\\b", "Mt Morris Pk W")
                .replaceAll("\\bMT MORRIS PARK WEST\\b", "Mt Morris Pk W")
                .replaceAll("\\bDUKE ELLINGTON CIRCLE\\b", "Duke Ellington Cir")
                .replaceAll("\\bMARGARET CORBIN PLAZA\\b", "Margaret Corbin Plz")
                // Handle Central Park variations
                .replaceAll("\\bCENTRAL PARK NORTH\\b", "Central Park N")
                .replaceAll("\\bCENTRAL PARK SOUTH\\b", "Central Park S")
                .replaceAll("\\bCENTRAL PARK WEST\\b", "Central Park W")
                .replaceAll("\\bCENTRAL PARK EAST\\b", "Central Park E")
                // Remove directional prefixes for numbered streets to save space
                .replaceAll("\\b(EAST|WEST|NORTH|SOUTH)\\s+", "")
                // Standard abbreviations
                .replaceAll("\\bAVENUE\\b", "Ave")
                .replaceAll("\\bSTREET\\b", "St")
                .replaceAll("\\bBOULEVARD\\b", "Blvd")
                .replaceAll("\\bPLAZA\\b", "Plz")
                .replaceAll("\\bSQUARE\\b", "Sq")
                .replaceAll("\\bPLACE\\b", "Pl")
                .replaceAll("\\bTERRACE\\b", "Ter");
    }

    private static String shortenIntersectionDescription(String intersection) {
        String[] streets = intersection.split("\\s++and\\s++", 2);
        if (streets.length == 2) {
            String street1 = shortenStreetName(streets[0].trim());
            String street2 = shortenStreetName(streets[1].trim());

            // Try to find number ranges for same street types
            String range = extractNumberRange(street1, street2);
            if (range != null) {
                return range;
            }

            // For very long avenue names, use even more aggressive abbreviation
            street1 = street1.replaceAll("\\bACP Jr Blvd\\b", "ACP")
                    .replaceAll("\\bF Douglass Blvd\\b", "FD")
                    .replaceAll("\\bF Douglas Blvd\\b", "FD")
                    .replaceAll("\\bMalcolm X Blvd\\b", "MX");

            street2 = street2.replaceAll("\\bACP Jr Blvd\\b", "ACP")
                    .replaceAll("\\bF Douglass Blvd\\b", "FD")
                    .replaceAll("\\bF Douglas Blvd\\b", "FD")
                    .replaceAll("\\bMalcolm X Blvd\\b", "MX");

            return street1 + " & " + street2;
        }

        return shortenStreetName(intersection);
    }

    private static String extractNumberRange(String street1, String street2) {
        java.util.regex.Pattern numberPattern = java.util.regex.Pattern.compile("(\\d+)\\s*+(St|Ave|Blvd)");
        java.util.regex.Matcher matcher1 = numberPattern.matcher(street1);
        java.util.regex.Matcher matcher2 = numberPattern.matcher(street2);

        if (matcher1.find() && matcher2.find()) {
            String num1 = matcher1.group(1);
            String type1 = matcher1.group(2);
            String num2 = matcher2.group(1);
            String type2 = matcher2.group(2);

            if (type1.equals(type2)) {
                return num1 + "-" + num2 + " " + type1;
            }
        }

        return null;
    }

    private static String applyGeneralAbbreviations(String name) {
        return name.replaceAll("\\bPlayground\\b", "Park")
                .replaceAll("\\bBrigadier General\\b", "Gen")
                .replaceAll("\\bCharles Young Playground\\b", "C Young Park");
    }
}
