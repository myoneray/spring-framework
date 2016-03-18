/*
 * Copyright 2002-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.beans.factory;

import org.springframework.beans.BeansException;
import org.springframework.core.ResolvableType;

/**
 * The root interface for accessing a Spring bean container. This is the basic client view
 * of a bean container; further interfaces such as {@link ListableBeanFactory} and
 * {@link org.springframework.beans.factory.config.ConfigurableBeanFactory} are available
 * for specific purposes.
 *
 * <p>
 * This interface is implemented by objects that hold a number of bean definitions, each
 * uniquely identified by a String name. Depending on the bean definition, the factory
 * will return either an independent instance of a contained object (the Prototype design
 * pattern), or a single shared instance (a superior alternative to the Singleton design
 * pattern, in which the instance is a singleton in the scope of the factory). Which type
 * of instance will be returned depends on the bean factory configuration: the API is the
 * same. Since Spring 2.0, further scopes are available depending on the concrete
 * application context (e.g. "request" and "session" scopes in a web environment).
 *
 * <p>
 * The point of this approach is that the BeanFactory is a central registry of application
 * components, and centralizes configuration of application components (no more do
 * individual objects need to read properties files, for example). See chapters 4 and 11
 * of "Expert One-on-One J2EE Design and Development" for a discussion of the benefits of
 * this approach.
 *
 * <p>
 * Note that it is generally better to rely on Dependency Injection ("push" configuration)
 * to configure application objects through setters or constructors, rather than use any
 * form of "pull" configuration like a BeanFactory lookup. Spring's Dependency Injection
 * functionality is implemented using this BeanFactory interface and its subinterfaces.
 *
 * <p>
 * Normally a BeanFactory will load bean definitions stored in a configuration source
 * (such as an XML document), and use the {@code org.springframework.beans} package to
 * configure the beans. However, an implementation could simply return Java objects it
 * creates as necessary directly in Java code. There are no constraints on how the
 * definitions could be stored: LDAP, RDBMS, XML, properties file, etc. Implementations
 * are encouraged to support references amongst beans (Dependency Injection).
 *
 * <p>
 * In contrast to the methods in {@link ListableBeanFactory}, all of the operations in
 * this interface will also check parent factories if this is a
 * {@link HierarchicalBeanFactory}. If a bean is not found in this factory instance, the
 * immediate parent factory will be asked. Beans in this factory instance are supposed to
 * override beans of the same name in any parent factory.
 *
 * <p>
 * Bean factory implementations should support the standard bean lifecycle interfaces as
 * far as possible. The full set of initialization methods and their standard order is:<br>
 * 1. BeanNameAware's {@code setBeanName}<br>
 * 2. BeanClassLoaderAware's {@code setBeanClassLoader}<br>
 * 3. BeanFactoryAware's {@code setBeanFactory}<br>
 * 4. ResourceLoaderAware's {@code setResourceLoader} (only applicable when running in an
 * application context)<br>
 * 5. ApplicationEventPublisherAware's {@code setApplicationEventPublisher} (only
 * applicable when running in an application context)<br>
 * 6. MessageSourceAware's {@code setMessageSource} (only applicable when running in an
 * application context)<br>
 * 7. ApplicationContextAware's {@code setApplicationContext} (only applicable when
 * running in an application context)<br>
 * 8. ServletContextAware's {@code setServletContext} (only applicable when running in a
 * web application context)<br>
 * 9. {@code postProcessBeforeInitialization} methods of BeanPostProcessors<br>
 * 10. InitializingBean's {@code afterPropertiesSet}<br>
 * 11. a custom init-method definition<br>
 * 12. {@code postProcessAfterInitialization} methods of BeanPostProcessors
 *
 * <p>
 * On shutdown of a bean factory, the following lifecycle methods apply:<br>
 * 1. DisposableBean's {@code destroy}<br>
 * 2. a custom destroy-method definition
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 13 April 2001
 * @see 1>BeanNameAware#setBeanName
 * @see 2>BeanClassLoaderAware#setBeanClassLoader
 * @see 3>BeanFactoryAware#setBeanFactory
 * @see 4>org.springframework.context.ResourceLoaderAware#setResourceLoader
 * @see 5>org.springframework.context.ApplicationEventPublisherAware#setApplicationEventPublisher
 * @see 6>org.springframework.context.MessageSourceAware#setMessageSource
 * @see 7>org.springframework.context.ApplicationContextAware#setApplicationContext
 * @see 8>org.springframework.web.context.ServletContextAware#setServletContext
 * @see 9>org.springframework.beans.factory.config.BeanPostProcessor#postProcessBeforeInitialization
 * @see 10>InitializingBean#afterPropertiesSet
 * @see 11>org.springframework.beans.factory.support.RootBeanDefinition#getInitMethodName
 * @see 12>org.springframework.beans.factory.config.BeanPostProcessor#postProcessAfterInitialization
 * @see 13>DisposableBean#destroy
 * @see 14>org.springframework.beans.factory.support.RootBeanDefinition#getDestroyMethodName
 */
