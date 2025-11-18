const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 8880,
    proxy: {
      '/api': {
        target: 'http://localhost:1145',
        changeOrigin: true,
        pathRewrite: {
          '^/api': '/api'
        }
      }
    }
  },
  // 禁用eslint
  lintOnSave: false
})
