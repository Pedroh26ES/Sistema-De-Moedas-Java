import { useEffect, useMemo, useState } from "react";
import {
  Bell,
  Coins,
  HandCoins,
  ReceiptText,
  Search,
  Send,
  UsersRound
} from "lucide-react";
import {
  DashboardShell,
  LoadingScreen,
  MetricCard,
  NotificationsPanel,
  SectionTitle,
  TransactionsTable
} from "../components/ui";
import { MENTORING_IMAGE } from "../config/app";
import { useDashboard } from "../hooks/useDashboard";
import { api } from "../services/api";
import { formatCpf, money } from "../utils/formatters";

function ProfessorDashboard({ session, logout, notify }) {
  const { data, loading, reload } = useDashboard("/api/professor/dashboard", notify);
  const initialAlunoId = data?.alunos?.[0]?.id || "";
  const [form, setForm] = useState({ alunoId: "", valor: "", mensagem: "" });
  const [studentQuery, setStudentQuery] = useState("");
  const [activeTab, setActiveTab] = useState("inicio");
  const tabs = [
    { value: "inicio", label: "Inicio", icon: <Coins /> },
    { value: "enviar", label: "Enviar moedas", icon: <Send /> },
    { value: "alunos", label: "Alunos", icon: <UsersRound /> },
    { value: "extrato", label: "Extrato", icon: <ReceiptText /> },
    { value: "notificacoes", label: "Notificacoes", icon: <Bell /> }
  ];

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
    <DashboardShell
      session={session}
      active="professor"
      logout={logout}
      summary={data.resumo}
      tabs={tabs}
      activeTab={activeTab}
      onTabChange={setActiveTab}
    >
      {activeTab === "inicio" && (
        <>
          <div className="dashboard-grid">
            <MetricCard icon={<Coins />} label="Saldo disponivel" value={`${money.format(data.professor.saldoMoedas)} moedas`} tone="green" />
            <MetricCard icon={<HandCoins />} label="Alunos cadastrados" value={data.alunos.length} tone="amber" />
            <MetricCard icon={<Send />} label="Moedas enviadas" value={`${money.format(totalEnviado)} moedas`} tone="blue" />
          </div>

          <section className="surface-card professor-identity-card">
            <SectionTitle eyebrow="Vinculo institucional" title={data.professor.instituicao} />
            <p>
              Cadastro recebido pela instituicao parceira. Esses dados identificam o professor responsavel pelos
              reconhecimentos enviados aos alunos.
            </p>
            <div className="identity-details">
              <span>
                <small>Professor</small>
                <strong>{data.professor.nome}</strong>
              </span>
              <span>
                <small>CPF institucional</small>
                <strong>{formatCpf(data.professor.cpf)}</strong>
              </span>
              <span>
                <small>Departamento</small>
                <strong>{data.professor.departamento}</strong>
              </span>
              <span>
                <small>Instituicao</small>
                <strong>{data.professor.instituicao}</strong>
              </span>
            </div>
          </section>
        </>
      )}

      {activeTab === "enviar" && (
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
      )}

      {activeTab === "alunos" && (
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
      )}

      {activeTab === "extrato" && <TransactionsTable title="Historico do professor" rows={data.extrato} />}
      {activeTab === "notificacoes" && <NotificationsPanel items={data.notificacoes || []} />}
    </DashboardShell>
  );
}

export default ProfessorDashboard;
