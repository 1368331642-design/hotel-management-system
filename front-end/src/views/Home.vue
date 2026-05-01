<template>
  <div class="home">
    <!-- 未登录：显示登录提示横幅 + 用户端首页内容 -->
    <template v-if="!isLoggedIn">
      <div class="login-prompt-banner">
        <div class="login-prompt-content">
          <span class="login-prompt-text">登录后可查看我的订单、在线预订及更多功能</span>
          <button @click="goToLogin" class="login-prompt-btn">登录系统</button>
          <router-link to="/register" class="login-prompt-register">注册账号</router-link>
        </div>
      </div>

      <div class="hero">
        <h2>欢迎光临翻斗花园酒店</h2>
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
          <h3>房间查询</h3>
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
    </template>

    <!-- 已登录：根据角色显示仪表板 -->
    <template v-else>
      <!-- 管理员首页 -->
      <div v-if="isAdmin" class="admin-home">
        <div class="hero">
          <h2>管理员中心</h2>
          <p>财务统计和服务日志管理</p>
        </div>
        <div class="features">
          <div class="feature" @click="goToAdmin" style="cursor: pointer;">
            <div class="feature-icon">🛠️</div>
            <h3>管理中心</h3>
            <p>系统配置和数据管理</p>
          </div>
          <div class="feature" @click="goToFinance" style="cursor: pointer;">
            <div class="feature-icon">💰</div>
            <h3>财务统计</h3>
            <p>财务报表和营收数据分析</p>
          </div>
          <div class="feature" @click="goToServiceLogs" style="cursor: pointer;">
            <div class="feature-icon">📋</div>
            <h3>服务日志</h3>
            <p>前台服务和用户评价管理</p>
          </div>
        </div>
      </div>

      <!-- 前台首页 -->
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

      <!-- 用户端首页 -->
      <div v-else class="user-home">
        <div class="hero">
          <h2>欢迎光临翻斗花园酒店</h2>
          <p>为您提供便捷、高效的酒店预订和管理服务</p>
          <div class="buttons">
            <button @click="goToBooking" class="btn">去订房</button>
            <button @click="goToRooms" class="btn btn-outline">我的房间</button>
          </div>
        </div>
        <div class="user-order-section">
          <div v-if="currentOrders.length > 0" class="current-order">
            <div class="current-order-header">
              <h3>我的房间</h3>
              <span class="order-count-badge">{{ currentOrders.length }}间</span>
            </div>
            <div class="orders-list">
              <div v-for="order in currentOrders" :key="order.id"
                class="order-item" :class="{ expanded: expandedOrders[order.id] }"
                @click="toggleOrderExpand(order.id)">
                <div class="order-summary">
                  <div class="order-summary-left">
                    <span class="order-room-number">{{ order.room?.roomNumber }}</span>
                    <span class="order-status-tag" :class="'tag-' + getOrderStatusKey(order)">{{ getOrderStatusText(order) }}</span>
                </div>
                  <span class="expand-arrow" :class="{ rotated: expandedOrders[order.id] }">▼</span>
                </div>
              <transition name="slide-fade">
                <div v-if="expandedOrders[order.id]" class="order-detail" @click.stop>
                  <div class="order-detail-compact">
                    <div class="order-detail-item">
                      <span class="label">住房日期</span>
                      <span class="order-date-range">{{ formatDate(order.checkInTime) }} 至 {{ formatDate(order.checkOutTime) }}</span>
                    </div>
                  </div>
                  <div v-if="order.status === '待支付'" class="order-detail-item">
                    <span class="label">支付倒计时</span>
                    <span class="countdown" :class="{ 'countdown-expired': getCountdown(order.createTime) <= 0 }">{{ formatCountdown(getCountdown(order.createTime)) }}</span>
                  </div>
                  <div class="order-actions">
                    <button v-if="order.status === '待支付'" @click="goToPayment(order.id)" class="btn btn-pay">去支付</button>
                    <button v-if="order.status === '待支付'" @click="showCancelModal(order.id)" class="btn btn-cancel">取消订单</button>
                    <button v-else-if="isOrderExpired(order)" @click="showCheckOutModal(order.id)" class="btn btn-checkout">退房</button>
                    <button v-else @click="goToRooms" class="btn btn-room-detail">房间详情</button>
                    <button v-if="isOrderExpired(order) || isOrderExpiringSoon(order)" @click="goToRenew(order.id)" class="btn btn-renew">续订</button>
                  </div>
                </div>
              </transition>
              </div>
            </div>
          </div>
          <div class="order-decoration">
          <svg viewBox="0 0 320 200" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="160" cy="100" r="130" fill="url(#deco-glow)" opacity="0.15" />
            <defs>
              <linearGradient id="deco-glow" x1="0" y1="0" x2="1" y2="1">
                <stop offset="0%" stop-color="#2385BB" stop-opacity="0.4" />
                <stop offset="100%" stop-color="#E6A23C" stop-opacity="0.1" />
              </linearGradient>
            </defs>
            <rect x="84" y="58" width="152" height="120" rx="4" fill="#2385BB" fill-opacity="0.12" stroke="#2385BB" stroke-width="1.5" stroke-opacity="0.25" />
            <rect x="80" y="54" width="160" height="6" rx="3" fill="#2385BB" fill-opacity="0.2" />
            <rect x="96" y="72" width="18" height="22" rx="2" fill="#2385BB" fill-opacity="0.15" />
            <rect x="122" y="72" width="18" height="22" rx="2" fill="#2385BB" fill-opacity="0.15" />
            <rect x="148" y="72" width="18" height="22" rx="2" fill="#2385BB" fill-opacity="0.15" />
            <rect x="174" y="72" width="18" height="22" rx="2" fill="#2385BB" fill-opacity="0.15" />
            <rect x="200" y="72" width="18" height="22" rx="2" fill="#2385BB" fill-opacity="0.15" />
            <rect x="122" y="72" width="18" height="22" rx="2" fill="#E6A23C" fill-opacity="0.4" />
            <rect x="174" y="72" width="18" height="22" rx="2" fill="#E6A23C" fill-opacity="0.4" />
            <rect x="96" y="104" width="18" height="22" rx="2" fill="#2385BB" fill-opacity="0.15" />
            <rect x="122" y="104" width="18" height="22" rx="2" fill="#2385BB" fill-opacity="0.15" />
            <rect x="148" y="104" width="18" height="22" rx="2" fill="#2385BB" fill-opacity="0.15" />
            <rect x="174" y="104" width="18" height="22" rx="2" fill="#2385BB" fill-opacity="0.15" />
            <rect x="200" y="104" width="18" height="22" rx="2" fill="#2385BB" fill-opacity="0.15" />
            <rect x="148" y="104" width="18" height="22" rx="2" fill="#E6A23C" fill-opacity="0.4" />
            <rect x="148" y="142" width="24" height="36" rx="8" fill="#2385BB" fill-opacity="0.18" stroke="#2385BB" stroke-width="1" stroke-opacity="0.3" />
            <ellipse cx="60" cy="158" rx="18" ry="22" fill="#67C23A" fill-opacity="0.18" />
            <rect x="57" y="158" width="6" height="20" rx="2" fill="#67C23A" fill-opacity="0.25" />
            <ellipse cx="260" cy="155" rx="16" ry="20" fill="#67C23A" fill-opacity="0.15" />
            <rect x="257" y="155" width="6" height="18" rx="2" fill="#67C23A" fill-opacity="0.22" />
            <line x1="40" y1="178" x2="280" y2="178" stroke="#2385BB" stroke-width="1" stroke-dasharray="4 3" stroke-opacity="0.15" />
            <text x="40" y="40" font-size="8" fill="#E6A23C" opacity="0.3">✦</text>
            <text x="72" y="28" font-size="6" fill="#E6A23C" opacity="0.2">✦</text>
            <text x="260" y="38" font-size="7" fill="#E6A23C" opacity="0.25">✦</text>
            <text x="290" y="50" font-size="5" fill="#E6A23C" opacity="0.2">✦</text>
            <ellipse cx="270" cy="30" rx="28" ry="10" fill="#2385BB" fill-opacity="0.06" />
            <ellipse cx="290" cy="28" rx="18" ry="8" fill="#2385BB" fill-opacity="0.04" />
          </svg>
        </div>
      </div>
        <div class="features-section-label">
          <span>功能服务</span>
        </div>
        <div class="features">
          <div class="feature" @click="goToBooking" style="cursor: pointer;">
            <div class="feature-icon">📅</div>
            <h3>在线预订</h3>
            <p>便捷的在线预订系统，让您随时随地预订心仪的房间</p>
          </div>
          <div class="feature" @click="goToRoomStatus" style="cursor: pointer;">
            <div class="feature-icon">🏨</div>
            <h3>房间查询</h3>
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
    </template>

    <!-- 酒店信息 - 显示在页面底部 -->
    <div v-if="hotelInfo && !isAdmin && !isFront" class="hotel-info">
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
      countdownNow: Date.now(),
      checkOutModalVisible: false,
      currentCheckOutOrderId: null,
      cancelModalVisible: false,
      currentCancelOrderId: null,
      isDestroyed: false,
      ordersLoading: false,
      _hasBeenHidden: false,
      userInfo: {
        id: null,
        username: '',
        name: '',
        phone: '',
        email: '',
        idCard: ''
      },
      expandedOrders: {}
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
  created() {
    // 组件渲染前初始化登录状态，避免白屏
    this.initUserInfo()
    this.checkUserRole()
  },
  mounted() {
    // 页面加载时初始化用户信息（created 中已执行，此处确保二次加载）
    this.getHotelInfo()
    // 仅已登录用户才加载订单和启动定时器
    if (this.isLoggedIn) {
      this.getOrders()
      this.startAutoRefresh()
      this.startCountdownTimer()
    }
  },
  beforeUnmount() {
    this.isDestroyed = true
    if (this.refreshInterval) {
      clearInterval(this.refreshInterval)
      this.refreshInterval = null
    }
    if (this.countdownTimer) {
      clearInterval(this.countdownTimer)
      this.countdownTimer = null
    }
    document.removeEventListener('visibilitychange', this.handleVisibilityChange)
  },
  methods: {
    goToLogin() {
      this.$router.push('/login/select')
    },
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
      if (this.isDestroyed || this.ordersLoading) return
      this.ordersLoading = true
      try {
        if (this.userInfo && this.userInfo.id) {
          const response = await axios.get(`/api/user/orders/user/${this.userInfo.id}/active`, {
            params: { page: 0, size: 20 },
            withCredentials: true
          })
          if (!this.isDestroyed) {
            this.orders = response.data.content || response.data
          }
        }
      } catch (error) {
        if (axios.isCancel(error)) return
        console.error('获取订单失败:', error)
      } finally {
        this.ordersLoading = false
      }
    },
    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-CN')
    },
    startAutoRefresh() {
      const checkAndRefresh = () => {
        if (this.isDestroyed) {
          clearInterval(this.refreshInterval)
          return
        }
        if (document.visibilityState === 'visible' && this.orders.length > 0) {
          this.getOrders()
        }
      }
      setTimeout(checkAndRefresh, 30000)
      this.refreshInterval = setInterval(checkAndRefresh, 60000)
    },
    async getHotelInfo() {
      if (this.isDestroyed) return
      try {
        const response = await axios.get('/api/user/hotel-info', { withCredentials: true })
        if (!this.isDestroyed) {
          this.hotelInfo = response.data
        }
      } catch (error) {
        if (axios.isCancel(error)) return
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
        this.$router.push('/rooms')
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
    goToFinance() {
      this.$router.push('/admin/finance')
    },
    goToServiceLogs() {
      this.$router.push('/admin/logs')
    },
    goToFrontDesk() {
      this.$router.push('/front-desk')
    },
    goToPayment(orderId) {
      // 跳转到支付页面
      this.$router.push(`/booking?orderId=${orderId}&pay=true`)
    },
    getCountdown(createTime) {
      // 计算15分钟倒计时 - 使用响应式的 countdownNow 驱动实时更新
      const createDate = new Date(createTime)
      const diff = createDate.getTime() + 15 * 60 * 1000 - this.countdownNow
      return Math.max(0, Math.floor(diff / 1000))
    },
    formatCountdown(seconds) {
      const minutes = Math.floor(seconds / 60)
      const remainingSeconds = seconds % 60
      return `${minutes.toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}`
    },
    startCountdownTimer() {
      this.countdownTimer = setInterval(() => {
        if (this.isDestroyed) {
          clearInterval(this.countdownTimer)
          return
        }
        this.countdownNow = Date.now()
        this.orders.forEach(order => {
          if (order.status === '待支付' && this.getCountdown(order.createTime) <= 0) {
            this.cancelOrder(order.id)
          }
        })
      }, 1000)
      document.addEventListener('visibilitychange', this.handleVisibilityChange)
    },
    handleVisibilityChange() {
      if (this.isDestroyed) return
      if (document.visibilityState === 'visible') {
        this.countdownNow = Date.now()
        if (this._hasBeenHidden) {
          this.getOrders()
        }
      } else {
        this._hasBeenHidden = true
      }
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
    },
    toggleOrderExpand(orderId) {
      this.expandedOrders = {
        ...this.expandedOrders,
        [orderId]: !this.expandedOrders[orderId]
      }
    },
    getOrderStatusKey(order) {
      if (this.isOrderExpired(order)) return 'expired'
      if (this.isOrderExpiringSoon(order)) return 'expiring'
      if (order.status === '待支付' || order.status === '已支付') return 'reserved'
      return order.status
    },
    getOrderStatusText(order) {
      if (this.isOrderExpired(order)) return '房间已到期'
      if (this.isOrderExpiringSoon(order)) return '即将到期'
      if (order.status === '待支付') return '待支付'
      if (order.status === '已支付') return '已预订'
      return order.status
    }
  }
}
</script>

