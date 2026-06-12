const fs = require('fs');
const path = require('path');

const root = path.resolve(__dirname, '..');
const dataDir = path.join(root, 'data', 'seed');
const frontendRoot = path.resolve(root, '..', 'sceniK-front');

function parseCsv(fileName) {
  const filePath = path.join(dataDir, fileName);
  if (!fs.existsSync(filePath)) return [];
  const text = fs.readFileSync(filePath, 'utf8').replace(/^\uFEFF/, '');
  const rows = [];
  let row = [];
  let field = '';
  let quoted = false;

  for (let i = 0; i < text.length; i += 1) {
    const ch = text[i];
    const next = text[i + 1];

    if (ch === '"') {
      if (quoted && next === '"') {
        field += '"';
        i += 1;
      } else {
        quoted = !quoted;
      }
      continue;
    }

    if (!quoted && ch === ',') {
      row.push(field);
      field = '';
      continue;
    }

    if (!quoted && (ch === '\n' || ch === '\r')) {
      if (ch === '\r' && next === '\n') i += 1;
      row.push(field);
      if (row.some((value) => value !== '')) rows.push(row);
      row = [];
      field = '';
      continue;
    }

    field += ch;
  }

  if (field || row.length) {
    row.push(field);
    if (row.some((value) => value !== '')) rows.push(row);
  }

  const [headers, ...records] = rows;
  return records.map((values) => Object.fromEntries(headers.map((header, index) => [header, values[index] ?? ''])));
}

function unique(list) {
  return [...new Set(list.filter(Boolean))];
}

function splitTags(value) {
  if (!value) return [];
  return unique(value.split(',').map((item) => item.trim()).filter((item) => item && item !== '정보없음'));
}

function sql(value) {
  if (value === undefined || value === null || value === '') return 'NULL';
  return `'${String(value).replaceAll("'", "''")}'`;
}

function sqlBool(value) {
  return value ? 'TRUE' : 'FALSE';
}

function sqlNum(value) {
  return value !== '' && value !== null && value !== undefined && Number.isFinite(Number(value))
    ? String(Number(value))
    : 'NULL';
}

function csvNumber(value) {
  return value !== '' && value !== null && value !== undefined && Number.isFinite(Number(value))
    ? Number(value)
    : null;
}

const categoryMap = {
  drama: '드라마',
  movie: '영화',
  variety: '예능',
};

const subcategoryMap = {
  drama: 'K-Drama',
  movie: 'K-Movie',
  variety: 'K-Variety',
};

const fallbackImages = ['/images/kdrama-scene.jpg', '/images/seoul-street.jpg', '/images/onboarding-hero.jpg'];
const rawContents = parseCsv('contents.csv');
const rawPlaces = parseCsv('places.csv');
const rawContentPlaces = parseCsv('content_places.csv');
const rawPlaceTags = parseCsv('place_tags.csv');
const rawEmbeddings = parseCsv('embedding_text.csv');

const mappingsByContent = new Map();
const mappingsByPlace = new Map();
rawContentPlaces.forEach((mapping) => {
  if (!mappingsByContent.has(mapping.content_id)) mappingsByContent.set(mapping.content_id, []);
  if (!mappingsByPlace.has(mapping.place_id)) mappingsByPlace.set(mapping.place_id, []);
  mappingsByContent.get(mapping.content_id).push(mapping);
  mappingsByPlace.get(mapping.place_id).push(mapping);
});

const tagRows = new Map(rawPlaceTags.map((row) => [row.place_id, row]));

const contents = rawContents.map((row, index) => {
  const tags = splitTags(row.tags);
  const relationCount = mappingsByContent.get(row.content_id)?.length ?? 0;
  const category = categoryMap[row.category] ?? row.category;
  return {
    contentId: row.content_id,
    title: row.title,
    category,
    subcategory: subcategoryMap[row.category] ?? category,
    releaseYear: Number(row.release_year) || null,
    status: Number(row.release_year) >= 2026 ? '예정' : '공개',
    description: row.description,
    castNames: row.cast_names,
    thumbnailUrl: row.thumbnail_url || fallbackImages[index % fallbackImages.length],
    isPopular: index < 12 || relationCount >= 5,
    country: 'Korea',
    ottPlatform: row.ott_platform,
    tags,
    relatedPlaceCount: relationCount,
  };
});

const contentsById = new Map(contents.map((content) => [content.contentId, content]));

