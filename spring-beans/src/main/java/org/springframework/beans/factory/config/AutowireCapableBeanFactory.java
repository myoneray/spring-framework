/*
 * Copyright 2002-2013 the original author or authors.
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

package org.springframework.beans.factory.config;

import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanFactory;

/**
 * Extension of the {@link org.springframework.beans.factory.BeanFactory}
 * interface to be implemented by bean factories that are capable of
 * autowiring, provided that they want to expose this functionality for
 * existing bean instances.
 *
 * <p>This subinterface of BeanFactory is not meant to be used in normal
 * application code: stick to {@link org.springframework.beans.factory.BeanFactory}
 * or {@link org.springframework.beans.factory.ListableBeanFactory} for
 * typical use cases.
 *
 * <p>Integration code for other frameworks can leverage this interface to
 * wire and populate existing bean instances that Spring does not control
 * the lifecycle of. This is particularly useful for WebWork Actions and
 * Tapestry Page objects, for example.
 *
 * <p>Note that this interface is not implemented by
 * {@link org.springframework.context.ApplicationContext} facades,
 * as it is hardly ever used by application code. That said, it is available
 * from an application context too, accessible through ApplicationContext's
 * {@link org.springframework.context.ApplicationContext#getAutowireCapableBeanFactory()}
 * method.
 *
 * <p>You may also implement the {@link org.springframework.beans.factory.BeanFactoryAware}
 * interface, which exposes the internal BeanFactory even when running in an
 * ApplicationContext, to get access to an AutowireCapableBeanFactory:
 * simply cast the passed-in BeanFactory to AutowireCapableBeanFactory.
 *
 * @author Juergen Hoeller
 * @since 04.12.2003
 * @see org.springframework.beans.factory.BeanFactoryAware
 * @see org.springframework.beans.factory.config.ConfigurableListableBeanFactory
 * @see org.springframework.context.ApplicationContext#getAutowireCapableBeanFactory()
 */
/**
 * 功能:装配applicationContext管理之外的Bean
 * 
 * @author upsmart
 * @since 4.2.1
 */
public interface AutowireCapableBeanFactory extends BeanFactory {

	int AUTOWIRE_NO = 0;

	// 通过名字自动装备Bean(适用于所有的Bean设置)
	int AUTOWIRE_BY_NAME = 1;

	// 通过类型自动装配(适用于所有的bean设置)
	int AUTOWIRE_BY_TYPE = 2;

	// 通过合适的构造函数自动装配
	int AUTOWIRE_CONSTRUCTOR = 3;

	// 过时
	@Deprecated
	int AUTOWIRE_AUTODETECT = 4;

	// 根据设定的Class创建Bean
	<T> T createBean(Class<T> beanClass) throws BeansException;

	/**
	 * Populate the given bean instance through applying after-instantiation callbacks and
	 * bean property post-processing (e.g. for annotation-driven injection).
	 * <p>
	 * Note: This is essentially intended for (re-)populating annotated fields and
	 * methods, either for new instances or for deserialized instances. It does <i>not</i>
	 * imply traditional by-name or by-type autowiring of properties; use
	 * {@link #autowireBeanProperties} for that purposes.
	 * 
	 * @param existingBean the existing bean instance
	 * @throws BeansException if wiring failed
	 */
	void autowireBean(Object existingBean) throws BeansException;

	// 配置给定的bean:自动装载bean的属性,应用bean属性的值. 这种方法需要对给定名称的bean定义！
	Object configureBean(Object existingBean, String beanName) throws BeansException;

	/**
	 * 对Factory的bean指定依赖
	 * 
	 * @param descriptor 依赖的描述
	 * @param beanName bean的这申明，本依赖名称
	 */
	Object resolveDependency(DependencyDescriptor descriptor, String beanName)
			throws BeansException;

	// -------------------------------------------------------------------------
	// Specialized methods for fine-grained control over the bean lifecycle
	// -------------------------------------------------------------------------

	/**
	 * 根绝指定的策略完全创建一个新的bean实例 ,在该接口中定义的所有常量这里支持。<br>
	 * 初始化所有的 {@link BeanPostProcessor BeanPostProcessors}. 这实际上时由{@link #autowire}提供的添加
	 * {@link #initializeBean}的行为.
	 * 
	 * @param beanClass 要创建Bean的Class
	 * @param autowireMode 名称或者类型在此接口使用常量
	 * @param dependencyCheck 是否执行一个对象的相关性检查 (并不适用于自动装配一个构造函数)
	 * @return 新的bean实例
	 * @see #AUTOWIRE_NO
	 * @see #AUTOWIRE_BY_NAME
	 * @see #AUTOWIRE_BY_TYPE
	 * @see #AUTOWIRE_CONSTRUCTOR
	 */
	Object createBean(Class<?> beanClass, int autowireMode, boolean dependencyCheck)
			throws BeansException;

