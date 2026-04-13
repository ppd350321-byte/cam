import request from '@/api/request';

/**
 * 获取报表汇总
 * @param {Object} params 查询参数
 * @param {'week'|'month'} params.viewMode 视图模式
 * @returns {Promise<Object>} 汇总数据
 */
export function getReportSummary(params) {
  // return request({ url: '/reports/summary', method: 'get', params });
  return {
    "totalRevenue": 168800,
    "totalProfit": 72680,
    "bestEmployee": "李大厨",
    "trendData": [
      { "name": "周一", "营收": 22000, "成本": 10500, "利润": 11500 },
      { "name": "周二", "营收": 24600, "成本": 11200, "利润": 13400 },
      { "name": "周三", "营收": 23100, "成本": 10800, "利润": 12300 },
      { "name": "周四", "营收": 25800, "成本": 12100, "利润": 13700 },
      { "name": "周五", "营收": 28500, "成本": 13200, "利润": 15300 },
      { "name": "周六", "营收": 32000, "成本": 14800, "利润": 17200 },
      { "name": "周日", "营收": 12800, "成本": 6500, "利润": 6300 }
    ],
    "productSales": [
      { "name": "香辣牛肉饭", "value": 320 },
      { "name": "奥尔良烤鸡饭", "value": 280 },
      { "name": "意式肉酱面", "value": 210 },
      { "name": "经典汉堡套餐", "value": 160 },
      { "name": "黑椒牛柳意面", "value": 130 }
    ]
  };
}

/**
 * 获取员工绩效列表
 * @param {Object} params 查询参数
 * @returns {Promise<Object>} 分页员工绩效数组
 */
export function getReportEmployees(params) {
  return request({ url: '/reports/employees', method: 'get', params });
  // return {
  //   "list": [
  //     { "name": "李大厨", "role": "主厨", "tasks": 186, "rating": 4.9, "score": 98 },
  //     { "name": "王师傅", "role": "炒锅", "tasks": 165, "rating": 4.8, "score": 95 },
  //     { "name": "张主厨", "role": "炖汤", "tasks": 142, "rating": 4.7, "score": 92 },
  //     { "name": "刘师傅", "role": "主食", "tasks": 138, "rating": 4.6, "score": 90 },
  //     { "name": "陈师傅", "role": "配菜", "tasks": 124, "rating": 4.5, "score": 88 }
  //   ],
  //   "total": 28
  // };
}


