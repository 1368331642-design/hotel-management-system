<template>
  <div class="room-status">
    <h2 class="page-title">实时房态</h2>

    <LoadingSpinner v-if="loading" variant="user" />

    <ErrorRetry v-else-if="error" :message="error" @retry="getRooms" />

    <div v-else-if="rooms.length === 0" class="empty">
      <svg class="empty-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"/></svg>
      <p>暂无房间数据</p>
    </div>

    <div v-else class="rooms-grid">
      <div v-for="room in rooms" :key="room.id" class="room-card card" :class="'status-' + room.status">
        <div class="room-number">{{ room.roomNumber }}</div>
        <div class="room-type">{{ room.roomType?.name }}</div>
        <span class="status-badge" :class="getRoomStatusClass(room.status)">{{ room.status }}</span>
        <div class="room-capacity">{{ room.roomType?.capacity }}人</div>
        <div class="room-price">¥{{ room.roomType?.price }}/晚</div>
      </div>
    </div>
  </div>
</template>

<script>
import LoadingSpinner from '../components/LoadingSpinner.vue'
import ErrorRetry from '../components/ErrorRetry.vue'

export default {
  name: 'RoomStatus',
  components: {
    LoadingSpinner,
    ErrorRetry
  },
  data() {
    return {
      loading: true,
      rooms: [],
      error: null,
      abortController: null,
      isDestroyed: false
    }
  },
  mounted() {
    this.getRooms()
  },
  beforeUnmount() {
    this.isDestroyed = true
    if (this.abortController) {
      this.abortController.abort()
      this.abortController = null
    }
  },
  methods: {
    async getRooms() {
      if (this.isDestroyed) return
      try {
        this.loading = true
        this.error = null
        if (this.abortController) {
          this.abortController.abort()
        }
        this.abortController = new AbortController()
        const response = await fetch('/api/user/rooms', {
          method: 'GET',
          credentials: 'include',
          headers: {
            'Content-Type': 'application/json'
          },
          signal: this.abortController.signal
        })
        const data = await response.json()
        if (!this.isDestroyed) {
          this.rooms = data
        }
      } catch (error) {
        if (error.name === 'AbortError') return
        console.error('获取房间状态失败:', error)
        if (!this.isDestroyed) {
          this.error = '获取房间状态失败，请稍后重试'
        }
      } finally {
        if (!this.isDestroyed) {
          this.loading = false
        }
      }
    },
    getRoomStatusClass(status) {
      switch (status) {
        case '空房': return 'status-success'
        case '已预订':
        case '已支付': return 'status-warning'
        case '已入住': return 'status-info'
        case '已完成': return 'status-warning'
        case '维护中': return 'status-danger'
        default: return 'status-info'
      }
    }
  }
}
</script>

<style scoped>
.room-status {
  max-width: 1200px;
  margin: 0 auto;
}

.rooms-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: var(--space-lg);
}

.room-card {
  text-align: center;
  position: relative;
  overflow: hidden;
}

.room-number {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.room-type {
  color: var(--text-secondary);
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
}

.room-capacity {
  color: var(--text-light);
  margin-bottom: 0.5rem;
  font-size: 0.85rem;
}

.room-price {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--primary-color);
}

.status-空房 { border-left: 2px solid var(--status-success); }
.status-已预订 { border-left: 2px solid var(--status-warning); }
.status-已入住 { border-left: 2px solid var(--status-info); }
.status-已支付 { border-left: 2px solid var(--status-warning); }
.status-已完成 { border-left: 2px solid var(--status-warning); }
.status-维护中 { border-left: 2px solid var(--status-danger); }

.message.error {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}

@media (max-width: 768px) {
  .rooms-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: var(--space-sm);
  }
}

@media (max-width: 480px) {
  .rooms-grid {
    grid-template-columns: 1fr;
  }
}
</style>