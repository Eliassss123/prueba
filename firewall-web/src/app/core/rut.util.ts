/** Formatea RUT chileno en tiempo real: solo dígitos y K, con guion antes del DV (ej. 21684679-6). */
export function formatRutInput(raw: string): string {
  const cleaned = raw.replace(/[^0-9kK]/g, '').toUpperCase();
  if (cleaned.length <= 1) {
    return cleaned;
  }
  const body = cleaned.slice(0, -1);
  const dv = cleaned.slice(-1);
  return `${body}-${dv}`;
}

/** Normaliza RUT para envío al API (trim). */
export function normalizeRut(rut: string): string {
  return rut.trim();
}
