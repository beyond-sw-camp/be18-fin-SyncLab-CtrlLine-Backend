package com.beyond.synclab.ctrlline.domain.equipment.dto;

/**
 * Runtime status levels that are surfaced to the UI without altering the equipment schema.
 */
public enum EquipmentRuntimeStatusLevel {
    /**
     * Equipment is idle, reset, completed, or otherwise stopped.
     */
    STOPPED,
    /**
     * Equipment is actively running or preparing to run.
     */
    RUNNING,
    /**
     * Equipment is still responding but has a light warning (e.g., STOPPING or INFO/WARN alarm).
     */
    LOW_WARNING,
    /**
     * Equipment reported a severe/critical alarm.
     */
    HIGH_WARNING
}
