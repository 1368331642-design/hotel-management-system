<template>
  <div class="front-room-status">
    <h2 class="page-title">房间状态</h2>

    <LoadingSpinner v-if="loading" variant="frontdesk" size="small" />
    <ErrorRetry v-else-if="error" :message="error" @retry="getRooms" />

    <div v-else-if="rooms.length === 0" class="empty">
      <p>暂无房间数据</p>
    </div>

    <div v-else class="room-table-wrapper">
      <div class="table-toolbar">
        <div class="toolbar-info">
          共 <strong>{{ rooms.length }}</strong> 间客房
        </div>
        <div class="toolbar-sort">
          <span class="sort-label">排序</span>
          <select v-model="sortKey" class="sort-select">
            <option value="status">按状态优先级</option>
            <option value="roomNumber">按房间编号</option>
            <option value="roomType">按客房类型</option>
          </select>
        </div>
      </div>

      <div class="table-container">
        <table>
          <thead>
            <tr>
              <th>房间编号</th>
              <th>客房类型</th>
              <th>容纳人数</th>
              <th>价格/晚</th>
              <th>当前状态</th>
              <th>入住客户</th>
              <th>住房日期</th>
              <th>操作</th>
              <th>清洁状态</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="room in sortedRooms" :key="room.id" :class="'row-status-' + getStatusClass(room.status).replace('status-', '')">
              <td class="cell-number">{{ room.roomNumber }}</td>
              <td>{{ room.roomType?.name || '-' }}</td>
              <td>{{ room.roomType?.capacity || '-' }}人</td>
              <td class="cell-price">¥{{ room.roomType?.price || '-' }}</td>
              <td>
                <span class="status-badge" :class="getStatusClass(room.status)">{{ room.status }}</span>
              </td>
              <td class="cell-user">
                <template v-if="room._guestName">
                  <span class="guest-name">{{ room._guestName }}</span>
                </template>
                <span v-else class="no-guest">—</span>
              </td>
              <td class="cell-date">
                <template v-if="room._checkInTime && room._checkOutTime">
                  {{ formatDate(room._checkInTime) }} 至 {{ formatDate(room._checkOutTime) }}
                </template>
                <template v-else-if="room._checkInTime">
                  {{ formatDate(room._checkInTime) }}
                </template>
                <span v-else class="no-guest">—</span>
              </td>
              <td>
                <button @click="openStatusDialog(room.id, room.status)" class="btn btn-sm">更新</button>
              </td>
              <td class="cell-cleaning">
                <span v-if="room.status === '已完成' && room._cleaningStatus !== 'done'" class="cleaning-badge cleaning-pending" @click="markCleaningDone(room.id)">待清洁</span>
                <span v-else-if="room.status === '已完成' && room._cleaningStatus === 'done'" class="cleaning-badge cleaning-done">完成清洁</span>
                <span v-else class="no-guest">—</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-if="showDialog" class="modal-overlay" @click="closeStatusDialog">
      <div class="modal-content" @click.stop>
        <h3>更新客房状态</h3>
        <div class="form-group">
          <label>选择状态</label>
          <select v-model="selectedStatus" class="form-input">
            <option value="空房">空房</option>
            <option value="已预订">已预订</option>
            <option value="已入住">已入住</option>
            <option value="已完成">已完成</option>
            <option value="维护中">维护中</option>
          </select>
        </div>
        <div class="modal-actions">
          <button @click="confirmUpdateStatus" class="btn btn-confirm">确认</button>
          <button @click="closeStatusDialog" class="btn btn-cancel">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import LoadingSpinner from '../components/LoadingSpinner.vue'
import ErrorRetry from '../components/ErrorRetry.vue'

const STATUS_PRIORITY = {
  '已入住': 0,
  '已完成': 1,
  '已预订': 2,
  '已支付': 3,
  '空房': 4,
  '维护中': 5
}

