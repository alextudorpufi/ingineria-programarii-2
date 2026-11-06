-- Development-only seed: 100,000 records per main entity.
-- docker compose --profile seed up db-seed-large
-- Fresh demo accounts: skillingadmin@contact.ro, skillingclient@contact.ro,
-- skillingprof@contact.ro; password: admin. Reruns preserve existing data.
\set record_count 100000
\set client_count 49999
\set professional_count 50000
\ir populate_records.sql
