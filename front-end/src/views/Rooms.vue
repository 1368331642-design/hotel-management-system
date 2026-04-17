<template>
  <div class="rooms">
    <h2>我的房间</h2>
    
    <div v-if="loading" class="loading">
      <p>加载中...</p>
    </div>

    <div v-else-if="error === '请先登录'" class="login-prompt">
      <p>您还没有登录</p>
      <button @click="goToLogin" class="btn">请先登录</button>
    </div>

    <div v-else-if="error" class="error">
      <p>{{ error }}</p>
      <button @click="getOrders" class="btn">重试</button>
    </div>

    <div v-else-if="myOrders.length === 0" class="no-rooms">
      <p>暂无预订或入住的房间</p>
      <button @click="goToBooking" class="btn">去预订</button>
    </div>

    <div v-else class="orders-list">
      <div v-for="order in myOrders" :key="order.id" class="order-item">
        <div class="order-info">
          <h3>{{ order.room?.roomType?.name }}</h3>
          <p><span class="label">房间号:</span> {{ order.room?.roomNumber }}</p>
          <p><span class="label">住房日期:</span> {{ formatDate(order.checkInTime) }} 至 {{ formatDate(order.checkOutTime) }}</p>
          <p><span class="label">总价:</span> ¥{{ order.totalPrice }}</p>
          <p><span class="label">状态:</span> 
            <span v-if="isOrderExpired(order)" class="status-expired">房间已到期</span>
            <span v-else-if="isOrderExpiringSoon(order)" class="status-expiring-soon">房间即将到期</span>
            <span v-else :class="'status-' + order.status">{{ order.status }}</span>
          </p>
        </div>
        <div class="order-actions">
          <div class="action-group">
            <button v-if="order.status === '已预订' || order.status === '已支付'" @click="showCancelModal(order.id)" class="btn btn-cancel">取消预定</button>
            <button v-if="canCheckOutEarly(order)" @click="showCheckOutModal(order.id)" class="btn btn-early-checkout">提前退房</button>
            <button v-else-if="isOrderExpired(order)" @click="showCheckOutModal(order.id)" class="btn btn-early-checkout">退房</button>
            <button v-if="canRenewOrder(order) || isOrderExpired(order) || isOrderExpiringSoon(order)" @click="renewOrder(order.id)" class="btn btn-renew">续订</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 确认退房弹窗 -->
    <div v-if="checkOutModalVisible" class="modal-overlay">
      <div class="modal-content">
        <h3>确认退房</h3>
        <p>确定要退房吗？</p>
        <div class="modal-actions">
          <button @click="closeCheckOutModal" class="btn">取消</button>
          <button @click="handleConfirmCheckOut" class="btn btn-confirm">确认</button>
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
    <div v-if="totalOrders > pageSize" class="pagination">
      <button 
        class="page-btn" 
        :disabled="currentPage === 0" 
        @click="changePage(currentPage - 1)">
        上一页
      </button>
      <span class="page-info">
        第 {{ currentPage + 1 }} / {{ totalPages }} 页，共 {{ totalOrders }} 条
      </span>
      <button 
        class="page-btn" 
        :disabled="currentPage >= totalPages - 1" 
        @click="changePage(currentPage + 1)">
        下一页
      </button>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'Rooms',
  data() {
    return {
      loading: true,
      isAutoRefresh: false,
      orders: [],
      refreshInterval: null,
      error: null,
      checkOutModalVisible: false,
      currentCheckOutOrderId: null,
      cancelModalVisible: false,
      currentCancelOrderId: null,
      userInfo: {
        id: null,
        username: '',
        name: '',
        phone: '',
        email: '',
        idCard: ''
      },
      currentPage: 0,
      pageSize: 10,
      totalOrders: 0
    }
  },
  computed: {
    totalPages() {
      return Math.ceil(this.totalOrders / this.pageSize)
    },
    myOrders() {
      if (!this.orders || this.orders.length === 0) {
        return []
      }
      // 使用内存中的用户信息，而不是直接从localStorage读取
      if (!this.userInfo || !this.userInfo.id) {
        return []
      }
      
      const now = new Date()
      
      const filteredOrders = this.orders
        .filter(order => order.user?.id === this.userInfo.id)
        .filter(order => {
          const statusMatch = (order.status === '已预订' || order.status === '已入住' || order.status === '已支付') && order.status !== '已退房'
          return statusMatch
        })
      
      return filteredOrders.sort((a, b) => new Date(a.checkInTime) - new Date(b.checkInTime))
    }
  },
  async mounted() {
    // 页面加载时初始化用户信息
    this.initUserInfo()
    if (this.userInfo && this.userInfo.id) {
      try {
        await this.getOrders()
      } catch (error) {
        console.error('getOrders方法执行失败:', error)
      }
    } else {
      // 用户未登录时也需要设置loading为false
      this.loading = false
      this.error = '请先登录'
    }
    this.startAutoRefresh()
  },
  activated() {
    // 组件激活时重新初始化用户信息并获取数据
    this.initUserInfo()
    if (this.userInfo && this.userInfo.id) {
      this.isAutoRefresh = false
      this.getOrders()
    } else {
      this.loading = false
      this.error = '请先登录'
    }
  },
  deactivated() {
    // 组件停用时停止自动刷新
    if (this.refreshInterval) {
      clearInterval(this.refreshInterval)
      this.refreshInterval = null
    }
  },
  beforeDestroy() {
    if (this.refreshInterval) {
      clearInterval(this.refreshInterval)
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
      // 只有非自动刷新时才显示loading
      if (!this.isAutoRefresh) {
        this.loading = true
      }
      this.error = null
      
      // 检查用户信息
      if (!this.userInfo || !this.userInfo.id) {
        this.orders = []
        this.error = '请先登录'
        this.loading = false
        this.isAutoRefresh = false
        return
      }
      
      try {
        // 添加超时控制
        const controller = new AbortController()
        const timeoutId = setTimeout(() => controller.abort(), 15000)
        
        // 使用分页接口
        const response = await fetch(`/api/user/orders/user/${this.userInfo.id}?page=${this.currentPage}&size=${this.pageSize}`, {
          method: 'GET',
          credentials: 'include',
          headers: {
            'Content-Type': 'application/json'
          },
          signal: controller.signal
        })
        
        clearTimeout(timeoutId)
        
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`)
        }
        
        const data = await response.json()
        
        // 处理Page对象，取content中的数据
        if (data.content) {
          this.orders = data.content
          this.totalOrders = data.totalElements || 0
        } else {
          this.orders = Array.isArray(data) ? data : []
          this.totalOrders = this.orders.length
        }
      } catch (error) {
        console.error('获取订单失败:', error)
        // 自动刷新失败时不显示错误
        if (!this.isAutoRefresh) {
          if (error.name === 'AbortError') {
            this.error = '请求超时，请检查网络连接'
          } else {
            this.error = '获取订单失败，请稍后重试'
          }
        }
      } finally {
        this.loading = false
        this.isAutoRefresh = false
      }
    },
    changePage(newPage) {
      this.currentPage = newPage
      this.getOrders()
      // 滚动到顶部
      window.scrollTo({ top: 0, behavior: 'smooth' })
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
      return (order.status === '已预订' || order.status === '已入住' || order.status === '已支付') && now < checkOutTime
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
      if (!order || !order.checkOutTime) return false
      const now = new Date()
      const checkOutTime = new Date(order.checkOutTime)
      return now > checkOutTime
    },
    isOrderExpiringSoon(order) {
      if (!order || !order.checkOutTime) return false
      const now = new Date()
      const checkOutTime = new Date(order.checkOutTime)
      const diff = checkOutTime.getTime() - now.getTime()
      // 检查是否在到期前半小时内
      return diff > 0 && diff <= 30 * 60 * 1000
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
          alert('退房成功')
          this.closeCheckOutModal()
          this.getOrders()
        }
      } catch (error) {
        console.error('退房失败:', error)
        alert('退房失败，请稍后重试')
      }
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
      this.refreshInterval = setInterval(() => {
        if (document.visibilityState === 'visible') {
          this.isAutoRefresh = true
          this.getOrders()
        }
      }, 60000) // 改为60秒刷新一次，减少频率
    }
  }
}
</script>

<style scoped>
.rooms {
  max-width: 1000px;
  margin: 0 auto;
}

.rooms h2 {
  text-align: center;
  margin-bottom: 2rem;
  color: var(--text-primary);
}

.loading {
  text-align: center;
  padding: 3rem;
  color: var(--text-secondary);
}

.login-prompt {
  text-align: center;
  padding: 4rem 2rem;
  background-color: var(--bg-white);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  margin: 1rem 0;
  box-shadow: var(--shadow-sm);
}

.login-prompt p {
  margin-bottom: 1.5rem;
  color: var(--text-primary);
  font-size: 1.2rem;
}

.error {
  text-align: center;
  padding: 3rem;
  color: var(--text-primary);
  background-color: var(--bg-white);
  border: 1px solid var(--border-color);
  border-radius: 4px;
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
  border: 1px solid var(--border-color);
  border-radius: 8px;
  color: var(--text-secondary);
  font-size: 1.1rem;
  box-shadow: var(--shadow-sm);
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
  border-radius: 8px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-info {
  flex: 1;
}

.order-info h3 {
  margin: 0 0 1rem 0;
  color: var(--text-primary);
  font-size: 1.4rem;
}

.order-info p {
  margin: 0.5rem 0;
  color: var(--text-secondary);
  font-size: 1rem;
}

.order-info .label {
  font-weight: 500;
  color: var(--text-primary);
}

.status-已预订 {
  color: var(--status-info);
  font-weight: 500;
}

.status-已入住 {
  color: var(--status-info);
  font-weight: 500;
}

.status-已完成 {
  color: var(--status-success);
  font-weight: 500;
}

.status-已取消 {
  color: var(--status-danger);
  font-weight: 500;
}

.status-已支付 {
  color: var(--status-success);
  font-weight: 500;
}

.status-expired {
  color: var(--status-danger);
  font-weight: 500;
}

.status-expiring-soon {
  color: var(--status-warning);
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

.btn {
  padding: 0.6rem 1.2rem;
  background-color: var(--primary-color);
  color: var(--text-white);
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.95rem;
}

.btn:hover:not(:disabled) {
  background-color: var(--primary-hover);
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.btn-cancel {
  background-color: var(--status-danger);
}

.btn-cancel:hover {
  background-color: #d94c4c;
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.btn-renew {
  background-color: var(--primary-color);
}

.btn-renew:hover {
  background-color: var(--primary-hover);
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.btn-early-checkout {
  background-color: var(--status-danger);
  color: var(--text-white);
}

.btn-early-checkout:hover {
  background-color: #d94c4c;
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.btn-confirm {
  background-color: var(--primary-color);
}

.btn-confirm:hover {
  background-color: var(--primary-hover);
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.modal-overlay {
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

.modal-content {
  background-color: var(--bg-white);
  padding: 2rem;
  border-radius: 8px;
  width: 90%;
  max-width: 400px;
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--border-color);
}

.modal-content h3 {
  margin-top: 0;
  margin-bottom: 1.5rem;
  color: var(--text-primary);
  text-align: center;
}

.modal-content p {
  margin-bottom: 1.5rem;
  color: var(--text-secondary);
  text-align: center;
}

.modal-actions {
  display: flex;
  justify-content: center;
  gap: 1rem;
}

/* 分页样式 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  margin-top: 2rem;
  padding: 1rem 0;
}

.page-btn {
  padding: 0.6rem 1.2rem;
  background-color: var(--primary-color);
  color: var(--text-white);
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.95rem;
}

.page-btn:hover:not(:disabled) {
  background-color: var(--primary-hover);
  transform: translateY(-1px);
}

.page-btn:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.page-info {
  color: var(--text-secondary);
  font-size: 0.95rem;
}
</style>
