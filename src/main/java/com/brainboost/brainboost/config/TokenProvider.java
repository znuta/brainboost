package com.brainboost.brainboost.config;


import com.brainboost.brainboost.auth.entity.AppUser;
import com.brainboost.brainboost.auth.entity.Permission;
import com.brainboost.brainboost.auth.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.brainboost.brainboost.auth.dto.input.Constants.AUTHORITIES_KEY;
import static com.brainboost.brainboost.auth.dto.input.Constants.SIGNING_KEY;
import static org.springframework.security.config.Elements.JWT;

@Component
@Slf4j
@Data
@RequiredArgsConstructor
public class TokenProvider {
    private final UserRepository userRepository;

    @Value("jjwt.secret.key")
    private String secret;

    private Key key;

    private Long id;

    private String email;

    private String username;

    private String firstname;

    private String lastname;

    private List<String> permissions;

    private String roles;

    private String token;

    private Key secretKey = Keys.hmacShaKeyFor("your-256-bit-secret".getBytes());

    public void setDetails(String token) {
        Jws<Claims> claimsJws = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token);

        Claims claims = claimsJws.getBody();
        this.id = claims.get("userId", Long.class);
        this.email = claims.get("email", String.class);
        this.firstname = claims.get("firstname", String.class);
        this.username = claims.get("username", String.class);
        this.lastname = claims.get("lastname", String.class);
        this.roles = claims.get("role", String.class);
        this.permissions = claims.get("permissions", List.class);
        this.token = token;
    }


    public String getUsernameFromJWTToken(String token) {
        return getClaimFromJWTToken(token, Claims::getSubject);
    }

    public Date getExpirationDateFromJWTToken(String token) {
        return getClaimFromJWTToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromJWTToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromJWTToken(token);
        return claimsResolver.apply(claims);
    }

    public Header<?> getHeaderFromJWTToken(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getHeader();
    }


    public Claims getAllClaimsFromJWTToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Boolean isJWTTokenExpired(String token) {
        Date expirationDate = getExpirationDateFromJWTToken(token);
        return expirationDate.before(new Date());
    }

    public String generateJWTToken(Authentication authentication) {
        AppUser user = userRepository.findByUserName(authentication.getName()).get();

        String authorities = user.getRole().getPermissions().stream()
                .map(Permission::getCode)
                .collect(Collectors.joining(","));

        // Decode the base64-encoded secret key to a byte array
        byte[] keyBytes = Decoders.BASE64.decode(SIGNING_KEY);
        // Create the key from the byte array
        Key key = Keys.hmacShaKeyFor(keyBytes);

        String jwts=  Jwts.builder()
                .setSubject(authentication.getName())
                .setExpiration(new Date())
                .claim("userId",user.getId())
                .claim("firstname", user.getFirstName())
                .claim("lastname", user.getLastName())
                .claim("role", user.getRole().getName())
                .claim(AUTHORITIES_KEY, authorities)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return jwts;
    }

    public String generateTokenForVerification (String id){
        return Jwts.builder()
                .setSubject(id)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + (long) (25200)))
                .signWith(SignatureAlgorithm.HS256, SIGNING_KEY)
                .compact();
    }


    public Boolean validateJWTToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromJWTToken(token);
        return (username.equals(userDetails.getUsername()) && !isJWTTokenExpired(token));
    }

    public UsernamePasswordAuthenticationToken getAuthenticationToken(String token, Authentication existingAuth, UserDetails userDetails) {
        final JwtParser jwtParser = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build();
        final Jws<Claims> claimsJws = jwtParser.parseClaimsJws(token);
        final Claims claims = claimsJws.getBody();

        final Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toSet());
        return new UsernamePasswordAuthenticationToken(userDetails, "", authorities);
    }

}
