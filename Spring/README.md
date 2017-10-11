#Spring IoC原理

所谓IoC，对于spring框架来说，就是由spring来负责控制对象的生命周期和对象间的关系。IoC的一个重点是在系统运行中，动态的向某个对象提供它所需要的其他对象。这一点是通过DI（Dependency Injection，依赖注入）来实现的。比如对象A需要操作数据库，以前我们总是要在A中自己编写代码来获得一个Connection对象，有了 spring我们就只需要告诉spring，A中需要一个Connection，至于这个Connection怎么构造，何时构造，A不需要知道。在系统运行时，spring会在适当的时候制造一个Connection，然后像打针一样，注射到A当中，这样就完成了对各个对象之间关系的控制。A需要依赖 Connection才能正常运行，而这个Connection是由spring注入到A中的，依赖注入的名字就这么来的。那么DI是如何实现的呢？ Java 1.3之后一个重要特征是**反射（reflection）**，它允许程序在运行的时候动态的生成对象、执行对象的方法、改变对象的属性，spring就是通过反射来实现注入的。

IoC的3种依赖注入类型如下。 

第1种是通过接口注射，这种方式要求我们的类必须实现容器给定的一个接口，然后容器会利用这个接口给我们这个类注射它所依赖的类。 
第2种是通过setter方法注射，这种方式也是Spring推荐的方式。 
第3种是通过构造方法注射类，这种方式Spring同样给予了实现，它和通过setter方式一样，都在类里无任何侵入性，但是，不是没有侵入性，只是把侵入性转移了。显然第1种方式要求实现特定的接口，侵入性非常强，不方便以后移植。 

创建bean有两种用法。在绝大多数情况下，BeanFactory直接调用bean的构造函数来“new”一个bean（相当于调用new的Java代码），class属性指定了需要创建的bean的类。在比较少的情况下，BeanFactory调用某个类的静态的工厂方法来创建bean，class属性指定了实际包含静态工厂方法的那个类（至于静态工厂方法返回的bean的类型是同一个类还是完全不同的另一个类，这并不重要）。

很多情况下，用户代码不需要实例化BeanFactory，因为Spring框架代码会做这件事。例如，Web层提供支持代码，在J2EE Web应用启动过程中自动载入一个Spring ApplicationContext。

1. 通过构造函数创建bean
    当使用构造函数创建bean时，所有普通的类都可以被Spring使用，并且和Spring兼容。这就是说，被创建的类不需要实现任何特定的接口或者按照特定的样式进行编写。仅仅指定bean的类就足够了。然而，根据bean使用的IoC类型，你可能需要一个默认的（空的）构造函数。
另外，BeanFactory并不局限于管理真正的JavaBean，它也能管理任何你想让它管理的类。虽然很多使用Spring的人喜欢在BeanFactory中用真正的JavaBean（仅包含一个默认的（无参数的）构造函数，在属性后面定义相对应的setter和getter方法），但是在你的BeanFactory中也可以使用特殊的非bean样式的类。举例来说，如果你需要使用一个遗留下来的完全没有遵守JavaBean规范的连接池，不要担心，Spring同样能够管理它。
使用XmlBeanFactory你可以像下面这样定义你的bean class。

```
<bean id="exampleBean"
      class="examples.ExampleBean"/>
<bean name="anotherExample"
      class="examples.ExampleBeanTwo"/>
```

    至于为构造函数提供（可选的）参数，以及对象实例创建后设置实例属性，将会在后面叙述。
	
2. 通过静态工厂方法创建bean
    当你定义一个使用静态工厂方法创建的bean，同时使用class属性指定包含静态工厂方法的类，这个时候需要factory-method属性来指定工厂方法名。Spring调用这个方法（包含一组可选的参数）并返回一个有效的对象，之后这个对象就完全和构造方法创建的对象一样。用户可以使用这样的bean定义在遗留代码中调用静态工厂。
下面是一个bean定义的例子，声明这个bean要通过factory-method指定的方法创建。注意，这个bean定义并没有指定返回对象的类型，只指定包含工厂方法的类。在这个例子中，createInstance 必须是static方法。

```
<bean id="exampleBean"
      class="examples.ExampleBean2"
      factory-method="createInstance"/>
```

    至于为工厂方法提供（可选的）参数，以及对象实例被工厂方法创建后设置实例属性，将会在后面叙述。
	
3. 通过实例工厂方法创建bean
    使用一个实例工厂方法（非静态的）创建bean和使用静态工厂方法非常类似，调用一个已存在的bean（这个bean应该是工厂类型）的工厂方法来创建新的bean。
    使用这种机制，class属性必须为空，而且factory-bean属性必须指定一个bean的名字，这个bean一定要在当前的bean工厂或者父bean工厂中，并包含工厂方法。而工厂方法本身仍然要通过factory-method属性设置。
    下面是一个例子。
