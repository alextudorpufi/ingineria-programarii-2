import { apiFetch } from './apiClient';

export interface CountyOption {
  id: string;
  name: string;
}

export function getRomanianCounties(): Promise<CountyOption[]> {
  return apiFetch('/api/locations/counties');
}
