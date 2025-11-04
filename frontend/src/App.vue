<template>
  <router-view />
</template>

<script>
export default {
  name: 'App'
}
</script>

<style>
html, body {
  margin: 0;
  padding: 0;
  height: 100%;
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

#app {
  height: 100%;
}

.page-container {
  padding: 20px;
}

.page-title {
  margin-bottom: 20px;
  font-weight: bold;
}

.form-container {
  max-width: 500px;
  margin: 0 auto;
}
</style>