	/**
	 * Instantiate a new bean instance of the given class with the specified autowire
	 * strategy. All constants defined in this interface are supported here. Can also be
	 * invoked with {@code AUTOWIRE_NO} in order to just apply before-instantiation
	 * callbacks (e.g. for annotation-driven injection).
	 * <p>
	 * Does <i>not</i> apply standard {@link BeanPostProcessor BeanPostProcessors}
	 * callbacks or perform any further initialization of the bean. This interface offers
	 * distinct, fine-grained operations for those purposes, for example
	 * {@link #initializeBean}. However, {@link InstantiationAwareBeanPostProcessor}
	 * callbacks are applied, if applicable to the construction of the instance.
	 * 
	 * @param beanClass the class of the bean to instantiate
	 * @param autowireMode by name or type, using the constants in this interface
	 * @param dependencyCheck whether to perform a dependency check for object references
	 *        in the bean instance (not applicable to autowiring a constructor, thus
	 *        ignored there)
	 * @return the new bean instance
	 * @throws BeansException if instantiation or wiring failed
	 * @see #AUTOWIRE_NO
	 * @see #AUTOWIRE_BY_NAME
	 * @see #AUTOWIRE_BY_TYPE
	 * @see #AUTOWIRE_CONSTRUCTOR
	 * @see #AUTOWIRE_AUTODETECT
	 * @see #initializeBean
	 * @see #applyBeanPostProcessorsBeforeInitialization
	 * @see #applyBeanPostProcessorsAfterInitialization
	 */
	Object autowire(Class<?> beanClass, int autowireMode, boolean dependencyCheck)
			throws BeansException;

	/**
	 * Autowire the bean properties of the given bean instance by name or type. Can also
	 * be invoked with {@code AUTOWIRE_NO} in order to just apply after-instantiation
	 * callbacks (e.g. for annotation-driven injection).
	 * <p>
	 * Does <i>not</i> apply standard {@link BeanPostProcessor BeanPostProcessors}
	 * callbacks or perform any further initialization of the bean. This interface offers
	 * distinct, fine-grained operations for those purposes, for example
	 * {@link #initializeBean}. However, {@link InstantiationAwareBeanPostProcessor}
	 * callbacks are applied, if applicable to the configuration of the instance.
	 * 
	 * @param existingBean the existing bean instance
	 * @param autowireMode by name or type, using the constants in this interface
	 * @param dependencyCheck whether to perform a dependency check for object references
	 *        in the bean instance
	 * @throws BeansException if wiring failed
	 * @see #AUTOWIRE_BY_NAME
	 * @see #AUTOWIRE_BY_TYPE
	 * @see #AUTOWIRE_NO
	 */
	void autowireBeanProperties(Object existingBean, int autowireMode,
			boolean dependencyCheck) throws BeansException;

	/**
	 * Apply the property values of the bean definition with the given name to the given
	 * bean instance. The bean definition can either define a fully self-contained bean,
	 * reusing its property values, or just property values meant to be used for existing
	 * bean instances.
	 * <p>
	 * This method does <i>not</i> autowire bean properties; it just applies explicitly
	 * defined property values. Use the {@link #autowireBeanProperties} method to autowire
	 * an existing bean instance. <b>Note: This method requires a bean definition for the
	 * given name!</b>
	 * <p>
	 * Does <i>not</i> apply standard {@link BeanPostProcessor BeanPostProcessors}
	 * callbacks or perform any further initialization of the bean. This interface offers
	 * distinct, fine-grained operations for those purposes, for example
	 * {@link #initializeBean}. However, {@link InstantiationAwareBeanPostProcessor}
	 * callbacks are applied, if applicable to the configuration of the instance.
	 * 
	 * @param existingBean the existing bean instance
	 * @param beanName the name of the bean definition in the bean factory (a bean
	 *        definition of that name has to be available)
	 * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException if there is
	 *         no bean definition with the given name
	 * @throws BeansException if applying the property values failed
	 * @see #autowireBeanProperties
	 */
	void applyBeanPropertyValues(Object existingBean, String beanName)
			throws BeansException;

	/**
	 * Initialize the given raw bean, applying factory callbacks such as
	 * {@code setBeanName} and {@code setBeanFactory}, also applying all bean post
	 * processors (including ones which might wrap the given raw bean).
	 * <p>
	 * Note that no bean definition of the given name has to exist in the bean factory.
	 * The passed-in bean name will simply be used for callbacks but not checked against
	 * the registered bean definitions.
	 * 
	 * @param existingBean the existing bean instance
	 * @param beanName the name of the bean, to be passed to it if necessary (only passed
	 *        to {@link BeanPostProcessor BeanPostProcessors})
	 * @return the bean instance to use, either the original or a wrapped one
	 * @throws BeansException if the initialization failed
	 */
	Object initializeBean(Object existingBean, String beanName) throws BeansException;

	/**
	 * Apply {@link BeanPostProcessor BeanPostProcessors} to the given existing bean
	 * instance, invoking their {@code postProcessBeforeInitialization} methods. The
	 * returned bean instance may be a wrapper around the original.
	 * 
	 * @param existingBean the new bean instance
	 * @param beanName the name of the bean
	 * @return the bean instance to use, either the original or a wrapped one
	 * @throws BeansException if any post-processing failed
	 * @see BeanPostProcessor#postProcessBeforeInitialization
	 */
	Object applyBeanPostProcessorsBeforeInitialization(Object existingBean,
			String beanName) throws BeansException;

	/**
	 * 调用其 @code postProcessAfterInitialization 方法,申请 @code BeanPostProcessor 给现有的 Bean实例,
	 * The returned bean instance may be a wrapper around the original.
	 */
	Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String beanName)
			throws BeansException;

	/**
	 * 销毁指定的Bean
	 */
	void destroyBean(Object existingBean);

	/**
	 * 对bean指定依赖
	 */
	Object resolveDependency(DependencyDescriptor descriptor, String beanName,
			Set<String> autowiredBeanNames, TypeConverter typeConverter)
			throws BeansException;

}
