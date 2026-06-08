# Progress

## 2026-05-29 Backend Maven To Gradle Migration

### Task chosen
- Migrate the Spring Boot backend build from Maven Wrapper to Gradle Wrapper while preserving existing controllers, services, mappers, DTOs, schema/data SQL, and API contracts.

### Changes made
- Added `backend/build.gradle`, `backend/settings.gradle`, Gradle Wrapper scripts, and `backend/gradle/wrapper/`.
- Kept Spring Boot at `3.3.6`, Java at `17`, MyBatis starter at `3.0.4`, and springdoc at `2.6.0` to match the previously working Maven build.
- Added Gradle `-parameters` compiler configuration so Spring MVC parameter binding does not depend on missing reflection parameter names.
- Added a minimal permissive `SecurityConfig` because the Gradle target stack includes `spring-boot-starter-security`; existing custom token validation remains in controllers/services.
- Archived Maven-only files under `archive/root-migration-20260529/backend-maven-backup/` and removed `backend/target/`.
- Updated backend/root README and AGENTS guidance to use Gradle commands.

### Validation result
- `backend .\gradlew.bat clean test`: passed after approved external execution because sandbox access to the Gradle user-home lock file was denied.
- `backend .\gradlew.bat clean build`: passed after approved external execution.
- No Spring Boot server was started.

## 2026-05-28 Frontend Empty State UI Polish

### Task chosen
- Polish empty, no-data, loading, and recoverable error states across the Vue frontend while preserving existing backend API integration and navigation.

### Changes made
- Expanded `EmptyState.vue` into a reusable status panel with tone, icon, compact mode, primary CTA, secondary CTA, and retry-button support.
- Replaced the old dashed empty-state styling with a soft card-like panel, lightweight route-line visual, clearer title/body hierarchy, and consistent mobile CTA spacing.
- Applied the unified empty-state pattern to saved AI courses, My Page activity lists, community feed/detail/comments, notifications, search, content list, place list, AI content selection, AI result missing state, course detail missing/loading state, course edit missing/login states, community write login state, and profile edit login state.
- Added loading/error panels for backend-backed list/community/notification/search/content/place/course-detail flows where the current store already exposes state.
- Kept all frontend calls routed through the Spring Boot backend APIs; no direct `sceniK-ai` call or new fake business data source was added.

### Design decisions
- Empty states use the existing sceniK card, pill button, border, and mobile shell language instead of a new visual system.
- Inline empty states inside existing cards remove the outer panel chrome to avoid nested-card UI.
- Saved-course empty state now routes users to AI Course and popular courses.
- Community empty state now distinguishes search-empty, board-empty, and first-post-empty cases.
- Notifications empty state now distinguishes logged-out and logged-in no-notification cases.

### Backend support notes
- No backend changes are required for this UI pass.
- Future improvement: a notification mark-read endpoint would let the "모두 읽음" state persist after reload.
- Future improvement: a backend scrap/bookmark endpoint would let My Page scraps stop depending on current session-local post metadata.

### Validation result
- `npm run lint`: passed.
- `npm run smoke:course`: passed.
- `npm run build`: sandbox run failed with known Vite/esbuild `spawn EPERM`; approved external execution passed with the existing large chunk warning.
- `git diff --check`: passed with existing CRLF normalization warnings only.

## 2026-05-26 Server-Backed MVP Implementation Pass

### Task chosen
- Move sceniK from localStorage/mock-first MVP behavior toward real Spring/MyBatis/MySQL-backed auth, saved routes, route place editing, and deterministic AI route generation.

### Prompt Director execution prompt
- Implement a full-stack MVP pass without a visual redesign.
- Preserve the current Vue 3/Vite/Router/Pinia/Axios frontend and Spring Boot/MyBatis/MySQL backend.
- Use parallel specialized agents for backend architecture, frontend integration, course editing UX, AI integration, and later QA.
- Prioritize P0: auth, saved routes, route detail/edit, add-place-in-edit, localStorage replacement for saved course flows, and `/api/ai/routes`.
- Implement P1 likes/visit status only if P0 remains stable.
- Keep FastAPI/RAG as a future TODO unless already scaffolded.

### Parallel analysis agents
- Backend API Architect Agent inspected backend structure and proposed DB/API additions for users, auth tokens, travel routes, route places, likes, and visit status.
- Frontend Integration Agent summarized localStorage/mock replacement risks and recommended keeping Pinia action names while moving implementation behind API adapters.
- Course Editing UX Agent inspected saved course detail/edit flow and confirmed the current edit view already supports draft editing, deletion, and reorder, but needs verified place search/add for saved courses.
- AI Integration Agent inspected AI generation and confirmed no FastAPI/RAG server exists; the MVP contract should be `POST /api/ai/routes` implemented in Spring with DB-backed candidate selection.

### Current architecture found
- Backend currently exposes content, place, and search read APIs only.
- Backend uses raw DTO/list responses, Java POJOs, Java records, MyBatis interfaces, and XML mappers.
- Existing DB uses `contents.content_id VARCHAR(64)` and `places.place_id VARCHAR(64)`.
- Frontend stores auth, saved routes, generated route, likes, visit status, and community activity in `uiStore.js` localStorage state.
- Course detail and edit currently resolve routes through `uiStore.resolveCourseDetail(routeId)`.
- AI loading currently builds a mock route in the frontend and validates it with `routeValidation.js`.

### Planned implementation scope
- Add DB-backed auth with PBKDF2 password hashes and bearer tokens stored by token hash.
- Add DB-backed saved route APIs and route place edit APIs.
- Add deterministic backend AI route generation from existing MySQL places/content relations.
- Add frontend API modules for auth, routes, and AI routes.
- Convert login/signup/profile, saved route list/detail/save/edit/delete, and generated route save to backend APIs.
- Extend course edit mode so saved routes can search verified DB places and add them to a selected day.
- Keep community localStorage and non-P0 local state out of scope unless trivial.

### Implementation status
- Implemented P0 backend auth, route persistence, route place editing APIs, and deterministic DB-backed AI route generation.
- Implemented P0 frontend auth/session API integration, saved route list/detail/save/edit/delete API integration, AI generation API-first flow, and saved course edit verified-place search/add behavior.
- Implemented P1 backend content likes, place likes, and visit status APIs.
- Implemented P1 frontend content likes, place likes, and visit status API integration for logged-in users.
- Kept community posts/comments/scraps localStorage-backed in this pass.

### Backend changes
- Added DB-backed users with PBKDF2 password hashes and server-side bearer token hashes.
- Added `users`, `auth_tokens`, `travel_routes`, `route_places`, `content_likes`, `place_likes`, and `visit_status` tables.
- Added auth endpoints:
  - `POST /api/auth/signup`
  - `POST /api/auth/login`
  - `GET /api/users/me`
  - `PATCH /api/users/me`
- Added route endpoints:
  - `GET /api/users/me/routes`
  - `POST /api/routes`
  - `GET /api/routes/{routeId}`
  - `PATCH /api/routes/{routeId}`
  - `DELETE /api/routes/{routeId}`
  - `POST /api/routes/{routeId}/places`
  - `PATCH /api/routes/{routeId}/places/{routePlaceId}`
  - `DELETE /api/routes/{routeId}/places/{routePlaceId}`
  - `PATCH /api/routes/{routeId}/places/reorder`
- Added `POST /api/ai/routes`, which selects deterministic verified DB candidates from `contents`, `content_places`, and `places`.
- Added likes/visit endpoints:
  - `POST /api/contents/{contentId}/likes`
  - `DELETE /api/contents/{contentId}/likes`
  - `POST /api/places/{placeId}/likes`
  - `DELETE /api/places/{placeId}/likes`
  - `GET /api/users/me/likes`
  - `POST /api/places/{placeId}/visit-status`
  - `GET /api/users/me/visit-status`

### Frontend changes
- Added Axios bearer token injection through `frontend/src/api/http.js`.
- Added `authApi.js`, `routeApi.js`, `aiApi.js`, and `engagementApi.js`.
- Converted login/signup/profile/preferences to API-first user flows.
- Converted saved routes to backend source of truth for list/detail/save/edit/delete.
- Converted AI loading to call `/api/ai/routes` first, retaining local deterministic fallback only when the backend is unavailable.
- Added saved course edit verified DB place search, selected-day add, duplicate place prevention, remove/reorder, and backend save.
- Connected content likes, place likes, and visit status to backend APIs for logged-in users.

### QA fixes after review
- Removed destructive route/like/visit deletions from `data.sql`; seed inserts now use `INSERT IGNORE`, so server restart does not wipe user-created saved routes or engagement data.
- Added a unique seed key for `embedding_text` and kept only safe seed embedding cleanup.
- Backend route creation now always issues a server-side saved route ID instead of trusting the AI/generated client route ID.
- Backend route save/edit now requires non-empty `candidatePlaces` and rejects route stops outside that candidate list.
- Frontend route normalization no longer fabricates candidate places from stops when the backend omits `candidatePlaces`.
- AI result and course detail now merge DB candidate metadata into displayed route places so DB-only places keep coordinates/details for map fallback.

### Verification result
- `javac @target/javac.args` in `backend/`: passed.
- MyBatis XML parse for all mapper XML files: passed.
- `npm run lint` in `frontend/`: passed.
- `npm run build` in `frontend/`: sandbox run failed with Vite/esbuild `spawn EPERM`; approved external execution passed. Existing chunk-size warning remains.
- `git diff --check`: passed with line-ending warnings only.
- `mvn test`: not run because no `mvn` executable or Maven wrapper exists in `backend/`.

### Remaining limitations
- Real Spring Boot runtime against MySQL was not smoke-tested in this environment.
- Community posts/comments/scraps remain localStorage-backed.
- AI/RAG remains deterministic Spring DB selection with a TODO for future ranking/RAG integration.
- Route edit UI persists the whole edited route through `PATCH /api/routes/{routeId}`; route-place add/update/delete/reorder endpoints are implemented but not the primary UI save path yet.

## Prototype Rebuild Analysis - 2026-05-20

### Prototype findings
- Source inspected: `prototype/src/app/App.tsx`, `prototype/src/app/contexts/AppStateContext.tsx`, shared components under `prototype/src/app/components/`, primary screens under `prototype/src/app/components/screens/`, `prototype/src/styles/theme.css`, and local reference images under `prototype/src/imports/`.
- Prototype stack: React/TSX, React Router, motion, lucide-react, Tailwind/shadcn-style components. This stack is reference-only and will not be reused.
- Main flow: onboarding -> login/signup -> preference selection -> five-tab app (`AI course`, `Explore`, `Home`, `Community`, `My`).
- Main routes found: Home dashboard and all-list pages; Explore search/content/place/map/detail/review flows; AI content selection, condition input, loading, result, save/share, edit; Community feed/detail/write/search/boards; My Page activity/visit/course/profile/settings; notification/settings/notice screens.
- Shared UI patterns found: 52px top navigation, fixed 70px bottom tab bar, compact card rails, poster cards, place thumbnails, rounded search inputs, mode toggles, chips, bottom sheets, centered date modal, toast/share sheets, map placeholders, route timeline, sticky bottom CTAs.
- State behavior found: localStorage-backed app state for read notifications, liked content, liked places, planned/completed visits, saved courses, and edited AI course items.
- Visual behavior found: mobile-first app frame, high-radius cards, black primary CTA, subtle borders, warm/off-white surfaces, simple placeholder image gradients, and prototype dark-mode variables. The rebuilt app will default to a dark mobile shell as requested while preserving prototype structure and interaction rhythm.
- Data assets: no CSV files were found in the then-current project root; `prototype/src/imports/*.png` are mostly screen reference captures rather than reusable content imagery, so the app will use existing local public images and CSS placeholders.

### Target implementation
- Rebuild `frontend/` as Vue 3 + Vite + Vue Router + Pinia + Axios with Composition API and plain CSS.
- Create `backend/` as Spring Boot MVC + MyBatis + MySQL with controller/service/mapper/domain/dto layers.
- Keep `prototype/` unchanged.
- Implement required APIs for contents, places, integrated search, and content-related places with MyBatis XML mappers and MySQL schema/data seed files.

### Implementation result
- `frontend/` now uses Vue 3, Vite, Vue Router, Pinia, Axios, and plain CSS.
- Recreated prototype flows as Vue views: Splash, Login, Signup, Preferences, Home, Content list/detail, Place list/detail, Search, AI Course content selection, AI conditions, AI loading, AI result, Map, Community, My Page, Notice, and shell list screens.
- Recreated prototype UI patterns: fixed top nav, five-tab bottom nav, dark mobile app shell, poster cards, place cards, course cards, horizontal rails, chip filters, search bars, map fallback, route timeline, sticky CTAs, login-required bottom sheet, toast feedback.
- `backend/` was created as Spring Boot MVC + MyBatis + MySQL with controller/service/mapper/domain/dto layers, MyBatis XML mappers, `application.yml`, `schema.sql`, and `data.sql`.
- No CSV files were found in the then-current project root; temporary MySQL seed data was created from the expected schema.
- `prototype/` was not modified.

### Verification result
- `npm install`: sandbox failed due registry cache/network; approved external execution passed after aligning `@vitejs/plugin-vue` to Vite 7.
- `npm run lint`: passed.
- `npm run build`: sandbox failed with Vite/esbuild `spawn EPERM`; approved external execution passed.
- Route validation smoke check: passed for a valid candidate-only route and rejected an invented `placeId`.
- `git diff --check`: passed with CRLF conversion warnings only.
- Backend runtime check: blocked. Local environment has Java 8 and no Gradle executable; Spring Boot 3 requires JDK 17 and Gradle 8.x or a generated full wrapper.

### Remaining limitations
- Backend API compile/runtime was not verified locally because Gradle is not installed and Java is version 8.
- The lightweight `backend/gradlew` launcher delegates to a locally installed `gradle`; a full Gradle wrapper should be generated once Gradle is available.
- Browser click-through QA was blocked because the Vite dev server also hits sandbox `spawn EPERM`, and the attempted background external launch was not stable in this environment.

## Autonomous Cycle - 2026-05-13

### Cycle 0 inspection summary
- Current architecture: Vue 3 + Vite + JavaScript, plain CSS, no Vue Router, internal `activeView` state in `frontend/src/main.js`.
- Main screens: Home, Explore, Content Detail, Place Detail, AI Course, AI Result, Community, My Page.
- Data/logic: `mockScenikAdapter.js` provides mock route generation/login shell behavior; `routeValidation.js` enforces candidate-place-only route output; `mockScenikData.js` contains `placeId`-aware content/place/home/community/profile data.
- Current app state: prior lint/build/route smoke checks passed, with sandboxed build requiring approved external execution due esbuild `spawn EPERM`.
- Current blockers: no P0 blocker found during inspection; browser DOM tooling is not available in the current active tool list, so source/build/smoke checks are the available QA path.
- Known limitations: no real backend/auth/map SDK; Community/My Page are shell-level; `frontend/src/main.js` is still large at 887 lines and is the next maintainability risk.
- Commands available: `npm run lint`, `npm run build`, `git diff --check`; no configured test script.
- Files likely to change: `AGENTS.md`, `progress.md`, `frontend/src/main.js`, new `frontend/src/screens/*`, and possibly docs if architecture changes meaningfully.

### Task chosen
- Highest-value current task: normalize the new official `sceniK` casing across active UI/docs and begin a safe screen-level component extraction from `frontend/src/main.js`.

### Why this task was chosen
- The latest user request made exact `sceniK` brand casing a hard product rule, so stale legacy casing variants are now a P1 product-quality issue.
- With no P0 stability issue found, the next highest-value engineering task is reducing the large `main.js` maintenance risk without redesigning or touching AI route validation.

### Short implementation plan
- Update `AGENTS.md` only where current project operating rules were stale: source priority, navigation order, and `sceniK` casing.
- Mechanically normalize visible legacy product-name casing to `sceniK` in active docs and frontend source, without renaming file paths.
- Extract low-risk detail/shell screens first: Content Detail, Place Detail, Community, and My Page.
- Preserve Home, Explore, AI Course, AI Result behavior in this cycle and leave deeper extraction for the next cycle.
- Run lint, build, diff check, route validation smoke check, and source checks.

### Implementation result
- Updated `AGENTS.md` source priority to match the active project direction: existing app behavior first, then `wireframe/`, then `sceni-k-travel-planner/`.
- Updated `AGENTS.md` navigation rule to AI Course, Explore, Home, Community, My Page with Home as center tab.
- Added official `sceniK` casing rule and normalized active UI/docs references from legacy casing variants to `sceniK` without renaming existing file paths.
- Extracted `ContentDetailScreen`, `PlaceDetailScreen`, `CommunityScreen`, and `MyPageScreen` into `frontend/src/screens/`.
- Reduced `frontend/src/main.js` from 887 lines to 731 lines while preserving core state, AI Course generation, route validation, Home, Explore, AI Course, and AI Result inline behavior.
- Updated `docs/scenik-implementation-spec.md` component hierarchy to record extracted screens and the remaining inline screens.

### Verification result
- `npm run lint`: passed.
- `npm run build`: sandbox run failed with known esbuild `spawn EPERM`; approved external execution passed.
- `git diff --check`: passed with CRLF conversion warnings only.
- Route validation smoke check: passed with `Seoul K-Drama AI Course | stops=3`.
- Source check: bottom nav order remained `aiCourse,explore,home,community,myPage`; required Home/Explore/AI Course/AI Result strings and new screen component registrations were present.
- Dependency check: no forbidden UI dependency was added to `frontend/package.json`.

### Review result
- P0: none found.
- P1: major active P1 fixed for this cycle: official `sceniK` casing now applies to active UI/docs; stale AGENTS source/nav rules are corrected; first maintainability slice of `main.js` extraction is complete.
- P1 remaining: Home, Explore, AI Course, and AI Result still remain inline in `main.js`; this is now the next maintainability task rather than a blocking issue.
- P2: CRLF warnings remain from Git line-ending normalization.

### Next recommended task
- Continue the screen extraction safely by moving `ExploreScreen` next, then `HomeScreen`, then `AiCourseScreen`/`AiResultScreen` after another verification pass.

## Functional MVP Gap Analysis

### Current functional state - 2026-05-13
- Current architecture: Vue 3 + Vite + JavaScript, plain CSS, no Vue Router, single app state in `frontend/src/main.js`, extracted detail/community/My Page screen modules under `frontend/src/screens/`.
- Current working core: five-tab navigation, Home/Explore/AI Course/AI Result rendering, mock candidate-place route generation, `placeId`-aware route validation, MapPreview fallback, RouteTimeline rendering.
- Current main blocker: service features are mostly shell-level. Auth, local user persistence, route save, content/place likes, visit status, community posting/comments, and My Page real user data need localStorage-backed MVP services.
- Commands available: `npm run lint`, `npm run build`, `git diff --check`; no configured test script.
- Files likely to change: `frontend/src/main.js`, `frontend/src/screens/*`, `frontend/src/components/LoginRequiredPanel.js`, `frontend/src/services/*`, `frontend/src/styles.css`, `progress.md`, and possibly `docs/scenik-implementation-spec.md`.

| Feature | Current status | Needed work |
| --- | --- | --- |
| onboarding | Missing as a working state; only documented from wireframes. | Add first-run onboarding state with start/login/signup choices. |
| signup | Missing. | Add mock signup with email/password/nickname validation and duplicate email rejection. |
| login | Only login-required notice shell exists. | Add mock login form, credential validation, and current session storage. |
| logout | Missing. | Add logout action that clears current session and updates My Page. |
| preference selection | Missing. | Add post-signup preference chips for content types, regions, and travel themes. |
| auth state persistence | Missing. | Add localStorage session restore through an auth service. |
| content list/search/detail | Content list/detail render; search input is not functional. | Add content search/category filtering and detail like action. |
| place list/detail | Place list/detail render; no persisted user actions. | Add place search/filter, recent viewed tracking, like, planned, and completed actions. |
| content like | Shell-only login notice. | Persist liked content per logged-in user. |
| place like | Missing/shell-only. | Persist liked places per logged-in user. |
| planned visit | Shell-only. | Persist planned visit state per user and place. |
| completed visit | Shell-only. | Persist completed visit state per user and place, replacing planned state when needed. |
| AI route generation | Working through mock adapter and validation. | Preserve candidate-place-only route generation and expose save/share/post actions. |
| route validation | Working in `routeValidation.js`. | Preserve and add smoke checks for valid, invented place, missing `placeId`, and duplicate visit order cases. |
| route save | Shell-only login notice. | Persist generated routes per logged-in user and show them in Home/My Page. |
| route regenerate | Currently returns to AI Course instead of regenerating immediately. | Regenerate a valid route from the same verified candidate pool. |
| route share | Shell-only login notice. | Add share-ready/copy shell with safe Clipboard fallback. |
| community list | Static sample posts render. | Back static posts with localStorage and merge user-created posts. |
| community create | Shell-only login notice. | Create localStorage posts from current route or a basic composer action. |
| community like | Shell-only login notice. | Persist post likes per logged-in user. |
| comments | Shell-only login notice. | Add lightweight persisted comments if feasible. |
| My Page | Static fake personal data appears regardless of auth. | Show login prompt when logged out; show actual saved/liked/visit/post data when logged in. |
| notifications/settings | Header/settings shell only. | Add simple settings rows and mock notifications without backend. |

### Functional MVP task chosen
- Highest-value task: build localStorage-backed auth and user activity services, then wire auth-required actions, route save, likes, visits, community posting, and My Page real data into the existing Vue screens.

### Why this task was chosen
- It fixes the core MVP gap: the app already looks like a product and can generate validated mock routes, but user accounts and persisted actions do not work yet.
- It preserves the current design/wireframe/v0 work while turning shell buttons into meaningful MVP behavior.

### Functional MVP implementation result
- Added localStorage-ready service boundaries:
  - `frontend/src/services/storageService.js`
  - `frontend/src/services/authService.js`
  - `frontend/src/services/userService.js`
  - `frontend/src/services/contentService.js`
  - `frontend/src/services/placeService.js`
  - `frontend/src/services/routeService.js`
  - `frontend/src/services/communityService.js`
- Added `frontend/src/screens/AuthScreen.js` with onboarding, login, signup, and preference selection states.
- Wired app startup to restore the current session or show first-run onboarding.
- Added mock signup validation for required email/password/nickname and duplicate email rejection.
- Added mock login validation and logout behavior.
- Added preference persistence on the current user profile.
- Added auth-required action guard: logged-out save/like/visit/post/comment actions show login-required UI; logged-in actions persist locally.
- Added content like persistence and selected state on content detail.
- Added place like, planned visit, and completed visit persistence on place detail.
- Added recent viewed place persistence when opening place detail.
- Added saved AI route persistence from AI Result; saved routes now feed Home recent saved routes and My Page.
- Added route share shell with clipboard attempt and safe fallback notice.
- Changed Regenerate to run a new valid mock generation flow from the current selected content/conditions.
- Added Community localStorage posts, post creation, post-from-route creation, post likes, and comments.
- Changed My Page from fake static data to logged-out prompt plus real logged-in user data for saved routes, liked content, liked places, planned visits, completed visits, and written posts.
- Added Explore keyword search, content/map segmented mode, category chips, content filtering, place filtering, and empty states.
- Updated `AGENTS.md` and `docs/scenik-implementation-spec.md` with the functional MVP local persistence rule and service architecture.

### localStorage keys used
- `sceniK.users`
- `sceniK.session`
- `sceniK.onboardingSeen`
- `sceniK.guestState`
- `sceniK.userState.{userId}`
- `sceniK.savedRoutes.{userId}`
- `sceniK.communityPosts`

### Functional MVP verification result
- `npm run lint`: passed.
- Functional service smoke check: passed.
  - Signup, session restore, logout, login.
  - Content like and place like persistence.
  - Planned visit replaced by completed visit without duplicate state.
  - Valid mock AI route passed validation.
  - Invented `placeId`, missing `placeId`, and duplicate same-day `visitOrder` were rejected.
  - Saved route persisted.
  - Community post from route, post like, and comment persisted.
- `npm run build`: sandbox execution failed with known Vite/esbuild `spawn EPERM`.
- External build approval request: rejected by automatic approval review, so no same-outcome workaround was attempted.
- `git diff --check`: passed with CRLF conversion warnings only.
- Source checks: auth screen strings, functional action handlers, bottom nav order, service keys, and My Page wiring were found in source.

### Functional MVP review result
- P0: no implementation P0 found in lint/source/service smoke checks. Full Vite build remains unverified because the sandbox blocks esbuild spawn and escalation was rejected.
- P1 fixed: auth, session persistence, preferences, likes, visits, recent viewed places, saved routes, route share shell, route posting, Community create/like/comment, Explore search/filter, and logged-in/logged-out My Page behavior now work through localStorage services.
- P1 remaining: no browser DOM walkthrough was available in the active tool set; real backend/API integration and real map SDK remain out of scope.
- P2 remaining: comments UI and auth forms can use additional visual polish after functional QA in a browser.

### Next recommended task
- Run the app in a browser/dev-server environment, perform a click-through QA for signup -> preferences -> generate route -> save -> My Page -> Community post/comment, then fix any runtime/UI issues found during manual testing.

## Maintainability Refactor Cycle - 2026-05-13

### Task chosen
- Refactor the remaining large inline screen sections out of `frontend/src/main.js` without changing behavior, visual design, localStorage keys, AI route generation, or route validation.

### Why this task was chosen
- The functional MVP now works through localStorage services, but `main.js` still owned large Home, Explore, AI Course, and AI Result templates. Moving those templates into presentational screen modules reduces maintenance risk while preserving the existing state and service boundaries.

### Files extracted
- `frontend/src/screens/HomeScreen.js`
- `frontend/src/screens/ExploreScreen.js`
- `frontend/src/screens/AiCourseScreen.js`
- `frontend/src/screens/AiResultScreen.js`

### Implementation result
- `HomeScreen` now owns the Home dashboard template and emits `go-to`, `open-content`, `open-place`, and `start-course`.
- `ExploreScreen` now owns search/filter/content/map layout and emits query/mode/category updates plus detail navigation events.
- `AiCourseScreen` now owns content selection, condition controls, candidate places, loading/error UI, and generation CTA.
- `AiResultScreen` now owns validated route result, trust panel, map preview, timeline, save/share/regenerate/post actions, and empty state.
- `frontend/src/main.js` still owns app state, auth/session service calls, localStorage persistence, AI generation, route validation, and cross-screen navigation.
- `frontend/src/main.js` line count reduced from 1227 to 761 lines.
- Removed obsolete unused Home tab state from `main.js`.
- Updated `docs/scenik-implementation-spec.md` component hierarchy to reflect extracted screen modules.

### Verification result
- `npm run lint`: passed.
- `npm run build`: sandbox run failed with known Vite/esbuild `spawn EPERM`; approved external execution passed.
- Functional service/route smoke check: passed.
  - Signup, session restore, logout, login.
  - Content/place likes persisted.
  - Planned visit was replaced by completed visit.
  - Valid mock route passed validation.
  - Invented `placeId`, missing `placeId`, and duplicate same-day `visitOrder` were rejected.
  - Saved route persisted.
  - Community post from route, post like, and comment persisted.
- `git diff --check`: passed with CRLF conversion warnings only.
- Source checks: bottom nav order remains AI Course, Explore, Home, Community, My Page; no forbidden visible legacy product casing found outside file/path names.

### Review result
- P0: none found.
- P1: no behavior/design regression found through lint, build, source checks, and service smoke checks.
- P2: `main.js` still owns orchestration and many service/action functions; this is acceptable for now but can be split into composables later after browser QA.

### Next recommended task
- Run browser click-through QA for onboarding -> signup -> preferences -> AI Course -> save route -> My Page -> Community post/comment, then extract orchestration helpers into composables only if the click-through stays stable.

## Wireframe Implementation Plan

### Wireframe files inspected
- `wireframe/home.png`
  - Likely screen: Home.
  - Main sections: global top bar, 최근 본 장소, 추천 콘텐츠, 인기 여행 코스, 공지사항, bottom navigation.
  - Layout: compact mobile dashboard with horizontal card rails, small section headers, category chips, compact notice rows.
  - Target: `frontend/src/main.js` Home template, `frontend/src/styles.css`, existing `ContentCard`, `PlaceCard`, `RouteTeaserCard`.
- `wireframe/explore.png`
  - Likely screens: Explore content mode, Explore filter bottom sheet, Explore map mode.
  - Main sections: search bar, content/map segmented control, category chips, content grid, filter/sort panel, map preview with place chips, nearby filming place list.
  - Target: `frontend/src/main.js` Explore template, `MapPreview`/map fallback styling, search/filter/chip/card CSS.
- `wireframe/ai course 1.png`
  - Likely screens: AI Course content selection, travel condition input, AI loading, itinerary result.
  - Main sections: step progress, content search/chips/poster grid, condition controls, AI loading checklist, course result title/map/day tabs/timeline/actions.
  - Target: `frontend/src/main.js` AI Course and AI Result templates, `RouteTimeline`, `MapPreview`, `ContentCard`, `Button`, `styles.css`.
- `wireframe/ai course 2.png`
  - Likely screen: Save/share shell for generated route.
  - Main sections: route summary card, save action, share actions, completion CTA.
  - Target: existing login-required/save/share shell behavior in `frontend/src/main.js`; implement only lightweight visual treatment if needed without backend.
- `wireframe/onboarding.png`
  - Likely screens: onboarding, login, signup, preference selection.
  - Main sections: app intro, login methods/form, signup form/agreement, preference chips.
  - Target: current app has no standalone onboarding route; map to lightweight auth/preference shell styling only if compatible with existing login-required behavior, otherwise defer.

### Wireframe mapping

| Wireframe file | Target screen/component | What to implement |
| --- | --- | --- |
| `wireframe/home.png` | Home in `frontend/src/main.js`; `ContentCard`, `PlaceCard`, `RouteTeaserCard`; `styles.css` | Keep compact IA and rails, align spacing/header/card rhythm to wireframe, port v0 card visuals/images. |
| `wireframe/explore.png` | Explore in `frontend/src/main.js`; search/filter/chips/cards/map fallback | Add wireframe-like search, content/map mode styling, category chips, two-column content cards, map preview and nearby place list treatment. |
| `wireframe/ai course 1.png` | AI Course and AI Result in `frontend/src/main.js`; `MapPreview`; `RouteTimeline` | Align step progress, content grid, condition inputs/chips, loading state, result map/tabs/timeline/actions to wireframe while preserving route logic. |
| `wireframe/ai course 2.png` | Save/share shell actions in AI Result and login-required panel | Recreate save/share option-card visual treatment using existing auth-required shell behavior; no backend save/share. |
| `wireframe/onboarding.png` | Auth/preference shell, currently deferred from primary 5-tab app | Defer full onboarding/login/signup/preference screens because current app has no auth routing; reuse visual patterns for login-required shell only. |

## v0 Visual Porting Plan

