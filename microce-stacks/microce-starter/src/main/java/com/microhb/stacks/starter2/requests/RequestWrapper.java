package com.microce.stacks.starter.requests;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

public class RequestWrapper extends HttpServletRequestWrapper {

	private final byte[] body;
	private boolean isMultipart;

	/**
	 * construct a wrapper for this request
	 * 
	 * @param request
	 * @throws IOException
	 */
	public RequestWrapper(HttpServletRequest request) throws IOException {
		
		super(request);
		isMultipart = StringUtils.startsWithIgnoreCase(request.getContentType(), "multipart/");
		
		if (isMultipart) {
			body = null;
		} else {
			body = IOUtils.toByteArray(super.getInputStream());
		}

	}

	private Map<String, String> headerMap = new HashMap<String, String>();

	/**
	 * add a header with given name and value
	 *
	 * @param name
	 * @param value
	 */
	public void addHeader(String name, String value) {
		headerMap.put(name, value);
	}

	@Override
	public String getHeader(String name) {
		String headerValue = super.getHeader(name);
		if (headerMap.containsKey(name)) {
			headerValue = headerMap.get(name);
		}
		return headerValue;
	}

	/**
	 * get the Header names
	 */
	@Override
	public Enumeration<String> getHeaderNames() {
		List<String> names = Collections.list(super.getHeaderNames());
		for (String name : headerMap.keySet()) {
			names.add(name);
		}
		return Collections.enumeration(names);
	}

	@Override
	public Enumeration<String> getHeaders(String name) {
		List<String> values = Collections.list(super.getHeaders(name));
		if (headerMap.containsKey(name)) {
			values.add(headerMap.get(name));
		}
		return Collections.enumeration(values);
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		if (body == null) {
			return super.getInputStream();
		}
		return new RequestBodyCachingInputStream(body);
	}

	public String getRequestBody() throws IOException {
		if (isMultipart) {
			return "(multipart)";
		} else if (body != null) {
			return new String(body);
		} else {
			return "(N/a)";
		}
	}

	private class RequestBodyCachingInputStream extends ServletInputStream {

		private byte[] body;

		private int lastIndexRetrieved = -1;

		private ReadListener listener;

		private RequestBodyCachingInputStream(byte[] body) {
			this.body = body;
		}

		@Override
		public int read() throws IOException {
			if (isFinished()) {
				return -1;
			}
			int i = body[lastIndexRetrieved + 1];
			lastIndexRetrieved++;
			if (isFinished() && listener != null) {
				try {
					listener.onAllDataRead();
				} catch (IOException e) {
					listener.onError(e);
					throw e;
				}
			}
			return i;
		}

		@Override
		public boolean isFinished() {
			return lastIndexRetrieved == body.length - 1;
		}

		@Override
		public boolean isReady() {
			// This implementation will never block
			// We also never need to call the readListener from this method, as this method
			// will never return false
			return isFinished();
		}

		@Override
		public void setReadListener(ReadListener listener) {
			if (listener == null) {
				throw new IllegalArgumentException("listener cann not be null");
			}
			if (this.listener != null) {
				throw new IllegalArgumentException("listener has been set");
			}
			this.listener = listener;
			try {
				listener.onAllDataRead();
			} catch (IOException e) {
				listener.onError(e);
			}
		}

		@Override
		public int available() {
			return body.length - lastIndexRetrieved - 1;
		}

		@Override
		public void close() {
			lastIndexRetrieved = body.length - 1;
			body = null;
		}
	}

}