import type { User } from "../../types/User";

type CurrentUserBarProps = {
  user: User;
  submitting: boolean;
  error: string | null;
  onLogout: () => Promise<void>;
};

function CurrentUserBar({ user, submitting, error, onLogout }: CurrentUserBarProps) {
  return (
    <section className="current-user-bar">
      <p className="current-user-bar__name">
        {user.displayName}（@{user.username}）としてログイン中
      </p>

      {error && <p className="current-user-bar__error">{error}</p>}

      <button type="button" disabled={submitting} onClick={() => onLogout()}>
        {submitting ? "ログアウト中..." : "ログアウト"}
      </button>
    </section>
  );
}

export default CurrentUserBar;
