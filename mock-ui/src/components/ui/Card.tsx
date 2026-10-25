import React from 'react';
import { DESIGN_TOKENS } from '../../theme/designSystem';

interface CardProps extends React.HTMLAttributes<HTMLDivElement> {
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
