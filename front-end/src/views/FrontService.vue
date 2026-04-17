<template>
  <div class="front-service">
    <h2>前台服务</h2>
    
    <div class="service-tabs">
      <button @click="activeTab = 'service'" :class="{ active: activeTab === 'service' }">呼叫/反馈</button>
      <button @click="activeTab = 'history'" :class="{ active: activeTab === 'history' }">呼叫/反馈记录</button>
    </div>

    <!-- 呼叫/反馈标签页 -->
    <div v-if="activeTab === 'service'" class="tab-content">
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
    <div v-if="activeTab === 'history'" class="tab-content">
      <div v-if="loading" class="loading">
        <p>加载中...</p>
      </div>
      
      <div v-else class="history-list">
        <div v-if="myServiceLogs.length === 0" class="empty">
          <p>暂无呼叫/反馈记录</p>
        </div>
        <div v-for="log in myServiceLogs" :key="log.id" class="history-item">
          <div class="history-header">
            <h4>{{ log.type }}</h4>
            <span :class="['status-badge', log.status === '待处理' ? 'status-pending' : 'status-processed']">
              {{ log.status }}
            </span>
          </div>
          <p class="history-content">{{ log.content }}</p>
          <p class="history-time">时间: {{ formatDate(log.createTime) }}</p>
        </div>
      </div>
    </div>
    
    <!-- 呼叫前台表单 -->
    <div v-if="showCallForm" class="form-modal">
      <div class="form-content">
        <h3>呼叫前台</h3>
        <form @submit.prevent="submitCall">
          <div class="form-group">
            <label for="callContent">呼叫内容</label>
            <textarea id="callContent" v-model="callForm.content" required></textarea>
          </div>
          <div class="form-actions">
            <button type="button" @click="showCallForm = false" class="btn btn-secondary">取消</button>
            <button type="submit" class="btn">提交</button>
          </div>
        </form>
      </div>
    </div>
    
    <!-- 问题反馈表单 -->
    <div v-if="showFeedbackForm" class="form-modal">
      <div class="form-content">
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
          <div class="form-actions">
            <button type="button" @click="showFeedbackForm = false" class="btn btn-secondary">取消</button>
            <button type="submit" class="btn">提交</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'FrontService',
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
      loading: false
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
        .filter(log => log.user?.id === user.id)
        .sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
    }
  },
  mounted() {
    this.getServiceLogs()
  },
  methods: {
    async getServiceLogs() {
      this.loading = true
      console.log('开始获取服务日志...')
      try {
        const response = await fetch('/api/admin/service-logs?page=0&size=1000', {
          method: 'GET',
          credentials: 'include',
          headers: {
            'Content-Type': 'application/json'
          }
        })
        console.log('服务日志响应状态:', response.status)
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`)
        }
        const data = await response.json()
        console.log('服务日志响应数据:', data)
        this.serviceLogs = data.content || data
      } catch (error) {
        console.error('获取服务日志失败:', error)
      } finally {
        this.loading = false
      }
    },
    async submitCall() {
      try {
        // 获取当前用户信息
        const user = JSON.parse(sessionStorage.getItem('user'))
        
        console.log('提交呼叫前台，用户信息:', user)
        
        // 使用 fetch API 提交呼叫前台请求
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
        
        console.log('响应状态:', response.status)
        
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`)
        }
        
        const data = await response.json()
        console.log('响应数据:', data)
        
        if (data) {
          alert('呼叫前台成功')
          this.showCallForm = false
          this.callForm.content = ''
          this.getServiceLogs()
        }
      } catch (error) {
        console.error('呼叫前台失败:', error)
        alert('呼叫前台失败，请稍后重试: ' + error.message)
      }
    },
    async submitFeedback() {
      try {
        // 获取当前用户信息
        const user = JSON.parse(sessionStorage.getItem('user'))
        
        console.log('提交问题反馈，用户信息:', user)
        
        // 使用 fetch API 提交问题反馈请求
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
        
        console.log('响应状态:', response.status)
        
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`)
        }
        
        const data = await response.json()
        console.log('响应数据:', data)
        
        if (data) {
          alert('问题反馈成功')
          this.showFeedbackForm = false
          this.feedbackForm.content = ''
          this.getServiceLogs()
        }
      } catch (error) {
        console.error('问题反馈失败:', error)
        alert('问题反馈失败，请稍后重试: ' + error.message)
      }
    },
    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleString('zh-CN')
    }
  }
}
</script>

<style scoped>
.front-service {
  max-width: 1000px;
  margin: 0 auto;
}

.front-service h2 {
  text-align: center;
  margin-bottom: 2rem;
  color: #333;
}

.service-tabs {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
}

.service-tabs button {
  padding: 0.8rem 1.5rem;
  background-color: var(--bg-white);
  color: var(--text-color);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.service-tabs button.active {
  background-color: var(--primary-color);
  color: var(--text-white);
  border-color: var(--primary-color);
}

.tab-content {
  background-color: white;
  padding: 2rem;
  border-radius: 8px;
}

.service-options {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2rem;
  margin-bottom: 2rem;
}

.service-card {
  background-color: #f9f9f9;
  padding: 2rem;
  border-radius: 8px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.service-card:hover {
  background-color: #e9e9e9;
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.service-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.service-card h3 {
  font-size: 1.5rem;
  margin-bottom: 1rem;
  color: #333;
}

.service-card p {
  color: #666;
  margin: 0;
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

.history-list {
  max-height: 600px;
  overflow-y: auto;
}

.history-item {
  padding: 1.5rem;
  border-bottom: 1px solid #eee;
  margin-bottom: 1rem;
  background-color: #f9f9f9;
  border-radius: 8px;
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
  color: #333;
  font-size: 1.1rem;
}

.history-content {
  color: #666;
  margin-bottom: 0.5rem;
}

.history-time {
  color: #999;
  font-size: 0.9rem;
  margin: 0;
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

.form-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.form-content {
  background-color: white;
  padding: 2rem;
  border-radius: 8px;
  width: 90%;
  max-width: 500px;
}

.form-content h3 {
  margin-bottom: 1.5rem;
  color: #333;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: #333;
}

.form-group textarea,
.form-group select {
  width: 100%;
  padding: 0.8rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

.form-group textarea {
  resize: vertical;
  min-height: 100px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
}

.btn {
  padding: 0.6rem 1.2rem;
  background-color: var(--primary-color);
  color: var(--text-white);
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.95rem;
}

.btn:hover {
  background-color: #1a70a5;
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.btn-secondary {
  background-color: var(--bg-white);
  color: var(--text-color);
  border: 1px solid var(--border-color);
}

.btn-secondary:hover {
  background-color: var(--bg-light);
  border-color: var(--primary-color);
  color: var(--primary-color);
}
</style>