```
<!-- The factory bean, which contains a method called
     createInstance -->
<bean id="myFactoryBean"
      class="...">
  ...
</bean>
<!-- The bean to be created via the factory bean -->
<bean id="exampleBean"
      factory-bean="myFactoryBean"
      factory-method="createInstance"/>
```

虽然我们要在后面讨论设置bean的属性，但是，这个方法意味着工厂bean本身能够被容器通过依赖注射来管理和配置。


#AOP（面向切面编程）原理

OO注重的是我们解决问题的方法(封装成Method),而AOP注重的是许多解决解决问题的方法中的共同点,是对OO思想的一种补充!

还是拿人家经常举的一个例子讲解一下吧:  
比如说,我们现在要开发的一个应用里面有很多的业务方法,但是,我们现在要对这个方法的执行做全面监控,或部分监控.也许我们就会在要一些方法前去加上一条日志记录。我们写个例子看看我们最简单的解决方案。

先写一个接口IHello.java代码如下:

```
package  sinosoft.dj.aop.staticaop;

 public   interface  IHello  {
       /** 
     * 假设这是一个业务方法
     *  @param  name
      */ 
     void  sayHello(String name);
} 
```

里面有个方法,用于输入"Hello" 加传进来的姓名;我们去写个类实现IHello接口

```
 package  sinosoft.dj.aop.staticaop;
 
  public   class  Hello  implements  IHello  {
 
       public   void  sayHello(String name)  {
         System.out.println( " Hello  "   +  name);
     } 
 
 } 
```

现在我们要为这个业务方法加上日志记录的业务,我们在不改变原代码的情况下,我们会去怎么做呢?也许,你会去写一个类去实现IHello接口,并依赖Hello这个类.代码如下:

```
package  sinosoft.dj.aop.staticaop;

 public   class  HelloProxy  implements  IHello  {
     private  IHello hello;

      public  HelloProxy(IHello hello)  {
         this .hello  =  hello;
    } 

      public   void  sayHello(String name)  {
        Logger.logging(Level.DEBUGE,  " sayHello method start  . " );
        hello.sayHello(name);
        Logger.logging(Level.INFO,  " sayHello method end! " );

    } 

} 
```

其中.Logger类和Level枚举代码如下:

Logger.java  
```
package  sinosoft.dj.aop.staticaop;

import  java.util.Date;

 public   class  Logger {
       /** 
     * 根据等级记录日志
     *  @param  level
     *  @param  context
      */ 
      public   static   void  logging(Level level, String context)  {
          if  (level.equals(Level.INFO))  {
            System.out.println( new  Date().toLocaleString()  +   "   "   +  context);
        } 
          if  (level.equals(Level.DEBUGE))  {
            System.err.println( new  Date()  +   "   "   +  context);
        } 
    } 

} 
```

Level.java  
```
package  sinosoft.dj.aop.staticaop;

 public   enum  Level  {
    INFO,DEBUGE;
} 
```

那我们去写个测试类看看,代码如下:
Test.java  
```
package  sinosoft.dj.aop.staticaop;

 public   class  Test  {
      public   static   void  main(String[] args)  {
        IHello hello  =   new  HelloProxy( new  Hello());
        hello.sayHello( " Doublej " );
    } 
} 
```

运行以上代码我们可以得到下面结果:   

    Tue Mar  04   20 : 57 : 12  CST  2008  sayHello method start  .
    Hello Doublej
    2008 - 3 - 4   20 : 57 : 12  sayHello method end! 

从上面的代码我们可以看出,hello对象是被HelloProxy这个所谓的代理态所创建的.这样,如果我们以后要把日志记录的功能去掉.那我们只要把得到hello对象的代码改成以下:

```
package  sinosoft.dj.aop.staticaop;

 public   class  Test  {
      public   static   void  main(String[] args)  {
        IHello hello  =   new  Hello();
        hello.sayHello( " Doublej " );
    } 
} 
```

上面代码,可以说是AOP最简单的实现!
但是我们会发现一个问题,如果我们像Hello这样的类很多,那么,我们是不是要去写很多个HelloProxy这样的类呢.没错,是的.其实也是一种很麻烦的事.在jdk1.3以后.jdk跟我们提供了一个API   java.lang.reflect.InvocationHandler的类. 这个类可以让我们在JVM调用某个类的方法时动态的为些方法做些什么事.让我们把以上的代码改一下来看看效果.
同样,我们写一个IHello的接口和一个Hello的实现类.在接口中.我们定义两个方法;代码如下 :

IHello.java  
```
package  sinosoft.dj.aop.proxyaop;

 public   interface  IHello  {
       /** 
     * 业务处理A方法
     *  @param  name
      */ 
     void  sayHello(String name);
       /** 
     * 业务处理B方法
     *  @param  name
      */ 
     void  sayGoogBye(String name);
} 
```

