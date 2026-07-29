import "./App.css";
import { useEffect, useState } from "react";
import type { FormEvent } from "react";
import type { Post } from "./types/Post";

const initialFormState = {
  title: "",
  description: "",
  imageUrl: "",
  whatCreated: "",
  tips: "",
  bestPrompt: "",
};

function App() {
  const [posts, setPosts] = useState<Post[]>([]);
  const [form, setForm] = useState(initialFormState);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [deletingId, setDeletingId] = useState<number | null>(null);

  const fetchPosts = () => {
    fetch("http://localhost:8080/api/posts")
      .then((response) => response.json())
      .then((data) => {
        setPosts(data);
      });
  };

  useEffect(() => {
    fetchPosts();
  }, []);

  const handleChange = (
    field: keyof typeof initialFormState,
    value: string,
  ) => {
    setForm((prev) => ({ ...prev, [field]: value }));
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);
    setSubmitting(true);

    try {
      const url =
        editingId !== null
          ? `http://localhost:8080/api/posts/${editingId}`
          : "http://localhost:8080/api/posts";
      const method = editingId !== null ? "PUT" : "POST";

      const response = await fetch(url, {
        method,
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(form),
      });

      if (!response.ok) {
        throw new Error(
          editingId !== null ? "投稿の更新に失敗しました" : "投稿の作成に失敗しました",
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
      const response = await fetch(`http://localhost:8080/api/posts/${id}`, {
        method: "DELETE",
      });

      if (!response.ok) {
        throw new Error("投稿の削除に失敗しました");
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
    <main className="post-list-page">
      <h1>Hubnapse</h1>

      <form className="post-form" onSubmit={handleSubmit}>
        <h2>{editingId !== null ? "投稿を編集" : "投稿を作成"}</h2>

        {error && <p className="post-form__error">{error}</p>}

        <div className="post-form__field">
          <label htmlFor="title">タイトル（必須・最大100文字）</label>
          <input
            id="title"
            type="text"
            required
            maxLength={100}
            value={form.title}
            onChange={(e) => handleChange("title", e.target.value)}
          />
        </div>

        <div className="post-form__field">
          <label htmlFor="description">説明（最大500文字）</label>
          <textarea
            id="description"
            maxLength={500}
            value={form.description}
            onChange={(e) => handleChange("description", e.target.value)}
          />
        </div>

        <div className="post-form__field">
          <label htmlFor="imageUrl">画像URL（必須）</label>
          <input
            id="imageUrl"
            type="text"
            required
            value={form.imageUrl}
            onChange={(e) => handleChange("imageUrl", e.target.value)}
          />
        </div>

        <div className="post-form__field">
          <label htmlFor="whatCreated">何を作ったか（必須）</label>
          <input
            id="whatCreated"
            type="text"
            required
            value={form.whatCreated}
            onChange={(e) => handleChange("whatCreated", e.target.value)}
          />
        </div>

        <div className="post-form__field">
          <label htmlFor="tips">Tips</label>
          <textarea
            id="tips"
            value={form.tips}
            onChange={(e) => handleChange("tips", e.target.value)}
          />
        </div>

        <div className="post-form__field">
          <label htmlFor="bestPrompt">Best Prompt</label>
          <textarea
            id="bestPrompt"
            value={form.bestPrompt}
            onChange={(e) => handleChange("bestPrompt", e.target.value)}
          />
        </div>

        <div className="post-form__actions">
          <button type="submit" disabled={submitting}>
            {submitting
              ? editingId !== null
                ? "更新中..."
                : "投稿中..."
              : editingId !== null
                ? "更新する"
                : "投稿する"}
          </button>

          {editingId !== null && (
            <button
              type="button"
              className="post-form__cancel"
              onClick={handleCancelEdit}
              disabled={submitting}
            >
              編集をキャンセル
            </button>
          )}
        </div>
      </form>

      <div className="post-list">
        {posts.map((post) => (
          <article className="post-card" key={post.id}>
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
                    onClick={() => handleEdit(post)}
                    disabled={deletingId === post.id}
                  >
                    編集
                  </button>
                  <button
                    type="button"
                    className="post-card__delete"
                    onClick={() => handleDelete(post.id)}
                    disabled={deletingId === post.id}
                  >
                    {deletingId === post.id ? "削除中..." : "削除"}
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
        ))}
      </div>
    </main>
  );
}

export default App;
