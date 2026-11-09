"use client";

import { useEffect, useState } from 'react';
import { getRomanianCounties, type CountyOption } from '@/services/api/locationCatalog';

export function useRomanianCounties() {
  const [counties, setCounties] = useState<CountyOption[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [revision, setRevision] = useState(0);

  useEffect(() => {
    let active = true;
    getRomanianCounties().then(result => {
      if (active) setCounties(result);
    }).catch(() => {
      if (active) setError('Lista județelor nu a putut fi încărcată.');
    }).finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [revision]);

  function retry() {
    setLoading(true);
    setError(null);
    setRevision(value => value + 1);
  }

  return { counties, loading, error, retry };
}
