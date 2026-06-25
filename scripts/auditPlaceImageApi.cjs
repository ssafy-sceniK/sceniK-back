const fs = require('fs');
const path = require('path');

const root = path.resolve(__dirname, '..');
const defaultCsvPath = path.join(root, 'data', 'seed', 'places.csv');
const emptyImageValues = new Set(['', '-', 'null', 'NULL', 'N/A', 'n/a']);

function parseCsv(text) {
  const source = text.replace(/^\uFEFF/, '');
  const rows = [];
  let row = [];
  let field = '';
  let quoted = false;

  for (let index = 0; index < source.length; index += 1) {
    const char = source[index];
    const next = source[index + 1];

    if (char === '"') {
      if (quoted && next === '"') {
        field += '"';
        index += 1;
      } else quoted = !quoted;
    } else if (!quoted && char === ',') {
      row.push(field);
      field = '';
    } else if (!quoted && (char === '\n' || char === '\r')) {
      if (char === '\r' && next === '\n') index += 1;
      row.push(field);
      if (row.some((value) => value !== '')) rows.push(row);
      row = [];
      field = '';
    } else field += char;
  }

  if (field || row.length) {
    row.push(field);
    if (row.some((value) => value !== '')) rows.push(row);
  }

  const [headers, ...records] = rows;
  if (!headers) return [];
  const normalizedHeaders = headers.map((header) => header.replace(/^\uFEFF/, ''));
  return records.map((values) => Object.fromEntries(normalizedHeaders.map((header, index) => [header, values[index] ?? ''])));
}

function text(value) {
  return String(value ?? '').trim();
}

function isEmptyImage(value) {
  return emptyImageValues.has(text(value));
}

function isValidImageUrl(value) {
  const source = text(value);
  if (isEmptyImage(source)) return false;
  if (/^(file|data):/i.test(source)) return false;
  if (/^[a-zA-Z]:[\\/]/.test(source)) return false;
  if (source.startsWith('/')) return false;
  try {
    const url = new URL(source);
    return url.protocol === 'https:' || url.protocol === 'http:';
  } catch {
    return false;
  }
}

function getApiImageUrl(place = {}) {
  const candidates = [
    place.imageUrl,
    place.image_url,
    place.thumbnailUrl,
    place.thumbnail_url,
    place.image,
    place.imageURL,
    place.placeImageUrl,
    place.place_image_url,
  ];
  return candidates.map(text).find((value) => value && !isEmptyImage(value)) ?? '';
}

function fieldSnapshot(place = {}) {
  return {
    imageUrl: place.imageUrl ?? null,
    image_url: place.image_url ?? null,
    thumbnailUrl: place.thumbnailUrl ?? null,
    thumbnail_url: place.thumbnail_url ?? null,
  };
}

function unwrapApiData(payload) {
  if (payload && typeof payload === 'object' && Object.prototype.hasOwnProperty.call(payload, 'data')) {
    return payload.data;
  }
  return payload;
}

async function fetchJson(url) {
  const response = await fetch(url, {
    headers: {
      accept: 'application/json',
    },
  });
  const body = await response.text();
  if (!response.ok) {
    throw new Error(`HTTP ${response.status} from ${url}: ${body.slice(0, 200)}`);
  }
  try {
    return JSON.parse(body);
  } catch (error) {
    throw new Error(`Invalid JSON from ${url}: ${body.slice(0, 200)}`);
  }
}

function loadLocalPlaces(csvPath) {
  return parseCsv(fs.readFileSync(csvPath, 'utf8'))
    .map((place) => ({
      placeId: text(place.place_id ?? place.placeId),
      placeName: text(place.place_name ?? place.placeName),
      imageUrl: text(place.image_url ?? place.imageUrl),
    }))
    .filter((place) => place.placeId);
}

function normalizeApiBaseUrl(apiBaseUrl) {
  return text(apiBaseUrl || process.env.API_BASE_URL || 'http://localhost:8080/api').replace(/\/+$/, '');
}

function comparePlace(localPlace, apiPlace) {
  const apiImageUrl = getApiImageUrl(apiPlace);
  const normalizedImageUrl = text(apiPlace?.imageUrl);
  const hasLocalImage = isValidImageUrl(localPlace.imageUrl);
  const frontendFallbackWouldTrigger = !apiImageUrl;

  return {
    hasLocalImage,
    apiImageUrl,
    frontendFallbackWouldTrigger,
    apiMissingImageUrl: hasLocalImage && frontendFallbackWouldTrigger,
    apiFieldContractIssue: hasLocalImage && !normalizedImageUrl && Boolean(apiImageUrl),
  };
}

