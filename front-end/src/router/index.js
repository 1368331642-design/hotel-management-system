import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue')
  },
  {
    path: '/login',
    name: 'LoginEntry',
    component: () => import('../views/LoginEntry.vue')
  },
  {
    path: '/login/select',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue')
  },
  {
    path: '/rooms',
    name: 'Rooms',
    component: () => import('../views/Rooms.vue')
  },
  {
    path: '/booking',
    name: 'Booking',
    component: () => import('../views/Booking.vue'),
    meta: {
      requiresAuth: true
    }
  },
  {
    path: '/orders',
    name: 'Orders',
    component: () => import('../views/Orders.vue'),
    meta: {
      requiresAuth: true
    }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/Profile.vue'),
    meta: {
      requiresAuth: true
    }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('../views/Admin.vue'),
    meta: {
      requiresAuth: true,
      requiresAdmin: true
    }
  },
  {
    path: '/front-desk',
    name: 'FrontDesk',
    component: () => import('../views/FrontDesk.vue'),
    meta: {
      requiresAuth: true
    }
  },
  {
    path: '/room-status',
    name: 'RoomStatus',
    component: () => import('../views/RoomStatus.vue'),
    meta: {
      requiresAuth: true
    }
  },
  {
    path: '/front-service',
    name: 'FrontService',
    component: () => import('../views/FrontService.vue'),
    meta: {
      requiresAuth: true
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫，用于权限控制
router.beforeEach((to, from, next) => {
  const user = sessionStorage.getItem('user')
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
  
  if (requiresAuth && !user) {
    next('/login')
  } else {
    next()
  }
})

export default router