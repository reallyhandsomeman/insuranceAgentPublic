import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    meta: {
      title: '首页 - AI保险智能体应用平台',
      description: 'AI保险智能体应用平台提供AI保险对话机器人和AI保险智能体服务，满足您的各种AI对话需求'
    }
  },
  {
    path: '/insurance-master',
    name: 'InsuranceMaster',
    component: () => import('../views/InsuranceMaster.vue'),
    meta: {
      title: 'AI保险对话机器人 - AI超级智能体应用平台',
      description: 'AI保险对话机器人是AI智能体应用平台的专业顾问，帮你解答各种保险问题'
    }
  },
  {
    path: '/super-agent',
    name: 'SuperAgent',
    component: () => import('../views/SuperAgent.vue'),
    meta: {
      title: 'AI保险智能体 - AI智能体应用平台',
      description: 'AI保险智能体是AI智能体应用平台的全能助手，能解答各类专业问题，提供精准建议和解决方案'
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局导航守卫，设置文档标题
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = to.meta.title
  }
  next()
})

export default router 