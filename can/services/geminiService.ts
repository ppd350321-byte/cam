import { GoogleGenAI } from "@google/genai";

const ai = new GoogleGenAI({ apiKey: process.env.API_KEY });

export const generateAIAnalysis = async (contextData: string, prompt: string): Promise<string> => {
  try {
    const fullPrompt = `
      You are an expert operations consultant for a Community Canteen.
      
      Here is the current operational context (JSON format):
      ${contextData}

      User Question/Request:
      ${prompt}

      Please provide a concise, professional, and actionable response. 
      Focus on efficiency, cost-saving, and customer satisfaction.
      If the user asks for a plan, provide a step-by-step list.
      If the user asks for analysis, interpret the numbers provided.
    `;

    const response = await ai.models.generateContent({
      model: 'gemini-3-flash-preview',
      contents: fullPrompt,
    });

    return response.text || "Unable to generate analysis at this time.";
  } catch (error) {
    console.error("Gemini API Error:", error);
    return "I'm sorry, I'm having trouble connecting to the analysis server right now. Please try again later.";
  }
};
