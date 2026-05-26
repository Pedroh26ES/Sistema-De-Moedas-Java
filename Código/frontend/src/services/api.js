import { API_BASE_URL } from "../config/app";

export async function api(path, options = {}) {
  const headers = options.body ? { "Content-Type": "application/json", ...options.headers } : options.headers;
  const response = await fetch(`${API_BASE_URL}${path}`, { credentials: "include", ...options, headers });
  const text = await response.text();
  let data = null;
  if (text) {
    try {
      data = JSON.parse(text);
    } catch {
      data = { mensagem: text };
    }
  }
  if (!response.ok) {
    const error = new Error(data?.mensagem || `Nao foi possivel concluir a operacao. Status ${response.status}.`);
    error.status = response.status;
    throw error;
  }
  return data;
}
