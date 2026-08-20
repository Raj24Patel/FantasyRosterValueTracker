// nginx (docker) and the dev-server proxy both forward /api to the backend,
// so a relative URL works in every environment
export const environment = {
  apiUrl: '/api'
};
