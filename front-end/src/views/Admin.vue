<template>
  <div class="admin">
    
    <div v-if="activeTab !== 'serviceLog'" class="admin-cards">
      <div class="admin-card" @click="activeTab = 'systemConfig'" :class="{ active: activeTab === 'systemConfig' }">
        <div class="card-icon"><SvgIcon name="gear" size="2rem" /></div>
        <h3>系统配置</h3>
        <p>酒店基础信息、客房资源管理</p>
      </div>
      <div class="admin-card" @click="activeTab = 'dataManage'" :class="{ active: activeTab === 'dataManage' }">
        <div class="card-icon"><SvgIcon name="bar-chart" size="2rem" /></div>
        <h3>数据管理</h3>
        <p>预订数据统计、订单管理</p>
      </div>
    </div>

    <!-- 系统配置 -->
    <div v-if="activeTab === 'systemConfig'" class="tab-content tab-pane-enter" :key="activeTab + '-' + systemConfigSubTab">
      <h3 class="section-title">系统配置</h3>
      <div class="service-log-tabs">
        <button @click="systemConfigSubTab = 'hotelInfo'" :class="{ active: systemConfigSubTab === 'hotelInfo' }">酒店信息</button>
        <button @click="systemConfigSubTab = 'account'" :class="{ active: systemConfigSubTab === 'account' }">账户管理</button>
        <button @click="systemConfigSubTab = 'roomType'" :class="{ active: systemConfigSubTab === 'roomType' }">房型管理</button>
        <button @click="systemConfigSubTab = 'room'" :class="{ active: systemConfigSubTab === 'room' }">房间管理</button>
      </div>

      <div v-if="systemConfigSubTab === 'hotelInfo'" class="config-section">
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

      <div v-if="systemConfigSubTab === 'account'" class="config-section">
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
            <div class="jump-page">
              <span>跳至</span>
              <input type="number" v-model.number="userJumpPage" @keyup.enter="handleUserJumpPage" min="1" :max="userTotalPages" />
              <span>页</span>
              <button @click="handleUserJumpPage" class="jump-btn">跳转</button>
            </div>
          </div>
          <div v-if="usersTotalPages > 1" class="pagination-info">
            共 {{ usersTotalElements }} 条，第 {{ userCurrentPage }} / {{ usersTotalPages }} 页
          </div>
        </div>
      </div>

      <div v-if="systemConfigSubTab === 'roomType'" class="config-section">
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

      <div v-if="systemConfigSubTab === 'room'" class="config-section">
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
              <button @click="showRoomDeleteModal(room.id, room.roomNumber)" class="btn btn-small btn-delete">删除</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 数据管理 -->
    <div v-if="activeTab === 'dataManage'" class="tab-content tab-pane-enter" :key="activeTab">
      <h3 class="section-title">数据管理</h3>

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
        <!-- 30天数据滑块 -->
        <div class="chart-slider-wrapper">
          <input type="range" class="chart-slider"
            v-model.number="sliderValue"
            :min="0" :max="totalDays - windowSize"
            @input="onSliderChange"
          />
        </div>
        <div class="list">
          <div v-if="filteredOrders.length === 0" class="empty">
            <svg class="empty-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M9 12h6m-6 4h6m2-12H7a2 2 0 00-2 2v14a2 2 0 002 2h10a2 2 0 002-2V6a2 2 0 00-2-2z"/></svg>
            <p>暂无订单</p>
          </div>
          <div v-else :key="orderStatusTab">
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
              <div class="jump-page">
                <span>跳至</span>
                <input type="number" v-model.number="orderJumpPage" @keyup.enter="handleOrderJumpPage" min="1" :max="orderTotalPages" />
                <span>页</span>
                <button @click="handleOrderJumpPage" class="jump-btn">跳转</button>
              </div>
            </div>
            <div v-if="orderTotalPages > 1" class="pagination-info">
              共 {{ filteredOrders.length }} 条，第 {{ orderCurrentPage }} / {{ orderTotalPages }} 页
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 服务日志 -->
    <div v-if="activeTab === 'serviceLog'" class="tab-content tab-pane-enter" :key="activeTab + '-' + serviceLogSubTab">
      <div class="service-log-tabs">
        <button @click="serviceLogSubTab = 'frontService'" :class="{ active: serviceLogSubTab === 'frontService' }">前台服务</button>
        <button @click="serviceLogSubTab = 'userReview'" :class="{ active: serviceLogSubTab === 'userReview' }">用户评价</button>
        <button @click="switchToRoomStatus" :class="{ active: serviceLogSubTab === 'roomStatus' }">房间状态</button>
      </div>
      
      <LoadingSpinner v-if="loadingServiceLogs && serviceLogSubTab !== 'roomStatus'" variant="admin" :skeleton-count="3" size="small" />
      <ErrorRetry v-else-if="serviceLogsError && serviceLogSubTab !== 'roomStatus'" :message="serviceLogsError" @retry="getServiceLogs" />
      
      <!-- 前台服务子模块 -->
      <div v-else-if="serviceLogSubTab === 'frontService'" class="log-layout">
        <!-- 待处理 -->
        <div class="log-panel">
          <div class="panel-header">
            <span class="panel-title">待处理</span>
            <span class="panel-count">{{ pendingServiceLogs.length }} 条</span>
          </div>
          <div class="panel-body">
            <div v-if="pendingServiceLogs.length === 0" class="empty">
              <svg class="empty-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"/></svg>
              <p>暂无待处理的服务</p>
            </div>
            <div v-else>
              <div v-for="log in paginatedPendingLogs" :key="log.id" class="item service-log-item">
                <div class="log-header">
                  <h6>{{ log.type }}</h6>
                  <span class="status-badge status-warning">{{ log.status }}</span>
                </div>
                <p>用户: {{ log.user?.name || log.user?.username }}</p>
                <p>内容: {{ log.content }}</p>
                <p>时间: {{ formatDateTime(log.createTime) }}</p>
                <div v-if="log.rating" class="review-display">
                  <div class="rating-stars">
                    <span v-for="i in 5" :key="i" class="star" :class="{ filled: i <= log.rating }">★</span>
                    <span class="rating-text">{{ log.rating }} 分</span>
                  </div>
                  <p v-if="log.review" class="review-content">评价: {{ log.review }}</p>
                </div>
                <!-- 评价图片 -->
                <div v-if="parseReviewImages(log.images).length > 0" class="review-images">
                  <img v-for="(img, idx) in parseReviewImages(log.images)" :key="idx"
                    :src="img" alt="评价图片" class="review-thumb"
                    @click="openImageViewer(log.images, idx)" />
                </div>
                <div class="log-actions">
                  <button @click="handleProcessLog(log.id)" class="btn btn-process">标记已处理</button>
                </div>
              </div>
              <div v-if="pendingTotalPages > 1" class="pagination">
                <button @click="goToPendingPrevPage" :disabled="!pendingHasPrevPage" class="page-btn">上一页</button>
                <div class="page-numbers">
                  <button v-for="page in pendingPageNumbers" :key="page"
                    @click="goToPendingPage(page)" :class="['page-number', { active: pendingCurrentPage === page }]">{{ page }}</button>
                </div>
                <button @click="goToPendingNextPage" :disabled="!pendingHasNextPage" class="page-btn">下一页</button>
                <div class="jump-page">
                  <span>跳至</span>
                  <input type="number" v-model.number="pendingJumpPage" @keyup.enter="handlePendingJumpPage" min="1" :max="pendingTotalPages" />
                  <span>页</span>
                  <button @click="handlePendingJumpPage" class="jump-btn">跳转</button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 已处理 -->
        <div class="log-panel">
          <div class="panel-header">
            <span class="panel-title">已处理</span>
            <span class="panel-count">{{ processedServiceLogs.length }} 条</span>
          </div>
          <div class="panel-body">
            <div v-if="processedServiceLogs.length === 0" class="empty">
              <svg class="empty-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
              <p>暂无已处理的服务</p>
            </div>
            <div v-else>
              <div v-for="log in paginatedProcessedLogs" :key="log.id" class="item service-log-item">
                <div class="log-header">
                  <h6>{{ log.type }}</h6>
                  <span class="status-badge status-success">{{ log.status }}</span>
                </div>
                <p>用户: {{ log.user?.name || log.user?.username }}</p>
                <p>内容: {{ log.content }}</p>
                <p>时间: {{ formatDateTime(log.createTime) }}</p>
                <div v-if="log.rating" class="review-display">
                  <div class="rating-stars">
                    <span v-for="i in 5" :key="i" class="star" :class="{ filled: i <= log.rating }">★</span>
                    <span class="rating-text">{{ log.rating }} 分</span>
                  </div>
                  <p v-if="log.review" class="review-content">评价: {{ log.review }}</p>
                </div>
              </div>
              <div v-if="totalPages > 1" class="pagination">
                <button @click="goToPrevPage" :disabled="!hasPrevPage" class="page-btn">上一页</button>
                <div class="page-numbers">
                  <button v-for="page in pageNumbers" :key="page"
                    @click="goToPage(page)" :class="['page-number', { active: currentPage === page }]">{{ page }}</button>
                </div>
                <button @click="goToNextPage" :disabled="!hasNextPage" class="page-btn">下一页</button>
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

      <!-- 用户评价子模块 -->
      <div v-else-if="serviceLogSubTab === 'userReview'" class="log-layout">
        <div class="log-panel log-panel-full">
          <div class="panel-header">
            <span class="panel-title">用户评价</span>
            <span class="panel-count">{{ reviewedLogs.length }} 条</span>
          </div>
          <div class="panel-body">
            <div v-if="reviewedLogs.length === 0" class="empty">
              <svg class="empty-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M11.049 2.927c.3-.921 1.603-.921 1.902 0l1.519 4.674a1 1 0 00.95.69h4.915c.969 0 1.371 1.24.588 1.81l-3.976 2.888a1 1 0 00-.363 1.118l1.518 4.674c.3.922-.755 1.688-1.538 1.118l-3.976-2.888a1 1 0 00-1.176 0l-3.976 2.888c-.783.57-1.838-.197-1.538-1.118l1.518-4.674a1 1 0 00-.363-1.118l-3.976-2.888c-.784-.57-.38-1.81.588-1.81h4.914a1 1 0 00.951-.69l1.519-4.674z"/></svg>
              <p>暂无用户评价</p>
            </div>
            <div v-else>
              <div v-for="log in paginatedReviewedLogs" :key="log.id" class="item service-log-item">
                <div class="log-header">
                  <h6>{{ log.type || '订单评价' }}</h6>
                  <span class="status-badge status-info">已评价</span>
                </div>
                <p>用户: {{ log.user?.name || log.user?.username }}</p>
                <p>内容: {{ log.content }}</p>
                <p>时间: {{ formatDateTime(log.createTime) }}</p>
                <div v-if="log.rating" class="review-display">
                  <div class="rating-stars">
                    <span v-for="i in 5" :key="i" class="star" :class="{ filled: i <= log.rating }">★</span>
                    <span class="rating-text">{{ log.rating }} 分</span>
                  </div>
                  <p v-if="log.review" class="review-content">评价: {{ log.review }}</p>
                </div>
                <!-- 评价图片 -->
                <div v-if="parseReviewImages(log.images).length > 0" class="review-images">
                  <img v-for="(img, idx) in parseReviewImages(log.images)" :key="idx"
                    :src="img" alt="评价图片" class="review-thumb"
                    @click="openImageViewer(log.images, idx)" />
                </div>
              </div>
              <div v-if="reviewTotalPages > 1" class="pagination">
                <button @click="goToReviewPrevPage" :disabled="!reviewHasPrevPage" class="page-btn">上一页</button>
                <div class="page-numbers">
                  <button v-for="page in reviewPageNumbers" :key="page"
                    @click="goToReviewPage(page)" :class="['page-number', { active: reviewCurrentPage === page }]">{{ page }}</button>
                </div>
                <button @click="goToReviewNextPage" :disabled="!reviewHasNextPage" class="page-btn">下一页</button>
                <div class="jump-page">
                  <span>跳至</span>
                  <input type="number" v-model.number="reviewJumpPage" @keyup.enter="handleReviewJumpPage" min="1" :max="reviewTotalPages" />
                  <span>页</span>
                  <button @click="handleReviewJumpPage" class="jump-btn">跳转</button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <!-- 房间状态子模块 -->
      <div v-else-if="serviceLogSubTab === 'roomStatus'" class="log-layout">
        <LoadingSpinner v-if="roomStatusLoading" variant="admin" :skeleton-count="3" size="small" />
        <ErrorRetry v-else-if="roomStatusError" :message="roomStatusError" @retry="getRoomStatusData" />
        <div v-else class="log-panel log-panel-full">
          <div class="panel-header">
            <span class="panel-title">房间状态</span>
            <div class="panel-header-right">
              <span class="panel-count">{{ roomStatusRooms.length }} 间客房</span>
              <div class="page-size-selector">
                <span class="sort-label">每页</span>
                <select v-model.number="roomStatusPageSize" @change="onRoomStatusPageSizeChange" class="sort-select">
                  <option :value="5">5条</option>
                  <option :value="10">10条</option>
                  <option :value="20">20条</option>
                  <option :value="40">40条</option>
                </select>
              </div>
            </div>
          </div>
          <div class="panel-body">
            <div v-if="roomStatusRooms.length === 0" class="empty">
              <svg class="empty-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"/></svg>
              <p>暂无房间数据</p>
            </div>
            <div v-else class="room-table-wrapper">
              <div class="table-container">
                <table>
                  <thead>
                    <tr>
                      <th @click="roomStatusSortBy('roomNumber')" :class="{ sortable: true, sorted: roomStatusSortKey === 'roomNumber' }">
                        房间编号
                        <span class="sort-icon">{{ roomStatusSortKey === 'roomNumber' ? (roomStatusSortOrder === 'asc' ? '↑' : '↓') : '' }}</span>
                      </th>
                      <th @click="roomStatusSortBy('roomType')" :class="{ sortable: true, sorted: roomStatusSortKey === 'roomType' }">
                        客房类型
                        <span class="sort-icon">{{ roomStatusSortKey === 'roomType' ? (roomStatusSortOrder === 'asc' ? '↑' : '↓') : '' }}</span>
                      </th>
                      <th @click="roomStatusSortBy('capacity')" :class="{ sortable: true, sorted: roomStatusSortKey === 'capacity' }">
                        容纳人数
                        <span class="sort-icon">{{ roomStatusSortKey === 'capacity' ? (roomStatusSortOrder === 'asc' ? '↑' : '↓') : '' }}</span>
                      </th>
                      <th @click="roomStatusSortBy('price')" :class="{ sortable: true, sorted: roomStatusSortKey === 'price' }">
                        价格/晚
                        <span class="sort-icon">{{ roomStatusSortKey === 'price' ? (roomStatusSortOrder === 'asc' ? '↑' : '↓') : '' }}</span>
                      </th>
                      <th @click="roomStatusSortBy('status')" :class="{ sortable: true, sorted: roomStatusSortKey === 'status' }">
                        当前状态
                        <span class="sort-icon">{{ roomStatusSortKey === 'status' ? (roomStatusSortOrder === 'asc' ? '↑' : '↓') : '' }}</span>
                      </th>
                      <th @click="roomStatusSortBy('guestName')" :class="{ sortable: true, sorted: roomStatusSortKey === 'guestName' }">
                        入住客户
                        <span class="sort-icon">{{ roomStatusSortKey === 'guestName' ? (roomStatusSortOrder === 'asc' ? '↑' : '↓') : '' }}</span>
                      </th>
                      <th @click="roomStatusSortBy('checkOutTime')" :class="{ sortable: true, sorted: roomStatusSortKey === 'checkOutTime' }">
                        预计退房
                        <span class="sort-icon">{{ roomStatusSortKey === 'checkOutTime' ? (roomStatusSortOrder === 'asc' ? '↑' : '↓') : '' }}</span>
                      </th>
                      <th>操作</th>
                      <th @click="roomStatusSortBy('cleaningStatus')" :class="{ sortable: true, sorted: roomStatusSortKey === 'cleaningStatus' }">
                        清洁状态
                        <span class="sort-icon">{{ roomStatusSortKey === 'cleaningStatus' ? (roomStatusSortOrder === 'asc' ? '↑' : '↓') : '' }}</span>
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="room in roomStatusPaginatedRooms" :key="room.id" :class="'row-status-' + getRoomStatusClass(room.status).replace('status-', '')">
                      <td class="cell-number">{{ room.roomNumber }}</td>
                      <td>{{ room.roomType?.name || '-' }}</td>
                      <td>{{ room.roomType?.capacity || '-' }}人</td>
                      <td class="cell-price">¥{{ room.roomType?.price || '-' }}</td>
                      <td>
                        <span class="status-badge" :class="getRoomStatusClass(room.status)">{{ room.status }}</span>
                      </td>
                      <td class="cell-user">
                        <template v-if="room._guestName">
                          <span class="guest-name">{{ room._guestName }}</span>
                        </template>
                        <span v-else class="no-guest">—</span>
                      </td>
                      <td class="cell-date">
                        <template v-if="room._checkOutTime">
                          {{ formatDate(room._checkOutTime) }}
                        </template>
                        <span v-else class="no-guest">—</span>
                      </td>
                      <td>
                        <button @click="openRoomStatusDialog(room.id, room.status)" class="btn btn-sm">更新</button>
                      </td>
                      <td class="cell-cleaning">
                        <span v-if="room.status === '已完成' && room._cleaningStatus !== 'done'" class="cleaning-badge cleaning-pending" @click="markCleaningDone(room.id)">待清洁</span>
                        <span v-else-if="room.status === '已完成' && room._cleaningStatus === 'done'" class="cleaning-badge cleaning-done">完成清洁</span>
                        <span v-else class="no-guest">—</span>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
            <!-- 房间状态分页 -->
            <div class="room-status-pagination">
              <div class="pagination-info">
                共 {{ roomStatusRooms.length }} 条记录，第 {{ roomStatusCurrentPage }} / {{ roomStatusTotalPages }} 页
              </div>
              <div class="pagination">
                <button @click="roomStatusGoToPage(1)" :disabled="roomStatusCurrentPage === 1" class="page-btn" title="第一页">«</button>
                <button @click="roomStatusPrevPage" :disabled="roomStatusCurrentPage === 1" class="page-btn">上一页</button>
                <div class="page-numbers">
                  <template v-for="page in roomStatusVisiblePages" :key="page">
                    <span v-if="page === -1" class="ellipsis">...</span>
                    <button v-else @click="roomStatusGoToPage(page)" :class="['page-number', { active: roomStatusCurrentPage === page }]">{{ page }}</button>
                  </template>
                </div>
                <button @click="roomStatusNextPage" :disabled="roomStatusCurrentPage === roomStatusTotalPages" class="page-btn">下一页</button>
                <button @click="roomStatusGoToPage(roomStatusTotalPages)" :disabled="roomStatusCurrentPage === roomStatusTotalPages" class="page-btn" title="最后一页">»</button>
              </div>
              <div class="jump-page">
                <span>跳至</span>
                <input type="number" v-model.number="roomStatusJumpPage" @keyup.enter="roomStatusHandleJumpPage" min="1" :max="roomStatusTotalPages" />
                <span>页</span>
                <button @click="roomStatusHandleJumpPage" class="jump-btn">跳转</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 房间状态弹窗 -->
    <transition name="modal">
    <div v-if="roomStatusShowDialog" class="modal-overlay" @click="closeRoomStatusDialog">
      <div class="modal-content" @click.stop>
        <h3>更新客房状态</h3>
        <div class="form-group">
          <label>选择状态</label>
          <select v-model="roomStatusSelectedStatus" class="form-input">
            <option value="空房">空房</option>
            <option value="已预订">已预订</option>
            <option value="已入住">已入住</option>
            <option value="已完成">已完成</option>
            <option value="维护中">维护中</option>
          </select>
        </div>
        <div class="modal-actions">
          <button @click="confirmRoomStatusUpdate" class="btn btn-confirm">确认</button>
          <button @click="closeRoomStatusDialog" class="btn btn-cancel">取消</button>
        </div>
      </div>
    </div>
    </transition>

    <!-- 图片查看器 -->
    <div v-if="imageViewerVisible" class="image-viewer-overlay" @click.self="closeImageViewer">
      <div class="image-viewer-content">
        <button @click="closeImageViewer" class="viewer-close">×</button>
        <button v-if="imageViewerImages.length > 1" @click="prevImage" class="viewer-nav viewer-prev">‹</button>
        <img :src="imageViewerImages[imageViewerIndex]" alt="评价图片" class="viewer-img" />
        <button v-if="imageViewerImages.length > 1" @click="nextImage" class="viewer-nav viewer-next">›</button>
        <div class="viewer-counter" v-if="imageViewerImages.length > 1">
          {{ imageViewerIndex + 1 }} / {{ imageViewerImages.length }}
        </div>
        <a :href="imageViewerImages[imageViewerIndex]" download class="viewer-download">下载图片</a>
      </div>
    </div>

    <!-- 确认删除弹窗 -->
    <transition name="modal">
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
    </transition>
    <!-- 房间删除确认弹窗 -->
    <transition name="modal">
    <div v-if="roomDeleteModalVisible" class="modal-overlay">
      <div class="modal-content">
        <h3>确认删除</h3>
        <p>确定要删除房间 <strong>{{ roomDeleteTarget.roomNumber }}</strong> 吗？此操作不可撤销。</p>
        <div class="modal-actions">
          <button @click="closeRoomDeleteModal" class="btn">取消</button>
          <button @click="handleRoomDeleteConfirm" class="btn btn-confirm">确认</button>
        </div>
      </div>
    </div>
    </transition>
  </div>
