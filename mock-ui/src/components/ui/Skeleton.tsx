import React from 'react';

export const Skeleton: React.FC<{ className?: string }> = ({ className = '' }) => {
  return (
    <div
      className={`animate-pulse bg-gradient-to-r from-slate-200 via-slate-100 to-slate-200 dark:from-slate-800 dark:via-slate-700/60 dark:to-slate-800 rounded-xl ${className}`}
    />
  );
};

export const SkeletonLeadCard: React.FC = () => {
  return (
    <div className="p-4 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200/80 dark:border-slate-800 space-y-3">
      <div className="flex items-center justify-between">
        <Skeleton className="w-24 h-4" />
        <Skeleton className="w-16 h-3" />
      </div>
      <Skeleton className="w-3/4 h-5" />
      <Skeleton className="w-1/2 h-4" />
      <div className="flex items-center justify-between pt-2 border-t border-slate-100 dark:border-slate-800">
        <Skeleton className="w-20 h-4" />
        <Skeleton className="w-16 h-6" />
      </div>
    </div>
  );
};

export const SkeletonFeed: React.FC<{ count?: number }> = ({ count = 4 }) => {
  return (
    <div className="p-4 space-y-3">
      {Array.from({ length: count }).map((_, i) => (
        <SkeletonLeadCard key={i} />
      ))}
    </div>
  );
};

export const SkeletonDetail: React.FC = () => {
  return (
    <div className="p-6 space-y-5 h-full">
      <div className="space-y-2 pb-4 border-b border-slate-200 dark:border-slate-800">
        <Skeleton className="w-32 h-4" />
        <Skeleton className="w-4/5 h-7" />
        <div className="grid grid-cols-3 gap-3 pt-3">
          <Skeleton className="h-16" />
          <Skeleton className="h-16" />
          <Skeleton className="h-16" />
        </div>
      </div>
      <Skeleton className="w-full h-28" />
      <Skeleton className="w-full h-44" />
      <Skeleton className="w-full h-32" />
    </div>
  );
};

export const SkeletonProfile: React.FC = () => {
  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center gap-4">
        <Skeleton className="w-20 h-20 rounded-2xl" />
        <div className="space-y-2">
          <Skeleton className="w-48 h-6" />
          <Skeleton className="w-32 h-4" />
          <Skeleton className="w-64 h-4" />
        </div>
      </div>
      <div className="grid grid-cols-4 gap-4">
        <Skeleton className="h-20" />
        <Skeleton className="h-20" />
        <Skeleton className="h-20" />
        <Skeleton className="h-20" />
      </div>
      <Skeleton className="w-full h-48" />
      <Skeleton className="w-full h-64" />
    </div>
  );
};

interface LoadingSpinnerProps {
  message?: string;
  text?: string;
  size?: 'sm' | 'md' | 'lg';
}

export const LoadingSpinner: React.FC<LoadingSpinnerProps> = ({
  message,
  text,
  size = 'md'
}) => {
  const displayMsg = text || message || 'Se încarcă datele...';

  const sizeClasses = {
    sm: 'w-6 h-6 border-2',
    md: 'w-10 h-10 border-3',
    lg: 'w-14 h-14 border-4'
  }[size];

  return (
    <div className="flex flex-col items-center justify-center p-6 space-y-3 text-center">
      <div className="relative">
        <div
          className={`${sizeClasses} border-indigo-200 dark:border-indigo-900 border-t-indigo-600 rounded-full animate-spin`}
        />
        <div
          className={`${sizeClasses} border-transparent border-b-violet-500 rounded-full animate-spin absolute inset-0`}
          style={{ animationDirection: 'reverse', animationDuration: '0.8s' }}
        />
      </div>
      <span className="text-xs font-semibold text-slate-500 dark:text-slate-400 animate-pulse">
        {displayMsg}
      </span>
    </div>
  );
};
