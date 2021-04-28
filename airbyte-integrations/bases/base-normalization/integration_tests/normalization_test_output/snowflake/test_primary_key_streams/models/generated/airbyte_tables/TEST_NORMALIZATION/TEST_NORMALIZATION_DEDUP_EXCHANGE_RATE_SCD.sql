{{ config(alias="DEDUP_EXCHANGE_RATE_SCD", schema="TEST_NORMALIZATION", tags=["top-level"]) }}
-- SQL model to build a Type 2 Slowly Changing Dimension (SCD) table for each record identified by their primary key
select
    ID,
    CURRENCY,
    DATE,
    HKD,
    NZD,
    USD,
    DATE as _airbyte_start_at,
    lag(DATE) over (
        partition by ID, CURRENCY, cast(NZD as {{ dbt_utils.type_string() }})
        order by DATE desc, _airbyte_emitted_at desc
    ) as _airbyte_end_at,
    lag(DATE) over (
        partition by ID, CURRENCY, cast(NZD as {{ dbt_utils.type_string() }})
        order by DATE desc, _airbyte_emitted_at desc
    ) is null as _airbyte_active_row,
    _airbyte_emitted_at,
    _AIRBYTE_DEDUP_EXCHANGE_RATE_HASHID
from {{ ref('_AIRBYTE_TEST_NORMALIZATION_DEDUP_EXCHANGE_RATE_AB4') }}
-- DEDUP_EXCHANGE_RATE from {{ source('TEST_NORMALIZATION', '_AIRBYTE_RAW_DEDUP_EXCHANGE_RATE') }}
where _airbyte_row_num = 1
