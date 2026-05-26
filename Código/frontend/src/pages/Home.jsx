import React from "react";
import {
  ArrowRight,
  BadgeCheck,
  BarChart3,
  Coins,
  HandCoins,
  MailCheck,
  ReceiptText,
  ShieldCheck,
  Sparkles,
  Store,
  TicketCheck
} from "lucide-react";
import { APP_NAME, CLASSROOM_IMAGE, HERO_IMAGE } from "../config/app";
import { BrandLogo } from "../components/Logo";
import { dashboardFor, navigateTo } from "../utils/navigation";

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
            O Valoriza Ae transforma boas entregas em uma experiencia clara: moedas com proposito,
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
            title="Confianca na entrega"
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
      <BrandLogo onClick={() => navigateTo("/")} />
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

export default Home;
