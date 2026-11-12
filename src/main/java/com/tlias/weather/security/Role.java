package com.tlias.weather.security;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Role {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true) private String code;
    @Column(nullable = false) private String name;
    protected Role() { }
    public Role(String code, String name) {
        this.code = code;
        this.name = name;
    }
    public String getCode() { return code; }
}