export default {
  name: 'FrontRoomStatus',
  components: {
    LoadingSpinner,
    ErrorRetry
  },
  data() {
    return {
      rooms: [],
      loading: true,
      error: null,
      showDialog: false,
      selectedStatus: '空房',
      currentRoomId: null,
      sortKey: 'status',
      cleaningTimers: {},
      abortController: null,
      isDestroyed: false
    }
  },
  computed: {
    sortedRooms() {
      const rooms = [...this.rooms]
      if (this.sortKey === 'roomNumber') {
        return rooms.sort((a, b) => (a.roomNumber || '').localeCompare(b.roomNumber || '', undefined, { numeric: true }))
      }
      if (this.sortKey === 'roomType') {
        return rooms.sort((a, b) => (a.roomType?.name || '').localeCompare(b.roomType?.name || ''))
      }
      return rooms.sort((a, b) => {
        const pa = STATUS_PRIORITY[a.status] ?? 99
        const pb = STATUS_PRIORITY[b.status] ?? 99
        if (pa !== pb) return pa - pb
        return (a.roomNumber || '').localeCompare(b.roomNumber || '', undefined, { numeric: true })
      })
    }
  },
  mounted() {
    this.getRooms()
    document.addEventListener('visibilitychange', this.handleVisibilityChange)
  },
  beforeUnmount() {
    this.isDestroyed = true
    Object.values(this.cleaningTimers).forEach(t => clearTimeout(t))
    this.cleaningTimers = {}
    if (this.abortController) {
      this.abortController.abort()
      this.abortController = null
    }
    document.removeEventListener('visibilitychange', this.handleVisibilityChange)
  },
  methods: {
    async getRooms() {
      if (this.isDestroyed) return
      this.loading = true
      this.error = null
      try {
        if (this.abortController) {
          this.abortController.abort()
        }
        this.abortController = new AbortController()

        const [roomsRes, ordersRes] = await Promise.all([
          axios.get('/api/user/rooms', {
            params: { page: 0, size: 200 },
            withCredentials: true,
            signal: this.abortController.signal
          }),
          axios.get('/api/user/orders', {
            params: { page: 0, size: 500 },
            withCredentials: true,
            signal: this.abortController.signal
          })
        ])

        if (this.isDestroyed) return

        const rooms = roomsRes.data.content || roomsRes.data
        const ordersData = ordersRes.data.content || ordersRes.data

        const activeOrders = ordersData.filter(o =>
          o.status === '已入住' || o.status === '已预订' || o.status === '已支付'
        )

        const roomOrderMap = {}
        activeOrders.forEach(order => {
          const roomId = order.room?.id
          if (roomId) {
            const existing = roomOrderMap[roomId]
            if (!existing || new Date(order.checkInTime) > new Date(existing.checkInTime)) {
              roomOrderMap[roomId] = order
            }
          }
        })

        this.rooms = rooms.map(room => {
          const order = roomOrderMap[room.id]
          const existingRoom = this.rooms.find(r => r.id === room.id)
          return {
            ...room,
            _guestName: order?.user?.name || order?.user?.username || null,
            _checkInTime: order?.checkInTime || null,
            _checkOutTime: order?.checkOutTime || null,
            _cleaningStatus: existingRoom?._cleaningStatus || null
          }
        })
      } catch (err) {
        if (axios.isCancel(err)) return
        console.error('获取房间数据失败:', err)
        if (!this.isDestroyed) {
          this.error = '获取房间数据失败，请检查网络后重试'
        }
      } finally {
        if (!this.isDestroyed) {
          this.loading = false
        }
      }
    },
    handleVisibilityChange() {
      if (this.isDestroyed) return
      if (document.visibilityState === 'visible') {
        this.getRooms()
      }
    },
    getStatusClass(status) {
      switch (status) {
        case '空房': return 'status-success'
        case '已预订':
        case '已支付': return 'status-warning'
        case '已入住': return 'status-info'
        case '已完成': return 'status-warning'
        case '维护中': return 'status-danger'
        default: return 'status-info'
      }
    },
    formatDate(dateString) {
      if (!dateString) return '-'
      const d = new Date(dateString)
      const y = d.getFullYear()
      const m = d.getMonth() + 1
      const day = d.getDate()
      return `${y}/${m}/${day}`
    },
    openStatusDialog(roomId, currentStatus) {
      this.currentRoomId = roomId
      this.selectedStatus = currentStatus || '空房'
      this.showDialog = true
    },
    closeStatusDialog() {
      this.showDialog = false
      this.currentRoomId = null
      this.selectedStatus = '空房'
    },
    async confirmUpdateStatus() {
      if (!this.currentRoomId) return
      try {
        const response = await axios.put(
          `/api/user/admin/rooms/${this.currentRoomId}/status?status=${encodeURIComponent(this.selectedStatus)}`,
          {},
          { withCredentials: true }
        )
        if (response.data) {
          alert('房间状态更新成功')
          this.getRooms()
          this.closeStatusDialog()
        }
      } catch (err) {
        console.error('更新房间状态失败:', err)
        alert('更新房间状态失败，请稍后重试')
      }
    },
    markCleaningDone(roomId) {
      const room = this.rooms.find(r => r.id === roomId)
      if (!room || room._cleaningStatus === 'done') return
      this.$set(room, '_cleaningStatus', 'done')
      if (this.cleaningTimers[roomId]) {
        clearTimeout(this.cleaningTimers[roomId])
      }
      this.cleaningTimers[roomId] = setTimeout(() => {
        this.$delete(this.cleaningTimers, roomId)
        this.resetRoomToVacant(roomId)
      }, 2000)
    },
    async resetRoomToVacant(roomId) {
      if (this.isDestroyed) return
      try {
        await axios.put(
          `/api/user/admin/rooms/${roomId}/status?status=${encodeURIComponent('空房')}`,
          {},
          { withCredentials: true }
        )
        if (!this.isDestroyed) {
          this.getRooms()
        }
      } catch (err) {
        console.error('重置房间状态失败:', err)
      }
    }
  }
}
</script>

