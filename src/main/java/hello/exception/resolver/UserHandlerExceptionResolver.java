package hello.exception.resolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import hello.exception.exception.UserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class UserHandlerExceptionResolver implements HandlerExceptionResolver {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {

        try {

            if (ex instanceof UserException) {
                log.info("UserException resolver to 400");
                // accept header 꺼내기
                String acceptHeader = request.getHeader("accept");
                // response 상태코드 변경
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

                // accept header가 json일 때
                if ("application/json".equals(acceptHeader)) {
                    Map<String, Object> errorResult = new HashMap<>();
                    // 어떤 Exception이 터졌는지, 오류 메시지가 뭔지 저장
                    errorResult.put("ex", ex.getClass());
                    errorResult.put("message", ex.getMessage());

                    // json 형태인 errorResult를 string으로 변환
                    String result = objectMapper.writeValueAsString(errorResult);

                    // 이 데이터를 response 바디에 넣어줌 (ModelAndView를 반환해야 돼서 -> json은 바디에 직접 반환)
                    response.setContentType("application/json");
                    response.setCharacterEncoding("utf-8");
                    response.getWriter().write(result);
                    // 빈 ModelAndView 반환 -> 예외는 먹어버리지만 정상 흐름으로 서블릿까지 response를 전달
                    return new ModelAndView();
                } else {
                    // text/html 등 json이 아닐 때
                    return new ModelAndView("error/500");
                }

            }

        } catch (IOException e) {
            log.error("resolver ex", e);
        }

        return null;
    }
}
