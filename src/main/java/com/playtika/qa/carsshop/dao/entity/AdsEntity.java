package com.playtika.qa.carsshop.dao.entity;

import lombok.*;
import org.hibernate.annotations.Check;


import javax.persistence.*;

import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "ads")
public class AdsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT")
    private Long id;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn (name = "user_id", nullable = false, columnDefinition = "BIGINT")
    private UserEntity user;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "car_id", nullable = false, columnDefinition = "BIGINT")
    private CarEntity car;

    @Column(nullable = false)
    @Check(constraints = "price > 0")
    private Integer price;

    @OneToOne
    private DealEntity deal;

    public AdsEntity(UserEntity user, CarEntity car, Integer price, DealEntity deal) {
        this.user = user;
        this.car = car;
        this.price = price;
        this.deal = deal;
    }
}

