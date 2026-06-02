import { useEffect, useState } from "react";
import {
  ArrowRight,
  KeyRound,
  LoaderCircle,
  MapPin,
  MailCheck,
  ShieldCheck,
  Store,
  UserRound
} from "lucide-react";
import { APP_NAME, HERO_IMAGE } from "../config/app";
import { BrandLogo } from "../components/Logo";
import { Field } from "../components/ui";
import { api } from "../services/api";
import { dashboardFor, navigateTo } from "../utils/navigation";

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
  const senhaInicial = "ValorizaAe#2026!";
  const [form, setForm] = useState({ email: "aluno@moedas.com", senha: senhaInicial });
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
        <button className="auth-text-button" type="button" onClick={() => navigateTo("/esqueci-senha")}>
          Esqueci minha senha
        </button>
      </form>
      <div className="quick-logins">
        <strong>Entradas rapidas</strong>
        <button onClick={() => setForm({ email: "aluno@moedas.com", senha: senhaInicial })}>Aluno</button>
        <button onClick={() => setForm({ email: "professor@moedas.com", senha: senhaInicial })}>Professor</button>
        <button onClick={() => setForm({ email: "empresa@moedas.com", senha: senhaInicial })}>Empresa</button>
      </div>
    </AuthLayout>
  );
}

