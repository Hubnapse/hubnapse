import type { Post } from "../types/Post";
import PostCard from "./PostCard";

type PostListProps = {
  posts: Post[];
  currentUserId: number | null;
  deletingId: number | null;
  onEdit: (post: Post) => void;
  onDelete: (id: number) => void;
};

function PostList({ posts, currentUserId, deletingId, onEdit, onDelete }: PostListProps) {
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
        />
      ))}
    </div>
  );
}

export default PostList;
