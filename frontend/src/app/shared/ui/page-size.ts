/** Registros por página que puede elegir el usuario (el backend admite como máximo 100). */
export const TAMANOS_PAGINA = [10, 20, 50, 100] as const;

const CLAVE = 'paginacion.tamano';

/** La preferencia guardada del usuario o, si no eligió ninguna, el valor por defecto de la pantalla. */
export function tamanoPagina(porDefecto = 10): number {
  try {
    const guardado = Number(localStorage.getItem(CLAVE));
    return TAMANOS_PAGINA.some((t) => t === guardado) ? guardado : porDefecto;
  } catch {
    return porDefecto; // almacenamiento no disponible
  }
}

export function guardarTamanoPagina(tamano: number): void {
  try {
    localStorage.setItem(CLAVE, String(tamano));
  } catch {
    /* la elección vale solo para esta pantalla */
  }
}
