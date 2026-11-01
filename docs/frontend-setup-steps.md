# Frontend setup steps

The frontend is a single browser-responsive Next.js application (React, TypeScript, Tailwind CSS, ESLint). It renders one layout that adapts to the viewport, so there is no separate desktop or mobile build. Run it locally with npm and connect it to the Spring Boot backend through the configured API rewrite.

## 1. Install prerequisites

Install Node.js 24 LTS, which includes npm. Verify:

```bash
node --version
npm --version
```

## 2. Install project dependencies

Use the existing `frontend/` project. From the repository root:

```bash
cd frontend
npm install
```

`npm install` installs the versions recorded in `package-lock.json` and adds any packages listed in `package.json` (for example `lucide-react` for icons). Use `npm ci` in CI or Docker builds that start from a clean checkout. Use npm consistently and commit the lockfile when intentionally updating dependencies.

## 3. Start the development server

From `frontend/`:

```bash
npm run dev
```

Open `http://localhost:3000`. Changes to `app/page.tsx` are reflected in the browser during development. Shared layout, fonts, and global styling live in `app/layout.tsx` and `app/globals.css`.

Application code is organized by responsibility:

- `app/` — route entry (`page.tsx`) and root layout.
- `components/` — UI primitives (`ui/`), shared domain cards and the lead detail sheet (`common/`), and the screen views plus app shell (`views/`).
- `context/` — global application state (`AppContext`).
- `models/`, `services/`, `data/`, `types/`, `theme/` — domain models, mock data services, fixtures, shared types, and design tokens.

The current frontend setup does not require an environment file. Database credentials belong to the backend environment and should not be added to frontend code.

## 4. Connect to the backend

For features that call the backend, start PostgreSQL and Spring Boot in a separate terminal using the [backend setup steps](backend-setup-steps.md). The UI currently runs on local mock data services, so it renders without the backend.

The existing `next.config.ts` forwards requests matching `/api/:path*` to `http://localhost:8080/api/:path*`:

```text
Browser → Next.js :3000/api/... → Spring Boot :8080/api/...
```

Browser-side API calls should use relative paths such as `/api/requests`. Spring Boot must provide the corresponding endpoint; the rewrite only forwards requests. Business rules and database access remain in the backend.

Restart the development server after changing the rewrite configuration. WebSocket routing will be configured separately when chat is implemented.

## 5. Verify the setup

Check that the Skillink workspace loads at `http://localhost:3000`. Resize the browser to confirm the layout adapts between the wide multi-pane view and the narrow stacked view. To verify the backend independently, open `http://localhost:8080/actuator/health` and confirm the response contains `"status":"UP"`.

The Actuator health URL is outside `/api/`, so the current rewrite does not forward it. A full frontend-to-backend check requires an implemented `/api/...` endpoint and a frontend request to it.

If an API request fails, check that Spring Boot is running on port `8080`, that the requested endpoint exists, and that Next.js was restarted after configuration changes. If port `3000` is occupied, check the terminal output for the actual development URL.

## 6. Run quality checks and stop the server

From `frontend/`, run:

```bash
npm run lint
npm run build
```

To preview the production build, stop the development server and run:

```bash
npm run start
```

Stop either server with `Ctrl+C`. Generated directories such as `node_modules/` and `.next/` stay local and are excluded from Git.
