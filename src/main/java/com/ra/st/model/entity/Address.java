package com.ra.st.model.entity;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "address")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(name = "full_address", nullable = false, length = 255)
    private String fullAddress;

    @Column(name = "phone", nullable = false, length = 15)
    private String phone;

    @Column(name = "receive_name", nullable = false, length = 50)
    private String receiveName;
}
