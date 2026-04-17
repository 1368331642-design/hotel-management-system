<template>
  <div class="home">
    <div v-if="isAdmin" class="admin-home">
      <div class="hero">
        <h2>管理员中心</h2>
        <p>系统配置管理和数据统计</p>
        <div class="buttons">
          <button @click="goToAdmin" class="btn">系统配置</button>
          <button @click="goToAdminData" class="btn btn-outline">数据管理</button>
        </div>
      </div>
      <div class="features">
        <div class="feature" @click="goToAdmin" style="cursor: pointer;">
          <div class="feature-icon">⚙️</div>
          <h3>系统配置</h3>
          <p>酒店基础信息、客房资源管理</p>
        </div>
        <div class="feature" @click="goToAdminData" style="cursor: pointer;">
          <div class="feature-icon">📊</div>
          <h3>数据管理</h3>
          <p>预订数据统计、服务日志管理</p>
        </div>
      </div>
    </div>

    <div v-else-if="isFront" class="front-home">
      <div class="hero">
        <h2>前台服务中心</h2>
        <p>客户咨询处理和客房状态管理</p>
        <div class="buttons">
          <button @click="goToFrontDesk" class="btn">前台服务</button>
        </div>
      </div>
      <div class="features">
        <div class="feature" @click="goToFrontDesk" style="cursor: pointer;">
          <div class="feature-icon">💬</div>
          <h3>服务咨询</h3>
          <p>处理客户咨询和客房问题</p>
        </div>
        <div class="feature" @click="goToFrontDesk" style="cursor: pointer;">
          <div class="feature-icon">🏠</div>
          <h3>客房状态</h3>
          <p>确认和更新客房状态</p>
        </div>
      </div>
    </div>

    <div v-else class="user-home">
      <div v-if="hotelInfo" class="hotel-info">
        <h2>{{ hotelInfo.name }}</h2>
        <div class="hotel-details">
          <div class="hotel-detail">
            <span class="icon">📍</span>
            <span>{{ hotelInfo.address }}</span>
          </div>
          <div class="hotel-detail">
            <span class="icon">📞</span>
            <span>{{ hotelInfo.phone }}</span>
          </div>
          <div class="hotel-detail">
            <span class="icon">📧</span>
            <span>{{ hotelInfo.email }}</span>
          </div>
        </div>
        <p v-if="hotelInfo.description" class="hotel-description">{{ hotelInfo.description }}</p>
      </div>
      <div v-if="currentOrders.length > 0" class="current-order">
        <h3>我的房间</h3>
        <div class="orders-list">
          <div v-for="order in currentOrders" :key="order.id" class="order-item">
            <div class="order-info-item">
              <span class="label">房间号:</span> {{ order.room?.roomNumber }}
            </div>
            <div class="order-info-item">
              <span class="label">房型:</span> {{ order.room?.roomType?.name }}
            </div>
            <div class="order-info-item">
              <span class="label">住房日期:</span> {{ formatDate(order.checkInTime) }} 至 {{ formatDate(order.checkOutTime) }}
            </div>
            <div class="order-info-item">
              <span class="label">总价:</span> ¥{{ order.totalPrice }}
            </div>
            <div class="order-info-item">
              <span class="label">状态:</span> 
              <span v-if="isOrderExpired(order)" class="status-expired">房间已到期</span>
              <span v-else-if="isOrderExpiringSoon(order)" class="status-expiring-soon">房间即将到期</span>
              <span v-else :class="'status-' + (order.status === '待支付' || order.status === '已支付' ? '已预订' : order.status)">{{ order.status === '待支付' || order.status === '已支付' ? '已预订' : order.status }}</span>
            </div>
            <div v-if="order.status === '待支付'" class="order-info-item">
              <span class="label">支付倒计时:</span> <span class="countdown" :class="{ 'countdown-expired': getCountdown(order.createTime) <= 0 }">{{ formatCountdown(getCountdown(order.createTime)) }}</span>
            </div>
            <div class="order-actions">
              <button v-if="order.status === '待支付'" @click="goToPayment(order.id)" class="btn btn-pay">去支付</button>
              <button v-if="order.status === '待支付'" @click="showCancelModal(order.id)" class="btn btn-cancel">取消订单</button>
              <button v-else-if="isOrderExpired(order)" @click="showCheckOutModal(order.id)" class="btn btn-checkout">退房</button>
              <button v-else @click="goToRooms" class="btn btn-room-detail">房间详情</button>
              <button v-if="isOrderExpired(order) || isOrderExpiringSoon(order)" @click="goToRenew(order.id)" class="btn btn-renew">续订</button>
            </div>
          </div>
        </div>
      </div>
      <div class="hero">
        <h2>欢迎使用酒店管理系统</h2>
        <p>为您提供便捷、高效的酒店预订和管理服务</p>
        <div class="buttons">
          <button @click="goToBooking" class="btn">去订房</button>
          <button @click="goToRooms" class="btn btn-outline">我的房间</button>
        </div>
      </div>
      <div class="features">
        <div class="feature" @click="goToBooking" style="cursor: pointer;">
          <div class="feature-icon">📅</div>
          <h3>在线预订</h3>
          <p>便捷的在线预订系统，让您随时随地预订心仪的房间</p>
        </div>
        <div class="feature" @click="goToRoomStatus" style="cursor: pointer;">
          <div class="feature-icon">🏨</div>
          <h3>实时房态</h3>
          <p>实时查看房间状态，确保您能预订到可用的房间</p>
        </div>
        <div class="feature" @click="goToFrontService" style="cursor: pointer;">
          <div class="feature-icon">💬</div>
          <h3>前台服务</h3>
          <p>呼叫前台和问题反馈，及时解决您的需求</p>
        </div>
        <div class="feature" @click="goToProfile" style="cursor: pointer;">
          <div class="feature-icon">📋</div>
          <h3>订单管理</h3>
          <p>方便的订单管理功能，随时查看和管理您的预订</p>
        </div>
      </div>
    </div>

    <!-- 确认退房弹窗 -->
    <div v-if="checkOutModalVisible" class="modal-overlay">
      <div class="modal-content">
        <h3>确认退房</h3>
        <p>确定要退房吗？</p>
        <div class="modal-actions">
          <button @click="closeCheckOutModal" class="btn btn-cancel">取消</button>
          <button @click="handleConfirmCheckOut" class="btn btn-confirm">确认</button>
        </div>
      </div>
    </div>

    <!-- 确认取消订单弹窗 -->
    <div v-if="cancelModalVisible" class="modal-overlay">
      <div class="modal-content">
        <h3>确认取消订单</h3>
        <p>确定要取消这个预定吗？</p>
        <div class="modal-actions">
          <button @click="closeCancelModal" class="btn btn-cancel">取消</button>
          <button @click="handleConfirmCancel" class="btn btn-confirm">确认</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'Home',
  data() {
    return {
      isLoggedIn: false,
      isAdmin: false,
      isFront: false,
      hotelInfo: null,
      orders: [],
      refreshInterval: null,
      countdownTimer: null,
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
      }
    }
  },
  computed: {
    currentOrders() {
      if (!this.orders || this.orders.length === 0) return []
      const now = new Date()
      return this.orders
        .filter(order => 
          (order.status === '已预订' || order.status === '已入住' || order.status === '待支付' || order.status === '已支付') && 
          order.status !== '已退房'
        )
        .sort((a, b) => new Date(a.createTime) - new Date(b.createTime))
    }
  },
  mounted() {
    // 页面加载时初始化用户信息
    this.initUserInfo()
    this.checkUserRole()
    this.getHotelInfo()
    this.getOrders()
    this.startAutoRefresh()
    this.startCountdownTimer()
  },
  beforeDestroy() {
    if (this.refreshInterval) {
      clearInterval(this.refreshInterval)
    }
    if (this.countdownTimer) {
      clearInterval(this.countdownTimer)
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
      try {
        // 使用内存中的用户信息，而不是直接从localStorage读取
        if (this.userInfo && this.userInfo.id) {
          // 使用 fetch API 替代 axios
          const response = await fetch('/api/user/orders?page=0&size=1000', {
            method: 'GET',
            credentials: 'include',
            headers: {
              'Content-Type': 'application/json'
            }
          })
          const data = await response.json()
          // 处理Page对象，取content中的数据
          const ordersData = data.content || data
          // 过滤出当前用户的订单
          let userOrders = ordersData.filter(order => order.user?.id === this.userInfo.id)
          
          // 对订单进行去重，保留最新的订单
          // 按房间 ID 和日期进行分组
          const orderMap = new Map()
          userOrders.forEach(order => {
            // 生成唯一键：房间 ID + 入住时间 + 退房时间
            const key = `${order.room?.id}-${order.checkInTime}-${order.checkOutTime}`
            // 如果键不存在，或者当前订单的创建时间比已存在的订单晚，则更新
            if (!orderMap.has(key) || new Date(order.createTime) > new Date(orderMap.get(key).createTime)) {
              orderMap.set(key, order)
            }
          })
          
          // 将 Map 转换回数组
          this.orders = Array.from(orderMap.values())
        }
      } catch (error) {
        console.error('获取订单失败:', error)
      }
    },
    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-CN')
    },
    startAutoRefresh() {
      this.refreshInterval = setInterval(() => {
        // 只有当页面可见时才刷新
        if (document.visibilityState === 'visible') {
          this.getOrders()
        }
      }, 60000) // 60秒刷新一次
    },
    async getHotelInfo() {
      try {
        const response = await axios.get('/api/user/hotel-info', { withCredentials: true })
        this.hotelInfo = response.data
      } catch (error) {
        console.error('获取酒店信息失败:', error)
      }
    },
    checkUserRole() {
      // 使用内存中的用户信息，而不是直接从localStorage读取
      if (this.userInfo && this.userInfo.username) {
        this.isLoggedIn = true
        this.isAdmin = this.userInfo.username === 'admin' || this.userInfo.roles?.some(role => role.name === 'ADMIN')
        this.isFront = this.userInfo.username === 'front' || this.userInfo.roles?.some(role => role.name === 'FRONT')
      } else {
        this.isLoggedIn = false
        this.isAdmin = false
        this.isFront = false
      }
    },
    checkLogin() {
      // 使用内存中的用户信息，而不是直接从localStorage读取
      if (!this.userInfo || !this.userInfo.username) {
        this.$router.push('/login')
        return false
      }
      return true
    },
    goToBooking() {
      if (this.checkLogin()) {
        this.$router.push('/booking')
      }
    },
    goToRooms() {
      if (this.checkLogin()) {
        this.$router.push('/rooms')
      }
    },
    goToProfile() {
      if (this.checkLogin()) {
        this.$router.push('/profile?tab=orderHistory')
      }
    },
    goToRoomStatus() {
      if (this.checkLogin()) {
        this.$router.push('/room-status')
      }
    },
    goToFrontService() {
      if (this.checkLogin()) {
        this.$router.push('/front-service')
      }
    },
    goToAdmin() {
      this.$router.push('/admin')
    },
    goToAdminData() {
      this.$router.push('/admin')
    },
    goToFrontDesk() {
      this.$router.push('/front-desk')
    },
    goToPayment(orderId) {
      // 跳转到支付页面
      this.$router.push(`/booking?orderId=${orderId}&pay=true`)
    },
    getCountdown(createTime) {
      // 计算15分钟倒计时
      const createDate = new Date(createTime)
      const now = new Date()
      const diff = createDate.getTime() + 15 * 60 * 1000 - now.getTime()
      return Math.max(0, Math.floor(diff / 1000))
    },
    formatCountdown(seconds) {
      const minutes = Math.floor(seconds / 60)
      const remainingSeconds = seconds % 60
      return `${minutes.toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}`
    },
    startCountdownTimer() {
      // 启动倒计时定时器，每5秒钟检查一次
      this.countdownTimer = setInterval(() => {
        // 检查所有待支付订单的倒计时
        this.orders.forEach(order => {
          if (order.status === '待支付' && this.getCountdown(order.createTime) <= 0) {
            // 倒计时结束，自动取消订单
            this.cancelOrder(order.id)
          }
        })
        // 只有当页面可见时才刷新订单列表
        if (document.visibilityState === 'visible') {
          this.getOrders()
        }
      }, 5000) // 每5秒钟检查一次
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
        await axios.put(`/api/user/orders/${this.currentCancelOrderId}/cancel`, {}, { withCredentials: true })
        // 刷新订单列表
        this.getOrders()
        alert('预定取消成功')
        this.closeCancelModal()
      } catch (error) {
        console.error('取消订单失败:', error)
        alert('取消预定失败，请稍后重试')
        this.closeCancelModal()
      }
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
        await axios.put(`/api/user/orders/${this.currentCheckOutOrderId}/status?status=已完成`, {}, { withCredentials: true })
        // 刷新订单列表
        this.getOrders()
        alert('退房成功')
        this.closeCheckOutModal()
      } catch (error) {
        console.error('退房失败:', error)
        alert('退房失败，请稍后重试')
      }
    },
    async earlyCheckOut(orderId) {
      // 保留原有方法，以便其他地方调用
      try {
        await axios.put(`/api/user/orders/${orderId}/status?status=已完成`, {}, { withCredentials: true })
        // 刷新订单列表
        this.getOrders()
        alert('提前退房成功')
      } catch (error) {
        console.error('提前退房失败:', error)
        alert('提前退房失败，请稍后重试')
      }
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
    goToRenew(orderId) {
      this.$router.push(`/booking?orderId=${orderId}&renew=true`)
    }
  }
}
</script>

