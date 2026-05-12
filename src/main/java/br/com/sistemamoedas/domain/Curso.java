package br.com.sistemamoedas.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "cursos", uniqueConstraints = @UniqueConstraint(columnNames = { "instituicao_id", "nome" }))
public class Curso extends PanacheEntity {

    @Column(nullable = false, length = 120)
    public String nome;

    @ManyToOne(optional = false)
    @JoinColumn(name = "instituicao_id", nullable = false)
    public Instituicao instituicao;

    protected Curso() {
    }

    public Curso(String nome, Instituicao instituicao) {
        this.nome = nome;
        this.instituicao = instituicao;
    }
}
