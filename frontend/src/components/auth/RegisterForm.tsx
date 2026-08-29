import { useState } from "react";
import type { FormEvent } from "react";
import type { RegisterFormValues } from "../../types/Auth";

const initialFormState: RegisterFormValues = {
  username: "",
  displayName: "",
  email: "",
  password: "",
  iconUrl: "",
  bio: "",
};

type RegisterFormProps = {
  submitting: boolean;
  onSubmit: (values: RegisterFormValues) => Promise<void>;
};

function RegisterForm({ submitting, onSubmit }: RegisterFormProps) {
  const [form, setForm] = useState<RegisterFormValues>(initialFormState);

  const handleChange = (field: keyof RegisterFormValues, value: string) => {
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
        <label htmlFor="register-username">
          ユーザー名（半角英数字とアンダースコア・3〜30文字）
        </label>
        <input
          id="register-username"
          type="text"
          required
          minLength={3}
          maxLength={30}
          pattern="^[a-zA-Z0-9_]+$"
          value={form.username}
          onChange={(e) => handleChange("username", e.target.value)}
        />
      </div>

      <div className="auth-form__field">
        <label htmlFor="register-displayName">表示名（最大50文字）</label>
        <input
          id="register-displayName"
          type="text"
          required
          maxLength={50}
          value={form.displayName}
          onChange={(e) => handleChange("displayName", e.target.value)}
        />
      </div>

      <div className="auth-form__field">
        <label htmlFor="register-email">メールアドレス</label>
        <input
          id="register-email"
          type="email"
          required
          value={form.email}
          onChange={(e) => handleChange("email", e.target.value)}
        />
      </div>

      <div className="auth-form__field">
        <label htmlFor="register-password">パスワード（8〜72文字）</label>
        <input
          id="register-password"
          type="password"
          required
          minLength={8}
          maxLength={72}
          value={form.password}
          onChange={(e) => handleChange("password", e.target.value)}
        />
      </div>

      <div className="auth-form__field">
        <label htmlFor="register-iconUrl">アイコンURL</label>
        <input
          id="register-iconUrl"
          type="text"
          value={form.iconUrl}
          onChange={(e) => handleChange("iconUrl", e.target.value)}
        />
      </div>

      <div className="auth-form__field">
        <label htmlFor="register-bio">自己紹介（最大300文字）</label>
        <textarea
          id="register-bio"
          maxLength={300}
          value={form.bio}
          onChange={(e) => handleChange("bio", e.target.value)}
        />
      </div>

      <button type="submit" disabled={submitting}>
        {submitting ? "登録中..." : "新規登録"}
      </button>
    </form>
  );
}

export default RegisterForm;
