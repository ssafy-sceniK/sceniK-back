const fs = require('fs');
const path = require('path');

const root = path.resolve(__dirname, '..');
const csvPath = path.join(root, 'data', 'seed', 'places.csv');
const sqlPath = path.join(root, 'src', 'main', 'resources', 'data.sql');
const updateSqlPath = path.join(root, 'build', 'place-image-update.sql');
const emptyImageValues = new Set(['', '-', 'null', 'NULL', 'N/A', 'n/a']);
const samplePlaceIds = ['P_0262', 'P_0150', 'P_0167', 'P_0168', 'P_0169', 'P_0170', 'P_0171', 'P_0172', 'P_0173'];
const writeDataSql = process.argv.includes('--write-data-sql');

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
  const normalizedHeaders = headers.map((header) => header.replace(/^\uFEFF/, ''));
  return records.map((values) => Object.fromEntries(normalizedHeaders.map((header, index) => [header, values[index] ?? ''])));
}

function sqlString(value) {
  return `'${String(value).replaceAll("'", "''")}'`;
}

function isEmptyImage(value) {
  return emptyImageValues.has(String(value ?? '').trim());
}

function isValidImageUrl(value) {
  const source = String(value ?? '').trim();
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

function splitTuple(line) {
  const start = line.indexOf('(');
  const end = line.lastIndexOf(')');
  if (start < 0 || end < start) return null;

  const fields = [];
  let field = '';
  let quoted = false;

  for (let index = start + 1; index < end; index += 1) {
    const char = line[index];
    const next = line[index + 1];
    if (char === "'") {
      field += char;
      if (quoted && next === "'") {
        field += next;
        index += 1;
      } else quoted = !quoted;
    } else if (!quoted && char === ',') {
      fields.push(field.trim());
      field = '';
    } else field += char;
  }

  fields.push(field.trim());
  return { fields, suffix: line.slice(end + 1) };
}

function sqlTupleStringValue(value) {
  const text = String(value ?? '').trim();
  if (/^NULL$/i.test(text)) return '';
  if (text.startsWith("'") && text.endsWith("'")) return text.slice(1, -1).replaceAll("''", "'");
  return text;
}

const csvPlaces = parseCsv(fs.readFileSync(csvPath, 'utf8'));
const duplicateIds = csvPlaces
  .map((place) => place.place_id?.trim())
  .filter((placeId, index, ids) => placeId && ids.indexOf(placeId) !== index);
if (duplicateIds.length) {
  throw new Error(`Duplicate place_id values in places.csv: ${[...new Set(duplicateIds)].join(', ')}`);
}

const imageRows = csvPlaces
  .map((place) => ({
    placeId: place.place_id?.trim(),
    placeName: place.place_name?.trim(),
    imageUrl: place.image_url?.trim() ?? '',
  }))
  .filter((place) => place.placeId);
const rowsWithImage = imageRows.filter((place) => !isEmptyImage(place.imageUrl));
const validImageRows = rowsWithImage.filter((place) => isValidImageUrl(place.imageUrl));
const invalidImageRows = rowsWithImage.filter((place) => !isValidImageUrl(place.imageUrl));
const skippedEmpty = imageRows.length - rowsWithImage.length;
const imageByPlaceId = new Map(validImageRows.map((place) => [place.placeId, place.imageUrl]));

const dataSql = fs.readFileSync(sqlPath, 'utf8');
const sqlLines = dataSql.split('\n');
let matchedInDataSql = 0;
let updatedDataSqlRows = 0;
let dataSqlRowsWithImage = 0;
const seenDataSqlPlaceIds = new Set();

const updatedSqlLines = sqlLines.map((line) => {
  const idMatch = line.match(/^\('([^']+)'/);
  if (!idMatch) return line;
  const placeId = idMatch[1];
  const imageUrl = imageByPlaceId.get(placeId);
  if (!imageUrl) return line;
  const tuple = splitTuple(line);
  if (!tuple || tuple.fields.length !== 15) return line;

  matchedInDataSql += 1;
  seenDataSqlPlaceIds.add(placeId);
  const currentImage = sqlTupleStringValue(tuple.fields[10]);
  if (!isEmptyImage(currentImage)) dataSqlRowsWithImage += 1;

  const nextImageValue = sqlString(imageUrl);
  if (tuple.fields[10] !== nextImageValue) {
    tuple.fields[10] = nextImageValue;
    updatedDataSqlRows += 1;
  }
  return `(${tuple.fields.join(', ')})${tuple.suffix}`;
});

if (writeDataSql) {
  fs.writeFileSync(sqlPath, updatedSqlLines.join('\n'), 'utf8');
}

fs.mkdirSync(path.dirname(updateSqlPath), { recursive: true });
const updateStatements = validImageRows.map((place) => (
  `UPDATE places SET image_url = ${sqlString(place.imageUrl)} WHERE place_id = ${sqlString(place.placeId)} AND (image_url IS NULL OR TRIM(image_url) = '' OR image_url IN ('-', 'null', 'NULL', 'N/A', 'n/a'));`
));
const verificationSql = [
  '',
  '-- Verification helpers:',
  'SELECT COUNT(*) AS total_places FROM places;',
  "SELECT COUNT(*) AS places_with_image_url FROM places WHERE image_url IS NOT NULL AND TRIM(image_url) <> '' AND image_url NOT IN ('-', 'null', 'NULL', 'N/A', 'n/a');",
  `SELECT place_id, place_name, image_url FROM places WHERE place_id IN (${samplePlaceIds.map(sqlString).join(', ')}) ORDER BY place_id;`,
  `SELECT COUNT(*) AS missing_sample_image_url FROM places WHERE place_id IN (${samplePlaceIds.map(sqlString).join(', ')}) AND (image_url IS NULL OR TRIM(image_url) = '' OR image_url IN ('-', 'null', 'NULL', 'N/A', 'n/a'));`,
];
fs.writeFileSync(updateSqlPath, `${updateStatements.join('\n')}\n${verificationSql.join('\n')}\n`, 'utf8');

const skippedMissingInDataSql = validImageRows.filter((place) => !seenDataSqlPlaceIds.has(place.placeId));
const samples = validImageRows
  .filter((place) => samplePlaceIds.includes(place.placeId))
  .map((place) => ({ placeId: place.placeId, placeName: place.placeName, imageUrl: place.imageUrl }));

console.log(JSON.stringify({
  csvRows: imageRows.length,
  csvRowsWithImageUrl: rowsWithImage.length,
  csvRowsWithValidImageUrl: validImageRows.length,
  skippedBecauseImageUrlEmpty: skippedEmpty,
  skippedBecauseImageUrlInvalid: invalidImageRows.length,
  duplicatePlaceIds: 0,
  matchedInDataSql,
  dataSqlRowsWithImage,
  updatedDataSqlRows: writeDataSql ? updatedDataSqlRows : 0,
  pendingDataSqlUpdatesIfWritten: writeDataSql ? 0 : updatedDataSqlRows,
  skippedBecauseNoMatchingDataSqlPlace: skippedMissingInDataSql.length,
  generatedProductionUpdateStatements: updateStatements.length,
  updateSqlPath,
  sampleFailingPlaceRows: samples,
  mode: writeDataSql ? 'write-data-sql' : 'dry-run',
}, null, 2));
