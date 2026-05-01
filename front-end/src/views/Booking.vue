<template>
  <div class="booking">
    <h2 class="page-title">{{ isRenew ? '续订' : '在线预订' }}</h2>
    
    <!-- 未登录提示横幅 -->
    <div v-if="!isLoggedIn" class="login-prompt-banner">
      <div class="login-prompt-content">
        <span class="login-prompt-text">登录后可在在线预订、查看和管理您的预订</span>
        <button @click="goToLogin" class="login-prompt-btn">登录系统</button>
        <router-link to="/register" class="login-prompt-register">注册账号</router-link>
      </div>
    </div>

    <template v-else>
      <div v-if="loading" class="loading">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else class="booking-content">
      <form @submit.prevent="submitBooking">
        <!-- 续订模式下直接显示原订单的房型和房间信息 -->
        <div v-if="isRenew && selectedRoomType" class="form-section">
          <h3>续订信息</h3>
          <div class="renew-info">
            <p><strong>房型：</strong>{{ selectedRoomType.name }}</p>
            <p><strong>房间号：</strong>{{ selectedRoom.roomNumber }}</p>
            <p><strong>价格：</strong>¥{{ selectedRoomType.price }}/晚</p>
          </div>
        </div>

        <!-- 非续订模式下显示房型选择 -->
        <div v-else class="form-section">
          <h3>选择房型</h3>
          <div v-if="roomTypes.length === 0" class="no-rooms">
            <p>暂无可用房间</p>
          </div>
          <div v-else class="room-types">
            <div 
              v-for="roomType in roomTypes" 
              :key="roomType.id"
              class="room-type-card"
              :class="{ active: selectedRoomType?.id === roomType.id }"
              @click="selectRoomType(roomType)"
            >
              <div v-if="roomType.image" class="room-type-image">
                <img :src="roomType.image" :alt="roomType.name">
              </div>
              <div class="room-type-info">
                <h4>{{ roomType.name }}</h4>
                <p class="price">¥{{ roomType.price }}/晚</p>
                <p class="capacity">可住{{ roomType.capacity }}人</p>
                <p class="facilities">{{ roomType.facilities }}</p>
                <div v-if="selectedRoomType?.id === roomType.id && form.checkInDate && form.checkOutDate" class="price-preview">
                  <p class="preview-label">预计价格：</p>
                  <p class="preview-price">¥{{ calculatePreviewPrice(roomType) }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 续订模式下选择续订时长 -->
        <div v-if="isRenew && selectedRoomType" class="form-section">
          <h3>选择续订时长</h3>
          <div class="duration-section-center">
            <div class="form-group-center">
              <label for="renewDuration">续订时长（天）</label>
              <select id="renewDuration" v-model="renewDuration" @change="calculateCheckOutDate" required>
                <option value="1">1天</option>
                <option value="2">2天</option>
                <option value="3">3天</option>
                <option value="5">5天</option>
                <option value="7">7天</option>
                <option value="14">14天</option>
                <option value="30">30天</option>
              </select>
            </div>
          </div>
        </div>

        <!-- 非续订模式下选择时间 -->
        <div v-else-if="!isRenew && selectedRoomType" class="form-section">
          <h3>选择时间</h3>
          <div class="date-section">
            <div class="form-group">
              <label for="checkInDate">住房日期</label>
              <input type="date" id="checkInDate" v-model="form.checkInDate" :min="minDate" @change="validateCheckInDate" @input="validateCheckInDate" required>
              <div v-if="checkInDateError" class="error-message">{{ checkInDateError }}</div>
            </div>
            <div class="form-group">
              <label for="checkOutDate" style="visibility: hidden; margin-bottom: 0.5rem; display: block;">退</label>
              <input type="date" id="checkOutDate" v-model="form.checkOutDate" :min="minCheckOutDate" @change="validateCheckOutDate" @input="validateCheckOutDate" required>
              <div v-if="checkOutDateError" class="error-message">{{ checkOutDateError }}</div>
            </div>
          </div>
        </div>

        <!-- 非续订模式下显示房间选择 -->
        <div v-if="!isRenew && selectedRoomType && form.checkInDate && form.checkOutDate && !checkInDateError" class="form-section">
          <h3>选择房间</h3>
          <div v-if="availableRooms.length === 0" class="no-rooms-warning">
            <p>已无空房</p>
            <div v-if="earliestCheckoutInfo" class="earliest-checkout">
              <p>最快退房时间：{{ earliestCheckoutInfo }}</p>
            </div>
          </div>
          <div v-else class="available-rooms-list">
            <div 
              v-for="room in availableRooms" 
              :key="room.id"
              class="room-item"
              :class="{ active: selectedRoom?.id === room.id }"
              @click="selectRoom(room)"
            >
              {{ room.roomNumber }}
            </div>
          </div>
        </div>

        <div v-if="selectedRoomType && selectedRoom && form.checkInDate && form.checkOutDate && totalHours > 0" class="form-section price-section">
          <h3>{{ isRenew ? '续订信息' : '预订信息' }}</h3>
          <div class="price-summary">
            <div v-if="!isRenew" class="price-item">
              <span>房型：</span>
              <span>{{ selectedRoomType.name }}</span>
            </div>
            <div class="price-item">
              <span>房间号：</span>
              <span>{{ selectedRoom.roomNumber }}</span>
            </div>
            <div class="price-item">
              <span>{{ isRenew ? '续订日期：' : '入住时间：' }}</span>
              <span>{{ formatDateTime(form.checkInDate) }}</span>
            </div>
            <div class="price-item">
              <span></span>
              <span>{{ formatDateTime(form.checkOutDate) }}</span>
            </div>
            <div class="price-item total">
              <span>总计：</span>
              <span>¥{{ totalPrice }}</span>
            </div>
          </div>
        </div>

        <button v-if="canSubmit" type="submit" class="btn" :disabled="submitting">
          {{ submitting ? '提交中...' : (isRenew ? '续订' : `住${durationText}`) }}
        </button>
      </form>

      <div v-if="showPaymentForm" class="modal-overlay">
        <div class="modal-content">
          <h3>{{ isRenew ? '续订支付' : '预订支付' }}</h3>
          <div class="payment-info">
            <p class="payment-amount">支付金额：¥{{ totalPrice }}</p>
            <p class="payment-details">
              {{ selectedRoomType.name }} - {{ formatDateTime(form.checkInDate) }} 至 {{ formatDateTime(form.checkOutDate) }}
            </p>
          </div>
          <div class="payment-methods">
            <h4>选择支付方式</h4>
            <div class="payment-option">
              <input type="radio" id="alipay" name="paymentMethod" value="alipay" checked>
              <label for="alipay">支付宝</label>
            </div>
            <div class="payment-option">
              <input type="radio" id="wechat" name="paymentMethod" value="wechat">
              <label for="wechat">微信支付</label>
            </div>
            <div class="payment-option">
              <input type="radio" id="card" name="paymentMethod" value="card">
              <label for="card">银行卡</label>
            </div>
          </div>
          <div class="modal-actions">
            <button @click="cancelPayment" class="btn btn-ghost" :disabled="paymentProcessing">取消</button>
            <button @click="processPayment" class="btn btn-confirm" :disabled="paymentProcessing">
              {{ paymentProcessing ? '支付中...' : '确认支付' }}
            </button>
          </div>
        </div>   <!-- closes modal-content -->
      </div>     <!-- closes modal-overlay -->
      </div>     <!-- closes booking-content -->
    </template>  <!-- closes v-else template -->
  </div>         <!-- closes booking -->
</template>

<script>
import axios from 'axios'

export default {
  name: 'Booking',
  data() {
    return {
      loading: true,
      isLoggedIn: false,
      loadingAvailability: false,
      submitting: false,
      isRenew: false,
      renewOrderId: null,
      roomTypes: [],
      allRooms: [],
      availableRoomsList: [],
      roomEarliestCheckout: {},
      selectedRoomType: null,
      selectedRoom: null,
      totalHours: 0,
      durationText: '',
      totalPrice: 0,
      earliestCheckoutInfo: '',
      checkInDateError: '',
      checkOutDateError: '',
      showPaymentForm: false,
      paymentProcessing: false,
      renewDuration: 1,
      stayDuration: 1,
      currentOrderId: null,
      pendingRenewData: null,
      abortController: null,
      isDestroyed: false,
      form: {
        checkInDate: '',
        checkOutDate: ''
      }
    }
  },
  computed: {
    availableRoomTypes() {
      const availableRoomTypeIds = new Set(
        this.availableRoomsList.map(room => room.roomType?.id).filter(id => id)
      )
      return this.roomTypes.filter(rt => availableRoomTypeIds.has(rt.id))
    },
    availableRooms() {
      if (!this.selectedRoomType) return []
      return this.availableRoomsList.filter(room => 
        room.roomType?.id === this.selectedRoomType.id
      )
    },
    minDate() {
      const now = new Date()
      const year = now.getFullYear()
      const month = String(now.getMonth() + 1).padStart(2, '0')
      const day = String(now.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    },
    minCheckOutDate() {
      if (!this.form.checkInDate) return this.minDate
      const checkIn = new Date(this.form.checkInDate)
      // 退房日期至少比入住日期晚一天
      const minCheckOut = new Date(checkIn.getTime() + 24 * 60 * 60 * 1000)
      const year = minCheckOut.getFullYear()
      const month = String(minCheckOut.getMonth() + 1).padStart(2, '0')
      const day = String(minCheckOut.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    },
    canSubmit() {
      return this.selectedRoomType && 
             this.selectedRoom &&
             this.form.checkInDate && 
             this.form.checkOutDate && 
             this.totalHours > 0 &&
             !this.checkInDateError &&
             !this.checkOutDateError
    }
  },
  created() {
    // 组件渲染前检查登录状态，避免白屏闪烁
    this.checkLoginStatus()
  },
  async mounted() {
    if (!this.isLoggedIn) {
      this.loading = false
      return
    }
    if (this.isDestroyed) return
    const roomId = this.$route.query.roomId
    const checkInDate = this.$route.query.checkInDate
    const checkOutDate = this.$route.query.checkOutDate
    const renewOrderId = this.$route.query.renewOrderId
    const orderId = this.$route.query.orderId
    const pay = this.$route.query.pay
    if (checkInDate) {
      this.form.checkInDate = checkInDate
    }
    if (checkOutDate) {
      this.form.checkOutDate = checkOutDate
    }
    if (renewOrderId) {
      this.isRenew = true
      this.renewOrderId = renewOrderId
      await this.loadRenewOrder(renewOrderId)
    }
    if (orderId && pay === 'true') {
      await this.loadOrderForPayment(orderId)
    }
    await this.loadData()
    if (this.isDestroyed) return
    const roomsRes = await axios.get('/api/user/rooms', {
      params: {
        page: 0,
        size: 100
      },
      withCredentials: true
    })
    if (!this.isDestroyed) {
      this.allRooms = roomsRes.data.content || roomsRes.data
      this.loading = false
    }
  },
  beforeUnmount() {
    this.isDestroyed = true
    if (this.abortController) {
      this.abortController.abort()
      this.abortController = null
    }
  },
  methods: {
    checkLoginStatus() {
      try {
        const userStr = sessionStorage.getItem('user')
        if (!userStr) {
          this.isLoggedIn = false
          return
        }
        const user = JSON.parse(userStr)
        if (!user || !user.id) {
          this.isLoggedIn = false
          return
        }
        this.isLoggedIn = true
      } catch (e) {
        console.error('检查登录状态失败:', e)
        this.isLoggedIn = false
      }
    },
    goToLogin() {
      this.$router.push('/login/select')
    },
    formatDateTimeForDisplay(dateString) {
      const date = new Date(dateString)
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      return `${year}/${month}/${day} ${hours}:${minutes}`
    },
    calculatePreviewPrice(roomType) {
      if (!this.form.checkInDate || !this.form.checkOutDate) {
        return 0
      }
      
      const checkIn = new Date(this.form.checkInDate)
      const checkOut = new Date(this.form.checkOutDate)
      
      // 计算天数差
      const diffTime = checkOut - checkIn
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
      
      if (diffDays > 0) {
        // 按天数计算价格，使用数据库中的实际价格
        return roomType.price * diffDays
      }
      return 0
    },
    validateCheckInDate() {
      this.checkInDateError = ''
      if (!this.form.checkInDate) return
      
      // 续订模式下不验证入住日期，因为入住日期是原订单的退房时间
      if (!this.isRenew) {
        const checkIn = new Date(this.form.checkInDate)
        const minDate = new Date(this.minDate)
        
        if (checkIn < minDate) {
          this.checkInDateError = `住房日期要在${this.minDate}之后`
          return
        }
      }
      
      // 如果已经选择了退房日期，验证退房日期
      if (this.form.checkOutDate) {
        this.validateCheckOutDate()
      }
    },
    validateCheckOutDate() {
      this.checkOutDateError = ''
      if (!this.form.checkOutDate || !this.form.checkInDate) return
      
      const checkOut = new Date(this.form.checkOutDate)
      const checkIn = new Date(this.form.checkInDate)
      
      if (checkOut <= checkIn) {
        this.checkOutDateError = '结束日期必须在起始日期之后'
        return
      }
      
      this.calculateDuration()
      this.calculatePrice()
      
      // 计算完退房日期后，检查房间可用性
      if (!this.isRenew && this.selectedRoomType && !this.checkInDateError) {
        this.checkAvailability()
      }
    },
    async checkAvailability() {
      if (this.isDestroyed) return
      if (this.checkInDateError || this.checkOutDateError) {
        this.availableRoomsList = []
        this.roomEarliestCheckout = {}
        this.selectedRoom = null
        this.earliestCheckoutInfo = ''
        return
      }
      if (!this.form.checkInDate || !this.form.checkOutDate) {
        this.availableRoomsList = []
        this.roomEarliestCheckout = {}
        this.selectedRoom = null
        this.earliestCheckoutInfo = ''
        return
      }
      this.loadingAvailability = true
      try {
        if (this.abortController) {
          this.abortController.abort()
        }
        this.abortController = new AbortController()
        const checkInDateTime = new Date(this.form.checkInDate)
        checkInDateTime.setHours(14, 0, 0, 0)
        const checkOutDateTime = new Date(this.form.checkOutDate)
        checkOutDateTime.setHours(12, 0, 0, 0)
        const formatDateTime = (date) => {
          const year = date.getFullYear()
          const month = String(date.getMonth() + 1).padStart(2, '0')
          const day = String(date.getDate()).padStart(2, '0')
          const hours = String(date.getHours()).padStart(2, '0')
          const minutes = String(date.getMinutes()).padStart(2, '0')
          return `${year}-${month}-${day}T${hours}:${minutes}`
        }
        const response = await axios.get('/api/user/rooms/available', {
          params: {
            checkInDate: formatDateTime(checkInDateTime),
            checkOutDate: formatDateTime(checkOutDateTime)
          },
          withCredentials: true,
          signal: this.abortController.signal
        })
        if (this.isDestroyed) return
        this.availableRoomsList = response.data.availableRooms
        this.roomEarliestCheckout = response.data.roomEarliestCheckout
        this.selectedRoom = null
        this.earliestCheckoutInfo = ''
        this.calculatePrice()
      } catch (error) {
        if (axios.isCancel(error)) return
        console.error('查询可用房间失败:', error)
        if (!this.isDestroyed) {
          alert('查询可用房间失败，请稍后重试')
        }
      } finally {
        if (!this.isDestroyed) {
          this.loadingAvailability = false
        }
      }
    },
    async loadData() {
      if (this.isDestroyed) return
      try {
        const roomTypesRes = await axios.get('/api/user/room-types', { withCredentials: true })
        if (!this.isDestroyed) {
          this.roomTypes = roomTypesRes.data
        }
      } catch (error) {
        if (axios.isCancel(error)) return
        console.error('加载数据失败:', error)
        if (!this.isDestroyed) {
          alert('加载数据失败，请刷新页面重试')
        }
      }
    },
    async loadRenewOrder(orderId) {
      if (this.isDestroyed) return
      try {
        const response = await axios.get('/api/user/orders', {
          params: {
            page: 0,
            size: 50
          },
          withCredentials: true
        })
        if (this.isDestroyed) return
        const ordersData = response.data.content || response.data
        const order = ordersData.find(o => o.id === parseInt(orderId))
        if (order) {
          if (order.checkOutTime) {
            const checkOutDate = new Date(order.checkOutTime)
            const year = checkOutDate.getFullYear()
            const month = String(checkOutDate.getMonth() + 1).padStart(2, '0')
            const day = String(checkOutDate.getDate()).padStart(2, '0')
            this.form.checkInDate = `${year}-${month}-${day}`
          } else {
            const now = new Date()
            const year = now.getFullYear()
            const month = String(now.getMonth() + 1).padStart(2, '0')
            const day = String(now.getDate()).padStart(2, '0')
            this.form.checkInDate = `${year}-${month}-${day}`
          }
          if (order.room && order.room.roomType) {
            this.selectedRoomType = order.room.roomType
            this.selectedRoom = order.room
          }
          this.calculateCheckOutDate()
        }
      } catch (error) {
        if (axios.isCancel(error)) return
        console.error('加载续订订单失败:', error)
      }
    },
    async loadOrderForPayment(orderId) {
      if (this.isDestroyed) return
      try {
        const response = await axios.get('/api/user/orders', {
          params: {
            page: 0,
            size: 50
          },
          withCredentials: true
        })
        if (this.isDestroyed) return
        const ordersData = response.data.content || response.data
        const order = ordersData.find(o => o.id === parseInt(orderId))
        if (order) {
          if (order.room && order.room.roomType) {
            this.selectedRoomType = order.room.roomType
            this.selectedRoom = order.room
          }
          if (order.checkInTime) {
            const checkInDate = new Date(order.checkInTime)
            const year = checkInDate.getFullYear()
            const month = String(checkInDate.getMonth() + 1).padStart(2, '0')
            const day = String(checkInDate.getDate()).padStart(2, '0')
            this.form.checkInDate = `${year}-${month}-${day}`
          }
          if (order.checkOutTime) {
            const checkOutDate = new Date(order.checkOutTime)
            const year = checkOutDate.getFullYear()
            const month = String(checkOutDate.getMonth() + 1).padStart(2, '0')
            const day = String(checkOutDate.getDate()).padStart(2, '0')
            this.form.checkOutDate = `${year}-${month}-${day}`
          }
          this.totalPrice = order.totalPrice
          this.totalHours = 24
          this.durationText = '1天'
          this.currentOrderId = order.id
          this.availableRoomsList = [order.room]
          this.showPaymentForm = true
        }
      } catch (error) {
        if (axios.isCancel(error)) return
        console.error('加载订单失败:', error)
        if (!this.isDestroyed) {
          alert('加载订单失败，请稍后重试')
        }
      }
    },
    calculateCheckOutDate() {
      if (!this.form.checkInDate) return
      
      const checkIn = new Date(this.form.checkInDate)
      const duration = this.isRenew ? this.renewDuration : this.stayDuration
      const checkOut = new Date(checkIn.getTime() + duration * 24 * 60 * 60 * 1000)
      
      const year = checkOut.getFullYear()
      const month = String(checkOut.getMonth() + 1).padStart(2, '0')
      const day = String(checkOut.getDate()).padStart(2, '0')
      
      this.form.checkOutDate = `${year}-${month}-${day}`
      this.calculateDuration()
      this.calculatePrice()
      
      // 计算完退房日期后，检查房间可用性
      if (!this.isRenew && this.selectedRoomType && !this.checkInDateError) {
        this.checkAvailability()
      }
    },
    formatDateForInput(dateString) {
      const date = new Date(dateString)
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    },
    formatDateTime(dateString) {
      const date = new Date(dateString)
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    },
    selectRoomType(roomType) {
      this.selectedRoomType = roomType
      this.selectedRoom = null
      this.earliestCheckoutInfo = ''
      
      if (this.form.checkInDate && this.form.checkOutDate && !this.checkInDateError && !this.checkOutDateError) {
        this.checkAvailability()
      }
      this.calculatePrice()
    },
    findEarliestCheckout(roomTypeId) {
      const sameTypeRooms = this.allRooms.filter(room => 
        room.roomType?.id === roomTypeId
      )
      
      let earliestDate = null
      let earliestRoom = null
      
      for (const room of sameTypeRooms) {
        const checkoutTime = this.roomEarliestCheckout[room.id]
        if (checkoutTime) {
          const date = new Date(checkoutTime)
          if (!earliestDate || date < earliestDate) {
            earliestDate = date
            earliestRoom = room
          }
        }
      }
      
      if (earliestDate) {
        this.earliestCheckoutInfo = `${earliestRoom.roomNumber}号房间 ${this.formatDateTime(earliestDate)}`
      }
    },
    selectRoom(room) {
      this.selectedRoom = room
      this.earliestCheckoutInfo = ''
      this.calculatePrice()
    },
    calculateDuration() {
      if (!this.form.checkInDate || !this.form.checkOutDate) {
        this.totalHours = 0
        this.durationText = ''
        return
      }
      
      const checkIn = new Date(this.form.checkInDate)
      const checkOut = new Date(this.form.checkOutDate)
      const diffTime = checkOut - checkIn
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
      
      this.totalHours = diffDays * 24 // 保持totalHours用于其他计算
      
      if (diffDays > 0) {
        this.durationText = `${diffDays}天`
      } else {
        this.durationText = '0天'
      }
    },
    calculatePrice() {
      if (!this.selectedRoomType || !this.form.checkInDate || !this.form.checkOutDate) {
        this.totalPrice = 0
        return
      }
      
      const checkIn = new Date(this.form.checkInDate)
      const checkOut = new Date(this.form.checkOutDate)
      
      // 计算天数差
      const diffTime = checkOut - checkIn
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
      
      if (diffDays > 0) {
        // 按天数计算价格，使用数据库中的实际价格
        this.totalPrice = this.selectedRoomType.price * diffDays
      } else {
        this.totalPrice = 0
      }
    },
    async submitBooking() {
      if (!this.canSubmit) return
      
      this.submitting = true
      try {
        const user = JSON.parse(sessionStorage.getItem('user'))
        if (!user) {
          this.$router.push('/login')
          return
        }

        // 将日期格式转换为日期时间格式
        const checkInDateTime = new Date(this.form.checkInDate)
        checkInDateTime.setHours(14, 0, 0, 0) // 默认入住时间为14:00
        
        const checkOutDateTime = new Date(this.form.checkOutDate)
        checkOutDateTime.setHours(12, 0, 0, 0) // 默认退房时间为12:00
        
        let response
        if (this.isRenew && this.renewOrderId) {
          // 续订模式：保存续订数据，等支付成功后再调用API
          this.pendingRenewData = {
            renewOrderId: this.renewOrderId,
            checkOutTime: checkOutDateTime,
            totalPrice: this.totalPrice
          }
          // 保存一个临时订单ID用于显示支付界面
          this.currentOrderId = this.renewOrderId
          // 跳转到支付界面
          this.showPaymentForm = true
        } else {
          // 新订单模式：创建新订单
          const orderData = {
            room: {
              id: this.selectedRoom.id
            },
            user: {
              id: user.id
            },
            checkInTime: checkInDateTime,
            checkOutTime: checkOutDateTime,
            totalPrice: this.totalPrice
          }
          response = await axios.post('/api/user/orders', orderData)
          
          if (response.data) {
            // 保存订单ID
            this.currentOrderId = response.data.id
            // 跳转到支付界面
            this.showPaymentForm = true
          }
        }
      } catch (error) {
        console.error(this.isRenew ? '续订失败:' : '预订失败:', error)
        alert(this.isRenew ? '续订失败，请稍后重试' : '预订失败，请稍后重试')
        this.submitting = false
      }
    },
    async processPayment() {
      try {
        // 模拟支付处理
        this.paymentProcessing = true
        // 模拟支付延迟
        await new Promise(resolve => setTimeout(resolve, 2000))
        
        if (this.isRenew && this.pendingRenewData) {
          // 续订模式：先执行续订操作
          const renewData = {
            checkOutTime: this.pendingRenewData.checkOutTime,
            totalPrice: this.pendingRenewData.totalPrice
          }
          await axios.put(`/api/user/orders/${this.pendingRenewData.renewOrderId}/renew`, renewData)
          
          // 续订成功，不需要更新订单状态为已支付（因为是续订已有订单）
          this.pendingRenewData = null
        } else if (this.currentOrderId) {
          // 新订单模式：更新订单状态为已支付
          try {
            await axios.put(`/api/user/orders/${this.currentOrderId}/status?status=已支付`)
          } catch (error) {
            console.error('更新订单状态失败:', error)
          }
        }
        
        // 模拟支付成功
        alert(this.isRenew ? '续订成功' : '预订成功')
        this.currentOrderId = null
        this.$router.push('/')
      } catch (error) {
        console.error('支付失败:', error)
        alert('支付失败，请稍后重试')
      } finally {
        this.paymentProcessing = false
        this.showPaymentForm = false
      }
    },
    async cancelPayment() {
      if (this.currentOrderId && !this.isRenew) {
        // 新订单模式下，取消支付不取消订单，改为待支付状态
        try {
          await axios.put(`/api/user/orders/${this.currentOrderId}/status?status=待支付`)
        } catch (error) {
          console.error('更新订单状态失败:', error)
        }
        this.currentOrderId = null
      } else if (this.isRenew) {
        // 续订模式下，取消支付不取消订单，清除待支付的续订数据
        this.currentOrderId = null
        this.pendingRenewData = null
      }
      this.showPaymentForm = false
      this.submitting = false
      // 新订单模式下跳转到用户首页，续订模式下仅关闭支付界面
      if (!this.isRenew) {
        this.$router.push('/')
      }
    }
  }
}
</script>

<style scoped>
.booking {
  max-width: 900px;
  margin: 0 auto;
}

.booking-content {
  background-color: var(--bg-white);
  padding: 2rem;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-light);
}

.form-section {
  margin-bottom: 2rem;
  padding-bottom: 2rem;
  border-bottom: 1px solid var(--border-light);
}

.form-section:last-of-type {
  border-bottom: none;
  margin-bottom: 1rem;
  padding-bottom: 0;
}

.form-section h3 {
  margin-bottom: 1.5rem;
  color: var(--text-primary);
  font-size: 1.1rem;
  font-weight: 600;
  padding-left: 0.5rem;
  border-left: 3px solid var(--primary-color);
}

.no-rooms {
  text-align: center;
  padding: 3rem;
  background-color: var(--bg-white);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  color: var(--text-secondary);
  font-size: 1.1rem;
  box-shadow: var(--shadow-sm);
}

.room-types {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1rem;
}

.room-type-card {
  display: flex;
  background-color: var(--bg-white);
  border: 2px solid var(--border-light);
  border-radius: var(--radius-md);
  padding: 1rem;
  cursor: pointer;
  transition: all var(--transition);
}

.room-type-card:hover {
  border-color: var(--primary-light);
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.room-type-card.active {
  border-color: var(--primary-color);
  background-color: var(--status-info-bg);
}

.room-type-image {
  width: 120px;
  height: 80px;
  flex-shrink: 0;
  margin-right: 1rem;
}

.room-type-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: var(--radius-sm);
}

.room-type-info {
  flex: 1;
}

.room-type-info h4 {
  margin: 0 0 0.5rem 0;
  color: var(--text-primary);
}

.room-type-info .price {
  font-size: 1.2rem;
  font-weight: 700;
  color: var(--accent-color);
  margin: 0 0 0.3rem 0;
}

.room-type-info .capacity {
  color: var(--text-secondary);
  margin: 0 0 0.3rem 0;
  font-size: 0.9rem;
}

.room-type-info .facilities {
  color: var(--text-light);
  margin: 0;
  font-size: 0.85rem;
}

.price-preview {
  margin-top: 0.8rem;
  padding-top: 0.8rem;
  border-top: 1px solid var(--border-light);
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.preview-label {
  margin: 0;
  font-size: 0.9rem;
  color: var(--text-secondary);
}

.preview-price {
  margin: 0;
  font-size: 1.2rem;
  font-weight: 700;
  color: var(--accent-color);
}

.date-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
  align-items: start;
}

.form-group input,
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
.form-group select:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(35, 133, 187, 0.12);
}

.error-message {
  color: var(--status-danger);
  font-size: 0.85rem;
  margin-top: 0.5rem;
}

.no-rooms-warning {
  padding: 1.5rem;
  background-color: var(--status-warning-bg);
  border: 1px solid var(--status-warning);
  border-radius: var(--radius-md);
  text-align: center;
  color: var(--status-warning);
  font-size: 1.05rem;
}

.earliest-checkout {
  margin-top: 1rem;
  padding: 1rem;
  background-color: var(--status-info-bg);
  border: 1px solid var(--primary-light);
  border-radius: var(--radius-md);
}

.earliest-checkout p {
  margin: 0;
  color: var(--primary-dark);
  font-size: 0.95rem;
}

.available-rooms-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.8rem;
}

