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
          <router-view v-slot="{ Component }">
            <transition :name="transitionName">
              <keep-alive include="index">
                <component :is="Component"></component>
              </keep-alive>
            </transition>
          </router-view>
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
  data() {
    return {
      transitionName: "",
    };
  },
  watch: {
    $route(to, from) {
      console.log(to.meta.index, from.meta.index);
      if (to.meta.index < from.meta.index) {
        this.transitionName = "slide-left";
      } else if (to.meta.index > from.meta.index) {
        this.transitionName = "slide-right";
      }
      console.log(this.transitionName);
    },
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

.fade-enter-active, .fade-leave-active {
  transition: opacity .75s ease;
}
.fade-enter, .fade-leave-active {
  opacity: 0;
}
.child-view {
  position: absolute;
  transition: all .75s cubic-bezier(.55,0,.1,1);
}
.slide-left-enter, .slide-right-leave-active {
  opacity: 0;
  -webkit-transform: translate(30px, 0);
  transform: translate(30px, 0);
}
.slide-left-leave-active, .slide-right-enter {
  opacity: 0;
  -webkit-transform: translate(-30px, 0);
  transform: translate(-30px, 0);
}
</style>