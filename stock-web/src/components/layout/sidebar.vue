<template>
  <div>
    <el-menu
      :uniqueOpened="true"
      default-active="1"
      class="el-menu-vertical-demo"
      @open="handleOpen"
      @close="handleClose"
      background-color="#0e1f3d"
      text-color="#ffffff"
      active-text-color="#136ff0"
      :router="true"
    >
      <el-menu-item
        v-for="(menu, index) in navList"
        :key="index"
        :index="menu.url"
      >
        <i :class="menu.icon"></i>
        <template #title>{{ menu.name }}</template>
      </el-menu-item>
    </el-menu>
  </div>
</template>

<script>
export default {
  data() {
    return {
      navList:[{
        name:"主页",
        url:"/"
      },
      {
        name:"用户管理",
        url:"/user"
      }]
    };
  },
  methods: {
    handleOpen(key, keyPath) {
      console.log(key, keyPath);
    },
    handleClose(key, keyPath) {
      console.log(key, keyPath);
    },
    getMenu() {
      this.request
        .post("/common/getMenu", {})
        .then((response) => {
          this.navList = response.data;
        })
        .catch((error) => {
          console.log(error);
        });
    },
  },
  mounted() {
    // this.getMenu();
  },
};
</script>

<style>
</style>