Hello.java  
```
package  sinosoft.dj.aop.proxyaop;

 public   class  Hello  implements  IHello  {

      public   void  sayHello(String name)  {
        System.out.println( " Hello  "   +  name);
    } 
      public   void  sayGoogBye(String name)  {
        System.out.println(name + "  GoodBye! " );
    } 
} 
```

我们一样的去写一个代理类.只不过.让这个类去实现java.lang.reflect.InvocationHandler接口,代码如下:  
```
package  sinosoft.dj.aop.proxyaop;

import  java.lang.reflect.InvocationHandler;
import  java.lang.reflect.Method;
import  java.lang.reflect.Proxy;

public   class  DynaProxyHello  implements  InvocationHandler  {

       /** 
     * 要处理的对象(也就是我们要在方法的前后加上业务逻辑的对象,如例子中的Hello)
      */ 
     private  Object delegate;

       /** 
     * 动态生成方法被处理过后的对象 (写法固定)
     * 
     *  @param  delegate
     *  @param  proxy
     *  @return 
      */ 
      public  Object bind(Object delegate)  {
         this .delegate  =  delegate;
         return  Proxy.newProxyInstance(
                 this .delegate.getClass().getClassLoader(),  this .delegate
                        .getClass().getInterfaces(),  this );
    } 
       /** 
     * 要处理的对象中的每个方法会被此方法送去JVM调用,也就是说,要处理的对象的方法只能通过此方法调用
     * 此方法是动态的,不是手动调用的
      */ 
     public  Object invoke(Object proxy, Method method, Object[] args)
              throws  Throwable  {
        Object result  =   null ;
          try   {
             // 执行原来的方法之前记录日志 
            Logger.logging(Level.DEBUGE, method.getName()  +   "  Method end   . " );
            
             // JVM通过这条语句执行原来的方法(反射机制) 
            result  =  method.invoke( this .delegate, args);
             // 执行原来的方法之后记录日志 
            Logger.logging(Level.INFO, method.getName()  +   "  Method Start! " );
         }   catch  (Exception e)  {
            e.printStackTrace();
        } 
         // 返回方法返回值给调用者 
         return  result;
    } 

} 
```

上面类中出现的Logger类和Level枚举还是和上一上例子的实现是一样的.这里就不贴出代码了.

让我们写一个Test类去测试一下.代码如下:
Test.java  
```
  package  sinosoft.dj.aop.proxyaop;
  
   public   class  Test  {
        public   static   void  main(String[] args)  {
          IHello hello  =  (IHello) new  DynaProxyHello().bind( new  Hello());
          hello.sayGoogBye( " Double J " );
          hello.sayHello( " Double J " );
          
      } 
  } 
```

运行输出的结果如下:
    Tue Mar  04   21 : 24 : 03  CST  2008  sayGoogBye Method end   .
    Double J GoodBye!
    2008 - 3 - 4   21 : 24 : 03  sayGoogBye Method Start!
    Tue Mar  04   21 : 24 : 03  CST  2008  sayHello Method end   .
    Hello Double J
    2008 - 3 - 4   21 : 24 : 03  sayHello Method Start! 

由于线程的关系,第二个方法的开始出现在第一个方法的结束之前.这不是我们所关注的!

从上面的例子我们看出.只要你是采用面向接口编程,那么,你的任何对象的方法执行之前要加上记录日志的操作都是可以的.他(DynaPoxyHello) 自动去代理执行被代理对象(Hello)中的每一个方法,一个java.lang.reflect.InvocationHandler接口就把我们的代理对象和被代理对象解藕了.

下面留一个问题给大家,如果我们不想让所有方法都被日志记录,我们应该怎么去解藕呢.?
我的想法是在代理对象的public Object invoke(Object proxy, Method method, Object[] args)方法里面加上个if(),对传进来的method的名字进行判断,判断的条件存在XML里面.这样我们就可以配置文件时行解藕了.如果有兴趣的朋友可以把操作者,被代理者,都通过配置文件进行配置 ,那么就可以写一个简单的SpringAOP框架了.

下面让我们实现一个Spring AOP的例子。在这个例子中，我们将实现一个before advice，这意味着advice的代码在被调用的public方法开始前被执行。以下是这个before advice的实现代码。   
```
package com.ascenttech.springaop.test; 

import java.lang.reflect.Method; 

import org.springframework.aop.MethodBeforeAdvice; 

public class TestBeforeAdvice implements MethodBeforeAdvice { 

public void before(Method m, Object[] args, Object target) 

throws Throwable { 

   System.out.println("Hello world! (by " 

     + this.getClass().getName() 

     + ")"); 

} 

} 
```