### Exact v0 files inspected
- `sceni-k-travel-planner/package.json`
- `sceni-k-travel-planner/components.json`
- `sceni-k-travel-planner/app/page.tsx`
- `sceni-k-travel-planner/app/globals.css`
- `sceni-k-travel-planner/styles/globals.css`
- `sceni-k-travel-planner/components/scenik/top-app-bar.tsx`
- `sceni-k-travel-planner/components/scenik/bottom-navigation.tsx`
- `sceni-k-travel-planner/components/scenik/section-header.tsx`
- `sceni-k-travel-planner/components/scenik/search-bar.tsx`
- `sceni-k-travel-planner/components/scenik/chip.tsx`
- `sceni-k-travel-planner/components/scenik/content-card.tsx`
- `sceni-k-travel-planner/components/scenik/place-card.tsx`
- `sceni-k-travel-planner/components/scenik/route-card.tsx`
- `sceni-k-travel-planner/components/scenik/ai-components.tsx`
- `sceni-k-travel-planner/components/scenik/itinerary-timeline.tsx`
- `sceni-k-travel-planner/components/screens/home-screen.tsx`
- `sceni-k-travel-planner/components/screens/explore-screen.tsx`
- `sceni-k-travel-planner/components/screens/ai-course-screen.tsx`
- `sceni-k-travel-planner/components/screens/content-detail-screen.tsx`
- `sceni-k-travel-planner/components/screens/place-detail-screen.tsx`
- `sceni-k-travel-planner/components/screens/community-screen.tsx`
- `sceni-k-travel-planner/components/screens/my-page-screen.tsx`
- `sceni-k-travel-planner/components/screens/onboarding-screen.tsx`
- `sceni-k-travel-planner/components/screens/login-screen.tsx`
- `sceni-k-travel-planner/components/screens/signup-screen.tsx`
- `sceni-k-travel-planner/components/screens/preference-screen.tsx`

### v0 visual system found
- Colors: warm ivory `#FFF8ED`, background-secondary `#FAF7F0`, primary scenic teal `#0F766E`, coral `#FF6B4A`, sky mist `#DDF3F5`, sand `#F3E7D3`, ink `#172033`, muted slate `#64748B`, card white, soft beige border `#E5E0D8`.
- Spacing: mobile max-width app canvas, 16-24px screen padding, 8/12/16px internal gaps, fixed bottom action bars, horizontal rails with hidden scrollbars.
- Radius: rounded-xl/2xl/full, route/card radii around 16-24px, large CTA 16-18px radius.
- Shadows: low card shadows and stronger selected/raised nav shadows, not neon.
- Typography: 24px major screen titles, 18px section titles, 14-16px body, 10-12px nav/meta labels.
- Card treatment: image-first poster/content cards with gradient overlays, rounded image cards, horizontal place/route cards with thumbnails, verified/filming badges.
- Buttons/chips/badges: black/primary filled CTAs in wireframes, v0 primary filled buttons, rounded chips, selected state fill/ring, pale secondary surfaces.
- Navigation: sticky top app bar, route-line sceniK logo, notification/settings actions, fixed bottom navigation with raised active AI Course in v0; current order must remain AI Course, Explore, Home, Community, My Page.
- Hero/section styling: image-led onboarding/detail/result heroes, compact section headers with see-all chevrons.
- Map/timeline styling: map preview image/fallback with overlay CTA, route-line dots, numbered itinerary timeline with thumbnail place cards and transport rows.
- Image usage: cinematic Seoul street, K-drama scene, onboarding scenic Korea image reused across content, place, route, onboarding, map, profile, and community visuals.

### v0 assets/images found
- `sceni-k-travel-planner/public/images/kdrama-scene.jpg`
  - Useful for content posters, AI result hero, filming-location cards.
  - Copy target: `frontend/public/scenik-assets/kdrama-scene.jpg`.
- `sceni-k-travel-planner/public/images/seoul-street.jpg`
  - Useful for place cards, map preview, route cards, community posts.
  - Copy target: `frontend/public/scenik-assets/seoul-street.jpg`.
- `sceni-k-travel-planner/public/images/onboarding-hero.jpg`
  - Useful for onboarding/preference-inspired image cards, route/community/profile placeholders.
  - Copy target: `frontend/public/scenik-assets/onboarding-hero.jpg`.
- `sceni-k-travel-planner/public/icon.svg`, `icon-dark-32x32.png`, `icon-light-32x32.png`, `apple-icon.png`, `placeholder*.jpg/png/svg`
  - Use only if needed for logo/placeholders; current Vue logo can remain CSS/SVG.

### v0 components/layouts to port
- Top app bar: sticky ivory/white header, text logo, notification/settings buttons.
- Bottom navigation: fixed five-tab app nav with compact icon/label rhythm, preserving current order and center Home.
- Content cards: poster image ratio, gradient overlay, top type badge, selected check state.
- Place cards: horizontal thumbnail cards, verified marker, map pin metadata, filming/related content badge.
- Route cards: compact thumbnail route card, featured image route card treatment where wireframe allows, route metadata badges.
- AI Course: step/progress bars, content poster grid, rounded search/input, pill chips, condition cards, fixed CTA, AI loading route visual.
- AI Result: image/route-map hero, route stats, AI reason/trust panel, day tabs, numbered timeline cards, save/share option-card treatment.
- Explore: search bar, segmented content/map mode, category chips, content grid, map preview and nearby place list.
- Community: feed cards with user row, large image, route timeline preview, actions, tags.
- My Page: profile card, stats grid, saved route list, grouped settings rows.
- Buttons/chips/badges: translate Tailwind/shadcn visual into plain CSS classes and existing Vue components.

### v0 to Vue mapping

| v0 file/component | Current Vue target | What will be ported |
| --- | --- | --- |
| `components/scenik/top-app-bar.tsx` | `frontend/src/components/AppShell.js`, `frontend/src/styles.css` | Header density, text logo/route mark, notification/settings action styling. |
| `components/scenik/bottom-navigation.tsx` | `frontend/src/components/AppShell.js`, `frontend/src/styles.css` | Fixed mobile nav rhythm and active/raised treatment while preserving current order. |
| `components/scenik/section-header.tsx` | Section headers in `frontend/src/main.js`, `.section__header` CSS | Compact title + see-all hierarchy. |
| `components/scenik/search-bar.tsx` | Explore/AI Course search blocks | Rounded search input with icon and soft card background. |
| `components/scenik/chip.tsx` | `.chip`, `.compact-chip-row`, condition filters | Filled/outline pill chips and selected states. |
| `components/scenik/content-card.tsx` | `frontend/src/components/ContentCard.js`, CSS | Poster image card, overlay title/type, selected check. |
| `components/scenik/place-card.tsx` | `frontend/src/components/PlaceCard.js`, CSS | Horizontal/image card variants, verified and filming badges. |
| `components/scenik/route-card.tsx` | `frontend/src/components/RouteTeaserCard.js`, CSS | Thumbnail route card and image-led route treatment. |
| `components/scenik/ai-components.tsx` | AI Course/AI Result sections in `frontend/src/main.js`, CSS | Progress steps, AI loading route visual, reason box, stats bar. |
| `components/scenik/itinerary-timeline.tsx` | `frontend/src/components/RouteTimeline.js`, CSS | Numbered day timeline, thumbnail stop cards, transport connectors. |
| `components/screens/home-screen.tsx` | Home in `frontend/src/main.js` | Use v0 card/image treatment while following `wireframe/home.png` compact IA. |
| `components/screens/explore-screen.tsx` | Explore in `frontend/src/main.js` | Search/chips/content grid/map-mode card treatment. |
| `components/screens/ai-course-screen.tsx` | AI Course and result in `frontend/src/main.js` | Step layouts, loading/result visual styling without changing route logic. |
| `components/screens/content-detail-screen.tsx` | Content detail in `frontend/src/main.js` | Image hero, detail title card, cast/scenes/locations rails. |
| `components/screens/place-detail-screen.tsx` | Place detail in `frontend/src/main.js` | Image gallery hero, detail card, quick actions, related content, map card. |
| `components/screens/community-screen.tsx` | Community in `frontend/src/main.js` | Feed cards, post image, route preview, action row, FAB-like create affordance if scoped. |
| `components/screens/my-page-screen.tsx` | My Page in `frontend/src/main.js` | Profile/stat card, tab chips, saved route cards, settings rows. |
| `components/screens/onboarding-screen.tsx`, `login-screen.tsx`, `signup-screen.tsx`, `preference-screen.tsx` | Auth/login-required shell only | Full auth routing deferred; visual motifs may inform login-required/preference shell if present. |

## Wireframe Layout Plan

### Wireframe files inspected
- `wireframe/home.png`
  - Likely screen: Home.
  - Section order: top app bar, recent viewed places, recommended content, popular travel routes, notices, bottom navigation.
  - Major layout: compact mobile dashboard; two/three card horizontal rails; small category chips; compact notice rows; no hero/search.
  - CTA/navigation: top notification/settings actions, section see-all affordances, fixed five-tab bottom navigation.
  - Vue target: Home section in `frontend/src/main.js`; `ContentCard`, `PlaceCard`, `RouteTeaserCard`; shared rail/card CSS.
- `wireframe/explore.png`
  - Likely screen: Explore content mode, filter/sort sheet, and map mode.
  - Section order: top app bar/search, content-map segmented control, category chips, content grid or map panel, nearby place list, bottom navigation.
  - Major layout: two-column poster grid for content mode; modal-like filter/sort panel; large map fallback with place chips and nearby cards for map mode.
  - CTA/navigation: search icon, filter/sort button, map/content segmented buttons, list view link.
  - Vue target: Explore section in `frontend/src/main.js`; `MapPreview`; filter/chip/card CSS.
- `wireframe/ai course 1.png`
  - Likely screens: AI Course content selection, travel condition input, AI loading, generated itinerary result.
  - Section order: progress indicator, content search/category chips/content cards, condition inputs/chips, loading checklist, result title/map/day tabs/timeline/action buttons.
  - Major layout: step-based mobile flow with black primary CTA, selected poster card state, form cards, circular loading visual, timeline result cards.
  - CTA/navigation: next/generate CTA, save/share/edit result actions.
  - Vue target: AI Course and AI Result sections in `frontend/src/main.js`; `ContentCard`, `MapPreview`, `RouteTimeline`, `Button`, CSS.
- `wireframe/ai course 2.png`
  - Likely screen: save/share shell for generated course.
  - Section order: back title bar, route summary card, save option card, share option cards, done CTA.
  - Major layout: stacked large option cards with icon, label, helper copy, and bottom full-width CTA.
  - CTA/navigation: save to My Course, copy link, SNS share, community post, Done.
  - Vue target: existing AI Result save/share/regenerate/post actions and login-required shell; no backend save/share.
- `wireframe/onboarding.png`
  - Likely screens: onboarding, login, signup, preference selection.
  - Section order: logo/intro/CTA, SNS login/email login, signup fields/agreement, preference chips/complete CTA.
  - Major layout: separate auth/onboarding flow outside the five-tab app.
  - CTA/navigation: start, login, signup complete, preference complete/skip.
  - Vue target: deferred as full screens because the current app has no auth routing; visual motifs can inform login-required shell only.

### Wireframe mapping

| Wireframe file | Target screen/component | Large structure to implement |
| --- | --- | --- |
| `wireframe/home.png` | Home in `frontend/src/main.js`; `ContentCard`; `PlaceCard`; `RouteTeaserCard`; `styles.css` | Compact dashboard starting at recent activity, horizontal rails for recent/recommended/routes, compact notice rows, fixed nav. |
| `wireframe/explore.png` | Explore in `frontend/src/main.js`; `MapPreview`; card/filter/chip CSS | Search and discovery controls, content/map segmented structure, content grid, map preview, nearby place list. |
| `wireframe/ai course 1.png` | AI Course and AI Result in `frontend/src/main.js`; `RouteTimeline`; `MapPreview`; `Button`; `styles.css` | Step/progress flow, content selection, condition input, verified candidates, loading state, polished itinerary result with map/timeline/actions. |
| `wireframe/ai course 2.png` | AI Result action area and `LoginRequiredPanel` | Save/share/post action shell cards that trigger login-required UI instead of fake backend success. |
| `wireframe/onboarding.png` | Auth/onboarding shell deferred | No full route in current app; document deferral and keep main tab app intact. |

## v0 Visual Design Porting Plan

### Exact v0 files inspected
- `sceni-k-travel-planner/package.json`
- `sceni-k-travel-planner/components.json`
- `sceni-k-travel-planner/app/page.tsx`
- `sceni-k-travel-planner/app/layout.tsx`
- `sceni-k-travel-planner/app/globals.css`
- `sceni-k-travel-planner/styles/globals.css`
- `sceni-k-travel-planner/components/scenik/top-app-bar.tsx`
- `sceni-k-travel-planner/components/scenik/bottom-navigation.tsx`
- `sceni-k-travel-planner/components/scenik/section-header.tsx`
- `sceni-k-travel-planner/components/scenik/search-bar.tsx`
- `sceni-k-travel-planner/components/scenik/chip.tsx`
- `sceni-k-travel-planner/components/scenik/content-card.tsx`
- `sceni-k-travel-planner/components/scenik/place-card.tsx`
- `sceni-k-travel-planner/components/scenik/route-card.tsx`
- `sceni-k-travel-planner/components/scenik/ai-components.tsx`
- `sceni-k-travel-planner/components/scenik/itinerary-timeline.tsx`
- `sceni-k-travel-planner/components/screens/home-screen.tsx`
- `sceni-k-travel-planner/components/screens/explore-screen.tsx`
- `sceni-k-travel-planner/components/screens/ai-course-screen.tsx`
- `sceni-k-travel-planner/components/screens/content-detail-screen.tsx`
- `sceni-k-travel-planner/components/screens/place-detail-screen.tsx`
- `sceni-k-travel-planner/components/screens/community-screen.tsx`
- `sceni-k-travel-planner/components/screens/my-page-screen.tsx`
- `sceni-k-travel-planner/components/screens/onboarding-screen.tsx`
- `sceni-k-travel-planner/components/screens/login-screen.tsx`
- `sceni-k-travel-planner/components/screens/signup-screen.tsx`
- `sceni-k-travel-planner/components/screens/preference-screen.tsx`
- `sceni-k-travel-planner/components/ui/button.tsx`
- `sceni-k-travel-planner/components/ui/badge.tsx`
- `sceni-k-travel-planner/components/ui/card.tsx`
- `sceni-k-travel-planner/components/ui/tabs.tsx`
- `sceni-k-travel-planner/lib/utils.ts`
- `sceni-k-travel-planner/next.config.mjs`
- `sceni-k-travel-planner/postcss.config.mjs`
- `sceni-k-travel-planner/tsconfig.json`

### v0 assets/images inspected
- `sceni-k-travel-planner/public/images/kdrama-scene.jpg`
- `sceni-k-travel-planner/public/images/seoul-street.jpg`
- `sceni-k-travel-planner/public/images/onboarding-hero.jpg`
- `sceni-k-travel-planner/public/icon.svg`
- `sceni-k-travel-planner/public/icon-dark-32x32.png`
- `sceni-k-travel-planner/public/icon-light-32x32.png`
- `sceni-k-travel-planner/public/apple-icon.png`
- `sceni-k-travel-planner/public/placeholder.jpg`
- `sceni-k-travel-planner/public/placeholder.svg`
- `sceni-k-travel-planner/public/placeholder-logo.png`
- `sceni-k-travel-planner/public/placeholder-logo.svg`
- `sceni-k-travel-planner/public/placeholder-user.jpg`

### v0 visual system found
- Colors/backgrounds: shadcn neutral base plus sceniK-specific warm ivory, card white, sand, sky-mist, scenic teal primary, sunset coral accent, ink navy text, muted slate metadata.
- Typography: compact mobile hierarchy with bold 24-32px screen titles, 18-20px section titles, 15-16px body, 12-13px labels, 10px nav captions.
- Spacing: mobile max-width canvas, 16px page gutters, 8/12/16/24px stack rhythm, horizontal rails, sticky bottom action/nav spacing.
- Radius/shadows: rounded-xl/2xl/full cards and controls, 16-24px card radius, low card shadow, stronger selected/raised nav/action shadow.
- Cards: poster image cards with gradient overlays and type badges; horizontal place cards with thumbnail and verified/filming metadata; route cards with image, dots, stats, tags.
- Buttons/chips/badges: dark filled primary CTA, outlined/secondary option cards, filled selected chips, pale secondary chips, verified and personalized badges.
- Navigation/app header: sticky app bar with sceniK route-line logo, notification/settings icon buttons, fixed bottom nav with active indicator and elevated active treatment.
- Map style: map-like soft surface with route/pin overlays and nearby place bubbles, designed fallback rather than broken-map UI.
- Route/timeline style: day header, vertical route line, numbered visit dots, thumbnail stop cards, time/duration, related content badge, transport rows.
- Community/profile: image-led feed cards, user/route metadata, action row, tags; My Page profile card, stat dashboard, tab chips, saved route/settings rows.

### v0 to Vue mapping

| v0 file/component | Current Vue target | What will be ported |
| --- | --- | --- |
| `components/scenik/top-app-bar.tsx` | `frontend/src/components/AppShell.js`; `frontend/src/styles.css` | Sticky warm app bar, route-line logo, notification/settings icon actions. |
| `components/scenik/bottom-navigation.tsx` | `frontend/src/components/AppShell.js`; `frontend/src/styles.css` | Fixed bottom nav rhythm, active indicator, raised active treatment while preserving current tab order. |
| `components/scenik/section-header.tsx` | Section headers in `frontend/src/main.js`; `.section__header` | Compact section title/eyebrow/see-all hierarchy. |
| `components/scenik/search-bar.tsx` | Explore and AI Course search/form surfaces | Rounded search input style with icon and soft card background. |
| `components/scenik/chip.tsx` | `.chip`, `.compact-chip-row`, condition filters | Filled/outline chips, selected state, touch-target spacing. |
| `components/scenik/content-card.tsx` | `frontend/src/components/ContentCard.js`; CSS | Poster card ratio, image overlay, type badge, selected check. |
| `components/scenik/place-card.tsx` | `frontend/src/components/PlaceCard.js`; CSS | Thumbnail place cards, verified badge, related content/map metadata. |
| `components/scenik/route-card.tsx` | `frontend/src/components/RouteTeaserCard.js`; route/community CSS | Image-led route card, route-dot motif, stats/tags/verified treatment. |
| `components/scenik/ai-components.tsx` | AI Course/AI Result in `frontend/src/main.js`; CSS | Progress steps, loading route visual, AI reason panel, route stats/trust banner. |
| `components/scenik/itinerary-timeline.tsx` | `frontend/src/components/RouteTimeline.js`; CSS | Day timeline, vertical route line, numbered stop cards, thumbnails, tips/transport rows. |
| `components/screens/home-screen.tsx` | Home in `frontend/src/main.js` | v0 card/image styling under `wireframe/home.png` compact structure. |
| `components/screens/explore-screen.tsx` | Explore in `frontend/src/main.js` | Search/filter header, segmented discovery, chips, content/place/map visual rhythm. |
| `components/screens/ai-course-screen.tsx` | AI Course and AI Result in `frontend/src/main.js` | Step screens, loading/result styling, action hierarchy without changing generation logic. |
| `components/screens/content-detail-screen.tsx` | Content detail in `frontend/src/main.js` | Media hero, content metadata, related places, create AI Course CTA. |
| `components/screens/place-detail-screen.tsx` | Place detail in `frontend/src/main.js`; `MapPreview` | Image hero, verified place metadata, map preview, auth-required action shells. |
| `components/screens/community-screen.tsx` | Community section in `frontend/src/main.js` | Shared route feed cards, image treatment, user row, tags, like/comment/save/share action row. |
| `components/screens/my-page-screen.tsx` | My Page section in `frontend/src/main.js` | Profile/stat dashboard, saved routes, liked content/places, visit state, settings rows. |
| `components/screens/onboarding-screen.tsx`, `login-screen.tsx`, `signup-screen.tsx`, `preference-screen.tsx` | Deferred auth/onboarding shell | No current auth routing; use visual motifs only for login-required shell unless a future route is added. |

## Wireframe/v0 Porting Implementation Result - 2026-05-13

### Task restatement
- Implement visible sceniK frontend screens from `wireframe/` while porting the visual design from `sceni-k-travel-planner/`.
- Preserve Vue + JavaScript + plain CSS, the current five-tab order `AI Course`, `Explore`, `Home`, `Community`, `My Page`, the AI Course flow, mock adapters, route validation, `placeId`, verified-place constraints, MapPreview/RouteTimeline behavior, and login-required shell behavior.

### Implementation result
- App shell: ported a v0-style sticky warm app bar, route-line sceniK logo, notification/settings icon actions, fixed bottom navigation treatment, and retained the required current tab order with Home centered.
- Home: kept the compact wireframe IA and applied v0 poster, place, route-card, chip, badge, rail, and image overlay styling without reintroducing the Home hero/search/top CTA.
- Explore: added a wireframe-like content/map segmented control, category chips, designed MapPreview block, and nearby place list while preserving existing content/place navigation.
- AI Course: restyled the step header, progress pills, content cards, condition chips, condition cards, candidate list, sticky CTA, loading state, and validation error states using the v0 warm teal/coral system.
- AI Result: restyled the result hero, trust stats, reason panel, map preview, route timeline, and next-action card area while leaving route generation and validation logic unchanged.
- Community: retained shell-level behavior and added v0-style shared-route feed cards with user rows, image treatment, tags, like/comment/save/share action buttons, and login-required shell actions.
- My Page: retained shell-level behavior and added v0-style profile card, stat dashboard, saved AI route rail, liked place rail, liked content rail, planned/completed visit cards, written-posts row, and account settings row.
- v0 assets: reused existing local frontend copies of `kdrama-scene.jpg`, `seoul-street.jpg`, and `onboarding-hero.jpg`; no new asset copy was required.
- Documentation: updated `docs/scenik-implementation-spec.md` with the current wireframe/v0 pass scope and acceptance criteria.

### Verification result
- `npm run lint`: passed.
- `npm run build`: sandboxed run failed with esbuild `Error: spawn EPERM`; approved external execution passed.
- `git diff --check`: passed; Git reported CRLF conversion warnings only.
- Route validation probe: passed with `Seoul K-Drama AI Course | stops=3`.
- Source checks confirmed required Home labels, Explore mode text, AI Course flow text, AI result text, bottom navigation order, v0/wireframe plan entries, local images, and no forbidden frontend dependencies in `frontend/package.json`.

### Review result
- P0: none found. Build passes outside the sandbox, navigation order is preserved, AI Course generation/validation still passes, and no forbidden dependency was added.
- P1: none remaining for the core request. Wireframes and v0 were inspected and documented with actual paths; implemented screens now visibly use v0-style app bar, cards, chips, badges, image overlays, route cards, map preview, and timeline patterns.
- P2: onboarding/login/signup/preference wireframe remains deferred because the current Vue app has no full auth routing; the existing login-required shell remains the MVP auth surface.

### Known limitations
- Onboarding/auth/preference screens are documented but not implemented as full routes.
- Community and My Page remain shell-level views without backend CRUD/auth.
- MapPreview remains a designed fallback, not a real map SDK.
- Home recent activity, recommendations, routes, and notices remain local mock data.

## Completed
- 2026-05-12: Fine-tuned Home visual saturation and 390px spacing.
  - Task restatement: polish only the current Home screen color saturation, card/rail visual weight, active Home nav state, chips, badges, route accents, background softness, and 390px spacing.
  - Implementation result:
    - Softened sky-blue background opacity and reduced page-level blue wash.
    - Lowered active Home bottom tab saturation and shadow strength while keeping it visually selected.
    - Softened selected chip, badge, card border, card shadow, route-map grid, route-line, and selected content states.
    - Reduced Home 390px section/subsection gaps and bottom padding slightly.
  - Preserved:
    - Home IA, removed intro state, bottom navigation order, AI Course logic, mock adapters, route validation, `placeId`, and verified-place constraints.
- 2026-05-12: Refined the sceniK Home top area and sky-blue palette.
  - Task restatement: remove the remaining Home intro/greeting/CTA block and soften the current cyan-heavy visual palette without changing Home IA, bottom navigation order, AI Course logic, mock adapters, route validation, or verified-place constraints.
  - Implementation result:
    - Removed the Home-only `home-compact-header` block so Home content starts directly with the recent activity section.
    - Updated Home `aria-labelledby` to use the first visible Home section title.
    - Shifted global sky-blue tokens toward Clear Sky Blue, Soft Sky Blue, Deep Travel Blue, Cloud White, Powder Blue, Soft Ice Blue, white/blue-tinted card surfaces, softer blue borders, Ink Navy, and muted slate text.
    - Softened active bottom nav, chips, badges, cards, shadows, and route/map blue accents so cyan is no longer dominant.
  - Preserved:
    - Bottom navigation order: AI Course, Explore, Home, Community, My Page.
    - Home information architecture and horizontal rail structure.
    - AI Course flow, mock adapters, route validation, `placeId`, verified-place constraints, and login-required behavior.
- 2026-05-12: Started sceniK full visual redesign plan.
  - Task restatement: perform a design overhaul only, preserving the existing Vue app structure, 5-tab navigation, AI Course flow, mock adapters, route validation, verified-place/placeId constraints, login-required behavior, and map fallback.
  - Current visual weaknesses:
    - The MVP still leans on plain white cards and generic app surfaces.
    - Scenic/cinematic sceniK identity is present in copy and data, but not strong enough visually.
    - AI Course and AI result need stronger route-line, map, film-frame, and verified-trust motifs.
    - Map fallback works but should look intentionally designed rather than like a simple placeholder.
    - Community and My Page shells need more travel-app polish while staying shell-level.
  - Design system changes planned:
    - Strengthen teal/coral/ivory/sand tokens with deeper shadows, layered scenic backgrounds, route-line patterns, film-frame borders, and map contour/grid motifs.
    - Make active/selected states more branded and tactile.
    - Improve mobile spacing, card depth, typography hierarchy, focus states, loading, empty, error, and login-required visuals.
  - Components to restyle:
    - `AppShell`, `Button`, `ContentCard`, `PlaceCard`, `RouteTeaserCard`, `VerifiedBadge`, `MapPreview`, `RouteTimeline`, `LoginRequiredPanel`, chips, action cards, feed cards, summary cards.
  - Screens to restyle:
    - Home, Explore, Content Detail, Place Detail, AI Course, AI Result, Community, and My Page.
  - Risks:
    - Avoid changing route generation behavior or validation logic while editing presentation.
    - Avoid remote-image dependency by making CSS placeholders and overlays strong enough even if images fail.
    - Avoid over-componentizing during a visual-only pass.
  - Implementation result:
    - Rebuilt the global CSS visual system around scenic teal, deep teal, sunset coral, warm ivory, soft sand, sky mist, ink navy, and muted brown.
    - Added scenic layered app background, route-line patterns, film-frame borders, map contour/grid motifs, richer card depth, stronger shadows, and branded active/selected states.
    - Restyled AppShell, bottom navigation, buttons, chips, verified badges, content/place cards, Home hero, Today's sceniK route panel, AI Course steps, AI result, map fallback, route timeline, community cards, My Page summaries, loading/error/login-required states.
    - Preserved existing Vue state flow, 5 tabs, mock adapters, route validation, candidate-only generation, `placeId` data, verified badges, login-required behavior, and map fallback behavior.
  - Review result:
    - P0: none found.
    - P1: none remaining after redesign; the app is visually distinct from the prior MVP, AI Course remains the hero flow, result timeline and map fallback are visibly branded, and selected states are clearer.
    - P2: map fallback still uses fixed visual pin positions rather than real coordinate projection; larger screen/component extraction remains a later cleanup.
  - Commands run:
    - `npm run lint`
    - `npm run build`
    - `git diff --check`
    - Node mock route validation probe
    - `npm run dev -- --port 4180 --strictPort` with Chrome headless mobile DOM verification
  - Verification result:
    - `npm run lint` passed.
    - `npm run build` passed outside the sandbox.
    - `git diff --check` passed with CRLF warnings only.
    - Mock route generation/validation probe passed.
    - Mobile DOM check passed with `DEV_STATUS=200` and required Home/AI Course/navigation text present.
  - Next recommended task:
    - Split the large screen template in `frontend/src/main.js` into screen-level Vue JS components without changing behavior.
- 2026-05-12: Integrated Stitch sceniK direction into a production-minded Vue MVP.
  - Task restatement: adapt `stitch_scenik_ai_travel_planner/` into the existing repository without copying generated static HTML, while producing a usable mobile-first sceniK MVP with AI Course, verified-place mock data, route validation, and key shell screens.
  - Assumptions:
    - sceniK is now the product name for this pass.
    - The sceniK app UI is English-first for international K-content travelers, while Codex final reports remain Korean.
    - Backend APIs, auth, and map SDK keys are unavailable, so mock adapters and login-required shell states are used.
    - Vue Router is not added; internal Vue state simulates the MVP screens to stay within the current learned stack and dependency set.
  - Discovery result:
    - Stitch is static HTML with Tailwind CDN and Material Symbols, so it was used only as design/component inspiration.
    - Existing frontend is Vue 3 + Vite, JavaScript ESM components, plain CSS, npm, no router, no API/auth layer.
    - Required P0 slice is app shell/navigation, AI Course flow, AI result, `placeId`-aware mock adapters, route validation, Explore/content/place shells, map fallback, save/share shell actions.
  - Implementation:
    - Updated `AGENTS.md` with sceniK rules, domain invariants, staged workflow, testing policy, review severities, and final response requirements.
    - Added `docs/scenik-implementation-spec.md`.
    - Rebranded the app shell to sceniK and changed the 5 tabs to `Home`, `Explore`, `AI Course`, `Community`, and `My Page`.
    - Implemented Home, Explore, Content Detail, Place Detail, AI Course, AI Result, Community, and My Page MVP screens through internal view state.
    - Added verified-place mock data with `placeId`, coordinates, related content, scene descriptions, and verified flags.
    - Added mock AI route generation and strengthened route validation for candidate membership, verified candidates, region match, day count, day order, visit order, duplicate stops, and required fields.
    - Added map fallback, route timeline, verified badges, login-required panel, selected-state accessibility, and AI result reason/condition summary.
  - Stitch reused:
    - 5-tab mobile nav pattern, poster cards, condition selectors, loading state concept, verified badges, map pins, route timeline, AI reason/result hierarchy, and warm scenic color tokens.
  - Stitch not reused:
    - Tailwind CDN, Material Symbols, static HTML, `href="#"` handlers, remote Stitch image dependencies, unvalidated route data, fake success actions, and zoom-disabling viewport patterns.
  - Files changed:
    - `AGENTS.md`
    - `docs/scenik-implementation-spec.md`
    - `frontend/src/main.js`
    - `frontend/src/styles.css`
    - `frontend/src/components/AppShell.js`
    - `frontend/src/components/Button.js`
    - `frontend/src/components/ContentCard.js`
    - `frontend/src/components/PlaceCard.js`
    - `frontend/src/components/RouteTeaserCard.js`
    - `frontend/src/components/VerifiedBadge.js`
    - `frontend/src/components/MapPreview.js`
    - `frontend/src/components/RouteTimeline.js`
    - `frontend/src/components/LoginRequiredPanel.js`
    - `frontend/src/data/mockHomeData.js`
    - `frontend/src/data/mockScenikData.js`
    - `frontend/src/adapters/mockScenikAdapter.js`
    - `frontend/src/adapters/routeValidation.js`
    - `progress.md`
  - Commands run:
    - `rg --files`
    - `Get-Content DESIGN.md -TotalCount 220`
    - `Get-Content frontend\package.json`
    - `Get-Content frontend\src\main.js`
    - `npm run lint`
    - `npm run build`
    - `git diff --check`
    - Node probes for valid route generation, cross-region validation failure, null route validation, duplicate placeId, invalid visitOrder, and unverified candidate failure.
    - `npm run dev -- --port 4175 --strictPort` with headless Chrome 390px DOM verification.
  - Verification result:
    - `npm run lint` passed.
    - `npm run build` passed when rerun outside the sandbox after sandbox `esbuild spawn EPERM`.
    - Route validation probes passed after P1 fixes.
    - Headless Chrome mobile DOM check passed with `DEV_STATUS=200` and required sceniK navigation/home text present.
    - `git diff --check` passed with only CRLF warnings.
  - Review summary:
    - Product/UX, code quality, and domain/security/RAG review agents found no P0 issues.
    - P1 issues found and fixed: cross-region route fallback, weak candidate/verified validation, malformed route crash, duplicate day/order/place validation, unknown ID fallback, stale AI generation timer, active tab mapping, selected-state accessibility, overly internal candidate copy, and thin AI result reason context.
    - P2 remaining: screen components are still concentrated in `main.js`, map fallback uses fixed visual pin positions, and some secondary shells are intentionally shallow.
  - Known mock-data assumptions:
    - Auth is unavailable; save/share/like/visit/post actions show login-required UI instead of pretending success.
    - Map SDK is unavailable; map preview is a CSS fallback while coordinates are preserved in mock data.
    - AI route generation uses `MockAiRouteGenerator`-style mock adapter only.
  - Next recommended task:
    - Split large `main.js` screen sections into separate Vue JS components, then connect the AI Course adapter to real Spring/MyBatis APIs when backend endpoints are available.
