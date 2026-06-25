package com.scenik.service;

import com.scenik.domain.Content;
import com.scenik.domain.Place;
import com.scenik.mapper.ContentMapper;
import com.scenik.mapper.PlaceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock ContentMapper contentMapper;
    @Mock PlaceMapper placeMapper;

    private SearchService searchService;

    @BeforeEach
    void setUp() {
        searchService = new SearchService(contentMapper, placeMapper, new SystemLocalizationService());
        when(contentMapper.findContents(null, null, null, null)).thenReturn(List.of(
                content("C_DRAMA_005", "도깨비", "드라마", "공유, 김고은", "강릉,주문진,로맨스,촬영지"),
                content("C_MOVIE_001", "파묘", "영화", "최민식, 김고은", "영화,미스터리")
        ));
        when(placeMapper.findPlaces(null, null, null, null, null)).thenReturn(List.of(
                place("P_0174", "성산일출봉", "제주", "서귀포시", "관광지"),
                place("P_0213", "코엑스 K-POP Square", "서울", "강남구", "공연장"),
                place("P_0256", "북촌동양문화박물관", "서울", "종로구", "관광지")
        ));
    }

    @Test
    void matchesLocalizedContentTitleByContains() {
        var result = searchService.search("blin", "en-US,en;q=0.8");

        assertThat(result.contents()).extracting("contentId").containsExactly("C_DRAMA_005");
        assertThat(result.contents().get(0).title()).isEqualTo("Goblin");
    }

    @Test
    void matchesOriginalContentTitleAndCastNamesByContains() {
        assertThat(searchService.search("깨비", "en-US,en;q=0.8").contents())
                .extracting("contentId")
                .containsExactly("C_DRAMA_005");
        assertThat(searchService.search("고은", "en-US,en;q=0.8").contents())
                .extracting("contentId")
                .containsExactly("C_DRAMA_005", "C_MOVIE_001");
    }

    @Test
    void matchesLocalizedPlaceNamesByContains() {
        var coexResult = searchService.search("COEX", "en-US,en;q=0.8");
        var ilchulResult = searchService.search("Ilchul", "en-US,en;q=0.8");
        var bukchonResult = searchService.search("Asian Cultural", "en-US,en;q=0.8");

        assertThat(coexResult.places()).extracting("placeId").containsExactly("P_0213");
        assertThat(ilchulResult.places()).extracting("placeId").containsExactly("P_0174");
        assertThat(bukchonResult.places()).extracting("placeId").containsExactly("P_0256");
    }

    private Content content(String contentId, String title, String category, String castNames, String tags) {
        Content content = new Content();
        content.setContentId(contentId);
        content.setTitle(title);
        content.setCategory(category);
        content.setDescription(title + " 소개");
        content.setCastNames(castNames);
        content.setTags(tags);
        return content;
    }

    private Place place(String placeId, String placeName, String region, String district, String category) {
        Place place = new Place();
        place.setPlaceId(placeId);
        place.setPlaceName(placeName);
        place.setRegion(region);
        place.setDistrict(district);
        place.setCategory(category);
        place.setDescription(placeName + " 설명");
        return place;
    }
}
