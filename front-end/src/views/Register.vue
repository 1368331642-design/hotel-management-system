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
        <div class="password-input-wrapper">
          <!-- 【修改点1】添加实时输入过滤，使用handlePasswordInput方法 -->
          <input :type="showPassword ? 'text' : 'password'" id="password" v-model="form.password" @input="handlePasswordInput" required>
          <button type="button" @click="togglePassword" class="password-toggle">
            <svg v-if="showPassword" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
              <circle cx="12" cy="12" r="3"></circle>
            </svg>
            <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
              <line x1="1" y1="1" x2="23" y2="23"></line>
            </svg>
          </button>
        </div>
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
      },
      showPassword: false
    }
  },
  methods: {
    togglePassword() {
      this.showPassword = !this.showPassword
    },
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
  background-color: #f9f9f9;
  border-radius: 8px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
}

.register h2 {
  text-align: center;
  margin-bottom: 2rem;
  color: #333;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: #333;
}

.form-group input {
  width: 100%;
  padding: 0.8rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

.password-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.password-input-wrapper input {
  padding-right: 3rem;
}

.password-toggle {
  position: absolute;
  right: 0.8rem;
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #666;
}

.password-toggle svg {
  width: 20px;
  height: 20px;
}

.password-toggle:hover {
  color: #333;
}

.btn {
  width: 100%;
  padding: 0.8rem;
  background-color: var(--primary-color);
  color: var(--text-white);
  border: none;
  border-radius: 6px;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn:hover {
  background-color: #1a70a5;
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.login-link {
  text-align: center;
  margin-top: 1rem;
  color: #666;
}

.login-link a {
  color: var(--primary-color);
  text-decoration: none;
}

.login-link a:hover {
  text-decoration: underline;
}
</style>