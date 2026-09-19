package com.app.cutoff.data.repository;
import androidx.lifecycle.LiveData;
import com.app.cutoff.data.database.dao.PlannedItemDao;
import com.app.cutoff.data.database.entity.PlannedItemEntity;
import java.util.List;
import javax.inject.Inject; import javax.inject.Singleton;
@Singleton public class PlannedItemRepository {
 private final PlannedItemDao dao; @Inject public PlannedItemRepository(PlannedItemDao dao){this.dao=dao;}
 public LiveData<List<PlannedItemEntity>> observeForProject(long projectId){return dao.observeForProject(projectId);}
 public void add(long projectId,String name,double amount,boolean first,boolean second,int firstPercent){dao.insert(new PlannedItemEntity(name,amount,1,first,second,projectId,firstPercent));}
 public void update(PlannedItemEntity item){dao.update(item);}
 public void updateAll(List<PlannedItemEntity> items){dao.updateAll(items);}
 public void delete(PlannedItemEntity item){dao.delete(item);}
}
