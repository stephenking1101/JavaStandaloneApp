1. 侵入式：class Order impletment/extend 框架
String是非侵入式,容易调试

2. 开闭原则
策略模式

3. Spring Ioc原理
Factory
Ioc 相当于BeanFactory（除分布式外，可代替EJB）, DI
(1)编写xml
(2)初始化Ioc容器
(3)调用getBean(id)，注入依赖

容器包含：生存周期，生命周期方法

用import来传入配置文件

Web 环境中建立，用listener, 在servlet启动时.

servlet容器，Ioc容器都是单实例多线程。不要有状态（实例变量）。

4. AOP
源码组成无关，Proxy模式，不同于装饰者。
Java中，动态代理必须实现接口
Advice- What,When
CutPoint- Where

Ioc - 解藕
AOP - 提供service