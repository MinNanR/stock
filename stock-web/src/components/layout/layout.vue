<template>
  <el-container class="my-container">
    <el-header>
      <my-header />
    </el-header>
    <el-container>
      <el-aside width="10vw">
        <sidebar />
      </el-aside>
      <el-main style="padding: 0">
        <div class="main-content">
          <router-view></router-view>
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
import sidebar from "./sidebar.vue";
import myHeader from "./header.vue";

export default {
  components: {
    sidebar,
    myHeader,
  },
};
</script>

<style>
.el-header,
.el-footer {
  background-color: #0e1f3d;
  color: #333;
  text-align: center;
  line-height: 60px;
}

.el-aside {
  background-color: #0e1f3d;
  color: #0e1f3d;
  text-align: left;
  line-height: 200px;
}

.el-main {
  background-color: #e9eef3;
  color: #333;
  text-align: left;
  /* line-height: 160px; */
}

body > .el-container {
  margin-bottom: 0px;
}

body {
  margin: 0;
}

.my-container {
  height: 100vh;
}

.main-content {
  width: 96%;
  margin-right: 2%;
  margin-left: 2%;
  margin-top: 20px;
}
</style>