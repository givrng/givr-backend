package com.backend.givr.shared.jwt;

import com.backend.givr.shared.interfaces.SecurityDetails;
import com.backend.givr.shared.service.TokenIdService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.Cookie;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;


@Service
public class GivrCookie {
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private TokenIdService tokenService;

    /**
     * Creates cookies and sets the HttpServletResponse headers with the cookie value.
     * Set status code and flush response to the client
     * @param details Security details of authenticated user
     * @param response HttpServletResponse object that will contain authorization cookies*/
    public void addCookieToResponse(SecurityDetails details, HttpServletResponse response){

        String accessId = JwtUtil.generateJti();
        String accessToken = jwtUtil.generateToken(details, accessId, JwtUtil.ACCESSEXPIRATION.toMillis());
        String refreshId = JwtUtil.generateJti();
        String refreshToken= jwtUtil.generateToken(details, refreshId, JwtUtil.REFRESHEXPIRATION.toMillis());

        String email = (details.getUsername());

        tokenService.createToken(refreshId, email, JwtUtil.REFRESHEXPIRATION.toMillis());

        ResponseCookie accessCookie = ResponseCookie.from("AccessToken").value(accessToken)
                .path("/")
                .maxAge(JwtUtil.ACCESSEXPIRATION)
                .sameSite(Cookie.SameSite.LAX.attributeValue())
                .httpOnly(true)
                .secure(true)
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("RefreshToken").value(refreshToken)
                .path("/")
                .maxAge(JwtUtil.REFRESHEXPIRATION)
                .sameSite(Cookie.SameSite.LAX.attributeValue())
                .httpOnly(true)
                .secure(true)
                .build();

        response.addHeader("Set-Cookie", accessCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());
    }
}
