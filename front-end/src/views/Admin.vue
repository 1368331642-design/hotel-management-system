<template>
  <div class="admin">
    <h2>管理中心</h2>
    
    <div class="admin-cards">
      <div class="admin-card" @click="activeTab = 'systemConfig'" :class="{ active: activeTab === 'systemConfig' }">
        <div class="card-icon">⚙️</div>
        <h3>系统配置</h3>
        <p>酒店基础信息、客房资源管理</p>
      </div>
      <div class="admin-card" @click="activeTab = 'dataManage'" :class="{ active: activeTab === 'dataManage' }">
        <div class="card-icon">📊</div>
        <h3>数据管理</h3>
        <p>预订数据统计、服务日志管理</p>
      </div>
    </div>

    <!-- 系统配置 -->
    <div v-if="activeTab === 'systemConfig'" class="tab-content">
      <h3>系统配置</h3>
      
      <div class="config-section">
        <h4>酒店信息</h4>
        <div class="add-form">
          <form @submit.prevent="saveHotelInfo">
            <div class="form-row">
              <div class="form-group">
                <label for="hotelName">酒店名称</label>
                <input type="text" id="hotelName" v-model="hotelInfoForm.name" required>
              </div>
              <div class="form-group">
                <label for="hotelAddress">地址</label>
                <input type="text" id="hotelAddress" v-model="hotelInfoForm.address" required>
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label for="hotelPhone">联系电话</label>
                <input type="text" id="hotelPhone" v-model="hotelInfoForm.phone" required>
              </div>
              <div class="form-group">
                <label for="hotelEmail">邮箱</label>
                <input type="email" id="hotelEmail" v-model="hotelInfoForm.email" required>
              </div>
            </div>
            <div class="form-group">
              <label for="hotelDescription">描述</label>
              <textarea id="hotelDescription" v-model="hotelInfoForm.description" rows="2"></textarea>
            </div>
            <button type="submit" class="btn">保存</button>
          </form>
        </div>
      </div>

      <div class="config-section">
        <h4>房型管理</h4>
        <div class="add-form">
          <form @submit.prevent="addRoomType">
            <div class="form-row">
              <div class="form-group">
                <label for="name">房型名称</label>
                <input type="text" id="name" v-model="roomTypeForm.name" required>
              </div>
              <div class="form-group">
                <label for="price">价格</label>
                <input type="number" id="price" v-model="roomTypeForm.price" min="1" step="0.01" required>
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label for="capacity">容量</label>
                <input type="number" id="capacity" v-model="roomTypeForm.capacity" min="1" required>
              </div>
              <div class="form-group">
                <label for="facilities">设施</label>
                <input type="text" id="facilities" v-model="roomTypeForm.facilities" required>
              </div>
            </div>
            <div class="form-group">
              <label for="description">描述</label>
              <textarea id="description" v-model="roomTypeForm.description" rows="2" required></textarea>
            </div>
            <div class="form-group">
              <label>房型图片</label>
              <input type="file" accept="image/*" @change="handleImageUpload" class="file-input">
              <div v-if="roomTypeForm.image" class="image-preview">
                <img :src="roomTypeForm.image" alt="预览" class="preview-img">
                <button type="button" @click="removeImage" class="btn btn-small btn-delete" style="margin-top: 10px;">删除图片</button>
              </div>
            </div>
            <button type="submit" class="btn">添加房型</button>
          </form>
        </div>
        <div class="list">
          <h5>已有房型</h5>
          <div v-for="roomType in roomTypes" :key="roomType.id" class="item room-type-item">
            <div v-if="roomType.image" class="room-type-image">
              <img :src="roomType.image" :alt="roomType.name" class="room-type-img">
            </div>
            <div class="room-type-info">
              <p>名称: {{ roomType.name }}</p>
              <p>价格: ¥{{ roomType.price }}/晚</p>
              <p>容量: {{ roomType.capacity }}人</p>
              <p v-if="roomType.facilities">设施: {{ roomType.facilities }}</p>
            </div>
            <button @click="deleteRoomType(roomType.id)" class="btn btn-small btn-delete">删除</button>
          </div>
        </div>
      </div>

      <div class="config-section">
        <h4>账户管理</h4>
        <div class="add-form">
          <form @submit.prevent="addUser">
            <div class="form-row">
              <div class="form-group">
                <label for="userUsername">用户名</label>
                <input type="text" id="userUsername" v-model="userForm.username" required>
              </div>
              <div class="form-group">
                <label for="userPassword">密码</label>
                <input type="text" id="userPassword" v-model="userForm.password" required>
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label for="userName">姓名</label>
                <input type="text" id="userName" v-model="userForm.name" required>
              </div>
              <div class="form-group">
                <label for="userRole">角色</label>
                <select id="userRole" v-model="userForm.role" required>
                  <option value="USER">普通用户</option>
                  <option value="FRONT">前台</option>
                </select>
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label for="userPhone">电话</label>
                <input type="text" id="userPhone" v-model="userForm.phone">
              </div>
              <div class="form-group">
                <label for="userEmail">邮箱</label>
                <input type="email" id="userEmail" v-model="userForm.email">
              </div>
            </div>
            <div class="form-group">
              <label for="userIdCard">身份证号</label>
              <input type="text" id="userIdCard" v-model="userForm.idCard">
            </div>
            <button type="submit" class="btn">添加账户</button>
          </form>
        </div>
        <div v-if="users.length > 0" class="list">
          <h5>账户列表</h5>
          <div v-for="user in users" :key="user.id" class="item user-item">
            <div class="user-info">
              <p>用户名: {{ user.username }}</p>
              <p>姓名: {{ user.name }}</p>
              <p v-if="user.phone">电话: {{ user.phone }}</p>
              <p v-if="user.email">邮箱: {{ user.email }}</p>
              <p>角色: {{ getRoleName(user.roles) }}</p>
            </div>
            <button @click="showDeleteModal(user.id)" class="btn btn-small btn-delete" :disabled="user.username === 'admin'">删除</button>
          </div>
          <div v-if="usersTotalPages > 1" class="pagination">
            <button @click="goToUserPrevPage" :disabled="!userHasPrevPage" class="page-btn">
              上一页
            </button>
            <div class="page-numbers">
              <button 
                v-for="page in userPageNumbers" 
                :key="page"
                @click="goToUserPage(page)"
                :class="['page-number', { active: userCurrentPage === page }]"
              >
                {{ page }}
              </button>
            </div>
            <button @click="goToUserNextPage" :disabled="!userHasNextPage" class="page-btn">
              下一页
            </button>
          </div>
          <div v-if="usersTotalPages > 1" class="pagination-info">
            共 {{ usersTotalElements }} 条，第 {{ userCurrentPage }} / {{ usersTotalPages }} 页
          </div>
        </div>
      </div>

      <div class="config-section">
        <h4>房间管理</h4>
        <div class="add-form">
          <form @submit.prevent="addRoom">
            <div class="form-row">
              <div class="form-group">
                <label for="roomNumber">房间号</label>
                <input type="text" id="roomNumber" v-model="roomForm.roomNumber" required>
              </div>
              <div class="form-group">
                <label for="roomType">房型</label>
                <select id="roomType" v-model="roomForm.roomTypeId" required>
                  <option v-for="roomType in roomTypes" :key="roomType.id" :value="roomType.id">{{ roomType.name }}</option>
                </select>
              </div>
              <div class="form-group">
                <label for="status">状态</label>
                <select id="status" v-model="roomForm.status" required>
                  <option value="空房">空房</option>
                  <option value="已预订">已预订</option>
                  <option value="已入住">已入住</option>
                  <option value="维护中">维护中</option>
                </select>
              </div>
            </div>
            <button type="submit" class="btn">添加房间</button>
          </form>
        </div>
        <div v-if="rooms.length > 0" class="list">
          <h5>房间列表</h5>
          <div v-for="room in rooms" :key="room.id" class="item room-item">
            <div class="room-info">
              <p>房间号: {{ room.roomNumber }}</p>
              <p>房型: {{ room.roomType?.name }}</p>
            </div>
            <div class="room-status-edit">
              <select v-model="room.status" class="status-select">
                <option value="空房">空房</option>
                <option value="已预订">已预订</option>
                <option value="已入住">已入住</option>
                <option value="维护中">维护中</option>
              </select>
              <button @click="updateRoomStatus(room.id, room.status)" class="btn btn-small">更新状态</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 数据管理 -->
    <div v-if="activeTab === 'dataManage'" class="tab-content">
      <h3>数据管理</h3>

      <div class="config-section">
        <h4>数据统计</h4>
        <div class="statistics">
          <div class="stat-card">
            <h5>总订单</h5>
            <p class="stat-number">{{ statistics.totalOrders }}</p>
          </div>
          <div class="stat-card">
            <h5>已预订</h5>
            <p class="stat-number">{{ statistics.totalBookings }}</p>
          </div>
          <div class="stat-card">
            <h5>已取消</h5>
            <p class="stat-number">{{ statistics.totalCancellations }}</p>
          </div>
          <div class="stat-card">
            <h5>已入住</h5>
            <p class="stat-number">{{ statistics.totalCheckIn }}</p>
          </div>
          <div class="stat-card">
            <h5>已完成</h5>
            <p class="stat-number">{{ statistics.totalCompleted }}</p>
          </div>
        </div>
        <div class="chart-container">
          <div ref="chartRef" style="width: 100%; height: 400px;"></div>
        </div>
      </div>

      <div class="config-section">
        <h4>订单统计</h4>
        <div class="order-tabs">
          <button @click="orderStatusTab = 'all'" :class="{ active: orderStatusTab === 'all' }">总订单</button>
          <button @click="orderStatusTab = '已预订'" :class="{ active: orderStatusTab === '已预订' }">已预订</button>
          <button @click="orderStatusTab = '已取消'" :class="{ active: orderStatusTab === '已取消' }">已取消</button>
          <button @click="orderStatusTab = '已入住'" :class="{ active: orderStatusTab === '已入住' }">已入住</button>
          <button @click="orderStatusTab = '已完成'" :class="{ active: orderStatusTab === '已完成' }">已完成</button>
        </div>
        <div class="order-chart-container">
          <div ref="orderChartRef" style="width: 100%; height: 350px;"></div>
        </div>
        <div class="list">
          <div v-if="filteredOrders.length === 0" class="empty">
            <p>暂无订单</p>
          </div>
          <div v-else>
            <div v-for="order in paginatedOrders" :key="order.id" class="item order-item">
              <div class="order-info">
                <p>订单号: {{ order.orderNumber }}</p>
                <p>房间号: {{ order.room?.roomNumber }}</p>
                <p>用户账号: {{ order.user?.username }}</p>
                <p>入住: {{ formatDate(order.checkInTime) }}</p>
                <p>退房: {{ formatDate(order.checkOutTime) }}</p>
              </div>
              <div class="order-status">
                <p>状态: {{ order.status }}</p>
              </div>
            </div>
            
            <div v-if="orderTotalPages > 1" class="pagination">
              <button @click="goToOrderPrevPage" :disabled="!orderHasPrevPage" class="page-btn">
                上一页
              </button>
              <div class="page-numbers">
                <button 
                  v-for="page in orderPageNumbers" 
                  :key="page"
                  @click="goToOrderPage(page)"
                  :class="['page-number', { active: orderCurrentPage === page }]"
                >
                  {{ page }}
                </button>
              </div>
              <button @click="goToOrderNextPage" :disabled="!orderHasNextPage" class="page-btn">
                下一页
              </button>
            </div>
            <div v-if="orderTotalPages > 1" class="pagination-info">
              共 {{ ordersTotalElements }} 条，第 {{ orderCurrentPage }} / {{ orderTotalPages }} 页
            </div>
          </div>
        </div>
      </div>

      <div class="config-section">
        <h4>服务日志</h4>
        
        <div v-if="loadingServiceLogs" class="loading">
          <p>加载中...</p>
        </div>
        
        <div v-else>
          <div class="list">
            <h5>待处理</h5>
            <div v-if="pendingServiceLogs.length === 0" class="empty">
              <p>暂无待处理的服务</p>
            </div>
            <div v-else>
              <div v-for="log in paginatedPendingLogs" :key="log.id" class="item service-log-item">
                <div class="log-header">
                  <h6>{{ log.type }}</h6>
                  <span :class="['status-badge', 'status-pending']">{{ log.status }}</span>
                </div>
                <p>用户: {{ log.user?.name || log.user?.username }}</p>
                <p>内容: {{ log.content }}</p>
                <p>时间: {{ formatDateTime(log.createTime) }}</p>
              </div>
              
              <div v-if="pendingTotalPages > 1" class="pagination">
                <button @click="goToPendingPrevPage" :disabled="!pendingHasPrevPage" class="page-btn">
                  上一页
                </button>
                <div class="page-numbers">
                  <button 
                    v-for="page in pendingPageNumbers" 
                    :key="page"
                    @click="goToPendingPage(page)"
                    :class="['page-number', { active: pendingCurrentPage === page }]"
                  >
                    {{ page }}
                  </button>
                </div>
                <button @click="goToPendingNextPage" :disabled="!pendingHasNextPage" class="page-btn">
                  下一页
                </button>
              </div>
              <div v-if="pendingTotalPages > 1" class="pagination-info">
                共 {{ pendingServiceLogs.length }} 条，第 {{ pendingCurrentPage }} / {{ pendingTotalPages }} 页
              </div>
            </div>
          </div>
          
          <div class="list" style="margin-top: 2rem;">
            <h5>已处理</h5>
            <div v-if="processedServiceLogs.length === 0" class="empty">
              <p>暂无已处理的服务</p>
            </div>
            <div v-else>
              <div v-for="log in paginatedProcessedLogs" :key="log.id" class="item service-log-item">
                <div class="log-header">
                  <h6>{{ log.type }}</h6>
                  <span :class="['status-badge', 'status-processed']">{{ log.status }}</span>
                </div>
                <p>用户: {{ log.user?.name || log.user?.username }}</p>
                <p>内容: {{ log.content }}</p>
                <p>时间: {{ formatDateTime(log.createTime) }}</p>
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
    </div>

    <!-- 确认删除弹窗 -->
    <div v-if="deleteModalVisible" class="modal-overlay">
      <div class="modal-content">
        <h3>确认删除</h3>
        <p>确定要删除这个账户吗？</p>
        <div class="modal-actions">
          <button @click="closeDeleteModal" class="btn">取消</button>
          <button @click="handleConfirmDelete" class="btn btn-confirm">确认</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import * as echarts from 'echarts'

