export const money = new Intl.NumberFormat("pt-BR");
export const dateTime = new Intl.DateTimeFormat("pt-BR", {
  dateStyle: "short",
  timeStyle: "short"
});

export function formatDate(value) {
  try {
    return dateTime.format(new Date(value));
  } catch {
    return value;
  }
}

export function isWithinPeriod(value, period) {
  if (period === "todos") return true;
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return true;
  const now = new Date();
  const start = new Date(now);
  start.setHours(0, 0, 0, 0);
  if (period === "dia") return date >= start;
  if (period === "semana") {
    const weekStart = new Date(start);
    weekStart.setDate(start.getDate() - start.getDay());
    return date >= weekStart;
  }
  if (period === "mes") return date.getFullYear() === now.getFullYear() && date.getMonth() === now.getMonth();
  if (period === "ano") return date.getFullYear() === now.getFullYear();
  return true;
}

export function labelTipo(tipo) {
  const labels = {
    CREDITO_SEMESTRAL: "Credito semestral",
    ENVIO_MOEDAS: "Envio de moedas",
    RESGATE_VANTAGEM: "Resgate"
  };
  return labels[tipo] || tipo;
}

export function couponColumnText(row) {
  if (row.tipo === "RESGATE_VANTAGEM") return "Gerado no resgate";
  if (row.tipo === "ENVIO_MOEDAS") return "Nao se aplica";
  if (row.tipo === "CREDITO_SEMESTRAL") return "Cota semestral";
  return "Nao se aplica";
}

export function formatCpf(value = "") {
  const digits = String(value).replace(/\D/g, "");
  if (digits.length !== 11) return value || "Nao informado";
  return `${digits.slice(0, 3)}.${digits.slice(3, 6)}.${digits.slice(6, 9)}-${digits.slice(9)}`;
}

export function formatNotificationText(value = "") {
  return value
    .replace(/\s*Link do QR Code:\s*https?:\/\/\S+/gi, "")
    .replace(/\s*Tela de validacao:\s*https?:\/\/\S+/gi, "")
    .replace(/\s*QR Code:\s*https?:\/\/\S+/gi, "")
    .replace(/\s*https?:\/\/\S+/gi, "")
    .replace(/\s{2,}/g, " ")
    .trim();
}
