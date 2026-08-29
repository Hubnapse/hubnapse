import type { FormEvent } from "react";
import type { PostFormValues } from "../types/PostForm";

type PostFormProps = {
  form: PostFormValues;
  editingId: number | null;
  submitting: boolean;
  error: string | null;
  onChange: (field: keyof PostFormValues, value: string) => void;
  onSubmit: (event: FormEvent<HTMLFormElement>) => void;
  onCancelEdit: () => void;
};

function PostForm({
  form,
  editingId,
  submitting,
  error,
  onChange,
  onSubmit,
  onCancelEdit,
}: PostFormProps) {
  return (
    <form className="post-form" onSubmit={onSubmit}>
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
          onChange={(e) => onChange("title", e.target.value)}
        />
      </div>

      <div className="post-form__field">
        <label htmlFor="description">説明（最大500文字）</label>
        <textarea
          id="description"
          maxLength={500}
          value={form.description}
          onChange={(e) => onChange("description", e.target.value)}
        />
      </div>

      <div className="post-form__field">
        <label htmlFor="imageUrl">画像URL（必須）</label>
        <input
          id="imageUrl"
          type="text"
          required
          value={form.imageUrl}
          onChange={(e) => onChange("imageUrl", e.target.value)}
        />
      </div>

      <div className="post-form__field">
        <label htmlFor="whatCreated">何を作ったか（必須）</label>
        <input
          id="whatCreated"
          type="text"
          required
          value={form.whatCreated}
          onChange={(e) => onChange("whatCreated", e.target.value)}
        />
      </div>

      <div className="post-form__field">
        <label htmlFor="tips">Tips</label>
        <textarea
          id="tips"
          value={form.tips}
          onChange={(e) => onChange("tips", e.target.value)}
        />
      </div>

      <div className="post-form__field">
        <label htmlFor="bestPrompt">Best Prompt</label>
        <textarea
          id="bestPrompt"
          value={form.bestPrompt}
          onChange={(e) => onChange("bestPrompt", e.target.value)}
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
            onClick={onCancelEdit}
            disabled={submitting}
          >
            編集をキャンセル
          </button>
        )}
      </div>
    </form>
  );
}

export default PostForm;
