/**
 * Build para imagen Docker: mismo origen que nginx; `/api` se proxifica al API Gateway.
 */
export const environment = {
  production: true,
  apiRoot: '',
  useDirectServices: false,
  direct: {
    usuarios: 'http://localhost:8084',
    reportes: 'http://localhost:8081',
    geo: 'http://localhost:8083',
  },
};
