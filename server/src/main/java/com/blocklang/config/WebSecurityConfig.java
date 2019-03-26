package com.blocklang.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequestEntityConverter;
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequestEntityConverter;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import com.blocklang.config.oauth2.qq.QqOauth2UserService;
import com.blocklang.core.constant.OauthSite;
import com.blocklang.core.constant.WebSite;
import com.blocklang.core.filter.RouterFilter;
import com.blocklang.core.model.UserInfo;
import com.blocklang.core.service.GithubLoginService;
import com.blocklang.core.service.QqLoginService;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private GithubLoginService githubLoginService;
	@Autowired
	private QqLoginService qqLoginService;
	
	// 支持 oauth2 client
	@Override
	protected void configure(HttpSecurity http){
		// We recommend disabling CSRF protection completely only if you are creating a
		// service that is used by non-browser clients
		// TODO: 考虑通过将服务拆分到单独项目中，然后打开此功能
		try {
			http.csrf().disable();
			http.addFilterBefore(new RouterFilter(), AnonymousAuthenticationFilter.class);
			
//		http.authorizeRequests().anyRequest().authenticated().and().oauth2Login().loginPage("/")
//		.and().oauth2Client();
			http//.authorizeRequests().antMatchers("/user").permitAll().anyRequest().authenticated().and()
				.logout()
					.logoutSuccessUrl(WebSite.HOME_URL) // 因为是 single page app，所以注销成功后返回首页
					.and()
				.oauth2Login()
					.loginPage(WebSite.HOME_URL) // 因为是 single page app，所以将登录页设置为首页
					.tokenEndpoint().accessTokenResponseClient(new MyAuthorizationCodeTokenResponseClient())
					.and()
					.userInfoEndpoint()
						.userService(this.oauth2UserService());
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
//	private OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
//		DefaultAuthorizationCodeTokenResponseClient accessTokenResponseClient = new DefaultAuthorizationCodeTokenResponseClient();
//		System.out.println("---------------accessTokenResponseClient--------------");
//		return accessTokenResponseClient;
//	}
	
	public class MyAuthorizationCodeTokenResponseClient 
	implements OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> {


		private static final String INVALID_TOKEN_RESPONSE_ERROR_CODE = "invalid_token_response";

		private Converter<OAuth2AuthorizationCodeGrantRequest, RequestEntity<?>> requestEntityConverter =
				new CustomOAuth2AuthorizationCodeGrantRequestEntityConverter();

		private RestOperations restOperations;

		public MyAuthorizationCodeTokenResponseClient() {
//			OAuth2AccessTokenResponseHttpMessageConverter tokenResponseHttpMessageConverter = new OAuth2AccessTokenResponseHttpMessageConverter();
//			tokenResponseHttpMessageConverter.setSupportedMediaTypes(Collections.singletonList(MediaType.TEXT_HTML));
//			tokenResponseHttpMessageConverter.setTokenResponseConverter(accessTokenResponseConverter());
			
			QqAccessTokenResponseHttpMessageConverter qqAccessTokenResponseHttpMessageConverter = new QqAccessTokenResponseHttpMessageConverter();
			qqAccessTokenResponseHttpMessageConverter.setSupportedMediaTypes(Collections.singletonList(MediaType.TEXT_HTML));
			qqAccessTokenResponseHttpMessageConverter.setTokenResponseConverter(accessTokenResponseConverter());
			
			RestTemplate restTemplate = new RestTemplate(Arrays.asList(qqAccessTokenResponseHttpMessageConverter));
			restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());
			this.restOperations = restTemplate;
		}

		private Converter<Map<String, String>, OAuth2AccessTokenResponse> accessTokenResponseConverter() {
			final Set<String> tokenResponseParameterNames = Stream.of(OAuth2ParameterNames.ACCESS_TOKEN,
					OAuth2ParameterNames.EXPIRES_IN, OAuth2ParameterNames.REFRESH_TOKEN, OAuth2ParameterNames.SCOPE)
					.collect(Collectors.toSet());
			
			return tokenResponseParameters -> {
				System.out.println("----------tokenResponseParameters:" + tokenResponseParameters);
				String accessToken = tokenResponseParameters.get(OAuth2ParameterNames.ACCESS_TOKEN);

				long expiresIn = tokenResponseParameters.containsKey(OAuth2ParameterNames.EXPIRES_IN)
						? Long.valueOf(tokenResponseParameters.get(OAuth2ParameterNames.EXPIRES_IN))
						: 0;

				Set<String> scopes = tokenResponseParameters.containsKey(OAuth2ParameterNames.SCOPE)
						? Arrays.stream(StringUtils.delimitedListToStringArray(
								tokenResponseParameters.get(OAuth2ParameterNames.SCOPE), " ")).collect(
										Collectors.toSet())
						: Collections.emptySet();

				String refreshToken = tokenResponseParameters.get(OAuth2ParameterNames.REFRESH_TOKEN);
				Map<String, Object> additionalParameters = new LinkedHashMap<>();
				tokenResponseParameters.entrySet().stream()
						.filter(e -> !tokenResponseParameterNames.contains(e.getKey()))
						.forEach(e -> additionalParameters.put(e.getKey(), e.getValue()));
				
				System.out.println(additionalParameters);
				return OAuth2AccessTokenResponse.withToken(accessToken)
						.tokenType(OAuth2AccessToken.TokenType.BEARER)
						.expiresIn(expiresIn)
						.scopes(scopes)
						.refreshToken(refreshToken)
						.additionalParameters(additionalParameters)
						.build();
				
			};
		}

		@Override
		public OAuth2AccessTokenResponse getTokenResponse(OAuth2AuthorizationCodeGrantRequest authorizationCodeGrantRequest) {
			System.out.println("-------getTokenResponse------");
			Assert.notNull(authorizationCodeGrantRequest, "authorizationCodeGrantRequest cannot be null");

			RequestEntity<?> request = this.requestEntityConverter.convert(authorizationCodeGrantRequest);

			ResponseEntity<OAuth2AccessTokenResponse> response;
			try {
				System.out.println("-------getTokenResponse  response begin------");
				response = this.restOperations.exchange(request, OAuth2AccessTokenResponse.class);
				System.out.println("-------getTokenResponse  response end------");
			} catch (RestClientException ex) {
				System.out.println("-------getTokenResponse  response error------");
				System.out.println(ex);
				Arrays.stream(ex.getStackTrace()).forEach(each -> System.out.println(each));
				
				OAuth2Error oauth2Error = new OAuth2Error(INVALID_TOKEN_RESPONSE_ERROR_CODE,
						"An error occurred while attempting to retrieve the OAuth 2.0 Access Token Response: " + ex.getMessage(), null);
				throw new OAuth2AuthorizationException(oauth2Error, ex);
			}

			System.out.println("-------getTokenResponse  response body------");
			OAuth2AccessTokenResponse tokenResponse = response.getBody();
			System.out.println(tokenResponse.getAdditionalParameters());
			System.out.println(tokenResponse.getAccessToken().getTokenValue());

			if (CollectionUtils.isEmpty(tokenResponse.getAccessToken().getScopes())) {
				// As per spec, in Section 5.1 Successful Access Token Response
				// https://tools.ietf.org/html/rfc6749#section-5.1
				// If AccessTokenResponse.scope is empty, then default to the scope
				// originally requested by the client in the Token Request
				tokenResponse = OAuth2AccessTokenResponse.withResponse(tokenResponse)
						.scopes(authorizationCodeGrantRequest.getClientRegistration().getScopes())
						.build();
			}

			return tokenResponse;
		}

		/**
		 * Sets the {@link Converter} used for converting the {@link OAuth2AuthorizationCodeGrantRequest}
		 * to a {@link RequestEntity} representation of the OAuth 2.0 Access Token Request.
		 *
		 * @param requestEntityConverter the {@link Converter} used for converting to a {@link RequestEntity} representation of the Access Token Request
		 */
		public void setRequestEntityConverter(Converter<OAuth2AuthorizationCodeGrantRequest, RequestEntity<?>> requestEntityConverter) {
			Assert.notNull(requestEntityConverter, "requestEntityConverter cannot be null");
			this.requestEntityConverter = requestEntityConverter;
		}

		/**
		 * Sets the {@link RestOperations} used when requesting the OAuth 2.0 Access Token Response.
		 *
		 * <p>
		 * <b>NOTE:</b> At a minimum, the supplied {@code restOperations} must be configured with the following:
		 * <ol>
		 *  <li>{@link HttpMessageConverter}'s - {@link FormHttpMessageConverter} and {@link OAuth2AccessTokenResponseHttpMessageConverter}</li>
		 *  <li>{@link ResponseErrorHandler} - {@link OAuth2ErrorResponseErrorHandler}</li>
		 * </ol>
		 *
		 * @param restOperations the {@link RestOperations} used when requesting the Access Token Response
		 */
		public void setRestOperations(RestOperations restOperations) {
			Assert.notNull(restOperations, "restOperations cannot be null");
			this.restOperations = restOperations;
		}
	}
	
	private OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService() {
		System.out.println("----- oauth2UserService -------");
		final DefaultOAuth2UserService defaultUserService = new DefaultOAuth2UserService();
		final QqOauth2UserService qqUserService = new QqOauth2UserService();
		System.out.println("----- oauth2UserService 1111 -------");
		return (userRequest) -> {
			System.out.println("-----     oauth2UserService 1 -------");
			String registrationId = userRequest.getClientRegistration().getRegistrationId();
			System.out.println(registrationId);
			System.out.println("-----     oauth2UserService 2 -------");
			
			OAuth2User oauthUser = null;
			
			System.out.println("-----     oauth2UserService 3 -------");
			OAuth2AccessToken accessToken = userRequest.getAccessToken();
			System.out.println("-----     oauth2UserService 4 -------");
			
			
			
			UserInfo userInfo = null;
			if(registrationId.equalsIgnoreCase(OauthSite.GITHUB.getValue())) {
				System.out.println("---- github");
				oauthUser = defaultUserService.loadUser(userRequest);
				userInfo = githubLoginService.updateUser(accessToken, oauthUser);
				
			}else if(registrationId.equalsIgnoreCase(OauthSite.QQ.getValue())) {
				System.out.println("---- qq");
				oauthUser = qqUserService.loadUser(userRequest);
				userInfo = qqLoginService.updateUser(accessToken, oauthUser);
			}
			System.out.println(oauthUser);
			System.out.println(oauthUser.getName());
			System.out.println(oauthUser.getAttributes());
			System.out.println(accessToken.getTokenValue());
			System.out.println(userRequest.getAdditionalParameters());
			
			if(userInfo != null) {
				// 将第三方用户信息转换为本网站的用户信息
				// 这里主要是存储用户 id 等页面上常用信息
				Map<String, Object> userAttributes = new HashMap<String, Object>();
				userAttributes.put("id", userInfo.getId());
				userAttributes.put("loginName", userInfo.getLoginName());
				userAttributes.put("avatarUrl", userInfo.getAvatarUrl());
				// 经过反复考虑，这里设置 loginName 而不是设置 id
				// 首先将 loginName 加上唯一约束后，使用 byLoginName 和 byId 获取用户信息的效果是一样的
				// 这样在写代码时，不要做各种转换
				return new DefaultOAuth2User(oauthUser.getAuthorities(), userAttributes, "loginName");
			}

			return oauthUser;
		};
	}

}