public interface BeanFactory {

	/**
	 * 这里是对FactoryBean的转义定义，因为如果使用bean的名字检索FactoryBean得到的对象是工厂生成的对象， <br>
	 * 如果需要得到工厂本身，需要转义<br>
	 * 用来引用一个实例，或把它和工厂产生的Bean区分开，就是说，如果一个FactoryBean的名字为a， <br>
	 * 那么，$a会得到那个Factory本身
	 */
	// @see http://www.oschina.net/question/1029535_130232
	// -----
	// @Resource('&properties')
	// private PropertiesFactoryBean properties;
	// 这样子说不定能得到~之所以叫FactoryBean，就因为是用它来生成Bean的，默认情况下，如果一个Bean是FactoryBean，
	// Spring是会返回其生成的Bean，而不是工厂本身，如果想要得到工厂本身，需要在ID前加&.
	// 上面这样子不知道行不行，但factory.getBean('&properteis')应该是可以获取到FactoryBean的。
	// 一般也很少会用到FactoryBean。
	String FACTORY_BEAN_PREFIX = "&";

	// 1> 这里根据bean的名字，在IOC容器中得到bean实例，这个IOC容器就是一个大的抽象工厂。
	Object getBean(String name) throws BeansException;

	// 2> 这里根据bean的名字和Class类型来得到bean实例，
	// 和上面的方法不同在于它会抛出异常：如果根据名字取得的bean实例的Class类型和需要的不同的话。
	<T> T getBean(String name, Class<T> requiredType) throws BeansException;

	// 3> 这里根据bean的Class类型来得到bean实例，
	<T> T getBean(Class<T> requiredType) throws BeansException;

	// 4> 这里根据bean的名字和来Object得到bean实例，
	Object getBean(String name, Object... args) throws BeansException;

	// 5> 这里根据bean的Class类型和来Object得到bean实例，
	<T> T getBean(Class<T> requiredType, Object... args) throws BeansException;

	// 1> 这里提供对bean的检索，看看是否在IOC容器有这个名字的bean
	boolean containsBean(String name);

	// 2> 这里根据bean名字得到bean实例，并同时判断这个bean是不是单实例
	boolean isSingleton(String name) throws NoSuchBeanDefinitionException;

	// 3> 是否为原型（多实例）
	boolean isPrototype(String name) throws NoSuchBeanDefinitionException;

	// 4> 名称、类型是否匹配
	boolean isTypeMatch(String name, ResolvableType typeToMatch)
			throws NoSuchBeanDefinitionException;

	// 5> 名称、类型是否匹配
	boolean isTypeMatch(String name, Class<?> typeToMatch)
			throws NoSuchBeanDefinitionException;

	// 获取类型
	Class<?> getType(String name) throws NoSuchBeanDefinitionException;

	// 根据实例的名字获取实例的别名
	String[] getAliases(String name);

}
