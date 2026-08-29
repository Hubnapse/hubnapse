import { apiFetch, extractErrorMessage } from "./http";
import type { AiTool, ProfileUpdateValues, UserProfile } from "../types/Profile";
import type { User } from "../types/User";

export async function fetchProfile(username: string): Promise<UserProfile | null> {
  const response = await apiFetch(`/api/users/${username}`);

  if (response.status === 404) {
    return null;
  }

  if (!response.ok) {
    throw new Error(await extractErrorMessage(response, "プロフィールの取得に失敗しました"));
  }

  return response.json();
}

export async function updateProfile(values: ProfileUpdateValues): Promise<User> {
  const response = await apiFetch("/api/users/me", {
    method: "PUT",
    body: JSON.stringify(values),
  });

  if (!response.ok) {
    throw new Error(await extractErrorMessage(response, "プロフィールの更新に失敗しました"));
  }

  return response.json();
}

export async function updateAiTools(aiToolIds: number[]): Promise<AiTool[]> {
  const response = await apiFetch("/api/users/me/ai-tools", {
    method: "PUT",
    body: JSON.stringify({ aiToolIds }),
  });

  if (!response.ok) {
    throw new Error(await extractErrorMessage(response, "よく使うAIの更新に失敗しました"));
  }

  return response.json();
}
