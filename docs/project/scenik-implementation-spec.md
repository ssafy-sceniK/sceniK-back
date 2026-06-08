# sceniK Implementation Spec

## Empty State UI Polish - 2026-05-28

### Goal And Scope

Unify empty, loading, no-data, and recoverable error states across the Vue frontend without changing backend contracts, route structure, or business-data persistence.

### Files/Folders Inspected

- `frontend/src/components/common/EmptyState.vue`
- `frontend/src/views/SimpleListView.vue`
- `frontend/src/views/CommunityView.vue`
- `frontend/src/views/NotificationsView.vue`
- `frontend/src/views/SearchView.vue`
- `frontend/src/views/ContentListView.vue`
- `frontend/src/views/PlaceListView.vue`
- `frontend/src/views/CourseView.vue`
- `frontend/src/views/CourseDetailView.vue`
- `frontend/src/views/CourseEditView.vue`
- `frontend/src/views/AiResultView.vue`
- `frontend/src/views/MyPageView.vue`
- `frontend/src/styles/profile-utility-overlays.css`
- `frontend/src/styles/course-management.css`

### Existing App Structure Summary

- The app already had a shared `EmptyState.vue`, but it only rendered a basic icon, title, and text.
- Saved courses, My Page activity lists, community feed, notifications, search, content list, place list, and AI route result all had separate or minimal no-data treatments.
- Loading and error messages were often plain text, visually disconnected from the surrounding card-based app language.

### Stitch Output Summary

No Stitch code was reused. The existing Vue/CSS app remained the source of truth.

### What To Reuse From Stitch

- General idea only: centered mobile status panels with clear hierarchy and primary recovery action.

### What Not To Reuse From Stitch

- Static prototype markup, external icon fonts, Tailwind classes, and any local-only fake data behavior.

### Target Routes/Pages

- `/my/activity/saved-routes`
- `/my/activity/liked-content`
- `/my/activity/liked-places`
- `/my/visit-planned`
- `/my/visit-completed`
- `/my/posts`
- `/my/comments`
- `/my/scraps`
- `/community`
- `/community/post/:id`
- `/notifications`
- `/search`
- `/contents`
- `/places`
- `/ai`
- `/ai/result`
- `/my/course-manage/:routeId`
- `/my/course-manage/:routeId/edit`

### Component Hierarchy

- `EmptyState.vue` is the reusable status component.
- Pages pass `icon`, `tone`, `title`, `text`, and optional primary/secondary CTA props.
- Pages keep their existing stores and API calls; no new frontend data source was introduced.

### Layout Rules

- Empty states use a soft white panel, subtle route-line visual, compact icon badge, clear title, lighter body copy, and full-width mobile CTAs.
- Inline empty states inside card panels, such as comments and add-place recommendations, remove the outer panel chrome to avoid card nesting.
- Loading and error states use the same component language as empty states.

### Mobile-First Behavior

- Status panels are sized for the existing 430px app shell.
- CTA buttons stay inside the mobile content column.
- No horizontal overflow or fragile absolute layout is introduced.

### Data Model And Adapter Plan

- No data model changes.
- Existing backend-backed saved routes, community posts, notifications, content/place lists, and engagement state remain the source of truth.

### Mock Data Plan If Backend APIs Are Unavailable

- No new mock business data was added.
- Existing local UI fallback behavior in stores remains unchanged.

### Loading States

- Added cohesive loading panels for saved/activity lists, community feed/detail, notifications, search, content list, place list, AI content selection, and course detail.

### Empty States

- Saved AI courses now point to AI Course generation and popular routes.
- Community no-post states now distinguish search-empty, board-empty, and global-empty cases.
- Notifications empty state distinguishes logged-in no-notification and logged-out states.
- Likes, visits, posts, comments, scraps, search, content, place, and AI result empty states now share the same structure.

### Error States

- Recoverable API failures in list/community/notification states now show an error-tone panel with a retry action.

### Login-Required States

- Course detail, course edit, community write, profile edit, and notifications logged-out states use the same status panel pattern where touched.

### Accessibility Notes

- CTAs remain native buttons or router links.
- Icon visuals are decorative with `aria-hidden`.
- Existing focus styles remain unchanged.

### RAG/placeId Validation Notes

- No AI route validation behavior changed.
- AI route result empty state still routes the user back to AI Course rather than fabricating a route.

### Acceptance Criteria

- Empty/no-data states are visually consistent.
- Saved AI course, community, notifications, likes, visits, search, content, place, and AI result empty states have clearer hierarchy and useful CTAs where appropriate.
- Loading and error states share the same design language.
- No frontend call to `sceniK-ai` is introduced.
- No fake local-only business data is added.

## Server-Backed MVP Implementation Pass - 2026-05-26

### Goal And Scope

Move the current localStorage-first MVP toward a real server-backed MVP without redesigning the UI or changing the approved Vue/Spring/MyBatis/MySQL stack.

P0 scope:

- DB-backed signup, login, current user, and profile update.
- Bearer-token auth backed by server-side token storage.
- DB-backed saved travel routes and route places.
- Course detail edit mode can search verified DB places, add them to a saved course, remove places, reorder places, and persist the result.
- Frontend auth and saved course source of truth moves from localStorage to backend APIs.
- `POST /api/ai/routes` returns deterministic DB-backed AI route results using real `placeId` values.

P1 scope if stable:

- DB-backed content likes, place likes, and visit status.
- Frontend content/place like and visit status actions should call the DB-backed engagement APIs for logged-in users.

P2 scope:

- Prepare AI/RAG integration only through a stable backend contract. Do not add FastAPI or an external vector DB in this pass.

### Files/Folders Inspected

