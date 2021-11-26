<template>
  <div>
    <router-view></router-view>
  </div>
</template>

<script>

export default {
  name: "App",
  components: {
    // HelloWorld,
  },
};
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
}

.content {
  background-color: white;
  padding: 10px 10px 10px 10px;
}
</style>
