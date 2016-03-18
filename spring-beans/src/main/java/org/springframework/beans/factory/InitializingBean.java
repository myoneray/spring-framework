/*
 * Copyright 2002-2012 the original author or authors.
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

/**
 * Interface to be implemented by beans that need to react once all their properties have
 * been set by a BeanFactory: for example, to perform custom initialization, or merely to
 * check that all mandatory properties have been set.
 *
 * <p>
 * An alternative to implementing InitializingBean is specifying a custom init-method, for
 * example in an XML bean definition. For a list of all bean lifecycle methods, see the
 * BeanFactory javadocs.
 *
 * @author Rod Johnson
 * @see BeanNameAware
 * @see BeanFactoryAware
 * @see BeanFactory
 * @see org.springframework.beans.factory.support.RootBeanDefinition#getInitMethodName
 * @see org.springframework.context.ApplicationContextAware
 */
/**
 * InitializingBean接口为bean提供了初始化方法的方式，它只包括afterPropertiesSet方法，
 * 凡是继承该接口的类，在初始化bean的时候会执行该方法 。
 * 
 * @see http://blog.csdn.net/mqboss/article/details/7452331
 * 
 * @author upsmart
 * @since 4.2.1
 */
public interface InitializingBean {

	/**
	 * Invoked by a BeanFactory after it has set all bean properties supplied (and
	 * satisfied BeanFactoryAware and ApplicationContextAware).
	 * <p>
	 * This method allows the bean instance to perform initialization only possible when
	 * all bean properties have been set and to throw an exception in the event of
	 * misconfiguration.
	 * 
	 * @throws Exception in the event of misconfiguration (such as failure to set an
	 *         essential property) or if initialization fails.
	 */
	// 这是第十步
	void afterPropertiesSet() throws Exception;
}

// 比如，TestBean实现了BeanNameAware回调接口，示例代码如下。
// public class TestBean implements ITestBean, BeanNameAware {
// private String beanName;
// public void setBeanName(String name) {
// this.beanName = name;
// }
// }