const places = rawPlaces.map((row, index) => {
  const tagRow = tagRows.get(row.place_id) ?? {};
  const tagValues = [
    row.region,
    row.district,
    row.category,
    ...splitTags(tagRow.theme_tags),
    ...splitTags(tagRow.mood_tags),
    ...splitTags(tagRow.companion_tags),
    ...splitTags(tagRow.time_tags),
    ...splitTags(tagRow.budget_tags),
    ...splitTags(tagRow.weather_tags),
    ...splitTags(tagRow.activity_tags),
  ];
  const relatedContentIds = unique((mappingsByPlace.get(row.place_id) ?? []).map((mapping) => mapping.content_id));
  return {
    placeId: row.place_id,
    placeName: row.place_name,
    region: row.region,
    district: row.district,
    address: row.address,
    category: row.category,
    description: row.description,
    openingHours: row.opening_hours,
    closedDays: row.closed_days,
    phoneNumber: row.phone_number,
    imageUrl: row.image_url || fallbackImages[index % fallbackImages.length],
    indoorOutdoor: row.indoor_outdoor,
    parkingAvailable: String(row.parking_available).toUpperCase() === 'TRUE',
    relatedContentIds,
    tags: unique(tagValues),
    verified: true,
    latitude: csvNumber(row.latitude),
    longitude: csvNumber(row.longitude),
  };
});

const placesById = new Map(places.map((place) => [place.placeId, place]));

const contentPlaces = rawContentPlaces
  .filter((row) => contentsById.has(row.content_id) && placesById.has(row.place_id))
  .map((row) => ({
    contentId: row.content_id,
    placeId: row.place_id,
    relationType: row.relation_type,
    relationDescription: row.scene_description || `${contentsById.get(row.content_id).title} 관련 검증 장소`,
    episodeInfo: row.episode_info,
  }));

function commonRegion(placeIds) {
  const counts = new Map();
  placeIds.forEach((placeId) => {
    const region = placesById.get(placeId)?.region;
    if (region) counts.set(region, (counts.get(region) ?? 0) + 1);
  });
  return [...counts.entries()].sort((a, b) => b[1] - a[1])[0]?.[0] ?? '서울';
}

const courseTemplates = contents
  .filter((content) => (mappingsByContent.get(content.contentId)?.length ?? 0) >= 3)
  .slice(0, 8)
  .map((content, index) => {
    const placeIds = unique((mappingsByContent.get(content.contentId) ?? []).map((mapping) => mapping.place_id)).slice(0, 5);
    const region = commonRegion(placeIds);
    const duration = placeIds.length >= 4 ? 2 : 1;
    return {
      routeId: `csv-route-${index + 1}`,
      title: `${content.title} ${region} ${duration}일 코스`,
      region,
      duration,
      places: placeIds,
      tags: unique(['AI 코스', content.category, region, content.tags[0]]),
      likes: 128 - index * 9,
    };
  });

const notices = [
  { id: 'notice-csv-1', title: '신규 K-콘텐츠 장소 데이터가 업데이트되었습니다.', date: '2026.05.20' },
  { id: 'notice-csv-2', title: 'AI 코스는 검증된 후보 장소만 기준으로 생성됩니다.', date: '2026.05.18' },
  { id: 'notice-csv-3', title: '방문 전 운영 시간과 휴무일을 다시 확인해주세요.', date: '2026.05.12' },
];

const communitySeedPosts = courseTemplates.slice(0, 4).map((course, index) => ({
  id: `seed-post-${index + 1}`,
  user: ['Hana', 'Minjun', 'Soo', 'Jiyoon'][index % 4],
  date: `2026.05.${String(18 - index).padStart(2, '0')}`,
  title: `${course.title}로 다녀온 촬영지 루트`,
  body: '검증된 장소만 이어서 이동 동선이 안정적이었고, 장면 설명을 보면서 걷는 재미가 있었습니다.',
  region: course.region,
  duration: `${course.duration}일`,
  tags: course.tags,
  likes: 42 - index * 5,
  likedBy: [],
  comments: [],
}));

const mockOutput = `// Generated from ../../data/*.csv. Do not hand-edit large arrays; update source CSV and rerun scripts/importCsvData.cjs.\nexport const contents = ${JSON.stringify(contents, null, 2)};\n\nexport const places = ${JSON.stringify(places, null, 2)};\n\nexport const contentPlaces = ${JSON.stringify(contentPlaces, null, 2)};\n\nexport const courseTemplates = ${JSON.stringify(courseTemplates, null, 2)};\n\nexport const notices = ${JSON.stringify(notices, null, 2)};\n\nexport const communitySeedPosts = ${JSON.stringify(communitySeedPosts, null, 2)};\n`;

fs.writeFileSync(path.join(frontendRoot, 'src/data/mockData.js'), mockOutput, 'utf8');

const sqlLines = [];
sqlLines.push('-- Generated from ../../data/*.csv for local MVP/bootstrap data.');
sqlLines.push('-- The source CSV files remain the canonical editable seed input.');
sqlLines.push("DELETE FROM content_places WHERE content_id LIKE 'C\\_%' OR place_id LIKE 'P\\_%' OR content_id LIKE 'content-%' OR place_id LIKE 'place-%';");
sqlLines.push("DELETE FROM place_tags WHERE place_id LIKE 'P\\_%' OR place_id LIKE 'place-%';");
sqlLines.push("DELETE FROM embedding_text WHERE (source_type = 'content' AND source_id LIKE 'content-%') OR (source_type = 'place_content' AND source_id LIKE 'E_%');");
sqlLines.push("DELETE FROM contents WHERE content_id LIKE 'C\\_%' OR content_id LIKE 'content-%';");
sqlLines.push("DELETE FROM places WHERE place_id LIKE 'P\\_%' OR place_id LIKE 'place-%';");
sqlLines.push('');

