package br.com.sistemamoedas.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "redefinicoes_senha")
public class RedefinicaoSenha extends PanacheEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    public Usuario usuario;

    @Column(nullable = false, unique = true, length = 64)
    public String tokenHash;

    @Column(nullable = false)
    public LocalDateTime expiraEm;

    @Column(nullable = false)
    public boolean usado = false;

    @Column(nullable = false)
    public LocalDateTime criadoEm = LocalDateTime.now();

    protected RedefinicaoSenha() {
    }

    public RedefinicaoSenha(Usuario usuario, String tokenHash, LocalDateTime expiraEm) {
        this.usuario = usuario;
        this.tokenHash = tokenHash;
        this.expiraEm = expiraEm;
    }
}