- 2026-05-11: Home screen redesigned as the English-first SceneTrail mobile app gateway.
  - Task restatement: redesign only the Home screen so it feels like a real mobile app gateway for foreign K-content fans, using Vue, JavaScript, HTML, and plain CSS only.
  - Assumptions:
    - SceneTrail is now the service name.
    - The Home screen is English-first for foreign users, overriding the earlier Korean-only UI rule for this screen.
    - The four gateway actions are placeholders only because no new pages or routing should be implemented in this task.
    - Existing remote image URL mock data can continue to be used for the image-led MVP Home screen.
  - Uncertainty:
    - Exact destination URLs for AI route generation, content discovery, map/place, and community screens are not implemented yet, so each gateway card records the destination as visible placeholder text.
  - Success criteria:
    - Mobile-first 360px to 430px structure.
    - No horizontal-overflow-oriented layout decisions in the Home source.
    - Touch targets remain at least 44px.
    - Home acts as a gateway to Create AI Route, Explore K-Content, Find Scene Spots, and Shared Trips.
    - Bottom navigation uses Home, Discover, Plan, Map, and Community, with Plan emphasized.
    - AI Route Preview communicates Choose -> Discover -> Route and shows an ordered sample route.
  - Home strategy change:
    - Replaced the long landing-page sequence with an app-gateway hierarchy: top app bar, search, compact image-led hero, 2x2 action grid, AI route preview, content/place rails, shared fan route feed, and mobile bottom tabs.
    - Updated `DESIGN_BRIEF.md` to record the English-first foreign-user direction.
  - 4 main action cards:
    - `Create AI Route`: strongest card, with route/timeline visual treatment and AI route generation screen placeholder.
    - `Explore K-Content`: poster/content visual treatment and content discovery screen placeholder.
    - `Find Scene Spots`: map/place visual treatment and place/map screen placeholder.
    - `Shared Trips`: community/feed visual treatment and community screen placeholder.
  - Bottom navigation structure:
    - `Home`
    - `Discover`
    - `Plan` emphasized as the core AI route feature
    - `Map`
    - `Community`
  - Hero changes:
    - Added SceneTrail top app bar with language and profile placeholders.
    - Added Home search with `Search K-dramas, stars, places...`.
    - Rebuilt hero as a compact mobile-first image-led module using the required headline, copy, and CTAs.
  - AI Route Preview changes:
    - Reworked preview into a strong route/timeline card.
    - Added `Choose -> Discover -> Route` flow.
    - Added the sample route `Bukchon Hanok Village -> Seongsu Cafe Street -> Han River Sunset`.
    - Added explicit `AI decides visit order` messaging.
  - Files changed:
    - `DESIGN_BRIEF.md`
    - `frontend/src/main.js`
    - `frontend/src/styles.css`
    - `frontend/src/components/AppShell.js`
    - `frontend/src/components/ContentCard.js`
    - `frontend/src/components/PlaceCard.js`
    - `frontend/src/components/RouteTeaserCard.js`
    - `frontend/src/data/mockHomeData.js`
    - `progress.md`
  - Commands run:
    - `Get-Content -Raw PRD.md`
    - `Get-Content -Raw AGENTS.md`
    - `Get-Content -Raw LEARNED_TECH_STACK.md`
    - `Get-Content -Raw DESIGN_BRIEF.md`
    - `Get-Content -Raw progress.md`
    - `Get-Content -Raw frontend\package.json`
    - `rg --files frontend\src`
    - `Get-Content -Raw -Encoding UTF8 frontend\src\**`
    - `git status -sb`
    - `git branch --show-current`
    - `git remote -v`
    - `git switch -c style/home-mobile-navigation-redesign`
    - `Test-Path frontend\node_modules`
    - `git diff --check`
    - `$env:Path='C:\Program Files\nodejs;' + $env:Path; & 'C:\Program Files\nodejs\npm.cmd' run build`
    - `$env:Path='C:\Program Files\nodejs;' + $env:Path; & 'C:\Program Files\nodejs\npm.cmd' run lint`
    - `npm run dev` through PowerShell job, followed by `Invoke-WebRequest http://127.0.0.1:5173/`
    - Headless Edge mobile checks at 390px and 430px with DOM text verification and screenshot output.
  - Result:
    - `npm install` was not needed because `frontend/node_modules` already existed.
    - `git diff --check` passed.
    - `npm run build` passed.
    - `npm run lint` passed.
    - `npm run dev` exists and returned HTTP 200 when run outside the sandbox.
    - Mobile DOM verification passed for the required hero, search, action cards, route preview, and bottom navigation text.
  - Notes:
    - Initial `npm run dev` checks failed in the sandbox because of Windows `Path/PATH` duplication and esbuild `spawn EPERM`; the dev server verification succeeded after approved outside-sandbox execution.
    - Headless Edge emitted non-blocking Chromium task-manager warnings during screenshot capture.
    - The action cards are intentionally placeholders and do not navigate to new pages yet.
  - Next recommended task:
    - Implement the content discovery screen behind `Explore K-Content`, then connect the Home action placeholders once the first destination screen exists.
- 2026-05-07: Home screen redesigned for a mobile-first experience.
  - Task restatement: redesign only the Home screen for 360px to 430px mobile screens first, keeping Vue, HTML, CSS, JavaScript, mock data, and the existing MVP scope.
  - Mobile-first redesign goal:
    - Make the Home screen feel like a modern mobile K-content travel app rather than a desktop-first student project.
    - Keep the primary CTA thumb-friendly and visible inside the first hero experience.
    - Preserve a vertical single-column mobile flow: header, hero, category shortcuts, popular content, AI route teaser, recommended places, community preview, bottom navigation.
  - Why the previous design was still not mobile-friendly enough:
    - The image collage improved the visual tone but the mobile first screen still felt like a resized desktop section.
    - The CTA and route preview competed with the fixed bottom navigation.
    - The bottom navigation duplicated labels and felt less like a real mobile app.
    - Content sections needed horizontal discovery rails and stronger mobile rhythm.
  - Major UI changes applied:
    - Rebuilt the hero as a single image-led mobile card with dark overlay, compact headline, supporting text, and two large CTAs.
    - Moved the AI route preview into a compact mobile strip below the hero, with three connected stop cards.
    - Converted content and place sections into horizontal mobile rails with snap-friendly card sizing.
    - Simplified the mobile bottom navigation to remove duplicated labels.
    - Reworked typography sizes, section spacing, button heights, card widths, and rail padding for 360px to 430px screens.
    - Kept desktop as a wider adaptation of the mobile hierarchy instead of a separate dashboard-like layout.
    - Updated mock copy in `mockHomeData.js` while preserving separated mock data and imageUrl-based content.
  - Files changed:
    - `frontend/src/main.js`
    - `frontend/src/styles.css`
    - `frontend/src/components/AppShell.js`
    - `frontend/src/data/mockHomeData.js`
    - `progress.md`
  - Commands run:
    - `git status -sb`
    - `git switch -c style/mobile-first-home-redesign`
    - `Get-ChildItem -Path frontend\src -Recurse -File`
    - Node-based reads for `PRD.md`, `AGENTS.md`, `LEARNED_TECH_STACK.md`, `DESIGN_BRIEF.md`, `progress.md`, `frontend/package.json`, `frontend/index.html`, and frontend source files
    - `git diff --check`
    - `$env:Path='C:\Program Files\nodejs;' + $env:Path; & 'C:\Program Files\nodejs\npm.cmd' run build`
    - `$env:Path='C:\Program Files\nodejs;' + $env:Path; & 'C:\Program Files\nodejs\npm.cmd' run lint`
    - `npm run dev` through `Start-Process`, followed by Headless Edge 390px screenshot and DOM render verification
  - Result:
    - `git diff --check` passed.
    - `npm run build` passed.
    - `npm run lint` passed.
    - `npm run dev` started successfully during Headless Edge verification.
    - Mobile-width render check confirmed the headline, `AI 여행 동선 만들기` CTA, route copy, and image-driven content were present in the rendered DOM.
    - The dev server was stopped after verification.
  - Next recommended task:
    - Review the mobile Home screen manually on a real browser/device width, then implement the content list screen with the same mobile rail/card system.
- 2026-05-07: Home screen fully redesigned with image-driven travel UI.
  - Task restatement: replace the scaffold-like Home visual design with a modern consumer-facing K-content travel app experience without adding backend features, new pages, dependencies, React, TypeScript, Tailwind CSS, or external UI libraries.
  - Why the previous design was insufficient:
    - The Home screen was structurally correct but still felt like a low-fidelity student wireframe.
    - Content and place cards used visual blocks and gradients instead of real travel imagery.
    - The hero was large but not cinematic enough to communicate K-content travel.
    - The AI route and community sections were functional but did not feel like real consumer product surfaces.
  - Redesign direction:
    - Modern Korean travel app.
    - K-drama filming location discovery.
    - Emotional travel magazine.
    - AI itinerary planner with a clear visit-order promise.
    - Mobile-first consumer service instead of dashboard or school-assignment styling.
  - Visual redesign completed:
    - Added remote image URLs to mock data for hero collage, content cards, place cards, route teaser, and community cards.
    - Rebuilt the hero into an image-first mobile collage with headline, subcopy, primary CTA, and secondary CTA.
    - Updated the hero headline to `좋아하는 장면을 따라, 나만의 여행 코스로`.
    - Added a category shortcut strip after the hero.
    - Rebuilt content cards as clickable image cards with overlays, badges, region, likes, saves, and richer Korean descriptions.
    - Rebuilt the AI route teaser as a strong route preview with image, CSS-only map detail, connected route dots, and timeline.
    - Rebuilt place cards with actual images, status badges, map chips, and travel metadata.
    - Rebuilt community preview as image-backed shared course cards with tags, writer metadata, save/like badges, and route summaries.
    - Reworked spacing, typography hierarchy, contrast, responsive desktop layout, shadows, and mobile visual density using plain CSS only.
  - Files changed:
    - `frontend/src/main.js`
    - `frontend/src/styles.css`
    - `frontend/src/components/ContentCard.js`
    - `frontend/src/components/PlaceCard.js`
    - `frontend/src/components/RouteTeaserCard.js`
    - `frontend/src/data/mockHomeData.js`
    - `progress.md`
  - Commands run:
    - `git status -sb`
    - `git branch --show-current`
    - `git remote -v`
    - `git switch -c style/home-full-redesign`
    - `Get-ChildItem -Path frontend\src -Recurse -File`
    - `Get-Content -Path frontend\scripts\check-frontend.js`
    - `git diff --check`
    - `$env:Path='C:\Program Files\nodejs;' + $env:Path; & 'C:\Program Files\nodejs\npm.cmd' run build`
    - `$env:Path='C:\Program Files\nodejs;' + $env:Path; & 'C:\Program Files\nodejs\npm.cmd' run lint`
    - Headless Edge screenshot and DOM render checks for the redesigned Home screen.
    - `npm run dev` through `Start-Process`, followed by `Invoke-WebRequest http://127.0.0.1:5173/`
  - Result:
    - `git diff --check` passed.
    - `npm run build` passed.
    - `npm run lint` passed.
    - Headless Edge rendered the redesigned Home screen during screenshot verification.
    - `npm run dev` started successfully and returned HTTP 200 from `http://127.0.0.1:5173/`; the dev server was stopped afterward.
  - Notes:
    - The new images are remote public placeholder images, so local visual richness depends on network availability.
    - One Headless Edge verification command failed due a PowerShell `Path/PATH` environment duplication issue; the dev server check was rerun with redirected logs and passed.
  - Next recommended task:
    - Manually review the image-driven Home UI in a browser, then implement the content list screen using the same image-card and route-preview visual system.
- 2026-05-07: Home UI premium polish completed.
  - Task restatement: improve only the existing Home screen's visual quality, emotional impact, spacing rhythm, CTA clarity, and AI route emphasis without adding features, pages, dependencies, or backend work.
  - Assumptions:
    - The current Home information architecture and mock data structure should remain intact.
    - The polish should stay within Vue, HTML, JavaScript, and plain CSS only.
    - The `Scene Trip Notebook` direction remains the selected visual concept.
  - UI polish goals:
    - Make the first screen feel like a modern Korean K-content travel product rather than a scaffold.
    - Make the main CTA and `콘텐츠 > 장소 > AI 동선` flow immediately visible.
    - Improve mobile-first density while keeping desktop scaling polished.
    - Make the AI route teaser visually distinct from normal content/place cards.
  - Visual improvements applied:
    - Rebuilt the hero as a layered travel-notebook cover with warm charcoal, coral, ivory, and mint tones.
    - Added a visible `콘텐츠 > 장소 > AI 동선` flow in the hero and AI route panel.
    - Integrated category chips into the hero so exploration starts in the first screen.
    - Strengthened CTA size, contrast, shadow, and tap feedback.
    - Improved content cards with stronger poster hierarchy, subtle texture, richer shadows, and clearer metadata.
    - Improved place cards with a more intentional map-like preview, status pill hierarchy, and compact hint chips.
    - Made the AI route section a distinct dark feature panel with a light notebook route card inside it.
    - Increased section rhythm, heading hierarchy, hover/tap feedback, and mobile readability.
  - Files changed:
    - `frontend/src/main.js`
    - `frontend/src/styles.css`
    - `frontend/src/components/RouteTeaserCard.js`
    - `progress.md`
  - Commands run:
    - `git status -sb`
    - `Get-ChildItem -Path frontend\src -Recurse -File`
    - `Get-Content -Path frontend\src\data\mockHomeData.js`
    - `Get-Content -Path frontend\src\main.js`
    - `Get-Content -Path frontend\src\styles.css`
    - `Get-Content -Path frontend\src\components\ContentCard.js`
    - `Get-Content -Path frontend\src\components\PlaceCard.js`
    - `Get-Content -Path frontend\src\components\RouteTeaserCard.js`
    - `Get-Content -Path frontend\src\components\Button.js`
    - `Get-Content -Path frontend\src\components\Chip.js`
    - `Get-Content -Path frontend\src\components\AppShell.js`
    - `git diff --check`
    - `$env:Path='C:\Program Files\nodejs;' + $env:Path; & 'C:\Program Files\nodejs\npm.cmd' run build`
    - `$env:Path='C:\Program Files\nodejs;' + $env:Path; & 'C:\Program Files\nodejs\npm.cmd' run lint`
    - `npm run dev` through `Start-Process`, followed by `Invoke-WebRequest http://127.0.0.1:5173/`
    - `npm run dev` through `Start-Process`, followed by headless Edge `--dump-dom` render verification
  - Result:
    - `git diff --check` passed.
    - `npm run build` passed.
    - `npm run lint` passed.
    - `npm run dev` started successfully and returned HTTP 200.
    - Headless Edge render verification found the Home headline, `AI 여행 동선 만들기` CTA, and `커뮤니티` navigation text in the rendered DOM.
    - The dev server was stopped after verification.
  - Next recommended task:
    - Manually review the polished Home UI in a browser, then implement the content list screen with the same Vue and plain CSS design system.
- 2026-05-07: Home UI polished with Lazyweb-derived patterns.
  - Task restatement: use Lazyweb to improve the existing Home UI design quality while keeping Vue, JavaScript, plain CSS, mock data, and the `Scene Trip Notebook` direction.
  - Assumptions:
    - The current `feat/initial-vue-home-ui` branch is still the appropriate PR branch for Home UI polish.
    - Lazyweb plugin tools are not exposed in this live Codex session until restart, so Lazyweb MCP was called directly through the configured local token file.
    - Lazyweb results are used only for reusable UX patterns, not copied visuals.
  - Lazyweb health:
    - `lazyweb_health` returned `healthy`.
  - Lazyweb search queries used:
    - `mobile travel itinerary planner home screen`
    - `travel app home screen destination cards`
    - `itinerary planner mobile app route timeline`
    - `map based place discovery mobile app`
    - `travel guide app mobile home screen`
    - `saved trips mobile app UI`
    - `travel community feed mobile app`
    - `AI travel planner itinerary UI`
    - `travel journal app mobile UI`
    - `trip planning app day by day itinerary`
    - `location discovery app list map toggle`
    - `destination recommendation app cards mobile`
    - `mobile route planner timeline UI`
    - `personalized travel recommendations app UI`
    - `film location travel app UI`
    - `filming location map app mobile`
    - `fan travel itinerary app`
    - `Korean travel app mobile UI`
    - `Kpop travel places app UI`
    - `drama filming location travel app`
  - Patterns selected:
    - Hero with planning preview.
    - Destination card rail with clear metadata.
    - Itinerary timeline as the core feature.
    - Place discovery with map hint.
    - Secondary community feed.
  - Home UI improvements completed:
    - Strengthened the hero promise and repeated the main CTA.
    - Added compact hero stats and a notebook-style route preview.
    - Wrapped category chips in a more intentional category panel.
    - Improved content card hierarchy with category, region, connected place count, and likes.
    - Improved place cards with status, location hints, and map-like preview blocks.
    - Made the AI route teaser more prominent with time, reason, and travel tip per stop.
    - Kept community previews useful but visually secondary with likes and saves.
    - Revised spacing, typography, shadows, and responsive layout using plain CSS only.
  - Files changed:
    - `DESIGN_BRIEF.md`
    - `frontend/src/main.js`
    - `frontend/src/styles.css`
    - `frontend/src/components/AppShell.js`
    - `frontend/src/components/ContentCard.js`
    - `frontend/src/components/PlaceCard.js`
    - `frontend/src/components/RouteTeaserCard.js`
    - `frontend/src/data/mockHomeData.js`
    - `progress.md`
  - Commands run:
    - `git status -sb`
    - `git branch --show-current`
    - `Get-Content -Path PRD.md`
    - `Get-Content -Path AGENTS.md`
    - `Get-Content -Path LEARNED_TECH_STACK.md`
    - `Get-Content -Path DESIGN_BRIEF.md`
    - `Get-Content -Path progress.md`
    - `Get-Content -Path frontend/package.json`
    - `Get-Content -Path frontend/index.html`
    - `Get-Content -Path frontend/src/main.js`
    - `Get-Content -Path frontend/src/styles.css`
    - `Get-Content -Path frontend/src/components/*`
    - `Get-Content -Path frontend/src/data/*`
    - Lazyweb MCP `lazyweb_health`
    - Lazyweb MCP `lazyweb_search` with 20 requested queries, `limit: 5`
    - `$env:Path='C:\Program Files\nodejs;' + $env:Path; & 'C:\Program Files\nodejs\npm.cmd' install`
    - `$env:Path='C:\Program Files\nodejs;' + $env:Path; & 'C:\Program Files\nodejs\npm.cmd' run build`
    - `$env:Path='C:\Program Files\nodejs;' + $env:Path; & 'C:\Program Files\nodejs\npm.cmd' run lint`
    - `npm run dev` through `Start-Process`, followed by `Invoke-WebRequest http://127.0.0.1:5173/`
  - Result:
    - Runtime fix was not needed because `npm run dev` and `npm run build` already existed.
    - `npm install` succeeded.
    - `npm run build` passed.
    - `npm run lint` passed.
    - `npm run dev` started and returned HTTP 200 from `http://127.0.0.1:5173/`; the dev server was stopped afterward.
  - Next recommended task:
    - Open the Home UI in a local browser for manual visual review, then implement the content list screen using Vue only.
- 2026-05-07: Vue frontend runtime enabled.
  - Task restatement: fix the frontend runtime so the existing Vue Home UI can run locally through a working development server.
  - Root cause:
    - The frontend was a plain browser Vue/importmap setup, not a complete Vue + Vite setup.
    - `package.json` did not have a `dev` script.
    - `build` only ran a custom source check, so it did not compile the Vue app or verify runtime imports.
    - `index.html` depended on a direct `node_modules` importmap and file-based CSS loading, which made direct browser opening unreliable.
  - Fixes applied:
    - Added Vite as the frontend development/build runtime.
    - Added working `npm run dev` and `npm run build` scripts.
    - Added `frontend/vite.config.js` with the Vue compiler-enabled build alias so current Vue template strings render correctly.
    - Updated `frontend/index.html` to use the Vite entry path.
    - Imported `src/styles.css` from `src/main.js`.
    - Verified the `#app` mount target before mounting Vue.
    - Normalized Home UI Korean copy and mock data used by visible cards and sections.
    - Added a README section with install, run, URL, and build instructions.
  - Files changed:
    - `README.md`
    - `frontend/index.html`
    - `frontend/package.json`
    - `frontend/package-lock.json`
    - `frontend/vite.config.js`
    - `frontend/scripts/check-frontend.js`
    - `frontend/src/main.js`
    - `frontend/src/components/AppShell.js`
    - `frontend/src/components/Button.js`
    - `frontend/src/components/Chip.js`
    - `frontend/src/components/ContentCard.js`
    - `frontend/src/components/PlaceCard.js`
    - `frontend/src/components/RouteTeaserCard.js`
    - `frontend/src/data/mockHomeData.js`
    - `progress.md`
  - Commands run:
    - `git status -sb`
    - `Get-Content -Path frontend/package.json`
    - `Get-Content -Path frontend/index.html`
    - `Get-Content -Path frontend/src/main.js`
    - `Get-Content -Path frontend/src/components/*`
    - `Get-Content -Path frontend/src/styles.css`
    - `$env:Path='C:\Program Files\nodejs;' + $env:Path; & 'C:\Program Files\nodejs\npm.cmd' install`
    - `$env:Path='C:\Program Files\nodejs;' + $env:Path; & 'C:\Program Files\nodejs\npm.cmd' run lint`
    - `$env:Path='C:\Program Files\nodejs;' + $env:Path; & 'C:\Program Files\nodejs\npm.cmd' run build`
    - `npm run dev` through `Start-Process`, followed by `Invoke-WebRequest http://127.0.0.1:5173/`
    - Headless Chrome/Edge DOM verification attempts
  - Final result:
    - `npm install` succeeded after npm registry access approval.
    - `npm run lint` passed.
    - `npm run build` passed after rerunning outside the sandbox because esbuild spawn was blocked inside the sandbox.
    - `npm run dev` started a local Vite server and returned HTTP 200 from `http://127.0.0.1:5173/`.
    - Browser automation through Node REPL failed with Windows `Access denied`; headless Chrome/Edge DOM dump also failed because local browser profile/Crashpad access was blocked. The app compile and dev server checks passed, but visual browser screenshot verification could not be completed inside this environment.
  - Next recommended task:
    - Open `http://127.0.0.1:5173/` in a local browser for manual visual confirmation, then implement the content list screen using Vue only.
- 2026-05-07: Initial Vue Home UI implemented.
  - Task restatement: create the first MVP implementation using only the learned stack, limited to the frontend Home screen.
  - Success criteria:
    - Existing repository structure detected before coding.
    - No previous implementation existed on this branch, so a simple frontend project structure was created.
    - Frontend uses Vue, HTML, CSS, and JavaScript only.
    - No React, TypeScript, Tailwind CSS, external UI component libraries, backend APIs, auth, database, AI generation, or community feature implementation was added.
    - Home screen follows `Scene Trip Notebook`: warm Korean travel notebook mood, mobile-first layout, card-based exploration, and route teaser.
    - Mock data is Korean and separated in `frontend/src/data/mockHomeData.js`.
  - Files changed:
    - `.gitignore`
    - `frontend/index.html`
    - `frontend/package.json`
    - `frontend/package-lock.json`
    - `frontend/scripts/check-frontend.js`
    - `frontend/src/main.js`
    - `frontend/src/styles.css`
    - `frontend/src/components/AppShell.js`
    - `frontend/src/components/Button.js`
    - `frontend/src/components/Chip.js`
    - `frontend/src/components/ContentCard.js`
    - `frontend/src/components/PlaceCard.js`
    - `frontend/src/components/RouteTeaserCard.js`
    - `frontend/src/data/mockHomeData.js`
    - `progress.md`
  - Commands run:
    - `git status -sb`
    - `git branch --show-current`
    - `Get-Content -Raw -Encoding UTF8 -Path PRD.md`
    - `Get-Content -Raw -Encoding UTF8 -Path AGENTS.md`
    - `Get-Content -Raw -Encoding UTF8 -Path LEARNED_TECH_STACK.md`
    - `Get-Content -Raw -Encoding UTF8 -Path DESIGN_BRIEF.md`
    - `Get-Content -Raw -Encoding UTF8 -Path progress.md`
    - `Get-ChildItem -Force`
    - `git switch -c feat/initial-vue-home-ui`
    - `npm --version`
    - `node --version`
    - `cmd /c where npm`
    - `& 'C:\Program Files\nodejs\node.exe' --version`
    - `& 'C:\Program Files\nodejs\npm.cmd' --version`
    - `& 'C:\Program Files\nodejs\npm.cmd' install`
    - `$env:Path='C:\Program Files\nodejs;' + $env:Path; & 'C:\Program Files\nodejs\npm.cmd' run build`
    - `$env:Path='C:\Program Files\nodejs;' + $env:Path; & 'C:\Program Files\nodejs\npm.cmd' run lint`
  - Build/lint result:
    - First `npm install` attempt failed because the sandbox used only-if-cached registry mode and Vue was not cached.
    - Retried with approval for npm registry access; install succeeded.
    - `npm run build` passed after prepending `C:\Program Files\nodejs` to PATH.
    - `npm run lint` passed after prepending `C:\Program Files\nodejs` to PATH.
  - Next recommended task:
    - Review and merge the learned stack PR first if needed, then implement the content list screen using Vue only.
- 2026-05-07: Learned technology stack locked.
  - Task restatement: update documentation and agent rules so the project only uses learned technologies, without implementing app code, scaffolding, or adding dependencies.
  - Success criteria:
    - `LEARNED_TECH_STACK.md` documents allowed technologies, forbidden technologies, default MVP stack, AI strategy, and styling strategy.
    - `AGENTS.md` includes a `Learned Technology Constraints` section.
    - `PRD.md` is narrowed to Vue + Spring Boot + MyBatis + MySQL-compatible database.
    - `DESIGN_BRIEF.md` points frontend work to Vue and plain/scoped CSS with no external UI libraries.
    - No application code or dependencies are added in this task.
  - Commands run:
    - `git status -sb`
    - `git branch --show-current`
    - `Get-Content -Raw -Encoding UTF8 -Path PRD.md`
    - `Get-Content -Raw -Encoding UTF8 -Path AGENTS.md`
    - `Get-Content -Raw -Encoding UTF8 -Path DESIGN_BRIEF.md`
    - `Get-Content -Raw -Encoding UTF8 -Path progress.md`
    - `Select-String -Path PRD.md,DESIGN_BRIEF.md,AGENTS.md,progress.md -Encoding UTF8 -Pattern ...`
    - `npm --version`
    - `node --version`
    - `cmd /c npm --version`
    - `cmd /c node --version`
    - `cmd /c where npm`
    - `cmd /c where node`
    - `git diff --check`
  - Check result:
    - Safe text checks were run.
    - `npm` is still not available in the current shell PATH.
    - `node` resolves to a WindowsApps executable but cannot run in this shell due to Access denied.
    - The build/lint retry from the previous task could not run in this branch because this documentation branch intentionally contains no app scaffold or package scripts, and npm is still unavailable in the current shell.
  - Next recommended task:
    - Create initial Vue + Spring Boot + MyBatis MVP implementation plan.
- 2026-05-07: Rewrote DESIGN_BRIEF.md in English on main.
  - Task restatement: rewrite the design brief in English as much as possible while keeping actual user-facing app copy examples in Korean.
  - Success criteria:
    - Work is done directly on `main` as requested.
    - `DESIGN_BRIEF.md` remains documentation-only and contains no application code.
    - Korean app UI copy rules are preserved.
  - Assumptions:
    - No GitHub push was requested, so the change is committed locally on `main` only.
- 2026-05-07: MVP original design direction selected.
  - Task restatement: create a product-led MVP design direction for a Korean K-content AI travel route recommendation platform without implementing app code.
  - Success criteria:
    - No external design references used.
    - All planned user-facing UI text remains Korean.
    - Direction supports content browsing, related places, map viewing, likes, visit status, AI route generation, route saving, and community sharing.
    - Mobile-first MVP demo can start from a small reusable design system and one page at a time.
  - Assumptions:
    - Primary users browse on mobile while planning K-content themed trips.
    - MVP should feel like a warm Korean travel guide and fan route notebook, not an admin dashboard.
    - Real images or seed image URLs can be connected later; this task defines visual direction only.
    - Frontend stack has not been selected in this repository yet.
  - Uncertainty:
    - Exact frontend framework and map provider integration details are not present in the repository.
    - This does not block the design direction, so implementation details are deferred.

## Selected Original Design Direction

