export enum OrderStatus {
  PENDING = 'Pending',
  PREPARING = 'Preparing',
  READY = 'Ready',
  COMPLETED = 'Completed',
  CANCELLED = 'Cancelled',
}

export enum UserTier {
  REGULAR = 'Regular',
  SILVER = 'Silver',
  GOLD = 'Gold',
  PLATINUM = 'Platinum',
}

export interface User {
  id: string;
  name: string;
  phone: string;
  tier: UserTier;
  balance: number;
  points: number;
  lastVisit: string;
}

export interface OrderItem {
  id: string;
  name: string;
  quantity: number;
  price: number;
}

export interface Order {
  id: string;
  userId: string;
  userName: string;
  items: OrderItem[];
  totalAmount: number;
  status: OrderStatus;
  createdAt: string;
  paymentMethod: 'WeChat' | 'Alipay' | 'Card' | 'Balance';
}

export interface InventoryItem {
  id: string;
  name: string;
  category: string;
  currentStock: number;
  unit: string;
  reorderLevel: number;
  supplier: string;
  lastRestocked: string;
}

export interface ProductionTask {
  id: string;
  name: string;
  startTime: string; // HH:mm
  endTime: string;   // HH:mm
  status: 'Pending' | 'In Progress' | 'Completed';
  staffAssigned: string;
  relatedOrderId?: string;
}

export interface SalesData {
  date: string;
  revenue: number;
  orders: number;
}