- `AGENTS.md`
- `DESIGN.md`
- `PRD.md`
- `LEARNED_TECH_STACK.md`
- `progress.md`
- `backend/build.gradle`
- `backend/src/main/resources/application.yml`
- `backend/src/main/resources/schema.sql`
- `backend/src/main/resources/data.sql`
- `backend/src/main/java/com/scenik/controller/`
- `backend/src/main/java/com/scenik/service/`
- `backend/src/main/java/com/scenik/mapper/`
- `backend/src/main/resources/mapper/`
- `frontend/package.json`
- `frontend/src/api/`
- `frontend/src/stores/uiStore.js`
- `frontend/src/views/LoginView.vue`
- `frontend/src/views/SignupView.vue`
- `frontend/src/views/AiLoadingView.vue`
- `frontend/src/views/AiResultView.vue`
- `frontend/src/views/CourseDetailView.vue`
- `frontend/src/views/CourseEditView.vue`
- `frontend/src/views/SimpleListView.vue`
- `frontend/src/router/index.js`

### Existing App Structure Summary

- Frontend is Vue 3, Vite, Vue Router, Pinia, Axios, JavaScript, and plain CSS.
- Backend is Spring Boot MVC, MyBatis XML mappers, MySQL, Java 17 target.
- Existing backend APIs cover content, place, and integrated search only.
- Existing frontend keeps auth, saved courses, likes, visit status, community activity, and generated route persistence in `uiStore.js` localStorage state.
- Existing course editing UI already supports draft-day editing, delete, drag reorder, time changes, and candidate-place add from route candidates.

### Stitch Output Summary

Stitch remains reference-only. This pass does not port Stitch UI, static HTML, Tailwind classes, or prototype-only data.

### What To Reuse From Stitch

- Nothing directly for this pass.
- Existing route timeline, loading, verified-place, and course management UI already adapted from previous reference work remains in place.

### What Not To Reuse From Stitch

- Static HTML.
- React/Tailwind/prototype code.
- Fake save/share/edit actions that imply persistence without backend confirmation.

### Target Routes/Pages

- `/login`
- `/signup`
- `/preferences`
- `/ai/loading`
- `/ai/result`
- `/ai/edit`
- `/my/activity/saved-routes`
- `/my/course-manage/:routeId`
- `/my/course-manage/:routeId/edit`
- `/my/profile-edit`

### Component Hierarchy

- Keep the current Vue route/view/component hierarchy.
- Add API modules under `frontend/src/api/`.
- Keep Pinia `uiStore.js` as the integration point for current UI flows in this pass.
- Backend follows controller -> service -> mapper -> XML mapper -> DB.

### Layout Rules

- No broad visual redesign.
- Preserve existing route names, screens, and navigation.
- Keep course edit add-place UI inside the existing candidate panel area.
- Use existing button, empty state, list, and course edit styles unless a small state style is needed.

### Mobile-First Behavior

- Course edit remains single-column mobile-first.
- Add-place search results must not create horizontal overflow.
- Save/delete/add actions need clear loading or error feedback.

### Data Model And Adapter Plan

Backend tables added for this pass:

- `users`
- `auth_tokens`
- `travel_routes`
- `route_places`
- `content_likes`
- `place_likes`
- `visit_status`

Important ID rule:

- Existing `contents.content_id` and `places.place_id` are `VARCHAR(64)`, so all new FKs must use `VARCHAR(64)` for content/place references.

Frontend route canonical shape remains:

- `routeId`
- `title`
- `summary`
- `region`
- `durationDays`
- `candidatePlaces[]`
- `days[].places[]`

Backend route persistence shape uses:

- `travel_routes.route_id`
- `route_places.route_place_id`
- `route_places.place_id`
- `route_places.day_number`
- `route_places.visit_order`

### Mock Data Plan If Backend APIs Are Unavailable

- Auth and saved routes should use backend APIs as the primary path.
- Generated AI route may keep a frontend deterministic fallback only when `/api/ai/routes` is unavailable so the demo remains usable.
- Community localStorage can remain out of P0 scope.
- Existing mock `places` and `contents` can remain for fallback rendering and non-P0 lists.

### AI Course Flow Details

- `/ai/loading` calls `POST /api/ai/routes` first.
- Request includes selected content IDs, region, trip dates/duration, transport, companion, style, and budget.
- Backend selects verified candidate places from existing MySQL `contents`, `content_places`, and `places`.
- Backend response must be compatible with the current AI result route shape and include real `placeId` values.
- Generated route remains savable through `POST /api/routes`.

### AI Itinerary Result Details

- Result screen keeps current route display.
- Save action becomes async and stores through backend route API for logged-in users.
- Display logic should prefer route `candidatePlaces` and route day stops rather than assuming mock-only place data.

### Map Preview/Fallback Behavior

- Preserve existing `MapPreview` fallback.
- Route places returned by backend must include coordinates from `places.latitude` and `places.longitude` for map fallback rendering.

### Loading States

- Login/signup/profile update show action-level pending state where touched.
- Saved route list/detail/edit fetches show simple loading or existing empty state.
- AI loading screen keeps circular progress and calls backend before fallback.

### Empty States

- Saved course list remains empty when backend returns no routes.
- Course detail/edit shows existing empty state when route is missing or unauthorized.
- Add-place search shows an empty verified-place state when no DB places match.

### Error States

- API errors should surface through existing `toast`, inline `saveError`, or form error state.
- Backend returns 400/401/403/404/409 for validation, auth, ownership, missing route/place, and duplicate order/place conflicts.

### Login-Required States

Backend-required actions:

