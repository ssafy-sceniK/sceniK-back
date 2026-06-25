-- Merge duplicate place P_0252 into canonical place P_0027.
-- Safe to run once or repeatedly on a MySQL database after taking a backup.
-- Goal: keep P_0027, move user/runtime references from P_0252, then remove P_0252.

START TRANSACTION;

INSERT INTO content_places (content_id, place_id, relation_description, sort_order)
SELECT content_id, 'P_0027', relation_description, sort_order
FROM content_places
WHERE place_id = 'P_0252'
ON DUPLICATE KEY UPDATE
  relation_description = COALESCE(NULLIF(VALUES(relation_description), ''), relation_description),
  sort_order = LEAST(sort_order, VALUES(sort_order));

INSERT IGNORE INTO place_tags (place_id, tag)
SELECT 'P_0027', tag
FROM place_tags
WHERE place_id = 'P_0252';

INSERT IGNORE INTO place_likes (user_id, place_id, created_at)
SELECT user_id, 'P_0027', created_at
FROM place_likes
WHERE place_id = 'P_0252';

INSERT INTO visit_status (user_id, place_id, status, created_at, updated_at)
SELECT user_id, 'P_0027', status, created_at, updated_at
FROM visit_status
WHERE place_id = 'P_0252'
ON DUPLICATE KEY UPDATE
  status = VALUES(status),
  updated_at = GREATEST(updated_at, VALUES(updated_at));

UPDATE place_reviews pr
LEFT JOIN place_reviews existing
  ON existing.place_id = 'P_0027'
 AND existing.user_id = pr.user_id
SET pr.place_id = 'P_0027'
WHERE pr.place_id = 'P_0252'
  AND existing.review_id IS NULL;

DELETE pr
FROM place_reviews pr
JOIN place_reviews existing
  ON existing.place_id = 'P_0027'
 AND existing.user_id = pr.user_id
WHERE pr.place_id = 'P_0252';

UPDATE route_places rp
LEFT JOIN route_places existing
  ON existing.route_id = rp.route_id
 AND existing.place_id = 'P_0027'
SET rp.place_id = 'P_0027'
WHERE rp.place_id = 'P_0252'
  AND existing.route_place_id IS NULL;

DELETE rp
FROM route_places rp
JOIN route_places existing
  ON existing.route_id = rp.route_id
 AND existing.place_id = 'P_0027'
WHERE rp.place_id = 'P_0252';

UPDATE notifications
SET place_id = 'P_0027'
WHERE place_id = 'P_0252';

DELETE FROM content_places WHERE place_id = 'P_0252';
DELETE FROM place_tags WHERE place_id = 'P_0252';
DELETE FROM place_likes WHERE place_id = 'P_0252';
DELETE FROM visit_status WHERE place_id = 'P_0252';
DELETE FROM places WHERE place_id = 'P_0252';

COMMIT;

SELECT place_id, place_name, address, latitude, longitude
FROM places
WHERE place_id IN ('P_0027', 'P_0252');

SELECT 'content_places' AS table_name, COUNT(*) AS p0252_rows FROM content_places WHERE place_id = 'P_0252'
UNION ALL SELECT 'place_tags', COUNT(*) FROM place_tags WHERE place_id = 'P_0252'
UNION ALL SELECT 'place_likes', COUNT(*) FROM place_likes WHERE place_id = 'P_0252'
UNION ALL SELECT 'visit_status', COUNT(*) FROM visit_status WHERE place_id = 'P_0252'
UNION ALL SELECT 'place_reviews', COUNT(*) FROM place_reviews WHERE place_id = 'P_0252'
UNION ALL SELECT 'route_places', COUNT(*) FROM route_places WHERE place_id = 'P_0252'
UNION ALL SELECT 'notifications', COUNT(*) FROM notifications WHERE place_id = 'P_0252';
