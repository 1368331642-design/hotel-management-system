<template>
  <div class="profile">
    <h2 class="page-title">个人中心</h2>

    <!-- 未登录提示横幅 -->
    <div v-if="!isLoggedIn" class="login-prompt-banner">
      <div class="login-prompt-content">
        <span class="login-prompt-text">登录后可管理个人信息、查看我的订单</span>
        <button @click="goToLogin" class="login-prompt-btn">登录系统</button>
        <router-link to="/register" class="login-prompt-register">注册账号</router-link>
      </div>
    </div>

    <template v-else>
      <div class="profile-tabs">
      <button @click="activeTab = 'personalInfo'" :class="{ active: activeTab === 'personalInfo' }">个人信息</button>
      <button @click="activeTab = 'orderHistory'" :class="{ active: activeTab === 'orderHistory' }">我的订单</button>
    </div>

    <!-- 个人信息 -->
    <div v-if="activeTab === 'personalInfo'" class="tab-content tab-pane-enter" :key="activeTab">
      <div class="info-header">
        <h3 class="section-title">个人信息</h3>
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
    <div v-if="activeTab === 'orderHistory'" class="tab-content tab-pane-enter" :key="'order-' + activeTab">
      <h3 class="section-title">我的订单</h3>
      <div class="order-tabs">
        <button @click="orderTab = 'pending'" :class="{ active: orderTab === 'pending' }">待支付</button>
        <button @click="orderTab = 'current'" :class="{ active: orderTab === 'current' }">已支付</button>
        <button @click="orderTab = 'reviews'; loadMyReviews()" :class="{ active: orderTab === 'reviews' }">我的评价</button>
      </div>
      <div v-if="orderTab === 'current'" class="sub-order-tabs tab-pane-enter" :key="'subtab-' + orderTab + '-' + subOrderTab">
        <button @click="subOrderTab = 'uncheckin'" :class="{ active: subOrderTab === 'uncheckin' }">未入住订单</button>
        <button @click="subOrderTab = 'checkedin'" :class="{ active: subOrderTab === 'checkedin' }">已入住订单</button>
        <button @click="subOrderTab = 'history'" :class="{ active: subOrderTab === 'history' }">历史订单</button>
      </div>
      <!-- 批量删除按钮 -->
      <div v-if="orderTab === 'current' && subOrderTab === 'history' && historyOrders.length > 0" class="batch-actions">
        <template v-if="showSelectAll">
          <button @click="selectAllOrders" class="btn btn-sm btn-ghost">全选</button>
          <button @click="showBatchDeleteModal" class="btn btn-sm btn-danger" :disabled="selectedOrders.length === 0">
            批量删除<span v-if="selectedOrders.length > 0">（{{ selectedOrders.length }}）</span>
          </button>
          <button @click="toggleSelectAll" class="btn btn-sm btn-ghost">取消</button>
        </template>
        <button v-else @click="toggleSelectAll" class="btn btn-sm btn-danger-outline">批量删除</button>
      </div>
      <div v-if="orderTab !== 'reviews'" class="list tab-pane-enter" :key="'list-' + orderTab + '-' + subOrderTab">
        <div v-if="subOrderTab === 'history' && historyLoading" class="loading">
          <div class="loading-spinner"></div>
          <p>正在加载数据...</p>
        </div>
        <div v-else-if="subOrderTab === 'history' && historyLoadError" class="error-state">
          <p>加载失败，请重试</p>
          <button @click="getOrders" class="retry-btn">重新加载</button>
        </div>
        <template v-else>
          <div v-if="subOrderTab === 'history' && paginatedHistoryOrders.length === 0" class="empty">
            <svg class="empty-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M9 12h6m-6 4h6m-2-12h4a2 2 0 012 2v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6a2 2 0 012-2z" />
            </svg>
            <p>暂无历史订单</p>
          </div>
          <div v-for="order in (subOrderTab === 'history' ? paginatedHistoryOrders : filteredOrders)" v-else :key="order.id" class="item" :class="{ 'has-checkbox': showSelectAll && orderTab === 'current' && subOrderTab === 'history' }">
            <div v-if="showSelectAll && orderTab === 'current' && subOrderTab === 'history'" class="checkbox-wrapper">
              <input type="checkbox" :value="order.id" v-model="selectedOrders">
            </div>
            <div class="order-body">
              <div class="order-content">
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
              </div>
            <div v-if="orderTab === 'current' && subOrderTab === 'history'" class="delete-button-wrapper">
              <button v-if="isOrderReviewed(order)" disabled class="btn btn-reviewed-disabled">已评价</button>
              <button v-else @click="openOrderReviewModal(order)" class="btn btn-review-small">评价</button>
              <button @click="showDeleteModal(order.id)" class="btn btn-delete-small">删除订单</button>
            </div>
          </div>
        </template>
        <div v-if="subOrderTab !== 'history' && filteredOrders.length === 0" class="empty">
          <svg class="empty-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M9 12h6m-6 4h6m2-12H7a2 2 0 00-2 2v14a2 2 0 002 2h10a2 2 0 002-2V6a2 2 0 00-2-2z"/></svg>
          <p v-if="orderTab === 'pending'">暂无待支付的订单</p>
          <p v-else-if="orderTab === 'current' && subOrderTab === 'uncheckin'">暂无未入住的订单</p>
          <p v-else-if="orderTab === 'current' && subOrderTab === 'checkedin'">暂无已入住的订单</p>
          <p v-else>暂无订单</p>
        </div>
      </div>

      <!-- 历史订单分页 -->
      <div v-if="orderTab !== 'reviews' && subOrderTab === 'history' && historyTotalElements > 0" class="pagination-wrapper">
        <div class="pagination-top-row">
          <div class="pagination-info">
            共 {{ historyTotalElements }} 条记录，第 {{ historyCurrentPage }} / {{ historyTotalPages }} 页·每页 5 条
          </div>
        </div>
        <div class="pagination">
          <button @click="historyGoToPage(1)" :disabled="!historyHasPrevPage" class="page-btn" title="第一页">«</button>
          <button @click="historyGoToPrevPage" :disabled="!historyHasPrevPage" class="page-btn">上一页</button>

          <div class="page-numbers">
            <template v-for="(page, idx) in historyVisiblePages" :key="idx">
              <span v-if="page === -1" class="ellipsis">...</span>
              <button
                v-else
                @click="historyGoToPage(page)"
                :class="['page-number', { active: historyCurrentPage === page }]"
              >
                {{ page }}
              </button>
            </template>
          </div>

          <button @click="historyGoToNextPage" :disabled="!historyHasNextPage" class="page-btn">下一页</button>
          <button @click="historyGoToPage(historyTotalPages)" :disabled="!historyHasNextPage" class="page-btn" title="最后一页">»</button>
        </div>

        <div class="jump-page">
          <span>跳至</span>
          <input type="number" v-model.number="historyJumpPage" @keyup.enter="historyHandleJumpPage" min="1" :max="historyTotalPages" />
          <span>页</span>
          <button @click="historyHandleJumpPage" class="jump-btn">跳转</button>
        </div>
      </div>

      <!-- 我的评价 -->
      <div v-if="orderTab === 'reviews'" class="reviews-panel tab-pane-enter" :key="'reviews-' + activeTab">
        <div v-if="myReviews.length === 0 && !loadingReviews" class="empty">
          <svg class="empty-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M11.049 2.927c.3-.921 1.603-.921 1.902 0l1.519 4.674a1 1 0 00.95.69h4.915c.969 0 1.371 1.24.588 1.81l-3.976 2.888a1 1 0 00-.363 1.118l1.518 4.674c.3.922-.755 1.688-1.538 1.118l-3.976-2.888a1 1 0 00-1.176 0l-3.976 2.888c-.783.57-1.838-.197-1.538-1.118l1.518-4.674a1 1 0 00-.363-1.118l-3.976-2.888c-.784-.57-.38-1.81.588-1.81h4.914a1 1 0 00.951-.69l1.519-4.674z"/></svg>
          <p>暂无评价记录</p>
        </div>
        <div v-else-if="loadingReviews" class="loading">
          <p>加载中...</p>
        </div>
        <div v-else class="reviews-list">
          <div v-for="review in paginatedReviews" :key="review.id" class="review-card">
            <div class="review-card-header">
              <span class="review-type-badge">订单评价</span>
              <span class="review-time">{{ formatDate(review.createTime) }}</span>
            </div>
            <div class="review-card-body">
              <p class="review-content-text">{{ review.content }}</p>
            </div>
            <div v-if="review.rating" class="review-card-footer">
              <div class="review-rating-display">
                <span v-for="i in 5" :key="i" class="star" :class="{ filled: i <= review.rating }">★</span>
                <span class="rating-num">{{ review.rating }} 分</span>
              </div>
              <p v-if="review.review" class="review-text">"{{ review.review }}"</p>
            </div>
            <!-- 评价图片 -->
            <div v-if="parseReviewImages(review.images).length > 0" class="review-images">
              <img v-for="(img, idx) in parseReviewImages(review.images)" :key="idx"
                :src="img" alt="评价图片" class="review-thumb" />
            </div>
          </div>
        </div>

        <!-- 评价分页 -->
        <div v-if="myReviews.length > reviewPageSize" class="reviews-pagination-wrapper">
          <div class="pagination-info">
            共 {{ myReviews.length }} 条评价，第 {{ reviewCurrentPage }} / {{ reviewTotalPages }} 页·每页 {{ reviewPageSize }} 条
          </div>
          <div class="pagination">
            <button @click="reviewGoToPage(1)" :disabled="!reviewHasPrevPage" class="page-btn" title="第一页">«</button>
            <button @click="reviewGoToPrevPage" :disabled="!reviewHasPrevPage" class="page-btn">上一页</button>
            <div class="page-numbers">
              <template v-for="(page, idx) in reviewVisiblePages" :key="idx">
                <span v-if="page === -1" class="ellipsis">...</span>
                <button
                  v-else
                  @click="reviewGoToPage(page)"
                  :class="['page-number', { active: reviewCurrentPage === page }]"
                >
                  {{ page }}
                </button>
              </template>
            </div>
            <button @click="reviewGoToNextPage" :disabled="!reviewHasNextPage" class="page-btn">下一页</button>
            <button @click="reviewGoToPage(reviewTotalPages)" :disabled="!reviewHasNextPage" class="page-btn" title="最后一页">»</button>
          </div>
          <div class="jump-page">
            <span>跳至</span>
            <input type="number" v-model.number="reviewJumpPage" @keyup.enter="reviewHandleJumpPage" min="1" :max="reviewTotalPages" />
            <span>页</span>
            <button @click="reviewHandleJumpPage" class="jump-btn">跳转</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 修改密码弹窗 -->
    <transition name="modal">
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
    </transition>

    <!-- 删除订单确认弹窗 -->
    <transition name="modal">
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
    </transition>

    <!-- 批量删除订单确认弹窗 -->
    <transition name="modal">
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
    </transition>

    <!-- 取消预定确认弹窗 -->
    <transition name="modal">
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
    </transition>
    <!-- 订单评价弹窗 -->
    <transition name="modal">
    <div v-if="orderReviewModalVisible" class="modal-overlay" @click.self="closeOrderReviewModal">
      <div class="modal-content review-modal-content">
        <h3>评价订单</h3>
        <p class="review-order-info">{{ currentReviewOrder?.room?.roomType?.name }} - {{ currentReviewOrder?.room?.roomNumber }}</p>
        <div class="rating-section">
          <label class="rating-label">评分</label>
          <div class="star-rating" @mouseleave="hoverRating = 0">
            <span v-for="i in 5" :key="i" class="star"
              :class="{ filled: (hoverRating || orderReviewForm.rating) >= i }"
              @mouseenter="hoverRating = i"
              @click="orderReviewForm.rating = i">★</span>
          </div>
          <p v-if="!orderReviewForm.rating && showOrderRatingError" class="error-text">请选择评分</p>
        </div>
        <div class="form-group">
          <label for="orderReviewContent">评价内容（选填，最多500字）</label>
          <textarea id="orderReviewContent" v-model="orderReviewForm.content" maxlength="500" placeholder="请写下您的评价..."></textarea>
          <div class="char-counter">{{ orderReviewForm.content.length }}/500</div>
        </div>
        <!-- 图片上传 -->
        <div class="form-group">
          <label>上传图片（选填，最多5张）</label>
          <div class="image-upload-area">
            <div v-for="(img, idx) in reviewImages" :key="idx" class="image-preview-item">
              <img :src="img.preview" alt="预览" class="preview-img" />
              <button @click="removeReviewImage(idx)" class="btn-remove-img" title="删除">×</button>
              <div v-if="img.uploading" class="upload-progress">
                <div class="progress-bar">
                  <div class="progress-fill" :style="{ width: img.progress + '%' }"></div>
                </div>
              </div>
            </div>
            <label v-if="reviewImages.length < 5" class="image-add-btn" :class="{ disabled: uploadingImages }">
              <input type="file" accept="image/jpeg,image/png,image/gif,image/webp" multiple
                @change="handleReviewImageSelect" :disabled="uploadingImages" style="display:none" />
              <span class="add-icon">+</span>
              <span class="add-text">添加图片</span>
            </label>
          </div>
          <p v-if="uploadError" class="error-text">{{ uploadError }}</p>
          <p class="upload-hint">支持 jpg/png/gif/webp，单张≤5MB</p>
        </div>
        <div class="modal-actions">
          <button @click="closeOrderReviewModal" class="btn btn-cancel">取消</button>
          <button @click="submitOrderReview" class="btn btn-confirm" :disabled="orderReviewSubmitting">
            {{ orderReviewSubmitting ? '提交中...' : '提交评价' }}
          </button>
        </div>
      </div>
    </div>
    </transition>
    </template>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'Profile',
  data() {
    return {
      isLoggedIn: false,
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
      historyCurrentPage: 1,
      historyPageSize: 5,
      historyTotalElements: 0,
      historyJumpPage: 1,
      historyLoading: false,
      historyLoadError: false,
      passwordForm: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      errorMessage: '',
      successMessage: '',
      countdownTimer: null,
      countdownNow: Date.now(),
      deleteModalVisible: false,
      currentDeleteOrderId: null,
      batchDeleteModalVisible: false,
      cancelModalVisible: false,
      currentCancelOrderId: null,
      abortController: null,
      isDestroyed: false,
      // 订单评价
      orderReviewModalVisible: false,
      currentReviewOrder: null,
      orderReviewForm: { rating: 0, content: '' },
      hoverRating: 0,
      orderReviewSubmitting: false,
      showOrderRatingError: false,
      // 评价图片
      reviewImages: [],
      uploadingImages: false,
      uploadError: '',
      // 我的评价
      myReviews: [],
      loadingReviews: false,
      // 评价分页
      reviewCurrentPage: 1,
      reviewPageSize: 5,
      reviewJumpPage: 1
    }
  },
  watch: {
    subOrderTab(val) {
      if (val === 'history') {
        this.loadMyReviews()
        this.historyCurrentPage = 1
        this.historyJumpPage = 1
      }
    },
    orderTab(val) {
      if (val === 'reviews') {
        this.reviewCurrentPage = 1
        this.reviewJumpPage = 1
      }
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
          // 未入住订单：已预订、已支付（不包括已入住、已退房、自动退房）
          filtered = this.orders.filter(order => 
            (order.status === '已预订' || order.status === '已支付') && order.status !== '已入住' && order.status !== '已退房' && order.status !== '自动退房'
          )
        } else if (this.subOrderTab === 'checkedin') {
          // 已入住订单
          filtered = this.orders.filter(order => 
            order.status === '已入住'
          )
        } else {
          // 历史订单：已完成、已取消、已退房、自动退房
          filtered = this.orders.filter(order => 
            order.status === '已完成' || order.status === '已取消' || order.status === '已退房' || order.status === '自动退房'
          )
        }
      }
      // 按创建时间倒序排序，最新的在上面
      return filtered.sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
    },
    historyOrders() {
      if (this.orderTab !== 'current' || this.subOrderTab !== 'history') return []
      return [...this.filteredOrders].sort((a, b) => {
        const timeA = a.checkOutTime ? new Date(a.checkOutTime).getTime() : new Date(a.createTime).getTime()
        const timeB = b.checkOutTime ? new Date(b.checkOutTime).getTime() : new Date(b.createTime).getTime()
        return timeB - timeA
      })
    },
    historyTotalPages() {
      return Math.ceil(this.historyOrders.length / this.historyPageSize) || 1
    },
    paginatedHistoryOrders() {
      const totalPages = this.historyTotalPages
      if (this.historyCurrentPage > totalPages) {
        this.historyCurrentPage = Math.max(1, totalPages)
      }
      this.historyTotalElements = this.historyOrders.length
      const start = (this.historyCurrentPage - 1) * this.historyPageSize
      const end = start + this.historyPageSize
      return this.historyOrders.slice(start, end)
    },
    historyHasPrevPage() {
      return this.historyCurrentPage > 1
    },
    historyHasNextPage() {
      return this.historyCurrentPage < this.historyTotalPages
    },
    historyVisiblePages() {
      const pages = []
      let start = Math.max(1, this.historyCurrentPage - 2)
      let end = Math.min(this.historyTotalPages, this.historyCurrentPage + 2)

      if (start > 1) {
        pages.push(1)
        if (start > 2) {
          pages.push(-1)
        }
      }

      for (let i = start; i <= end; i++) {
        pages.push(i)
      }

      if (end < this.historyTotalPages) {
        if (end < this.historyTotalPages - 1) {
          pages.push(-1)
        }
        pages.push(this.historyTotalPages)
      }

      return pages
    },
    // 评价分页
    reviewTotalPages() {
      return Math.ceil(this.myReviews.length / this.reviewPageSize) || 1
    },
    paginatedReviews() {
      const totalPages = this.reviewTotalPages
      if (this.reviewCurrentPage > totalPages) {
        this.reviewCurrentPage = Math.max(1, totalPages)
      }
      const start = (this.reviewCurrentPage - 1) * this.reviewPageSize
      const end = start + this.reviewPageSize
      return this.myReviews.slice(start, end)
    },
    reviewHasPrevPage() {
      return this.reviewCurrentPage > 1
    },
    reviewHasNextPage() {
      return this.reviewCurrentPage < this.reviewTotalPages
    },
    reviewVisiblePages() {
      const pages = []
      let start = Math.max(1, this.reviewCurrentPage - 2)
      let end = Math.min(this.reviewTotalPages, this.reviewCurrentPage + 2)
      if (start > 1) {
        pages.push(1)
        if (start > 2) pages.push(-1)
      }
      for (let i = start; i <= end; i++) pages.push(i)
      if (end < this.reviewTotalPages) {
        if (end < this.reviewTotalPages - 1) pages.push(-1)
        pages.push(this.reviewTotalPages)
      }
      return pages
    }
  },
  created() {
    // 在组件渲染前检查登录状态，避免白屏
    this.checkLoginStatus()
  },
  mounted() {
    // 已登录才执行数据加载等操作
    if (!this.isLoggedIn) return

    // 页面加载时初始化用户信息
    this.initUserInfo()
    this.getOrders()
    const tab = this.$route.query.tab
    if (tab === 'orderHistory') {
      this.activeTab = 'orderHistory'
      this.orderTab = 'current'
    }
    const subTab = this.$route.query.subTab
    if (subTab === 'history') {
      this.subOrderTab = 'history'
      this.historyCurrentPage = 1
      this.historyJumpPage = 1
    }
    this.startCountdownTimer()
  },
  beforeUnmount() {
    this.isDestroyed = true
    if (this.abortController) {
      this.abortController.abort()
      this.abortController = null
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
    // 检查登录状态 - 在 created 中调用，确保首次渲染前状态正确
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
        if (response.data && response.data.success) {
          alert('个人信息更新成功')
          const updatedUser = response.data.user
          if (updatedUser) {
            this.userInfo = { ...this.userInfo, ...updatedUser }
          } else {
            this.userInfo = { ...this.userInfo, ...this.editForm }
          }
          sessionStorage.setItem('user', JSON.stringify(this.userInfo))
          this.isEditing = false
        } else {
          alert(response.data.message || '更新失败，请稍后重试')
        }
      } catch (error) {
        console.error('更新个人信息失败:', error)
        alert('更新个人信息失败，请稍后重试')
      }
    },
    async getOrders() {
      if (this.isDestroyed) return
      try {
        if (this.abortController) {
          this.abortController.abort()
        }
        this.abortController = new AbortController()
        const response = await axios.get('/api/user/orders', {
          params: {
            page: 0,
            size: 100
          },
          withCredentials: true,
          signal: this.abortController.signal
        })
        if (this.isDestroyed) return
        if (this.userInfo && this.userInfo.id) {
          const ordersData = response.data.content || response.data
          let userOrders = ordersData.filter(order => order.user && order.user.id === this.userInfo.id)
          const orderMap = new Map()
          userOrders.forEach(order => {
            const key = `${order.room?.id}-${order.checkInTime}-${order.checkOutTime}`
            if (!orderMap.has(key) || new Date(order.createTime) > new Date(orderMap.get(key).createTime)) {
              orderMap.set(key, order)
            }
          })
          this.orders = Array.from(orderMap.values())
        }
      } catch (error) {
        if (axios.isCancel(error)) return
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
      if (confirm('确定要提前退房吗？退房后订单将移至历史订单。')) {
        try {
          const response = await axios.put(`/api/user/orders/${orderId}/status?status=已完成`, {}, { withCredentials: true })
          if (response.data) {
            alert('退房成功！订单已移至历史订单。')
            this.subOrderTab = 'history'
            this.getOrders()
          }
        } catch (error) {
          console.error('提前退房失败:', error)
          alert('提前退房失败，请稍后重试')
        }
      }
    },
    historyGoToPage(page) {
      if (page < 1 || page > this.historyTotalPages) return
      this.historyLoading = true
      this.historyCurrentPage = page
      this.historyJumpPage = page
      this.$nextTick(() => {
        this.historyLoading = false
      })
    },
    historyGoToPrevPage() {
      if (this.historyHasPrevPage) {
        this.historyGoToPage(this.historyCurrentPage - 1)
      }
    },
    historyGoToNextPage() {
      if (this.historyHasNextPage) {
        this.historyGoToPage(this.historyCurrentPage + 1)
      }
    },
    historyHandleJumpPage() {
      let page = parseInt(this.historyJumpPage)
      if (isNaN(page) || page < 1 || page > this.historyTotalPages) {
        this.historyJumpPage = this.historyCurrentPage
        return
      }
      this.historyGoToPage(page)
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
        this.getOrders()
      }
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
      const orders = this.subOrderTab === 'history' ? this.paginatedHistoryOrders : this.filteredOrders
      if (this.selectedOrders.length === orders.length) {
        this.selectedOrders = []
      } else {
        this.selectedOrders = orders.map(order => order.id)
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
    },
    // 订单评价
    openOrderReviewModal(order) {
      this.currentReviewOrder = order
      this.orderReviewForm = { rating: 0, content: '' }
      this.hoverRating = 0
      this.showOrderRatingError = false
      this.reviewImages = []
      this.uploadError = ''
      this.orderReviewModalVisible = true
    },
    closeOrderReviewModal() {
      this.orderReviewModalVisible = false
      this.currentReviewOrder = null
      this.orderReviewForm = { rating: 0, content: '' }
      this.hoverRating = 0
      this.showOrderRatingError = false
      this.reviewImages = []
      this.uploadError = ''
    },
    async submitOrderReview() {
      if (this.orderReviewForm.rating < 1 || this.orderReviewForm.rating > 5) {
        this.showOrderRatingError = true
        return
      }
      this.orderReviewSubmitting = true
      try {
        const userStr = sessionStorage.getItem('user')
        const user = userStr ? JSON.parse(userStr) : null
        const imageUrls = this.reviewImages.map(img => img.url).filter(Boolean)
        const imagesJson = imageUrls.length > 0 ? JSON.stringify(imageUrls) : null
        await axios.post('/api/admin/service-logs', {
          type: '订单评价',
          content: `${this.currentReviewOrder?.room?.roomType?.name} ${this.currentReviewOrder?.room?.roomNumber} 订单${this.currentReviewOrder?.orderNumber}`,
          status: '已处理',
          rating: this.orderReviewForm.rating,
          review: this.orderReviewForm.content,
          images: imagesJson,
          userId: user?.id
        }, { withCredentials: true })
        alert('评价提交成功！')
        this.closeOrderReviewModal()
        // 刷新评价列表
        if (this.orderTab === 'reviews') {
          this.loadMyReviews()
        }
      } catch (error) {
        console.error('提交评价失败:', error)
        const retry = confirm('评价提交失败，是否重试？')
        if (retry) {
          this.submitOrderReview()
        }
      } finally {
        this.orderReviewSubmitting = false
      }
    },
    // 图片选择
    async handleReviewImageSelect(e) {
      const files = Array.from(e.target.files)
      this.uploadError = ''
      const remaining = 5 - this.reviewImages.length
      if (files.length > remaining) {
        this.uploadError = `最多还能上传${remaining}张图片`
        e.target.value = ''
        return
      }
      for (const file of files) {
        const ext = file.name.split('.').pop().toLowerCase()
        if (!['jpg','jpeg','png','gif','webp'].includes(ext)) {
          this.uploadError = `不支持 ${ext} 格式，仅支持 jpg/png/gif/webp`
          e.target.value = ''
          return
        }
        if (file.size > 5 * 1024 * 1024) {
          this.uploadError = `"${file.name}" 超过5MB限制`
          e.target.value = ''
          return
        }
      }
      this.uploadingImages = true
      const previews = files.map(f => ({ file: f, preview: URL.createObjectURL(f), uploading: true, progress: 0, url: null }))
      this.reviewImages.push(...previews)
      try {
        const formData = new FormData()
        files.forEach(f => formData.append('files', f))
        const res = await axios.post('/api/upload/review-images', formData, {
          withCredentials: true,
          onUploadProgress: (e) => {
            const pct = Math.round((e.loaded * 100) / e.total)
            previews.forEach(p => { p.progress = pct })
          }
        })
        if (res.data.success) {
          const urls = res.data.urls
          previews.forEach((p, i) => {
            p.url = urls[i] || null
            p.uploading = false
          })
        } else {
          this.uploadError = res.data.message
          previews.forEach(p => {
            const idx = this.reviewImages.indexOf(p)
            if (idx >= 0) this.reviewImages.splice(idx, 1)
          })
        }
      } catch (err) {
        this.uploadError = '图片上传失败：' + (err.response?.data?.message || err.message)
        previews.forEach(p => {
          const idx = this.reviewImages.indexOf(p)
          if (idx >= 0) this.reviewImages.splice(idx, 1)
        })
      } finally {
        this.uploadingImages = false
        e.target.value = ''
      }
    },
    removeReviewImage(idx) {
      const img = this.reviewImages[idx]
      if (img && img.preview && img.preview.startsWith('blob:')) {
        URL.revokeObjectURL(img.preview)
      }
      this.reviewImages.splice(idx, 1)
      this.uploadError = ''
    },
    // 加载我的订单评价
    async loadMyReviews() {
      this.loadingReviews = true
      this.reviewCurrentPage = 1
      this.reviewJumpPage = 1
      try {
        const userStr = sessionStorage.getItem('user')
        const user = userStr ? JSON.parse(userStr) : null
        if (!user?.id) {
          this.myReviews = []
          return
        }
        const response = await axios.get('/api/admin/service-logs/my-reviews', {
          params: { userId: user.id },
          withCredentials: true
        })
        this.myReviews = response.data
      } catch (error) {
        console.error('获取我的评价失败:', error)
        this.myReviews = []
      } finally {
        this.loadingReviews = false
      }
    },
    isOrderReviewed(order) {
      if (!this.myReviews || !order?.orderNumber) return false
      return this.myReviews.some(r => r.content && r.content.includes(order.orderNumber))
    },
    parseReviewImages(images) {
      if (!images) return []
      try { return JSON.parse(images) } catch (e) { return [] }
    },
    // 评价分页方法
    reviewGoToPage(page) {
      if (page < 1 || page > this.reviewTotalPages) return
      this.reviewCurrentPage = page
      this.reviewJumpPage = page
    },
    reviewGoToPrevPage() {
      if (this.reviewHasPrevPage) this.reviewGoToPage(this.reviewCurrentPage - 1)
    },
    reviewGoToNextPage() {
      if (this.reviewHasNextPage) this.reviewGoToPage(this.reviewCurrentPage + 1)
    },
    reviewHandleJumpPage() {
      const page = parseInt(this.reviewJumpPage)
      if (isNaN(page) || page < 1 || page > this.reviewTotalPages) {
        this.reviewJumpPage = this.reviewCurrentPage
        return
      }
      this.reviewGoToPage(page)
    }
  }
}
</script>