- Save route.
- Edit saved route.
- Delete saved route.
- Add/update/delete/reorder route places.
- Likes and visit status.
- Profile update.

Frontend should not fake success for these P0 backend actions.

### Accessibility Notes

- Preserve native buttons and labels.
- Search/add place controls should remain keyboard reachable.
- Loading buttons should be disabled while pending.

### RAG/placeId Validation Notes

- AI generation must start from DB candidate places.
- Generated and saved routes must reference existing `places.place_id` values.
- Backend route save/edit validates:
  - route owner for owned actions.
  - `placeId` exists.
  - `dayNumber` is within `durationDays`.
  - no duplicate `placeId` per route.
  - no duplicate `(route_id, day_number, visit_order)`.
  - route places have required day/order fields.

### Acceptance Criteria

- `POST /api/auth/signup`, `POST /api/auth/login`, `GET /api/users/me`, and `PATCH /api/users/me` work against DB-backed users.
- Passwords are stored as hashes, not plain text.
- Frontend sends bearer token through Axios for authenticated calls.
- `GET /api/users/me/routes`, `POST /api/routes`, `GET /api/routes/{routeId}`, `PATCH /api/routes/{routeId}`, and `DELETE /api/routes/{routeId}` are implemented.
- Route place add/update/delete/reorder endpoints are implemented with validation.
- Saved course detail/edit source of truth is backend route data.
- Course detail edit mode supports adding a searched verified DB place to a selected day.
- Added places persist after refetch/reload.
- `/api/ai/routes` returns a valid route using real DB `placeId` values.
- Relevant frontend/backend checks are run and documented.

## Goal And Scope

Integrate the Stitch-generated sceniK UI direction into the existing Vue frontend as a production-minded MVP mobile app. The implementation must prioritize:

1. App shell/navigation
2. Home entry into AI Course
3. AI Course content selection, condition input, loading, and itinerary result
4. Verified-place / `placeId`-aware mock adapters
5. Route validation helper
6. Explore/content/place shells needed to support the AI Course UX
7. Map preview/fallback and login-required shell actions

This pass does not implement backend APIs, real auth, real map SDKs, admin features, advanced community comments, or deep settings.

## CSS Architecture - 2026-05-21

The frontend keeps `frontend/src/styles.css` as the global style entry point because `frontend/src/main.js` and the frontend structure check expect that file to exist.

`styles.css` now imports ordered CSS modules under `frontend/src/styles/`:

- `tokens.css`: design tokens.
- `base.css`: reset, body, focus, image, and accessibility helpers.
- `shell.css`: app shell, top navigation, bottom navigation, splash layout, and screen padding.
- `controls.css`: buttons, form fields, section headers, chips, search, and horizontal rails.
- `content-cards.css`: poster, place, course, badge, list, notice, and tab card patterns.
- `detail-map-panels.css`: map preview, detail hero/body, panel surfaces, action grids, and sticky actions.
- `ai-course.css`: AI Course conditions, loading, AI result, share sheet, and result-specific layouts.
- `course-management.css`: course detail/edit, editable stops, candidate panels, and route timeline.
- `community.css`: community feed, board rows, linked course cards, comments, and post gallery.
- `profile-utility-overlays.css`: My Page, profile edit, utilities, settings, empty states, bottom sheets, toast, and desktop wrapper media query.

The split preserves the previous cascade by importing modules in the same order as the former single file. Component-scoped CSS, CSS modules, and external UI frameworks remain out of scope.

## Wireframe And v0 Visual Porting Pass - 2026-05-13

Goal: align the existing Vue frontend screens to the `wireframe/` layouts while porting the visual system from `sceni-k-travel-planner/` as directly as the current Vue + JavaScript + plain CSS stack allows.

Source priority for this pass:

1. `wireframe/` for screen structure, section order, and composition.
2. `sceni-k-travel-planner/` for visual design, palette, card styling, spacing, app bar, bottom navigation treatment, map/timeline motifs, and local example images.
3. Existing `frontend/` for working behavior, data flow, route validation, mock adapters, and Vue implementation conventions.

Scope:

- Preserve the current five-tab order: AI Course, Explore, Home, Community, My Page.
- Preserve Home as the center tab.
- Keep Home compact and avoid reintroducing the Home hero, Home search, or Home top AI Course CTA.
- Port v0 warm ivory, scenic teal, coral accent, sand, sky-mist, ink-navy, rounded cards, image overlays, route-line motifs, chips, badges, map preview, and timeline patterns.
- Align Explore to the wireframe content/map mode structure with search, chips, map preview, and nearby place cards.
- Align AI Course and AI Result styling to wireframe/v0 step progress, card rails, sticky CTA, verified candidate places, result trust panel, map preview, and route timeline.
- Keep Community and My Page as shell-level screens but restyle them with v0 card, feed, and summary treatments already available in the CSS system.

Non-scope:

- No backend integration, real auth, real map SDK, routing architecture rewrite, dependency additions, or external UI libraries.
- No changes to `mockScenikAdapter.js`, `routeValidation.js`, `placeId` handling, candidate-place-only generation, verified-place constraints, or login-required behavior.
- Onboarding/auth/preference wireframes are documented but deferred because the current app has no full auth route; the existing login-required shell remains the MVP auth surface.

Acceptance criteria for this pass:

- `progress.md` contains a wireframe mapping table and actual v0 file paths/assets inspected.
- Clear wireframes map to implemented or explicitly deferred Vue screens.
- App shell, Home, Explore, AI Course, AI Result, Community, and My Page visually show v0 influence while preserving the current logic.
- Local v0 images are reused through existing `frontend/public/images/` assets where appropriate.
- Lint, build, and diff whitespace checks are run and documented.

