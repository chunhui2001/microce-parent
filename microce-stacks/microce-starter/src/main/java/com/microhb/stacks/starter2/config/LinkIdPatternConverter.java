package com.microce.stacks.starter.config;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.core.pattern.PatternConverter;

import com.microce.stacks.starter.requests.RequestAssets;

/**
 * 此类用于向log4j2.xml 文件传递 lid
 * @author keesh
 */
@Plugin(name = "LinkIdPatternConverter", category = PatternConverter.CATEGORY)
@ConverterKeys({ "lid", "linkId" })
public class LinkIdPatternConverter extends LogEventPatternConverter {

	private static final LinkIdPatternConverter INSTANCE = new LinkIdPatternConverter();

	public static LinkIdPatternConverter newInstance(final String[] options) {
		return INSTANCE;
	}

	private LinkIdPatternConverter() {
		super("LinkId", "linkId");
	}

	@Override
	public void format(LogEvent event, StringBuilder toAppendTo) {
		String linkId = ThreadContext.get(RequestAssets.REQUEST_LINK_ID);
		toAppendTo.append(StringUtils.isBlank(linkId) ? "(N/a)" : linkId);
	}

}
