-- Development-only seed: 5,000 records per main entity.
-- docker compose --profile seed up db-seed
-- Fresh demo accounts: skillingadmin@contact.ro, skillingclient@contact.ro,
-- skillingprof@contact.ro; password: admin. Reruns preserve existing data.
\set record_count 5000
\set client_count 2499
\set professional_count 2500
\ir populate_records.sql
