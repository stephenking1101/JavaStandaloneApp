package example.store.cassandra;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select.Where;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import example.store.cassandra.annotation.IndexColumn;
import example.store.cassandra.exception.StoreCassandraException;
import example.store.cassandra.util.CassandraUtil;


/**
* 泛型类
* 声明类的同时声明泛型类型 T
* 1.方法的返回值可以是使用声明的泛型类型
* 2.方法的参数也可以是声明类的泛型类型
* 3.方法体内可以使用泛型类型
*/
public class CassandraObjectMapper<T> extends CassandraTemplate {

	private Logger logger;
    private Mapper<T> mapper;
    private Class<T> clazz;
    protected String tableName;
    private ConsistencyLevel readConsistency;
    private ConsistencyLevel writeConsistency;
    private Map<String, Method> columnGetterMap;
    private List<String> primaryKeyColumns;
    private Map<String, CassandraObjectMapper<T>.IndexColumnInfo> indexNameMap;
    private ListeningExecutorService listeningService;
    final Function<Object, Object> NOOP;
    final Function<ResultSet, T> mapOneFunction;
    final Function<ResultSet, Result<T>> mapAllFunction;

    public CassandraObjectMapper(Class<T> clazz) {
        this(clazz, (SessionFactoryImpl)null);
    }

    public CassandraObjectMapper(Class<T> clazz, SessionFactoryImpl sessionFactory) {
        this.logger = LoggerFactory.getLogger(CassandraObjectMapper.class);
        this.NOOP = Functions.constant((Object)null);
        this.mapOneFunction = new Function<ResultSet, T>() {
            public T apply(ResultSet rs) {
            	/*Mapper#map provides a way to convert the results of a regular query:
            	 *This method will ignore:
                 *  extra columns in the ResultSet that are not mapped for this entity.
            	 *  mapped fields that are not present in the ResultSet (setters won’t be called so the value will be the one after invocation of the class’s default constructor).*/
                return CassandraObjectMapper.this.getMapper().map(rs).one();
            }
        };
        this.mapAllFunction = new Function<ResultSet, Result<T>>() {
        	//Mapper#map returns a Result
        	//Result is similar to ResultSet but for a given mapped class. It provides methods one(), all(), iterator(), getExecutionInfo() and isExhausted(). Note that iterating the Result will consume the ResultSet, and vice-versa.
            public Result<T> apply(ResultSet rs) {
                return CassandraObjectMapper.this.getMapper().map(rs);
            }
        };
        this.clazz = clazz;
        super.setSessionFactory(sessionFactory);
        this.validTableAnnotation();
        this.init();
    }

    private void init() {
        this.logger.debug("init object mapper");
        this.columnGetterMap = Maps.newHashMap();
        this.primaryKeyColumns = Lists.newArrayList();
        this.indexNameMap = Maps.newHashMap();
        Field[] declaredFields = this.clazz.getDeclaredFields();
        Field[] arr$ = declaredFields;
        int len$ = declaredFields.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            Field field = arr$[i$];
            String columnName = field.getName();
            if (field.isAnnotationPresent(Column.class)) {
                columnName = ((Column)field.getAnnotation(Column.class)).name();
            }

            if (field.isAnnotationPresent(IndexColumn.class)) {
                this.columnGetterMap.put(columnName, this.getReadMethod(field));
                boolean isPrimaryKey = false;
                if (field.isAnnotationPresent(ClusteringColumn.class)) {
                    isPrimaryKey = true;
                }

                this.indexNameMap.put(field.getName(), new CassandraObjectMapper.IndexColumnInfo(field.getName(), columnName, isPrimaryKey));
                this.logger.debug("get an index column: {}", columnName);
            }

            if (field.isAnnotationPresent(PartitionKey.class) || field.isAnnotationPresent(ClusteringColumn.class)) {
                this.columnGetterMap.put(columnName, this.getReadMethod(field));
                this.primaryKeyColumns.add(columnName);
                this.logger.debug("get a primary key column: {}", columnName);
            }
        }

