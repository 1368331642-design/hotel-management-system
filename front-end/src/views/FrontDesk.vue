<template>
  <div class="front-desk">
    <h2 class="page-title">前台服务</h2>
    <div class="tab-buttons">
      <button @click="activeTab = 'serviceLogs'" :class="{ active: activeTab === 'serviceLogs' }">服务咨询</button>
      <button @click="activeTab = 'roomStatus'" :class="{ active: activeTab === 'roomStatus' }">客房状态</button>
    </div>

    <!-- 服务咨询 -->
    <div v-if="activeTab === 'serviceLogs'" class="tab-content">
      <h3>服务咨询</h3>
      
      <div v-if="loadingServiceLogs" class="loading">
        <p>加载中...</p>
      </div>
      
      <div v-else>
        <div class="list">
          <h4>咨询列表</h4>
          <div v-if="pendingServiceLogs.length === 0" class="empty">
            <p>暂无待处理的咨询</p>
          </div>
          <div v-for="log in pendingServiceLogs" :key="log.id" class="item">
            <p>类型: {{ log.type }}</p>
            <p>内容: {{ log.content }}</p>
            <p v-if="log.user">用户: {{ log.user.name || log.user.username }}</p>
            <p>状态: <span class="status-badge" :class="log.status === '待处理' ? 'status-warning' : 'status-success'">{{ log.status }}</span></p>
            <p>时间: {{ formatDate(log.createTime) }}</p>
            <!-- 评价展示 -->
            <div v-if="log.rating" class="review-display">
              <div class="rating-stars">
                <span v-for="i in 5" :key="i" class="star" :class="{ filled: i <= log.rating }">★</span>
                <span class="rating-text">{{ log.rating }} 分</span>
              </div>
              <p v-if="log.review" class="review-content">评价: {{ log.review }}</p>
            </div>
            <button @click="openStatusDialog(log.id, log.status)" class="btn">更新状态</button>
          </div>
        </div>
        <div class="list" style="margin-top: 2rem;">
          <h4>服务记录</h4>
          <div v-if="processedServiceLogs.length === 0" class="empty">
            <p>暂无服务记录</p>
          </div>
          <div v-else>
            <div v-for="log in paginatedProcessedLogs" :key="log.id" class="item">
              <p>类型: {{ log.type }}</p>
              <p>内容: {{ log.content }}</p>
              <p v-if="log.user">用户: {{ log.user.name || log.user.username }}</p>
              <p>状态: <span class="status-badge status-success">{{ log.status }}</span></p>
              <p>时间: {{ formatDate(log.createTime) }}</p>
              <!-- 评价展示 -->
              <div v-if="log.rating" class="review-display">
                <div class="rating-stars">
                  <span v-for="i in 5" :key="i" class="star" :class="{ filled: i <= log.rating }">★</span>
                  <span class="rating-text">{{ log.rating }} 分</span>
                </div>
                <p v-if="log.review" class="review-content">评价: {{ log.review }}</p>
              </div>
            </div>
            
            <div v-if="totalPages > 1" class="pagination">
              <button @click="goToPrevPage" :disabled="!hasPrevPage" class="page-btn">
                上一页
              </button>
              <div class="page-numbers">
                <button 
                  v-for="page in pageNumbers" 
                  :key="page"
                  @click="goToPage(page)"
                  :class="['page-number', { active: currentPage === page }]"
                >
                  {{ page }}
                </button>
              </div>
              <button @click="goToNextPage" :disabled="!hasNextPage" class="page-btn">
                下一页
              </button>
              <div class="jump-page">
                <span>跳至</span>
                <input type="number" v-model.number="jumpPage" @keyup.enter="handleJumpPage" min="1" :max="totalPages" />
                <span>页</span>
                <button @click="handleJumpPage" class="jump-btn">跳转</button>
              </div>
            </div>
            <div v-if="totalPages > 1" class="pagination-info">
              共 {{ processedServiceLogs.length }} 条，第 {{ currentPage }} / {{ totalPages }} 页
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 客房状态 -->
    <div v-if="activeTab === 'roomStatus'" class="tab-content">
      <h3>客房状态</h3>
      
      <div v-if="loadingRooms" class="loading">
        <p>加载中...</p>
      </div>
      
      <div v-else class="list">
        <h4>房间列表</h4>
        <div v-if="rooms.length === 0" class="empty">
          <p>暂无房间数据</p>
        </div>
        <div v-for="room in rooms" :key="room.id" class="item">
          <p>房间号: {{ room.roomNumber }}</p>
          <p>房型: {{ room.roomType.name }}</p>
          <p>状态: {{ room.status }}</p>
          <button @click="openRoomStatusDialog(room.id, room.status)" class="btn">更新状态</button>
        </div>
      </div>
    </div>

    <!-- 状态选择弹窗 -->
    <div v-if="showStatusDialog" class="modal-overlay" @click="closeStatusDialog">
      <div class="modal-content" @click.stop>
        <h3>更新服务状态</h3>
        <div class="form-group">
          <label>选择状态:</label>
          <select v-model="selectedStatus" class="form-input">
            <option value="待处理">待处理</option>
            <option value="已处理">已处理</option>
          </select>
        </div>
        <div class="modal-actions">
          <button @click="confirmUpdateStatus" class="btn btn-confirm">确认</button>
          <button @click="closeStatusDialog" class="btn btn-ghost">取消</button>
        </div>
      </div>
    </div>

    <div v-if="showRoomStatusDialog" class="modal-overlay" @click="closeRoomStatusDialog">
      <div class="modal-content" @click.stop>
        <h3>更新客房状态</h3>
        <div class="form-group">
          <label>选择状态:</label>
          <select v-model="selectedRoomStatus" class="form-input">
            <option value="空房">空房</option>
            <option value="已预订">已预订</option>
            <option value="已入住">已入住</option>
            <option value="维护中">维护中</option>
          </select>
        </div>
        <div class="modal-actions">
          <button @click="confirmUpdateRoomStatus" class="btn btn-confirm">确认</button>
          <button @click="closeRoomStatusDialog" class="btn btn-ghost">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'FrontDesk',
  data() {
    return {
      activeTab: 'serviceLogs',
      serviceLogs: [],
      rooms: [],
      showStatusDialog: false,
      selectedStatus: '待处理',
      currentLogId: null,
      showRoomStatusDialog: false,
      selectedRoomStatus: '空房',
      currentRoomId: null,
      refreshInterval: null,
      loadingServiceLogs: false,
      loadingRooms: false,
      currentPage: 1,
      pageSize: 5,
      jumpPage: 1
    }
  },
  computed: {
    pendingServiceLogs() {
      return this.serviceLogs
        .filter(log => log.status === '待处理' && log.type !== '订单评价')
        .sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
    },
    processedServiceLogs() {
      return this.serviceLogs
        .filter(log => log.status === '已处理' && log.type !== '订单评价')
        .sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
    },
    paginatedProcessedLogs() {
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      return this.processedServiceLogs.slice(start, end)
    },
    totalPages() {
      return Math.ceil(this.processedServiceLogs.length / this.pageSize)
    },
    hasPrevPage() {
      return this.currentPage > 1
    },
    hasNextPage() {
      return this.currentPage < this.totalPages
    },
    pageNumbers() {
      const pages = []
      for (let i = 1; i <= this.totalPages; i++) {
        pages.push(i)
      }
      return pages
    }
  },
  mounted() {
    this.getServiceLogs()
    this.getRooms()
    this.startAutoRefresh()
  },
  beforeDestroy() {
    if (this.refreshInterval) {
      clearInterval(this.refreshInterval)
    }
  },
  methods: {
    startAutoRefresh() {
      // 优化：只在页面可见时刷新，减少不必要的请求
      const checkAndRefresh = () => {
        if (document.visibilityState === 'visible') {
          this.getRooms()
          this.getServiceLogs()
        }
      }
      // 初始延迟30秒后检查，之后每60秒检查一次
      setTimeout(checkAndRefresh, 30000)
      this.refreshInterval = setInterval(checkAndRefresh, 60000)
    },

    async getServiceLogs() {
      this.loadingServiceLogs = true
      console.log('开始获取服务日志...')
      try {
        const response = await axios.get('/api/admin/service-logs', {
          params: {
            page: 0, // 获取全部服务日志用于前端分类
            size: 100
          },
          withCredentials: true
        })
        console.log('服务日志响应:', response.data)
        // 处理Page对象，取content中的数据
        const logsData = response.data.content || response.data
        this.serviceLogs = logsData.map(log => {
          // 处理 status 字段，确保它是字符串
          if (typeof log.status === 'object' && log.status !== null) {
            log.status = log.status.status || '待处理'
          }
          return log
        })
        console.log('处理后的服务日志:', this.serviceLogs)
      } catch (error) {
        console.error('获取服务日志失败:', error)
        alert('获取服务日志失败: ' + error.message)
      } finally {
        this.loadingServiceLogs = false
      }
    },

    openStatusDialog(logId, currentStatus) {
      this.currentLogId = logId
      this.selectedStatus = currentStatus || '待处理'
      this.showStatusDialog = true
    },

    closeStatusDialog() {
      this.showStatusDialog = false
      this.currentLogId = null
      this.selectedStatus = '待处理'
    },

    async confirmUpdateStatus() {
      if (this.currentLogId) {
        try {
          const response = await axios.put(`/api/admin/service-logs/${this.currentLogId}/status`, { status: this.selectedStatus }, { withCredentials: true })
          if (response.data) {
            alert('服务日志状态更新成功')
            this.getServiceLogs()
            this.closeStatusDialog()
          }
        } catch (error) {
          console.error('更新服务日志状态失败:', error)
          alert('更新服务日志状态失败，请稍后重试')
        }
      }
    },
    async getRooms() {
      this.loadingRooms = true
      console.log('开始获取房间列表...')
      try {
        const response = await axios.get('/api/user/rooms', {
          params: {
            page: 0, // 获取全部房间
            size: 100
          },
          withCredentials: true
        })
        console.log('房间列表响应:', response.data)
        // 处理Page对象，取content中的数据
        this.rooms = response.data.content || response.data
      } catch (error) {
        console.error('获取房间失败:', error)
        alert('获取房间失败: ' + error.message)
      } finally {
        this.loadingRooms = false
      }
    },
    openRoomStatusDialog(roomId, currentStatus) {
      this.currentRoomId = roomId
      this.selectedRoomStatus = currentStatus || '空房'
      this.showRoomStatusDialog = true
    },

    closeRoomStatusDialog() {
      this.showRoomStatusDialog = false
      this.currentRoomId = null
      this.selectedRoomStatus = '空房'
    },

    async confirmUpdateRoomStatus() {
      if (this.currentRoomId) {
        try {
          const response = await axios.put(`/api/user/admin/rooms/${this.currentRoomId}/status?status=${encodeURIComponent(this.selectedRoomStatus)}`, {}, { withCredentials: true })
          if (response.data) {
            alert('房间状态更新成功')
            this.getRooms()
            this.closeRoomStatusDialog()
          }
        } catch (error) {
          console.error('更新房间状态失败:', error)
          alert('更新房间状态失败，请稍后重试')
        }
      }
    },
    formatDate(dateString) {
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-CN')
    },
    goToPage(page) {
      this.currentPage = page
      this.jumpPage = page
    },
    goToPrevPage() {
      if (this.hasPrevPage) {
        this.currentPage--
        this.jumpPage = this.currentPage
      }
    },
    goToNextPage() {
      if (this.hasNextPage) {
        this.currentPage++
        this.jumpPage = this.currentPage
      }
    },
    handleJumpPage() {
      let page = parseInt(this.jumpPage)
      if (isNaN(page) || page < 1 || page > this.totalPages) {
        this.jumpPage = this.currentPage
        return
      }
      this.goToPage(page)
    }
  }
}
</script>