## Functional MVP Local Persistence Pass - 2026-05-13

Goal: turn shell-level user actions into a working frontend MVP using localStorage-backed services while preserving the existing Vue/plain CSS implementation, mock AI generation, route validation, and verified-place constraints.

Implemented service boundaries:

- `frontend/src/services/storageService.js`: JSON read/write helpers, in-memory fallback for Node smoke checks, localStorage key constants, ID helper.
- `frontend/src/services/authService.js`: mock signup, login, logout, session restore, onboarding flag, profile preference update, login-required notice shape.
- `frontend/src/services/userService.js`: per-user liked content, liked places, visit status, recent viewed place persistence.
- `frontend/src/services/contentService.js`: content list/detail/search helpers.
- `frontend/src/services/placeService.js`: place list/detail/search helpers.
- `frontend/src/services/routeService.js`: saved route persistence, saved route teaser projection, share text shell.
- `frontend/src/services/communityService.js`: persisted seed posts, create post, create post from route, post likes, comments.

localStorage keys:

- `sceniK.users`
- `sceniK.session`
- `sceniK.onboardingSeen`
- `sceniK.guestState`
- `sceniK.userState.{userId}`
- `sceniK.savedRoutes.{userId}`
- `sceniK.communityPosts`

Functional behavior:

- First-run onboarding can continue as guest, login, or sign up.
- Signup validates email/password/nickname and rejects duplicate email.
- Login validates required fields and credentials, then restores a session after refresh.
- Signup transitions to preference selection for K-content type, preferred region, and travel themes.
- Logged-out auth-required actions show login-required UI and do not fake success.
- Logged-in content/place likes, planned/completed visits, saved routes, community posts, post likes, and comments persist locally.
- Opening place detail updates recent viewed places for guest or current user.
- Saving an AI route stores the validated generated route and shows it in Home recent saved routes and My Page.
- AI Result save/share/regenerate/post actions now provide meaningful MVP behavior.
- My Page shows a login prompt when logged out and actual user-local data when logged in.
- Explore search, content/place category filtering, and map mode use service helpers.

Validation rules preserved:

- `mockScenikAdapter.js` still generates routes from verified candidate places only.
- `routeValidation.js` still rejects invented `placeId`s, missing `placeId`s, invalid day/order data, and duplicate same-day visit orders.
- No backend dependencies or UI libraries were added.

## Targeted Home IA Update - 2026-05-12

Goal: update only the sceniK Home screen information architecture and the five-tab bottom navigation order.

Scope:

- Change bottom navigation order to AI Course, Explore, Home, Community, My Page.
- Keep Home as the center tab and keep tab switching mapped to the correct screens.
- Rebuild Home as a dashboard for recent activity, recommended K-content, popular AI travel routes, and notices.
- Preserve the existing Vue + JavaScript + plain CSS stack and the current sky-blue sceniK visual direction.

Non-scope:

- Do not restart the full redesign.
- Do not rewrite the app shell, AI Course flow, mock adapters, route validation, placeId validation, verified-place constraints, MapPreview, RouteTimeline, or login-required behavior.
- Do not add dependencies or backend calls.

Target Home IA:

1. 최근 본 장소
   - 최근 조회 장소
   - 최근 저장 코스
2. 추천 콘텐츠
   - 인기 드라마
   - 인기 영화
   - 인기 스타
   - AI 맞춤 추천
3. 인기 여행 코스
   - 실시간 인기 코스
   - 지역별 인기 코스
   - 테마별 추천 코스
4. 공지
   - 공지사항

Acceptance criteria for this targeted pass:

- Bottom nav order is AI Course, Explore, Home, Community, My Page.
- AI Course tab opens the AI Course screen and Home tab opens the Home screen.
- Home contains 최근 본 장소, 최근 조회 장소, 최근 저장 코스, 추천 콘텐츠, 인기 드라마, 인기 영화, 인기 스타, AI 맞춤 추천, 인기 여행 코스, 실시간 인기 코스, 지역별 인기 코스, 테마별 추천 코스, and 공지사항.
- Existing AI Course generation and route validation code paths are untouched.
- Home mock data is clearly local/mock and replaceable by future API adapters.

## Files/Folders Inspected

- `AGENTS.md`
- `DESIGN.md`
- `PRD.md`
- `README.md`
- `LEARNED_TECH_STACK.md`
- `progress.md`
- `frontend/package.json`
- `frontend/package-lock.json`
- `frontend/src/main.js`
- `frontend/src/styles.css`
- `frontend/src/components/`
- `frontend/src/data/mockHomeData.js`
- `stitch_scenik_ai_travel_planner/`
- `stitch_scenik_ai_travel_planner/scenik_design_system/DESIGN.md`

## Existing App Structure Summary

- Frontend is Vue 3 + Vite with JavaScript ESM components.
- No Vue Router is installed; the current app is a single mounted Vue app.
- Components are plain `.js` Vue option objects under `frontend/src/components/`.
- Styling is centralized in `frontend/src/styles.css`.
- Current data is static mock data under `frontend/src/data/`.
- Available npm scripts: `dev`, `build`, `lint`.
- No test or typecheck script exists.
- No backend, API adapter, auth layer, or map SDK is currently wired into the frontend.

## Stitch Output Summary

Stitch output is static HTML plus screenshots. It includes screens for onboarding, auth, preferences, home dashboard, AI Course selection, travel conditions, generating route, itinerary result, explore, content detail, place detail, map view, community, My Page, settings, and notifications.

Generated code uses Tailwind CDN, Google fonts, Material Symbols, static links, remote generated images, and duplicated HTML. It is not directly compatible with the repository architecture or technology constraints.

