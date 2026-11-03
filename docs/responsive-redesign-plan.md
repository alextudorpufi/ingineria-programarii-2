# Comprehensive UI & Architectural Engineering Plan: Multi-Device Responsiveness, Left-Anchored Navigation, Direct Authentication, and Independent Dual-Pane Scroll Architecture

> **Document Status:** Architectural Specification & Implementation Blueprint  
> **Application:** Skillink – Intelligent Services Marketplace  
> **Target Framework:** Next.js 15 (React 19), Tailwind CSS v4, TypeScript 5  
> **Author:** Antigravity Engineering  
> **Intended Implementer:** Automated Code Agent (e.g. Swift 1.5 / Sonnet / Developer)  
> **Scope:** Full responsive overhaul across Mobile (320px–640px), Tablet (768px–1024px), Desktop (1024px–1440px), and Ultrawide (1440px+).

---

## Table of Contents
1. [Executive Summary & Problem Statement](#1-executive-summary--problem-statement)
2. [Root Cause Analysis (RCA) of Current Deficiencies](#2-root-cause-analysis-rca-of-current-deficiencies)
3. [Design System & Breakpoint Specifications](#3-design-system--breakpoint-specifications)
4. [Phase 1: Header & Navigation Bar Redesign (`Header.tsx`)](#4-phase-1-header--navigation-bar-redesign-headertsx)
5. [Phase 2: Direct Authentication Card & Complete Demo Removal (`App.tsx`, `AuthModal.tsx`, `Input.tsx`)](#5-phase-2-direct-authentication-card--complete-demo-removal-apptsx-authmodaltsx-inputtsx)
6. [Phase 3: Independent Dual-Zone Scroll Architecture on Main Page (`App.tsx`, `LeadFeed.tsx`, `LeadDetail.tsx`)](#6-phase-3-independent-dual-zone-scroll-architecture-on-main-page-apptsx-leadfeedtsx-leaddetailtsx)
7. [Phase 4: Full Site Responsiveness & Mobile Ergonomics](#7-phase-4-full-site-responsiveness--mobile-ergonomics)
8. [Step-by-Step Implementation Instructions (Turnkey for Agent Execution)](#8-step-by-step-implementation-instructions-turnkey-for-agent-execution)
9. [Verification Matrix, Testing Scenarios & Quality Gates](#9-verification-matrix-testing-scenarios--quality-gates)

---

## 1. Executive Summary & Problem Statement

The Skillink application currently exhibits four primary UI/UX and architectural bottlenecks that impair desktop usability and degrade mobile responsiveness:

1. **Top Navigation Header Bar (`Header.tsx`):**
   - An extraneous role badge (`ADMIN`, `PRO`, or `CLIENT`) is attached directly adjacent to the `Skillink` brand logo. This creates visual clutter and is redundant, as the user profile on the far right already displays the user's role and verified status.
   - The navigation bar uses CSS Flexbox with `justify-between` across three items: (1) Brand, (2) Navigation Tabs, (3) User Actions. This forces the navigation tabs into the center/right of the viewport, crowding the user actions (Dark Mode Toggle, Credits Balance, Profile Avatar, and Logout).
   - **Requirement:** Remove the role badge from the logo bar and shift the navigation tabs immediately to the **left** (right next to the logo). Align user actions to the far right using `ml-auto`.

2. **Login & Authentication Experience (`App.tsx` & `AuthModal.tsx`):**
   - When an unauthenticated visitor arrives (`!user`), the app renders an intermediate promotional hero screen ("Conectează-te la marketplace") featuring technical feature cards and a button that opens a popup modal. This adds unnecessary friction.
   - The login/register form contains demo credentials and an auto-fill button (`Cont demo: skilling@contact.ro / admin`), which is unsuitable for a clean production interface.
   - Input fields suffer from mobile keyboard quirks: mobile browsers capitalize the first letter of emails or add trailing whitespace, which causes backend Spring Boot `@Email` validation to reject the request with `400 Bad Request`.
   - On iOS Safari, input elements with `font-size < 16px` trigger an involuntary browser viewport zoom that distorts the layout.
   - **Requirement:** Eliminate the intermediate screen. Render the login/registration input fields directly on the page. Fix user inputs (auto-capitalize off, trim whitespace, prevent iOS zoom). Completely remove the "Cont demo" button, function, and all demo references.

3. **Main Leads Workspace Scroll Chaining (`LeadFeed.tsx` & `LeadDetail.tsx`):**
   - The main workspace layout uses `md:col-span-5 h-[620px] md:h-auto` and `md:col-span-7 h-[620px] md:h-auto`. The `md:h-auto` property removes viewport height constraints on desktop viewports.
   - Neither column or parent container enforces `min-h-0`. As a consequence, whenever either column expands, the entire browser window scrolls (`window scroll`).
   - Scrolling the leads feed on the left scrolls the entire page downward, pushing the open lead detail out of view. Similarly, scrolling the lead detail moves the left feed.
   - **Requirement:** Create **two completely independent scrollable zones** (`overscroll-contain`, `overflow-y-auto`, and bounded viewport height). Scrolling the left leads list must NEVER scroll the open lead detail, and scrolling the open lead detail must NEVER scroll the leads list.

4. **Multi-Device Responsiveness (Mobile, Tablet, Desktop):**
   - On screens `< 768px`, stacking the feed and detail pane vertically results in a 1300px+ tall document where the user cannot see the active lead without scrolling past the entire list.
   - Modal headers (e.g. `WalletModal.tsx`) overflow on narrow devices (360px).
   - Touch targets for mobile buttons must be at least 44px × 44px (WCAG AAA compliant).
   - **Requirement:** Deliver a native app-like experience across all screen sizes (responsive Master-Detail toggle on mobile, responsive split view on desktop/tablet).

---

## 2. Root Cause Analysis (RCA) of Current Deficiencies

### 2.1 Header Alignment RCA
```
[CURRENT HEADER STRUCTURE (justify-between)]
+-----------------------------------------------------------------------------------------------+
| [Sparkles Skillink [ADMIN]]           [Audit  AI  Chat  Arbitraj  Metrici  Profil]    [🌙 10000cr User] |
|        (Zone 1)                                   (Zone 2 - Floating Centered)          (Zone 3)     |
+-----------------------------------------------------------------------------------------------+
Issue: Zone 2 is pushed into Zone 3 when viewport shrinks below 1400px. Bad utilization of left space.

[TARGET HEADER STRUCTURE (Grouped Left + ml-auto Right)]
+-----------------------------------------------------------------------------------------------+
| [Sparkles Skillink]  [Audit  AI  Chat  Arbitraj  Metrici  Profil]         [ml-auto]   [🌙 10000cr User] |
| <--- Left Group (Brand + NavLinks) --->                                      <--- Right Group --->    |
+-----------------------------------------------------------------------------------------------+
Result: Clean visual hierarchy, zero overlap, links anchored predictably on the left.
```

### 2.2 Dual-Zone Scroll Chaining RCA
In CSS Flexbox and CSS Grid:
1. Every flex item and grid item defaults to `min-height: auto` (CSS Flexbox Level 1 & CSS Grid Level 1 specifications).
2. If a parent container has `h-auto` or lacks an explicit height boundary along with `min-h-0`, any scrollable child container (`overflow-y-auto`) will choose to expand to fit its content rather than clip and scroll.
3. Because `App.tsx` specifies `md:h-auto` on the column wrappers, the parent `div` grows to the maximum height of its children. The outer window becomes the scroll container.
4. When the user scrolls with their mouse wheel or trackpad anywhere over the viewport, the DOM triggers `scroll` on `window`, displacing both columns simultaneously.

**The Fix:**
- Viewport boundary: `h-[calc(100dvh-4.25rem)]` on the main container.
- Explicit height propagation: `flex-1 min-h-0 flex flex-col overflow-hidden` on all intermediate containers.
- Grid constraint: `grid grid-cols-1 md:grid-cols-12 flex-1 min-h-0 h-full overflow-hidden`.
- Column isolation: `h-full min-h-0 overflow-y-auto overscroll-contain` on each column's scrollable view.

---

## 3. Design System & Breakpoint Specifications

The application uses Tailwind CSS. The responsive architecture must conform to the following viewport matrix:

| Breakpoint | Viewport Width | Header Layout | Main Page Layout | Authentication View |
| :--- | :--- | :--- | :--- | :--- |
| **`xs` (Mobile Narrow)** | `320px – 480px` | Logo only + Compact Profile / Theme. Horizontal scroll tab bar (`lg:hidden`). | Mobile Master-Detail mode: Shows Feed only. Tapping a lead displays Detail with a sticky "← Back to Leads" button. | Full-width embedded card with 16px padding. Inputs at `16px` font size. |
| **`sm` (Mobile Wide)** | `481px – 640px` | Logo + compact tabs + theme toggle. | Mobile Master-Detail mode. Fast switching between List and Detail. | Centered card (`max-w-md`), comfortable tap targets. |
| **`md` (Tablet)** | `768px – 1023px` | Header with compact tabs or horizontal scroll bar. | Side-by-Side Dual Pane (5 cols Feed / 7 cols Detail). Independent scroll zones activated. | Centered card (`max-w-md`) with glassmorphism surface. |
| **`lg` (Desktop Laptop)** | `1024px – 1279px` | Left-anchored Brand + Nav links (`gap-5`). Right actions pushed via `ml-auto`. | Side-by-Side Dual Pane (5 cols Feed / 7 cols Detail). Viewport height locked (`h-[calc(100dvh-4.25rem)]`). | Centered card with ambient gradient background. |
| **`xl` (Large Desktop)** | `1280px+` | Spacious left-anchored Brand + Nav links (`gap-8`). Max width container (`max-w-7xl`). | Side-by-Side Dual Pane. Zero global window scrollbar. Maximum ergonomic view. | Centered card with high-contrast dark mode support. |

---

## 4. Phase 1: Header & Navigation Bar Redesign (`Header.tsx`)

### 4.1 Objectives
1. **Remove Role Badge from Logo:** Strip the `<span className="text-[10px] uppercase font-bold ...">{roleLabel}</span>` from the brand link. The user's role is already clearly presented in the user avatar block on the right.
2. **Move Nav Tabs to the Left:** Change the top-level container from `justify-between` to an architecture where the logo and the navigation tabs are wrapped together in a single flex container on the left (`flex items-center gap-6 xl:gap-8`).
3. **Anchor Actions to the Right:** Push the actions container (Theme Toggle, Sold Balance, New Request button, Profile Avatar, and Logout) to the far right using `ml-auto`.
4. **Refine Mobile Tab Bar (`lg:hidden`):** Ensure the horizontal tab strip for viewports below `lg` has `[scrollbar-width:none] [&::-webkit-scrollbar]:hidden`, smooth scroll, and minimum touch target heights of 40px.

### 4.2 Exact File Modifications in `frontend/components/views/Header.tsx`

#### Change 1: Remove `ROLE_BADGE_CLASS` and unused references
- Remove `ROLE_BADGE_CLASS` from lines 15–19 (or keep only if needed elsewhere, but remove from the brand button).
- Remove `roleLabel` from lines 72–73 if not needed in the brand link.

#### Change 2: Redesign the Header Flex Container (Lines 76–115)

```tsx
// BEFORE:
<div className="max-w-7xl mx-auto px-4 sm:px-6 h-16 flex items-center justify-between gap-3 sm:gap-6">
  {/* Zone 1: Brand Wordmark (Skillink) */}
  <button
    onClick={() => setCurrentTab('leads')}
    className="group text-lg font-extrabold tracking-tight text-slate-900 dark:text-white whitespace-nowrap shrink-0 flex items-center gap-2.5 cursor-pointer"
  >
    <div className="w-8 h-8 rounded-xl bg-gradient-to-tr from-indigo-600 via-indigo-500 to-violet-500 flex items-center justify-center text-white shadow-xs group-hover:scale-105 transition-transform">
      <Sparkles className="w-4 h-4" />
    </div>
    <span>
      Skill<span className="bg-gradient-to-r from-indigo-600 to-violet-600 bg-clip-text text-transparent">ink</span>
    </span>
    <span
      className={`text-[10px] uppercase font-bold tracking-wider px-2 py-0.5 rounded-md border ${
        ROLE_BADGE_CLASS[user?.role ?? 'pro']
      }`}
    >
      {roleLabel}
    </span>
  </button>

  {/* Zone 2: Navigation Links adapting to Role */}
  <nav className="hidden lg:flex items-center gap-6 text-sm font-semibold text-slate-600 dark:text-slate-300">
    {navTabs.map(tab => ( ... ))}
  </nav>

  {/* Zone 3: Authenticated profile ... */}
  <div className="flex items-center gap-2 sm:gap-3 shrink-0">
    ...
  </div>
</div>
```

```tsx
// AFTER:
<div className="max-w-7xl mx-auto px-4 sm:px-6 h-16 flex items-center gap-6 xl:gap-8">
  {/* Left Group: Brand Wordmark + Desktop Nav Tabs Anchored to the LEFT */}
  <div className="flex items-center gap-6 xl:gap-8 shrink-0">
    <button
      onClick={() => setCurrentTab('leads')}
      className="group text-lg font-extrabold tracking-tight text-slate-900 dark:text-white whitespace-nowrap shrink-0 flex items-center gap-2.5 cursor-pointer"
    >
      <div className="w-8 h-8 rounded-xl bg-gradient-to-tr from-indigo-600 via-indigo-500 to-violet-500 flex items-center justify-center text-white shadow-xs group-hover:scale-105 transition-transform">
        <Sparkles className="w-4 h-4" />
      </div>
      <span>
        Skill<span className="bg-gradient-to-r from-indigo-600 to-violet-600 bg-clip-text text-transparent">ink</span>
      </span>
      {/* ROLE BADGE COMPLETELY REMOVED FROM LOGO */}
    </button>

    {/* Navigation Links moved immediately to the left right next to Logo */}
    <nav className="hidden lg:flex items-center gap-5 xl:gap-6 text-sm font-semibold text-slate-600 dark:text-slate-300">
      {navTabs.map(tab => (
        <button
          key={tab}
          onClick={() => setCurrentTab(tab)}
          className={`whitespace-nowrap shrink-0 transition-all py-1.5 cursor-pointer ${
            currentTab === tab
              ? 'text-indigo-600 dark:text-indigo-400 border-b-2 border-indigo-600 dark:border-indigo-400 font-bold'
              : 'hover:text-indigo-600 dark:hover:text-white'
          }`}
        >
          {getTabLabel(tab)}
        </button>
      ))}
    </nav>
  </div>

  {/* Right Group: Pushed to far right via ml-auto */}
  <div className="ml-auto flex items-center gap-2 sm:gap-3 shrink-0">
    {/* Dark mode toggle, credits balance, new request, profile avatar, logout */}
    ...
  </div>
</div>
```

---

## 5. Phase 2: Direct Authentication Card & Complete Demo Removal (`App.tsx`, `AuthModal.tsx`, `Input.tsx`)

### 5.1 Objectives
1. **Remove Intermediate Screen:** When unauthenticated (`!user`), do not show the database icon, "Conectează-te la marketplace" heading, marketing feature cards, or "Autentificare / Creare Cont" button.
2. **Direct Inline Authentication Form:** Directly display a clean, centered authentication card embedded right inside the page container. The user can immediately type their email and password and click "Autentificare" or switch to "Înregistrare" in one click.
3. **Delete Demo Account References:**
   - In `AuthModal.tsx`: Delete `fillDemoAdmin()`, the demo button with `KeyRound` icon (`Cont demo: skilling@contact.ro / admin`), and any demo credentials logic.
   - In `App.tsx`: Delete the text `Cont demo admin: skilling@contact.ro / admin`.
4. **Fix Inputs for User Experience:**
   - In `Input.tsx`: Ensure font class uses `text-base sm:text-sm` (16px base font on mobile devices to prevent iOS Safari auto-zoom).
   - In Authentication Form: Ensure `type="email"`, `inputMode="email"`, `autoCapitalize="none"`, `autoCorrect="off"`, `spellCheck={false}`.
   - In Form Submit Handler: Clean email string with `email.trim().toLowerCase()` prior to API dispatch so spaces or accidental casing do not trigger `400 Bad Request`.
   - Support `Enter` key form submission.

### 5.2 Implementation of the Direct Authentication Form in `App.tsx`

In `frontend/components/views/App.tsx`, replace lines 65–103 with the embedded direct authentication card:

```tsx
) : !user ? (
  <div className="flex-1 flex items-center justify-center p-4 sm:p-8 bg-gradient-to-b from-indigo-50/40 via-white to-white dark:from-slate-950 dark:via-slate-900 dark:to-slate-950 min-h-0 overflow-y-auto">
    <div className="w-full max-w-md bg-white dark:bg-slate-900 border border-slate-200/90 dark:border-slate-800 rounded-3xl shadow-xl p-6 sm:p-8 space-y-6">
      {/* Brand Header */}
      <div className="flex items-center gap-3">
        <div className="w-10 h-10 rounded-2xl bg-gradient-to-tr from-indigo-600 via-indigo-500 to-violet-600 text-white flex items-center justify-center shadow-xs">
          <Sparkles className="w-5 h-5" />
        </div>
        <div>
          <h2 className="text-lg font-bold text-slate-900 dark:text-white">
            {authMode === 'login' ? 'Conectare în Skillink' : 'Creare Cont Nou'}
          </h2>
          <p className="text-xs text-slate-500 dark:text-slate-400">
            {authMode === 'login'
              ? 'Introdu datele pentru accesul la cont'
              : 'Alege-ți rolul și începe lucrul pe marketplace'}
          </p>
        </div>
      </div>

      {/* Mode Selector Toggle: Login vs Register */}
      <div className="grid grid-cols-2 p-1 bg-slate-100 dark:bg-slate-800/80 rounded-xl border border-slate-200 dark:border-slate-700">
        <button
          type="button"
          onClick={() => { setAuthMode('login'); setAuthError(null); }}
          className={`py-2 text-xs font-semibold rounded-lg transition-all cursor-pointer ${
            authMode === 'login'
              ? 'bg-white dark:bg-slate-900 text-indigo-600 dark:text-white shadow-xs'
              : 'text-slate-500 hover:text-slate-900 dark:hover:text-slate-200'
          }`}
        >
          Autentificare
        </button>
        <button
          type="button"
          onClick={() => { setAuthMode('register'); setAuthError(null); }}
          className={`py-2 text-xs font-semibold rounded-lg transition-all cursor-pointer ${
            authMode === 'register'
              ? 'bg-white dark:bg-slate-900 text-indigo-600 dark:text-white shadow-xs'
              : 'text-slate-500 hover:text-slate-900 dark:hover:text-slate-200'
          }`}
        >
          Înregistrare
        </button>
      </div>

      {/* Error alert banner */}
      {authError && (
        <div
          role="alert"
          className="p-3 rounded-xl bg-rose-50 dark:bg-rose-950/50 border border-rose-200 dark:border-rose-900 flex items-center gap-2.5 text-xs text-rose-600 dark:text-rose-300"
        >
          <ShieldAlert className="w-4 h-4 shrink-0" />
          <span>{authError}</span>
        </div>
      )}

      {/* Direct Form */}
      <form onSubmit={handleDirectAuthSubmit} className="space-y-4">
        {authMode === 'register' && (
          <>
            <Input
              label="Nume Complet"
              placeholder="ex. Radu Teodorescu"
              autoComplete="name"
              leftIcon={<User className="w-4 h-4" />}
              value={authName}
              onChange={e => setAuthName(e.target.value)}
              required
            />

            <div className="space-y-1.5">
              <span className="block text-xs font-semibold text-slate-700 dark:text-slate-200">Tip Cont</span>
              <div className="grid grid-cols-2 gap-2">
                <button
                  type="button"
                  onClick={() => setAuthRole('pro')}
                  className={`p-2.5 rounded-xl border text-xs font-semibold flex items-center justify-center gap-1.5 cursor-pointer transition-all ${
                    authRole === 'pro'
                      ? 'border-indigo-600 bg-indigo-50 dark:bg-indigo-950/60 text-indigo-700 dark:text-indigo-300'
                      : 'border-slate-200 dark:border-slate-700 text-slate-600 dark:text-slate-400'
                  }`}
                >
                  <Briefcase className="w-3.5 h-3.5" />
                  Profesionist
                </button>
                <button
                  type="button"
                  onClick={() => setAuthRole('client')}
                  className={`p-2.5 rounded-xl border text-xs font-semibold flex items-center justify-center gap-1.5 cursor-pointer transition-all ${
                    authRole === 'client'
                      ? 'border-emerald-600 bg-emerald-50 dark:bg-emerald-950/60 text-emerald-700 dark:text-emerald-300'
                      : 'border-slate-200 dark:border-slate-700 text-slate-600 dark:text-slate-400'
                  }`}
                >
                  <User className="w-3.5 h-3.5" />
                  Client
                </button>
              </div>
            </div>
          </>
        )}

        <Input
          label="Adresă Email"
          type="email"
          inputMode="email"
          autoCapitalize="none"
          autoCorrect="off"
          spellCheck={false}
          placeholder="nume@exemplu.ro"
          autoComplete="email"
          leftIcon={<Mail className="w-4 h-4" />}
          value={authEmail}
          onChange={e => setAuthEmail(e.target.value)}
          required
        />

        <PasswordInput
          label="Parolă"
          placeholder="••••••••"
          value={authPassword}
          onChange={e => setAuthPassword(e.target.value)}
          showStrengthMeter={authMode === 'register'}
          autoComplete={authMode === 'register' ? 'new-password' : 'current-password'}
          required
        />

        <Button
          type="submit"
          variant="primary"
          size="lg"
          className="w-full justify-center mt-2"
          isLoading={isAuthSubmitting}
        >
          {authMode === 'login' ? 'Autentificare' : 'Creează Contul'}
        </Button>
      </form>

      {/* ZERO DEMO BUTTONS OR DEMO LABELS */}
    </div>
  </div>
) : (
```

### 5.3 Modifications in `AuthModal.tsx`
- Delete lines 47–51:
  ```tsx
  // DELETE:
  const fillDemoAdmin = () => {
    setEmail('skilling@contact.ro');
    setPassword('admin');
    setErrorMessage(null);
  };
  ```
- Delete lines 194–205:
  ```tsx
  // DELETE:
  {mode === 'login' && (
    <div className="pt-2 border-t border-slate-100 dark:border-slate-800 text-center">
      <button
        type="button"
        onClick={fillDemoAdmin}
        className="inline-flex items-center gap-1.5 text-xs text-indigo-600 dark:text-indigo-400 hover:underline font-medium cursor-pointer"
      >
        <KeyRound className="w-3.5 h-3.5" />
        Cont demo: skilling@contact.ro / admin
      </button>
    </div>
  )}
  ```
- In `handleSubmit`, sanitize inputs:
  ```tsx
  const cleanEmail = email.trim().toLowerCase();
  if (mode === 'login') {
    await login(cleanEmail, password);
  } else {
    await register({ email: cleanEmail, password, name: name.trim(), role });
  }
  ```

### 5.4 Modifications in `Input.tsx` & `designSystem.ts`
To stop iOS Safari from zooming automatically when users tap an input:
- Update `DESIGN_TOKENS.inputs.base`:
  ```ts
  base: 'w-full px-3.5 py-2.5 rounded-xl text-base sm:text-sm bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-white placeholder-slate-400 shadow-2xs focus:outline-none focus:ring-2 focus:ring-indigo-500/30 focus:border-indigo-500 dark:focus:border-indigo-400 transition-all duration-150',
  withLeftIcon: 'pl-10 pr-3.5 py-2.5 rounded-xl text-base sm:text-sm bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-white placeholder-slate-400 shadow-2xs focus:outline-none focus:ring-2 focus:ring-indigo-500/30 focus:border-indigo-500 dark:focus:border-indigo-400 transition-all duration-150',
  ```

---

## 6. Phase 3: Independent Dual-Zone Scroll Architecture on Main Page (`App.tsx`, `LeadFeed.tsx`, `LeadDetail.tsx`)

### 6.1 Architectural Principle
To achieve true two-zone independent scrolling where:
- Scrolling the left leads list does **NOT** move the open lead detail;
- Scrolling the open lead detail does **NOT** move the leads list;
- The browser window itself stays fixed without vertical page scroll;

We apply CSS isolation:

```
[MAIN VIEWPORT BOUNDARY]
height: calc(100dvh - 4.25rem);
overflow: hidden;
display: flex;
flex-direction: column;

  │
  ├──> [INNER WRAPPER CARD]
  │    flex: 1 1 0%;
  │    min-height: 0;
  │    overflow: hidden;
  │    display: flex;
  │    flex-direction: column;
  │
  └──> [GRID SPLIT VIEW] (grid-cols-1 md:grid-cols-12 flex-1 min-h-0 h-full overflow-hidden)
         │
         ├──> [LEFT COLUMN - ZONE 1: md:col-span-5 flex flex-col h-full min-h-0 overflow-hidden]
         │      ├── [Header & Search Filter - shrink-0]
         │      └── [Cards List: flex-1 min-h-0 overflow-y-auto overscroll-contain]
         │
         └──> [RIGHT COLUMN - ZONE 2: md:col-span-7 flex flex-col h-full min-h-0 overflow-hidden]
                └── [Lead Detail Content: h-full min-h-0 overflow-y-auto overscroll-contain]
```

### 6.2 Implementation in `frontend/components/views/App.tsx`

1. **Add Mobile Master-Detail State:**
   ```tsx
   const [mobileLeadView, setMobileLeadView] = useState<'feed' | 'detail'>('feed');
   ```
2. **Update Main Container and Leads Tab Rendering:**

```tsx
// Outer Container:
<main className="flex-1 max-w-7xl w-full mx-auto p-2 sm:p-4 lg:p-6 flex flex-col h-[calc(100dvh-4.25rem)] overflow-hidden">
  <div className="flex-1 bg-white/95 dark:bg-slate-900/90 rounded-2xl sm:rounded-3xl border border-indigo-100/80 dark:border-slate-800 shadow-md backdrop-blur-xs overflow-hidden flex flex-col min-h-0">
    ...
    {activeTab === 'leads' && (
      <div className="flex-1 grid grid-cols-1 md:grid-cols-12 overflow-hidden h-full min-h-0">
        {/* Zone 1: Left Leads Feed Pane */}
        <div
          className={`md:col-span-5 h-full min-h-0 flex flex-col overflow-hidden ${
            mobileLeadView === 'detail' ? 'hidden md:flex' : 'flex'
          }`}
        >
          <LeadFeed
            onOpenLeadSheet={handleOpenLeadSheet}
            onSelectLeadOnMobile={() => setMobileLeadView('detail')}
          />
        </div>

        {/* Zone 2: Right Lead Detail Pane */}
        <div
          className={`md:col-span-7 h-full min-h-0 flex flex-col overflow-hidden border-t md:border-t-0 md:border-l border-slate-200/90 dark:border-slate-800 bg-slate-50/50 dark:bg-slate-900/50 ${
            mobileLeadView === 'feed' ? 'hidden md:flex' : 'flex'
          }`}
        >
          <LeadDetail
            onBackToFeed={() => setMobileLeadView('feed')}
            onOpenChat={handleOpenChat}
            onOpenDispute={handleOpenDispute}
          />
        </div>
      </div>
    )}
```

### 6.3 Implementation in `frontend/components/views/LeadFeed.tsx`

1. Prop signature update:
   ```tsx
   interface LeadFeedProps {
     onOpenLeadSheet?: (leadId: string) => void;
     onSelectLeadOnMobile?: () => void;
   }
   ```
2. Outer container constraint (Line 66):
   ```tsx
   <div className="h-full min-h-0 flex flex-col md:border-r border-slate-200/90 dark:border-slate-800 bg-white dark:bg-slate-950 overflow-hidden">
   ```
3. Top Search & Filter Bar: Add `shrink-0` (Line 68):
   ```tsx
   <div className="shrink-0 p-4 border-b border-slate-200/90 dark:border-slate-800 space-y-3 bg-gradient-to-b from-indigo-50/40 to-white dark:from-slate-900/60 dark:to-slate-950">
   ```
4. Feed List scroll container (Line 251):
   ```tsx
   <div className="flex-1 min-h-0 overflow-y-auto overscroll-contain divide-y divide-slate-100 dark:divide-slate-800/80">
   ```
5. On card click:
   ```tsx
   onClick={() => {
     setActiveLeadId(lead.id);
     onSelectLeadOnMobile?.();
   }}
   ```

### 6.4 Implementation in `frontend/components/views/LeadDetail.tsx`

1. Prop signature update:
   ```tsx
   interface LeadDetailProps {
     onBackToFeed?: () => void;
     onOpenChat: (leadId: string) => void;
     onOpenDispute: (leadId: string) => void;
   }
   ```
2. Scroll isolation and mobile back button:
   ```tsx
   return (
     <div className="h-full min-h-0 overflow-y-auto overscroll-contain p-4 sm:p-6 space-y-6">
       {/* Mobile-Only Back Bar (< md) */}
       {onBackToFeed && (
         <div className="md:hidden pb-2 border-b border-slate-200/90 dark:border-slate-800 flex items-center justify-between">
           <button
             type="button"
             onClick={onBackToFeed}
             className="inline-flex items-center gap-1.5 text-xs font-bold text-indigo-600 dark:text-indigo-400 bg-indigo-50 dark:bg-indigo-950/60 px-3 py-1.5 rounded-xl border border-indigo-200 dark:border-indigo-800 cursor-pointer active:scale-95 transition-transform"
           >
             <ArrowLeft className="w-3.5 h-3.5" />
             Înapoi la lista de cereri
           </button>
           <span className="text-[11px] font-mono text-slate-400">{activeLead.id}</span>
         </div>
       )}

       {/* Top Header ... */}
   ```

---

## 7. Phase 4: Full Site Responsiveness & Mobile Ergonomics

### 7.1 Viewport Heights on Secondary Tabs (`AIArchitect`, `ChatView`, `DisputesView`, `AnalyticsView`, `ProfileView`)
Currently, `App.tsx` has:
```tsx
<div className="flex-1 h-[720px] overflow-hidden">
```
On screens with viewport heights lower than 850px, this introduces double scrollbars.
**Fix:** Replace all fixed `h-[720px]` with `flex-1 min-h-0 h-full overflow-hidden`. Each view component handles its internal scrolling smoothly.

### 7.2 `WalletModal.tsx` Header Wrap on Small Phones (320px–375px)
In `WalletModal.tsx` lines 149–174:
- Add `flex-wrap` and `gap-3` to the header.
- On screens `< 640px`, allow the balance badge to sit underneath the title or stack neatly so the modal close button is never pushed off-screen.

### 7.3 Touch Targets and Accessibility
- All clickable buttons and interactive tab elements must maintain a minimum target size of `44px × 44px` on mobile.
- `overscroll-contain` applied on all scroll containers prevents pull-to-refresh interference and body scroll chaining.

---

## 8. Step-by-Step Implementation Instructions (Turnkey for Agent Execution)

Follow these phases sequentially:

### Step 1: Update `Header.tsx`
1. Open [Header.tsx](file:///Users/theo/Documents/ingineria-programarii-2/frontend/components/views/Header.tsx).
2. Remove the role badge tag `<span className="text-[10px] uppercase font-bold tracking-wider px-2 py-0.5 rounded-md border ...">{roleLabel}</span>` inside the brand `<button>`.
3. Wrap the brand `<button>` and the `<nav className="hidden lg:flex ...">` in a shared left container:
   ```tsx
   <div className="flex items-center gap-6 xl:gap-8 shrink-0">
   ```
4. On the right-side actions `<div>`, replace `flex items-center gap-2 sm:gap-3 shrink-0` with:
   ```tsx
   <div className="ml-auto flex items-center gap-2 sm:gap-3 shrink-0">
   ```
5. Ensure mobile tab strip has smooth horizontal scroll with hidden scrollbar.

### Step 2: Update `Input.tsx` & `designSystem.ts`
1. Open [designSystem.ts](file:///Users/theo/Documents/ingineria-programarii-2/frontend/theme/designSystem.ts).
2. In `DESIGN_TOKENS.inputs.base` and `DESIGN_TOKENS.inputs.withLeftIcon`, change `text-xs sm:text-sm` to `text-base sm:text-sm`.
3. Open [Input.tsx](file:///Users/theo/Documents/ingineria-programarii-2/frontend/components/ui/Input.tsx) and ensure full passthrough of `inputMode`, `autoCapitalize`, and `spellCheck`.

### Step 3: Clean Demo References in `AuthModal.tsx`
1. Open [AuthModal.tsx](file:///Users/theo/Documents/ingineria-programarii-2/frontend/components/auth/AuthModal.tsx).
2. Delete `fillDemoAdmin()` function (lines 47–51).
3. Delete the demo admin button and its wrapper (lines 194–205).
4. Update `handleSubmit` to call `email.trim().toLowerCase()`.
5. Ensure the email input has `type="email"`, `inputMode="email"`, `autoCapitalize="none"`, `autoCorrect="off"`, and `spellCheck={false}`.

### Step 4: Implement Direct Login & Responsive Viewport in `App.tsx`
1. Open [App.tsx](file:///Users/theo/Documents/ingineria-programarii-2/frontend/components/views/App.tsx).
2. Add state for direct auth mode:
   ```tsx
   const [directAuthMode, setDirectAuthMode] = useState<'login' | 'register'>('login');
   const [directEmail, setDirectEmail] = useState('');
   const [directPassword, setDirectPassword] = useState('');
   const [directName, setDirectName] = useState('');
   const [directRole, setDirectRole] = useState<'client' | 'pro'>('pro');
   const [isDirectSubmitting, setIsDirectSubmitting] = useState(false);
   const [directError, setDirectError] = useState<string | null>(null);
   const [mobileLeadView, setMobileLeadView] = useState<'feed' | 'detail'>('feed');
   ```
3. Replace the `!user` hero block (lines 65–103) with the direct inline authentication card.
4. Update `<main>` classes:
   `flex-1 max-w-7xl w-full mx-auto p-2 sm:p-4 lg:p-6 flex flex-col h-[calc(100dvh-4.25rem)] overflow-hidden`
5. Update the inner container:
   `flex-1 bg-white/95 dark:bg-slate-900/90 rounded-2xl sm:rounded-3xl border border-indigo-100/80 dark:border-slate-800 shadow-md backdrop-blur-xs overflow-hidden flex flex-col min-h-0`
6. Connect `mobileLeadView` to show/hide the feed vs detail on mobile (`md:hidden`).
7. Replace all fixed `h-[720px]` in remaining tabs (`ai`, `chat`, `disputes`, `analytics`, `profile`) with `flex-1 min-h-0 h-full overflow-hidden`.

### Step 5: Update `LeadFeed.tsx` and `LeadDetail.tsx` for Independent Scroll
1. Open [LeadFeed.tsx](file:///Users/theo/Documents/ingineria-programarii-2/frontend/components/views/LeadFeed.tsx).
   - Change root `<div>` class to `h-full min-h-0 flex flex-col md:border-r border-slate-200/90 dark:border-slate-800 bg-white dark:bg-slate-950 overflow-hidden`.
   - Add `shrink-0` to the filter bar header.
   - Set feed cards container to `flex-1 min-h-0 overflow-y-auto overscroll-contain divide-y divide-slate-100 dark:divide-slate-800/80`.
   - On lead card click, trigger `onSelectLeadOnMobile?.()`.
2. Open [LeadDetail.tsx](file:///Users/theo/Documents/ingineria-programarii-2/frontend/components/views/LeadDetail.tsx).
   - Set root container to `h-full min-h-0 overflow-y-auto overscroll-contain p-4 sm:p-6 space-y-6`.
   - Add sticky mobile top back button: `← Înapoi la lista de cereri` when `onBackToFeed` is provided.

### Step 6: Verify and Build
1. Run `cd frontend && npm run build` to verify TypeScript types and Tailwind CSS output.
2. Test at widths 375px, 768px, 1024px, 1440px.

---

## 9. Verification Matrix, Testing Scenarios & Quality Gates

| Test ID | Test Scenario | Expected Outcome | Verification Status |
| :--- | :--- | :--- | :--- |
| **TC-01** | Header Brand Display | Logo displays only `Skillink` with sparkles icon. **No role badge** (`ADMIN`/`PRO`/`CLIENT`) is visible next to the logo. | Planned |
| **TC-02** | Header Navigation Position | Navigation links (`Audit`, `Reguli AI`, `Mediere`, etc.) start immediately to the right of the `Skillink` logo. Right-side items are pushed to the far right via `ml-auto`. | Planned |
| **TC-03** | Unauthenticated Direct Login | Loading the app when logged out directly shows the email and password inputs inside the workspace card. No promotional hero or modal trigger button required. | Planned |
| **TC-04** | Demo Reference Removal | Zero occurrences of "Cont demo", "skilling@contact.ro", or "admin" credentials in the UI. No auto-fill demo button. | Planned |
| **TC-05** | Input Trimming & Validation | Entering an email with accidental leading/trailing spaces (e.g. `" admin@skillink.ro "`) successfully logs in without Spring Boot `@Email` 400 Bad Request. | Planned |
| **TC-06** | iOS Safari Font Size Check | All input elements have computed font-size ≥ 16px on mobile viewports (< 640px). Tapping input does not trigger involuntary viewport zoom. | Planned |
| **TC-07** | Independent Left Pane Scroll | Scrolling the mouse wheel over the leads feed cards list scrolls ONLY the leads list. The open lead on the right remains static. | Planned |
| **TC-08** | Independent Right Pane Scroll | Scrolling the mouse wheel over the open lead specifications scrolls ONLY the lead detail view. The leads feed list on the left remains static. | Planned |
| **TC-09** | Mobile Master-Detail Workflow | On mobile (< 768px), tapping a lead switches view to the full lead detail. Tapping "← Înapoi la cereri" returns smoothly to the lead feed list. | Planned |
| **TC-10** | Clean Build Verification | `npm run build` completes with 0 errors and 0 lint warnings. | Planned |
