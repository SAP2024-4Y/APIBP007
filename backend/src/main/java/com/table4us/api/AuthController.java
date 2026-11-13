package com.table4us.api;
import com.table4us.service.JwtService; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @RequestMapping("/api/v1/auth")
public class AuthController {
 private final JwtService jwt; public AuthController(JwtService jwt){this.jwt=jwt;}
 @PostMapping("/demo") public Map<String,Object> demo(@RequestBody(required=false) Map<String,String> body){
  String email=body==null?"demo@table4us.local":body.getOrDefault("email","demo@table4us.local");
  String token=jwt.issue(email);
  return Map.of("accessToken",token,"tokenType","Bearer","expiresIn",3600,"demoOnly",true);
 }
}
