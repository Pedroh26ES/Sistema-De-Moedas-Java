import { useEffect, useState } from "react";

export function useRoute() {
  const [path, setPath] = useState(window.location.pathname);

  useEffect(() => {
    const sync = () => setPath(window.location.pathname);
    window.addEventListener("popstate", sync);
    return () => window.removeEventListener("popstate", sync);
  }, []);

  if (path === "/login") return "login";
  if (path === "/esqueci-senha") return "esqueciSenha";
  if (path === "/redefinir-senha") return "redefinirSenha";
  if (path === "/cadastro") return "cadastro";
  if (path === "/alunos/novo") return "cadastroAluno";
  if (path === "/empresas/nova") return "cadastroEmpresa";
  if (path.startsWith("/aluno")) return "aluno";
  if (path.startsWith("/professor")) return "professor";
  if (path.startsWith("/empresa")) return "empresa";
  return "home";
}
