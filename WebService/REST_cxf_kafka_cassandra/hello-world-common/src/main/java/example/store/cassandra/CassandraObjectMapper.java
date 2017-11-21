package example.store.cassandra;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
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

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;


/**
* 泛型类
* 声明类的同时声明泛型类型 T
* 1.方法的返回值可以是使用声明的泛型类型
* 2.方法的参数也可以是声明类的泛型类型
* 3.方法体内可以使用泛型类型
*/
public class CassandraObjectMapper<T> extends CassandraTemplate {

    private Logger logger = LoggerFactory.getLogger(CassandraObjectMapper.class);
    private Mapper<T> mapper;
    private Class<T> clazz;

    protected String tableName;
    // can be set in runtime
    private ConsistencyLevel readConsistency;
    private ConsistencyLevel writeConsistency;
    //store the getter method of the column
    private Map<String, Method> columnGetterMap;
    private List<String> primaryKeyColumns;
    //store the field name and the column name, key is field name in clazz, value is the column name in the table
    private Map<String, IndexColumnInfo> indexNameMap;

    //ExecutorService to execute cassandra commands asynchronous
    private ListeningExecutorService listeningService;

    final Function<Object, Void> NOOP = Functions.<Void> constant(null);

    final Function<ResultSet, T> mapOneFunction = new Function<ResultSet, T>() {
        @Override
        public T apply(ResultSet rs) {
			/*Mapper#map provides a way to convert the results of a regular query:
            	 *This method will ignore:
                 *  extra columns in the ResultSet that are not mapped for this entity.
            	 *  mapped fields that are not present in the ResultSet (setters won’t be called so the value will be the one after invocation of the class’s default constructor).*/
            return CassandraObjectMapper.this.getMapper().map(rs).one();
        }
    };
    final Function<ResultSet, Result<T>> mapAllFunction = new Function<ResultSet, Result<T>>() {
        @Override
        public Result<T> apply(ResultSet rs) {
			//Mapper#map returns a Result
        	//Result is similar to ResultSet but for a given mapped class. It provides methods one(), all(), iterator(), getExecutionInfo() and isExhausted(). Note that iterating the Result will consume the ResultSet, and vice-versa.
            return CassandraObjectMapper.this.getMapper().map(rs);
        }
    };





    public CassandraObjectMapper(Class<T> clazz) {
        this(clazz, null);
    }

    public CassandraObjectMapper(Class<T> clazz, SessionFactoryImpl sessionFactory) {
        this.clazz = clazz;
        super.setSessionFactory(sessionFactory);
        validTableAnnotation();
        init();
    }

    private void init() {
        //init primary column and index column
        logger.debug("init object mapper");
        columnGetterMap = Maps.newHashMap();
        primaryKeyColumns = Lists.newArrayList();
        indexNameMap = Maps.newHashMap();

        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {

            String columnName = field.getName();
            if (field.isAnnotationPresent(Column.class)) {
                columnName = field.getAnnotation(Column.class).name();
            }
            if (field.isAnnotationPresent(IndexColumn.class)) {
                columnGetterMap.put(columnName, getReadMethod(field));
                boolean isPrimaryKey = false;
                if (field.isAnnotationPresent(ClusteringColumn.class)) {
                    isPrimaryKey = true;
                }
                indexNameMap.put(field.getName(), new IndexColumnInfo(field.getName(), columnName, isPrimaryKey));
                logger.debug("get an index column: {}", columnName);
            }
            if (field.isAnnotationPresent(PartitionKey.class) || field.isAnnotationPresent(ClusteringColumn.class)) {
                columnGetterMap.put(columnName, getReadMethod(field));
                primaryKeyColumns.add(columnName);
                logger.debug("get a primary key column: {}", columnName);
            }

        }

        listeningService = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
    }

    public void destory() {
        logger.debug("destory CassandraObjectMapper");
        CassandraUtil.destoryThreadPool(listeningService);
    }


    /*
     * ===========================================
     *
     * save methods: save/saveWithIndex/saveAsync
     *
     * ===========================================
     */

    /**
     * save entity
     *
     * @param entity
     */
    public void save(T entity) {
		//Mapper.saveQuery(entity): returns a statement generated by the mapper to save entity into the database
    	//This gives the client a chance to customize the statement before executing it
        Statement saveQuery = getSaveQuery(entity);
        this.getSession().execute(saveQuery);
    }

