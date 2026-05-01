<template>
  <div class="front-service">
    <h2 class="page-title">前台服务</h2>
    <div class="tab-buttons">
      <button @click="activeTab = 'service'" :class="{ active: activeTab === 'service' }">呼叫/反馈</button>
      <button @click="activeTab = 'history'" :class="{ active: activeTab === 'history' }">呼叫/反馈记录</button>
    </div>

    <!-- 呼叫/反馈标签页 -->
    <div v-if="activeTab === 'service'" class="tab-content tab-pane-enter" :key="'service-' + activeTab">
      <div class="service-options">
        <div class="service-card" @click="showCallForm = true">
          <div class="service-icon">📞</div>
          <h3>呼叫前台</h3>
          <p>需要前台协助？点击呼叫前台</p>
        </div>
        <div class="service-card" @click="showFeedbackForm = true">
          <div class="service-icon">📝</div>
          <h3>问题反馈</h3>
          <p>有任何问题或建议？提交反馈</p>
        </div>
      </div>
    </div>

    <!-- 呼叫/反馈记录标签页 -->
    <div v-if="activeTab === 'history'" class="tab-content tab-pane-enter" :key="'history-' + activeTab">
      <LoadingSpinner v-if="loading" variant="user" />
       
        <div v-else class="history-list">
        <div v-if="myServiceLogs.length === 0" class="empty">
          <svg class="empty-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"/></svg>
          <p>暂无呼叫/反馈记录</p>
        </div>
        <div v-for="log in myServiceLogs" :key="log.id" class="history-item">
          <div class="history-header">
            <h4>{{ log.type }}</h4>
            <span class="status-badge" :class="log.status === '待处理' ? 'status-warning' : 'status-success'">
              {{ log.status }}
            </span>
          </div>
          <p class="history-content">{{ log.content }}</p>
          <p class="history-time">时间: {{ formatDate(log.createTime) }}</p>
          
          <!-- 评价展示区 -->
          <div v-if="log.rating" class="review-display">
            <div class="rating-stars">
              <span v-for="i in 5" :key="i" class="star" :class="{ filled: i <= log.rating }">★</span>
              <span class="rating-text">{{ log.rating }} 分</span>
            </div>
            <p v-if="log.review" class="review-content">{{ log.review }}</p>
          </div>
          
          <!-- 评价按钮 - 仅在已处理且未评价时显示 -->
          <div v-if="log.status === '已处理' && !log.rating" class="review-section">
            <button @click="openReviewModal(log)" class="btn btn-review">评价服务</button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 评价表单弹窗 -->
    <transition name="modal">
    <div v-if="showReviewModal" class="modal-overlay">
      <div class="modal-content review-form-content">
        <h3>评价服务</h3>
        
        <div class="rating-section">
          <label class="rating-label">请选择评分</label>
          <div class="star-rating" @mouseleave="hoverRating = 0">
            <span 
              v-for="i in 5" 
              :key="i" 
              class="star"
              :class="{ 
                filled: (hoverRating || reviewForm.rating) >= i,
                hover: hoverRating === i 
              }"
              @mouseenter="hoverRating = i"
              @click="reviewForm.rating = i"
            >★</span>
          </div>
          <p v-if="!reviewForm.rating && showRatingError" class="error-text">请选择评分</p>
        </div>
        
        <div class="form-group">
          <label for="reviewContent">评价内容 (选填，最多500字)</label>
          <textarea 
            id="reviewContent" 
            v-model="reviewForm.content" 
            maxlength="500"
            placeholder="请写下您的评价..."
          ></textarea>
          <div class="char-counter">{{ reviewForm.content.length }}/500</div>
        </div>
        
        <div class="modal-actions">
          <button type="button" @click="closeReviewModal" class="btn btn-ghost">取消</button>
          <button 
            type="button" 
            @click="submitReview" 
            class="btn btn-confirm"
            :disabled="submitting"
          >
            {{ submitting ? '提交中...' : '提交评价' }}
          </button>
        </div>
      </div>
    </div>
    </transition>
    
    <transition name="modal">
    <div v-if="showCallForm" class="modal-overlay">
      <div class="modal-content">
        <h3>呼叫前台</h3>
        <form @submit.prevent="submitCall">
          <div class="form-group">
            <label for="callContent">呼叫内容</label>
            <textarea id="callContent" v-model="callForm.content" required></textarea>
          </div>
          <div class="modal-actions">
            <button type="button" @click="showCallForm = false" class="btn btn-ghost">取消</button>
            <button type="submit" class="btn btn-confirm">提交</button>
          </div>
        </form>
      </div>
    </div>
    </transition>
    
    <transition name="modal">
    <div v-if="showFeedbackForm" class="modal-overlay">
      <div class="modal-content">
        <h3>问题反馈</h3>
        <form @submit.prevent="submitFeedback">
          <div class="form-group">
            <label for="feedbackType">反馈类型</label>
            <select id="feedbackType" v-model="feedbackForm.type" required>
              <option value="客房问题">客房问题</option>
              <option value="服务问题">服务问题</option>
              <option value="其他">其他</option>
            </select>
          </div>
          <div class="form-group">
            <label for="feedbackContent">反馈内容</label>
            <textarea id="feedbackContent" v-model="feedbackForm.content" required></textarea>
          </div>
          <div class="modal-actions">
            <button type="button" @click="showFeedbackForm = false" class="btn btn-ghost">取消</button>
            <button type="submit" class="btn btn-confirm">提交</button>
          </div>
        </form>
      </div>
    </div>
    </transition>
  </div>
