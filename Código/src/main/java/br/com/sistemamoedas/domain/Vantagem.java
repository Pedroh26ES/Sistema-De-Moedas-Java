package br.com.sistemamoedas.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "vantagens")
public class Vantagem extends PanacheEntity {

    @Column(nullable = false, length = 120)
    public String titulo;

    @Column(nullable = false, length = 600)
    public String descricao;

    @Column(nullable = false, length = 600)
    public String fotoUrl;

    @Column(nullable = false)
    public int custoMoedas;

    @Column(nullable = false)
    public boolean ativa = true;

    @ManyToOne(optional = false)
    public EmpresaParceira empresa;

    protected Vantagem() {
    }

    public Vantagem(String titulo, String descricao, String fotoUrl, int custoMoedas, EmpresaParceira empresa) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.fotoUrl = fotoUrl;
        this.custoMoedas = custoMoedas;
        this.empresa = empresa;
    }
}
