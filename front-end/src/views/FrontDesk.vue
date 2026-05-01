<template>
  <div class="front-desk">
    <h2 class="page-title">前台服务</h2>

    <!-- 服务咨询 -->
    <div class="tab-content">
      <h3 class="section-title">服务咨询</h3>
      
      <LoadingSpinner v-if="loadingServiceLogs" variant="frontdesk" size="small" />
      <ErrorRetry v-else-if="serviceLogsError" :message="serviceLogsError" @retry="getServiceLogs" />

      <div v-else-if="!loadingServiceLogs && !serviceLogsError">
        <div class="list">
          <h4 class="section-title">咨询列表</h4>
          <div v-if="pendingServiceLogs.length === 0" class="empty">
            <svg class="empty-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"/></svg>
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
          <h4 class="section-title">服务记录</h4>
          <div v-if="processedServiceLogs.length === 0" class="empty">
            <svg class="empty-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
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

    <!-- 状态选择弹窗 -->
    <transition name="modal">
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
    </transition>
  </div>
</template>

<script>
import axios from 'axios'
import LoadingSpinner from '../components/LoadingSpinner.vue'
import ErrorRetry from '../components/ErrorRetry.vue'

export default {
  name: 'FrontDesk',
  components: {
    LoadingSpinner,
    ErrorRetry
  },
  data() {
    return {
      serviceLogs: [],
      showStatusDialog: false,
      selectedStatus: '待处理',
      currentLogId: null,
      refreshInterval: null,
      loadingServiceLogs: false,
      serviceLogsError: null,
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
    this.startAutoRefresh()
  },
  beforeUnmount() {
    if (this.refreshInterval) {
      clearInterval(this.refreshInterval)
    }
  },
  methods: {
    startAutoRefresh() {
      // 优化：只在页面可见时刷新，减少不必要的请求
      const checkAndRefresh = () => {
        if (document.visibilityState === 'visible') {
          this.getServiceLogs()
        }
      }
      // 初始延迟30秒后检查，之后每60秒检查一次
      setTimeout(checkAndRefresh, 30000)
      this.refreshInterval = setInterval(checkAndRefresh, 60000)
    },

    async getServiceLogs() {
      this.loadingServiceLogs = true
      this.serviceLogsError = null
      try {
        const response = await axios.get('/api/admin/service-logs', {
          params: {
            page: 0,
            size: 100
          },
          withCredentials: true
        })
        const logsData = response.data.content || response.data
        this.serviceLogs = logsData.map(log => {
          if (typeof log.status === 'object' && log.status !== null) {
            log.status = log.status.status || '待处理'
          }
          return log
        })
      } catch (error) {
        console.error('获取服务日志失败:', error)
        this.serviceLogsError = '获取服务日志失败，请检查网络后重试'
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
  background: var(--bg-white);
  padding: 2rem;
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-light);
  box-shadow: var(--shadow-xs);
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
  box-shadow: 0 0 0 3px rgba(91, 155, 213, 0.15);
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
  background: var(--bg-white);
  transition: all var(--transition);
}

.item:hover {
  box-shadow: var(--shadow-xs);
  border-color: var(--primary-color);
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

@media (max-width: 768px) {
  .tab-content {
    padding: 1rem;
  }
}
</style>
