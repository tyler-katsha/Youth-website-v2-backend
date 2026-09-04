package com.tyler.YouthEngedi.utils;

import java.util.Set;
import java.util.regex.Pattern;

public final class BotFingerprintSignatures {

    // Signatures produced by default automation HTTP engines
    public static final Set<String> KNOWN_BOT_JA3 = Set.of(
            "b32309a26cedf1e92902f61737ac2314", // Python 3.x requests / urllib
            "06900f507b949669e4f5068617d3a2ca", // Go-http-client
            "e7d705a3286e19ea42f587b344ee6865"  // cURL default
    );

    // Browser User-Agent patterns
    public static final Pattern BROWSER_UA_PATTERN =
            Pattern.compile("Mozilla/5\\.0.*(Chrome|Safari|Firefox|Edg)/.*");

    // Standard Chrome desktop JA3 (example hash; vary across Chrome versions)
    public static final Set<String> CHROME_FAMILY_JA3 = Set.of(
            "66918128f1b9b03303d77c6f2eefd128",
            "b32309a26cedf1e92902f61737ac2314" // Will flag if this masquerades as Chrome
    );

}
