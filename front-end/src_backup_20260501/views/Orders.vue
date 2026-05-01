<template>
  <div class="orders">
    <h2 class="page-title">我的订单</h2>
    <LoadingSpinner v-if="loading" variant="user" />
    <div v-else>
      <div v-if="orders.length === 0" class="empty">
        <p>暂无订单</p>
      </div>
      <div v-else class="orders-list">
        <div v-for="order in orders" :key="order.id" class="order card">
          <div class="order-header">
            <h3>{{ order.orderNumber }}</h3>
            <span class="status-badge" :class="getStatusClass(order.status)">{{ order.status }}</span>
          </div>
          <div class="order-body">
            <p><span class="label">房间号</span>{{ order.room.roomNumber }}</p>
            <p><span class="label">房型</span>{{ order.room.roomType.name }}</p>
            <p><span class="label">入住日期</span>{{ formatDateTime(order.checkInTime) }}</p>
            <p><span class="label">退房日期</span>{{ formatDateTime(order.checkOutTime) }}</p>
            <p><span class="label">总价格</span><span class="price">¥{{ order.totalPrice }}</span></p>
          </div>
          <div class="order-actions">
            <button v-if="order.status === '待支付'" @click="cancelOrder(order.id)" class="btn btn-danger btn-sm">取消订单</button>
            <button v-if="order.status === '待支付'" @click="payOrder(order.id)" class="btn btn-confirm btn-sm">去支付</button>
            <button v-if="order.status === '已入住' || order.status === '已预订'" @click="renewOrder(order.id)" class="btn btn-success btn-sm">续订</button>
          </div>
        </div>
      </div>
      
      <div v-if="totalOrders > pageSize" class="pagination">
        <button 
          class="pagination-btn" 
          :disabled="currentPage === 0" 
          @click="changePage(currentPage - 1)">
          上一页
        </button>
        <span class="page-info">
          第 {{ currentPage + 1 }} / {{ totalPages }} 页，共 {{ totalOrders }} 条
        </span>
        <button 
          class="pagination-btn" 
          :disabled="currentPage >= totalPages - 1" 
          @click="changePage(currentPage + 1)">
          下一页
        </button>
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
import LoadingSpinner from '../components/LoadingSpinner.vue'

export default {
  name: 'Orders',
  components: {
    LoadingSpinner
  },
  data() {
    return {
      orders: [],
      loading: true,
      currentPage: 0,
      pageSize: 10,
      totalOrders: 0,
      jumpPage: 1,
      abortController: null,
      isDestroyed: false
    }
  },
  computed: {
    totalPages() {
      return Math.ceil(this.totalOrders / this.pageSize)
    }
  },
  mounted() {
    this.getOrders()
  },
  beforeUnmount() {
    this.isDestroyed = true
    if (this.abortController) {
      this.abortController.abort()
      this.abortController = null
    }
  },
  methods: {
    async getOrders() {
      if (this.isDestroyed) return
      this.loading = true
      try {
        if (this.abortController) {
          this.abortController.abort()
        }
        this.abortController = new AbortController()
        const response = await axios.get('/api/user/orders', {
          params: {
            page: this.currentPage,
            size: this.pageSize
          },
          withCredentials: true,
          signal: this.abortController.signal
        })
        if (this.isDestroyed) return
        if (response.data && response.data.content) {
          this.orders = response.data.content
          this.totalOrders = response.data.totalElements || response.data.total
          if (this.orders.length === 0 && this.totalOrders > 0 && this.currentPage > 0) {
            const newTotalPages = Math.ceil(this.totalOrders / this.pageSize)
            this.currentPage = newTotalPages - 1
            await this.getOrders()
            return
          }
        } else {
          this.orders = response.data
          this.totalOrders = response.data.length
        }
      } catch (error) {
        if (axios.isCancel(error)) return
        console.error('获取订单失败:', error)
        if (!this.isDestroyed) {
          alert('获取订单失败，请稍后重试')
        }
      } finally {
        if (!this.isDestroyed) {
          this.loading = false
        }
      }
    },
    changePage(newPage) {
      this.currentPage = newPage
      this.jumpPage = this.currentPage + 1
      this.getOrders()
      // 滚动到顶部
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
    async cancelOrder(orderId) {
      if (confirm('确定要取消该订单吗？')) {
        try {
          const response = await axios.put(`/api/user/orders/${orderId}/cancel`, {}, { withCredentials: true })
          if (response.data) {
            alert('订单已取消')
            this.getOrders()
          }
        } catch (error) {
          console.error('取消订单失败:', error)
          alert('取消订单失败，请稍后重试')
        }
      }
    },
    payOrder(orderId) {
      // 跳转到支付页面
      this.$router.push(`/booking?orderId=${orderId}&pay=true`)
    },
    renewOrder(orderId) {
      // 跳转到续订页面
      this.$router.push(`/booking?renewOrderId=${orderId}`)
    },
    formatDateTime(dateString) {
      const date = new Date(dateString)
      return date.toLocaleString('zh-CN')
    },
    getStatusClass(status) {
      switch (status) {
        case '待支付':
          return 'status-warning'
        case '已支付':
        case '已预订':
          return 'status-info'
        case '已入住':
          return 'status-success'
        case '已完成':
        case '已退房':
        case '自动退房':
          return 'status-pending'
        case '已取消':
          return 'status-danger'
        default:
          return 'status-pending'
      }
    }
  }
}
</script>

<style scoped>
.orders {
  max-width: 1200px;
  margin: 0 auto;
}

.orders-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(340px, 1fr));
  gap: var(--space-lg);
}

.order {
  position: relative;
  overflow: hidden;
}

.order::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: var(--primary-gradient);
  border-radius: 4px 0 0 4px;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-lg);
  padding-bottom: var(--space-md);
  border-bottom: 1px solid var(--border-light);
}

.order-header h3 {
  margin: 0;
  color: var(--text-primary);
  font-size: 1.05rem;
  font-weight: 600;
}

.order-body p {
  margin-bottom: 0.5rem;
  color: var(--text-secondary);
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.order-body .label {
  color: var(--text-light);
  min-width: 70px;
  font-size: 0.85rem;
}

.order-body .price {
  color: var(--accent-color);
  font-weight: 600;
  font-size: 1rem;
}

.order-actions {
  display: flex;
  gap: 0.8rem;
  margin-top: var(--space-lg);
  padding-top: var(--space-md);
  border-top: 1px solid var(--border-light);
}

.page-info {
  color: var(--text-secondary);
  font-size: 0.9rem;
}

@media (max-width: 768px) {
  .orders-list {
    grid-template-columns: 1fr;
  }
  .order-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }
}
</style>