</template>

<script>
import axios from 'axios'
import * as echarts from 'echarts'
import LoadingSpinner from '../components/LoadingSpinner.vue'
import ErrorRetry from '../components/ErrorRetry.vue'
import SvgIcon from '../components/SvgIcon.vue'

export default {
  name: 'Admin',
  components: {
    LoadingSpinner,
    ErrorRetry,
    SvgIcon
  },
  data() {
    return {
      activeTab: 'systemConfig',
      systemConfigSubTab: 'hotelInfo',
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
      serviceLogsError: null,
      currentPage: 1,
      pageSize: 5,
      pendingCurrentPage: 1,
      jumpPage: 1,
      pendingJumpPage: 1,
      serviceLogSubTab: 'frontService',
      reviewedLogs: [],
      reviewCurrentPage: 1,
      reviewPageSize: 5,
      reviewJumpPage: 1,
      // 图片查看器
      imageViewerVisible: false,
      imageViewerImages: [],
      imageViewerIndex: 0,
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
      orderJumpPage: 1,
      refreshInterval: null,
      chart: null,
      orderChart: null,
      // 滑块相关（30天数据，7天窗口）
      sliderValue: 23,
      windowSize: 7,
      totalDays: 30,
      // 账户管理相关
      users: [],
      usersTotalElements: 0,
      usersTotalPages: 0,
      userCurrentPage: 1,
      userPageSize: 5,
      userJumpPage: 1,
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
      currentDeleteUserId: null,
      roomDeleteModalVisible: false,
      roomDeleteTarget: { id: null, roomNumber: '' },
      // 房间状态（服务日志子模块）
      roomStatusRooms: [],
      roomStatusLoading: false,
      roomStatusError: null,
      roomStatusSortKey: 'roomNumber',
      roomStatusSortOrder: 'asc',
      roomStatusCurrentPage: 1,
      roomStatusPageSize: 10,
      roomStatusJumpPage: 1,
      roomStatusShowDialog: false,
      roomStatusSelectedStatus: '空房',
      roomStatusCurrentRoomId: null,
      roomStatusCleaningTimers: {}
    }
  },
  computed: {
    pendingServiceLogs() {
      return this.serviceLogs
        .filter(log => log.status === '待处理' && log.type !== '订单评价')
        .sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
    },
    processedServiceLogs() {
      return this.serviceLogs
        .filter(log => log.status === '已处理' && log.type !== '订单评价')
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
      } else if (this.orderStatusTab === '已完成') {
        result = this.allOrders.filter(order =>
          order.status === '已完成' || order.status === '已退房' || order.status === '自动退房'
        )
      } else {
        result = this.allOrders.filter(order => order.status === this.orderStatusTab)
      }
      return result.sort((a, b) => b.id - a.id)
    },
    paginatedOrders() {
      const start = (this.orderCurrentPage - 1) * this.orderPageSize
      return this.filteredOrders.slice(start, start + this.orderPageSize)
    },
    orderTotalPages() {
      return Math.ceil(this.filteredOrders.length / this.orderPageSize) || 1
    },
    orderHasPrevPage() {
      return this.orderCurrentPage > 1
    },
    orderHasNextPage() {
      return this.orderCurrentPage < this.orderTotalPages
    },
    orderPageNumbers() {
      const pages = []
      const total = this.orderTotalPages
      for (let i = 1; i <= total; i++) {
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
    },
    // 用户评价分页
    paginatedReviewedLogs() {
      const start = (this.reviewCurrentPage - 1) * this.reviewPageSize
      return this.reviewedLogs.slice(start, start + this.reviewPageSize)
    },
    reviewTotalPages() {
      return Math.ceil(this.reviewedLogs.length / this.reviewPageSize) || 1
    },
    reviewHasPrevPage() {
      return this.reviewCurrentPage > 1
    },
    reviewHasNextPage() {
      return this.reviewCurrentPage < this.reviewTotalPages
    },
    reviewPageNumbers() {
      const pages = []
      for (let i = 1; i <= this.reviewTotalPages; i++) {
        pages.push(i)
      }
      return pages
    },
    sortedRoomStatusRooms() {
      if (!this.roomStatusRooms.length) return []
      const rooms = [...this.roomStatusRooms]
      const key = this.roomStatusSortKey
      const order = this.roomStatusSortOrder
      const compareString = (a, b) => {
        const va = (a || '').toLowerCase()
        const vb = (b || '').toLowerCase()
        return order === 'asc' ? va.localeCompare(vb) : vb.localeCompare(va)
      }
      const compareNumber = (a, b) => {
        const na = a || 0
        const nb = b || 0
        return order === 'asc' ? na - nb : nb - na
      }
      const compareDate = (a, b) => {
        const da = a ? new Date(a).getTime() : 0
        const db = b ? new Date(b).getTime() : 0
        return order === 'asc' ? da - db : db - da
      }
      const statusPriority = {
        '已入住': 0,
        '已完成': 1,
        '已预订': 2,
        '已支付': 3,
        '空房': 4,
        '维护中': 5
      }
      return rooms.sort((a, b) => {
        switch (key) {
          case 'roomNumber':
            return compareString(a.roomNumber, b.roomNumber)
          case 'roomType':
            return compareString(a.roomType?.name, b.roomType?.name)
          case 'capacity':
            return compareNumber(a.roomType?.capacity, b.roomType?.capacity)
          case 'price':
            return compareNumber(a.roomType?.price, b.roomType?.price)
          case 'status': {
            const pa = statusPriority[a.status] ?? 99
            const pb = statusPriority[b.status] ?? 99
            if (pa !== pb) return order === 'asc' ? pa - pb : pb - pa
            return compareString(a.roomNumber, b.roomNumber)
          }
          case 'guestName':
            return compareString(a._guestName, b._guestName)
          case 'checkOutTime':
            return compareDate(a._checkOutTime, b._checkOutTime)
          case 'cleaningStatus': {
            const cs = (r) => {
              if (r.status === '已完成' && r._cleaningStatus !== 'done') return 0
              if (r.status === '已完成' && r._cleaningStatus === 'done') return 1
              return 2
            }
            const ca = cs(a)
            const cb = cs(b)
            if (ca !== cb) return order === 'asc' ? ca - cb : cb - ca
            return compareString(a.roomNumber, b.roomNumber)
          }
          default:
            return 0
        }
      })
    },
    roomStatusTotalPages() {
      return Math.ceil(this.roomStatusRooms.length / this.roomStatusPageSize) || 1
    },
    roomStatusPaginatedRooms() {
      const start = (this.roomStatusCurrentPage - 1) * this.roomStatusPageSize
      return this.sortedRoomStatusRooms.slice(start, start + this.roomStatusPageSize)
    },
    roomStatusVisiblePages() {
      const pages = []
      let start = Math.max(1, this.roomStatusCurrentPage - 2)
      let end = Math.min(this.roomStatusTotalPages, this.roomStatusCurrentPage + 2)
      if (start > 1) {
        pages.push(1)
        if (start > 2) pages.push(-1)
      }
      for (let i = start; i <= end; i++) pages.push(i)
      if (end < this.roomStatusTotalPages) {
        if (end < this.roomStatusTotalPages - 1) pages.push(-1)
        pages.push(this.roomStatusTotalPages)
      }
      return pages
    }
  },
  watch: {
    '$route.path'(newPath) {
      if (newPath === '/admin/logs') {
        this.activeTab = 'serviceLog'
        this.getServiceLogs()
      } else if (newPath === '/admin' && this.activeTab === 'serviceLog') {
        this.activeTab = 'systemConfig'
      }
    },
    orderStatusTab() {
      this.orderCurrentPage = 1
      this.sliderValue = Math.min(this.sliderValue, this.totalDays - this.windowSize)
      this.$nextTick(() => {
        if (!this.orderChart) {
          this.initOrderChart()
        } else {
          this.updateOrderChart()
        }
      })
    },
    activeTab(newVal) {
      if (newVal === 'dataManage') {
        this.getOrders()
        this.getStatistics()
        this.$nextTick(() => {
          this.initChart()
          this.initOrderChart()
        })
        this.startAutoRefresh()
      } else {
        this.stopAutoRefresh()
      }
      if (newVal === 'serviceLog') {
        this.getServiceLogs()
      }
    }
  },
  mounted() {
    if (this.$route.path === '/admin/logs') {
      this.activeTab = 'serviceLog'
    }
    this.getHotelInfo()
    this.getRoomTypes()
    this.getRooms()
    this.getUsers()
    this.getRoles()
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
    Object.values(this.roomStatusCleaningTimers).forEach(t => clearTimeout(t))
    this.roomStatusCleaningTimers = {}
    window.removeEventListener('resize', this.handleResize)
  },
  methods: {
    goToOrderPage(page) {
      this.orderCurrentPage = page
      this.orderJumpPage = page
    },
    goToOrderPrevPage() {
      if (this.orderHasPrevPage) {
        this.orderCurrentPage--
        this.orderJumpPage = this.orderCurrentPage
      }
    },
    goToOrderNextPage() {
      if (this.orderHasNextPage) {
        this.orderCurrentPage++
        this.orderJumpPage = this.orderCurrentPage
      }
    },
    handleOrderJumpPage() {
      let page = parseInt(this.orderJumpPage)
      if (isNaN(page) || page < 1 || page > this.orderTotalPages) {
        this.orderJumpPage = this.orderCurrentPage
        return
      }
      this.goToOrderPage(page)
    },
    startAutoRefresh() {
      this.stopAutoRefresh()
      // 优化：只在页面可见时刷新，减少不必要的请求
      const checkAndRefresh = () => {
        if (document.visibilityState === 'visible') {
          this.getRooms()
          this.getOrders()
          this.getStatistics()
        }
      }
      // 初始延迟30秒后检查，之后每60秒检查一次
      setTimeout(checkAndRefresh, 30000)
      this.refreshInterval = setInterval(checkAndRefresh, 60000)
    },
    stopAutoRefresh() {
      if (this.refreshInterval) {
        clearInterval(this.refreshInterval)
        this.refreshInterval = null
      }
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
      this.serviceLogsError = null
      try {
        const [logsRes, reviewedRes] = await Promise.all([
          axios.get('/api/admin/service-logs', {
            params: { page: 0, size: 200 },
            withCredentials: true
          }),
          axios.get('/api/admin/service-logs/reviewed', {
            params: { page: 0, size: 200 },
            withCredentials: true
          })
        ])
        this.serviceLogs = logsRes.data.content
        this.serviceLogsTotalElements = logsRes.data.totalElements
        this.serviceLogsTotalPages = logsRes.data.totalPages
        this.reviewedLogs = reviewedRes.data.content
      } catch (error) {
        console.error('获取服务日志失败:', error)
        this.serviceLogsError = '获取服务日志失败，请检查网络后重试'
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
    async handleProcessLog(logId) {
      await this.updateServiceLogStatus(logId, '已处理')
    },
    goToPage(page) {
      this.currentPage = page
      this.jumpPage = page
    },
    goToPrevPage() {
      if (this.hasPrevPage) {
        this.currentPage--
        this.jumpPage = this.currentPage
      }
    },
    goToNextPage() {
      if (this.hasNextPage) {
        this.currentPage++
        this.jumpPage = this.currentPage
      }
    },
    handleJumpPage() {
      let page = parseInt(this.jumpPage)
      if (isNaN(page) || page < 1 || page > this.totalPages) {
        this.jumpPage = this.currentPage
        return
      }
      this.goToPage(page)
    },
    goToPendingPage(page) {
      this.pendingCurrentPage = page
      this.pendingJumpPage = page
    },
    goToPendingPrevPage() {
      if (this.pendingHasPrevPage) {
        this.pendingCurrentPage--
        this.pendingJumpPage = this.pendingCurrentPage
      }
    },
    goToPendingNextPage() {
      if (this.pendingHasNextPage) {
        this.pendingCurrentPage++
        this.pendingJumpPage = this.pendingCurrentPage
      }
    },
    handlePendingJumpPage() {
      let page = parseInt(this.pendingJumpPage)
      if (isNaN(page) || page < 1 || page > this.pendingTotalPages) {
        this.pendingJumpPage = this.pendingCurrentPage
        return
      }
      this.goToPendingPage(page)
    },
    // 用户评价分页方法
    goToReviewPage(page) {
      this.reviewCurrentPage = page
      this.reviewJumpPage = page
    },
    goToReviewPrevPage() {
      if (this.reviewHasPrevPage) {
        this.reviewCurrentPage--
        this.reviewJumpPage = this.reviewCurrentPage
      }
    },
    goToReviewNextPage() {
      if (this.reviewHasNextPage) {
        this.reviewCurrentPage++
        this.reviewJumpPage = this.reviewCurrentPage
      }
    },
    handleReviewJumpPage() {
      let page = parseInt(this.reviewJumpPage)
      if (isNaN(page) || page < 1 || page > this.reviewTotalPages) {
        this.reviewJumpPage = this.reviewCurrentPage
        return
      }
      this.goToReviewPage(page)
    },
    // 房间状态方法
    roomStatusSortBy(key) {
      if (this.roomStatusSortKey === key) {
        this.roomStatusSortOrder = this.roomStatusSortOrder === 'asc' ? 'desc' : 'asc'
      } else {
        this.roomStatusSortKey = key
        this.roomStatusSortOrder = 'asc'
      }
      this.roomStatusCurrentPage = 1
      this.roomStatusJumpPage = 1
    },
    roomStatusPrevPage() {
      if (this.roomStatusCurrentPage > 1) this.roomStatusCurrentPage--
    },
    roomStatusNextPage() {
      if (this.roomStatusCurrentPage < this.roomStatusTotalPages) this.roomStatusCurrentPage++
    },
    roomStatusGoToPage(page) {
      if (page >= 1 && page <= this.roomStatusTotalPages) {
        this.roomStatusCurrentPage = page
        this.roomStatusJumpPage = page
      }
    },
    onRoomStatusPageSizeChange() {
      this.roomStatusCurrentPage = 1
      this.roomStatusJumpPage = 1
    },
    roomStatusHandleJumpPage() {
      const page = parseInt(this.roomStatusJumpPage)
      if (isNaN(page) || page < 1 || page > this.roomStatusTotalPages) {
        this.roomStatusJumpPage = this.roomStatusCurrentPage
        return
      }
      this.roomStatusCurrentPage = page
    },
    switchToRoomStatus() {
      this.serviceLogSubTab = 'roomStatus'
      this.getRoomStatusData()
    },
    async getRoomStatusData() {
      this.roomStatusLoading = true
      this.roomStatusError = null
      this.roomStatusCurrentPage = 1
      this.roomStatusJumpPage = 1
      try {
        const [roomsRes, ordersRes] = await Promise.all([
          axios.get('/api/user/rooms', {
            params: { page: 0, size: 200 },
            withCredentials: true
          }),
          axios.get('/api/user/orders', {
            params: { page: 0, size: 500 },
            withCredentials: true
          })
        ])

        const rooms = roomsRes.data.content || roomsRes.data
        const ordersData = ordersRes.data.content || ordersRes.data

        const activeOrders = ordersData.filter(o =>
          o.status === '已入住' || o.status === '已预订' || o.status === '已支付'
        )

        const roomOrderMap = {}
        activeOrders.forEach(order => {
          const roomId = order.room?.id
          if (roomId) {
            const existing = roomOrderMap[roomId]
            if (!existing || new Date(order.checkInTime) > new Date(existing.checkInTime)) {
              roomOrderMap[roomId] = order
            }
          }
        })

        this.roomStatusRooms = rooms.map(room => {
          const order = roomOrderMap[room.id]
          const existingRoom = this.roomStatusRooms.find(r => r.id === room.id)
          return {
            ...room,
            _guestName: order?.user?.name || order?.user?.username || null,
            _checkOutTime: order?.checkOutTime || null,
            _cleaningStatus: existingRoom?._cleaningStatus || null
          }
        })
      } catch (err) {
        if (axios.isCancel(err)) return
        console.error('获取房间状态数据失败:', err)
        this.roomStatusError = '获取房间数据失败，请检查网络后重试'
      } finally {
        this.roomStatusLoading = false
      }
    },
    getRoomStatusClass(status) {
      switch (status) {
        case '空房': return 'status-success'
        case '已预订':
        case '已支付': return 'status-warning'
        case '已入住': return 'status-info'
        case '已完成': return 'status-warning'
        case '维护中': return 'status-danger'
        default: return 'status-info'
      }
    },
    openRoomStatusDialog(roomId, currentStatus) {
      this.roomStatusCurrentRoomId = roomId
      this.roomStatusSelectedStatus = currentStatus || '空房'
      this.roomStatusShowDialog = true
    },
    closeRoomStatusDialog() {
      this.roomStatusShowDialog = false
      this.roomStatusCurrentRoomId = null
      this.roomStatusSelectedStatus = '空房'
    },
    async confirmRoomStatusUpdate() {
      if (!this.roomStatusCurrentRoomId) return
      try {
        const response = await axios.put(
          `/api/user/admin/rooms/${this.roomStatusCurrentRoomId}/status?status=${encodeURIComponent(this.roomStatusSelectedStatus)}`,
          {},
          { withCredentials: true }
        )
        if (response.data) {
          alert('房间状态更新成功')
          this.getRoomStatusData()
          this.closeRoomStatusDialog()
        }
      } catch (err) {
        console.error('更新房间状态失败:', err)
        alert('更新房间状态失败，请稍后重试')
      }
    },
    markCleaningDone(roomId) {
      const room = this.roomStatusRooms.find(r => r.id === roomId)
      if (!room || room._cleaningStatus === 'done') return
      this.$set(room, '_cleaningStatus', 'done')
      if (this.roomStatusCleaningTimers[roomId]) {
        clearTimeout(this.roomStatusCleaningTimers[roomId])
      }
      this.roomStatusCleaningTimers[roomId] = setTimeout(() => {
        this.$delete(this.roomStatusCleaningTimers, roomId)
        this.resetRoomToVacant(roomId)
      }, 2000)
    },
    async resetRoomToVacant(roomId) {
      try {
        await axios.put(
          `/api/user/admin/rooms/${roomId}/status?status=${encodeURIComponent('空房')}`,
          {},
          { withCredentials: true }
        )
        this.getRoomStatusData()
      } catch (err) {
        console.error('重置房间状态失败:', err)
      }
    },
    // 图片查看器
    parseReviewImages(images) {
      if (!images) return []
      try {
        return JSON.parse(images)
      } catch (e) {
        return []
      }
    },
    openImageViewer(images, idx) {
      const urls = this.parseReviewImages(images)
      this.imageViewerImages = urls
      this.imageViewerIndex = idx || 0
      this.imageViewerVisible = true
    },
    closeImageViewer() {
      this.imageViewerVisible = false
      this.imageViewerImages = []
      this.imageViewerIndex = 0
    },
    prevImage() {
      if (this.imageViewerIndex > 0) this.imageViewerIndex--
    },
    nextImage() {
      if (this.imageViewerIndex < this.imageViewerImages.length - 1) this.imageViewerIndex++
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
        animation: true,
        animationDuration: 800,
        animationDurationUpdate: 300,
        animationEasing: 'cubicOut',
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
              { value: this.statistics.totalBookings, name: '已预订', itemStyle: { color: '#5b9bd5' } },
              { value: this.statistics.totalCancellations, name: '已取消', itemStyle: { color: '#ff4d4f' } },
              { value: this.statistics.totalCheckIn, name: '已入住', itemStyle: { color: '#faad14' } },
              { value: this.statistics.totalCompleted, name: '已完成', itemStyle: { color: '#52c41a' } }
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
    // 获取近30天全部数据
    getAllDaysData() {
      const today = new Date()
      const days = []
      const data = []
      
      let filteredOrders
      if (this.orderStatusTab === '已完成') {
        filteredOrders = this.allOrders.filter(order =>
          order.status === '已完成' || order.status === '已退房' || order.status === '自动退房'
        )
      } else if (this.orderStatusTab === 'all') {
        filteredOrders = this.allOrders
      } else {
        filteredOrders = this.allOrders.filter(order => order.status === this.orderStatusTab)
      }
      
      for (let i = 29; i >= 0; i--) {
        const date = new Date(today)
        date.setDate(today.getDate() - i)
        const month = date.getMonth() + 1
        const day = date.getDate()
        days.push(`${month}/${day}`)
        
        let count = 0
        
        for (const order of filteredOrders) {
          const orderDate = new Date(order.createTime || order.checkInTime)
          if (orderDate.toDateString() === date.toDateString()) {
            count++
          }
        }
        data.push(count)
      }
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
    updateOrderChart() {
      if (!this.orderChart) return
      
      const { days, data } = this.getWindowData()
      
      const chartTitle = this.orderStatusTab === 'all' ? '近7天订单统计' : `近7天${this.orderStatusTab}统计`
      
      let barColor = '#5b9bd5'
      if (this.orderStatusTab === '已取消') {
        barColor = '#ff4d4f'
      } else if (this.orderStatusTab === '已入住') {
        barColor = '#faad14'
      } else if (this.orderStatusTab === '已完成') {
        barColor = '#52c41a'
      } else if (this.orderStatusTab === '已预订') {
        barColor = '#5b9bd5'
      }
      
      const option = {
        animation: true,
        animationDuration: 800,
        animationDurationUpdate: 300,
        animationEasing: 'cubicOut',
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
      if (this.orderChart) {
        this.orderChart.resize()
      }
    },
    onSliderChange() {
      this.$nextTick(() => this.updateOrderChart())
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
            page: 0,
            size: 200
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
            page: 0,
            size: 200
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
        
        // 检查当前页面是否还有数据，如果没有就调整到最后一页
        if (this.orders.length === 0 && this.ordersTotalElements > 0 && this.orderCurrentPage > 1) {
          this.orderCurrentPage = this.ordersTotalPages  // 因为页码从1开始
          await this.getOrders()  // 递归重新获取
          return
        }
        
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
        if (response.data.success) {
          alert(response.data.message)
          this.getRooms()
          this.roomForm = {
            roomNumber: '',
            roomTypeId: '',
            status: '空房'
          }
        } else {
          alert(response.data.message || '添加失败')
        }
      } catch (error) {
        console.error('添加房间失败:', error)
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
    showRoomDeleteModal(roomId, roomNumber) {
      this.roomDeleteTarget = { id: roomId, roomNumber }
      this.roomDeleteModalVisible = true
    },
    closeRoomDeleteModal() {
      this.roomDeleteModalVisible = false
      this.roomDeleteTarget = { id: null, roomNumber: '' }
    },
    async handleRoomDeleteConfirm() {
      const roomId = this.roomDeleteTarget.id
      if (!roomId) return
      try {
        const response = await axios.delete(`/api/user/admin/rooms/${roomId}`, { withCredentials: true })
        if (response.data.success) {
          alert('房间删除成功')
          this.getRooms()
        } else {
          alert(response.data.message || '删除失败')
        }
      } catch (error) {
        console.error('删除房间失败:', error)
        alert('删除房间失败，请稍后重试')
      } finally {
        this.closeRoomDeleteModal()
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
        
        // 检查当前页面是否还有数据，如果没有就调整到最后一页
        if (this.users.length === 0 && this.usersTotalElements > 0 && this.userCurrentPage > 1) {
          this.userCurrentPage = this.usersTotalPages  // 因为页码从1开始
          await this.getUsers()  // 递归重新获取
          return
        }
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
      this.userJumpPage = page
      this.getUsers()
    },
    goToUserPrevPage() {
      if (this.userHasPrevPage) {
        this.userCurrentPage--
        this.userJumpPage = this.userCurrentPage
        this.getUsers()
      }
    },
    goToUserNextPage() {
      if (this.userHasNextPage) {
        this.userCurrentPage++
        this.userJumpPage = this.userCurrentPage
        this.getUsers()
      }
    },
    handleUserJumpPage() {
      let page = parseInt(this.userJumpPage)
      if (isNaN(page) || page < 1 || page > this.userTotalPages) {
        this.userJumpPage = this.userCurrentPage
        return
      }
      this.goToUserPage(page)
    }
  }
}
</script>

<style scoped>
.admin {
  max-width: 1280px;
  margin: 0 auto;
  animation: fadeInUp var(--transition-slow);
}

.admin-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 2rem;
  margin-bottom: 2.5rem;
}

.admin-card {
  padding: 2.8rem 2rem;
  background-color: var(--bg-white);
  border-radius: var(--radius-lg);
  text-align: center;
  cursor: pointer;
  transition: all var(--transition);
  border: 2px solid var(--border-light);
  border-left: 4px solid transparent;
  position: relative;
  overflow: hidden;
}

.admin-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: var(--primary-gradient);
  transform: scaleX(0);
  transition: transform var(--transition);
}

.admin-card:nth-child(1)::before { background: var(--primary-gradient); }
.admin-card:nth-child(2)::before { background: var(--primary-gradient); }

.admin-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-lg);
  border-color: var(--primary-light);
  border-left-color: var(--primary-color);
}

.admin-card:active {
  transform: translateY(-2px) scale(0.97);
  box-shadow: var(--shadow-sm);
  transition-duration: 0.1s;
}

.admin-card:hover::before {
  transform: scaleX(1);
}

.admin-card.active {
  border-color: var(--primary-color);
  background: var(--primary-gradient);
}

.admin-card.active h3,
.admin-card.active p {
  color: var(--text-white);
}

.admin-card.active .card-icon {
  background: rgba(255,255,255,0.2);
}

.admin-card .card-icon {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  margin: 0 auto 1.2rem;
  background: var(--status-info-bg);
  box-shadow: var(--shadow-xs);
  transition: all var(--transition);
}

.admin-card:hover .card-icon {
  transform: scale(1.1);
  box-shadow: var(--shadow-sm);
}

.admin-card h3 {
  font-size: 1.4rem;
  margin-bottom: 0.5rem;
  color: var(--text-primary);
}

.admin-card p {
  color: var(--text-secondary);
  margin: 0;
}

.tab-content {
  background: var(--bg-white);
  padding: 2rem;
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-light);
  box-shadow: var(--shadow-xs);
}

.tab-content h3 {
  text-align: center;
  margin-bottom: 2rem;
  font-weight: 700;
  font-size: 1.5rem;
  color: var(--primary-color);
}

.config-section {
  margin-bottom: 2.5rem;
  padding-bottom: 2rem;
  border-bottom: 1px solid var(--border-light);
}

.config-section:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.config-section h4 {
  margin-bottom: 1.5rem;
  color: var(--text-primary);
  font-size: 1.15rem;
  font-weight: 600;
  padding-left: 0.75rem;
  border-left: 3px solid var(--primary-color);
}

.config-section h5 {
  margin-bottom: 1rem;
  color: var(--text-secondary);
  font-weight: 500;
}

.add-form {
  margin-bottom: 1.5rem;
  padding: 1.5rem;
  background-color: var(--bg-white);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
}

.form-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.form-group input,
.form-group textarea,
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
.form-group textarea:focus,
.form-group select:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(91, 155, 213, 0.12);
}

.btn-small {
  padding: 0.4rem 0.8rem;
  font-size: 0.9rem;
}

.list {
  background-color: var(--bg-white);
  padding: 1.5rem;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
}

.item {
  padding: 1rem;
  border-bottom: 1px solid var(--border-light);
  margin-bottom: 1rem;
}

.item:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.item p {
  margin-bottom: 0.5rem;
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.statistics {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 2rem;
}

.stat-card {
  padding: 2rem 1.5rem;
  border-radius: var(--radius-lg);
  text-align: center;
  background: var(--bg-white);
  border: 1px solid var(--border-light);
  box-shadow: var(--shadow-xs);
  transition: all var(--transition);
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
}

.stat-card:nth-child(1)::before { background: var(--primary-color); }
.stat-card:nth-child(2)::before { background: var(--status-warning); }
.stat-card:nth-child(3)::before { background: var(--status-success); }
.stat-card:nth-child(4)::before { background: var(--primary-color); }
.stat-card:nth-child(5)::before { background: var(--primary-color); }

.stat-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-3px);
}

