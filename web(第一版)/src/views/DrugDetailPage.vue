<template>
  <el-card>
    <template #header>药品详情页</template>
    <el-descriptions :column="1" border>
      <el-descriptions-item label="药品名">{{ drugName }}</el-descriptions-item>
      <el-descriptions-item label="适应症">常见消化道/皮肤问题辅助治疗（示例）</el-descriptions-item>
      <el-descriptions-item label="推荐剂量">请遵医嘱或参考兽医处方</el-descriptions-item>
      <el-descriptions-item label="禁忌症">孕期、幼宠、过敏体质请先咨询兽医</el-descriptions-item>
    </el-descriptions>
    <el-alert
      style="margin-top: 12px"
      type="warning"
      :closable="false"
      title="安全提示：本页面信息仅供参考，处方药必须基于执业兽医电子处方购买。"
    />
  </el-card>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();
const drugName = computed(() => decodeURIComponent(String(route.params.name || '未知药品')));
</script>
