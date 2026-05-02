<template>
  <div class="front-room-status">
    <h2 class="page-title">房间状态</h2>

    <LoadingSpinner v-if="loading" variant="frontdesk" size="small" />
    <ErrorRetry v-else-if="error" :message="error" @retry="getRooms" />

    <div v-else-if="rooms.length === 0" class="empty">
      <svg class="empty-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"/></svg>
      <p>暂无房间数据</p>
    </div>

    <div v-else class="room-table-wrapper">
      <div class="table-toolbar">
        <div class="toolbar-info">
          共 <strong>{{ rooms.length }}</strong> 间客房
        </div>
        <div class="page-size-selector">
          <span class="sort-label">每页</span>
          <select v-model.number="pageSize" @change="onPageSizeChange" class="sort-select">
            <option :value="5">5条</option>
            <option :value="10">10条</option>
            <option :value="20">20条</option>
            <option :value="40">40条</option>
          </select>
        </div>
      </div>

      <div class="table-container">
        <table>
          <thead>
            <tr>
              <th @click="sortBy('roomNumber')" :class="{ sortable: true, sorted: sortKey === 'roomNumber' }">
                房间编号
                <span class="sort-icon">{{ sortKey === 'roomNumber' ? (sortOrder === 'asc' ? '↑' : '↓') : '' }}</span>
              </th>
              <th @click="sortBy('roomType')" :class="{ sortable: true, sorted: sortKey === 'roomType' }">
                客房类型
                <span class="sort-icon">{{ sortKey === 'roomType' ? (sortOrder === 'asc' ? '↑' : '↓') : '' }}</span>
              </th>
              <th @click="sortBy('capacity')" :class="{ sortable: true, sorted: sortKey === 'capacity' }">
                容纳人数
                <span class="sort-icon">{{ sortKey === 'capacity' ? (sortOrder === 'asc' ? '↑' : '↓') : '' }}</span>
              </th>
              <th @click="sortBy('price')" :class="{ sortable: true, sorted: sortKey === 'price' }">
                价格/晚
                <span class="sort-icon">{{ sortKey === 'price' ? (sortOrder === 'asc' ? '↑' : '↓') : '' }}</span>
              </th>
              <th @click="sortBy('status')" :class="{ sortable: true, sorted: sortKey === 'status' }">
                当前状态
                <span class="sort-icon">{{ sortKey === 'status' ? (sortOrder === 'asc' ? '↑' : '↓') : '' }}</span>
              </th>
              <th @click="sortBy('guestName')" :class="{ sortable: true, sorted: sortKey === 'guestName' }">
                入住客户
                <span class="sort-icon">{{ sortKey === 'guestName' ? (sortOrder === 'asc' ? '↑' : '↓') : '' }}</span>
              </th>
              <th @click="sortBy('checkInTime')" :class="{ sortable: true, sorted: sortKey === 'checkInTime' }">
                住房日期
                <span class="sort-icon">{{ sortKey === 'checkInTime' ? (sortOrder === 'asc' ? '↑' : '↓') : '' }}</span>
              </th>
              <th>操作</th>
              <th @click="sortBy('cleaningStatus')" :class="{ sortable: true, sorted: sortKey === 'cleaningStatus' }">
                清洁状态
                <span class="sort-icon">{{ sortKey === 'cleaningStatus' ? (sortOrder === 'asc' ? '↑' : '↓') : '' }}</span>
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="room in paginatedRooms" :key="room.id" :class="'row-status-' + getStatusClass(room.status).replace('status-', '')">
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
      <!-- 分页控件 -->
      <div class="pagination-wrapper">
        <div class="pagination-info">
          共 {{ rooms.length }} 条记录，第 {{ currentPage }} / {{ totalPages }} 页
        </div>
        <div class="pagination">
          <button @click="goToPage(1)" :disabled="currentPage === 1" class="page-btn" title="第一页">«</button>
          <button @click="prevPage" :disabled="currentPage === 1" class="page-btn">上一页</button>
          <div class="page-numbers">
            <template v-for="page in visiblePages" :key="page">
              <span v-if="page === -1" class="ellipsis">...</span>
              <button v-else @click="goToPage(page)" :class="['page-number', { active: currentPage === page }]">{{ page }}</button>
            </template>
          </div>
          <button @click="nextPage" :disabled="currentPage === totalPages" class="page-btn">下一页</button>
          <button @click="goToPage(totalPages)" :disabled="currentPage === totalPages" class="page-btn" title="最后一页">»</button>
        </div>
        <div class="jump-page">
          <span>跳至</span>
          <input type="number" v-model.number="jumpPage" @keyup.enter="handleJumpPage" min="1" :max="totalPages" />
          <span>页</span>
          <button @click="handleJumpPage" class="jump-btn">跳转</button>
        </div>
      </div>
    </div>

    <transition name="modal">
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
    </transition>
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
      sortKey: 'roomNumber',
      sortOrder: 'asc',
      currentPage: 1,
      pageSize: 10,
      jumpPage: 1,
      cleaningTimers: {},
      abortController: null,
      isDestroyed: false
    }
  },
  computed: {
    sortedRooms() {
      if (!this.rooms.length) return []
      const rooms = [...this.rooms]
      const key = this.sortKey
      const order = this.sortOrder
      const compareString = (a, b) => {
        const va = (a || '').toLowerCase()
        const vb = (b || '').toLowerCase()
        return order === 'asc' ? va.localeCompare(vb) : vb.localeCompare(va)
      }
      const compareNumber = (a, b) => {
        const na = a || 0
        const nb = b || 0
        return order === 'asc' ? na - nb : nb - na
      }
      const compareDate = (a, b) => {
        const da = a ? new Date(a).getTime() : 0
        const db = b ? new Date(b).getTime() : 0
        return order === 'asc' ? da - db : db - da
      }
      return rooms.sort((a, b) => {
        switch (key) {
          case 'roomNumber':
            return compareString(a.roomNumber, b.roomNumber)
          case 'roomType':
            return compareString(a.roomType?.name, b.roomType?.name)
          case 'capacity':
            return compareNumber(a.roomType?.capacity, b.roomType?.capacity)
          case 'price':
            return compareNumber(a.roomType?.price, b.roomType?.price)
          case 'status': {
            const pa = STATUS_PRIORITY[a.status] ?? 99
            const pb = STATUS_PRIORITY[b.status] ?? 99
            if (pa !== pb) return order === 'asc' ? pa - pb : pb - pa
            return compareString(a.roomNumber, b.roomNumber)
          }
          case 'guestName':
            return compareString(a._guestName, b._guestName)
          case 'checkInTime':
            return compareDate(a._checkInTime, b._checkInTime)
          case 'cleaningStatus': {
            const cs = (r) => {
              if (r.status === '已完成' && r._cleaningStatus !== 'done') return 0
              if (r.status === '已完成' && r._cleaningStatus === 'done') return 1
              return 2
            }
            const ca = cs(a)
            const cb = cs(b)
            if (ca !== cb) return order === 'asc' ? ca - cb : cb - ca
            return compareString(a.roomNumber, b.roomNumber)
          }
          default:
            return 0
        }
      })
    },
    totalPages() {
      return Math.ceil(this.rooms.length / this.pageSize) || 1
    },
    paginatedRooms() {
      const start = (this.currentPage - 1) * this.pageSize
      return this.sortedRooms.slice(start, start + this.pageSize)
    },
    visiblePages() {
      const pages = []
      let start = Math.max(1, this.currentPage - 2)
      let end = Math.min(this.totalPages, this.currentPage + 2)
      if (start > 1) {
        pages.push(1)
        if (start > 2) pages.push(-1)
      }
      for (let i = start; i <= end; i++) pages.push(i)
      if (end < this.totalPages) {
        if (end < this.totalPages - 1) pages.push(-1)
        pages.push(this.totalPages)
      }
      return pages
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
    sortBy(key) {
      if (this.sortKey === key) {
        this.sortOrder = this.sortOrder === 'asc' ? 'desc' : 'asc'
      } else {
        this.sortKey = key
        this.sortOrder = 'asc'
      }
      this.currentPage = 1
      this.jumpPage = 1
    },
    prevPage() {
      if (this.currentPage > 1) this.currentPage--
    },
    nextPage() {
      if (this.currentPage < this.totalPages) this.currentPage++
    },
    goToPage(page) {
      if (page >= 1 && page <= this.totalPages) {
        this.currentPage = page
        this.jumpPage = page
      }
    },
    onPageSizeChange() {
      this.currentPage = 1
      this.jumpPage = 1
    },
    handleJumpPage() {
      const page = parseInt(this.jumpPage)
      if (isNaN(page) || page < 1 || page > this.totalPages) {
        this.jumpPage = this.currentPage
        return
      }
      this.currentPage = page
    },
    async getRooms() {
      if (this.isDestroyed) return
      this.loading = true
      this.error = null
      this.currentPage = 1
      this.jumpPage = 1
      try {
        if (this.abortController) {
          this.abortController.abort()
        }
        this.abortController = new AbortController()

        await axios.post('/api/user/admin/rooms/sync-status', {}, { withCredentials: true }).catch(() => {})

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

.table-container {
  overflow-x: auto;
}

.table-container table {
  width: 100%;
  border-collapse: collapse;
  min-width: 900px;
}

.table-container th {
  background-color: var(--bg-light);
  font-weight: 600;
  color: var(--text-primary);
  white-space: nowrap;
  padding: 0.75rem 0.8rem;
  text-align: left;
  border-bottom: 1px solid var(--border-light);
}

.table-container th.sortable {
  cursor: pointer;
  user-select: none;
  transition: background-color 0.2s ease;
}

.table-container th.sortable:hover {
  background-color: var(--status-info-bg);
}

.table-container th.sortable.sorted {
  background-color: var(--status-info-bg);
  color: var(--primary-color);
}

.sort-icon {
  margin-left: 0.3rem;
  font-size: 0.8rem;
}

.table-container tbody tr {
  transition: background-color 0.2s ease;
}

.table-container td {
  padding: 0.75rem 0.8rem;
  border-bottom: 1px solid var(--border-light);
  color: var(--text-primary);
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

/* 分页样式 */
.pagination-wrapper {
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
  align-items: center;
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid var(--border-light);
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.pagination-info {
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.page-btn {
  padding: 0.5rem 1rem;
  background-color: var(--bg-white);
  color: var(--text-primary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all var(--transition);
  font-size: 0.9rem;
}

.page-btn:hover:not(:disabled) {
  background-color: var(--primary-color);
  color: var(--text-white);
  border-color: var(--primary-color);
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.page-numbers {
  display: flex;
  gap: 0.3rem;
}

.page-number {
  width: 36px;
  height: 36px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: var(--bg-white);
  color: var(--text-primary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all var(--transition);
  font-size: 0.9rem;
}

.page-number:hover {
  background-color: var(--primary-color);
  color: var(--text-white);
  border-color: var(--primary-color);
}

.page-number.active {
  background: var(--primary-gradient);
  color: var(--text-white);
  border-color: var(--primary-color);
}

.ellipsis {
  padding: 0 0.3rem;
  color: var(--text-light);
  display: flex;
  align-items: center;
  font-weight: bold;
}

.page-size-selector {
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

@media (max-width: 768px) {
  .table-toolbar {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