<style scoped>
.profile {
  max-width: 1200px;
  margin: 0 auto;
}

.profile h2 {
  margin-bottom: 1.5rem;
  color: var(--text-primary);
}

.profile-tabs {
  display: flex;
  gap: 0.5rem;
  border-bottom: 2px solid var(--border-light);
  padding-bottom: 0;
  margin-bottom: 2rem;
}

.profile-tabs button {
  padding: 0.8rem 2rem;
  background: none;
  border: none;
  border-bottom: 3px solid transparent;
  cursor: pointer;
  color: var(--text-light);
  font-size: 1.05rem;
  font-weight: 500;
  transition: all var(--transition);
  margin-bottom: -2px;
  position: relative;
  letter-spacing: 0.5px;
}

.profile-tabs button:hover {
  color: var(--primary-color);
  background-color: rgba(35, 133, 187, 0.04);
}

.profile-tabs button.active {
  color: var(--primary-color);
  border-bottom-color: var(--primary-color);
  font-weight: 600;
}

.order-tabs {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
}

.order-tabs button {
  padding: 0.6rem 1.4rem;
  background-color: var(--bg-white);
  color: var(--text-primary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all var(--transition);
  font-size: 0.95rem;
  font-weight: 500;
  position: relative;
  overflow: hidden;
}

.order-tabs button::after {
  content: '';
  position: absolute;
  inset: 0;
  background: rgba(255, 255, 255, 0);
  transition: background var(--transition-fast);
}

.order-tabs button:hover {
  border-color: var(--primary-color);
  color: var(--primary-color);
  box-shadow: var(--shadow-sm);
  transform: translateY(-1px);
}

.order-tabs button:hover::after {
  background: rgba(35, 133, 187, 0.04);
}

.order-tabs button:active {
  transform: translateY(0);
  box-shadow: none;
}

.order-tabs button.active {
  background: var(--primary-gradient);
  color: var(--text-white);
  border-color: var(--primary-color);
  box-shadow: 0 2px 8px rgba(35, 133, 187, 0.25);
  font-weight: 600;
}

.order-tabs button.active:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(35, 133, 187, 0.35);
}

