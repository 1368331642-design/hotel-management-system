<template>
  <div class="rooms">
    <h2 class="page-title">我的房间</h2>
    
    <div v-if="!isLoggedIn" class="login-prompt-banner">
      <div class="login-prompt-content">
        <span class="login-prompt-text">登录后可查看我的房间订单</span>
        <button @click="goToLogin" class="login-prompt-btn">登录系统</button>
        <router-link to="/register" class="login-prompt-register">注册账号</router-link>
      </div>
    </div>

    <div v-else-if="loading" class="loading">
      <div class="skeleton-card">
        <div class="skeleton skeleton-title"></div>
        <div class="skeleton skeleton-line skeleton-line-long"></div>
        <div class="skeleton skeleton-line skeleton-line-medium"></div>
        <div class="skeleton skeleton-line skeleton-line-short"></div>
      </div>
      <div class="skeleton-card">
        <div class="skeleton skeleton-title"></div>
        <div class="skeleton skeleton-line skeleton-line-long"></div>
        <div class="skeleton skeleton-line skeleton-line-medium"></div>
        <div class="skeleton skeleton-line skeleton-line-short"></div>
      </div>
    </div>

    <div v-else-if="error" class="error">
      <p>{{ error }}</p>
      <button @click="getOrders" class="btn">重试</button>
    </div>

    <div v-else-if="myOrders.length === 0" class="no-rooms">
      <div class="empty-icon">
        <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M3 7l9-4 9 4"/><path d="M3 7v10l9 4"/><path d="M12 17l9-4V7"/><path d="M12 17v4"/><path d="M8 5.5l8 3.5"/></svg>
      </div>
      <p>暂无预订或入住的房间</p>
      <button @click="goToBooking" class="btn">去预订</button>
    </div>

    <div v-else class="orders-list">
      <div v-for="order in myOrders" :key="order.id" :class="['order-item', 'order-status-' + getOrderStatusClass(order)]">
        <div class="order-info">
          <h3 class="room-type-title">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 4v16"/><path d="M2 8h18a2 2 0 0 1 2 2v10"/><path d="M2 17h20"/><path d="M6 8v9"/></svg>
            {{ order.room?.roomType?.name }}
          </h3>
          <p><span class="label">房间号:</span> {{ order.room?.roomNumber }}</p>
          <p><span class="label">住房日期:</span> {{ formatDate(order.checkInTime) }} 至 {{ formatDate(order.checkOutTime) }}</p>
          <p><span class="label">总价:</span> <span class="price">¥{{ order.totalPrice }}</span></p>
          <p><span class="label">状态:</span> 
            <span v-if="isGracePeriod(order)" class="status-badge status-grace-badge">{{ graceCountdowns[order.id] }}</span>
            <span v-else-if="isOrderExpired(order)" class="status-badge status-expired-badge">房间已到期</span>
            <span v-else-if="isOrderExpiringSoon(order)" class="status-badge status-expiring-badge">房间即将到期</span>
            <span v-else-if="order.status === '已预订'" class="status-badge status-reserved-badge">{{ order.status }}</span>
            <span v-else-if="order.status === '已入住'" class="status-badge status-checkedin-badge">{{ order.status }}</span>
            <span v-else-if="order.status === '已支付'" class="status-badge status-paid-badge">{{ order.status }}</span>
            <span v-else :class="'status-badge status-' + order.status">{{ order.status }}</span>
          </p>
        </div>
        <div class="order-actions">
          <div class="action-group">
            <button v-if="order.status === '已预订' || order.status === '已支付'" @click="showCancelModal(order.id)" class="btn btn-cancel">取消预定</button>
            <template v-if="canCheckOutEarly(order)">
              <button @click="showCheckOutModal(order.id)" class="btn btn-early-checkout">提前退房</button>
              <button @click="renewOrder(order.id)" class="btn btn-renew">续订</button>
            </template>
            <template v-else-if="isGracePeriod(order)">
              <div class="grace-actions">
                <button @click="showCheckOutModal(order.id)" class="btn btn-early-checkout">退房</button>
                <button @click="renewOrder(order.id)" class="btn btn-renew">续订</button>
                <span class="grace-countdown">{{ graceCountdowns[order.id] }}</span>
              </div>
            </template>
            <template v-else>
              <button v-if="isOrderExpired(order)" @click="showCheckOutModal(order.id)" class="btn btn-early-checkout">退房</button>
              <button v-if="canRenewOrder(order) || isOrderExpired(order) || isOrderExpiringSoon(order)" @click="renewOrder(order.id)" class="btn btn-renew">续订</button>
            </template>
          </div>
        </div>
      </div>
    </div>

    <!-- 确认退房弹窗 -->
    <div v-if="checkOutModalVisible" class="modal-overlay">
      <div class="modal-content">
        <h3>确认退房</h3>
        <p>确定要退房吗？退房后订单将移至历史订单。</p>
        <div class="modal-actions">
          <button @click="closeCheckOutModal" class="btn btn-ghost">取消</button>
          <button @click="handleConfirmCheckOut" class="btn btn-confirm">确认退房</button>
        </div>
      </div>
    </div>

    <!-- 退房成功提示 -->
    <div v-if="checkOutSuccessVisible" class="modal-overlay">
      <div class="modal-content">
        <h3>✅ 退房成功</h3>
        <p>订单已标记为已完成，您可以在历史订单中查看。</p>
        <div class="modal-actions">
          <button @click="checkOutSuccessVisible = false" class="btn btn-ghost">留在当前页</button>
          <button @click="goToOrderHistory" class="btn btn-confirm">查看历史订单</button>
        </div>
      </div>
    </div>

    <!-- 确认取消预定弹窗 -->
    <div v-if="cancelModalVisible" class="modal-overlay">
      <div class="modal-content">
        <h3>确认取消预定</h3>
        <p>确定要取消这个预定吗？</p>
        <div class="modal-actions">
          <button @click="closeCancelModal" class="btn">取消</button>
          <button @click="handleConfirmCancel" class="btn btn-confirm">确认</button>
        </div>
      </div>
    </div>

    <!-- 分页组件 -->
    <div v-if="totalOrders > pageSize" class="pagination-wrapper">
      <div class="pagination-info">
        共 {{ totalOrders }} 条记录，第 {{ currentPage + 1 }} / {{ totalPages }} 页
      </div>
      <div class="pagination">
        <button :disabled="!hasPrevPage" @click="changePage(0)" class="page-btn" title="首页">«</button>
        <button :disabled="!hasPrevPage" @click="changePage(currentPage - 1)" class="page-btn">上一页</button>
        <div class="page-numbers">
          <template v-for="page in visiblePages" :key="page">
            <span v-if="page === -1" class="ellipsis">...</span>
            <button v-else @click="goToPage(page)" :class="['page-number', { active: currentPage + 1 === page }]">{{ page }}</button>
          </template>
        </div>
        <button :disabled="!hasNextPage" @click="changePage(currentPage + 1)" class="page-btn">下一页</button>
        <button :disabled="!hasNextPage" @click="changePage(totalPages - 1)" class="page-btn" title="末页">»</button>
        <div class="jump-page">
          <span>跳至</span>
          <input type="number" v-model.number="jumpPage" @keyup.enter="handleJumpPage" min="1" :max="totalPages" />
          <span>页</span>
          <button @click="handleJumpPage" class="jump-btn">跳转</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'Rooms',
  data() {
    return {
      isLoggedIn: false,
      loading: true,
      isAutoRefresh: false,
      orders: [],
      refreshInterval: null,
      error: null,
      checkOutModalVisible: false,
      checkOutSuccessVisible: false,
      currentCheckOutOrderId: null,
      cancelModalVisible: false,
      currentCancelOrderId: null,
      graceCountdowns: {},
      graceInterval: null,
      abortController: null,
      isDestroyed: false,
      userInfo: {
        id: null,
        username: '',
        name: '',
        phone: '',
        email: '',
        idCard: ''
      },
      currentPage: 0,
      pageSize: 3,
      totalOrders: 0,
      jumpPage: 1
    }
  },
  computed: {
    totalPages() {
      return Math.ceil(this.totalOrders / this.pageSize) || 1
    },
    visiblePages() {
      const pages = []
      const total = this.totalPages
      const current = this.currentPage + 1 // 转为1-based显示
      let start = Math.max(1, current - 2)
      let end = Math.min(total, current + 2)
      if (start > 1) {
        pages.push(1)
        if (start > 2) pages.push(-1)
      }
      for (let i = start; i <= end; i++) pages.push(i)
      if (end < total) {
        if (end < total - 1) pages.push(-1)
        pages.push(total)
      }
      return pages
    },
    hasPrevPage() {
      return this.currentPage > 0
    },
    hasNextPage() {
      return this.currentPage + 1 < this.totalPages
    },
    myOrders() {
      if (!this.orders || this.orders.length === 0) {
        return []
      }
      // 使用内存中的用户信息，而不是直接从localStorage读取
      if (!this.userInfo || !this.userInfo.id) {
        return []
      }
      
      // 后端已经返回了活跃订单，直接返回并排序
      return [...this.orders].sort((a, b) => new Date(a.checkInTime) - new Date(b.checkInTime))
    }
  },
  created() {
    // 组件渲染前检查登录状态，避免白屏
    const userStr = sessionStorage.getItem('user')
    if (userStr) {
      try {
        const user = JSON.parse(userStr)
        if (user && user.id) {
          this.isLoggedIn = true
        }
      } catch (e) {
        console.error('检查登录状态失败:', e)
      }
    }
    this.initUserInfo()
  },
  async mounted() {
    // 页面加载时初始化用户信息
    this.initUserInfo()
    // 从缓存恢复上次浏览的页码
    const savedPage = sessionStorage.getItem('rooms_currentPage')
    if (savedPage !== null) {
      this.currentPage = parseInt(savedPage)
      this.jumpPage = this.currentPage + 1
    }
    if (this.userInfo && this.userInfo.id) {
      try {
        await this.getOrders()
      } catch (error) {
        console.error('getOrders方法执行失败:', error)
      }
    } else {
      this.loading = false
    }
    this.startAutoRefresh()
    this.startGraceCountdowns()
  },
  activated() {
    if (this.isDestroyed) return
    this.initUserInfo()
    if (this.userInfo && this.userInfo.id) {
      this.isAutoRefresh = false
      this.getOrders()
      this.startGraceCountdowns()
    } else {
      this.loading = false
    }
  },
  deactivated() {
    if (this.refreshInterval) {
      clearInterval(this.refreshInterval)
      this.refreshInterval = null
    }
    if (this.graceInterval) {
      clearInterval(this.graceInterval)
      this.graceInterval = null
    }
  },
  beforeUnmount() {
    this.isDestroyed = true
    if (this.abortController) {
      this.abortController.abort()
      this.abortController = null
    }
    if (this.refreshInterval) {
      clearInterval(this.refreshInterval)
      this.refreshInterval = null
    }
    if (this.graceInterval) {
      clearInterval(this.graceInterval)
      this.graceInterval = null
    }
  },
  methods: {
    // 初始化用户信息 - 仅在页面加载时调用
    initUserInfo() {
      const userStr = sessionStorage.getItem('user')
      if (userStr) {
        try {
          this.userInfo = JSON.parse(userStr)
        } catch (error) {
          console.error('解析用户信息失败:', error)
          sessionStorage.removeItem('user')
          this.userInfo = {
            id: null,
            username: '',
            name: '',
            phone: '',
            email: '',
            idCard: ''
          }
        }
      }
    },
    async getOrders() {
      if (this.isDestroyed) return
      if (!this.isAutoRefresh) {
        this.loading = true
      }
      this.error = null
      if (!this.userInfo || !this.userInfo.id) {
        this.orders = []
        this.loading = false
        this.isAutoRefresh = false
        return
      }
      try {
        if (this.abortController) {
          this.abortController.abort()
        }
        this.abortController = new AbortController()
        const timeoutId = setTimeout(() => this.abortController.abort(), 15000)
        const response = await fetch(`/api/user/orders/user/${this.userInfo.id}/active?page=${this.currentPage}&size=${this.pageSize}`, {
          method: 'GET',
          credentials: 'include',
          headers: {
            'Content-Type': 'application/json'
          },
          signal: this.abortController.signal
        })
        clearTimeout(timeoutId)
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`)
        }
        const data = await response.json()
        if (this.isDestroyed) return
        if (data.content) {
          this.orders = data.content
          this.totalOrders = data.totalElements || 0
          if (this.orders.length === 0 && this.totalOrders > 0 && this.currentPage > 0) {
            const newTotalPages = Math.ceil(this.totalOrders / this.pageSize)
            this.currentPage = newTotalPages - 1
            await this.getOrders()
            return
          }
        } else {
          this.orders = Array.isArray(data) ? data : []
          this.totalOrders = this.orders.length
        }
      } catch (error) {
        if (error.name === 'AbortError') {
          if (!this.isAutoRefresh) {
            this.error = '请求超时，请检查网络连接'
          }
          return
        }
        console.error('获取订单失败:', error)
        if (!this.isAutoRefresh) {
          this.error = '获取订单失败，请稍后重试'
        }
      } finally {
        if (!this.isDestroyed) {
          this.loading = false
        }
        this.isAutoRefresh = false
      }
    },
    changePage(newPage) {
      this.currentPage = newPage
      this.jumpPage = this.currentPage + 1
      // 持久化当前页码
      sessionStorage.setItem('rooms_currentPage', this.currentPage)
      this.getOrders()
      window.scrollTo({ top: 0, behavior: 'smooth' })
    },
    handleJumpPage() {
      let page = parseInt(this.jumpPage)
      if (isNaN(page) || page < 1 || page > this.totalPages) {
        this.jumpPage = this.currentPage + 1
        return
      }
      this.changePage(page - 1)
    },
    goToPage(page) {
      this.changePage(page - 1)
    },
    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-CN')
    },
    showCancelModal(orderId) {
      this.currentCancelOrderId = orderId
      this.cancelModalVisible = true
    },
    closeCancelModal() {
      this.cancelModalVisible = false
      this.currentCancelOrderId = null
    },
    async handleConfirmCancel() {
      if (!this.currentCancelOrderId) return
      
      try {
        const response = await axios.put(`/api/user/orders/${this.currentCancelOrderId}/cancel`, {}, { withCredentials: true })
        if (response.data) {
          alert('预订取消成功')
          this.closeCancelModal()
          this.getOrders()
        }
      } catch (error) {
        console.error('取消预订失败:', error)
        alert('取消预订失败，请稍后重试')
      }
    },
    async cancelOrder(orderId) {
      // 保留原有方法，以便其他地方调用
      try {
        const response = await axios.put(`/api/user/orders/${orderId}/cancel`, {}, { withCredentials: true })
        if (response.data) {
          alert('预订取消成功')
          this.getOrders()
        }
      } catch (error) {
        console.error('取消预订失败:', error)
        alert('取消预订失败，请稍后重试')
      }
    },
    canRenewOrder(order) {
      const now = new Date()
      const checkOutTime = new Date(order.checkOutTime)
      if (this.isGracePeriod(order)) return true
      return (order.status === '已预订' || order.status === '已入住' || order.status === '已支付') && now < checkOutTime
    },
    getOrderStatusClass(order) {
      if (this.isGracePeriod(order)) return 'grace'
      if (this.isOrderExpired(order)) return 'expired'
      if (this.isOrderExpiringSoon(order)) return 'expiring-soon'
      if (order.status === '已入住') return 'checked-in'
      if (order.status === '已预订') return 'reserved'
      if (order.status === '已支付') return 'paid'
      return order.status
    },
    renewOrder(orderId) {
      this.$router.push(`/booking?renewOrderId=${orderId}`)
    },
    canCheckOutEarly(order) {
      const now = new Date()
      const checkOutTime = new Date(order.checkOutTime)
      return order.status === '已入住' && now < checkOutTime
    },
    isOrderExpired(order) {
      if (!order || !order.checkOutTime || order.status !== '已入住') return false
      const now = new Date()
      const checkOutTime = new Date(order.checkOutTime)
      return now > checkOutTime
    },
    isOrderExpiringSoon(order) {
      if (!order || !order.checkOutTime || order.status !== '已入住') return false
      const now = new Date()
      const checkOutTime = new Date(order.checkOutTime)
      const diff = checkOutTime.getTime() - now.getTime()
      // 15分钟内到期提醒（11:45-12:00）
      return diff > 0 && diff <= 15 * 60 * 1000
    },
    isGracePeriod(order) {
      if (!order || !order.checkOutTime || order.status !== '已入住') return false
      const now = new Date()
      const checkOutTime = new Date(order.checkOutTime)
      // 宽限期：12:00-13:00（退房后1小时内）
      const graceEnd = new Date(checkOutTime.getTime() + 60 * 60 * 1000)
      return now >= checkOutTime && now < graceEnd
    },
    startGraceCountdowns() {
      this.graceInterval = setInterval(() => {
        if (this.isDestroyed) {
          clearInterval(this.graceInterval)
          return
        }
        const updates = {}
        this.myOrders.forEach(order => {
          if (order.status === '已入住' && this.isGracePeriod(order)) {
            const graceEnd = new Date(order.checkOutTime).getTime() + 60 * 60 * 1000
            const remaining = Math.max(0, graceEnd - Date.now())
            const minutes = Math.floor(remaining / 60000)
            const seconds = Math.floor((remaining % 60000) / 1000)
            updates[order.id] = `宽限期剩余 ${minutes}分${seconds}秒`
          }
        })
        this.graceCountdowns = updates
      }, 1000)
    },
    showCheckOutModal(orderId) {
      this.currentCheckOutOrderId = orderId
      this.checkOutModalVisible = true
    },
    closeCheckOutModal() {
      this.checkOutModalVisible = false
      this.currentCheckOutOrderId = null
    },
    async handleConfirmCheckOut() {
      if (!this.currentCheckOutOrderId) return
      
      try {
        const response = await axios.put(`/api/user/orders/${this.currentCheckOutOrderId}/status?status=已完成`, {}, { withCredentials: true })
        if (response.data) {
          this.closeCheckOutModal()
          this.checkOutSuccessVisible = true
          this.getOrders()
        }
      } catch (error) {
        console.error('退房失败:', error)
        alert('退房失败，请稍后重试')
      }
    },
    goToOrderHistory() {
      this.checkOutSuccessVisible = false
      this.$router.push('/profile?tab=orderHistory&subTab=history')
    },
    async earlyCheckOut(orderId) {
      // 保留原有方法，以便其他地方调用
      try {
        const response = await axios.put(`/api/user/orders/${orderId}/status?status=已完成`, {}, { withCredentials: true })
        if (response.data) {
          alert('退房成功')
          this.getOrders()
        }
      } catch (error) {
        console.error('退房失败:', error)
        alert('退房失败，请稍后重试')
      }
    },
    goToBooking() {
      this.$router.push('/booking')
    },
    goToLogin() {
      console.log('跳转到登录页面')
      console.log('当前路由:', this.$route.path)
      console.log('路由对象:', this.$router)
      try {
        this.$router.push('/login').catch(err => {
          console.error('路由跳转失败:', err)
          console.log('尝试使用备用跳转方式')
          window.location.href = '/login'
        })
      } catch (error) {
        console.error('跳转出错:', error)
        console.log('使用备用跳转方式')
        window.location.href = '/login'
      }
    },
    startAutoRefresh() {
      const checkAndRefresh = () => {
        if (this.isDestroyed) {
          clearInterval(this.refreshInterval)
          return
        }
        if (document.visibilityState === 'visible' && this.myOrders.length > 0) {
          this.isAutoRefresh = true
          this.getOrders()
        }
      }
      setTimeout(checkAndRefresh, 30000)
      this.refreshInterval = setInterval(checkAndRefresh, 60000)
    }
  }
}
</script>

<style scoped>
.rooms {
  max-width: 1000px;
  margin: 0 auto;
}

.loading {
  text-align: center;
  padding: 0;
  color: var(--text-light);
}

.skeleton-card {
  background-color: var(--bg-white);
  padding: 1.5rem;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
  margin-bottom: 1.5rem;
}

.skeleton-title {
  width: 30%;
  height: 24px;
  margin-bottom: 1rem;
}

.skeleton-line {
  height: 16px;
  margin-bottom: 0.5rem;
}

.skeleton-line-long {
  width: 70%;
}

.skeleton-line-medium {
  width: 50%;
}

.skeleton-line-short {
  width: 30%;
}

.login-prompt-banner {
  background: linear-gradient(135deg, #e8f4fa 0%, #f0f7fd 100%);
  border: 1px solid #c5e2f2;
  border-radius: var(--radius-lg);
  padding: 1rem 1.5rem;
  margin-bottom: 2rem;
  box-shadow: var(--shadow-sm);
}

.login-prompt-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  flex-wrap: wrap;
}

.login-prompt-text {
  color: var(--text-secondary);
  font-size: 0.95rem;
}

.login-prompt-btn {
  padding: 0.5rem 1.5rem;
  background: var(--primary-gradient);
  color: var(--text-white);
  border: none;
  border-radius: var(--radius-sm);
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition);
  box-shadow: 0 4px 12px rgba(35, 133, 187, 0.25);
  text-decoration: none;
}

.login-prompt-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(35, 133, 187, 0.35);
}

.login-prompt-register {
  color: var(--primary-color);
  font-size: 0.9rem;
  text-decoration: none;
  font-weight: 500;
  padding: 0.4rem 0.8rem;
  border-radius: var(--radius-sm);
  transition: all var(--transition);
}

.login-prompt-register:hover {
  background: rgba(35, 133, 187, 0.08);
}

.error {
  text-align: center;
  padding: 3rem;
  color: var(--text-primary);
  background-color: var(--bg-white);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  margin: 1rem 0;
  box-shadow: var(--shadow-sm);
}

.error .btn {
  margin-top: 1rem;
}

.no-rooms {
  text-align: center;
  padding: 4rem 2rem;
  background-color: var(--bg-white);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  color: var(--text-secondary);
  font-size: 1.1rem;
  box-shadow: var(--shadow-sm);
}

.no-rooms .empty-icon {
  margin-bottom: 1.2rem;
  color: var(--text-light);
}

.no-rooms p {
  margin-bottom: 1.5rem;
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.order-item {
  background-color: var(--bg-white);
  padding: 1.5rem;
  padding-left: 1.25rem;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
  border-left: 4px solid var(--border-light);
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all var(--transition);
}

.order-item:hover {
  box-shadow: var(--shadow-md);
  border-color: var(--border-color);
}

.order-status-reserved {
  border-left-color: var(--status-info);
}

.order-status-checked-in {
  border-left-color: var(--status-success);
}

.order-status-paid {
  border-left-color: var(--status-success);
}

.order-status-expiring-soon {
  border-left-color: var(--status-warning);
}

.order-status-grace {
  border-left-color: var(--status-danger);
}

.order-status-expired {
  border-left-color: var(--status-danger);
}

.order-info {
  flex: 1;
}

.order-info h3 {
  margin: 0 0 1rem 0;
  color: var(--text-primary);
  font-size: 1.3rem;
}

.room-type-title {
  display: flex;
  align-items: center;
  gap: 0.4rem;
}

.room-type-title svg {
  flex-shrink: 0;
  color: var(--primary-color);
}

.order-info p {
  margin: 0.5rem 0;
  color: var(--text-secondary);
  font-size: 0.95rem;
}

.order-info .label {
  font-weight: 500;
  color: var(--text-primary);
  font-size: 0.85rem;
  min-width: 70px;
  display: inline-block;
}

.price {
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--primary-color);
}

.status-badge {
  display: inline-block;
  padding: 0.2rem 0.6rem;
  border-radius: var(--radius-sm);
  font-size: 0.8rem;
  font-weight: 500;
}

.status-reserved-badge {
  background: var(--status-info-bg);
  color: var(--status-info);
}

.status-checkedin-badge {
  background: var(--status-success-bg);
  color: var(--status-success);
}

.status-paid-badge {
  background: var(--status-success-bg);
  color: var(--status-success);
}

.status-expiring-badge {
  background: var(--status-warning-bg);
  color: var(--status-warning);
}

.status-expired-badge {
  background: var(--status-danger-bg);
  color: var(--status-danger);
}

.status-grace-badge {
  background: var(--status-danger-bg);
  color: var(--status-danger);
  font-size: 0.75rem;
}

.grace-actions {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.grace-countdown {
  color: var(--status-warning);
  font-size: 0.85rem;
  font-weight: 500;
}

.order-actions {
  display: flex;
  margin-left: 2rem;
}

.action-group {
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
}

.btn-cancel {
  background-color: var(--status-danger);
  color: var(--text-white);
}

.btn-cancel:hover:not(:disabled) {
  background-color: #e04040;
}

.btn-early-checkout {
  background-color: var(--status-danger);
  color: var(--text-white);
}

.btn-early-checkout:hover:not(:disabled) {
  background-color: #e04040;
}

.btn-renew {
  background-color: var(--status-warning);
  color: var(--text-white);
}

.btn-renew:hover:not(:disabled) {
  background-color: #cf9236;
}

.pagination-wrapper {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  align-items: center;
  margin-top: 2rem;
  padding-top: 1.5rem;
  border-top: 1px solid var(--border-light);
}

.pagination-info {
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap;
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
  min-width: 40px;
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
  width: 40px;
  height: 40px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: var(--bg-white);
  color: var(--text-primary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all var(--transition);
  font-size: 0.95rem;
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
  padding: 0 0.5rem;
  color: var(--text-light);
  display: flex;
  align-items: center;
  font-weight: bold;
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
  .pagination {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .jump-page {
    margin-left: 0;
    margin-top: 0.5rem;
  }
  
  .order-item {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .order-actions {
    margin-left: 0;
    margin-top: 1rem;
  }
  
  .action-group {
    flex-direction: row;
    flex-wrap: wrap;
  }
}

@media (max-width: 480px) {
  .page-btn {
    padding: 0.4rem 0.7rem;
    font-size: 0.85rem;
  }
  
  .page-number {
    width: 36px;
    height: 36px;
    font-size: 0.85rem;
  }
}
</style>
