package com.scenik.service;

import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class LanguageDetectionService {
    private static final Set<String> SUPPORTED_LANGUAGES = Set.of("ko", "en", "ja", "zh-CN");

    public String detect(String text, String fallbackLanguage) {
        String fallback = normalize(fallbackLanguage);
        if (text == null || text.isBlank()) return fallback;
        int hangul = 0;
        int kana = 0;
        int cjk = 0;
        int latin = 0;
        for (int offset = 0; offset < text.length(); ) {
            int codePoint = text.codePointAt(offset);
            offset += Character.charCount(codePoint);
            if (isHangul(codePoint)) hangul++;
            else if (isKana(codePoint)) kana++;
            else if (isCjkUnifiedIdeograph(codePoint)) cjk++;
            else if (isLatinLetter(codePoint)) latin++;
        }
        if (hangul > 0 && hangul >= kana + cjk) return "ko";
        if (kana > 0) return "ja";
        if (cjk > 0) return "zh-CN";
        if (latin > 0) return "en";
        return fallback;
    }

    public String normalize(String language) {
        if (language == null || language.isBlank()) return "ko";
        String normalized = language.trim().replace('_', '-');
        String lower = normalized.toLowerCase();
        if (lower.startsWith("ko")) return "ko";
        if (lower.startsWith("en")) return "en";
        if (lower.startsWith("ja")) return "ja";
        if (lower.equals("zh-cn") || lower.startsWith("zh-hans") || lower.startsWith("zh")) return "zh-CN";
        return SUPPORTED_LANGUAGES.contains(normalized) ? normalized : "ko";
    }

    private boolean isHangul(int codePoint) {
        return (codePoint >= 0xAC00 && codePoint <= 0xD7A3)
                || (codePoint >= 0x1100 && codePoint <= 0x11FF)
                || (codePoint >= 0x3130 && codePoint <= 0x318F);
    }

    private boolean isKana(int codePoint) {
        return (codePoint >= 0x3040 && codePoint <= 0x30FF)
                || (codePoint >= 0x31F0 && codePoint <= 0x31FF);
    }

    private boolean isCjkUnifiedIdeograph(int codePoint) {
        return (codePoint >= 0x4E00 && codePoint <= 0x9FFF)
                || (codePoint >= 0x3400 && codePoint <= 0x4DBF);
    }

    private boolean isLatinLetter(int codePoint) {
        return (codePoint >= 'A' && codePoint <= 'Z') || (codePoint >= 'a' && codePoint <= 'z');
    }
}