CREATE TEMPORARY TABLE tmp_duplicate_performance AS
SELECT production_performance_id
FROM (
    SELECT production_performance_id,
           ROW_NUMBER() OVER (PARTITION BY production_plan_id ORDER BY created_at DESC, production_performance_id DESC) AS rn
    FROM production_performance
) ranked
WHERE rn > 1;

DELETE FROM production_performance
WHERE production_performance_id IN (
    SELECT production_performance_id
    FROM tmp_duplicate_performance
);

DROP TABLE tmp_duplicate_performance;

ALTER TABLE production_performance
    ADD CONSTRAINT uq_production_performance_plan
        UNIQUE (production_plan_id);