<style scoped>
.front-desk {
  max-width: 1280px;
  margin: 0 auto;
  animation: fadeInUp var(--transition-slow);
}

.tab-content {
  background: linear-gradient(135deg, #fafcfd 0%, #f8fafc 100%);
  padding: 2rem;
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-light);
  box-shadow: var(--shadow-sm);
  position: relative;
  overflow: hidden;
}

.tab-content::before {
  content: '';
  position: absolute;
  top: -60px;
  right: -60px;
  width: 160px;
  height: 160px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(35,133,187,0.04) 0%, transparent 70%);
  pointer-events: none;
}

.add-form {
  margin-bottom: 2rem;
  padding: 1.5rem;
  background-color: var(--bg-white);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-light);
}

.add-form h4 {
  margin-bottom: 1rem;
  color: var(--text-primary);
}

.form-group input,
.form-group textarea,
.form-group select {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 1.5px solid var(--border-color);
  border-radius: var(--radius-sm);
  font-size: 0.95rem;
  transition: all var(--transition);
  background: var(--bg-white);
  color: var(--text-primary);
}

.form-group input:focus,
.form-group textarea:focus,
.form-group select:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(35, 133, 187, 0.12);
}

.list {
  background: var(--bg-white);
  padding: 1.5rem;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-light);
  box-shadow: var(--shadow-sm);
}

