import React from 'react';
import { DESIGN_TOKENS } from '../../theme/designSystem';

export type BadgeVariant = 'emerald' | 'indigo' | 'amber' | 'violet' | 'rose';

interface BadgeProps {
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
