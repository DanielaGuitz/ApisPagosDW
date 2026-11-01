create table Clientes
(
    id             int auto_increment
        primary key,
    nombre         varchar(100)                   not null,
    telefono       varchar(20)                    null,
    direccion      varchar(200)                   not null,
    email          varchar(100)                   null,
    fecha_registro date       default (curdate()) null,
    activo         tinyint(1) default 1           null
);

create table Roles
(
    id             int auto_increment
        primary key,
    nombre         varchar(50)                        not null,
    descripcion    varchar(200)                       null,
    fecha_creacion datetime default CURRENT_TIMESTAMP null,
    constraint nombre
        unique (nombre)
);

create table Tipos_Servicio
(
    id             int auto_increment
        primary key,
    nombre         varchar(50)                          not null comment 'Ej: Residencial, Comercial, Industrial, Institucional',
    descripcion    varchar(200)                         null,
    activo         tinyint(1) default 1                 null,
    fecha_creacion datetime   default CURRENT_TIMESTAMP null,
    constraint uk_tipo_servicio_nombre
        unique (nombre)
);

create table Contadores
(
    id                int auto_increment
        primary key,
    cliente_id        int                            not null,
    codigo            varchar(20)                    not null,
    referencia        varchar(50)                    null,
    sector            varchar(50)                    null,
    tipo_servicio_id  int                            not null comment 'Tipo de servicio del contador',
    activo            tinyint(1) default 1           null,
    fecha_instalacion date       default (curdate()) null,
    ubicacion         varchar(100)                   null,
    deleted_at        timestamp                      null,
    constraint uk_contadores_codigo
        unique (codigo),
    constraint fk_contadores_cliente
        foreign key (cliente_id) references Clientes (id)
            on update cascade,
    constraint fk_contadores_tipo_servicio
        foreign key (tipo_servicio_id) references Tipos_Servicio (id)
            on update cascade
);

create table Tarifas
(
    id                  int auto_increment
        primary key,
    tipo_servicio_id    int                                      not null comment 'Referencia al catálogo de tipos',
    volumen_incluido_m3 decimal(10, 2) default 0.00              not null comment 'Metros cúbicos incluidos en tarifa base',
    tarifa_base         decimal(10, 4)                           not null comment 'Tarifa por m³ dentro del volumen incluido',
    tarifa_exceso       decimal(10, 4)                           not null comment 'Tarifa por m³ de excedente',
    vigente_desde       date                                     not null,
    vigente_hasta       date                                     null,
    activo              tinyint(1)     default 1                 null,
    fecha_creacion      datetime       default CURRENT_TIMESTAMP null,
    constraint fk_tarifas_tipo_servicio
        foreign key (tipo_servicio_id) references Tipos_Servicio (id)
            on update cascade,
    constraint chk_tarifas_fechas
        check ((`vigente_hasta` is null) or (`vigente_hasta` >= `vigente_desde`)),
    constraint chk_tarifas_tarifas
        check ((`tarifa_base` >= 0) and (`tarifa_exceso` >= 0)),
    constraint chk_tarifas_volumen
        check (`volumen_incluido_m3` >= 0)
);

create table Usuarios
(
    id             int auto_increment
        primary key,
    nombre         varchar(100)                         not null,
    email          varchar(100)                         not null,
    password_hash  varchar(255)                         not null,
    rol_id         int                                  not null,
    activo         tinyint(1) default 1                 null,
    fecha_creacion datetime   default CURRENT_TIMESTAMP null,
    ultimo_acceso  datetime                             null,
    constraint email
        unique (email),
    constraint fk_usuarios_rol
        foreign key (rol_id) references Roles (id)
            on update cascade
);

create table Lecturas
(
    id                  int auto_increment
        primary key,
    contador_id         int                                      not null,
    lectura_anterior    decimal(10, 2) default 0.00              null,
    lectura_actual      decimal(10, 2)                           not null,
    consumo             decimal(10, 2) as (greatest((`lectura_actual` - `lectura_anterior`), 0)) stored,
    tarifa_id           int                                      null comment 'ID de la tarifa aplicada',
    volumen_base_m3     decimal(10, 2) default 0.00              null comment 'Volumen incluido en tarifa base (m³)',
    consumo_base_m3     decimal(10, 2) default 0.00              null comment 'Consumo cubierto por tarifa base (m³)',
    consumo_exceso_m3   decimal(10, 2) default 0.00              null comment 'Consumo excedente (m³)',
    tarifa_base_valor   decimal(10, 4) default 0.0000            null comment 'Valor de tarifa base aplicada',
    tarifa_exceso_valor decimal(10, 4) default 0.0000            null comment 'Valor de tarifa exceso aplicada',
    monto_base          decimal(10, 2) default 0.00              null comment 'Monto por consumo base',
    monto_exceso        decimal(10, 2) default 0.00              null comment 'Monto por consumo excedente',
    monto_total         decimal(10, 2) as ((coalesce(`monto_base`, 0) + coalesce(`monto_exceso`, 0))) stored comment 'Monto total calculado',
    fecha_lectura       datetime       default CURRENT_TIMESTAMP null,
    usuario_lector_id   int                                      not null,
    observaciones       varchar(200)                             null,
    periodo             date                                     not null,
    constraint fk_lecturas_contador
        foreign key (contador_id) references Contadores (id)
            on update cascade,
    constraint fk_lecturas_tarifa
        foreign key (tarifa_id) references Tarifas (id)
            on update cascade on delete set null,
    constraint fk_lecturas_usuario
        foreign key (usuario_lector_id) references Usuarios (id)
            on update cascade,
    constraint chk_lecturas_consumo
        check (`lectura_actual` >= `lectura_anterior`)
);

create table Historial
(
    id           int auto_increment
        primary key,
    contador_id  int                                null,
    lectura_id   int                                null,
    usuario_id   int                                null,
    fecha_cambio datetime default CURRENT_TIMESTAMP null,
    tipo_cambio  varchar(30)                        not null,
    detalle      varchar(200)                       not null,
    ip_origen    varchar(45)                        null,
    constraint fk_historial_contador
        foreign key (contador_id) references Contadores (id)
            on update cascade on delete set null,
    constraint fk_historial_lectura
        foreign key (lectura_id) references Lecturas (id)
            on update cascade on delete set null,
    constraint fk_historial_usuario
        foreign key (usuario_id) references Usuarios (id)
            on update cascade on delete set null
);

create table Pagos
(
    id               int auto_increment
        primary key,
    lectura_id       int                                   not null,
    monto            decimal(10, 2)                        not null,
    fecha_pago       date        default (curdate())       null,
    metodo           varchar(30)                           not null,
    usuario_registro int                                   not null,
    comprobante      varchar(100)                          null,
    observaciones    varchar(200)                          null,
    estado           varchar(20) default 'Completado'      null,
    fecha_registro   datetime    default CURRENT_TIMESTAMP null,
    constraint fk_pagos_lectura
        foreign key (lectura_id) references Lecturas (id)
            on update cascade,
    constraint fk_pagos_usuario
        foreign key (usuario_registro) references Usuarios (id)
            on update cascade,
    constraint chk_pagos_estado
        check (`estado` in ('Completado', 'Pendiente', 'Anulado')),
	constraint chk_pagos_monto
		check (`monto` > 0)
);

create table ci_sessions
(
    id         varchar(128)             not null
        primary key,
    ip_address varchar(45)              not null,
    timestamp  int unsigned default '0' not null,
    data       blob                     not null
);

create index ci_sessions_timestamp
    on ci_sessions (timestamp);

