package br.com.sistemamoedas.repository;

import br.com.sistemamoedas.domain.EmailNotificacao;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class EmailNotificacaoRepository implements PanacheRepository<EmailNotificacao> {

    public List<EmailNotificacao> porDestinatario(String destinatario) {
        return list("destinatario = ?1 order by criadoEm desc", destinatario);
    }
}