.sub-order-tabs {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
}

.sub-order-tabs button {
  padding: 0.45rem 1.1rem;
  background-color: var(--bg-light);
  color: var(--text-secondary);
  border: 1px solid var(--border-light);
  border-radius: 20px;
  cursor: pointer;
  transition: all var(--transition);
  font-size: 0.88rem;
  font-weight: 500;
}

.sub-order-tabs button:hover {
  border-color: var(--primary-light);
  color: var(--primary-color);
  background-color: var(--status-info-bg);
  box-shadow: var(--shadow-sm);
  transform: translateY(-1px);
}

.sub-order-tabs button:active {
  transform: translateY(0);
  box-shadow: none;
}

.sub-order-tabs button.active {
  background-color: var(--primary-color);
  color: var(--text-white);
  border-color: var(--primary-color);
  box-shadow: 0 2px 6px rgba(35, 133, 187, 0.2);
}

.sub-order-tabs button.active:hover {
  background-color: var(--primary-hover);
  box-shadow: 0 3px 10px rgba(35, 133, 187, 0.3);
  transform: translateY(-1px);
}

.tab-content {
  background-color: var(--bg-white);
  padding: 2rem;
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-light);
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
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
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
  background: var(--primary-gradient);
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
  background-color: #e04040;
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.form-input {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 1.5px solid var(--border-color);
  border-radius: var(--radius-sm);
  font-size: 1rem;
  box-sizing: border-box;
  transition: all var(--transition);
}

