package com.sparta.hotitemcollector.domain.order;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOrders is a Querydsl query type for Orders
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QOrders extends EntityPathBase<Orders> {

    private static final long serialVersionUID = 181069236L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QOrders orders = new QOrders("orders");

    public final com.sparta.hotitemcollector.global.QTimestamped _super = new com.sparta.hotitemcollector.global.QTimestamped(this);

    public final StringPath address = createString("address");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final ListPath<com.sparta.hotitemcollector.domain.orderitem.OrderItem, com.sparta.hotitemcollector.domain.orderitem.QOrderItem> orderItems = this.<com.sparta.hotitemcollector.domain.orderitem.OrderItem, com.sparta.hotitemcollector.domain.orderitem.QOrderItem>createList("orderItems", com.sparta.hotitemcollector.domain.orderitem.OrderItem.class, com.sparta.hotitemcollector.domain.orderitem.QOrderItem.class, PathInits.DIRECT2);

    public final ListPath<com.sparta.hotitemcollector.domain.payment.Payment, com.sparta.hotitemcollector.domain.payment.QPayment> paymentList = this.<com.sparta.hotitemcollector.domain.payment.Payment, com.sparta.hotitemcollector.domain.payment.QPayment>createList("paymentList", com.sparta.hotitemcollector.domain.payment.Payment.class, com.sparta.hotitemcollector.domain.payment.QPayment.class, PathInits.DIRECT2);

    public final StringPath phoneNumber = createString("phoneNumber");

    public final com.sparta.hotitemcollector.domain.user.QUser user;

    public final StringPath userName = createString("userName");

    public QOrders(String variable) {
        this(Orders.class, forVariable(variable), INITS);
    }

    public QOrders(Path<? extends Orders> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QOrders(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QOrders(PathMetadata metadata, PathInits inits) {
        this(Orders.class, metadata, inits);
    }

    public QOrders(Class<? extends Orders> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new com.sparta.hotitemcollector.domain.user.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

