package example.dao;

import java.util.List;

import example.service.payload.HelloWorld;

public interface HelloWorldDao {
	
	HelloWorld get(String userName, Long timestamp);

    void create(HelloWorld helloWorld);

    List<HelloWorld> queryByUserName(String userName);
    
    void deleteByUserName(String userName);
}
