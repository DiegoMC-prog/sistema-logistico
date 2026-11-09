import { environment } from '../../../environments/environment';

export const API_URL = environment.apiUrl;

/** Convierte un objeto de filtros en parámetros HTTP omitiendo los vacíos. */
export function toParams(obj: object): Record<string, string> {
  const out: Record<string, string> = {};
  for (const [k, v] of Object.entries(obj)) {
    if (v !== undefined && v !== null && v !== '') out[k] = String(v);
  }
  return out;
}
