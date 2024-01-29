package webserver.http;

import db.H2Database;
import db.SessionManager;
import db.UserRepository;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.LoginChecker;
import utils.UserFormDataParser;
import webserver.http.constants.HttpMethod;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class RequestHandler {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);
    private final Map<Route, Consumer<Request>> routeHandlers= new HashMap<>();
    private static class Route{
        private final HttpMethod httpMethod;
        private final String routeName;

        private Route(HttpMethod httpMethod, String routeName){
            this.httpMethod = httpMethod;
            this.routeName = routeName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Route route = (Route) o;
            return httpMethod == route.httpMethod &&
                    Objects.equals(routeName, route.routeName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(httpMethod, routeName);
        }
    }

    public RequestHandler() {
        initializeRoutes();
    }

    private void initializeRoutes() {
        routeHandlers.put(new Route(HttpMethod.GET,"/user/create"), (Request r)-> getUserCreate(r));
        routeHandlers.put(new Route(HttpMethod.POST,"/user/create"), (Request r)-> postUserCreate(r));
        routeHandlers.put(new Route(HttpMethod.POST,"/user/login"), (Request r)-> postUserLogin(r));
        routeHandlers.put(new Route(HttpMethod.GET,"/user/list"), (Request r)-> getUserList(r));
        routeHandlers.put(new Route(HttpMethod.GET,"/user/list.html"), (Request r)-> getUserList(r));
        routeHandlers.put(new Route(HttpMethod.GET,"/board/write.html"), (Request r)-> getBoardWrite(r));
    }

    public void handleRequest(Request request) {
        if (request.getHttpMethod() == HttpMethod.NULL)
            throw new IllegalArgumentException("Method NULL");
        String requestTarget = request.getRequestTarget().split("\\?")[0];
        Route inputRoute = new Route(request.getHttpMethod() ,requestTarget);
        if (routeHandlers.containsKey(inputRoute)) {
            routeHandlers.get(inputRoute).accept(request);
        } else {
            handleNotFound();
        }
    }

    private void getUserCreate(Request request) {
        String data = request.getRequestTarget().split("\\?")[1];
        UserFormDataParser userFormDataParser = new UserFormDataParser(data);
        HashMap<String,String> formData = new HashMap<>(userFormDataParser.parseData());
        User user = new User(formData.get("userId"), formData.get("password"), formData.get("name"), formData.get("email") );
        UserRepository.adduser(user);
        request.addRequestHeader("Location","/user/form.html");
    }

    private void postUserCreate(Request request) {
        HashMap<String,String> formData = (HashMap<String, String>) request.getRequestBody();
        User user = new User(formData.get("userId"), formData.get("password"), formData.get("name"), formData.get("email") );

        //같은 아이디 회원 가입 방지
        if(UserRepository.findUserById(user.getUserId()) != null)
            return;

        UserRepository.adduser(user);
        request.addRequestHeader("Location","/index.html");
    }

    private void postUserLogin(Request request) {
        HashMap<String,String> formData = (HashMap<String, String>) request.getRequestBody();
        String id = formData.get("userId");
        String pw = formData.get("password");
        if(UserRepository.isValidLogin(id, pw)){
            request.addRequestHeader("Location","/index.html");
            String session = SessionManager.addSession(UserRepository.findUserById(id));
            request.addRequestHeader("Set-Cookie", "sid=" + session + "; Path=/");
        }else{
            request.addRequestHeader("Location","/user/login_failed.html");
        }
    }

    private void getUserList(Request request) {
        if(!LoginChecker.loginCheck(request)){
            request.addRequestHeader("Location","/user/login.html");
        }
    }

    private void getBoardWrite(Request request) {
        if(!LoginChecker.loginCheck(request)){
            request.addRequestHeader("Location","/user/login.html");
        }
    }

    private void handleNotFound() {
        logger.error("request : NOT FOUND");
    }
}