import "./App.css";
import { useEffect, useState } from "react";
import type { Post } from "./types/Post";

function App() {
  const [posts, setPosts] = useState<Post[]>([]);

  useEffect(() => {
    fetch("http://localhost:8080/api/posts")
      .then((response) => response.json())
      .then((data) => {
        setPosts(data);
        console.log(data);
      });
  }, []);

  return (
    <main className="post-list-page">
      <h1>Hubnapse</h1>

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
