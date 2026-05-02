<template>
  <div class="finance">
    <div class="header-actions">
      <div class="refresh-section">
        <span class="last-update">最后更新: {{ lastUpdateTime }}</span>
        <button @click="refreshAllData" :disabled="refreshing" class="refresh-btn">
          <span v-if="refreshing">刷新中...</span>
          <span v-else><SvgIcon name="refresh" size="1em" /> 刷新数据</span>
        </button>
      </div>
    </div>

    <!-- 顶部数据看板 -->
    <div class="stats-grid">
      <div class="stat-card stat-revenue">
        <h5>总收入</h5>
        <p class="stat-number">¥{{ summary.totalRevenue?.toFixed(2) || '0.00' }}</p>
      </div>
      <div class="stat-card stat-revenue">
        <h5>本月收入</h5>
        <p class="stat-number">¥{{ summary.monthRevenue?.toFixed(2) || '0.00' }}</p>
      </div>
      <div class="stat-card stat-revenue">
        <h5>今日收入</h5>
        <p class="stat-number">¥{{ summary.todayRevenue?.toFixed(2) || '0.00' }}</p>
      </div>
      <div class="stat-card stat-order-paid">
        <h5>已支付订单</h5>
        <p class="stat-number">{{ summary.paidOrders || 0 }}</p>
      </div>
      <div class="stat-card stat-order-pending">
        <h5>待支付订单</h5>
        <p class="stat-number">{{ summary.pendingOrders || 0 }}</p>
      </div>
      <div class="stat-card stat-order-cancelled">
        <h5>已取消订单</h5>
        <p class="stat-number">{{ summary.cancelledOrders || 0 }}</p>
      </div>
      <div class="stat-card stat-room-reserved">
        <h5>已预定房间</h5>
        <p class="stat-number">{{ summary.reservedRooms || 0 }}</p>
      </div>
      <div class="stat-card stat-room-checkedin">
        <h5>已入住房间</h5>
        <p class="stat-number">{{ summary.checkedInRooms || 0 }}</p>
      </div>
      <div class="stat-card stat-room-available">
        <h5>空房</h5>
        <p class="stat-number">{{ summary.availableRooms || 0 }}</p>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <!-- 月度收入趋势折线图 -->
      <div class="chart-card">
        <h4 class="section-title">月度收入趋势</h4>
        <div ref="lineChartRef" style="width: 100%; height: 350px;"></div>
      </div>

      <!-- 各房型收入占比饼图 -->
      <div class="chart-card">
        <h4 class="section-title">各房型收入占比</h4>
        <div ref="pieChartRef" style="width: 100%; height: 350px;"></div>
      </div>
    </div>

    <!-- 每日收入明细 -->
    <div class="chart-section">
      <h4 class="section-title">本月每日收入明细</h4>
      <div class="chart-container">
        <div ref="barChartRef" style="width: 100%; height: 350px;"></div>
      </div>
    </div>
    <!-- 30天数据滑块 -->
    <div class="chart-slider-wrapper">
      <input type="range" class="chart-slider"
        v-model.number="sliderValue"
        :min="0" :max="totalDays - windowSize"
        @input="onSliderChange"
      />
    </div>

    <!-- 收入流水列表 -->
    <div class="orders-section">
      <div class="section-header">
        <h4 class="section-title">收入流水列表</h4>
        <!-- 筛选功能 -->
        <div class="filter-bar">
          <select v-model="selectedStatus" @change="onStatusChange" class="status-filter">
            <option value="">全部状态</option>
            <option value="已预订">已预订</option>
            <option value="已入住">已入住</option>
            <option value="已完成">已完成</option>
            <option value="已取消">已取消</option>
          </select>
        </div>
      </div>
      
      <!-- 加载状态 -->
      <div v-if="loadingOrders" class="loading">
        <div class="loading-spinner"></div>
        <p>正在加载数据...</p>
      </div>
      
      <!-- 错误状态 -->
      <div v-else-if="loadError" class="error-state">
        <p>加载失败，请重试</p>
        <button @click="getOrders" class="retry-btn">重新加载</button>
      </div>
      
      <div v-else class="list">
        <!-- 空数据状态 -->
        <div v-if="orders.length === 0" class="empty">
          <svg class="empty-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M9 12h6m-6 4h6m-2-12h4a2 2 0 012 2v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6a2 2 0 012-2z" />
          </svg>
          <p>暂无订单数据</p>
        </div>
        
        <div v-else class="table-container">
          <table class="order-table">
            <thead>
              <tr>
                <th @click="sortBy('orderNumber')" :class="{ sortable: true, sorted: sortKey === 'orderNumber' }">
                  订单号
                  <span class="sort-icon">{{ sortKey === 'orderNumber' ? (sortOrder === 'asc' ? '↑' : '↓') : '' }}</span>
                </th>
                <th @click="sortBy('roomNumber')" :class="{ sortable: true, sorted: sortKey === 'roomNumber' }">
                  房间号
                  <span class="sort-icon">{{ sortKey === 'roomNumber' ? (sortOrder === 'asc' ? '↑' : '↓') : '' }}</span>
                </th>
                <th @click="sortBy('userName')" :class="{ sortable: true, sorted: sortKey === 'userName' }">
                  用户信息
                  <span class="sort-icon">{{ sortKey === 'userName' ? (sortOrder === 'asc' ? '↑' : '↓') : '' }}</span>
                </th>
                <th @click="sortBy('roomTypeName')" :class="{ sortable: true, sorted: sortKey === 'roomTypeName' }">
                  房型名称
                  <span class="sort-icon">{{ sortKey === 'roomTypeName' ? (sortOrder === 'asc' ? '↑' : '↓') : '' }}</span>
                </th>
                <th @click="sortBy('totalPrice')" :class="{ sortable: true, sorted: sortKey === 'totalPrice' }">
                  交易金额
                  <span class="sort-icon">{{ sortKey === 'totalPrice' ? (sortOrder === 'asc' ? '↑' : '↓') : '' }}</span>
                </th>
                <th @click="sortBy('createTime')" :class="{ sortable: true, sorted: sortKey === 'createTime' }">
                  支付时间
                  <span class="sort-icon">{{ sortKey === 'createTime' ? (sortOrder === 'asc' ? '↑' : '↓') : '' }}</span>
                </th>
                <th @click="sortBy('status')" :class="{ sortable: true, sorted: sortKey === 'status' }">
                  订单状态
                  <span class="sort-icon">{{ sortKey === 'status' ? (sortOrder === 'asc' ? '↑' : '↓') : '' }}</span>
                </th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="order in sortedOrders" :key="order.id" :class="getRowClass(order.status)">
                <td class="order-number">{{ order.orderNumber }}</td>
                <td class="room-number">{{ order.room?.roomNumber }}</td>
                <td class="user-info">
                  <div class="user-name">{{ order.user?.name || order.user?.username }}</div>
                  <div class="user-extra" v-if="order.user?.phone">{{ order.user.phone }}</div>
                </td>
                <td class="room-type">{{ order.room?.roomType?.name }}</td>
                <td class="amount-cell" :class="'amount-' + getStatusBadgeClass(order.status)">
                  ¥{{ order.totalPrice?.toFixed(2) }}
                </td>
                <td class="order-time">{{ formatDateTime(order.createTime) }}</td>
                <td><span :class="['status-badge', getStatusBadgeClass(order.status)]">{{ getDisplayStatus(order.status) }}</span></td>
              </tr>
            </tbody>
          </table>

          <!-- 分页控件 -->
          <div class="pagination-wrapper">
            <div class="pagination-info">
              共 {{ totalElements }} 条记录，第 {{ currentPage }} / {{ totalPages }} 页
            </div>
            <div class="pagination">
              <button @click="goToPage(1)" :disabled="!hasPrevPage" class="page-btn" title="第一页">«</button>
              <button @click="goToPrevPage" :disabled="!hasPrevPage" class="page-btn">上一页</button>
              
              <div class="page-numbers">
                <template v-for="page in visiblePages" :key="page">
                    <span v-if="page === -1" class="ellipsis">...</span>
                    <button
                      v-else
                      @click="goToPage(page)"
                      :class="['page-number', { active: currentPage === page }]"
                    >
                      {{ page }}
                    </button>
                  </template>
              </div>
              
              <button @click="goToNextPage" :disabled="!hasNextPage" class="page-btn">下一页</button>
              <button @click="goToPage(totalPages)" :disabled="!hasNextPage" class="page-btn" title="最后一页">»</button>
            </div>
            
            <!-- 快速跳转 -->
            <div class="jump-page">
              <span>跳至</span>
              <input type="number" v-model.number="jumpPage" @keyup.enter="handleJumpPage" min="1" :max="totalPages" />
              <span>页</span>
              <button @click="handleJumpPage" class="jump-btn">跳转</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import * as echarts from 'echarts'
