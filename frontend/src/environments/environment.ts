export const environment = {
  production: true,
  // Producción: se asume un reverse proxy que sirve el API bajo el mismo origen.
  // Cambia por la URL absoluta del backend si se despliega por separado.
  apiUrl: '/api/v1',
};
