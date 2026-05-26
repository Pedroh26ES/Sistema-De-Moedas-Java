export function navigateTo(path) {
  window.history.pushState({}, "", path);
  window.dispatchEvent(new PopStateEvent("popstate"));
}

export function dashboardFor(perfil) {
  if (perfil === "PROFESSOR") return "/professor";
  if (perfil === "EMPRESA") return "/empresa";
  return "/aluno";
}
