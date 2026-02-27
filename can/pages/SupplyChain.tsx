import React from 'react';
import { Package, Truck, AlertTriangle, TrendingDown } from 'lucide-react';
import { MOCK_INVENTORY } from '../constants';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell } from 'recharts';

const SupplyChain: React.FC = () => {
  return (
    <div className="space-y-6">
       <div>
        <h2 className="text-2xl font-bold text-slate-800">Supply Chain & Inventory</h2>
        <p className="text-slate-500 text-sm">Manage raw materials, procurement plans, and supplier relationships.</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <div className="bg-white p-6 rounded-xl border border-slate-100 shadow-sm flex items-center gap-4">
          <div className="p-3 bg-blue-100 text-blue-600 rounded-lg">
            <Package className="w-6 h-6" />
          </div>
          <div>
            <p className="text-sm text-slate-500">Total SKUs</p>
            <h3 className="text-2xl font-bold text-slate-900">48</h3>
          </div>
        </div>
        <div className="bg-white p-6 rounded-xl border border-slate-100 shadow-sm flex items-center gap-4">
          <div className="p-3 bg-orange-100 text-orange-600 rounded-lg">
            <AlertTriangle className="w-6 h-6" />
          </div>
          <div>
            <p className="text-sm text-slate-500">Low Stock Items</p>
            <h3 className="text-2xl font-bold text-slate-900">3</h3>
          </div>
        </div>
        <div className="bg-white p-6 rounded-xl border border-slate-100 shadow-sm flex items-center gap-4">
          <div className="p-3 bg-emerald-100 text-emerald-600 rounded-lg">
            <Truck className="w-6 h-6" />
          </div>
          <div>
            <p className="text-sm text-slate-500">Pending Deliveries</p>
            <h3 className="text-2xl font-bold text-slate-900">2</h3>
          </div>
        </div>
        <div className="bg-white p-6 rounded-xl border border-slate-100 shadow-sm flex items-center gap-4">
          <div className="p-3 bg-purple-100 text-purple-600 rounded-lg">
            <TrendingDown className="w-6 h-6" />
          </div>
          <div>
            <p className="text-sm text-slate-500">Waste Rate</p>
            <h3 className="text-2xl font-bold text-slate-900">1.2%</h3>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Inventory Table */}
        <div className="lg:col-span-2 bg-white rounded-xl border border-slate-100 shadow-sm overflow-hidden">
          <div className="p-6 border-b border-slate-100 flex justify-between items-center">
            <h3 className="font-bold text-slate-800">Current Inventory Levels</h3>
            <button className="text-sm text-emerald-600 font-medium hover:underline">Download Report</button>
          </div>
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-slate-50 text-slate-500">
                <tr>
                  <th className="px-6 py-4 font-medium">Item Name</th>
                  <th className="px-6 py-4 font-medium">Category</th>
                  <th className="px-6 py-4 font-medium">Stock</th>
                  <th className="px-6 py-4 font-medium">Supplier</th>
                  <th className="px-6 py-4 font-medium">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {MOCK_INVENTORY.map((item) => (
                  <tr key={item.id} className="hover:bg-slate-50">
                    <td className="px-6 py-4 font-medium text-slate-900">{item.name}</td>
                    <td className="px-6 py-4 text-slate-500">{item.category}</td>
                    <td className="px-6 py-4 text-slate-700">
                      {item.currentStock} <span className="text-slate-400 text-xs">{item.unit}</span>
                    </td>
                    <td className="px-6 py-4 text-slate-500">{item.supplier}</td>
                    <td className="px-6 py-4">
                      {item.currentStock < item.reorderLevel ? (
                        <span className="text-red-600 bg-red-50 px-2 py-1 rounded text-xs font-bold border border-red-100">Restock</span>
                      ) : (
                        <span className="text-emerald-600 bg-emerald-50 px-2 py-1 rounded text-xs font-bold border border-emerald-100">OK</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {/* Stock Levels Chart */}
        <div className="bg-white rounded-xl border border-slate-100 shadow-sm p-6">
          <h3 className="font-bold text-slate-800 mb-6">Stock vs Reorder Level</h3>
          <div className="h-64">
             <ResponsiveContainer width="100%" height="100%">
               <BarChart data={MOCK_INVENTORY} layout="vertical">
                 <CartesianGrid strokeDasharray="3 3" horizontal={true} vertical={false} stroke="#e2e8f0" />
                 <XAxis type="number" hide />
                 <YAxis dataKey="name" type="category" width={80} tick={{fontSize: 12, fill: '#64748b'}} />
                 <Tooltip />
                 <Bar dataKey="currentStock" name="Current" fill="#10b981" barSize={10} radius={[0, 4, 4, 0]} />
                 <Bar dataKey="reorderLevel" name="Min Level" fill="#f43f5e" barSize={10} radius={[0, 4, 4, 0]} />
               </BarChart>
             </ResponsiveContainer>
          </div>
          <div className="mt-4 p-4 bg-slate-50 rounded-lg border border-slate-100">
            <h4 className="text-sm font-bold text-slate-700 mb-2">Automated Procurement Plan</h4>
            <p className="text-xs text-slate-500 leading-relaxed">
              Based on current consumption rates, the system recommends ordering 
              <strong> 10kg of Pork Belly</strong> and <strong>5kg of Bok Choy</strong> by 2:00 PM today to ensure supply for tomorrow's lunch service.
            </p>
            <button className="mt-3 w-full py-2 bg-emerald-600 text-white text-xs font-bold rounded hover:bg-emerald-700 transition-colors">
              Approve & Send to Suppliers
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SupplyChain;
