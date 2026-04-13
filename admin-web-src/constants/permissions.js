export const PERMISSIONS = {
  DASHBOARD_VIEW: 'dashboard:view',
  USERS_VIEW: 'users:view',
  USERS_EDIT: 'users:edit',
  USERS_RECHARGE: 'users:recharge',
  USERS_POINTS: 'users:points',
  USERS_STATUS: 'users:status',
  ORDERS_VIEW: 'orders:view',
  ORDERS_EXPORT: 'orders:export',
  ORDERS_ACCEPT: 'orders:accept',
  ORDERS_COMPLETE: 'orders:complete',
  ORDERS_PICKUP: 'orders:pickup',
  ORDERS_DETAIL: 'orders:detail',
  SUPPLY_VIEW: 'supply:view',
  SUPPLY_SMART_PLAN: 'supply:smart-plan',
  SUPPLY_PROCUREMENT_ADD: 'supply:procurement:add',
  SUPPLY_PROCUREMENT_APPROVE: 'supply:procurement:approve',
  SUPPLY_PROCUREMENT_RECEIVE: 'supply:procurement:receive',
  SUPPLY_PROCUREMENT_DETAIL: 'supply:procurement:detail',
  SUPPLY_SUPPLIER_ADD: 'supply:supplier:add',
  SUPPLY_SUPPLIER_DETAIL: 'supply:supplier:detail',

  PRODUCTION_VIEW: 'production:view',
  PRODUCTION_TASK_START: 'production:task:start',
  PRODUCTION_TASK_COMPLETE: 'production:task:complete',
  PRODUCTION_SCHEDULE_OPTIMIZE: 'production:schedule:optimize',
  PRODUCTION_SCHEDULE_ADJUST: 'production:schedule:adjust',
  REPORTS_VIEW: 'reports:view',
  REPORTS_EXPORT: 'reports:export',
  VIP_COUPON_VIEW: 'vip-coupon:view',
  VIP_COUPON_EDIT: 'vip-coupon:edit',
  PERMISSION_CONFIG_VIEW: 'permission-config:view',
  MENU_VIEW: 'menu:view',
  MENU_ADD: 'menu:add',
  MENU_EDIT: 'menu:edit',
  MENU_DELETE: 'menu:delete',
  ROLE_CREATE: 'permission-config:role:create',
  ROLE_UPDATE: 'permission-config:role:update',
  ROLE_DELETE: 'permission-config:role:delete',
  ROLE_GRANT: 'permission-config:role:grant'
};

