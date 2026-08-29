import type { Post } from "../types/Post";

type PostCardProps = {
  post: Post;
  isDeleting: boolean;
  onEdit: (post: Post) => void;
  onDelete: (id: number) => void;
};

function PostCard({ post, isDeleting, onEdit, onDelete }: PostCardProps) {
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
          <h2 className="post-card__title">{post.title}</h2>
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
      </div>
    </article>
  );
}

export default PostCard;