import SvgIcon from '../components/SvgIcon.vue'

export default {
  name: 'Finance',
  components: {
    SvgIcon
  },
  data() {
    return {
      summary: {
        totalRevenue: 0,
        todayRevenue: 0,
        monthRevenue: 0,
        avgOrderPrice: 0,
        totalOrders: 0,
        paidOrders: 0,
        pendingOrders: 0,
        cancelledOrders: 0,
        reservedRooms: 0,
        checkedInRooms: 0,
        availableRooms: 0
      },
      monthlyData: [],
      dailyData: [],
      roomTypeData: [],
      orders: [],
      totalElements: 0,
      totalPages: 0,
      currentPage: 1,
      pageSize: 10,
      loadingOrders: false,
      loadError: false,
      lineChart: null,
      pieChart: null,
      barChart: null,
      // 滑块相关（30天数据，7天窗口）
      sliderValue: 23,
      windowSize: 7,
      totalDays: 30,
      resizeListenerAdded: false,
      // 筛选和排序
      selectedStatus: '',
      sortKey: 'createTime',
      sortOrder: 'desc',
      jumpPage: 1,
      // 刷新相关
      refreshing: false,
      lastUpdateTime: '',
      refreshInterval: null,
      autoRefreshEnabled: true
    }
  },
  computed: {
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
    visiblePages() {
      // 只显示当前页附近的页码
      const pages = []
      let start = Math.max(1, this.currentPage - 2)
      let end = Math.min(this.totalPages, this.currentPage + 2)
      
      if (start > 1) {
        pages.push(1)
        if (start > 2) {
          pages.push(-1) // 表示省略号
        }
      }
      
      for (let i = start; i <= end; i++) {
        pages.push(i)
      }
      
      if (end < this.totalPages) {
        if (end < this.totalPages - 1) {
          pages.push(-1)
        }
        pages.push(this.totalPages)
      }
      
      return pages
    },
    sortedOrders() {
      if (!this.orders.length) return []
      
      return [...this.orders].sort((a, b) => {
        let aVal, bVal
        
        switch (this.sortKey) {
          case 'orderNumber':
            aVal = a.orderNumber || ''
            bVal = b.orderNumber || ''
            break
          case 'roomNumber':
            aVal = a.room?.roomNumber || ''
            bVal = b.room?.roomNumber || ''
            break
          case 'userName':
            aVal = a.user?.name || a.user?.username || ''
            bVal = b.user?.name || b.user?.username || ''
            break
          case 'roomTypeName':
            aVal = a.room?.roomType?.name || ''
            bVal = b.room?.roomType?.name || ''
            break
          case 'totalPrice':
            aVal = a.totalPrice || 0
            bVal = b.totalPrice || 0
            break
          case 'createTime':
            aVal = a.createTime ? new Date(a.createTime).getTime() : 0
            bVal = b.createTime ? new Date(b.createTime).getTime() : 0
            break
          case 'status':
            aVal = a.status || ''
            bVal = b.status || ''
            break
          default:
            return 0
        }
        
        if (typeof aVal === 'string') {
          aVal = aVal.toLowerCase()
          bVal = bVal.toLowerCase()
        }
        
        if (this.sortOrder === 'asc') {
          return aVal > bVal ? 1 : -1
        } else {
          return aVal < bVal ? 1 : -1
        }
      })
    }
  },
  mounted() {
    // 先初始化图表
    this.$nextTick(() => {
      this.initLineChart()
      this.initPieChart()
      this.initBarChart()
    })
    // 再加载数据
    this.refreshAllData()
    // 启动自动刷新（每10秒刷新一次）
    if (this.autoRefreshEnabled) {
      this.startAutoRefresh()
    }
  },
  beforeDestroy() {
    // 清除自动刷新定时器
    this.stopAutoRefresh()
    // 清除事件监听器
    if (this.resizeListenerAdded) {
      window.removeEventListener('resize', this.handleResize)
    }
    // 销毁图表
    if (this.lineChart) {
      this.lineChart.dispose()
    }
    if (this.pieChart) {
      this.pieChart.dispose()
    }
    if (this.barChart) {
      this.barChart.dispose()
    }
  },
  methods: {
    // 更新最后刷新时间
    updateLastUpdateTime() {
      const now = new Date()
      this.lastUpdateTime = now.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      })
    },
    // 刷新所有数据
    async refreshAllData() {
      if (this.refreshing) return
      
      this.refreshing = true
      console.log('[财务统计] 开始刷新数据...')
      
      try {
        // 并行加载所有数据以提高效率
        await Promise.all([
          this.getSummary(),
          this.getMonthlyData(),
          this.getDailyData(),
          this.getRoomTypeData(),
          this.getOrders()
        ])
        
        this.updateLastUpdateTime()
        console.log('[财务统计] 数据刷新完成')
      } catch (error) {
        console.error('[财务统计] 刷新数据失败:', error)
      } finally {
        this.refreshing = false
      }
    },
    // 启动自动刷新
    startAutoRefresh() {
      if (this.refreshInterval) return
      
      console.log('[财务统计] 启动自动刷新（每60秒）')
      // 优化：只在页面可见时刷新，减少不必要的请求
      const checkAndRefresh = () => {
        if (document.visibilityState === 'visible' && !this.refreshing) {
          console.log('[财务统计] 自动刷新触发')
          this.refreshAllData()
        }
      }
      // 初始延迟30秒后检查，之后每60秒检查一次
      setTimeout(checkAndRefresh, 30000)
      this.refreshInterval = setInterval(checkAndRefresh, 60000)
    },
    // 停止自动刷新
    stopAutoRefresh() {
      if (this.refreshInterval) {
        console.log('[财务统计] 停止自动刷新')
        clearInterval(this.refreshInterval)
        this.refreshInterval = null
      }
    },
    async getSummary() {
      try {
        const response = await axios.get('/api/admin/finance/summary', { withCredentials: true })
        this.summary = response.data
        console.log('[财务统计] 摘要数据已更新:', response.data)
      } catch (error) {
        console.error('获取财务摘要失败:', error)
      }
    },
    async getMonthlyData() {
      try {
        const response = await axios.get('/api/admin/finance/monthly', { 
          params: { months: 6 },
          withCredentials: true 
        })
        this.monthlyData = response.data
        console.log('[财务统计] 月度数据已更新:', response.data.length, '条')
        this.$nextTick(() => {
          if (this.lineChart) {
            this.updateLineChart()
          }
        })
      } catch (error) {
        console.error('获取月度数据失败:', error)
      }
    },
    async getDailyData() {
      try {
        const response = await axios.get('/api/admin/finance/daily', { 
          params: { days: 30 },
          withCredentials: true 
        })
        this.dailyData = response.data
        console.log('[财务统计] 每日数据已更新:', response.data.length, '条')
        this.$nextTick(() => {
          if (this.barChart) {
            this.updateBarChart()
          }
        })
      } catch (error) {
        console.error('获取每日数据失败:', error)
      }
    },
    async getRoomTypeData() {
      try {
        const response = await axios.get('/api/admin/finance/room-type-stats', { withCredentials: true })
        this.roomTypeData = response.data
        console.log('[财务统计] 房型数据已更新:', response.data.length, '条')
        this.$nextTick(() => {
          if (this.pieChart) {
            this.updatePieChart()
          }
        })
      } catch (error) {
        console.error('获取房型数据失败:', error)
      }
    },
    async getOrders() {
      this.loadingOrders = true
      this.loadError = false
      try {
        const response = await axios.get('/api/admin/finance/orders', { 
          params: { 
            page: this.currentPage - 1, // 后端从0开始
            size: this.pageSize,
            status: this.selectedStatus || undefined
          },
          withCredentials: true 
        })
        this.orders = response.data.content
        this.totalElements = response.data.totalElements
        this.totalPages = response.data.totalPages
        this.jumpPage = this.currentPage
        console.log('[财务统计] 订单列表已更新:', response.data.content.length, '条，总计:', response.data.totalElements)
      } catch (error) {
        console.error('获取订单列表失败:', error)
        this.loadError = true
      } finally {
        this.loadingOrders = false
      }
    },
    // 排序方法
    sortBy(key) {
      if (this.sortKey === key) {
        this.sortOrder = this.sortOrder === 'asc' ? 'desc' : 'asc'
      } else {
        this.sortKey = key
        this.sortOrder = 'desc'
      }
    },
    // 状态筛选改变
    onStatusChange() {
      this.currentPage = 1
      this.getOrders()
    },
    // 快速跳转
    handleJumpPage() {
      let page = parseInt(this.jumpPage)
      if (isNaN(page) || page < 1 || page > this.totalPages) {
        this.jumpPage = this.currentPage
        return
      }
      this.goToPage(page)
    },
    // 获取行样式类
    getDisplayStatus(status) {
      if (status === '自动退房') return '已完成'
      return status
    },
    getRowClass(status) {
      const classes = []
      if (status === '已支付' || status === '已完成' || status === '自动退房') {
        classes.push('row-paid')
      } else if (status === '已预订') {
        classes.push('row-pending')
      } else if (status === '已入住') {
        classes.push('row-checked')
      } else if (status === '已取消') {
        classes.push('row-cancelled')
      }
      return classes
    },
    // 获取状态徽章类
    getStatusBadgeClass(status) {
      switch (status) {
        case '已支付':
          return 'status-paid'
        case '已完成':
        case '自动退房':
          return 'status-completed'
        case '已预订':
          return 'status-warning'
        case '已入住':
          return 'status-info'
        case '已取消':
          return 'status-danger'
        default:
          return 'status-default'
      }
    },
    // 判断是否是已支付状态
    isPaidStatus(status) {
      return status === '已支付' || status === '已完成' || status === '自动退房'
    },
    goToPage(page) {
      this.currentPage = page
      this.getOrders()
    },
    goToPrevPage() {
      if (this.hasPrevPage) {
        this.currentPage--
        this.getOrders()
      }
    },
    goToNextPage() {
      if (this.hasNextPage) {
        this.currentPage++
        this.getOrders()
      }
    },
    initLineChart() {
      const chartDom = this.$refs.lineChartRef
      if (!chartDom) return
      
      if (this.lineChart) {
        this.lineChart.dispose()
        this.lineChart = null
      }
      
      this.lineChart = echarts.init(chartDom)
      if (!this.resizeListenerAdded) {
        window.addEventListener('resize', this.handleResize)
        this.resizeListenerAdded = true
      }
      this.updateLineChart()
    },
    updateLineChart() {
      if (!this.lineChart) return
      
      const option = {
        animation: true,
        animationDuration: 800,
        animationDurationUpdate: 300,
        animationEasing: 'cubicOut',
        tooltip: {
          trigger: 'axis',
          formatter: function(params) {
            const data = params[0]
            if (!data) return ''
            return `${data.name}<br/>收入: ¥${(data.value || 0).toFixed(2)}`
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
          boundaryGap: false,
          data: this.monthlyData.map(item => item.month),
          axisLabel: {
            interval: 0
          }
        },
        yAxis: {
          type: 'value',
          axisLabel: {
            formatter: '¥{value}'
          }
        },
        series: [
          {
            name: '收入',
            type: 'line',
            smooth: true,
            data: this.monthlyData.map(item => item.revenue || 0),
            itemStyle: {
              color: '#5b9bd5'
            },
            areaStyle: {
              color: {
                type: 'linear',
                x: 0, y: 0, x2: 0, y2: 1,
                colorStops: [
                  { offset: 0, color: 'rgba(91, 155, 213, 0.3)' },
                  { offset: 1, color: 'rgba(91, 155, 213, 0.05)' }
                ]
              }
            },
            lineStyle: {
              width: 3
            }
          }
        ]
      }
      
      this.lineChart.setOption(option, { notMerge: true })
    },
    initPieChart() {
      const chartDom = this.$refs.pieChartRef
      if (!chartDom) return
      
      if (this.pieChart) {
        this.pieChart.dispose()
        this.pieChart = null
      }
      
      this.pieChart = echarts.init(chartDom)
      this.updatePieChart()
    },
    updatePieChart() {
      if (!this.pieChart) return
      
      const colors = ['#5b9bd5', '#52c41a', '#faad14', '#ff4d4f', '#909399', '#00CED1', '#FF69B4']
      
      const option = {
        animation: true,
        animationDuration: 800,
        animationDurationUpdate: 300,
        animationEasing: 'cubicOut',
        tooltip: {
          trigger: 'item',
          formatter: function(params) {
            if (!params) return ''
            const percentage = (params.percent || 0).toFixed(1)
            return `${params.name}<br/>收入: ¥${(params.value || 0).toFixed(2)} (${percentage}%)`
          }
        },
        legend: {
          orient: 'vertical',
          left: 'left'
        },
        series: [
          {
            name: '收入',
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
            data: this.roomTypeData.map((item, index) => ({
              value: item.revenue || 0,
              name: item.roomTypeName || '未知房型',
              itemStyle: {
                color: colors[index % colors.length]
              }
            }))
          }
        ]
      }
      
      this.pieChart.setOption(option, { notMerge: true })
    },
    initBarChart() {
      const chartDom = this.$refs.barChartRef
      if (!chartDom) return
      
      if (this.barChart) {
        this.barChart.dispose()
        this.barChart = null
      }
      
      this.barChart = echarts.init(chartDom)
      this.updateBarChart()
    },
    // 获取30天的完整数据
    getAllDaysData() {
      const days = this.dailyData.map(item => item.date)
      const data = this.dailyData.map(item => item.revenue)
      return { allDays: days, allData: data }
    },
    // 根据滑块位置取7天窗口
    getWindowData() {
      const { allDays, allData } = this.getAllDaysData()
      const startIdx = Math.min(this.sliderValue, this.totalDays - this.windowSize)
      return {
        days: allDays.slice(startIdx, startIdx + this.windowSize),
        data: allData.slice(startIdx, startIdx + this.windowSize)
      }
    },
    updateBarChart() {
      if (!this.barChart) return
      
      const { days, data } = this.getWindowData()
      
      const option = {
        animation: true,
        animationDuration: 800,
        animationDurationUpdate: 300,
        animationEasing: 'cubicOut',
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          },
          formatter: function(params) {
            const dataItem = params[0]
            if (!dataItem) return ''
            return `${dataItem.name}<br/>收入: ¥${(dataItem.value || 0).toFixed(2)}`
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
          axisLabel: {
            formatter: '¥{value}'
          },
          minInterval: 1
        },
        series: [
          {
            name: '收入',
            type: 'bar',
            data: data.map(d => d || 0),
            itemStyle: {
              color: '#5b9bd5',
              borderRadius: [4, 4, 0, 0]
            },
            barWidth: '50%',
            label: {
              show: true,
              position: 'top',
              formatter: function(params) {
                return '¥' + (params.value || 0).toFixed(0)
              }
            }
          }
        ]
      }
      
      this.barChart.setOption(option, { notMerge: true })
    },
    handleResize() {
      if (this.lineChart) {
        this.lineChart.resize()
      }
      if (this.pieChart) {
        this.pieChart.resize()
      }
      if (this.barChart) {
        this.barChart.resize()
      }
    },
    onSliderChange() {
      this.$nextTick(() => this.updateBarChart())
    },
    formatDateTime(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      return `${year}-${month}-${day} ${hours}:${minutes}`
    }
  }
}
</script>