export const PERMISSION_TREE = [
  { id: 'menu-dashboard', label: '菜单/仪表盘', code: PERMISSIONS.DASHBOARD_VIEW },
  {
    id: 'menu-users',
    label: '菜单/用户管理',
    code: PERMISSIONS.USERS_VIEW,
    children: [
      { id: 'btn-users-edit', label: '按钮/编辑用户', code: PERMISSIONS.USERS_EDIT },
      { id: 'btn-users-recharge', label: '按钮/余额充值', code: PERMISSIONS.USERS_RECHARGE },
      { id: 'btn-users-points', label: '按钮/积分发放', code: PERMISSIONS.USERS_POINTS },
      { id: 'btn-users-status', label: '按钮/启用禁用', code: PERMISSIONS.USERS_STATUS }
    ]
  },
  {
    id: 'menu-orders',
    label: '菜单/订单管理',
    code: PERMISSIONS.ORDERS_VIEW,
    children: [
      { id: 'btn-orders-export', label: '按钮/导出订单报表', code: PERMISSIONS.ORDERS_EXPORT },
      { id: 'btn-orders-accept', label: '按钮/接单派发', code: PERMISSIONS.ORDERS_ACCEPT },
      { id: 'btn-orders-complete', label: '按钮/标记完成', code: PERMISSIONS.ORDERS_COMPLETE },
      { id: 'btn-orders-pickup', label: '按钮/确认取餐', code: PERMISSIONS.ORDERS_PICKUP },
      { id: 'btn-orders-detail', label: '按钮/订单详情', code: PERMISSIONS.ORDERS_DETAIL }
    ]
  },
  {
    id: 'menu-menu-management',
    label: '菜单/菜品管理',
    code: PERMISSIONS.MENU_VIEW,
    children: [
      { id: 'btn-menu-add', label: '按钮/新增菜品/分类', code: PERMISSIONS.MENU_ADD },
      { id: 'btn-menu-edit', label: '按钮/编辑菜品/分类', code: PERMISSIONS.MENU_EDIT },
      { id: 'btn-menu-delete', label: '按钮/删除菜品/分类', code: PERMISSIONS.MENU_DELETE }
    ]
  },
  {
    id: 'menu-supply',
    label: '菜单/供应链配置',
    code: PERMISSIONS.SUPPLY_VIEW,
    children: [
      { id: 'btn-supply-smart-plan', label: '按钮/智能生成计划', code: PERMISSIONS.SUPPLY_SMART_PLAN },
      { id: 'btn-supply-proc-add', label: '按钮/新增采购', code: PERMISSIONS.SUPPLY_PROCUREMENT_ADD },
      { id: 'btn-supply-proc-approve', label: '按钮/审核采购', code: PERMISSIONS.SUPPLY_PROCUREMENT_APPROVE },
      { id: 'btn-supply-proc-receive', label: '按钮/确认收货', code: PERMISSIONS.SUPPLY_PROCUREMENT_RECEIVE },
      { id: 'btn-supply-proc-detail', label: '按钮/采购详情', code: PERMISSIONS.SUPPLY_PROCUREMENT_DETAIL },
      { id: 'btn-supply-supplier-add', label: '按钮/新增供应商', code: PERMISSIONS.SUPPLY_SUPPLIER_ADD },
      { id: 'btn-supply-supplier-detail', label: '按钮/供应商详情', code: PERMISSIONS.SUPPLY_SUPPLIER_DETAIL },

    ]
  },
  {
    id: 'menu-production',
    label: '菜单/生产调度',
    code: PERMISSIONS.PRODUCTION_VIEW,
    children: [
      { id: 'btn-production-start', label: '按钮/开始任务', code: PERMISSIONS.PRODUCTION_TASK_START },
      { id: 'btn-production-complete', label: '按钮/完成任务', code: PERMISSIONS.PRODUCTION_TASK_COMPLETE },
      { id: 'btn-production-optimize', label: '按钮/智能一键排产', code: PERMISSIONS.PRODUCTION_SCHEDULE_OPTIMIZE }
    ]
  },
  {
    id: 'menu-reports',
    label: '菜单/报表中心',
    code: PERMISSIONS.REPORTS_VIEW,
    children: [
      { id: 'btn-reports-export', label: '按钮/导出报表', code: PERMISSIONS.REPORTS_EXPORT }
    ]
  },
  {
    id: 'menu-vip-coupon',
    label: '菜单/VIP与优惠券',
    code: PERMISSIONS.VIP_COUPON_VIEW,
    children: [
      { id: 'btn-vip-coupon-edit', label: '按钮/编辑VIP与优惠券', code: PERMISSIONS.VIP_COUPON_EDIT }
    ]
  },
  {
    id: 'menu-permission-config',
    label: '菜单/权限配置(仅管理员)',
    code: PERMISSIONS.PERMISSION_CONFIG_VIEW,
    children: [
      { id: 'btn-role-create', label: '按钮/创建角色', code: PERMISSIONS.ROLE_CREATE },
      { id: 'btn-role-update', label: '按钮/编辑角色', code: PERMISSIONS.ROLE_UPDATE },
      { id: 'btn-role-delete', label: '按钮/删除角色', code: PERMISSIONS.ROLE_DELETE },
      { id: 'btn-role-grant', label: '按钮/角色授权', code: PERMISSIONS.ROLE_GRANT }
    ]
  }
];

export function flattenPermissionCodes(tree = PERMISSION_TREE) {
  const result = [];
  const walk = (nodes) => {
    (nodes || []).forEach((node) => {
      if (node.code) result.push(node.code);
      if (Array.isArray(node.children) && node.children.length) walk(node.children);
    });
  };
  walk(tree);
  return result;
}