export default {
  name: 'Admin',
  data() {
    return {
      activeTab: 'systemConfig',
      roomTypes: [],
      rooms: [],
      showRoomList: false,
      orders: [], // 用于显示的分页订单
      allOrders: [], // 用于图表统计的全部订单
      ordersTotalElements: 0,
      ordersTotalPages: 0,
      serviceLogs: [],
      serviceLogsTotalElements: 0,
      serviceLogsTotalPages: 0,
      loadingServiceLogs: false,
      currentPage: 1,
      pageSize: 5,
      pendingCurrentPage: 1,
      hotelInfoForm: {
        name: '',
        address: '',
        phone: '',
        email: '',
        description: ''
      },
      roomTypeForm: {
        name: '',
        description: '',
        price: '',
        capacity: '',
        facilities: '',
        image: ''
      },
      roomForm: {
        roomNumber: '',
        roomTypeId: '',
        status: '空房'
      },
      statistics: {
        totalOrders: 0,
        totalBookings: 0,
        totalCancellations: 0,
        totalCheckIn: 0,
        totalCompleted: 0
      },
      orderStatusTab: 'all',
      orderCurrentPage: 1,
      orderPageSize: 5,
      refreshInterval: null,
      chart: null,
      orderChart: null,
      // 账户管理相关
      users: [],
      usersTotalElements: 0,
      usersTotalPages: 0,
      userCurrentPage: 1,
      userPageSize: 5,
      roles: [],
      userForm: {
        username: '',
        password: '',
        name: '',
        phone: '',
        email: '',
        idCard: '',
        role: 'USER'
      },
      // 删除确认模态框
      deleteModalVisible: false,
      currentDeleteUserId: null
    }
  },
  computed: {
    pendingServiceLogs() {
      return this.serviceLogs
        .filter(log => log.status === '待处理')
        .sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
    },
    processedServiceLogs() {
      return this.serviceLogs
        .filter(log => log.status === '已处理')
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
    },
    paginatedPendingLogs() {
      const start = (this.pendingCurrentPage - 1) * this.pageSize
      const end = start + this.pageSize
      return this.pendingServiceLogs.slice(start, end)
    },
    pendingTotalPages() {
      return Math.ceil(this.pendingServiceLogs.length / this.pageSize)
    },
    pendingHasPrevPage() {
      return this.pendingCurrentPage > 1
    },
    pendingHasNextPage() {
      return this.pendingCurrentPage < this.pendingTotalPages
    },
    pendingPageNumbers() {
      const pages = []
      for (let i = 1; i <= this.pendingTotalPages; i++) {
        pages.push(i)
      }
      return pages
    },
    filteredOrders() {
      let result
      if (this.orderStatusTab === 'all') {
        result = this.allOrders
      } else {
        result = this.allOrders.filter(order => order.status === this.orderStatusTab)
      }
      return result.sort((a, b) => b.id - a.id)
    },
    paginatedOrders() {
      // 直接使用后端返回的分页数据
      return this.orders
    },
    orderTotalPages() {
      return this.ordersTotalPages
    },
    orderHasPrevPage() {
      return this.orderCurrentPage > 1
    },
    orderHasNextPage() {
      return this.orderCurrentPage < this.orderTotalPages
    },
    orderPageNumbers() {
      const pages = []
      for (let i = 1; i <= this.orderTotalPages; i++) {
        pages.push(i)
      }
      return pages
    },
    userTotalPages() {
      return this.usersTotalPages
    },
    userHasPrevPage() {
      return this.userCurrentPage > 1
    },
    userHasNextPage() {
      return this.userCurrentPage < this.userTotalPages
    },
    userPageNumbers() {
      const pages = []
      for (let i = 1; i <= this.userTotalPages; i++) {
        pages.push(i)
      }
      return pages
    }
  },
  watch: {
    orderStatusTab() {
      this.orderCurrentPage = 1
      this.$nextTick(() => {
        this.updateOrderChart()
      })
    },
    activeTab(newVal) {
      if (newVal === 'dataManage') {
        this.$nextTick(() => {
          this.initChart()
          this.initOrderChart()
        })
      }
    }
  },
  mounted() {
    this.getHotelInfo()
    this.getRoomTypes()
    this.getRooms()
    this.getOrders()
    this.getServiceLogs()
    this.getStatistics()
    this.getUsers()
    this.getRoles()
    this.startAutoRefresh()
    this.$nextTick(() => {
      this.initChart()
      this.initOrderChart()
    })
  },
  beforeDestroy() {
    if (this.refreshInterval) {
      clearInterval(this.refreshInterval)
    }
    if (this.chart) {
      this.chart.dispose()
    }
    if (this.orderChart) {
      this.orderChart.dispose()
    }
    window.removeEventListener('resize', this.handleResize)
  },
  methods: {
    goToOrderPage(page) {
      this.orderCurrentPage = page
      this.getOrders()
    },
    goToOrderPrevPage() {
      if (this.orderHasPrevPage) {
        this.orderCurrentPage--
        this.getOrders()
      }
    },
    goToOrderNextPage() {
      if (this.orderHasNextPage) {
        this.orderCurrentPage++
        this.getOrders()
      }
    },
    startAutoRefresh() {
      this.refreshInterval = setInterval(() => {
        this.getRooms()
        this.getOrders()
        this.getStatistics()
      }, 5000)
    },
    async getHotelInfo() {
      try {
        const response = await axios.get('/api/admin/hotel-info', { withCredentials: true })
        if (response.data) {
          this.hotelInfoForm = response.data
        }
      } catch (error) {
        console.error('获取酒店信息失败:', error)
      }
    },
    async saveHotelInfo() {
      try {
        const response = await axios.post('/api/admin/hotel-info', this.hotelInfoForm, { withCredentials: true })
        if (response.data) {
          alert('酒店信息保存成功')
        }
      } catch (error) {
        console.error('保存酒店信息失败:', error)
        alert('保存酒店信息失败，请稍后重试')
      }
    },
    async getServiceLogs() {
      this.loadingServiceLogs = true
      console.log('开始获取服务日志...')
      try {
        const response = await axios.get('/api/admin/service-logs', {
          params: {
            page: 0, // 获取全部服务日志用于前端分类（待处理/已处理）
            size: 1000 // 足够大的数量
          },
          withCredentials: true
        })
        console.log('服务日志响应:', response.data)
        this.serviceLogs = response.data.content
        this.serviceLogsTotalElements = response.data.totalElements
        this.serviceLogsTotalPages = response.data.totalPages
      } catch (error) {
        console.error('获取服务日志失败:', error)
        alert('获取服务日志失败: ' + error.message)
      } finally {
        this.loadingServiceLogs = false
      }
    },
    async updateServiceLogStatus(logId, status) {
      try {
        await axios.put(`/api/admin/service-logs/${logId}/status`, { status }, { withCredentials: true })
        alert('服务状态更新成功')
        this.getServiceLogs()
      } catch (error) {
        console.error('更新服务状态失败:', error)
        alert('更新服务状态失败，请稍后重试')
      }
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
    },
    goToPendingPage(page) {
      this.pendingCurrentPage = page
    },
    goToPendingPrevPage() {
      if (this.pendingHasPrevPage) {
        this.pendingCurrentPage--
      }
    },
    goToPendingNextPage() {
      if (this.pendingHasNextPage) {
        this.pendingCurrentPage++
      }
    },
    async getStatistics() {
      try {
        const response = await axios.get('/api/admin/statistics', { withCredentials: true })
        console.log('统计数据:', response.data)
        this.statistics = response.data
        this.$nextTick(() => {
          this.updateChart()
        })
      } catch (error) {
        console.error('获取统计数据失败:', error)
      }
    },
    initChart() {
      const chartDom = this.$refs.chartRef
      if (!chartDom) return
      
      if (this.chart) {
        this.chart.dispose()
        this.chart = null
      }
      
      this.chart = echarts.init(chartDom)
      window.addEventListener('resize', this.handleResize)
      this.updateChart()
    },
    updateChart() {
      if (!this.chart) return
      
      const option = {
        animation: false,
        animationDuration: 0,
        animationDurationUpdate: 0,
        title: {
          text: '订单状态分布',
          left: 'center'
        },
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left'
        },
        series: [
          {
            name: '订单状态',
            type: 'pie',
            radius: ['40%', '70%'],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 10,
              borderColor: '#fff',
              borderWidth: 2
            },
            label: {
              show: false,
              position: 'center'
            },
            emphasis: {
              label: {
                show: true,
                fontSize: 20,
                fontWeight: 'bold'
              }
            },
            labelLine: {
              show: false
            },
            data: [
              { value: this.statistics.totalBookings, name: '已预订', itemStyle: { color: '#2385bb' } },
              { value: this.statistics.totalCancellations, name: '已取消', itemStyle: { color: '#f56c6c' } },
              { value: this.statistics.totalCheckIn, name: '已入住', itemStyle: { color: '#e6a23c' } },
              { value: this.statistics.totalCompleted, name: '已完成', itemStyle: { color: '#67c23a' } }
            ]
          }
        ]
      }
      
      this.chart.setOption(option, true)
    },
    initOrderChart() {
      const chartDom = this.$refs.orderChartRef
      if (!chartDom) return
      
      if (this.orderChart) {
        this.orderChart.dispose()
        this.orderChart = null
      }
      
      this.orderChart = echarts.init(chartDom)
      this.updateOrderChart()
    },
    getLast7DaysData() {
      const today = new Date()
      const days = []
      const data = []
      
      for (let i = 6; i >= 0; i--) {
        const date = new Date(today)
        date.setDate(today.getDate() - i)
        const dateStr = `${date.getMonth() + 1}/${date.getDate()}`
        days.push(dateStr)
        
        let count = 0
        const filteredOrders = this.orderStatusTab === 'all' 
          ? this.allOrders 
          : this.allOrders.filter(order => order.status === this.orderStatusTab)
        
        for (const order of filteredOrders) {
          const orderDate = new Date(order.createTime || order.checkInTime)
          if (orderDate.toDateString() === date.toDateString()) {
            count++
          }
        }
        data.push(count)
      }
      
      return { days, data }
    },
    updateOrderChart() {
      if (!this.orderChart) return
      
      const { days, data } = this.getLast7DaysData()
      
      const chartTitle = this.orderStatusTab === 'all' ? '近7天总订单统计' : `近7天${this.orderStatusTab}统计`
      
      let barColor = '#2385bb'
      if (this.orderStatusTab === '已取消') {
        barColor = '#f56c6c'
      } else if (this.orderStatusTab === '已入住') {
        barColor = '#e6a23c'
      } else if (this.orderStatusTab === '已完成') {
        barColor = '#67c23a'
      } else if (this.orderStatusTab === '已预订') {
        barColor = '#2385bb'
      }
      
      const option = {
        animation: false,
        animationDuration: 0,
        animationDurationUpdate: 0,
        title: {
          text: chartTitle,
          left: 'center'
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: days,
          axisLabel: {
            interval: 0
          }
        },
        yAxis: {
          type: 'value',
          minInterval: 1
        },
        series: [
          {
            name: '订单数',
            type: 'bar',
            data: data,
            itemStyle: {
              color: barColor,
              borderRadius: [4, 4, 0, 0]
            },
            barWidth: '50%',
            label: {
              show: true,
              position: 'top'
            }
          }
        ]
      }
      
      this.orderChart.setOption(option, { notMerge: true })
    },
    handleResize() {
      if (this.chart) {
        this.chart.resize()
      }
    },
    async getRoomTypes() {
      try {
        const response = await axios.get('/api/user/room-types', { withCredentials: true })
        this.roomTypes = response.data
      } catch (error) {
        console.error('获取房型失败:', error)
      }
    },
    async getRooms() {
      try {
        const response = await axios.get('/api/user/rooms', {
          params: {
            page: 0, // 获取全部房间
            size: 1000
          },
          withCredentials: true
        })
        // 处理Page对象，取content中的数据
        this.rooms = response.data.content || response.data
      } catch (error) {
        console.error('获取房间失败:', error)
      }
    },
    async getOrders() {
      try {
        // 先获取全部订单用于图表统计
        const allOrdersResponse = await axios.get('/api/user/orders', {
          params: {
            page: 0, // 获取全部订单
            size: 1000
          },
          withCredentials: true
        })
        // 处理Page对象，取content中的数据
        this.allOrders = allOrdersResponse.data.content || allOrdersResponse.data
        
        // 再获取分页订单用于显示
        const response = await axios.get('/api/admin/orders', {
          params: {
            page: this.orderCurrentPage - 1, // 后端从0开始
            size: this.orderPageSize
          },
          withCredentials: true
        })
        console.log('分页订单:', response.data)
        this.orders = response.data.content
        this.ordersTotalElements = response.data.totalElements
        this.ordersTotalPages = response.data.totalPages
        
        this.$nextTick(() => {
          this.updateOrderChart()
        })
      } catch (error) {
        console.error('获取订单失败:', error)
      }
    },
    handleImageUpload(event) {
      const file = event.target.files[0]
      if (file) {
        const reader = new FileReader()
        reader.onload = (e) => {
          const img = new Image()
          img.onload = () => {
            const canvas = document.createElement('canvas')
            const maxWidth = 800
            const maxHeight = 600
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
            this.roomTypeForm.image = canvas.toDataURL('image/jpeg', 0.7)
          }
          img.src = e.target.result
        }
        reader.readAsDataURL(file)
      }
    },
    removeImage() {
      this.roomTypeForm.image = ''
    },
    async addRoomType() {
      try {
        const roomTypeData = {
          name: this.roomTypeForm.name,
          description: this.roomTypeForm.description,
          price: this.roomTypeForm.price ? parseFloat(this.roomTypeForm.price) : 0,
          capacity: this.roomTypeForm.capacity ? parseInt(this.roomTypeForm.capacity) : 1,
          facilities: this.roomTypeForm.facilities,
          image: this.roomTypeForm.image || ''
        }
        console.log('准备添加的房型数据:', roomTypeData)
        const response = await axios.post('/api/user/admin/room-types', roomTypeData, { withCredentials: true })
        if (response.data) {
          alert('房型添加成功')
          this.getRoomTypes()
          this.roomTypeForm = {
            name: '',
            description: '',
            price: '',
            capacity: '',
            facilities: '',
            image: ''
          }
        }
      } catch (error) {
        console.error('添加房型失败:', error)
        console.error('错误详情:', error.response?.data)
        console.error('完整错误:', error)
        alert('添加房型失败: ' + (error.response?.data?.message || error.message || '请稍后重试'))
      }
    },
    async deleteRoomType(roomTypeId) {
      if (confirm('确定要删除这个房型吗？')) {
        try {
          await axios.delete(`/api/user/admin/room-types/${roomTypeId}`, { withCredentials: true })
          alert('房型删除成功')
          this.getRoomTypes()
        } catch (error) {
          console.error('删除房型失败:', error)
          alert('删除房型失败，请稍后重试')
        }
      }
    },
    async addRoom() {
      try {
        const roomData = {
          roomNumber: this.roomForm.roomNumber,
          status: this.roomForm.status,
          roomType: {
            id: parseInt(this.roomForm.roomTypeId)
          }
        }
        const response = await axios.post('/api/user/admin/rooms', roomData, { withCredentials: true })
        if (response.data) {
          alert('房间添加成功')
          this.getRooms()
          this.roomForm = {
            roomNumber: '',
            roomTypeId: '',
            status: '可用'
          }
        }
      } catch (error) {
        console.error('添加房间失败:', error)
        console.error('错误详情:', error.response?.data)
        alert('添加房间失败，请稍后重试')
      }
    },
    async updateRoomStatus(roomId, status) {
      try {
        const response = await axios.put(`/api/user/admin/rooms/${roomId}/status?status=${encodeURIComponent(status)}`, {}, { withCredentials: true })
        if (response.data) {
          alert('房间状态更新成功')
          this.getRooms()
        }
      } catch (error) {
        console.error('更新房间状态失败:', error)
        alert('更新房间状态失败，请稍后重试')
      }
    },
    async updateOrderStatus(orderId, status) {
      try {
        const response = await axios.put(`/api/admin/orders/${orderId}/status?status=${encodeURIComponent(status)}`, {}, { withCredentials: true })
        if (response.data) {
          alert('订单状态更新成功')
          this.getOrders()
          this.getStatistics()
        }
      } catch (error) {
        console.error('更新订单状态失败:', error)
        alert('更新订单状态失败，请稍后重试')
      }
    },
    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-CN')
    },
    formatDateTime(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleString('zh-CN')
    },
    // 账户管理相关方法
    async getUsers() {
      try {
        const response = await axios.get('/api/admin/users', {
          params: {
            page: this.userCurrentPage - 1, // 后端从0开始
            size: this.userPageSize
          },
          withCredentials: true
        })
        this.users = response.data.content
        this.usersTotalElements = response.data.totalElements
        this.usersTotalPages = response.data.totalPages
      } catch (error) {
        console.error('获取用户列表失败:', error)
      }
    },
    async getRoles() {
      try {
        const response = await axios.get('/api/admin/users/roles', { withCredentials: true })
        this.roles = response.data
      } catch (error) {
        console.error('获取角色列表失败:', error)
      }
    },
    async addUser() {
      try {
        const response = await axios.post('/api/admin/users', this.userForm, { withCredentials: true })
        if (response.data.success) {
          alert('账户添加成功')
          this.getUsers()
          this.userForm = {
            username: '',
            password: '',
            name: '',
            phone: '',
            email: '',
            idCard: '',
            role: 'USER'
          }
        } else {
          alert('账户添加失败: ' + response.data.message)
        }
      } catch (error) {
        console.error('添加账户失败:', error)
        console.error('错误详情:', error.response?.data)
        alert('添加账户失败: ' + (error.response?.data?.message || error.message || '请稍后重试'))
      }
    },
    showDeleteModal(userId) {
      this.currentDeleteUserId = userId
      this.deleteModalVisible = true
    },
    closeDeleteModal() {
      this.deleteModalVisible = false
      this.currentDeleteUserId = null
    },
    async handleConfirmDelete() {
      if (!this.currentDeleteUserId) return
      
      try {
        const response = await axios.delete(`/api/admin/users/${this.currentDeleteUserId}`, { withCredentials: true })
        if (response.data.success) {
          alert('账户删除成功')
          this.closeDeleteModal()
          this.getUsers()
        } else {
          alert('账户删除失败: ' + response.data.message)
        }
      } catch (error) {
        console.error('删除账户失败:', error)
        alert('删除账户失败，请稍后重试')
      }
    },
    getRoleName(roles) {
      if (!roles || roles.length === 0) return '普通用户'
      const role = roles[0]
      if (role.name === 'ADMIN') return '管理员'
      if (role.name === 'FRONT') return '前台'
      return '普通用户'
    },
    goToUserPage(page) {
      this.userCurrentPage = page
      this.getUsers()
    },
    goToUserPrevPage() {
      if (this.userHasPrevPage) {
        this.userCurrentPage--
        this.getUsers()
      }
    },
    goToUserNextPage() {
      if (this.userHasNextPage) {
        this.userCurrentPage++
        this.getUsers()
      }
    }
  }
}
</script>