<style scoped>
.front-room-status {
  max-width: 1200px;
  margin: 0 auto;
}

.table-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  flex-wrap: wrap;
  gap: 0.8rem;
}

.toolbar-info {
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.toolbar-sort {
  display: flex;
  align-items: center;
  gap: 0.4rem;
}

.sort-label {
  color: var(--text-light);
  font-size: 0.85rem;
}

.sort-select {
  padding: 0.35rem 0.6rem;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  background: var(--bg-white);
  color: var(--text-primary);
  font-size: 0.85rem;
  outline: none;
  cursor: pointer;
}

.sort-select:focus {
  border-color: var(--primary-color);
}

.cell-number {
  font-weight: 700;
  color: var(--text-primary);
}

.cell-price {
  font-weight: 600;
  color: var(--primary-color);
}

.guest-name {
  color: var(--text-primary);
  font-weight: 500;
}

.no-guest {
  color: var(--text-light);
}

.row-status-success { }
.row-status-warning { background-color: #fffdf5; }
.row-status-info { background-color: #f6faff; }
.row-status-danger { background-color: #fefafa; }

td .btn-sm {
  padding: 0.3rem 0.7rem;
  font-size: 0.8rem;
  white-space: nowrap;
}

.cleaning-badge {
  display: inline-block;
  padding: 0.2rem 0.6rem;
  border-radius: var(--radius-sm);
  font-size: 0.8rem;
  font-weight: 500;
  white-space: nowrap;
}

.cleaning-pending {
  color: #e6a23c;
  background: #fdf6ec;
  border: 1px solid #f5dab1;
  cursor: pointer;
}

.cleaning-pending:hover {
  background: #faead7;
}

.cleaning-done {
  color: #67c23a;
  background: #f0f9eb;
  border: 1px solid #c2e7b0;
}

@media (max-width: 768px) {
  .table-toolbar {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