.form-input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(35, 133, 187, 0.12);
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
  background-color: #cf9236;
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.btn-save {
  background-color: var(--status-success);
  color: var(--text-white);
}

.btn-save:hover {
  background-color: #5daf34;
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 600;
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.form-group p {
  margin: 0;
  color: var(--text-primary);
  font-size: 1rem;
}

.list {
  background-color: var(--bg-white);
  padding: 1.5rem;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
}

.item {
  padding: 1.5rem;
  border-bottom: 1px solid var(--border-light);
  margin-bottom: 1.5rem;
  display: flex;
  flex-direction: column;
  position: relative;
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
  font-size: 0.9rem;
}

.order-actions {
  display: flex;
  gap: 0.8rem;
  margin-top: 1rem;
}

.delete-button-wrapper {
  display: flex;
  justify-content: flex-end;
  gap: 0.8rem;
  margin-top: 1rem;
}

.btn-review-small {
  padding: 0.4rem 0.9rem;
  background-color: var(--status-warning);
  color: var(--text-white);
  border: none;
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-size: 0.85rem;
  transition: all var(--transition);
}

.btn-review-small:hover {
  background-color: #cf9236;
}

.btn-reviewed-disabled {
  padding: 0.4rem 0.9rem;
  background-color: var(--bg-light);
  color: var(--text-light);
  border: none;
  border-radius: var(--radius-sm);
  font-size: 0.85rem;
  cursor: not-allowed;
}

.btn-batch-delete {
  background-color: var(--status-danger);
  color: var(--text-white);
}

.btn-batch-delete:hover:not(:disabled) {
  background-color: #e04040;
}

.batch-actions {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 0.6rem;
  margin-bottom: 1rem;
  padding: 0.8rem 1rem;
  background-color: var(--bg-light);
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-light);
}