</template>

<script>
import LoadingSpinner from '../components/LoadingSpinner.vue'

export default {
  name: 'FrontService',
  components: {
    LoadingSpinner
  },
  data() {
    return {
      activeTab: 'service',
      showCallForm: false,
      showFeedbackForm: false,
      callForm: {
        content: ''
      },
      feedbackForm: {
        type: '客房问题',
        content: ''
      },
      serviceLogs: [],
      loading: false,
      showReviewModal: false,
      currentReviewLog: null,
      reviewForm: {
        rating: 0,
        content: ''
      },
      hoverRating: 0,
      submitting: false,
      showRatingError: false,
      abortController: null,
      isDestroyed: false
    }
  },
  computed: {
    myServiceLogs() {
      if (!this.serviceLogs || this.serviceLogs.length === 0) {
        return []
      }
      const userStr = sessionStorage.getItem('user')
      if (!userStr) {
        return []
      }
      const user = JSON.parse(userStr)
      return this.serviceLogs
        .filter(log => log.user?.id === user.id && log.type !== '订单评价')
        .sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
    }
  },
  mounted() {
    this.getServiceLogs()
  },
  beforeUnmount() {
    this.isDestroyed = true
    if (this.abortController) {
      this.abortController.abort()
      this.abortController = null
    }
  },
  methods: {
    async getServiceLogs() {
      if (this.isDestroyed) return
      this.loading = true
      try {
        if (this.abortController) {
          this.abortController.abort()
        }
        this.abortController = new AbortController()
        const response = await fetch('/api/admin/service-logs?page=0&size=100', {
          method: 'GET',
          credentials: 'include',
          headers: {
            'Content-Type': 'application/json'
          },
          signal: this.abortController.signal
        })
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`)
        }
        const data = await response.json()
        if (!this.isDestroyed) {
          this.serviceLogs = data.content || data
        }
      } catch (error) {
        if (error.name === 'AbortError') return
        console.error('获取服务日志失败:', error)
      } finally {
        if (!this.isDestroyed) {
          this.loading = false
        }
      }
    },
    async submitCall() {
      if (this.isDestroyed) return
      try {
        const user = JSON.parse(sessionStorage.getItem('user'))
        const response = await fetch('/api/admin/service-logs', {
          method: 'POST',
          credentials: 'include',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            type: '客房呼叫',
            content: this.callForm.content,
            status: '待处理',
            userId: user.id
          })
        })
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`)
        }
        const data = await response.json()
        if (data && !this.isDestroyed) {
          alert('呼叫前台成功')
          this.showCallForm = false
          this.callForm.content = ''
          this.getServiceLogs()
        }
      } catch (error) {
        console.error('呼叫前台失败:', error)
        if (!this.isDestroyed) {
          alert('呼叫前台失败，请稍后重试: ' + error.message)
        }
      }
    },
    async submitFeedback() {
      if (this.isDestroyed) return
      try {
        const user = JSON.parse(sessionStorage.getItem('user'))
        const response = await fetch('/api/admin/service-logs', {
          method: 'POST',
          credentials: 'include',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            type: '客房反馈',
            content: this.feedbackForm.content,
            status: '待处理',
            userId: user.id
          })
        })
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`)
        }
        const data = await response.json()
        if (data && !this.isDestroyed) {
          alert('问题反馈成功')
          this.showFeedbackForm = false
          this.feedbackForm.content = ''
          this.getServiceLogs()
        }
      } catch (error) {
        console.error('问题反馈失败:', error)
        if (!this.isDestroyed) {
          alert('问题反馈失败，请稍后重试: ' + error.message)
        }
      }
    },
    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleString('zh-CN')
    },
    // 打开评价弹窗
    openReviewModal(log) {
      this.currentReviewLog = log
      this.reviewForm = {
        rating: 0,
        content: ''
      }
      this.hoverRating = 0
      this.showRatingError = false
      this.submitting = false
      this.showReviewModal = true
    },
    // 关闭评价弹窗
    closeReviewModal() {
      this.showReviewModal = false
      this.currentReviewLog = null
      this.reviewForm = {
        rating: 0,
        content: ''
      }
    },
    // 提交评价
    async submitReview() {
      // 验证评分
      if (this.reviewForm.rating < 1 || this.reviewForm.rating > 5) {
        this.showRatingError = true
        return
      }
      
      this.submitting = true
      try {
        const response = await fetch(`/api/admin/service-logs/${this.currentReviewLog.id}/review`, {
          method: 'PUT',
          credentials: 'include',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            rating: this.reviewForm.rating,
            review: this.reviewForm.content
          })
        })
        
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`)
        }
        
        const data = await response.json()
        console.log('评价提交成功:', data)
        
        alert('评价提交成功！')
        this.closeReviewModal()
        // 重新获取服务日志
        await this.getServiceLogs()
        
      } catch (error) {
        console.error('评价提交失败:', error)
        alert('评价提交失败，请稍后重试: ' + error.message)
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style scoped>
.front-service {
  max-width: 1000px;
  margin: 0 auto;
}

.tab-content {
  background-color: var(--bg-white);
  padding: 2rem;
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-light);
  box-shadow: var(--shadow-sm);
}