<style scoped>
.finance {
  max-width: 1200px;
  margin: 0 auto;
  padding: 1rem;
  animation: fadeInUp var(--transition-slow);
}

.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  flex-wrap: wrap;
  gap: 1rem;
}

.header-actions h2 {
  margin: 0;
  color: var(--text-primary);
}

.refresh-section {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.last-update {
  font-size: 0.9rem;
  color: var(--text-secondary);
}

.refresh-btn {
  padding: 0.6rem 1.2rem;
  background: var(--primary-gradient);
  color: var(--text-white);
  border: none;
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-size: 0.95rem;
  transition: all var(--transition);
}

.refresh-btn:hover:not(:disabled) {
  box-shadow: 0 4px 12px rgba(91, 155, 213, 0.25);
  transform: translateY(-1px);
}

.refresh-btn:disabled {
  background-color: var(--text-light);
  cursor: not-allowed;
  opacity: 0.7;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.stat-card {
  padding: 1.5rem;
  background-color: var(--bg-white);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-xs);
  text-align: center;
  transition: all var(--transition);
  border: 1px solid var(--border-light);
  border-top: 3px solid transparent;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border-color: var(--border-light);
}

.stat-card::before {
  display: none;
}

.stat-card.stat-revenue {
  border-top-color: var(--primary-color);
}

.stat-card.stat-order-paid {
  border-top-color: #52c41a;
}

.stat-card.stat-order-pending {
  border-top-color: #faad14;
}

.stat-card.stat-order-cancelled {
  border-top-color: #ff4d4f;
}

.stat-card.stat-room-reserved {
  border-top-color: #9254de;
}

.stat-card.stat-room-checkedin {
  border-top-color: #13c2c2;
}

.stat-card.stat-room-available {
  border-top-color: #73d13d;
}

.stat-card.stat-revenue .stat-number {
  background: var(--primary-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.stat-card.stat-order-paid .stat-number {
  color: #52c41a;
  -webkit-text-fill-color: #52c41a;
}

.stat-card.stat-order-pending .stat-number {
  color: #faad14;
  -webkit-text-fill-color: #faad14;
}

.stat-card.stat-order-cancelled .stat-number {
  color: #ff4d4f;
  -webkit-text-fill-color: #ff4d4f;
}

.stat-card.stat-room-reserved .stat-number {
  color: #9254de;
  -webkit-text-fill-color: #9254de;
}

.stat-card.stat-room-checkedin .stat-number {
  color: #13c2c2;
  -webkit-text-fill-color: #13c2c2;
}

.stat-card.stat-room-available .stat-number {
  color: #73d13d;
  -webkit-text-fill-color: #73d13d;
}

.stat-card h5 {
  margin-bottom: 1rem;
  color: var(--text-secondary);
  font-size: 0.95rem;
  font-weight: 500;
}

.stat-number {
  font-size: 2.2rem;
  font-weight: 700;
}

.charts-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(450px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.chart-card {
  background-color: var(--bg-white);
  padding: 1.5rem;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-xs);
  border: 1px solid var(--border-light);
}

.chart-card h4 {
  margin-bottom: 1rem;
  color: var(--text-primary);
  text-align: center;
  font-weight: 600;
}

.chart-section {
  background-color: var(--bg-white);
  padding: 1.5rem;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-xs);
  border: 1px solid var(--border-light);
  margin-bottom: 2rem;
}

.chart-section h4 {
  margin-bottom: 1rem;
  color: var(--text-primary);
  text-align: center;
  font-weight: 600;
}

.chart-container {
  margin-bottom: 1rem;
}

.chart-slider-wrapper {
  background-color: var(--bg-white);
  padding: 1rem 2rem 2rem;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-xs);
  border: 1px solid var(--border-light);
  margin-bottom: 1.5rem;
}

.chart-slider {
  width: 100%;
  height: 6px;
  -webkit-appearance: none;
  appearance: none;
  background: linear-gradient(to right, var(--primary-color), var(--primary-light));
  border-radius: 3px;
  outline: none;
  cursor: pointer;
}

.chart-slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 24px;
  height: 10px;
  background: var(--primary-color);
  border-radius: 2px;
  cursor: pointer;
  box-shadow: 0 1px 4px rgba(0,0,0,0.25);
  transition: transform 0.2s ease;
}

