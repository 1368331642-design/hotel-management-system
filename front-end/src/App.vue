<template>
  <div class="app">
    <header class="header">
      <div class="header-inner">
        <h1>🏨 翻斗花园酒店</h1>
        <nav>
          <router-link to="/" v-if="!isAdmin">首页</router-link>
          <router-link to="/rooms" v-if="!isAdmin && !isFront">房间查询</router-link>
          <router-link to="/booking" v-if="!isAdmin && !isFront">在线预订</router-link>
          <router-link to="/profile" v-if="isLoggedIn && !isAdmin && !isFront">个人中心</router-link>
          <router-link to="/front-desk" v-if="isFront && !isAdmin">前台服务</router-link>
          <router-link to="/front-room-status" v-if="isFront && !isAdmin">房间状态</router-link>
          <router-link to="/" v-if="isAdmin">首页</router-link>
          <router-link to="/admin/finance" v-if="isAdmin">财务统计</router-link>
          <router-link to="/admin/logs" v-if="isAdmin">服务日志</router-link>
          <router-link to="/admin" v-if="isAdmin">管理中心</router-link>
          <router-link to="/login" v-if="!isLoggedIn">登录</router-link>
          <router-link to="/register" v-if="!isLoggedIn && !isAdmin && !isFront">注册</router-link>
          <span v-if="isLoggedIn" class="user-welcome">👋 {{ username }}</span>
          <button v-if="isLoggedIn" @click="logout" class="logout-btn">退出</button>
        </nav>
      </div>
    </header>
    <main class="main">
      <BackButton v-if="$route.path !== '/'" class="global-back-btn" />
      <router-view v-slot="{ Component, route }">
        <component :is="Component" :key="route.fullPath" />
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
  background: var(--bg-color, #f0f4f8);
}

.header {
  background: var(--bg-header);
  position: sticky;
  top: 0;
  z-index: 999;
  box-shadow: 0 2px 12px rgba(74,140,201,0.2);
}

.header-inner {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 2rem;
  height: 54px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header h1 {
  font-size: 1.15rem;
  font-weight: 700;
  color: #fff;
  letter-spacing: 0.5px;
  white-space: nowrap;
  text-shadow: 0 1px 2px rgba(0,0,0,0.1);
}

nav {
  display: flex;
  gap: 2px;
  align-items: center;
  flex-wrap: wrap;
}

nav a {
  color: rgba(255,255,255,0.85);
  text-decoration: none;
  padding: 0.4rem 0.75rem;
  border-radius: 6px;
  font-weight: 500;
  font-size: 0.875rem;
  white-space: nowrap;
  transition: all 0.25s ease;
}

nav a:hover {
  color: #fff;
  background: rgba(255,255,255,0.18);
}

nav a.router-link-active {
  color: #fff;
  background: rgba(255,255,255,0.22);
  font-weight: 600;
}

.user-welcome {
  color: rgba(255,255,255,0.85);
  font-size: 0.85rem;
  margin-left: 0.5rem;
  padding: 0.2rem 0.5rem;
}

.logout-btn {
  background: rgba(255,255,255,0.12);
  border: 1px solid rgba(255,255,255,0.3);
  color: rgba(255,255,255,0.9);
  padding: 0.35rem 0.85rem;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  font-size: 0.85rem;
  margin-left: 4px;
  transition: all 0.25s ease;
}

.logout-btn:hover {
  background: rgba(255,77,79,0.85);
  border-color: rgba(255,77,79,0.9);
  color: #fff;
}

.main {
  flex: 1;
  max-width: 1280px;
  width: 100%;
  margin: 0 auto;
  padding: 1.5rem 2rem;
}

.global-back-btn {
  margin-bottom: 1rem;
  display: inline-flex;
}

.footer {
  background: linear-gradient(135deg, #3d7cc4 0%, #5b9bd5 100%);
  color: rgba(255,255,255,0.7);
  padding: 0.75rem;
  text-align: center;
  margin-top: auto;
  font-size: 0.8rem;
}

@media (max-width: 768px) {
  .header-inner {
    padding: 0 1rem;
    height: auto;
    padding-top: 0.5rem;
    padding-bottom: 0.5rem;
    flex-direction: column;
    gap: 0.4rem;
  }
  .header h1 { font-size: 1.05rem; }
  nav { justify-content: center; }
  nav a { padding: 0.3rem 0.55rem; font-size: 0.8rem; }
  .main { padding: 1rem; }
}
</style>