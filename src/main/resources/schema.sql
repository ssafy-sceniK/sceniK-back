CREATE TABLE IF NOT EXISTS contents (
  content_id VARCHAR(64) PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  category VARCHAR(80) NOT NULL,
  subcategory VARCHAR(80),
  release_year INT,
  status VARCHAR(80),
  description TEXT,
  cast_names VARCHAR(500),
  thumbnail_url VARCHAR(500),
  is_popular BOOLEAN DEFAULT FALSE,
  country VARCHAR(80),
  ott_platform VARCHAR(120),
  tags VARCHAR(500),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS places (
  place_id VARCHAR(64) PRIMARY KEY,
  place_name VARCHAR(255) NOT NULL,
  region VARCHAR(80) NOT NULL,
  district VARCHAR(80),
  address VARCHAR(500),
  category VARCHAR(80),
  status VARCHAR(80) DEFAULT 'ACTIVE',
  description TEXT,
  opening_hours VARCHAR(120),
  closed_days VARCHAR(120),
  phone_number VARCHAR(80),
  image_url VARCHAR(500),
  indoor_outdoor VARCHAR(40),
  parking_available BOOLEAN DEFAULT FALSE,
  latitude DECIMAL(10, 7),
  longitude DECIMAL(10, 7)
);

CREATE TABLE IF NOT EXISTS content_places (
  content_id VARCHAR(64) NOT NULL,
  place_id VARCHAR(64) NOT NULL,
  relation_description VARCHAR(500),
  sort_order INT DEFAULT 0,
  PRIMARY KEY (content_id, place_id),
  CONSTRAINT fk_content_places_content FOREIGN KEY (content_id) REFERENCES contents(content_id),
  CONSTRAINT fk_content_places_place FOREIGN KEY (place_id) REFERENCES places(place_id)
);

CREATE TABLE IF NOT EXISTS place_tags (
  place_id VARCHAR(64) NOT NULL,
  tag VARCHAR(80) NOT NULL,
  PRIMARY KEY (place_id, tag),
  CONSTRAINT fk_place_tags_place FOREIGN KEY (place_id) REFERENCES places(place_id)
);

CREATE TABLE IF NOT EXISTS embedding_text (
  embedding_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  source_type VARCHAR(40) NOT NULL,
  source_id VARCHAR(64) NOT NULL,
  text TEXT NOT NULL,
  UNIQUE KEY uq_embedding_text_source (source_type, source_id)
);

CREATE TABLE IF NOT EXISTS users (
  user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  email VARCHAR(255) NOT NULL,
  nickname VARCHAR(120) NOT NULL,
  password_hash VARCHAR(500) NOT NULL,
  preferences TEXT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_users_email (email),
  UNIQUE KEY uq_users_nickname (nickname)
);

CREATE TABLE IF NOT EXISTS auth_tokens (
  token_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  token_hash VARCHAR(120) NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  expires_at DATETIME NOT NULL,
  revoked_at DATETIME,
  UNIQUE KEY uq_auth_tokens_hash (token_hash),
  KEY idx_auth_tokens_user_id (user_id),
  CONSTRAINT fk_auth_tokens_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS travel_routes (
  route_id VARCHAR(64) PRIMARY KEY,
  user_id BIGINT NOT NULL,
  title VARCHAR(255) NOT NULL,
  summary TEXT,
  content_id VARCHAR(64),
  content_ids TEXT,
  content_title VARCHAR(255),
  region VARCHAR(80) NOT NULL,
  duration_days INT NOT NULL,
  requested_duration_days INT,
  start_date DATE,
  end_date DATE,
  date_range_label VARCHAR(120),
  transport VARCHAR(80),
  companion VARCHAR(80),
  style VARCHAR(120),
  budget_min INT,
  budget_max INT,
  budget VARCHAR(120),
  trust_message VARCHAR(500),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_travel_routes_user_id (user_id),
  KEY idx_travel_routes_content_id (content_id),
  CONSTRAINT fk_travel_routes_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
  CONSTRAINT fk_travel_routes_content FOREIGN KEY (content_id) REFERENCES contents(content_id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS route_places (
  route_place_id VARCHAR(64) PRIMARY KEY,
  route_id VARCHAR(64) NOT NULL,
  place_id VARCHAR(64) NOT NULL,
  day_number INT NOT NULL,
  visit_order INT NOT NULL,
  recommended_time VARCHAR(40) NOT NULL,
  reason VARCHAR(700) NOT NULL,
  travel_tip VARCHAR(700) NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_route_places_route_place (route_id, place_id),
  UNIQUE KEY uq_route_places_route_day_order (route_id, day_number, visit_order),
  KEY idx_route_places_place_id (place_id),
  CONSTRAINT fk_route_places_route FOREIGN KEY (route_id) REFERENCES travel_routes(route_id) ON DELETE CASCADE,
  CONSTRAINT fk_route_places_place FOREIGN KEY (place_id) REFERENCES places(place_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS content_likes (
  user_id BIGINT NOT NULL,
  content_id VARCHAR(64) NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, content_id),
  CONSTRAINT fk_content_likes_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
  CONSTRAINT fk_content_likes_content FOREIGN KEY (content_id) REFERENCES contents(content_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS place_likes (
  user_id BIGINT NOT NULL,
  place_id VARCHAR(64) NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, place_id),
  CONSTRAINT fk_place_likes_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
  CONSTRAINT fk_place_likes_place FOREIGN KEY (place_id) REFERENCES places(place_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS visit_status (
  user_id BIGINT NOT NULL,
  place_id VARCHAR(64) NOT NULL,
  status VARCHAR(20) NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, place_id),
  CONSTRAINT fk_visit_status_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
  CONSTRAINT fk_visit_status_place FOREIGN KEY (place_id) REFERENCES places(place_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS place_reviews (
  review_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  place_id VARCHAR(64) NOT NULL,
  user_id BIGINT NOT NULL,
  rating DECIMAL(2, 1),
  content TEXT NOT NULL,
  status VARCHAR(20) DEFAULT 'ACTIVE',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_place_reviews_place_user (place_id, user_id),
  KEY idx_place_reviews_place_id (place_id),
  KEY idx_place_reviews_user_id (user_id),
  CONSTRAINT fk_place_reviews_place FOREIGN KEY (place_id) REFERENCES places(place_id) ON DELETE CASCADE,
  CONSTRAINT fk_place_reviews_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS community_posts (
  post_id VARCHAR(64) PRIMARY KEY,
  user_id BIGINT NOT NULL,
  title VARCHAR(255) NOT NULL,
  content TEXT NOT NULL,
  region VARCHAR(80),
  route_id VARCHAR(64),
  status VARCHAR(40) DEFAULT 'ACTIVE',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_community_posts_user_id (user_id),
  KEY idx_community_posts_route_id (route_id),
  CONSTRAINT fk_community_posts_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
  CONSTRAINT fk_community_posts_route FOREIGN KEY (route_id) REFERENCES travel_routes(route_id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS comments (
  comment_id VARCHAR(64) PRIMARY KEY,
  post_id VARCHAR(64) NOT NULL,
  user_id BIGINT NOT NULL,
  parent_comment_id VARCHAR(64),
  content TEXT NOT NULL,
  status VARCHAR(40) DEFAULT 'ACTIVE',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_comments_post_id (post_id),
  KEY idx_comments_user_id (user_id),
  CONSTRAINT fk_comments_post FOREIGN KEY (post_id) REFERENCES community_posts(post_id) ON DELETE CASCADE,
  CONSTRAINT fk_comments_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
  CONSTRAINT fk_comments_parent FOREIGN KEY (parent_comment_id) REFERENCES comments(comment_id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS post_tags (
  post_id VARCHAR(64) NOT NULL,
  tag VARCHAR(80) NOT NULL,
  PRIMARY KEY (post_id, tag),
  CONSTRAINT fk_post_tags_post FOREIGN KEY (post_id) REFERENCES community_posts(post_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS post_likes (
  user_id BIGINT NOT NULL,
  post_id VARCHAR(64) NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, post_id),
  CONSTRAINT fk_post_likes_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
  CONSTRAINT fk_post_likes_post FOREIGN KEY (post_id) REFERENCES community_posts(post_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS reports (
  report_id VARCHAR(64) PRIMARY KEY,
  user_id BIGINT NOT NULL,
  post_id VARCHAR(64) NOT NULL,
  reason VARCHAR(500),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_reports_post_id (post_id),
  UNIQUE KEY uq_reports_user_post (user_id, post_id),
  CONSTRAINT fk_reports_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
  CONSTRAINT fk_reports_post FOREIGN KEY (post_id) REFERENCES community_posts(post_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS notifications (
  notification_id VARCHAR(64) PRIMARY KEY,
  user_id BIGINT NOT NULL,
  title VARCHAR(255) NOT NULL,
  message VARCHAR(700),
  read_at DATETIME,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_notifications_user_id (user_id),
  CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);
