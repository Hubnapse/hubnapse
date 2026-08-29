import type { Post } from "../types/Post";
import PostComments from "./comments/PostComments";

type PostCardProps = {
  post: Post;
  currentUserId: number | null;
  isDeleting: boolean;
  onEdit: (post: Post) => void;
  onDelete: (id: number) => void;
  onToggleLike: (postId: number, currentlyLiked: boolean) => void;
  onToggleFollow: (userId: number, currentlyFollowed: boolean) => void;
};

function PostCard({
  post,
  currentUserId,
  isDeleting,
  onEdit,
  onDelete,
  onToggleLike,
  onToggleFollow,
}: PostCardProps) {
  const isOwner = currentUserId !== null && currentUserId === post.author.id;
  const canFollow = currentUserId !== null && !isOwner;

  return (
    <article className="post-card">
      {post.imageUrl && (
        <img
          className="post-card__image"
          src={post.imageUrl}
          alt={post.title}
          onError={(e) => {
            e.currentTarget.style.display = "none";
          }}
        />
      )}

      <div className="post-card__body">
        <div className="post-card__header">
          <div>
            <h2 className="post-card__title">{post.title}</h2>
            <div className="post-card__author">
              {post.author.iconUrl && (
                <img
                  className="post-card__author-icon"
                  src={post.author.iconUrl}
                  alt=""
                  onError={(e) => {
                    e.currentTarget.style.display = "none";
                  }}
                />
              )}
              <span className="post-card__author-name">
                {post.author.displayName}（@{post.author.username}）
              </span>
              {canFollow && (
                <button
                  type="button"
                  className={
                    post.author.followedByCurrentUser
                      ? "post-card__unfollow"
                      : "post-card__follow"
                  }
                  onClick={() => onToggleFollow(post.author.id, post.author.followedByCurrentUser)}
                >
                  {post.author.followedByCurrentUser ? "フォロー中" : "フォローする"}
                </button>
              )}
            </div>
          </div>

          {isOwner && (
            <div className="post-card__actions">
              <button
                type="button"
                className="post-card__edit"
                onClick={() => onEdit(post)}
                disabled={isDeleting}
              >
                編集
              </button>
              <button
                type="button"
                className="post-card__delete"
                onClick={() => onDelete(post.id)}
                disabled={isDeleting}
              >
                {isDeleting ? "削除中..." : "削除"}
              </button>
            </div>
          )}
        </div>

        <p>{post.description}</p>

        <section className="post-card__section">
          <h3>何を作ったか</h3>
          <p>{post.whatCreated}</p>
        </section>

        {post.tips && (
          <section className="post-card__section">
            <h3>Tips</h3>
            <p>{post.tips}</p>
          </section>
        )}

        {post.bestPrompt && (
          <section className="post-card__section">
            <h3>Best Prompt</h3>
            <p>{post.bestPrompt}</p>
          </section>
        )}

        <div className="post-card__like-row">
          <button
            type="button"
            className={
              post.likedByCurrentUser ? "post-card__like post-card__like--active" : "post-card__like"
            }
            onClick={() => onToggleLike(post.id, post.likedByCurrentUser)}
            disabled={currentUserId === null}
          >
            {post.likedByCurrentUser ? "♥" : "♡"} {post.likeCount}
          </button>
        </div>

        <PostComments postId={post.id} currentUserId={currentUserId} />
      </div>
    </article>
  );
}

export default PostCard;
