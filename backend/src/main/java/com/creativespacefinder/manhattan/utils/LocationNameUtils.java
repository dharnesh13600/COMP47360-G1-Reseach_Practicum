package com.creativespacefinder.manhattan.utils;

// References:
// https://docs.oracle.com/javase/8/docs/api/java/lang/String.html
// https://www.regular-expressions.info/java.html
// https://medium.com/@anil.goyal0057/mastering-string-algorithms-in-java-4488d9bff9df 
// https://mammadyahya.medium.com/how-to-design-utility-classes-in-java-15772c5a6e1f
// https://www.geeksforgeeks.org/java/how-to-replace-a-string-using-regex-pattern-in-java/
// https://www.geeksforgeeks.org/java/java-string-manipulation-best-practices-for-clean-code/

public class LocationNameUtils {
    // Leave the location name if it is null or only 25 characters
    public static String shortenLocationName(String originalName) {
        if (originalName == null || originalName.length() <= 25) {
            return originalName;
        }

        String shortened = originalName;

        // Remove duplicated location names with Plaza
        shortened = removeDuplicatedPlazaNames(shortened);

        // Any location with : or Plaza or Square should be shortened
        if (shortened.contains(": ") && (shortened.contains("Plaza") || shortened.contains("Square"))) {
            shortened = shortenNamedPlaza(shortened);
        }

        // Get rid of locations referencing in-between other locations/streets
        if (shortened.contains(" between ")) {
            shortened = shortenStreetIntersection(shortened);
        }

        // Enforce the general abbreviations that I set
        shortened = applyGeneralAbbreviations(shortened);

        // Use my function smartTruncate to shorten the name at a more natural breaking point
        shortened = smartTruncate(shortened, 28);

        return shortened;
    }

    private static String smartTruncate(String text, int maxLength) {
        // If the text is short enough then leave it
        if (text.length() <= maxLength) {
            return text;
        }

        // Slice at natural breaking points of location names
        String[] breakpoints = {": ", " - ", " (", " between ", " and ", " "};

        // Check for the last occurrence of each breaking point within the maxLength limit
        for (String breakpoint : breakpoints) {
            int lastBreakpoint = text.lastIndexOf(breakpoint, maxLength);
            if (lastBreakpoint > 15) { // Don't cut too early
                String truncated = text.substring(0, lastBreakpoint);
                // Clean up any punctation left at the end of the name
                return cleanHangingPunctuation(truncated);
            }
        }

        // If we have no breaking point, find the last space before max (last resort)
        int lastSpace = text.lastIndexOf(" ", maxLength);
        if (lastSpace > 15) {
            String truncated = text.substring(0, lastSpace);
            return cleanHangingPunctuation(truncated);
        }

        // If there is no last then truncate at max
        String truncated = text.substring(0, maxLength);
        return cleanHangingPunctuation(truncated);
    }

    private static String cleanHangingPunctuation(String text) {
        // Remove any of the following ending geographical suffixes
        text = text.replaceAll(",\\s*New$", "")
                .replaceAll(",\\s*North$", "")
                .replaceAll(",\\s*South$", "")
                .replaceAll(",\\s*East$", "")
                .replaceAll(",\\s*West$", "")
                .replaceAll(",\\s*Manhattan$", "")
                .replaceAll(",\\s*NY$", "")
                .replaceAll(",\\s*Brooklyn$", "")
                .replaceAll(",\\s*Queens$", "");

        // Remove any ending punctuation
        text = text.replaceAll("[(:,\\-\\s]++$", "");

        // If there is unmatched opening parentheses, remove them
        long openParens = text.chars().filter(ch -> ch == '(').count();
        long closeParens = text.chars().filter(ch -> ch == ')').count();

        // If we have an unmatched opening parentheses, just get rid of the last one
        while (openParens > closeParens && text.contains("(")) {
            int lastOpenParen = text.lastIndexOf("(");
            text = text.substring(0, lastOpenParen).trim();
            openParens = text.chars().filter(ch -> ch == '(').count();
            closeParens = text.chars().filter(ch -> ch == ')').count();
        }

        return text.trim();
    }

    // This function removes the duplication of Plaza, occuring in locations :)))
    // Example: "Pershing Square Plaza (Pershing Square Plaza): between X and Y"
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

    // Shorten any names that mention a plaza or square
    private static String shortenNamedPlaza(String name) {
        String[] mainParts = name.split(": ", 2);
        if (mainParts.length == 2) {
            String plazaName = mainParts[0];
            String intersection = mainParts[1];

            plazaName = plazaName.replaceAll("Pedestrian Plaza", "Plaza")
                    .replaceAll(" Plaza Plaza", " Plaza")
                    .replaceAll("\\(Herald Square.*?\\)", "(Herald Sq)")
                    .replaceAll("Pershing Square Plaza", "Pershing Sq");

            // Shorten the intersection part too if necessary
            if (intersection.contains(" between ")) {
                intersection = shortenStreetIntersection(intersection);
            }

            return plazaName + ": " + intersection;
        }
        return name;
    }

    // Shorten names like "X Street between Y and Z"
    private static String shortenStreetIntersection(String streetName) {
        String[] parts = streetName.split(" between ", 2);
        if (parts.length != 2) return streetName;

        String mainStreet = parts[0].trim();
        String intersectionPart = parts[1].trim();

        // Shorten the main street's name via the function
        mainStreet = shortenStreetName(mainStreet);
        String shortenedIntersection = shortenIntersectionDescription(intersectionPart);
        return mainStreet + " (" + shortenedIntersection + ")";
    }

    private static String shortenStreetName(String streetName) {
        return streetName
                // Handmade attempt at abbreviation for very long boulevard names
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
                // Central Park variations
                .replaceAll("\\bCENTRAL PARK NORTH\\b", "Central Park N")
                .replaceAll("\\bCENTRAL PARK SOUTH\\b", "Central Park S")
                .replaceAll("\\bCENTRAL PARK WEST\\b", "Central Park W")
                .replaceAll("\\bCENTRAL PARK EAST\\b", "Central Park E")
                // Remove directional words for streets to save some space
                .replaceAll("\\b(EAST|WEST|NORTH|SOUTH)\\s+", "")
                // Standard abbreviations anyways
                .replaceAll("\\bAVENUE\\b", "Ave")
                .replaceAll("\\bSTREET\\b", "St")
                .replaceAll("\\bBOULEVARD\\b", "Blvd")
                .replaceAll("\\bPLAZA\\b", "Plz")
                .replaceAll("\\bSQUARE\\b", "Sq")
                .replaceAll("\\bPLACE\\b", "Pl")
                .replaceAll("\\bTERRACE\\b", "Ter");
    }

    // This is an attempt to summarise the intersection of 2 streets ref, via numbers & abbreviations....
    private static String shortenIntersectionDescription(String intersection) {
        String[] streets = intersection.split("\\s++and\\s++", 2);
        if (streets.length == 2) {
            String street1 = shortenStreetName(streets[0].trim());
            String street2 = shortenStreetName(streets[1].trim());

            // I attempt to make a number range if two streets are mentioned with numbers
            String range = extractNumberRange(street1, street2);
            if (range != null) {
                return range;
            }

            // Another round of abbreviations for really long names
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

    // I try to take a number range from two streets if they are in one location name
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
