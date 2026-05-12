package br.com.sistemamoedas.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "transacoes")
public class Transacao extends PanacheEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    public TipoTransacao tipo;

    @Column(nullable = false)
    public int valor;

    @Column(nullable = false)
    public LocalDateTime criadaEm = LocalDateTime.now();

    @Column(nullable = false, length = 800)
    public String mensagem;

    @Column(length = 40)
    public String codigoCupom;

    @Column(nullable = false)
    public boolean cupomValidado = false;

    public LocalDateTime validadoEm;

    @ManyToOne
    public Professor professor;

    @ManyToOne
    public Aluno aluno;

    @ManyToOne
    public EmpresaParceira empresa;

    @ManyToOne
    public Vantagem vantagem;
}
