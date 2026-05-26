import { useEffect, useState } from "react";
import { AuthRequired, LoadingScreen, Protected, Toast } from "./components/ui";
import { useRoute } from "./hooks/useRoute";
import Home from "./pages/Home";
import { CadastroAluno, CadastroEmpresa, CadastroEscolha, EsqueciSenha, Login, RedefinirSenha } from "./pages/Auth";
import AlunoDashboard from "./pages/AlunoDashboard";
import ProfessorDashboard from "./pages/ProfessorDashboard";
import EmpresaDashboard from "./pages/EmpresaDashboard";
import { api } from "./services/api";
import { dashboardFor, navigateTo } from "./utils/navigation";

function App() {
  const route = useRoute();
  const [session, setSession] = useState(null);
  const [loadingSession, setLoadingSession] = useState(true);
  const [toast, setToast] = useState(null);

  const refreshSession = async () => {
    try {
      const current = await api("/api/me");
      setSession(current);
    } catch {
      setSession(null);
    } finally {
      setLoadingSession(false);
    }
  };

  useEffect(() => {
    refreshSession();
  }, []);

  const notify = (type, message) => {
    setToast({ type, message });
    window.setTimeout(() => setToast(null), 4500);
  };

  const logout = async () => {
    await api("/api/logout", { method: "POST" });
    setSession(null);
    navigateTo("/");
    notify("success", "Sessao encerrada.");
  };

  if (loadingSession) {
    return <LoadingScreen />;
  }

  return (
    <>
      {toast && <Toast type={toast.type}>{toast.message}</Toast>}
      {route === "home" && <Home session={session} />}
      {route === "login" && <Login onLogin={setSession} notify={notify} />}
      {route === "esqueciSenha" && <EsqueciSenha notify={notify} />}
      {route === "redefinirSenha" && <RedefinirSenha notify={notify} />}
      {route === "cadastro" && <CadastroEscolha />}
      {route === "cadastroAluno" && <CadastroAluno notify={notify} />}
      {route === "cadastroEmpresa" && <CadastroEmpresa notify={notify} />}
      {route === "aluno" && (
        <Protected perfil="ALUNO" session={session}>
          <AlunoDashboard session={session} logout={logout} notify={notify} />
        </Protected>
      )}
      {route === "professor" && (
        <Protected perfil="PROFESSOR" session={session}>
          <ProfessorDashboard session={session} logout={logout} notify={notify} />
        </Protected>
      )}
      {route === "empresa" && (
        <Protected perfil="EMPRESA" session={session}>
          <EmpresaDashboard session={session} logout={logout} notify={notify} />
        </Protected>
      )}
    </>
  );
}

export default App;
