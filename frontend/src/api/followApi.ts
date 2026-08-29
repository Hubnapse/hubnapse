import { apiFetch, extractErrorMessage } from "./http";

export async function followUser(userId: number): Promise<void> {
  const response = await apiFetch(`/api/users/${userId}/follow`, {
    method: "POST",
  });

  if (!response.ok) {
    throw new Error(await extractErrorMessage(response, "フォローに失敗しました"));
  }
}

export async function unfollowUser(userId: number): Promise<void> {
  const response = await apiFetch(`/api/users/${userId}/follow`, {
    method: "DELETE",
  });

  if (!response.ok) {
    throw new Error(await extractErrorMessage(response, "フォロー解除に失敗しました"));
  }
}