function EsqueciSenha({ notify }) {
  const [email, setEmail] = useState("");
  const [loading, setLoading] = useState(false);
  const [sent, setSent] = useState(false);

  const submit = async (event) => {
    event.preventDefault();
    setLoading(true);
    try {
      const response = await api("/api/senha/esqueci", {
        method: "POST",
        body: JSON.stringify({ email })
      });
      setSent(true);
      notify("success", response.mensagem);
    } catch (error) {
      notify("error", error.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthLayout title="Recuperar senha" subtitle="Informe o e-mail da sua conta para receber um link seguro de redefinicao.">
      {sent ? (
        <div className="auth-success-state">
          <span><MailCheck size={24} /></span>
          <strong>Verifique seu e-mail</strong>
          <p>Enviamos o link para {email}. Ele fica valido por 30 minutos.</p>
          <button className="outline-button full" onClick={() => navigateTo("/login")}>Voltar ao login</button>
        </div>
      ) : (
        <form className="stack-form" onSubmit={submit}>
          <label>Email
            <input type="email" value={email} onChange={(event) => setEmail(event.target.value)} required />
          </label>
          <button className="primary-button full" disabled={loading} type="submit">
            {loading ? "Enviando..." : "Enviar link de recuperacao"}
          </button>
          <button className="auth-text-button" type="button" onClick={() => navigateTo("/login")}>
            Voltar ao login
          </button>
        </form>
      )}
    </AuthLayout>
  );
}

function RedefinirSenha({ notify }) {
  const token = new URLSearchParams(window.location.search).get("token") || "";
  const [form, setForm] = useState({ novaSenha: "", confirmarSenha: "" });
  const [loading, setLoading] = useState(false);

  const submit = async (event) => {
    event.preventDefault();
    if (form.novaSenha !== form.confirmarSenha) {
      notify("error", "As senhas informadas precisam ser iguais.");
      return;
    }
    setLoading(true);
    try {
      const response = await api("/api/senha/redefinir", {
        method: "POST",
        body: JSON.stringify({ token, novaSenha: form.novaSenha })
      });
      notify("success", response.mensagem);
      navigateTo("/login");
    } catch (error) {
      notify("error", error.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthLayout title="Criar nova senha" subtitle="Defina uma senha nova para voltar a acessar seu painel.">
      {!token ? (
        <div className="auth-success-state warning">
          <span><KeyRound size={24} /></span>
          <strong>Link invalido</strong>
          <p>Solicite um novo link de recuperacao para trocar sua senha.</p>
          <button className="outline-button full" onClick={() => navigateTo("/esqueci-senha")}>Solicitar novo link</button>
        </div>
      ) : (
        <form className="stack-form" onSubmit={submit}>
          <label>Nova senha
            <input
              type="password"
              minLength="6"
              value={form.novaSenha}
              onChange={(event) => setForm({ ...form, novaSenha: event.target.value })}
              required
            />
          </label>
          <label>Confirmar nova senha
            <input
              type="password"
              minLength="6"
              value={form.confirmarSenha}
              onChange={(event) => setForm({ ...form, confirmarSenha: event.target.value })}
              required
            />
          </label>
          <button className="primary-button full" disabled={loading} type="submit">
            {loading ? "Salvando..." : "Atualizar senha"}
          </button>
        </form>
      )}
    </AuthLayout>
  );
}

function CadastroAluno({ notify }) {
  const [instituicoes, setInstituicoes] = useState([]);
  const [cepLoading, setCepLoading] = useState(false);
  const [form, setForm] = useState({
    nome: "",
    email: "",
    telefoneWhatsapp: "",
    senha: "",
    cpf: "",
    rg: "",
    cep: "",
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
      const { cep, ...payload } = form;
      await api("/api/alunos", { method: "POST", body: JSON.stringify({ ...payload, instituicaoId: Number(form.instituicaoId) }) });
      notify("success", "Aluno cadastrado com sucesso.");
      navigateTo("/login");
    } catch (error) {
      notify("error", error.message);
    }
  };

  const consultarCep = async () => {
    const cep = form.cep.replace(/\D/g, "");
    if (cep.length !== 8) {
      notify("error", "Informe um CEP com 8 digitos para buscar o endereco.");
      return;
    }
    setCepLoading(true);
    try {
      const endereco = await api(`/api/cep/${cep}`);
      setForm((current) => ({
        ...current,
        cep: endereco.cep,
        endereco: endereco.enderecoFormatado || current.endereco
      }));
      notify("success", "Endereco preenchido pelo ViaCEP.");
    } catch (error) {
      notify("error", error.message);
    } finally {
      setCepLoading(false);
    }
  };

  return (
    <AuthLayout title="Cadastro de aluno" subtitle="Informe seus dados e selecione a instituicao pre-cadastrada em que voce estuda.">
      <form className="grid-form" onSubmit={submit}>
        <Field label="Nome" value={form.nome} onChange={(nome) => setForm({ ...form, nome })} />
        <Field label="Email" type="email" value={form.email} onChange={(email) => setForm({ ...form, email })} />
        <Field label="WhatsApp (opcional)" value={form.telefoneWhatsapp} onChange={(telefoneWhatsapp) => setForm({ ...form, telefoneWhatsapp })} />
        <Field label="Senha" type="password" value={form.senha} onChange={(senha) => setForm({ ...form, senha })} />
        <Field label="CPF" value={form.cpf} onChange={(cpf) => setForm({ ...form, cpf })} />
        <Field label="RG" value={form.rg} onChange={(rg) => setForm({ ...form, rg })} />
        <label>CEP
          <div className="input-action-row">
            <input
              value={form.cep}
              onBlur={() => form.cep.replace(/\D/g, "").length === 8 && consultarCep()}
              onChange={(e) => setForm({ ...form, cep: e.target.value })}
              placeholder="Ex.: 30140071"
            />
            <button className="ghost-button small" type="button" onClick={consultarCep} disabled={cepLoading}>
              {cepLoading ? <LoaderCircle size={16} className="spin-icon" /> : <MapPin size={16} />}
              Buscar
            </button>
          </div>
        </label>
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
  const [form, setForm] = useState({ nome: "", email: "", telefoneWhatsapp: "", senha: "", cnpj: "", cep: "", endereco: "", contato: "" });
  const [cepLoading, setCepLoading] = useState(false);

  const submit = async (event) => {
    event.preventDefault();
    try {
      const { cep, ...payload } = form;
      await api("/api/empresas", { method: "POST", body: JSON.stringify(payload) });
      notify("success", "Empresa cadastrada com sucesso.");
      navigateTo("/login");
    } catch (error) {
      notify("error", error.message);
    }
  };

  const consultarCep = async () => {
    const cep = form.cep.replace(/\D/g, "");
    if (cep.length !== 8) {
      notify("error", "Informe um CEP com 8 digitos para buscar o endereco.");
      return;
    }
    setCepLoading(true);
    try {
      const endereco = await api(`/api/cep/${cep}`);
      setForm((current) => ({
        ...current,
        cep: endereco.cep,
        endereco: endereco.enderecoFormatado || current.endereco
      }));
      notify("success", "Endereco preenchido pelo ViaCEP.");
    } catch (error) {
      notify("error", error.message);
    } finally {
      setCepLoading(false);
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
        <Field label="WhatsApp para avisos (opcional)" value={form.telefoneWhatsapp} onChange={(telefoneWhatsapp) => setForm({ ...form, telefoneWhatsapp })} />
        <label>CEP
          <div className="input-action-row">
            <input
              value={form.cep}
              onBlur={() => form.cep.replace(/\D/g, "").length === 8 && consultarCep()}
              onChange={(e) => setForm({ ...form, cep: e.target.value })}
              placeholder="Ex.: 30140071"
            />
            <button className="ghost-button small" type="button" onClick={consultarCep} disabled={cepLoading}>
              {cepLoading ? <LoaderCircle size={16} className="spin-icon" /> : <MapPin size={16} />}
              Buscar
            </button>
          </div>
        </label>
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
      <BrandLogo className="auth-brand" onClick={() => navigateTo("/")} />
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

export { CadastroAluno, CadastroEmpresa, CadastroEscolha, EsqueciSenha, Login, RedefinirSenha };
