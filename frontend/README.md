# Skillink — frontend

Single browser-responsive [Next.js](https://nextjs.org) application for the Intelligent Services Marketplace (Skillink). One layout adapts to the viewport; there is no separate desktop or mobile build.

## Getting Started

Install dependencies, then run the development server:

```bash
npm install
npm run dev
```

Open [http://localhost:3000](http://localhost:3000) and resize the browser to see the layout adapt between the wide multi-pane view and the narrow stacked view.

## Public landing page and authentication

The `/` entry checks the session through the existing `AuthProvider`. Visitors without a session see the Romanian landing page; authenticated users enter the existing workspace. While an existing session is being verified, the page displays a loading message. Logout and expired sessions return to the public page.

`components/landing/LandingPage.tsx` contains the service-request introduction, client steps, professional credit explanation and calls to action. `PublicHeader.tsx` provides section links and a mobile menu. Both use the existing design tokens and UI components.

Login and registration use the existing `AuthModal`, not separate `/login` or `/register` routes. Client and professional CTAs preselect the corresponding registration role, which visitors can change. The native dialog supports keyboard focus containment, Escape to close, focus restoration and scrolling on small screens. Successful authentication updates `AuthProvider` and opens the workspace without introducing another routing or session mechanism.

The landing page does not mount `AppProvider` or fetch marketplace data. It works without the backend for visitors without a stored session. Login, registration and session verification still require the real `/api/auth/*` endpoints through the existing Next.js rewrite. Workspace functionality and authorization remain owned by the existing frontend and backend; the landing page does not advertise AI, simulated chat, platform statistics or guaranteed project awards.

### Manual verification

1. Run `npm run dev -- --port 3001` if Docker already occupies port 3000. Open `http://localhost:3001` in a browser profile without a session.
2. Check widths of 320, 375, 768, 1024 and 1440 pixels for readable text, no horizontal overflow, visible CTAs and working section links. On phones, open and close the navigation menu and use its login action.
3. Use Tab to reach the skip link and all navigation controls. Open client registration and professional registration; confirm the matching role is selected. Switch between login and registration, close with Escape and confirm focus returns to the trigger. At small heights, ensure the entire form can be scrolled.
4. With the backend running, log in using an existing account. Confirm the workspace opens, refresh preserves the verified session, and logout returns to the landing page. Check invalid credentials display the existing API error. Without the backend, confirm the public page still loads and auth errors stay in the dialog.
5. Check the browser console and repeat with reduced motion and the `dark` root class enabled.

Run `npm run lint`, `npx tsc --noEmit` and `npm run build`. If Turbopack cannot bind its worker port in a restricted environment, `npm run build -- --webpack` provides an alternative production build check without changing project configuration. Google Fonts must be reachable during the build. Focused navigation tests use Node's built-in test runner; browser checks remain a separate verification step.

## Role-based workspace navigation

`config/navigation.ts` owns the tab order, labels, defaults, header actions and pure access checks. Roles come from the existing authenticated profile; `UserRole` reuses `AuthRole`. There are no new routes or API endpoints.

| Role | Tabs, in navigation order | Header actions |
| --- | --- | --- |
| Client | Cererile Mele, AI Architect, Oferte & Chat, Profil Client | Cerere Nouă |
| Professional | Oportunități, Conversații, Sesizări, Analitice, Profil & Rating | Credit balance and professional wallet |
| Administrator | Audit & Moderare Lead-uri, Centru Arbitraj, Metrici Platformă, Profil Admin | No professional checkout or request creation |
| Guest | Public landing page | Existing login and registration dialogs |

`AppContext.setActiveTab` resolves forbidden destinations to the role's default (`leads`). The exposed active tab is also resolved during rendering, and `App` independently checks the current authenticated role before selecting a view. These synchronous checks prevent a flash of forbidden content and do not require redirect effects. Indirect chat/dispute callbacks are guarded, and unsupported actions are omitted from lead views. No recognized role means no workspace access. The homepage keeps the existing session-loading screen and shows an unsupported-role state with logout when necessary.

The provider is keyed by user ID and role, resetting workspace state on account/role changes. Logout or an expired session unmounts the workspace through the existing AuthProvider boundary. Refresh continues to verify the token with `/api/auth/me` and starts at `leads`.

Desktop and mobile render the same ordered navigation configuration. The mobile disclosure closes on selection and Escape and restores focus to its trigger. Active items use `aria-current`; controls have visible focus indicators. Desktop links begin at `xl` to accommodate the longer role-specific labels; narrow screens put credit/request actions in the dropdown.

Only professionals may mount or open `WalletModal`, including via checkout return parameters or insufficient-credit callbacks. The wallet explains the administrator-verification requirement. The backend checks `proVerified`, which is not exposed in `UserProfileDto`; `verifiedBadge` is a separate field and must not be used as purchase eligibility. The frontend therefore preserves the existing checkout request and error handling rather than incorrectly disabling verified accounts. The backend remains authoritative for verification, purchase eligibility and credit grants; Stripe processing is unchanged. An explicit eligibility field would be needed to disable purchases before requesting checkout.

AI Architect, local chat and partial analytics remain existing experiences, not completed SalaBot, WebSocket or dashboard integrations. Administrator manual lead validation and bulk-refund placeholders are disabled and explicitly marked unavailable; the existing dispute arbitration API remains available.

### Navigation verification

```bash
npm run test:navigation
npm run lint
npx tsc --noEmit
npm run build
```

The navigation suite uses Node 22.6+ type stripping, the built-in test runner, existing TypeScript and React server rendering. It checks exact role matrices, unknown roles, header actions, forbidden/stale destinations, actual workspace render gates, desktop/mobile markup, homepage auth boundaries and the wallet gate. Explicit auth/context fixtures isolate components; these are not backend or browser end-to-end tests. No additional test dependencies are installed.

For manual checks, start the frontend and backend, sign in with each real role, and verify the matrix above at 320, 375, 768, 1024 and 1440 pixels. Test the menu with Tab/Escape, selection and focus restoration; try role-restricted entry points from lead details. Verify logout, refresh, expired sessions, checkout return handling and both verified/unverified professional accounts. Never use browser storage edits as proof of successful authentication.

## Project structure

```text
app/            Route entry (page.tsx), root layout and global styles
components/
  views/        App shell and screen views (Header, App, LeadFeed, LeadDetail, AIArchitect,
                ChatView, DisputesView, AnalyticsView, WalletModal)
  common/       Shared domain pieces (Lead Passport, Explainable Match, Profile, Lead detail sheet)
  ui/           Design-system primitives (Button, Badge, Input, Skeleton)
context/        Global application state (AppContext, useApp)
models/         Domain models (Lead, Profile, Rating protocol)
services/       Caching and mock data services
data/           Seed fixtures
types/          Shared TypeScript contracts
theme/          Design tokens
```

The UI runs on local mock data services today; swap the service implementations for the Spring Boot API through the `/api/:path*` rewrite in `next.config.ts`.

## Quality checks

```bash
npm run lint
npm run build
```

See [docs/frontend-setup-steps.md](../docs/frontend-setup-steps.md) for the full setup and backend connection guide.
