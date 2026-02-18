package com.nythicalnorm.planetshine.storage;

public interface IDataSavable<T> {
    boolean isDirty();
    void markDirty(boolean state);
    T getDataToSave();
}