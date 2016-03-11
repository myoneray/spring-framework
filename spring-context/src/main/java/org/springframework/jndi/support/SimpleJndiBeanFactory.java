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

package org.springframework.jndi.support;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.core.ResolvableType;
import org.springframework.jndi.JndiLocatorSupport;
import org.springframework.jndi.TypeMismatchNamingException;

/**
 * Simple JNDI-based implementation of Spring's
 * {@link org.springframework.beans.factory.BeanFactory} interface. <br>
 * Does not support enumerating bean definitions, hence doesn't implement the
 * {@link org.springframework.beans.factory.ListableBeanFactory} interface.
 *
 * 简单的基于JNDI的实现Spring的BeanFactory接口。 <br>
 * 不支持枚举bean定义，因此没有实现ListableBeanFactory接口。
 * <p>
 * This factory resolves given bean names as JNDI names within the J2EE application's
 * "java:comp/env/" namespace. <br>
 * 这个factory以bean的名字作为J2EE的命名空间命名 <br>
 * <p>
 * It caches the resolved types for all obtained objects, and optionally also caches
 * shareable objects (if they are explicitly marked as {@link #addShareableResource
 * shareable resource}. <br>
 * 它缓存获取的所有对象,以及共享对象(要是这个对象已经标记为共享资源)<br>
 * <p>
 * The main intent of this factory is usage in combination with Spring's
 * {@link org.springframework.context.annotation.CommonAnnotationBeanPostProcessor},
 * configured as "resourceFactory" for resolving {@code @Resource} annotations as JNDI
 * objects without intermediate bean definitions. It may be used for similar lookup
 * scenarios as well, of course, in particular if BeanFactory-style type checking is
 * required. <br>
 * 他的主要作用是与spring的"CommonAnnotationBeanPostProcessor"配置为"resourceFactory",解决 JNDI @Resource
 * 注解,不需要定义中间bean .它可被用于类似的查找方案，当然 ，特别是如果需要的BeanFactory风格类型检查。
 * 
 * @author Juergen Hoeller
 * @since 2.5
 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory
 * @see org.springframework.context.annotation.CommonAnnotationBeanPostProcessor
 */
public class SimpleJndiBeanFactory extends JndiLocatorSupport implements BeanFactory {

	/** JNDI names of resources that are known to be shareable, i.e. can be cached */
	/** 缓存名称已共享的JNDI */
	private final Set<String> shareableResources = new HashSet<String>();

	/** Cache of shareable singleton objects: bean name --> bean instance */
	/** 缓存已共享的单例对象:bean 名字 --> bean实例 */
	private final Map<String, Object> singletonObjects = new HashMap<String, Object>();

	/** Cache of the types of nonshareable resources: bean name --> bean type */
	/** 缓存类型为非共享的资源:bean 名字 --> bean 类型 */
	private final Map<String, Class<?>> resourceTypes = new HashMap<String, Class<?>>();

	/** 构造方法 */
	/** 设置JndiLocatorSupport中的resourceRef为true */
	public SimpleJndiBeanFactory() {
		setResourceRef(true);
	}

	/**
	 * Add the name of a shareable JNDI resource, which this factory is allowed to cache
	 * once obtained. <br>
	 * 添加共享的JNDI资源,factory允许被缓存一次.<br>
	 * 
	 * @param shareableResource the JNDI name (typically within the "java:comp/env/"
	 *        namespace)
	 * @参数 JNDI共享数据(通常在"java:comp/env/"命名空间内)
	 */
	public void addShareableResource(String shareableResource) {
		this.shareableResources.add(shareableResource);
	}

	/**
	 * Set a list of names of shareable JNDI resources, which this factory is allowed to
	 * cache once obtained.<br>
	 * 设置一个List共享的JNDI资源,factory允许被缓存一次.<br>
	 * 
	 * @param shareableResources the JNDI names (typically within the "java:comp/env/"
	 *        namespace)
	 * @参数 JNDI共享数据(通常在"java:comp/env/"命名空间内)
	 */
	public void setShareableResources(String... shareableResources) {
		this.shareableResources.addAll(Arrays.asList(shareableResources));
	}

	// ---------------------------------------------------------------------
	// Implementation of BeanFactory interface
	// 实现BeanFactory 接口
	// ---------------------------------------------------------------------

	// 返回实例，其可以指定Bean共享或独立。
	@Override
	public Object getBean(String name) throws BeansException {
		return getBean(name, Object.class);
	}

