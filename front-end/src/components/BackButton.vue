<template>
  <button
    class="back-button"
    :class="{ disabled: !canGoBack }"
    :disabled="!canGoBack"
    @click="goBack"
    :title="canGoBack ? '返回上一页' : '已在顶层'"
  >
    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="15 18 9 12 15 6"/></svg>
    <span>返回</span>
  </button>
</template>

<script>
export default {
  name: 'BackButton',
  computed: {
    canGoBack() {
      return window.history.length > 1
    }
  },
  methods: {
    goBack() {
      if (this.canGoBack) {
        this.$router.back()
      }
    }
  }
}
</script>

<style scoped>
.back-button {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.5rem 1rem;
  min-width: 44px;
  min-height: 44px;
  background-color: var(--bg-white);
  color: var(--primary-color);
  border: 1.5px solid var(--primary-color);
  border-radius: var(--radius-sm);
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition);
  box-shadow: var(--shadow-sm);
  user-select: none;
  -webkit-tap-highlight-color: transparent;
}

.back-button:hover:not(.disabled) {
  background-color: var(--primary-color);
  color: var(--text-white);
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.back-button:active:not(.disabled) {
  transform: translateY(0);
  box-shadow: var(--shadow-sm);
}

.back-button.disabled {
  opacity: 0.4;
  cursor: not-allowed;
  border-color: var(--border-color);
  color: var(--text-light);
}
</style>
