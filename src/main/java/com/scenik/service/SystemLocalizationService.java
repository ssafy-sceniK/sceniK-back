package com.scenik.service;

import com.scenik.dto.ContentCategoryDto;
import com.scenik.dto.ContentDto;
import com.scenik.dto.PlaceDto;
import com.scenik.dto.RelatedContentDto;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class SystemLocalizationService {
    private static final Map<String, Map<String, String>> LABELS = new HashMap<>();
    private static final Map<String, Map<String, String>> CONTENT_TITLES = new HashMap<>();
    private static final Map<String, Map<String, String>> PLACE_NAMES = new HashMap<>();
    private static final Map<String, String> CONTENT_SOURCE_IDS = new HashMap<>();
    private static final Map<String, Map<String, String>> CONTENT_TITLE_ALIASES = new HashMap<>();

    static {
        addLabel("드라마", "Drama", "ドラマ", "电视剧");
        addLabel("영화", "Movie", "映画", "电影");
        addLabel("예능", "Variety show", "バラエティ", "综艺");
        addLabel("아이돌", "Idol", "アイドル", "偶像");
        addLabel("배우", "Actor", "俳優", "演员");
        addLabel("촬영지", "Filming location", "ロケ地", "拍摄地");
        addLabel("카페", "Cafe", "カフェ", "咖啡馆");
        addLabel("맛집", "Restaurant", "グルメ", "美食");
        addLabel("공원", "Park", "公園", "公园");
        addLabel("거리", "Street", "通り", "街道");
        addLabel("학교", "School", "学校", "学校");
        addLabel("호텔", "Hotel", "ホテル", "酒店");
        addLabel("테마파크", "Theme park", "テーマパーク", "主题公园");
        addLabel("시장", "Market", "市場", "市场");
        addLabel("해변", "Beach", "海辺", "海滩");
        addLabel("관광지", "Tourist attraction", "観光地", "观光地");
        addLabel("케이블카", "Cable car", "ケーブルカー", "缆车");
        addLabel("산", "Mountain", "山", "山");
        addLabel("전망대", "Observatory", "展望台", "观景台");
        addLabel("쇼핑센터", "Shopping center", "ショッピングセンター", "购物中心");
        addLabel("서울", "Seoul", "ソウル", "首尔");
        addLabel("부산", "Busan", "釜山", "釜山");
        addLabel("인천", "Incheon", "仁川", "仁川");
        addLabel("경기", "Gyeonggi", "京畿", "京畿");
        addLabel("강원", "Gangwon", "江原", "江原");
        addLabel("제주", "Jeju", "済州", "济州");
        addLabel("충남", "Chungnam", "忠南", "忠南");
        addLabel("강릉", "Gangneung", "江陵", "江陵");
        addLabel("주문진", "Jumunjin", "注文津", "注文津");
        addLabel("로맨스", "Romance", "ロマンス", "浪漫");
        addLabel("감성", "Emotional", "感性", "感性");
        addLabel("데이트", "Date spot", "デート", "约会");
        addLabel("야경", "Night view", "夜景", "夜景");
        addLabel("힐링", "Healing", "癒やし", "治愈");
        addLabel("사진찍기좋은", "Photo spot", "写真映え", "适合拍照");
        addLabel("로맨틱한", "Romantic", "ロマンチック", "浪漫");
        addLabel("활기찬", "Lively", "にぎやか", "热闹");
        addLabel("아늑한", "Cozy", "居心地のよい", "舒适");
        addLabel("맛집투어", "Food tour", "グルメ巡り", "美食之旅");
        addLabel("친구", "Friends", "友達", "朋友");
        addLabel("팬덤", "Fandom", "ファンダム", "粉丝");
        addLabel("가족", "Family", "家族", "家庭");
        addLabel("연인", "Couple", "恋人", "情侣");
        addLabel("오전", "Morning", "午前", "上午");
        addLabel("오후", "Afternoon", "午後", "下午");
        addLabel("저녁", "Evening", "夕方", "晚上");
        addLabel("하루코스", "One-day course", "日帰りコース", "一日路线");
        addLabel("무료", "Free", "無料", "免费");
        addLabel("유료", "Paid", "有料", "收费");
        addLabel("저예산", "Budget-friendly", "低予算", "低预算");
        addLabel("중간예산", "Mid-range budget", "中予算", "中等预算");
        addLabel("비오는날", "Rainy day", "雨の日", "雨天");
        addLabel("실내추천", "Indoor pick", "屋内おすすめ", "室内推荐");
        addLabel("맑은날", "Clear day", "晴れの日", "晴天");
        addLabel("겨울추천", "Winter pick", "冬おすすめ", "冬季推荐");
        addLabel("사진", "Photography", "写真", "摄影");
        addLabel("산책", "Walk", "散策", "散步");
        addLabel("식사", "Dining", "食事", "用餐");
        addLabel("성지순례", "Fan pilgrimage", "聖地巡礼", "圣地巡礼");

        // Place dataset localization overrides.
        addLabel("\uAC00\uAD6C", "Furniture", "Furniture shop", "Furniture shop");
        addLabel("\uAC00\uD3C9\uAD70", "Gapyeong County", "Gapyeong County", "Gapyeong County");
        addLabel("\uAC15\uB0A8\uAD6C", "Gangnam-gu", "Gangnam-gu", "Gangnam-gu");
        addLabel("\uAC15\uB989\uC2DC", "Gangneung City", "Gangneung City", "Gangneung City");
        addLabel("\uAC15\uBD81\uAD6C", "Gangbuk-gu", "Gangbuk-gu", "Gangbuk-gu");
        addLabel("\uAC15\uC6D0", "Gangwon", "\u6C5F\u539F", "\u6C5F\u539F");
        addLabel("\uAC24\uB7EC\uB9AC", "Gallery", "Gallery", "Gallery");
        addLabel("\uAC70\uB9AC", "Street", "\u901A\u308A", "\u8857\u9053");
        addLabel("\uACBD\uAE30", "Gyeonggi", "\u4EAC\u757F", "\u4EAC\u757F");
        addLabel("\uACBD\uB0A8", "Gyeongnam", "\u6176\u5357", "\u5E86\u5357");
        addLabel("\uACBD\uBD81", "Gyeongbuk", "\u6176\u5317", "\u5E86\u5317");
        addLabel("\uACE0\uC591\uC2DC", "Goyang City", "Goyang City", "Goyang City");
        addLabel("\uACE0\uCC3D\uAD70", "Gochang County", "Gochang County", "Gochang County");
        addLabel("\uACE8\uD504\uC7A5", "Golf course", "Golf course", "Golf course");
        addLabel("\uACF5\uC5F0\uC7A5", "Performance hall", "Performance hall", "Performance hall");
        addLabel("\uACF5\uC6D0", "Park", "\u516C\u5712", "\u516C\u56ED");
        addLabel("\uACF5\uC8FC\uC2DC", "Gongju City", "Gongju City", "Gongju City");
        addLabel("\uACFC\uCC9C\uC2DC", "Gwacheon City", "Gwacheon City", "Gwacheon City");
        addLabel("\uAD00\uAD11\uC9C0", "Tourist attraction", "Tourist attraction", "Tourist attraction");
        addLabel("\uAD11\uC8FC", "Gwangju", "\u5149\u5DDE", "\u5149\u5DDE");
        addLabel("\uAD11\uC9C4\uAD6C", "Gwangjin-gu", "Gwangjin-gu", "Gwangjin-gu");
        addLabel("\uAD6C\uB9AC\uC2DC", "Guri City", "Guri City", "Guri City");
        addLabel("\uAD6D\uAC00 \uC720\uC0B0", "Heritage", "\u56FD\u5BB6\u907A\u7523", "\u56FD\u5BB6\u9057\u4EA7");
        addLabel("\uAD6D\uAC00\uC720\uC0B0", "Heritage", "\u56FD\u5BB6\u907A\u7523", "\u56FD\u5BB6\u9057\u4EA7");
        addLabel("\uAD70\uC0B0\uC2DC", "Gunsan City", "Gunsan City", "Gunsan City");
        addLabel("\uAD70\uC704\uAD70", "Gunwi County", "Gunwi County", "Gunwi County");
        addLabel("\uAD7F\uC988\uC0F5", "Goods shop", "Goods shop", "Goods shop");
        addLabel("\uAD81\uAD90", "Palace", "Palace", "Palace");
        addLabel("\uAE08\uC815\uAD6C", "Geumjeong-gu", "Geumjeong-gu", "Geumjeong-gu");
        addLabel("\uAE30\uB150\uBB3C", "Monument", "Monument", "Monument");
        addLabel("\uAE40\uD3EC\uC2DC", "Gimpo City", "Gimpo City", "Gimpo City");
        addLabel("\uAE40\uD574\uC2DC", "Gimhae City", "Gimhae City", "Gimhae City");
        addLabel("\uB0A8\uAD6C", "Nam-gu", "Nam-gu", "Nam-gu");
        addLabel("\uB0A8\uD574\uAD70", "Namhae County", "Namhae County", "Namhae County");
        addLabel("\uB17C\uC0B0\uC2DC", "Nonsan City", "Nonsan City", "Nonsan City");
        addLabel("\uB2EC\uC131\uAD70", "Dalseong County", "Dalseong County", "Dalseong County");
        addLabel("\uB2F4\uC591\uAD70", "Damyang County", "Damyang County", "Damyang County");
        addLabel("\uB2F9\uC9C4\uC2DC", "Dangjin City", "Dangjin City", "Dangjin City");
        addLabel("\uB300\uAD6C", "Daegu", "\u5927\u90B1", "\u5927\u90B1");
        addLabel("\uB3C4\uBD09\uAD6C", "Dobong-gu", "Dobong-gu", "Dobong-gu");
        addLabel("\uB3C4\uC11C\uAD00", "Library", "Library", "Library");
        addLabel("\uB3D9\uAD6C", "Dong-gu", "Dong-gu", "Dong-gu");
        addLabel("\uB3D9\uB300\uBB38\uAD6C", "Dongdaemun-gu", "Dongdaemun-gu", "Dongdaemun-gu");
        addLabel("\uB9C8\uD3EC\uAD6C", "Mapo-gu", "Mapo-gu", "Mapo-gu");
        addLabel("\uB9DB\uC9D1", "Restaurant", "\u30B0\u30EB\u30E1", "\u7F8E\u98DF");
        addLabel("\uBB34\uC8FC\uAD70", "Muju County", "Muju County", "Muju County");
        addLabel("\uBB38\uACBD\uC2DC", "Mungyeong City", "Mungyeong City", "Mungyeong City");
        addLabel("\uBB38\uD654\uACF5\uAC04", "Culture space", "Cultural space", "Cultural space");
        addLabel("\uBBF8\uC220\uAD00", "Art museum", "Art museum", "Art museum");
        addLabel("\uBBF8\uCD94\uD640\uAD6C", "Michuhol-gu", "Michuhol-gu", "Michuhol-gu");
        addLabel("\uBCF4\uB839\uC2DC", "Boryeong City", "Boryeong City", "Boryeong City");
        addLabel("\uBD80\uC0B0", "Busan", "\u91DC\u5C71", "\u91DC\u5C71");
        addLabel("\uBD80\uC0B0\uC9C4\uAD6C", "Busanjin-gu", "Busanjin-gu", "Busanjin-gu");
        addLabel("\uBD80\uC5EC\uAD70", "Buyeo County", "Buyeo County", "Buyeo County");
        addLabel("\uBD81\uAD6C", "Buk-gu", "Buk-gu", "Buk-gu");
        addLabel("\uC0AC\uC6B0\uB098/\uCC1C\uC9C8\uBC29", "Sauna", "\u30B5\u30A6\u30CA\u30FB\u30C1\u30E0\u30B8\u30EB\u30D0\u30F3", "\u6851\u62FF/\u6C57\u84B8\u623F");
        addLabel("\uC0AC\uD558\uAD6C", "Saha-gu", "Saha-gu", "Saha-gu");
        addLabel("\uC0B0", "Mountain", "Mountain", "Mountain");
        addLabel("\uC0BC\uCC99\uC2DC", "Samcheok City", "Samcheok City", "Samcheok City");
        addLabel("\uC11C\uAD6C", "Seo-gu", "Seo-gu", "Seo-gu");
        addLabel("\uC11C\uADC0\uD3EC\uC2DC", "Seogwipo City", "Seogwipo City", "Seogwipo City");
        addLabel("\uC11C\uB300\uBB38\uAD6C", "Seodaemun-gu", "Seodaemun-gu", "Seodaemun-gu");
        addLabel("\uC11C\uC6B8", "Seoul", "\u30BD\u30A6\u30EB", "\u9996\u5C14");
        addLabel("\uC11C\uC810", "Bookstore", "Bookstore", "Bookstore");
        addLabel("\uC11C\uCD08\uAD6C", "Seocho-gu", "Seocho-gu", "Seocho-gu");
        addLabel("\uC12C", "Island", "Island", "Island");
        addLabel("\uC131\uB0A8\uC2DC", "Seongnam City", "Seongnam City", "Seongnam City");
        addLabel("\uC131\uB3D9\uAD6C", "Seongdong-gu", "Seongdong-gu", "Seongdong-gu");
        addLabel("\uC131\uBD81\uAD6C", "Seongbuk-gu", "\u57CE\u5317\u533A", "\u57CE\u5317\u533A");
        addLabel("\uC18D\uCD08\uC2DC", "Sokcho City", "Sokcho City", "Sokcho City");
        addLabel("\uC1A1\uD30C\uAD6C", "Songpa-gu", "Songpa-gu", "Songpa-gu");
        addLabel("\uC1FC\uD551\uC13C\uD130", "Shopping center", "Shopping center", "Shopping center");
        addLabel("\uC218\uC601\uAD6C", "Suyeong-gu", "Suyeong-gu", "Suyeong-gu");
        addLabel("\uC218\uC6D0\uC2DC", "Suwon City", "Suwon City", "Suwon City");
        addLabel("\uC219\uBC15", "Lodging", "Lodging", "Lodging");
        addLabel("\uC21C\uCC9C\uC2DC", "Suncheon City", "Suncheon City", "Suncheon City");
        addLabel("\uC2A4\uD29C\uB514\uC624", "Studio", "Studio", "Studio");
        addLabel("\uC2DC\uC7A5", "Market", "Market", "Market");
        addLabel("\uC2DC\uC7A5/\uC704\uD310\uC7A5", "Market", "Market", "Market");
        addLabel("\uC2DC\uD765\uC2DC", "Siheung City", "Siheung City", "Siheung City");
        addLabel("\uC548\uB3D9\uC2DC", "Andong City", "Andong City", "Andong City");
        addLabel("\uC548\uC131\uC2DC", "Anseong City", "Anseong City", "Anseong City");
        addLabel("\uC548\uC591\uC2DC", "Anyang City", "Anyang City", "Anyang City");
        addLabel("\uC591\uC591\uAD70", "Yangyang County", "Yangyang County", "Yangyang County");
        addLabel("\uC591\uC8FC\uC2DC", "Yangju City", "Yangju City", "Yangju City");
        addLabel("\uC591\uD3C9\uAD70", "Yangpyeong County", "Yangpyeong County", "Yangpyeong County");
        addLabel("\uC5ED/\uAD50\uD1B5", "Transit", "\u99C5\u30FB\u4EA4\u901A", "\u8F66\u7AD9/\u4EA4\u901A");
        addLabel("\uC5F0\uC218\uAD6C", "Yeonsu-gu", "Yeonsu-gu", "Yeonsu-gu");
        addLabel("\uC601\uB4F1\uD3EC\uAD6C", "Yeongdeungpo-gu", "Yeongdeungpo-gu", "Yeongdeungpo-gu");
        addLabel("\uC601\uC6D4\uAD70", "Yeongwol County", "Yeongwol County", "Yeongwol County");
        addLabel("\uC624\uC0B0\uC2DC", "Osan City", "Osan City", "Osan City");
        addLabel("\uC639\uC9C4\uAD70", "Ongjin County", "Ongjin County", "Ongjin County");
        addLabel("\uC644\uC8FC\uAD70", "Wanju County", "Wanju County", "Wanju County");
        addLabel("\uC6A9\uC0B0\uAD6C", "Yongsan-gu", "Yongsan-gu", "Yongsan-gu");
        addLabel("\uC6A9\uC778\uC2DC", "Yongin City", "Yongin City", "Yongin City");
        addLabel("\uC6B8\uB989\uAD70", "Ulleung County", "Ulleung County", "Ulleung County");
        addLabel("\uC6B8\uC0B0", "Ulsan", "\u851A\u5C71", "\u851A\u5C71");
        addLabel("\uC720\uC801\uC9C0", "Historic site", "Historic site", "Historic site");
        addLabel("\uC740\uD3C9\uAD6C", "Eunpyeong-gu", "Eunpyeong-gu", "Eunpyeong-gu");
        addLabel("\uC758\uC815\uBD80\uC2DC", "Uijeongbu City", "Uijeongbu City", "Uijeongbu City");
        addLabel("\uC774\uCC9C\uC2DC", "Icheon City", "Icheon City", "Icheon City");
        addLabel("\uC775\uC0B0\uC2DC", "Iksan City", "Iksan City", "Iksan City");
        addLabel("\uC778\uCC9C", "Incheon", "\u4EC1\u5DDD", "\u4EC1\u5DDD");
        addLabel("\uC790\uC5F0\uBA85\uC18C", "Nature spot", "Nature spot", "Nature spot");
        addLabel("\uC804\uB0A8", "Jeonnam", "\u5168\u5357", "\u5168\u5357");
        addLabel("\uC804\uB9DD\uB300", "Observatory", "Observatory", "Observatory");
        addLabel("\uC804\uBD81", "Jeonbuk", "\u5168\u5317", "\u5168\u5317");
        addLabel("\uC804\uC8FC\uC2DC", "Jeonju City", "Jeonju City", "Jeonju City");
        addLabel("\uC804\uD1B5\uB9C8\uC744", "Traditional village", "Traditional village", "Traditional village");
        addLabel("\uC808", "Temple", "Temple", "Temple");
        addLabel("\uC815\uC120\uAD70", "Jeongseon County", "Jeongseon County", "Jeongseon County");
        addLabel("\uC81C\uC8FC", "Jeju", "\u6E08\u5DDE", "\u6D4E\u5DDE");
        addLabel("\uC81C\uC8FC\uC2DC", "Jeju City", "Jeju City", "Jeju City");
        addLabel("\uC81C\uCC9C\uC2DC", "Jecheon City", "Jecheon City", "Jecheon City");
        addLabel("\uC885\uB85C\uAD6C", "Jongno-gu", "Jongno-gu", "Jongno-gu");
        addLabel("\uC911\uAD6C", "Jung-gu", "Jung-gu", "Jung-gu");
        addLabel("\uC911\uB791\uAD6C", "Jungnang-gu", "Jungnang-gu", "Jungnang-gu");
        addLabel("\uCC3D\uC6D0\uC2DC", "Changwon City", "\u660C\u539F\u5E02", "\u660C\u539F\u5E02");
        addLabel("\uCCAD\uB3C4\uAD70", "Cheongdo County", "Cheongdo County", "Cheongdo County");
        addLabel("\uCCAD\uC8FC\uC2DC", "Cheongju City", "Cheongju City", "Cheongju City");
        addLabel("\uCD2C\uC601\uC9C0", "Filming location", "\u30ED\u30B1\u5730", "\u62CD\u6444\u5730");
        addLabel("\uCD98\uCC9C\uC2DC", "Chuncheon City", "Chuncheon City", "Chuncheon City");
        addLabel("\uCDA9\uB0A8", "Chungnam", "\u5FE0\u5357", "\u5FE0\u5357");
        addLabel("\uCDA9\uBD81", "Chungbuk", "\u5FE0\u5317", "\u5FE0\u5317");
        addLabel("\uCDA9\uC8FC\uC2DC", "Chungju City", "Chungju City", "Chungju City");
        addLabel("\uCE74\uD398", "Cafe", "\u30AB\u30D5\u30A7", "\u5496\u5561\u9986");
        addLabel("\uCE74\uD398/\uAC00\uAD6C\uC810", "Cafe and furniture shop", "\u30AB\u30D5\u30A7\u30FB\u5BB6\u5177\u5E97", "\u5496\u5561\u9986/\u5BB6\u5177\u5E97");
        addLabel("\uCF00\uC774\uBE14\uCE74", "Cable car", "Cable car", "Cable car");
        addLabel("\uCF00\uC774\uBE14\uCE74/\uAD00\uAD11\uC9C0", "Cable car", "Cable car", "Cable car");
        addLabel("\uD0DC\uC548\uAD70", "Taean County", "Taean County", "Taean County");
        addLabel("\uD14C\uB9C8\uD30C\uD06C", "Theme park", "Theme park", "Theme park");
        addLabel("\uD30C\uC8FC\uC2DC", "Paju City", "Paju City", "Paju City");
        addLabel("\uD3C9\uCC3D\uAD70", "Pyeongchang County", "Pyeongchang County", "Pyeongchang County");
        addLabel("\uD3C9\uD0DD\uC2DC", "Pyeongtaek City", "Pyeongtaek City", "Pyeongtaek City");
        addLabel("\uD3EC\uCC9C\uC2DC", "Pocheon City", "Pocheon City", "Pocheon City");
        addLabel("\uD3EC\uD56D\uC2DC", "Pohang City", "Pohang City", "Pohang City");
        addLabel("\uD558\uB3D9\uAD70", "Hadong County", "Hadong County", "Hadong County");
        addLabel("\uD559\uAD50", "School", "\u5B66\u6821", "\u5B66\u6821");
        addLabel("\uD55C\uC625/\uBB38\uD654\uACF5\uAC04", "Hanok cultural space", "\u97D3\u5C4B\u6587\u5316\u7A7A\u9593", "\u97E9\u5C4B\u6587\u5316\u7A7A\u95F4");
        addLabel("\uD568\uC548\uAD70", "Haman County", "Haman County", "Haman County");
        addLabel("\uD574\uBCC0", "Beach", "Beach", "Beach");
        addLabel("\uD574\uC6B4\uB300\uAD6C", "Haeundae-gu", "Haeundae-gu", "Haeundae-gu");
        addLabel("\uD64D\uCC9C\uAD70", "Hongcheon County", "Hongcheon County", "Hongcheon County");
        addLabel("\uD654\uC131\uC2DC", "Hwaseong City", "Hwaseong City", "Hwaseong City");
        addPlaceName("P_0002", "Seoul Land", "ソウルランド", "首尔乐园");
        addPlaceName("P_0006", "Yangjae Citizen's Forest Station", "良才市民の森駅", "良才市民之林站");
        addPlaceName("P_0026", "Gyeongbokgung Palace", "景福宮", "景福宫");
        addPlaceName("P_0027", "Unhyeongung Yanggwan", "雲峴宮洋館", "云岘宫洋馆");
        addPlaceName("P_0055", "Hwaseong Haenggung Palace", "華城行宮", "华城行宫");
        addPlaceName("P_0020", "Grand Walkerhill Seoul", "\u30B0\u30E9\u30F3\u30C9\u30A6\u30A9\u30FC\u30AB\u30FC\u30D2\u30EB\u30BD\u30A6\u30EB", "\u9996\u5C14\u683C\u5170\u5FB7\u534E\u514B\u5C71\u5E84");
        addPlaceName("P_0039", "Gyeongwonjae Ambassador Incheon", "\u6176\u6E90\u9F4B\u30A2\u30F3\u30D0\u30B5\u30C0\u30FC\u4EC1\u5DDD", "\u4EC1\u5DDD\u5E86\u6E90\u658B\u5927\u4F7F\u9152\u5E97");
        addPlaceName("P_0064", "Lotte Department Store Gangnam Branch", "\u30ED\u30C3\u30C6\u767E\u8CA8\u5E97 \u6C5F\u5357\u5E97", "\u4E50\u5929\u767E\u8D27\u6C5F\u5357\u5E97");
        addPlaceName("P_0083", "Wolfgang's Steakhouse", "\u30A6\u30EB\u30D5\u30AE\u30E3\u30F3\u30B0\u30FB\u30B9\u30C6\u30FC\u30AD\u30CF\u30A6\u30B9", "\u6C83\u5C14\u592B\u5188\u725B\u6392\u9986");
        addPlaceName("P_0106", "Seonungak", "\u30BD\u30CC\u30F3\u30AC\u30AF", "\u4ED9\u4E91\u9601");
        addPlaceName("P_0114", "Oakwood Premier Incheon", "\u30AA\u30FC\u30AF\u30A6\u30C3\u30C9\u30D7\u30EC\u30DF\u30A2\u4EC1\u5DDD", "\u4EC1\u5DDD\u5965\u514B\u4F0D\u5FB7\u5C0A\u8D35\u9152\u5E97");
        addPlaceName("P_0138", "Oryukdo Skywalk", "\u4E94\u516D\u5CF6\u30B9\u30AB\u30A4\u30A6\u30A9\u30FC\u30AF", "\u4E94\u516D\u5C9B\u5929\u7A7A\u6B65\u9053");
        addPlaceName("P_0167", "Mungyeong Yongdu-ri Super Set", "\u805E\u6176\u9F8D\u982D\u91CC\u30B9\u30FC\u30D1\u30FC\u30BB\u30C3\u30C8", "\u95FB\u5E86\u9F99\u5934\u91CC\u8D85\u5E02\u5E03\u666F");
        addPlaceName("P_0169", "Mungyeong Eco World", "\u805E\u6176\u30A8\u30B3\u30EF\u30FC\u30EB\u30C9", "\u95FB\u5E86\u751F\u6001\u4E16\u754C");
        addPlaceName("P_0170", "Grand Walkerhill Seoul Aston House", "\u30B0\u30E9\u30F3\u30C9\u30A6\u30A9\u30FC\u30AB\u30FC\u30D2\u30EB\u30BD\u30A6\u30EB \u30A2\u30B9\u30C8\u30F3\u30CF\u30A6\u30B9", "\u9996\u5C14\u683C\u5170\u5FB7\u534E\u514B\u5C71\u5E84 Aston House");
        addPlaceName("P_0168", "Mungyeong Rail Bike Gurang-ri Station", "\u805E\u6176\u9244\u9053\u81EA\u8EE2\u8ECA \u4E5D\u90CE\u91CC\u99C5", "\u95FB\u5E86\u94C1\u8DEF\u81EA\u884C\u8F66\u4E5D\u90CE\u91CC\u7AD9");
        addPlaceName("P_0171", "Aqua Planet Ilsan", "\u30A2\u30AF\u30A2\u30D7\u30E9\u30CD\u30C3\u30C8\u4E00\u5C71", "Aqua Planet \u4E00\u5C71");
        addPlaceName("P_0172", "Sayuwon", "\u30B5\u30E6\u30A6\u30A9\u30F3", "\u601D\u60DF\u56ED");
        addPlaceName("P_0173", "Chungju Geonji Village", "\u5FE0\u5DDE\u4E7E\u829D\u6751", "\u5FE0\u5DDE\u4E7E\u829D\u6751");
        addPlaceName("P_0174", "Seongsan Ilchulbong", "城山日出峰", "城山日出峰");
        addPlaceName("P_0177", "Beach in front of Delmundo Gimnyeong", "デルムンド金寧店前ビーチ", "Delmundo 金宁店前海滩");
        addPlaceName("P_0178", "Baegyagi Oreum", "ペギャギオルム", "百药伊岳");
        addPlaceName("P_0179", "Andol Oreum", "アンドルオルム", "安突岳");
        addPlaceName("P_0186", "Neopge Oreum", "ノプケオルム", "讷盖岳");
        addPlaceName("P_0189", "Hallim Fisheries Cooperative Fish Market", "翰林水協魚市場", "翰林水协鱼市");
        addPlaceName("P_0193", "Gunsan Oreum", "クンサンオルム", "军山岳");
        addPlaceName("P_0199", "Sangyeo Oreum", "サンヨオルム", "Sangyeo Oreum");
        addPlaceName("P_0213", "COEX K-POP Square", "COEX K-POP広場", "COEX K-POP广场");
        addPlaceName("P_0233", "Yongnuni Oreum", "ヨンヌニオルム", "龙眼岳");
        addPlaceName("P_0069", "Lotte World", "ロッテワールド", "乐天世界");
        addPlaceName("P_0108", "Suyeonsanbang", "スヨンサンバン", "寿砚山房");
        addPlaceName("P_0150", "Korean Stone Art Museum", "韓国石造美術館", "韩国石造艺术博物馆");
        addPlaceName("P_0152", "Seoul Museum of History", "ソウル歴史博物館", "首尔历史博物馆");
        addPlaceName("P_0184", "Jeju Stone Park", "済州石文化公園", "济州石文化公园");
        addPlaceName("P_0200", "Haenyeo Museum", "海女博物館", "海女博物馆");
        addPlaceName("P_0202", "Asia Culture Center", "国立アジア文化殿堂", "国立亚洲文化殿堂");
        addPlaceName("P_0211", "Bukchon Hanok Village", "北村韓屋村", "北村韩屋村");
        addPlaceName("P_0214", "N Seoul Tower", "Nソウルタワー", "N首尔塔");
        addPlaceName("P_0215", "Naksan Park", "駱山公園", "骆山公园");
        addPlaceName("P_0216", "Myeongdong Street", "明洞通り", "明洞街");
        addPlaceName("P_0217", "Jamsil Hangang Park", "蚕室漢江公園", "蚕室汉江公园");
        addPlaceName("P_0218", "Seoul Olympic Stadium", "ソウルオリンピック主競技場", "首尔奥林匹克主竞技场");
        addPlaceName("P_0219", "Lotte World Tower Seoul Sky", "ロッテワールドタワー ソウルスカイ", "乐天世界塔首尔天空");
        addPlaceName("P_0231", "Woljeongri Beach", "月汀里海水浴場", "月汀里海水浴场");
        addPlaceName("P_0236", "Eulwangni Beach", "乙旺里海水浴場", "乙旺里海水浴场");
        addPlaceName("P_0237", "Nami Island", "南怡島", "南怡岛");
        addPlaceName("P_0244", "Dokdo Observatory", "独島展望台", "独岛展望台");
        addPlaceName("P_0245", "Dokdo", "独島", "独岛");
        addPlaceName("P_0250", "Paradise City", "パラダイスシティ", "百乐达斯城");
        addPlaceName("P_0251", "INSPIRE Entertainment Resort", "インスパイア・エンターテインメント・リゾート", "INSPIRE 娱乐度假村");
        addPlaceName("P_0252", "Unhyeongung Yanggwan", "雲峴宮洋館", "云岘宫洋馆");
        addPlaceName("P_0253", "Seonungak", "ソヌンガク", "仙云阁");
        addPlaceName("P_0254", "Bonghwanggak", "鳳凰閣", "凤凰阁");
        addPlaceName("P_0255", "BaekInje's House Museum", "白麟済家屋", "白麟济家屋");
        addPlaceName("P_0256", "Bukchon Asian Cultural Art Museum", "北村東洋文化博物館", "北村东方文化博物馆");
        addPlaceName("P_0257", "Choi Sunu House", "チェ・スヌ旧宅", "崔淳雨故居");
        addPlaceName("P_0260", "Baekje Cultural Land", "百済文化団地", "百济文化园区");
        addPlaceName("P_0261", "Kyungnam University", "慶南大学校", "庆南大学");
        addPlaceName("P_0262", "BTS Bus Stop", "BTSバス停", "BTS巴士站");
        addPlaceName("P_0263", "Hamaengbang Beach", "ハメンバン海水浴場", "下孟芳海水浴场");
        addPlaceName("P_0264", "Goyang Tourist Information Center", "高陽観光情報センター", "高阳旅游信息中心");
        addPlaceName("P_0265", "Seohuri Forest", "ソフリの森", "西厚里森林");
        addPlaceName("P_0266", "Everland Rocksville", "エバーランド・ロックスビル", "爱宝乐园 Rocksville");
        addPlaceName("P_0267", "Korea Furniture Museum", "韓国家具博物館", "韩国家具博物馆");
        addPlaceName("P_0268", "Awon Historic House", "アウォン古宅", "我园古宅");
        addPlaceName("P_0271", "Korea House", "韓国の家", "韩国之家");
        addPlaceName("P_0272", "Nodeul Island", "ノドゥル島", "Nodeul岛");
        addPlaceName("P_0273", "Songpa Naru Park Seokchon Lake", "松坡ナル公園 石村湖", "松坡渡口公园石村湖");
        addPlaceName("P_0275", "Iho Tewoo Beach", "梨湖テウ海岸", "梨湖泰乌海边");
        addPlaceName("P_0278", "Snoopy Garden", "スヌーピーガーデン", "史努比庭园");
        addPlaceName("P_0279", "Gamcheon Culture Village", "甘川文化村", "甘川文化村");
        addPlaceName("P_0281", "Busan Citizens Park", "釜山市民公園", "釜山市民公园");
        // Extended English place-name quality overrides.
        addPlaceName("P_0001", "KINTEX", "킨텍스", "킨텍스");
        addPlaceName("P_0007", "Namsan Walking Trail", "남산 산책로", "남산 산책로");
        addPlaceName("P_0008", "Seongapdo Island", "선갑도", "선갑도");
        addPlaceName("P_0009", "My Land", "마이랜드", "마이랜드");
        addPlaceName("P_0013", "Daegwallyeong Samyang Roundhill", "대관령 삼양라운드힐", "대관령 삼양라운드힐");
        addPlaceName("P_0014", "Balwangsan Cable Car", "발왕산 관광케이블카", "발왕산 관광케이블카");
        addPlaceName("P_0015", "Woljeongsa Fir Forest Trail", "월정사 전나무 숲길", "월정사 전나무 숲길");
        addPlaceName("P_0019", "Konkuk University", "건국대학교", "건국대학교");
        addPlaceName("P_0024", "Sageun-Yongdap Pedestrian Bridge", "사근용답간인도교", "사근용답간인도교");
        addPlaceName("P_0029", "Birch Tree Story", "자작나무이야기", "자작나무이야기");
        addPlaceName("P_0035", "Hanmi Bookstore", "한미서점", "한미서점");
        addPlaceName("P_0037", "Dream Park", "드림파크", "드림파크");
        addPlaceName("P_0038", "Cheongna Lake Park", "청라호수공원", "청라호수공원");
        addPlaceName("P_0044", "Neoreundeul Hagwon Farm (formerly Borinara Hagwon Farm)", "넓은들 학원농장 /(구) 보리나라 학원농장", "넓은들 학원농장 /(구) 보리나라 학원농장");
        addPlaceName("P_0047", "Seokbyeong 1-ri Breakwater", "석병1리방파제", "석병1리방파제");
        addPlaceName("P_0048", "Cheongha Five-day Market", "청하5일장", "청하5일장");
        addPlaceName("P_0051", "The River", "더리버", "더리버");
        addPlaceName("P_0052", "Seoul Dragon City", "서울드래곤시티", "서울드래곤시티");
        addPlaceName("P_0053", "Ravie Books", "라비브북스", "라비브북스");
        addPlaceName("P_0054", "Cafe Hwahong", "카페화홍", "카페화홍");
        addPlaceName("P_0056", "Jidong Market Sundae Town", "지동시장 순대타운", "지동시장 순대타운");
        addPlaceName("P_0063", "The Conran Shop Korea", "더콘란샵코리아", "더콘란샵코리아");
        addPlaceName("P_0072", "Daily Beer Yeonsinnae Branch", "생활맥주 연신내점", "생활맥주 연신내점");
        addPlaceName("P_0073", "JCC Creative Center", "JCC크리에이티브센터", "JCC크리에이티브센터");
        addPlaceName("P_0074", "Triple Street", "트리플 스트리트", "트리플 스트리트");
        addPlaceName("P_0077", "Onbit Natural Recreation Forest", "온빛자연휴양림", "온빛자연휴양림");
        addPlaceName("P_0078", "Boryeong Green Barley Field", "보령 청보리밭", "보령 청보리밭");
        addPlaceName("P_0079", "Byeolmaro Observatory", "별마로천문대", "별마로천문대");
        addPlaceName("P_0080", "Hongsam Spa Charcoal Kiln Sauna", "홍삼스파 참숯가마 사우나", "홍삼스파 참숯가마 사우나");
        addPlaceName("P_0081", "Pocheon Hantangang Sky Bridge", "포천한탄강 하늘다리", "포천한탄강 하늘다리");
        addPlaceName("P_0082", "Commodore Hotel Busan", "코모도호텔 부산", "코모도호텔 부산");
        addPlaceName("P_0084", "BBQ Heliocity Branch", "BBQ 헬리오시티점", "BBQ 헬리오시티점");
        addPlaceName("P_0085", "Unsan", "운산", "운산");
        addPlaceName("P_0086", "Jemulpo Club", "신제물포구락부", "신제물포구락부");
        addPlaceName("P_0091", "Forest of Healing", "치유의 숲", "치유의 숲");
        addPlaceName("P_0092", "Cheongsan Arboretum", "청산수목원", "청산수목원");
        addPlaceName("P_0095", "Tangeumho Rainbow Road", "탄금호 무지개길", "탄금호 무지개길");
        addPlaceName("P_0097", "Vivaldi Park", "대명비발디파크", "대명비발디파크");
        addPlaceName("P_0098", "Wondang Stud Farm", "원당종마목장", "원당종마목장");
        addPlaceName("P_0099", "Bellacitta", "벨라시타", "벨라시타");
        addPlaceName("P_0100", "Asosan Ilsan Main Branch", "아소산 일산본점", "아소산 일산본점");
        addPlaceName("P_0102", "Bom Farmers Garden", "봄파머스 가든", "봄파머스 가든");
        addPlaceName("P_0103", "Kare Klint Bundang-Yongin Branch", "카레클린트 분당용인점", "카레클린트 분당용인점");
        addPlaceName("P_0104", "Forest of Wisdom", "지혜의 숲", "지혜의 숲");
        addPlaceName("P_0105", "Muddy Murphy's", "머디머피스", "머디머피스");
        addPlaceName("P_0109", "Hambakuteum Sundae Gukbap", "함박웃음순대국", "함박웃음순대국");
        addPlaceName("P_0110", "L'Amant Secret", "라망시크레", "라망시크레");
        addPlaceName("P_0111", "L'Escape Hotel", "레스케이프 호텔", "레스케이프 호텔");
        addPlaceName("P_0112", "Yongin Dae Jang Geum Park", "용인 대장금 파크", "용인 대장금 파크");
        addPlaceName("P_0113", "Byeongsan Seowon Confucian Academy", "병산서원", "병산서원");
        addPlaceName("P_0116", "Ocean World", "오션월드", "오션월드");
        addPlaceName("P_0117", "Petite France", "쁘띠프랑스", "쁘띠프랑스");
        addPlaceName("P_0119", "The Nurungji", "더 누룽지", "더 누룽지");
        addPlaceName("P_0120", "Gimpo Art Village", "김포아트빌리지", "김포아트빌리지");
        addPlaceName("P_0121", "Siheung Oido Prehistoric Park", "시흥 오이도 선사유적공원", "시흥 오이도 선사유적공원");
        addPlaceName("P_0122", "Lighthouse Sashimi Restaurant", "등대횟집", "등대횟집");
        addPlaceName("P_0123", "Cheongchun Jogae Main Branch", "청춘조개본점", "청춘조개본점");
        addPlaceName("P_0126", "Myeongga Donkatsu", "명가돈까스", "명가돈까스");
        addPlaceName("P_0127", "Simmons Terrace", "시몬스 테라스점", "시몬스 테라스점");
        addPlaceName("P_0128", "Mulbit Haneul Garden Main Branch", "물빛하늘정원 본점", "물빛하늘정원 본점");
        addPlaceName("P_0130", "First Garden", "퍼스트가든", "퍼스트가든");
        addPlaceName("P_0131", "Speed Park", "스피드파크", "스피드파크");
        addPlaceName("P_0132", "Utdari Culture Village", "웃다리문화촌", "웃다리문화촌");
        addPlaceName("P_0133", "Pyeonggang Land", "평강랜드", "평강랜드");
        addPlaceName("P_0135", "Korea Zipwire", "코리아 짚와이어", "코리아 짚와이어");
        addPlaceName("P_0140", "Yeongjin Dwaeji Gukbap Costco Centum Branch", "영진돼지국밥코스트코센텀점", "영진돼지국밥코스트코센텀점");
        addPlaceName("P_0142", "Wonjo Milmyeon", "원조밀면", "원조밀면");
        addPlaceName("P_0143", "Saeachim Restaurant", "새아침식당", "새아침식당");
        addPlaceName("P_0144", "Alver", "알베르", "알베르");
        addPlaceName("P_0146", "Bongmilga Gangnam-gu Office Branch", "봉밀가 강남구청점", "봉밀가 강남구청점");
        addPlaceName("P_0147", "Neulpureun Mokjang Sangam Branch", "늘푸른목장 상암점", "늘푸른목장 상암점");
        addPlaceName("P_0148", "Oil Tank Culture Park", "문화비축기지", "문화비축기지");
        addPlaceName("P_0149", "Sigoljip Cheonggukjang", "시골집청국장", "시골집청국장");
        addPlaceName("P_0151", "Pizzeria O", "핏제리아오", "핏제리아오");
        addPlaceName("P_0153", "Yongma Land", "용마랜드", "용마랜드");
        addPlaceName("P_0154", "Jangsaengpo Whale Culture Village", "장생포 고래문화마을", "장생포 고래문화마을");
        addPlaceName("P_0155", "Sammok Ferry Terminal", "삼목선착장", "삼목선착장");
        addPlaceName("P_0156", "Moon China", "문차이나", "문차이나");
        addPlaceName("P_0159", "Gongsanseong Fortress", "공산성", "공산성");
        addPlaceName("P_0162", "Myeongam Boat Dock", "명암보트장", "명암보트장");
        addPlaceName("P_0165", "Haenggung-dong King's Alley", "행궁동 왕의 골목", "행궁동 왕의 골목");
        addPlaceName("P_0190", "Geumneung Port", "금능포구", "금능포구");
        addPlaceName("P_0192", "Deokcheon-ri Drive Course", "덕천리 드라이브 코스", "덕천리 드라이브 코스");
        addPlaceName("P_0194", "Yeongnak-ri Coastal Road", "영락리 해안도로", "영락리 해안도로");
        addPlaceName("P_0196", "Jongdal Village Road", "종달 마을길", "종달 마을길");
        addPlaceName("P_0197", "Jungmun Duty Free Shop", "중문면세점", "중문면세점");
        addPlaceName("P_0198", "Seogwipo KAL Hotel", "서귀포칼호텔", "서귀포칼호텔");
        addPlaceName("P_0201", "Ora-dong Buckwheat Field", "오라동메밀밭", "오라동메밀밭");
        addPlaceName("P_0203", "Daemunbawi Rock", "대문바위", "대문바위");
        addPlaceName("P_0204", "Hyangrobong Peak", "향로봉", "향로봉");
        addPlaceName("P_0205", "Gyeongsang-ri Zelkova Tree", "경상리 느티나무", "경상리 느티나무");
        addPlaceName("P_0206", "Changsin-dong Alley", "창신동 골목", "창신동 골목");
        addPlaceName("P_0208", "Yeongwol Jangneung Royal Tomb", "영월 장릉", "영월 장릉");
        addPlaceName("P_0210", "Danjong History Museum", "단종역사관", "단종역사관");
        addPlaceName("P_0220", "Yeongnangho Beombawi Rock", "영랑호 범바위", "영랑호 범바위");
        addPlaceName("P_0222", "Park Hyatt Busan", "파크하얏트 부산", "파크하얏트 부산");
        addPlaceName("P_0230", "Tindertown", "틴더타운", "틴더타운");
        addPlaceName("P_0232", "Flowave", "플로웨이브", "플로웨이브");
        addPlaceName("P_0234", "Dongbok Haenyeo Seafood Direct Sales Market", "동복해녀해산물직매장", "동복해녀해산물직매장");
        addPlaceName("P_0239", "Healing Stay KOSMOS", "코스모스 울릉도", "코스모스 울릉도");
        addPlaceName("P_0243", "Taeha Hyangmok Monorail", "태하향목 모노레일", "태하향목 모노레일");
        addPlaceName("P_0246", "Coffee Gieop Apgujeong Branch", "커피기업 압구정점", "커피기업 압구정점");
        addPlaceName("P_0247", "Cheonjangjigu Cafe", "천장지구 카페", "천장지구 카페");
        addPlaceName("P_0248", "MIMIMI Garden Cafe", "미미미가든카페", "미미미가든카페");
        addPlaceName("P_0249", "Saseungbongdo Island", "사승봉도", "사승봉도");
        addContentSourceTitle("\uC120\uC7AC \uC5C5\uACE0 \uD280\uC5B4", "C_DRAMA_001");
        addContentSourceTitle("\uB208\uBB3C\uC758 \uC5EC\uC655", "C_DRAMA_002");
        addContentSourceTitle("\uD3ED\uC2F9 \uC18D\uC558\uC218\uB2E4", "C_DRAMA_003");
        addContentSourceTitle("\uC624\uC9D5\uC5B4 \uAC8C\uC784", "C_DRAMA_004");
        addContentSourceTitle("\uB3C4\uAE68\uBE44", "C_DRAMA_005");
        addContentSourceTitle("\uAC2F\uB9C8\uC744 \uCC28\uCC28\uCC28", "C_DRAMA_006");
        addContentSourceTitle("\uADF8 \uD574 \uC6B0\uB9AC\uB294", "C_DRAMA_007");
        addContentSourceTitle("\uC0AC\uB791\uC758 \uBD88\uC2DC\uCC29", "C_DRAMA_008");
        addContentSourceTitle("\uC774 \uC0AC\uB791 \uD1B5\uC5ED \uB418\uB098\uC694?", "C_DRAMA_009");
        addContentSourceTitle("21\uC138\uAE30 \uB300\uAD70\uBD80\uC778", "C_DRAMA_010");
        addContentSourceTitle("\uBD80\uBD80\uC758 \uC138\uACC4", "C_DRAMA_011");
        addContentSourceTitle("\uCCA0\uC778\uC655\uD6C4", "C_DRAMA_012");
        addContentSourceTitle("\uD30C\uBB18", "C_MOVIE_001");
        addContentSourceTitle("\uBC94\uC8C4\uB3C4\uC2DC4", "C_MOVIE_002");
        addContentSourceTitle("\uC655\uACFC \uC0AC\uB294 \uB0A8\uC790", "C_MOVIE_003");
        addContentSourceTitle("\uCF00\uC774\uD31D \uB370\uBAAC \uD5CC\uD130\uC2A4", "C_MOVIE_004");
        addContentSourceTitle("\uD5E4\uC5B4\uC9C8 \uACB0\uC2EC", "C_MOVIE_005");
        addContentSourceTitle("\uD658\uC2B9\uC5F0\uC560", "C_VARIETY_001");
        addContentSourceTitle("\uB7F0\uB2DD\uB9E8", "C_VARIETY_002");
        addContentSourceTitle("\uB300\uD658\uC7A5 \uAE30\uC548\uC7A5", "C_VARIETY_003");
        addContentSourceTitle("\uD551\uACC4\uACE0", "C_VARIETY_004");
        addContentSourceTitle("\uC194\uB85C\uC9C0\uC625", "C_VARIETY_005");
        addContentTitleAlias("\uC624\uC9D5\uC5B4\uAC8C\uC7842", "Squid Game 2", "\u30A4\u30AB\u30B2\u30FC\u30E02", "\u9C7F\u9C7C\u6E38\u620F2");
        addContentTitleAlias("BTS", "BTS", "BTS", "BTS");
        // End place dataset localization overrides.


        addContentTitle("C_DRAMA_001", "Lovely Runner", "ソンジェ背負って走れ", "背着善宰跑");
        addContentTitle("C_DRAMA_002", "Queen of Tears", "涙の女王", "泪之女王");
        addContentTitle("C_DRAMA_003", "When Life Gives You Tangerines", "おつかれさま", "苦尽柑来遇见你");
        addContentTitle("C_DRAMA_004", "Squid Game", "イカゲーム", "鱿鱼游戏");
        addContentTitle("C_DRAMA_005", "Goblin", "トッケビ", "孤单又灿烂的神－鬼怪");
        addContentTitle("C_DRAMA_006", "Hometown Cha-Cha-Cha", "海街チャチャチャ", "海岸村恰恰恰");
        addContentTitle("C_DRAMA_007", "Our Beloved Summer", "その年、私たちは", "那年，我们的夏天");
        addContentTitle("C_DRAMA_008", "Crash Landing on You", "愛の不時着", "爱的迫降");
        addContentTitle("C_DRAMA_009", "Can This Love Be Translated?", "この恋、通訳できますか？", "这段恋爱可以翻译吗？");
        addContentTitle("C_DRAMA_010", "The 21st Century Grand Prince's Wife", "21世紀の大君夫人", "21世纪大君夫人");
        addContentTitle("C_DRAMA_011", "The World of the Married", "夫婦の世界", "夫妻的世界");
        addContentTitle("C_DRAMA_012", "Mr. Queen", "哲仁王后", "哲仁王后");
        addContentTitle("C_MOVIE_001", "Exhuma", "破墓", "破墓");
        addContentTitle("C_MOVIE_002", "The Roundup: Punishment", "犯罪都市 PUNISHMENT", "犯罪都市4");
        addContentTitle("C_MOVIE_003", "The King and the Clown", "王の男", "王的男人");
        addContentTitle("C_MOVIE_004", "KPop Demon Hunters", "KPop Demon Hunters", "KPop Demon Hunters");
        addContentTitle("C_MOVIE_005", "Decision to Leave", "別れる決心", "分手的决心");
        addContentTitle("C_VARIETY_001", "EXchange", "乗り換え恋愛", "换乘恋爱");
        addContentTitle("C_VARIETY_002", "Running Man", "ランニングマン", "Running Man");
        addContentTitle("C_VARIETY_003", "Kian's Bizarre B&B", "奇案の民宿", "奇案84民宿");
        addContentTitle("C_VARIETY_004", "Pinggyego", "ピンゲゴ", "Pinggyego");
        addContentTitle("C_VARIETY_005", "Single's Inferno", "脱出おひとり島", "单身即地狱");
    }

    public String normalizeLocale(String headerOrLocale) {
        if (headerOrLocale == null || headerOrLocale.isBlank()) return "ko";
        for (String rawPart : headerOrLocale.split(",")) {
            String candidate = rawPart.split(";", 2)[0].trim().replace('_', '-').toLowerCase(Locale.ROOT);
            if (candidate.isBlank()) continue;
            if (candidate.startsWith("zh")) return "zh-CN";
            if (candidate.startsWith("en")) return "en";
            if (candidate.startsWith("ja")) return "ja";
            if (candidate.startsWith("ko")) return "ko";
        }
        return "ko";
    }

    public ContentDto localize(ContentDto dto, String headerOrLocale) {
        if (dto == null) return null;
        String locale = normalizeLocale(headerOrLocale);
        if ("ko".equals(locale)) return dto;
        List<String> sourceTags = dto.originalTags() == null || dto.originalTags().isEmpty() ? dto.tags() : dto.originalTags();
        return new ContentDto(
                dto.contentId(),
                translateContentTitle(dto.contentId(), original(dto.originalTitle(), dto.title()), locale),
                translateLabel(original(dto.originalCategory(), dto.category()), locale),
                translateLabel(original(dto.originalSubcategory(), dto.subcategory()), locale),
                dto.releaseYear(),
                dto.status(),
                original(dto.originalDescription(), dto.description()),
                original(dto.originalCastNames(), dto.castNames()),
                dto.thumbnailUrl(),
                dto.isPopular(),
                dto.country(),
                dto.ottPlatform(),
                translateLabels(sourceTags, locale),
                dto.likeCount(),
                dto.viewCount(),
                dto.relatedPlaceCount(),
                dto.isLiked(),
                original(dto.originalTitle(), dto.title()),
                original(dto.originalCategory(), dto.category()),
                original(dto.originalSubcategory(), dto.subcategory()),
                original(dto.originalDescription(), dto.description()),
                original(dto.originalCastNames(), dto.castNames()),
                sourceTags == null ? List.of() : List.copyOf(sourceTags),
                locale
        );
    }

    public ContentCategoryDto localize(ContentCategoryDto dto, String headerOrLocale) {
        if (dto == null) return null;
        String locale = normalizeLocale(headerOrLocale);
        if ("ko".equals(locale)) return dto;
        return new ContentCategoryDto(dto.categoryId(), translateLabel(dto.name(), locale));
    }

    public RelatedContentDto localize(RelatedContentDto dto, String headerOrLocale) {
        if (dto == null) return null;
        String locale = normalizeLocale(headerOrLocale);
        if ("ko".equals(locale)) return dto;
        String originalTitle = original(dto.originalTitle(), dto.title());
        String originalCategory = original(dto.originalCategory(), dto.category());
        return new RelatedContentDto(
                dto.contentId(),
                translateContentTitle(dto.contentId(), originalTitle, locale),
                translateLabel(originalCategory, locale),
                dto.thumbnailUrl(),
                dto.releaseYear(),
                originalTitle,
                originalCategory,
                locale
        );
    }

    public PlaceDto localize(PlaceDto dto, String headerOrLocale) {
        if (dto == null) return null;
        String locale = normalizeLocale(headerOrLocale);
        if ("ko".equals(locale)) return dto;
        String originalName = original(dto.originalName(), dto.name());
        String originalPlaceName = original(dto.originalPlaceName(), dto.placeName());
        String originalRegion = original(dto.originalRegion(), dto.region());
        String originalDistrict = original(dto.originalDistrict(), dto.district());
        String originalCategory = original(dto.originalCategory(), dto.category());
        String originalDescription = original(dto.originalDescription(), dto.description());
        List<RelatedContentDto> relatedContents = dto.relatedContents() == null
                ? List.of()
                : dto.relatedContents().stream().map(content -> localize(content, locale)).toList();
        return new PlaceDto(
                dto.placeId(),
                translatePlaceName(dto.placeId(), originalName, locale),
                translatePlaceName(dto.placeId(), originalPlaceName, locale),
                dto.address(),
                translateLabel(originalRegion, locale),
                translateLabel(originalDistrict, locale),
                translateLabel(originalCategory, locale),
                translatePlaceDescription(originalDescription, originalCategory, originalRegion, originalDistrict, locale),
                dto.openingHours(),
                dto.closedDays(),
                dto.phoneNumber(),
                dto.thumbnailUrl(),
                dto.imageUrl(),
                dto.indoorOutdoor(),
                dto.parkingAvailable(),
                dto.latitude(),
                dto.longitude(),
                dto.likeCount(),
                dto.isLiked(),
                dto.visitStatus(),
                dto.reviewCount(),
                dto.averageRating(),
                relatedContents,
                dto.verified(),
                originalName,
                originalPlaceName,
                originalRegion,
                originalDistrict,
                originalCategory,
                originalDescription,
                locale
        );
    }

    private String translatePlaceName(String placeId, String fallback, String locale) {
        Map<String, String> translations = PLACE_NAMES.get(placeId);
        if (translations == null) return fallback;
        return translations.getOrDefault(locale, fallback);
    }

    private String translatePlaceDescription(String description, String category, String region, String district, String locale) {
        String source = description == null ? "" : description.trim();
        if (source.isBlank()) return description;
        String categoryLabel = translateLabel(category, locale);
        if (categoryLabel == null || categoryLabel.isBlank()) categoryLabel = "en".equals(locale) ? "place" : "ja".equals(locale) ? "\u5834\u6240" : "\u5730\u70B9";
        String regionLabel = translateLabel(region, locale);
        String districtLabel = translateLabel(district, locale);
        String location = joinLocation(districtLabel, regionLabel, locale);
        String contentTitle = contentTitleFromDescription(source, locale);
        if ("en".equals(locale)) {
            String lowerCategory = categoryLabel.toLowerCase(Locale.ROOT);
            return contentTitle.isBlank()
                    ? "A verified " + lowerCategory + (location.isBlank() ? "" : " in " + location) + "."
                    : "A verified " + lowerCategory + (location.isBlank() ? "" : " in " + location) + " connected to " + contentTitle + ".";
        }
        if ("ja".equals(locale)) {
            String placeLabel = (location.isBlank() ? "" : location + "\u306E") + categoryLabel;
            return contentTitle.isBlank()
                    ? placeLabel + "\u3067\u3059\u3002"
                    : contentTitle + "\u306B\u95A2\u9023\u3059\u308B" + placeLabel + "\u3067\u3059\u3002";
        }
        if ("zh-CN".equals(locale)) {
            String locationText = location.isBlank() ? "" : "\u4F4D\u4E8E" + location + "\uFF0C";
            String contentText = contentTitle.isBlank() ? "" : "\u4E0E" + contentTitle + "\u76F8\u5173\u7684";
            return locationText + contentText + categoryLabel + "\u3002";
        }
        return description;
    }

    private String joinLocation(String district, String region, String locale) {
        String left = district == null ? "" : district.trim();
        String right = region == null ? "" : region.trim();
        if (left.isBlank()) return right;
        if (right.isBlank()) return left;
        return left + ("en".equals(locale) ? ", " : "\u3001") + right;
    }

    private String contentTitleFromDescription(String description, String locale) {
        String source = description == null ? "" : description.trim();
        if (!source.startsWith("[")) return "";
        int endIndex = source.indexOf(']');
        if (endIndex <= 1) return "";
        String title = source.substring(1, endIndex).trim();
        Map<String, String> alias = CONTENT_TITLE_ALIASES.get(normalizeSourceTitle(title));
        if (alias != null) return alias.getOrDefault(locale, title);
        String contentId = CONTENT_SOURCE_IDS.get(normalizeSourceTitle(title));
        return translateContentTitle(contentId, title, locale);
    }

    private String normalizeSourceTitle(String title) {
        return title == null ? "" : title.replaceAll("\\s+", "");
    }

    private String translateContentTitle(String contentId, String fallback, String locale) {
        Map<String, String> translations = CONTENT_TITLES.get(contentId);
        if (translations == null) return fallback;
        return translations.getOrDefault(locale, fallback);
    }

    private String translateLabel(String value, String locale) {
        String source = value == null ? null : value.trim();
        if (source == null || source.isBlank()) return value;
        Map<String, String> translations = LABELS.get(source);
        if (translations == null) return value;
        return translations.getOrDefault(locale, value);
    }

    private List<String> translateLabels(List<String> values, String locale) {
        if (values == null || values.isEmpty()) return List.of();
        return values.stream().map(value -> translateLabel(value, locale)).toList();
    }

    private String original(String original, String current) {
        return original == null || original.isBlank() ? current : original;
    }

    private static void addLabel(String ko, String en, String ja, String zhCN) {
        LABELS.put(ko, Map.of("en", en, "ja", ja, "zh-CN", zhCN));
    }

    private static void addPlaceName(String placeId, String en, String ja, String zhCN) {
        PLACE_NAMES.put(placeId, Map.of("en", en, "ja", ja, "zh-CN", zhCN));
    }

    private static void addContentSourceTitle(String sourceTitle, String contentId) {
        CONTENT_SOURCE_IDS.put(sourceTitle == null ? "" : sourceTitle.replaceAll("\\s+", ""), contentId);
    }

    private static void addContentTitleAlias(String sourceTitle, String en, String ja, String zhCN) {
        CONTENT_TITLE_ALIASES.put(sourceTitle == null ? "" : sourceTitle.replaceAll("\\s+", ""), Map.of("en", en, "ja", ja, "zh-CN", zhCN));
    }

    private static void addContentTitle(String contentId, String en, String ja, String zhCN) {
        CONTENT_TITLES.put(contentId, Map.of("en", en, "ja", ja, "zh-CN", zhCN));
    }
}

