package com.nythicalnorm.voxelspaceprogram.storage;

public interface IDataSavable<T> {
    boolean isDirty();
    void markDirty(boolean state);
    T getDataToSave();
}