## What To Reuse From Stitch

- Five-tab mobile bottom navigation concept
- Poster cards and selected chips
- Travel condition selector cards
- Scenic AI loading pattern
- AI reason panel
- Day-by-day itinerary timeline
- Verified place badges
- Numbered map pins and route preview
- Place/content hero card hierarchy
- Community feed card concept
- Color direction: scenic teal, sunset coral, warm ivory, sky mist, sand, ink navy
- Mobile spacing rhythm: 20px container, 8/16/24/32px stack, 44px touch targets

## What Not To Reuse From Stitch

- Tailwind CDN classes and generated static HTML
- Material Symbols dependency
- Google-hosted generated image URLs as hard dependencies
- `href="#"` fake links
- Fake save/share/regenerate actions that imply success
- Unvalidated itinerary data
- Map images that cannot fall back when API keys are missing
- Viewport settings that disable zoom
- Desktop-only side navigation patterns that conflict with the five-tab app shell

## Target Routes/Pages

No router dependency will be added in this pass. The app will use a small internal view state to simulate MVP screens:

- `home`
- `explore`
- `contentDetail`
- `placeDetail`
- `aiCourse`
- `aiResult`
- `community`
- `myPage`

The five main tabs are currently ordered per the latest product request:

1. AI Course
2. Explore
3. Home
4. Community
5. My Page

## Component Hierarchy

- `AppShell`
  - top app bar
  - slot content
  - bottom navigation
- MVP screen components under `frontend/src/screens/`
  - `HomeScreen` is extracted to `frontend/src/screens/HomeScreen.js`.
  - `ExploreScreen` is extracted to `frontend/src/screens/ExploreScreen.js`.
  - `ContentDetailScreen` is extracted to `frontend/src/screens/ContentDetailScreen.js`.
  - `PlaceDetailScreen` is extracted to `frontend/src/screens/PlaceDetailScreen.js`.
  - `AiCourseScreen` is extracted to `frontend/src/screens/AiCourseScreen.js`.
  - `AiResultScreen` is extracted to `frontend/src/screens/AiResultScreen.js`.
  - `CommunityScreen` is extracted to `frontend/src/screens/CommunityScreen.js`.
  - `MyPageScreen` is extracted to `frontend/src/screens/MyPageScreen.js`.
- Reusable UI components:
  - `Button`
  - `ContentCard`
  - `PlaceCard`
  - `VerifiedBadge`
  - `MapPreview`
  - `RouteTimeline`
  - `LoginRequiredPanel`

Refactor rule: keep screen components presentational. `main.js` owns state, localStorage service calls, route generation, validation, and cross-screen navigation; screen modules receive props and emit events only.

## Layout Rules

- Mobile-first 360px to 430px layout.
- No horizontal overflow.
- Minimum 44px touch targets for buttons, tabs, chips, and controls.
- User-facing sceniK app copy is English-first for international K-content travelers in this MVP. Final Codex reports remain Korean.
- Use bottom navigation for primary movement.
- Use sticky CTA only inside AI Course steps where useful.
- Use cinematic image cards, but keep copy short and scannable.
- Use scenic teal for primary route/navigation identity and sunset coral for accent actions.
- Avoid generic purple styling.

## Mobile-First Behavior

- Default layout is single-column mobile app shell.
- Desktop may widen the container and use grid cards, but must not become a landing page.
- AI Course should feel like a focused step flow.
- Result screen should prioritize route trust: verified badges, map fallback, day/order timeline, reasons, tips.

## Data Model And Adapter Plan

Create mock data and adapter logic that models future backend shapes:

- contents
- places
- content-place relationships
- route candidate places
- AI route result
- liked target state
- planned/completed visit state
- saved route state

Every mock place must include:

- `placeId`
- `name`
- `region`
- `category`
- `address`
- `latitude`
- `longitude`
- `relatedContentId`
- `relatedContentTitle`
- `sceneDescription`
- `verified`
- `imageUrl`

AI route places must include:

- `placeId`
- `dayNumber`
- `visitOrder`
- `recommendedTime`
- `stayMinutes`
- `reason`
- `travelTip`

## Mock Data Plan

Backend APIs are unavailable, so use clearly marked mock adapters in `frontend/src/adapters/`.

Adapters should expose simple functions such as:

- `getFeaturedContents()`
- `getPlacesForContent(contentId)`
- `getRouteCandidates(filters)`
- `generateMockAiRoute(request)`
- `validateAiRoute(route, candidatePlaces, request)`
- `toggleLike(targetType, targetId)`
- `setVisitStatus(placeId, status)`

The generated route must use only candidate places. Save/share/like/visit actions should show login-required UI because auth is unavailable.

## AI Course Flow Details

Step 1: Select K-content.

- Search placeholder for dramas, movies, stars, and places.
- Category chips.
- Poster/content cards.
- Selected content summary.

Step 2: Enter travel conditions.

- Region
- Duration
- Transport
- Companion type
- Travel style
- Budget

Step 3: Generate route.

- Use mock adapter.
- Show scenic loading/trust messages.
- Generate a JSON-like route result from verified candidates only.
- Validate before rendering.

Step 4: Itinerary result.

- Route title and summary.
- Based on verified places message.
- Day-by-day timeline.
- Visit order.
- Verified place badge.
- Map preview/fallback.
- Estimated travel time.
- Transportation tips.
- Recommendation reasons.
- Related K-content context.
- Save/share/regenerate/post to community actions with login-required shell state where auth is unavailable.

## AI Itinerary Result Details

The result must show:

- `route.title`
- `route.summary`
- selected content
- selected conditions
- validation status
- day count
- route places with `placeId`
- travel time between stops when present
- day-by-day route timeline
- trust message based on candidate places

