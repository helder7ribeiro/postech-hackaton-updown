package org.fiap.updown.infrastructure.config;

import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.List;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.validation.enabled:true}")
    private boolean jwtValidationEnabled;

    @Value("${app.cognito.jwkUrl}")
    private String jwkUrl;

    @Value("${app.cognito.clientId}")
    private String expectedClientId;

    @Value("${app.cognito.issuer}")
    private String expectedIssuer;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        
        // Permitir acesso a endpoints públicos
        if (isPublicEndpoint(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Verificar se é um endpoint protegido
        if (isProtectedEndpoint(path)) {
            String authHeader = request.getHeader("Authorization");
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Token de autorização não encontrado para endpoint protegido: {}", path);
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.getWriter().write("{\"error\":\"Token de autorização necessário\"}");
                return;
            }

            // Se a validação JWT estiver desabilitada (para testes), apenas verificar se o token existe
            if (!jwtValidationEnabled) {
                log.debug("Validação JWT desabilitada para testes");
                filterChain.doFilter(request, response);
                return;
            }

            String token = authHeader.substring(7);
            
            if (!validateToken(token)) {
                log.warn("Token inválido para endpoint protegido: {}", path);
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.getWriter().write("{\"error\":\"Token inválido\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/actuator/health") ||
               path.startsWith("/api-docs") ||
               path.startsWith("/swagger") ||
               path.startsWith("/api/v1/app-users");
    }

    private boolean isProtectedEndpoint(String path) {
        return path.startsWith("/api/v1/jobs");
    }

    private boolean validateToken(String token) {
        try {
            // Parse do token
            SignedJWT signedJWT = SignedJWT.parse(token);
            
            // Verificar se não expirou
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            if (claims.getExpirationTime() != null && claims.getExpirationTime().getTime() < System.currentTimeMillis()) {
                log.warn("Token expirado");
                return false;
            }

            // Verificar issuer
            String issuer = claims.getIssuer();
            if (!expectedIssuer.equals(issuer)) {
                log.warn("Issuer inválido. Esperado: {}, Recebido: {}", expectedIssuer, issuer);
                return false;
            }

            // Verificar client_id
            String clientId = claims.getStringClaim("client_id");
            if (!expectedClientId.equals(clientId)) {
                log.warn("Client ID inválido. Esperado: {}, Recebido: {}", expectedClientId, clientId);
                return false;
            }

            // Verificar token_use
            String tokenUse = claims.getStringClaim("token_use");
            if (!"access".equals(tokenUse)) {
                log.warn("Token deve ser do tipo 'access', mas é: {}", tokenUse);
                return false;
            }

            // Verificar assinatura
            JWKSet jwkSet = JWKSet.load(new URL(jwkUrl));
            
            // Obter a chave pelo kid do header
            String kid = signedJWT.getHeader().getKeyID();
            if (kid != null) {
                RSAKey rsaKey = jwkSet.getKeyByKeyId(kid).toRSAKey();
                if (rsaKey != null) {
                    JWSVerifier verifier = new RSASSAVerifier(rsaKey);
                    if (signedJWT.verify(verifier)) {
                        log.debug("Token JWT válido para usuário: {}", claims.getStringClaim("cognito:username"));
                        return true;
                    }
                }
            }

            log.warn("Assinatura do token inválida");
            return false;

        } catch (Exception e) {
            log.error("Erro ao validar token JWT", e);
            return false;
        }
    }
}
