# Week 3 Demo Flow

This flow assumes:

- Spring backend is running at `http://localhost:8080`.
- Vue frontend is running at `http://127.0.0.1:5173`.
- MySQL was reset with `backend/src/main/resources/schema.sql` and `data.sql`.
- Demo user exists through signup: `demo@scenik.com` / `password123`.

## Flow

1. Sign up or log in.
   - Use `demo@scenik.com` / `password123`.
   - Confirm `/api/users/me` returns the user.

2. View content list.
   - Open Explore or call `GET /api/contents`.
   - Confirm content cards use `contentId`, `title`, `thumbnailUrl`, and counts.

3. View content detail.
   - Open a content card or call `GET /api/contents/{contentId}`.

4. View related places.
   - In the content detail screen, open related places.
   - API: `GET /api/contents/{contentId}/places`.

5. View place detail.
   - Open a related place.
   - API: `GET /api/places/{placeId}`.

6. Like place.
   - Tap the like action.
   - API: `POST /api/places/{placeId}/likes`.
   - Refresh or re-enter the page and confirm liked state if supported by the current screen.

7. Mark place as `WANT_TO_GO`.
   - API: `PUT /api/places/{placeId}/visit-status`.
   - Body: `{ "status": "WANT_TO_GO" }`.
   - Confirm `GET /api/users/me/visit-status`.

8. Generate AI route.
   - Use AI Course flow.
   - API: `POST /api/ai/routes`.
   - Confirm response uses verified DB `placeId` values.

9. Save route.
   - Save from AI result.
   - API: `POST /api/routes`.

10. View saved route.
   - Open My Courses or My Page saved routes.
   - API: `GET /api/users/me/routes`, then `GET /api/routes/{routeId}`.

11. Create community post.
   - API: `POST /api/posts`.

12. Add comment.
   - API: `POST /api/posts/{postId}/comments`.

13. Check My Page activity.
   - Saved route, liked place, visit status, and authored community posts are backend-backed.

## Demo Risks

- Community scrap state has no backend contract yet.
- Use the deterministic Spring `/api/ai/routes` path for the demo. `sceniK-ai` exists as a future internal service skeleton only.
