package com.shomop.crm.dao.impl;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.jdbc.Work;
import org.springframework.beans.factory.annotation.Autowired;

import com.shomop.crm.dao.GenericDao;
import com.shomop.crm.model.Identifier;
import com.shomop.crm.model.annotation.BatchSaveIgnore;
import com.shomop.util.UUID;

public abstract class GenericDaoImpl<T extends Identifier<I>, I extends Serializable>
        implements GenericDao<T, I> {

    private Class<T> type;

    @Autowired
    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    protected SessionFactory getSessionFactory() {
        if (sessionFactory == null)
            throw new IllegalStateException(
                    "SessionFactory has not been set on DAO before usage");
        return sessionFactory;
    }

    protected Session getCurrentSession() {
        return getSessionFactory().getCurrentSession();
    }

    public Class<T> getType() {
        return type;
    }

    @SuppressWarnings("unchecked")
    public GenericDaoImpl() {
        this.type = (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
    }

    @SuppressWarnings("unchecked")
    @Override
    public T find(I id) {
        return (T) getCurrentSession().get(getType(), id);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<T> findAll() {
        Criteria crit = getCurrentSession().createCriteria(getType());
        return crit.list();
    }

	@Override
	public void update(T obj) {
		
		getCurrentSession().update(obj);
	}
	
    @Override
    public void delete(T obj) {
        getCurrentSession().delete(obj);
    }

    @Override
    public void saveOrUpdate(T obj) {
        getCurrentSession().saveOrUpdate(obj);
    }

    // helper methods
    @SuppressWarnings("rawtypes")
    protected List findByQuery(String queryString, Object...values) {
        Session session = getCurrentSession();
        Query queryObject = session.createQuery(queryString);
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                queryObject.setParameter(i, values[i]);
            }
        }
        return queryObject.list();
    }
    
    @SuppressWarnings("unchecked")
	protected Object findUniqueByQuery(String queryString, Object...values) {
    	List<Object> list = findByQuery(queryString,values);
        if(list.size() >0){
        	return list.get(0);
        }else{
        	return null;
        }
    }
    
    @SuppressWarnings("rawtypes")
    public List findByQueryAndNamedParam(String queryString, String paramName, Object value) {
        return findByQueryAndNamedParams(queryString, new String[] {paramName}, new Object[] {value});
    }

    @SuppressWarnings("rawtypes")
    protected List findByQueryAndNamedParams(String queryString, String[] paramNames, Object[] values) {
        if (paramNames.length != values.length) {
            throw new IllegalArgumentException("Length of paramNames array must match length of values array");
        }
        Session session = getCurrentSession();
        Query queryObject = session.createQuery(queryString);
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                String paramName = paramNames[i];
                Object value = values[i];
                if (value instanceof Collection) {
                    queryObject.setParameterList(paramName, (Collection<?>) value);
				} else if (value instanceof Object[]) {
					queryObject.setParameterList(paramName, (Object[]) value);
				} else if (value instanceof Long) {
					queryObject.setLong(paramName, (Long) value);
				} else {
					queryObject.setParameter(paramName, value);
				}
            }
        }
        return queryObject.list();
    }

    protected int bulkUpdate(String queryString,Object... values) {
        Session session = getCurrentSession();
        Query queryObject = session.createSQLQuery(queryString);
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                queryObject.setParameter(i, values[i]);
            }
        }
        return queryObject.executeUpdate();
    }
    
    protected void saveOrUpdateAll(Collection<? extends T> entities) {
        Session session = getCurrentSession();
        for (T entity : entities) {
            session.saveOrUpdate(entity);
        }
    }
    
    protected void saveAll(Collection<? extends T> entities) {
        Session session = getCurrentSession();
        for (T entity : entities) {
            session.save(entity);
        }
    }
    /**
     * 批量插入或更新,存在更新,不存在插入
     * 如果更新，进行字段拷贝覆盖
     * @param entities
     * @param param 判断数据是否重复的字段
     * @throws NoSuchFieldException 
     * @throws SecurityException 
     * @throws IllegalAccessException 
     * @throws IllegalArgumentException 
     */
    protected void saveOrUpdateAll(Collection<? extends T> entities,String[] param) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        if(entities == null || entities.size() == 0){
        	return ;
        }
    	Session session = getCurrentSession();
        for (T entity : entities) {
        	Object[] arrayObject = new Object[param.length];
        	for(int i=0; i<param.length; i++){
        		Field field = entity.getClass().getDeclaredField(param[i]);
        		field.setAccessible(true);
        		arrayObject[i] =  field.get(entity);
        	}
        	List<T> list = findByParams(param,null,arrayObject);
        	if(list == null || list.size() == 0){
        		  session.saveOrUpdate(entity);
        	}else{
        		T object = list.get(0);
        		copyProperty(object,entity);
        	    session.saveOrUpdate(object);
        	}
        }
    }
    /**
     * 
     * 根据条件查询List
     * @param param 参数名
     * @param signs  符号
     * @param values 值
     * 例: List<User> findByParams(new String[]{"mktBalance","id"},new String[]{">","="},new Object[]{0,"123456"})
     * 实际sql 为 from User a where a.mktBalance > 0 and a.id = '123456'
     * @return
     */
    @SuppressWarnings("unchecked")
    protected  List<T> findByParams(String[] param, String [] signs,Object[] values){
       	if(param == null || param.length == 0){
            throw new IllegalArgumentException("Length of param array should not null.");
    	}
    	String sql = getSql(param,signs);
    	return (List<T>)findByQueryAndNamedParams( sql,  param,  values) ;
    }
    
    private String getSql(String[] param, String [] signs){
        // 符号为空要默认为"="？
		if (signs == null) {
			signs = new String[param.length];
			for (int i = 0; i < param.length; i++) {
				signs[i] = "=";
			}
		}
    	StringBuilder sb = new StringBuilder();
    	sb.append("from ").append(type.getSimpleName()).append(" a where ");
    	for(int i=0;i<param.length ;i++){
    		sb.append(" a.").append(param[i]).append(signs[i]).append(":").append(param[i]);
    		if(i+1 != param.length){
    			sb.append(" and ");
    		}
    	}
    	return sb.toString();
    }
    /**
     * 
     * 有默认值且为默认值的不进行拷贝.
     * @param t1
     * @param t2
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    private void copyProperty(T t1,T t2) throws IllegalArgumentException, IllegalAccessException{
    	Field[] fields = t1.getClass().getDeclaredFields();
    	for(Field field :fields){
    		if(field.getModifiers() != 2)
    			continue;
    		field.setAccessible(true);
    		Object object = field.get(t2);
    		if(object == null)
    			continue;
    		if(object instanceof Integer){
    			if((Integer)object != 0)
    			field.set(t1, object);
    		}else if(object instanceof Long){
    			if((Long)object != 0l)	
    			field.set(t1, object);
        	}else if(object instanceof String){
        		if(!"".equals((String)object)){
        			field.set(t1, object);
        		}
        	}else if(object instanceof Boolean){
        		if(((Boolean)object).booleanValue() == true){
        			field.set(t1, object);
        		}
        	}else{
        		field.set(t1, object);
        	}
    	}
    }
    /**
     * 批量保存,如果存在该记录用当前属性跟新(替换所有字段).如果不存在则插入
     * 判断是否存在根据唯一索引
     */
	public void saveAndUpdateByJdbc(Collection<? extends T> entities){
    		if(entities == null || entities.size() == 0)
    			return;
        	final Collection<? extends T> beanList = entities;
        	Session session = getCurrentSession();
            session.doWork(new Work() {
			public void execute(Connection connection) throws SQLException {
				String sql = createSql(beanList.iterator().next());
				PreparedStatement stmt = connection.prepareStatement(sql,ResultSet.TYPE_SCROLL_SENSITIVE,
						ResultSet.CONCUR_READ_ONLY);
				for (T bean : beanList) {
					setValue(stmt, bean);
				}
				stmt.executeBatch();
			}
		});
    }
	
	/**
	 * 批量保存
	 * 根据唯一索引判断
	 * 如果存在记录则更新或者忽略
	 * @param entities
	 * @param updateOrIngore true 更新
	 * 						 false 忽略
	 */
	public void saveOrIgnoreByJdbc(Collection<T> entities,boolean updateOrIngore) {
		if (entities == null || entities.size() == 0) {
			return;
		}
		final Collection<T> beanList = entities;
		final boolean update = updateOrIngore;
		Session session = getCurrentSession();
		session.doWork(new Work() {
			public void execute(Connection connection) throws SQLException {
				List<Field> fields = fieldSOfType();
				String sql = createSqlByBean(fields,update);
//				System.out.println("batch sql： "+sql);
				PreparedStatement stmt = connection.prepareStatement(sql,
						ResultSet.TYPE_SCROLL_SENSITIVE,
						ResultSet.CONCUR_READ_ONLY);
				for (T bean : beanList) {
					assignValueForSql(stmt,bean,fields,update);
					stmt.addBatch();
				}
				stmt.executeBatch();
				/*int[] result = stmt.executeBatch();
				for (int i = 0; i < result.length; i++) {
					System.out.println(result[i]);
				}*/
			}
		});
	}
	/**
	 * 根据{@link BatchSaveIgnore} ingoreLevel
	 * 获取需要拼接sql的属性(插入不忽略的所有字段)
	 * 默认所有私有属性(主键在最后)
	 * @param bean
	 * @return
	 */
	private List<Field> fieldSOfType() {
		Field[] fields = type.getDeclaredFields();
		List<Field> fieldList = new ArrayList<Field>();
		Field idField = null;
    	for(Field field : fields){
    		if(field.getModifiers() != Modifier.PRIVATE){
 				continue;
 			}
    		// 主键
    		Id id = field.getAnnotation(Id.class);
    		// 如果使用'id'作为主键名称，则不需要在属性上添加@Id注解
 			if (id != null || field.getName().equals(BatchSaveIgnore.defaultIdName)) {
 				idField = field;
 				fieldList.add(idField);
 				continue;
 			}
 			// 除了跳过字段，其他私有属性拼接sql
 			BatchSaveIgnore ignoreField = field.getAnnotation(BatchSaveIgnore.class);
 			if (ignoreField == null || ignoreField.ingoreLevel() != BatchSaveIgnore.all) {
 				field.setAccessible(true);
    			fieldList.add(field);
 			}
    	}
    	// 现有@Id注解都在方法上
//    	if(idField == null){
//          Method[] mothods = type.getDeclaredMethods();
//			for (Method method : mothods) {
//				Id id = method.getAnnotation(Id.class);
//				if (id != null) {
//					String idString = method.getName().replace("get", "");
//					idString = idString.substring(0,1).toLowerCase() + idString.substring(1);
//					try {
//						Field temp = type.getDeclaredField(idString);
//						idField = temp;
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//					break;
//				}
//			}
//    	}
    	if(idField == null){
    		throw new IllegalStateException(type.getAnnotation(Table.class).name()+" table has not a primary key");
    	}
    	return fieldList;
	}
    
	/**
	 * 简单的结果查询,每一条记录一个数组,第一个条记录里记录列名,第二条开始记录数据.
	 * 如果无查询结果,则只有一条记录(记录数大于10000会报错)
	 */
	public List<String[]> getValueByJDBC(String sql){
		final String executeSql = sql;
		final List<String[]> valueList = new ArrayList<String[]>();
    	Session session = getCurrentSession();
        session.doWork(new Work() {
			public void execute(Connection connection) throws SQLException {
				PreparedStatement stmt =  connection.prepareStatement(executeSql);  
				ResultSet rs = stmt.executeQuery(executeSql);
					getReult(rs,valueList);
			}
        });
        return valueList;
	}
	

	
	/**
	 * @param rs
	 * @return
	 */
	private void getReult(ResultSet rs, List<String[]> valueList ){
		ResultSetMetaData rsmd=null;
		try {
			rsmd=rs.getMetaData();
			int columnLength = rsmd.getColumnCount();
			String[] columns = new String[columnLength]; 
			for(int i=1;i<=columnLength;i++){
				columns[i-1] = rsmd.getColumnName(i);
			}
			valueList.add(columns);
			 while(rs.next()){  
				String[] values = new String[columnLength];
				for (int i = 1; i <= columnLength; i++) {
					int type=rsmd.getColumnType(i);
					String str= "";
					switch(type){
					case Types.SMALLINT:
					case Types.NUMERIC:
					case Types.INTEGER:str=str+rs.getInt(i);break;
					case Types.BIGINT:str=str+rs.getLong(i);break;
					case Types.DOUBLE:
					case Types.FLOAT:
					case Types.DECIMAL:str=str+rs.getDouble(i);break;
					case Types.CHAR:
					case Types.VARCHAR:str=rs.getString(i);break;
					case Types.DATE:str=str+rs.getDate(i);break;
					default:str=rs.getString(i);break;
					}
				    values[i-1] = str;
				}
				valueList.add(values);
			 }
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 执行原生的sql
	 */
	 public void executeUpdateSqlByJdbc(List<String> sqls){
		 if(sqls == null || sqls.size() == 0){
			 return ;
		 }
		 final List<String> sqlList = sqls;
		 Session session = getCurrentSession();
         session.doWork(new Work() {
 		 public void execute(Connection connection) throws SQLException {
 			Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
 			for (String sql : sqlList) {
 				 stmt.addBatch(sql);
 			 }	
 	    	  stmt.executeBatch();
 			}
 		});
	 }
    private String createSql(T bean){
    	StringBuilder sb = new StringBuilder();
    	String name = bean.getClass().getAnnotation(javax.persistence.Table.class).name();
    	//默认所有实体类主键为id
    	StringBuilder columns = new StringBuilder().append("id");
    	StringBuilder values = new StringBuilder().append("?");
    	StringBuilder updateValues = new StringBuilder();
    	Field[] fields = bean.getClass().getDeclaredFields();
    	int j=0;
    	for(int i=0; i<fields.length ;i++){
    	    BatchSaveIgnore batchSaveIgnore = fields[i].getAnnotation(BatchSaveIgnore.class);
    		if(fields[i].getModifiers() == 2&&!"id".equals(fields[i].getName()) && batchSaveIgnore == null){
    			if(j != 0){
    				updateValues.append(",");
    			}
    			columns.append(",").append(fields[i].getName());
    			values.append(",").append("?");
    			updateValues.append(fields[i].getName()).append("=").append("?");
    			j++;
    		}
    	}
    	sb.append("insert into ").append(name).append(" ( ").append(columns).append(" ) VALUES ( ").append(values);
    	sb.append(" ) on duplicate key update ").append(updateValues);
    	return sb.toString();
    }
    /**
     * 根据属性、表名构造sql
     * 属性不包括主键
     * @param fields
     * @return
     */
    private String createSqlByBean(List<Field> fields,boolean update){
    	// 表名
    	String tableName = type.getAnnotation(Table.class).name();
    	StringBuilder sb = new StringBuilder();
    	// insert sql
    	StringBuilder columns = new StringBuilder();
    	StringBuilder values = new StringBuilder();
    	// update sql
    	StringBuilder updateInfo = new StringBuilder();
    	int i = 0;
    	int j = 0;
		for (Field field : fields) {
			if (j != 0) {
				columns.append(",");
				values.append(",");
			}
			j++;
			columns.append(field.getName());
			values.append("?");
			if (update) {
				// 主键更新默认忽略
				Id id = field.getAnnotation(Id.class);
				if(id != null || field.getName().equals(BatchSaveIgnore.defaultIdName)){
					continue;
				}
				BatchSaveIgnore ignoreField = field.getAnnotation(BatchSaveIgnore.class);
				if (ignoreField == null || ignoreField.ingoreLevel() != BatchSaveIgnore.update) {
					if (i != 0) {
						updateInfo.append(",");
					}
					updateInfo.append(field.getName()).append("=").append("?");
					i++;
				}
			}
		}
		if (update) {
			sb.append("INSERT INTO ").append(tableName).append(" ( ")
					.append(columns).append(" ) VALUES ( ").append(values)
					.append(" ) ON DUPLICATE KEY UPDATE ").append(updateInfo);
		} else {
			sb.append("INSERT IGNORE INTO ").append(tableName).append("(")
					.append(columns).append(") VALUES (").append(values)
					.append(")");
		}
    	return sb.toString();
    }
    
    private void setValue(PreparedStatement stmt ,T bean) throws SQLException{
    	Field[] fields = bean.getClass().getDeclaredFields();
    	List<Field> fieldList = new ArrayList<Field>();
    	for(Field field : fields){
    	    BatchSaveIgnore batchSaveIgnore = field.getAnnotation(BatchSaveIgnore.class);
    		if(field.getModifiers() == Modifier.PRIVATE &&!"id".equals(field.getName())&&batchSaveIgnore == null){
    			field.setAccessible(true);
    			fieldList.add(field);
    		}
    	}
    	stmt.setString(1, UUID.generate().toString());
    	for(int i=0; i<fieldList.size(); i++){
    		try {
				Object object = fieldList.get(i).get(bean);
				if(object instanceof Integer){
					stmt.setInt(i+2, (Integer)object);
					stmt.setInt(i+2+fieldList.size(), (Integer)object);
				}else if(object instanceof Long){
					stmt.setLong(i+2, (Long)object);
					stmt.setLong(i+2+fieldList.size(), (Long)object);
				}else if(object instanceof Boolean){
					stmt.setBoolean(i+2, ( Boolean)object);
					stmt.setBoolean(i+2+fieldList.size(), (Boolean)object);
				}else if(object instanceof Date){
					stmt.setLong(i+2, ((Date)object).getTime());
					stmt.setLong(i+2+fieldList.size(), ((Date)object).getTime());
				}else{
					stmt.setObject(i+2, object);
					stmt.setObject(i+2+fieldList.size(), object);
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
    	}
		stmt.addBatch();
    }
    
	private void assignValueForSql(PreparedStatement stmt,T bean,List<Field> fields,boolean update) throws SQLException {
		// 偏移量
		int offset = fields.size();
		int index = 0;
		for (int i = 0; i < offset; i++) {
			try {
				Field field = fields.get(i);
				// 主键默认uuid
				if(field.getAnnotation(Id.class) != null || 
						field.getName().equals(BatchSaveIgnore.defaultIdName)){
					stmt.setObject(i+1, UUID.generate().toString());
					continue;
				}
				Object object = field.get(bean);
				if (object instanceof Date) {
					object = Long.valueOf(((Date) object).getTime());
				}
				stmt.setObject(i+1, object);
				if(update){
				   BatchSaveIgnore ignoreField = field.getAnnotation(BatchSaveIgnore.class);
					if (ignoreField == null || ignoreField.ingoreLevel() != BatchSaveIgnore.update) {
						stmt.setObject(offset + index +1, object);
						index++;
					}
				}
				
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}
    
    public List<T> find(String[] param ,String [] signs,Object[] values){
        return  findByParams(param, signs, values);
    }
    
	/**
	 *根据sql查询结果,并封装成List对象返回,只能执行查询操作,基本类型为空时值为-1
	 */
    public  <E extends Object> List<E> getValueByJDBC(String sql,Class<E> clazz){
//    	System.out.println(sql);
        final List<E> valueList = new ArrayList<E>();
        final List<E> allValue = new ArrayList<E>();
        boolean limit = true;
	    while (limit) {
            final String executeSql ;
            valueList.clear();
	        if(sql.indexOf("limit") >-1){
	            limit=false;
	            executeSql = sql;
	        }else {
	            executeSql = sql+" limit "+allValue.size()+","+8888;
            }
	        final Class<E>  finalClazz = clazz;
	        if (sql.indexOf("select") <0)
	            return valueList;
	        Session session = getCurrentSession();
	        session.doWork(new Work() {
	            public void execute(Connection connection) throws SQLException {
	                PreparedStatement stmt =  connection.prepareStatement(executeSql);  
	                ResultSet rs = stmt.executeQuery(executeSql);
	                    try {
	                        getReult(rs,valueList,finalClazz);
	                    } catch (SecurityException e) {
	                        e.printStackTrace();
	                    } catch (NoSuchFieldException e) {
	                        e.printStackTrace();
	                    } catch (InstantiationException e) {
	                        e.printStackTrace();
	                    } catch (IllegalAccessException e) {
	                        e.printStackTrace();
	                    }
	            }
	        });
	        if(valueList.size() != 8888){
	            limit = false;
	        }
	        if(valueList != null){
	            for (int i = 0; i < valueList.size(); i++) {
	                allValue.add(valueList.get(i));
	            }
	        }
        }
        return allValue;
	}
	
	/**
	 * @param rs
	 * @return
	 * @throws NoSuchFieldException 
	 * @throws SecurityException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	private <E extends Object>  void  getReult(ResultSet rs,  List<E> valueList,Class<E> clazz) throws SecurityException, NoSuchFieldException, InstantiationException, IllegalAccessException{
		ResultSetMetaData rsmd=null;
		try {
			rsmd=rs.getMetaData();
			int columnLength = rsmd.getColumnCount();
			Field[] columns = new Field[columnLength]; 
			for(int i=1;i<=columnLength;i++){
				columns[i-1] = clazz.getDeclaredField(rsmd.getColumnLabel(i));
				columns[i-1].setAccessible(true);
			}
			 while(rs.next()){  
				E object = clazz.newInstance();
				for (int i = 1; i <= columnLength; i++) {
				    Field field = columns[i-1];
				    if(rs.getObject(i) == null &&  !field.getType().isPrimitive()){
				    	continue;
				    }else if(rs.getObject(i) == null &&  field.getType().isPrimitive()){
				        if(field.getType().getSimpleName().equals("Boolean")||field.getType().getSimpleName().equals("boolean")){
				            field.set(object, false);
				        }else {
	                        field.set(object, -1);
                        }
				    }else if(field.getType().getSimpleName().equals("Date")){
	                    field.set(object, new Date((Long)(rs.getObject(i))));
				    }else if(rs.getObject(i) instanceof BigDecimal){
				        field.set(object, ((BigDecimal)rs.getObject(i)).longValue());
				    }else if(rs.getObject(i) instanceof BigInteger){
                        field.set(object, ((BigInteger)rs.getObject(i)).intValue());
                    }else {
				    	field.set(object, rs.getObject(i));
				    }
				}
				valueList.add(object);
			 }
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
    @Override
    public void updateParmerter(Map<String, Object> Parmerter) {
        if(Parmerter == null){
            return;
        }
        final String id = (String)Parmerter.get("id");
        final String tableName = (String)Parmerter.get("tableName");
        if(id == null || tableName == null){
            return;
        }else {
            Parmerter.remove("id");
            Parmerter.remove("tableName");
        }
        final Map<String, Object> map = Parmerter;
        Set<String> set = Parmerter.keySet();
        final Object[] parmerters = set.toArray();
        if(parmerters.length <1){
            return;
        }
        Session session = getCurrentSession();
        session.doWork(new Work() {
        public void execute(Connection connection) throws SQLException {
            StringBuilder sb = new StringBuilder().append("update ").append(tableName).append(" set ");
            for (int i = 1; i <= parmerters.length; i++) {
                sb.append(parmerters[0]).append("=").append("?");
                if(parmerters.length   != i){
                    sb.append(",");
                }
            }
            sb.append(" where id='").append(id).append("'");
            PreparedStatement stmt = connection.prepareStatement(sb.toString(),ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            for (int i = 1; i <= parmerters.length; i++) {
                Object object = map.get(parmerters[i-1]);
                if(object instanceof Integer){
                    stmt.setInt(i, (Integer)object);
                }else if(object instanceof Long){
                    stmt.setLong(i, (Long)object);
                }else if(object instanceof Boolean){
                    stmt.setBoolean(i, ( Boolean)object);
                }else if(object instanceof Date){
                    stmt.setLong(i, ((Date)object).getTime());
                }else{
                    stmt.setObject(i, object);
                }
            }
            stmt.addBatch();
            stmt.executeBatch();
        }
        });
    }
}