<style scoped>
.admin {
  max-width: 1200px;
  margin: 0 auto;
}

.admin h2 {
  text-align: center;
  margin-bottom: 2rem;
  color: #333;
}

.admin-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 2rem;
  margin-bottom: 2rem;
}

.admin-card {
  padding: 2.5rem 2rem;
  background-color: var(--card-bg);
  border-radius: 12px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid var(--border-color);
}

.admin-card:hover {
  transform: translateY(-5px);
  box-shadow: var(--shadow-md);
  border-color: var(--primary-color);
}

.admin-card.active {
  border-color: var(--primary-color);
  background-color: var(--primary-color);
}

.admin-card.active h3,
.admin-card.active p {
  color: var(--text-white);
}

.admin-card .card-icon {
  font-size: 3.5rem;
  margin-bottom: 1rem;
}

.admin-card h3 {
  font-size: 1.4rem;
  margin-bottom: 0.5rem;
  color: #333;
}

.admin-card p {
  color: #666;
  margin: 0;
}

.tab-content {
  background-color: #f9f9f9;
  padding: 2rem;
  border-radius: 8px;
}

.tab-content h3 {
  text-align: center;
  margin-bottom: 2rem;
  color: #333;
}

.config-section {
  margin-bottom: 2.5rem;
  padding-bottom: 2rem;
  border-bottom: 1px solid #ddd;
}

