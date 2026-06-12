const fs = require('fs');
const path = require('path');

const root = path.resolve(__dirname, '..');
const csvPath = path.join(root, 'data', 'seed', 'places.csv');
const cachePath = path.join(root, 'build', 'geocode-cache.json');
const reportPath = path.join(root, 'build', 'geocode-report.json');
const apply = process.argv.includes('--apply');

function parseEnv(filePath) {
  if (!fs.existsSync(filePath)) return {};
  return Object.fromEntries(fs.readFileSync(filePath, 'utf8')
    .split(/\r?\n/)
    .map((line) => line.trim())
    .filter((line) => line && !line.startsWith('#') && line.includes('='))
    .map((line) => {
      const separator = line.indexOf('=');
      return [line.slice(0, separator).trim(), line.slice(separator + 1).trim()];
    }));
}

function parseCsv(text) {
  const rows = [];
  let row = [];
  let field = '';
  let quoted = false;
  const source = text.replace(/^\uFEFF/, '');
  for (let index = 0; index < source.length; index += 1) {
    const char = source[index];
    const next = source[index + 1];
    if (char === '"') {
      if (quoted && next === '"') {
        field += '"';
        index += 1;
      } else {
        quoted = !quoted;
      }
    } else if (!quoted && char === ',') {
      row.push(field);
      field = '';
    } else if (!quoted && (char === '\n' || char === '\r')) {
      if (char === '\r' && next === '\n') index += 1;
      row.push(field);
      if (row.some(Boolean)) rows.push(row);
      row = [];
      field = '';
    } else {
      field += char;
    }
  }
  if (field || row.length) {
    row.push(field);
    if (row.some(Boolean)) rows.push(row);
  }
  const [headers, ...records] = rows;
  return { headers, records: records.map((values) => Object.fromEntries(headers.map((header, index) => [header, values[index] ?? '']))) };
}

