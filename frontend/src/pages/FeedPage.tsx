import { useEffect, useState } from "react";
import type { FormEvent } from "react";
import type { Post } from "../types/Post";
import type { PostFormValues } from "../types/PostForm";
import PostForm from "../components/PostForm";
import PostList from "../components/PostList";
import { apiFetch, extractErrorMessage } from "../api/http";
import { likePost, unlikePost } from "../api/likeApi";
import { followUser, unfollowUser } from "../api/followApi";
import type { AuthState } from "../hooks/useAuth";

const initialFormState: PostFormValues = {
  title: "",
  description: "",
  imageUrl: "",
  whatCreated: "",
  tips: "",
  bestPrompt: "",
};

type FeedPageProps = {
  auth: AuthState;
};

function FeedPage({ auth }: FeedPageProps) {
  const [posts, setPosts] = useState<Post[]>([]);
  const [form, setForm] = useState<PostFormValues>(initialFormState);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [deletingId, setDeletingId] = useState<number | null>(null);

  const fetchPosts = () => {
    apiFetch("/api/posts")
      .then((response) => response.json())
      .then((data) => {
        setPosts(data);
      });
  };

  useEffect(() => {
    fetchPosts();
  }, []);

  const handleChange = (field: keyof PostFormValues, value: string) => {
    setForm((prev) => ({ ...prev, [field]: value }));
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);
    setSubmitting(true);

    try {
      const url =
        editingId !== null ? `/api/posts/${editingId}` : "/api/posts";
      const method = editingId !== null ? "PUT" : "POST";

      const response = await apiFetch(url, {
        method,
        body: JSON.stringify(form),
      });

      if (!response.ok) {
        throw new Error(
          await extractErrorMessage(
            response,
            editingId !== null ? "投稿の更新に失敗しました" : "投稿の作成に失敗しました",
          ),
        );
      }

      setForm(initialFormState);
      setEditingId(null);
      await fetchPosts();
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : editingId !== null
            ? "投稿の更新に失敗しました"
            : "投稿の作成に失敗しました",
      );
    } finally {
      setSubmitting(false);
    }
  };

  const handleToggleLike = async (postId: number, currentlyLiked: boolean) => {
    setPosts((prev) =>
      prev.map((p) =>
        p.id === postId
          ? { ...p, likedByCurrentUser: !currentlyLiked, likeCount: p.likeCount + (currentlyLiked ? -1 : 1) }
          : p,
      ),
    );

    try {
      if (currentlyLiked) {
        await unlikePost(postId);
      } else {
        await likePost(postId);
      }
    } catch (err) {
      setPosts((prev) =>
        prev.map((p) =>
          p.id === postId
            ? { ...p, likedByCurrentUser: currentlyLiked, likeCount: p.likeCount + (currentlyLiked ? 1 : -1) }
            : p,
        ),
      );
      setError(err instanceof Error ? err.message : "いいね操作に失敗しました");
    }
  };

  const handleToggleFollow = async (userId: number, currentlyFollowed: boolean) => {
    setPosts((prev) =>
      prev.map((p) =>
        p.author.id === userId
          ? { ...p, author: { ...p.author, followedByCurrentUser: !currentlyFollowed } }
          : p,
      ),
    );

    try {
      if (currentlyFollowed) {
        await unfollowUser(userId);
      } else {
        await followUser(userId);
      }
    } catch (err) {
      setPosts((prev) =>
        prev.map((p) =>
          p.author.id === userId
            ? { ...p, author: { ...p.author, followedByCurrentUser: currentlyFollowed } }
            : p,
        ),
      );
      setError(err instanceof Error ? err.message : "フォロー操作に失敗しました");
    }
  };

  const handleEdit = (post: Post) => {
    setEditingId(post.id);
    setForm({
      title: post.title,
      description: post.description ?? "",
      imageUrl: post.imageUrl,
      whatCreated: post.whatCreated,
      tips: post.tips ?? "",
      bestPrompt: post.bestPrompt ?? "",
    });
    setError(null);
  };

  const handleCancelEdit = () => {
    setEditingId(null);
    setForm(initialFormState);
    setError(null);
  };

  const handleDelete = async (id: number) => {
    const confirmed = window.confirm("この投稿を削除しますか？");
    if (!confirmed) {
      return;
    }

    setError(null);
    setDeletingId(id);

    try {
      const response = await apiFetch(`/api/posts/${id}`, {
        method: "DELETE",
      });

      if (!response.ok) {
        throw new Error(await extractErrorMessage(response, "投稿の削除に失敗しました"));
      }

      if (editingId === id) {
        setEditingId(null);
        setForm(initialFormState);
      }

      await fetchPosts();
    } catch (err) {
      setError(err instanceof Error ? err.message : "投稿の削除に失敗しました");
    } finally {
      setDeletingId(null);
    }
  };

  return (
    <>
      {auth.user ? (
        <PostForm
          form={form}
          editingId={editingId}
          submitting={submitting}
          error={error}
          onChange={handleChange}
          onSubmit={handleSubmit}
          onCancelEdit={handleCancelEdit}
        />
      ) : (
        !auth.initializing && <p className="post-form__login-required">投稿するにはログインしてください</p>
      )}

      <PostList
        posts={posts}
        currentUserId={auth.user?.id ?? null}
        deletingId={deletingId}
        onEdit={handleEdit}
        onDelete={handleDelete}
        onToggleLike={handleToggleLike}
        onToggleFollow={handleToggleFollow}
      />
    </>
  );
}

export default FeedPage;
