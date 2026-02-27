import React from 'react';
import { 
  LayoutDashboard, 
  Users, 
  ShoppingBag, 
  Truck, 
  ChefHat, 
  BarChart3, 
  Settings 
} from 'lucide-react';

interface SidebarProps {
  currentPath: string;
  navigate: (path: string) => void;
}

const Sidebar: React.FC<SidebarProps> = ({ currentPath, navigate }) => {
  const menuItems = [
    { id: '/', label: 'Dashboard', icon: LayoutDashboard },
    { id: '/orders', label: 'Order Management', icon: ShoppingBag },
    { id: '/users', label: 'User Management', icon: Users },
    { id: '/supply-chain', label: 'Supply Chain', icon: Truck },
    { id: '/production', label: 'Production', icon: ChefHat },
    { id: '/reports', label: 'Reports', icon: BarChart3 },
    { id: '/settings', label: 'Settings', icon: Settings },
  ];

  return (
    <div className="w-64 bg-slate-900 text-slate-100 h-screen flex flex-col fixed left-0 top-0 shadow-xl z-20">
      <div className="p-6 border-b border-slate-800">
        <h1 className="text-xl font-bold bg-gradient-to-r from-emerald-400 to-teal-300 text-transparent bg-clip-text">
          Community Canteen
        </h1>
        <p className="text-xs text-slate-400 mt-1">Admin Operations</p>
      </div>
      
      <nav className="flex-1 py-4">
        {menuItems.map((item) => {
          const isActive = currentPath === item.id;
          const Icon = item.icon;
          return (
            <button
              key={item.id}
              onClick={() => navigate(item.id)}
              className={`w-full flex items-center px-6 py-3 transition-colors duration-200 ${
                isActive 
                  ? 'bg-emerald-600/20 text-emerald-400 border-r-2 border-emerald-400' 
                  : 'text-slate-400 hover:bg-slate-800 hover:text-slate-100'
              }`}
            >
              <Icon className="w-5 h-5 mr-3" />
              <span className="font-medium">{item.label}</span>
            </button>
          );
        })}
      </nav>

      <div className="p-4 border-t border-slate-800 text-xs text-slate-500">
        v1.0.2 &copy; 2024
      </div>
    </div>
  );
};

export default Sidebar;
