package com.educonnect.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    // Matbaayı (JwtUtil) ve Veritabanı Telsizini (UserDetailsService) memura zimmetliyoruz
    public JwtFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Gelen isteğin başlığına (Header) bak, "Authorization" adında bir bilet kutusu var mı?
        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        // 2. Bilet kutusu var mı ve bilet "Bearer " (Taşıyıcı) kelimesiyle mi başlıyor?
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7); // "Bearer " yazısını (7 karakter) at, asıl bileti al
            username = jwtUtil.extractUsername(jwt); // Biletin içinden kullanıcının adını oku
        }

        // 3. Kullanıcı adı var ama sistemde henüz "Giriş Yapmış" görünmüyorsa
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Veritabanından bu kişinin sicil kaydını (Rolü, şifresi vb.) getir
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // Matbaaya sor: "Bu bilet sahte mi? Süresi geçmiş mi? Bu adama mı ait?"
            if (jwtUtil.validateToken(jwt, userDetails)) {

                // Bilet tertemiz! Memur, kişiye "Gemiye Binebilir" (Authenticated) damgasını vuruyor
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Kişiyi geminin güvenli alanına (SecurityContext) yerleştir
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Memur işini bitirdi. İsteği bir sonraki aşamaya yolla (Bileti olmayanlar zaten kapıda kalacak)
        filterChain.doFilter(request, response);
    }
}
