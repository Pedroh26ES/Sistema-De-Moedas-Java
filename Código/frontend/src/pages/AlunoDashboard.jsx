import { useMemo, useState } from "react";
import { Bell, Copy, Gift, QrCode, ReceiptText, Store, TicketCheck, WalletCards, X } from "lucide-react";
import {
  BenefitDescription,
  CatalogToolbar,
  DashboardShell,
  EmptyState,
  LoadingScreen,
  MetricCard,
  NotificationsPanel,
  SectionTitle,
  TransactionsTable
} from "../components/ui";
import { useDashboard } from "../hooks/useDashboard";
import { api } from "../services/api";
import { money } from "../utils/formatters";

function AlunoDashboard({ session, logout, notify }) {
  const { data, loading, reload } = useDashboard("/api/aluno/dashboard", notify);
  const [catalogQuery, setCatalogQuery] = useState("");
  const [catalogFilter, setCatalogFilter] = useState("todos");
  const [lastCoupon, setLastCoupon] = useState(null);
  const [expandedQr, setExpandedQr] = useState(null);
  const [activeTab, setActiveTab] = useState("inicio");
  const tabs = [
    { value: "inicio", label: "Inicio", icon: <WalletCards /> },
    { value: "catalogo", label: "Vantagens", icon: <Store /> },
    { value: "extrato", label: "Extrato", icon: <ReceiptText /> },
    { value: "notificacoes", label: "Notificacoes", icon: <Bell /> }
  ];

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
  const bonusLevels = [
    { spent: 100, title: "Essencial", benefit: "Voucher de impressao", reward: "+60 paginas P&B" },
    { spent: 250, title: "Plus", benefit: "Combo cafe e estudo", reward: "1 retirada extra" },
    { spent: 500, title: "Elite", benefit: "Credito na livraria", reward: "+R$ 30 em material" }
  ];
  const unlockedBonus = [...bonusLevels].reverse().find((bonus) => moedasUsadas >= bonus.spent);
  const nextBonus = bonusLevels.find((bonus) => moedasUsadas < bonus.spent);
  const bonusTarget = nextBonus || unlockedBonus || bonusLevels[0];
  const bonusProgress = bonusTarget ? Math.min(100, Math.round((moedasUsadas / bonusTarget.spent) * 100)) : 100;
  const missingForBonus = Math.max((bonusTarget?.spent || 0) - moedasUsadas, 0);
  const bonusTitle = unlockedBonus ? `${unlockedBonus.title} liberado` : `${bonusTarget.title} em andamento`;
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
  const extratoCupomRecente = extrato.find((row) => row.codigoCupom);
  const recentCoupon = lastCoupon || (extratoCupomRecente ? {
    codigo: extratoCupomRecente.codigoCupom,
    qrCodeUrl: extratoCupomRecente.qrCodeUrl,
    vantagem: extratoCupomRecente.vantagem || "Cupom resgatado"
  } : null);

  const resgatar = async (vantagemId) => {
    try {
      const vantagem = vantagens.find((item) => item.id === vantagemId);
      const cupom = await api("/api/aluno/resgates", { method: "POST", body: JSON.stringify({ vantagemId }) });
      setLastCoupon({ codigo: cupom.codigo, qrCodeUrl: cupom.qrCodeUrl, vantagem: vantagem?.titulo || "Vantagem resgatada" });
      notify("success", `${cupom.mensagem} Codigo: ${cupom.codigo}`);
      reload();
    } catch (error) {
      notify("error", error.message);
    }
  };

  const copiarCupom = async () => {
    if (!recentCoupon) return;
    try {
      await navigator.clipboard.writeText(recentCoupon.codigo);
      notify("success", "Cupom copiado.");
    } catch {
      notify("error", "Nao foi possivel copiar o cupom automaticamente.");
    }
  };

  if (loading) return <LoadingScreen />;
  if (!data) return null;

  return (
    <DashboardShell
      session={session}
      active="aluno"
      logout={logout}
      summary={data.resumo}
      tabs={tabs}
      activeTab={activeTab}
      onTabChange={setActiveTab}
    >
      {activeTab === "inicio" && (
        <>
          <div className="dashboard-grid">
            <MetricCard icon={<WalletCards />} label="Saldo atual" value={`${money.format(data.aluno.saldoMoedas)} moedas`} tone="green" />
            <MetricCard icon={<ReceiptText />} label="Moedas recebidas" value={`${money.format(moedasRecebidas)} moedas`} tone="amber" />
            <MetricCard icon={<TicketCheck />} label="Cupons validados" value={cuponsValidados} tone="blue" />
          </div>

          <div className="dashboard-insight-grid">
            <section className="surface-card bonus-card">
          <div className="bonus-head">
            <div className="bonus-card-icon"><Gift size={23} /></div>
            <SectionTitle eyebrow="Recompensa por resgates" title={bonusTitle} />
          </div>
          <p className="compact-note bonus-current">
            {unlockedBonus
              ? <><strong>Liberado:</strong> {unlockedBonus.benefit} ({unlockedBonus.reward})</>
              : <><strong>Primeira meta:</strong> {bonusTarget.benefit} ({bonusTarget.reward})</>}
          </p>
          <div className="bonus-summary">
            <span>
              <strong>{money.format(moedasUsadas)}</strong>
              usadas
            </span>
            <span>
              <strong>{nextBonus ? money.format(missingForBonus) : "0"}</strong>
              {nextBonus ? `faltam para ${nextBonus.title}` : "metas completas"}
            </span>
            <span>
              <strong>{bonusTarget.title}</strong>
              meta atual
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
                    <small>{money.format(bonus.spent)} moedas</small>
                  </div>
                  <p>
                    <b>{bonus.benefit}</b> {bonus.reward}
                  </p>
                </article>
              );
            })}
          </div>
            </section>

            <section className="surface-card coupon-card">
          <SectionTitle eyebrow="Cupom recente" title={recentCoupon ? recentCoupon.codigo : "Sem cupom ativo"} />
          <p>{recentCoupon ? recentCoupon.vantagem : "Resgate uma vantagem para receber codigo e QR Code."}</p>
          {recentCoupon?.qrCodeUrl && (
            <div className="coupon-qr-card">
              <button
                type="button"
                className="qr-image-button"
                onClick={() => setExpandedQr(recentCoupon)}
                aria-label={`Ampliar QR Code do cupom ${recentCoupon.codigo}`}
              >
                <img src={recentCoupon.qrCodeUrl} alt={`QR Code do cupom ${recentCoupon.codigo}`} />
              </button>
              <span>Apresente no parceiro para validar a retirada.</span>
            </div>
          )}
          <button className="outline-button" onClick={copiarCupom} disabled={!recentCoupon}>
            <Copy size={17} />
            Copiar cupom
          </button>
            </section>
          </div>
        </>
      )}

      {activeTab === "catalogo" && (
        <>
          <div className="section-title-row tab-section-title">
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
                    <div className={vantagem.cupomValidado ? "coupon-pass validated" : couponPaused ? "coupon-pass deactivated" : "coupon-pass pending"}>
                      <div className="coupon-pass-info">
                        <span><TicketCheck size={16} /> Cupom</span>
                        <strong>{vantagem.codigoCupom}</strong>
                        <small>
                          {vantagem.cupomValidado
                            ? "Validado pelo parceiro"
                            : couponPaused
                              ? "Pausado pelo parceiro"
                              : "Aguardando validacao"}
                        </small>
                      </div>
                      {vantagem.qrCodeUrl && (
                        <div className="coupon-pass-qr">
                          <button
                            type="button"
                            className="qr-image-button small"
                            onClick={() => setExpandedQr({
                              codigo: vantagem.codigoCupom,
                              qrCodeUrl: vantagem.qrCodeUrl,
                              vantagem: vantagem.titulo
                            })}
                            aria-label={`Ampliar QR Code do cupom ${vantagem.codigoCupom}`}
                          >
                            <img src={vantagem.qrCodeUrl} alt={`QR Code do cupom ${vantagem.codigoCupom}`} />
                          </button>
                          <span><QrCode size={13} /> QR</span>
                        </div>
                      )}
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
        </>
      )}

      {activeTab === "extrato" && <TransactionsTable title="Extrato do aluno" rows={data.extrato} />}
      {activeTab === "notificacoes" && <NotificationsPanel items={data.notificacoes || []} />}
      {expandedQr && <QrCodeModal cupom={expandedQr} onClose={() => setExpandedQr(null)} />}
    </DashboardShell>
  );
}

function QrCodeModal({ cupom, onClose }) {
  return (
    <div className="qr-modal-backdrop" role="presentation" onClick={onClose}>
      <section className="qr-modal" role="dialog" aria-modal="true" aria-label={`QR Code do cupom ${cupom.codigo}`} onClick={(event) => event.stopPropagation()}>
        <button className="qr-modal-close" type="button" onClick={onClose} aria-label="Fechar QR Code">
          <X size={18} />
        </button>
        <span className="eyebrow">Cupom para validacao</span>
        <h2>{cupom.codigo}</h2>
        <p>{cupom.vantagem}</p>
        <img src={cupom.qrCodeUrl} alt={`QR Code ampliado do cupom ${cupom.codigo}`} />
        <small>Mostre este QR Code ao parceiro para localizar e validar o cupom no atendimento.</small>
      </section>
    </div>
  );
}

export default AlunoDashboard;
