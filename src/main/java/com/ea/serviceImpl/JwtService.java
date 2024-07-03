package com.ea.serviceImpl;

import java.security.Key;
import java.util.Base64.Decoder;
import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ea.entity.RefreshToken;
import com.ea.repository.RefreshTokenRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService{
	
	//will pass in run configuration
	@Value("${application.jwt.secret}")
	private String secret;
	
//	String secrett= "FCrM4+we8zrrktOuZ3Hnu7xbIlj/1+mr7ykz6H7jtccp5dDy5q6yjdNGa0F6guE3ttVi+LIhaINUTKaff4BAvg==";
	
	private static final String ROLE ="role";
	
	private final RefreshTokenRepository refreshTokenRepository;

	public JwtService(RefreshTokenRepository refreshTokenRepository) {
		super();
		this.refreshTokenRepository = refreshTokenRepository;
	}

	public String createJwtToken(String username, long expirationDurationInMilis, String userRole)
	{
		
		return Jwts.builder().setClaims(Map.of(ROLE, userRole)).setSubject(username)
				.setIssuedAt(new Date(System.currentTimeMillis()))
		.setExpiration(new Date(System.currentTimeMillis()+expirationDurationInMilis))
		.signWith(getSignKey(), SignatureAlgorithm.HS512).compact();
		//compact make the string
		
	}
	

	
	private Key getSignKey() {

		return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
		
	}
	
	private Claims passJwtToken(String token)
	{
		
		JwtParser jwtParser = Jwts.parserBuilder().setSigningKey(getSignKey()).build();
		 return jwtParser.parseClaimsJws(token).getBody();
		 //getBody will extract the claims object and return it
		
	}

	public String extractUsername(String token)
	{
			return passJwtToken(token).getSubject();
	}
	
	public Date extractIssuedAt(String token)
	{
		return passJwtToken(token).getIssuedAt();
	}
	
	public Date extractExpirationDate(String token)
	{
		return passJwtToken(token).getExpiration();
	}
	
	public String extractUserRole(String token)
	{
		return passJwtToken(token).get(ROLE, String.class);
	}
	
	public boolean isTokenValid(String token) {
		try {
			RefreshToken refreshToken = refreshTokenRepository.findByToken(token);
			return passJwtToken(token).getExpiration()
					.after(new Date()) && refreshToken != null && !refreshToken.isBlocked();
		} catch (Exception e) {
			return false;
		}
		
	}
}
