package br.com.sistemamoedas;

import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.sistemamoedas.domain.Aluno;
import br.com.sistemamoedas.domain.EmpresaParceira;
import br.com.sistemamoedas.domain.Professor;
import br.com.sistemamoedas.repository.AlunoRepository;
import br.com.sistemamoedas.repository.EmpresaParceiraRepository;
import br.com.sistemamoedas.repository.ProfessorRepository;
import br.com.sistemamoedas.service.WhatsappBotService;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

@QuarkusTest
class WhatsappBotServiceTest {

    @Inject
    WhatsappBotService bot;

    @Inject
    AlunoRepository alunos;

    @Inject
    ProfessorRepository professores;

    @Inject
    EmpresaParceiraRepository empresas;

    @Test
    @Transactional
    void devePedirLoginNaPrimeiraMensagem() {
        bot.processarMensagem("5500999999999@c.us", "sair");

        String resposta = bot.processarMensagem("5500999999999@c.us", "Oi");

        assertTrue(resposta.contains("José"));
        assertTrue(resposta.contains("informe seu e-mail"));
    }

    @Test
    @Transactional
    void deveResponderMenuSaldoEVantagensDoAluno() {
        Aluno aluno = alunos.porEmail("aluno@moedas.com").orElseThrow();
        aluno.telefoneWhatsapp = "5500999999999";

        bot.processarMensagem("5500999999999@c.us", "sair");
        String pedidoSenha = bot.processarMensagem("5500999999999@c.us", "aluno@moedas.com");
        String login = bot.processarMensagem("5500999999999@c.us", "ValorizaAe#2026!");
        String menu = bot.processarMensagem("5500999999999@c.us", "menu");
        String saldo = bot.processarMensagem("5500999999999@c.us", "saldo");
        String vantagens = bot.processarMensagem("5500999999999@c.us", "vantagens");

        assertTrue(pedidoSenha.contains("informe sua senha"));
        assertTrue(login.contains("Login confirmado como Aluno"));
        assertTrue(menu.contains("Menu do aluno"));
        assertTrue(saldo.contains("moedas disponíveis"));
        assertTrue(vantagens.contains("Vantagens disponíveis"));
    }

    @Test
    @Transactional
    void deveExigirLoginQuandoNumeroTemMaisDeUmPerfil() {
        Aluno aluno = alunos.porEmail("aluno@moedas.com").orElseThrow();
        Professor professor = professores.find("email", "professor@moedas.com").firstResult();
        EmpresaParceira empresa = empresas.find("email", "empresa@moedas.com").firstResult();
        aluno.telefoneWhatsapp = "5500999999999";
        professor.telefoneWhatsapp = "5500999999999";
        empresa.telefoneWhatsapp = "5500999999999";

        bot.processarMensagem("5500999999999@c.us", "sair");
        String escolha = bot.processarMensagem("5500999999999@c.us", "menu");
        String login = bot.processarMensagem("5500999999999@c.us", "login aluno@moedas.com ValorizaAe#2026!");
        String saldo = bot.processarMensagem("5500999999999@c.us", "saldo");

        assertTrue(escolha.contains("e-mail"));
        assertTrue(login.contains("Login confirmado como Aluno"));
        assertTrue(saldo.contains("moedas disponíveis"));
    }

    @Test
    @Transactional
    void deveReconhecerNumeroBrasileiroSemNonoDigito() {
        Aluno aluno = alunos.porEmail("aluno@moedas.com").orElseThrow();
        aluno.telefoneWhatsapp = "5500999999999";

        bot.processarMensagem("550099999999@c.us", "sair");
        String login = bot.processarMensagem("550099999999@c.us", "login aluno@moedas.com ValorizaAe#2026!");
        String saldo = bot.processarMensagem("550099999999@c.us", "saldo");

        assertTrue(login.contains("Login confirmado como Aluno"));
        assertTrue(saldo.contains("moedas disponíveis"));
    }
}
