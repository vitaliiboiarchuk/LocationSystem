<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
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
    <a href="/">My location</a></p>
    <a href="/">Add location</a></p>
</sec:authorize>
</body>
</html>