        this.listeningService = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
    }

    public void destory() {
        this.logger.debug("destory CassandraObjectMapper");
        CassandraUtil.destoryThreadPool(this.listeningService);
    }

    public void save(T entity) {
    	//Mapper.saveQuery(entity): returns a statement generated by the mapper to save entity into the database
    	//This gives the client a chance to customize the statement before executing it
        Statement saveQuery = this.getSaveQuery(entity);
        this.getSession().execute(saveQuery);
    }

    public void saveWithIndex(T entity) {
        BatchStatement batch = new BatchStatement();
        List<Statement> statements = this.getSaveWithIndexStatements(entity);
        Iterator i$ = statements.iterator();

        while(i$.hasNext()) {
            Statement statement = (Statement)i$.next();
            batch.add(statement);
        }

        if (this.getWriteConsistency() != null) {
            batch.setConsistencyLevel(this.getWriteConsistency());
        }

        this.getSession().execute(batch);
    }

    public List<Statement> getSaveWithIndexStatements(T entity) {
        List<Statement> result = Lists.newArrayList();
        result.add(this.getMapper().saveQuery(entity));
        List<Object> primaryKeyValues = this.getPrimaryKeyValues(entity);
        Iterator i$ = this.indexNameMap.values().iterator();

        while(i$.hasNext()) {
            CassandraObjectMapper<T>.IndexColumnInfo index = (CassandraObjectMapper.IndexColumnInfo)i$.next();
            List<String> columnKeys = Lists.newArrayList(this.primaryKeyColumns);
            List<Object> columnValues = Lists.newArrayList(primaryKeyValues);
            if (!index.isPrimaryKey()) {
                columnKeys.add(index.getColumnName());
                columnValues.add(this.getColumnValue(entity, (Method)this.columnGetterMap.get(index.getColumnName())));
            }

            Insert insertIndexStatement = QueryBuilder.insertInto(this.getIndexTableName(index.getColumnName())).values((String[])columnKeys.toArray(new String[0]), columnValues.toArray());
            result.add(insertIndexStatement);
        }

        return result;
    }

    public ListenableFuture<Object> saveAsync(T entity) {
        Statement saveQuery = this.getSaveQuery(entity);
        return Futures.transform(this.getSession().executeAsync(saveQuery), this.NOOP);
    }

    public Statement getSaveQuery(T entity) {
        Statement saveQuery = this.getMapper().saveQuery(entity);
        if (this.getWriteConsistency() != null) {
            saveQuery.setConsistencyLevel(this.getWriteConsistency());
        }

        return saveQuery;
    }

    public T get(Object... primaryKey) {
    	//Mapper.getQuery(primaryKey): returns a statement to select a row in the database, selected on the given primaryKey, and matching the mapped object structure.
        Statement getQuery = this.getGetQuery(primaryKey);
        return this.getMapper().map(this.getSession().execute(getQuery)).one();
    }

    public ListenableFuture<T> getAsync(Object... primaryKey) {
        this.logger.debug("Async get by primary keys's values:{}", Arrays.toString(primaryKey));
        Statement getQuery = this.getGetQuery(primaryKey);
        return Futures.transform(this.getSession().executeAsync(getQuery), this.mapOneFunction);
    }

    public List<T> getByIndex(String indexField, Object indexValue) {
        List<T> result = Lists.newArrayList();
        CassandraObjectMapper<T>.IndexColumnInfo indexColumn = (CassandraObjectMapper.IndexColumnInfo)this.indexNameMap.get(indexField);
        if (indexColumn == null) {
            throw new IllegalArgumentException(indexField + " is not an index field");
        } else {
            List<ListenableFuture<T>> futureList = Lists.newArrayList();
            Where select = QueryBuilder.select().all().from(this.getIndexTableName(indexColumn.getColumnName())).where(QueryBuilder.eq(indexColumn.getColumnName(), indexValue));
            if (this.getReadConsistency() != null) {
                select.setConsistencyLevel(this.getReadConsistency());
            }

            Iterator i$ = this.getSession().execute(select).iterator();

            while(i$.hasNext()) {
                Row row = (Row)i$.next();
                futureList.add(this.getAsync(this.getPrimaryKeyValues(row)));
            }

            i$ = futureList.iterator();

            while(i$.hasNext()) {
                ListenableFuture future = (ListenableFuture)i$.next();

                try {
                    T entity = (T) future.get();
                    if (this.isIndexValueMatchValueInEntity(entity, indexColumn.getColumnName(), indexValue)) {
                        result.add(entity);
                    }
                } catch (InterruptedException var10) {
                    throw new StoreCassandraException("operation interupted");
                } catch (ExecutionException var11) {
                    throw new StoreCassandraException(var11);
                }
            }

            return result;
        }
    }

    public ListenableFuture<List<T>> getByIndexAsync(final String indexField, final Object indexValue) {
        return this.listeningService.submit(new Callable<List<T>>() {
            public List<T> call() throws Exception {
                return CassandraObjectMapper.this.getByIndex(indexField, indexValue);
            }
        });
    }

    private Statement getGetQuery(Object... primaryKey) {
        Statement getQuery = this.getMapper().getQuery(primaryKey);
        if (this.getReadConsistency() != null) {
            getQuery.setConsistencyLevel(this.getReadConsistency());
        }

        return getQuery;
    }

    public void delete(Object... primaryKey) {
        Statement deleteQuery = this.getDeleleQuery(primaryKey);
        this.getSession().execute(deleteQuery);
    }

    public void deleteWithIndex(Object... primaryKey) {
        T entity = this.get(primaryKey);
        if (null != entity) {
            this.deleteInternal(entity);
        }
    }

    public void deleteWithIndex(T entity) {
        this.deleteInternal(entity);
    }

    public void delete(T entity) {
        Statement deleteQuery = this.getMapper().deleteQuery(entity);
        if (this.getWriteConsistency() != null) {
            deleteQuery.setConsistencyLevel(this.getWriteConsistency());
        }

        this.getSession().execute(deleteQuery);
    }

    public ListenableFuture<Object> deleteAsync(Object... primaryKey) {
        Statement deleteQuery = this.getDeleleQuery(primaryKey);
        return Futures.transform(this.getSession().executeAsync(deleteQuery), this.NOOP);
    }

    public Statement getDeleleQuery(Object... primaryKey) {
        Statement deleteQuery = this.getMapper().deleteQuery(primaryKey);
        if (this.getWriteConsistency() != null) {
            deleteQuery.setConsistencyLevel(this.getWriteConsistency());
        }

        return deleteQuery;
    }

    private boolean isIndexValueMatchValueInEntity(T entity, String indexName, Object indexValue) {
        Object actualValue = this.getColumnValue(entity, (Method)this.columnGetterMap.get(indexName));
        if (!indexValue.equals(actualValue)) {
            this.logger.debug("index [{}] actual value is {}, value in index table is {}", new Object[]{indexName, actualValue, indexValue});
            com.datastax.driver.core.querybuilder.Delete.Where delete = this.getDeleteIndexStatement(entity, indexName, indexValue);
            this.getSession().executeAsync(delete);
            return false;
        } else {
            return true;
        }
    }

    private com.datastax.driver.core.querybuilder.Delete.Where getDeleteIndexStatement(T entity, String indexName, Object indexValue) {
        com.datastax.driver.core.querybuilder.Delete.Where delete = QueryBuilder.delete().from(this.getIndexTableName(indexName)).where(QueryBuilder.eq(indexName, indexValue));
        Iterator i$ = this.primaryKeyColumns.iterator();

        while(i$.hasNext()) {
            String primaryKey = (String)i$.next();
            if (!primaryKey.equals(indexName)) {
                Object columnValue = this.getColumnValue(entity, (Method)this.columnGetterMap.get(primaryKey));
                delete.and(QueryBuilder.eq(primaryKey, columnValue));
            }
        }

        return delete;
    }

    private Object[] getPrimaryKeyValues(Row row) {
        List<Object> result = Lists.newArrayList();
        Iterator i$ = this.primaryKeyColumns.iterator();

        while(i$.hasNext()) {
            String primaryKey = (String)i$.next();
            Object value = row.getObject(primaryKey);
            this.logger.debug("Get primary key: {}:{}", primaryKey, value);
            result.add(value);
        }

        return result.toArray();
    }

    private Object getColumnValue(T entity, Method getMethod) {
        try {
            return entity != null ? getMethod.invoke(entity) : null;
        } catch (Exception var4) {
            throw new StoreCassandraException(var4);
        }
    }

    private Method getReadMethod(Field field) {
        try {
            PropertyDescriptor pd = new PropertyDescriptor(field.getName(), this.clazz);
            return pd.getReadMethod();
        } catch (IntrospectionException var4) {
            throw new StoreCassandraException(var4);
        }
    }

    private void validTableAnnotation() {
        if (!this.clazz.isAnnotationPresent(Table.class)) {
            throw new IllegalArgumentException("Error initialing CassandraObjectMapper because Table annotation missing in class " + this.clazz.getSimpleName());
        } else {
            Table tableAnnotation = (Table)this.clazz.getAnnotation(Table.class);
            this.tableName = tableAnnotation.name();
            if (Strings.isNullOrEmpty(this.tableName)) {
                throw new IllegalArgumentException("Error initialing CassandraObjectMapper because Table name mssing in Table annotation.");
            }
        }
    }

    private List<Object> getPrimaryKeyValues(T entity) {
        List<Object> primaryKeyValues = Lists.newArrayList();
        Iterator i$ = this.primaryKeyColumns.iterator();

        while(i$.hasNext()) {
            String primaryKey = (String)i$.next();
            primaryKeyValues.add(this.getColumnValue(entity, (Method)this.columnGetterMap.get(primaryKey)));
        }

        return primaryKeyValues;
    }

    private void deleteInternal(T entity) {
        BatchStatement batch = new BatchStatement();
        List<Statement> statements = this.getDeleteWithIndexStatements(entity);
        Iterator i$ = statements.iterator();

        while(i$.hasNext()) {
            Statement statement = (Statement)i$.next();
            batch.add(statement);
        }

        if (this.getWriteConsistency() != null) {
            batch.setConsistencyLevel(this.getWriteConsistency());
        }

        this.getSession().execute(batch);
    }

    private List<Statement> getDeleteWithIndexStatements(T entity) {
        List<Statement> result = Lists.newArrayList();
        result.add(this.getMapper().deleteQuery(entity));
        Iterator i$ = this.indexNameMap.values().iterator();

        while(i$.hasNext()) {
            CassandraObjectMapper<T>.IndexColumnInfo index = (CassandraObjectMapper.IndexColumnInfo)i$.next();
            Object indexValue = this.getColumnValue(entity, (Method)this.columnGetterMap.get(index.getColumnName()));
            com.datastax.driver.core.querybuilder.Delete.Where delete = this.getDeleteIndexStatement(entity, index.getColumnName(), indexValue);
            result.add(delete);
        }

        return result;
    }

    public List<Statement> getDeleteWithIndexStatements(Object... primaryKey) {
        T entity = this.get(primaryKey);
        return (List)(null == entity ? Lists.newArrayList() : this.getDeleteWithIndexStatements(entity));
    }

    private Mapper<T> getMapper() {
        if (this.mapper == null) {
            synchronized(this) {
                if (this.mapper == null) {
                	//Each entity class (annotated with @Table) is managed by a dedicated Mapper object. Obtains this object from the MappingManager
                	//Mapper objects are thread-safe. The manager caches them internally, so calling manager#mapper more than once for the same class will return the previously generated mapper.
                    this.mapper = this.getMappingManager().mapper(this.clazz);
                }
            }
        }

        return this.mapper;
    }

    private String getIndexTableName(String indexColumn) {
        return this.tableName + "_by_" + indexColumn;
    }

    public ConsistencyLevel getReadConsistency() {
        return this.readConsistency;
    }

    public void setReadConsistency(ConsistencyLevel readConsistency) {
        this.readConsistency = readConsistency;
    }

    public ConsistencyLevel getWriteConsistency() {
        return this.writeConsistency;
    }

    public void setWriteConsistency(ConsistencyLevel writeConsistency) {
        this.writeConsistency = writeConsistency;
    }

    public String getTableName() {
        return this.tableName;
    }

    private class IndexColumnInfo {
        private String fieldName;
        private String columnName;
        private boolean isPrimaryKey;

        public IndexColumnInfo(String fieldName, String columnName, boolean isPrimaryKey) {
            this.fieldName = fieldName;
            this.columnName = columnName;
            this.isPrimaryKey = isPrimaryKey;
        }

        public String getColumnName() {
            return this.columnName;
        }

        public boolean isPrimaryKey() {
            return this.isPrimaryKey;
        }
    }
}
