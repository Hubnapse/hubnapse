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
    <div>
      <h1>Hubnapse</h1>

      <div>
        {posts.map((post) => (
          <article key={post.id}>
            <h2>{post.title}</h2>

            {post.imageUrl && (
              <img src={post.imageUrl} alt={post.title} width="300" />
            )}

            <p>{post.description}</p>

            <h3>何を作ったか</h3>
            <p>{post.whatCreated}</p>

            {post.tips && (
              <>
                <h3>Tips</h3>
                <p>{post.tips}</p>
              </>
            )}

            {post.bestPrompt && (
              <>
                <h3>Best Prompt</h3>
                <p>{post.bestPrompt}</p>
              </>
            )}

            <hr />
          </article>
        ))}
      </div>
    </div>
  );
}

export default App;
