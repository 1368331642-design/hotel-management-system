<template>
  <div class="retry-container" role="alert">
    <div class="retry-icon"><SvgIcon name="alert" size="3rem" /></div>
    <p class="retry-text">{{ message }}</p>
    <button class="retry-btn" @click="handleRetry" :disabled="retrying">
      {{ retrying ? '重试中...' : '重新加载' }}
    </button>
  </div>
</template>

<script>
import SvgIcon from './SvgIcon.vue'

export default {
  name: 'ErrorRetry',
  components: {
    SvgIcon
  },
  props: {
    message: {
      type: String,
      default: '数据加载失败，请检查网络连接后重试'
    }
  },
  data() {
    return {
      retrying: false
    }
  },
  beforeUnmount() {
    this.retrying = false
  },
  methods: {
    handleRetry() {
      if (this.retrying) return
      this.retrying = true
      this.$emit('retry')
      setTimeout(() => {
        this.retrying = false
      }, 3000)
    }
  }
}
</script>
