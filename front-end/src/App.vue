<template>
  <div class="app" :class="{ 'no-scroll-layout': isAuthLayout }">
    <div class="sidebar-edge-trigger" @mouseenter="openSidebar" aria-hidden="true"></div>
    <aside class="theme-sidebar" :class="{ open: isSidebarOpen }" @mouseenter="openSidebar" @mouseleave="closeSidebar">
      <div class="sidebar-title">界面设置</div>
      <div class="sidebar-group">
        <label class="sidebar-label" for="themeSelect">主题</label>
        <select id="themeSelect" v-model="themeMode" @change="handleThemeChange" class="sidebar-select">
          <option value="light">亮色</option>
          <option value="dark">暗色</option>
          <option value="contrast">高对比</option>
        </select>
      </div>
      <div class="sidebar-group">
        <label class="sidebar-label" for="accentColor">主色</label>
        <input
          id="accentColor"
          v-model="customPrimary"
          type="color"
          class="sidebar-color-picker"
          @input="applyCustomPrimary"
          aria-label="自定义主题主色"
        />
      </div>
    </aside>

    <header class="header" :class="{ 'header-scrolled': isHeaderScrolled }">
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
    <div class="back-button-bar" v-if="$route.path !== '/' && !isAuthLayout">
      <BackButton />
    </div>
    <main class="main" :class="{ 'main-no-scroll': isAuthLayout }">
      <router-view v-slot="{ Component, route }">
        <div :key="route.fullPath" class="route-stage page-soft-enter">
          <component :is="Component" />
        </div>
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
    isAuthLayout() {
      return this.$route.path === '/login' || this.$route.path === '/login/select'
    },
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
      username: '',
      themeMode: 'light',
      customPrimary: '#5b9bd5',
      isHeaderScrolled: false,
      isSidebarOpen: false
    }
  },
  created() {
    // 在组件渲染前初始化登录状态，确保子组件渲染时父级状态已就绪
    this.initLoginStatus()
  },
  mounted() {
    this.loadThemePreferences()
    this.updateHeaderScrollState()
    window.addEventListener('scroll', this.updateHeaderScrollState, { passive: true })
    // 监听路由变化，确保登录状态同步
    this.$router.afterEach(() => {
      this.initLoginStatus()
    })
  },
  beforeUnmount() {
    window.removeEventListener('scroll', this.updateHeaderScrollState)
  },
  methods: {
    loadThemePreferences() {
      const storedTheme = localStorage.getItem('hotel-ui-theme')
      const storedPrimary = localStorage.getItem('hotel-ui-primary')
      if (storedTheme) this.themeMode = storedTheme
      this.applyThemePreferences()
      if (storedPrimary) {
        this.customPrimary = storedPrimary
        this.applyCustomPrimary()
      } else {
        this.customPrimary = this.getThemeDefaultPrimary(this.themeMode)
      }
    },
    handleThemeChange() {
      this.clearCustomPrimaryOverrides()
      this.applyThemePreferences()
      this.customPrimary = this.getThemeDefaultPrimary(this.themeMode)
      localStorage.setItem('hotel-ui-primary', this.customPrimary)
    },
    applyThemePreferences() {
      document.documentElement.setAttribute('data-theme', this.themeMode)
      document.documentElement.setAttribute('data-motion', 'normal')
      localStorage.setItem('hotel-ui-theme', this.themeMode)
    },
    applyCustomPrimary() {
      const root = document.documentElement
      root.style.setProperty('--primary-color', this.customPrimary)
      root.style.setProperty('--primary-hover', this.adjustColor(this.customPrimary, -8))
      root.style.setProperty('--primary-dark', this.adjustColor(this.customPrimary, -16))
      root.style.setProperty('--primary-light', this.adjustColor(this.customPrimary, 14))
      root.style.setProperty('--primary-lighter', this.adjustColor(this.customPrimary, 34))
      root.style.setProperty(
        '--primary-gradient',
        `linear-gradient(135deg, ${this.adjustColor(this.customPrimary, 8)} 0%, ${this.adjustColor(this.customPrimary, -14)} 100%)`
      )
      localStorage.setItem('hotel-ui-primary', this.customPrimary)
    },
    clearCustomPrimaryOverrides() {
      const root = document.documentElement
      root.style.removeProperty('--primary-color')
      root.style.removeProperty('--primary-hover')
      root.style.removeProperty('--primary-dark')
      root.style.removeProperty('--primary-light')
      root.style.removeProperty('--primary-lighter')
      root.style.removeProperty('--primary-gradient')
    },
    getThemeDefaultPrimary(theme) {
      if (theme === 'dark') return '#7fb0f5'
      if (theme === 'contrast') return '#004fcc'
      return '#5b9bd5'
    },
    adjustColor(hex, amount) {
      const normalized = hex.replace('#', '')
      const value = Number.parseInt(normalized, 16)
      if (Number.isNaN(value)) return hex
      const r = Math.max(0, Math.min(255, (value >> 16) + amount))
      const g = Math.max(0, Math.min(255, ((value >> 8) & 0x00ff) + amount))
      const b = Math.max(0, Math.min(255, (value & 0x0000ff) + amount))
      return `#${(r << 16 | g << 8 | b).toString(16).padStart(6, '0')}`
    },
    openSidebar() {
      this.isSidebarOpen = true
    },
    closeSidebar() {
      this.isSidebarOpen = false
    },
    updateHeaderScrollState() {
      this.isHeaderScrolled = window.scrollY > 8
    },
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

.app.no-scroll-layout {
  height: 100vh;
  overflow: hidden;
}

.header {
  background: var(--bg-header);
  position: sticky;
  top: 0;
  z-index: 999;
  box-shadow: 0 2px 12px rgba(74,140,201,0.2);
  border-bottom: 1px solid transparent;
  transition: background var(--transition), box-shadow var(--transition), border-color var(--transition), backdrop-filter var(--transition);
}

.sidebar-edge-trigger {
  position: fixed;
  top: 0;
  right: 0;
  width: 12px;
  height: 100vh;
  z-index: 1200;
}

.theme-sidebar {
  position: fixed;
  top: 50%;
  right: 0;
  transform: translate(100%, -50%);
  width: 220px;
  padding: 1rem 0.9rem;
  border-radius: var(--radius-lg) 0 0 var(--radius-lg);
  background: var(--surface-glass);
  border: 1px solid var(--surface-glass-border);
  box-shadow: var(--shadow-lg);
  backdrop-filter: blur(10px);
  z-index: 1250;
  transition: transform var(--transition-slow) ease;
}

.theme-sidebar.open {
  transform: translate(0%, -50%);
}

.sidebar-title {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 0.8rem;
}

.sidebar-group {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.6rem;
  margin-bottom: 0.7rem;
}

.sidebar-group:last-child {
  margin-bottom: 0;
}

.sidebar-label {
  font-size: 0.8rem;
  color: var(--text-secondary);
}

.sidebar-select {
  width: 118px;
  border: 1px solid var(--border-color);
  background: var(--bg-white);
  color: var(--text-primary);
  border-radius: 6px;
  font-size: 0.8rem;
  padding: 0.25rem 0.4rem;
}

.sidebar-color-picker {
  width: 30px;
  height: 26px;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  background: transparent;
  cursor: pointer;
  padding: 0;
}

.route-stage {
  width: 100%;
}

.header-scrolled {
  background: var(--surface-glass);
  backdrop-filter: blur(12px);
  box-shadow: var(--shadow-md);
  border-bottom-color: var(--surface-glass-border);
}

.header-inner {
  width: 100%;
  max-width: none;
  margin: 0;
  padding: 0 0.8rem;
  min-height: 54px;
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
  margin-left: auto;
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
  padding: 1rem 2rem;
}

.main.main-no-scroll {
  max-width: none;
  width: 100%;
  padding: 0.5rem 1rem;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-button-bar {
  width: 100%;
  padding: 10px 0.8rem;
  display: flex;
  justify-content: flex-start;
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
    padding: 0 0.6rem;
    height: auto;
    padding-top: 0.5rem;
    padding-bottom: 0.5rem;
    flex-direction: column;
    gap: 0.4rem;
  }
  .header h1 { font-size: 1.05rem; }
  nav { justify-content: center; }
  nav a { padding: 0.3rem 0.55rem; font-size: 0.8rem; }
  .back-button-bar { padding: 8px 0.6rem; }
  .main { padding: 1rem; }
  .main.main-no-scroll { padding: 0.4rem 0.6rem; }
  .theme-sidebar {
    width: 200px;
  }
}
</style>