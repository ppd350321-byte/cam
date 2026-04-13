<template>
  <div>
    <!-- 分类管理 -->
    <el-card style="margin-bottom: 16px">
      <div slot="header" style="display: flex; align-items: center; justify-content: space-between">
        <span>菜品分类</span>
        <el-button v-if="$hasPermission('menu:add')" type="primary" size="small" @click="openCategoryDialog(null)">新增分类</el-button>
      </div>
      <el-table v-loading="loadingCategories" :data="categories" stripe size="small">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="分类名称" width="160" />
        <el-table-column prop="icon" label="图标" width="120" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.isActive ? 'success' : 'info'">{{ scope.row.isActive ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template slot-scope="scope">
            <el-button v-if="$hasPermission('menu:edit')" size="mini" type="warning" plain @click="openCategoryDialog(scope.row)">编辑</el-button>
            <el-button v-if="$hasPermission('menu:delete')" size="mini" type="danger" plain @click="doDeleteCategory(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 菜品管理 -->
    <el-card>
      <div slot="header" style="display: flex; align-items: center; justify-content: space-between">
        <span>菜品列表</span>
        <el-button v-if="$hasPermission('menu:add')" type="primary" size="small" @click="openDishDialog(null)">新增菜品</el-button>
      </div>
      <el-form :inline="true" size="small" class="toolbar">
        <el-form-item>
          <el-input v-model="dishKeyword" placeholder="搜索菜品名称" clearable @clear="loadDishes" />
        </el-form-item>
        <el-form-item>
          <el-select v-model="dishCategoryFilter" placeholder="全部分类" clearable @change="loadDishes">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="small" @click="loadDishes">搜索</el-button>
        </el-form-item>
      </el-form>
      <el-table v-loading="loadingDishes" :data="dishes" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="菜品名称" width="160" />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column label="价格" width="100">
          <template slot-scope="scope">¥{{ Number(scope.row.price).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="VIP价" width="100">
          <template slot-scope="scope">
            <span v-if="scope.row.vipPrice">¥{{ Number(scope.row.vipPrice).toFixed(2) }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column prop="sales" label="销量" width="80" />
        <el-table-column label="上架" width="80">
          <template slot-scope="scope">
            <el-tag :type="scope.row.available ? 'success' : 'info'" size="small">{{ scope.row.available ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template slot-scope="scope">
            <el-button v-if="$hasPermission('menu:edit')" size="mini" type="warning" plain @click="openDishDialog(scope.row)">编辑</el-button>
            <el-button v-if="$hasPermission('menu:delete')" size="mini" type="danger" plain @click="doDeleteDish(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager-wrap">
        <el-pagination
          :current-page="dishPage"
          :page-size="dishPageSize"
          :total="dishTotal"
          layout="total, sizes, prev, pager, next"
          :page-sizes="[10, 20, 50]"
          @current-change="onDishPageChange"
          @size-change="onDishSizeChange"
        />
      </div>
    </el-card>

    <!-- 分类 新增/编辑 弹窗 -->
    <el-dialog :visible.sync="categoryDialogVisible" :title="categoryForm.id ? '编辑分类' : '新增分类'" width="460px">
      <el-form :model="categoryForm" label-width="80px" size="small">
        <el-form-item label="名称">
          <el-input v-model="categoryForm.name" placeholder="分类名称" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="categoryForm.icon" placeholder="图标标识（可选）" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="categoryForm.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="categoryForm.isActive" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="categoryDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="categorySaving" @click="doSaveCategory">确定</el-button>
      </span>
    </el-dialog>

    <!-- 菜品 新增/编辑 弹窗 -->
    <el-dialog :visible.sync="dishDialogVisible" :title="dishForm.id ? '编辑菜品' : '新增菜品'" width="600px">
      <el-form :model="dishForm" label-width="90px" size="small">
        <el-form-item label="菜品名称">
          <el-input v-model="dishForm.name" placeholder="菜品名称" />
        </el-form-item>
        <el-form-item label="所属分类">
          <el-select v-model="dishForm.categoryId" placeholder="选择分类">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="dishForm.description" type="textarea" :rows="2" placeholder="菜品描述（可选）" />
        </el-form-item>
        <el-form-item label="菜品图片">
          <el-upload
            class="dish-image-uploader"
            action="#"
            :auto-upload="false"
            :show-file-list="false"
            accept="image/jpeg,image/png,image/gif,image/webp"
            :on-change="onDishImageSelected"
          >
            <img v-if="dishForm.imageUrl" :src="dishForm.imageUrl" class="dish-image-preview" />
            <i v-else class="el-icon-plus dish-image-uploader-icon"></i>
          </el-upload>
          <el-button v-if="dishForm.imageUrl" type="text" size="mini" style="margin-top: 4px" @click="dishForm.imageUrl = ''">移除图片</el-button>
          <div v-if="dishImageUploading" style="color: #409EFF; font-size: 12px; margin-top: 4px;">上传中...</div>
        </el-form-item>
        <el-form-item label="售价">
          <el-input-number v-model="dishForm.price" :min="0" :precision="2" :step="1" />
        </el-form-item>
        <el-form-item label="原价">
          <el-input-number v-model="dishForm.originalPrice" :min="0" :precision="2" :step="1" />
        </el-form-item>
        <el-form-item label="VIP价">
          <el-switch v-model="dishForm.useCustomVipPrice" active-text="自定义" inactive-text="默认(95折)" style="margin-bottom: 6px" />
          <el-input-number v-if="dishForm.useCustomVipPrice" v-model="dishForm.vipPrice" :min="0" :precision="2" :step="1" />
          <span v-else class="el-form-item__label" style="margin-left: 12px; color: #909399">¥{{ dishForm.price ? (dishForm.price * 0.95).toFixed(2) : '0.00' }}</span>
        </el-form-item>
        <el-form-item label="库存">
          <el-input-number v-model="dishForm.stock" :min="0" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="dishForm.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="dishForm.tags" placeholder="如：新品,招牌（逗号分隔）" />
        </el-form-item>
        <el-form-item label="所需原料">
          <div v-for="(mat, idx) in dishForm.materials" :key="idx" style="display: flex; gap: 8px; margin-bottom: 6px; align-items: center;">
            <el-select v-model="mat.materialId" filterable placeholder="选择原料" size="small" style="width: 160px;" @change="onMaterialSelect(mat)">
              <el-option v-for="m in materialOptions" :key="m.id" :label="m.name" :value="m.id" />
            </el-select>
            <el-input-number v-model="mat.quantityPerServing" :min="0" :precision="3" :step="0.1" size="small" placeholder="用量/份" style="width: 130px;" />
            <el-tag size="small" style="min-width: 50px; text-align: center;">{{ getMaterialUnit(mat.materialId) }}</el-tag>
            <el-button size="mini" type="danger" icon="el-icon-delete" circle @click="dishForm.materials.splice(idx, 1)" />
          </div>
          <el-button size="mini" type="text" icon="el-icon-plus" @click="dishForm.materials.push({ materialId: null, quantityPerServing: 0, unit: '' })">添加原料</el-button>
        </el-form-item>
        <el-form-item label="上架">
          <el-switch v-model="dishForm.available" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dishDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="dishSaving" @click="doSaveDish">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { listCategories, createCategory, updateCategory, deleteCategory, listDishes, createDish, updateDish, deleteDish } from '@/api/modules/menu';
import { uploadImage } from '@/api/modules/upload';
import { listAllMaterials } from '@/api/modules/supply';

export default {
  name: 'MenuManagement',
  data() {
    return {
      // 分类
      categories: [],
      categoryDialogVisible: false,
      categorySaving: false,
      categoryForm: { id: null, name: '', icon: '', sortOrder: 0, isActive: true },
      // 菜品
      dishes: [],
      dishKeyword: '',
      dishCategoryFilter: null,
      dishPage: 1,
      dishPageSize: 10,
      dishTotal: 0,
      dishDialogVisible: false,
      dishSaving: false,
      dishForm: this.emptyDishForm(),
      loadingCategories: false,
      loadingDishes: false,
      dishImageUploading: false,
      materialOptions: []
    };
  },
  created() {
    this.loadCategories();
    this.loadDishes();
    this.loadMaterialOptions();
  },
  methods: {
    emptyDishForm() {
      return {
        id: null, categoryId: null, name: '', description: '', imageUrl: '',
        price: 0, originalPrice: null, vipPrice: null, stock: 100,
        sortOrder: 0, tags: '', available: true,
        useCustomVipPrice: false, materials: []
      };
    },

    async loadMaterialOptions() {
      try {
        const res = await listAllMaterials();
        if (Array.isArray(res)) this.materialOptions = res;
      } catch (e) {}
    },
    getMaterialUnit(materialId) {
      if (!materialId) return '-';
      const m = this.materialOptions.find(o => o.id === materialId);
      return m ? m.unit : '-';
    },
    onMaterialSelect(mat) {
      const m = this.materialOptions.find(o => o.id === mat.materialId);
      if (m) mat.unit = m.unit;
    },

    // ── 图片上传 ──
    async onDishImageSelected(file) {
      if (!file || !file.raw) return;
      this.dishImageUploading = true;
      try {
        const res = await uploadImage(file.raw);
        if (res && res.url) {
          this.dishForm.imageUrl = res.url;
          this.$message.success('图片上传成功');
        }
      } catch (e) {
        this.$message.error('图片上传失败');
      } finally {
        this.dishImageUploading = false;
      }
    },

    // ── 分类 ──
    async loadCategories() {
      this.loadingCategories = true;
      try {
        this.categories = await listCategories();
      } catch (e) { /* error handled by interceptor */ } finally {
        this.loadingCategories = false;
      }
    },
    openCategoryDialog(row) {
      if (row) {
        this.categoryForm = { id: row.id, name: row.name, icon: row.icon || '', sortOrder: row.sortOrder || 0, isActive: row.isActive !== false };
      } else {
        this.categoryForm = { id: null, name: '', icon: '', sortOrder: 0, isActive: true };
      }
      this.categoryDialogVisible = true;
    },
    async doSaveCategory() {
      if (!this.categoryForm.name) {
        this.$message.warning('请输入分类名称');
        return;
      }
      this.categorySaving = true;
      try {
        const payload = { name: this.categoryForm.name, icon: this.categoryForm.icon, sortOrder: this.categoryForm.sortOrder, isActive: this.categoryForm.isActive };
        if (this.categoryForm.id) {
          await updateCategory(this.categoryForm.id, payload);
          this.$message.success('分类已更新');
        } else {
          await createCategory(payload);
          this.$message.success('分类已创建');
        }
        this.categoryDialogVisible = false;
        this.loadCategories();
      } catch (e) { /* error handled by interceptor */ }
      this.categorySaving = false;
    },
    doDeleteCategory(row) {
      this.$confirm(`确定删除分类「${row.name}」？该分类下的菜品将取消分类归属。`, '确认删除', { type: 'warning' })
        .then(async () => {
          await deleteCategory(row.id);
          this.$message.success('分类已删除');
          this.loadCategories();
          this.loadDishes();
        }).catch(() => {});
    },

    // ── 菜品 ──
    async loadDishes() {
      this.loadingDishes = true;
      try {
        const params = { page: this.dishPage, pageSize: this.dishPageSize };
        if (this.dishKeyword) params.keyword = this.dishKeyword;
        if (this.dishCategoryFilter) params.categoryId = this.dishCategoryFilter;
        const res = await listDishes(params);
        this.dishes = res.list || [];
        this.dishTotal = res.total || 0;
      } catch (e) { /* error handled by interceptor */ } finally {
        this.loadingDishes = false;
      }
    },
    onDishPageChange(p) {
      this.dishPage = p;
      this.loadDishes();
    },
    onDishSizeChange(s) {
      this.dishPageSize = s;
      this.dishPage = 1;
      this.loadDishes();
    },
    openDishDialog(row) {
      if (row) {
        this.dishForm = {
          id: row.id, categoryId: row.categoryId, name: row.name,
          description: row.description || '', imageUrl: row.imageUrl || row.image || '',
          price: Number(row.price), originalPrice: row.originalPrice ? Number(row.originalPrice) : null,
          vipPrice: row.vipPrice ? Number(row.vipPrice) : null,
          stock: row.stock || 0, sortOrder: row.sortOrder || 0, tags: row.tags || '',
          available: row.available !== false,
          useCustomVipPrice: !!row.useCustomVipPrice,
          materials: (row.materials || []).map(m => ({ materialId: m.materialId, quantityPerServing: m.quantityPerServing, unit: m.unit }))
        };
      } else {
        this.dishForm = this.emptyDishForm();
      }
      this.dishDialogVisible = true;
    },
    async doSaveDish() {
      if (!this.dishForm.name) {
        this.$message.warning('请输入菜品名称');
        return;
      }
      if (!this.dishForm.price && this.dishForm.price !== 0) {
        this.$message.warning('请输入菜品价格');
        return;
      }
      this.dishSaving = true;
      try {
        const payload = { ...this.dishForm };
        delete payload.id;
        if (this.dishForm.id) {
          await updateDish(this.dishForm.id, payload);
          this.$message.success('菜品已更新');
        } else {
          await createDish(payload);
          this.$message.success('菜品已创建');
        }
        this.dishDialogVisible = false;
        this.loadDishes();
      } catch (e) { /* error handled by interceptor */ }
      this.dishSaving = false;
    },
    doDeleteDish(row) {
      this.$confirm(`确定删除菜品「${row.name}」？`, '确认删除', { type: 'warning' })
        .then(async () => {
          await deleteDish(row.id);
          this.$message.success('菜品已删除');
          this.loadDishes();
        }).catch(() => {});
    }
  }
};
</script>

<style scoped>
.toolbar { margin-bottom: 8px; }
.pager-wrap { margin-top: 12px; text-align: right; }
.dish-image-uploader >>> .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  width: 120px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  transition: border-color 0.2s;
}
.dish-image-uploader >>> .el-upload:hover {
  border-color: #409EFF;
}
.dish-image-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}
.dish-image-preview {
  width: 120px;
  height: 120px;
  object-fit: cover;
}
</style>
