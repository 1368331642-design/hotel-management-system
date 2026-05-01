<template>
  <div class="login">
    <div v-if="!showLoginForm" class="login-type-select">
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
          
          const frontRedirect = this.selectedLoginType === 'front'
          
          sessionStorage.setItem('user', JSON.stringify(user))
          this.resetFormState()
          
          let targetRoute = '/'
          if (frontRedirect) {
            targetRoute = '/front-desk'
          }
          console.log('跳转到:', targetRoute)
          
          this.$router.push(targetRoute).catch(err => {
            if (err.name !== 'NavigationDuplicated') {
              console.error('路由跳转异常:', err)
            }
          })
        } else {
          alert('登录失败: ' + (response.data?.message || '未知错误'))
        }
      } catch (error) {
        console.error('登录失败:', error)
        console.error('错误响应:', error.response)
        alert('登录失败，请检查用户名和密码')
      }
    },
    resetFormState() {
      this.showLoginForm = false
      this.selectedLoginType = ''
      this.form = {
        username: '',
        password: ''
      }
    }
  },
  deactivated() {
    this.resetFormState()
  }
}
</script>

<style scoped>
.login {
  min-height: 85vh;
  display: flex;
  justify-content: center;
  align-items: center;
  max-width: 900px;
  margin: 0 auto;
  padding: 2rem;
}

.login-type-select {
  width: 100%;
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
  background: var(--bg-white);
  border-radius: var(--radius-xl);
  text-align: center;
  cursor: pointer;
  transition: all var(--transition);
  border: 2px solid var(--border-light);
  box-shadow: var(--shadow-sm);
  position: relative;
  overflow: hidden;
}

.login-type-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: var(--primary-gradient);
  transform: scaleX(0);
  transition: transform var(--transition);
}

.login-type-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--shadow-lg);
  border-color: var(--primary-light);
}

.login-type-card:hover::before {
  transform: scaleX(1);
}

.login-type-card:active {
  transform: translateY(-4px);
}

.login-type-card .icon {
  font-size: 3.5rem;
  margin-bottom: 1rem;
  display: inline-block;
}

.login-type-card h3 {
  font-size: 1.2rem;
  margin-bottom: 0.5rem;
  color: var(--text-primary);
  font-weight: 600;
}

.login-type-card p {
  color: var(--text-light);
  margin: 0;
  font-size: 0.9rem;
}

.login-form {
  max-width: 420px;
  margin: 0 auto;
  padding: 2.5rem;
  background: var(--bg-white);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-xl);
  border: 1px solid var(--border-light);
  animation: scaleIn var(--transition);
  position: relative;
  overflow: hidden;
}

.login-form::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: var(--primary-gradient);
}

.back-btn {
  background: none;
  border: none;
  color: var(--text-light);
  cursor: pointer;
  font-size: 0.95rem;
  padding: 0.4rem 0;
  margin-bottom: 1rem;
  transition: all var(--transition-fast);
}

.back-btn:hover {
  color: var(--primary-color);
  transform: translateX(-2px);
}

.login-form h2 {
  text-align: center;
  margin-bottom: 2rem;
  color: var(--text-primary);
  font-weight: 700;
  font-size: 1.3rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: var(--text-primary);
  font-weight: 500;
  font-size: 0.9rem;
}

.form-group input {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 1.5px solid var(--border-color);
  border-radius: var(--radius-sm);
  font-size: 1rem;
  transition: all var(--transition);
  background: var(--bg-light);
}

.form-group input:hover {
  border-color: var(--text-light);
}

.form-group input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(35, 133, 187, 0.12);
  background: var(--bg-white);
}

.btn {
  width: 100%;
  padding: 0.85rem;
  background: var(--primary-gradient);
  color: var(--text-white);
  border: none;
  border-radius: var(--radius-sm);
  font-size: 1.05rem;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition);
  box-shadow: 0 4px 16px rgba(35, 133, 187, 0.25);
}

.btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 24px rgba(35, 133, 187, 0.4);
}

.register-link {
  text-align: center;
  margin-top: 1.2rem;
  color: var(--text-light);
  font-size: 0.9rem;
}

.register-link a {
  color: var(--primary-color);
  text-decoration: none;
  font-weight: 500;
  transition: color var(--transition-fast);
}

.register-link a:hover {
  text-decoration: underline;
}
</style>