<style scoped>
.home {
  max-width: 1200px;
  margin: 0 auto;
}

.hotel-info {
  background-color: var(--bg-white);
  padding: 2rem;
  border-radius: 8px;
  margin-bottom: 2rem;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-color);
}

.hotel-info h2 {
  text-align: center;
  margin-bottom: 1.5rem;
  color: var(--text-primary);
  font-size: 2rem;
}

.hotel-details {
  display: flex;
  justify-content: center;
  gap: 2rem;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
}

.hotel-detail {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--text-secondary);
  font-size: 1.1rem;
}

.hotel-detail .icon {
  font-size: 1.3rem;
}

.hotel-description {
  text-align: center;
  color: var(--text-light);
  font-size: 1.05rem;
  line-height: 1.6;
  margin: 0;
}

.current-order {
  background-color: var(--bg-white);
  padding: 1.5rem;
  border-radius: 8px;
  margin-bottom: 2rem;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-color);
}

.current-order h3 {
  margin-bottom: 1rem;
  color: var(--text-primary);
  font-size: 1.3rem;
  border-bottom: 2px solid var(--primary-color);
  padding-bottom: 0.5rem;
}

.current-order-info {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.current-order-info p {
  margin: 0;
  color: var(--text-secondary);
  font-size: 1rem;
}

.current-order-info .label {
  font-weight: 500;
  color: var(--text-primary);
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.order-item {
  background-color: var(--bg-light);
  padding: 1rem;
  border-radius: 8px;
  border: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
  flex-wrap: wrap;
}

.order-info-item {
  display: flex;
  align-items: center;
  gap: 0.3rem;
  white-space: nowrap;
}

.order-info-item .label {
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

.status-待支付 {
  color: var(--status-warning);
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

.btn-checkout {
  background-color: var(--status-danger);
  color: var(--text-white);
  padding: 0.5rem 1rem;
  font-size: 0.9rem;
  line-height: 1.2;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-checkout:hover {
  background-color: #d94c4c;
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.btn-renew {
  background-color: var(--primary-color);
  color: var(--text-white);
  padding: 0.5rem 1rem;
  font-size: 0.9rem;
  line-height: 1.2;
  margin-left: 0.5rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-renew:hover {
  background-color: var(--primary-hover);
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.btn-pay {
  background-color: var(--status-warning);
  color: var(--text-white);
  padding: 0.5rem 1rem;
  font-size: 0.9rem;
  line-height: 1.2;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-pay:hover {
  background-color: #cf8a2e;
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.countdown {
  color: var(--status-warning);
  font-weight: 500;
}

.countdown-expired {
  color: var(--status-danger);
  font-weight: 500;
}

.order-actions {
  display: flex;
  justify-content: flex-end;
  margin-left: 1rem;
  flex-shrink: 0;
}

.btn-room-detail {
  background-color: var(--primary-color);
  color: var(--text-white);
  padding: 0.5rem 1rem;
  font-size: 0.9rem;
  line-height: 1.2;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-room-detail:hover {
  background-color: var(--primary-hover);
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.btn-cancel {
  background-color: var(--status-danger);
  color: var(--text-white);
  padding: 0.5rem 1rem;
  font-size: 0.9rem;
  line-height: 1.2;
  margin-right: 0.5rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-cancel:hover {
  background-color: #d94c4c;
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.btn-early-checkout {
  background-color: var(--primary-color);
  color: var(--text-white);
  padding: 0.5rem 1rem;
  font-size: 0.9rem;
  line-height: 1.2;
  margin-right: 0.5rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-early-checkout:hover {
  background-color: var(--primary-hover);
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.hero {
  text-align: center;
  padding: 4rem 0;
  background-color: var(--bg-white);
  border-radius: 8px;
  margin-bottom: 2rem;
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-sm);
}

.hero h2 {
  font-size: 2.5rem;
  margin-bottom: 1rem;
  color: var(--text-primary);
}

.hero p {
  font-size: 1.2rem;
  margin-bottom: 2rem;
  color: var(--text-secondary);
}

.buttons {
  display: flex;
  justify-content: center;
  gap: 1rem;
}

.btn {
  padding: 0.8rem 1.5rem;
  background-color: var(--primary-color);
  color: var(--text-white);
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 1rem;
}

.btn:hover {
  background-color: var(--primary-hover);
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.btn-outline {
  background-color: transparent;
  border: 1px solid var(--primary-color);
  color: var(--primary-color);
}

.btn-outline:hover {
  background-color: var(--primary-color);
  color: var(--text-white);
}

.features {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2rem;
}

.feature {
  padding: 2rem;
  background-color: var(--bg-white);
  border-radius: 8px;
  border: 1px solid var(--border-color);
  text-align: center;
  transition: all 0.3s ease;
}

.feature:hover {
  border-color: var(--primary-color);
  transform: translateY(-5px);
  box-shadow: var(--shadow-md);
}

.feature-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.feature h3 {
  font-size: 1.5rem;
  margin-bottom: 1rem;
  color: var(--text-primary);
}

.feature p {
  color: var(--text-secondary);
  margin: 0;
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

.btn-confirm {
  background-color: var(--primary-color);
}

.btn-confirm:hover {
  background-color: var(--primary-hover);
}
</style>
