import { useCallback, useEffect, useState } from "react";
import {
  fetchCurrentUser,
  login as loginRequest,
  logout as logoutRequest,
  registerUser,
} from "../api/authApi";
import type { LoginFormValues, RegisterFormValues } from "../types/Auth";
import type { User } from "../types/User";

export function useAuth() {
  const [user, setUser] = useState<User | null>(null);
  const [initializing, setInitializing] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [authError, setAuthError] = useState<string | null>(null);

  useEffect(() => {
    fetchCurrentUser()
      .then(setUser)
      .catch((err) => {
        setAuthError(
          err instanceof Error ? err.message : "ログイン状態の確認に失敗しました",
        );
      })
      .finally(() => setInitializing(false));
  }, []);

  const register = useCallback(async (values: RegisterFormValues) => {
    setAuthError(null);
    setSubmitting(true);

    try {
      await registerUser(values);
    } catch (err) {
      setAuthError(err instanceof Error ? err.message : "新規登録に失敗しました");
      throw err;
    } finally {
      setSubmitting(false);
    }
  }, []);

  const login = useCallback(async (values: LoginFormValues) => {
    setAuthError(null);
    setSubmitting(true);

    try {
      const loggedInUser = await loginRequest(values);
      setUser(loggedInUser);
    } catch (err) {
      setAuthError(err instanceof Error ? err.message : "ログインに失敗しました");
      throw err;
    } finally {
      setSubmitting(false);
    }
  }, []);

  const logout = useCallback(async () => {
    setAuthError(null);
    setSubmitting(true);

    try {
      await logoutRequest();
      setUser(null);
    } catch (err) {
      setAuthError(err instanceof Error ? err.message : "ログアウトに失敗しました");
      throw err;
    } finally {
      setSubmitting(false);
    }
  }, []);

  return { user, initializing, submitting, authError, login, register, logout };
}
