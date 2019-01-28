create table provider_channel (
  provider_address text not null,
  user_address text not null,
  bitmask text not null,
  revoke_address text not null,
  revoke_tx_id text not null,
  creation_time integer not null,
  since integer,
  until integer,
  path text not null,
  primary key (provider_address, user_address)
);

create table user_channel (
  provider_name text not null,
  provider_address text not null,
  user_address text not null,
  bitmask text not null,
  revoke_address text not null,
  revoke_tx_id text not null,
  since integer,
  until integer,
  path text not null,
  primary key (provider_name, provider_address, user_address)
);