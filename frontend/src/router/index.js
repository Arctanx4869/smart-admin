/*
 * 路由
 *
 * @Author:    1024创新实验室-主任：卓大
 * @Date:      2022-09-06 20:52:04
 * @Wechat:    zhuda1024
 * @Email:     lab1024@163.com
 * @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
 */
// 静态预览：只注册基础首页，不加载登录页和后台动态菜单。
import { createRouter, createWebHashHistory } from 'vue-router';
import SmartLayout from '/@/layout/index.vue';
import { HOME_PAGE_NAME } from '/@/constants/system/home-const';

export const router = createRouter({
  history: createWebHashHistory(import.meta.env.BASE_URL),
  scrollBehavior: () => ({ left: 0, top: 0 }),
  routes: [
    {
      path: '/',
      component: SmartLayout,
      redirect: { name: HOME_PAGE_NAME },
      children: [
        {
          path: 'home',
          name: HOME_PAGE_NAME,
          // id 与 main.js 中本地菜单的 menuId 对应。
          meta: {
            id: '1001',
            title: '首页',
            icon: 'HomeOutlined',
          },
          component: () => import('/@/views/system/home/index.vue'),
        },
      ],
    },
    // 旧登录、个人中心及业务地址统一回首页，不加载原请求组件。
    {
      path: '/:pathMatch(.*)*',
      redirect: { name: HOME_PAGE_NAME },
    },
  ],
});