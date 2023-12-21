export const environment = {
  production: true,
  baseHref: '/frontend/',
  authConfig: {
    enabled: false,
    issuer: '',
    clientId: ''
  },
  endpoints: {
    orders: window.location.origin + '/services/order-service/api/v1/orders',
    products: window.location.origin +  '/services/product-service/api/v1/products'
  }
};
