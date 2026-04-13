<template>
  <div>
    <div class="head">
      <div>
        <h2>VIP与优惠券配置</h2>
        <p>管理VIP等级规则和优惠券模板</p>
      </div>
    </div>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="VIP等级配置" name="vip">
        <el-card>
          <div slot="header" class="tab-head">
            <span>VIP等级列表</span>
            <el-button v-if="$hasPermission('vip-coupon:edit')" type="primary" size="small" @click="openVipDialog()">新增等级</el-button>
          </div>
          <el-table :data="vipLevels" v-loading="loadingVip" size="small" border>
            <el-table-column prop="level" label="等级" width="80" />
            <el-table-column prop="minSpend" label="消费金额(元)" width="140" />
            <el-table-column label="折扣(%)" width="120">
              <template slot-scope="scope">{{ scope.row.discount != null ? scope.row.discount : '-' }}</template>
            </el-table-column>
            <el-table-column label="每天积分" width="120">
              <template slot-scope="scope">{{ scope.row.dailyPoints != null ? scope.row.dailyPoints : '-' }}</template>
            </el-table-column>
            <el-table-column label="操作" width="160" v-if="$hasPermission('vip-coupon:edit')">
              <template slot-scope="scope">
                <el-button type="text" @click="openVipDialog(scope.row)">编辑</el-button>
                <el-button type="text" style="color: #e11d48;" @click="removeVipLevel(scope.row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="优惠券配置" name="coupon">
        <el-card>
          <div slot="header" class="tab-head">
            <span>优惠券模板列表</span>
            <el-button v-if="$hasPermission('vip-coupon:edit')" type="primary" size="small" @click="openCouponDialog()">新增优惠券</el-button>
          </div>
          <el-table :data="coupons" v-loading="loadingCoupon" size="small" border>
            <el-table-column prop="title" label="名称" min-width="120" />
            <el-table-column prop="amount" label="面额(元)" width="100" />
            <el-table-column prop="minAmount" label="满减门槛(元)" width="120" />
            <el-table-column label="兑换所需积分" width="120">
              <template slot-scope="scope">{{ scope.row.pointsCost != null ? scope.row.pointsCost : '-' }}</template>
            </el-table-column>
            <el-table-column label="有效天数" width="100">
              <template slot-scope="scope">{{ scope.row.validDays != null ? scope.row.validDays + '天' : '-' }}</template>
            </el-table-column>
            <el-table-column label="状态" width="80">
              <template slot-scope="scope">
                <el-tag :type="scope.row.isActive ? 'success' : 'info'" size="mini">{{ scope.row.isActive ? '启用' : '停用' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="160" v-if="$hasPermission('vip-coupon:edit')">
              <template slot-scope="scope">
                <el-button type="text" @click="openCouponDialog(scope.row)">编辑</el-button>
                <el-button type="text" style="color: #e11d48;" @click="removeCoupon(scope.row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pager-wrap">
            <el-pagination
              :current-page="couponPage"
              :page-size="couponPageSize"
              :total="couponTotal"
              layout="total, sizes, prev, pager, next"
              :page-sizes="[5, 10, 20]"
              @current-change="onCouponPageChange"
              @size-change="onCouponSizeChange"
            />
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- VIP Level Dialog -->
    <el-dialog :title="vipForm.id ? '编辑VIP等级' : '新增VIP等级'" :visible.sync="vipDialog" width="420px">
      <el-form :model="vipForm" label-width="120px" size="small">
        <el-form-item label="等级">
          <el-input-number v-model="vipForm.level" :min="0" :max="10" disabled />
        </el-form-item>
        <el-form-item label="消费金额(元)">
          <el-input v-model.number="vipForm.minSpend" type="number" :disabled="vipForm.level === 0" />
          <span v-if="vipForm.level === 0" style="color:#909399;font-size:12px;">非会员等级，消费金额固定为0</span>
        </el-form-item>
        <el-form-item label="折扣(%)">
          <el-input v-model.number="vipForm.discount" type="number" placeholder="如95表示9.5折" />
        </el-form-item>
        <el-form-item label="每天积分">
          <el-input v-model.number="vipForm.dailyPoints" type="number" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="vipDialog = false">取消</el-button>
        <el-button type="primary" @click="saveVipLevel">确定</el-button>
      </span>
    </el-dialog>

    <!-- Coupon Template Dialog -->
    <el-dialog :title="couponForm.id ? '编辑优惠券' : '新增优惠券'" :visible.sync="couponDialog" width="460px">
      <el-form :model="couponForm" label-width="120px" size="small">
        <el-form-item label="名称">
          <el-input v-model="couponForm.title" />
        </el-form-item>
        <el-form-item label="面额(元)">
          <el-input v-model.number="couponForm.amount" type="number" />
        </el-form-item>
        <el-form-item label="满减门槛(元)">
          <el-input v-model.number="couponForm.minAmount" type="number" />
        </el-form-item>
        <el-form-item label="兑换所需积分">
          <el-input v-model.number="couponForm.pointsCost" type="number" placeholder="此优惠券可用积分兑换，留空则不可兑" />
        </el-form-item>
        <el-form-item label="有效天数">
          <el-input v-model.number="couponForm.validDays" type="number" placeholder="领取后N天有效" />
        </el-form-item>
        <el-form-item label="是否启用">
          <el-switch v-model="couponForm.isActive" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="couponDialog = false">取消</el-button>
        <el-button type="primary" @click="saveCoupon">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  listVipLevels, addVipLevel, updateVipLevel, deleteVipLevel,
  listCouponTemplates, addCouponTemplate, updateCouponTemplate, deleteCouponTemplate
} from '@/api/modules/settings';

export default {
  name: 'VipCouponConfig',
  data() {
    return {
      activeTab: 'vip',
      vipLevels: [],
      loadingVip: false,
      vipDialog: false,
      vipForm: { id: null, level: 0, minSpend: 0, discount: 100, dailyPoints: 0 },
      coupons: [],
      couponPage: 1,
      couponPageSize: 10,
      couponTotal: 0,
      loadingCoupon: false,
      couponDialog: false,
      couponForm: { id: null, title: '', amount: 0, minAmount: 0, pointsCost: null, validDays: 7, isActive: true }
    };
  },
  async created() {
    await this.loadVipLevels();
    await this.loadCoupons();
  },
  methods: {
    async loadVipLevels() {
      this.loadingVip = true;
      try {
        const data = await listVipLevels();
        this.vipLevels = Array.isArray(data) ? data : [];
      } catch (e) {} finally {
        this.loadingVip = false;
      }
    },
    openVipDialog(row) {
      if (row) {
        this.vipForm = { id: row.id, level: row.level, minSpend: row.minSpend, discount: row.discount, dailyPoints: row.dailyPoints };
      } else {
        const nextLevel = this.vipLevels.length > 0 ? this.vipLevels[this.vipLevels.length - 1].level + 1 : 0;
        const defaultMinSpend = nextLevel === 0 ? 0 : '';
        const defaultDiscount = nextLevel === 0 ? 100 : 95;
        this.vipForm = { id: null, level: nextLevel, minSpend: defaultMinSpend, discount: defaultDiscount, dailyPoints: 0 };
      }
      this.vipDialog = true;
    },
    async saveVipLevel() {
      try {
        if (this.vipForm.id) {
          await updateVipLevel(this.vipForm.id, this.vipForm);
        } else {
          await addVipLevel(this.vipForm);
        }
      } catch (e) {
        this.$message.error('保存失败');
        return;
      }
      this.vipDialog = false;
      this.$message.success('保存成功');
      await this.loadVipLevels();
    },
    async removeVipLevel(id) {
      const row = this.vipLevels.find(v => v.id === id);
      if (!row) return;
      const highest = this.vipLevels[this.vipLevels.length - 1];
      if (row.level !== highest.level) {
        this.$message.warning('只能从最高等级开始删除');
        return;
      }
      await this.$confirm('确定删除该VIP等级吗？', '确认', { type: 'warning' });
      try {
        await deleteVipLevel(id);
      } catch (e) {
        this.$message.error('删除失败');
        return;
      }
      this.$message.success('已删除');
      await this.loadVipLevels();
    },
    async loadCoupons() {
      this.loadingCoupon = true;
      try {
        const data = await listCouponTemplates({ page: this.couponPage, pageSize: this.couponPageSize });
        if (data && Array.isArray(data.list)) {
          this.coupons = data.list;
          this.couponTotal = Number(data.total || 0);
        }
      } catch (e) {} finally {
        this.loadingCoupon = false;
      }
    },
    onCouponPageChange(page) {
      this.couponPage = page;
      this.loadCoupons();
    },
    onCouponSizeChange(size) {
      this.couponPageSize = size;
      this.couponPage = 1;
      this.loadCoupons();
    },
    openCouponDialog(row) {
      if (row) {
        this.couponForm = {
          id: row.id, title: row.title, amount: row.amount, minAmount: row.minAmount,
          pointsCost: row.pointsCost, validDays: row.validDays, isActive: row.isActive
        };
      } else {
        this.couponForm = { id: null, title: '', amount: 0, minAmount: 0, pointsCost: null, validDays: 7, isActive: true };
      }
      this.couponDialog = true;
    },
    async saveCoupon() {
      if (!this.couponForm.title) {
        this.$message.error('请输入优惠券名称');
        return;
      }
      try {
        if (this.couponForm.id) {
          await updateCouponTemplate(this.couponForm.id, this.couponForm);
        } else {
          await addCouponTemplate(this.couponForm);
        }
      } catch (e) {
        this.$message.error('保存失败');
        return;
      }
      this.couponDialog = false;
      this.$message.success('保存成功');
      await this.loadCoupons();
    },
    async removeCoupon(id) {
      await this.$confirm('确定删除该优惠券模板吗？', '确认', { type: 'warning' });
      try {
        await deleteCouponTemplate(id);
      } catch (e) {
        this.$message.error('删除失败');
        return;
      }
      this.$message.success('已删除');
      await this.loadCoupons();
    }
  }
};
</script>

<style scoped>
.head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
h2 { margin: 0; color: #0f172a; }
p { margin: 4px 0 0 0; color: #64748b; }
.tab-head { display: flex; align-items: center; justify-content: space-between; }
.pager-wrap { margin-top: 12px; display: flex; justify-content: flex-end; }
</style>