.list h4 {
  margin-bottom: 1rem;
  color: var(--text-primary);
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.list h4::before {
  content: '';
  display: inline-block;
  width: 3px;
  height: 16px;
  background: var(--primary-gradient);
  border-radius: 2px;
}

.item {
  padding: 1.2rem;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-light);
  margin-bottom: 1rem;
  background: linear-gradient(135deg, #fafbfc 0%, #f5f7fa 100%);
  transition: all var(--transition);
}

.item:hover {
  box-shadow: var(--shadow-sm);
  border-color: var(--primary-light);
  background: linear-gradient(135deg, #f8fafc 0%, #f0f7fb 100%);
  transform: translateY(-2px);
}

.item p {
  margin-bottom: 0.5rem;
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.item:last-child {
  margin-bottom: 0;
}

.loading {
  text-align: center;
  padding: 2rem;
  color: var(--text-light);
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 0.5rem;
  margin-top: 1.5rem;
  padding-top: 1.5rem;
  border-top: 1px solid var(--border-light);
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

.pagination-info {
  text-align: center;
  margin-top: 0.5rem;
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.review-display {
  margin-top: 1rem;
  padding: 1rem;
  background-color: var(--status-warning-bg);
  border-radius: var(--radius-sm);
  border-left: 4px solid var(--gold);
}

.rating-stars {
  display: flex;
  align-items: center;
  gap: 0.3rem;
  margin-bottom: 0.5rem;
}

.rating-stars .star {
  font-size: 1.2rem;
  color: var(--border-color);
  transition: all var(--transition-fast);
}

.rating-stars .star.filled {
  color: var(--gold);
}

.rating-text {
  margin-left: 0.5rem;
  color: var(--text-secondary);
  font-weight: 500;
  font-size: 0.9rem;
}

.review-content {
  color: var(--text-primary);
  margin: 0;
  line-height: 1.5;
  font-size: 0.9rem;
}

.jump-page {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  color: var(--text-secondary);
  font-size: 0.9rem;
  margin-left: 0.8rem;
}

.jump-page input {
  width: 56px;
  padding: 0.35rem 0.5rem;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  text-align: center;
  outline: none;
  transition: all var(--transition);
}

.jump-page input:focus {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(35, 133, 187, 0.12);
}

@media (max-width: 768px) {
  .tab-content {
    padding: 1rem;
  }
}
</style>
