import type { EditableProfessionalProfile } from '@/types';
import { apiFetch } from './apiClient';

const endpoint = '/api/professional-profile/me';

export const professionalProfileService = {
  get: (): Promise<EditableProfessionalProfile> => apiFetch(endpoint),
  save: (profile: EditableProfessionalProfile): Promise<EditableProfessionalProfile> =>
    apiFetch(endpoint, { method: 'PUT', body: JSON.stringify(profile) }),
};
