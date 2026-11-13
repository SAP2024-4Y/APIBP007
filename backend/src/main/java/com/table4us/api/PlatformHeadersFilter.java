package com.table4us.api;
import jakarta.servlet.*; import jakarta.servlet.http.*; import org.springframework.stereotype.Component; import java.io.IOException; import java.util.concurrent.*; import java.util.concurrent.atomic.AtomicInteger;
@Component public class PlatformHeadersFilter implements Filter {
 private final ConcurrentHashMap<String,Window> windows=new ConcurrentHashMap<>();
 public void doFilter(ServletRequest req,ServletResponse res,FilterChain chain)throws IOException,ServletException{
  var request=(HttpServletRequest)req;var response=(HttpServletResponse)res;String ip=request.getRemoteAddr();long now=System.currentTimeMillis();Window w=windows.compute(ip,(k,v)->v==null||now-v.start>=60000?new Window(now):v);
  int used=w.count.incrementAndGet();response.setHeader("X-RateLimit-Limit","120");response.setHeader("X-RateLimit-Remaining",Integer.toString(Math.max(0,120-used)));response.setHeader("X-RateLimit-Reset",Long.toString((w.start+60000)/1000));
  if(used>120){response.setStatus(429);response.setContentType("application/json");response.getWriter().write("{\"code\":\"RATE_LIMITED\",\"message\":\"Please try again shortly\"}");return;}chain.doFilter(req,res);
 }
 private static class Window{final long start;final AtomicInteger count=new AtomicInteger();Window(long start){this.start=start;}}
}
