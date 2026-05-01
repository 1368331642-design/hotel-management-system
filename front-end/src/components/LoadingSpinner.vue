<template>
  <div
    class="global-loading"
    :class="{ small: size === 'small' }"
    role="status"
    aria-live="polite"
    aria-label="加载中"
  >
    <!-- 管理员端：骨架屏脉冲 -->
    <template v-if="variant === 'admin'">
      <div class="skeleton-stack">
        <div v-for="n in skeletonCount" :key="n" class="skeleton-card">
          <div class="skeleton-row header"></div>
          <div class="skeleton-row long"></div>
          <div class="skeleton-row medium"></div>
          <div class="skeleton-row short"></div>
        </div>
      </div>
      <p class="dot-loading-text" v-if="showText">{{ text }}</p>
    </template>

    <!-- 前台端：呼吸灯 + 文字轮换 -->
    <template v-else-if="variant === 'frontdesk'">
      <div class="breathe-card">
        <div class="breathe-icon">🏨</div>
        <p class="breathe-text">{{ rotatingText }}</p>
      </div>
    </template>

    <!-- 用户端：圆点跳动 -->
    <template v-else-if="variant === 'user'">
      <div class="dot-loader" :class="{ large: size === 'large', small: size === 'small' }">
        <span class="dot"></span>
        <span class="dot"></span>
        <span class="dot"></span>
      </div>
      <p class="dot-loading-text" v-if="showText">{{ text }}</p>
    </template>

    <!-- 默认 -->
    <template v-else>
      <div class="dot-loader">
        <span class="dot"></span>
        <span class="dot"></span>
        <span class="dot"></span>
      </div>
      <p class="dot-loading-text" v-if="showText">{{ text }}</p>
    </template>
  </div>
</template>

<script>
export default {
  name: 'LoadingSpinner',
  props: {
    variant: {
      type: String,
      default: 'user',
      validator: v => ['admin', 'frontdesk', 'user', 'default'].includes(v)
    },
    text: {
      type: String,
      default: '加载中...'
    },
    showText: {
      type: Boolean,
      default: true
    },
    size: {
      type: String,
      default: 'medium',
      validator: v => ['small', 'medium', 'large'].includes(v)
    },
    skeletonCount: {
      type: Number,
      default: 4
    }
  },
  data() {
    return {
      textIndex: 0,
      textTimer: null
    }
  },
  computed: {
    rotatingTexts() {
      return [
        '正在加载数据...',
        '正在整理信息...',
        '即将准备就绪...',
        '正在更新状态...'
      ]
    },
    rotatingText() {
      return this.rotatingTexts[this.textIndex] || this.rotatingTexts[0]
    }
  },
  mounted() {
    if (this.variant === 'frontdesk') {
      this.startTextRotation()
    }
  },
  beforeUnmount() {
    if (this.textTimer) {
      clearInterval(this.textTimer)
      this.textTimer = null
    }
  },
  methods: {
    startTextRotation() {
      this.textTimer = setInterval(() => {
        this.textIndex = (this.textIndex + 1) % this.rotatingTexts.length
      }, 2000)
    }
  }
}
</script>
