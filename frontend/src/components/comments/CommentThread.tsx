import { useState } from "react";
import type { Comment } from "../../types/Comment";
import CommentForm from "./CommentForm";

type CommentThreadProps = {
  comment: Comment;
  repliesByParentId: Map<number, Comment[]>;
  currentUserId: number | null;
  depth: number;
  submittingReply: boolean;
  deletingId: number | null;
  onReply: (parentId: number, content: string) => Promise<void>;
  onDelete: (commentId: number) => void;
};

function CommentThread({
  comment,
  repliesByParentId,
  currentUserId,
  depth,
  submittingReply,
  deletingId,
  onReply,
  onDelete,
}: CommentThreadProps) {
  const [showReplyForm, setShowReplyForm] = useState(false);

  const replies = repliesByParentId.get(comment.id) ?? [];
  const isOwner = currentUserId !== null && currentUserId === comment.author.id;

  const handleReplySubmit = async (content: string) => {
    await onReply(comment.id, content);
    setShowReplyForm(false);
  };

  return (
    <div className="comment-item" style={{ marginLeft: depth * 20 }}>
      <div className="comment-item__header">
        {comment.author.iconUrl && (
          <img
            className="comment-item__author-icon"
            src={comment.author.iconUrl}
            alt=""
            onError={(e) => {
              e.currentTarget.style.display = "none";
            }}
          />
        )}
        <span className="comment-item__author-name">
          {comment.author.displayName}（@{comment.author.username}）
        </span>
      </div>

      <p className="comment-item__content">{comment.content}</p>

      <div className="comment-item__actions">
        {currentUserId !== null && (
          <button
            type="button"
            className="comment-item__reply"
            onClick={() => setShowReplyForm((prev) => !prev)}
          >
            {showReplyForm ? "キャンセル" : "返信"}
          </button>
        )}

        {isOwner && (
          <button
            type="button"
            className="comment-item__delete"
            onClick={() => onDelete(comment.id)}
            disabled={deletingId === comment.id}
          >
            {deletingId === comment.id ? "削除中..." : "削除"}
          </button>
        )}
      </div>

      {showReplyForm && (
        <CommentForm
          submitting={submittingReply}
          placeholder="返信を入力..."
          submitLabel="返信する"
          onSubmit={handleReplySubmit}
          onCancel={() => setShowReplyForm(false)}
          autoFocus
        />
      )}

      {replies.map((reply) => (
        <CommentThread
          key={reply.id}
          comment={reply}
          repliesByParentId={repliesByParentId}
          currentUserId={currentUserId}
          depth={depth + 1}
          submittingReply={submittingReply}
          deletingId={deletingId}
          onReply={onReply}
          onDelete={onDelete}
        />
      ))}
    </div>
  );
}

export default CommentThread;
