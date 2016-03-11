#### 关于scope="singleton"的解释:

```
scope="singleton"
```
	

>singleton作用域：
	当把一个Bean定义设置为singleton作用域时，
	Spring IoC容器中只会存在一个共享的Bean实例，并且所有对Bean的请求，
	只要id与该Bean定义相匹配，则只会返回该Bean的同一实例。
	值得强调的是singleton作用域是Spring中的缺省作用域。 
	
>prototype作用域 ：
	prototype作用域的Bean会导致在每次对该Bean请求
	（将其注入到另一个Bean中，或者以程序的方式调用容器的getBean ()方法）时都会创建一个新的Bean实例。
	
>根据经验，
	对有状态的Bean应使用prototype作用域，
	而对无状态的Bean则应该使用singleton作用域。
	对于具有prototype作用域的Bean，有一点很重要，即Spring不能对该Bean的整个生命周期负责。
	具有prototype作用域的Bean创建后交由调用者负责销毁对象回收资源。 
	
>简单的说：
	singleton 只有一个实例，也即是单例模式。 
	prototype访问一次创建一个实例，相当于new。 
	
>应用场合：
	1.需要回收重要资源(数据库连接等)的事宜配置为singleton，如果配置为prototype需要应用确保资源正常回收。
	2.有状态的Bean配置成singleton会引发未知问题，可以考虑配置为prototype。

>scope为singleton的会在启动服务器时实例化，而prototype是在请求的时候再实例化
	其实是这样的，如果一个bean是prototype的， 并且这个bean要被注入到其它bean或者你通过getBean这样的方式获得这个bean的时候，
	spring容器会创建一个新的实例给你。
	**singleton模式指的是对某个对象的完全共享，包括代码空间和数据空间，也就是说，singleton会让所有线程共享他的成员变量。**
	**prototype则不会这样。**
	>此外singleton的生命周期由容器来管理，但是prototype的生命周期得你自己管理。
	最后，有个小trick，singleton的bean引用一个prototype的bean时会出现问题，因为singleton只初始化一次，但prototype每请求一次都会有一个新的对象，但prototype类型的bean是singleton类型bean的一个属性，所以不可能有新prototpye的bean产生

```
 <?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:p="http://www.springframework.org/schema/p"
	xmlns:context="http://www.springframework.org/schema/context"
	xmlns:aop="http://www.springframework.org/schema/aop" xmlns:tx="http://www.springframework.org/schema/tx"
	xsi:schemaLocation="http://www.springframework.org/schema/beans
           http://www.springframework.org/schema/beans/spring-beans-2.5.xsd
           http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-2.5.xsd
           http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop-2.5.xsd
           http://www.springframework.org/schema/tx http://www.springframework.org/schema/tx/spring-tx-2.5.xsd">

	<bean id="person" class="Person" init-method="init" scope="singleton"
		destroy-method="destroy">
		<property name="name" value="XINYI" />
		<property name="age" value="12" />
	</bean>
</beans>
```


