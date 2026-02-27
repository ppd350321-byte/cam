import { InventoryItem, Order, OrderStatus, ProductionTask, SalesData, User, UserTier } from "./types";

export const MOCK_USERS: User[] = [
  { id: 'u1', name: 'Alice Zhang', phone: '13800138000', tier: UserTier.PLATINUM, balance: 450.00, points: 1200, lastVisit: '2023-10-25' },
  { id: 'u2', name: 'Bob Li', phone: '13900139000', tier: UserTier.GOLD, balance: 120.50, points: 540, lastVisit: '2023-10-24' },
  { id: 'u3', name: 'Charlie Wang', phone: '13700137000', tier: UserTier.REGULAR, balance: 0.00, points: 50, lastVisit: '2023-10-20' },
  { id: 'u4', name: 'Diana Chen', phone: '13600136000', tier: UserTier.SILVER, balance: 85.00, points: 300, lastVisit: '2023-10-25' },
];

export const MOCK_ORDERS: Order[] = [
  {
    id: 'ord-1001', userId: 'u1', userName: 'Alice Zhang',
    items: [{ id: 'p1', name: 'Braised Pork Rice', quantity: 1, price: 25 }],
    totalAmount: 25, status: OrderStatus.COMPLETED, createdAt: '2023-10-26T11:30:00', paymentMethod: 'Balance'
  },
  {
    id: 'ord-1002', userId: 'u2', userName: 'Bob Li',
    items: [{ id: 'p2', name: 'Beef Noodle Soup', quantity: 2, price: 30 }],
    totalAmount: 60, status: OrderStatus.PREPARING, createdAt: '2023-10-26T11:45:00', paymentMethod: 'WeChat'
  },
  {
    id: 'ord-1003', userId: 'u3', userName: 'Charlie Wang',
    items: [{ id: 'p3', name: 'Vegetable Stir-fry', quantity: 1, price: 18 }, { id: 'p4', name: 'Rice', quantity: 1, price: 2 }],
    totalAmount: 20, status: OrderStatus.PENDING, createdAt: '2023-10-26T12:05:00', paymentMethod: 'Alipay'
  },
];

export const MOCK_INVENTORY: InventoryItem[] = [
  { id: 'inv-1', name: 'Pork Belly', category: 'Meat', currentStock: 15, unit: 'kg', reorderLevel: 20, supplier: 'FreshFarms Ltd', lastRestocked: '2023-10-24' },
  { id: 'inv-2', name: 'Rice', category: 'Grains', currentStock: 200, unit: 'kg', reorderLevel: 50, supplier: 'GrainCo', lastRestocked: '2023-10-20' },
  { id: 'inv-3', name: 'Bok Choy', category: 'Vegetable', currentStock: 5, unit: 'kg', reorderLevel: 10, supplier: 'Local Green', lastRestocked: '2023-10-25' },
  { id: 'inv-4', name: 'Beef Shank', category: 'Meat', currentStock: 8, unit: 'kg', reorderLevel: 10, supplier: 'FreshFarms Ltd', lastRestocked: '2023-10-24' },
];

export const MOCK_TASKS: ProductionTask[] = [
  { id: 't1', name: 'Prep Vegetables', startTime: '10:00', endTime: '10:45', status: 'Completed', staffAssigned: 'Chef Liu' },
  { id: 't2', name: 'Slow Cook Pork', startTime: '10:15', endTime: '11:30', status: 'Completed', staffAssigned: 'Chef Zhang' },
  { id: 't3', name: 'Boil Noodles', startTime: '11:30', endTime: '13:00', status: 'In Progress', staffAssigned: 'Assistant Wang' },
  { id: 't4', name: 'Plate Orders (Batch 1)', startTime: '11:45', endTime: '12:15', status: 'In Progress', staffAssigned: 'Server Li' },
  { id: 't5', name: 'Clean Kitchen Station A', startTime: '13:30', endTime: '14:00', status: 'Pending', staffAssigned: 'Cleaner Chen' },
];

export const SALES_DATA: SalesData[] = [
  { date: 'Mon', revenue: 1200, orders: 45 },
  { date: 'Tue', revenue: 1450, orders: 52 },
  { date: 'Wed', revenue: 1100, orders: 38 },
  { date: 'Thu', revenue: 1600, orders: 60 },
  { date: 'Fri', revenue: 2100, orders: 85 },
  { date: 'Sat', revenue: 2400, orders: 95 },
  { date: 'Sun', revenue: 2200, orders: 90 },
];
