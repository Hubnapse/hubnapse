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
      const response = await fetch("http://localhost:8080/api/posts", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(form),
      });

      if (!response.ok) {
        throw new Error("投稿の作成に失敗しました");
      }

      setForm(initialFormState);
      await fetchPosts();
    } catch (err) {
      setError(err instanceof Error ? err.message : "投稿の作成に失敗しました");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <main className="post-list-page">
      <h1>Hubnapse</h1>

      <form className="post-form" onSubmit={handleSubmit}>
        <h2>投稿を作成</h2>

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

        <button type="submit" disabled={submitting}>
          {submitting ? "投稿中..." : "投稿する"}
        </button>
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
              <h2 className="post-card__title">{post.title}</h2>

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
