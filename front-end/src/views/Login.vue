<template>
  <div class="login">
    <div v-if="!showLoginForm" class="login-type-select">
      <h2>请选择登录方式</h2>
      <div class="login-types">
        <div class="login-type-card" @click="selectLoginType('admin')">
          <div class="icon">👨‍💼</div>
          <h3>管理员登录</h3>
          <p>系统管理和数据统计</p>
        </div>
        <div class="login-type-card" @click="selectLoginType('front')">
          <div class="icon">👨‍💻</div>
          <h3>前台登录</h3>
          <p>客房服务和状态管理</p>
        </div>
        <div class="login-type-card" @click="selectLoginType('user')">
          <div class="icon">👤</div>
          <h3>客户登录</h3>
          <p>客房预订和订单管理</p>
        </div>
      </div>
    </div>

    <div v-if="showLoginForm" class="login-form">
      <button @click="goBack" class="back-btn">← 返回</button>
      <h2>{{ getLoginTitle() }}</h2>
      <form @submit.prevent="login">
        <div class="form-group">
          <label for="username">用户名</label>
          <input type="text" id="username" v-model="form.username" required>
        </div>
        <div class="form-group">
          <label for="password">密码</label>
          <input type="password" id="password" v-model="form.password" required>
        </div>
        <button type="submit" class="btn">登录</button>
        <p v-if="selectedLoginType === 'user'" class="register-link">还没有账号？<router-link to="/register">立即注册</router-link></p>
      </form>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'Login',
  data() {
    return {
      showLoginForm: false,
      selectedLoginType: '',
      form: {
        username: '',
        password: ''
      }
    }
  },
  methods: {
    selectLoginType(type) {
      this.selectedLoginType = type
      this.showLoginForm = true
    },
    goBack() {
      this.showLoginForm = false
      this.selectedLoginType = ''
      this.form = {
        username: '',
        password: ''
      }
    },
    getLoginTitle() {
      const titles = {
        admin: '管理员登录',
        front: '前台登录',
        user: '客户登录'
      }
      return titles[this.selectedLoginType] || '用户登录'
    },
    async login() {
      try {
        console.log('登录请求:', this.form)
        const response = await axios.post('/api/auth/login', this.form, { withCredentials: true })
        console.log('登录响应:', response)
        
        if (response.data && response.data.success) {
          const user = response.data.user
          console.log('用户信息:', user)
          console.log('用户角色:', user.roles)
          
          sessionStorage.setItem('user', JSON.stringify(user))
          
          if (this.selectedLoginType === 'admin') {
            console.log('跳转到管理员页面')
            this.$router.push('/admin')
          } else if (this.selectedLoginType === 'front') {
            console.log('跳转到前台页面')
            this.$router.push('/front-desk')
          } else {
            console.log('跳转到首页')
            this.$router.push('/')
          }
        } else {
          alert('登录失败: ' + (response.data?.message || '未知错误'))
        }
      } catch (error) {
        console.error('登录失败:', error)
        console.error('错误响应:', error.response)
        alert('登录失败，请检查用户名和密码')
      }
    }
  }
}
</script>

<style scoped>
.login {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
}

.login-type-select h2 {
  text-align: center;
  margin-bottom: 3rem;
  color: var(--text-primary);
}

.login-types {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 2rem;
}

.login-type-card {
  padding: 2.5rem 2rem;
  background-color: var(--bg-white);
  border-radius: 12px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid var(--border-color);
  box-shadow: var(--shadow-sm);
}

.login-type-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--shadow-md);
  border-color: var(--primary-color);
}

.login-type-card .icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.login-type-card h3 {
  font-size: 1.3rem;
  margin-bottom: 0.5rem;
  color: var(--text-primary);
}

.login-type-card p {
  color: var(--text-secondary);
  margin: 0;
}

.login-form {
  max-width: 400px;
  margin: 0 auto;
  padding: 2rem;
  background-color: var(--bg-white);
  border-radius: 8px;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-color);
}

.back-btn {
  background: none;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  font-size: 1rem;
  padding: 0.5rem 0;
  margin-bottom: 1rem;
  transition: color 0.3s ease;
}

.back-btn:hover {
  color: var(--primary-color);
}

.login-form h2 {
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
}

.form-group input {
  width: 100%;
  padding: 0.8rem;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  font-size: 1rem;
  transition: border-color 0.3s ease;
}

.form-group input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(35, 133, 187, 0.2);
}

.btn {
  width: 100%;
  padding: 0.8rem;
  background-color: var(--primary-color);
  color: var(--text-white);
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn:hover {
  background-color: var(--primary-hover);
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.register-link {
  text-align: center;
  margin-top: 1rem;
  color: var(--text-secondary);
}

.register-link a {
  color: var(--primary-color);
  text-decoration: none;
  transition: color 0.3s ease;
}

.register-link a:hover {
  text-decoration: underline;
}
</style>