.chart-slider::-webkit-slider-thumb:hover {
  transform: scaleY(1.3);
}

.chart-slider::-moz-range-thumb {
  width: 24px;
  height: 10px;
  background: var(--primary-color);
  border-radius: 2px;
  cursor: pointer;
  box-shadow: 0 1px 4px rgba(0,0,0,0.25);
}

.orders-section {
  background-color: var(--bg-white);
  padding: 1.5rem;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-xs);
  border: 1px solid var(--border-light);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
  gap: 1rem;
}

.section-header h4 {
  margin: 0;
  color: var(--text-primary);
  font-weight: 600;
}

.filter-bar {
  display: flex;
  gap: 1rem;
}

.status-filter {
  padding: 0.5rem 1rem;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  background-color: var(--bg-white);
  color: var(--text-primary);
  cursor: pointer;
  font-size: 0.9rem;
  outline: none;
  transition: all var(--transition);
}

.status-filter:hover,
.status-filter:focus {
  border-color: var(--primary-color);
}

.list {
  margin-top: 1rem;
}

.table-container {
  overflow-x: auto;
}

.order-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 800px;
}

.order-table tbody tr {
  transition: background-color 0.2s ease;
}

.order-table thead {
  position: sticky;
  top: 0;
  z-index: 10;
}

