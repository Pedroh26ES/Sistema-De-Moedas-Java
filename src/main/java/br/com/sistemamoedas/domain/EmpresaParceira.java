package br.com.sistemamoedas.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "empresas_parceiras")
public class EmpresaParceira extends Usuario {

    @Column(nullable = false, unique = true, length = 18)
    public String cnpj;

    @Column(nullable = false, length = 220)
    public String endereco;

    @Column(nullable = false, length = 80)
    public String contato;

    protected EmpresaParceira() {
    }

    public EmpresaParceira(String nome, String email, String senhaHash, String cnpj, String endereco, String contato) {
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
        this.perfil = Perfil.EMPRESA;
        this.cnpj = cnpj;
        this.endereco = endereco;
        this.contato = contato;
    }
}