.stat-card h5 {
  margin-bottom: 0.5rem;
  color: var(--text-secondary);
  font-weight: 500;
  font-size: 0.9rem;
}

.stat-number {
  font-size: 2.2rem;
  font-weight: 700;
}

.stat-card:nth-child(1) .stat-number { color: var(--primary-color); }
.stat-card:nth-child(2) .stat-number { color: var(--status-warning); }
.stat-card:nth-child(3) .stat-number { color: var(--status-success); }
.stat-card:nth-child(4) .stat-number { color: var(--primary-color); }

.chart-container {
  margin-top: 2rem;
  padding: 1.5rem;
  background-color: var(--bg-white);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-xs);
  border: 1px solid var(--border-light);
}

.file-input {
  width: 100%;
  padding: 0.6rem;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
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
  border: 2px solid var(--border-color);
  border-radius: var(--radius-md);
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
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-color);
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
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  font-size: 0.95rem;
  background: var(--bg-white);
  color: var(--text-primary);
}

.order-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.order-info {
  flex: 1;
}

.order-status {
  padding: 1rem;
  background-color: var(--bg-light);
  border-radius: var(--radius-sm);
  min-width: 120px;
  text-align: center;
}

.loading {
  text-align: center;
  padding: 2rem;
  color: var(--text-light);
}