.order-table th,
.order-table td {
  padding: 1rem;
  text-align: left;
  border-bottom: 1px solid var(--border-light);
}

.order-table th {
  background-color: var(--bg-light);
  font-weight: 600;
  color: var(--text-primary);
  white-space: nowrap;
}

.order-table th.sortable {
  cursor: pointer;
  user-select: none;
  transition: background-color 0.2s ease;
}

.order-table th.sortable:hover {
  background-color: var(--status-info-bg);
}

.order-table th.sortable.sorted {
  background-color: var(--status-info-bg);
  color: var(--primary-color);
}

.sort-icon {
  margin-left: 0.3rem;
  font-size: 0.8rem;
}

.order-table tr:hover {
  background-color: var(--bg-light);
}

.order-table tr.row-paid {
  background-color: rgba(144, 147, 153, 0.05);
}

.order-table tr.row-paid:hover {
  background-color: rgba(144, 147, 153, 0.1);
}

.order-table tr.row-pending {
  background-color: rgba(144, 147, 153, 0.05);
}

.order-table tr.row-pending:hover {
  background-color: rgba(144, 147, 153, 0.1);
}

.order-table tr.row-checked {
  background-color: rgba(144, 147, 153, 0.05);
}

.order-table tr.row-checked:hover {
  background-color: rgba(144, 147, 153, 0.1);
}

