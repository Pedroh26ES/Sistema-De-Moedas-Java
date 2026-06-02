package br.com.sistemamoedas.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Usuario extends PanacheEntity {

    @Column(nullable = false, length = 120)
    public String nome;

    @Column(nullable = false, unique = true, length = 160)
    public String email;

    @Column(nullable = false, length = 220)
    public String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public Perfil perfil;

    
    @Column(length = 32)
    public String telefoneWhatsapp;
@Column(nullable = false)
    public boolean ativo = true;
}
