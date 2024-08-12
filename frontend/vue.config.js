const { defineConfig } = require('@vue/cli-service');

module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 8081, // vue 포트 번호 설정
    proxy: {
      '/products': {
        target: 'http://hotitemcollector.com:8080',
        changeOrigin: true,
      },
      '/prepare/order': {
        target: 'http://hotitemcollector.com:8080',
        changeOrigin: true,
      },
      '/prepare/payments': {
        target: 'http://hotitemcollector.com:8080',
        changeOrigin: true,
      },
    },
  },
});