.service-options {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: var(--space-xl);
  margin-bottom: 2rem;
}

.service-card {
  background-color: var(--bg-white);
  padding: 2rem;
  border-radius: var(--radius-lg);
  text-align: center;
  cursor: pointer;
  border: 2px solid var(--border-light);
  position: relative;
  overflow: hidden;
  transition: all var(--transition);
}

.service-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.service-card h3 {
  font-size: 1.3rem;
  margin-bottom: 0.8rem;
  color: var(--text-primary);
  font-weight: 600;
}

.service-card p {
  color: var(--text-secondary);
  margin: 0;
  font-size: 0.95rem;
}

.service-card:hover {
  border-color: var(--primary-color);
  box-shadow: var(--shadow-xs);
}

.service-card:active {
  transform: scale(0.97);
  box-shadow: none;
  transition-duration: 0.1s;
}

.loading {
  text-align: center;
  padding: 2rem;
  color: var(--text-light);
}

.history-list {
  max-height: 600px;
  overflow-y: auto;
}

.history-item {
  padding: 1.5rem;
  border-bottom: 1px solid var(--border-light);
  margin-bottom: 1rem;
  background-color: var(--bg-white);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-light);
}

.history-item:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.history-header h4 {
  margin: 0;
  color: var(--text-primary);
  font-size: 1.05rem;
  font-weight: 600;
}

.history-content {
  color: var(--text-secondary);
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
}

.history-time {
  color: var(--text-light);
  font-size: 0.85rem;
  margin: 0;
}

.review-display {
  margin-top: 1rem;
  padding: 1rem;
  background-color: var(--status-warning-bg);
  border-radius: var(--radius-sm);
  border-left: 4px solid var(--gold);
}

.rating-stars {
  display: flex;
  align-items: center;
  gap: 0.3rem;
  margin-bottom: 0.5rem;
}

.rating-stars .star {
  font-size: 1.5rem;
  color: var(--border-color);
}

.rating-stars .star.filled {
  color: var(--gold);
}

.rating-text {
  margin-left: 0.5rem;
  color: var(--text-secondary);
  font-weight: 500;
  font-size: 0.9rem;
}

.review-content {
  color: var(--text-primary);
  margin: 0;
  line-height: 1.5;
  font-size: 0.9rem;
}

.review-section {
  margin-top: 1rem;
  display: flex;
  justify-content: flex-start;
}

.review-form-content {
  max-width: 450px;
}

.rating-section {
  margin-bottom: 1.5rem;
}

.rating-label {
  display: block;
  margin-bottom: 0.5rem;
  color: var(--text-primary);
  font-weight: 500;
  font-size: 0.9rem;
}

.star-rating {
  display: flex;
  gap: 0.3rem;
  padding: 0.5rem 0;
}

.star-rating .star {
  font-size: 2.5rem;
  color: var(--border-color);
  cursor: pointer;
}

.star-rating .star.filled {
  color: var(--gold);
}

.error-text {
  color: var(--status-danger);
  font-size: 0.85rem;
  margin-top: 0.3rem;
}

.char-counter {
  text-align: right;
  color: var(--text-light);
  font-size: 0.85rem;
  margin-top: 0.3rem;
}

.form-group textarea,
.form-group select {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 1.5px solid var(--border-color);
  border-radius: var(--radius-sm);
  font-size: 0.95rem;
  background: var(--bg-white);
  color: var(--text-primary);
  box-sizing: border-box;
}

.form-group textarea:focus,
.form-group select:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(91, 155, 213, 0.15);
}

.form-group textarea {
  resize: vertical;
  min-height: 100px;
}

@media (max-width: 640px) {
  .service-options {
    grid-template-columns: 1fr;
  }
  
  .star-rating .star {
    font-size: 2rem;
  }
}
</style>