.checkbox-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  padding-right: 0.5rem;
}

.checkbox-wrapper input[type="checkbox"] {
  width: 18px;
  height: 18px;
  cursor: pointer;
  accent-color: var(--primary-color);
}

.item.has-checkbox {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
}

.item.has-checkbox .order-body {
  flex: 1;
  min-width: 0;
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
  background-color: #e04040;
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.btn-pay {
  background-color: var(--status-warning);
  color: var(--text-white);
}

.btn-pay:hover {
  background-color: #cf9236;
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.btn-delete {
  background-color: var(--status-danger);
  color: var(--text-white);
}

.btn-delete:hover {
  background-color: #e04040;
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.btn-confirm {
  background: transparent;
  color: var(--status-danger);
  border: 1px solid var(--status-danger);
}

.btn-confirm:hover:not(:disabled) {
  background: var(--status-danger-bg);
  box-shadow: none;
}

.btn-ghost {
  background: transparent;
  color: var(--status-warning);
  border: 1px solid var(--status-warning);
}

.btn-ghost:hover:not(:disabled) {
  background: var(--status-warning-bg);
  color: var(--status-warning);
  border-color: var(--status-warning);
}

.btn-delete-small {
  padding: 0.4rem 0.8rem;
  background-color: var(--status-danger);
  color: var(--text-white);
  border: none;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all var(--transition);
  font-size: 0.9rem;
}

.btn-delete-small:hover {
  background-color: #e04040;
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.empty {
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
  box-shadow: 0 4px 12px rgba(35, 133, 187, 0.35);
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

.pagination-top-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  flex-wrap: wrap;
  gap: 0.8rem;
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

.reviews-pagination-wrapper {
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
  align-items: center;
  margin-top: 2rem;
  padding-top: 1.5rem;
  border-top: 1px solid var(--border-light);
}

.jump-page {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  color: var(--text-secondary);
  font-size: 0.88rem;
}

.jump-page input {
  width: 52px;
  padding: 0.35rem 0.4rem;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  font-size: 0.88rem;
  text-align: center;
  transition: border-color var(--transition);
}

.jump-page input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(35, 133, 187, 0.12);
}

.jump-btn {
  padding: 0.35rem 0.8rem;
  background-color: var(--primary-color);
  color: var(--text-white);
  border: none;
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-size: 0.85rem;
  transition: all var(--transition);
}

.jump-btn:hover {
  background-color: var(--primary-hover);
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
  border-radius: var(--radius-sm);
  margin-bottom: 1rem;
  font-size: 0.95rem;
}

.message.error {
  background-color: var(--status-danger-bg);
  color: var(--status-danger);
  border: 1px solid var(--status-danger);
}

.message.success {
  background-color: var(--status-success-bg);
  color: var(--status-success);
  border: 1px solid var(--status-success);
}

.review-modal-content {
  max-width: 500px;
}

.review-order-info {
  text-align: center;
  color: var(--text-secondary);
  margin-bottom: 1.5rem;
  font-size: 0.95rem;
}

.rating-section {
  text-align: center;
  margin-bottom: 1.5rem;
}

.rating-label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: var(--text-primary);
}

.star-rating {
  display: inline-flex;
  gap: 0.3rem;
  font-size: 2rem;
}

.star-rating .star {
  cursor: pointer;
  color: var(--border-color);
  transition: all var(--transition-fast);
  user-select: none;
}

.star-rating .star.filled {
  color: var(--gold);
}

.star-rating .star:hover {
  transform: scale(1.2);
}

.error-text {
  color: var(--status-danger);
  font-size: 0.85rem;
  margin-top: 0.3rem;
}

.char-counter {
  text-align: right;
  font-size: 0.8rem;
  color: var(--text-light);
  margin-top: 0.3rem;
}

.form-group textarea {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 1.5px solid var(--border-color);
  border-radius: var(--radius-sm);
  font-size: 0.95rem;
  resize: vertical;
  min-height: 80px;
  transition: all var(--transition);
}

.form-group textarea:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(35, 133, 187, 0.12);
}

.reviews-panel {
  padding: 1rem 0;
}

.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.review-card {
  background: var(--bg-white);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  padding: 1.2rem;
  transition: all var(--transition);
}

.review-card:hover {
  box-shadow: var(--shadow-sm);
  border-color: var(--border-color);
}

.review-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.8rem;
}

.review-type-badge {
  background: var(--status-warning);
  color: var(--text-white);
  padding: 0.2rem 0.6rem;
  border-radius: var(--radius-sm);
  font-size: 0.8rem;
  font-weight: 500;
}

.review-time {
  color: var(--text-light);
  font-size: 0.85rem;
}

.review-card-body {
  margin-bottom: 0.8rem;
}

.review-content-text {
  color: var(--text-primary);
  font-size: 0.95rem;
  line-height: 1.5;
}

.review-card-footer {
  border-top: 1px solid var(--border-light);
  padding-top: 0.8rem;
}

.review-rating-display {
  display: flex;
  align-items: center;
  gap: 0.3rem;
}

.review-rating-display .star {
  color: var(--border-color);
  font-size: 1.2rem;
}

.review-rating-display .star.filled {
  color: var(--gold);
}

.rating-num {
  margin-left: 0.3rem;
  color: var(--text-secondary);
  font-size: 0.9rem;
  font-weight: 500;
}

.review-text {
  color: var(--text-secondary);
  font-style: italic;
  margin-top: 0.5rem;
  font-size: 0.9rem;
}

.review-images {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-top: 0.8rem;
}

.review-thumb {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-light);
  cursor: pointer;
  transition: transform var(--transition-fast);
}

