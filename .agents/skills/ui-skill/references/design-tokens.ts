/**
 * Centralized Design System Tokens & Classes
 * Extracted from mock-ui for reusable AI code generation
 */

export const DESIGN_TOKENS = {
  colors: {
    primaryGradient: 'bg-gradient-to-r from-indigo-600 via-indigo-500 to-violet-600',
    primaryGradientHover: 'hover:from-indigo-500 hover:via-indigo-400 hover:to-violet-500',
    successGradient: 'bg-gradient-to-r from-emerald-500 to-teal-600',
    successGradientHover: 'hover:from-emerald-400 hover:to-teal-500',
    warmGradient: 'bg-gradient-to-r from-amber-500 to-orange-500',
    warmGradientHover: 'hover:from-amber-400 hover:to-orange-400',
    accentGradient: 'bg-gradient-to-r from-violet-600 via-fuchsia-600 to-pink-500',
    badgeGlow: 'shadow-xs',
  },

  surfaces: {
    appBackground: 'bg-gradient-to-b from-indigo-50/50 via-slate-50 to-white dark:from-slate-950 dark:via-slate-900 dark:to-slate-950',
    card: 'bg-white dark:bg-slate-900/90 border border-slate-200/90 dark:border-slate-800 shadow-sm rounded-2xl',
    cardHover: 'bg-white dark:bg-slate-900/90 border border-slate-200/90 dark:border-slate-800 hover:border-indigo-400 dark:hover:border-indigo-600 shadow-sm hover:shadow-md transition-all duration-200 rounded-2xl',
    glass: 'bg-white/85 dark:bg-slate-950/85 backdrop-blur-md border-b border-slate-200/80 dark:border-slate-800/80',
    subtleWell: 'bg-slate-50/90 dark:bg-slate-900/50 border border-slate-200/80 dark:border-slate-800 rounded-xl',
  },

  inputs: {
    base: 'w-full px-3.5 py-2.5 rounded-xl text-xs sm:text-sm bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-white placeholder-slate-400 shadow-2xs focus:outline-none focus:ring-2 focus:ring-indigo-500/30 focus:border-indigo-500 dark:focus:border-indigo-400 transition-all duration-150',
    withLeftIcon: 'pl-10 pr-3.5 py-2.5 rounded-xl text-xs sm:text-sm bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-white placeholder-slate-400 shadow-2xs focus:outline-none focus:ring-2 focus:ring-indigo-500/30 focus:border-indigo-500 dark:focus:border-indigo-400 transition-all duration-150',
    select: 'w-full px-3.5 py-2.5 rounded-xl text-xs sm:text-sm bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-indigo-500/30 focus:border-indigo-500 dark:focus:border-indigo-400 transition-all duration-150 cursor-pointer shadow-2xs',
    textarea: 'w-full p-3.5 rounded-xl text-xs sm:text-sm bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-indigo-500/30 focus:border-indigo-500 dark:focus:border-indigo-400 transition-all duration-150 resize-y shadow-2xs',
  },

  buttons: {
    primary: 'bg-gradient-to-r from-indigo-600 to-violet-600 hover:from-indigo-500 hover:to-violet-500 text-white font-semibold shadow-sm hover:shadow-indigo-500/20 active:scale-[0.98] transition-all duration-150 disabled:opacity-50 disabled:cursor-not-allowed',
    success: 'bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-500 hover:to-teal-500 text-white font-semibold shadow-sm hover:shadow-emerald-500/20 active:scale-[0.98] transition-all duration-150 disabled:opacity-50 disabled:cursor-not-allowed',
    warm: 'bg-gradient-to-r from-amber-500 to-orange-500 hover:from-amber-400 hover:to-orange-400 text-white font-semibold shadow-sm hover:shadow-amber-500/20 active:scale-[0.98] transition-all duration-150 disabled:opacity-50 disabled:cursor-not-allowed',
    secondary: 'bg-slate-100 hover:bg-slate-200 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-800 dark:text-slate-100 font-medium active:scale-[0.98] transition-all duration-150 border border-slate-200 dark:border-slate-700',
    outline: 'border border-slate-300 dark:border-slate-700 hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-700 dark:text-slate-200 font-medium active:scale-[0.98] transition-all duration-150',
    ghost: 'text-slate-600 hover:text-slate-900 dark:text-slate-400 dark:hover:text-white hover:bg-slate-100 dark:hover:bg-slate-800/60 font-medium transition-all duration-150',
  },

  badges: {
    emerald: 'bg-emerald-50 text-emerald-700 dark:bg-emerald-950/70 dark:text-emerald-300 border border-emerald-200/80 dark:border-emerald-800/60',
    indigo: 'bg-indigo-50 text-indigo-700 dark:bg-indigo-950/70 dark:text-indigo-300 border border-indigo-200/80 dark:border-indigo-800/60',
    amber: 'bg-amber-50 text-amber-700 dark:bg-amber-950/70 dark:text-amber-300 border border-amber-200/80 dark:border-amber-800/60',
    violet: 'bg-violet-50 text-violet-700 dark:bg-violet-950/70 dark:text-violet-300 border border-violet-200/80 dark:border-violet-800/60',
    rose: 'bg-rose-50 text-rose-700 dark:bg-rose-950/70 dark:text-rose-300 border border-rose-200/80 dark:border-rose-800/60',
  }
};