async function auditPlaceImageApi(options = {}) {
  const csvPath = options.csvPath || defaultCsvPath;
  const apiBaseUrl = normalizeApiBaseUrl(options.apiBaseUrl);
  const localPlaces = loadLocalPlaces(csvPath);
  const localById = new Map(localPlaces.map((place) => [place.placeId, place]));
  const targetIds = (options.placeIds || []).map(text).filter(Boolean);
  const validLocalPlaces = localPlaces.filter((place) => isValidImageUrl(place.imageUrl));
  const apiPlaces = [];
  const errors = [];

  if (targetIds.length) {
    for (const placeId of targetIds) {
      try {
        const payload = await fetchJson(`${apiBaseUrl}/places/${encodeURIComponent(placeId)}`);
        apiPlaces.push(unwrapApiData(payload));
      } catch (error) {
        errors.push({ placeId, message: error.message });
      }
    }
  } else {
    try {
      const payload = await fetchJson(`${apiBaseUrl}/places`);
      const data = unwrapApiData(payload);
      if (!Array.isArray(data)) {
        throw new Error('Expected /places response data to be an array.');
      }
      apiPlaces.push(...data);
    } catch (error) {
      errors.push({ placeId: null, message: error.message });
    }
  }

  const mismatches = [];
  const contractIssues = [];
  let apiPlacesChecked = 0;

  for (const apiPlace of apiPlaces) {
    const placeId = text(apiPlace?.placeId ?? apiPlace?.place_id);
    const localPlace = localById.get(placeId);
    if (!placeId || !localPlace || !isValidImageUrl(localPlace.imageUrl)) continue;

    apiPlacesChecked += 1;
    const comparison = comparePlace(localPlace, apiPlace);
    const row = {
      placeId,
      placeName: localPlace.placeName || text(apiPlace?.placeName ?? apiPlace?.name),
      localImageUrl: localPlace.imageUrl,
      apiImageUrl: comparison.apiImageUrl,
      frontendFallbackWouldTrigger: comparison.frontendFallbackWouldTrigger,
      apiFields: fieldSnapshot(apiPlace),
    };

    if (comparison.apiMissingImageUrl) mismatches.push(row);
    if (comparison.apiFieldContractIssue) contractIssues.push(row);
  }

  return {
    apiBaseUrl,
    csvPath,
    summary: {
      csvPlaces: localPlaces.length,
      csvPlacesWithValidImageUrl: validLocalPlaces.length,
      apiPlacesChecked,
      apiMissingImageUrl: mismatches.length,
      apiFieldContractIssues: contractIssues.length,
      requestErrors: errors.length,
    },
    mismatches,
    contractIssues,
    errors,
  };
}

function parseArgs(argv) {
  const options = { placeIds: [], format: 'json' };
  for (let index = 0; index < argv.length; index += 1) {
    const arg = argv[index];
    const next = argv[index + 1];
    if (arg === '--csv') {
      options.csvPath = next;
      index += 1;
    } else if (arg === '--api-base-url') {
      options.apiBaseUrl = next;
      index += 1;
    } else if (arg === '--place-id') {
      options.placeIds.push(next);
      index += 1;
    } else if (arg === '--format') {
      options.format = next;
      index += 1;
    } else if (arg === '--help' || arg === '-h') {
      options.help = true;
    } else {
      throw new Error(`Unknown argument: ${arg}`);
    }
  }
  return options;
}

function printHelp() {
  console.log(`Usage: node scripts/auditPlaceImageApi.cjs [options]\n\nOptions:\n  --api-base-url <url>  API base URL. Defaults to API_BASE_URL or http://localhost:8080/api\n  --csv <path>          places.csv path. Defaults to data/seed/places.csv\n  --place-id <id>       Audit one place detail endpoint. Can be repeated.\n  --format json         Output JSON.\n`);
}

async function main() {
  const options = parseArgs(process.argv.slice(2));
  if (options.help) {
    printHelp();
    return;
  }
  const report = await auditPlaceImageApi(options);
  console.log(JSON.stringify(report, null, 2));
}

if (require.main === module) {
  main().catch((error) => {
    console.error(error.message);
    process.exitCode = 1;
  });
}

module.exports = {
  auditPlaceImageApi,
  comparePlace,
  getApiImageUrl,
  isValidImageUrl,
  parseCsv,
};
