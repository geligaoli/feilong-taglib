/*
 * Copyright (C) 2008 feilong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.feilong.taglib.display.httpconcat.builder;

import static com.feilong.core.Validator.isNotNullOrEmpty;
import static com.feilong.core.Validator.isNullOrEmpty;
import static com.feilong.taglib.display.httpconcat.builder.HttpConcatGlobalConfigBuilder.GLOBAL_CONFIG;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.util.Date;

import javax.servlet.jsp.PageContext;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.feilong.core.date.DateUtil;
import com.feilong.taglib.TagUtils;

/**
 * The Class VersionRebuilder.
 *
 * @author <a href="http://feitianbenyue.iteye.com/">feilong</a>
 */
public class VersionRebuilder{

    /** The Constant LOGGER. */
    private static final Logger LOGGER = LoggerFactory.getLogger(VersionRebuilder.class);

    /** Don't let anyone instantiate this class. */
    private VersionRebuilder(){
        //AssertionError不是必须的. 但它可以避免不小心在类的内部调用构造器. 保证该类在任何情况下都不会被实例化.
        //see 《Effective Java》 2nd
        throw new AssertionError("No " + getClass().getName() + " instances for you!");
    }

    //---------------------------------------------------------------

    /**
     * Rebuild version.
     *
     * @param version
     *            the version
     * @param pageContext
     *            the page context
     * @return the string
     */
    public static String rebuild(String version,PageContext pageContext){
        String versionValue = build(version, pageContext);

        return doWithAutoRefresh(versionValue);
    }

    //---------------------------------------------------------------

    /**
     * Do with auto refresh.
     *
     * @param versionValue
     *            the version value
     * @return the string
     */
    static String doWithAutoRefresh(String versionValue){
        String versionAutoRefreshValue = GLOBAL_CONFIG.getVersionAutoRefreshValue();

        //当version 值是指定value的时候,每次自动变
        if (isNotNullOrEmpty(versionAutoRefreshValue) && StringUtils.equals(versionValue, versionAutoRefreshValue)){
            long time = DateUtil.getTime(new Date());

            String result = "" + time;
            LOGGER.debug("versionAutoRefreshValue config:[{}],new value is:[{}]", versionAutoRefreshValue, result);
            return result;
        }
        return versionValue;
    }

    //---------------------------------------------------------------

    /**
     * Builds the.
     *
     * @param version
     *            the version
     * @param pageContext
     *            the page context
     * @return the string
     */
    private static String build(String version,PageContext pageContext){
        //不是空 那么直接返回
        if (isNotNullOrEmpty(version)){
            return version;
        }

        //---------------------------------------------------------------
        //没有指定名字,直接返回
        String versionNameInScope = GLOBAL_CONFIG.getVersionNameInScope();
        if (isNullOrEmpty(versionNameInScope)){
            LOGGER.debug("version is null or empty, but can not find versionNameInScope in GLOBAL_CONFIG");
            return EMPTY;
        }

        //---------------------------------------------------------------
        String versionSearchScope = GLOBAL_CONFIG.getVersionSearchScope();

        String versionValue = "";
        //没有指定scope 那么从pageContext中查找
        if (isNullOrEmpty(versionSearchScope)){
            versionValue = (String) pageContext.findAttribute(versionNameInScope);
        }else{
            //如果指定了, 那么从指定的scope中获取
            versionValue = (String) pageContext.getAttribute(versionNameInScope, TagUtils.getScope(versionSearchScope));
        }

        //---------------------------------------------------------------
        //如果找不到值, 那么返回 empty
        if (isNullOrEmpty(versionValue)){
            LOGGER.warn(
                            "in GLOBAL_CONFIG,versionNameInScope:[{}] searchScope:[{}],but can't get value",
                            versionNameInScope,
                            versionSearchScope);
            return EMPTY;
        }
        //---------------------------------------------------------------
        LOGGER.debug("in Scope:[{}] find name:[{}],versionValue:[{}]", versionSearchScope, versionNameInScope, versionValue);
        return versionValue;
    }
}
