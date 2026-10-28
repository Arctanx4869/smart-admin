<!--
  * 首页
  *
  * @Author:    1024创新实验室-主任：卓大
  * @Date:      2022-09-12 22:34:00
  * @Wechat:    zhuda1024
  * @Email:     lab1024@163.com
  * @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
  *
-->
<template>
  <div class="preview-home">
    <!-- 静态预览：替换登录用户信息和天气 iframe。 -->
    <a-card title="首页" style="margin-bottom: 10px">
      欢迎访问智能综合办公系统。下方图表使用本地示例数据。
    </a-card>

    <a-row :gutter="[10, 10]">
      <!-- 静态预览：保留页面区域，不挂载原来的公告和日志查询组件。 -->
      <a-col :xs="24" :lg="12">
        <a-card title="公告">
          <a-empty description="暂无内容" />
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="12">
        <a-card title="更新日志">
          <a-empty description="暂无内容" />
        </a-card>
      </a-col>

      <!-- 保留已有图表；数据来自组件内部，不请求后台。 -->
      <a-col :xs="24" :lg="12">
        <Pie />
      </a-col>
      <a-col :xs="24" :lg="12">
        <Category />
      </a-col>
      <a-col :span="24">
        <Gradient />
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
import Pie from './components/echarts/pie.vue';
import Category from './components/echarts/category.vue';
import Gradient from './components/echarts/gradient.vue';
</script>

<style scoped>
/* 内容区独立滚动，避免首页内容超出布局后无法查看。 */
.preview-home {
  height: 100%;
  overflow: auto;
}
</style>