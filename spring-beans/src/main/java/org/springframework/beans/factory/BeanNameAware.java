/*
 * Copyright 2002-2011 the original author or authors.
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
 * Interface to be implemented by beans that want to be aware of their bean name in a bean
 * factory. Note that it is not usually recomm ended that an object depend on its bean
 * name, as this represents a potentially brittle dependence on external configuration, as
 * well as a possibly unnecessary dependence on a Spring API.
 *
 * <p>
 * For a list of all bean lifecycle methods, see the {@link BeanFactory BeanFactory
 * javadocs}.
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 01.11.2003
 * @see BeanClassLoaderAware
 * @see BeanFactoryAware
 * @see InitializingBean
 */
// BeanNameAware回调接口
public interface BeanNameAware extends Aware {

	/**
	 * 这是第一步 如果受管Bean实现了BeanNameAware回调接口，则受管 Bean本身的id（name）会被BeanFactory注入到受管Bean中 <br>
	 */
	void setBeanName(String name);
}
