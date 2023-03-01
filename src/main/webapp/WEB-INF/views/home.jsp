<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  Created by IntelliJ IDEA.
  User: boyar
  Date: 27/02/2023
  Time: 21:15
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>
<sec:authorize access="isAnonymous()">
<h1>Welcome!</h1>
<a href="/registration">Create an account</a></p>
<a href="/login">Login</a></p>
</sec:authorize>
<sec:authorize access="isAuthenticated()">
    <h1>My profile</h1>
    <a href="/myLocations">My locations</a></p>
    <a href="/addLocation">Add location</a></p>
    <form action="<c:url value="/logout"/>" method="post">
        <input type="submit" value="Log Out">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
    </form>
</sec:authorize>
</body>
</html>
