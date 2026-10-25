---
name: ui-skill
description: >-
  Use this skill whenever creating, modifying, or reviewing frontend user interfaces,
  pages, components, layouts, and styles for the Marketplace application.
  Enforces the exact design tokens, typography, color palettes, and component patterns
  established in the mock-ui design system.
---

# Marketplace UI Design System Skill

This skill defines the complete visual design system, token rules, component contracts, and styling conventions for the Intelligent Services Marketplace. All frontend development in `frontend/` must adhere strictly to these patterns to maintain exact parity with `mock-ui`.

---

## 1. Core Visual Philosophy

The Marketplace design language is built on four visual pillars:
1. **Vibrant & Energetic Accents**: High-contrast, radiant gradients (Electric Indigo, Vivid Emerald, Amber Tangerine, Neon Violet) set against clean, breathable slate surfaces.
2. **Glassmorphism & Depth**: Translucent surfaces with background blur (`backdrop-blur-md`), subtle borders (`border-slate-200/90 dark:border-slate-800`), and layered drop shadows (`shadow-sm`, `shadow-md`, `shadow-2xs`).
3. **First-Class Dark Mode**: Every layout, card, button, and input provides symmetric, high-contrast dark mode classes (`dark:bg-slate-950`, `dark:border-slate-800`, `dark:text-slate-100`).
4. **Desktop & Mobile Parity**: Seamless dual-mode execution with dedicated responsive layouts: desktop multi-pane views and mobile app-like experiences (bottom nav bar, action sheets, compact lead cards).

---

## 2. Complete Design Tokens Directory

