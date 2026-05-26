package br.com.sistemamoedas;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.sistemamoedas.domain.Aluno;
import br.com.sistemamoedas.domain.Vantagem;
import br.com.sistemamoedas.repository.AlunoRepository;
import br.com.sistemamoedas.repository.EmailNotificacaoRepository;
import br.com.sistemamoedas.repository.VantagemRepository;
import br.com.sistemamoedas.service.RegraNegocioException;
import br.com.sistemamoedas.service.VantagemService;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
class VantagemServiceTest {

    @Inject
    VantagemService vantagens;

    @Inject
    AlunoRepository alunos;

    @Inject
    VantagemRepository vantagemRepository;

    @Inject
    EmailNotificacaoRepository notificacoes;

    @Test
    void deveBloquearResgateDuplicadoDaMesmaVantagem() {
        Aluno aluno = alunos.find("email", "aluno@moedas.com").firstResult();
        Vantagem voucher = vantagemRepository.find("titulo", "Voucher de impressao").firstResult();

        RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                () -> vantagens.resgatar(aluno.id, voucher.id));

        assertTrue(erro.getMessage().contains("ja possui"));
    }

    @Test
    void deveAlternarStatusDaVantagemDaEmpresa() {
        Vantagem vantagem = vantagemRepository.find("titulo", "Material academico").firstResult();

        vantagens.alterarStatus(vantagem.empresa.id, vantagem.id, false);
        vantagemRepository.getEntityManager().clear();
        assertFalse(vantagemRepository.findById(vantagem.id).ativa);

        vantagens.alterarStatus(vantagem.empresa.id, vantagem.id, true);
        vantagemRepository.getEntityManager().clear();
        assertTrue(vantagemRepository.findById(vantagem.id).ativa);
    }

    @Test
    void deveNotificarAlunoEImpedirValidacaoQuandoCupomPendenteForPausado() {
        Vantagem voucher = vantagemRepository.find("titulo", "Voucher de impressao").firstResult();

        vantagens.alterarStatus(voucher.empresa.id, voucher.id, false);

        assertTrue(notificacoes.porDestinatario("aluno@moedas.com").stream()
                .anyMatch(email -> "Cupom temporariamente desativado".equals(email.assunto)
                        && "SME-CAMPUS26".equals(email.codigoReferencia)));
        RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                () -> vantagens.validarCupom(voucher.empresa.id, "SME-CAMPUS26"));
        assertTrue(erro.getMessage().contains("desativado"));

        vantagens.alterarStatus(voucher.empresa.id, voucher.id, true);
    }

    @Test
    void deveExcluirVantagemSemResgateEManterHistoricoQuandoJaExisteCupom() {
        Vantagem base = vantagemRepository.find("titulo", "Combo cafe e estudo").firstResult();
        Vantagem temporaria = vantagens.cadastrar(base.empresa.id, "Beneficio temporario", base.descricao,
                base.fotoUrl, 30);

        vantagens.excluir(base.empresa.id, temporaria.id);
        vantagemRepository.getEntityManager().clear();
        assertNull(vantagemRepository.findById(temporaria.id));

        Vantagem voucherComCupom = vantagemRepository.find("titulo", "Voucher de impressao").firstResult();
        RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                () -> vantagens.excluir(voucherComCupom.empresa.id, voucherComCupom.id));

        assertTrue(erro.getMessage().contains("Pause a vantagem"));
    }

    @Test
    void emailDeCupomDeveOrientarUsoSemExporRotasTecnicas() {
        Aluno aluno = alunos.find("email", "aluno@moedas.com").firstResult();
        Vantagem base = vantagemRepository.find("titulo", "Combo cafe e estudo").firstResult();
        Vantagem vantagem = vantagens.cadastrar(base.empresa.id, "Passe rapido de teste", base.descricao,
                base.fotoUrl, 10);

        String codigo = vantagens.resgatar(aluno.id, vantagem.id);

        assertNotNull(codigo);
        assertTrue(notificacoes.porDestinatario(aluno.email).stream()
                .anyMatch(email -> codigo.equals(email.codigoReferencia)
                        && email.conteudo.contains("QR Code e o status ficam disponiveis no seu painel")
                        && !email.conteudo.contains("/api/cupons/")
                        && !email.conteudo.contains("/empresa?cupom=")));
    }
}
