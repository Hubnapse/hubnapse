import { apiFetch, extractErrorMessage } from "./http";
import type { AiTool } from "../types/Profile";

export async function fetchAiTools(): Promise<AiTool[]> {
  const response = await apiFetch("/api/ai-tools");

  if (!response.ok) {
    throw new Error(await extractErrorMessage(response, "AIツール一覧の取得に失敗しました"));
  }

  return response.json();
}
