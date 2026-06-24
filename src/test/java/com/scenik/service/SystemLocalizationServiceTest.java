package com.scenik.service;

import com.scenik.domain.Content;
import com.scenik.domain.Place;
import com.scenik.dto.ContentDto;
import com.scenik.dto.PlaceDto;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SystemLocalizationServiceTest {

    private final SystemLocalizationService localizationService = new SystemLocalizationService();

    @Test
    void localizesKnownContentDisplayFieldsAndKeepsOriginals() {
        Content content = new Content();
        content.setContentId("C_DRAMA_005");
        content.setTitle("도깨비");
        content.setCategory("드라마");
        content.setDescription("불멸의 삶을 끝내기 위해 인간 신부가 필요한 도깨비 이야기.");
        content.setCastNames("공유, 김고은");
        content.setTags("강릉,주문진,로맨스,촬영지");

        ContentDto dto = localizationService.localize(ContentDto.from(content), "en-US,en;q=0.8");

        assertThat(dto.locale()).isEqualTo("en");
        assertThat(dto.title()).isEqualTo("Goblin");
        assertThat(dto.category()).isEqualTo("Drama");
        assertThat(dto.tags()).contains("Gangneung", "Jumunjin", "Romance", "Filming location");
        assertThat(dto.originalTitle()).isEqualTo("도깨비");
        assertThat(dto.originalCategory()).isEqualTo("드라마");
        assertThat(dto.originalTags()).containsExactly("강릉", "주문진", "로맨스", "촬영지");
    }

    @Test
    void localizesPlaceLabelsAndFallsBackToOriginalName() {
        Place place = new Place();
        place.setPlaceId("P_0001");
        place.setPlaceName("킨텍스");
        place.setRegion("경기");
        place.setDistrict("고양시");
        place.setCategory("촬영지");
        place.setDescription("오징어 게임 마지막 장면 관련 장소.");

        PlaceDto dto = localizationService.localize(PlaceDto.from(place), "zh-CN,zh;q=0.9");

        assertThat(dto.locale()).isEqualTo("zh-CN");
        assertThat(dto.placeName()).isEqualTo("킨텍스");
        assertThat(dto.region()).isEqualTo("京畿");
        assertThat(dto.category()).isEqualTo("拍摄地");
        assertThat(dto.originalPlaceName()).isEqualTo("킨텍스");
        assertThat(dto.originalCategory()).isEqualTo("촬영지");
    }

    @Test
    void localizesPlaceDistrictAndGeneratedDescriptionForNonKoreanLocale() {
        Place place = new Place();
        place.setPlaceId("P_0001");
        place.setPlaceName("킨텍스");
        place.setRegion("경기");
        place.setDistrict("고양시");
        place.setCategory("촬영지");
        place.setDescription("[오징어게임] 마지막 장면 관련 장소.");

        PlaceDto dto = localizationService.localize(PlaceDto.from(place), "en-US,en;q=0.8");

        assertThat(dto.locale()).isEqualTo("en");
        assertThat(dto.placeName()).isEqualTo("킨텍스");
        assertThat(dto.region()).isEqualTo("Gyeonggi");
        assertThat(dto.district()).isEqualTo("Goyang City");
        assertThat(dto.category()).isEqualTo("Filming location");
        assertThat(dto.description()).isEqualTo("A verified filming location in Goyang City, Gyeonggi connected to Squid Game.");
        assertThat(dto.originalDescription()).isEqualTo("[오징어게임] 마지막 장면 관련 장소.");
    }

    @Test
    void localizesHighPriorityPlaceNamesWithOfficialOrCommonNames() {
        Place lotteSeoulSky = new Place();
        lotteSeoulSky.setPlaceId("P_0219");
        lotteSeoulSky.setPlaceName("롯데월드타워 서울스카이");
        lotteSeoulSky.setRegion("서울");
        lotteSeoulSky.setDistrict("송파구");
        lotteSeoulSky.setCategory("전망대");
        lotteSeoulSky.setDescription("[롯데월드타워 서울스카이] 전망대.");

        PlaceDto englishDto = localizationService.localize(PlaceDto.from(lotteSeoulSky), "en-US,en;q=0.8");
        PlaceDto japaneseDto = localizationService.localize(PlaceDto.from(lotteSeoulSky), "ja-JP,ja;q=0.8");
        PlaceDto chineseDto = localizationService.localize(PlaceDto.from(lotteSeoulSky), "zh-CN,zh;q=0.8");

        assertThat(englishDto.placeName()).isEqualTo("Lotte World Tower Seoul Sky");
        assertThat(japaneseDto.placeName()).isEqualTo("ロッテワールドタワー ソウルスカイ");
        assertThat(chineseDto.placeName()).isEqualTo("乐天世界塔首尔天空");

        Place bukchonMuseum = new Place();
        bukchonMuseum.setPlaceId("P_0256");
        bukchonMuseum.setPlaceName("북촌동양문화박물관");
        bukchonMuseum.setRegion("서울");
        bukchonMuseum.setDistrict("종로구");
        bukchonMuseum.setCategory("관광지");
        bukchonMuseum.setDescription("[북촌동양문화박물관] 관광지.");

        PlaceDto museumDto = localizationService.localize(PlaceDto.from(bukchonMuseum), "en-US,en;q=0.8");

        assertThat(museumDto.placeName()).isEqualTo("Bukchon Asian Cultural Art Museum");
        assertThat(museumDto.originalPlaceName()).isEqualTo("북촌동양문화박물관");
    }

    @Test
    void localizesAdditionalBrandAndFacilityPlaceNamesWithoutBrokenRomanization() {
        assertEnglishPlaceName("P_0020", "\uADF8\uB79C\uB4DC \uC6CC\uCEE4\uD790 \uC11C\uC6B8", "Grand Walkerhill Seoul");
        assertEnglishPlaceName("P_0039", "\uACBD\uC6D0\uC7AC \uBC14\uC774 \uC6CC\uCEE4\uD790", "Gyeongwonjae Ambassador Incheon");
        assertEnglishPlaceName("P_0064", "\uB86F\uB370\uBC31\uD654\uC810 \uAC15\uB0A8\uC810", "Lotte Department Store Gangnam Branch");
        assertEnglishPlaceName("P_0083", "\uC6B8\uD504\uAC15 \uC2A4\uD14C\uC774\uD06C \uD558\uC6B0\uC2A4", "Wolfgang's Steakhouse");
        assertEnglishPlaceName("P_0114", "\uC624\uD06C\uC6B0\uB4DC\uD504\uB9AC\uBBF8\uC5B4 \uC778\uCC9C", "Oakwood Premier Incheon");
        assertEnglishPlaceName("P_0138", "\uC624\uB959\uB3C4 \uC2A4\uCE74\uC774\uC6CC\uD06C", "Oryukdo Skywalk");
        assertEnglishPlaceName("P_0167", "\uBB38\uACBD \uC6A9\uB450\uB9AC \uC288\uD37C \uC138\uD2B8", "Mungyeong Yongdu-ri Super Set");
        assertEnglishPlaceName("P_0169", "\uBB38\uACBD \uC5D0\uCF54\uC6D4\uB4DC", "Mungyeong Eco World");
        assertEnglishPlaceName("P_0170", "\uADF8\uB79C\uB4DC \uC6CC\uCEE4\uD790 \uC11C\uC6B8 \uC560\uC2A4\uD1A4\uD558\uC6B0\uC2A4", "Grand Walkerhill Seoul Aston House");
        assertEnglishPlaceName("P_0168", "\uBB38\uACBD \uCCA0\uB85C\uC790\uC804\uAC70 \uAD6C\uB791\uB9AC\uC5ED", "Mungyeong Rail Bike Gurang-ri Station");
        assertEnglishPlaceName("P_0171", "\uC544\uCFE0\uC544\uD50C\uB77C\uB137 \uC77C\uC0B0", "Aqua Planet Ilsan");
        assertEnglishPlaceName("P_0172", "\uC0AC\uC720\uC6D0", "Sayuwon");
        assertEnglishPlaceName("P_0173", "\uCDA9\uC8FC \uAC74\uC9C0\uB9C8\uC744", "Chungju Geonji Village");
        assertEnglishPlaceName("P_0262", "BTS \uBC84\uC2A4\uC815\uB958\uC7A5", "BTS Bus Stop");

        Place ecoWorld = place("P_0169", "\uBB38\uACBD \uC5D0\uCF54\uC6D4\uB4DC");
        assertThat(localizationService.localize(PlaceDto.from(ecoWorld), "ja-JP").placeName())
                .isEqualTo("\u805E\u6176\u30A8\u30B3\u30EF\u30FC\u30EB\u30C9");
        assertThat(localizationService.localize(PlaceDto.from(ecoWorld), "zh-CN").placeName())
                .isEqualTo("\u95FB\u5E86\u751F\u6001\u4E16\u754C");
    }

    private void assertEnglishPlaceName(String placeId, String placeName, String expected) {
        assertThat(localizationService.localize(PlaceDto.from(place(placeId, placeName)), "en-US,en;q=0.8").placeName())
                .isEqualTo(expected);
    }

    private Place place(String placeId, String placeName) {
        Place place = new Place();
        place.setPlaceId(placeId);
        place.setPlaceName(placeName);
        return place;
    }

}