<style scoped>
.home {
  max-width: 1280px;
  margin: 0 auto;
}

.hotel-info {
  background: var(--bg-white);
  padding: 2.5rem 2rem;
  border-radius: var(--radius-lg);
  margin-top: 3rem;
  margin-bottom: 0;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
  position: relative;
  overflow: hidden;
}

.hotel-info h2 {
  text-align: center;
  margin-bottom: 1.2rem;
  font-size: 2rem;
  font-weight: 700;
  color: var(--text-primary);
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
  background: var(--bg-white);
  padding: 0.7rem 1rem;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
  position: relative;
  overflow: hidden;
  flex: none;
  width: 420px;
}

.user-order-section {
  display: flex;
  align-items: stretch;
  gap: 1.5rem;
  margin-bottom: 1.2rem;
}

.order-decoration {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-white);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-light);
  overflow: hidden;
  min-height: 140px;
  position: relative;
}

.order-decoration svg {
  width: 100%;
  max-width: 320px;
  height: auto;
}

.current-order-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.4rem;
}

.current-order::before {
  display: none;
}

.order-count-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 32px;
  padding: 0.1rem 0.5rem;
  background: var(--primary-color);
  color: #fff;
  font-size: 0.72rem;
  font-weight: 600;
  border-radius: 12px;
  line-height: 1.4;
  flex-shrink: 0;
}