function csvValue(value) {
  const text = value == null ? '' : String(value);
  return /[",\r\n]/.test(text) ? `"${text.replaceAll('"', '""')}"` : text;
}

function stringifyCsv(headers, records) {
  return `\uFEFF${[headers, ...records.map((record) => headers.map((header) => record[header] ?? ''))]
    .map((row) => row.map(csvValue).join(','))
    .join('\n')}\n`;
}

function normalize(value) {
  return String(value ?? '').toLowerCase().replace(/[^0-9a-z가-힣]/g, '');
}

function distanceMeters(first, second) {
  if (!first || !second) return null;
  const radians = (degrees) => degrees * Math.PI / 180;
  const latitudeDelta = radians(second.latitude - first.latitude);
  const longitudeDelta = radians(second.longitude - first.longitude);
  const a = Math.sin(latitudeDelta / 2) ** 2
    + Math.cos(radians(first.latitude)) * Math.cos(radians(second.latitude)) * Math.sin(longitudeDelta / 2) ** 2;
  return 6371000 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
}

function addressResult(document) {
  if (!document) return null;
  const structuredAddress = document.road_address || document.address;
  return {
    address: document.road_address?.address_name || document.address?.address_name || document.address_name,
    region: structuredAddress?.region_1depth_name,
    district: structuredAddress?.region_2depth_name?.split(' ')[0],
    latitude: Number(document.y),
    longitude: Number(document.x),
  };
}

function keywordResult(document) {
  if (!document) return null;
  const addressParts = document.address_name.split(/\s+/);
  return {
    name: document.place_name,
    address: document.road_address_name || document.address_name,
    region: addressParts[0],
    district: addressParts[1],
    latitude: Number(document.y),
    longitude: Number(document.x),
  };
}

function normalizeRegion(value) {
  const region = String(value ?? '');
  const aliases = {
    서울특별시: '서울', 부산광역시: '부산', 대구광역시: '대구', 인천광역시: '인천',
    광주광역시: '광주', 대전광역시: '대전', 울산광역시: '울산', 세종특별자치시: '세종',
    경기도: '경기', 강원특별자치도: '강원', 강원도: '강원', 충청북도: '충북', 충청남도: '충남',
    전북특별자치도: '전북', 전라북도: '전북', 전라남도: '전남', 경상북도: '경북', 경상남도: '경남',
    제주특별자치도: '제주',
  };
  return aliases[region] || region;
}

async function main() {
  const env = { ...parseEnv(path.join(root, '.env')), ...process.env };
  const restApiKey = env.KAKAO_OAUTH_CLIENT_ID || env.KAKAO_REST_API_KEY;
  if (!restApiKey) throw new Error('KAKAO_OAUTH_CLIENT_ID 또는 KAKAO_REST_API_KEY가 필요합니다.');

  const { headers: sourceHeaders, records } = parseCsv(fs.readFileSync(csvPath, 'utf8'));
  const headers = [...sourceHeaders.filter((header) => !['latitude', 'longitude', 'coordinate_source'].includes(header)), 'latitude', 'longitude', 'coordinate_source'];
  fs.mkdirSync(path.dirname(cachePath), { recursive: true });
  const cache = fs.existsSync(cachePath) ? JSON.parse(fs.readFileSync(cachePath, 'utf8')) : {};

  async function kakao(endpoint, query) {
    const cacheKey = `${endpoint}:${query}`;
    if (cache[cacheKey]) return cache[cacheKey];
    const url = new URL(`https://dapi.kakao.com/v2/local/search/${endpoint}.json`);
    url.searchParams.set('query', query);
    const response = await fetch(url, { headers: { Authorization: `KakaoAK ${restApiKey}` } });
    if (!response.ok) throw new Error(`Kakao Local API ${response.status}`);
    const payload = await response.json();
    cache[cacheKey] = payload;
    fs.writeFileSync(cachePath, JSON.stringify(cache, null, 2));
    return payload;
  }

  const report = [];
  for (let index = 0; index < records.length; index += 1) {
    const record = records[index];
    const [addressPayload, keywordPayload] = await Promise.all([
      kakao('address', record.address),
      kakao('keyword', `${record.place_name} ${record.region} ${record.district}`),
    ]);
    const addressMatch = addressResult(addressPayload.documents?.[0]);
    const exactKeywordDocument = keywordPayload.documents?.find((document) => {
      const sourceName = normalize(record.place_name);
      const resultName = normalize(document.place_name);
      return resultName === sourceName || resultName.includes(sourceName) || sourceName.includes(resultName);
    });
    const keywordMatch = keywordResult(exactKeywordDocument);
    const chosen = keywordMatch || addressMatch;
    const disagreementMeters = Math.round(distanceMeters(addressMatch, keywordMatch) ?? 0);

    if (chosen) {
      record.address = chosen.address || record.address;
      record.region = normalizeRegion(chosen.region) || record.region;
      record.district = chosen.district || record.district;
      record.latitude = chosen.latitude.toFixed(7);
      record.longitude = chosen.longitude.toFixed(7);
      record.coordinate_source = keywordMatch ? 'KAKAO_KEYWORD' : 'KAKAO_ADDRESS';
    } else {
      record.latitude = '';
      record.longitude = '';
      record.coordinate_source = 'UNRESOLVED';
    }

    report.push({
      placeId: record.place_id,
      placeName: record.place_name,
      sourceAddress: record.address,
      source: record.coordinate_source,
      latitude: record.latitude || null,
      longitude: record.longitude || null,
      addressKeywordDisagreementMeters: disagreementMeters || null,
    });
    process.stdout.write(`\r${index + 1}/${records.length}`);
  }
  process.stdout.write('\n');

  const summary = {
    total: records.length,
    keyword: report.filter((item) => item.source === 'KAKAO_KEYWORD').length,
    address: report.filter((item) => item.source === 'KAKAO_ADDRESS').length,
    unresolved: report.filter((item) => item.source === 'UNRESOLVED').length,
    disagreementsOver500m: report.filter((item) => item.addressKeywordDisagreementMeters > 500).length,
  };
  fs.writeFileSync(reportPath, JSON.stringify({ summary, places: report }, null, 2));
  if (apply) fs.writeFileSync(csvPath, stringifyCsv(headers, records));
  console.log(JSON.stringify({ apply, summary, reportPath }, null, 2));
}

main().catch((error) => {
  console.error(error.message);
  process.exitCode = 1;
});
