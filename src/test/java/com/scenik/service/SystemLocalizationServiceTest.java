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
}
