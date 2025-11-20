ALTER TABLE IF EXISTS performance_defective
    RENAME TO plan_defective;

ALTER TABLE IF EXISTS plan_defective
    RENAME COLUMN performance_defective_id TO plan_defective_id;