接口MethodBeforeAdvice只有一个方法before需要实现，它定义了advice的实现。before方法共用3个参数，它们提供了相当丰富的信息。参数Method m是advice开始后执行的方法，方法名称可以用作判断是否执行代码的条件。Object[] args是传给被调用的public方法的参数数组。当需要记日志时，参数args和被执行方法的名称都是非常有用的信息。你也可以改变传给m的参数，但要小心使用这个功能；编写最初主程序的程序员并不知道主程序可能会和传入参数的发生冲突。Object target是执行方法m对象的引用。 

在下面的BeanImpl类中，每个public方法调用前，都会执行advice，代码如下。    
```
package com.ascenttech.springaop.test; 

public class BeanImpl implements Bean { 

public void theMethod() { 

   System.out.println(this.getClass().getName() 

     + "." + new Exception().getStackTrace()[0].getMethodName() 

     + "()" 

     + " says HELLO!"); 

} 

} 
```

类BeanImpl实现了下面的接口Bean，代码如下。   
```
package com.ascenttech.springaop.test; 

public interface Bean { 

public void theMethod(); 

} 
```

虽然不是必须使用接口，但面向接口而不是面向实现编程是良好的编程实践，Spring也鼓励这样做。 

pointcut和advice通过配置文件来实现，因此，接下来你只需编写主方法的Java代码，代码如下。   
```
package com.ascenttech.springaop.test; 

import org.springframework.context.ApplicationContext; 

import org.springframework.context.support.FileSystemXmlApplicationContext; 

public class Main { 

public static void main(String[] args) { 

   //Read the configuration file 

   ApplicationContext ctx 

     = new FileSystemXmlApplicationContext("springconfig.xml"); 

   //Instantiate an object 

   Bean x = (Bean) ctx.getBean("bean"); 

   //Execute the public method of the bean (the test) 

   x.theMethod(); 

} 

} 
```

我们从读入和处理配置文件开始，接下来马上要创建它。这个配置文件将作为粘合程序不同部分的“胶水”。读入和处理配置文件后，我们会得到一个创建工厂ctx，任何一个Spring管理的对象都必须通过这个工厂来创建。对象通过工厂创建后便可正常使用。 

仅仅用配置文件便可把程序的每一部分组装起来，代码如下。   
```
<?xml version="1.0" encoding="UTF-8"?> 

<!DOCTYPE beans PUBLIC "-//SPRING//DTD BEAN//EN" "http://www.springframework. org/dtd/spring-beans.dtd"> 

<beans> 

<!--CONFIG--> 

<bean id="bean" class="org.springframework.aop.framework.ProxyFactoryBean"> 

<property name="proxyInterfaces"> 

    <value>com.ascenttech.springaop.test.Bean</value> 

   </property> 

   <property name="target"> 

    <ref local="beanTarget"/> 

   </property> 

   <property name="interceptorNames"> 

    <list> 

     <value>theAdvisor</value> 

    </list> 

   </property> 

</bean> 

<!--CLASS--> 

<bean id="beanTarget" class="com.ascenttech.springaop.test.BeanImpl"/> 

<!--ADVISOR--> 

<!--Note: An advisor assembles pointcut and advice--> 

<bean id="theAdvisor" class="org.springframework.aop.support.RegexpMethod PointcutAdvisor"> 

   <property name="advice"> 

    <ref local="theBeforeAdvice"/> 

   </property> 

   <property name="pattern"> 

    <value>com\.ascenttech\.springaop\.test\.Bean\.theMethod</value> 

   </property> 

</bean> 

<!--ADVICE--> 

<bean id="theBeforeAdvice" class="com.ascenttech.springaop.test.TestBefore Advice"/> 

</beans> 
```

4个bean定义的次序并不重要。我们现在有了一个advice、一个包含了正则表达式pointcut的advisor、一个主程序类和一个配置好的接口，通过工厂ctx，这个接口返回自己本身实现的一个引用。 

BeanImpl和TestBeforeAdvice都是直接配置。我们用一个惟一的ID创建一个bean元素，并指定了一个实现类，这就是全部的工作。 

advisor通过Spring framework提供的一个RegexMethodPointcutAdvisor类来实现。我们用advisor的第一个属性来指定它所需的advice-bean，第二个属性则用正则表达式定义了pointcut，确保良好的性能和易读性。 

最后配置的是bean，它可以通过一个工厂来创建。bean的定义看起来比实际上要复杂。bean是ProxyFactoryBean的一个实现，它是Spring framework的一部分。这个bean的行为通过以下的3个属性来定义。 

— 属性proxyInterface定义了接口类。 

— 属性target指向本地配置的一个bean，这个bean返回一个接口的实现。 

— 属性interceptorNames是惟一允许定义一个值列表的属性，这个列表包含所有需要在beanTarget上执行的advisor。注意，advisor列表的次序是非常重要的。
