/**
 * Service category reference catalog.
 *
 * The database is populated with 5,000 categories (see
 * `scripts/populate_5000_records.sql`). The five baseline categories are the
 * user-facing taxonomy; the generated rows are exposed only through their
 * deterministic display label so the marketplace never renders raw IDs.
 */

export interface ServiceCategoryOption {
  id: string;
  name: string;
  shortName: string;
}

const ROOT_CATEGORIES: ServiceCategoryOption[] = [
  { id: 'it-software', name: 'IT & Dezvoltare Software', shortName: 'IT' },
  { id: 'constructii', name: 'Construcții & Amenajări', shortName: 'Construcții' },
  { id: 'contabilitate', name: 'Contabilitate & Fiscalitate', shortName: 'Contabilitate' },
  { id: 'marketing', name: 'Marketing & Design Digital', shortName: 'Marketing' },
  { id: 'instalatii', name: 'Instalații & Climatizare', shortName: 'Instalații' },
];

export const SERVICE_CATEGORIES: ServiceCategoryOption[] = ROOT_CATEGORIES;

const ROOT_NAMES = ['IT & Dezvoltare Software', 'Construcții & Amenajări', 'Contabilitate & Fiscalitate',
  'Marketing & Design Digital', 'Instalații & Climatizare'];

const LEGACY_NAMES: Record<string, string> = {
  'it-software': ROOT_NAMES[0],
  constructii: ROOT_NAMES[1],
  contabilitate: ROOT_NAMES[2],
  marketing: ROOT_NAMES[3],
  instalatii: ROOT_NAMES[4],
};

export function resolveCategoryLabel(categoryId: string): string {
  const legacy = LEGACY_NAMES[categoryId];
  if (legacy) return legacy;

  const generated = /^cat-(\d+)$/.exec(categoryId);
  if (generated) {
    const index = Number(generated[1]);
    if (index >= 1 && index <= ROOT_NAMES.length) {
      return ROOT_NAMES[index - 1];
    }
    return `Serviciu Specializat #${index}`;
  }

  return categoryId.replace(/[-_]/g, ' ').replace(/\b\w/g, letter => letter.toUpperCase());
}

/**
 * Normalizes a category id or a human-readable category label into the id used
 * by the API. AI-generated drafts use labels, while API payloads use ids.
 */
export function resolveCategoryId(value: string): string {
  if (LEGACY_NAMES[value] || /^cat-\d+$/.test(value)) return value;

  const normalized = value.toLowerCase();
  if (normalized.includes('it') || normalized.includes('software')) return 'it-software';
  if (normalized.includes('construc')) return 'constructii';
  if (normalized.includes('contabilit') || normalized.includes('fiscal')) return 'contabilitate';
  if (normalized.includes('marketing') || normalized.includes('design')) return 'marketing';
  if (normalized.includes('instala') || normalized.includes('climat')) return 'instalatii';
  return 'it-software';
}
