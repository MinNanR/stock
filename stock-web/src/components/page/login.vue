<template>
  <div style="margin-top: 10rem">
    <div class="loginContain">
      <h3>请登录</h3>
      <div class="login-form">
        <el-form
          :label-position="'right'"
          label-width="80px"
          :model="loginForm"
          ref="loginForm"
        >
          <el-form-item label="用户名" style="width: 300px" prop="username">
            <el-input v-model="loginForm.username" autocomplete="off"></el-input>
          </el-form-item>
          <el-form-item label="密码" style="width: 300px" prop="password">
            <el-input type="password" v-model="loginForm.password"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="login">登录</el-button>
            <el-button @click="resetForm('loginForm')">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script>
import md5 from "js-md5";

export default {
  data() {
    return {
      loginForm: {
        username: "",
        password: "",
      },
    };
  },
  methods: {
    login() {
      let submitForm = {
        username: this.loginForm.username,
        password: md5(this.loginForm.password),
      };
      this.request
        .post("/auth/login", submitForm)
        .then((response) => {
          let data = response.data;
          let token = data.token;
          localStorage.setItem("stock-token", `Bearer ${token}`);
          this.$router.push('/');
        })
        .catch((error) => {
          console.log(error);
          alert("登录失败!");
        });
    },
    resetForm(formName) {
      this.$refs[formName].resetFields();
    },
  },
};
</script>

<style>
/* .loginDiv {
  text-align: center;
  position: relative;
}

.login-form {
  position: absolute;
  top: 50%;
  left: 50%;
  align-content: center;
  transform: translate(-50%,-50%);
} */
.loginContain {
  position: absolute;
  left: 50%;
  top: 20%;
  transform: translate(-50%, -50%);
}
</style>