package br.com.sistemamoedas.service;

import br.com.sistemamoedas.domain.Aluno;
import br.com.sistemamoedas.domain.EmpresaParceira;
import br.com.sistemamoedas.domain.Professor;
import br.com.sistemamoedas.domain.Vantagem;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EmailTemplateService {

    public String moedasRecebidas(Aluno aluno, Professor professor, int valor, String motivo) {
        return "Professor " + professor.nome + " enviou " + valor + " moedas."
                + " Saldo atual: " + aluno.saldoMoedas + " moedas."
                + " Motivo: " + motivo + ".";
    }

    public String moedasEnviadas(Aluno aluno, int valor, String motivo) {
        return "Envio confirmado para " + aluno.nome + ": " + valor
                + " moedas registradas. Motivo informado: " + motivo
                + ". O registro ja aparece no seu extrato e no painel do aluno.";
    }

    public String cupomAluno(Vantagem vantagem, String codigo, String validacaoUrl, String qrCodeUrl) {
        return "Cupom " + codigo + " gerado para " + vantagem.titulo
                + ". Mostre este codigo ou abra o QR Code no atendimento."
                + " O parceiro precisa validar o cupom para liberar o beneficio."
                + " O QR Code e o status ficam disponiveis no seu painel.";
    }

    public String cupomEmpresa(Aluno aluno, Vantagem vantagem, String codigo, String validacaoUrl, String qrCodeUrl) {
        return "Novo cupom para validar: " + codigo + ". Aluno: " + aluno.nome
                + ". Vantagem: " + vantagem.titulo
                + ". Use a area de validacao do painel para confirmar o atendimento.";
    }

    public String cupomValidado(EmpresaParceira empresa, String codigo, String vantagemTitulo) {
        return "Seu cupom " + codigo + " foi validado por " + empresa.nome
                + " para a vantagem " + vantagemTitulo
                + ". A troca ficou registrada no seu extrato.";
    }
}
