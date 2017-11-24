package example.dao.common;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import example.test.common.EmbeddedCassandraHelper;

@ContextConfiguration(locations = {"/applicationContext-store.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public abstract class AbstractDaoTestCase {
    @BeforeClass
    public static void beforeClass() throws Exception {
        EmbeddedCassandraHelper.startEmbeddedCassandraAndLoadData();

    }

    @AfterClass
    public static void afterClass() {
    }

}
