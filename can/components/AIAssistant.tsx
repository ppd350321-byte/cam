import React, { useState } from 'react';
import { X, Sparkles, Send, Loader2 } from 'lucide-react';
import { generateAIAnalysis } from '../services/geminiService';

interface AIAssistantProps {
  isOpen: boolean;
  onClose: () => void;
  contextData: any;
}

const AIAssistant: React.FC<AIAssistantProps> = ({ isOpen, onClose, contextData }) => {
  const [prompt, setPrompt] = useState('');
  const [response, setResponse] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleAnalyze = async () => {
    if (!prompt.trim()) return;
    
    setLoading(true);
    setResponse(null);
    
    const contextString = JSON.stringify(contextData);
    const result = await generateAIAnalysis(contextString, prompt);
    
    setResponse(result);
    setLoading(false);
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">
      <div className="bg-white rounded-2xl w-full max-w-2xl shadow-2xl overflow-hidden flex flex-col max-h-[85vh]">
        {/* Header */}
        <div className="bg-gradient-to-r from-emerald-600 to-teal-600 p-4 flex justify-between items-center text-white">
          <div className="flex items-center gap-2">
            <Sparkles className="w-5 h-5" />
            <h2 className="font-semibold text-lg">Smart Canteen Assistant</h2>
          </div>
          <button onClick={onClose} className="hover:bg-white/20 p-1 rounded-full transition-colors">
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content Area */}
        <div className="p-6 overflow-y-auto flex-1 bg-slate-50">
          {!response && !loading && (
             <div className="text-center text-slate-500 py-10">
               <Sparkles className="w-12 h-12 mx-auto text-emerald-200 mb-4" />
               <p>Ask me about production schedules, inventory optimization, or sales analysis.</p>
               <p className="text-xs mt-2 text-slate-400">Powered by Gemini AI</p>
             </div>
          )}

          {loading && (
            <div className="flex flex-col items-center justify-center py-12">
              <Loader2 className="w-8 h-8 text-emerald-600 animate-spin mb-3" />
              <p className="text-slate-500 text-sm animate-pulse">Analyzing operational data...</p>
            </div>
          )}

          {response && (
            <div className="bg-white p-5 rounded-xl border border-slate-100 shadow-sm prose prose-emerald max-w-none text-sm">
              <div className="whitespace-pre-wrap">{response}</div>
            </div>
          )}
        </div>

        {/* Input Area */}
        <div className="p-4 bg-white border-t border-slate-100">
          <div className="relative">
            <input
              type="text"
              value={prompt}
              onChange={(e) => setPrompt(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleAnalyze()}
              placeholder="e.g., 'Generate a procurement plan based on low stock' or 'Analyze today's efficiency'"
              className="w-full pl-4 pr-12 py-3 rounded-lg border border-slate-200 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all"
            />
            <button 
              onClick={handleAnalyze}
              disabled={loading || !prompt.trim()}
              className="absolute right-2 top-2 p-1.5 bg-emerald-600 text-white rounded-md hover:bg-emerald-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            >
              <Send className="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AIAssistant;
