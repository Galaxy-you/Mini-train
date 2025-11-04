const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  // 开发服务器配置
  devServer: {
    port: 8080,
    host: '0.0.0.0', // 允许从任意IP访问
    allowedHosts: 'all', // 允许任何域名访问
    proxy: {
      '/api': {
        target: 'http://localhost:1145',
        changeOrigin: true
      }
    }
  }
})
