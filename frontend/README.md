# Skillink — frontend

Single browser-responsive [Next.js](https://nextjs.org) application for the Intelligent Services Marketplace (Skillink). One layout adapts to the viewport; there is no separate desktop or mobile build.

## Getting Started

Install dependencies, then run the development server:

```bash
npm install
npm run dev
```

Open [http://localhost:3000](http://localhost:3000) and resize the browser to see the layout adapt between the wide multi-pane view and the narrow stacked view.

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
