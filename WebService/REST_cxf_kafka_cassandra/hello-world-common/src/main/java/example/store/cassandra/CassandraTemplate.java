package example.store.cassandra;

import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.MappingManager;

public class CassandraTemplate {

	private SessionFactory sessionFactory;
    private volatile MappingManager mappingManager;

    //泛型只在编译阶段有效
    /**
     * 泛型方法的基本介绍
     * @param accessorClazz 传入的泛型实参
     * @return A 返回值为T类型
     * 说明：
     *     1）public 与 返回值中间<A>非常重要，可以理解为声明此方法为泛型方法。
     *     2）只有声明了<A>的方法才是泛型方法，泛型类中的使用了泛型的成员方法并不是泛型方法。
     *     3）<A>表明该方法将使用泛型类型T，此时才可以在方法中使用泛型类型A。
     *     4）与泛型类的定义一样，此处A可以随便写为任意标识，常见的如T、E、K、V等形式的参数常用于表示泛型。
     *     
     * 泛型的数量也可以为任意多个 
     *    如：public <T,K> K showKeyName(Generic<T> container){
     *        ...
     *        }
     */
    public <A> A getAccessor(Class<A> accessorClazz) {
        return this.getMappingManager().createAccessor(accessorClazz);
    }

    public MappingManager getMappingManager() {
        if (this.mappingManager == null) {
            synchronized(this) {
                if (this.mappingManager == null) {
                    this.mappingManager = new MappingManager(this.sessionFactory.getSession());
                }
            }
        }

        return this.mappingManager;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public Session getSession() {
        return this.sessionFactory.getSession();
    }
}