.current-order h3 {
  margin: 0;
  color: var(--text-primary);
  font-size: 0.9rem;
  font-weight: 600;
  position: relative;
  z-index: 1;
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.order-item {
  background: var(--bg-white);
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-light);
  cursor: pointer;
  user-select: none;
  overflow: hidden;
}

.order-item:hover {
  border-color: var(--primary-light);
  box-shadow: var(--shadow-sm);
}

.order-item.expanded {
  border-color: var(--primary-light);
  box-shadow: var(--shadow-sm);
  background: var(--bg-white);
}

.order-summary {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.4rem 0.7rem;
  min-height: 32px;
}

.order-summary-left {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.order-room-number {
  font-size: 0.9rem;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: 1px;
}

.order-status-tag {
  display: inline-block;
  padding: 0.15rem 0.5rem;
  border-radius: 20px;
  font-size: 0.72rem;
  font-weight: 600;
  white-space: nowrap;
}

.tag-已预订, .tag-reserved {
  background: var(--status-info-bg);
  color: var(--status-info);
}

.tag-已入住 {
  background: var(--status-success-bg);
  color: var(--status-success);
}

.tag-待支付 {
  background: var(--status-warning-bg);
  color: var(--status-warning);
}

.tag-expiring {
  background: var(--status-warning-bg);
  color: var(--status-warning);
}

.tag-expired {
  background: var(--status-danger-bg);
  color: var(--status-danger);
}

.tag-已完成 {
  background: var(--status-success-bg);
  color: var(--status-success);
}

.expand-arrow {
  font-size: 0.6rem;
  color: var(--text-light);
  flex-shrink: 0;
}

.expand-arrow.rotated {
  transform: rotate(180deg);
  color: var(--primary-color);
}

.order-detail {
  padding: 0 0.7rem 0.7rem;
  border-top: 1px solid var(--border-light);
  margin-top: 0;
  padding-top: 0.6rem;
}

.order-detail-compact {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0.4rem 0;
}

.order-detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 0.4rem;
  margin-bottom: 0.5rem;
}

