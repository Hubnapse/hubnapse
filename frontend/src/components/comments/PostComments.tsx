import { useEffect, useState } from "react";
import type { Comment } from "../../types/Comment";
import { createComment, deleteComment, fetchComments } from "../../api/commentApi";
import CommentForm from "./CommentForm";
import CommentThread from "./CommentThread";

type PostCommentsProps = {
  postId: number;
  currentUserId: number | null;
};

function collectWithDescendants(comments: Comment[], rootId: number): Set<number> {
  const ids = new Set<number>([rootId]);

  let added = true;
  while (added) {
    added = false;
    for (const comment of comments) {
      if (comment.parentId !== null && ids.has(comment.parentId) && !ids.has(comment.id)) {
        ids.add(comment.id);
        added = true;
      }
    }
  }

  return ids;
}

function PostComments({ postId, currentUserId }: PostCommentsProps) {
  const [comments, setComments] = useState<Comment[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [deletingId, setDeletingId] = useState<number | null>(null);

  useEffect(() => {
    fetchComments(postId)
      .then(setComments)
      .catch((err) => setError(err instanceof Error ? err.message : "コメントの取得に失敗しました"))
      .finally(() => setLoading(false));
  }, [postId]);

  const topLevelComments = comments.filter((comment) => comment.parentId === null);
  const repliesByParentId = new Map<number, Comment[]>();

  for (const comment of comments) {
    if (comment.parentId !== null) {
      const list = repliesByParentId.get(comment.parentId) ?? [];
      list.push(comment);
      repliesByParentId.set(comment.parentId, list);
    }
  }

  const handleCreate = async (content: string, parentId: number | null) => {
    setSubmitting(true);
    setError(null);

    try {
      const created = await createComment(postId, content, parentId);
      setComments((prev) => [...prev, created]);
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (commentId: number) => {
    const confirmed = window.confirm("このコメントを削除しますか？（返信も削除されます）");
    if (!confirmed) {
      return;
    }

    setError(null);
    setDeletingId(commentId);

    try {
      await deleteComment(commentId);
      const idsToRemove = collectWithDescendants(comments, commentId);
      setComments((prev) => prev.filter((comment) => !idsToRemove.has(comment.id)));
    } catch (err) {
      setError(err instanceof Error ? err.message : "コメントの削除に失敗しました");
    } finally {
      setDeletingId(null);
    }
  };

  return (
    <section className="post-comments">
      <h3 className="post-comments__title">コメント（{comments.length}）</h3>

      {error && <p className="post-comments__error">{error}</p>}

      {currentUserId !== null ? (
        <CommentForm
          submitting={submitting}
          placeholder="コメントを入力..."
          submitLabel="コメントする"
          onSubmit={(content) => handleCreate(content, null)}
        />
      ) : (
        <p className="post-comments__login-required">コメントするにはログインしてください</p>
      )}

      {!loading && (
        <div className="comment-thread-list">
          {topLevelComments.map((comment) => (
            <CommentThread
              key={comment.id}
              comment={comment}
              repliesByParentId={repliesByParentId}
              currentUserId={currentUserId}
              depth={0}
              submittingReply={submitting}
              deletingId={deletingId}
              onReply={(parentId, content) => handleCreate(content, parentId)}
              onDelete={handleDelete}
            />
          ))}
        </div>
      )}
    </section>
  );
}

export default PostComments;