	// 返回实例，其可以指定Bean共享或独立。
	@Override
	public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
		try {
			if (isSingleton(name)) {// set集合中是不是存在
				return doGetSingleton(name, requiredType);// 存在.去获取实例
			}
			else {
				return lookup(name, requiredType);// 不存在,?
			}
		}
		catch (NameNotFoundException ex) {
			// 抛出异常
			throw new NoSuchBeanDefinitionException(name, "not found in JNDI environment");
		}
		catch (TypeMismatchNamingException ex) {
			// 抛出异常
			throw new BeanNotOfRequiredTypeException(name, ex.getRequiredType(),
					ex.getActualType());
		}
		catch (NamingException ex) {
			// 抛出异常
			throw new BeanDefinitionStoreException("JNDI environment", name,
					"JNDI lookup failed", ex);
		}
	}

	// 返回实例，其可以指定Bean共享或独立。
	@Override
	public <T> T getBean(Class<T> requiredType) throws BeansException {
		return getBean(requiredType.getSimpleName(), requiredType);
	}

	// 返回实例，其可以指定Bean共享或独立。
	@Override
	public Object getBean(String name, Object... args) throws BeansException {
		if (args != null) {
			throw new UnsupportedOperationException(
					"SimpleJndiBeanFactory does not support explicit bean creation arguments");
		}
		return getBean(name);
	}

	// 返回实例，其可以指定Bean共享或独立。
	@Override
	public <T> T getBean(Class<T> requiredType, Object... args) throws BeansException {
		if (args != null) {
			throw new UnsupportedOperationException(
					"SimpleJndiBeanFactory does not support explicit bean creation arguments");
		}
		return getBean(requiredType);
	}

	@Override
	public boolean containsBean(String name) {
		if (this.singletonObjects.containsKey(name)
				|| this.resourceTypes.containsKey(name)) {
			return true;
		}
		try {
			doGetType(name);
			return true;
		}
		catch (NamingException ex) {
			return false;
		}
	}

	// Nean name是否在shareableResources已经存在,存在返回True,不存在返回false
	// Set.contains
	@Override
	public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {

		// 传递Bean
		// Set集合中存name在返回True
		// Set集合中不存在name返回false
		return this.shareableResources.contains(name);
	}

	@Override
	public boolean isPrototype(String name) throws NoSuchBeanDefinitionException {
		return !this.shareableResources.contains(name);
	}

	@Override
	public boolean isTypeMatch(String name, ResolvableType typeToMatch)
			throws NoSuchBeanDefinitionException {
		Class<?> type = getType(name);
		return (type != null && typeToMatch.isAssignableFrom(type));
	}

	@Override
	public boolean isTypeMatch(String name, Class<?> typeToMatch)
			throws NoSuchBeanDefinitionException {
		Class<?> type = getType(name);
		return (typeToMatch == null || (type != null && typeToMatch.isAssignableFrom(type)));
	}

	@Override
	public Class<?> getType(String name) throws NoSuchBeanDefinitionException {
		try {
			return doGetType(name);
		}
		catch (NameNotFoundException ex) {
			throw new NoSuchBeanDefinitionException(name, "not found in JNDI environment");
		}
		catch (NamingException ex) {
			return null;
		}
	}

	@Override
	public String[] getAliases(String name) {
		return new String[0];
	}

	/**
	 * 获取单例
	 * 
	 * @param name BeanName
	 * @param requiredType 实例类型
	 * @return 泛型集合
	 * @throws NamingException
	 */
	@SuppressWarnings("unchecked")
	private <T> T doGetSingleton(String name, Class<T> requiredType)
			throws NamingException {
		// 一次只能有一个线程进入
		synchronized (this.singletonObjects) {
			// 如果存在就获取:jndiObject
			if (this.singletonObjects.containsKey(name)) {
				Object jndiObject = this.singletonObjects.get(name);
				// 如果requiredType与通过singletonObjects获取到的对象类型不一致,抛出异常
				if (requiredType != null && !requiredType.isInstance(jndiObject)) {
					throw new TypeMismatchNamingException(convertJndiName(name),
							requiredType, (jndiObject != null ? jndiObject.getClass()
									: null));
				}
				// 否则就强制转换为该(requiredType)类型
				return (T) jndiObject;
			}
			T jndiObject = lookup(name, requiredType);
			// 查找到对象添加到Set集合中去
			this.singletonObjects.put(name, jndiObject);
			// 返回set单例集合
			return jndiObject;
		}
	}

	/**
	 * 获取类型
	 * 
	 * @param name :BeanName
	 * @return Class<?>:未知的类
	 * @throws NamingException
	 */
	private Class<?> doGetType(String name) throws NamingException {
		// set集合中是否存在
		if (isSingleton(name)) {
			// 存在
			Object jndiObject = doGetSingleton(name, null); // 获取实例
			return (jndiObject != null ? jndiObject.getClass() : null);// 返回实例
		}
		else {
			// 不存在
			// 单线程 resourceTypes? 不明白
			synchronized (this.resourceTypes) {
				// 存在BeanName为name的返回该缓存对象
				if (this.resourceTypes.containsKey(name)) {
					return this.resourceTypes.get(name);
				}
				// 不存在BeanName为name的返回该缓存对象
				else {
					Object jndiObject = lookup(name, null);
					// 获取实例的类型
					Class<?> type = (jndiObject != null ? jndiObject.getClass() : null);
					// 添加到Set集合中
					this.resourceTypes.put(name, type);
					// 返回
					return type;
				}
			}
		}
	}

}
