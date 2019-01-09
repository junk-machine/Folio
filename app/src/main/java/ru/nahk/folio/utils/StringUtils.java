package ru.nahk.folio.utils;

/**
 * Utility methods to work with {@link String}.
 */
public final class StringUtils {
    /**
     * Checks if string contains a substring ignoring the case.
     * @param src Source string.
     * @param substring Look-up string.
     * @return True if source string contains a substring, otherwise false.
     */
    public static boolean containsIgnoreCase(String src, String substring) {
        final int length = substring.length();
        if (length == 0) {
            // Empty substring always exists
            return true;
        }

        // Store upper and lower case of the first substring characters.
        // This will allow to do quick match.
        final char firstLo = Character.toLowerCase(substring.charAt(0));
        final char firstUp = Character.toUpperCase(substring.charAt(0));

        int msxCharIndex = src.length() - length;
        for (int charIndex = 0; charIndex <= msxCharIndex; ++charIndex) {
            // Quick check before calling more expensive regionMatches() method
            final char ch = src.charAt(charIndex);
            if (ch != firstLo && ch != firstUp){
                continue;
            }

            if (src.regionMatches(true, charIndex, substring, 0, length)){
                return true;
            }
        }

        return false;
    }
}
