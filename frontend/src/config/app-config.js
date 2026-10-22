export const appDefaultConfig = {
  // i18n 语言选择
  language: 'zh_CN',
  // 布局: side 或者 side-expand 或者 top
  layout: 'side',
  // 侧边菜单宽度 ， 默认为200px
  sideMenuWidth: 200,
  //标签页位置
  pageTagLocation: 'center',
  // 夜间模式
  darkModeFlag: false,
  // 菜单主题
  sideMenuTheme: 'dark',
  // 主题颜色索引
  colorIndex: 0,
  // 顶部菜单页面宽度
  pageWidth: '99%',
  // 圆角
  borderRadius: 6,
  // 菜单展开模式
  menuSingleExpandFlag: true,
  // 标签页
  pageTagFlag: true,
  // 标签页样式: default、 antd、chrome
  pageTagStyle: 'chrome',
  // 面包屑
  breadCrumbFlag: true,
  // 页脚
  footerFlag: true,
  // 帮助文档
  helpDocFlag: true,
  // 帮助文档默认展开
  helpDocExpandFlag: false,
  // 水印
  watermarkFlag: true,
  // 网站名称
  websiteName: '智能综合办公系统',
  // 主题颜色
  primaryColor: '#1677ff',
  // 紧凑
  compactFlag: false,
};

// 新增：侧栏独立配色，供侧栏布局使用。
export const sidebarTheme = {
  token: {
    // 覆盖浅色侧栏容器；不影响配置范围以外的内容区。
    colorBgContainer: '#3490DC',
    colorText: '#FFFFFF',
  },
  components: {
    Layout: {
      // 深色侧栏背景和底部折叠按钮背景。
      colorBgHeader: '#3490DC',
      colorBgTrigger: '#3490DC',
    },
    Menu: {
      // 菜单主体、展开的子菜单及弹出菜单使用统一底色。
      colorItemBg: '#3490DC',
      colorSubItemBg: '#3490DC',

      // 普通、悬停和选中的菜单文字使用白色。
      colorItemText: '#FFFFFF',
      colorItemTextHover: '#FFFFFF',
      colorItemTextSelected: '#FFFFFF',
      colorGroupTitle: '#FFFFFF',

      // 用透明底色区分悬停和选中状态。
      colorItemBgHover: 'rgba(255, 255, 255, 0.12)',
      colorItemBgSelected: 'rgba(0, 0, 0, 0.18)',
    },
  },
};
