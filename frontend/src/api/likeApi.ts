import { apiFetch, extractErrorMessage } from "./http";

export async function likePost(postId: number): Promise<void> {
  const response = await apiFetch(`/api/posts/${postId}/likes`, {
    method: "POST",
  });

  if (!response.ok) {
    throw new Error(await extractErrorMessage(response, "いいねに失敗しました"));
  }
}

export async function unlikePost(postId: number): Promise<void> {
  const response = await apiFetch(`/api/posts/${postId}/likes`, {
    method: "DELETE",
  });

  if (!response.ok) {
    throw new Error(await extractErrorMessage(response, "いいねの解除に失敗しました"));
  }
}
