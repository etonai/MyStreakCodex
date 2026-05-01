package com.pseddev.mystreak.utils

object TextNormalizer {

    /**
     * Normalizes text by standardizing apostrophes, quotes, and whitespace.
     * This ensures consistent piece names across import, export, and manual entry.
     */
    fun normalizePieceName(text: String): String {
        // First apply Unicode normalization to decompose any composite characters
        val unicodeNormalized = java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFKD)

        val normalized = unicodeNormalized
            .trim()
            .replace("\\s+".toRegex(), " ") // Collapse multiple spaces to single space
            // Remove any invisible/control characters
            .replace("\\p{C}".toRegex(), "") // Remove control characters
            // Normalize all apostrophe variations to regular apostrophe using Unicode escapes
            .replace("\u2018", "'") // LEFT SINGLE QUOTATION MARK (U+2018)
            .replace("\u2019", "'") // RIGHT SINGLE QUOTATION MARK (U+2019) - THIS IS THE KEY ONE!
            .replace("\u201B", "'") // SINGLE HIGH-REVERSED-9 QUOTATION MARK (U+201B)
            .replace("\u02BC", "'") // MODIFIER LETTER APOSTROPHE (U+02BC)
            .replace("\u02C8", "'") // MODIFIER LETTER VERTICAL LINE (U+02C8)
            .replace("\u02CA", "'") // MODIFIER LETTER ACUTE ACCENT
            .replace("\u0060", "'") // GRAVE ACCENT (sometimes used as apostrophe)
            .replace("\u00B4", "'") // ACUTE ACCENT
            // Normalize all quote variations to regular quotes using Unicode escapes
            .replace("\u201C", "\"") // LEFT DOUBLE QUOTATION MARK (U+201C)
            .replace("\u201D", "\"") // RIGHT DOUBLE QUOTATION MARK (U+201D)
            .replace("\u201E", "\"") // DOUBLE LOW-9 QUOTATION MARK (U+201E)
            .replace("\u201A", "'") // SINGLE LOW-9 QUOTATION MARK (U+201A)
            // Apply final Unicode composition normalization

        val finalNormalized = java.text.Normalizer.normalize(normalized, java.text.Normalizer.Form.NFC)

        if (text != finalNormalized) {
            android.util.Log.d("TextNormalizer", "Normalized '$text' -> '$finalNormalized'")
            android.util.Log.d("TextNormalizer", "Original bytes: ${text.toByteArray().joinToString { it.toString() }}")
            android.util.Log.d("TextNormalizer", "Final bytes: ${finalNormalized.toByteArray().joinToString { it.toString() }}")
        }

        return finalNormalized
    }

    /**
     * Normalizes any user input text (for piece names, notes, etc.)
     */
    fun normalizeUserInput(text: String): String {
        return normalizePieceName(text)
    }

    /**
     * Test function to verify normalization is working correctly
     */
    fun testNormalization(): String {
        val regular = "Somewhere That's Green"  // Regular apostrophe
        val curly = "Somewhere That's Green"    // Curly apostrophe (U+2019)

        val normalizedRegular = normalizePieceName(regular)
        val normalizedCurly = normalizePieceName(curly)

        return "Regular: '$regular' -> '$normalizedRegular'\n" +
               "Curly: '$curly' -> '$normalizedCurly'\n" +
               "Equal after normalization: ${normalizedRegular == normalizedCurly}"
    }
}