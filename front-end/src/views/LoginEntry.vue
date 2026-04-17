<template>
  <div class="login-entry">
    <h2 class="login-entry-title">请选择登录方式</h2>
    <div class="login-entry-container">
      <div class="login-entry-icon">🏨</div>
      <h3 class="login-entry-subtitle">欢迎使用酒店管理系统</h3>
      <p class="login-entry-description">请登录以继续</p>
      <button @click="goToLogin" class="login-btn">登录系统</button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'LoginEntry',
  methods: {
    goToLogin() {
      this.$router.push('/login/select')
    }
  }
}
</script>

<style scoped>
.login-entry {
  min-height: 80vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background-color: var(--bg-color);
  padding: 2rem;
}

.login-entry-title {
  color: var(--text-color);
  font-size: 1.5rem;
  margin-bottom: 3rem;
  text-align: center;
}

.login-entry-container {
  background-color: var(--card-bg);
  padding: 3rem;
  border-radius: 8px;
  box-shadow: var(--shadow-md);
  text-align: center;
  max-width: 350px;
  width: 100%;
  transition: transform 0.3s;
  border: 1px solid var(--border-color);
}

.login-entry-container:hover {
  transform: translateY(-5px);
}

.login-entry-icon {
  font-size: 4rem;
  margin-bottom: 1.5rem;
}

.login-entry-subtitle {
  font-size: 1.3rem;
  margin-bottom: 1rem;
  color: var(--text-color);
}

.login-entry-description {
  font-size: 1rem;
  margin-bottom: 2rem;
  color: #666;
}

.login-btn {
  padding: 0.8rem 2rem;
  background-color: var(--primary-color);
  color: var(--text-white);
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 1rem;
  font-weight: 500;
}

.login-btn:hover {
  background-color: #1a70a5;
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}
</style>