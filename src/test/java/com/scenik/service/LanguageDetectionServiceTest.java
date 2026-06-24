package com.scenik.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LanguageDetectionServiceTest {
    private final LanguageDetectionService service = new LanguageDetectionService();

    @Test
    void detectsSupportedLanguagesFromUserGeneratedText() {
        assertThat(service.detect("서울 촬영지 분위기가 정말 좋아요", "en")).isEqualTo("ko");
        assertThat(service.detect("This filming location is beautiful", "ko")).isEqualTo("en");
        assertThat(service.detect("東京から行きたいロケ地です", "ko")).isEqualTo("ja");
        assertThat(service.detect("这个拍摄地很漂亮", "ko")).isEqualTo("zh-CN");
    }

    @Test
    void normalizesFallbackLanguageWhenTextIsEmptyOrAmbiguous() {
        assertThat(service.detect("", "ja-JP")).isEqualTo("ja");
        assertThat(service.detect("12345 !?", "zh_Hans_CN")).isEqualTo("zh-CN");
        assertThat(service.detect("12345 !?", "unsupported")).isEqualTo("ko");
    }
}
