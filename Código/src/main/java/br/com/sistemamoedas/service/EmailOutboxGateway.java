package br.com.sistemamoedas.service;

import br.com.sistemamoedas.domain.EmailNotificacao;
import br.com.sistemamoedas.repository.EmailNotificacaoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

@ApplicationScoped
public class EmailOutboxGateway implements EmailGateway {

    private static final Logger LOG = Logger.getLogger(EmailOutboxGateway.class);

    @Inject
    EmailNotificacaoRepository emails;

    @Override
    @Transactional
    public void enviar(String destinatario, String assunto, String conteudo, String codigoReferencia) {
        emails.persist(new EmailNotificacao(destinatario, assunto, conteudo, codigoReferencia));
        LOG.infof("Email registrado para %s: %s", destinatario, assunto);
    }
}
