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
                originalName,
                originalPlaceName,
                dto.address(),
                translateLabel(originalRegion, locale),
                translateLabel(originalDistrict, locale),
                translateLabel(originalCategory, locale),
                originalDescription,
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

    private static void addContentTitle(String contentId, String en, String ja, String zhCN) {
        CONTENT_TITLES.put(contentId, Map.of("en", en, "ja", ja, "zh-CN", zhCN));
    }
}

