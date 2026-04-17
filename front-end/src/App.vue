<template>
  <div class="app">
    <header class="header">
      <h1>酒店管理系统</h1>
      <nav>
        <router-link to="/" v-if="!isAdmin">首页</router-link>
        <router-link to="/rooms" v-if="!isAdmin && !isFront">房间查询</router-link>
        <router-link to="/booking" v-if="!isAdmin && !isFront">在线预订</router-link>
        <router-link to="/profile" v-if="!isAdmin && !isFront">个人中心</router-link>
        <router-link to="/front-desk" v-if="isFront && !isAdmin">前台服务</router-link>
        <router-link to="/admin" v-if="isAdmin">管理中心</router-link>
        <router-link to="/login" v-if="!isLoggedIn">登录</router-link>
        <router-link to="/register" v-if="!isLoggedIn && !isAdmin && !isFront">注册</router-link>
        <span v-if="isLoggedIn">欢迎，{{ username }}</span>
        <button v-if="isLoggedIn" @click="logout" class="logout-btn">退出</button>
      </nav>
    </header>
    <main class="main">
      <keep-alive>
        <router-view />
      </keep-alive>
    </main>
    <footer class="footer">
      <p>© 2026 酒店管理系统</p>
    </footer>
  </div>
</template>

<script>
export default {
  name: 'App',
  data() {
    return {
      isLoggedIn: false,
      isAdmin: false,
      isFront: false,
      username: ''
    }
  },
  mounted() {
    // 页面加载时从localStorage初始化内存状态
    this.initLoginStatus()
    // 监听路由变化，确保登录状态同步
    this.$router.afterEach(() => {
      this.initLoginStatus()
    })
  },
  methods: {
    // 初始化登录状态 - 仅在页面加载时调用
    initLoginStatus() {
      const user = sessionStorage.getItem('user')
      if (user) {
        try {
          const userData = JSON.parse(user)
          this.isLoggedIn = true
          this.username = userData.name || userData.username
          this.isAdmin = userData.username === 'admin' || userData.roles?.some(role => role.name === 'ADMIN')
          this.isFront = userData.username === 'front' || userData.roles?.some(role => role.name === 'FRONT')
        } catch (error) {
          console.error('解析用户信息失败:', error)
          sessionStorage.removeItem('user')
          this.isLoggedIn = false
          this.isAdmin = false
          this.isFront = false
          this.username = ''
        }
      } else {
        this.isLoggedIn = false
        this.isAdmin = false
        this.isFront = false
        this.username = ''
      }
    },
    // 退出登录
    logout() {
      // 从sessionStorage中删除用户信息
      sessionStorage.removeItem('user')
      // 更新内存状态
      this.isLoggedIn = false
      this.isAdmin = false
      this.isFront = false
      this.username = ''
      // 跳转到登录页面
      this.$router.push('/login')
    }
  }
}
</script>

<style scoped>
.app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background-color: var(--primary-color);
  color: var(--text-white);
  padding: 1rem 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: var(--shadow-md);
}

.header h1 {
  font-size: 1.5rem;
  font-weight: 600;
}

nav {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

nav a {
  color: var(--text-white);
  text-decoration: none;
  padding: 0.6rem 1rem;
  border-radius: 6px;
  transition: all 0.3s ease;
  font-weight: 500;
}

nav a:hover {
  background-color: rgba(255, 255, 255, 0.2);
  transform: translateY(-1px);
}

nav a.router-link-active {
  background-color: rgba(255, 255, 255, 0.25);
}

.logout-btn {
  background: none;
  border: 1px solid var(--text-white);
  color: var(--text-white);
  padding: 0.5rem 1rem;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 500;
}

.logout-btn:hover {
  background-color: var(--text-white);
  color: var(--primary-color);
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.main {
  flex: 1;
  padding: 2rem;
  background-color: var(--bg-color);
}

.footer {
  background-color: var(--primary-color);
  color: var(--text-white);
  padding: 1rem;
  text-align: center;
  margin-top: auto;
  font-size: 0.9rem;
}
</style>