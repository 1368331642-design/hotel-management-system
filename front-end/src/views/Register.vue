<template>
  <div class="register">
    <h2>用户注册</h2>
    <form @submit.prevent="register">
      <div class="form-group">
        <label for="username">用户名</label>
        <input type="text" id="username" v-model="form.username" @input="validateUsername" required>
      </div>
      <div class="form-group">
        <label for="password">密码</label>
        <input type="password" id="password" v-model="form.password" @input="handlePasswordInput" required>
      </div>
      <div class="form-group">
        <label for="name">姓名</label>
        <input type="text" id="name" v-model="form.name" required>
      </div>
      <div class="form-group">
        <label for="phone">电话</label>
        <input type="tel" id="phone" v-model="form.phone" required>
      </div>
      <div class="form-group">
        <label for="email">邮箱</label>
        <input type="email" id="email" v-model="form.email" required>
      </div>
      <div class="form-group">
        <label for="idCard">身份证号</label>
        <input type="text" id="idCard" v-model="form.idCard" required>
      </div>
      <button type="submit" class="btn">注册</button>
      <p class="login-link">已有账号？<router-link to="/login">立即登录</router-link></p>
    </form>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'Register',
  data() {
    return {
      form: {
        username: '',
        password: '',
        name: '',
        phone: '',
        email: '',
        idCard: ''
      }
    }
  },
  methods: {
    validateUsername() {
      const usernameInput = document.getElementById('username')
      const username = this.form.username
      
      // 清除之前的自定义验证
      usernameInput.setCustomValidity('')
      
      if (!username) return
      
      if (username.length > 6) {
        usernameInput.setCustomValidity('用户名长度不能超过6个字符')
        return
      }
      
      const usernameRegex = /^[\u4e00-\u9fa5a-zA-Z0-9]+$/
      if (!usernameRegex.test(username)) {
        usernameInput.setCustomValidity('不能含有特殊符号')
      }
    },
    // 【修改点2】实时输入过滤，自动移除非字母数字的字符，并限制最大长度16个字符
    handlePasswordInput(event) {
      const value = event.target.value
      // 正则：匹配所有非字母、数字的字符，替换为空
      let filteredValue = value.replace(/[^A-Za-z0-9]/g, '')
      // 限制最大长度为16个字符
      if (filteredValue.length > 16) {
        filteredValue = filteredValue.substring(0, 16)
      }
      // 把过滤后的内容重新赋值给v-model，实现实时过滤
      this.form.password = filteredValue
      // 调用validatePassword进行二次校验
      this.validatePassword()
    },
    // 【修改点3】表单校验规则，确保密码仅由字母和数字组成，长度6-16个字符
    validatePassword() {
      const passwordInput = document.getElementById('password')
      const password = this.form.password
      
      // 清除之前的自定义验证
      passwordInput.setCustomValidity('')
      
      if (!password) return
      
      if (password.length < 6) {
        passwordInput.setCustomValidity('密码长度不能少于6个字符')
        return
      }
      
      if (password.length > 16) {
        passwordInput.setCustomValidity('密码长度不能超过16个字符')
        return
      }
      
      const passwordRegex = /^[a-zA-Z0-9]+$/
      if (!passwordRegex.test(password)) {
        passwordInput.setCustomValidity('密码只能是英文或数字')
      }
    },
    async register() {
      // 先验证用户名和密码，设置自定义验证信息
      this.validateUsername()
      this.validatePassword()
      
      // 使用HTML5原生表单验证
      const form = document.querySelector('form')
      if (!form.checkValidity()) {
        return
      }
      
      try {
        const response = await axios.post('/api/auth/register', this.form)
        if (response.data.success) {
          this.$router.push('/login')
        }
      } catch (error) {
        console.error('注册失败:', error)
        alert('注册失败，请稍后重试')
      }
    }
  }
}
</script>

<style scoped>
.register {
  max-width: 400px;
  margin: 0 auto;
  padding: 2rem;
  background-color: var(--bg-white);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-light);
  animation: fadeInUp var(--transition-slow);
}

.register h2 {
  text-align: center;
  margin-bottom: 2rem;
  color: var(--text-primary);
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: var(--text-primary);
  font-weight: 500;
}

.form-group input {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 1.5px solid var(--border-color);
  border-radius: var(--radius-sm);
  font-size: 1rem;
  transition: all var(--transition);
}

.form-group input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(35, 133, 187, 0.12);
}

.btn {
  width: 100%;
  padding: 0.8rem;
  background: var(--primary-gradient);
  color: var(--text-white);
  border: none;
  border-radius: var(--radius-sm);
  font-size: 1rem;
  cursor: pointer;
  transition: all var(--transition);
}

.btn:hover {
  box-shadow: 0 4px 12px rgba(35, 133, 187, 0.35);
  transform: translateY(-1px);
}

.login-link {
  text-align: center;
  margin-top: 1rem;
  color: var(--text-secondary);
}

.login-link a {
  color: var(--primary-color);
  text-decoration: none;
}

.login-link a:hover {
  text-decoration: underline;
}
</style>