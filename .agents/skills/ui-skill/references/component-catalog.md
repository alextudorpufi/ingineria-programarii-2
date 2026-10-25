# UI Component Catalog

This document specifies the exact component contracts, props, and implementations used across the Marketplace, based on `mock-ui`.

---

## 1. `Button` Component

```tsx
import React from 'react';
import { DESIGN_TOKENS } from '../references/design-tokens';

export type ButtonVariant = 'primary' | 'success' | 'warm' | 'secondary' | 'outline' | 'ghost';
export type ButtonSize = 'xs' | 'sm' | 'md' | 'lg';

export interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant;
  size?: ButtonSize;
  icon?: React.ReactNode;
  iconRight?: React.ReactNode;
  isLoading?: boolean;
}

export const Button: React.FC<ButtonProps> = ({
  children,
  variant = 'primary',
  size = 'md',
  icon,
  iconRight,
  isLoading = false,
  className = '',
  disabled,
  ...props
}) => {
  const sizeClasses: Record<ButtonSize, string> = {
    xs: 'px-2.5 py-1 text-[11px] rounded-lg gap-1',
    sm: 'px-3 py-1.5 text-xs rounded-lg gap-1.5',
    md: 'px-4 py-2 text-xs sm:text-sm rounded-xl gap-2',
    lg: 'px-5 py-2.5 text-sm sm:text-base rounded-xl gap-2.5'
  };

  return (
    <button
      disabled={disabled || isLoading}
      className={`inline-flex items-center justify-center font-medium whitespace-nowrap select-none transition-all duration-150 cursor-pointer ${
        DESIGN_TOKENS.buttons[variant]
      } ${sizeClasses[size]} ${className}`}
      {...props}
    >
      {isLoading ? (
        <span className="w-4 h-4 border-2 border-current border-t-transparent rounded-full animate-spin shrink-0" />
      ) : (
        icon && <span className="shrink-0">{icon}</span>
      )}
      {children && <span>{children}</span>}
      {!isLoading && iconRight && <span className="shrink-0">{iconRight}</span>}
    </button>
  );
};
```

---

## 2. `Card` Component

```tsx
import React from 'react';
import { DESIGN_TOKENS } from '../references/design-tokens';

export interface CardProps extends React.HTMLAttributes<HTMLDivElement> {
  interactive?: boolean;
  padding?: 'none' | 'sm' | 'md' | 'lg';
}

export const Card: React.FC<CardProps> = ({
  children,
  interactive = false,
  padding = 'md',
  className = '',
  ...props
}) => {
  const paddingClasses = {
    none: '',
    sm: 'p-3',
    md: 'p-4 sm:p-5',
    lg: 'p-6 sm:p-8'
  };

  return (
    <div
      className={`${
        interactive ? DESIGN_TOKENS.surfaces.cardHover : DESIGN_TOKENS.surfaces.card
      } ${paddingClasses[padding]} ${className}`}
      {...props}
    >
      {children}
    </div>
  );
};
```

---

## 3. `Badge` Component

```tsx
import React from 'react';
import { DESIGN_TOKENS } from '../references/design-tokens';

export type BadgeVariant = 'emerald' | 'indigo' | 'amber' | 'violet' | 'rose';

export interface BadgeProps {
  children: React.ReactNode;
  variant?: BadgeVariant;
  icon?: React.ReactNode;
  className?: string;
}

export const Badge: React.FC<BadgeProps> = ({
  children,
  variant = 'indigo',
  icon,
  className = ''
}) => {
  return (
    <span
      className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[11px] font-semibold whitespace-nowrap ${
        DESIGN_TOKENS.badges[variant]
      } ${className}`}
    >
      {icon && <span className="shrink-0">{icon}</span>}
      <span>{children}</span>
    </span>
  );
};
```

---

## 4. `Input` Component

```tsx
import React from 'react';
import { DESIGN_TOKENS } from '../references/design-tokens';

export interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  leftIcon?: React.ReactNode;
  rightIcon?: React.ReactNode;
  error?: string;
  helperText?: string;
}

export const Input: React.FC<InputProps> = ({
  label,
  leftIcon,
  rightIcon,
  error,
  helperText,
  className = '',
  id,
  ...props
}) => {
  const inputId = id || (label ? `input-${label.toLowerCase().replace(/\s+/g, '-')}` : undefined);

  return (
    <div className="w-full space-y-1">
      {label && (
        <label
          htmlFor={inputId}
          className="block text-xs font-semibold text-slate-700 dark:text-slate-200"
        >
          {label}
        </label>
      )}

      <div className="relative flex items-center">
        {leftIcon && (
          <div className="absolute left-3.5 text-slate-400 pointer-events-none flex items-center justify-center">
            {leftIcon}
          </div>
        )}

        <input
          id={inputId}
          className={`${
            leftIcon ? DESIGN_TOKENS.inputs.withLeftIcon : DESIGN_TOKENS.inputs.base
          } ${error ? 'border-rose-500 focus:ring-rose-500/30 focus:border-rose-500' : ''} ${className}`}
          {...props}
        />

        {rightIcon && (
          <div className="absolute right-3.5 text-slate-400 flex items-center justify-center">
            {rightIcon}
          </div>
        )}
      </div>

      {error ? (
        <p className="text-[11px] text-rose-500 font-medium">{error}</p>
      ) : helperText ? (
        <p className="text-[11px] text-slate-400">{helperText}</p>
      ) : null}
    </div>
  );
};
```
