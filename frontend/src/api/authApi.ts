import { apiFetch, extractErrorMessage } from "./http";
import type { LoginFormValues, RegisterFormValues } from "../types/Auth";
import type { User } from "../types/User";

export async function registerUser(values: RegisterFormValues): Promise<User> {
  const response = await apiFetch("/api/users", {
    method: "POST",
    body: JSON.stringify(values),
  });

  if (!response.ok) {
    throw new Error(await extractErrorMessage(response, "新規登録に失敗しました"));
  }

  return response.json();
}

export async function login(values: LoginFormValues): Promise<User> {
  const response = await apiFetch("/api/auth/login", {
    method: "POST",
    body: JSON.stringify(values),
  });

  if (!response.ok) {
    throw new Error(await extractErrorMessage(response, "ログインに失敗しました"));
  }

  return response.json();
}

export async function logout(): Promise<void> {
  const response = await apiFetch("/api/auth/logout", { method: "POST" });

  if (!response.ok) {
    throw new Error(await extractErrorMessage(response, "ログアウトに失敗しました"));
  }
}

export async function fetchCurrentUser(): Promise<User | null> {
  const response = await apiFetch("/api/users/me");

  if (response.status === 401) {
    return null;
  }

  if (!response.ok) {
    throw new Error(
      await extractErrorMessage(response, "ログイン状態の確認に失敗しました"),
    );
  }

  return response.json();
}
