/*
 * Copyright 2002-2016 the original author or authors.
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

package org.springframework.beans.factory.xml;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

/**
 * Default implementation of the {@link BeanDefinitionDocumentReader} interface that reads
 * bean definitions according to the "spring-beans" DTD and XSD format (Spring's default
 * XML bean definition format).
 *
 * <p>
 * The structure, elements, and attribute names of the required XML document are
 * hard-coded in this class. (Of course a transform could be run if necessary to produce
 * this format). {@code <beans>} does not need to be the root element of the XML document:
 * this class will parse all bean definition elements in the XML file, regardless of the
 * actual root element.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Erik Wiersma
 * @since 18.12.2003
 */
public class DefaultBeanDefinitionDocumentReader implements BeanDefinitionDocumentReader {

	public static final String BEAN_ELEMENT = BeanDefinitionParserDelegate.BEAN_ELEMENT;

	public static final String NESTED_BEANS_ELEMENT = "beans";

	public static final String ALIAS_ELEMENT = "alias";

	public static final String NAME_ATTRIBUTE = "name";

	public static final String ALIAS_ATTRIBUTE = "alias";

	public static final String IMPORT_ELEMENT = "import";

	public static final String RESOURCE_ATTRIBUTE = "resource";

	public static final String PROFILE_ATTRIBUTE = "profile";

	protected final Log logger = LogFactory.getLog(getClass());

	private XmlReaderContext readerContext;

	private BeanDefinitionParserDelegate delegate;

	/**
	 * This implementation parses bean definitions according to the "spring-beans" XSD (or
	 * DTD, historically).
	 * <p>
	 * Opens a DOM Document; then initializes the default settings specified at the
	 * {@code <beans/>} level; then parses the contained bean definitions.
	 */
	/* 提取Root,以便於再次將Root作爲參數繼續 BeanDefinition */
	@Override
	public void registerBeanDefinitions(Document doc, XmlReaderContext readerContext) {
		this.readerContext = readerContext;
		logger.debug("Loading bean definitions");
		Element root = doc.getDocumentElement();
		/* 真正的加載解析XML */
		doRegisterBeanDefinitions(root);
	}

	/**
	 * Return the descriptor for the XML resource that this parser works on.
	 */
	protected final XmlReaderContext getReaderContext() {
		return this.readerContext;
	}

	/**
	 * Invoke the {@link org.springframework.beans.factory.parsing.SourceExtractor} to
	 * pull the source metadata from the supplied {@link Element}.
	 */
	protected Object extractSource(Element ele) {
		return getReaderContext().extractSource(ele);
	}

	/**
	 * Register each bean definition within the given root {@code <beans/>} element.
	 */
	/* 真正的加載解析XML */
	protected void doRegisterBeanDefinitions(Element root) {

		/* 这里创建了一个BeanDefinitionParserDelegate示例，解析XML的过程就是委托它完成的 */
		BeanDefinitionParserDelegate parent = this.delegate;
		this.delegate = createDelegate(getReaderContext(), root, parent);

		if (this.delegate.isDefaultNamespace(root)) {
			/**
			 * PROFILE_ATTRIBUTE="profile"
			 * 
			 * @see http://radiumxie.iteye.com/blog/1851919
			 */
			String profileSpec = root.getAttribute(PROFILE_ATTRIBUTE);
			/* 解析 profile 屬性開始 */
			if (StringUtils.hasText(profileSpec)) {
				String[] specifiedProfiles = StringUtils.tokenizeToStringArray(
						profileSpec,
						BeanDefinitionParserDelegate.MULTI_VALUE_ATTRIBUTE_DELIMITERS);

				if (!getReaderContext().getEnvironment().acceptsProfiles(
						specifiedProfiles)) {

					if (logger.isInfoEnabled()) {
						logger.info("Skipped XML bean definition file due to specified profiles ["
								+ profileSpec
								+ "] not matching: "
								+ getReaderContext().getResource());
					}
					return;
				}
			}
			/* 解析 profile 屬性結束 */
		}
		/* 設計模式>模板方法: 解析前處理留給子類實現 */
		preProcessXml(root);
		/* 該方法用實現加載類 */
		parseBeanDefinitions(root, this.delegate);
		/* 設計模式>模板方法: 解析後處理留給子類實現 */
		postProcessXml(root);
		this.delegate = parent;
	}

