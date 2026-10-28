/*
 * 项目启动入口方法
 *
 * @Author:    1024创新实验室-主任：卓大
 * @Date:      2022-09-06 20:59:23
 * @Wechat:    zhuda1024
 * @Email:     lab1024@163.com
 * @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
 */

import { createApp } from 'vue';
import Antd from 'ant-design-vue';
import * as antIcons from '@ant-design/icons-vue';
import lodash from 'lodash';
import JsonViewer from 'vue3-json-viewer';
import 'vue3-json-viewer/dist/index.css';
import App from './App.vue';
import constantsInfo from '/@/constants/index';
import { privilegeDirective } from '/@/directives/privilege';
import i18n from '/@/i18n/index';
import privilegePlugin from '/@/plugins/privilege-plugin';
import dictPlugin from '/@/plugins/dict-plugin';
import smartEnumPlugin from '/@/plugins/smart-enums-plugin';
import { router } from '/@/router';
import { store } from '/@/store';
import { useUserStore } from '/@/store/modules/system/user';
import { useAppConfigStore } from '/@/store/modules/system/app-config';
import 'ant-design-vue/dist/reset.css';
import '/@/theme/index.less';
import '/@/utils/ployfill';

// 静态预览：重置旧布局配置，避免浏览器缓存重新启用帮助栏。
const appConfigStore = useAppConfigStore(store);
appConfigStore.reset();
appConfigStore.$patch({
  layout: 'side',
  pageTagLocation: 'center',
  helpDocFlag: false,
  helpDocExpandFlag: false,
  watermarkFlag: false,
});

// 静态预览：直接提供展示信息，不调用登录、菜单、未读消息接口。
useUserStore(store).$patch({
  token: '',
  actualName: '访客',
  administratorFlag: false,
  // 忽略以前登录时缓存的业务标签页。
  tagNav: [],
  // 菜单改为本地维护；1001 要与下一步路由中的 meta.id 一致。
  menuTree: [
    {
      menuId: 1001,
      parentId: 0,
      menuName: '首页',
      path: '/home',
      icon: 'HomeOutlined',
      visibleFlag: true,
      disabledFlag: false,
    },
  ],
});

const app = createApp(App);

// 先安装状态管理，再安装路由，保证布局组件能够读取状态。
app
  .use(store)
  .use(router)
  .use(i18n)
  .use(Antd)
  .use(smartEnumPlugin, constantsInfo)
  .use(privilegePlugin)
  .use(dictPlugin)
  .use(JsonViewer);

// 保留原有权限指令注册，方便以后继续开发。
app.directive('privilege', {
  mounted(el, binding) {
    privilegeDirective(el, binding);
  },
});

// 保留现有布局和图表使用的图标、工具。
Object.keys(antIcons).forEach((key) => {
  app.component(key, antIcons[key]);
});
app.config.globalProperties.$antIcons = antIcons;
app.config.globalProperties.$lodash = lodash;

// 不再等待任何后台接口，只等待本地路由准备完成。
await router.isReady();
app.mount('#app');