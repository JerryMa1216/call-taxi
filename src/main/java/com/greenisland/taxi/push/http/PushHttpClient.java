package com.greenisland.taxi.push.http;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.greenisland.taxi.push.auth.PushCredentials;
import com.greenisland.taxi.push.auth.PushSigner;
import com.greenisland.taxi.push.handler.ErrorResponseHandler;
import com.greenisland.taxi.push.handler.HttpResponseHandler;
import com.greenisland.taxi.push.request.BaiduPushRequest;
import com.greenisland.taxi.push.response.PushResponse;
import com.greenisland.taxi.push.util.PushClientException;
import com.greenisland.taxi.push.util.PushServiceException;


/**
 * httpRequest的发送者
 * @author liyazhou@baidu.com
 *
 */
public class PushHttpClient {
	private static final Logger logger = Logger.getLogger(PushHttpClient.class);
	
	private HttpClient httpClient;
	private HttpRequestFactory httpRequestFactory = new HttpRequestFactory();
	private HttpClientFactory clientFactory = new HttpClientFactory();
	private ErrorResponseHandler<PushServiceException> errorResponseHandler = new ErrorResponseHandler<PushServiceException>();
	private PushCredentials credentials;
	
	public PushHttpClient(PushCredentials credentials) {
		this.credentials = credentials;
	}

	private PushHttpResponse createPushHttpResponse(HttpResponse httpResponse) throws IllegalStateException, IOException {
		PushHttpResponse pushHttpResponse = new PushHttpResponse();

		HttpEntity entity = httpResponse.getEntity();
		if (null != entity) {
			String body = EntityUtils.toString(entity);
			if (entity != null) {
				EntityUtils.consume(entity);
			}
			
			pushHttpResponse.setContent(body);
		}
		
		pushHttpResponse.setStatusCode(httpResponse.getStatusLine().getStatusCode());
		pushHttpResponse.setStatusText(httpResponse.getStatusLine().getReasonPhrase());

		for (Header header : httpResponse.getAllHeaders()) {
			pushHttpResponse.addHeader(header.getName(), header.getValue());
		}
		return pushHttpResponse;
	}
	
	public <T> PushResponse<T> execute(PushHttpRequest pushHttpRequest, 
			HttpResponseHandler<T> responseHandler) {
		BaiduPushRequest originalRequest = pushHttpRequest.getOriginalRequest();
		pushHttpRequest.setHttpMethod(originalRequest.getHttpMethod());
		
		// sign
		logger.trace("begin sign");
		PushSigner.sign(pushHttpRequest.getOriginalRequest(), pushHttpRequest, credentials);
		logger.trace("after sign");
		
		logger.info("httpRequest:"+pushHttpRequest);
		HttpRequestBase httpRequest = this.httpRequestFactory.createHttpRequestBase(pushHttpRequest);
		this.httpClient = this.clientFactory.createHttpClient();
		HttpResponse httpResponse;
		try {
			httpResponse = this.httpClient.execute(httpRequest);
		} catch (ClientProtocolException e) {
			throw new PushClientException("Send to server failed.", e);
		} catch (IOException e) {
			throw new PushClientException("Send to server failed.", e);
		}
		try {
			if (this.isRequestSuccessful(httpResponse)) {
				return this.handleHttpResponse(httpResponse, responseHandler);
			} else {
				throw this.handleErrorHttpResponse(httpResponse, this.errorResponseHandler).getResult();
			}
		} catch (IllegalStateException e) {
			throw new PushClientException("Handle http response failed.", e);
		} catch (IOException e) {
			throw new PushClientException("Handle http response failed.", e);
		} finally {
			/*
			 * Some response handlers need to manually manage the HTTP
			 * connection and will take care of releasing the connection on
			 * their own, but if this response handler doesn't need the
			 * connection left open, we go ahead and release the it to free up
			 * resources.
			 */
			if (!responseHandler.isNeedsConnectionLeftOpen()) {
				try {
					httpResponse.getEntity().getContent().close();
				} catch (Throwable t) {

				}
			}

		}

	}
	
	private PushResponse<PushServiceException> handleErrorHttpResponse(HttpResponse httpResponse,
			ErrorResponseHandler<PushServiceException> errorResponseHandler) throws IllegalStateException, IOException {
		PushHttpResponse pushHttpResponse = this.createPushHttpResponse(httpResponse);
		return errorResponseHandler.handle(pushHttpResponse);
	}

	private <T> PushResponse<T> handleHttpResponse(HttpResponse httpResponse, HttpResponseHandler<T> responseHandler)
			throws IllegalStateException, IOException {
		PushHttpResponse pushHttpResponse = this.createPushHttpResponse(httpResponse);
		return responseHandler.handle(pushHttpResponse);
	}
	
	private boolean isRequestSuccessful(HttpResponse response) {
		int status = response.getStatusLine().getStatusCode();
		return (status / 100) == 2;
	}
}
