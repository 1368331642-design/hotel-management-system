<template>
  <div class="login-entry">
    <div class="login-entry-container">
      <div class="login-entry-icon">🏨</div>
      <h3 class="login-entry-subtitle">欢迎光临翻斗花园酒店</h3>
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
  min-height: 85vh;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 2rem;
}

.login-entry-container {
  background: var(--bg-white);
  padding: 3.5rem 3rem;
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-xl);
  text-align: center;
  max-width: 380px;
  width: 100%;
  border: 1px solid var(--border-light);
  position: relative;
  overflow: hidden;
}

.login-entry-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: var(--primary-gradient);
}

.login-entry-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
  display: inline-block;
}

.login-entry-subtitle {
  font-size: 1.4rem;
  margin-bottom: 0.5rem;
  color: var(--text-primary);
  font-weight: 700;
}

.login-entry-description {
  color: var(--text-light);
  margin-bottom: 2rem;
  font-size: 0.95rem;
}

.login-btn {
  padding: 0.8rem 2.5rem;
  background: var(--primary-gradient);
  color: var(--text-white);
  border: none;
  border-radius: var(--radius-sm);
  font-size: 1.05rem;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(35, 133, 187, 0.3);
}
</style>