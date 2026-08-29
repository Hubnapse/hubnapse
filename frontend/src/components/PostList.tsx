import type { Post } from "../types/Post";
import PostCard from "./PostCard";

type PostListProps = {
  posts: Post[];
  currentUserId: number | null;
  deletingId: number | null;
  onEdit: (post: Post) => void;
  onDelete: (id: number) => void;
  onToggleLike: (postId: number, currentlyLiked: boolean) => void;
  onToggleFollow: (userId: number, currentlyFollowed: boolean) => void;
};

function PostList({
  posts,
  currentUserId,
  deletingId,
  onEdit,
  onDelete,
  onToggleLike,
  onToggleFollow,
}: PostListProps) {
  return (
    <div className="post-list">
      {posts.map((post) => (
        <PostCard
          key={post.id}
          post={post}
          currentUserId={currentUserId}
          isDeleting={deletingId === post.id}
          onEdit={onEdit}
          onDelete={onDelete}
          onToggleLike={onToggleLike}
          onToggleFollow={onToggleFollow}
        />
      ))}
    </div>
  );
}

export default PostList;