.order-table tr.row-cancelled {
  background-color: rgba(144, 147, 153, 0.05);
  opacity: 0.8;
}

.order-table tr.row-cancelled:hover {
  background-color: rgba(144, 147, 153, 0.1);
}

.order-number {
  font-family: 'Monaco', 'Consolas', monospace;
  font-size: 0.9rem;
  color: var(--text-secondary);
}

.room-number {
  font-weight: 500;
  color: var(--text-primary);
}

.user-info {
  min-width: 120px;
}

.user-name {
  font-weight: 500;
  color: var(--text-primary);
}

.user-extra {
  font-size: 0.8rem;
  color: var(--text-light);
  margin-top: 0.2rem;
}

.room-type {
  color: var(--text-secondary);
}

.amount-cell {
  font-weight: 600;
  font-size: 1.05rem;
  min-width: 100px;
}

.amount-cell.amount-status-warning {
  color: var(--status-warning);
}

.amount-cell.amount-status-info {
  color: var(--status-info);
}

.amount-cell.amount-status-danger {
  color: var(--status-danger);
}

.amount-cell.amount-status-completed {
  color: var(--status-success);
}

.amount-cell.amount-status-paid {
  color: var(--status-info);
}

.amount-cell.amount-status-default {
  color: var(--primary-color);
}