.config-section:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.config-section h4 {
  margin-bottom: 1.5rem;
  color: #333;
  font-size: 1.2rem;
}

.config-section h5 {
  margin-bottom: 1rem;
  color: #555;
}

.add-form {
  margin-bottom: 1.5rem;
  padding: 1.5rem;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 0 5px rgba(0, 0, 0, 0.1);
}

.form-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: #333;
  font-weight: 500;
}

.form-group input,
.form-group textarea,
.form-group select {
  width: 100%;
  padding: 0.6rem;
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

.btn-small {
  padding: 0.4rem 0.8rem;
  font-size: 0.9rem;
}

.btn-delete {
  background-color: #dc3545;
}

.btn-delete:hover {
  background-color: #c82333;
}

.list {
  background-color: white;
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: 0 0 5px rgba(0, 0, 0, 0.1);
}

.item {
  padding: 1rem;
  border-bottom: 1px solid #eee;
  margin-bottom: 1rem;
}

.item:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.item p {
  margin-bottom: 0.5rem;
  color: #666;
}

.statistics {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 2rem;
}

.stat-card {
  padding: 2rem;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 0 5px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.stat-card h5 {
  margin-bottom: 1rem;
  color: #666;
}

.stat-number {
  font-size: 2.5rem;
  font-weight: bold;
  color: #333;
}

.chart-container {
  margin-top: 2rem;
  padding: 1.5rem;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 0 5px rgba(0, 0, 0, 0.1);
}

.file-input {
  width: 100%;
  padding: 0.6rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
  margin-bottom: 1rem;
}

.image-preview {
  margin-top: 1rem;
  text-align: center;
}

.preview-img {
  max-width: 300px;
  max-height: 200px;
  border: 2px solid #ddd;
  border-radius: 8px;
  object-fit: cover;
}

.room-type-item {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.room-type-image {
  flex-shrink: 0;
}

.room-type-img {
  width: 120px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #ddd;
}

.room-type-info {
  flex: 1;
}

.room-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.room-info {
  flex: 1;
}

.room-status-edit {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.status-select {
  padding: 0.4rem 0.6rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 0.95rem;
}

.order-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.order-info {
  flex: 1;
}

.order-status {
  padding: 1rem;
  background-color: #f9f9f9;
  border-radius: 4px;
  min-width: 120px;
  text-align: center;
}

.loading {
  text-align: center;
  padding: 2rem;
  color: #666;
}

.empty {
  text-align: center;
  padding: 2rem;
  color: #999;
  background-color: #f9f9f9;
  border-radius: 4px;
}

.service-log-item {
  background-color: #f9f9f9;
  border-radius: 8px;
  border-bottom: none !important;
  margin-bottom: 1rem;
  padding: 1.5rem !important;
}

.log-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.log-header h6 {
  margin: 0;
  color: #333;
  font-size: 1rem;
}

.status-badge {
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

.order-tabs {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
}

.order-tabs button {
  padding: 0.6rem 1.2rem;
  background-color: var(--bg-white);
  color: var(--text-color);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.95rem;
}

.order-tabs button:hover {
  border-color: var(--primary-color);
  color: var(--primary-color);
}
.order-tabs button.active {
  background-color: var(--primary-color);
  color: var(--text-white);
  border-color: var(--primary-color);
}

.order-chart-container {
  background-color: white;
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: 0 0 5px rgba(0, 0, 0, 0.1);
  margin-bottom: 1.5rem;
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
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}
</style>
