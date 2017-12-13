package com.playtika.qa.carsshop.dao.entity;


import lombok.*;
import org.hibernate.annotations.Check;
import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "deal")

public class DealEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT")
    private Long id;

    @ManyToOne(targetEntity = AdsEntity.class)
    @JoinColumn(name = "ads_id", columnDefinition = "BIGINT")
    private AdsEntity ads;

    public static enum Status {
        ACTIVATED, DECLINED, ACCEPTED
    }
    @Column(columnDefinition = "ENUM('ACTIVATED', 'DECLINED', 'ACCEPTED')", nullable = false)
    private Status status;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    @Check(constraints = "price > 0")
    private int price;
}



