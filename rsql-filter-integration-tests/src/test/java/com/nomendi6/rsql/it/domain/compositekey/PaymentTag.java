package com.nomendi6.rsql.it.domain.compositekey;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

/**
 * A derived identity: the payment it tags is part of its {@code @IdClass} key. Hibernate gives such an entity a
 * virtual identifier with no attribute of its own, so a path through it has an identifier step with no name.
 */
@Entity
@IdClass(PaymentTag.Key.class)
@Table(name = "payment_tag")
public class PaymentTag implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @ManyToOne
    @JoinColumn(name = "payment_id")
    private KeyedPayment payment;

    @Id
    @Column(name = "tag")
    private String tag;

    public PaymentTag() {}

    public PaymentTag(KeyedPayment payment, String tag) {
        this.payment = payment;
        this.tag = tag;
    }

    public KeyedPayment getPayment() {
        return payment;
    }

    public String getTag() {
        return tag;
    }

    public static class Key implements Serializable {

        private static final long serialVersionUID = 1L;

        private Long payment;
        private String tag;

        public Key() {}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key other)) return false;
            return Objects.equals(payment, other.payment) && Objects.equals(tag, other.tag);
        }

        @Override
        public int hashCode() {
            return Objects.hash(payment, tag);
        }
    }
}
