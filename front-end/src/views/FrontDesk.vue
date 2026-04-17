<template>
  <div class="front-desk">
    <h2>前台服务</h2>
    <div class="front-desk-tabs">
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
            <p>状态: <span :class="['status-button', log.status === '待处理' ? 'status-pending' : 'status-processed']">{{ log.status }}</span></p>
            <p>时间: {{ formatDate(log.createTime) }}</p>
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
              <p>状态: <span :class="['status-button', 'status-processed']">{{ log.status }}</span></p>
              <p>时间: {{ formatDate(log.createTime) }}</p>
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
    <div v-if="showStatusDialog" class="dialog-overlay" @click="closeStatusDialog">
      <div class="dialog-content" @click.stop>
        <h4>更新服务状态</h4>
        <div class="form-group">
          <label>选择状态:</label>
          <select v-model="selectedStatus" class="status-select">
            <option value="待处理">待处理</option>
            <option value="已处理">已处理</option>
          </select>
        </div>
        <div class="dialog-buttons">
          <button @click="confirmUpdateStatus" class="btn btn-confirm">确认</button>
          <button @click="closeStatusDialog" class="btn btn-cancel">取消</button>
        </div>
      </div>
    </div>

    <!-- 客房状态选择弹窗 -->
    <div v-if="showRoomStatusDialog" class="dialog-overlay" @click="closeRoomStatusDialog">
      <div class="dialog-content" @click.stop>
        <h4>更新客房状态</h4>
        <div class="form-group">
          <label>选择状态:</label>
          <select v-model="selectedRoomStatus" class="status-select">
            <option value="空房">空房</option>
            <option value="已预订">已预订</option>
            <option value="已入住">已入住</option>
            <option value="维护中">维护中</option>
          </select>
        </div>
        <div class="dialog-buttons">
          <button @click="confirmUpdateRoomStatus" class="btn btn-confirm">确认</button>
          <button @click="closeRoomStatusDialog" class="btn btn-cancel">取消</button>
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
      pageSize: 5
    }
  },
  computed: {
    pendingServiceLogs() {
      return this.serviceLogs.filter(log => log.status === '待处理')
    },
    processedServiceLogs() {
      return this.serviceLogs.filter(log => log.status === '已处理')
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
      // 每30秒自动刷新数据
      this.refreshInterval = setInterval(() => {
        this.getRooms()
        this.getServiceLogs()
      }, 30000)
    },

    async getServiceLogs() {
      this.loadingServiceLogs = true
      console.log('开始获取服务日志...')
      try {
        const response = await axios.get('/api/admin/service-logs', {
          params: {
            page: 0, // 获取全部服务日志用于前端分类
            size: 1000
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
            size: 1000
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
    },
    goToPrevPage() {
      if (this.hasPrevPage) {
        this.currentPage--
      }
    },
    goToNextPage() {
      if (this.hasNextPage) {
        this.currentPage++
      }
    }
  }
}
</script>

<style scoped>
.front-desk {
  max-width: 1200px;
  margin: 0 auto;
}

.front-desk-tabs {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
}

.front-desk-tabs button {
  padding: 0.8rem 1.5rem;
  background-color: var(--bg-white);
  color: var(--text-color);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.front-desk-tabs button.active {
  background-color: var(--primary-color);
  color: var(--text-white);
  border-color: var(--primary-color);
}

.tab-content {
  background-color: #f9f9f9;
  padding: 2rem;
  border-radius: 8px;
}

.add-form {
  margin-bottom: 2rem;
  padding: 1.5rem;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 0 5px rgba(0, 0, 0, 0.1);
}

.add-form h4 {
  margin-bottom: 1rem;
  color: #333;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: #333;
}

.form-group input,
.form-group textarea,
.form-group select {
  width: 100%;
  padding: 0.8rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

.btn {
  padding: 0.6rem 1.2rem;
  background-color: var(--primary-color);
  color: var(--text-white);
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn:hover {
  background-color: #1a70a5;
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.list {
  background-color: white;
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: 0 0 5px rgba(0, 0, 0, 0.1);
}

.list h4 {
  margin-bottom: 1rem;
  color: #333;
}

.item {
  padding: 1rem;
  border-bottom: 1px solid #eee;
  margin-bottom: 1rem;
}

.item p {
  margin-bottom: 0.5rem;
  color: #666;
}

.item:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

/* 弹窗样式 */
.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.dialog-content {
  background-color: white;
  padding: 2rem;
  border-radius: 8px;
  min-width: 300px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.dialog-content h4 {
  margin-bottom: 1.5rem;
  color: #333;
  text-align: center;
}

.status-select {
  width: 100%;
  padding: 0.8rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
  background-color: white;
}

.dialog-buttons {
  display: flex;
  gap: 1rem;
  margin-top: 1.5rem;
  justify-content: center;
}

.btn-confirm {
  background-color: var(--primary-color);
}

.btn-confirm:hover {
  background-color: #1a70a5;
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.btn-cancel {
  background-color: var(--bg-white);
  color: var(--text-color);
  border: 1px solid var(--border-color);
}

.btn-cancel:hover {
  background-color: var(--bg-light);
  border-color: var(--primary-color);
  color: var(--primary-color);
}

/* 状态按钮样式 */
.status-button {
  display: inline-block;
  padding: 0.3rem 0.8rem;
  border-radius: 4px;
  font-size: 0.85rem;
  font-weight: 500;
  color: white;
}

.status-pending {
  background-color: #dc3545;
}

.status-processed {
  background-color: #28a745;
}

.loading {
  text-align: center;
  padding: 2rem;
  color: #666;
  background-color: white;
  border-radius: 8px;
}

.empty {
  text-align: center;
  padding: 2rem;
  color: #999;
  background-color: #f9f9f9;
  border-radius: 4px;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 0.5rem;
  margin-top: 1.5rem;
  padding-top: 1.5rem;
  border-top: 1px solid #eee;
}

.page-btn {
  padding: 0.5rem 1rem;
  background-color: var(--bg-white);
  color: var(--text-color);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
}

.page-btn:hover:not(:disabled) {
  background-color: var(--primary-color);
  color: var(--text-white);
  border-color: var(--primary-color);
}

.page-btn:disabled {
  opacity: 0.5;
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
  color: var(--text-color);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
}

.page-number:hover {
  background-color: var(--primary-color);
  color: var(--text-white);
  border-color: var(--primary-color);
}

.page-number.active {
  background-color: var(--primary-color);
  color: var(--text-white);
  border-color: var(--primary-color);
}

.pagination-info {
  text-align: center;
  margin-top: 0.5rem;
  color: #666;
  font-size: 0.9rem;
}
</style>
