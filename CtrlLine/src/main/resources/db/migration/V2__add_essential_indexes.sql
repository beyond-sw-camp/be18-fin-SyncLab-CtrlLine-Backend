-- alarm
DROP INDEX IF EXISTS idx_alarm_equipment_id ON alarm;
CREATE INDEX idx_alarm_equipment_id ON alarm (equipment_id);

DROP INDEX IF EXISTS idx_alarm_user_id ON alarm;
CREATE INDEX idx_alarm_user_id ON alarm (user_id);

DROP INDEX IF EXISTS idx_alarm_time ON alarm;
CREATE INDEX idx_alarm_time ON alarm (occurred_at, cleared_at);


-- defective
DROP INDEX IF EXISTS idx_defective_equipment_id ON defective;
CREATE INDEX idx_defective_equipment_id ON defective (equipment_id);


-- equipment
DROP INDEX IF EXISTS idx_equipment_line_id ON equipment;
CREATE INDEX idx_equipment_line_id ON equipment (line_id);

DROP INDEX IF EXISTS idx_equipment_status_id ON equipment;
CREATE INDEX idx_equipment_status_id ON equipment (equipment_status_id);

DROP INDEX IF EXISTS idx_equipment_user_id ON equipment;
CREATE INDEX idx_equipment_user_id ON equipment (user_id);


-- item_line
DROP INDEX IF EXISTS idx_item_line_line ON item_line;
CREATE INDEX idx_item_line_line ON item_line (line_id);

DROP INDEX IF EXISTS idx_item_line_item ON item_line;
CREATE INDEX idx_item_line_item ON item_line (item_id);


-- item_serial
DROP INDEX IF EXISTS idx_item_serial_lot ON item_serial;
CREATE INDEX idx_item_serial_lot ON item_serial (lot_id);


-- line
DROP INDEX IF EXISTS idx_line_factory_id ON line;
CREATE INDEX idx_line_factory_id ON line (factory_id);

DROP INDEX IF EXISTS idx_line_user_id ON line;
CREATE INDEX idx_line_user_id ON line (user_id);


-- lot
DROP INDEX IF EXISTS idx_lot_item_id ON lot;
CREATE INDEX idx_lot_item_id ON lot (item_id);

DROP INDEX IF EXISTS idx_lot_production_plan_id ON lot;
CREATE INDEX idx_lot_production_plan_id ON lot (production_plan_id);


-- mes_data
DROP INDEX IF EXISTS idx_mes_data_factory_id ON mes_data;
CREATE INDEX idx_mes_data_factory_id ON mes_data (factory_id);

DROP INDEX IF EXISTS idx_mes_data_factory_created_at ON mes_data;
CREATE INDEX idx_mes_data_factory_created_at ON mes_data (factory_id, created_at);


-- plan_defective
DROP INDEX IF EXISTS idx_plan_defective_plan_id ON plan_defective;
CREATE INDEX idx_plan_defective_plan_id ON plan_defective (production_plan_id);


-- plan_defective_xref
DROP INDEX IF EXISTS idx_plan_def_xref_defective_id ON plan_defective_xref;
CREATE INDEX idx_plan_def_xref_defective_id ON plan_defective_xref (defective_id);

DROP INDEX IF EXISTS idx_plan_def_xref_plan_id ON plan_defective_xref;
CREATE INDEX idx_plan_def_xref_plan_id ON plan_defective_xref (plan_defective_id);


-- production_performance
DROP INDEX IF EXISTS idx_prod_perf_plan_id ON production_performance;
CREATE INDEX idx_prod_perf_plan_id ON production_performance (production_plan_id);

DROP INDEX IF EXISTS idx_prod_perf_time ON production_performance;
CREATE INDEX idx_prod_perf_time ON production_performance (start_time, end_time);


-- production_plan
DROP INDEX IF EXISTS idx_production_plan_item_line ON production_plan;
CREATE INDEX idx_production_plan_item_line ON production_plan (item_line_id);

DROP INDEX IF EXISTS idx_production_plan_sales_mgr ON production_plan;
CREATE INDEX idx_production_plan_sales_mgr ON production_plan (sales_manager_id);

DROP INDEX IF EXISTS idx_production_plan_prod_mgr ON production_plan;
CREATE INDEX idx_production_plan_prod_mgr ON production_plan (production_manager_id);

DROP INDEX IF EXISTS idx_production_plan_status ON production_plan;
CREATE INDEX idx_production_plan_status ON production_plan (production_plan_status);

DROP INDEX IF EXISTS idx_production_plan_due_date ON production_plan;
CREATE INDEX idx_production_plan_due_date ON production_plan (due_date);

DROP INDEX IF EXISTS idx_production_plan_start_end ON production_plan;
CREATE INDEX idx_production_plan_start_end ON production_plan (start_time, end_time);
