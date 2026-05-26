package br.com.sistemamoedas.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "eventos_fila_local")
public class EventoFilaLocal extends PanacheEntity {

    @Column(nullable = false, length = 80)
    public String tipo;

    @Lob
    @Column(nullable = false)
    public String payload;

    @Column(nullable = false, length = 160)
    public String origem;

    @Column(nullable = false)
    public LocalDateTime criadoEm = LocalDateTime.now();

    protected EventoFilaLocal() {
    }

    public EventoFilaLocal(String tipo, String payload, String origem) {
        this.tipo = tipo;
        this.payload = payload;
        this.origem = origem;
    }
}