### Visual Concept
- Name: "Scene Trip Notebook"
- Mood: a warm Korean travel notebook where users collect favorite K-content scenes, discover real places, and turn them into practical travel routes.
- Core metaphor: scene cards become place stops, and place stops become a day-by-day route timeline.
- Visual feel: emotional but quiet, fan-friendly but not flashy, travel-oriented but still clear enough for planning.

### Product Design Principles
- Start from the user's fan intent: "좋아하는 콘텐츠에서 출발해 실제 장소로 이동한다."
- Keep the main path obvious: 콘텐츠 선택 -> 장소 탐색 -> AI 동선 생성 -> 저장/공유.
- Make cards scannable with one primary action per card.
- Treat AI output as a planner result that must show place identity, visit order, reason, and movement tip clearly.
- Make personal states visible: 좋아요, 방문예정, 방문완료, 저장됨.

### Color Direction
- Primary: warm coral for main CTAs such as "AI 여행 동선 만들기" and "저장하기".
- Secondary: soft ivory/warm neutral for page backgrounds.
- Accent: clear sky blue or mint for map, route, and movement hints.
- Text: charcoal or deep navy for readable Korean body text.
- Status colors: green for 방문완료, amber for 방문예정, soft red for destructive actions or errors.
- Avoid a one-note palette; balance warm travel tones with cool route/map accents.

### Typography Direction
- Use a Korean-friendly sans-serif system font stack first for MVP speed.
- Page titles should be confident but not oversized.
- Card titles should be compact and readable on mobile.
- Metadata such as 지역, 기간, 좋아요 수, 방문 상태 should be smaller but never tiny.
- Korean labels should be natural: "콘텐츠", "장소", "동선", "방문예정", "방문완료", "코스 공유".

### Layout System
- Mobile-first single-column flow.
- Use horizontal chips for category, region, duration, theme, and transport filters.
- Use cards for content, places, routes, and posts.
- Use split map/list only on larger screens; on mobile, map is a focused section or toggle view.
- AI result page should use a vertical route timeline grouped by day.
- Desktop layout can expand into a 12-column content area with a persistent right-side map or route summary where useful.

### Component System
- App shell: top header, mobile bottom navigation, desktop header navigation.
- Navigation labels: "홈", "콘텐츠", "장소", "AI 동선", "커뮤니티", "마이".
- Buttons: primary, secondary, icon, danger, loading state.
- Chips and segmented controls: category, region, duration, theme, transport.
- Cards: content card, place card, route card, community post card.
- Route timeline: day header, visit order marker, place stop, reason, travel tip.
- State controls: like button, 방문예정 toggle, 방문완료 toggle, saved route indicator.
- Feedback: empty state, loading state, validation error, AI generation pending state.

### Page-by-Page UI Plan
- Home: warm service entry, category shortcuts, popular content, recommended places, AI route CTA.
- Content list: category tabs, search, sort, content cards.
- Content detail: content overview, related places, map preview, AI route CTA.
- Place list: filters, place cards, map entry.
- Place detail: image/summary, address, map, related content, like and visit status actions.
- AI route generation: step-based form for content, region, duration, theme, transport.
- AI route result: summary, day timeline, place reasons, movement tips, save/share actions.
- Saved routes: user's saved AI routes with region, duration, and route summary.
- Community list: shared route posts with region, duration, like count, writer.
- Community detail: post body, ordered places, comments if implemented.
- My page: liked content, liked places, planned places, visited places, saved routes, my posts.

### Mobile-First Responsive Strategy
- Design all primary flows for 360px width first.
- Bottom navigation is the default mobile navigation.
- Keep CTA placement thumb-friendly near the bottom of the viewport when possible.
- Convert horizontal desktop sections into vertical mobile cards.
- Keep map interactions simple on mobile: preview first, expanded map when selected.
- On tablet and desktop, pair route timeline with map or summary panels.

### Accessibility Considerations
- Maintain strong text contrast on warm backgrounds.
- Do not rely on color alone for 방문예정/방문완료/좋아요 states.
- Buttons and chips need clear focus, hover, selected, disabled, and loading states.
- Touch targets should be at least 44px high on mobile.
- Form errors should be written in Korean and placed near the relevant field.
- AI generation state should announce progress with clear Korean text such as "AI가 여행 동선을 만들고 있어요."

### Frontend Implementation Order
1. Define design tokens and base app shell.
2. Build shared components: buttons, chips, cards, empty/loading/error states.
3. Implement Home as the first MVP demo screen.
4. Implement content list and content detail.
5. Implement place list/detail with map placeholder or provider integration.
6. Implement AI route generation form.
7. Implement AI route result timeline and save/share actions.
8. Implement saved routes and My page.
9. Implement community list/detail.
10. Add responsive polish, accessibility pass, and build/lint verification.

## Current Status
- Initial Vue frontend Home screen has been implemented.
- Backend features have not been implemented.
- Learned stack is locked to Vue + Spring Boot + MyBatis + MySQL-compatible database.
- First MVP AI route generation must use MockAiRouteGenerator.
- External Vector DB and unlearned technologies require explicit approval.
- `DESIGN_BRIEF.md` is now written primarily in English and keeps Korean UI copy examples for actual app text.
- The selected MVP design direction is recorded in progress.md.

## Next Recommended Task
- Review and merge the learned stack documentation PR if needed.
- Implement the content list screen using Vue only.

## v0 Design Extraction

### Exact v0 files inspected
- `scenik/sceni-k-travel-planner/package.json`
- `scenik/sceni-k-travel-planner/components.json`
- `scenik/sceni-k-travel-planner/app/page.tsx`
- `scenik/sceni-k-travel-planner/app/layout.tsx`
- `scenik/sceni-k-travel-planner/app/globals.css`
- `scenik/sceni-k-travel-planner/styles/globals.css`
- `scenik/sceni-k-travel-planner/components/scenik/top-app-bar.tsx`
- `scenik/sceni-k-travel-planner/components/scenik/bottom-navigation.tsx`
- `scenik/sceni-k-travel-planner/components/scenik/content-card.tsx`
- `scenik/sceni-k-travel-planner/components/scenik/place-card.tsx`
- `scenik/sceni-k-travel-planner/components/scenik/route-card.tsx`
- `scenik/sceni-k-travel-planner/components/scenik/itinerary-timeline.tsx`
- `scenik/sceni-k-travel-planner/components/scenik/ai-components.tsx`
- `scenik/sceni-k-travel-planner/components/scenik/search-bar.tsx`
- `scenik/sceni-k-travel-planner/components/scenik/chip.tsx`
- `scenik/sceni-k-travel-planner/components/scenik/section-header.tsx`
- `scenik/sceni-k-travel-planner/components/scenik/index.ts`
- `scenik/sceni-k-travel-planner/components/screens/home-screen.tsx`
- `scenik/sceni-k-travel-planner/components/screens/explore-screen.tsx`
- `scenik/sceni-k-travel-planner/components/screens/ai-course-screen.tsx`
- `scenik/sceni-k-travel-planner/components/screens/content-detail-screen.tsx`
- `scenik/sceni-k-travel-planner/components/screens/place-detail-screen.tsx`
- `scenik/sceni-k-travel-planner/components/screens/community-screen.tsx`
- `scenik/sceni-k-travel-planner/components/screens/my-page-screen.tsx`
- `scenik/sceni-k-travel-planner/public/icon.svg`
- `scenik/sceni-k-travel-planner/public/images/kdrama-scene.jpg`
- `scenik/sceni-k-travel-planner/public/images/onboarding-hero.jpg`
- `scenik/sceni-k-travel-planner/public/images/seoul-street.jpg`

### v0 layout patterns found
- Mobile app canvas uses a centered max-width shell (`max-w-lg`/mobile-first), sticky top app bar, and fixed bottom navigation.
- Home uses greeting, rounded search, horizontal chips, a featured "Today's sceniK Route" image-led route card, horizontal poster rails, compact place cards, and popular route cards.
- AI Course uses a linear step flow: content selection, travel conditions, loading, result. Steps use progress indicators, sticky CTAs, poster grids, chip groups, icon option cards, and candidate place previews.
- Result uses a hero image, AI generated badge, route stats, AI recommendation reason box, map preview, and a day-by-day itinerary timeline.
- Explore uses a top search/filter header, horizontal category chips, a map preview/bento discovery area, cinematic image cards, and poster rails.
- Content/place detail screens use large media heroes with gradient overlays, floating title cards, rounded action buttons, verified/filming badges, related content rails, map preview, and fixed bottom CTAs.
- Community uses feed posts with profile header, square image, route timeline preview, actions, tags, and FAB.
- My Page uses a rounded profile card, stats grid, chip tabs, saved route cards, grouped settings rows, and empty states.

### v0 visual tokens found
- Framework/design source: Next.js + React + TypeScript + Tailwind + shadcn/Radix/lucide. These are visual references only and will not be installed or copied directly.
- v0 original colors include old sceniK teal/coral/warm ivory tokens (`#0F766E`, `#FF6B4A`, `#FFF8ED`, `#DDF3F5`, `#172033`), plus shadcn semantic variables.
- Required corrective palette override: translate the v0 structure into sky-blue tokens: `#38BDF8`, `#0EA5E9`, `#0369A1`, `#F8FCFF`, `#EAF7FF`, `#DFF4FF`, `#FF7A59`, `#FFE4D8`, `#172033`, `#475569`, `#D8EAF5`, `#FFF4E6`.
- Typography scale found: hero 32/40 bold, page 24/32 bold, section 20/28 semibold, card 17/24 semibold, body 15/22, caption 12/16, label 13/18.
- Spacing found: container margin 20px, stack 8/16/24px, section gap 32px, minimum touch target 44px.
- Shape/shadow found: rounded-xl/2xl/full, soft card shadows, image gradient overlays, sticky bottom CTAs, pill chips, circular route markers, raised AI Course nav button.

### v0 components to port
- Hero route card with image/gradient, verified badge, route metadata, and CTA.
- Sticky/glass top app bar with sceniK route-line logo.
- Polished fixed bottom navigation with raised AI Course tab and active dot.
- Poster-style K-content cards with overlay title/type badges and selected check state.
- Horizontal place cards with thumbnail, verified badge, map pin metadata, and related content cue.
- AI Course progress/step indicator, chip groups, option cards, selected states, and sticky generate CTA.
- AI loading route visual: map/route motif, progress ring/bar, calm status messages.
- AI result hero, route stats, trust/verified banner, AI recommendation reason panel.
- Itinerary timeline with day header, vertical route line, numbered stop markers, thumbnail, time/duration, reason, tip, and transport hints.
- Sky-blue map fallback derived from v0/Stitch map preview patterns.
- Community feed card and My Page profile/stats/settings card polish.

### v0 to Vue porting plan

| v0 file/component | Current Vue target | What will be ported |
| --- | --- | --- |
| `components/scenik/top-app-bar.tsx` | `frontend/src/components/AppShell.js`, `frontend/src/styles.css` | Route-line logo, sticky glass header, compact actions |
| `components/scenik/bottom-navigation.tsx` | `frontend/src/components/AppShell.js`, `frontend/src/styles.css` | Raised AI Course tab, active dot, app-like fixed bottom bar |
| `components/screens/home-screen.tsx` | `frontend/src/main.js`, `frontend/src/styles.css` | Greeting/dashboard rhythm, search, featured route hero, horizontal rails |
| `components/scenik/route-card.tsx` | Home route proof and community cards in `frontend/src/main.js`, CSS | Image-led route cards, numbered route dots, verified route treatment |
| `components/scenik/content-card.tsx` | `frontend/src/components/ContentCard.js`, CSS | Poster card layout, overlay badges, selected check |
| `components/scenik/place-card.tsx` | `frontend/src/components/PlaceCard.js`, CSS | Image-first place cards, verified/filming badges, location metadata |
| `components/scenik/search-bar.tsx` | Search boxes in `frontend/src/main.js`, CSS | Rounded search panel with icon and soft background |
| `components/scenik/chip.tsx` | Existing `.chip` buttons and selects in `frontend/src/main.js`, CSS | Filled/outline chips, selected ring, touch target |
| `components/scenik/ai-components.tsx` | `frontend/src/main.js`, `frontend/src/styles.css` | AI reason panel, progress steps, loading route motif, stats bar |
| `components/scenik/itinerary-timeline.tsx` | `frontend/src/components/RouteTimeline.js`, CSS | Day timeline with vertical line, order markers, thumbnails, tips |
| `components/screens/explore-screen.tsx` | Explore section in `frontend/src/main.js`, CSS | Search/filter header, category chips, map/discovery card rhythm |
| `components/screens/content-detail-screen.tsx` | Content detail section in `frontend/src/main.js`, CSS | Media hero, gradient overlay, fixed CTA feel through hero actions |
| `components/screens/place-detail-screen.tsx` | Place detail section and `MapPreview.js`, CSS | Photo hero, verified badges, action buttons, designed map preview |
| `components/screens/community-screen.tsx` | Community section in `frontend/src/main.js`, CSS | Travel feed card, route tags, action metadata |
| `components/screens/my-page-screen.tsx` | My Page section in `frontend/src/main.js`, CSS | Profile/stat dashboard and grouped activity cards |

### What old MVP design will be removed
- Remove the previous green/teal-dominant visual identity and teal shadows.
- Remove generic white-card-only MVP surfaces where possible.
- Remove plain tab-row bottom navigation styling.
- Remove plain route result list styling in favor of a custom itinerary timeline.
- Remove weak map placeholder styling and replace it with a deliberate sky-blue route/map preview.
- Remove old low-emotion dashboard spacing and replace it with v0-inspired mobile app rhythm, image overlays, route motifs, badges, and sticky action hierarchy.

## Stitch Secondary Reference

### Exact Stitch files/folders inspected
- `scenik/stitch_scenik_ai_travel_planner/design.md`
- `scenik/stitch_scenik_ai_travel_planner/home_dashboard/code.html`
- `scenik/stitch_scenik_ai_travel_planner/ai_course_select_content/code.html`
- `scenik/stitch_scenik_ai_travel_planner/ai_course_travel_conditions/code.html`
- `scenik/stitch_scenik_ai_travel_planner/ai_course_generating_route/code.html`
- `scenik/stitch_scenik_ai_travel_planner/ai_course_itinerary_result/code.html`
- `scenik/stitch_scenik_ai_travel_planner/explore/code.html`
- `scenik/stitch_scenik_ai_travel_planner/map_view/code.html`
- `scenik/stitch_scenik_ai_travel_planner/community_feed/code.html`
- `scenik/stitch_scenik_ai_travel_planner/my_page/code.html`
- `scenik/stitch_scenik_ai_travel_planner/scenik_design_system/DESIGN.md`

### Useful ideas borrowed from Stitch
- Strong mobile dashboard sections, "Today's sceniK Route" framing, rounded search/search-filter headers, and bottom nav shell.
- AI Course content grid with selected poster cards and sticky selected-state CTA.
- Travel condition bento cards and image/style cards as inspiration for richer option panels.
- Generating route screen with soft map background, route-line overlay, glass center, progress status, and calm copy.
- Itinerary result with map/route emphasis, verified route trust, day-by-day stops, travel time, and action footer.
- Map view ideas: visible route line, numbered pins, filter chips, bottom sheet-like place preview.

### Ideas rejected from Stitch
- Remote Google Fonts, Material Symbols CDN, Tailwind CDN, and remote generated image URLs.
- Old teal/green-heavy Stitch palette as a dominant direction.
- `maximum-scale=1.0, user-scalable=no` viewport constraints because they weaken accessibility.
- Copying static HTML directly or replacing Vue behavior/data flow.
- Overly large remote-image hero dependencies that would make local MVP visuals fragile.

### How Stitch complements the v0 direction
- v0 is used as the primary component and screen architecture reference.
- Stitch adds extra travel/K-content atmosphere, especially for map, route generation, itinerary detail density, and community feed treatment.
- Both references agree on mobile-first routes, verified badges, poster cards, rounded search, sticky CTAs, and route-line motifs; these are translated into Vue + JavaScript + plain CSS with the required sky-blue palette.

## 2026-05-12 Targeted Home IA / Bottom Navigation Update

### Task restatement
- Update only the sceniK Home screen information architecture and bottom navigation order.
- Preserve the existing AI Course flow, mock adapters, route validation, placeId validation, verified-place constraints, MapPreview, RouteTimeline, and login-required behavior.

### Assumptions
- The current Home screen can use Korean labels for the required IA sections while the rest of the existing MVP copy remains unchanged.
- Backend APIs are still unavailable, so Home recent activity, route, recommendation, and notice content will remain safe local mock data.
- Existing route validation and AI mock generation must not be edited for this targeted task.

### Success criteria
- Bottom navigation order is AI Course, Explore, Home, Community, My Page.
- AI Course opens the AI Course screen, and the center Home tab opens the Home dashboard.
- Home includes recently viewed places, recently saved routes, recommended content groups, popular route groups, and compact notices.
- `npm run lint`, `npm run build`, and `git diff --check` are run and documented.

### Implementation result
- Bottom navigation order changed to AI Course, Explore, Home, Community, My Page.
- Home was restructured into a dashboard: 최근 본 장소, 추천 콘텐츠, 인기 여행 코스, 공지.
- Home data now reuses existing `contents` and `places` for content/place cards and adds local mock route/notice data only for Home.
- `RouteTeaserCard` was lightly extended for Home route cards without changing AI route generation or validation logic.

### Verification result
- `npm run lint`: passed.
- `npm run build`: sandbox run failed with `Error: spawn EPERM` from esbuild; approved external execution passed.
- `git diff --check`: passed, with only existing CRLF conversion warnings.
- Source check verified required Home Korean labels and bottom nav order.
- Mock AI route validation check passed for `content-moonlit-seoul` / Seoul / 1 day with 3 candidate places.
- Local dev server is running at `http://127.0.0.1:5173/` after external execution.

### Known limitations
- Home recent activity, saved routes, popular routes, and notices are mock data only.
- No backend calls, real auth, real saved-route persistence, or live map SDK were added.
- The in-app browser tool was not available in this tool context, so DOM-level browser interaction was approximated with source checks, production build, and HTTP 200 server verification.

## 2026-05-12 Targeted Home Compaction Refinement

### Task restatement
- Refine only the current Home layout based on screenshot feedback.
- Keep the bottom nav order as AI Course, Explore, Home, Community, My Page.
- Do not change AI Course logic, mock adapters, route validation, placeId validation, verified-place constraints, MapPreview, RouteTimeline, or login-required behavior.

### Implementation result
- Removed the large Home dashboard/hero treatment and replaced it with a compact text header plus a small AI Course CTA.
- Removed the Home search bar only; Explore search remains.
- Kept 최근 본 장소 but reduced vertical pressure with compact section spacing, horizontal rails, and category chips.
- Changed 최근 저장 코스 cards to use the same horizontal swipe rail behavior as recent places instead of vertical stacking.
- Changed 추천 콘텐츠 categories into horizontally swipeable compact groups with category chips.
- Changed 인기 여행 코스 categories into horizontally swipeable compact groups with category chips and compact route-card rails.
- Kept 공지사항 as a compact bottom list.
- Login-required panel behavior was unchanged; it still appears only after an auth-required action sets `loginNotice`.

### Verification result
- `npm run lint`: passed.
- `npm run build`: sandbox run failed with esbuild `Error: spawn EPERM`; approved external execution passed.
- `git diff --check`: passed, with CRLF conversion warnings only.
- Source check verified the large Home dashboard title and Home search are removed, required Home section labels remain, route/card rails use horizontal scroll/snap styles, and bottom nav order is unchanged.
- Mock AI route validation still passes for `content-moonlit-seoul` / Seoul / 1 day with 3 candidate places.

### Known limitations
- The Home compact category chips are lightweight visual filters/group labels; the current implementation compacts the categories into horizontal grouped rails rather than building a full tab state machine.
- Home data remains local mock data and is not backend-integrated.

## 2026-05-20 Prototype Light UI Alignment

### Task restatement
- Correct the visual direction from dark mobile app styling to the light Figma Make prototype.
- Keep the Vue 3/Vite implementation and existing behavior while making the current screens feel much closer to `prototype/`.

### Prototype references checked
- `prototype/src/app/components/screens/Home.tsx`
- `prototype/src/app/components/screens/ExploreMain.tsx`
- `prototype/src/app/components/TopNav.tsx`
- `prototype/src/app/components/BottomTabBar.tsx`
- `prototype/src/app/components/PlaceCard.tsx`
- `prototype/src/app/components/ContentPosterCard.tsx`
- `prototype/src/app/components/CourseListCard.tsx`
- `prototype/src/app/components/SectionHeader.tsx`
- `prototype/src/styles/theme.css`

### Implementation result
- Reworked `frontend/src/styles.css` around the prototype light tokens: white app canvas, `#F5F5F5` panels, `#E8E8E8` borders, `#1A1A1A` text/primary action, `#999999` muted labels, and coral only for small heart/accent details.
- Updated Home structure to mirror the prototype: recent place rail, section dividers, content chips + poster rail, popular course chips + vertical course list, and compact notice rows.
- Updated Explore main to mirror the prototype: search input, mode toggle, category label/chips, and two-column popular content grid without the previous map/nearby panel.
- Replaced symbolic top/bottom navigation glyphs with inline SVG icons that match the prototype rhythm without adding an icon dependency.
- Adjusted poster cards, place cards, and course cards to use prototype-like placeholder visuals, compact typography, simple tags, and list/card dimensions.

### Verification result
- `npm run lint`: passed.
- `npm run build`: sandbox run failed with esbuild `spawn EPERM`; approved external execution passed.
- `git diff --check`: passed, with CRLF conversion warnings only.
- Route validation smoke check passed: valid route accepted; invented place, missing `placeId`, and duplicate same-day `visitOrder` rejected.

### Known limitations
- Browser screenshot comparison was not completed in this cycle; exact pixel matching still needs visual QA against the running prototype.
- The Vue app now follows the prototype's light layout and component rhythm more closely, but some deeper secondary screens still use the existing MVP structure rather than a one-to-one prototype port.

## 2026-05-20 Previous Frontend Archive

### Task restatement
- Move the previously built frontend implementation out of the active `frontend/src` tree while keeping it available for reference.
- Preserve the current Vue Router + prototype-light frontend as the active app.

### Implementation result
- Created `archive/previous-frontend/`.
- Moved the unused previous JS screen/component/service implementation there while preserving the old source layout:
  - `archive/previous-frontend/src/screens/`
  - `archive/previous-frontend/src/components/`
  - `archive/previous-frontend/src/services/`
  - `archive/previous-frontend/src/adapters/mockScenikAdapter.js`
  - `archive/previous-frontend/src/data/mockScenikData.js`
  - `archive/previous-frontend/src/data/mockHomeData.js`
- Kept the active frontend files in place:
  - `frontend/src/App.vue`
  - `frontend/src/router/`
  - `frontend/src/views/`
  - `frontend/src/components/common/`
  - `frontend/src/components/layout/`
  - `frontend/src/components/content/`
  - `frontend/src/components/place/`
  - `frontend/src/components/course/`
  - `frontend/src/data/mockData.js`
  - `frontend/src/adapters/routeValidation.js`

### Verification result
- Source search found no active `frontend/src` imports pointing to the archived previous frontend files.
- `npm run lint`: passed.
- `npm run build`: sandbox run failed with esbuild `spawn EPERM`; approved external execution passed.
- `git diff --check`: passed, with CRLF conversion warnings only.
- Current route validation smoke check passed after the archive move.

## 2026-05-20 Reference Material Archive

### Task restatement
- Move files and folders that are not used by the current active frontend/backend version into archive folders.
- Keep the active `frontend/` and `backend/` projects in place.

### Implementation result
- Moved reference sources to `archive/reference-material/`:
  - `archive/reference-material/prototype/`
  - `archive/reference-material/wireframe/`
  - `archive/reference-material/sceni-k-travel-planner/`
  - `archive/reference-material/stitch_scenik_ai_travel_planner/`
- Moved unused root artifacts to `archive/unused-artifacts/`:
  - `archive/unused-artifacts/frontend.zip`
  - `archive/unused-artifacts/package-lock.json`
- Kept current active app structure at the root:
  - `frontend/`
  - `backend/`
  - `docs/`
  - root project docs and operating notes.

### Verification result
- `npm run lint`: passed.
- `npm run build`: sandbox run failed with esbuild `spawn EPERM`; approved external execution passed.
- `git diff --check`: passed, with CRLF conversion warnings only.

## 2026-05-20 Codex Design Multi-Agent Workflow

### Task restatement
- Add project-scoped Codex custom agents for premium frontend design implementation.
- Keep model fields omitted so agents inherit the parent session defaults.

### Implementation result
- Added `.codex/config.toml` with agent thread/depth limits.
- Added custom agent TOML files:
  - `.codex/agents/design-director.toml`
  - `.codex/agents/design-researcher.toml`
  - `.codex/agents/open-design-analyst.toml`
  - `.codex/agents/design-system-architect.toml`
  - `.codex/agents/frontend-implementer.toml`
  - `.codex/agents/visual-qa.toml`
- Research, Open Design analysis, and visual QA agents are configured as read-only.
- Updated `AGENTS.md` with the Frontend Design Workflow section and adjusted archived reference-material paths.

### Verification result
- TOML syntax validation with Python `tomllib`: passed.
- `git diff --check`: passed, with CRLF conversion warnings only.

## 2026-05-20 Prompt Director Skill And Agent

### Task restatement
- Add a repository-scoped Prompt Director agent and skill that converts rough Korean requests into precise English execution prompts before implementation.

### Implementation result
- Added `.codex/agents/prompt-director.toml`.
- Added `.agents/skills/prompt-director/SKILL.md`.
- Updated `AGENTS.md` with the Prompt Director Workflow section.
- The prompt director workflow is configured to:
  - infer product intent from casual Korean requests
  - write a structured English task brief
  - decide whether design, frontend, research, QA, or architecture subagents are needed
  - preserve product constraints and existing behavior
  - continue into execution when the user asked for implementation

### Verification result
- TOML syntax validation with Python `tomllib`: passed.
- Prompt Director skill frontmatter validation: passed.
- `git diff --check`: passed, with CRLF conversion warnings only.

## 2026-05-20 Premium Mobile UI Design Pass

### Task restatement
- Redesign the current sceniK UI into a cleaner, more premium, mobile-first consumer product experience.
- Use Lazyweb and Open Design references before implementation, preserve routing, data flow, auth, localStorage behavior, AI Course logic, and route validation.

### Research and agent workflow
- Spawned dedicated design subagents for:
  - Lazyweb design research
  - Open Design/reference analysis
  - frontend structure inspection
  - design system architecture
  - frontend implementation planning
  - visual QA
- Lazyweb MCP tools were not exposed in this session, so the design research agent reported the blocker and produced a reference-pattern summary from available local context.
- Open Design/reference analysis reused the archived v0/Stitch visual sources as project-local design references.

### Design direction
- Shifted the active UI toward a warm cinematic travel palette:
  - scenic teal
  - sunset coral
  - warm ivory
  - soft cream
  - sky mist
  - sand beige
  - ink navy
- Prioritized poster-led discovery, verified place cards, itinerary/map surfaces, compact bottom navigation, refined chips, and clearer action hierarchy.

### Implementation result
- Updated `frontend/src/styles.css` with a central token layer for:
  - colors
  - surfaces
  - text colors
  - spacing-related component rhythm
  - radius
  - shadows
  - cards
  - buttons
  - chips
  - badges
  - bottom navigation
  - map preview
  - timeline/result surfaces
  - focus and hover states
- Updated `frontend/src/components/content/ContentCard.vue` so poster cards show category badges while preserving selection behavior.
- Preserved existing Vue routing, Pinia stores, auth/localStorage services, AI Course behavior, route generation, route validation, and bottom navigation order.

### Visual QA fixes
- Added visible focus states for buttons, links, inputs, textarea fields, and button-like controls.
- Increased bottom tab readability and added a subtle active icon pill treatment.
- Added top title overflow protection for long page titles.
- Reduced fixed map preview height to a responsive mobile-friendly clamp.
- Increased poster rail card width and clamped poster titles to two lines.
- Softened section dividers and raised small metadata contrast.

### Verification result
- `npm run lint`: passed.
- `npm run build`: sandbox run failed with esbuild `spawn EPERM`; approved external execution passed.
- Route validation smoke check: passed for valid route, invented place rejection, missing `placeId` rejection, and duplicate same-day `visitOrder` rejection.
- `git diff --check`: passed, with CRLF conversion warnings only.

## 2026-05-20 Browser-Based Mobile QA Polish

### Task restatement
- Use the `design_director` workflow for a real browser-based 390px mobile QA pass.
- Inspect Home, Explore, AI Course, AI Result, Community, and My Page for spacing, typography, bottom nav, card density, CTA clarity, scroll rhythm, image cropping, safe area, and visual balance.

### Browser QA result
- Captured 390px mobile screenshots under `qa-screenshots/mobile-390/`:
  - `01-home.png`
  - `02-explore.png`
  - `03-ai-course.png`
  - `04-ai-result.png`
  - `05-community.png`
  - `06-my-page.png`
- Saved layout metrics:
  - `metrics.json`
  - `metrics-after-polish.json`
- QA static server was configured to return 404 for `/api/*` so the frontend mock fallback path was exercised instead of incorrectly treating SPA fallback HTML as API data.
- Final post-polish metrics showed no horizontal overflow for all six checked routes.

### Design director findings applied
- Did not re-add a Home intro or Home top AI Course CTA because earlier product direction explicitly removed it.
- Fixed AI Course CTA visibility with a fixed/stable bottom action area.
- Added bottom navigation safe-area handling via `--bottom-nav-height` and `env(safe-area-inset-bottom)`.
- Stabilized bottom nav icon dimensions so active state does not jump.
- Increased chip touch target height.
- Increased badge readability and refined dark poster badge treatment.
- Improved Home recent place card density and image crop size.
- Added result/community/My Page screen gaps for clearer card rhythm.
- Reduced My Page guest heading scale and added page padding.
- Added map fallback grid/land-patch texture and refined map label.
- Increased map pin touch size.
- Stopped the final timeline connector on the last itinerary item.
- Raised small timeline/feed text readability.
- Added selected-summary badge alignment.

### Verification result
- `npm run lint`: passed.
- `npm run build`: sandbox run failed with esbuild `spawn EPERM`; approved external execution passed.
- Browser QA at 390px: Home, Explore, AI Course, AI Result, Community, and My Page rendered with no horizontal overflow.
- Route validation smoke check: passed for valid route, invented place rejection, missing `placeId` rejection, and duplicate same-day `visitOrder` rejection.
- `git diff --check`: passed, with CRLF conversion warnings only.

## 2026-05-20 CSV Data Import and My Page Fit Polish

### Task chosen
- Reflect the newly supplied CSV files under `data/` in the active sceniK app and fix awkward My Page spacing/button sizing without changing product behavior.

