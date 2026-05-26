export const APP_NAME = "Valoriza Aê";
export const HERO_IMAGE =
  "https://images.unsplash.com/photo-1523580846011-d3a5bc25702b?auto=format&fit=crop&w=1600&q=85";
export const CLASSROOM_IMAGE =
  "https://images.unsplash.com/photo-1577896851231-70ef18881754?auto=format&fit=crop&w=1200&q=85";
export const MENTORING_IMAGE =
  "https://images.unsplash.com/photo-1522202176988-66273c2fd55f?auto=format&fit=crop&w=1200&q=85";
export const PARTNER_IMAGE =
  "https://images.unsplash.com/photo-1556741533-6e6a62bd8b49?auto=format&fit=crop&w=1200&q=85";
export const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || "").replace(/\/+$/, "");

export const periodFilters = [
  ["todos", "Todos os registros"],
  ["dia", "Hoje"],
  ["semana", "Esta semana"],
  ["mes", "Este mes"],
  ["ano", "Este ano"]
];

export const benefitDescriptionExample =
  "R$ 20 de credito em produto util para o aluno. | Como usar: apresente o codigo do cupom ao parceiro e aguarde a validacao antes de utilizar.";
