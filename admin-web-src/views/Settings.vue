<template>
  <div>
    <div class="head">
      <div>
        <h2>优惠与VIP规则</h2>
        <p>管理VIP升级门槛、积分规则及充值赠送</p>
      </div>
      <div>
        <el-button v-if="$hasPermission('settings:save')" type="success" @click="saveAll">保存设置</el-button>
      </div>
    </div>

    <el-card>
      <div slot="header">VIP与积分规则设置</div>
      <el-form :model="vipSettings" label-width="200px">
        <el-form-item label="VIP 升级门槛 (累计消费/元)"><el-input v-model.number="vipSettings.upgradeThreshold" type="number" style="max-width: 260px;" /></el-form-item>
        <el-form-item label="VIP 专属折扣 (%)"><el-input v-model.number="vipSettings.discount" type="number" style="max-width: 260px;" /></el-form-item>
        <el-form-item label="消费积分比例 (1元 = ? 积分)"><el-input v-model.number="vipSettings.pointsRatio" type="number" style="max-width: 260px;" /></el-form-item>
        <el-form-item label="积分抵扣比例 (? 积分 = 1元)"><el-input v-model.number="vipSettings.pointsDeduction" type="number" style="max-width: 260px;" /></el-form-item>
      </el-form>

      <div class="sub-head">充值赠送规则</div>
      <el-table v-loading="loadingRules" :data="rechargeRules" size="mini" border>
        <el-table-column label="充值满(元)">
          <template slot-scope="scope"><el-input v-model.number="scope.row.amount" type="number" size="mini" /></template>
        </el-table-column>
        <el-table-column label="赠送(元)">
          <template slot-scope="scope"><el-input v-model.number="scope.row.bonus" type="number" size="mini" /></template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template slot-scope="scope"><el-button type="text" style="color: #e11d48;" @click="delRechargeRule(scope.row.id)">删除</el-button></template>
        </el-table-column>
      </el-table>
      <div class="pager-wrap">
        <el-pagination
          :current-page="rulePage"
          :page-size="rulePageSize"
          :total="ruleTotal"
          layout="total, sizes, prev, pager, next"
          :page-sizes="[5, 10, 20]"
          @current-change="onRulePageChange"
          @size-change="onRuleSizeChange"
        />
      </div>
      <el-button type="text" @click="addRechargeRule">+ 新增规则</el-button>
    </el-card>
  </div>
</template>

<script>
import { getSettings, saveSettings, listRechargeRules } from '@/api/modules/settings';

export default {
  name: 'Settings',
  data() {
    return {
      vipSettings: { upgradeThreshold: 0, discount: 0, pointsRatio: 0, pointsDeduction: 0 },
      rechargeRules: [],
      rulePage: 1,
      rulePageSize: 10,
      ruleTotal: 0,
      loadingRules: false
    };
  },
  async created() {
    try {
      const data = await getSettings();
      if (data && data.vipSettings) this.vipSettings = data.vipSettings;
    } catch (error) {}
    await this.loadRechargeRules();
  },
  methods: {
    async loadRechargeRules() {
      this.loadingRules = true;
      try {
        const res = await listRechargeRules({ page: this.rulePage, pageSize: this.rulePageSize });
        if (res && Array.isArray(res.list)) {
          this.rechargeRules = res.list;
          this.ruleTotal = Number(res.total || 0);
        }
      } catch (error) {} finally {
        this.loadingRules = false;
      }
    },
    onRulePageChange(page) {
      this.rulePage = page;
      this.loadRechargeRules();
    },
    onRuleSizeChange(size) {
      this.rulePageSize = size;
      this.rulePage = 1;
      this.loadRechargeRules();
    },
    async addRechargeRule() {
      const all = await listRechargeRules({ page: 1, pageSize: 999 });
      const allRules = Array.isArray(all && all.list) ? all.list : [];
      const id = Math.max(0, ...allRules.map((r) => r.id)) + 1;
      const nextRules = [...allRules, { id, amount: 0, bonus: 0 }];
      try {
        await saveSettings({ rechargeRules: nextRules });
      } catch (error) {}
      this.rulePage = 1;
      await this.loadRechargeRules();
    },
    async delRechargeRule(id) {
      const all = await listRechargeRules({ page: 1, pageSize: 999 });
      const allRules = Array.isArray(all && all.list) ? all.list : [];
      const nextRules = allRules.filter((r) => r.id !== id);
      try {
        await saveSettings({ rechargeRules: nextRules });
      } catch (error) {}
      await this.loadRechargeRules();
      this.$message.success('充值赠送规则已删除');
    },
    async saveAll() {
      let mergedRules = [];
      try {
        const all = await listRechargeRules({ page: 1, pageSize: 999 });
        const allRules = Array.isArray(all && all.list) ? all.list : [];
        const pageRuleMap = new Map(this.rechargeRules.map((item) => [item.id, item]));
        mergedRules = allRules.map((item) => pageRuleMap.get(item.id) || item);
      } catch (error) {
        mergedRules = this.rechargeRules;
      }
      const payload = {
        vipSettings: this.vipSettings,
        rechargeRules: mergedRules
      };
      try {
        await saveSettings(payload);
      } catch (error) {}
      await this.loadRechargeRules();
      this.$message.success('VIP与优惠规则已保存');
    }
  }
};
</script>

<style scoped>
.head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
h2 { margin: 0; color: #0f172a; }
p { margin: 4px 0 0 0; color: #64748b; }
.sub-head { margin: 12px 0 8px; color: #334155; font-weight: 600; }
.pager-wrap { margin-top: 12px; display: flex; justify-content: flex-end; }
</style>