.service-log-item {
  background-color: var(--bg-white);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-light) !important;
  border-bottom: none !important;
  margin-bottom: 1rem;
  padding: 1.5rem !important;
  transition: all var(--transition);
}

.service-log-item:hover {
  box-shadow: var(--shadow-sm);
  border-color: var(--border-color) !important;
}

.log-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 0.8rem;
  padding-top: 0.8rem;
  border-top: 1px solid var(--border-light);
}

.btn-process {
  padding: 0.4rem 1rem;
  background-color: var(--primary-color);
  color: var(--text-white);
  border: none;
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-size: 0.9rem;
  transition: all var(--transition);
}

.btn-process:hover {
  background-color: var(--primary-hover);
  box-shadow: var(--shadow-sm);
}

.log-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.log-header h6 {
  margin: 0;
  color: var(--text-primary);
  font-size: 1rem;
  font-weight: 600;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 0.5rem;
  margin-top: 1.5rem;
  padding-top: 1.5rem;
  border-top: 1px solid var(--border-light);
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
  width: 36px;
  height: 36px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: var(--bg-white);
  color: var(--text-primary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all var(--transition);
  font-size: 0.9rem;
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

.pagination-info {
  text-align: center;
  margin-top: 0.5rem;
  color: var(--text-secondary);
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
  color: var(--text-primary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all var(--transition);
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
  background-color: var(--bg-white);
  padding: 1.5rem;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-xs);
  border: 1px solid var(--border-light);
  margin-bottom: 1rem;
}

.chart-slider-wrapper {
  background-color: var(--bg-white);
  padding: 1rem 2rem 2rem;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
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

.service-log-tabs {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.5rem;
  border-bottom: 2px solid var(--border-light);
  padding-bottom: 0;
}

.service-log-tabs button {
  padding: 0.7rem 1.5rem;
  background: none;
  border: none;
  border-bottom: 3px solid transparent;
  cursor: pointer;
  color: var(--text-secondary);
  font-size: 1rem;
  font-weight: 500;
  transition: all var(--transition);
  margin-bottom: -2px;
}

.service-log-tabs button.active {
  color: var(--primary-color);
  border-bottom-color: var(--primary-color);
}

.service-log-tabs button:hover {
  color: var(--primary-color);
}

.log-panel-full {
  grid-column: 1 / -1;
}

.log-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
}

.log-panel {
  background-color: var(--bg-white);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
  overflow: hidden;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.5rem;
  background-color: var(--bg-light);
  border-bottom: 1px solid var(--border-light);
}

.panel-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
}

.panel-count {
  font-size: 0.85rem;
  color: var(--text-light);
  background: var(--bg-white);
  padding: 0.2rem 0.6rem;
  border-radius: 10px;
  border: 1px solid var(--border-light);
}

.panel-body {
  padding: 1rem;
  max-height: 600px;
  overflow-y: auto;
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
  font-size: 1.2rem;
  color: var(--border-color);
  transition: all var(--transition-fast);
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

.review-images {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-top: 0.6rem;
}

.review-thumb {
  width: 70px;
  height: 70px;
  object-fit: cover;
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-light);
  cursor: pointer;
  transition: transform var(--transition-fast);
}

.review-thumb:hover {
  transform: scale(1.08);
}

.image-viewer-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.9);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}

