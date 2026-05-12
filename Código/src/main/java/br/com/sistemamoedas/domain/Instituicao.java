package br.com.sistemamoedas.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "instituicoes")
public class Instituicao extends PanacheEntity {

    @Column(nullable = false, unique = true, length = 140)
    public String nome;

    @Column(nullable = false, length = 80)
    public String cidade;

    protected Instituicao() {
    }

    public Instituicao(String nome, String cidade) {
        this.nome = nome;
        this.cidade = cidade;
    }
}