All styling classes must align with the centralized design tokens defined in:
[design-tokens.ts](file:///Users/theo/Documents/ingineria-programarii-2/.agents/skills/ui-skill/references/design-tokens.ts)

### 2.1 Gradients & Brand Palette

| Token Name | Tailwind Classes | Usage Context |
| :--- | :--- | :--- |
| `primaryGradient` | `bg-gradient-to-r from-indigo-600 via-indigo-500 to-violet-600` | Primary CTA buttons, brand badges, active states |
| `primaryGradientHover` | `hover:from-indigo-500 hover:via-indigo-400 hover:to-violet-500` | Primary hover state |
| `successGradient` | `bg-gradient-to-r from-emerald-500 to-teal-600` | Unlock Lead, Credit purchase, Positive balances |
| `successGradientHover` | `hover:from-emerald-400 hover:to-teal-500` | Success hover state |
| `warmGradient` | `bg-gradient-to-r from-amber-500 to-orange-500` | Urgent leads, High-budget highlights, Warnings |
| `warmGradientHover` | `hover:from-amber-400 hover:to-orange-400` | Warm hover state |
| `accentGradient` | `bg-gradient-to-r from-violet-600 via-fuchsia-600 to-pink-500` | AI features, AI Architect badges, Special promotions |

### 2.2 Surfaces & Backgrounds

- **Global App Background**:
  ```tsx
  className="min-h-screen bg-gradient-to-b from-indigo-50/50 via-slate-50 to-white dark:from-slate-950 dark:via-slate-900 dark:to-slate-950"
  ```
- **Vibrant Mesh Ambient Gradient** (applied to root containers):
  ```css
  .bg-vibrant-mesh {
    background-image: 
      radial-gradient(at 0% 0%, rgba(99, 102, 241, 0.12) 0px, transparent 50%),
      radial-gradient(at 100% 0%, rgba(168, 85, 247, 0.12) 0px, transparent 50%),
      radial-gradient(at 50% 100%, rgba(16, 185, 129, 0.08) 0px, transparent 50%);
  }
  .dark .bg-vibrant-mesh {
    background-image: 
      radial-gradient(at 0% 0%, rgba(99, 102, 241, 0.22) 0px, transparent 50%),
      radial-gradient(at 100% 0%, rgba(168, 85, 247, 0.18) 0px, transparent 50%),
      radial-gradient(at 50% 100%, rgba(16, 185, 129, 0.12) 0px, transparent 50%);
  }
  ```
- **Standard Card Container**:
  ```tsx
  className="bg-white dark:bg-slate-900/90 border border-slate-200/90 dark:border-slate-800 shadow-sm rounded-2xl"
  ```
- **Interactive Card Container (Clickable / Hoverable)**:
  ```tsx
  className="bg-white dark:bg-slate-900/90 border border-slate-200/90 dark:border-slate-800 hover:border-indigo-400 dark:hover:border-indigo-600 shadow-sm hover:shadow-md transition-all duration-200 rounded-2xl cursor-pointer"
  ```
- **Glass Header / Navigation**:
  ```tsx
  className="bg-white/85 dark:bg-slate-950/85 backdrop-blur-md border-b border-slate-200/80 dark:border-slate-800/80 sticky top-0 z-40 shadow-2xs"
  ```
- **Subtle Inset Well / Info Box**:
  ```tsx
  className="bg-slate-50/90 dark:bg-slate-900/50 border border-slate-200/80 dark:border-slate-800 rounded-xl p-3 sm:p-4"
  ```

### 2.3 Typography & Numbers
- **Font Sans**: `Plus Jakarta Sans`, system-ui, -apple-system, sans-serif
- **Font Mono**: `JetBrains Mono`, ui-monospace, monospace
- **Tabular Numerics (`tabular-nums`)**: MUST be applied to all numerical values (credits, currency in RON, dates, percentages, match scores).
- **Hierarchy Scale**:
  - `Hero / Page Title`: `text-2xl sm:text-3xl font-extrabold tracking-tight text-slate-900 dark:text-white`
  - `Section Title`: `text-lg sm:text-xl font-bold tracking-tight text-slate-900 dark:text-white`
  - `Card Title`: `text-base font-semibold text-slate-800 dark:text-slate-100`
  - `Body Text`: `text-sm text-slate-600 dark:text-slate-300 leading-relaxed`
  - `Metadata / Caption`: `text-xs font-medium text-slate-400 dark:text-slate-500`
  - `Badge / Tag Label`: `text-[11px] font-semibold tracking-wide uppercase`

---

## 3. Reusable UI Components Catalog

Detailed component specifications and prop signatures are recorded in:
[component-catalog.md](file:///Users/theo/Documents/ingineria-programarii-2/.agents/skills/ui-skill/references/component-catalog.md)

### 3.1 Button Specification (`Button.tsx`)
All buttons must use the `active:scale-[0.98]` micro-interaction and provide loading spinner feedback:
- **Primary**: `bg-gradient-to-r from-indigo-600 to-violet-600 hover:from-indigo-500 hover:to-violet-500 text-white font-semibold shadow-sm hover:shadow-indigo-500/20`
- **Success**: `bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-500 hover:to-teal-500 text-white font-semibold shadow-sm hover:shadow-emerald-500/20`
- **Warm**: `bg-gradient-to-r from-amber-500 to-orange-500 hover:from-amber-400 hover:to-orange-400 text-white font-semibold shadow-sm hover:shadow-amber-500/20`
- **Secondary**: `bg-slate-100 hover:bg-slate-200 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-800 dark:text-slate-100 font-medium border border-slate-200 dark:border-slate-700`
- **Outline**: `border border-slate-300 dark:border-slate-700 hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-700 dark:text-slate-200 font-medium`
- **Ghost**: `text-slate-600 hover:text-slate-900 dark:text-slate-400 dark:hover:text-white hover:bg-slate-100 dark:hover:bg-slate-800/60 font-medium`

Sizes:
- `xs`: `px-2.5 py-1 text-[11px] rounded-lg gap-1`
- `sm`: `px-3 py-1.5 text-xs rounded-lg gap-1.5`
- `md`: `px-4 py-2 text-xs sm:text-sm rounded-xl gap-2`
- `lg`: `px-5 py-2.5 text-sm sm:text-base rounded-xl gap-2.5`

### 3.2 Badge Specification (`Badge.tsx`)
Pill badges format: `inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[11px] font-semibold whitespace-nowrap border`:
- `emerald`: `bg-emerald-50 text-emerald-700 dark:bg-emerald-950/70 dark:text-emerald-300 border-emerald-200/80 dark:border-emerald-800/60`
- `indigo`: `bg-indigo-50 text-indigo-700 dark:bg-indigo-950/70 dark:text-indigo-300 border-indigo-200/80 dark:border-indigo-800/60`
- `amber`: `bg-amber-50 text-amber-700 dark:bg-amber-950/70 dark:text-amber-300 border-amber-200/80 dark:border-amber-800/60`
- `violet`: `bg-violet-50 text-violet-700 dark:bg-violet-950/70 dark:text-violet-300 border-violet-200/80 dark:border-violet-800/60`
- `rose`: `bg-rose-50 text-rose-700 dark:bg-rose-950/70 dark:text-rose-300 border-rose-200/80 dark:border-rose-800/60`

### 3.3 Form Inputs (`Input.tsx`)
Standardized inputs with icon prefix support:
```tsx
// Base input styling
className="w-full px-3.5 py-2.5 rounded-xl text-xs sm:text-sm bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-white placeholder-slate-400 shadow-2xs focus:outline-none focus:ring-2 focus:ring-indigo-500/30 focus:border-indigo-500 dark:focus:border-indigo-400 transition-all duration-150"

// With left icon:
className="pl-10 pr-3.5 py-2.5 rounded-xl ..."

// Error state:
className="border-rose-500 focus:ring-rose-500/30 focus:border-rose-500"
```

---

## 4. Specialized Domain Components

### 4.1 Lead Passport Card (`LeadPassportCard.tsx`)
Renders the 4 validation signals of a verified lead:
1. **Completeness Score**: Progress bar with percentage (e.g., `92%`), color-coded (`emerald` if >80%, `amber` if 60-80%, `rose` if <60%).
2. **Coherence Status**: Badge showing `Excelentă`, `Bună`, or `Necesită clarificări`.
3. **Contact Verified**: Green checkmark icon with `Număr & Email Verificat`.
4. **Duplicate Risk**: Shield badge with `Risc Duplicat Scăzut (< 2%)`.
5. **Required Deliverables**: Clean bulleted list with mini check icons.

### 4.2 Explainable Match Card (`ExplainableMatchCard.tsx`)
Explains why this specific professional was matched with this lead:
- Overall compatibility score circle (e.g., `98% Potrivire`).
- Match factors list:
  - Geographic distance (e.g., `În aria ta de acoperire: București Sector 1 (3.2 km)`).
  - Category fit (e.g., `Specialitate: Tablouri KNX & Instalații Trifazate`).
  - Budget alignment (e.g., `Buget client compatibil: 3.500 - 5.000 RON`).
  - Response time rating (e.g., `Timp mediu răspuns: < 10 minute`).

### 4.3 Gated Contact Information Pattern
Before lead purchase by professional:
```tsx
<div className="p-4 rounded-xl bg-slate-50 dark:bg-slate-900/60 border border-slate-200/80 dark:border-slate-800">
  <div className="flex items-center justify-between">
    <div className="space-y-1">
      <div className="flex items-center gap-2 text-slate-400 text-xs font-medium">
        <Lock className="w-3.5 h-3.5" />
        <span>Date contact client protejate</span>
      </div>
      <p className="text-sm font-mono text-slate-500 tracking-wider">+40 7•• ••• ••• • client@•••••••.ro</p>
    </div>
    <Button variant="success" size="sm" icon={<Coins className="w-4 h-4" />}>
      Deblochează (30 Credite)
    </Button>
  </div>
</div>
```

After purchase:
```tsx
<div className="p-4 rounded-xl bg-emerald-50/60 dark:bg-emerald-950/40 border border-emerald-200 dark:border-emerald-800">
  <div className="flex items-center justify-between">
    <div>
      <Badge variant="emerald" icon={<ShieldCheck className="w-3 h-3" />}>Contact Deblocat</Badge>
      <p className="text-sm font-semibold text-slate-900 dark:text-white mt-1">+40 722 554 112 • radu@exemplu.ro</p>
    </div>
    <Button variant="primary" size="sm" icon={<MessageSquare className="w-4 h-4" />}>
      Deschide Conversația
    </Button>
  </div>
</div>
```

---

## 5. Romanian UI Dictionary & Localization

All customer-facing labels, tabs, and microcopy must follow the domain dictionary:
[ui-text-dictionary.md](file:///Users/theo/Documents/ingineria-programarii-2/.agents/skills/ui-skill/references/ui-text-dictionary.md)

Key Terms:
- **Client**: `Client`
- **Professional**: `Profesionist` / `Specialist` / `Pro`
- **Lead / Request**: `Cerere de Serviciu` / `Lead` / `Oportunitate`
- **Credits**: `Credite Virtuale`
- **Unlock Contact**: `Deblochează Contact`
- **Dispute**: `Sesizare` / `Arbitraj` / `Garanție Plată`
- **AI Architect**: `Asistent AI Cerere` / `AI Architect`
