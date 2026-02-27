import React from 'react';
import { Star, CreditCard, Gift, Search } from 'lucide-react';
import { MOCK_USERS } from '../constants';
import { UserTier } from '../types';

const Users: React.FC = () => {
  return (
    <div className="space-y-6">
       <div className="flex justify-between items-center">
        <div>
          <h2 className="text-2xl font-bold text-slate-800">User Management</h2>
          <p className="text-slate-500 text-sm">VIP accounts, loyalty points, and prepaid balances.</p>
        </div>
      </div>

      {/* VIP Tiers Summary */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        {[UserTier.PLATINUM, UserTier.GOLD, UserTier.SILVER, UserTier.REGULAR].map(tier => (
          <div key={tier} className="bg-white p-4 rounded-xl border border-slate-100 shadow-sm text-center">
             <div className={`mx-auto w-10 h-10 rounded-full flex items-center justify-center mb-2 ${
               tier === UserTier.PLATINUM ? 'bg-slate-800 text-white' :
               tier === UserTier.GOLD ? 'bg-yellow-100 text-yellow-600' :
               tier === UserTier.SILVER ? 'bg-slate-100 text-slate-500' :
               'bg-emerald-50 text-emerald-600'
             }`}>
               <Star className="w-5 h-5 fill-current" />
             </div>
             <div className="font-bold text-slate-800">{tier}</div>
             <div className="text-xs text-slate-400">24 Active Users</div>
          </div>
        ))}
      </div>

      <div className="bg-white rounded-xl shadow-sm border border-slate-100 overflow-hidden">
        <div className="p-4 border-b border-slate-100 flex justify-between items-center bg-slate-50/50">
          <div className="relative w-64">
            <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
            <input 
              type="text" 
              placeholder="Find user..." 
              className="w-full pl-9 pr-4 py-2 rounded-lg border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
            />
          </div>
          <button className="text-emerald-600 text-sm font-bold">Export List</button>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead className="bg-slate-50 text-slate-500">
              <tr>
                <th className="px-6 py-4 font-medium">User Info</th>
                <th className="px-6 py-4 font-medium">Tier</th>
                <th className="px-6 py-4 font-medium">Balance (Prepaid)</th>
                <th className="px-6 py-4 font-medium">Points</th>
                <th className="px-6 py-4 font-medium">Last Visit</th>
                <th className="px-6 py-4 font-medium text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {MOCK_USERS.map((user) => (
                <tr key={user.id} className="hover:bg-slate-50">
                  <td className="px-6 py-4">
                    <div className="font-medium text-slate-900">{user.name}</div>
                    <div className="text-xs text-slate-500">{user.phone}</div>
                  </td>
                  <td className="px-6 py-4">
                    <span className={`px-2 py-1 rounded text-xs font-bold ${
                      user.tier === UserTier.PLATINUM ? 'bg-slate-800 text-white' :
                      user.tier === UserTier.GOLD ? 'bg-yellow-100 text-yellow-700' :
                      user.tier === UserTier.SILVER ? 'bg-slate-100 text-slate-700' :
                      'bg-emerald-50 text-emerald-700'
                    }`}>
                      {user.tier}
                    </span>
                  </td>
                  <td className="px-6 py-4 font-mono font-medium text-emerald-700">
                    ¥{user.balance.toFixed(2)}
                  </td>
                  <td className="px-6 py-4 text-slate-600">
                    <div className="flex items-center gap-1">
                      <Gift className="w-3 h-3 text-purple-500" />
                      {user.points}
                    </div>
                  </td>
                  <td className="px-6 py-4 text-slate-500">
                    {user.lastVisit}
                  </td>
                  <td className="px-6 py-4 text-right">
                    <button className="text-emerald-600 hover:text-emerald-700 font-medium text-xs mr-3">Top Up</button>
                    <button className="text-slate-400 hover:text-slate-600 font-medium text-xs">Edit</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default Users;
