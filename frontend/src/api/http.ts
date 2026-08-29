const API_BASE_URL = "http://localhost:8080";

const SAFE_METHODS = new Set(["GET", "HEAD", "OPTIONS"]);

let csrfTokenPromise: Promise<{ headerName: string; token: string }> | null = null;

function fetchCsrfToken(): Promise<{ headerName: string; token: string }> {
  return fetch(`${API_BASE_URL}/api/csrf`, { credentials: "include" }).then(
    async (response) => {
      if (!response.ok) {
        throw new Error("CSRFトークンの取得に失敗しました");
      }

      const data = await response.json();
      return { headerName: data.headerName, token: data.token };
    },
  );
}

function ensureCsrfToken(): Promise<{ headerName: string; token: string }> {
  if (!csrfTokenPromise) {
    csrfTokenPromise = fetchCsrfToken().catch((err) => {
      csrfTokenPromise = null;
      throw err;
    });
  }

  return csrfTokenPromise;
}

export async function apiFetch(
  path: string,
  init: RequestInit = {},
): Promise<Response> {
  const method = (init.method ?? "GET").toUpperCase();
  const headers = new Headers(init.headers);

  if (!SAFE_METHODS.has(method)) {
    const { headerName, token } = await ensureCsrfToken();
    headers.set(headerName, token);
  }

  if (init.body && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }

  return fetch(`${API_BASE_URL}${path}`, {
    ...init,
    method,
    headers,
    credentials: "include",
  });
}

export async function extractErrorMessage(
  response: Response,
  fallback: string,
): Promise<string> {
  try {
    const data = await response.json();
    if (data && typeof data.message === "string") {
      return data.message;
    }
  } catch {
    // JSON以外のレスポンスはフォールバックメッセージを使う
  }

  return fallback;
}