.order-detail-item {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
}

.order-detail-item .label {
  font-size: 0.7rem;
  font-weight: 500;
  color: var(--text-light);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.order-detail-item span:not(.label) {
  font-size: 0.82rem;
  color: var(--text-primary);
  font-weight: 500;
}

.order-price {
  font-size: 0.95rem !important;
  font-weight: 700 !important;
  color: var(--primary-color) !important;
}

.order-date-range {
  font-size: 0.85rem !important;
  color: var(--primary-color) !important;
  font-weight: 600 !important;
  letter-spacing: 0.3px;
}

.order-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.4rem;
  flex-wrap: wrap;
  padding-top: 0.5rem;
  border-top: 1px solid var(--border-light);
}

.btn-checkout {
  background-color: var(--status-danger);
  color: var(--text-white);
  padding: 0.35rem 0.7rem;
  font-size: 0.78rem;
  line-height: 1.2;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.btn-checkout:hover {
  background-color: #d94c4c;
}

.btn-renew {
  background-color: var(--primary-color);
  color: var(--text-white);
  padding: 0.35rem 0.7rem;
  font-size: 0.78rem;
  line-height: 1.2;
  margin-left: 0.5rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.btn-renew:hover {
  background-color: var(--primary-hover);
}

.btn-pay {
  background-color: var(--status-warning);
  color: var(--text-white);
  padding: 0.35rem 0.7rem;
  font-size: 0.78rem;
  line-height: 1.2;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.btn-pay:hover {
  background-color: #cf8a2e;
}

.countdown {
  color: var(--status-warning);
  font-weight: 500;
}

.countdown-expired {
  color: var(--status-danger);
  font-weight: 500;
}

.btn-room-detail {
  background-color: var(--primary-color);
  color: var(--text-white);
  padding: 0.35rem 0.7rem;
  font-size: 0.78rem;
  line-height: 1.2;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.btn-room-detail:hover {
  background-color: var(--primary-hover);
}

.btn-cancel {
  background-color: var(--status-danger);
  color: var(--text-white);
  padding: 0.35rem 0.7rem;
  font-size: 0.78rem;
  line-height: 1.2;
  margin-right: 0.5rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.btn-cancel:hover {
  background-color: #d94c4c;
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
}

.btn-early-checkout:hover {
  background-color: var(--primary-hover);
}

/* ======= 未登录提示横幅 ======= */
.login-prompt-banner {
  background: var(--status-info-bg);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  padding: 1rem 1.5rem;
  margin-bottom: 2rem;
  box-shadow: var(--shadow-xs);
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
  background: var(--primary-color);
  color: var(--text-white);
  border: none;
  border-radius: var(--radius-sm);
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  box-shadow: var(--shadow-xs);
  text-decoration: none;
}

.login-prompt-register {
  color: var(--primary-color);
  font-size: 0.9rem;
  text-decoration: none;
  font-weight: 500;
  padding: 0.4rem 0.8rem;
  border-radius: var(--radius-sm);
}

/* ======= 管理员端专属样式 ======= */
.admin-home .features .feature:nth-child(1) .feature-icon {
  background: var(--status-info-bg);
}

.admin-home .features .feature:nth-child(2) .feature-icon {
  background: var(--status-info-bg);
}

.admin-home .features .feature:nth-child(3) .feature-icon {
  background: var(--status-warning-bg);
}

.admin-home .features .feature:nth-child(4) .feature-icon {
  background: var(--status-success-bg);
}

/* ======= 前台端专属样式 ======= */
.front-home .features .feature .feature-icon {
  background: var(--status-info-bg);
}

.hero {
  text-align: center;
  padding: 5rem 2rem;
  background: var(--bg-white);
  border-radius: var(--radius-lg);
  margin-bottom: 2.5rem;
  border: 1px solid var(--border-light);
  box-shadow: var(--shadow-sm);
  position: relative;
  overflow: hidden;
}

.hero::before,
.hero::after {
  display: none;
}

.hero h2 {
  font-size: 2.4rem;
  margin-bottom: 1rem;
  font-weight: 700;
  color: var(--text-primary);
  position: relative;
  z-index: 1;
}

.hero p {
  font-size: 1.15rem;
  margin-bottom: 2rem;
  color: var(--text-secondary);
  position: relative;
  z-index: 1;
}

.buttons {
  display: flex;
  justify-content: center;
  gap: 1.2rem;
  flex-wrap: wrap;
  position: relative;
  z-index: 1;
}

.btn-outline {
  background-color: transparent !important;
  border: 2px solid var(--primary-color) !important;
  color: var(--primary-color) !important;
}

.btn-outline:hover {
  background-color: var(--primary-color) !important;
  color: var(--text-white) !important;
}

.features {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 1.8rem;
}

.features-section-label {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  margin-bottom: 1rem;
  margin-top: 0.5rem;
  position: relative;
}

.features-section-label::before,
.features-section-label::after {
  content: '';
  flex: 1;
  height: 1px;
  background: var(--border-color);
}

.features-section-label span {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--text-light);
  letter-spacing: 1px;
  text-transform: uppercase;
  white-space: nowrap;
  padding: 0 0.5rem;
}

.feature {
  padding: 2.5rem 1.5rem 2rem;
  background: var(--bg-white);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-light);
  text-align: center;
  box-shadow: var(--shadow-sm);
  position: relative;
  overflow: hidden;
}

.feature-icon {
  width: 68px;
  height: 68px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.8rem;
  margin: 0 auto 1.2rem;
  background: var(--status-info-bg);
  box-shadow: var(--shadow-xs);
  position: relative;
  z-index: 1;
}

.feature h3 {
  font-size: 1.3rem;
  margin-bottom: 0.6rem;
  color: var(--text-primary);
  font-weight: 600;
  letter-spacing: 0.5px;
  position: relative;
  z-index: 1;
}

.feature p {
  color: var(--text-light);
  margin: 0;
  font-size: 0.92rem;
  line-height: 1.6;
  position: relative;
  z-index: 1;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.45);
  backdrop-filter: blur(4px);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: var(--bg-white);
  padding: 2.5rem;
  border-radius: var(--radius-lg);
  width: 90%;
  max-width: 400px;
  box-shadow: var(--shadow-md);
}

.modal-content h3 {
  margin-top: 0;
  margin-bottom: 1.5rem;
  color: var(--text-primary);
  text-align: center;
  font-weight: 600;
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
  background: var(--primary-color) !important;
  color: var(--text-white) !important;
}
</style>