sqlLines.push('INSERT INTO contents (content_id, title, category, subcategory, release_year, status, description, cast_names, thumbnail_url, is_popular, country, ott_platform, tags, created_at)');
sqlLines.push('VALUES');
sqlLines.push(contents.map((content) => `(${sql(content.contentId)}, ${sql(content.title)}, ${sql(content.category)}, ${sql(content.subcategory)}, ${sqlNum(content.releaseYear)}, ${sql(content.status)}, ${sql(content.description)}, ${sql(content.castNames)}, ${sql(content.thumbnailUrl)}, ${sqlBool(content.isPopular)}, ${sql(content.country)}, ${sql(content.ottPlatform)}, ${sql(content.tags.join(','))}, CURRENT_TIMESTAMP)`).join(',\n'));
sqlLines.push('ON DUPLICATE KEY UPDATE title = VALUES(title), category = VALUES(category), subcategory = VALUES(subcategory), release_year = VALUES(release_year), status = VALUES(status), description = VALUES(description), cast_names = VALUES(cast_names), thumbnail_url = VALUES(thumbnail_url), is_popular = VALUES(is_popular), country = VALUES(country), ott_platform = VALUES(ott_platform), tags = VALUES(tags);');
sqlLines.push('');

sqlLines.push('INSERT INTO places (place_id, place_name, region, district, address, category, description, opening_hours, closed_days, phone_number, image_url, indoor_outdoor, parking_available, latitude, longitude)');
sqlLines.push('VALUES');
sqlLines.push(places.map((place) => `(${sql(place.placeId)}, ${sql(place.placeName)}, ${sql(place.region)}, ${sql(place.district)}, ${sql(place.address)}, ${sql(place.category)}, ${sql(place.description)}, ${sql(place.openingHours)}, ${sql(place.closedDays)}, ${sql(place.phoneNumber)}, ${sql(place.imageUrl)}, ${sql(place.indoorOutdoor)}, ${sqlBool(place.parkingAvailable)}, ${sqlNum(place.latitude)}, ${sqlNum(place.longitude)})`).join(',\n'));
sqlLines.push('ON DUPLICATE KEY UPDATE place_name = VALUES(place_name), region = VALUES(region), district = VALUES(district), address = VALUES(address), category = VALUES(category), description = VALUES(description), opening_hours = VALUES(opening_hours), closed_days = VALUES(closed_days), phone_number = VALUES(phone_number), image_url = VALUES(image_url), indoor_outdoor = VALUES(indoor_outdoor), parking_available = VALUES(parking_available), latitude = VALUES(latitude), longitude = VALUES(longitude);');
sqlLines.push('');

const sortCounters = new Map();
sqlLines.push('INSERT INTO content_places (content_id, place_id, relation_description, sort_order)');
sqlLines.push('VALUES');
sqlLines.push(contentPlaces.map((mapping) => {
  const nextOrder = (sortCounters.get(mapping.contentId) ?? 0) + 1;
  sortCounters.set(mapping.contentId, nextOrder);
  const description = [mapping.relationType, mapping.episodeInfo, mapping.relationDescription].filter(Boolean).join(' · ');
  return `(${sql(mapping.contentId)}, ${sql(mapping.placeId)}, ${sql(description)}, ${nextOrder})`;
}).join(',\n'));
sqlLines.push('ON DUPLICATE KEY UPDATE relation_description = VALUES(relation_description), sort_order = VALUES(sort_order);');
sqlLines.push('');

const placeTagPairs = [];
places.forEach((place) => {
  place.tags.forEach((tag) => placeTagPairs.push([place.placeId, tag]));
});
if (placeTagPairs.length) {
  sqlLines.push('INSERT IGNORE INTO place_tags (place_id, tag)');
  sqlLines.push('VALUES');
  sqlLines.push(placeTagPairs.map(([placeId, tag]) => `(${sql(placeId)}, ${sql(tag)})`).join(',\n'));
  sqlLines.push(';');
  sqlLines.push('');
}

if (rawEmbeddings.length) {
  sqlLines.push('INSERT INTO embedding_text (source_type, source_id, text)');
  sqlLines.push('VALUES');
  sqlLines.push(rawEmbeddings.map((row) => `('place_content', ${sql(row.embedding_id)}, ${sql(row.embedding_text)})`).join(',\n'));
  sqlLines.push(';');
  sqlLines.push('');
}

fs.writeFileSync(path.join(root, 'src/main/resources/data.sql'), sqlLines.join('\n'), 'utf8');

console.log(JSON.stringify({
  contents: contents.length,
  places: places.length,
  contentPlaces: contentPlaces.length,
  placeTags: placeTagPairs.length,
  embeddings: rawEmbeddings.length,
  courseTemplates: courseTemplates.length,
}, null, 2));
