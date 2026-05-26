import { useMemo, useState } from "react";
import {
  Bell,
  Building2,
  ClipboardCheck,
  EyeOff,
  PlusCircle,
  Power,
  ReceiptText,
  Store,
  TicketCheck
} from "lucide-react";
import {
  BenefitDescription,
  CatalogToolbar,
  DashboardShell,
  EmptyState,
  Field,
  LoadingScreen,
  MetricCard,
  NotificationsPanel,
  SectionTitle,
  TransactionsTable
} from "../components/ui";
import { PARTNER_IMAGE, benefitDescriptionExample } from "../config/app";
import { useDashboard } from "../hooks/useDashboard";
import { api } from "../services/api";
import { money } from "../utils/formatters";
import { emptyVantagem } from "../utils/vantagens";

function EmpresaDashboard({ session, logout, notify }) {
  const { data, loading, reload, setData } = useDashboard("/api/empresa/dashboard", notify);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(emptyVantagem());
  const [catalogQuery, setCatalogQuery] = useState("");
  const [statusFilter, setStatusFilter] = useState("todas");
  const [couponCode, setCouponCode] = useState(() => new URLSearchParams(window.location.search).get("cupom") || "");
  const [couponResult, setCouponResult] = useState(null);
  const [statusChangingId, setStatusChangingId] = useState(null);
  const [deletingId, setDeletingId] = useState(null);
  const [activeTab, setActiveTab] = useState("inicio");
  const tabs = [
    { value: "inicio", label: "Inicio", icon: <Building2 /> },
    { value: "vantagem", label: "Nova vantagem", icon: <PlusCircle /> },
    { value: "validacao", label: "Validacao", icon: <ClipboardCheck /> },
    { value: "catalogo", label: "Catalogo", icon: <Store /> },
    { value: "resgates", label: "Resgates", icon: <ReceiptText /> },
    { value: "notificacoes", label: "Notificacoes", icon: <Bell /> }
  ];

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
    <DashboardShell
      session={session}
      active="empresa"
      logout={logout}
      summary={data.resumo}
      tabs={tabs}
      activeTab={activeTab}
      onTabChange={setActiveTab}
    >
      {activeTab === "inicio" && (
        <div className="dashboard-grid">
          <MetricCard icon={<Building2 />} label="Empresa" value={data.empresa.nome} tone="green" />
          <MetricCard icon={<Store />} label="Vantagens ativas" value={`${activeCount}/${data.vantagens.length}`} tone="amber" />
          <MetricCard icon={<TicketCheck />} label="Cupons pendentes" value={cuponsPendentes} tone="blue" />
        </div>
      )}

      {activeTab === "vantagem" && (
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
      )}

      {activeTab === "validacao" && (
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
                    placeholder="Ex.: SME-CAMPUS26"
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
        </div>
      )}

      {activeTab === "catalogo" && (
        <>
          <div className="section-title-row tab-section-title">
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
        </>
      )}

      {activeTab === "resgates" && <TransactionsTable title="Cupons e resgates recebidos" rows={resgates} />}
      {activeTab === "notificacoes" && <NotificationsPanel items={data.notificacoes || []} />}
    </DashboardShell>
  );
}

export default EmpresaDashboard;