.room-item {
  padding: 0.7rem 1.4rem;
  background-color: var(--bg-white);
  border: 2px solid var(--border-color);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all var(--transition);
  font-size: 0.95rem;
  font-weight: 500;
}

.room-item:hover {
  border-color: var(--primary-color);
  background-color: var(--status-info-bg);
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.room-item.active {
  border-color: var(--primary-color);
  background-color: var(--primary-color);
  color: var(--text-white);
}

.price-section {
  background-color: var(--bg-white);
  padding: 1.5rem;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-light);
}

.price-summary {
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
}

.price-item {
  display: flex;
  justify-content: space-between;
  font-size: 0.95rem;
  color: var(--text-secondary);
}

.price-item.total {
  font-size: 1.3rem;
  font-weight: 700;
  color: var(--text-primary);
  padding-top: 0.8rem;
  border-top: 2px solid var(--border-light);
}

.price-item.total span:last-child {
  color: var(--accent-color);
}

.payment-info {
  margin-bottom: 1.5rem;
  padding: 1rem;
  background-color: var(--bg-light);
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-light);
}

.payment-amount {
  font-size: 1.4rem;
  font-weight: 700;
  color: var(--accent-color);
  margin: 0 0 0.5rem 0;
}

.payment-details {
  color: var(--text-secondary);
  margin: 0;
  font-size: 0.9rem;
}

