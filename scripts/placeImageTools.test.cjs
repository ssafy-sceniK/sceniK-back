const assert = require('node:assert/strict');
const fs = require('node:fs');
const http = require('node:http');
const os = require('node:os');
const path = require('node:path');
const test = require('node:test');

const repoRoot = path.resolve(__dirname, '..');

test('syncPlaceImages includes P_0262 in production verification samples', () => {
  const script = fs.readFileSync(path.join(repoRoot, 'scripts', 'syncPlaceImages.cjs'), 'utf8');
  const sampleMatch = script.match(/const samplePlaceIds = \[([\s\S]*?)\];/);

  assert.ok(sampleMatch);
  assert.match(sampleMatch[1], /P_0262/);
});

test('auditPlaceImageApi reports API image gaps that would trigger frontend fallback', async () => {
  const tempDir = fs.mkdtempSync(path.join(os.tmpdir(), 'scenik-place-image-audit-'));
  const csvPath = path.join(tempDir, 'places.csv');
  fs.writeFileSync(
    csvPath,
    [
      'place_id,place_name,image_url',
      'P_0262,BTS Bus Stop,https://example.com/p-0262.jpg',
    ].join('\n'),
    'utf8',
  );

  const server = http.createServer((request, response) => {
    if (request.url === '/api/places/P_0262') {
      response.setHeader('content-type', 'application/json');
      response.end(JSON.stringify({
        success: true,
        data: {
          placeId: 'P_0262',
          placeName: 'BTS Bus Stop',
          imageUrl: '',
          thumbnailUrl: '',
        },
      }));
      return;
    }
    response.statusCode = 404;
    response.end('not found');
  });

  await new Promise((resolve) => server.listen(0, '127.0.0.1', resolve));
  try {
    const { port } = server.address();
    const { auditPlaceImageApi } = require('./auditPlaceImageApi.cjs');
    const report = await auditPlaceImageApi({
      csvPath,
      apiBaseUrl: `http://127.0.0.1:${port}/api`,
      placeIds: ['P_0262'],
    });

    assert.equal(report.summary.apiPlacesChecked, 1);
    assert.equal(report.summary.apiMissingImageUrl, 1);
    assert.equal(report.mismatches.length, 1);
    assert.equal(report.mismatches[0].placeId, 'P_0262');
    assert.equal(report.mismatches[0].frontendFallbackWouldTrigger, true);
    assert.equal(report.mismatches[0].localImageUrl, 'https://example.com/p-0262.jpg');
  } finally {
    await new Promise((resolve) => server.close(resolve));
    fs.rmSync(tempDir, { recursive: true, force: true });
  }
});