If validation fails, show an error state and do not pretend the route is usable.

## Map Preview/Fallback Behavior

No map SDK key is configured. Use a CSS/SVG-style fallback map preview with:

- region label
- numbered pins
- route line
- fallback message such as "Map preview shown without live map API"
- coordinates stored in data for later map API integration

## Loading States

- AI Course generation loading with step messages.
- Explore loading shell if mock adapter is delayed.

## Empty States

- No content results.
- No candidate places for selected filters.
- No saved/liked/planned items in My Page shell.

## Error States

- Route validation failure.
- Missing candidate places.
- Missing selected content.
- Map API unavailable fallback, not a crash.

## Login-Required States

Show a login-required panel or inline notice for:

- Save route
- Share route
- Like content/place/route/post
- Mark planned/completed
- Post to community
- My Page data actions

## Accessibility Notes

- Keep native buttons for actions.
- Avoid disabled zoom settings.
- Provide labels for icon-only controls.
- Maintain color contrast on image overlays.
- Preserve keyboard focus visibility.
- Use semantic headings per screen.

## RAG/`placeId` Validation Notes

Route validation must conceptually check:

- all route `placeId`s exist in candidate places
- no invented places
- day count matches duration when possible
- `dayNumber` is valid
- `visitOrder` is present
- duplicate `visitOrder` is not used within the same day
- required fields exist

The UI must surface this through verified badges, candidate-place messaging, route timelines, and validation/trust copy.

## Acceptance Criteria

- `AGENTS.md` exists and includes sceniK project rules.
- `docs/scenik-implementation-spec.md` exists.
- App brand is sceniK.
- Main navigation uses AI Course, Explore, Home, Community, My Page with Home as the center tab.
- Home clearly routes users into AI Course.
- AI Course flow includes content selection, condition input, generation state, and result.
- AI result shows day-by-day timeline, visit order, map preview/fallback, verified badges, recommendation reasons, travel tips, and save/share/regenerate/community actions.
- Mock AI route uses only verified candidate places and valid `placeId`s.
- Route validation helper exists and blocks invalid mock route rendering.
- Explore, content detail, and place detail shells support the AI Course concept.
- Loading, empty, error, and login-required states exist where relevant.
- Relevant npm checks are run and documented.
- No remaining P0 review issues.
# Figma Make Prototype Rebuild - 2026-05-20

## Goal And Scope

Rebuild the Figma Make prototype in `scenik/prototype` as a real sceniK application while keeping the prototype as reference-only material. The implementation uses the requested stack:

- Frontend: Vue 3, Vite, Vue Router, Pinia, Axios, plain CSS.
- Backend: Spring Boot MVC, MyBatis, MySQL.

React, Next.js, Tailwind, shadcn/Radix, external UI frameworks, JPA/Hibernate, Prisma, MongoDB, Firebase, and other unapproved stacks are not used in the rebuilt app.

## Prototype Structure Summary

The prototype contains onboarding, login, signup, preference selection, Home, Explore, AI Course, AI result/save/share/edit, Community, My Page, notifications, settings, and notices. Its core interaction patterns are fixed top navigation, fixed five-tab bottom navigation, card rails, poster cards, place cards, chip filters, search flows, map placeholders, bottom sheets, sticky CTAs, loading states, and route timelines.

## Rebuilt Frontend Structure

- `frontend/src/router/index.js`: route map for onboarding/auth, Home, Explore, content/place detail, search, AI flow, Community, My Page, map, notices, and shell subroutes.
- `frontend/src/stores/*`: Pinia stores for content, places, search, and local UI/user state.
- `frontend/src/api/*`: Axios API adapters for contents, places, and integrated search.
- `frontend/src/views/*`: Vue views that recreate the prototype screen flow.
- `frontend/src/components/*`: layout, common, content, place, course/map/timeline components.
- `frontend/src/styles.css`: plain CSS mobile app frame, default dark mode, card/chip/nav/search/map/timeline styles.

## Rebuilt Backend Structure

- `backend/src/main/java/com/scenik/controller`: REST controllers for contents, places, search.
- `backend/src/main/java/com/scenik/service`: service layer.
- `backend/src/main/java/com/scenik/mapper`: MyBatis mapper interfaces.
- `backend/src/main/java/com/scenik/domain`: domain objects for `Content` and `Place`.
- `backend/src/main/java/com/scenik/dto`: response DTOs.
- `backend/src/main/resources/mapper`: MyBatis XML SQL.
- `backend/src/main/resources/schema.sql` and `data.sql`: MySQL schema and temporary seed data because no CSV files were found.

## API Acceptance Criteria

- `GET /api/contents` supports `category`, `keyword`, and `popular`.
- `GET /api/contents/{contentId}` returns content detail.
- `GET /api/contents/{contentId}/places` returns related places through `content_places`.
- `GET /api/places` supports `region`, `district`, `category`, and `keyword`.
- `GET /api/places/{placeId}` returns place detail.
- `GET /api/search` returns integrated content/place search results.

## Known Environment Notes

The current local shell has Java 8 and no Gradle executable, so Spring Boot 3 backend runtime verification requires installing JDK 17 and Gradle 8.x or generating a full Gradle wrapper. Frontend production build was verified with approved external execution because sandboxed Vite/esbuild process spawning is blocked with `spawn EPERM`.

# Week 2 P0 Stabilization - 2026-05-28

## Goal And Scope

Stabilize the server-backed MVP without broad refactors or new product features. The frontend continues to call only the Spring Boot backend. The backend may call an AI server later, but the current `POST /api/ai/routes` contract remains backend-owned.

