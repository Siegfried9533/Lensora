package com.camerashop.config;

import com.camerashop.repository.UserRepository;
import com.camerashop.service.AuthService;
import com.camerashop.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * OAuth2 authentication success handler.
 * Uses ApplicationContext to avoid circular dependency.
 */
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private ApplicationContext applicationContext;

    public OAuth2SuccessHandler() {
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String provider = oauthToken.getAuthorizedClientRegistrationId();

        // Get beans from application context
        AuthService authService = applicationContext.getBean(AuthService.class);

        // Extract user info from OAuth2 provider
        Map<String, Object> attributes = oauthToken.getPrincipal().getAttributes();

        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String providerId = (String) attributes.get("sub");

        if (email == null) {
            // Try alternative attribute names for different providers
            email = (String) attributes.get("email_address");
        }

        if (name == null) {
            // Combine first and last name if available
            String firstName = (String) attributes.get("first_name");
            String lastName = (String) attributes.get("last_name");
            if (firstName != null && lastName != null) {
                name = firstName + " " + lastName;
            } else if (firstName != null) {
                name = firstName;
            }
        }

        // Register or login the user
        var authResponse = authService.registerOAuthUser(email, name, provider, providerId);

        // Redirect to frontend with token using deep link scheme
        // For Expo Go: mobile://oauth-success?token=...
        String frontendUrl = "mobile://oauth-success?token=" + authResponse.getToken();
        response.sendRedirect(frontendUrl);
    }
}