### Files inspected
- `data/contents.csv`
- `data/places.csv`
- `data/content_places.csv`
- `data/place_tags.csv`
- `data/embedding_text.csv`
- `frontend/src/data/mockData.js`
- `frontend/src/views/HomeView.vue`
- `frontend/src/views/ContentListView.vue`
- `frontend/src/views/CourseConditionsView.vue`
- `frontend/src/views/AiLoadingView.vue`
- `frontend/src/views/MyPageView.vue`
- `frontend/src/components/content/ContentCard.vue`
- `frontend/src/components/place/PlaceCard.vue`
- `frontend/src/views/ContentDetailView.vue`
- `frontend/src/views/PlaceDetailView.vue`
- `backend/src/main/resources/data.sql`

### Changes made
- Added `scripts/importCsvData.cjs` to convert the root CSV files into the existing frontend mock-data shape and backend MySQL seed SQL.
- Regenerated `frontend/src/data/mockData.js` with:
  - 22 contents
  - 281 places
  - 269 content-place relationships
  - 8 route templates
  - CSV-derived tags, related content IDs, verified flags, and deterministic region-based coordinates for route validation.
- Regenerated `backend/src/main/resources/data.sql` from the same CSV source while preserving existing API and MyBatis mapper contracts.
- Updated Home/Explore category chips for the CSV categories: drama, movie, and variety mapped to 드라마, 영화, 예능.
- Updated AI Course condition selection to use regions actually available for the selected content and to avoid invalid duration options when a region has too few candidate places.
- Updated AI route generation fallback so it chooses a valid candidate-region pool instead of validating mixed-region candidates against the wrong requested region.
- Added image fallback handlers for content/place cards and detail hero images.
- Added frontend place-response normalization so backend API place data can still use CSV-derived UI fields such as tags, related content IDs, verified state, and fallback images.
- Tightened My Page profile card, guest login panel, stat buttons, and 3-tab toggle sizing for mobile fit.

### Verification result
- `npm run lint`: passed.
- `npm run build`: sandbox run failed with esbuild `spawn EPERM`; approved external execution passed. Vite reported a large chunk warning after CSV data import.
- `mvn -q -DskipTests package`: passed.
- Route validation smoke check: passed for valid route, invented place rejection, missing `placeId` rejection, and duplicate same-day `visitOrder` rejection.
- `git diff --check`: passed, with CRLF conversion warnings only.

## 2026-05-20 Community, Utility Routes, and AI Course Function Fixes

### Task chosen
- Fix the user-reported MVP issues: community board/detail navigation, notification/settings route behavior, oversized comment text, My Page liked-empty state, and awkward AI Course generation.

### Prototype reference inspected
- `archive/reference-material/prototype/src/app/components/screens/CommunityBoardList.tsx`
- `archive/reference-material/prototype/src/app/components/screens/CommunityPostDetail.tsx`
- `archive/reference-material/prototype/src/app/components/notifications/NotificationsScreen.tsx`
- `archive/reference-material/prototype/src/app/components/settings/SettingsScreen.tsx`
- `archive/reference-material/prototype/src/app/components/screens/MyActivityLikedContent.tsx`
- `archive/reference-material/prototype/src/app/components/screens/MyActivityLikedPlaces.tsx`
- `archive/reference-material/prototype/src/app/components/screens/AIContentSelection.tsx`
- `archive/reference-material/prototype/src/app/components/screens/AIResult.tsx`

### Changes made
- Rebuilt `frontend/src/views/CommunityView.vue` into route-aware states for board list, board feed, post detail, and write form while keeping localStorage community behavior.
- Added dedicated `frontend/src/views/NotificationsView.vue` and `frontend/src/views/SettingsView.vue`, then wired `/notifications` and `/settings` in `frontend/src/router/index.js`.
- Reduced comment row typography in `frontend/src/styles.css` to a compact 11px treatment.
- Updated My Page liked tab to show `아직 좋아요한 콘텐츠나 장소가 없습니다.` when both liked content and liked places are empty.
- Added multi-content AI selection through `selectedContentIds` in `frontend/src/stores/uiStore.js`.
- Updated AI Course selection, conditions, loading/generation, and result views so two or more selected contents can generate one validated route from verified candidate places only.
- Added result day tabs and improved generated copy/trust messaging without changing route validation rules.

### Browser/source QA result
- Browser route check passed for `/community/boards`, `/community/post/seed-post-1`, `/notifications`, `/settings`, `/my`, and `/ai`.
- Browser verified community board list opens, post detail opens, notification screen opens, settings screen opens, AI Course shows multi-select copy, and selecting a second content updates the CTA count to `(2)`.
- Browser signup/login-state check verified the My Page liked tab empty state appears for a logged-in user with no liked items.
- Browser AI Course flow generated an AI result after selecting two contents; the result retained verified-place trust messaging and route result structure.
- Comment-input automation was blocked by the Browser plugin virtual clipboard, so comment font was verified by source CSS (`.comment-row` and `.comment-row b` at 11px).

### Verification result
- `npm run lint`: passed.
- `npm run build`: sandbox run failed with esbuild `spawn EPERM`; approved external execution passed. Vite reported the existing large chunk warning after CSV data import.
- Route validation smoke check: passed for valid route, invented place rejection, missing `placeId` rejection, and duplicate same-day `visitOrder` rejection.

## 2026-05-21 AI Course Date Range and Top Spacing Polish

### Task chosen
- Fix the fixed-header spacing problem where the first card/panel sits too close to the top app bar.
- Replace the AI Course duration-only 1/2 day choice with departure and return date selection.

### Changes made
- Added `frontend/src/utils/tripDates.js` for local date defaults, date-range normalization, duration calculation, and display labels.
- Added `startDate` and `endDate` to `courseConditions` in `frontend/src/stores/uiStore.js`, with compatibility normalization for existing saved localStorage state.
- Updated `frontend/src/views/CourseConditionsView.vue` to show departure/return date inputs and a computed duration summary instead of the old duration chip-only control.
- Updated `frontend/src/views/AiLoadingView.vue` so generated route duration and summary come from the selected date range while still validating against verified candidate places only.
- Updated `frontend/src/views/AiResultView.vue` to show the selected date range in the result hero.
- Updated `frontend/src/styles.css` with `--top-nav-height`, `--screen-top-gap`, safer screen top padding, date-range input styling, and a compact result-date pill.

### Verification result
- `npm run lint`: passed.
- `npm run build`: sandbox run failed with esbuild `spawn EPERM`; approved external execution passed. Vite reported the existing large chunk warning after CSV data import.
- Route validation smoke check: passed for date-derived 3-day duration, valid route, invented place rejection, missing `placeId` rejection, and duplicate same-day `visitOrder` rejection.
- Browser QA on `/ai/conditions`: date inputs rendered, default date min values were valid, and the top gap under the fixed app bar was 14px.
- Browser QA on `/ai/result`: selecting `2026-05-28` to `2026-05-30` generated a 3-day result with the date range visible, day tabs visible, and a 14px gap below the top app bar.

## 2026-05-21 AI Course Selection Persistence Cleanup

### Task chosen
- Stop AI Course content selections from being saved/restored after finishing a route and re-entering the AI Course flow.
- Remove the selected chip shadow from category buttons such as 전체, 드라마, 영화, and 예능.

### Changes made
- Changed AI Course selection state in `frontend/src/stores/uiStore.js` to default to no selected content.
- Excluded `selectedContentId` and `selectedContentIds` from `uiStore.persist()` so localStorage no longer stores AI Course selection.
- Added `setAiSelection()` for one-time session preselection from content/place detail CTAs.
- Added `resetAiSelection()` and call it on `frontend/src/views/CourseView.vue` mount so re-entering AI Course starts clean.
- Updated detail CTAs to use session-only AI selection without persisting it.
- Added direct guard in AI loading so generation redirects back to AI Course if no content is selected.
- Removed `box-shadow` from `.chip.selected` in `frontend/src/styles.css`.

### Verification result
- `npm run lint`: passed.
- `npm run build`: sandbox run failed with esbuild `spawn EPERM`; approved external execution passed. Vite reported the existing large chunk warning after CSV data import.
- Store smoke check: selected content IDs were not written to localStorage and `restore()` reset the AI selection to empty.
- Route validation smoke check: passed for valid route and invented-place rejection.
- Browser QA on `/ai`: initial selected poster count was 0, selecting one content made the count 1, and re-entering `/ai` reset selected poster count back to 0.
- Browser QA confirmed selected category chip `box-shadow` computed as `none`.

## 2026-05-21 sceniK Design Token Visual Pass

### Task chosen
- Apply the user-provided Design.md-style visual direction to the current sceniK UI without changing behavior.
- Keep Home recent places and recommended content as horizontal swipe rails.

### Files inspected
- `frontend/src/styles.css`
- `frontend/src/views/HomeView.vue`
- `progress.md`

### Changes made
- Added `DESIGN.md` as the sceniK design-system reference with the new gradient mesh, indigo CTA, deep navy text, canvas/canvas-soft/canvas-cream surfaces, pill controls, subtle elevation, and Home rail rules.
- Updated `frontend/src/styles.css` design tokens:
  - primary indigo `#533afd`
  - deep indigo `#4434d4`
  - press indigo `#2e2b8c`
  - ruby accent `#ea2261`
  - ink navy `#0d253d`
  - canvas/canvas-soft/canvas-cream surfaces
  - hairline border and subtle navy shadow tokens
- Replaced the old green/teal dominant background treatment with the supplied pastel gradient-mesh language.
- Restyled app shell, top nav, bottom nav active state, buttons, chips, badges, map/timeline accents, and form focus rings around the new indigo system.
- Kept behavior and Vue state/data flow unchanged.
- Adjusted Home rails:
  - recent place cards now use `flex: 0 0 min(82%, 320px)` so one main card appears with the next-card hint.
  - recommended content posters now use a horizontal snap rail with about two cards visible at 390px and a next-card hint.
  - both rails retain `overflow-x: auto` and `scroll-snap-type: x mandatory`.

### Verification result
- `npm run lint`: passed.
- `npm run build`: sandbox run failed with esbuild `spawn EPERM`; approved external execution passed. Vite reported the existing large chunk warning.
- `git diff --check`: passed, with existing CRLF conversion warnings only.
- Route validation smoke check: passed for valid route, invented place rejection, missing `placeId` rejection, and duplicate same-day `visitOrder` rejection.
- Browser DOM QA on `/home` at 390px: no Vite error overlay, `--color-primary` computed as `#533afd`, body background includes the gradient mesh, bottom active tab uses indigo, recent rail and poster rail both use horizontal overflow and mandatory scroll snap.
- Browser measured recent card width at about 293px on a 390px rail and poster card width at about 165px, matching the intended swipe/card hint behavior.
- Browser screenshot capture timed out in the browser plugin, so visual verification was completed through DOM/computed-style checks.

## 2026-05-21 Pretendard and Coinbase-Style Blue Visual Pass

### Task chosen
- Replace the previous pastel/indigo mesh direction with the user-provided Coinbase-style design language.
- Keep the work visual-only: no routing, store, auth, localStorage, route generation, or validation behavior changes.

### Files inspected
- `DESIGN.md`
- `frontend/src/styles.css`
- `frontend/src/views/HomeView.vue`

### Changes made
- Rewrote `DESIGN.md` around a clean institutional sceniK Blue system:
  - primary blue `#0052ff`
  - active blue `#003ecc`
  - white canvas
  - soft gray surfaces
  - near-black dark editorial panels
  - minimal shadows
  - pill CTAs
  - Pretendard primary font stack
- Updated `frontend/src/styles.css` tokens and visible styling to match:
  - `font-family` now starts with `"Pretendard Variable", Pretendard`.
  - removed the active pastel gradient mesh from page/app backgrounds.
  - changed brand wordmark and selected states to sceniK Blue.
  - changed buttons/chips/badges/focus rings/nav active states to the single-blue system.
  - changed map/timeline accents to blue/gray instead of teal/ruby.
  - changed AI result hero to a dark editorial panel using `#0a0b0d`.
- Preserved Home horizontal rails:
  - recent place rail still uses horizontal overflow and scroll snap.
  - recommended content poster rail still uses horizontal overflow and scroll snap.

### Verification result
- `npm run lint`: passed.
- `npm run build`: sandbox run failed with esbuild `spawn EPERM`; approved external execution passed. Vite reported the existing large chunk warning.
- `git diff --check`: passed, with existing CRLF conversion warnings only.
- Route validation smoke check: passed for valid route, invented place rejection, missing `placeId` rejection, and duplicate same-day `visitOrder` rejection.
- Browser DOM QA on `/home`: primary token computed as `#0052ff`, body background is white, brand color is blue, font stack starts with Pretendard, recent/poster rails remain `overflow-x: auto` and `scroll-snap-type: x mandatory`.
- Browser DOM QA on `/ai/result`: result hero background computed as `rgb(10, 11, 13)` and hero title text as white, with no Vite error overlay.

## 2026-05-21 Designer, Visual QA, and UI/UX Polish Pass

### Task chosen
- Use dedicated designer, visual QA, and UI/UX agent review to identify safe visual/layout improvements after the Coinbase-style blue/Pretendard pass.
- Apply only improvements that do not change core product behavior, AI Course generation, route validation, auth, localStorage keys, or bottom navigation order.

### Agent review summary
- Designer Agent:
  - P0: none.
  - P1: reduce old visual aliases in usage, remove heavy hover shadow, change active day tabs to blue, soften map decoration, improve dark result hero padding, increase chip/icon touch targets.
- Visual QA Agent:
  - P0: none.
  - P1: AI Result action hierarchy and bottom spacing, empty AI Result side padding, Home poster ratio, Community feed image density, avoid over-dominant blue if brand direction changes later.
- UI/UX Agent:
  - P0: none.
  - P1: Explore map view needs a way back to content discovery, AI Result action hierarchy should prioritize save, Community card write CTA is duplicated, optional Home AI Course entry strip.

### Changes made
- Updated `frontend/src/views/AiResultView.vue`:
  - Save is now the primary result action.
  - Share remains secondary.
  - Regenerate is lowered to ghost/tertiary.
  - Empty result state gets a scoped `result-empty` class for side padding.
- Updated `frontend/src/views/MapView.vue`:
  - Added the same segmented mode toggle used in Explore so users can move back to content discovery.
- Updated `frontend/src/views/CommunityView.vue`:
  - Removed repeated `글쓰기` link from every feed card, leaving the single FAB/write route as the creation CTA.
- Updated `frontend/src/styles.css`:
  - Reduced hover/card/nav shadow intensity to fit the minimal financial-style visual system.
  - Increased icon buttons, chips, mode toggles, day tabs, My Page toggles, profile logout, and ghost buttons to 44px touch targets.
  - Changed AI Result day-tab selected state to sceniK Blue.
  - Improved AI Result dark panel padding/radius and scoped dark badges.
  - Made result actions layout: Save full-width primary, Share/Regenerate side-by-side, Community post full-width secondary.
  - Added side margin for empty AI Result state.
  - Changed Home poster rail cards to 3:4 poster aspect ratio while preserving horizontal swipe.
  - Reduced Community feed placeholder height and added a lightweight route-line/pin motif.
  - Flattened empty state and selected summary backgrounds.

### Decisions
- Did not add a Home AI Course top CTA because previous user direction explicitly removed Home top intro/CTA. This can be revisited if the next product direction prioritizes AI Course entry over the compact Home IA.
- Did not split the single-blue palette into coral/teal because the immediately preceding user request explicitly asked to follow the Coinbase-style single-blue direction.

### Verification result
- `npm run lint`: passed.
- `npm run build`: sandbox run failed with esbuild `spawn EPERM`; approved external execution passed. Vite reported the existing large chunk warning.
- `git diff --check`: passed before progress update, with existing CRLF conversion warnings only.
- Route validation smoke check: passed for valid route, invented place rejection, missing `placeId` rejection, and duplicate same-day `visitOrder` rejection.
- Browser QA after patch:
  - `/home`: poster rail now measures about 150px wide and 200px high with 3:4 ratio; no horizontal page overflow.
  - `/explore/map`: segmented toggle is visible; both toggle targets are 44px high.
  - `/ai/result`: Save is full-width primary blue, Share and Regenerate are side-by-side, Community post is full-width secondary, all actions 44px high.
  - `/community`: feed image is now about 356x178, mode-toggle buttons are 44px high.
  - `/my`: My Page mode-toggle buttons and logout button are 44px high.

### Follow-up UI/UX agent fix
- Browser QA found Community feed action buttons and MapPreview pin buttons were still below the 44px mobile touch-target baseline.
- Updated `frontend/src/styles.css` so:
  - `.map-pin` is 44x44px.
  - `.feed-actions button`, `.feed-actions a`, and `.comment-box button` have 44px minimum height.

### Final verification after follow-up fix
- `npm run lint`: passed.
- `npm run build`: approved external execution passed after sandbox `spawn EPERM`; Vite still reports the existing large chunk warning.
- `git diff --check`: passed, with existing CRLF conversion warnings only.
- Route validation smoke check: passed for valid route, invented place rejection, missing `placeId` rejection, and duplicate same-day `visitOrder` rejection.
- 390px browser QA:
  - `/explore/map`: map pins are 44x44px, no horizontal overflow, no Vite overlay.
  - `/community`: feed action buttons are 44px high, no horizontal overflow, no Vite overlay.

## 2026-05-21 Prototype-aligned interaction fix pass

### Task chosen
- Fix user-reported UI and interaction gaps against the archived Figma Make prototype:
  - AI Result top card is too dark.
  - Regenerate button is too weak.
  - My Page stat cards should route to the matching saved-course/posts/completed-visit screens.
  - Saved/popular course cards should open the result screen.
  - Content/place likes should follow the prototype top-right heart pattern.
  - Empty comment/empty list cards should feel less heavy.

### Prototype references inspected
- `archive/reference-material/prototype/src/app/components/screens/AIResult.tsx`
- `archive/reference-material/prototype/src/app/components/screens/MyPage.tsx`
- `archive/reference-material/prototype/src/app/components/screens/MyActivitySavedRoutes.tsx`
- `archive/reference-material/prototype/src/app/components/screens/ExploreContentDetail.tsx`
- `archive/reference-material/prototype/src/app/components/screens/ExplorePlaceDetail.tsx`
- `archive/reference-material/prototype/src/app/components/screens/CommunityPostDetail.tsx`
- `archive/reference-material/prototype/src/app/contexts/AppStateContext.tsx`

### Changes made
- Updated `frontend/src/components/layout/TopNav.vue` and `frontend/src/components/layout/AppLayout.vue` to support a named top-action slot while preserving default notification/settings actions.
- Updated content/place detail screens with prototype-style top-right heart buttons.
- Updated `frontend/src/stores/uiStore.js` with `openCourse(course)`, converting saved/template course cards into AI Result-compatible route data without changing localStorage keys.
- Updated Home, My Page, and SimpleList saved-route/course lists so course cards open AI Result.
- Updated My Page stat buttons to navigate to saved routes, posts, and completed visits.
- Lightened AI Result hero styling from a dark editorial block to a white/soft-gray card and strengthened the regenerate button.
- Simplified empty states and made comment empty states less card-like.

### Verification result
- `npm run lint`: passed.
- `npm run build`: sandbox run failed with esbuild `spawn EPERM`; approved external execution passed. Vite still reports the existing large chunk warning.
- `git diff --check`: passed before this progress update, with existing CRLF conversion warnings only.
- Route validation smoke check: passed for valid route, invented place rejection, missing `placeId` rejection, and duplicate same-day `visitOrder` rejection.
- 390px browser QA:
  - `/ai/result`: no Vite overlay, no horizontal overflow, regenerate button has visible blue-tinted background.
  - `/my`: stat buttons render as 58px-high tappable controls.
  - `/contents/C_DRAMA_001` and `/places/P_0001`: top-right heart buttons render at 44x44px.
  - `/community/post/seed-post-1`: empty comment state is transparent/no-border instead of a heavy card.

## 2026-05-21 Saved course detail/edit flow fix

### Task chosen
- Fix the user-reported My Page saved-course issue:
  - My Page saved course cards should open a real course detail/manage screen.
  - Saved courses should be editable from inside the course.
  - Prototype interaction flow should guide the implementation.

### Agents used
- Prototype Flow Analyst inspected the archived Figma Make prototype screens:
  - `MyPage.tsx`
  - `MyActivitySavedRoutes.tsx`
  - `MyCourseManage.tsx`
  - `MyCourseEdit.tsx`
  - `CourseAddPlace.tsx`
  - `AIResult.tsx`
  - `AppStateContext.tsx`
- Current Vue Flow Analyst inspected router, My Page, list, AI Result, store, validation, and course components.
- QA/Risk Analyst inspected generated-route coupling, localStorage risks, route validation risks, and required navigation checks.

### Root cause
- Saved course cards were calling `uiStore.openCourse(course)` and navigating to `/ai/result`.
- `/ai/result` reads only `uiStore.generatedRoute`, so saved-course detail state was mixed with the latest AI generated result state.
- There was no routeId-based course detail route and no saved-course edit persistence path.

### Changes made
- Added `frontend/src/views/CourseDetailView.vue`:
  - Opens a route by `routeId`.
  - Supports saved course detail and popular/template course detail.
  - Shows map preview, day tabs, timeline, trust/validation panel, share, save, edit, AI-result view, and delete actions where appropriate.
- Added `frontend/src/views/CourseEditView.vue`:
  - Allows editing saved course stop time, reason, travel tip, order, and removing stops.
  - Allows adding only verified candidate places that are not already in the route.
  - Saves edits back to the same saved route without creating duplicates.
- Updated `frontend/src/stores/uiStore.js`:
  - Added `resolveCourseDetail(routeId)`.
  - Added `updateSavedCourse(route)`.
  - Added `deleteSavedCourse(routeId)`.
  - Strengthened course normalization so saved/template routes get candidate places, day/order normalization, and route validation.
  - Kept the existing `scenik-vue-mvp-state` localStorage key.
- Updated `frontend/src/router/index.js`:
  - Added `/ai/courses/:routeId`.
  - Replaced `/my/course-manage` shell list reuse with routeId-based course detail.
  - Added `/my/course-manage/:routeId/edit` for saved-course editing.
- Updated My Page, SimpleList, and Home course-card click handlers:
  - My Page and saved route lists now open `/my/course-manage/:routeId`.
  - Home/popular routes now open `/ai/courses/:routeId`.
- Added `frontend/scripts/check-course-flow.js`:
  - Locks down routeId detail lookup, valid candidate-place route resolution, edit persistence, routeId preservation, no duplicate saved route, and edited-route validation.
- Updated `frontend/src/styles.css` with course detail/edit layout classes.

### Prototype reuse
- Reused the prototype flow:
  - My Page saved routes -> course manage/detail.
  - Course manage -> edit.
  - Day tabs and itinerary stop editing.
  - Share/delete/manage actions.
- Changed from prototype:
  - Prototype uses static course arrays and has incomplete persistence in MyCourseEdit.
  - Vue implementation persists edits through Pinia/localStorage and validates against candidate places.
  - Drag/drop was simplified to explicit up/down buttons for safer mobile MVP behavior.
- Intentionally not reused:
  - React/TSX implementation, prototype tech stack, static-only delete behavior, and non-persisted edit state.

### Verification result
- `node scripts/check-course-flow.js`: initially failed because `resolveCourseDetail` did not exist, then passed after implementation.
- `npm run lint`: passed.
- `npm run build`: sandbox run failed with esbuild `spawn EPERM`; approved external execution passed after the latest Vue template changes. Existing large chunk warning remains.
- `git diff --check`: passed, with existing CRLF conversion warnings only.
- Route validation smoke check: passed for valid route, invented place rejection, missing `placeId` rejection, and duplicate same-day `visitOrder` rejection.
- 390px browser QA:
  - `/ai/courses/csv-route-1`: course detail renders, map/timeline render, no Vite overlay, no horizontal overflow.
  - `/my/course-manage/csv-route-1/edit`: edit screen renders after saving the template course, candidate panel and sticky save render, no Vite overlay, no horizontal overflow.
  - Edit save returns to `/my/course-manage/csv-route-1` and shows the course detail/edit button.

### Review
- P0: fixed missing course detail route, saved-course edit persistence, and route validation on saved/template course resolution.
- P1: fixed `/ai/result` overuse for saved course navigation. Remaining user-specific saved course isolation is still localStorage/mock-level and should be revisited with real auth/API.
- P2: drag/drop from the prototype was replaced with up/down ordering buttons; full drag/drop can be added later if needed.

## 2026-05-21 AI edit, course detail edit, and community polish pass

### Task chosen
- Respond to the latest UI/UX feedback:
  - AI Course result and course detail must both support editing.
  - Course detail cards/buttons need horizontal breathing room.
  - Search magnifier icon was too small.
  - Community UI and empty comment state felt awkward.
  - Prototype behavior should guide the edit and community interaction model.

### Agents used
- Prototype behavior analyst compared the Vue app against the archived prototype flow:
  - `AIResult.tsx`
  - `AIEdit.tsx`
  - `MyCourseManage.tsx`
  - `MyCourseEdit.tsx`
  - `CourseAddPlace.tsx`
  - community feed/detail/write screens
- UI/UX specialist identified the highest-impact polish items:
  - prototype-style editable itinerary cards
  - visible AI result edit action
  - larger search icon
  - improved course detail padding
  - calmer community feed/actions/empty comment state
- QA specialist identified behavior risks:
  - `/ai/edit` must update `generatedRoute`, not conditions state
  - saved-course edit must keep routeId and route validation
  - invalid community post IDs should not fall back to the first post
  - route validation should fail gracefully if candidate places are missing

### Changes made
- Updated `frontend/src/views/AiResultView.vue`:
  - Added visible `코스 편집` action.
  - Keeps save/share/regenerate/community actions.
  - Regenerate button is now full-width and visually stronger.
- Updated `frontend/src/views/CourseDetailView.vue`:
  - Saved courses route to `/my/course-manage/:routeId/edit`.
  - Popular/template course details can be opened into `/ai/edit` by first setting the course as the current generated route.
  - Share/delete/save/view-as-result behavior remains intact.
- Rebuilt `frontend/src/views/CourseEditView.vue`:
  - One editor now supports both AI result editing and saved-course editing.
  - Uses prototype-like editable itinerary cards with time pill, reorder controls, remove action, and expandable reason/tip fields.
  - Adds only verified candidate places and persists through `updateGeneratedRoute` or `updateSavedCourse`.
- Updated `frontend/src/stores/uiStore.js`:
  - Added `updateGeneratedRoute(route)` for AI result edit persistence.
- Updated `frontend/src/router/index.js`:
  - `/ai/edit` now opens the actual course editor.
- Updated `frontend/src/views/CommunityView.vue`:
  - Cleaned board/feed/detail/write UI copy and structure.
  - Added board chevrons, feed summary, clearer action pills, and a lighter empty comment note.
  - Invalid post URLs now show an empty state instead of leaking the first post.
- Updated `frontend/src/components/common/SearchBar.vue`:
  - Replaced the tiny text magnifier with a 22px inline SVG icon.
- Updated `frontend/src/styles.css`:
  - Added course detail/edit screen horizontal padding.
  - Added editable itinerary card styles.
  - Improved search icon, community feed, board row, action row, and empty comment styles.
- Updated `frontend/src/adapters/routeValidation.js`:
  - Candidate list validation no longer throws when `candidatePlaces` is missing; it returns validation errors.
- Updated `frontend/scripts/check-course-flow.js`:
  - Added generated route edit persistence and missing candidate list smoke coverage.

### Prototype reuse
- Reused:
  - AI result -> edit flow.
  - My course detail -> edit flow.
  - Time-pill itinerary stop cards.
  - Stop removal, order adjustment, and expandable edit detail.
  - Board/feed/detail/comment community hierarchy.
- Changed from prototype:
  - Drag/drop is represented by explicit up/down controls for stable mobile MVP behavior.
  - Vue implementation keeps placeId/candidate-place validation and localStorage persistence.
  - Community post relationships remain mock/localStorage-level.
- Intentionally not reused:
  - React/TSX stack, prototype-only static state, unvalidated itinerary mutations.

### Verification result
- `npm run lint`: passed.
- `node scripts/check-course-flow.js`: passed.
- Route validation smoke check: passed for valid route, invented place rejection, missing `placeId` rejection, duplicate same-day order rejection.
- `npm run build`: sandbox build failed with esbuild `spawn EPERM`; approved external execution passed. Existing large chunk warning remains.
- `git diff --check`: passed, with CRLF conversion warnings only.
- 390px browser QA:
  - `/ai/result`: result renders with `코스 편집`, visible full-width regenerate button, and no horizontal overflow.
  - `/ai/courses/csv-route-1`: course detail renders with horizontal padding, edit button, map/timeline, and no horizontal overflow.
  - `/my/course-manage/csv-route-1/edit`: editor opens from course detail, renders editable cards and sticky save button, no horizontal overflow.
  - `/community`: feed cards, feed summary, FAB, 22px search icon, and no horizontal overflow.
  - `/community/boards`: 5 board rows and 5 chevrons render.
  - `/community/post/not-found`: shows missing-post empty state and no longer leaks the first post.

### Review
- P0: no remaining build, route validation, candidate-place, or navigation blocker found in this pass.
- P1: fixed AI result edit, course detail edit, community fallback, search icon, and course detail padding. Full drag/drop remains intentionally deferred.
- P2: community visual polish can continue with richer image thumbnails and post-detail gallery later.

## 2026-05-21 Prototype-style drag editing and community refinement

### Task chosen
- Refine the previous course editor and community polish based on new user feedback:
  - Replace up/down reorder controls with left-handle drag reorder.
  - Show all Day sections on the course edit screen at once.
  - Keep editing simple: time only, not recommendation reason/travel tip.
  - Make Home horizontal rails draggable with a mouse.
  - Reduce awkward community placeholder/card UI.

### Agents used
- Prototype behavior agent inspected:
  - `AIEdit.tsx`
  - `MyCourseEdit.tsx`
  - `MyCourseManage.tsx`
  - `CommunityFeed.tsx`
  - `CommunityPostDetail.tsx`
  - `CommunityBoardList.tsx`
  - `CommunityDramaBoard.tsx`
  - `CommunityWrite.tsx`
- UI/UX agent reviewed:
  - `CourseEditView.vue`
  - `CommunityView.vue`
  - `HomeView.vue`
  - relevant CSS selectors.
- QA agent reviewed:
  - route validation risks
  - drag reorder persistence risks
  - Home rail drag click-suppression risks
  - auth-required community action risks.

### Changes made
- Added `frontend/src/components/common/DraggableRail.vue`:
  - Enables pointer-based mouse/touch drag scrolling for horizontal rails.
  - Suppresses accidental link clicks after a drag gesture.
- Updated `frontend/src/views/HomeView.vue`:
  - Wrapped recent place and poster rails with `DraggableRail`.
