<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>
<sec:authorize access="isAnonymous()">
    <h1>Welcome</h1>
    <a href="/registration">Create an account</a></p>
    <a href="/login">Login</a></p>
</sec:authorize>
<sec:authorize access="isAuthenticated()">
    <h1>My profile</h1>
    <a href="/location">My locations</a></p>
    <a href="/location/add">Add location</a></p>
    <a href="/location/share">Share</a></p>
    <form action="<c:url value="/logout"/>" method="post">
        <input type="submit" value="Log Out">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
    </form>
</sec:authorize>
</body>
</html>
