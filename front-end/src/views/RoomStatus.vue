<template>
  <div class="room-status">
    <h2 class="page-title">实时房态</h2>
    
    <div v-if="loading" class="loading">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>

    <div v-else-if="error" class="message error">
      <p>{{ error }}</p>
      <button @click="getRooms" class="btn btn-sm">重试</button>
    </div>

    <div v-else-if="rooms.length === 0" class="empty">
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
export default {
  name: 'RoomStatus',
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
        case '空闲': return 'status-success'
        case '已预订': return 'status-warning'
        case '已入住': return 'status-info'
        case '清洁中': return 'status-danger'
        case '维修中': return 'status-expired'
        default: return ''
      }
    }
  }
}
</script>

<style scoped>
.room-status {
  max-width: 1200px;
  margin: 0 auto;
  animation: fadeInUp var(--transition-slow);
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

.room-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: var(--primary-gradient);
  transform: scaleX(0);
  transition: transform var(--transition);
}

.room-card:hover::before {
  transform: scaleX(1);
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

.status-空闲 { border-left: 3px solid var(--status-success); }
.status-已预订 { border-left: 3px solid var(--status-warning); }
.status-已入住 { border-left: 3px solid var(--status-info); }
.status-清洁中 { border-left: 3px solid var(--status-danger); }
.status-维修中 { border-left: 3px solid var(--text-light); }

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