    /**
     * save the entity and index table at the same time the table name
     * should be <main_table_name>_by_<index_column>
     *
     * @param entity
     */
    public void saveWithIndex(T entity) {
        //use batch statement to save entity and its indexes in transaction
        BatchStatement batch = new BatchStatement();

        //save the entity
        List<Statement> statements = getSaveWithIndexStatements(entity);
        for (Statement statement : statements) {
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
        List<Object> primaryKeyValues = getPrimaryKeyValues(entity);
        //add all index
        for (IndexColumnInfo index : indexNameMap.values()) {
            List<String> columnKeys = Lists.newArrayList(primaryKeyColumns);
            List<Object> columnValues = Lists.newArrayList(primaryKeyValues);
            if (!index.isPrimaryKey()) {
                columnKeys.add(index.getColumnName());
                columnValues.add(getColumnValue(entity, columnGetterMap.get(index.getColumnName())));
            }
            Insert insertIndexStatement = QueryBuilder.insertInto(getIndexTableName(index.getColumnName()))
                    .values(columnKeys.toArray(new String[0]), columnValues.toArray());
            result.add(insertIndexStatement);
        }
        return result;
    }

    public ListenableFuture<Void> saveAsync(T entity) {
        Statement saveQuery = getSaveQuery(entity);
        return Futures.transform(this.getSession().executeAsync(saveQuery), NOOP);
    }

    public Statement getSaveQuery(T entity) {
        Statement saveQuery = this.getMapper().saveQuery(entity);
        if (this.getWriteConsistency() != null) {
            saveQuery.setConsistencyLevel(this.getWriteConsistency());
        }
        return saveQuery;
    }

    /*
     * ===========================================
     *
     * get methods: get/getByIndex/getByIndexAync
     *
     * ===========================================
     */

    public T get(Object... primaryKey) {
        //Mapper.getQuery(primaryKey): returns a statement to select a row in the database, selected on the given primaryKey, and matching the mapped object structure.
        Statement getQuery = getGetQuery(primaryKey);
        return this.getMapper().map(this.getSession().execute(getQuery)).one();

    }

    public ListenableFuture<T> getAsync(Object... primaryKey) {
        logger.debug("Async get by primary keys's values:{}", Arrays.toString(primaryKey));
        Statement getQuery = getGetQuery(primaryKey);
        return Futures.transform((this.getSession().executeAsync(getQuery)), mapOneFunction);

    }

    public List<T> getByIndex(String indexField, Object indexValue) {
        List<T> result = Lists.newArrayList();

        IndexColumnInfo indexColumn = indexNameMap.get(indexField);
        if (indexColumn == null) {
            throw new IllegalArgumentException(indexField + " is not an index field");
        }

        List<ListenableFuture<T>> futureList = Lists.newArrayList();

        //get the primary keys's value by index
        com.datastax.driver.core.querybuilder.Select.Where select = QueryBuilder.select().all()
                .from(getIndexTableName(indexColumn.getColumnName()))
                .where(QueryBuilder.eq(indexColumn.getColumnName(), indexValue));

        if (this.getReadConsistency() != null) {
            select.setConsistencyLevel(this.getReadConsistency());
        }
        for (Row row : getSession().execute(select)) {
            futureList.add(this.getAsync(getPrimaryKeyValues(row)));
        }

        /*
         * Since we won't update the index table while the index
         * column is updated in the original table there may exist
         * some obsolete index If the value of the index column in the
         * original table doesn't match the indexValue delete this
         * index
         */
        for (ListenableFuture<T> future : futureList) {
            try {
                T entity = future.get();

                if (isIndexValueMatchValueInEntity(entity, indexColumn.getColumnName(), indexValue)) {
                    result.add(entity);
                }
            } catch (InterruptedException e) {
                throw new StoreCassandraException("operation interupted");
            } catch (ExecutionException e) {
                throw new StoreCassandraException(e);
            }

        }
        return result;
    }

    public ListenableFuture<List<T>> getByIndexAsync(final String indexField, final Object indexValue) {
        return listeningService.submit(new Callable<List<T>>() {

            @Override
            public List<T> call() throws Exception {
                return getByIndex(indexField, indexValue);
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





    /*
     * ===========================================
     *
     * delete methods: delete/deleteWithIndex
     *
     * ===========================================
     */

    public void delete(Object... primaryKey) {
        Statement deleteQuery = getDeleleQuery(primaryKey);
        this.getSession().execute(deleteQuery);
    }


    public void deleteWithIndex(Object... primaryKey) {
        T entity = this.get(primaryKey);
        if (null == entity) {
            return;
        }
        deleteInternal(entity);
    }

    public void deleteWithIndex(T entity) {
        deleteInternal(entity);
    }

    public void delete(T entity) {
        Statement deleteQuery = this.getMapper().deleteQuery(entity);
        if (this.getWriteConsistency() != null) {
            deleteQuery.setConsistencyLevel(this.getWriteConsistency());
        }
        this.getSession().execute(deleteQuery);
    }

    public ListenableFuture<Void> deleteAsync(Object... primaryKey) {
        Statement deleteQuery = getDeleleQuery(primaryKey);

        return Futures.transform(this.getSession().executeAsync(deleteQuery), NOOP);
    }

    public Statement getDeleleQuery(Object... primaryKey) {
        Statement deleteQuery = this.getMapper().deleteQuery(primaryKey);
        if (this.getWriteConsistency() != null) {
            deleteQuery.setConsistencyLevel(this.getWriteConsistency());
        }
        return deleteQuery;
    }

    private boolean isIndexValueMatchValueInEntity(T entity, String indexName, Object indexValue) {
        Object actualValue = getColumnValue(entity, columnGetterMap.get(indexName));
        if (!indexValue.equals(actualValue)) {
            logger.debug("index [{}] actual value is {}, value in index table is {}", indexName, actualValue,
                    indexValue);
            //the index is obsolete, delete the index asynchronously
            com.datastax.driver.core.querybuilder.Delete.Where delete = getDeleteIndexStatement(entity, indexName,
                    indexValue);

            getSession().executeAsync(delete);
            return false;

        }
        return true;
    }

    private com.datastax.driver.core.querybuilder.Delete.Where getDeleteIndexStatement(T entity, String indexName,
            Object indexValue) {
        com.datastax.driver.core.querybuilder.Delete.Where delete = QueryBuilder.delete()
                .from(getIndexTableName(indexName)).where(QueryBuilder.eq(indexName, indexValue));
        for (String primaryKey : primaryKeyColumns) {
            if (!primaryKey.equals(indexName)) {
                Object columnValue = getColumnValue(entity, columnGetterMap.get(primaryKey));
                delete.and(QueryBuilder.eq(primaryKey, columnValue));
            }
        }
        return delete;
    }

    private Object[] getPrimaryKeyValues(Row row) {
        List<Object> result = Lists.newArrayList();
        for (String primaryKey : primaryKeyColumns) {
            Object value = row.getObject(primaryKey);
            logger.debug("Get primary key: {}:{}", primaryKey, value);
            result.add(value);
        }
        return result.toArray();
    }

    private Object getColumnValue(T entity, Method getMethod) {
        try {
            if (entity != null) {
                return getMethod.invoke(entity);
            }
            return null;
        } catch (Exception e) {
            throw new StoreCassandraException(e);
        }
    }

    private Method getReadMethod(Field field) {
        PropertyDescriptor pd;
        try {
            pd = new PropertyDescriptor(field.getName(), clazz);
            return pd.getReadMethod();
        } catch (IntrospectionException e) {
            throw new StoreCassandraException(e);
        }
    }

    private void validTableAnnotation() {
        if (!this.clazz.isAnnotationPresent(Table.class)) {
            throw new IllegalArgumentException(
                    "Error initialing CassandraObjectMapper because Table annotation missing in class "
                            + clazz.getSimpleName());
        }

        Table tableAnnotation = clazz.getAnnotation(Table.class);
        this.tableName = tableAnnotation.name();
        if (Strings.isNullOrEmpty(this.tableName)) {
            throw new IllegalArgumentException(
                    "Error initialing CassandraObjectMapper because Table name mssing in Table annotation.");
        }
    }

    private List<Object> getPrimaryKeyValues(T entity) {
        List<Object> primaryKeyValues = Lists.newArrayList();
        for (String primaryKey : primaryKeyColumns) {
            primaryKeyValues.add(getColumnValue(entity, columnGetterMap.get(primaryKey)));

        }
        return primaryKeyValues;
    }

    private void deleteInternal(T entity) {
        //use batch statement to DELETE entity and its indexes in transaction
        BatchStatement batch = new BatchStatement();
        //save the entity
        List<Statement> statements = getDeleteWithIndexStatements(entity);
        for (Statement statement : statements) {
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
        for (IndexColumnInfo index : indexNameMap.values()) {
            Object indexValue = getColumnValue(entity, columnGetterMap.get(index.getColumnName()));
            com.datastax.driver.core.querybuilder.Delete.Where delete = getDeleteIndexStatement(entity,
                    index.getColumnName(), indexValue);
            result.add(delete);
        }
        return result;
    }

    public List<Statement> getDeleteWithIndexStatements(Object... primaryKey) {
        T entity = this.get(primaryKey);
        if (null == entity) {
            return Lists.newArrayList();
        }
        return getDeleteWithIndexStatements(entity);
    }

    private Mapper<T> getMapper() {
        if (this.mapper == null) {
            synchronized (this) {
                if (this.mapper == null) {
					//Each entity class (annotated with @Table) is managed by a dedicated Mapper object. Obtains this object from the MappingManager
                	//Mapper objects are thread-safe. The manager caches them internally, so calling manager#mapper more than once for the same class will return the previously generated mapper.
                    this.mapper = getMappingManager().mapper(clazz);
                }
            }
        }
        return this.mapper;
    }

    private String getIndexTableName(String indexColumn) {
        return this.tableName + "_by_" + indexColumn;
    }

    public ConsistencyLevel getReadConsistency() {
        return readConsistency;
    }

    public void setReadConsistency(ConsistencyLevel readConsistency) {
        this.readConsistency = readConsistency;
    }

    public ConsistencyLevel getWriteConsistency() {
        return writeConsistency;
    }

    public void setWriteConsistency(ConsistencyLevel writeConsistency) {
        this.writeConsistency = writeConsistency;
    }

    public String getTableName() {
        return tableName;
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
            return columnName;
        }

        public boolean isPrimaryKey() {
            return isPrimaryKey;
        }

    }

}

