import { apiFetch, extractErrorMessage } from "./http";
import type { Comment } from "../types/Comment";

export async function fetchComments(postId: number): Promise<Comment[]> {
  const response = await apiFetch(`/api/posts/${postId}/comments`);

  if (!response.ok) {
    throw new Error(await extractErrorMessage(response, "コメントの取得に失敗しました"));
  }

  return response.json();
}

export async function createComment(
  postId: number,
  content: string,
  parentId: number | null,
): Promise<Comment> {
  const response = await apiFetch(`/api/posts/${postId}/comments`, {
    method: "POST",
    body: JSON.stringify({ content, parentId }),
  });

  if (!response.ok) {
    throw new Error(await extractErrorMessage(response, "コメントの投稿に失敗しました"));
  }

  return response.json();
}

export async function deleteComment(commentId: number): Promise<void> {
  const response = await apiFetch(`/api/comments/${commentId}`, {
    method: "DELETE",
  });

  if (!response.ok) {
    throw new Error(await extractErrorMessage(response, "コメントの削除に失敗しました"));
  }
}
