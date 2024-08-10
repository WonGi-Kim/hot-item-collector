const { defineConfig } = require('@vue/cli-service');

module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 8081, // vue 포트 번호 설정
    proxy: {
      '/products': {
        target: 'http://13.125.60.206:8080',
        changeOrigin: true,
      },
      '/prepare/order': {
        target: 'http://13.125.60.206:8080',
        changeOrigin: true,
      },
      '/prepare/payments': {
        target: 'http://13.125.60.206:8080',
        changeOrigin: true,
      },
    },
  },
});