- Rebuilt `frontend/src/views/CourseEditView.vue`:
  - Removed Day tab filtering from edit mode.
  - Shows every Day section stacked in one screen.
  - Left drag handle now reorders stops with Pointer Events.
  - Reordering can move a stop within or across Day sections while preserving validation-safe renumbering.
  - Removed up/down buttons.
  - Removed editable recommendation reason/travel tip inputs.
  - Time is edited through a compact time pill/menu only.
  - Save still routes through `updateGeneratedRoute` or `updateSavedCourse` so route validation and persistence remain intact.
- Updated `frontend/src/views/CommunityView.vue`:
  - Reworked feed cards into route-linked story cards with compact route thumbnails.
  - Removed duplicate feed-summary write link and kept the FAB.
  - Added a simple post gallery treatment for detail view.
  - Kept store-backed `createPost`, `togglePostLike`, and `addComment`.
- Updated `frontend/src/styles.css`:
  - Added draggable rail states.
  - Added all-day course editor sections, drag handle, drag-over, time menu, and day candidate row styles.
  - Refined community route-story cards, action rows, gallery placeholders, and compact feed layout.

### Prototype reuse
- Reused:
  - All Day sections visible in edit mode.
  - Left grip/handle based reorder.
  - Time pill/menu editing.
  - Community feed/card/detail hierarchy.
- Changed from prototype:
  - Used Pointer Events instead of React/native `draggable` because automated browser QA showed native drag was not reliable enough in this app context.
  - Reason/travel tip remain read-only to protect AI recommendation context.
  - Drag movement preserves sceniK route validation and localStorage persistence.
- Intentionally not reused:
  - React stack, animation library, unvalidated static state, and prototype-only local component state for community actions.

### Verification result
- `npm run lint`: passed.
- `node scripts/check-course-flow.js`: passed.
- `npm run build`: passed with approved external execution. Sandbox build still fails with esbuild `spawn EPERM`; existing large chunk warning remains.
- `git diff --check`: passed with CRLF conversion warnings only.
- 390px browser QA:
  - `/my/course-manage/csv-route-1/edit`: 2 Day sections, 4 editable cards, 4 drag handles, 0 up/down buttons, 0 reason text inputs, no horizontal overflow.
  - Dragging the first stop handle below the second stop changed order from `행리단길, 행궁동 왕의 골목` to `행궁동 왕의 골목, 행리단길`.
  - `/home`: poster rail has horizontal overflow and mouse drag changed `scrollLeft` from `0` to `162` without route navigation.
  - `/community`: 4 route story cards, 4 compact thumbnails, no old `.feed-image`, no duplicate write link, no horizontal overflow.

### Review
- P0: no remaining route validation, generated/saved persistence, or navigation blocker found.
- P1: fixed all-day edit layout, actual drag-handle reorder, time-only editing, Home mouse drag rail, and community card awkwardness.
- P2: future work can add richer real community photos and keyboard-accessible reorder alternatives.

## 2026-05-21 Community Feed Polish and Scrap Flow

### Task chosen
- Improve community card/button quality, board category presentation, action alignment, liked state visibility, scrap persistence, and share behavior based on prototype screenshots.

### Agents used
- Prototype behavior agent reviewed community feed/detail/board/share patterns from the archived prototype.
- UI/UX agent reviewed feed card hierarchy, board icons, tab segmentation, and action-row alignment.
- QA agent reviewed localStorage persistence, auth-required risks, RouterLink/action propagation, and share API fallback risks.

### Changes made
- Rebuilt `frontend/src/views/CommunityView.vue` community copy and structure:
  - Board tabs now show clearer `전체 / 게시판` divided cells.
  - Board rows use category icons instead of DR/MV/ID/TV/KR text-only blocks.
  - Feed and detail actions show active filled hearts when liked.
  - Feed save button now reflects real saved/scrapped state.
  - Detail share button opens a bottom sheet with native share and link-copy fallback.
  - Comment empty and comment row UI were tightened so the text no longer feels oversized or awkward.
- Updated `frontend/src/stores/uiStore.js`:
  - Added auth-required `togglePostScrap(postId)`.
  - New posts initialize `scrappedBy: []`.
  - Scrap state is persisted through the existing app localStorage state.
- Updated My Page list handling:
  - `/my/scraps` now displays actual scrapped community posts instead of falling through to notices.
- Updated `frontend/src/styles.css`:
  - More explicit community segmented tabs.
  - Polished route-story cards, thumbnails, board icon plates, action buttons, comment rows, and share sheet.

### Verification result
- `npm run lint`: passed.
- `node scripts/check-course-flow.js`: passed. Added smoke coverage for missing `placeId`, duplicate same-day `visitOrder`, and saved route `budgetMin`/`budgetMax` persistence.
- `npm run build`: sandbox run failed with esbuild `spawn EPERM`; approved external execution passed. Existing large chunk warning remains.
- `git diff --check`: passed with CRLF conversion warnings only.

### Follow-up fixes from agent review
- Fixed My Page and SimpleList post ownership to use stable `userId` first, with nickname fallback for older localStorage records.
- Fixed comment activity matching to use the same `userId` fallback logic across My Page and detail lists.
- Preserved `budgetMin` and `budgetMax` on generated AI routes.
- Improved the budget range control with a visible selected range track and larger slider thumb affordance.
- Added disabled-state helper copy to AI Course and Community write CTAs.
- Added board abbreviations to the Community full writing page.
- Reduced toast button-like treatment and moved it above sticky action areas.

## 2026-05-21 My Page Tabs, Budget Range, AI Result Menu Polish

### Task chosen
- Reduce My Page tab visual noise.
- Replace the budget UI with a single dual-thumb range treatment.
- Reposition the AI result overflow menu so it opens directly under the top-right action.

### Changes made
- Updated `frontend/src/views/MyPageView.vue`:
  - Removed tab sublabels/count text under `내 활동`, `방문 관리`, `게시글`.
  - Replaced the post-section menu glyphs with inline SVG icons for written posts, comments, and scraps.
- Updated `frontend/src/styles/profile-utility-overlays.css`:
  - Removed rigid divider lines between My Page tabs.
  - Changed tabs into a softer pill segmented control.
  - Added SVG icon sizing/stroke rules for My Page menu icons.
- Updated `frontend/src/views/CourseConditionsView.vue` and `frontend/src/styles/ai-course.css`:
  - Rebuilt budget selection as one visual bar with two draggable range thumbs for min/max.
  - Kept `budgetMin`, `budgetMax`, and formatted `budget` persistence unchanged.
- Updated `frontend/src/styles/ai-course.css`:
  - Moved the AI result action menu closer to the top-right menu button and softened its shadow/radius.

### Verification result
- `npm run lint`: passed.
- `node scripts/check-course-flow.js`: passed.
- `npm run build`: sandbox run failed with esbuild `spawn EPERM`; approved external execution passed. Existing large chunk warning remains.
- `git diff --check`: passed with CRLF conversion warnings only.

## 2026-05-21 Budget Control, Candidate Places, My Page Icon Polish

### Task chosen
- Fix the awkward budget range control.
- Make AI Course place-pool wording understandable and increase the available real-place pool without weakening validation.
- Improve My Page activity icons and make the three tabs slightly more distinguishable.

### Changes made
- Updated `frontend/src/views/CourseConditionsView.vue`:
  - Renamed the confusing `검증된 후보 장소` section to `AI 코스에 사용할 장소`.
  - Added helper copy explaining direct content-linked places plus same-region verified fallback places.
  - Expanded the displayed candidate pool with same-region verified places when direct content-linked places are too few.
  - Kept the generated route source limited to actual verified place records.
- Updated `frontend/src/views/AiLoadingView.vue`:
  - Route generation now uses direct content-linked places plus same-region verified fallback places.
  - Filters candidate collection to verified places only.
  - Reworded loading/result trust copy away from confusing `후보` language.
- Updated `frontend/src/views/MyPageView.vue`:
  - Replaced liked content, liked places, and saved AI course menu icons with cleaner inline SVG icons.
- Updated `frontend/src/styles/ai-course.css`:
  - Simplified the budget dual range into a cleaner single-track visual with smaller blue handles.
  - Added helper text styling for the AI course place-pool explanation.
- Updated `frontend/src/styles/profile-utility-overlays.css`:
  - Added a subtle border/background to the My Page tab segmented control so the three tabs are distinguishable without harsh divider lines.

### Verification result
- `npm run lint`: passed.
- `node scripts/check-course-flow.js`: passed.
- `npm run build`: sandbox run failed with esbuild `spawn EPERM`; approved external execution passed. Existing large chunk warning remains.
- `git diff --check`: passed with CRLF conversion warnings only.
- Community source smoke check: passed for filled heart rendering, scrap action, share sheet, native share/clipboard fallback, and My Page scraps list.
- Community store smoke check: passed for logged-in like, logged-in scrap save/unsave, and logged-out auth-required handling.
- Browser screenshot QA was not run in this pass because Playwright is not installed in the frontend workspace and no browser automation tool is exposed in this session.

### Review
- P0: no remaining community auth-required, persistence, build, or route-validation blocker found.
- P1: fixed weak board icons, unclear tabs, misaligned action row, missing filled liked state, missing scrap persistence, and placeholder share behavior.
- P2: future polish can replace CSS/emoji category icons with real local board illustration assets if a final visual asset set is provided.

## 2026-05-21 Logo Asset Swap

### Task chosen
- Replace the visible sceniK wordmark treatment with the user-provided logo image while preserving app behavior and routing.

### Changes made
- Copied the provided logo image to `frontend/public/images/scenik-logo.png`.
- Updated `frontend/src/components/layout/TopNav.vue`:
  - Header brand now uses the new image asset with accessible `sceniK` text.
- Updated `frontend/src/views/SplashView.vue`:
  - Splash mark now uses the new logo asset instead of the old generated icon/text lockup.
- Updated `frontend/src/styles.css`:
  - Added reusable `.sr-only`.
  - Added image-based header and splash logo treatments using the copied local asset.

### Verification result
- `npm run lint`: passed.
- Source logo check: passed for `TopNav.vue`, `SplashView.vue`, `styles.css`, and copied `frontend/public/images/scenik-logo.png`.
- `git diff --check`: passed with CRLF conversion warnings only.
- `npm run build`: sandbox run failed with esbuild `spawn EPERM`; approved external execution passed. Existing large chunk warning remains.

## 2026-05-21 Logo Fit Correction

### Task chosen
- Fix the header logo looking like a cropped black capsule after applying the wide generated logo image.

### Changes made
- Added `frontend/public/images/scenik-logo-ui.svg`:
  - Clean UI-ready sceniK wordmark using the user-provided logo's blue/yellow/green K-film motif.
  - Transparent background so it fits the white mobile app header.
- Updated `frontend/src/components/layout/TopNav.vue`:
  - Header brand now renders the clean SVG image instead of a cropped background image.
- Updated `frontend/src/views/SplashView.vue`:
  - Splash logo now uses the same clean SVG mark.
- Updated `frontend/src/styles.css`:
  - Removed dark background/border treatment from `.brand-mark` and `.splash-logo`.
  - Added image-fit sizing so the logo does not crop or look compressed.

### Verification result
- `npm run lint`: passed.
- Source logo SVG check: passed for `scenik-logo-ui.svg`, `TopNav.vue`, `SplashView.vue`, and `styles.css`.
- `git diff --check`: passed with CRLF conversion warnings only.
- `npm run build`: sandbox run failed with esbuild `spawn EPERM`; approved external execution passed. Existing large chunk warning remains.

## 2026-05-21 Community Detail Action Polish

### Task chosen
- Fix awkward like/comment buttons and the linked AI course card on the community post detail screen.

### Changes made
- Updated `frontend/src/views/CommunityView.vue`:
  - Converted the linked AI course block into a full-card CTA.
  - Added clearer course title/meta copy and a compact `보기` pill.
  - Changed the comment count from a mismatched gray span into a same-height link button.
- Updated `frontend/src/styles.css`:
  - Fixed `.detail-actions span` over-targeting so the heart icon is no longer styled as a large pill.
  - Unified detail action button sizing, borders, and active color.
  - Refined the linked course card with a lighter surface, blue-green AI mark, and less bulky CTA.

### Verification result
- `npm run lint`: passed.
- Community detail source check: passed for full-card linked course, comment anchor, direct-child action span selector, and linked course CTA styling.
- `git diff --check`: passed with CRLF conversion warnings only.
- `npm run build`: sandbox run failed with esbuild `spawn EPERM`; approved external execution passed. Existing large chunk warning remains.

## 2026-05-21 My Page Section Clarity and Profile Edit

### Task chosen
- Make My Page `내 코스`, `방문 관리`, and `좋아요` sections easier to distinguish.
- Implement a real profile edit screen based on the prototype flow.

### Prototype references inspected
- `archive/reference-material/prototype/src/app/components/screens/MyPage.tsx`
- `archive/reference-material/prototype/src/app/components/screens/MyProfileEdit.tsx`
- `archive/reference-material/prototype/src/imports/pasted_text/my-page-wireframes.md`

### Changes made
- Updated `frontend/src/views/MyPageView.vue`:
  - Replaced the weak 3-way mode toggle with large section cards for `내 코스`, `방문 관리`, and `좋아요`.
  - Each section now shows an icon, count, and short description.
  - Visits are separated into `방문 예정` and `방문 완료`.
  - Likes are separated into `좋아요한 콘텐츠` and `좋아요한 장소`.
  - Profile card now has a visible profile edit button and avatar edit affordance.
- Added `frontend/src/views/MyProfileEditView.vue`:
  - Editable nickname and introduction.
  - Editable content, region, and theme preferences.
  - Save action persists to the Pinia/localStorage MVP state.
  - Logged-out users see a login-required state.
- Updated `frontend/src/stores/uiStore.js`:
  - Added `updateProfile()` with auth-required protection and nickname validation.
  - Updates both `currentUser` and the stored `users` record while preserving the existing localStorage key.
- Updated `frontend/src/router/index.js`:
  - `/my/profile-edit` now opens the new profile edit screen instead of the onboarding preference screen.
- Updated `frontend/src/styles.css`:
  - Added profile edit, avatar edit badge, My Page section card tabs, section headers, and top save button styles.

### Verification result
- `npm run lint`: passed.
- Source check: passed for profile edit route import, profile edit view, `updateProfile`, My Page section tabs, and profile edit CSS.
- `node scripts/check-course-flow.js`: passed.
- Profile edit smoke check: passed for current user update, users array update, password preservation, and localStorage persistence.
- `git diff --check`: passed with CRLF conversion warnings only.
- `npm run build`: sandbox run failed with esbuild `spawn EPERM`; approved external execution passed. Existing large chunk warning remains.

### Review
- P0: no auth/session, build, route validation, or persistence blocker found.
- P1: fixed unclear My Page section separation and missing real profile edit flow.
- P2: profile image upload remains MVP shell-only and uses the default avatar.

## 2026-05-21 Logo Cache Fix and Korea Icon Replacement

### Task chosen
- Replace the app logo with the newly supplied SVG through a cache-safe asset path.
- Replace the My Page `내 코스` world-map icon with a Korean peninsula silhouette.

### Root cause
- The previous logo update reused `/images/scenik-logo-ui.svg`, so a running browser could continue showing the cached old logo.
- The previous My Page icon was a CSS polygon approximation, not a recognizable Korean peninsula silhouette.

### Changes made
- Added `frontend/public/images/scenik-logo-brand-20260521.svg` from the supplied logo SVG and pointed both the top nav and splash screen at the new filename.
- Removed the obsolete `frontend/public/images/scenik-logo-ui.svg` reference from active code.
- Added `frontend/public/images/korea-peninsula.svg` and switched the My Page `내 코스` tab icon to use it as a CSS mask.

### Verification result
- `npm run lint`: passed.
- `node scripts/check-course-flow.js`: passed.
- `git diff --check`: passed with CRLF conversion warnings only.
- `npm run build`: approved external execution passed after the latest Vue template changes. Existing large chunk warning remains.
- Build output check: `dist/images/scenik-logo-brand-20260521.svg` and `dist/images/korea-peninsula.svg` both exist.

## 2026-05-21 Logo Size and My Course Emoji Adjustment

### Task chosen
- Slightly reduce the displayed sceniK logo size.
- Replace the imperfect Korean peninsula custom icon with a clearer course-related emoji.

### Changes made
- Reduced top-nav logo width from `124px` to `112px`.
- Reduced splash logo max sizing from `min(82%, 280px)` to `min(78%, 260px)`.
- Replaced the My Page `내 코스` custom SVG icon with `🧭`.
- Removed the unused `korea-peninsula.svg` asset and its CSS mask rule.

### Verification result
- `npm run lint`: passed.
- `node scripts/check-course-flow.js`: passed.
- `git diff --check`: passed with CRLF conversion warnings only.
- `npm run build`: sandbox run failed with esbuild `spawn EPERM`; approved external execution passed. Existing large chunk warning remains.

## 2026-05-21 Prototype Gap Agent Review and Saved Course UX Fixes

### Task chosen
- Use PM, prototype logic, UI/UX, and QA agent passes to find missing prototype logic/pages.
- Fix the most immediate saved course and course detail issues reported by the user.

### Agents used
- Product/PM agent: reviewed prototype/current route gaps and prioritized missing MVP behavior.
- Prototype interaction analyst: compared prototype course/community/My Page flows with current Vue routes.
- UI/UX designer: reviewed awkward visual states, saved course delete affordance, and course not-found screen.
- QA agent: checked auth-required, route validation, saved route, and community persistence risks.

### Prototype references inspected by agents
- `archive/reference-material/prototype/src/app/App.tsx`
- `archive/reference-material/prototype/src/app/components/screens/MyActivitySavedRoutes.tsx`
- `archive/reference-material/prototype/src/app/components/screens/MyCourseManage.tsx`
- `archive/reference-material/prototype/src/app/components/screens/MyCourseEdit.tsx`
- `archive/reference-material/prototype/src/app/components/screens/MyComments.tsx`
- `archive/reference-material/prototype/src/app/components/screens/MyScraps.tsx`
- `archive/reference-material/prototype/src/app/components/screens/CommunityFeed.tsx`
- `archive/reference-material/prototype/src/app/components/screens/CommunityPostDetail.tsx`
- `archive/reference-material/prototype/src/app/components/screens/AISaveShare.tsx`

### Changes made
- Added reusable project agents:
  - `.codex/agents/product-manager.toml`
  - `.codex/agents/prototype-logic-analyst.toml`
  - `.codex/agents/ui-ux-designer.toml`
  - `.codex/agents/qa-engineer.toml`
- Updated `frontend/src/views/CourseDetailView.vue`:
  - Renamed the header title to `코스 상세`.
  - Replaced the awkward not-found empty state with a proper course empty panel and two CTAs.
  - Replaced weak delete text with a visible danger button and a prototype-style delete bottom sheet.
  - Prevented public `/ai/courses/:routeId` details from showing saved-course delete controls.
  - Made route sharing auth-required.
- Updated `frontend/src/views/SimpleListView.vue`:
  - Added real `/my/comments` handling instead of falling through to notices.
  - Added visible delete buttons on saved course list rows.
- Updated `frontend/src/stores/uiStore.js`:
  - `saveCourse()` now validates routes before persistence.
  - `createPost()` now returns the created post or `null` when blocked by auth.
  - Community posts can preserve `routeId`.
  - Comments now preserve `id`, `postId`, `userId`, and date.
- Updated `frontend/src/views/AiResultView.vue`:
  - Community posting only redirects after the post is actually created.
  - Posting now preserves the generated route ID.
  - Route sharing is auth-required.
- Updated `frontend/src/views/CommunityView.vue`:
  - Linked AI course cards now navigate to the post's `routeId` when available.
- Updated `frontend/src/data/mockData.js`:
  - Seed community posts now include representative `routeId` values.
- Updated `frontend/scripts/check-course-flow.js`:
  - Added smoke checks for invalid route save rejection, auth-blocked community posting, route-linked posts, and comment metadata.

### Verification result
- Agent TOML syntax check with Python `tomllib`: passed.
- `npm run lint`: passed.
- `node scripts/check-course-flow.js`: passed.
- `git diff --check`: passed with CRLF conversion warnings only.
- `npm run build`: sandbox run failed with esbuild `spawn EPERM`; approved external execution passed. Existing large chunk warning remains.

### Remaining prototype gaps
- `/ai/save-share` still reuses the AI result screen rather than a dedicated prototype-style save/share screen.
- Community edit/delete and advanced filter/search flows are still reduced MVP versions.
- Saved course gallery/list toggle from the prototype is not implemented yet.

## 2026-05-21 My Page Post List and Course Card Polish

### Task chosen
- Improve the awkward My Page written/scrapped/commented post list presentation.
- Remove saved-course delete controls from the course detail page.
- Replace numbered course thumbnail placeholders with a cleaner map-route visual.

### Changes made
- Updated `frontend/src/views/SimpleListView.vue`:
  - Added a shared `activityPosts` computed list for written posts, scrapped posts, and commented posts.
  - Replaced plain `panel-card` post rows with richer My Page activity cards showing board/category, date, region, body copy, tags, engagement counts, linked-course status, and comment preview for commented posts.
- Updated `frontend/src/views/CourseDetailView.vue`:
  - Removed the saved-course delete action and delete bottom sheet from the course detail view so deletion stays a list-management action instead of appearing inside the course reading/editing surface.
- Updated `frontend/src/components/course/CourseCard.vue`:
  - Replaced numbered `1/2/3/4` mini-pin thumbnails with a compact route-line visual and region badge.
- Updated `frontend/src/styles.css`:
  - Added reusable My Page post-card styles.
  - Added clean route thumbnail styling for course cards while preserving existing map pin styling.

### Verification result
- `npm run lint`: passed.
- `node scripts/check-course-flow.js`: passed.
- `git diff --check`: passed with CRLF conversion warnings only.
- `npm run build`: approved external execution passed after the latest Vue template changes. Existing large chunk warning remains.

## 2026-05-21 Saved Course Delete Placement Correction

### Task chosen
- Correct the saved-course deletion UX: deletion belongs in the saved course detail screen, not in the saved course list.

### Changes made
- Updated `frontend/src/views/SimpleListView.vue`:
  - Removed the delete button and delete handler from the saved course list.
  - Saved course rows now only open the course detail/manage screen.
- Updated `frontend/src/views/CourseDetailView.vue`:
  - Restored the saved-course delete action in the course detail screen only for saved courses.
  - Added a confirmation bottom sheet before deleting.
  - After deletion, the user is returned to the saved course list.
- Updated `frontend/src/styles.css`:
  - Removed the now-unused saved-course list delete button style.

### Verification result
- `npm run lint`: passed.
- `node scripts/check-course-flow.js`: passed.
- `git diff --check`: passed with CRLF conversion warnings only.
- `npm run build`: approved external execution passed. Existing large chunk warning remains.

## 2026-05-21 Saved Course Edit Candidate Place Fix

### Task chosen
- Make saved course editing support adding verified places, matching the AI result edit behavior.

### Root cause
- `CourseEditView.vue` already had a per-Day `추가` candidate row.
- Saved course normalization rebuilt `candidatePlaces` only from places already present in the itinerary, so every candidate was considered used and the add row had no available places.

### Changes made
- Updated `frontend/src/stores/uiStore.js`:
  - Preserves existing `candidatePlaces` when generated AI routes are saved.
  - Rebuilds candidate pools from related content/place mappings and same-region verified places when opening saved/template course details.
  - Keeps candidate places region-safe so `validateAiRoute` still rejects out-of-region candidates.
- Updated `frontend/scripts/check-course-flow.js`:
  - Added a smoke check proving saved course details preserve unused verified candidates.
  - Added a smoke check proving a saved course can add one of those verified candidates and still pass route validation.

### Verification result
- `npm run lint`: passed.
- `node scripts/check-course-flow.js`: passed.
- `git diff --check`: passed with CRLF conversion warnings only.
- `npm run build`: approved external execution passed. Existing large chunk warning remains.

## 2026-05-21 Prototype-style AI Result and Loading Progress Polish

### Task chosen
- Match the AI course result screen to the prototype layout.
- Move save/share/edit into a top-right overflow menu.
- Add a prototype-style route share bottom sheet.
- Fix the AI loading progress bar so it starts from an empty bar.

### Prototype reference inspected
- `archive/reference-material/prototype/src/app/components/screens/AIResult.tsx`

### Changes made
- Updated `frontend/src/views/AiResultView.vue`:
  - Rebuilt the result screen as a dedicated prototype-style mobile result card with a top title bar, overflow menu, map placeholder, Day tabs, simple timeline, and bottom `완료` button.
  - Added menu actions for `저장`, `공유`, and `편집`.
  - Added share bottom sheet options for 카카오톡, 인스타그램, 메시지, 링크 복사, 마이페이지 저장, and 커뮤니티 게시.
  - Preserved auth-required behavior for sharing/saving/posting.
- Updated `frontend/src/views/AiLoadingView.vue`:
  - Generated AI route titles now include content title, region, style, and duration label.
  - Generated routes now preserve companion metadata for result subtitles.
- Updated `frontend/src/styles.css`:
  - Added prototype-style AI result layout/menu/share sheet/timeline styles.
  - Updated loading progress animation to start at 0% and fill forward instead of starting half-filled.

### Verification result
- `npm run lint`: passed.
- `node scripts/check-course-flow.js`: passed.
- `git diff --check`: passed with CRLF conversion warnings only.
- `npm run build`: approved external execution passed. Existing large chunk warning remains.

## 2026-05-21 AI Result Color Correction

### Task chosen
- Keep the prototype-inspired AI result structure and menu/share behavior, but restore the existing sceniK app color system.

### Changes made
- Updated `frontend/src/styles.css`:
  - Replaced hard black/gray prototype colors in the AI result screen with existing app tokens.
  - Restored primary action, active Day tab, timeline marker, tags, map card, and footer CTA to the app's blue/white visual system.
  - Softened share option colors so they fit the current sceniK palette while keeping the prototype share-sheet structure.

### Verification result
- `npm run lint`: passed.
- `node scripts/check-course-flow.js`: passed.
- `git diff --check`: passed with CRLF conversion warnings only.
- `npm run build`: approved external execution passed. Existing large chunk warning remains.

## 2026-05-21 AI Course Result and Edit UX Polish

### Task chosen
- Polish the AI result action menu, complete travel condition inputs, remove the nested-screen feeling from the result view, and move course-edit recommended places into one dedicated add section.

### Agent review summary
- Prototype review: AI result menu should follow the prototype's save/share/edit overflow pattern, travel conditions need companion and budget, and recommended places should not repeat under every Day.
- UI/UX review: the result wrapper should be a full app screen rather than a rounded card inside the screen; menu icons should use consistent SVG line icons.
- QA review: preserve `saveCourse`, `createPost`, `updateGeneratedRoute`, `updateSavedCourse`, auth-required sharing, and candidate-place-only additions.

### Changes made
- Updated `frontend/src/views/AiResultView.vue`:
  - Replaced rough menu glyphs with inline SVG icons for save, share, and edit.
  - Added `aria-expanded` and `aria-controls` to the top-right menu button.
- Updated `frontend/src/views/CourseConditionsView.vue`:
  - Added companion type and budget chip groups using the existing `uiStore.courseConditions` fields.
- Updated `frontend/src/views/CourseEditView.vue`:
  - Removed repeated per-Day candidate add rows.
  - Added a single recommended verified-place section with Day target tabs and compact candidate cards.
  - Kept additions restricted to `course.candidatePlaces`.
- Updated `frontend/src/styles.css`:
  - Removed the AI result outer card frame so the result screen no longer looks like a screen inside a screen.
  - Softened the shared sticky bottom action surface.
  - Added SVG menu icon styling and recommended-place add-section styles.

### Verification result
- `npm run lint`: passed.
- `node scripts/check-course-flow.js`: passed.
- `git diff --check`: passed with CRLF conversion warnings only.
- `npm run build`: sandbox run failed with esbuild `spawn EPERM`; approved external execution passed. Existing large chunk warning remains.

## 2026-05-21 CSS Modularization and Project Rename Prep

### Task chosen
- Rename the project folder from the old workspace name to the project name.
- Split the oversized frontend CSS file into maintainable modules without changing UI behavior.

### Agent review summary
- Refactor review recommended keeping `frontend/src/styles.css` as the import entry point because `main.js` and the frontend structure check expect it.
- QA review identified the main risk as CSS import order changes; the split therefore preserves the original selector order exactly through sequential module imports.

### Changes made
- Updated `README.md` title from the old workspace name to `sceniK`.
- Updated `docs/scenik-implementation-spec.md` with the CSS module architecture.
- Converted `frontend/src/styles.css` into an import aggregator.
- Added ordered CSS modules:
  - `frontend/src/styles/tokens.css`
  - `frontend/src/styles/base.css`
  - `frontend/src/styles/shell.css`
  - `frontend/src/styles/controls.css`
  - `frontend/src/styles/content-cards.css`
  - `frontend/src/styles/detail-map-panels.css`
  - `frontend/src/styles/ai-course.css`
  - `frontend/src/styles/course-management.css`
  - `frontend/src/styles/community.css`
  - `frontend/src/styles/profile-utility-overlays.css`

### Verification result
- CSS module line total check: 3,975 lines preserved across modules.
- `npm run lint`: passed.
- `node scripts/check-course-flow.js`: passed.
- `git diff --check`: passed with CRLF conversion warnings only.
- `npm run build`: sandbox run failed with esbuild `spawn EPERM`; approved external execution passed. Existing large chunk warning remains.
- Folder rename was later completed by the user as `scenik`; future commands should use `C:\Users\SSAFY\Downloads\scenik`.

## 2026-05-21 Functional UX Polish: Budget Range, Community Write, My Page IA

### Task chosen
- Replace the coarse AI Course budget chips with a monetary range control.
- Move community writing from an inline compose block to a dedicated full writing page.
- Re-align My Page with the prototype IA: `내 활동`, `방문 관리`, `게시글`.
- Restyle toast feedback and AI Course primary CTAs so they read as system feedback and clear actions, not ambiguous buttons.

### Agent review summary
- Prototype behavior review: budget should be an amount range, Community `+` should route to a full write page, and My Page should land on grouped activity/visit/post rows.
- UI/UX review: toast should be compact feedback with neutral surface, CTAs should include action + context, and My Page tabs should be segmented instead of oversized cards.
- QA review: preserve auth-required behavior, localStorage persistence, bottom navigation, and route validation.

### Changes made
- Updated `frontend/src/stores/uiStore.js`:
  - Added persisted `budgetMin`/`budgetMax` defaults and normalized restored budget labels.
- Updated `frontend/src/views/CourseConditionsView.vue`:
  - Replaced 3-option budget chips with min/max monetary range sliders.
  - Kept the generated route condition summary compatible through `courseConditions.budget`.
- Updated `frontend/src/views/CourseView.vue` and `frontend/src/styles/ai-course.css`:
  - Clarified AI Course CTAs with primary text and supporting context.
- Added `frontend/src/views/CommunityWriteView.vue` and updated `frontend/src/router/index.js`:
  - `/community/write` and `/community/edit` now render a full writing page.
  - Post creation still uses `uiStore.createPost`, so login-required behavior and localStorage persistence are preserved.
