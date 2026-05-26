import React, { useState } from "react";
import {
  Bell,
  CalendarDays,
  CheckCircle2,
  Filter,
  House,
  LayoutDashboard,
  LogOut,
  Search,
  UserRound,
  UsersRound
} from "lucide-react";
import { APP_NAME, periodFilters } from "../config/app";
import { BrandLogo, LogoMark } from "./Logo";
import { navigateTo, dashboardFor } from "../utils/navigation";
import {
  couponColumnText,
  formatDate,
  formatNotificationText,
  isWithinPeriod,
  labelTipo,
  money
} from "../utils/formatters";
import { parseBenefitDescription } from "../utils/benefits";

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

function DashboardShell({ session, logout, summary, tabs = [], activeTab, onTabChange, children }) {
  const profileLabel = {
    ALUNO: "Aluno",
    PROFESSOR: "Professor",
    EMPRESA: "Empresa parceira"
  }[session.perfil] || "Usuario";

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <BrandLogo onClick={() => navigateTo("/")} />
        <div className="sidebar-profile">
          <span>{profileLabel}</span>
          <strong>{session.nome}</strong>
          <small>{session.email}</small>
        </div>
        <nav>
          {tabs.length > 0 ? tabs.map((tab) => (
            <SidebarButton
              key={tab.value}
              icon={tab.icon}
              label={tab.label}
              active={activeTab === tab.value}
              onClick={() => onTabChange(tab.value)}
            />
          )) : (
            <SidebarButton icon={<LayoutDashboard />} label="Painel" active path={dashboardFor(session.perfil)} />
          )}
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

function SidebarButton({ icon, label, active, path, onClick }) {
  return (
    <button className={active ? "sidebar-link active" : "sidebar-link"} onClick={onClick || (() => navigateTo(path))}>
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
        <SectionTitle eyebrow="Notificacoes" title="Avisos do sistema" />
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
                <p>{formatNotificationText(item.conteudo)}</p>
                <small>{formatDate(item.criadaEm)}{item.codigoReferencia ? ` - Cupom ${item.codigoReferencia}` : ""}</small>
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

function Toast({ type, children }) {
  return <div className={`toast ${type}`}><CheckCircle2 size={18} />{children}</div>;
}

function LoadingScreen() {
  return (
    <main className="loading-screen">
      <div className="loader" />
      <LogoMark className="loading-logo" />
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

export {
  AuthRequired,
  BenefitDescription,
  CatalogToolbar,
  DashboardShell,
  EmptyState,
  Field,
  LoadingScreen,
  MetricCard,
  NotificationsPanel,
  PeriodFilter,
  Protected,
  SectionTitle,
  Toast,
  TransactionsTable
};