.payment-methods {
  margin-bottom: 1.5rem;
}

.payment-methods h4 {
  margin-top: 0;
  margin-bottom: 1rem;
  color: var(--text-secondary);
  font-size: 0.95rem;
}

.payment-option {
  margin-bottom: 0.8rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.renew-info {
  background-color: var(--status-info-bg);
  padding: 1.5rem;
  border-radius: var(--radius-md);
  border: 1px solid var(--primary-light);
}

.renew-info p {
  margin: 0.5rem 0;
  color: var(--primary-dark);
  font-size: 1rem;
}

.renew-info p strong {
  color: var(--primary-color);
}

.duration-section-center {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 1rem 0;
}

.form-group-center {
  width: 100%;
  max-width: 400px;
}

.form-group-center label {
  text-align: center;
  margin-bottom: 0.8rem;
  display: block;
  color: var(--text-primary);
  font-weight: 500;
}

.form-group-center select {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 1.5px solid var(--border-color);
  border-radius: var(--radius-sm);
  font-size: 0.95rem;
  transition: all var(--transition);
  background: var(--bg-white);
  color: var(--text-primary);
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='%23909399' d='M6 8L1 3h10z'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 12px center;
  padding-right: 2rem;
  cursor: pointer;
}

.form-group-center select:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(35, 133, 187, 0.12);
}

@media (max-width: 768px) {
  .date-section {
    grid-template-columns: 1fr;
  }
  .form-group-center {
    max-width: 100%;
  }
}

/* ======= 未登录提示横幅 ======= */
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
</style>
