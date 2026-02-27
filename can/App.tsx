import React, { useState } from 'react';
import { HashRouter as Router, Routes, Route, useLocation, useNavigate } from 'react-router-dom';
import Sidebar from './components/Sidebar';
import Dashboard from './pages/Dashboard';
import Orders from './pages/Orders';
import Production from './pages/Production';
import SupplyChain from './pages/SupplyChain';
import Users from './pages/Users';
import { Bell, Search, UserCircle, Sparkles } from 'lucide-react';
import AIAssistant from './components/AIAssistant';
import { MOCK_INVENTORY, MOCK_ORDERS, MOCK_TASKS, SALES_DATA } from './constants';

const Layout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const location = useLocation();
  const navigate = useNavigate();
  const [isAIModalOpen, setAIModalOpen] = useState(false);

  // Aggregate data for AI context
  const contextData = {
    sales: SALES_DATA,
    inventory_alerts: MOCK_INVENTORY.filter(i => i.currentStock < i.reorderLevel),
    pending_orders: MOCK_ORDERS.filter(o => o.status !== 'Completed').length,
    active_tasks: MOCK_TASKS.filter(t => t.status === 'In Progress'),
    system_time: new Date().toLocaleString()
  };

  return (
    <div className="flex min-h-screen bg-slate-50 font-sans">
      <Sidebar currentPath={location.pathname} navigate={navigate} />
      
      <div className="flex-1 ml-64 flex flex-col">
        {/* Header */}
        <header className="h-16 bg-white border-b border-slate-100 flex items-center justify-between px-8 sticky top-0 z-10 shadow-sm">
          <div className="flex items-center w-96">
            <Search className="w-4 h-4 text-slate-400 mr-2" />
            <input 
              type="text" 
              placeholder="Global search..." 
              className="bg-transparent text-sm w-full focus:outline-none text-slate-600 placeholder-slate-400"
            />
          </div>
          
          <div className="flex items-center gap-6">
            <button 
              onClick={() => setAIModalOpen(true)}
              className="flex items-center gap-2 bg-gradient-to-r from-emerald-500 to-teal-500 text-white px-4 py-2 rounded-full text-sm font-medium shadow hover:shadow-lg transition-all hover:-translate-y-0.5"
            >
              <Sparkles className="w-4 h-4" />
              AI Assistant
            </button>
            <div className="relative">
              <Bell className="w-5 h-5 text-slate-500 hover:text-slate-700 cursor-pointer transition-colors" />
              <span className="absolute -top-1 -right-1 w-2 h-2 bg-red-500 rounded-full"></span>
            </div>
            <div className="flex items-center gap-2 cursor-pointer">
              <UserCircle className="w-8 h-8 text-slate-400" />
              <div className="hidden md:block">
                <div className="text-sm font-medium text-slate-800">Admin User</div>
                <div className="text-xs text-slate-500">Manager</div>
              </div>
            </div>
          </div>
        </header>

        {/* Main Content */}
        <main className="flex-1 p-8 overflow-y-auto">
          <div className="max-w-7xl mx-auto">
             {children}
          </div>
        </main>
      </div>

      <AIAssistant 
        isOpen={isAIModalOpen} 
        onClose={() => setAIModalOpen(false)} 
        contextData={contextData}
      />
    </div>
  );
};

const App: React.FC = () => {
  return (
    <Router>
      <Layout>
        <Routes>
          <Route path="/" element={<Dashboard />} />
          <Route path="/orders" element={<Orders />} />
          <Route path="/production" element={<Production />} />
          <Route path="/supply-chain" element={<SupplyChain />} />
          <Route path="/users" element={<Users />} />
          <Route path="/reports" element={<div className="text-center p-20 text-slate-500">Reporting Module Loaded. See Dashboard for charts.</div>} />
          <Route path="/settings" element={<div className="text-center p-20 text-slate-500">Settings Module Placeholder</div>} />
        </Routes>
      </Layout>
    </Router>
  );
};

export default App;
