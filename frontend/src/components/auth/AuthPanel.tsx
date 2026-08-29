import { useState } from "react";
import type { LoginFormValues, RegisterFormValues } from "../../types/Auth";
import LoginForm from "./LoginForm";
import RegisterForm from "./RegisterForm";

type AuthMode = "login" | "register";

type AuthPanelProps = {
  submitting: boolean;
  error: string | null;
  onLogin: (values: LoginFormValues) => Promise<void>;
  onRegister: (values: RegisterFormValues) => Promise<void>;
};

function AuthPanel({ submitting, error, onLogin, onRegister }: AuthPanelProps) {
  const [mode, setMode] = useState<AuthMode>("login");
  const [registeredMessage, setRegisteredMessage] = useState<string | null>(null);

  const handleRegister = async (values: RegisterFormValues) => {
    await onRegister(values);
    setRegisteredMessage("登録が完了しました。ログインしてください。");
    setMode("login");
  };

  const handleModeChange = (nextMode: AuthMode) => {
    setRegisteredMessage(null);
    setMode(nextMode);
  };

  return (
    <section className="auth-panel">
      <div className="auth-panel__tabs">
        <button
          type="button"
          className={mode === "login" ? "auth-panel__tab--active" : "auth-panel__tab"}
          onClick={() => handleModeChange("login")}
        >
          ログイン
        </button>
        <button
          type="button"
          className={mode === "register" ? "auth-panel__tab--active" : "auth-panel__tab"}
          onClick={() => handleModeChange("register")}
        >
          新規登録
        </button>
      </div>

      {registeredMessage && <p className="auth-panel__success">{registeredMessage}</p>}
      {error && <p className="auth-panel__error">{error}</p>}

      {mode === "login" ? (
        <LoginForm submitting={submitting} onSubmit={onLogin} />
      ) : (
        <RegisterForm submitting={submitting} onSubmit={handleRegister} />
      )}
    </section>
  );
}

export default AuthPanel;
