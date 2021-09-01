CREATE TABLE public.vehicle
(
    id   uuid        NOT NULL,
    name varchar(50) NOT NULL,
    CONSTRAINT vehicle_pk PRIMARY KEY (id)

);
ALTER TABLE public.vehicle
    OWNER TO maxoptra_gps_app;

CREATE TABLE public.location
(
    id        uuid            NOT NULL,
    latitude  numeric(13, 10) NOT NULL,
    longitude numeric(13, 10) NOT NULL,
    CONSTRAINT location_pk PRIMARY KEY (id)

);
ALTER TABLE public.location
    OWNER TO maxoptra_gps_app;

CREATE TABLE public.schedule
(
    id                    uuid      NOT NULL,
    vehicle_id            uuid      NOT NULL,
    location_id           uuid      NOT NULL,
    plan_arrival_time     timestamp NOT NULL,
    plan_departure_time   timestamp NOT NULL,
    actual_arrival_time   timestamp,
    actual_departure_time timestamp,
    CONSTRAINT schedule_pk PRIMARY KEY (id)

);
ALTER TABLE public.schedule
    OWNER TO maxoptra_gps_app;

CREATE TABLE public.gps
(
    id         uuid            NOT NULL,
    vehicle_id uuid            NOT NULL,
    "time"     timestamp       NOT NULL,
    latitude   numeric(13, 10) NOT NULL,
    longitude  numeric(13, 10) NOT NULL,
    CONSTRAINT gps_pk PRIMARY KEY (id)

);
ALTER TABLE public.gps
    OWNER TO maxoptra_gps_app;

CREATE INDEX vehicle_name_index ON public.vehicle
    USING btree
    (
     name
        );

ALTER TABLE public.gps
    ADD CONSTRAINT vehicle_fk FOREIGN KEY (vehicle_id)
        REFERENCES public.vehicle (id) MATCH FULL
        ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE public.schedule
    ADD CONSTRAINT vehicle_fk FOREIGN KEY (vehicle_id)
        REFERENCES public.vehicle (id) MATCH FULL
        ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE public.schedule
    ADD CONSTRAINT location_fk FOREIGN KEY (location_id)
        REFERENCES public.location (id) MATCH FULL
        ON DELETE CASCADE ON UPDATE CASCADE;
