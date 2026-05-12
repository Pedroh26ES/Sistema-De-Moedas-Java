package br.com.sistemamoedas.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "emails_notificacao")
public class EmailNotificacao extends PanacheEntity {

    @Column(nullable = false, length = 180)
    public String destinatario;

    @Column(nullable = false, length = 160)
    public String assunto;

    @Column(nullable = false, length = 1200)
    public String conteudo;

    @Column(length = 40)
    public String codigoReferencia;

    @Column(nullable = false)
    public LocalDateTime criadoEm = LocalDateTime.now();

    protected EmailNotificacao() {
    }

    public EmailNotificacao(String destinatario, String assunto, String conteudo, String codigoReferencia) {
        this.destinatario = destinatario;
        this.assunto = assunto;
        this.conteudo = conteudo;
        this.codigoReferencia = codigoReferencia;
    }
}
