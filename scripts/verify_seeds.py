#!/usr/bin/env python3
"""Verify development seeds in an ephemeral PostgreSQL container, never the app DB."""
import subprocess
import time
import uuid
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
CONTAINER = 'skillink-seed-check-' + uuid.uuid4().hex[:12]
TABLES = ('service_categories', 'service_subcategories', 'customers', 'users',
          'leads', 'lead_claims', 'wallet_transactions', 'disputes',
          'chat_messages', 'ratings', 'audit_logs')


def run(*args):
    result = subprocess.run(args, text=True, capture_output=True)
    if result.returncode:
        raise RuntimeError(result.stderr or result.stdout)
    return result.stdout.strip()


def sql(database, statement):
    return run('docker', 'exec', CONTAINER, 'psql', '-U', 'postgres', '-d', database,
               '-v', 'ON_ERROR_STOP=1', '-Atc', statement)


def file(database, path):
    run('docker', 'exec', CONTAINER, 'psql', '-U', 'postgres', '-d', database,
        '-v', 'ON_ERROR_STOP=1', '-f', path)


def seed(database, size):
    file(database, f'/scripts/populate_{size}_records.sql')


def snapshot(database):
    # Hash every row, including timestamps, identities, balances and profile edits.
    tables = (*TABLES, 'user_professional')
    return {table: sql(database,
            f"SELECT md5(string_agg(row_hash, '' ORDER BY row_hash)) FROM "
            f"(SELECT md5(row_to_json(t)::text) row_hash FROM {table} t) rows")
            for table in tables}


def verify(database, size):
    for table in TABLES:
        assert int(sql(database, f'SELECT count(*) FROM {table}')) == size, table
    assert int(sql(database, 'SELECT count(*) FROM user_professional')) == size // 2
    checks = {
        'roles': "SELECT count(*) FROM users WHERE role NOT IN ('pro','client','admin')",
        'categories': "SELECT count(*) FROM user_professional p, jsonb_array_elements_text(p.categories) c WHERE NOT EXISTS (SELECT 1 FROM service_categories s WHERE s.id=c)",
        'counties': "SELECT count(*) FROM user_professional p, jsonb_array_elements_text(p.coverage_areas) c WHERE c NOT IN ('RO-B','RO-IF')",
        'profiles': "SELECT count(*) FROM user_professional WHERE onboarding_status <> 'COMPLETE' OR NOT accepting_leads OR NOT supports_onsite OR NOT supports_remote OR capabilities='[]'::jsonb",
        'matching': "SELECT count(*) FROM leads WHERE work_mode IS NULL OR required_capabilities='[]'::jsonb OR preferred_capabilities='[]'::jsonb",
        'claim counts': "SELECT count(*) FROM leads l WHERE current_pros <> (SELECT count(*) FROM lead_claims c WHERE c.lead_id=l.id)",
        'client roles': "SELECT count(*) FROM leads l JOIN users u ON u.id=l.client_id WHERE u.role <> 'client'",
        'pro roles': "SELECT count(*) FROM lead_claims c JOIN users u ON u.id=c.pro_id WHERE u.role <> 'pro'",
    }
    for label, query in checks.items():
        assert sql(database, query) == '0', label
    print(f'PASS {database}: {size:,} records; matching/profile/relationship checks', flush=True)


def create(database):
    sql('postgres', f'CREATE DATABASE {database}')
    for migration in sorted((ROOT / 'backend/marketplace-app/src/main/resources/db/migration').glob('V*.sql'), key=lambda p: int(p.name.split('__')[0][1:])):
        file(database, '/migrations/' + migration.name)


try:
    run('docker', 'run', '-d', '--rm', '--name', CONTAINER,
        '-e', 'POSTGRES_HOST_AUTH_METHOD=trust',
        '--mount', f'type=bind,src={ROOT / "scripts"},dst=/scripts,readonly',
        '--mount', f'type=bind,src={ROOT / "backend/marketplace-app/src/main/resources/db/migration"},dst=/migrations,readonly',
        'postgres:18')
    for attempt in range(60):
        try:
            sql('postgres', 'SELECT 1')
            break
        except RuntimeError:
            time.sleep(1)
    else:
        raise RuntimeError('Temporary database did not start')
    create('upgrade_check')
    seed('upgrade_check', 5000)
    verify('upgrade_check', 5000)
    sql('upgrade_check', "UPDATE users SET name='Edited user', password_hash='preserve password', credits_balance=1234 WHERE id='00000000-0000-0000-0002-000000000001'; UPDATE user_professional SET services_offered='[\"Edited service\"]', version=7 WHERE user_id='00000000-0000-0000-0002-000000000001'")
    before = snapshot('upgrade_check')
    seed('upgrade_check', 5000)
    assert before == snapshot('upgrade_check'), 'Small rerun changed existing rows'
    print('PASS small rerun preserves every existing row and user edits', flush=True)
    seed('upgrade_check', 100000)
    verify('upgrade_check', 100000)
    assert sql('upgrade_check', "SELECT name || ':' || password_hash || ':' || credits_balance FROM users WHERE id='00000000-0000-0000-0002-000000000001'") == 'Edited user:preserve password:1234'
    assert sql('upgrade_check', "SELECT version FROM user_professional WHERE user_id='00000000-0000-0000-0002-000000000001'") == '7'
    before = snapshot('upgrade_check')
    seed('upgrade_check', 100000)
    seed('upgrade_check', 5000)
    assert before == snapshot('upgrade_check'), 'Large/small rerun changed existing rows'
    print('PASS 5k → 100k → 100k → 5k preserves existing rows and user edits', flush=True)
    create('fresh_large')
    seed('fresh_large', 100000)
    verify('fresh_large', 100000)
    print('All seed checks passed. The application database was not accessed.', flush=True)
finally:
    subprocess.run(['docker', 'rm', '-f', '-v', CONTAINER], capture_output=True)