- Updated `frontend/src/views/MyPageView.vue`:
  - Rebuilt logged-in My Page tabs as `내 활동`, `방문 관리`, `게시글`.
  - Added direct landing rows for liked content, liked places, saved AI courses, planned/completed visits, map, written posts, commented posts, and scraps.
- Updated `frontend/src/views/SimpleListView.vue`:
  - Added `planned` list handling for `/my/visit-planned`.
- Updated CSS modules:
  - Added budget range, dedicated community write page, compact My Page tabs, My Page menu rows, visit progress, and neutral toast styling.

### Verification result
- `npm run lint`: passed.
- `node scripts/check-course-flow.js`: passed.
- `npm run build`: sandbox run failed with esbuild `spawn EPERM`; approved external execution passed. Existing large chunk warning remains.
- `git diff --check`: passed with CRLF conversion warnings only.

## 2026-05-21 Community Write and Budget Range Follow-up

### Task chosen
- Fix the AI Course budget range ceiling and polish the slider handle alignment.
- Bring Community write behavior in line with the five-board IA.
- Remove awkward community helper copy and add practical post metadata inputs.

### Changes made
- Updated `frontend/src/views/CourseConditionsView.vue`:
  - Raised the maximum AI Course budget from 1,000,000 KRW to 10,000,000 KRW.
  - Adjusted the range step to 100,000 KRW for a usable 10,000,000 KRW range.
- Updated `frontend/src/styles/ai-course.css`:
  - Lifted the budget range control slightly so the slider thumb feels visually aligned with the track.
- Updated `frontend/src/views/CommunityView.vue`:
  - Removed the feed subtitle text `검증 장소 기반 코스 후기`.
  - Added detail display for attachment metadata on posts created from the write page.
- Updated `frontend/src/views/CommunityWriteView.vue`:
  - Removed the awkward English `Community` badge from the write intro.
  - Added all five boards: drama, movie, idol, variety, and region.
  - Simplified board selection to text-only pills instead of DR/MV abbreviation badges.
  - Added board-specific prefix selection using mock content titles or regions.
  - Added photo/video attachment selection; MVP stores file metadata only because no backend upload API exists.
- Updated `frontend/src/stores/uiStore.js`:
  - Preserved `prefix` and `attachments` metadata when persisting community posts.
- Updated `frontend/src/styles/community.css`:
  - Added compact five-board write selector styling, select focus styling, and attachment metadata rows.

### Verification result
- `npm run lint`: passed.
- `node scripts/check-course-flow.js`: passed.
- `npm run build`: sandbox run failed with esbuild `spawn EPERM`; approved external execution passed. Existing large chunk warning remains.
- `git diff --check`: passed with CRLF conversion warnings only.

### Remaining notes
- Photo/video attachment is an MVP local metadata flow only. Real preview/upload requires backend storage and API support.

## 2026-05-22 Workspace Folder Rename

### Task chosen
- Acknowledge the project folder rename to `scenik` and update repository guidance references that still pointed at the old project root.

### Changes made
- Confirmed the active workspace path is now `C:\Users\SSAFY\Downloads\scenik`.
- Checked `.codex/` and `.agents/`; no old absolute workspace path was present in Codex agent config.
- Updated current implementation docs to `scenik/prototype`.
- Updated progress references for current/future workspace usage to `scenik`.

### Remaining notes
- `backend/target/**` still contains old absolute paths generated by Maven compiler metadata. This is build output, not source config, and should be regenerated by `mvn clean package` rather than hand-edited.

## 2026-05-22 CSS-built sceniK Logo Pass

### Task chosen
- Replace the active SVG logo usage with a directly rendered Vue/CSS logo inspired by the user-provided blue/red sceniK reference image.
- Make the logo background transparent so splash/onboarding and in-app header surfaces do not show mismatched white image boxes.

### Prompt-director execution prompt
- Build a shared Vue component for the sceniK logo instead of using an SVG file or image asset.
- Match the supplied reference as closely as practical in code: blue `sceni` wordmark, red-to-blue final `K`, and a hanok-roof style red accent with white rafter cuts.
- Use the same component in splash/onboarding and the app header, preserve routing and app behavior, avoid new dependencies, and validate with lint/build plus browser visual QA.

### Changes made
- Added `frontend/src/components/brand/BrandLogo.vue`.
- Updated `frontend/src/components/layout/TopNav.vue` to render the shared `BrandLogo` component instead of `/images/scenik-logo-brand-20260521.svg`.
- Updated `frontend/src/views/SplashView.vue` to render the same shared logo component.
- Updated `frontend/src/styles/shell.css` with the CSS-built wordmark, gradient `K`, red roof accent, white rafter cuts, and transparent logo background.
- Updated `frontend/scripts/check-frontend.js` so active logo surfaces must use `BrandLogo` and must not reference the previous SVG logo asset.

### Verification result
- `npm run lint`: first failed as expected before implementation because `BrandLogo.vue` was missing; passed after implementation.
- `npm run build`: sandbox run failed with esbuild `spawn EPERM`; approved external execution passed. Existing large chunk warning remains.
- Browser QA on `/` and `/home`: splash and header logos render as `.scenik-logo`, have transparent backgrounds, reference zero active `scenik-logo` image elements, reference zero old SVG logo image elements, and report no browser console errors.

### Remaining notes
- This is a CSS/code approximation of the supplied logo, not a vector trace or backend-integrated brand asset pipeline.
- Existing unused PNG/SVG logo assets remain in `frontend/public/images/` for now because removing them is unrelated cleanup and may affect prior review context.

## 2026-05-22 Designer-Agent Canvas Logo Correction

### Task chosen
- Replace the rejected CSS-shape logo with a designer-agent-guided canvas renderer that better matches the supplied sceniK reference without using an SVG file.

### Prompt-director execution prompt
- Use a design director to critique the current logo and produce a precise implementation direction.
- Implement the logo as a transparent canvas component, preserving `BrandLogo size="nav" | "splash"` and existing app routing.
- Use a bold rounded blue `sceni`, a red-to-blue gradient `K`, and a simpler hanok-roof/ribbon motif drawn with canvas curves.
- Run visual QA as a separate review agent and fix any P1 findings before finishing.

### Agents used
- `design_director`: recommended replacing CSS pseudo-element shapes with a canvas renderer because the logo needs real curves, gradients, and transparent cutouts.
- `visual_qa`: found no P0 issues, then flagged P1 clipping/narrow-screen risks and P2 focus/roof legibility polish.

### Changes made
- Rewrote `frontend/src/components/brand/BrandLogo.vue`:
  - Uses `<canvas>` instead of CSS shape markup.
  - Draws the blue wordmark, gradient `K`, and hanok-roof/ribbon motif with Canvas 2D.
  - Redraws after `document.fonts.ready`.
  - Uses the actual wrapper width for narrow-screen resizing.
  - Clamps `K` placement and shortens ribbon coordinates to prevent right-edge clipping.
- Updated `frontend/src/styles/shell.css`:
  - Removed the old CSS pseudo-element logo drawing rules.
  - Keeps only logo layout sizing and transparent canvas styling.
  - Added `brand-mark:focus-visible` outline.
- Updated `frontend/scripts/check-frontend.js`:
  - Requires `BrandLogo` to use a canvas renderer.
  - Rejects inline SVG and SVG data URLs for the active logo component.

### Verification result
- `npm run lint`: passed after the canvas implementation.
- `npm run build`: approved external execution passed. Existing large chunk warning remains.
- `git diff --check`: passed with CRLF conversion warnings only.
- Browser DOM QA on `/` and `/home`: both surfaces render a canvas logo, logo/canvas backgrounds are transparent, active logo image count is `0`, old SVG image count is `0`, horizontal overflow is `false`, and browser console error count is `0`.

### Review result
- P0: none found.
- P1: visual QA clipping and narrow-screen risks were fixed by clamping `K`, shortening the ribbon, and drawing to the actual wrapper width.
- P2: the logo is still a code-drawn approximation rather than a formal brand vector, but it is no longer the crude CSS-shape version.

### Remaining notes
- Browser screenshot capture timed out in the in-app browser, so final QA used DOM/canvas presence, transparent-background checks, overflow checks, build/lint, and agent source review.

## 2026-05-22 Logo Geometry Redesign Follow-up

### Task chosen
- Fix the user-reported remaining logo issues: the `niK` overlap impression and the strange pasted-on hanok/ribbon motif around the `K`.

### Agent review summary
- `design_director` found the core issue was coordinate structure: `K` was being pulled left by a clamp and the ribbon was drawn as a separate overlay.
- `ui_ux_designer` flagged P0/P1 risks: `sceniK` readability in the 136x42 header, `i`/`K` overlap, text-cutting compositing, over-complex nav motif, and possible title/header collision.
- A later `visual_qa` pass confirmed overlap/text-cut improved after the first rewrite, but still rejected handoff because the eave was a separate layer and title pages could conflict with the large logo.

### Changes made
- Updated `frontend/src/components/brand/BrandLogo.vue`:
  - Removed overlap-prone `fillText('K')` rendering.
  - Removed `Math.min(..., 151 * scale)` K positioning.
  - Removed `drawEaveLayer`, offscreen eave compositing, and `destination-out` cutouts.
  - Draws the `K` as fixed canvas geometry using stem, joint, upper arm, and lower arm paths.
  - Keeps `sceni` in a fixed word area and shrinks font size when needed instead of moving `K` left.
  - Draws a simpler integrated eave curve from the `K` joint/upper-arm area, so the motif no longer cuts or overlays the wordmark.
- Updated `frontend/src/components/layout/TopNav.vue`:
  - Hides the brand logo on titled top-nav screens and uses a leading spacer instead, preventing title/logo collisions.
- Updated `frontend/src/styles/shell.css`:
  - Added `.top-leading-spacer` for title-page header balance.
- Updated `frontend/scripts/check-frontend.js`:
  - Rejects `fillText('K')`, the old overlap clamp, `destination-out`, and `drawEaveLayer` in the active logo component.

### Verification result
- `npm run lint`: passed.
- `npm run build`: approved external execution passed. Existing large chunk warning remains.
- `git diff --check`: passed with CRLF conversion warnings only.
- Browser DOM QA:
  - `/`: transparent 300x92 canvas logo, no logo image/SVG references, no horizontal overflow, no console errors.
  - `/home`: transparent 136x42 canvas logo, no logo image/SVG references, no horizontal overflow, no console errors.
  - `/my`: title page renders zero logo instances, title remains present, no horizontal overflow.

### Review result
- P0: fixed structural overlap and text-cut risks.
- P1: fixed pasted-on eave layer and title/logo collision risks identified by review agents.
- P2: still a canvas approximation rather than a formal traced logo asset.

### Remaining notes
- The final visual QA agent timed out and was shut down, so final acceptance is based on the prior review findings being directly removed from code, lint/build, and browser DOM QA.

## 2026-05-22 Text-only Logo Variants

### Task chosen
- Drop the canvas hanok/ribbon logo direction after repeated user rejection and switch to a text-only sceniK wordmark.
- Provide multiple text/font variants for user comparison.

### Prompt-director execution prompt
- Replace the active logo with a text-only `sceniK` wordmark.
- Make `sceni` italic and `K` upright for the recommended active version.
- Avoid SVG, canvas, images, roof/ribbon motifs, external fonts, and new dependencies.
- Create a standalone comparison preview with multiple font/style options and mark the recommended choice.

### Changes made
- Rewrote `frontend/src/components/brand/BrandLogo.vue`:
  - Now renders only two text spans: `sceni` and `K`.
  - No SVG, no canvas, no image, no motif.
- Updated `frontend/src/styles/shell.css`:
  - Active app logo uses an Avenir/Segoe/Pretendard stack.
  - `sceni` is italic and medium-heavy.
  - `K` is upright, heavier, and deeper blue.
- Added `frontend/logo-variants.html`:
  - Includes 8 text-only logo variants with different font stacks and color emphasis.
  - Shows each as a large splash-style sample and a top-nav sample.
  - Marks variant 01 as the recommended active direction.
- Updated `frontend/scripts/check-frontend.js`:
  - Requires the text-only spans.
  - Rejects SVG, canvas, image rendering, and the previous canvas motif renderer.

### Verification result
- `npm run lint`: passed.
- `npm run build`: approved external execution passed. Existing large chunk warning remains.
- `git diff --check`: passed with CRLF conversion warnings only.
- Browser DOM QA:
  - `/logo-variants.html`: 8 variant cards, recommended option present, no horizontal overflow.
  - `/home`: active logo text is `sceniK`, canvas count `0`, logo image count `0`, SVG logo count `0`, no horizontal overflow, no browser console errors.

### Review result
- P0: no active SVG/canvas/image logo path remains.
- P1: the problematic hanok/ribbon motif and `niK` canvas overlap structure are removed.
- P2: the selected active wordmark is still a typography treatment, not a custom type-designed brand mark.

### Remaining notes
- Browser screenshot capture continued to time out, so the variants are delivered as an HTML preview file rather than an embedded screenshot.

## 2026-05-22 Text Logo Color Unification

### Task chosen
- Keep logo variant 01 but make the `K` use the same color as `sceni`.

### Changes made
- Updated `frontend/src/styles/shell.css`:
  - Removed the active logo `K` color override so it inherits the same sceniK blue as `sceni`.
- Updated `frontend/logo-variants.html`:
  - Removed the variant 01 `K` color override.
- Updated `frontend/scripts/check-frontend.js`:
  - Added a guard so `.scenik-logo__k` cannot reintroduce a separate active color.

### Verification result
- `npm run lint`: first failed as expected after adding the guard; passed after removing the color override.
- `npm run build`: approved external execution passed. Existing large chunk warning remains.
- `git diff --check`: passed with CRLF conversion warnings only.
- Browser DOM QA on `/home`: `sceni` and `K` both compute to `rgb(0, 82, 255)`, colors match, no horizontal overflow, and no browser console errors.

### Review result
- P0: none.
- P1: fixed the mismatched `K` color in the active wordmark.
- P2: other variants intentionally keep their own experimental colors for comparison, except recommended variant 01.

## 2026-05-22 Text Logo K Weight Reduction

### Task chosen
- Keep text logo variant 01 but make the `K` slightly less bold.

### Changes made
- Updated `frontend/src/styles/shell.css`:
  - Reduced active `.scenik-logo__k` font weight from `860` to `800`.
- Updated `frontend/logo-variants.html`:
  - Reduced variant 01 `.k` font weight from `860` to `800`.

### Verification result
- `npm run lint`: passed.
- `npm run build`: approved external execution passed. Existing large chunk warning remains.
- `git diff --check`: passed with CRLF conversion warnings only.
- Browser DOM QA on `/home`: `sceni` weight is `760`, `K` weight is `800`, colors still match, no horizontal overflow, and no browser console errors.

### Review result
- P0: none.
- P1: fixed the overly heavy `K` treatment.
- P2: active `K` remains slightly heavier than `sceni` for brand emphasis.

## 2026-05-22 Community Board Transition Polish

### Task chosen
- Make the visual transition between the Community `전체 / 게시판` selector and board list feel more natural.

### UI/UX designer review summary
- P0: no functional or routing blocker.
- P1: the tab `border-left` made the selector feel like table cells, the active underline collided visually with the board list, negative helper text margin compressed the section, and stacked bordered cards felt awkward.
- P2: row separators should be inset/softer, and board counts would read better as chips.

### Changes made
- Updated `frontend/src/styles/content-cards.css`:
  - Converted `.community-tabs` into a softer pill segmented control with `gap: 4px` and `padding: 4px`.
  - Removed the vertical border between `전체` and `게시판`.
  - Removed active underline and replaced it with a white active pill plus subtle shadow.
- Updated `frontend/src/styles/community.css`:
  - Removed negative helper text margin and added more breathing room before the board list.
  - Softened the board list outer border/shadow.
  - Replaced full-width row separators with inset dividers starting after the board icon.
  - Restyled board counts as small soft chips.

### Verification result
- `npm run lint`: passed.
- `npm run build`: approved external execution passed. Existing large chunk warning remains.
- `git diff --check`: passed with CRLF conversion warnings only.
- Browser DOM QA on `/community/boards`: tab gap and padding are `4px`, second tab border-left is `0px`, active tab uses white background and subtle shadow, helper bottom margin is `10px`, board count is a pill chip, five board rows render, no horizontal overflow, and no browser console errors.

### Review result
- P0: none.
- P1: fixed the unnatural divider/underline and cramped transition.
- P2: no new section label was added; existing helper copy now acts as the spacing bridge.

## 2026-05-22 Home Popular Route Preview Limit

### Task chosen
- Limit the Home `인기 여행 코스` section so it behaves as a preview before users open `전체보기`.

### UX decision
- Show three popular course cards on Home.
- Keep the existing tab sort behavior and preserve the full course list on `/home/routes-all`.
- Three cards is enough to signal available courses without making the Home feed feel overloaded on mobile.

### Changes made
- Updated `frontend/src/views/HomeView.vue`:
  - Added `homePopularRouteLimit = 3`.
  - Added `visibleRouteItems` computed from the currently selected route tab.
  - Changed the Home course card loop to render `visibleRouteItems` instead of the full `routeItems`.
- Updated `frontend/scripts/check-frontend.js`:
  - Added a regression guard requiring the three-card Home preview.
  - Added a guard preventing Home from rendering the full route collection directly.

### Verification result
- `npm run lint`: initially failed with the new guard, then passed after implementation.
- `npm run build`: sandbox run failed with `spawn EPERM`; approved external execution passed. Existing large chunk warning remains.
- `git diff --check`: passed with CRLF conversion warnings only.
- Browser DOM QA:
  - `/home` renders 3 `.course-link-card` items and has no horizontal overflow.
  - `/home/routes-all` still renders 8 `.course-link-card` items and has no horizontal overflow.

### Review result
- P0: none.
- P1: fixed the overloaded Home popular route preview while preserving the full list route.
- P2: no new visual treatment was added; the existing card stack now has better density through item count control.

## 2026-05-22 Text Logo K Weight Fine Tune

### Task chosen
- Interpret the latest logo feedback as making the `K` slightly less heavy because it still looked too thick.

### Changes made
- Updated `frontend/src/styles/shell.css`:
  - Reduced active `.scenik-logo__k` font weight from `800` to `780`.
- Updated `frontend/logo-variants.html`:
  - Matched recommended variant 01 `K` weight to `780`.
- Updated `frontend/scripts/check-frontend.js`:
  - Added a guard to keep the active logo `K` at `780`.

### Verification result
- `npm run lint`: initially failed with the new guard, then passed after implementation.
- `npm run build`: sandbox run failed with `spawn EPERM`; approved external execution passed. Existing large chunk warning remains.
- Browser DOM QA on `/home`: `sceni` weight is `760`, `K` weight is `780`, both colors are `rgb(0, 82, 255)`, and no horizontal overflow was detected.

### Review result
- P0: none.
- P1: fixed the visually heavy `K` while keeping a small brand emphasis over `sceni`.
- P2: no font family, color, or layout changes were made.

## 2026-05-22 Community Save Copy to Scrap

### Task chosen
- Rename Community post save actions to `스크랩` because the underlying behavior is already post scrap/bookmark behavior.

### Changes made
- Updated `frontend/src/views/CommunityView.vue`:
  - Changed Community list and post-detail action labels from `저장` / `저장됨` to `스크랩` / `스크랩됨`.
- Updated `frontend/src/stores/uiStore.js`:
  - Changed the Community scrap login-required message from save wording to scrap wording.
  - Changed Community scrap toast messages to `게시글을 스크랩했습니다.` and `게시글 스크랩을 해제했습니다.`
  - Renamed the local action-state variable from `saved` to `scrappedPost` for clearer intent.

### Verification result
- `rg` check found no remaining Community post save-action copy in `CommunityView.vue` or the Community scrap branch of `uiStore.js`.
- `npm run lint`: passed.
- `npm run build`: sandbox run failed with `spawn EPERM`; approved external execution passed. Existing large chunk warning remains.
- Browser DOM QA:
  - `/community` action labels show `스크랩`, and no `저장` action copy remains.
  - `/community/post/seed-post-1` detail actions show `스크랩`, and no `저장` action copy remains.
  - No horizontal overflow was detected.

### Review result
- P0: none.
- P1: fixed Community terminology so the UI matches the existing `scrappedBy` / `togglePostScrap` behavior.
- P2: underlying localStorage data shape and function names were preserved for compatibility.

## 2026-05-22 Primary Tab Header Alignment

### Task chosen
- Make Community and My Page feel aligned with AI Course, Explore, and Home when entering the primary tabs.
- Move the notification/settings controls slightly to the right and reduce their gap.

### Cause found
- The app shell itself was not shifted. Browser measurement showed the same `430px` shell and top-nav coordinates across tabs.
- Community and My Page looked different because they used `AppLayout title` mode with a centered title, while AI Course, Explore, and Home used the logo-led header.

### Changes made
- Updated `frontend/src/views/CommunityView.vue`:
  - Community main tab now uses the logo-led header.
  - Post detail and compose/back flows keep title mode.
- Updated `frontend/src/views/MyPageView.vue`:
  - My Page main tab now uses the logo-led header.
- Updated `frontend/src/styles/shell.css`:
  - Changed `.top-nav` right padding from `16px` to `12px`, moving top actions 4px to the right.
  - Changed `.top-actions` gap from `4px` to `2px`, moving the notification button closer to settings.
- Updated `frontend/scripts/check-frontend.js`:
  - Added guards for the tighter top-action alignment and primary-tab logo header consistency.

### Verification result
- `npm run lint`: passed.
- `npm run build`: sandbox run failed with `spawn EPERM`; approved external execution passed. Existing large chunk warning remains.
- `git diff --check`: passed with CRLF conversion warnings only.
- Browser DOM QA:
  - `/ai`, `/explore`, `/home`, `/community`, and `/my` all use logo-led headers with no centered title.
  - Top actions have right inset `12px` and button gap `2px` across those tabs.
  - No horizontal overflow was detected.

### Review result
- P0: none.
- P1: fixed the perceived primary-tab header alignment mismatch.
- P2: preserved titled/back behavior on secondary flows instead of changing every route header globally.

## 2026-05-22 Stable Scrollbar Centering Fix

### Task chosen
- Fix the remaining slight left-shift feeling on AI Course, Explore, and Home after the primary-tab header alignment pass.

### Cause found
- AI Course, Explore, and Home are long scrolling pages.
- My Page can be short enough not to require vertical scroll.
- On desktop environments, pages with and without a vertical scrollbar can make a centered app shell appear horizontally inconsistent.

### Changes made
- Updated `frontend/src/styles/base.css`:
  - Added an `html` rule with `scrollbar-gutter: stable both-edges` and `overflow-y: scroll`.
  - This reserves vertical scrollbar space consistently on both sides so the centered 430px app shell stays visually centered.
- Updated `frontend/scripts/check-frontend.js`:
  - Added a guard requiring the stable both-edge scrollbar gutter.

### Verification result
- `npm run lint`: initially failed after adding the guard, then passed after implementation.
- `npm run build`: sandbox run failed with `spawn EPERM`; approved external execution passed. Existing large chunk warning remains.
- `git diff --check`: passed with CRLF conversion warnings only.
- Browser DOM QA:
  - `/ai`, `/explore`, `/home`, `/community`, and `/my` all computed `scrollbar-gutter: stable both-edges` and `overflow-y: scroll`.
  - App shell center matched viewport center with `centerDelta: 0` on all five primary tabs.
  - No horizontal overflow was detected.

### Review result
- P0: none.
- P1: fixed the remaining tab-to-tab horizontal centering variance caused by scrollbar state differences.
- P2: no per-page content padding changes were made; the fix is global and structural.

## 2026-05-22 Logo Font Variant Preview

### Task chosen
- Create a preview page showing the sceniK text logo in the requested font candidates.

### Changes made
- Added `frontend/logo-font-variants.html`:
  - Groups variants into Requested, Favourite, and Current.
  - Renders text-only `sceniK` logo cards and compact nav samples.
  - Uses each requested font-family name directly, with the current system fallback stack.
  - Includes a canvas-based local font availability check and marks each card as `available` or `fallback`.

### Verification result
- Browser DOM QA at `/logo-font-variants.html`:
  - Rendered 19 cards: 9 Requested, 6 Favourite, 4 Current.
  - No horizontal overflow was detected.
  - Local availability check reported 0 available and 19 fallback fonts on this machine.
- `npm run lint`: passed.
- `git diff --check -- frontend/logo-font-variants.html`: passed.

### Review result
- P0: none.
- P1: preview page is useful for comparison only when the target font files are installed or loaded.
- P2: no app runtime route or active logo implementation was changed.

## 2026-05-22 Local Font Logo Variant Matrix

### Task chosen
- Use the font files placed in `fonts/` to create actual sceniK logo previews.
- Show three logo treatments per loaded font: basic, italic sceni with upright K, and bold.

### Changes made
- Added `logo-font-variants.html`:
  - Standalone local-file preview that references `fonts/` relatively.
- Added `frontend/logo-local-font-variants.html`:
  - Vite-served preview page at `/logo-local-font-variants.html`.
  - Generates 11 loaded font families and 33 logo variants.
  - Shows missing requested fonts that are not present as `.otf` / `.ttf` in `fonts/`.
- Updated `frontend/vite.config.js`:
  - Added a narrow `server.fs.allow` entry for the frontend project root and local `fonts/` folder so the Vite preview can load the font files through `/@fs/...`.

### Fonts loaded
- Ragika
- RUSKYN
- Sunday Ridges
- Ciguatera
- Devina Garden
- Dit Magi
- Aspect Range
- CHABUL
- Ledira
- Neue Haas Unica Pro
- OGG

### Missing from `fonts/`
- Call Heart
- FERRON
- SPACE RABBIT
- ROUTINE
- Rulingpen
- Viana Script
- The Bellonte
- San Brainy

### Verification result
- Browser DOM QA at `/logo-local-font-variants.html`:
  - 11 font sets rendered.
  - 33 variant cards rendered.
  - 11 fonts reported `loaded`, 0 fallback.
  - 8 missing font names displayed.
  - No horizontal overflow was detected.
- `npm run lint`: passed.
- `npm run build`: sandbox run failed with `spawn EPERM`; approved external execution passed. Existing large chunk warning remains.
- `git diff --check -- logo-font-variants.html frontend/logo-local-font-variants.html frontend/vite.config.js`: passed with CRLF conversion warning for `vite.config.js` only.

### Review result
- P0: none.
- P1: actual preview is limited to font files currently present under `fonts/`; missing candidate fonts need their files added before they can be compared.
- P2: active app logo was intentionally not changed yet; this is a comparison surface only.

## 2026-05-22 Logo Font Final Shortlist

### Task chosen
- Narrow the logo font preview to the final five candidates only.

### Final candidates kept
- CHABUL
- Aspect Range
- Dit Magi
- Ciguatera
- OGG

### Changes made
- Updated `frontend/logo-local-font-variants.html`:
  - Removed all non-final font candidates.
  - Removed the missing-font list.
  - Updated the count to `5 fonts · 15 logo variants`.
- Updated `logo-font-variants.html`:
  - Mirrored the same final-candidate-only set for standalone local preview.

### Verification result
- Browser DOM QA at `/logo-local-font-variants.html`:
  - 5 font sets rendered.
  - 15 variant cards rendered.
  - All 5 fonts reported `loaded`.
  - Removed font names were not present in visible page text.
  - No horizontal overflow was detected.
- `npm run lint`: passed.
- `git diff --check -- logo-font-variants.html frontend/logo-local-font-variants.html`: passed.

### Review result
- P0: none.
- P1: final preview now focuses only on the selected logo candidates.
- P2: active app logo was intentionally not changed yet.

## 2026-05-22 Route-Based Logo Font Test

### Task chosen
- Add the previous active text logo to the logo preview.
- Apply the five final candidate fonts to the actual app, one font per primary tab, all in bold, so they can be tested in context.

### Font mapping
- AI Course: CHABUL
- Explore: Aspect Range
- Home: Dit Magi
- Community: Ciguatera
- My Page: OGG

### Changes made
- Updated `frontend/src/components/brand/BrandLogo.vue`:
  - Added route-aware logo font variant classes.
  - Kept the shared text-only `sceniK` component and avoided SVG/canvas/image rendering.
- Updated `frontend/src/styles/shell.css`:
  - Added `@font-face` rules for the five final candidate fonts.
  - Added route-test font classes.
  - Changed the active logo test style to bold upright `sceni` plus bold upright `K`.
- Updated `frontend/logo-local-font-variants.html` and `logo-font-variants.html`:
  - Added an Existing Logo Reference baseline showing the previous Avenir-style text logo.
  - Kept only the five final candidates and their 15 variants.
- Updated `frontend/scripts/check-frontend.js`:
  - Added guards for route-based font test variants and the five active logo test fonts.

### Verification result
- Browser DOM QA:
  - `/ai`: `Logo CHABUL`, loaded, `sceni` weight 700, `K` weight 780.
  - `/explore`: `Logo Aspect Range`, loaded, `sceni` weight 700, `K` weight 780.
  - `/home`: `Logo Dit Magi`, loaded, `sceni` weight 700, `K` weight 780.
  - `/community`: `Logo Ciguatera`, loaded, `sceni` weight 700, `K` weight 780.
  - `/my`: `Logo OGG`, loaded, `sceni` weight 700, `K` weight 780.
  - `/logo-local-font-variants.html`: existing reference rendered, 5 final fonts rendered, 15 variants rendered, all loaded.
  - No horizontal overflow was detected.
- `npm run lint`: passed.
- `npm run build`: sandbox run failed with `spawn EPERM`; approved external execution passed. Existing large chunk warning remains. The build emitted the five font assets successfully.
- `git diff --check`: passed with CRLF conversion warning for `frontend/scripts/check-frontend.js` only.

### Review result
- P0: none.
- P1: active app logo is now intentionally in a temporary route-based test state, not a final brand selection.
- P2: Splash and non-primary default logo paths currently fall back to the Home/Dit Magi variant through the route fallback.

## 2026-05-22 Dit Magi Logo Weight Increase

### Task chosen
- Make the Home/Dit Magi route-test logo slightly heavier while preserving the other route-test logo weights.

### Changes made
- Updated `frontend/src/styles/shell.css`:
  - Added Dit Magi-specific overrides:
    - `sceni` weight `760`
    - `K` weight `820`
- Updated `frontend/scripts/check-frontend.js`:
  - Added a guard to keep the Dit Magi route-test logo slightly heavier than the shared route-test baseline.

### Verification result
- `npm run lint`: initially failed after adding the guard, then passed after implementation.
- Browser DOM QA:
  - `/home` uses `Logo Dit Magi`, `sceni` weight `760`, `K` weight `820`.
  - `/ai` still uses `Logo CHABUL`, `sceni` weight `700`, `K` weight `780`.
  - No horizontal overflow was detected.
- `npm run build`: sandbox run failed with `spawn EPERM`; approved external execution passed. Existing large chunk warning remains.