## Backend API Stabilization

- Controller parameters now use explicit `@PathVariable`, `@RequestParam`, and `@RequestHeader` names instead of relying on Java reflection parameter metadata.
- Gradle Java compiler configuration enables `-parameters`.
- Gradle Wrapper files were added under `backend/` so the backend can be built without a preinstalled Gradle binary.
- Authentication remains opaque DB-stored token based, not JWT.
- `POST /api/auth/logout` revokes the current bearer token.
- `POST /api/auth/token/refresh` accepts the existing opaque refresh token and issues a new opaque token pair.
- `PUT /api/places/{placeId}/visit-status` and `DELETE /api/places/{placeId}/visit-status` are the preferred visit-status endpoints.
- Existing `POST /api/places/{placeId}/visit-status` is retained for backward compatibility.
- Visit status API values are `WANT_TO_GO` and `VISITED`; the frontend maps these to its current local labels where needed.
- `GlobalExceptionHandler` returns consistent JSON errors with `code` and `message`.

## ID Policy

- `contentId` and `placeId` remain string IDs from CSV/source data, such as `C_DRAMA_001` and `P_001`.
- `categoryId` may remain a string code if introduced later.
- `routeId` currently remains a string because the existing schema, DTOs, backend services, and frontend route state already use string IDs deeply.

## Validation Notes

- Backend package verification uses `backend/gradlew.bat clean build`.
- Frontend course smoke now tests the async backend/API route persistence flow with mocked HTTP adapters instead of old synchronous localStorage assumptions.
- Real local API smoke with MySQL `ssafy/ssafy` passed for signup, login, refresh, AI route generation, save, My Courses, detail, add/remove place, likes, visit status, search, content detail, place detail, and logout.

# Week 2 Core API Alignment - 2026-05-28

## Goal And Scope

Align the Spring Boot/MyBatis backend API surface for content, place, maps, routes, deterministic AI route generation, and temporary admin content/place management. The frontend remains backend-only; no direct `sceniK-ai` or Kakao calls are introduced from the frontend.

## API Decisions

- `contentId`, `placeId`, and future `categoryId` values remain string codes.
- `routeId` remains a string in this repo because the current schema, MyBatis mappers, DTOs, and frontend route state already use string route IDs deeply.
- Content and place card images use `thumbnailUrl` as the canonical response field. `imageUrl`/`placeName` are still returned where needed as compatibility aliases for existing Vue screens.
- Route responses now expose stable `routeId`, `durationDays`, `sourceType`, `isPublic`, `places`, `createdAt`, and `updatedAt` fields. `sourceType` and `isPublic` are currently returned as service-level defaults because the current schema does not store those columns.
- AI generation uses an in-memory `generationId` cache for Week 2. Regenerate reuses the original request and rotates deterministic DB-backed candidate ordering. This cache is intentionally temporary and will be replaced when FastAPI/RAG integration is added.

## Implemented Endpoint Groups

- Content:
  - `GET /api/contents`
  - `GET /api/contents/categories`
  - `GET /api/contents/popular`
  - `GET /api/contents?sort=popular`
  - `GET /api/contents/{contentId}`
  - `GET /api/contents/{contentId}/places`
  - `POST /api/contents/{contentId}/likes`
  - `DELETE /api/contents/{contentId}/likes`
- Places:
  - `GET /api/places`
  - `GET /api/places/nearby`
  - `GET /api/places/{placeId}`
  - `POST /api/places/{placeId}/likes`
  - `DELETE /api/places/{placeId}/likes`
  - `PUT /api/places/{placeId}/visit-status`
  - `DELETE /api/places/{placeId}/visit-status`
- Maps:
  - `GET /api/maps/geocode`
  - `GET /api/maps/reverse-geocode`
  - `GET /api/maps/local-search`
- AI routes:
  - `POST /api/ai/routes`
  - `POST /api/ai/routes/{generationId}/regenerate`
- Admin:
  - `POST /api/admin/contents`
  - `PATCH /api/admin/contents/{contentId}`
  - `DELETE /api/admin/contents/{contentId}`
  - `POST /api/admin/places`
  - `PATCH /api/admin/places/{placeId}`
  - `DELETE /api/admin/places/{placeId}`

## Known Limitations

- Kakao map endpoints are Week 2 skeletons. They return safe empty results unless/until real Kakao REST integration is implemented behind the backend.
- Admin endpoints are protected by the current bearer-token auth only. Role-based admin authorization is marked as a TODO.
- Content delete uses the existing `contents.status` column as a soft delete. Place delete is hard delete after removing local `content_places` and `place_tags` references.
- `sceniK-ai` is not present in this workspace, so AI remains deterministic DB-backed recommendation.

# Week 3 Frontend API Preparation - 2026-05-28

## Goal And Scope

Prepare the Vue frontend for backend integration by keeping all HTTP traffic behind `sceniK-back`, replacing community business-data mutations with API calls, and updating the course smoke test to validate async backend route persistence.

## Data Flow Decisions

- `VITE_API_BASE_URL` is the frontend backend base URL. The default remains `/api` for dev proxy compatibility.
- Authorization headers are attached by the shared Axios instance.
- Refresh-token retry is attempted once through `POST /api/auth/token/refresh`; if refresh fails, token storage is cleared.
- Saved routes, likes, visit status, and community posts are no longer persisted inside the Pinia UI-state localStorage payload.
- `generatedRoute`, onboarding state, route form conditions, and recent-view UI state may remain local UI state.

## Community API Contract Expected By Frontend

