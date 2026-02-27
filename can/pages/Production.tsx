import React from 'react';
import { Clock, ChefHat, CheckSquare, AlertCircle } from 'lucide-react';
import { MOCK_TASKS } from '../constants';

const Production: React.FC = () => {
  // Sort tasks by start time
  const sortedTasks = [...MOCK_TASKS].sort((a, b) => a.startTime.localeCompare(b.startTime));

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-slate-800">Production Schedule</h2>
        <p className="text-slate-500 text-sm">Minute-level task arrangement and staff monitoring.</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Timeline View */}
        <div className="lg:col-span-2 space-y-4">
          <div className="bg-white rounded-xl shadow-sm border border-slate-100 p-6">
             <h3 className="text-lg font-bold text-slate-800 mb-6 flex items-center gap-2">
               <Clock className="w-5 h-5 text-emerald-600" /> 
               Today's Timeline
             </h3>
             
             <div className="space-y-0 relative before:absolute before:inset-0 before:ml-5 before:-translate-x-px md:before:mx-auto md:before:translate-x-0 before:h-full before:w-0.5 before:bg-gradient-to-b before:from-transparent before:via-slate-200 before:to-transparent">
               {sortedTasks.map((task) => (
                 <div key={task.id} className="relative flex items-center justify-between md:justify-normal md:odd:flex-row-reverse group is-active mb-8 last:mb-0">
                    {/* Icon on the line */}
                    <div className="flex items-center justify-center w-10 h-10 rounded-full border border-white bg-slate-50 group-[.is-active]:bg-emerald-500 text-slate-500 group-[.is-active]:text-white shadow shrink-0 md:order-1 md:group-odd:-translate-x-1/2 md:group-even:translate-x-1/2 z-10">
                      <ChefHat className="w-5 h-5" />
                    </div>
                    
                    {/* Card */}
                    <div className="w-[calc(100%-4rem)] md:w-[calc(50%-2.5rem)] bg-white p-4 rounded-xl border border-slate-100 shadow-sm hover:shadow-md transition-shadow">
                      <div className="flex justify-between items-start mb-1">
                        <span className={`text-xs font-bold px-2 py-0.5 rounded ${
                          task.status === 'Completed' ? 'bg-emerald-100 text-emerald-700' :
                          task.status === 'In Progress' ? 'bg-blue-100 text-blue-700' :
                          'bg-slate-100 text-slate-600'
                        }`}>
                          {task.status}
                        </span>
                        <span className="text-xs font-mono text-slate-500">{task.startTime} - {task.endTime}</span>
                      </div>
                      <h4 className="font-bold text-slate-800 text-sm">{task.name}</h4>
                      <div className="mt-2 flex items-center text-xs text-slate-500 gap-2">
                        <span className="flex items-center gap-1"><UsersIcon className="w-3 h-3"/> {task.staffAssigned}</span>
                      </div>
                    </div>
                 </div>
               ))}
             </div>
          </div>
        </div>

        {/* KPIs / Current Status */}
        <div className="space-y-6">
          <div className="bg-emerald-600 rounded-xl p-6 text-white shadow-lg">
            <h3 className="font-semibold text-emerald-100 text-sm uppercase tracking-wider mb-1">Kitchen Efficiency</h3>
            <div className="text-4xl font-bold mb-2">92%</div>
            <p className="text-emerald-100 text-sm">Tasks completed on time today.</p>
            <div className="mt-4 w-full bg-emerald-900/30 rounded-full h-2">
              <div className="bg-white h-2 rounded-full w-[92%]"></div>
            </div>
          </div>

          <div className="bg-white rounded-xl shadow-sm border border-slate-100 p-6">
            <h3 className="font-bold text-slate-800 mb-4">Staff Assignment</h3>
            <div className="space-y-4">
              {['Chef Liu', 'Chef Zhang', 'Assistant Wang', 'Server Li'].map((staff, idx) => (
                <div key={idx} className="flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <div className="w-8 h-8 rounded-full bg-slate-100 flex items-center justify-center text-xs font-bold text-slate-600">
                      {staff.split(' ')[1][0]}
                    </div>
                    <div>
                      <div className="text-sm font-medium text-slate-900">{staff}</div>
                      <div className="text-xs text-slate-500">Kitchen</div>
                    </div>
                  </div>
                  <div className="h-2 w-2 rounded-full bg-emerald-500 animate-pulse"></div>
                </div>
              ))}
            </div>
          </div>

          <div className="bg-white rounded-xl shadow-sm border border-slate-100 p-6">
            <h3 className="font-bold text-slate-800 mb-4 flex items-center gap-2">
              <AlertCircle className="w-5 h-5 text-orange-500" />
              Alerts
            </h3>
            <ul className="space-y-3">
               <li className="text-sm text-slate-600 p-3 bg-orange-50 rounded-lg border border-orange-100">
                 <strong>Oven 2</strong> requires maintenance check.
               </li>
               <li className="text-sm text-slate-600 p-3 bg-blue-50 rounded-lg border border-blue-100">
                 Lunch peak preparation starting in 15 mins.
               </li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
};

// Helper icon
const UsersIcon = ({className}:{className?:string}) => (
  <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className={className}><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
);

export default Production;