### Review result
- P0: none.
- P1: Dit Magi is now visibly heavier only on the Home route-test logo.
- P2: The logo test state remains temporary until a final font is selected.

## 2026-05-22 Dit Magi 900 Equal Weight Adjustment

### Task chosen
- Make the Home/Dit Magi route-test logo heavier and apply the same weight to `sceni` and `K` instead of treating `K` separately.

### Changes made
- Updated `frontend/src/styles/shell.css`:
  - Combined the Dit Magi `sceni` and `K` selector.
  - Set both parts to `font-weight: 900`.
- Updated `frontend/scripts/check-frontend.js`:
  - Added a guard that requires Dit Magi `sceni` and `K` to share the same 900 weight.

### Verification result
- `npm run lint`: passed.
- Browser DOM QA on `/home`:
  - Logo class: `scenik-logo--font-dit-magi`.
  - `Logo Dit Magi` loaded.
  - `sceni` weight: `900`.
  - `K` weight: `900`.
  - Same weight: true.
  - Horizontal overflow: false.
- `npm run build`: sandbox run failed with `spawn EPERM`; approved external execution passed. Existing large chunk warning remains.

### Review result
- P0: none.
- P1: Dit Magi now uses the same 900 weight for the full `sceniK` wordmark on the Home route-test logo.
- P2: The route-based logo font test state remains temporary until a final font is selected.

## 2026-05-22 Dit Magi Maximum Weight Adjustment

### Task chosen
- Make the Home/Dit Magi route-test logo heavier again after the 900 equal-weight pass.
- Use the browser-supported maximum `font-weight: 1000` because `1050` is outside the standard CSS font-weight range and may be ignored by browsers.

### Changes made
- Updated `frontend/src/styles/shell.css`:
  - Changed the combined Dit Magi `sceni` and `K` selector from `900` to `1000`.
- Updated `frontend/scripts/check-frontend.js`:
  - Changed the Dit Magi guard to require the same maximum 1000 weight.

### Verification result
- `npm run lint`: passed.
- `npm run build`: sandbox run failed with `spawn EPERM`; approved external execution passed. Existing large chunk warning remains.
- Browser DOM QA on `/home`:
  - Logo class: `scenik-logo--font-dit-magi`.
  - `Logo Dit Magi` loaded.
  - `sceni` weight: `1000`.
  - `K` weight: `1000`.
  - Same weight: true.
  - Horizontal overflow: false.

### Review result
- P0: none.
- P1: Dit Magi now uses the heaviest valid CSS weight for the full `sceniK` wordmark.
- P2: If this still looks visually too light, the next step is a deliberate optical bolding treatment such as a very subtle stroke, not an invalid font-weight value.

## 2026-05-22 Dit Magi 2000 Optical Bold Test

### Task chosen
- Try a heavier-than-maximum Dit Magi logo treatment after the request for `2000`.
- Keep `font-weight: 1000` because CSS numeric font weights above 1000 are invalid, then add a subtle same-color stroke to simulate a heavier optical weight.

### Changes made
- Updated `frontend/src/styles/shell.css`:
  - Kept the combined Dit Magi `sceni` and `K` selector.
  - Added `-webkit-text-stroke: 0.45px currentColor` to both parts.
- Updated `frontend/scripts/check-frontend.js`:
  - Changed the Dit Magi guard to require both maximum weight and the optical bold stroke.

### Verification result
- `npm run lint`: passed.
- `npm run build`: sandbox run failed with `spawn EPERM`; approved external execution passed. Existing large chunk warning remains.
- Browser DOM QA on `/home`:
  - Logo class: `scenik-logo--font-dit-magi`.
  - `Logo Dit Magi` loaded.
  - `sceni` weight: `1000`.
  - `K` weight: `1000`.
  - `sceni` stroke width: `0.45px`.
  - `K` stroke width: `0.45px`.
  - Horizontal overflow: false.

### Review result
- P0: none.
- P1: The full `sceniK` wordmark now gets the same heavier optical treatment; `K` is not styled separately.
- P2: This is an optical bold test, not a literal CSS `font-weight: 2000`, because that value is outside browser-supported numeric weight range.

## 2026-05-22 Global Dit Magi Logo Application

### Task chosen
- Apply the selected Dit Magi 2000 optical bold logo treatment to every screen instead of keeping the temporary route-based font test.

### Changes made
- Updated `frontend/src/components/brand/BrandLogo.vue`:
  - Removed route-based font switching.
  - Fixed the shared logo component to always use `scenik-logo--font-dit-magi`.
- Updated `frontend/src/styles/shell.css`:
  - Removed active app `@font-face` blocks and classes for CHABUL, Aspect Range, Ciguatera, and OGG.
  - Kept only the active Dit Magi font for the app logo.
  - Kept the same combined `sceni` and `K` optical bold rule: `font-weight: 1000` plus `-webkit-text-stroke: 0.45px currentColor`.
  - Removed separate base font-weight handling for `K`.
- Updated `frontend/scripts/check-frontend.js`:
  - Replaced route-test logo guards with final global Dit Magi logo guards.
  - Added checks that route-test font assets are no longer loaded by the active app CSS.

### Verification result
- `npm run lint`: passed.
- `npm run build`: sandbox run failed with `spawn EPERM`; approved external execution passed. Existing large chunk warning remains.
- Browser DOM QA:
  - `/`, `/ai`, `/explore`, `/home`, `/community`, and `/my` all use `scenik-logo--font-dit-magi`.
  - All checked logo instances use `Logo Dit Magi`.
  - `sceni` weight: `1000`.
  - `K` weight: `1000`.
  - `sceni` stroke width: `0.45px`.
  - `K` stroke width: `0.45px`.
  - No route-test classes were present.
  - No horizontal overflow was detected.
- Production build now emits only the active Dit Magi logo font asset among the previous route-test logo fonts.

### Review result
- P0: none.
- P1: final selected logo is now applied globally through the shared BrandLogo component.
- P2: Standalone logo preview pages still retain the shortlisted font comparison matrix for future reference; active app CSS no longer uses those alternate fonts.

## 2026-05-22 AI Course Loading Circular Progress

### Task chosen
- Replace the AI course generation loading screen's old route-dot visual and straight progress bar with a prototype-inspired circular loading screen.
- Keep the current sceniK color, typography, spacing, and app shell conventions.

### Changes made
- Updated `frontend/src/views/AiLoadingView.vue`:
  - Added reactive loading progress.
  - Added a circular `progressbar` with visible percentage.
  - Added three generation steps: filming-place data search, route optimization, and recommendation composition.
  - Kept the existing AI route generation, candidate-place validation, persistence, and redirect logic.
  - Added timer cleanup on unmount.
- Updated `frontend/src/styles/ai-course.css`:
  - Replaced the old dot-route visual and straight progress bar with a circular conic-gradient progress ring plus a rotating arc.
  - Added centered loading copy and a compact step checklist.
  - Used current app tokens for ink, border, muted text, and surface colors.
- Updated `frontend/scripts/check-frontend.js`:
  - Added guards requiring the circular progress loader and visible step progress.
  - Added a guard preventing the old straight progress bar from returning.

### Verification result
- `npm run lint`: passed.
- `npm run build`: sandbox run failed with `spawn EPERM`; approved external execution passed. Existing large chunk warning remains.
- Browser DOM QA on `/ai/loading`:
  - Circular loader exists.
  - `role="progressbar"` is present.
  - Percentage is visible and updates.
  - Three step rows render with done/active/waiting state.
  - Old `.progress-bar` and `.loading-route` are absent.
  - No horizontal overflow was detected.
- Browser visual QA:
  - The loading content is centered.
  - The circular ring, percentage, headline, status copy, and step list fit without overlap.

### Review result
- P0: none.
- P1: loading screen now matches the requested prototype structure while preserving existing generation behavior.
- P2: Because mock generation is fast, the progress animation is intentionally short before the result redirect.

## 2026-05-26 Server-Backed MVP Smoke Stabilization

### Task chosen
- Run a real integration smoke/stabilization pass for the Spring Boot + MySQL + Vue MVP flow.
- Use Backend Runtime QA, Frontend Runtime QA, and Contract QA agents before applying fixes.

### Environment result
- MySQL Windows service `MySQL80` is running and `127.0.0.1:3306` is reachable.
- MySQL Shell exists, but configured default credentials `root/password` are rejected with MySQL error 1045.
- Java 17 is available.
- Maven and Maven wrapper are not available in this workspace.
- The packaged backend jar in `backend/target` is stale and does not include the new auth/route/AI/like/visit controllers.
- Vue dev server is reachable on `http://127.0.0.1:5173/`.

### Stabilization changes made
- Updated `frontend/src/views/AiLoadingView.vue`:
  - AI route generation API failures no longer silently fall through to local deterministic success.
  - Local deterministic AI fallback is now opt-in with `VITE_ENABLE_LOCAL_AI_FALLBACK=true`.
- Updated `frontend/src/stores/uiStore.js`:
  - Saving an AI route now replaces the current generated route with the server-saved route, including the server-generated `routeId`.
  - Saved-route list/detail API failures no longer return stale localStorage-backed data as if the backend succeeded.
- Updated `frontend/src/api/routeApi.js`:
  - Route-place add/update/delete/reorder wrappers now normalize the backend's updated route response.
  - Route normalization preserves all candidate places returned by the backend.
- Updated `backend/src/main/java/com/scenik/service/AiRouteService.java`:
  - If region fallback is used, the response region now matches the actual candidate region.
- Updated `backend/src/main/java/com/scenik/service/RouteService.java`:
  - Reorder validation now checks final day/order conflicts against existing route places not included in the request.

### Verification result
- `javac @target\\javac.args`: passed.
- MyBatis mapper XML parse check: passed.
- `npm run lint`: passed.
- `npm run build`: sandbox run failed with `spawn EPERM`; approved external execution passed. Existing large chunk warning remains.
- `git diff --check`: passed, with existing CRLF warnings only.
- Backend runtime start using freshly compiled classes plus packaged jar dependencies reached Spring Boot startup, then failed at DB connection because `root/password` was rejected.
- Full signup/login/AI route/save/edit/like/visit smoke could not be executed until MySQL credentials and a fresh backend package/run path are available.

### Remaining limitations
- Install Maven or add a Maven wrapper, then rebuild the stale backend jar.
- Set the actual MySQL credentials with `DB_USERNAME` and `DB_PASSWORD`.
- Ensure the `scenik` database exists before running the backend.
- Unused route candidate places are still not persisted in a separate backend table; saved-course edit should rely on place search for adding new places.
- `PATCH /api/users/me` still does not persist `bio`; this is outside the smoke stabilization scope.

## 2026-05-28 Week 2 P0 Runtime Stabilization

### Task chosen
- Stabilize the server-backed MVP for Spring runtime parameter binding, auth token lifecycle, visit-status API shape, consistent error responses, Maven Wrapper builds, and async frontend route smoke testing.

### Changes made
- Updated backend controllers:
  - Added explicit annotation names for `@PathVariable`, `@RequestParam`, and `@RequestHeader` usages.
  - Added auth logout and token refresh endpoints.
  - Added visit-status `PUT` and `DELETE` endpoints while keeping existing `POST` compatibility.
- Updated backend auth:
  - Kept the existing opaque DB-stored token approach.
  - Logout revokes the current bearer token in `auth_tokens`.
  - Refresh validates an active opaque refresh token, revokes it, and issues a new opaque token pair.
- Updated backend error handling:
  - Added `GlobalExceptionHandler` with `{ code, message }` responses for invalid request, unauthorized, forbidden, not found, conflict, and internal errors.
- Updated backend build:
  - Added Maven Wrapper files and `maven-wrapper.jar`.
  - Added Maven compiler `parameters` config.
  - Added `application-example.yml` without real secrets.
- Updated visit status:
  - Backend API values are now `WANT_TO_GO` and `VISITED`.
  - Frontend maps current local `planned/completed` labels to the API values.
- Updated frontend smoke:
  - Replaced the old synchronous localStorage course smoke assumptions with an async mocked-backend API flow covering signup/login, AI route generation, save, list/detail retrieval, and update.

### ID policy
- `contentId` and `placeId` remain string IDs such as `C_DRAMA_001` and `P_001`.
- `routeId` remains a string because the existing DB schema, DTOs, services, and frontend route state already use string route IDs deeply.

### Verification result
- `backend/mvnw.cmd package`: passed.
- `npm run lint`: passed.
- `npm run smoke:course`: passed.
- `npm run build`: sandbox run failed with `spawn EPERM`; approved external execution passed. Existing large chunk warning remains.
- Real local backend API E2E with `DB_USERNAME=ssafy` and `DB_PASSWORD=ssafy`: passed for signup, login, token refresh, AI route generation, route save, My Courses, route detail, add place, refetch persistence, remove place, likes, visit status PUT/GET/DELETE, search, content detail, place detail, and logout.

### Remaining limitations
- The local PowerShell smoke had to sanitize display text when echoing AI route JSON back into `POST /api/routes` because PowerShell reserialized Korean text with mojibake/control characters. Browser/Axios UTF-8 flow is not expected to have that issue.
- `sceniK-ai` is not present in this workspace, so the backend AI route endpoint remains the deterministic DB-backed contract from the MVP pass.

## 2026-05-28 Core API / AI Prep / Admin Stabilization

### Task chosen
- Align sceniK-back content, place, map helper, route, deterministic AI route, admin content/place CRUD, DTO naming, and Swagger discoverability for Week 2 P0 stabilization.

### Changes made
- Content APIs:
  - Added `GET /api/contents/categories`.
  - Added `GET /api/contents/popular` and `GET /api/contents?sort=popular`.
  - Added content DTO aggregate fields: `likeCount`, `viewCount`, `relatedPlaceCount`, `isLiked`.
- Place APIs:
  - Added `GET /api/places/nearby` with simple DB-side distance calculation.
  - Added place DTO fields `name`, `thumbnailUrl`, `likeCount`, `isLiked`, `visitStatus`, and `relatedContents`.
  - Kept `placeName` and `imageUrl` as compatibility aliases for current frontend screens.
- Maps:
  - Added backend-only Kakao helper skeleton endpoints under `/api/maps`.
  - Added `kakao.rest-api-key` / `KAKAO_REST_API_KEY` configuration documentation without real secrets.
- Routes / AI:
  - Added `generationId`, `sourceType`, `isPublic`, flat `places`, `createdAt`, and `updatedAt` response fields.
  - Added `POST /api/ai/routes/{generationId}/regenerate` with temporary in-memory generation state.
  - Kept deterministic DB-backed AI route generation; no OpenAI/RAG integration was added.
- Admin:
  - Added temporary-auth-protected admin content/place create, patch, and delete endpoints.
  - Content delete is soft delete through `contents.status = 'DELETED'`.
  - Place delete is hard delete after local reference cleanup.
- OpenAPI:
  - Added `springdoc-openapi-starter-webmvc-ui`; Swagger UI is available through the standard Springdoc route.

### Verification result
- `backend/mvnw.cmd package` from repository root failed because the wrapper needs the backend working directory.
- `.\mvnw.cmd package` from `backend/`: passed after approved Maven cache/dependency download.
- Real backend smoke with MySQL `ssafy/ssafy` on port `18080`: passed for contents, categories, popular, content places, places, nearby, maps skeleton, signup, content/place likes, visit status PUT/DELETE, AI route generate, AI route regenerate, route save/list/detail, admin content create/delete, and admin place create/delete.

### ID policy
- `contentId` and `placeId` remain string source IDs.
- `categoryId` remains a future string-code concept; current category values remain existing DB strings.
- `routeId` remains string because the current backend/frontend implementation already deeply uses string route IDs.

### Remaining limitations
- `sourceType` and `isPublic` are response defaults, not persisted columns yet.
- Admin authorization is not role based yet.
- Kakao helper endpoints are skeleton/fallback endpoints until real Kakao REST integration is wired behind sceniK-back.
- `sceniK-ai` is still not present in this workspace.

## 2026-05-28 Frontend Week 3 API Preparation

### Task chosen
- Prepare `frontend/` for Week 3 backend integration by routing API calls through `sceniK-back`, replacing community localStorage business data with API calls, and updating the course smoke test for async backend route persistence.

### Changes made
- Added `frontend/.env.example` with `VITE_API_BASE_URL=http://localhost:8080/api` and no secrets.
- Updated shared Axios setup to use `VITE_API_BASE_URL`, attach bearer tokens, and retry once through `/auth/token/refresh` on 401.
- Added `frontend/src/api/communityApi.js` for posts, comments, likes, and reports.
- Updated `uiStore` so saved routes, likes, visit status, and community posts are not persisted in the UI localStorage state.
- Updated community list/detail/write, My Page, and simple list screens to load community posts from API and await create/comment/like operations.
- Updated `scripts/check-course-flow.js` to verify AI generation response shape, route save payload, saved route list/detail retrieval, and route update through mocked backend adapters.

### Verification result
- `npm run smoke:course`: passed.
- `npm run lint`: passed.
- `npm run build`: sandbox run failed with `spawn EPERM`; approved external execution passed. Existing large chunk warning remains.

### Remaining limitations
- The current backend workspace does not yet expose community endpoints, so community screens will show backend errors until those endpoints are implemented.
- Community scrap is still session-local because no backend scrap endpoint was specified.
- `generatedRoute` remains local UI state for the result/edit flow and offline fallback cache behavior.

## 2026-05-28 Week 4 AI/RAG Skeleton

### Task chosen
- Create a separate `sceniK-ai/` FastAPI skeleton that can later be called internally by `sceniK-back`, without exposing AI directly to the frontend and without adding real OpenAI/RAG dependencies yet.

### Changes made
- Added FastAPI app structure under `sceniK-ai/app/`.
- Added `GET /health`.
- Added internal `POST /ai/routes` protected by `Internal-Api-Key`.
- Added internal embedding stub endpoints:
  - `POST /ai/embeddings/rebuild`
  - `PUT /ai/embeddings/{placeId}`
- Added deterministic route recommendation from backend-provided `candidatePlaces`.
- Added in-memory vector-store stub with TODOs for future FAISS/Chroma or another approved vector store.
- Added `sceniK-ai/.env.example`, `sceniK-ai/requirements.txt`, `sceniK-ai/README.md`, and minimal pytest API tests.
- Updated `.gitignore` for Python cache and virtualenv artifacts.

### Verification result
- Initial `python -m pytest` failed because `pytest` was not installed.
- Initial `python -m pip install -r requirements.txt` failed in the sandbox because package indexes were unavailable.
- Approved external `python -m pip install -r requirements.txt`: passed.
- `python -m pytest`: passed, 4 tests.
- `python -m compileall app tests`: passed.
- Temporary uvicorn run on `127.0.0.1:8010`: `/health` returned `ok`, then the process was stopped.

### Remaining limitations
- No real OpenAI call, vector DB, prompt optimization, or production RAG is implemented.
- Embedding data is in-memory only and resets on process restart.
- Spring Boot has not been wired to call `sceniK-ai`; backend deterministic DB fallback remains the active AI route implementation.

## 2026-05-28 Week 3 Integration Readiness Docs

### Task chosen
- Add Week 3 integration documentation, Swagger metadata, HTTP API collection, seed reset instructions, demo flow, DTO naming checklist, and frontend state migration checklist.

### Changes made
- Confirmed `springdoc-openapi-starter-webmvc-ui` already exists in `backend/pom.xml`.
- Added minimal `OpenApiConfig` so Swagger shows the sceniK backend title/version/description.
- Added `api-tests/scenik-week3.http` with auth, content, place, AI route, saved route, and requested community post/comment calls.
- Added `docs/seed-reset.md` with MySQL reset instructions and demo account creation through signup.
- Added `docs/demo-flow.md` with the required Week 3 demo path.
- Added `docs/api-field-naming.md` with canonical DTO field names and compatibility aliases.
- Added `docs/frontend-state-migration.md` with backend-backed vs local-only state guidance.

### Known gaps documented
- Resolved later in the stabilization pass: backend community endpoints now live under `/api/posts`, and the frontend adapter uses `/posts`.
- Demo account insertion should be done through signup because password hashes use PBKDF2 with random salts.

### Verification result
- `backend/.\\mvnw.cmd package`: passed.
- `frontend npm run lint`: passed.
- `frontend npm run build`: sandbox run failed with `spawn EPERM`; approved external execution passed. Existing large chunk warning remains.
- Secret scan over the added docs/API collection did not find real OpenAI/Kakao key patterns.

## 2026-05-28 Integration 500 Stabilization Pass

### Task chosen
- Make backend 500 errors observable, fix frontend/backend community path mismatch, harden route saving, and add local DB reset/smoke tooling.

### Changes made
- Updated `GlobalExceptionHandler` to log full stack traces for unexpected errors with request method and URI while returning safe client errors.
- Added specific handling/logging for DB access, duplicate keys, SQL integrity constraints, unreadable JSON, missing params, type mismatches, validation errors, and `ResponseStatusException`.
- Added community backend tables and endpoints under `/api/posts`, `/api/posts/{postId}/comments`, `/api/posts/{postId}/likes`, `/api/posts/{postId}/reports`, `/api/notifications`, and `/api/users/me/posts`.
- Updated `frontend/src/api/communityApi.js` from `/community/posts` paths to `/posts` paths.
- Changed route saving so `POST /api/routes` accepts both full AI route responses and simple saved-route payloads with flat `places`.
- Added defaults for route-place add requests so edit-mode add-place payloads do not require AI-only reason/time/tip fields.
- Added split My Page endpoints for liked contents and liked places.
- Added map helper compatibility for `address`/`keyword` plus legacy `query`.
- Changed admin place deletion to delete dependent rows in a controlled order instead of relying on `places.status`, so older local DBs do not 500.
- Added `scripts/reset-local-db.ps1`, `scripts/reset-local-db.sh`, `backend/README.md`, and `api-tests/backend-db-smoke.http`.
- Updated docs to remove the now-obsolete community-backend-missing note.

### Root causes found
- `GET /api/places` returned 500 on the local DB because the local `places` table was from an older schema and did not have the newly referenced `status` column.
- Community frontend calls used `/api/community/posts` while the intended backend contract was `/api/posts`.
- `POST /api/routes` only accepted full AI `candidatePlaces + days` payloads, while frontend save flows could send a simple `places` array.
- Admin place deletion could become FK-sensitive if referenced places existed in route/like/visit/content mapping tables.

### Verification result
- `backend/.\\mvnw.cmd package`: passed.
- `frontend npm run build`: sandbox run failed with `spawn EPERM`; approved external execution passed with the existing large chunk warning.
- `frontend npm run smoke:course`: passed.
- `frontend npm run lint`: passed.
- Local backend smoke using `DB_USERNAME=ssafy`, `DB_PASSWORD=ssafy`: `/api/contents`, `/api/places`, and `/api/posts` returned 200.
- Authenticated API smoke passed for signup, login, AI route generation, simple route save, My Routes, community post/comment creation, place like, and visit status.

### Remaining limitations
- `places.status` exists in the new schema for future soft-delete migration, but runtime place APIs avoid depending on it so older local DBs keep working.
- Community scrap still has no backend contract.
- Real Kakao REST and FastAPI/RAG integration remain future backend-owned work.

## 2026-05-28 Critical 500 Reproduction Pass

### Task chosen
- Re-run the core Week 2 demo flow after reports of login, AI route, route save, and community 500 errors.

### Evidence gathered
- `backend/.\\mvnw.cmd clean package` failed in the sandbox because Maven could not write to `C:\\Users\\SSAFY\\.m2`; approved external execution passed.
- A fresh backend run with `DB_USERNAME=ssafy`, `DB_PASSWORD=ssafy` reproduced the requested sequence.
- Signup, login, users/me, contents, places, AI route generation, posts list, post create, and comment create did not return 500.
- Saving the raw AI response through PowerShell initially returned 400 because `Invoke-RestMethod` sent Korean JSON without UTF-8 bytes. The Spring log showed `HttpMessageNotReadableException: Invalid UTF-8 middle byte`, not a backend route-save 500.
- Re-running route save with UTF-8 request bytes passed for both the full AI response shape and the simple `places` payload.

### Changes made
- Added backend-local reset scripts:
  - `backend/scripts/reset-local-db.ps1`
  - `backend/scripts/reset-local-db.sh`
- Added `backend/api-tests/week2-critical-flow.http` with the critical signup/login/content/place/AI/route/community flow and UTF-8 content types.
- Updated `backend/README.md` to point to backend-local reset scripts.

### Current status
- The core demo API flow passes against local MySQL using `ssafy` / `ssafy`.
- No current login, AI route generation, or community 500 was reproducible after the previous stabilization changes.

## 2026-05-28 Week 2 Remaining / Week 3 Frontend Readiness

### Task chosen
- Finish remaining Week 2 backend and Week 3 frontend integration readiness items without broad refactors.

### Changes made
- Rebuilt the signup and login Vue screens with readable Korean copy.
- Added frontend password validation matching backend rule: password length must be at least 8 characters.
- Added frontend auth error mapping so expected backend errors show useful Korean messages for short password, duplicate email, invalid login, missing auth, conflicts, and server errors.
- Updated frontend engagement API loading to use split endpoints `/api/users/me/liked-contents` and `/api/users/me/liked-places`, with the old `/api/users/me/likes` as compatibility fallback.
- Added backend `/api/users/me/history` as a simple aggregate of routes, liked IDs, visit status, and authored posts.
- Added duplicate post-report protection returning `409 CONFLICT`.
- Added basic notification inserts for post likes and comments, plus existing `GET /api/notifications`.
- Updated notification screen to load backend notifications instead of static local mock items.
- Added backend-local auth HTTP checks in `backend/api-tests/auth-flow.http`.
- Replaced the corrupted root `README.md` with current backend/frontend run, DB reset, demo account, and smoke-test instructions.

### Verification result
- `backend/.\\mvnw.cmd clean package`: passed.
- `frontend npm run build`: sandbox run failed with `spawn EPERM`; approved external execution passed with the existing large chunk warning.
- `frontend npm run smoke:course`: passed.
- `frontend npm run lint`: passed.
- Current built backend JAR was run on `--server.port=18080` to avoid an existing 8080 Java process. The full critical API flow passed:
  - short password -> expected 400
  - signup -> OK
  - duplicate signup -> expected 409
  - invalid login -> expected 401
  - login -> OK
  - users/me, contents, places -> OK
  - AI route generation -> OK
  - route save -> OK
  - my routes, split likes, visit status -> OK
  - posts list/create, invalid post 400, comment create -> OK
  - report create, duplicate report 409 -> OK
  - missing post 404, notifications, history -> OK

### Remaining limitations
- Port 8080 was already occupied by an existing Java process during verification; final smoke used port 18080 for the freshly built JAR.
- Reports are still post-target reports in the current table shape, with response fields exposing `targetType=POST` and `status=PENDING`.
- Notification read state remains local UI-only; no mark-read backend endpoint yet.
- Real Kakao, OAuth, OpenAI/RAG, uploads, comment likes/reports, and advanced admin remain out of scope.

## 2026-05-28 Backend Support for Empty/List States

### Task chosen
- Support the frontend UI polish pass with minimal backend changes for empty states, counts, notification read state, and response consistency.

### Frontend support requests reviewed
- No required backend change was blocking the empty-state UI work.
- Optional notification mark-read persistence was useful and small enough for this pass.
- Scrap/bookmark persistence for My Page remains a larger backend contract and was intentionally left out.

### Changes made
- Kept existing list response bodies as arrays for compatibility, while adding count metadata via response headers:
  - `X-Total-Count` on saved routes, posts, comments, notifications, user posts, liked contents, liked places, and visit-status lists.
  - `X-Unread-Count` on notifications.
- Exposed the count headers through CORS.
- Added notification read APIs:
  - `PATCH /api/notifications/read`
  - `PATCH /api/notifications/{notificationId}/read`
- Added `GET /api/users/me/activity-summary` for empty-state counts and boolean flags:
  - saved routes, posts, liked contents, liked places, want-to-go, visited, notifications, unread notifications.
- Added additive notification compatibility fields (`id`, `subtitle`, `read`, `time`) while preserving the existing fields.
- Added count fields to the combined likes response without removing existing ID arrays.
- Updated backend smoke HTTP files with the new summary and notification checks.

### Verification result
- `backend/.\\mvnw.cmd package`: passed.
- `git diff --check` on touched backend/API-test files: passed, with only an existing LF-to-CRLF warning for `WebConfig.java`.

### Notes for frontend
- Existing array-based consumers can keep working unchanged.
- New UI can use `GET /api/users/me/activity-summary` for badge counts and empty-state decisions.
- Notification "mark all read" can call `PATCH /api/notifications/read` and then refetch `/api/notifications`.
- Per-notification read persistence can call `PATCH /api/notifications/{notificationId}/read`.

### QA focus
- Confirm empty saved routes, posts, notifications, likes, and visit-status lists return `200` with `[]`, not `500`.
- Confirm list count headers are present and readable by the browser.
- Confirm notification read endpoints update `isRead/read` and reduce `X-Unread-Count`.
- Confirm unknown notification IDs return `404`, not `500`.
- Confirm the frontend still renders existing list pages because response bodies remain arrays.

## 2026-05-29 Error Handling Stabilization

### Task chosen
- Finish backend/frontend user-friendly API error handling without starting a persistent Spring Boot server.

### Changes made
- Standardized frontend API error parsing in `frontend/src/api/error.js`, including `getUserMessage` and `getFieldErrors`.
- Prevented raw Axios/status messages from leaking to users by filtering technical messages such as `Request failed with status code 401`.
- Updated signup/login forms to use shared error parsing and frontend validation for required email, email format, required password, password length, and required nickname.
- Updated backend invalid-login mapping to return `INVALID_CREDENTIALS` with `가입되지 않았거나 비밀번호가 올바르지 않습니다.`
- Added report reason validation and mapped missing report reason to `신고 사유를 선택해 주세요.`
- Removed corrupted legacy commented localStorage code from `uiStore.js`.
- Added `backend/api-tests/error-cases.http` for common validation/auth/route/community error cases.
- Stopped leftover Java/Spring Boot processes that were locking the backend target JAR.

### Verification result
- `frontend npm run smoke:errors`: passed.
- `frontend npm run smoke:course`: passed.
- `frontend npm run lint`: passed.
- `frontend npm run build`: sandbox run failed with esbuild `spawn EPERM`; approved external execution passed with the existing large chunk warning.
- `backend .\\mvnw.cmd -Dtest=GlobalExceptionHandlerTest test`: passed.
- `backend .\\mvnw.cmd clean package`: passed after stopping the leftover Java process that held the target JAR lock.
- Confirmed no remaining Java process via `jps -lvm` and no listener on ports 8080/18080.

### Remaining limitations
- Live browser/API QA was not run in this pass to avoid starting a long-running backend process.
- `backend/api-tests/error-cases.http` should be used manually against a locally started backend for live response checks.
- Real OpenAI/RAG and direct FastAPI integration remain out of scope.
