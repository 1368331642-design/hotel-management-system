<template>
  <div class="orders">
    <h2>我的订单</h2>
    <div v-if="loading" class="loading">
      <p>加载中...</p>
    </div>
    <div v-else>
      <div v-if="orders.length === 0" class="no-orders">
        <p>暂无订单</p>
      </div>
      <div v-else class="orders-list">
        <div v-for="order in orders" :key="order.id" class="order">
          <h3>订单号: {{ order.orderNumber }}</h3>
          <p>房间号: {{ order.room.roomNumber }}</p>
          <p>房型: {{ order.room.roomType.name }}</p>
          <p>入住日期: {{ formatDateTime(order.checkInTime) }}</p>
          <p>退房日期: {{ formatDateTime(order.checkOutTime) }}</p>
          <p>总价格: ¥{{ order.totalPrice }}</p>
          <p class="status" :class="getStatusClass(order.status)">{{ order.status }}</p>
          <div class="order-actions">
            <button v-if="order.status === '待支付'" @click="cancelOrder(order.id)" class="btn btn-cancel">取消订单</button>
            <button v-if="order.status === '待支付'" @click="payOrder(order.id)" class="btn btn-pay">去支付</button>
            <button v-if="order.status === '已入住' || order.status === '已预订'" @click="renewOrder(order.id)" class="btn btn-renew">续订</button>
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
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'Orders',
  data() {
    return {
      orders: [],
      loading: true,
      currentPage: 0,
      pageSize: 10,
      totalOrders: 0
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
  methods: {
    async getOrders() {
      this.loading = true
      try {
        const response = await axios.get('/api/user/orders', {
          params: {
            page: this.currentPage,
            size: this.pageSize
          },
          withCredentials: true
        })
        // 处理 Page 对象格式
        if (response.data && response.data.content) {
          this.orders = response.data.content
          this.totalOrders = response.data.totalElements || response.data.total
        } else {
          // 兼容非分页格式
          this.orders = response.data
          this.totalOrders = response.data.length
        }
      } catch (error) {
        console.error('获取订单失败:', error)
        alert('获取订单失败，请稍后重试')
      } finally {
        this.loading = false
      }
    },
    changePage(newPage) {
      this.currentPage = newPage
      this.getOrders()
      // 滚动到顶部
      window.scrollTo({ top: 0, behavior: 'smooth' })
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
          return 'status-pending'
        case '已支付':
        case '已预订':
          return 'status-booked'
        case '已入住':
          return 'status-checked-in'
        case '已完成':
          return 'status-completed'
        case '已取消':
          return 'status-cancelled'
        default:
          return ''
      }
    }
  }
}
</script>

<style scoped>
.orders {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.loading {
  text-align: center;
  padding: 4rem;
  color: #666;
}

.no-orders {
  text-align: center;
  padding: 4rem;
  background-color: #f9f9f9;
  border-radius: 8px;
  color: #666;
}

.orders-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 2rem;
}

.order {
  padding: 1.5rem;
  background-color: #f9f9f9;
  border-radius: 8px;
  box-shadow: 0 0 5px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s;
}

.order:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.order h3 {
  margin-bottom: 1rem;
  color: #333;
  font-size: 1.2rem;
}

.order p {
  margin-bottom: 0.5rem;
  color: #666;
}

.status {
  font-weight: bold;
  margin: 1rem 0;
  padding: 0.5rem;
  border-radius: 4px;
  display: inline-block;
}

.status-pending {
  background-color: #fff3cd;
  color: #856404;
}

.status-booked {
  background-color: #d1ecf1;
  color: #0c5460;
}

.status-checked-in {
  background-color: #d4edda;
  color: #155724;
}

.status-completed {
  background-color: #e2e3e5;
  color: #383d41;
}

.status-cancelled {
  background-color: #f8d7da;
  color: #721c24;
}

.order-actions {
  display: flex;
  gap: 1rem;
  margin-top: 1.5rem;
}

.btn {
  padding: 0.6rem 1.2rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
  font-size: 0.95rem;
}

.btn-cancel {
  background-color: #f44336;
  color: white;
}

.btn-cancel:hover {
  background-color: #d32f2f;
}

.btn-pay {
  background-color: #2196f3;
  color: white;
}

.btn-pay:hover {
  background-color: #1976d2;
}

.btn-renew {
  background-color: #4caf50;
  color: white;
}

.btn-renew:hover {
  background-color: #388e3c;
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
  padding: 0.5rem 1rem;
  background-color: #2196f3;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.page-btn:hover:not(:disabled) {
  background-color: #1976d2;
}

.page-btn:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.page-info {
  color: #666;
  font-size: 0.95rem;
}
</style>