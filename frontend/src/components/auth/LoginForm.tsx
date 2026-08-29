import { useState } from "react";
import type { FormEvent } from "react";
import type { LoginFormValues } from "../../types/Auth";

const initialFormState: LoginFormValues = {
  email: "",
  password: "",
};

type LoginFormProps = {
  submitting: boolean;
  onSubmit: (values: LoginFormValues) => Promise<void>;
};

function LoginForm({ submitting, onSubmit }: LoginFormProps) {
  const [form, setForm] = useState<LoginFormValues>(initialFormState);

  const handleChange = (field: keyof LoginFormValues, value: string) => {
    setForm((prev) => ({ ...prev, [field]: value }));
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    try {
      await onSubmit(form);
      setForm(initialFormState);
    } catch {
      // エラー内容は呼び出し元がauthErrorとして表示する
    }
  };

  return (
    <form className="auth-form" onSubmit={handleSubmit}>
      <div className="auth-form__field">
        <label htmlFor="login-email">メールアドレス</label>
        <input
          id="login-email"
          type="email"
          required
          value={form.email}
          onChange={(e) => handleChange("email", e.target.value)}
        />
      </div>

      <div className="auth-form__field">
        <label htmlFor="login-password">パスワード</label>
        <input
          id="login-password"
          type="password"
          required
          value={form.password}
          onChange={(e) => handleChange("password", e.target.value)}
        />
      </div>

      <button type="submit" disabled={submitting}>
        {submitting ? "ログイン中..." : "ログイン"}
      </button>
    </form>
  );
}

export default LoginForm;
