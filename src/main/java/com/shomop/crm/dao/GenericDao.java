package com.shomop.crm.dao;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.shomop.crm.model.Identifier;

public interface GenericDao<T extends Identifier<I>, I extends Serializable> {

	T find(I id);

	List<T> findAll();

	void delete(T obj);
	
	void update(T obj);

	void saveOrUpdate(T obj);

	void saveAndUpdateByJdbc(Collection<? extends T> entities);

	void executeUpdateSqlByJdbc(List<String> sqls);
	
	public List<String[]> getValueByJDBC(String sql);
	
	List<T> find(String[] param ,String [] signs,Object[] values);
    /**
     * 根据配置的xml查询结果,并封装成List对象返回,只能执行查询操作
     * @param sqlId xml中的sql的Id
     * @param obect 包含参数的对象
     * @param clazz 返回结果的类型
     * @return
     */
	public <E extends Object> List<E> find(String sqlId,Object object,Class<E> clazz);
	/**
	 * 更新某个表的某个字段
	 * @param Parmerter map的set里必须有id,tableName
	 */
	public void updateParmerter(Map<String, Object> Parmerter);
	/**
	 * 批量更新
	 * 如果存在选择更新或者忽略
	 * @param entities
	 * @param updateOrIngore 更新或者忽略
	 */
	public void saveOrIgnoreByJdbc(Collection<T> entities,boolean updateOrIngore);
}
