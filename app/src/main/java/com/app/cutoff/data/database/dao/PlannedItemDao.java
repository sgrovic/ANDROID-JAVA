package com.app.cutoff.data.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.app.cutoff.data.database.entity.PlannedItemEntity;
import java.util.List;

@Dao public interface PlannedItemDao {
    @Insert long insert(PlannedItemEntity item);
    @Delete void delete(PlannedItemEntity item);
    @Update void update(PlannedItemEntity item);
    @Update void updateAll(List<PlannedItemEntity> items);
    @Query("SELECT * FROM planned_item WHERE projectId = :projectId ORDER BY id DESC")
    LiveData<List<PlannedItemEntity>> observeForProject(long projectId);
}
