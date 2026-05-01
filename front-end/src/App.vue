<template>
  <div class="app">
    <header class="header">
      <h1>酒店管理系统</h1>
      <nav>
        <router-link to="/" v-if="!isAdmin">首页</router-link>
        <router-link to="/rooms" v-if="!isAdmin && !isFront">房间查询</router-link>
        <router-link to="/booking" v-if="!isAdmin && !isFront">在线预订</router-link>
        <router-link to="/profile" v-if="isLoggedIn && !isAdmin && !isFront">个人中心</router-link>
        <router-link to="/front-desk" v-if="isFront && !isAdmin">前台服务</router-link>
        <router-link to="/" v-if="isAdmin">首页</router-link>
        <router-link to="/admin/finance" v-if="isAdmin">财务统计</router-link>
        <router-link to="/admin/logs" v-if="isAdmin">服务日志</router-link>
        <router-link to="/admin" v-if="isAdmin">管理中心</router-link>
        <router-link to="/login" v-if="!isLoggedIn">登录</router-link>
        <router-link to="/register" v-if="!isLoggedIn && !isAdmin && !isFront">注册</router-link>
        <span v-if="isLoggedIn">欢迎，{{ username }}</span>
        <button v-if="isLoggedIn" @click="logout" class="logout-btn">退出</button>
      </nav>
    </header>
    <main class="main">
      <BackButton v-if="$route.path !== '/'" class="global-back-btn" />
      <router-view v-slot="{ Component, route }">
        <transition name="page-fade" mode="out-in">
          <component :is="Component" :key="route.path" />
        </transition>
      </router-view>
    </main>
    <footer class="footer">
      <p>{{ footerText }}</p>
    </footer>
  </div>
</template>

<script>
import BackButton from './components/BackButton.vue'

export default {
  name: 'App',
  components: {
    BackButton
  },
  computed: {
    footerText() {
      if (this.isAdmin || this.isFront) {
        return '翻斗花园酒店管理系统'
      }
      return '欢迎来到翻斗花园酒店'
    }
  },
  data() {
    return {
      isLoggedIn: false,
      isAdmin: false,
      isFront: false,
      username: ''
    }
  },
  created() {
    // 在组件渲染前初始化登录状态，确保子组件渲染时父级状态已就绪
    this.initLoginStatus()
  },
  mounted() {
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
      // 先跳转到首页
      this.$router.push('/')
      // 强制刷新页面，确保所有组件重新加载、清除可见的已登录内容
      window.location.reload()
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
  background: linear-gradient(135deg, #1a6a94 0%, #2385bb 50%, #2b8fc9 100%);
  color: var(--text-white);
  padding: 0.8rem 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 16px rgba(35, 133, 187, 0.3);
  position: sticky;
  top: 0;
  z-index: 999;
}

.header::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.3), transparent);
}

.header h1 {
  font-size: 1.4rem;
  font-weight: 700;
  letter-spacing: 1px;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.header h1::before {
  content: '🏨';
  font-size: 1.4rem;
}

nav {
  display: flex;
  gap: 0.3rem;
  align-items: center;
  flex-wrap: wrap;
}

nav a {
  color: var(--text-white);
  text-decoration: none;
  padding: 0.5rem 0.9rem;
  border-radius: var(--radius-sm);
  transition: all var(--transition);
  font-weight: 500;
  font-size: 0.9rem;
  opacity: 0.9;
  white-space: nowrap;
}

nav a:hover {
  background-color: rgba(255, 255, 255, 0.18);
  opacity: 1;
  transform: translateY(-1px);
}

nav a.router-link-active {
  background-color: rgba(255, 255, 255, 0.22);
  opacity: 1;
}

nav span {
  color: rgba(255, 255, 255, 0.9);
  font-size: 0.9rem;
  margin-left: 0.5rem;
}

.logout-btn {
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.4);
  color: var(--text-white);
  padding: 0.45rem 1rem;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all var(--transition);
  font-weight: 500;
  font-size: 0.9rem;
  backdrop-filter: blur(4px);
  margin-left: 0.3rem;
}

.logout-btn:hover {
  background-color: rgba(255, 255, 255, 0.28);
  border-color: rgba(255, 255, 255, 0.7);
  transform: translateY(-1px);
}

.main {
  flex: 1;
  padding: 2rem;
}

/* ======= 页面切换过渡动画 ======= */
.page-fade-enter-active {
  transition: opacity 0.15s ease-out, transform 0.15s ease-out;
}
.page-fade-leave-active {
  transition: opacity 0.1s ease-in;
}
.page-fade-enter-from {
  opacity: 0;
  transform: translateY(16px);
}
.page-fade-leave-to {
  opacity: 0;
}

.global-back-btn {
  margin-bottom: 1.5rem;
  display: inline-flex;
}

.footer {
  background: linear-gradient(135deg, #1a6a94 0%, #2385bb 100%);
  color: rgba(255, 255, 255, 0.7);
  padding: 1rem;
  text-align: center;
  margin-top: auto;
  font-size: 0.85rem;
  letter-spacing: 0.5px;
}

@media (max-width: 768px) {
  .header {
    padding: 0.6rem 1rem;
    flex-direction: column;
    gap: 0.5rem;
  }
  .header h1 { font-size: 1.2rem; }
  nav { justify-content: center; }
  nav a { padding: 0.4rem 0.6rem; font-size: 0.8rem; }
  .main { padding: 1rem; }
}
</style>