import React, { useEffect, useMemo, useState } from "react";
import { createRoot } from "react-dom/client";
import {
  ArrowRight,
  BadgeCheck,
  BarChart3,
  Bell,
  Building2,
  CalendarDays,
  CheckCircle2,
  Coins,
  ClipboardCheck,
  Copy,
  EyeOff,
  Filter,
  Gift,
  HandCoins,
  House,
  LayoutDashboard,
  LogOut,
  MailCheck,
  Power,
  ReceiptText,
  ShieldCheck,
  Sparkles,
  Store,
  Search,
  Send,
  TicketCheck,
  Trophy,
  UserRound,
  UsersRound,
  WalletCards
} from "lucide-react";
import "./styles.css";

const APP_NAME = "Valoriza Aê";
const HERO_IMAGE =
  "https://images.unsplash.com/photo-1523580846011-d3a5bc25702b?auto=format&fit=crop&w=1600&q=85";
const CLASSROOM_IMAGE =
  "https://images.unsplash.com/photo-1577896851231-70ef18881754?auto=format&fit=crop&w=1200&q=85";
const MENTORING_IMAGE =
  "https://images.unsplash.com/photo-1522202176988-66273c2fd55f?auto=format&fit=crop&w=1200&q=85";
const PARTNER_IMAGE =
  "https://images.unsplash.com/photo-1556741533-6e6a62bd8b49?auto=format&fit=crop&w=1200&q=85";

const money = new Intl.NumberFormat("pt-BR");
const dateTime = new Intl.DateTimeFormat("pt-BR", {
  dateStyle: "short",
  timeStyle: "short"
});
const periodFilters = [
  ["todos", "Todos os registros"],
  ["dia", "Hoje"],
  ["semana", "Esta semana"],
  ["mes", "Este mes"],
  ["ano", "Este ano"]
];
const benefitDescriptionExample =
  "R$ 20 de credito em produto util para o aluno. | Como usar: apresente o codigo do cupom ao parceiro e aguarde a validacao antes de utilizar.";

function parseBenefitDescription(description = "") {
  const parts = description
    .split("|")
    .map((part) => part.trim())
    .filter(Boolean);
  if (parts.length < 2) {
    return [];
  }
  return parts.map((part) => {
    const separator = part.indexOf(":");
    if (separator === -1) {
      return { type: "description", label: "", text: part };
    }
    const label = part.slice(0, separator).trim();
    const text = part.slice(separator + 1).trim();
    if (label.toLowerCase() === "limite") {
      return null;
    }
    if (label.toLowerCase() === "valor pratico") {
      return { type: "description", label: "", text };
    }
    return {
      type: "detail",
      label,
      text
    };
  }).filter((item) => item?.text);
}

