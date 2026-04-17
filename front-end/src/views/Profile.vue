<template>
  <div class="profile">
    <h2>个人中心</h2>
    <div class="profile-tabs">
      <button @click="activeTab = 'personalInfo'" :class="{ active: activeTab === 'personalInfo' }">个人信息</button>
      <button @click="activeTab = 'orderHistory'" :class="{ active: activeTab === 'orderHistory' }">我的订单</button>
    </div>

    <!-- 个人信息 -->
    <div v-if="activeTab === 'personalInfo'" class="tab-content">
      <div class="info-header">
        <h3>个人信息</h3>
        <div class="header-actions">
          <button v-if="!isEditing" @click="showChangePassword = true" class="btn btn-change-password">修改密码</button>
          <button v-if="!isEditing" @click="startEditing" class="btn btn-edit">编辑信息</button>
        </div>
      </div>
      <div class="info-form">
        <div class="avatar-section">
          <div class="avatar-display">
            <img v-if="isEditing ? editForm.avatar : userInfo.avatar" :src="isEditing ? editForm.avatar : userInfo.avatar" alt="头像" class="avatar-img">
            <div v-else class="avatar-placeholder">
              <span>{{ userInfo.name ? userInfo.name.charAt(0) : 'U' }}</span>
            </div>
          </div>
          <div v-if="isEditing" class="avatar-upload">
            <input type="file" accept="image/*" @change="handleAvatarUpload" class="file-input" ref="avatarInput">
            <button type="button" @click="triggerAvatarUpload" class="btn btn-upload">上传头像</button>
            <button v-if="editForm.avatar" type="button" @click="removeAvatar" class="btn btn-remove">删除头像</button>
          </div>
        </div>
        <div class="form-group">
          <label>用户名</label>
          <p>{{ userInfo.username }}</p>
        </div>
        <div class="form-group">
          <label>姓名</label>
          <input v-if="isEditing" v-model="editForm.name" type="text" class="form-input">
          <p v-else>{{ userInfo.name }}</p>
        </div>
        <div class="form-group">
          <label>电话</label>
          <input v-if="isEditing" v-model="editForm.phone" type="text" class="form-input">
          <p v-else>{{ userInfo.phone }}</p>
        </div>
        <div class="form-group">
          <label>邮箱</label>
          <input v-if="isEditing" v-model="editForm.email" type="email" class="form-input">
          <p v-else>{{ userInfo.email }}</p>
        </div>
        <div class="form-group">
          <label>身份证号</label>
          <input v-if="isEditing" v-model="editForm.idCard" type="text" class="form-input">
          <p v-else>{{ userInfo.idCard }}</p>
        </div>
        <div v-if="isEditing" class="edit-actions">
          <button @click="saveProfile" class="btn btn-save">保存</button>
          <button @click="cancelEditing" class="btn btn-cancel">取消</button>
        </div>
      </div>
    </div>

    <!-- 我的订单 -->
    <div v-if="activeTab === 'orderHistory'" class="tab-content">
      <h3>我的订单</h3>
      <div class="order-tabs">
        <button @click="orderTab = 'pending'" :class="{ active: orderTab === 'pending' }">待支付</button>
        <button @click="orderTab = 'current'" :class="{ active: orderTab === 'current' }">已支付</button>
      </div>
      <!-- 已支付子标签 -->
      <div v-if="orderTab === 'current'" class="sub-order-tabs">
        <button @click="subOrderTab = 'uncheckin'" :class="{ active: subOrderTab === 'uncheckin' }">未入住订单</button>
        <button @click="subOrderTab = 'checkedin'" :class="{ active: subOrderTab === 'checkedin' }">已入住订单</button>
        <button @click="subOrderTab = 'history'" :class="{ active: subOrderTab === 'history' }">历史订单</button>
      </div>
      <!-- 批量删除按钮 -->
      <div v-if="orderTab === 'current' && subOrderTab === 'history' && filteredOrders.length > 0" class="batch-actions">
        <button v-if="showSelectAll" @click="selectAllOrders" class="btn btn-select-all">全选</button>
        <button v-if="showSelectAll" @click="showBatchDeleteModal" class="btn btn-batch-delete" :disabled="selectedOrders.length === 0">批量删除</button>
        <button v-if="showSelectAll" @click="toggleSelectAll" class="btn btn-cancel">取消</button>
        <button v-else @click="toggleSelectAll" class="btn btn-batch-delete">批量删除</button>
      </div>
      <div class="list">
        <div v-for="order in filteredOrders" :key="order.id" class="item">
          <div class="order-content">
            <!-- 历史订单添加复选框 -->
            <div v-if="showSelectAll && orderTab === 'current' && subOrderTab === 'history'" class="checkbox-wrapper">
              <input type="checkbox" :value="order.id" v-model="selectedOrders">
            </div>
            <p>订单号: {{ order.orderNumber }}</p>
            <p>房间号: {{ order.room.roomNumber }}</p>
            <p>房型: {{ order.room.roomType.name }}</p>
            <p>住房日期: {{ formatDate(order.checkInTime) }} 至 {{ formatDate(order.checkOutTime) }}</p>
            <p>住房时间: {{ calculateStayDuration(order.checkInTime, order.checkOutTime) }}</p>
            <p>总价: ¥{{ order.totalPrice }}</p>
            <p>状态: {{ order.status === '待支付' ? '已预定' : order.status }}</p>
            <p v-if="order.status === '待支付'">支付倒计时: <span class="countdown" :class="{ 'countdown-expired': getCountdown(order.createTime) <= 0 }">{{ formatCountdown(getCountdown(order.createTime)) }}</span></p>
            <div class="order-actions" v-if="order.status === '待支付'">
              <button @click="goToPayment(order.id)" class="btn btn-pay">去支付</button>
            </div>
          </div>
          <div v-if="orderTab === 'current' && subOrderTab === 'history'" class="delete-button-wrapper">
            <button @click="showDeleteModal(order.id)" class="btn btn-delete-small">删除订单</button>
          </div>
        </div>
        <div v-if="filteredOrders.length === 0" class="empty">
          <p v-if="orderTab === 'pending'">暂无待支付的订单</p>
          <p v-else-if="orderTab === 'current' && subOrderTab === 'uncheckin'">暂无未入住的订单</p>
          <p v-else-if="orderTab === 'current' && subOrderTab === 'checkedin'">暂无已入住的订单</p>
          <p v-else>暂无历史订单</p>
        </div>
      </div>
    </div>

    <!-- 修改密码弹窗 -->
    <div v-if="showChangePassword" class="modal-overlay" @click.self="showChangePassword = false">
      <div class="modal-content">
        <h3>修改密码</h3>
        <div v-if="errorMessage" class="message error">{{ errorMessage }}</div>
        <div v-if="successMessage" class="message success">{{ successMessage }}</div>
        <div class="form-group">
          <label>当前密码</label>
          <input type="password" v-model="passwordForm.oldPassword" class="form-input" placeholder="请输入当前密码">
        </div>
        <div class="form-group">
          <label>新密码</label>
          <input type="password" v-model="passwordForm.newPassword" class="form-input" placeholder="请输入新密码">
        </div>
        <div class="form-group">
          <label>确认新密码</label>
          <input type="password" v-model="passwordForm.confirmPassword" class="form-input" placeholder="请再次输入新密码">
        </div>
        <div class="modal-actions">
          <button @click="showChangePassword = false" class="btn btn-cancel">取消</button>
          <button @click="changePassword" class="btn btn-save">确定</button>
        </div>
      </div>
    </div>

    <!-- 删除订单确认弹窗 -->
    <div v-if="deleteModalVisible" class="modal-overlay" @click.self="closeDeleteModal">
      <div class="modal-content">
        <h3>确认删除</h3>
        <p>确定要删除这个订单吗？</p>
        <div class="modal-actions">
          <button @click="closeDeleteModal" class="btn btn-cancel">取消</button>
          <button @click="handleConfirmDelete" class="btn btn-delete">确认</button>
        </div>
      </div>
    </div>

    <!-- 批量删除订单确认弹窗 -->
    <div v-if="batchDeleteModalVisible" class="modal-overlay" @click.self="closeBatchDeleteModal">
      <div class="modal-content">
        <h3>确认批量删除</h3>
        <p>确定要删除选中的{{ selectedOrders.length }}个订单吗？</p>
        <div class="modal-actions">
          <button @click="closeBatchDeleteModal" class="btn btn-cancel">取消</button>
          <button @click="handleConfirmBatchDelete" class="btn btn-delete">确认</button>
        </div>
      </div>
    </div>

    <!-- 取消预定确认弹窗 -->
    <div v-if="cancelModalVisible" class="modal-overlay" @click.self="closeCancelModal">
      <div class="modal-content">
        <h3>确认取消预定</h3>
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
  name: 'Profile',
  data() {
    return {
      activeTab: 'personalInfo',
      userInfo: {
        id: null,
        username: '',
        name: '',
        phone: '',
        email: '',
        idCard: ''
      },
      editForm: {
        name: '',
        phone: '',
        email: '',
        idCard: '',
        avatar: ''
      },
      isEditing: false,
      orders: [],
      showChangePassword: false,
      orderTab: 'current',
      subOrderTab: 'uncheckin',
      selectedOrders: [],
      showSelectAll: false,
      passwordForm: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      errorMessage: '',
      successMessage: '',
      countdownTimer: null,
      deleteModalVisible: false,
      currentDeleteOrderId: null,
      batchDeleteModalVisible: false,
      cancelModalVisible: false,
      currentCancelOrderId: null
    }
  },
  computed: {
    filteredOrders() {
      let filtered = []
      if (this.orderTab === 'pending') {
        // 待支付的订单
        filtered = this.orders.filter(order => 
          order.status === '待支付'
        )
      } else if (this.orderTab === 'current') {
        if (this.subOrderTab === 'uncheckin') {
          // 未入住订单：已预订、已支付（不包括已入住、已退房）
          filtered = this.orders.filter(order => 
            (order.status === '已预订' || order.status === '已支付') && order.status !== '已入住' && order.status !== '已退房'
          )
        } else if (this.subOrderTab === 'checkedin') {
          // 已入住订单
          filtered = this.orders.filter(order => 
            order.status === '已入住'
          )
        } else {
          // 历史订单：已完成、已取消、已退房
          filtered = this.orders.filter(order => 
            order.status === '已完成' || order.status === '已取消' || order.status === '已退房'
          )
        }
      }
      // 按创建时间倒序排序，最新的在上面
      return filtered.sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
    }
  },
  mounted() {
    // 页面加载时从localStorage初始化用户信息
    this.initUserInfo()
    this.getOrders()
    const tab = this.$route.query.tab
    if (tab === 'orderHistory') {
      this.activeTab = 'orderHistory'
    }
    this.startCountdownTimer()
  },
  methods: {
    // 初始化用户信息 - 仅在页面加载时调用
    initUserInfo() {
      const user = sessionStorage.getItem('user')
      if (user) {
        try {
          this.userInfo = JSON.parse(user)
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
    startEditing() {
      this.editForm = {
        name: this.userInfo.name,
        phone: this.userInfo.phone,
        email: this.userInfo.email,
        idCard: this.userInfo.idCard,
        avatar: this.userInfo.avatar || ''
      }
      this.isEditing = true
    },
    triggerAvatarUpload() {
      this.$refs.avatarInput.click()
    },
    handleAvatarUpload(event) {
      const file = event.target.files[0]
      if (file) {
        const reader = new FileReader()
        reader.onload = (e) => {
          const img = new Image()
          img.onload = () => {
            const canvas = document.createElement('canvas')
            const maxWidth = 300
            const maxHeight = 300
            let width = img.width
            let height = img.height

            if (width > maxWidth || height > maxHeight) {
              if (width > height) {
                height = Math.round(height * maxWidth / width)
                width = maxWidth
              } else {
                width = Math.round(width * maxHeight / height)
                height = maxHeight
              }
            }

            canvas.width = width
            canvas.height = height
            const ctx = canvas.getContext('2d')
            ctx.drawImage(img, 0, 0, width, height)
            this.editForm.avatar = canvas.toDataURL('image/jpeg', 0.8)
          }
          img.src = e.target.result
        }
        reader.readAsDataURL(file)
      }
    },
    removeAvatar() {
      this.editForm.avatar = ''
    },
    cancelEditing() {
      this.isEditing = false
    },
    async saveProfile() {
      try {
        const response = await axios.put(`/api/user/profile/${this.userInfo.id}`, this.editForm, { withCredentials: true })
        if (response.data) {
          alert('个人信息更新成功')
          this.userInfo = { ...this.userInfo, ...this.editForm }
          sessionStorage.setItem('user', JSON.stringify(this.userInfo))
          this.isEditing = false
        }
      } catch (error) {
        console.error('更新个人信息失败:', error)
        alert('更新个人信息失败，请稍后重试')
      }
    },
    async getOrders() {
      try {
        const response = await axios.get('/api/user/orders', {
          params: {
            page: 0, // 获取全部订单
            size: 1000
          },
          withCredentials: true
        })
        // 使用内存中的用户信息，而不是直接从localStorage读取
        if (this.userInfo && this.userInfo.id) {
          // 处理Page对象，取content中的数据
          const ordersData = response.data.content || response.data
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
          alert('订单取消成功')
          this.closeCancelModal()
          this.getOrders()
        }
      } catch (error) {
        console.error('取消订单失败:', error)
        alert('取消订单失败，请稍后重试')
      }
    },
    async cancelOrder(orderId) {
      // 保留原有方法，以便其他地方调用
      try {
        const response = await axios.put(`/api/user/orders/${orderId}/cancel`, {}, { withCredentials: true })
        if (response.data) {
          alert('订单取消成功')
          this.getOrders()
        }
      } catch (error) {
        console.error('取消订单失败:', error)
        alert('取消订单失败，请稍后重试')
      }
    },
    renewOrder(orderId) {
      this.$router.push(`/booking?renewOrderId=${orderId}`)
    },
    canRenewOrder(order) {
      const now = new Date()
      const checkOutTime = new Date(order.checkOutTime)
      return (order.status === '已预订' || order.status === '已入住' || order.status === '已支付') && now < checkOutTime
    },
    canCheckOutEarly(order) {
      const now = new Date()
      const checkOutTime = new Date(order.checkOutTime)
      return order.status === '已入住' && now < checkOutTime
    },
    async earlyCheckOut(orderId) {
      if (confirm('确定要提前退房吗？')) {
        try {
          const response = await axios.put(`/api/user/orders/${orderId}/status?status=已完成`, {}, { withCredentials: true })
          if (response.data) {
            alert('提前退房成功')
            this.getOrders()
          }
        } catch (error) {
          console.error('提前退房失败:', error)
          alert('提前退房失败，请稍后重试')
        }
      }
    },
    canDeleteOrder(order) {
      return true
    },
    showDeleteModal(orderId) {
      this.currentDeleteOrderId = orderId
      this.deleteModalVisible = true
    },
    closeDeleteModal() {
      this.deleteModalVisible = false
      this.currentDeleteOrderId = null
    },
    async handleConfirmDelete() {
      if (!this.currentDeleteOrderId) return
      
      try {
        const response = await axios.delete(`/api/user/orders/${this.currentDeleteOrderId}`, { withCredentials: true })
        if (response.data && response.data.success) {
          alert('订单删除成功')
          this.closeDeleteModal()
          this.getOrders()
        } else {
          alert(response.data?.message || '订单删除失败，请稍后重试')
        }
      } catch (error) {
        console.error('删除订单失败:', error)
        alert('删除订单失败，请稍后重试')
      }
    },
    async deleteOrder(orderId) {
      // 保留原有方法，以便其他地方调用
      try {
        const response = await axios.delete(`/api/user/orders/${orderId}`, { withCredentials: true })
        if (response.data && response.data.success) {
          alert('订单删除成功')
          this.getOrders()
        } else {
          alert(response.data?.message || '订单删除失败，请稍后重试')
        }
      } catch (error) {
        console.error('删除订单失败:', error)
        alert('删除订单失败，请稍后重试')
      }
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
    formatDate(dateString) {
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-CN')
    },
    calculateStayDuration(checkInTime, checkOutTime) {
      const checkIn = new Date(checkInTime)
      const checkOut = new Date(checkOutTime)
      const diffTime = checkOut - checkIn
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
      return `${diffDays}天`
    },
    selectAllOrders() {
      if (this.selectedOrders.length === this.filteredOrders.length) {
        // 取消全选
        this.selectedOrders = []
      } else {
        // 全选
        this.selectedOrders = this.filteredOrders.map(order => order.id)
      }
    },
    async toggleSelectAll() {
      if (this.showSelectAll) {
        // 点击取消按钮
        this.showSelectAll = false
        this.selectedOrders = []
      } else {
        // 点击批量删除按钮
        this.showSelectAll = true
      }
    },
    showBatchDeleteModal() {
      if (this.selectedOrders.length === 0) return
      this.batchDeleteModalVisible = true
    },
    closeBatchDeleteModal() {
      this.batchDeleteModalVisible = false
    },
    async handleConfirmBatchDelete() {
      if (this.selectedOrders.length === 0) return
      
      try {
        // 批量删除订单，这里假设后端支持批量删除接口
        const response = await axios.delete('/api/user/orders/batch', {
          data: { orderIds: this.selectedOrders },
          withCredentials: true
        })
        
        if (response.data && response.data.success) {
          alert('批量删除成功')
          this.selectedOrders = []
          this.showSelectAll = false
          this.closeBatchDeleteModal()
          this.getOrders()
        } else {
          alert(response.data?.message || '批量删除失败，请稍后重试')
        }
      } catch (error) {
        console.error('批量删除订单失败:', error)
        alert('批量删除失败，请稍后重试')
      }
    },
    async batchDeleteOrders() {
      // 保留原有方法，以便其他地方调用
      if (this.selectedOrders.length === 0) return
      
      try {
        // 批量删除订单，这里假设后端支持批量删除接口
        const response = await axios.delete('/api/user/orders/batch', {
          data: { orderIds: this.selectedOrders },
          withCredentials: true
        })
        
        if (response.data && response.data.success) {
          alert('批量删除成功')
          this.selectedOrders = []
          this.showSelectAll = false
          this.getOrders()
        } else {
          alert(response.data?.message || '批量删除失败，请稍后重试')
        }
      } catch (error) {
        console.error('批量删除订单失败:', error)
        alert('批量删除失败，请稍后重试')
      }
    },
    async changePassword() {
      this.errorMessage = ''
      this.successMessage = ''
      
      if (!this.passwordForm.oldPassword) {
        this.errorMessage = '请输入当前密码'
        return
      }
      if (!this.passwordForm.newPassword) {
        this.errorMessage = '请输入新密码'
        return
      }
      if (this.passwordForm.newPassword === this.passwordForm.oldPassword) {
        this.errorMessage = '新密码不能与当前密码一致'
        return
      }
      if (this.passwordForm.newPassword !== this.passwordForm.confirmPassword) {
        this.errorMessage = '两次输入的新密码不一致'
        return
      }
      if (this.passwordForm.newPassword.length < 6) {
        this.errorMessage = '新密码长度不能少于6位'
        return
      }

      try {
        const response = await axios.put(`/api/user/profile/${this.userInfo.id}/password`, {
          oldPassword: this.passwordForm.oldPassword,
          newPassword: this.passwordForm.newPassword
        }, { withCredentials: true })
        if (response.data) {
          this.successMessage = '密码修改成功'
          setTimeout(() => {
            this.showChangePassword = false
            this.passwordForm = {
              oldPassword: '',
              newPassword: '',
              confirmPassword: ''
            }
            this.successMessage = ''
          }, 1500)
        }
      } catch (error) {
        console.error('修改密码失败:', error)
        this.errorMessage = error.response?.data?.message || '修改密码失败，请稍后重试'
      }
    }
  }
}
</script>

<style scoped>
.profile {
  max-width: 1200px;
  margin: 0 auto;
}

.profile-tabs {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
}

.profile-tabs button {
  padding: 0.8rem 1.5rem;
  background-color: var(--bg-light);
  border: 1px solid var(--border-color);
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: var(--text-primary);
}

.profile-tabs button:hover {
  background-color: var(--border-light);
}

.profile-tabs button.active {
  background-color: var(--primary-color);
  color: var(--text-white);
  border-color: var(--primary-color);
}

.order-tabs {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.order-tabs button {
  padding: 0.6rem 1.2rem;
  background-color: var(--bg-light);
  border: 1px solid var(--border-color);
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: var(--text-primary);
}

.order-tabs button:hover {
  background-color: var(--border-light);
}

.order-tabs button.active {
  background-color: var(--primary-color);
  color: var(--text-white);
  border-color: var(--primary-color);
}

.sub-order-tabs {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.5rem;
  margin-top: 0.5rem;
  padding-left: 1rem;
}

.sub-order-tabs button {
  padding: 0.4rem 0.8rem;
  background-color: var(--bg-light);
  border: 1px solid var(--border-color);
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
  color: var(--text-primary);
}

.sub-order-tabs button:hover {
  background-color: var(--border-light);
}

.sub-order-tabs button.active {
  background-color: var(--primary-color);
  color: var(--text-white);
  border-color: var(--primary-color);
}

.batch-actions {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.btn-select-all {
  background-color: var(--text-light);
  color: var(--text-white);
}

.btn-select-all:hover {
  background-color: var(--text-secondary);
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.btn-batch-delete {
  background-color: var(--status-danger);
  color: var(--text-white);
}

.btn-batch-delete:hover:not(:disabled) {
  background-color: #d94c4c;
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.btn-batch-delete:disabled {
  background-color: var(--text-light);
  cursor: not-allowed;
}

.checkbox-wrapper {
  position: absolute;
  top: 1rem;
  left: 1rem;
}

.item {
  position: relative;
}

.tab-content {
  background-color: var(--bg-white);
  padding: 2rem;
  border-radius: 8px;
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-sm);
}

.info-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.header-actions {
  display: flex;
  gap: 0.8rem;
}

.info-form {
  background-color: var(--bg-white);
  padding: 2rem;
  border-radius: 8px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
}

.avatar-section {
  display: flex;
  gap: 2rem;
  margin-bottom: 2rem;
  padding-bottom: 2rem;
  border-bottom: 1px solid var(--border-light);
}

.avatar-display {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}

.avatar-img {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid var(--border-color);
}

.avatar-placeholder {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background-color: var(--primary-color);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-white);
  font-size: 2.5rem;
  font-weight: bold;
  border: 3px solid var(--border-color);
}

.avatar-upload {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  justify-content: center;
}

.file-input {
  display: none;
}

.btn-upload {
  background-color: var(--primary-color);
  color: var(--text-white);
}

.btn-upload:hover {
  background-color: var(--primary-hover);
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.btn-remove {
  background-color: var(--status-danger);
  color: var(--text-white);
}

.btn-remove:hover {
  background-color: #d94c4c;
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.form-input {
  width: 100%;
  padding: 0.8rem;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  font-size: 1.1rem;
  box-sizing: border-box;
  transition: border-color 0.3s ease;
}

.form-input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(35, 133, 187, 0.2);
}

.edit-actions {
  display: flex;
  gap: 1rem;
  margin-top: 2rem;
}

.btn-edit {
  background-color: var(--primary-color);
  color: var(--text-white);
}

.btn-edit:hover {
  background-color: var(--primary-hover);
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.btn-change-password {
  background-color: var(--status-warning);
  color: var(--text-white);
}

.btn-change-password:hover {
  background-color: #cf8a2e;
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.btn-save {
  background-color: var(--status-success);
  color: var(--text-white);
}

.btn-save:hover {
  background-color: #5aaf2f;
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: bold;
  color: var(--text-secondary);
}

.form-group p {
  margin: 0;
  color: var(--text-primary);
  font-size: 1.1rem;
}

.list {
  background-color: var(--bg-white);
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
}

.item {
  padding: 1.5rem;
  border-bottom: 1px solid var(--border-light);
  margin-bottom: 1.5rem;
  display: flex;
  flex-direction: column;
}

.item:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.order-content {
  flex: 1;
}

.item p {
  margin-bottom: 0.5rem;
  color: var(--text-secondary);
}

.order-actions {
  display: flex;
  gap: 1rem;
  margin-top: 1rem;
}

.delete-button-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 1rem;
}

.btn {
  padding: 0.6rem 1.2rem;
  background-color: var(--primary-color);
  color: var(--text-white);
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn:hover {
  background-color: var(--primary-hover);
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.btn-cancel {
  background-color: var(--text-light);
  color: var(--text-white);
}

.btn-cancel:hover {
  background-color: var(--text-secondary);
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.btn-renew {
  background-color: var(--primary-color);
  color: var(--text-white);
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

.btn-pay {
  background-color: var(--status-warning);
  color: var(--text-white);
}

.btn-pay:hover {
  background-color: #cf8a2e;
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.btn-delete {
  background-color: var(--status-danger);
  color: var(--text-white);
}

.btn-delete:hover {
  background-color: #d94c4c;
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.btn-confirm {
  background-color: var(--primary-color);
  color: var(--text-white);
}

.btn-confirm:hover {
  background-color: var(--primary-hover);
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.btn-delete-small {
  padding: 0.4rem 0.8rem;
  background-color: var(--status-danger);
  color: var(--text-white);
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
}

.btn-delete-small:hover {
  background-color: #d94c4c;
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.empty {
  text-align: center;
  padding: 3rem;
  color: var(--text-light);
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
  max-width: 500px;
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--border-color);
}

.modal-content h3 {
  margin-top: 0;
  margin-bottom: 1.5rem;
  color: var(--text-primary);
}

.modal-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  margin-top: 1.5rem;
}

.message {
  padding: 0.8rem 1rem;
  border-radius: 4px;
  margin-bottom: 1rem;
  font-size: 0.95rem;
}

.message.error {
  background-color: #ffe9e9;
  color: #a73333;
  border: 1px solid #f5c6cb;
}

.message.success {
  background-color: #eaf7ea;
  color: #2d7a2d;
  border: 1px solid #c3e6cb;
}
</style>