.image-viewer-content {
  position: relative;
  max-width: 90vw;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.viewer-close {
  position: absolute;
  top: -40px;
  right: 0;
  background: none;
  border: none;
  color: white;
  font-size: 32px;
  cursor: pointer;
}

.viewer-img {
  max-width: 90vw;
  max-height: 80vh;
  object-fit: contain;
  border-radius: var(--radius-sm);
}

.viewer-nav {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background: rgba(255,255,255,0.2);
  border: none;
  color: white;
  font-size: 40px;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background var(--transition);
}

.viewer-nav:hover {
  background: rgba(255,255,255,0.4);
}

.viewer-prev { left: -60px; }
.viewer-next { right: -60px; }

.viewer-counter {
  color: white;
  margin-top: 1rem;
  font-size: 0.9rem;
}

.viewer-download {
  color: var(--primary-light);
  margin-top: 0.8rem;
  text-decoration: none;
  cursor: pointer;
}

.viewer-download:hover {
  text-decoration: underline;
}

@media (max-width: 900px) {
  .log-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .viewer-prev { left: 10px; }
  .viewer-next { right: 10px; }
}

/* 房间状态表格 */
.toolbar-sort {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  margin-left: auto;
}

.sort-label {
  color: var(--text-light);
  font-size: 0.85rem;
}

.sort-select {
  padding: 0.35rem 0.6rem;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  background: var(--bg-white);
  color: var(--text-primary);
  font-size: 0.85rem;
  outline: none;
  cursor: pointer;
}

.sort-select:focus {
  border-color: var(--primary-color);
}

.room-table-wrapper {
  margin-top: 0.5rem;
}

.room-table-wrapper tbody tr {
  transition: background-color 0.2s ease;
}

.room-table-wrapper th {
  background-color: var(--bg-light);
  font-weight: 600;
  color: var(--text-primary);
  white-space: nowrap;
  padding: 0.75rem 0.8rem;
  text-align: left;
  border-bottom: 1px solid var(--border-light);
}

.room-table-wrapper th.sortable {
  cursor: pointer;
  user-select: none;
  transition: background-color 0.2s ease;
}

.room-table-wrapper th.sortable:hover {
  background-color: var(--status-info-bg);
}

.room-table-wrapper th.sortable.sorted {
  background-color: var(--status-info-bg);
  color: var(--primary-color);
}

.sort-icon {
  margin-left: 0.3rem;
  font-size: 0.8rem;
}

.panel-header-right {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.page-size-selector {
  display: flex;
  align-items: center;
  gap: 0.4rem;
}

.room-status-pagination {
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
  align-items: center;
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid var(--border-light);
}

.ellipsis {
  padding: 0 0.3rem;
  color: var(--text-light);
  display: flex;
  align-items: center;
  font-weight: bold;
}

.cell-number {
  font-weight: 700;
  color: var(--text-primary);
}

.cell-price {
  font-weight: 600;
  color: var(--primary-color);
}

.guest-name {
  color: var(--text-primary);
  font-weight: 500;
}

.no-guest {
  color: var(--text-light);
}

.row-status-success { }
.row-status-warning { background-color: #fffdf5; }
.row-status-info { background-color: #f6faff; }
.row-status-danger { background-color: #fefafa; }

td .btn-sm {
  padding: 0.3rem 0.7rem;
  font-size: 0.8rem;
  white-space: nowrap;
}

.cleaning-badge {
  display: inline-block;
  padding: 0.2rem 0.6rem;
  border-radius: var(--radius-sm);
  font-size: 0.8rem;
  font-weight: 500;
  white-space: nowrap;
}

.cleaning-pending {
  color: #e6a23c;
  background: #fdf6ec;
  border: 1px solid #f5dab1;
  cursor: pointer;
}

.cleaning-pending:hover {
  background: #faead7;
}

.cleaning-done {
  color: #67c23a;
  background: #f0f9eb;
  border: 1px solid #c2e7b0;
}

.cell-cleaning {
  text-align: center;
}

.cell-user {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.cell-date {
  white-space: nowrap;
  font-size: 0.85rem;
}
</style>