.review-thumb:hover {
  transform: scale(1.05);
}

.image-upload-area {
  display: flex;
  flex-wrap: wrap;
  gap: 0.8rem;
}

.image-preview-item {
  width: 80px;
  height: 80px;
  position: relative;
  border-radius: var(--radius-sm);
  overflow: hidden;
  border: 1px solid var(--border-color);
}

.preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.btn-remove-img {
  position: absolute;
  top: 2px;
  right: 2px;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: rgba(0,0,0,0.6);
  color: white;
  border: none;
  font-size: 14px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.upload-progress {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
}

.progress-bar {
  height: 4px;
  background: rgba(0,0,0,0.2);
}

.progress-fill {
  height: 100%;
  background: var(--primary-color);
  transition: width 0.3s ease;
}

.image-add-btn {
  width: 80px;
  height: 80px;
  border: 2px dashed var(--border-color);
  border-radius: var(--radius-sm);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--text-light);
  transition: all var(--transition);
}

.image-add-btn:hover {
  border-color: var(--primary-color);
  color: var(--primary-color);
}

.image-add-btn.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.add-icon {
  font-size: 24px;
  line-height: 1;
}

.add-text {
  font-size: 11px;
  margin-top: 2px;
}

.upload-hint {
  color: var(--text-light);
  font-size: 0.8rem;
  margin-top: 0.4rem;
}

@media (max-width: 768px) {
  .avatar-section {
    flex-direction: column;
    align-items: center;
  }

  .profile-tabs button {
    padding: 0.6rem 1.2rem;
    font-size: 0.95rem;
  }

  .order-tabs button {
    padding: 0.5rem 1rem;
    font-size: 0.9rem;
  }

  .sub-order-tabs button {
    padding: 0.4rem 0.9rem;
    font-size: 0.82rem;
  }

  .pagination-wrapper {
    align-items: stretch;
  }

  .pagination {
    flex-wrap: wrap;
  }

  .pagination-top-row {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 480px) {
  .profile-tabs {
    gap: 0;
  }

  .profile-tabs button {
    padding: 0.5rem 0.8rem;
    font-size: 0.9rem;
    flex: 1;
    text-align: center;
  }

  .order-tabs {
    gap: 0.3rem;
  }

  .order-tabs button {
    padding: 0.4rem 0.8rem;
    font-size: 0.85rem;
  }

  .sub-order-tabs {
    gap: 0.3rem;
  }

  .sub-order-tabs button {
    padding: 0.35rem 0.7rem;
    font-size: 0.8rem;
  }

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
