import { useEffect, useRef, useState } from "react";
import type { FormEvent } from "react";

type CommentFormProps = {
  submitting: boolean;
  placeholder: string;
  submitLabel: string;
  onSubmit: (content: string) => Promise<void>;
  onCancel?: () => void;
  autoFocus?: boolean;
};

function CommentForm({
  submitting,
  placeholder,
  submitLabel,
  onSubmit,
  onCancel,
  autoFocus,
}: CommentFormProps) {
  const [content, setContent] = useState("");
  const [error, setError] = useState<string | null>(null);
  const textareaRef = useRef<HTMLTextAreaElement>(null);

  useEffect(() => {
    if (autoFocus) {
      textareaRef.current?.focus();
    }
  }, [autoFocus]);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);

    try {
      await onSubmit(content);
      setContent("");
    } catch (err) {
      setError(err instanceof Error ? err.message : "投稿に失敗しました");
    }
  };

  return (
    <form className="comment-form" onSubmit={handleSubmit}>
      {error && <p className="comment-form__error">{error}</p>}

      <textarea
        ref={textareaRef}
        className="comment-form__textarea"
        placeholder={placeholder}
        required
        maxLength={1000}
        value={content}
        onChange={(e) => setContent(e.target.value)}
      />

      <div className="comment-form__actions">
        <button type="submit" disabled={submitting}>
          {submitting ? "送信中..." : submitLabel}
        </button>

        {onCancel && (
          <button type="button" className="comment-form__cancel" onClick={onCancel}>
            キャンセル
          </button>
        )}
      </div>
    </form>
  );
}

export default CommentForm;
