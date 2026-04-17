<template>
  <div class="room-status">
    <h2>实时房态</h2>
    
    <div v-if="loading" class="loading">
      <p>加载中...</p>
    </div>

    <div v-else-if="error" class="error">
      <p>{{ error }}</p>
      <button @click="getRooms" class="btn">重试</button>
    </div>

    <div v-else class="rooms-grid">
      <div v-for="room in rooms" :key="room.id" class="room-card" :class="'status-' + room.status">
        <div class="room-number">{{ room.roomNumber }}</div>
        <div class="room-type">{{ room.roomType?.name }}</div>
        <div class="room-status-badge">{{ room.status }}</div>
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
      error: null
    }
  },
  mounted() {
    this.getRooms()
  },
  methods: {
    async getRooms() {
      try {
        this.loading = true
        this.error = null
        // 使用 fetch API 获取所有房间的状态
        const response = await fetch('/api/user/rooms', {
          method: 'GET',
          credentials: 'include',
          headers: {
            'Content-Type': 'application/json'
          }
        })
        const data = await response.json()
        this.rooms = data
      } catch (error) {
        console.error('获取房间状态失败:', error)
        this.error = '获取房间状态失败，请稍后重试'
      } finally {
        this.loading = false
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

.room-status h2 {
  text-align: center;
  margin-bottom: 2rem;
  color: #333;
}

.loading {
  text-align: center;
  padding: 3rem;
  color: #666;
}

.error {
  text-align: center;
  padding: 3rem;
  color: #dc3545;
  background-color: #f8d7da;
  border: 1px solid #f5c6cb;
  border-radius: 4px;
  margin: 1rem 0;
}

.error .btn {
  margin-top: 1rem;
}

.rooms-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 1.5rem;
}

.room-card {
  background-color: white;
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  text-align: center;
  transition: all 0.3s;
}

.room-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.room-number {
  font-size: 1.5rem;
  font-weight: bold;
  color: #333;
  margin-bottom: 0.5rem;
}

.room-type {
  color: #666;
  margin-bottom: 0.5rem;
}

.room-status-badge {
  display: inline-block;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 500;
  margin-bottom: 0.5rem;
}

.room-capacity {
  color: #666;
  margin-bottom: 0.5rem;
}

.room-price {
  font-size: 1.1rem;
  font-weight: 500;
  color: #333;
}

/* 不同状态的样式 */
.status-空闲 .room-status-badge {
  background-color: #d4edda;
  color: #155724;
}

.status-已预订 .room-status-badge {
  background-color: #fff3cd;
  color: #856404;
}

.status-已入住 .room-status-badge {
  background-color: #cce7ff;
  color: #004085;
}

.status-清洁中 .room-status-badge {
  background-color: #f8d7da;
  color: #721c24;
}

.status-维修中 .room-status-badge {
  background-color: #e2e3e5;
  color: #383d41;
}

.btn {
  padding: 0.6rem 1.2rem;
  background-color: var(--primary-color);
  color: var(--text-white);
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.95rem;
}

.btn:hover {
  background-color: #1a70a5;
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}
</style>