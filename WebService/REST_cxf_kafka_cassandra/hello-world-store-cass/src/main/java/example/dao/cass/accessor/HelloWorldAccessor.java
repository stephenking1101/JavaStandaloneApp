package example.dao.cass.accessor;

import com.datastax.driver.core.Statement;
import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;
import com.datastax.driver.mapping.annotations.QueryParameters;

import example.dao.cass.entity.HelloWorldEntity;

//Accessors provide a way to map custom queries not supported by the default entity mappers.
@Accessor
public interface HelloWorldAccessor {
    @QueryParameters(consistency = "LOCAL_QUORUM")
    @Query("SELECT * FROM hello_world WHERE user_name = :userName")
    Result<HelloWorldEntity> queryByUserName(@Param("userName") String userName);

    @QueryParameters(consistency = "LOCAL_QUORUM")
    @Query("DELETE FROM hello_world WHERE user_name = :userName")
    Statement deleteByUserName(@Param("userName") String userName);
}

