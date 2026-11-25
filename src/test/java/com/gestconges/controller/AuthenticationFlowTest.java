package com.gestconges.controller;

import com.gestconges.filter.AuthFilter;
import com.gestconges.model.Role;
import com.gestconges.service.AuthService;
import com.gestconges.util.PasswordUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationFlowTest {

    @Test
    void authServiceRejectsMissingCredentials() {
        AuthService authService = new AuthService();

        assertNull(authService.authenticate(null, "password"));
        assertNull(authService.authenticate("user@example.test", " "));
    }

    @Test
    void passwordHashCanBeVerified() {
        String hash = PasswordUtil.hashPassword("CorrectPassword123");

        assertNotEquals("CorrectPassword123", hash);
        assertTrue(PasswordUtil.verifyPassword("CorrectPassword123", hash));
        assertFalse(PasswordUtil.verifyPassword("WrongPassword", hash));
    }

    @Test
    void loginServletReturnsErrorForBlankCredentials() throws Exception {
        TestSession session = new TestSession();
        Fixture fixture = new Fixture("/login", "POST", session, Map.of(
                "email", "",
                "password", ""
        ));
        LoginServlet loginServlet = new LoginServlet();
        loginServlet.init();

        loginServlet.doPost(fixture.request, fixture.response);

        assertEquals("Email ou mot de passe incorrect.", fixture.errorAttribute);
        assertEquals("/WEB-INF/views/auth/login.jsp", fixture.forwardedTo);
        assertNull(fixture.redirect);
    }

    @Test
    void filterAllowsPublicRoutesWithoutSession() throws Exception {
        Fixture fixture = new Fixture("/login", "GET", null, Map.of());

        new AuthFilter().doFilter(
                fixture.request,
                fixture.response,
                fixture.chain
        );

        assertTrue(fixture.chainCalled);
        assertNull(fixture.redirect);
    }

    @Test
    void filterKeepsLogoutPublicSoUnauthenticatedUsersCanReachIt()
            throws Exception {
        Fixture fixture = new Fixture("/logout", "POST", null, Map.of());

        new AuthFilter().doFilter(
                fixture.request,
                fixture.response,
                fixture.chain
        );

        assertTrue(fixture.chainCalled);
        assertNull(fixture.redirect);
    }

    @Test
    void filterRedirectsUnauthenticatedRequestsToLogin() throws Exception {
        Fixture fixture = new Fixture("/dashboard/employe", "GET", null, Map.of());

        new AuthFilter().doFilter(
                fixture.request,
                fixture.response,
                fixture.chain
        );

        assertEquals("/gestconges/login", fixture.redirect);
        assertFalse(fixture.chainCalled);
    }

    @Test
    void filterAllowsAuthenticatedRequestForMatchingRole() throws Exception {
        TestSession session = new TestSession(
                Map.of("employeId", 42L, "employeRole", Role.EMPLOYE.name())
        );
        Fixture fixture = new Fixture(
                "/dashboard/employe",
                "GET",
                session,
                Map.of()
        );

        new AuthFilter().doFilter(
                fixture.request,
                fixture.response,
                fixture.chain
        );

        assertTrue(fixture.chainCalled);
        assertNull(fixture.errorStatus);
    }

    @Test
    void filterRejectsDashboardForDifferentRole() throws Exception {
        TestSession session = new TestSession(
                Map.of("employeId", 42L, "employeRole", Role.EMPLOYE.name())
        );
        Fixture fixture = new Fixture(
                "/dashboard/admin",
                "GET",
                session,
                Map.of()
        );

        new AuthFilter().doFilter(
                fixture.request,
                fixture.response,
                fixture.chain
        );

        assertEquals(HttpServletResponse.SC_FORBIDDEN, fixture.errorStatus);
        assertFalse(fixture.chainCalled);
    }

    @Test
    void filterRejectsEmployeeManagementForNonAdmin() throws Exception {
        TestSession session = new TestSession(
                Map.of("employeId", 42L, "employeRole", Role.EMPLOYE.name())
        );
        Fixture fixture = new Fixture(
                "/admin/employees",
                "GET",
                session,
                Map.of()
        );

        new AuthFilter().doFilter(
                fixture.request,
                fixture.response,
                fixture.chain
        );

        assertEquals(HttpServletResponse.SC_FORBIDDEN, fixture.errorStatus);
        assertFalse(fixture.chainCalled);
    }

    @Test
    void filterAllowsEmployeeManagementForAdmin() throws Exception {
        TestSession session = new TestSession(
                Map.of("employeId", 42L, "employeRole", Role.ADMIN.name())
        );
        Fixture fixture = new Fixture(
                "/admin/employees",
                "GET",
                session,
                Map.of()
        );

        new AuthFilter().doFilter(
                fixture.request,
                fixture.response,
                fixture.chain
        );

        assertTrue(fixture.chainCalled);
        assertNull(fixture.errorStatus);
    }

    @Test
    void filterRejectsDepartmentManagementForNonAdmin() throws Exception {
        TestSession session = new TestSession(
                Map.of("employeId", 42L, "employeRole", Role.RESPONSABLE.name())
        );
        Fixture fixture = new Fixture(
                "/admin/departments",
                "GET",
                session,
                Map.of()
        );

        new AuthFilter().doFilter(
                fixture.request,
                fixture.response,
                fixture.chain
        );

        assertEquals(HttpServletResponse.SC_FORBIDDEN, fixture.errorStatus);
        assertFalse(fixture.chainCalled);
    }

    @Test
    void filterAllowsDepartmentManagementForAdmin() throws Exception {
        TestSession session = new TestSession(
                Map.of("employeId", 42L, "employeRole", Role.ADMIN.name())
        );
        Fixture fixture = new Fixture(
                "/admin/departments/team",
                "GET",
                session,
                Map.of()
        );

        new AuthFilter().doFilter(
                fixture.request,
                fixture.response,
                fixture.chain
        );

        assertTrue(fixture.chainCalled);
        assertNull(fixture.errorStatus);
    }

    @Test
    void filterRejectsLeaveTypeManagementForNonAdmin() throws Exception {
        TestSession session = new TestSession(
                Map.of("employeId", 42L, "employeRole", Role.RESPONSABLE.name())
        );
        Fixture fixture = new Fixture(
                "/admin/leave-types",
                "GET",
                session,
                Map.of()
        );

        new AuthFilter().doFilter(
                fixture.request,
                fixture.response,
                fixture.chain
        );

        assertEquals(HttpServletResponse.SC_FORBIDDEN, fixture.errorStatus);
        assertFalse(fixture.chainCalled);
    }

    @Test
    void filterAllowsLeaveTypeManagementForAdmin() throws Exception {
        TestSession session = new TestSession(
                Map.of("employeId", 42L, "employeRole", Role.ADMIN.name())
        );
        Fixture fixture = new Fixture(
                "/admin/leave-types/status",
                "POST",
                session,
                Map.of()
        );

        new AuthFilter().doFilter(
                fixture.request,
                fixture.response,
                fixture.chain
        );

        assertTrue(fixture.chainCalled);
        assertNull(fixture.errorStatus);
    }

    @Test
    void filterRejectsBalanceManagementForNonAdmin() throws Exception {
        TestSession session = new TestSession(
                Map.of("employeId", 42L, "employeRole", Role.EMPLOYE.name())
        );
        Fixture fixture = new Fixture(
                "/admin/balances",
                "GET",
                session,
                Map.of()
        );

        new AuthFilter().doFilter(
                fixture.request,
                fixture.response,
                fixture.chain
        );

        assertEquals(HttpServletResponse.SC_FORBIDDEN, fixture.errorStatus);
        assertFalse(fixture.chainCalled);
    }

    @Test
    void filterAllowsBalanceManagementForAdmin() throws Exception {
        TestSession session = new TestSession(
                Map.of("employeId", 42L, "employeRole", Role.ADMIN.name())
        );
        Fixture fixture = new Fixture(
                "/admin/balances/adjust",
                "POST",
                session,
                Map.of()
        );

        new AuthFilter().doFilter(
                fixture.request,
                fixture.response,
                fixture.chain
        );

        assertTrue(fixture.chainCalled);
        assertNull(fixture.errorStatus);
    }

    @Test
    void filterAllowsEmployeeLeaveRequestsForAuthenticatedEmployee()
            throws Exception {
        TestSession session = new TestSession(
                Map.of("employeId", 42L, "employeRole", Role.EMPLOYE.name())
        );
        Fixture fixture = new Fixture(
                "/employee/leave-requests",
                "GET",
                session,
                Map.of()
        );

        new AuthFilter().doFilter(
                fixture.request,
                fixture.response,
                fixture.chain
        );

        assertTrue(fixture.chainCalled);
        assertNull(fixture.errorStatus);
    }

    @Test
    void filterRedirectsUnauthenticatedEmployeeLeaveRequestsToLogin()
            throws Exception {
        Fixture fixture = new Fixture(
                "/employee/leave-requests",
                "GET",
                null,
                Map.of()
        );

        new AuthFilter().doFilter(
                fixture.request,
                fixture.response,
                fixture.chain
        );

        assertEquals("/gestconges/login", fixture.redirect);
        assertFalse(fixture.chainCalled);
    }

    @Test
    void dashboardEntryRedirectsToRoleSpecificDestination() throws Exception {
        TestSession session = new TestSession(
                Map.of("employeId", 42L, "employeRole", Role.RESPONSABLE.name())
        );
        Fixture fixture = new Fixture("/dashboard", "GET", session, Map.of());

        new DashboardServlet().doGet(fixture.request, fixture.response);

        assertEquals("/gestconges/dashboard/responsable", fixture.redirect);
        assertNull(fixture.forwardedTo);
    }

    @Test
    void dashboardForwardsToRoleSpecificJsp() throws Exception {
        TestSession session = new TestSession(
                Map.of("employeId", 42L, "employeRole", Role.ADMIN.name())
        );
        Fixture fixture = new Fixture(
                "/dashboard/admin",
                "GET",
                session,
                Map.of()
        );

        new DashboardServlet().doGet(fixture.request, fixture.response);

        assertEquals("/WEB-INF/views/dashboard/admin.jsp", fixture.forwardedTo);
        assertNull(fixture.redirect);
    }

    @Test
    void dashboardServletRejectsRouteForDifferentRole() throws Exception {
        TestSession session = new TestSession(
                Map.of("employeId", 42L, "employeRole", Role.EMPLOYE.name())
        );
        Fixture fixture = new Fixture(
                "/dashboard/responsable",
                "GET",
                session,
                Map.of()
        );

        new DashboardServlet().doGet(fixture.request, fixture.response);

        assertEquals(HttpServletResponse.SC_FORBIDDEN, fixture.errorStatus);
        assertNull(fixture.forwardedTo);
    }

    @Test
    void logoutInvalidatesSessionAndSetsNoCacheHeaders() throws Exception {
        TestSession session = new TestSession(
                Map.of("employeId", 42L, "employeRole", Role.EMPLOYE.name())
        );
        Fixture fixture = new Fixture("/logout", "POST", session, Map.of());

        new LogoutServlet().doPost(fixture.request, fixture.response);

        assertTrue(session.invalidated);
        assertEquals("/gestconges/login?logout=success", fixture.redirect);
        assertEquals(
                "no-cache, no-store, must-revalidate",
                fixture.headers.get("Cache-Control")
        );
        assertEquals("no-cache", fixture.headers.get("Pragma"));
        assertEquals(0L, fixture.dateHeaders.get("Expires"));

        Fixture afterLogout = new Fixture(
                "/dashboard/employe",
                "GET",
                null,
                Map.of()
        );
        new AuthFilter().doFilter(
                afterLogout.request,
                afterLogout.response,
                afterLogout.chain
        );
        assertEquals("/gestconges/login", afterLogout.redirect);
    }

    private static <T> T proxy(Class<T> type, Invocation invocation) {
        return type.cast(Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[]{type},
                (proxy, method, arguments) -> invocation.invoke(
                        method.getName(),
                        arguments == null ? new Object[0] : arguments
                )
        ));
    }

    @FunctionalInterface
    private interface Invocation {
        Object invoke(String methodName, Object[] arguments) throws Throwable;
    }

    private static final class TestSession {
        private final Map<String, Object> attributes;
        private boolean invalidated;
        private final HttpSession session;

        private TestSession() {
            this(Map.of());
        }

        private TestSession(Map<String, Object> initialAttributes) {
            attributes = new HashMap<>(initialAttributes);
            session = proxy(HttpSession.class, (methodName, arguments) -> {
                if ("getAttribute".equals(methodName)) {
                    return attributes.get(arguments[0]);
                }
                if ("setAttribute".equals(methodName)) {
                    attributes.put((String) arguments[0], arguments[1]);
                    return null;
                }
                if ("invalidate".equals(methodName)) {
                    invalidated = true;
                    attributes.clear();
                    return null;
                }
                return defaultValue(methodName);
            });
        }
    }

    private static final class Fixture {
        private final Map<String, String> headers = new HashMap<>();
        private final Map<String, Long> dateHeaders = new HashMap<>();
        private final HttpServletRequest request;
        private final HttpServletResponse response;
        private final FilterChain chain;
        private String redirect;
        private String forwardedTo;
        private String errorAttribute;
        private Integer errorStatus;
        private boolean chainCalled;

        private Fixture(
                String path,
                String method,
                TestSession session,
                Map<String, String> parameters
        ) {
            request = proxy(HttpServletRequest.class, (methodName, arguments) -> {
                switch (methodName) {
                    case "getContextPath":
                        return "/gestconges";
                    case "getRequestURI":
                        return "/gestconges" + path;
                    case "getMethod":
                        return method;
                    case "getSession":
                        return session == null ? null : session.session;
                    case "getParameter":
                        return parameters.get(arguments[0]);
                    case "setAttribute":
                        if ("error".equals(arguments[0])) {
                            errorAttribute = (String) arguments[1];
                        }
                        return null;
                    case "getRequestDispatcher":
                        String dispatchPath = (String) arguments[0];
                        RequestDispatcher dispatcher = proxy(
                                RequestDispatcher.class,
                                (dispatcherMethod, dispatcherArguments) -> {
                                    if ("forward".equals(dispatcherMethod)) {
                                        forwardedTo = dispatchPath;
                                    }
                                    return null;
                                }
                        );
                        return dispatcher;
                    default:
                        return defaultValue(methodName);
                }
            });
            response = proxy(HttpServletResponse.class, (methodName, arguments) -> {
                switch (methodName) {
                    case "sendRedirect":
                        redirect = (String) arguments[0];
                        return null;
                    case "sendError":
                        errorStatus = (Integer) arguments[0];
                        return null;
                    case "setHeader":
                        headers.put((String) arguments[0], (String) arguments[1]);
                        return null;
                    case "setDateHeader":
                        dateHeaders.put(
                                (String) arguments[0],
                                (Long) arguments[1]
                        );
                        return null;
                    default:
                        return defaultValue(methodName);
                }
            });
            chain = proxy(FilterChain.class, (methodName, arguments) -> {
                if ("doFilter".equals(methodName)) {
                    chainCalled = true;
                }
                return null;
            });
        }
    }

    private static Object defaultValue(String methodName) {
        return switch (methodName) {
            case "isCommitted", "isNew", "isRequestedSessionIdValid" -> false;
            case "getStatus", "getMaxInactiveInterval", "getContentLength" -> 0;
            default -> null;
        };
    }
}
