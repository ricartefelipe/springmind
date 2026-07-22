package dev.springmind.wallet.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "beneficiaries")
public class BeneficiaryEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(name = "pix_key", nullable = false)
    private String pixKey;

    protected BeneficiaryEntity() {}

    public BeneficiaryEntity(String id, String name, String pixKey) {
        this.id = id;
        this.name = name;
        this.pixKey = pixKey;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPixKey() {
        return pixKey;
    }
}