async function api(path, options = {}) {
  const headers = options.body ? { "Content-Type": "application/json", ...options.headers } : options.headers;
  const response = await fetch(path, { credentials: "include", ...options, headers });
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

function navigateTo(path) {
  window.history.pushState({}, "", path);
  window.dispatchEvent(new PopStateEvent("popstate"));
}

function useRoute() {
  const [path, setPath] = useState(window.location.pathname);

  useEffect(() => {
    const sync = () => setPath(window.location.pathname);
    window.addEventListener("popstate", sync);
    return () => window.removeEventListener("popstate", sync);
  }, []);

  if (path === "/login") return "login";
  if (path === "/cadastro") return "cadastro";
  if (path === "/alunos/novo") return "cadastroAluno";
  if (path === "/empresas/nova") return "cadastroEmpresa";
  if (path.startsWith("/aluno")) return "aluno";
  if (path.startsWith("/professor")) return "professor";
  if (path.startsWith("/empresa")) return "empresa";
  return "home";
}

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

function Protected({ perfil, session, children }) {
  if (!session || session.perfil !== perfil) {
    return (
      <AuthRequired
        title="Acesso restrito"
        message="Entre com o perfil correto para acessar este painel."
      />
    );
  }
  return children;
}

function Home({ session }) {
  const dashboardPath = session ? dashboardFor(session.perfil) : "/login";

  return (
    <main className="marketing-page">
      <MarketingNav session={session} />
      <section className="hero-section">
        <img src={HERO_IMAGE} alt="Campus universitario com estudantes" />
        <div className="hero-overlay" />
        <div className="hero-content">
          <div className="hero-copy">
            <span className="eyebrow">{APP_NAME}</span>
            <h1>Reconhecimento academico que vira beneficio no campus.</h1>
            <p>
              Professores valorizam boas participacoes, alunos acompanham suas moedas e parceiros
              confirmam os cupons no atendimento.
            </p>
            <div className="hero-actions">
              <button className="primary-button" onClick={() => navigateTo(dashboardPath)}>
                {session ? "Abrir meu painel" : "Entrar no sistema"}
                <ArrowRight size={18} />
              </button>
              <button className="ghost-button hero-ghost" onClick={() => navigateTo("/cadastro")}>
                Comecar cadastro
              </button>
            </div>
            <div className="trust-row hero-trust">
              <span><ShieldCheck size={17} /> Entrada segura</span>
              <span><ReceiptText size={17} /> Historico organizado</span>
              <span><TicketCheck size={17} /> Cupom validado</span>
            </div>
          </div>
        </div>
      </section>

      <section className="impact-strip">
        <ImpactMetric icon={<Coins />} value="Reconhecimento visivel" label="boas entregas ganham destaque e incentivam novas participacoes" />
        <ImpactMetric icon={<BadgeCheck />} value="Beneficios reais" label="o desempenho pode virar descontos, servicos e experiencias no campus" />
        <ImpactMetric icon={<MailCheck />} value="Uso com confianca" label="quem recebe, oferece e valida consegue acompanhar tudo com clareza" />
      </section>

      <section className="section-block value-section">
        <div className="section-heading">
          <span className="eyebrow">Por que usar</span>
          <h2>O reconhecimento do campus ganha ritmo, valor e prova.</h2>
          <p>
            O Valoriza Aê transforma boas entregas em uma experiencia clara: moedas com proposito,
            beneficios reais e comprovantes acompanhando cada etapa.
          </p>
        </div>
        <div className="benefit-grid">
          <BenefitCard
            icon={<Sparkles />}
            title="Reconhecimento que engaja"
            text="Cada conquista vira saldo visivel, historico confiavel e motivacao para continuar participando."
          />
          <BenefitCard
            icon={<BarChart3 />}
            title="Beneficios com valor real"
            text="O catalogo mostra vantagens uteis, custo em moedas e instrucoes de retirada sem confusao."
          />
          <BenefitCard
            icon={<Store />}
            title="Gestao sem ruido"
            text="Saldos, cupons, notificacoes e status ficam reunidos para consulta rapida por quem precisa agir."
          />
          <BenefitCard
            icon={<TicketCheck />}
            title="Confiança na entrega"
            text="A validacao do cupom confirma que o beneficio foi usado e fecha o ciclo com rastreabilidade."
          />
        </div>
      </section>

      <section className="workflow-section">
        <div className="workflow-copy">
          <span className="eyebrow">Experiencia completa</span>
          <h2>Reconhecer, resgatar e comprovar em poucos passos.</h2>
          <p>
            O sistema guia cada pessoa para a acao certa e mantem o processo simples para usar no dia a dia.
          </p>
        </div>
        <div className="workflow-list">
          <WorkflowStep number="01" icon={<HandCoins />} title="Valor reconhecido" text="Boas entregas viram moedas com motivo registrado e historico consultavel." />
          <WorkflowStep number="02" icon={<ReceiptText />} title="Beneficio escolhido" text="O saldo se transforma em cupom para uma vantagem cadastrada no catalogo." />
          <WorkflowStep number="03" icon={<TicketCheck />} title="Uso confirmado" text="A validacao fecha o atendimento e deixa o status claro para todos." />
        </div>
      </section>

      <section className="closing-cta">
        <img src={CLASSROOM_IMAGE} alt="Sala de aula com estudantes" />
        <div>
          <span className="eyebrow">Pronto para usar</span>
          <h2>Acesse o painel e continue de onde parou.</h2>
          <button className="primary-button" onClick={() => navigateTo(dashboardPath)}>
            Acessar agora
            <ArrowRight size={18} />
          </button>
        </div>
      </section>
    </main>
  );
}

function MarketingNav({ session }) {
  return (
    <nav className="marketing-nav">
      <button className="brand" onClick={() => navigateTo("/")}>
        <span><Coins size={22} /></span>
        {APP_NAME}
      </button>
      <div className="nav-actions">
        <button className="nav-link" onClick={() => navigateTo("/cadastro")}>Cadastrar</button>
        <button className="nav-link" onClick={() => navigateTo("/empresas/nova")}>Parceiros</button>
        <button className="outline-button" onClick={() => navigateTo(session ? dashboardFor(session.perfil) : "/login")}>
          {session ? "Meu painel" : "Login"}
        </button>
      </div>
    </nav>
  );
}

function ImpactMetric({ icon, value, label }) {
  return (
    <article>
      <span>{React.cloneElement(icon, { size: 22 })}</span>
      <strong>{value}</strong>
      <p>{label}</p>
    </article>
  );
}

function BenefitCard({ icon, title, text }) {
  return (
    <article className="benefit-card">
      <span>{React.cloneElement(icon, { size: 22 })}</span>
      <h3>{title}</h3>
      <p>{text}</p>
    </article>
  );
}

function WorkflowStep({ number, icon, title, text }) {
  return (
    <article className="workflow-step">
      <strong>{number}</strong>
      <span>{React.cloneElement(icon, { size: 21 })}</span>
      <div>
        <h3>{title}</h3>
        <p>{text}</p>
      </div>
    </article>
  );
}

function CadastroEscolha() {
  return (
    <AuthLayout title="Escolha seu cadastro" subtitle="Aluno cria conta propria; empresa parceira entra para publicar beneficios.">
      <div className="signup-choice-grid">
        <button className="signup-choice-card" onClick={() => navigateTo("/alunos/novo")}>
          <span><UserRound size={24} /></span>
          <strong>Sou aluno</strong>
          <p>Acompanhar saldo, consultar cupons e resgatar beneficios disponiveis.</p>
          <small>Continuar <ArrowRight size={15} /></small>
        </button>
        <button className="signup-choice-card" onClick={() => navigateTo("/empresas/nova")}>
          <span><Store size={24} /></span>
          <strong>Sou empresa parceira</strong>
          <p>Publicar vantagens, acompanhar retiradas e validar cupons no atendimento.</p>
          <small>Continuar <ArrowRight size={15} /></small>
        </button>
      </div>
      <div className="signup-note">
        <ShieldCheck size={18} />
        <p>Professores sao pre-cadastrados pela instituicao e acessam pelo login recebido.</p>
      </div>
    </AuthLayout>
  );
}

function Login({ onLogin, notify }) {
  const [form, setForm] = useState({ email: "aluno@moedas.com", senha: "123456" });
  const [loading, setLoading] = useState(false);

  const submit = async (event) => {
    event.preventDefault();
    setLoading(true);
    try {
      const session = await api("/api/login", { method: "POST", body: JSON.stringify(form) });
      onLogin(session);
      navigateTo(dashboardFor(session.perfil));
    } catch (error) {
      notify("error", error.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthLayout title="Entrar no sistema" subtitle="Use o perfil recebido para abrir apenas o painel correspondente.">
      <form className="stack-form" onSubmit={submit}>
        <label>Email
          <input type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} required />
        </label>
        <label>Senha
          <input type="password" value={form.senha} onChange={(e) => setForm({ ...form, senha: e.target.value })} required />
        </label>
        <button className="primary-button full" disabled={loading} type="submit">
          {loading ? "Entrando..." : "Entrar"}
        </button>
      </form>
      <div className="demo-logins">
        <strong>Acessos demo</strong>
        <button onClick={() => setForm({ email: "aluno@moedas.com", senha: "123456" })}>Aluno</button>
        <button onClick={() => setForm({ email: "professor@moedas.com", senha: "123456" })}>Professor</button>
        <button onClick={() => setForm({ email: "empresa@moedas.com", senha: "123456" })}>Empresa</button>
      </div>
    </AuthLayout>
  );
}

function CadastroAluno({ notify }) {
  const [instituicoes, setInstituicoes] = useState([]);
  const [form, setForm] = useState({
    nome: "",
    email: "",
    senha: "",
    cpf: "",
    rg: "",
    endereco: "",
    instituicaoId: "",
    curso: ""
  });

  useEffect(() => {
    api("/api/instituicoes")
      .then((items) => {
        setInstituicoes(items);
        setForm((current) => ({
          ...current,
          instituicaoId: items[0]?.id || "",
          curso: items[0]?.cursos?.[0] || ""
        }));
      })
      .catch((error) => notify("error", error.message));
  }, []);

  const instituicaoSelecionada = instituicoes.find((instituicao) => String(instituicao.id) === String(form.instituicaoId));
  const cursosDisponiveis = instituicaoSelecionada?.cursos || [];
  const trocarInstituicao = (instituicaoId) => {
    const instituicao = instituicoes.find((item) => String(item.id) === String(instituicaoId));
    setForm({
      ...form,
      instituicaoId,
      curso: instituicao?.cursos?.[0] || ""
    });
  };

  const submit = async (event) => {
    event.preventDefault();
    try {
      await api("/api/alunos", { method: "POST", body: JSON.stringify({ ...form, instituicaoId: Number(form.instituicaoId) }) });
      notify("success", "Aluno cadastrado com sucesso.");
      navigateTo("/login");
    } catch (error) {
      notify("error", error.message);
    }
  };

  return (
    <AuthLayout title="Cadastro de aluno" subtitle="Informe seus dados e selecione a instituicao pre-cadastrada em que voce estuda.">
      <form className="grid-form" onSubmit={submit}>
        <Field label="Nome" value={form.nome} onChange={(nome) => setForm({ ...form, nome })} />
        <Field label="Email" type="email" value={form.email} onChange={(email) => setForm({ ...form, email })} />
        <Field label="Senha" type="password" value={form.senha} onChange={(senha) => setForm({ ...form, senha })} />
        <Field label="CPF" value={form.cpf} onChange={(cpf) => setForm({ ...form, cpf })} />
        <Field label="RG" value={form.rg} onChange={(rg) => setForm({ ...form, rg })} />
        <label className="wide">Endereco
          <input value={form.endereco} onChange={(e) => setForm({ ...form, endereco: e.target.value })} required />
        </label>
        <label>Instituicao
          <select value={form.instituicaoId} onChange={(e) => trocarInstituicao(e.target.value)} required>
            {instituicoes.map((instituicao) => (
              <option key={instituicao.id} value={instituicao.id}>{instituicao.nome} - {instituicao.cidade}</option>
            ))}
          </select>
        </label>
        <label>Curso
          <select value={form.curso} onChange={(e) => setForm({ ...form, curso: e.target.value })} required disabled={!cursosDisponiveis.length}>
            {cursosDisponiveis.map((curso) => (
              <option key={curso} value={curso}>{curso}</option>
            ))}
          </select>
        </label>
        <button className="primary-button full wide" type="submit">Cadastrar aluno</button>
      </form>
    </AuthLayout>
  );
}

function CadastroEmpresa({ notify }) {
  const [form, setForm] = useState({ nome: "", email: "", senha: "", cnpj: "", endereco: "", contato: "" });

  const submit = async (event) => {
    event.preventDefault();
    try {
      await api("/api/empresas", { method: "POST", body: JSON.stringify(form) });
      notify("success", "Empresa cadastrada com sucesso.");
      navigateTo("/login");
    } catch (error) {
      notify("error", error.message);
    }
  };

  return (
    <AuthLayout title="Cadastro de empresa" subtitle="Dados do parceiro que vai oferecer beneficios e confirmar cupons.">
      <form className="grid-form" onSubmit={submit}>
        <Field label="Nome da empresa" value={form.nome} onChange={(nome) => setForm({ ...form, nome })} />
        <Field label="Email" type="email" value={form.email} onChange={(email) => setForm({ ...form, email })} />
        <Field label="Senha" type="password" value={form.senha} onChange={(senha) => setForm({ ...form, senha })} />
        <Field label="CNPJ" value={form.cnpj} onChange={(cnpj) => setForm({ ...form, cnpj })} />
        <Field label="Contato" value={form.contato} onChange={(contato) => setForm({ ...form, contato })} />
        <label className="wide">Endereco
          <input value={form.endereco} onChange={(e) => setForm({ ...form, endereco: e.target.value })} required />
        </label>
        <button className="primary-button full wide" type="submit">Cadastrar empresa</button>
      </form>
    </AuthLayout>
  );
}

function AuthLayout({ title, subtitle, children }) {
  return (
    <main className="auth-page">
      <button className="brand auth-brand" onClick={() => navigateTo("/")}>
        <span><Coins size={22} /></span>
        {APP_NAME}
      </button>
      <section className="auth-card">
        <div className="auth-visual">
          <img src={HERO_IMAGE} alt="Campus universitario" />
          <div>
            <span className="eyebrow">{APP_NAME}</span>
            <h2>Reconhecimento que motiva, beneficios que fazem diferenca.</h2>
          </div>
        </div>
        <div className="auth-panel">
          <h1>{title}</h1>
          <p>{subtitle}</p>
          {children}
        </div>
      </section>
    </main>
  );
}

function AlunoDashboard({ session, logout, notify }) {
  const { data, loading, reload } = useDashboard("/api/aluno/dashboard", notify);
  const [catalogQuery, setCatalogQuery] = useState("");
  const [catalogFilter, setCatalogFilter] = useState("todos");
  const [lastCoupon, setLastCoupon] = useState(null);

  const saldo = data?.aluno?.saldoMoedas || 0;
  const vantagens = data?.vantagens || [];
  const extrato = data?.extrato || [];
  const moedasRecebidas = extrato
    .filter((row) => row.tipo === "ENVIO_MOEDAS")
    .reduce((total, row) => total + row.valor, 0);
  const moedasUsadas = extrato
    .filter((row) => row.tipo === "RESGATE_VANTAGEM")
    .reduce((total, row) => total + row.valor, 0);
  const cuponsValidados = extrato.filter((row) => row.codigoCupom && row.cupomValidado).length;
  const journeyLevels = [
    { min: 0, title: "Em evolucao" },
    { min: 200, title: "Protagonista" },
    { min: 500, title: "Embaixador" }
  ];
  const currentJourney = [...journeyLevels].reverse().find((level) => moedasRecebidas >= level.min) || journeyLevels[0];
  const nextJourney = journeyLevels.find((level) => moedasRecebidas < level.min);
  const journeyMissing = Math.max((nextJourney?.min || 0) - moedasRecebidas, 0);
  const nivelAluno = currentJourney.title;
  const bonusLevels = [
    { spent: 100, title: "Nivel Essencial", benefit: "Voucher de impressao", reward: "brinde extra de 60 paginas P&B." },
    { spent: 250, title: "Nivel Plus", benefit: "Combo cafe e estudo", reward: "retirada extra do combo no parceiro." },
    { spent: 500, title: "Nivel Elite", benefit: "Credito na livraria", reward: "credito extra de R$ 30 em material de apoio." }
  ];
  const unlockedBonus = [...bonusLevels].reverse().find((bonus) => moedasUsadas >= bonus.spent);
  const nextBonus = bonusLevels.find((bonus) => moedasUsadas < bonus.spent);
  const bonusTarget = nextBonus || unlockedBonus || bonusLevels[0];
  const bonusProgress = bonusTarget ? Math.min(100, Math.round((moedasUsadas / bonusTarget.spent) * 100)) : 100;
  const missingForBonus = Math.max((bonusTarget?.spent || 0) - moedasUsadas, 0);
  const bonusTitle = unlockedBonus ? `${unlockedBonus.title} liberado` : `${bonusTarget.title} em andamento`;
  const impactoAluno = [
    ["Reconhecimento", `${money.format(moedasRecebidas)} moedas recebidas por merito`],
    ["Uso dos beneficios", `${money.format(moedasUsadas)} moedas gastas em resgates`],
    ["Cupons confirmados", `${cuponsValidados} beneficios validados por parceiros`]
  ];
  const filteredVantagens = useMemo(() => {
    const query = catalogQuery.trim().toLowerCase();
    return vantagens.filter((vantagem) => {
      const matchesQuery = !query
        || vantagem.titulo.toLowerCase().includes(query)
        || vantagem.descricao.toLowerCase().includes(query)
        || vantagem.empresaNome.toLowerCase().includes(query);
      const matchesFilter = catalogFilter === "todos"
        || (catalogFilter === "disponiveis" && saldo >= vantagem.custoMoedas && !vantagem.adquirida)
        || (catalogFilter === "adquiridas" && vantagem.adquirida)
        || (catalogFilter === "metas" && saldo < vantagem.custoMoedas && !vantagem.adquirida);
      return matchesQuery && matchesFilter;
    });
  }, [catalogFilter, catalogQuery, saldo, vantagens]);

  const resgatar = async (vantagemId) => {
    try {
      const vantagem = vantagens.find((item) => item.id === vantagemId);
      const cupom = await api("/api/aluno/resgates", { method: "POST", body: JSON.stringify({ vantagemId }) });
      setLastCoupon({ codigo: cupom.codigo, vantagem: vantagem?.titulo || "Vantagem resgatada" });
      notify("success", `${cupom.mensagem} Codigo: ${cupom.codigo}`);
      reload();
    } catch (error) {
      notify("error", error.message);
    }
  };

  const copiarCupom = async () => {
    if (!lastCoupon) return;
    try {
      await navigator.clipboard.writeText(lastCoupon.codigo);
      notify("success", "Cupom copiado.");
    } catch {
      notify("error", "Nao foi possivel copiar o cupom automaticamente.");
    }
  };

  if (loading) return <LoadingScreen />;
  if (!data) return null;

  return (
    <DashboardShell session={session} active="aluno" logout={logout} summary={data.resumo}>
      <div className="dashboard-grid">
        <MetricCard icon={<WalletCards />} label="Saldo atual" value={`${money.format(data.aluno.saldoMoedas)} moedas`} tone="green" />
        <MetricCard icon={<ReceiptText />} label="Moedas recebidas" value={`${money.format(moedasRecebidas)} moedas`} tone="amber" />
        <MetricCard icon={<TicketCheck />} label="Cupons validados" value={cuponsValidados} tone="blue" />
      </div>

      <div className="dashboard-insight-grid">
        <section className="surface-card bonus-card">
          <div className="bonus-card-icon"><Gift size={24} /></div>
          <SectionTitle eyebrow="Recompensa por resgates" title={bonusTitle} />
          <p className="compact-note">
            {unlockedBonus
              ? `Ganho atual: ${unlockedBonus.benefit} como ${unlockedBonus.reward}`
              : `Primeiro ganho: ${bonusTarget.benefit} como ${bonusTarget.reward}`}
          </p>
          <div className="bonus-summary">
            <span>
              <strong>{money.format(moedasUsadas)}</strong>
              usadas em resgates
            </span>
            <span>
              <strong>{nextBonus ? money.format(missingForBonus) : "0"}</strong>
              {nextBonus ? `faltam para ${nextBonus.title}` : "metas completas"}
            </span>
          </div>
          <div className="progress-track" aria-label="Progresso da meta">
            <span style={{ width: `${bonusProgress}%` }} />
          </div>
          <strong>{money.format(moedasUsadas)} / {money.format(bonusTarget.spent)} moedas usadas</strong>
          <div className="bonus-level-list" aria-label="Niveis de recompensa">
            {bonusLevels.map((bonus) => {
              const unlocked = moedasUsadas >= bonus.spent;
              const current = nextBonus?.title === bonus.title;
              const status = unlocked ? "Liberado" : current ? "Proxima meta" : "Bloqueado";
              return (
                <article className={unlocked ? "unlocked" : current ? "current" : ""} key={bonus.title}>
                  <span>{status}</span>
                  <div>
                    <strong>{bonus.title}</strong>
                    <small>{money.format(bonus.spent)} moedas usadas</small>
                  </div>
                  <p>
                    <b>{bonus.benefit}</b>
                    {bonus.reward}
                  </p>
                </article>
              );
            })}
          </div>
        </section>

        <section className="surface-card coupon-card">
          <SectionTitle eyebrow="Cupom recente" title={lastCoupon ? lastCoupon.codigo : "Nenhum cupom novo"} />
          <p>{lastCoupon ? lastCoupon.vantagem : "Quando voce resgatar uma vantagem, o codigo fica em destaque aqui."}</p>
          <button className="outline-button" onClick={copiarCupom} disabled={!lastCoupon}>
            <Copy size={17} />
            Copiar cupom
          </button>
        </section>
      </div>

      <section className="surface-card journey-card">
        <div className="journey-profile">
          <span><Trophy size={26} /></span>
          <div>
            <span className="eyebrow">Jornada do aluno</span>
            <h2>{nivelAluno}</h2>
            <p className="compact-note">
              {nextJourney
                ? `${money.format(moedasRecebidas)} moedas recebidas por reconhecimento dos professores. Proximo nivel: ${nextJourney.title}.`
                : `${money.format(moedasRecebidas)} moedas recebidas por reconhecimento dos professores. Maior nivel do semestre.`}
            </p>
            <div className="journey-level-list">
              {journeyLevels.map((level) => (
                <span className={moedasRecebidas >= level.min ? "active" : ""} key={level.title}>
                  <strong>{level.title}</strong>
                  <small>{level.min === 0 ? "Inicio" : `${money.format(level.min)} moedas recebidas`}</small>
                </span>
              ))}
            </div>
          </div>
        </div>
        <div className="journey-facts">
          {impactoAluno.map(([label, value]) => (
            <span key={label}>
              <strong>{label}</strong>
              {value}
            </span>
          ))}
        </div>
      </section>

      <div className="section-title-row">
        <SectionTitle eyebrow="Catalogo" title="Vantagens disponiveis" />
        <CatalogToolbar
          query={catalogQuery}
          onQuery={setCatalogQuery}
          filter={catalogFilter}
          onFilter={setCatalogFilter}
          filters={[
            ["todos", "Todas"],
            ["disponiveis", "Posso resgatar"],
            ["adquiridas", "Adquiridas"],
            ["metas", "Metas"]
          ]}
        />
      </div>
      <div className="advantage-grid">
        {filteredVantagens.length === 0 && <EmptyState text="Nenhuma vantagem encontrada com esses filtros." />}
        {filteredVantagens.map((vantagem) => {
          const acquired = Boolean(vantagem.adquirida);
          const couponPaused = acquired && !vantagem.ativa && !vantagem.cupomValidado;
          const canRedeem = saldo >= vantagem.custoMoedas && !acquired;
          const missing = Math.max(vantagem.custoMoedas - saldo, 0);
          return (
          <article className={canRedeem ? "advantage-card" : "advantage-card locked"} key={vantagem.id}>
            <img src={vantagem.fotoUrl} alt={vantagem.titulo} />
            <div>
              <div className="card-kicker">
                <span className="pill">{vantagem.empresaNome}</span>
                <span className={couponPaused ? "status-dot paused" : acquired || canRedeem ? "status-dot ready" : "status-dot waiting"}>
                  {couponPaused ? "Cupom pausado" : acquired ? "Ja adquirido" : canRedeem ? "Disponivel" : `Faltam ${money.format(missing)}`}
                </span>
              </div>
              <h3>{vantagem.titulo}</h3>
              <BenefitDescription description={vantagem.descricao} />
              {acquired && (
                <div className={vantagem.cupomValidado ? "owned-benefit validated" : couponPaused ? "owned-benefit deactivated" : "owned-benefit pending"}>
                  <TicketCheck size={17} />
                  <span>
                    {vantagem.codigoCupom} - {vantagem.cupomValidado
                      ? "validado e pronto para uso"
                      : couponPaused
                        ? "temporariamente desativado. O parceiro pausou esta vantagem; aguarde a republicacao para usar o cupom."
                      : "pendente de validacao. Aguarde a confirmacao do parceiro para usar o cupom."}
                  </span>
                </div>
              )}
              <div className="card-footer">
                <strong>{money.format(vantagem.custoMoedas)} moedas</strong>
                <button className="primary-button small" onClick={() => resgatar(vantagem.id)} disabled={!canRedeem}>
                  {acquired ? "Ja adquirido" : "Resgatar"}
                </button>
              </div>
            </div>
          </article>
        );})}
      </div>

      <TransactionsTable title="Extrato do aluno" rows={data.extrato} />
      <NotificationsPanel items={data.notificacoes || []} />
    </DashboardShell>
  );
}

function ProfessorDashboard({ session, logout, notify }) {
  const { data, loading, reload } = useDashboard("/api/professor/dashboard", notify);
  const initialAlunoId = data?.alunos?.[0]?.id || "";
  const [form, setForm] = useState({ alunoId: "", valor: "", mensagem: "" });
  const [studentQuery, setStudentQuery] = useState("");

  useEffect(() => {
    if (initialAlunoId && !form.alunoId) {
      setForm((current) => ({ ...current, alunoId: initialAlunoId }));
    }
  }, [initialAlunoId]);

  const alunosFiltrados = useMemo(() => {
    const query = studentQuery.trim().toLowerCase();
    const alunos = data?.alunos || [];
    if (!query) return alunos;
    return alunos.filter((aluno) => (
      aluno.nome.toLowerCase().includes(query)
      || aluno.email.toLowerCase().includes(query)
      || aluno.curso.toLowerCase().includes(query)
    ));
  }, [data, studentQuery]);

  const selectedAluno = (data?.alunos || []).find((aluno) => String(aluno.id) === String(form.alunoId));
  const valorEnvio = Number(form.valor) || 0;
  const saldoAposEnvio = Math.max((data?.professor?.saldoMoedas || 0) - valorEnvio, 0);
  const totalEnviado = (data?.extrato || [])
    .filter((row) => row.tipo === "ENVIO_MOEDAS")
    .reduce((total, row) => total + row.valor, 0);
  const rankingAlunos = [...(data?.alunos || [])].sort((a, b) => b.saldoMoedas - a.saldoMoedas).slice(0, 5);
  const templatesReconhecimento = [
    "Participacao ativa em sala e colaboracao com colegas.",
    "Entrega de atividade com qualidade acima do esperado.",
    "Evolucao consistente no projeto pratico do laboratorio."
  ];

  const enviar = async (event) => {
    event.preventDefault();
    try {
      await api("/api/professor/envios", {
        method: "POST",
        body: JSON.stringify({ alunoId: Number(form.alunoId), valor: Number(form.valor), mensagem: form.mensagem })
      });
      notify("success", "Moedas enviadas com sucesso.");
      setForm({ alunoId: initialAlunoId, valor: "", mensagem: "" });
      reload();
    } catch (error) {
      notify("error", error.message);
    }
  };

  const creditar = async () => {
    try {
      const response = await api("/api/professor/credito-semestral", { method: "POST" });
      notify("success", response.mensagem);
      reload();
    } catch (error) {
      notify("error", error.message);
    }
  };

  if (loading) return <LoadingScreen />;
  if (!data) return null;

  return (
    <DashboardShell session={session} active="professor" logout={logout} summary={data.resumo}>
      <div className="dashboard-grid">
        <MetricCard icon={<Coins />} label="Saldo disponivel" value={`${money.format(data.professor.saldoMoedas)} moedas`} tone="green" />
        <MetricCard icon={<HandCoins />} label="Alunos cadastrados" value={data.alunos.length} tone="amber" />
        <MetricCard icon={<Send />} label="Moedas enviadas" value={`${money.format(totalEnviado)} moedas`} tone="blue" />
      </div>

      <section className="surface-card professor-identity-card">
        <SectionTitle eyebrow="Vinculo institucional" title={data.professor.instituicao} />
        <p>Professor pre-cadastrado pela instituicao parceira para operar reconhecimentos academicos.</p>
        <div className="mini-facts light">
          <span>Nome: {data.professor.nome}</span>
          <span>CPF cadastrado: {data.professor.cpf}</span>
          <span>Departamento: {data.professor.departamento}</span>
          <span>Instituicao: {data.professor.instituicao}</span>
        </div>
      </section>

      <div className="work-grid">
        <section className="surface-card">
          <SectionTitle eyebrow="Reconhecimento" title="Enviar moedas" />
          <form className="stack-form" onSubmit={enviar}>
            <label>Buscar aluno
              <div className="input-with-icon">
                <Search size={17} />
                <input value={studentQuery} onChange={(e) => setStudentQuery(e.target.value)} placeholder="Nome, email ou curso" />
              </div>
            </label>
            <label>Aluno
              <select value={form.alunoId} onChange={(e) => setForm({ ...form, alunoId: e.target.value })} required>
                {alunosFiltrados.map((aluno) => (
                  <option key={aluno.id} value={aluno.id}>{aluno.nome} - {aluno.curso}</option>
                ))}
              </select>
            </label>
            <label>Quantidade
              <input type="number" min="1" value={form.valor} onChange={(e) => setForm({ ...form, valor: e.target.value })} required />
            </label>
            <div className="quick-actions">
              {[25, 50, 100, 150].map((valor) => (
                <button key={valor} type="button" className="chip-button" onClick={() => setForm({ ...form, valor: String(valor) })}>
                  {valor} moedas
                </button>
              ))}
            </div>
            <label>Justificativa obrigatoria
              <textarea rows="4" value={form.mensagem} onChange={(e) => setForm({ ...form, mensagem: e.target.value })} required />
            </label>
            <div className="template-grid">
              {templatesReconhecimento.map((template) => (
                <button key={template} type="button" onClick={() => setForm({ ...form, mensagem: template })}>
                  {template}
                </button>
              ))}
            </div>
            <button className="primary-button full" type="submit">Enviar moedas</button>
          </form>
        </section>

        <section className="surface-card send-plan-card">
          <img src={MENTORING_IMAGE} alt="Professor orientando estudantes em uma mesa" />
          <div className="send-plan-content">
            <span className="eyebrow">Previa do reconhecimento</span>
            <h2>{selectedAluno ? selectedAluno.nome : "Selecione um aluno"}</h2>
            <p>
              {selectedAluno && valorEnvio > 0
                ? `${money.format(valorEnvio)} moedas serao registradas para ${selectedAluno.nome}. Seu saldo ficara em ${money.format(saldoAposEnvio)} moedas.`
                : selectedAluno
                  ? `Defina a quantidade e a justificativa para reconhecer ${selectedAluno.nome}.`
                  : "Escolha um aluno para visualizar o impacto antes de enviar."}
            </p>
            <div className="send-plan-breakdown">
              <span>
                <strong>{money.format(valorEnvio)}</strong>
                moedas neste envio
              </span>
              <span>
                <strong>{money.format(saldoAposEnvio)}</strong>
                saldo apos confirmar
              </span>
            </div>
            <div className="mini-facts light">
              <span>Semestre: {data.professor.ultimoCreditoSemestral || "Pendente"}</span>
              <span>Cota semestral: 1.000 moedas</span>
            </div>
            <button className="outline-button" onClick={creditar}>Creditar semestre atual</button>
          </div>
        </section>
      </div>

      <section className="surface-card roster-card">
        <SectionTitle eyebrow="Acompanhamento" title="Alunos e saldos visiveis" />
        <div className="student-list">
          {rankingAlunos.map((aluno, index) => (
            <article className="student-row" key={aluno.id}>
              <span>{index + 1}</span>
              <div>
                <strong>{aluno.nome}</strong>
                <small>{aluno.curso} - {aluno.email}</small>
              </div>
              <b>{money.format(aluno.saldoMoedas)} moedas</b>
            </article>
          ))}
        </div>
      </section>

      <TransactionsTable title="Historico do professor" rows={data.extrato} />
      <NotificationsPanel items={data.notificacoes || []} />
    </DashboardShell>
  );
}

function EmpresaDashboard({ session, logout, notify }) {
  const { data, loading, reload, setData } = useDashboard("/api/empresa/dashboard", notify);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(emptyVantagem());
  const [catalogQuery, setCatalogQuery] = useState("");
  const [statusFilter, setStatusFilter] = useState("todas");
  const [couponCode, setCouponCode] = useState("");
  const [couponResult, setCouponResult] = useState(null);
  const [statusChangingId, setStatusChangingId] = useState(null);
  const [deletingId, setDeletingId] = useState(null);

  const vantagens = data?.vantagens || [];
  const resgates = data?.resgates || [];
  const cuponsPendentes = resgates.filter((resgate) => resgate.codigoCupom && !resgate.cupomValidado).length;
  const cuponsValidados = resgates.filter((resgate) => resgate.codigoCupom && resgate.cupomValidado).length;
  const couponRows = [...resgates]
    .filter((resgate) => resgate.codigoCupom)
    .sort((a, b) => Number(a.cupomValidado) - Number(b.cupomValidado)
      || new Date(b.criadaEm).getTime() - new Date(a.criadaEm).getTime())
    .slice(0, 4);
  const normalizedCouponCode = couponCode.trim().toUpperCase();
  const activeCount = vantagens.filter((vantagem) => vantagem.ativa).length;
  const inactiveCount = vantagens.length - activeCount;
  const averageCost = vantagens.length
    ? Math.round(vantagens.reduce((total, vantagem) => total + vantagem.custoMoedas, 0) / vantagens.length)
    : 0;
  const filteredVantagens = useMemo(() => {
    const query = catalogQuery.trim().toLowerCase();
    return vantagens.filter((vantagem) => {
      const matchesQuery = !query
        || vantagem.titulo.toLowerCase().includes(query)
        || vantagem.descricao.toLowerCase().includes(query);
      const matchesStatus = statusFilter === "todas"
        || (statusFilter === "ativas" && vantagem.ativa)
        || (statusFilter === "inativas" && !vantagem.ativa);
      return matchesQuery && matchesStatus;
    });
  }, [catalogQuery, statusFilter, vantagens]);

  const edit = (vantagem) => {
    setEditingId(vantagem.id);
    setForm({
      titulo: vantagem.titulo,
      descricao: vantagem.descricao,
      fotoUrl: vantagem.fotoUrl,
      custoMoedas: String(vantagem.custoMoedas),
      ativa: vantagem.ativa
    });
  };

  const submit = async (event) => {
    event.preventDefault();
    const payload = { ...form, custoMoedas: Number(form.custoMoedas) };
    try {
      if (editingId) {
        await api(`/api/empresa/vantagens/${editingId}`, { method: "PUT", body: JSON.stringify(payload) });
        notify("success", "Vantagem atualizada.");
      } else {
        await api("/api/empresa/vantagens", { method: "POST", body: JSON.stringify(payload) });
        notify("success", "Vantagem cadastrada.");
      }
      setEditingId(null);
      setForm(emptyVantagem());
      reload();
    } catch (error) {
      notify("error", error.message);
    }
  };

  const excluir = async (vantagem) => {
    if (vantagem.excluivel === false) {
      notify("error", "Esta vantagem ja gerou cupom. Pause para ocultar do catalogo sem apagar o historico.");
      return;
    }
    setDeletingId(vantagem.id);
    try {
      await api(`/api/empresa/vantagens/${vantagem.id}/excluir`, { method: "DELETE" });
      setData((current) => current
        ? { ...current, vantagens: current.vantagens.filter((item) => item.id !== vantagem.id) }
        : current);
      if (editingId === vantagem.id) {
        setEditingId(null);
        setForm(emptyVantagem());
      }
      notify("success", "Vantagem excluida do cadastro.");
    } catch (error) {
      notify("error", error.message);
    } finally {
      setDeletingId(null);
    }
  };

  const alterarStatus = async (vantagem, ativa) => {
    setStatusChangingId(vantagem.id);
    try {
      let atualizada;
      try {
        atualizada = await api(`/api/empresa/vantagens/${vantagem.id}/status`, {
          method: "PUT",
          body: JSON.stringify({ ativa })
        });
      } catch (error) {
        if (error.status !== 404) {
          throw error;
        }
        await api(`/api/empresa/vantagens/${vantagem.id}`, {
          method: "PUT",
          body: JSON.stringify({
            titulo: vantagem.titulo,
            descricao: vantagem.descricao,
            fotoUrl: vantagem.fotoUrl,
            custoMoedas: vantagem.custoMoedas,
            ativa
          })
        });
        atualizada = { ...vantagem, ativa };
      }
      setData((current) => current
        ? {
          ...current,
          vantagens: current.vantagens.map((item) => item.id === vantagem.id ? { ...item, ...atualizada } : item)
        }
        : current);
      if (editingId === vantagem.id) {
        setForm((current) => ({ ...current, ativa }));
      }
      notify("success", ativa
        ? "Vantagem publicada no catalogo dos alunos."
        : "Vantagem pausada. Ela saiu do catalogo dos alunos.");
    } catch (error) {
      notify("error", error.message);
    } finally {
      setStatusChangingId(null);
    }
  };

  const validarCupom = async (event) => {
    event.preventDefault();
    const codigo = normalizedCouponCode;
    if (!codigo) {
      setCouponResult({ type: "error", text: "Informe o codigo apresentado pelo aluno." });
      return;
    }
    try {
      const validado = await api("/api/empresa/cupons/validar", {
        method: "POST",
        body: JSON.stringify({ codigo })
      });
      setCouponResult({
        type: "success",
        text: `Atendimento confirmado: ${validado.codigoCupom} para ${validado.contraparte} em ${validado.vantagem}.`
      });
      setCouponCode("");
      notify("success", "Cupom validado com sucesso.");
      reload();
    } catch (error) {
      setCouponResult({ type: "error", text: error.message });
      notify("error", error.message);
    }
  };

  if (loading) return <LoadingScreen />;
  if (!data) return null;

  return (
    <DashboardShell session={session} active="empresa" logout={logout} summary={data.resumo}>
      <div className="dashboard-grid">
        <MetricCard icon={<Building2 />} label="Empresa" value={data.empresa.nome} tone="green" />
        <MetricCard icon={<Store />} label="Vantagens ativas" value={`${activeCount}/${data.vantagens.length}`} tone="amber" />
        <MetricCard icon={<TicketCheck />} label="Cupons pendentes" value={cuponsPendentes} tone="blue" />
      </div>

      <div className="work-grid">
        <section className="surface-card">
          <SectionTitle eyebrow={editingId ? "Edicao" : "Nova vantagem"} title={editingId ? "Editar vantagem" : "Cadastrar vantagem"} />
          <form className="grid-form" onSubmit={submit}>
            <Field label="Titulo" value={form.titulo} onChange={(titulo) => setForm({ ...form, titulo })} />
            <Field label="Custo em moedas" type="number" value={form.custoMoedas} onChange={(custoMoedas) => setForm({ ...form, custoMoedas })} />
            <label className="wide">Foto URL
              <input type="url" value={form.fotoUrl} onChange={(e) => setForm({ ...form, fotoUrl: e.target.value })} required />
            </label>
            <label className="wide">Descricao pratica
              <textarea
                rows="5"
                value={form.descricao}
                onChange={(e) => setForm({ ...form, descricao: e.target.value })}
                placeholder={benefitDescriptionExample}
                required
              />
            </label>
            <small className="field-hint wide">
              Escreva uma descricao direta do que o aluno recebe e mantenha o trecho "Como usar" para orientar o uso do cupom.
            </small>
            <label className="checkbox wide">
              <input type="checkbox" checked={form.ativa} onChange={(e) => setForm({ ...form, ativa: e.target.checked })} />
              Vantagem ativa
            </label>
            <div className="form-actions wide">
              <button className="primary-button" type="submit">{editingId ? "Salvar alteracoes" : "Cadastrar vantagem"}</button>
              {editingId && <button className="ghost-button" type="button" onClick={() => { setEditingId(null); setForm(emptyVantagem()); }}>Cancelar</button>}
            </div>
          </form>
        </section>

        <section className="surface-card preview-card">
          <SectionTitle eyebrow="Previa do catalogo" title="Vantagem em montagem" />
          <article className="advantage-card compact">
            <img src={form.fotoUrl || PARTNER_IMAGE} alt="Preview da vantagem" />
            <div>
              <span className={form.ativa ? "pill" : "pill muted"}>{form.ativa ? "Ativa" : "Inativa"}</span>
              <h3>{form.titulo || "Titulo da vantagem"}</h3>
              {form.descricao
                ? <BenefitDescription description={form.descricao} />
                : <p>Preencha a descricao para mostrar ao aluno o que ele recebe e como retirar o beneficio.</p>}
              <strong>{form.custoMoedas ? `${money.format(Number(form.custoMoedas))} moedas` : "Defina o custo"}</strong>
            </div>
          </article>
          <div className="mini-facts light">
            <span>{inactiveCount} inativas</span>
            <span>Custo medio: {money.format(averageCost)} moedas</span>
          </div>
        </section>
      </div>

      <div className="company-ops-grid">
        <section className="surface-card coupon-checker">
          <div className="coupon-checker-head">
            <span><ClipboardCheck size={21} /></span>
            <SectionTitle eyebrow="Atendimento" title="Validar cupom do aluno" />
          </div>
          <form className="coupon-form" onSubmit={validarCupom}>
            <label>Codigo do cupom
              <div className="input-with-icon">
                <TicketCheck size={17} />
                <input
                  value={couponCode}
                  onChange={(e) => {
                    setCouponCode(e.target.value.toUpperCase());
                    setCouponResult(null);
                  }}
                  placeholder="Ex.: SME-DEMO2026"
                  required
                />
              </div>
            </label>
            <button className="primary-button" type="submit" disabled={!normalizedCouponCode}>
              <ClipboardCheck size={17} />
              Confirmar
            </button>
          </form>
          <div className="coupon-status-grid">
            <article>
              <strong>{cuponsPendentes}</strong>
              <span>Aguardando validacao</span>
            </article>
            <article>
              <strong>{cuponsValidados}</strong>
              <span>Ja confirmados</span>
            </article>
          </div>
          <div className="coupon-queue">
            <div className="coupon-queue-title">
              <strong>Cupons recentes</strong>
              <span>{couponRows.length} na lista</span>
            </div>
            {couponRows.length === 0 ? (
              <EmptyState text="Nenhum cupom recebido ainda." compact />
            ) : (
              couponRows.map((resgate) => {
                const couponPaused = !resgate.cupomValidado && resgate.vantagemAtiva === false;
                return (
                  <button
                    key={resgate.id}
                    type="button"
                    className="coupon-queue-item"
                    onClick={() => {
                      setCouponCode(resgate.codigoCupom);
                      setCouponResult(null);
                    }}
                  >
                    <span>
                      <strong>{resgate.codigoCupom}</strong>
                      <small>{resgate.contraparte} - {resgate.vantagem}</small>
                    </span>
                    <b className={resgate.cupomValidado ? "status-dot ready" : couponPaused ? "status-dot paused" : "status-dot waiting"}>
                      {resgate.cupomValidado ? "Validado" : couponPaused ? "Desativado" : "Pendente"}
                    </b>
                  </button>
                );
              })
            )}
          </div>
          {couponResult && <p className={`validation-result ${couponResult.type}`}>{couponResult.text}</p>}
        </section>
        <NotificationsPanel items={data.notificacoes || []} />
      </div>

      <div className="section-title-row">
        <SectionTitle eyebrow="Catalogo parceiro" title="Vantagens cadastradas" />
        <CatalogToolbar
          query={catalogQuery}
          onQuery={setCatalogQuery}
          filter={statusFilter}
          onFilter={setStatusFilter}
          filters={[
            ["todas", "Todas"],
            ["ativas", "Ativas"],
            ["inativas", "Inativas"]
          ]}
        />
      </div>
      <div className="partner-catalog-summary">
        <span><strong>{activeCount}</strong> publicadas para alunos</span>
        <span><strong>{inactiveCount}</strong> pausadas</span>
        <p>Pausar oculta a vantagem para novos resgates e marca cupons pendentes como temporariamente desativados. Ao publicar, eles voltam a poder ser validados.</p>
      </div>
      <div className="advantage-grid">
        {filteredVantagens.length === 0 && <EmptyState text="Nenhuma vantagem encontrada com esses filtros." />}
        {filteredVantagens.map((vantagem) => {
          const statusBusy = statusChangingId === vantagem.id;
          const deleteBusy = deletingId === vantagem.id;
          const busy = statusBusy || deleteBusy;
          return (
            <article className={vantagem.ativa ? "advantage-card partner-advantage-card" : "advantage-card partner-advantage-card inactive"} key={vantagem.id}>
              <img src={vantagem.fotoUrl} alt={vantagem.titulo} />
              <div>
                <div className={vantagem.ativa ? "partner-card-status active" : "partner-card-status paused"}>
                  <span>{vantagem.ativa ? "Publicada" : "Pausada"}</span>
                  <small>{vantagem.ativa ? "Visivel no catalogo dos alunos" : "Oculta para novos resgates"}</small>
                </div>
                <h3>{vantagem.titulo}</h3>
                <BenefitDescription description={vantagem.descricao} />
                <div className="card-footer partner-card-footer">
                  <strong>{money.format(vantagem.custoMoedas)} moedas</strong>
                  <div className="inline-actions">
                    <button type="button" className="ghost-button small" onClick={() => edit(vantagem)} disabled={busy}>Editar</button>
                    <button
                      type="button"
                      className="danger-button small"
                      onClick={() => excluir(vantagem)}
                      disabled={busy}
                    >
                      {deleteBusy ? "Excluindo..." : "Excluir"}
                    </button>
                    <button
                      type="button"
                      className={vantagem.ativa ? "status-toggle-button pause" : "status-toggle-button publish"}
                      onClick={() => alterarStatus(vantagem, !vantagem.ativa)}
                      disabled={busy}
                      aria-pressed={vantagem.ativa}
                    >
                      {vantagem.ativa ? <EyeOff size={16} /> : <Power size={16} />}
                      {statusBusy ? "Atualizando..." : vantagem.ativa ? "Pausar" : "Publicar"}
                    </button>
                  </div>
                  {vantagem.excluivel === false && (
                    <small className="delete-note">Ja tem cupom gerado: pause para ocultar sem perder o historico.</small>
                  )}
                </div>
              </div>
            </article>
          );
        })}
      </div>

      <TransactionsTable title="Cupons e resgates recebidos" rows={resgates} />
    </DashboardShell>
  );
}

function DashboardShell({ session, logout, summary, children }) {
  const profileLabel = {
    ALUNO: "Aluno",
    PROFESSOR: "Professor",
    EMPRESA: "Empresa parceira"
  }[session.perfil] || "Usuario";

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <button className="brand" onClick={() => navigateTo("/")}>
          <span><Coins size={21} /></span>
          {APP_NAME}
        </button>
        <div className="sidebar-profile">
          <span>{profileLabel}</span>
          <strong>{session.nome}</strong>
          <small>{session.email}</small>
        </div>
        <nav>
          <SidebarButton icon={<LayoutDashboard />} label="Meu painel" active path={dashboardFor(session.perfil)} />
          <SidebarButton icon={<House />} label="Pagina inicial" active={false} path="/" />
        </nav>
      </aside>
      <main className="dashboard-main">
        <header className="dashboard-header">
          <div>
            <span className="eyebrow">Painel {session.perfil.toLowerCase()}</span>
            <h1>Ola, {session.nome}</h1>
            <p>{summary}</p>
          </div>
          <button className="outline-button" onClick={logout}>
            <LogOut size={17} />
            Sair
          </button>
        </header>
        {children}
      </main>
    </div>
  );
}

function SidebarButton({ icon, label, active, path }) {
  return (
    <button className={active ? "sidebar-link active" : "sidebar-link"} onClick={() => navigateTo(path)}>
      {React.cloneElement(icon, { size: 18 })}
      {label}
    </button>
  );
}

function MetricCard({ icon, label, value, tone }) {
  return (
    <article className={`metric-card ${tone}`}>
      <span>{React.cloneElement(icon, { size: 23 })}</span>
      <p>{label}</p>
      <strong>{value}</strong>
    </article>
  );
}

function CatalogToolbar({ query, onQuery, filter, onFilter, filters }) {
  return (
    <div className="catalog-toolbar">
      <label className="search-field">
        <Search size={17} />
        <input value={query} onChange={(event) => onQuery(event.target.value)} placeholder="Buscar" />
      </label>
      <div className="segmented-controls" aria-label="Filtros do catalogo">
        <Filter size={16} />
        {filters.map(([value, label]) => (
          <button key={value} className={filter === value ? "active" : ""} onClick={() => onFilter(value)}>
            {label}
          </button>
        ))}
      </div>
    </div>
  );
}

function NotificationsPanel({ items }) {
  const [period, setPeriod] = useState("todos");
  const visibleItems = items.filter((item) => isWithinPeriod(item.criadaEm, period)).slice(0, 4);
  return (
    <section className="surface-card notifications-card">
      <div className="section-title-row compact">
        <SectionTitle eyebrow="Notificacoes" title="Emails registrados" />
        <PeriodFilter value={period} onChange={setPeriod} />
      </div>
      {visibleItems.length === 0 ? (
        <EmptyState text="Nenhuma notificacao registrada para este perfil ainda." compact />
      ) : (
        <div className="notification-list">
          {visibleItems.map((item) => (
            <article className="notification-item" key={item.id}>
              <span><Bell size={17} /></span>
              <div>
                <strong>{item.assunto}</strong>
                <p>{item.conteudo}</p>
                <small>{formatDate(item.criadaEm)}{item.codigoReferencia ? ` - ${item.codigoReferencia}` : ""}</small>
              </div>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}

function EmptyState({ text, compact = false }) {
  return (
    <div className={compact ? "empty-state compact" : "empty-state"}>
      <UsersRound size={compact ? 20 : 26} />
      <p>{text}</p>
    </div>
  );
}

function PeriodFilter({ value, onChange }) {
  return (
    <label className="period-select">Periodo
      <span>
        <CalendarDays size={16} />
        <select value={value} onChange={(event) => onChange(event.target.value)} aria-label="Periodo">
          {periodFilters.map(([period, label]) => (
            <option key={period} value={period}>{label}</option>
          ))}
        </select>
      </span>
    </label>
  );
}

function TransactionsTable({ title, rows }) {
  const [period, setPeriod] = useState("todos");
  const filteredRows = rows.filter((row) => isWithinPeriod(row.criadaEm, period));
  return (
    <section className="surface-card table-card">
      <div className="section-title-row compact">
        <SectionTitle eyebrow="Extrato" title={title} />
        <PeriodFilter value={period} onChange={setPeriod} />
      </div>
      <div className="table-scroll">
        <table>
          <thead>
            <tr>
              <th>Data</th>
              <th>Tipo</th>
              <th>Valor</th>
              <th>Relacionado</th>
              <th>Descricao</th>
              <th>Cupom</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            {filteredRows.length === 0 && (
              <tr><td colSpan="7">Nenhuma transacao registrada.</td></tr>
            )}
            {filteredRows.map((row) => (
              <tr key={row.id}>
                <td>{formatDate(row.criadaEm)}</td>
                <td>{labelTipo(row.tipo)}</td>
                <td>{money.format(row.valor)}</td>
                <td>{row.contraparte}</td>
                <td>{row.mensagem}</td>
                <td>{row.codigoCupom || couponColumnText(row)}</td>
                <td>{row.codigoCupom ? <CouponStatus row={row} /> : <TransactionStatus row={row} />}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}

function couponColumnText(row) {
  if (row.tipo === "RESGATE_VANTAGEM") return "Gerado no resgate";
  if (row.tipo === "ENVIO_MOEDAS") return "Nao se aplica";
  if (row.tipo === "CREDITO_SEMESTRAL") return "Cota semestral";
  return "Nao se aplica";
}

function TransactionStatus({ row }) {
  const status = {
    CREDITO_SEMESTRAL: ["status-dot ready", "Cota creditada"],
    ENVIO_MOEDAS: ["status-dot ready", "Credito recebido"],
    RESGATE_VANTAGEM: ["status-dot waiting", "Aguardando cupom"]
  }[row.tipo] || ["status-dot waiting", "Registrado"];

  return <span className={status[0]}>{status[1]}</span>;
}

function CouponStatus({ row }) {
  if (!row.cupomValidado && row.vantagemAtiva === false) {
    return <span className="status-dot paused">Cupom desativado pelo parceiro</span>;
  }

  return (
    <span className={row.cupomValidado ? "status-dot ready" : "status-dot waiting"}>
      {row.cupomValidado ? `Validado${row.validadoEm ? ` em ${formatDate(row.validadoEm)}` : ""}` : "Pendente"}
    </span>
  );
}

function BenefitDescription({ description }) {
  const details = parseBenefitDescription(description);
  if (details.length === 0) {
    return <p>{description}</p>;
  }

  return (
    <div className="benefit-details">
      {details.map((detail) => (
        <div className={detail.type === "description" ? "benefit-description-main" : ""} key={`${detail.label}-${detail.text}`}>
          {detail.type !== "description" && <span><CheckCircle2 size={14} /></span>}
          <p>
            {detail.label && <strong>{detail.label}</strong>}
            {detail.text}
          </p>
        </div>
      ))}
    </div>
  );
}

function SectionTitle({ eyebrow, title }) {
  return (
    <div className="section-title">
      <span className="eyebrow">{eyebrow}</span>
      <h2>{title}</h2>
    </div>
  );
}

function Field({ label, value, onChange, type = "text" }) {
  return (
    <label>{label}
      <input type={type} value={value} onChange={(event) => onChange(event.target.value)} required />
    </label>
  );
}

function useDashboard(path, notify) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  const load = async () => {
    setLoading(true);
    try {
      setData(await api(path));
    } catch (error) {
      notify("error", error.message);
      navigateTo("/login");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, [path]);

  return { data, loading, reload: load, setData };
}

function Toast({ type, children }) {
  return <div className={`toast ${type}`}><CheckCircle2 size={18} />{children}</div>;
}

function LoadingScreen() {
  return (
    <main className="loading-screen">
      <div className="loader" />
      <strong>{APP_NAME}</strong>
      <span>Carregando...</span>
    </main>
  );
}

function AuthRequired({ title, message }) {
  return (
    <main className="auth-required">
      <div className="surface-card">
        <UserRound size={30} />
        <h1>{title}</h1>
        <p>{message}</p>
        <button className="primary-button" onClick={() => navigateTo("/login")}>Entrar</button>
      </div>
    </main>
  );
}

function dashboardFor(perfil) {
  if (perfil === "PROFESSOR") return "/professor";
  if (perfil === "EMPRESA") return "/empresa";
  return "/aluno";
}

function emptyVantagem() {
  return {
    titulo: "",
    descricao: "",
    fotoUrl: "https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?auto=format&fit=crop&w=1200&q=85",
    custoMoedas: "",
    ativa: true
  };
}

function formatDate(value) {
  try {
    return dateTime.format(new Date(value));
  } catch {
    return value;
  }
}

function isWithinPeriod(value, period) {
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

function labelTipo(tipo) {
  const labels = {
    CREDITO_SEMESTRAL: "Credito semestral",
    ENVIO_MOEDAS: "Envio de moedas",
    RESGATE_VANTAGEM: "Resgate"
  };
  return labels[tipo] || tipo;
}

createRoot(document.getElementById("root")).render(<App />);
