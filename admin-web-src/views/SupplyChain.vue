<template>
  <div>
    <el-card>
      <div slot="header" class="head">
        <el-tabs v-model="activeTab" style="margin-bottom: -10px;">
          <el-tab-pane label="采购计划" name="采购计划" />
          <el-tab-pane label="供应商管理" name="供应商管理" />
          <el-tab-pane label="原料管理" name="原料管理" />
        </el-tabs>
        <div>
          <el-button v-if="activeTab === '采购计划' && $hasPermission('supply:procurement:add')" size="small" type="success" @click="purchaseDialog = true">新增采购</el-button>
          <el-button v-if="activeTab === '供应商管理' && $hasPermission('supply:supplier:add')" size="small" type="success" @click="supplierDialog = true">新增供应商</el-button>
          <el-button v-if="activeTab === '原料管理'" size="small" type="success" @click="openMaterialDialog()">新增原料</el-button>
        </div>
      </div>

      <el-form v-if="activeTab === '采购计划'" :inline="true" :model="procurementFilters" size="small" class="filter-form">
        <el-form-item label="关键字">
          <el-input v-model="procurementFilters.keyword" clearable placeholder="单号/项目/供应商" @keyup.enter.native="searchProcurements" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="procurementFilters.status" style="width: 120px;">
            <el-option label="全部" value="all" />
            <el-option label="待审核" value="pending" />
            <el-option label="已审批" value="approved" />
            <el-option label="运输中" value="in_transit" />
            <el-option label="已收货" value="received" />
            <el-option label="已完成" value="completed" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchProcurements">查询</el-button>
          <el-button @click="resetProcurementFilters">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table v-if="activeTab === '采购计划'" v-loading="loadingProcurements" :data="procurements" stripe>
        <el-table-column prop="id" label="计划单号" width="170" />
        <el-table-column prop="item" label="采购项目" width="160" />
        <el-table-column prop="supplier" label="供应商" width="180" />
        <el-table-column prop="quantity" label="数量" width="100" />
        <el-table-column prop="expectedDate" label="预计到货" width="120" />
        <el-table-column prop="cost" label="预估成本" width="110" />
        <el-table-column prop="statusLabel" label="状态" width="100">
          <template slot-scope="scope"><el-tag :type="statusType(scope.row.status)">{{ scope.row.statusLabel }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template slot-scope="scope">
            <el-button v-if="scope.row.status === 'pending' && $hasPermission('supply:procurement:approve')" size="mini" type="warning" @click="updatePurchaseStatus(scope.row, 'approved', '审核')">审核</el-button>
            <el-button v-if="(scope.row.status === 'approved' || scope.row.status === 'in_transit') && $hasPermission('supply:procurement:receive')" size="mini" type="primary" @click="updatePurchaseStatus(scope.row, 'received', '确认收货')">确认收货</el-button>
            <el-button v-if="scope.row.status === 'received'" size="mini" type="success" @click="updatePurchaseStatus(scope.row, 'completed', '完成')">完成</el-button>
            <el-button v-if="$hasPermission('supply:procurement:detail')" size="mini" @click="showDetails(scope.row, 'purchase')">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="activeTab === '采购计划'" class="pager-wrap">
        <el-pagination
          :current-page="procurementPage"
          :page-size="procurementPageSize"
          :total="procurementTotal"
          layout="total, sizes, prev, pager, next"
          :page-sizes="[5, 10, 20]"
          @current-change="onProcurementPageChange"
          @size-change="onProcurementSizeChange"
        />
      </div>

      <el-form v-if="activeTab === '供应商管理'" :inline="true" :model="supplierFilters" size="small" class="filter-form">
        <el-form-item label="关键字">
          <el-input v-model="supplierFilters.keyword" clearable placeholder="编号/名称/联系人" @keyup.enter.native="searchSuppliers" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="supplierFilters.status" style="width: 100px;">
            <el-option label="全部" value="all" />
            <el-option label="正常" value="正常" />
            <el-option label="异常" value="异常" />
          </el-select>
        </el-form-item>
        <el-form-item label="品类">
          <el-select v-model="supplierFilters.category" style="width: 130px;">
            <el-option label="全部" value="all" />
            <el-option v-for="c in supplierCategories" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchSuppliers">查询</el-button>
          <el-button @click="resetSupplierFilters">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table v-if="activeTab === '供应商管理'" v-loading="loadingSuppliers" :data="suppliers" stripe>
        <el-table-column prop="id" label="供应商编号" width="140" />
        <el-table-column prop="name" label="名称" width="200" />
        <el-table-column prop="category" label="主营品类" width="140" />
        <el-table-column prop="contact" label="联系人" width="100" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="rating" label="评分" width="90">
          <template slot-scope="scope">★ {{ scope.row.rating }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template slot-scope="scope"><el-tag :type="scope.row.status === '正常' ? 'success' : 'danger'">{{ scope.row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template slot-scope="scope"><el-button v-if="$hasPermission('supply:supplier:detail')" size="mini" @click="showDetails(scope.row, 'supplier')">详情</el-button></template>
        </el-table-column>
      </el-table>
      <div v-if="activeTab === '供应商管理'" class="pager-wrap">
        <el-pagination
          :current-page="supplierPage"
          :page-size="supplierPageSize"
          :total="supplierTotal"
          layout="total, sizes, prev, pager, next"
          :page-sizes="[5, 10, 20]"
          @current-change="onSupplierPageChange"
          @size-change="onSupplierSizeChange"
        />
      </div>

      <!-- 原料管理 -->
      <el-form v-if="activeTab === '原料管理'" :inline="true" :model="materialFilters" size="small" class="filter-form">
        <el-form-item label="关键字">
          <el-input v-model="materialFilters.keyword" clearable placeholder="名称/编号" @keyup.enter.native="searchMaterials" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="materialFilters.category" style="width: 120px;">
            <el-option label="全部" value="all" />
            <el-option v-for="c in materialCategories" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchMaterials">查询</el-button>
          <el-button @click="resetMaterialFilters">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table v-if="activeTab === '原料管理'" v-loading="loadingMaterials" :data="materials" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="名称" width="140" />
        <el-table-column prop="sku" label="SKU" width="120" />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column prop="currentStock" label="当前库存" width="100" />
        <el-table-column label="操作" width="160" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" @click="openMaterialDialog(scope.row)">编辑</el-button>
            <el-button size="mini" type="danger" @click="removeMaterial(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="activeTab === '原料管理'" class="pager-wrap">
        <el-pagination
          :current-page="materialPage"
          :page-size="materialPageSize"
          :total="materialTotal"
          layout="total, sizes, prev, pager, next"
          :page-sizes="[5, 10, 20]"
          @current-change="onMaterialPageChange"
          @size-change="onMaterialSizeChange"
        />
      </div>

    </el-card>

    <el-dialog :visible.sync="purchaseDialog" title="新增采购单" width="520px">
      <el-form :model="purchaseForm" label-width="90px">
        <el-form-item label="采购原料">
          <el-select v-model="purchaseForm.materialId" filterable placeholder="请选择原料">
            <el-option v-for="m in materialOptions" :key="m.id" :label="m.name" :value="m.id" />
          </el-select>
          <span v-if="selectedMaterialUnit" style="margin-left: 8px; color: #909399;">单位：{{ selectedMaterialUnit }}</span>
        </el-form-item>
        <el-form-item label="数量">
          <el-input v-model="purchaseForm.quantity" type="number">
            <template slot="append">{{ selectedMaterialUnit }}</template>
          </el-input>
        </el-form-item>
        <el-form-item label="供应商">
          <el-select v-model="purchaseForm.supplierId" filterable>
            <el-option v-for="s in supplierOptions" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="预计到货"><el-date-picker v-model="purchaseForm.expectedDate" value-format="yyyy-MM-dd" type="date" /></el-form-item>
        <el-form-item label="单价"><el-input v-model="purchaseForm.unitPrice" type="number" /></el-form-item>
      </el-form>
      <span slot="footer"><el-button @click="purchaseDialog = false">取消</el-button><el-button type="success" @click="addPurchase">提交采购单</el-button></span>
    </el-dialog>

    <el-dialog :visible.sync="supplierDialog" title="新增供应商" width="520px">
      <el-form :model="supplierForm" label-width="90px">
        <el-form-item label="供应商名称"><el-input v-model="supplierForm.name" /></el-form-item>
        <el-form-item label="主营品类"><el-input v-model="supplierForm.category" /></el-form-item>
        <el-form-item label="联系人"><el-input v-model="supplierForm.contact" /></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="supplierForm.phone" /></el-form-item>
      </el-form>
      <span slot="footer"><el-button @click="supplierDialog = false">取消</el-button><el-button type="success" @click="addNewSupplier">保存供应商</el-button></span>
    </el-dialog>

    <el-dialog :visible.sync="detailDialog" :title="detailTitle" width="640px">
      <el-descriptions :column="2" border>
        <el-descriptions-item v-for="(value, key) in detailFields" :key="key" :label="key">{{ value }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog :visible.sync="materialDialog" :title="editingMaterial ? '编辑原料' : '新增原料'" width="520px">
      <el-form :model="materialForm" label-width="90px">
        <el-form-item label="名称"><el-input v-model="materialForm.name" /></el-form-item>
        <el-form-item label="SKU"><el-input v-model="materialForm.sku" /></el-form-item>
        <el-form-item label="分类"><el-input v-model="materialForm.category" /></el-form-item>
        <el-form-item label="单位"><el-input v-model="materialForm.unit" /></el-form-item>
      </el-form>
      <span slot="footer"><el-button @click="materialDialog = false">取消</el-button><el-button type="success" @click="saveMaterial">保存</el-button></span>
    </el-dialog>
  </div>
</template>

<script>
import {
  listSuppliers,
  addSupplier,
  listProcurements,
  addProcurement,
  updateProcurementStatus,
  listMaterials,
  listAllMaterials,
  listMaterialCategories,
  addMaterial,
  updateMaterial,
  deleteMaterial
} from '@/api/modules/supply';

export default {
  name: 'SupplyChain',
  data() {
    return {
      activeTab: '采购计划',
      suppliers: [],
      supplierOptions: [],
      procurements: [],
      procurementPage: 1,
      procurementPageSize: 10,
      procurementTotal: 0,
      procurementFilters: {
        keyword: '',
        status: 'all'
      },
      supplierPage: 1,
      supplierPageSize: 10,
      supplierTotal: 0,
      supplierFilters: {
        keyword: '',
        status: 'all',
        category: 'all'
      },
      purchaseDialog: false,
      supplierDialog: false,
      materialDialog: false,
      detailDialog: false,
      detailType: 'purchase',
      detailFields: {},
      purchaseForm: { materialId: null, supplierId: null, quantity: '', expectedDate: '', unitPrice: '' },
      supplierForm: { name: '', category: '', contact: '', phone: '' },
      materialForm: { name: '', sku: '', category: '', unit: '' },
      editingMaterial: null,
      materials: [],
      materialOptions: [],
      materialCategories: [],
      materialFilters: {
        keyword: '',
        category: 'all'
      },
      materialPage: 1,
      materialPageSize: 10,
      materialTotal: 0,
      loadingMaterials: false,
      loadingProcurements: false,
      loadingSuppliers: false
    };
  },
  computed: {
    supplierCategories() {
      return Array.from(new Set(this.supplierOptions.map((item) => item.category).filter(Boolean)));
    },
    selectedMaterialUnit() {
      if (!this.purchaseForm.materialId) return '';
      const m = this.materialOptions.find(o => o.id === this.purchaseForm.materialId);
      return m ? m.unit : '';
    },
    detailTitle() {
      if (this.detailType === 'purchase') return '采购单详情';
      return '供应商详情';
    }
  },
  watch: {
    activeTab(value) {
      if (value === '采购计划') this.loadProcurements();
      if (value === '供应商管理') this.loadSuppliers();
      if (value === '原料管理') this.loadMaterials();
    }
  },
  async created() {
    await this.loadSuppliers();
    await this.loadSupplierOptions();
    await this.loadProcurements();
    await this.loadMaterialOptions();
    await this.loadMaterialCategories();
  },
  methods: {
    async loadSuppliers() {
      this.loadingSuppliers = true;
      try {
        const res = await listSuppliers({
          page: this.supplierPage,
          pageSize: this.supplierPageSize,
          keyword: this.supplierFilters.keyword,
          status: this.supplierFilters.status,
          category: this.supplierFilters.category
        });
        if (res && Array.isArray(res.list)) {
          this.suppliers = res.list;
          this.supplierTotal = Number(res.total || 0);
        }
      } catch (error) {} finally {
        this.loadingSuppliers = false;
      }
    },
    async loadSupplierOptions() {
      try {
        const res = await listSuppliers({ page: 1, pageSize: 999 });
        if (res && Array.isArray(res.list)) {
          this.supplierOptions = res.list;
        }
      } catch (error) {}
    },
    async loadProcurements() {
      this.loadingProcurements = true;
      try {
        const res = await listProcurements({
          page: this.procurementPage,
          pageSize: this.procurementPageSize,
          keyword: this.procurementFilters.keyword,
          status: this.procurementFilters.status
        });
        if (res && Array.isArray(res.list)) {
          this.procurements = res.list;
          this.procurementTotal = Number(res.total || 0);
        }
      } catch (error) {} finally {
        this.loadingProcurements = false;
      }
    },
    onProcurementPageChange(page) {
      this.procurementPage = page;
      this.loadProcurements();
    },
    onProcurementSizeChange(size) {
      this.procurementPageSize = size;
      this.procurementPage = 1;
      this.loadProcurements();
    },
    searchProcurements() {
      this.procurementPage = 1;
      this.loadProcurements();
    },
    resetProcurementFilters() {
      this.procurementFilters = { keyword: '', status: 'all' };
      this.procurementPage = 1;
      this.loadProcurements();
    },
    onSupplierPageChange(page) {
      this.supplierPage = page;
      this.loadSuppliers();
    },
    onSupplierSizeChange(size) {
      this.supplierPageSize = size;
      this.supplierPage = 1;
      this.loadSuppliers();
    },
    searchSuppliers() {
      this.supplierPage = 1;
      this.loadSuppliers();
    },
    resetSupplierFilters() {
      this.supplierFilters = { keyword: '', status: 'all', category: 'all' };
      this.supplierPage = 1;
      this.loadSuppliers();
    },
    statusType(status) {
      if (status === 'received' || status === 'completed') return 'success';
      if (status === 'in_transit' || status === 'approved') return 'primary';
      if (status === 'pending') return 'warning';
      return 'info';
    },
    async addPurchase() {
      if (!this.purchaseForm.materialId || !this.purchaseForm.quantity) return this.$message.error('请填写必填项');
      try {
        await addProcurement({
          materialId: this.purchaseForm.materialId,
          supplierId: this.purchaseForm.supplierId,
          quantity: Number(this.purchaseForm.quantity),
          unitPrice: this.purchaseForm.unitPrice ? Number(this.purchaseForm.unitPrice) : undefined,
          expectedDate: this.purchaseForm.expectedDate
        });
      } catch (error) {}
      this.procurementPage = 1;
      await this.loadProcurements();
      this.purchaseDialog = false;
      this.purchaseForm = { materialId: null, supplierId: null, quantity: '', expectedDate: '', unitPrice: '' };
      this.$message.success('采购单已新增');
    },
    async addNewSupplier() {
      if (!this.supplierForm.name || !this.supplierForm.contact) return this.$message.error('请填写必填项');
      const row = { id: `SUP-00${this.suppliers.length + 1}`, ...this.supplierForm, status: '正常', rating: 5 };
      try {
        await addSupplier(row);
      } catch (error) {}
      this.supplierPage = 1;
      await this.loadSuppliers();
      await this.loadSupplierOptions();
      this.supplierDialog = false;
      this.supplierForm = { name: '', category: '', contact: '', phone: '' };
      this.$message.success('供应商已新增');
    },
    updatePurchaseStatus(row, status, action) {
      this.$confirm(`您确定要对单号 ${row.id} 执行${action}操作吗？`, '确认操作', { type: 'warning' }).then(async () => {
        try {
          await updateProcurementStatus(row.id, status);
        } catch (error) {}
        await this.loadProcurements();
        this.$message.success(`操作成功`);
      }).catch(() => {});
    },
    // ── 原料管理 ──
    async loadMaterials() {
      this.loadingMaterials = true;
      try {
        const res = await listMaterials({
          page: this.materialPage,
          pageSize: this.materialPageSize,
          keyword: this.materialFilters.keyword || undefined,
          category: this.materialFilters.category === 'all' ? undefined : this.materialFilters.category
        });
        if (res && Array.isArray(res.list)) {
          this.materials = res.list;
          this.materialTotal = Number(res.total || 0);
        }
      } catch (error) {} finally {
        this.loadingMaterials = false;
      }
    },
    async loadMaterialCategories() {
      try {
        const res = await listMaterialCategories();
        if (Array.isArray(res)) this.materialCategories = res;
      } catch (error) {}
    },
    searchMaterials() {
      this.materialPage = 1;
      this.loadMaterials();
    },
    resetMaterialFilters() {
      this.materialFilters = { keyword: '', category: 'all' };
      this.materialPage = 1;
      this.loadMaterials();
    },
    async loadMaterialOptions() {
      try {
        const res = await listAllMaterials();
        if (Array.isArray(res)) this.materialOptions = res;
      } catch (error) {}
    },
    onMaterialPageChange(page) {
      this.materialPage = page;
      this.loadMaterials();
    },
    onMaterialSizeChange(size) {
      this.materialPageSize = size;
      this.materialPage = 1;
      this.loadMaterials();
    },
    openMaterialDialog(row) {
      if (row) {
        this.editingMaterial = row;
        this.materialForm = {
          name: row.name, sku: row.sku, category: row.category,
          unit: row.unit
        };
      } else {
        this.editingMaterial = null;
        this.materialForm = { name: '', sku: '', category: '', unit: '' };
      }
      this.materialDialog = true;
    },
    async saveMaterial() {
      if (!this.materialForm.name) return this.$message.error('请填写原料名称');
      try {
        if (this.editingMaterial) {
          await updateMaterial(this.editingMaterial.id, this.materialForm);
        } else {
          await addMaterial(this.materialForm);
        }
      } catch (error) { return; }
      this.materialDialog = false;
      await this.loadMaterials();
      await this.loadMaterialOptions();
      this.$message.success(this.editingMaterial ? '原料已更新' : '原料已新增');
    },
    async removeMaterial(row) {
      try {
        await this.$confirm(`确认删除原料 ${row.name} 吗？`, '确认删除', { type: 'warning' });
        await deleteMaterial(row.id);
        await this.loadMaterials();
        await this.loadMaterialOptions();
        this.$message.success('已删除');
      } catch (error) {}
    },
    showDetails(row, type) {
      this.detailType = type;
      this.detailFields = row;
      this.detailDialog = true;
    }
  }
};
</script>

<style scoped>
.metric { font-size: 14px; color: #475569; }
.metric b { color: #0f172a; font-size: 24px; margin: 0 6px; }
.head { display: flex; align-items: center; justify-content: space-between; }
.filter-form { margin-bottom: -8px; }
.pager-wrap { margin-top: 12px; display: flex; justify-content: flex-end; }

@media (max-width: 768px) {
  .head { flex-direction: column; align-items: flex-start; gap: 10px; }
}
</style>