- `GET /api/posts`
- `GET /api/posts/{postId}`
- `POST /api/posts`
- `PATCH /api/posts/{postId}`
- `DELETE /api/posts/{postId}`
- `GET /api/posts/{postId}/comments`
- `POST /api/posts/{postId}/comments`
- `PATCH /api/posts/{postId}/comments/{commentId}`
- `DELETE /api/posts/{postId}/comments/{commentId}`
- `POST /api/posts/{postId}/likes`
- `DELETE /api/posts/{postId}/likes`
- `POST /api/posts/{postId}/reports`

## Validation Notes

- Frontend lint passed.
- Frontend course smoke passed with mocked backend adapters for signup/login, AI route generation, route save payload, route list/detail, and route update.
- Frontend production build passed after running outside the sandbox because esbuild spawn was blocked by sandbox `EPERM`.

## Known Limitations

- Community scrap state remains session-local because there is no matching backend scrap endpoint in the current contract.
- AI fallback cache remains UI-local for demo resilience; real AI/RAG remains backend-owned.

# Week 4 AI/RAG Skeleton - 2026-05-28

## Goal And Scope

Create a separate `sceniK-ai/` FastAPI skeleton that can later be called only by `sceniK-back`. The frontend remains backend-only and no real OpenAI key or external vector DB is introduced.

## Structure Added

- `sceniK-ai/app/main.py`
- `sceniK-ai/app/api/health.py`
- `sceniK-ai/app/api/routes.py`
- `sceniK-ai/app/api/embeddings.py`
- `sceniK-ai/app/core/config.py`
- `sceniK-ai/app/core/security.py`
- `sceniK-ai/app/services/route_recommender.py`
- `sceniK-ai/app/services/embedding_service.py`
- `sceniK-ai/app/services/vector_store.py`
- `sceniK-ai/app/schemas/route.py`
- `sceniK-ai/app/schemas/embedding.py`

## Internal Endpoints

- `GET /health`
- `POST /ai/routes`
- `POST /ai/embeddings/rebuild`
- `PUT /ai/embeddings/{placeId}`

All `/ai/...` endpoints require `Internal-Api-Key`. The configured value is read from `INTERNAL_API_KEY`.

## AI/RAG Behavior

- Route generation currently sorts backend-provided `candidatePlaces` deterministically by region match, related content match, and `placeId`.
- Places are distributed across requested `durationDays` with stable `visitOrder` values.
- OpenAI is not called. `OPENAI_API_KEY` and `ENABLE_OPENAI` are reserved for a later integration pass.
- Embedding endpoints store data in an in-memory stub. TODO comments mark the future FAISS/Chroma or approved vector-store replacement point.

## Validation Notes

- `python -m pip install -r requirements.txt`: passed after approved external package download.
- `python -m pytest`: passed.
- `python -m compileall app tests`: passed.
- Temporary uvicorn launch on `127.0.0.1:8010` returned `/health` status `ok`, then the process was stopped.

## Known Limitations

- No production RAG, vector search, prompt orchestration, or OpenAI call is implemented.
- The in-memory embedding store is process-local and resets on restart.
- Spring Boot is not wired to this service yet; the existing backend deterministic DB fallback remains the active production path.

# Week 3 Integration Readiness Docs - 2026-05-28

## Goal And Scope

Add lightweight integration assets for Week 3: Swagger discoverability, HTTP request collection, seed reset instructions, demo flow, API field naming checklist, and frontend state migration checklist.

## Swagger/OpenAPI

- `springdoc-openapi-starter-webmvc-ui` is installed in `backend/build.gradle`.
- Added minimal OpenAPI metadata config.
- Swagger UI is available at `/swagger-ui/index.html`.
- OpenAPI JSON is available at `/v3/api-docs`.

## Added Documentation And Test Assets

- `api-tests/scenik-week3.http`
- `docs/seed-reset.md`
- `docs/demo-flow.md`
- `docs/api-field-naming.md`
- `docs/frontend-state-migration.md`

## Week 3 Stabilization Pass - 2026-05-28

### Goal And Scope

Make integration 500 errors observable, align backend/frontend community paths, and harden the route-save contract without adding broad new UI features.

### Changes

- `GlobalExceptionHandler` now logs unexpected exceptions with request method and URI while keeping safe client responses.
- Community API is implemented under `/api/posts`, `/api/posts/{postId}/comments`, `/api/posts/{postId}/likes`, `/api/posts/{postId}/reports`, and `/api/notifications`.
- Frontend community calls now use `/posts` instead of `/community/posts`.
- `POST /api/routes` accepts both full AI route responses and the simple saved-route payload with a flat `places` array.
- Admin place delete no longer depends on new `places.status`; it deletes dependent rows in a controlled order to avoid FK 500s on older local schemas.
- Map helper endpoints accept both `address`/`keyword` and legacy `query` parameters.
- Split My Page endpoints are available for liked contents, liked places, visit status, and posts.
- Added `scripts/reset-local-db.ps1`, `scripts/reset-local-db.sh`, and `api-tests/backend-db-smoke.http`.

### Observed Root Cause

The local MySQL database had an older `places` table without a `status` column. A new query referenced `p.status`, causing `GET /api/places` to return 500. The code was changed to avoid requiring that column at runtime, and reset scripts were added for future schema changes.

### Validation

- `backend/.\\gradlew.bat clean build`: passed after Maven-to-Gradle migration.
- `frontend npm run build`: passed after approved external execution because sandboxed esbuild returned `spawn EPERM`.
- `frontend npm run smoke:course`: passed.
- `frontend npm run lint`: passed.
- Local backend API smoke with `DB_USERNAME=ssafy`, `DB_PASSWORD=ssafy`: signup, login, AI route generation, simple route save, my routes, post create, comment create, place like, and visit status passed.