	protected BeanDefinitionParserDelegate createDelegate(XmlReaderContext readerContext,
			Element root, BeanDefinitionParserDelegate parentDelegate) {

		BeanDefinitionParserDelegate delegate = new BeanDefinitionParserDelegate(
				readerContext);
		delegate.initDefaults(root, parentDelegate);
		return delegate;
	}

	/**
	 * Parse the elements at the root level in the document: "import", "alias", "bean".
	 * 
	 * @param root the DOM root element of the document
	 */
	protected void parseBeanDefinitions(Element root,
			BeanDefinitionParserDelegate delegate) {
		/* 處理Bean: 判斷根節點是默認命名空間 */
		if (delegate.isDefaultNamespace(root)) {
			NodeList nl = root.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				Node node = nl.item(i);
				if (node instanceof Element) {
					Element ele = (Element) node;
					/* 處理Bean: 判斷根節點是默認命名空間 */
					if (delegate.isDefaultNamespace(ele)) {
						/* Spring默認解析XML標籤的方法 */
						parseDefaultElement(ele, delegate);
					}
					else {
						/* 最终解析XML元素的是delegate.parseCustomElement(ele)方法 */
						delegate.parseCustomElement(ele);
					}
				}
			}
		}
		else {
			/* 處理Bean:根節點不是默認命名空間 */
			delegate.parseCustomElement(root);
		}
	}

	/**
	 * Spring默認解析XML標籤的方法
	 * 
	 * @param ele
	 * @param delegate
	 */
	private void parseDefaultElement(Element ele, BeanDefinitionParserDelegate delegate) {
		// 對import標籤解析
		if (delegate.nodeNameEquals(ele, IMPORT_ELEMENT)) {
			importBeanDefinitionResource(ele);
		}
		// 對alias標籤解析
		else if (delegate.nodeNameEquals(ele, ALIAS_ELEMENT)) {
			processAliasRegistration(ele);
		}
		// 對bean標籤解析
		else if (delegate.nodeNameEquals(ele, BEAN_ELEMENT)) {
			processBeanDefinition(ele, delegate);
		}
		// 對beans標籤解析
		else if (delegate.nodeNameEquals(ele, NESTED_BEANS_ELEMENT)) {
			// recurse
			doRegisterBeanDefinitions(ele);
		}
	}

	/**
	 * Parse an "import" element and load the bean definitions from the given resource
	 * into the bean factory.
	 */
	// 解析import標籤
	protected void importBeanDefinitionResource(Element ele) {
		String location = ele.getAttribute(RESOURCE_ATTRIBUTE);
		if (!StringUtils.hasText(location)) {
			getReaderContext().error("Resource location must not be empty", ele);
			return;
		}

		// Resolve system properties: e.g. "${user.dir}"
		location = getReaderContext().getEnvironment().resolveRequiredPlaceholders(
				location);

		Set<Resource> actualResources = new LinkedHashSet<Resource>(4);

		// Discover whether the location is an absolute or relative URI
		boolean absoluteLocation = false;
		try {
			absoluteLocation = ResourcePatternUtils.isUrl(location)
					|| ResourceUtils.toURI(location).isAbsolute();
		}
		catch (URISyntaxException ex) {
			// cannot convert to an URI, considering the location relative
			// unless it is the well-known Spring prefix "classpath*:"
		}

		// Absolute or relative?
		if (absoluteLocation) {
			try {
				int importCount = getReaderContext().getReader().loadBeanDefinitions(
						location, actualResources);
				if (logger.isDebugEnabled()) {
					logger.debug("Imported " + importCount
							+ " bean definitions from URL location [" + location + "]");
				}
			}
			catch (BeanDefinitionStoreException ex) {
				getReaderContext().error(
						"Failed to import bean definitions from URL location ["
								+ location + "]", ele, ex);
			}
		}
		else {
			// No URL -> considering resource location as relative to the current file.
			try {
				int importCount;
				Resource relativeResource = getReaderContext().getResource().createRelative(
						location);
				if (relativeResource.exists()) {
					importCount = getReaderContext().getReader().loadBeanDefinitions(
							relativeResource);
					actualResources.add(relativeResource);
				}
				else {
					String baseLocation = getReaderContext().getResource().getURL().toString();
					importCount = getReaderContext().getReader().loadBeanDefinitions(
							StringUtils.applyRelativePath(baseLocation, location),
							actualResources);
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Imported " + importCount
							+ " bean definitions from relative location [" + location
							+ "]");
				}
			}
			catch (IOException ex) {
				getReaderContext().error("Failed to resolve current resource location",
						ele, ex);
			}
			catch (BeanDefinitionStoreException ex) {
				getReaderContext().error(
						"Failed to import bean definitions from relative location ["
								+ location + "]", ele, ex);
			}
		}
		Resource[] actResArray = actualResources.toArray(new Resource[actualResources.size()]);
		getReaderContext().fireImportProcessed(location, actResArray, extractSource(ele));
	}

	// 解析Alias標籤
	protected void processAliasRegistration(Element ele) {
		String name = ele.getAttribute(NAME_ATTRIBUTE);
		String alias = ele.getAttribute(ALIAS_ATTRIBUTE);
		boolean valid = true;
		if (!StringUtils.hasText(name)) {
			getReaderContext().error("Name must not be empty", ele);
			valid = false;
		}
		if (!StringUtils.hasText(alias)) {
			getReaderContext().error("Alias must not be empty", ele);
			valid = false;
		}
		if (valid) {
			try {
				getReaderContext().getRegistry().registerAlias(name, alias);
			}
			catch (Exception ex) {
				getReaderContext().error(
						"Failed to register alias '" + alias + "' for bean with name '"
								+ name + "'", ele, ex);
			}
			getReaderContext().fireAliasRegistered(name, alias, extractSource(ele));
		}
	}

	/**
	 * Process the given bean element, parsing the bean definition and registering it with
	 * the registry.
	 */
	// 解析Bean標籤
	protected void processBeanDefinition(Element ele,
			BeanDefinitionParserDelegate delegate) {

		/* 首先委託BeanDefinitionParserDelegate的parseBeanDefinitionElement方法進行元素解析 <br> */
		/* 處理完成bdHolder包含了配置文件的各種屬性 class,name,id,alias..... */
		BeanDefinitionHolder bdHolder = delegate.parseBeanDefinitionElement(ele);

		if (bdHolder != null) {
			// 若默認標籤下的子標籤包含自定義標籤,還需要對自定義標籤解析.
			bdHolder = delegate.decorateBeanDefinitionIfRequired(ele, bdHolder);
			try {
				// Register the final decorated instance.
				// 解析完成,需要委託BeanDefinitionReaderUtils的registerBeanDefinition方法對bdHolder進行註冊
				BeanDefinitionReaderUtils.registerBeanDefinition(bdHolder,
						getReaderContext().getRegistry());
			}
			catch (BeanDefinitionStoreException ex) {
				getReaderContext().error(
						"Failed to register bean definition with name '"
								+ bdHolder.getBeanName() + "'", ele, ex);
			}
			// Send registration event.
			// 發送响应事件,通知相關的監聽器這個bean已經加載完成
			getReaderContext().fireComponentRegistered(
					new BeanComponentDefinition(bdHolder));
		}
	}

	/**
	 * Allow the XML to be extensible by processing any custom element types first, before
	 * we start to process the bean definitions. This method is a natural extension point
	 * for any other custom pre-processing of the XML.
	 * <p>
	 * The default implementation is empty. Subclasses can override this method to convert
	 * custom elements into standard Spring bean definitions, for example. Implementors
	 * have access to the parser's bean definition reader and the underlying XML resource,
	 * through the corresponding accessors.
	 * 
	 * @see #getReaderContext()
	 */
	protected void preProcessXml(Element root) {
	}

	/**
	 * Allow the XML to be extensible by processing any custom element types last, after
	 * we finished processing the bean definitions. This method is a natural extension
	 * point for any other custom post-processing of the XML.
	 * <p>
	 * The default implementation is empty. Subclasses can override this method to convert
	 * custom elements into standard Spring bean definitions, for example. Implementors
	 * have access to the parser's bean definition reader and the underlying XML resource,
	 * through the corresponding accessors.
	 * 
	 * @see #getReaderContext()
	 */
	protected void postProcessXml(Element root) {
	}

}