.order-time {
  font-size: 0.9rem;
  color: var(--text-secondary);
  white-space: nowrap;
}

.loading {
  text-align: center;
  padding: 3rem 2rem;
  color: var(--text-light);
}

.error-state {
  text-align: center;
  padding: 3rem 2rem;
  color: var(--text-secondary);
}

.retry-btn {
  margin-top: 1rem;
  padding: 0.5rem 1.5rem;
  background: var(--primary-gradient);
  color: var(--text-white);
  border: none;
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-size: 0.95rem;
  transition: all var(--transition);
}

.retry-btn:hover {
  box-shadow: 0 4px 12px rgba(91, 155, 213, 0.25);
  transform: translateY(-1px);
}

.pagination-wrapper {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  align-items: center;
  margin-top: 1.5rem;
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

@media (max-width: 768px) {
  .finance {
    padding: 0.5rem;
  }
  
  .stats-grid {
    grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
    gap: 1rem;
  }
  
  .stat-card {
    padding: 1rem;
  }
  
  .stat-number {
    font-size: 1.8rem;
  }
  
  .charts-section {
    grid-template-columns: 1fr;
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .pagination-wrapper {
    align-items: stretch;
  }
  
  .pagination {
    flex-wrap: wrap;
  }
  
  .table-container {
    -webkit-overflow-scrolling: touch;
  }
}

@media (max-width: 480px) {
  .pagination-wrapper {
    gap: 0.5rem;
  }
  
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
