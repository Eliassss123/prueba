export const environment = {
  production: false,
  /**
   * Base del API. Vacío = mismo origen (`ng serve`) y `proxy.conf.json` reenvía `/api` al gateway :8080.
   * Sin proxy: `http://localhost:8080` (gateway) o servicios directos vía `useDirectServices`.
   */
  apiRoot: '',
  /**
   * Si true y `apiRoot` vacío, se usan puertos locales del monorepo (sin gateway).
   * Con gateway Docker, deja false y usa proxy o `apiRoot` al puerto 8080.
   */
  useDirectServices: false,
  direct: {
    usuarios: 'http://localhost:8084',
    reportes: 'http://localhost:8081',
    geo: 'http://localhost:8083',
  },
};
