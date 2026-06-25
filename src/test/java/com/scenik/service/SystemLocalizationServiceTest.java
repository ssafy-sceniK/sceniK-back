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
        assertThat(dto.placeName()).isEqualTo("KINTEX");
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
        assertEnglishPlaceName("P_0174", "성산일출봉", "Seongsan Ilchulbong");
        assertEnglishPlaceName("P_0177", "델문도 김녕점 앞 해변", "Beach in front of Delmundo Gimnyeong");
        assertEnglishPlaceName("P_0178", "백약이오름", "Baegyagi Oreum");
        assertEnglishPlaceName("P_0179", "안돌오름", "Andol Oreum");
        assertEnglishPlaceName("P_0186", "넙게오름", "Neopge Oreum");
        assertEnglishPlaceName("P_0189", "한림수협위판장", "Hallim Fisheries Cooperative Fish Market");
        assertEnglishPlaceName("P_0193", "군산오름", "Gunsan Oreum");
        assertEnglishPlaceName("P_0199", "상여오름", "Sangyeo Oreum");
        assertEnglishPlaceName("P_0213", "코엑스 K-POP Square", "COEX K-POP Square");
        assertEnglishPlaceName("P_0233", "용눈이오름", "Yongnuni Oreum");
        assertEnglishPlaceName("P_0262", "BTS \uBC84\uC2A4\uC815\uB958\uC7A5", "BTS Bus Stop");

        Place coexKpopSquare = place("P_0213", "코엑스 K-POP Square");
        assertThat(localizationService.localize(PlaceDto.from(coexKpopSquare), "ja-JP").placeName())
                .isEqualTo("COEX K-POP広場");
        assertThat(localizationService.localize(PlaceDto.from(coexKpopSquare), "zh-CN").placeName())
                .isEqualTo("COEX K-POP广场");

        Place seongsanIlchulbong = place("P_0174", "성산일출봉");
        assertThat(localizationService.localize(PlaceDto.from(seongsanIlchulbong), "ja-JP").placeName())
                .isEqualTo("城山日出峰");
        assertThat(localizationService.localize(PlaceDto.from(seongsanIlchulbong), "zh-CN").placeName())
                .isEqualTo("城山日出峰");

        Place ecoWorld = place("P_0169", "\uBB38\uACBD \uC5D0\uCF54\uC6D4\uB4DC");
        assertThat(localizationService.localize(PlaceDto.from(ecoWorld), "ja-JP").placeName())
                .isEqualTo("\u805E\u6176\u30A8\u30B3\u30EF\u30FC\u30EB\u30C9");
        assertThat(localizationService.localize(PlaceDto.from(ecoWorld), "zh-CN").placeName())
                .isEqualTo("\u95FB\u5E86\u751F\u6001\u4E16\u754C");
    }


    @Test
    void localizesExtendedEnglishPlaceNamesFromQualityAudit() {
        assertEnglishPlaceName("P_0001", "fallback", "KINTEX");
        assertEnglishPlaceName("P_0007", "fallback", "Namsan Walking Trail");
        assertEnglishPlaceName("P_0008", "fallback", "Seongapdo Island");
        assertEnglishPlaceName("P_0009", "fallback", "My Land");
        assertEnglishPlaceName("P_0013", "fallback", "Daegwallyeong Samyang Roundhill");
        assertEnglishPlaceName("P_0014", "fallback", "Balwangsan Cable Car");
        assertEnglishPlaceName("P_0015", "fallback", "Woljeongsa Fir Forest Trail");
        assertEnglishPlaceName("P_0019", "fallback", "Konkuk University");
        assertEnglishPlaceName("P_0024", "fallback", "Sageun-Yongdap Pedestrian Bridge");
        assertEnglishPlaceName("P_0029", "fallback", "Birch Tree Story");
        assertEnglishPlaceName("P_0035", "fallback", "Hanmi Bookstore");
        assertEnglishPlaceName("P_0037", "fallback", "Dream Park");
        assertEnglishPlaceName("P_0038", "fallback", "Cheongna Lake Park");
        assertEnglishPlaceName("P_0044", "fallback", "Neoreundeul Hagwon Farm (formerly Borinara Hagwon Farm)");
        assertEnglishPlaceName("P_0047", "fallback", "Seokbyeong 1-ri Breakwater");
        assertEnglishPlaceName("P_0048", "fallback", "Cheongha Five-day Market");
        assertEnglishPlaceName("P_0051", "fallback", "The River");
        assertEnglishPlaceName("P_0052", "fallback", "Seoul Dragon City");
        assertEnglishPlaceName("P_0053", "fallback", "Ravie Books");
        assertEnglishPlaceName("P_0054", "fallback", "Cafe Hwahong");
        assertEnglishPlaceName("P_0056", "fallback", "Jidong Market Sundae Town");
        assertEnglishPlaceName("P_0063", "fallback", "The Conran Shop Korea");
        assertEnglishPlaceName("P_0072", "fallback", "Daily Beer Yeonsinnae Branch");
        assertEnglishPlaceName("P_0073", "fallback", "JCC Creative Center");
        assertEnglishPlaceName("P_0074", "fallback", "Triple Street");
        assertEnglishPlaceName("P_0077", "fallback", "Onbit Natural Recreation Forest");
        assertEnglishPlaceName("P_0078", "fallback", "Boryeong Green Barley Field");
        assertEnglishPlaceName("P_0079", "fallback", "Byeolmaro Observatory");
        assertEnglishPlaceName("P_0080", "fallback", "Hongsam Spa Charcoal Kiln Sauna");
        assertEnglishPlaceName("P_0081", "fallback", "Pocheon Hantangang Sky Bridge");
        assertEnglishPlaceName("P_0082", "fallback", "Commodore Hotel Busan");
        assertEnglishPlaceName("P_0084", "fallback", "BBQ Heliocity Branch");
        assertEnglishPlaceName("P_0085", "fallback", "Unsan");
        assertEnglishPlaceName("P_0086", "fallback", "Jemulpo Club");
        assertEnglishPlaceName("P_0091", "fallback", "Forest of Healing");
        assertEnglishPlaceName("P_0092", "fallback", "Cheongsan Arboretum");
        assertEnglishPlaceName("P_0095", "fallback", "Tangeumho Rainbow Road");
        assertEnglishPlaceName("P_0097", "fallback", "Vivaldi Park");
        assertEnglishPlaceName("P_0098", "fallback", "Wondang Stud Farm");
        assertEnglishPlaceName("P_0099", "fallback", "Bellacitta");
        assertEnglishPlaceName("P_0100", "fallback", "Asosan Ilsan Main Branch");
        assertEnglishPlaceName("P_0102", "fallback", "Bom Farmers Garden");
        assertEnglishPlaceName("P_0103", "fallback", "Kare Klint Bundang-Yongin Branch");
        assertEnglishPlaceName("P_0104", "fallback", "Forest of Wisdom");
        assertEnglishPlaceName("P_0105", "fallback", "Muddy Murphy's");
        assertEnglishPlaceName("P_0109", "fallback", "Hambakuteum Sundae Gukbap");
        assertEnglishPlaceName("P_0110", "fallback", "L'Amant Secret");
        assertEnglishPlaceName("P_0111", "fallback", "L'Escape Hotel");
        assertEnglishPlaceName("P_0112", "fallback", "Yongin Dae Jang Geum Park");
        assertEnglishPlaceName("P_0113", "fallback", "Byeongsan Seowon Confucian Academy");
        assertEnglishPlaceName("P_0116", "fallback", "Ocean World");
        assertEnglishPlaceName("P_0117", "fallback", "Petite France");
        assertEnglishPlaceName("P_0119", "fallback", "The Nurungji");
        assertEnglishPlaceName("P_0120", "fallback", "Gimpo Art Village");
        assertEnglishPlaceName("P_0121", "fallback", "Siheung Oido Prehistoric Park");
        assertEnglishPlaceName("P_0122", "fallback", "Lighthouse Sashimi Restaurant");
        assertEnglishPlaceName("P_0123", "fallback", "Cheongchun Jogae Main Branch");
        assertEnglishPlaceName("P_0126", "fallback", "Myeongga Donkatsu");
        assertEnglishPlaceName("P_0127", "fallback", "Simmons Terrace");
        assertEnglishPlaceName("P_0128", "fallback", "Mulbit Haneul Garden Main Branch");
        assertEnglishPlaceName("P_0130", "fallback", "First Garden");
        assertEnglishPlaceName("P_0131", "fallback", "Speed Park");
        assertEnglishPlaceName("P_0132", "fallback", "Utdari Culture Village");
        assertEnglishPlaceName("P_0133", "fallback", "Pyeonggang Land");
        assertEnglishPlaceName("P_0135", "fallback", "Korea Zipwire");
        assertEnglishPlaceName("P_0140", "fallback", "Yeongjin Dwaeji Gukbap Costco Centum Branch");
        assertEnglishPlaceName("P_0142", "fallback", "Wonjo Milmyeon");
        assertEnglishPlaceName("P_0143", "fallback", "Saeachim Restaurant");
        assertEnglishPlaceName("P_0144", "fallback", "Alver");
        assertEnglishPlaceName("P_0146", "fallback", "Bongmilga Gangnam-gu Office Branch");
        assertEnglishPlaceName("P_0147", "fallback", "Neulpureun Mokjang Sangam Branch");
        assertEnglishPlaceName("P_0148", "fallback", "Oil Tank Culture Park");
        assertEnglishPlaceName("P_0149", "fallback", "Sigoljip Cheonggukjang");
        assertEnglishPlaceName("P_0151", "fallback", "Pizzeria O");
        assertEnglishPlaceName("P_0153", "fallback", "Yongma Land");
        assertEnglishPlaceName("P_0154", "fallback", "Jangsaengpo Whale Culture Village");
        assertEnglishPlaceName("P_0155", "fallback", "Sammok Ferry Terminal");
        assertEnglishPlaceName("P_0156", "fallback", "Moon China");
        assertEnglishPlaceName("P_0159", "fallback", "Gongsanseong Fortress");
        assertEnglishPlaceName("P_0162", "fallback", "Myeongam Boat Dock");
        assertEnglishPlaceName("P_0165", "fallback", "Haenggung-dong King's Alley");
        assertEnglishPlaceName("P_0190", "fallback", "Geumneung Port");
        assertEnglishPlaceName("P_0192", "fallback", "Deokcheon-ri Drive Course");
        assertEnglishPlaceName("P_0194", "fallback", "Yeongnak-ri Coastal Road");
        assertEnglishPlaceName("P_0196", "fallback", "Jongdal Village Road");
        assertEnglishPlaceName("P_0197", "fallback", "Jungmun Duty Free Shop");
        assertEnglishPlaceName("P_0198", "fallback", "Seogwipo KAL Hotel");
        assertEnglishPlaceName("P_0201", "fallback", "Ora-dong Buckwheat Field");
        assertEnglishPlaceName("P_0203", "fallback", "Daemunbawi Rock");
        assertEnglishPlaceName("P_0204", "fallback", "Hyangrobong Peak");
        assertEnglishPlaceName("P_0205", "fallback", "Gyeongsang-ri Zelkova Tree");
        assertEnglishPlaceName("P_0206", "fallback", "Changsin-dong Alley");
        assertEnglishPlaceName("P_0208", "fallback", "Yeongwol Jangneung Royal Tomb");
        assertEnglishPlaceName("P_0210", "fallback", "Danjong History Museum");
        assertEnglishPlaceName("P_0220", "fallback", "Yeongnangho Beombawi Rock");
        assertEnglishPlaceName("P_0222", "fallback", "Park Hyatt Busan");
        assertEnglishPlaceName("P_0230", "fallback", "Tindertown");
        assertEnglishPlaceName("P_0232", "fallback", "Flowave");
        assertEnglishPlaceName("P_0234", "fallback", "Dongbok Haenyeo Seafood Direct Sales Market");
        assertEnglishPlaceName("P_0239", "fallback", "Healing Stay KOSMOS");
        assertEnglishPlaceName("P_0243", "fallback", "Taeha Hyangmok Monorail");
        assertEnglishPlaceName("P_0246", "fallback", "Coffee Gieop Apgujeong Branch");
        assertEnglishPlaceName("P_0247", "fallback", "Cheonjangjigu Cafe");
        assertEnglishPlaceName("P_0248", "fallback", "MIMIMI Garden Cafe");
        assertEnglishPlaceName("P_0249", "fallback", "Saseungbongdo Island");
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
