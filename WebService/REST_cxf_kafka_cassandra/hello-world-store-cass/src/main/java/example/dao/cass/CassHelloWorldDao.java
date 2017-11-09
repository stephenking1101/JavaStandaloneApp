package example.dao.cass;

import java.util.ArrayList;
import java.util.List;

import com.datastax.driver.mapping.Result;

import example.dao.HelloWorldDao;
import example.dao.cass.accessor.HelloWorldAccessor;
import example.dao.cass.entity.HelloWorldEntity;
import example.service.payload.HelloWorld;
import example.store.cassandra.CassandraObjectMapper;

public class CassHelloWorldDao extends CassandraObjectMapper<HelloWorldEntity> implements HelloWorldDao {

    public CassHelloWorldDao() {
        super(HelloWorldEntity.class);
    }

	@Override
	public HelloWorld get(String userName, Long timestamp) {
		return this.entity2pojo(super.get(userName, timestamp));
	}
	
    @Override
    public void create(HelloWorld helloWorld) { super.save(pojo2entity(helloWorld)); }

    @Override
    public List<HelloWorld> queryByUserName(String userName) {
        final HelloWorldAccessor accessor = super.getAccessor(HelloWorldAccessor.class);
        final List<HelloWorld> pojoList = new ArrayList<HelloWorld>();

        Result<HelloWorldEntity> entitys = accessor.queryByUserName(userName);
        for (HelloWorldEntity row : entitys) {
            pojoList.add(entity2pojo(row));
        }
        return pojoList;
    }

	@Override
	public void deleteByUserName(String userName) {
		final HelloWorldAccessor accessor = super.getAccessor(HelloWorldAccessor.class);
		
		super.getSession().execute(accessor.deleteByUserName(userName));
	}

    private HelloWorld entity2pojo(HelloWorldEntity entity) {
        if (null == entity) {
            return null;
        }
        final HelloWorld pojo = new HelloWorld();
        pojo.setUserName(entity.getUserName());
        pojo.setTimestamp(entity.getTimestamp());

        pojo.setExtension(entity.getExtension());
        return pojo;
    }

    private HelloWorldEntity pojo2entity(HelloWorld pojo) {
        if (null == pojo) {
            return null;
        }
        final HelloWorldEntity entity = new HelloWorldEntity();
        entity.setUserName(pojo.getUserName());
        entity.setTimestamp(pojo.getTimestamp());

        entity.setExtension(pojo.getExtension());
        return entity;
    }

}
