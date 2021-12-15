<template>
  <div class="header">
    <div class="brand">
      Stock
    </div>
    <div class="header-right">
        <p>你好,{{username}}</p>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      username: "",
    };
  },
  methods:{
    getUserInfo(){
      this.request.post("/auth/getUserInfo", {})
      .then(response => {
        let data = response.data
        this.username = data.user.nickName
      })
    }
  },
  mounted(){
    // this.getUserInfo()
  }
};
</script>

<style>
.brand {
  font-size: 25px;
  height: 65px;
  line-height: 65px;
  color: #ffffff;
}

.header-right {
  font-size: 18px;
  line-height: 100%;
  color: #ffffff;
}

.header {
  display: flex;
  justify-content: space-between;
}
</style>