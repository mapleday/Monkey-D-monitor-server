package com.sohu.sns.monitor.common.dao.notifyPerson;

import com.sohu.sns.monitor.common.module.NotifyPerson;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * author:jy
 * time:16-12-28下午3:20
 *
 * yw on 2017.2.9
 */
@Repository
public interface NotifyPersonDao {
    /**
     * 查询通知
     */
    public List<NotifyPerson> getAllPerson();
    public List<NotifyPerson> getDutyPersons();
    public List<String> getHasSendMsgPersonNames();
    public List<NotifyPerson> getWaitForDutyPersons();
    public int getMaxDutyGroupNum();
    public void updatePerson(NotifyPerson notifyPerson);
    public void deletePerson(NotifyPerson notifyPerson);
    public void createPerson(NotifyPerson notifyPerson);
}
