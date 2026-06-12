const fs = require('fs');
const path = require('path');

const root = path.resolve(__dirname, '..');
const frontendRoot = path.resolve(root, '..', 'sceniK-front');
const csvPath = path.join(root, 'data', 'seed', 'places.csv');
const sqlPath = path.join(root, 'src', 'main', 'resources', 'data.sql');
const mockPath = path.join(frontendRoot, 'src', 'data', 'mockData.js');
const updateSqlPath = path.join(root, 'build', 'place-coordinate-update.sql');

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
      } else quoted = !quoted;
    } else if (!quoted && char === ',') {
      row.push(field);
      field = '';
    } else if (!quoted && (char === '\n' || char === '\r')) {
      if (char === '\r' && next === '\n') index += 1;
      row.push(field);
      if (row.some(Boolean)) rows.push(row);
      row = [];
      field = '';
    } else field += char;
  }
  if (field || row.length) {
    row.push(field);
    if (row.some(Boolean)) rows.push(row);
  }
  const [headers, ...records] = rows;
  return records.map((values) => Object.fromEntries(headers.map((header, index) => [header, values[index] ?? ''])));
}

function sqlString(value) {
  return value ? `'${String(value).replaceAll("'", "''")}'` : 'NULL';
}

function sqlNumber(value) {
  return value !== '' && Number.isFinite(Number(value)) ? String(Number(value)) : 'NULL';
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

const places = parseCsv(fs.readFileSync(csvPath, 'utf8'));
const placesById = new Map(places.map((place) => [place.place_id, place]));

const sqlLines = fs.readFileSync(sqlPath, 'utf8').split('\n');
let sqlUpdates = 0;
const updatedSqlLines = sqlLines.map((line) => {
  const idMatch = line.match(/^\('([^']+)'/);
  const place = idMatch ? placesById.get(idMatch[1]) : null;
  if (!place) return line;
  const tuple = splitTuple(line);
  if (!tuple || tuple.fields.length !== 15) return line;
  tuple.fields[2] = sqlString(place.region);
  tuple.fields[3] = sqlString(place.district);
  tuple.fields[4] = sqlString(place.address);
  tuple.fields[13] = sqlNumber(place.latitude);
  tuple.fields[14] = sqlNumber(place.longitude);
  sqlUpdates += 1;
  return `(${tuple.fields.join(', ')})${tuple.suffix}`;
});
fs.writeFileSync(sqlPath, updatedSqlLines.join('\n'));

const mockSource = fs.readFileSync(mockPath, 'utf8');
const mockMatch = mockSource.match(/export const places = (\[[\s\S]*?\]);\n\nexport const contentPlaces/);
if (!mockMatch) throw new Error('프론트 mockData.js에서 places 배열을 찾지 못했습니다.');
const mockPlaces = JSON.parse(mockMatch[1]);
let mockUpdates = 0;
mockPlaces.forEach((place) => {
  const source = placesById.get(place.placeId);
  if (!source) return;
  place.region = source.region;
  place.district = source.district;
  place.address = source.address;
  place.latitude = source.latitude === '' ? null : Number(source.latitude);
  place.longitude = source.longitude === '' ? null : Number(source.longitude);
  mockUpdates += 1;
});
const updatedMockSource = mockSource.replace(mockMatch[1], JSON.stringify(mockPlaces, null, 2));
fs.writeFileSync(mockPath, updatedMockSource);

fs.mkdirSync(path.dirname(updateSqlPath), { recursive: true });
const databaseUpdates = places.map((place) => `UPDATE places SET region = ${sqlString(place.region)}, district = ${sqlString(place.district)}, address = ${sqlString(place.address)}, latitude = ${sqlNumber(place.latitude)}, longitude = ${sqlNumber(place.longitude)} WHERE place_id = ${sqlString(place.place_id)};`);
fs.writeFileSync(updateSqlPath, `${databaseUpdates.join('\n')}\n`);

console.log(JSON.stringify({ csvPlaces: places.length, sqlUpdates, mockUpdates, updateSqlPath }, null, 2));
