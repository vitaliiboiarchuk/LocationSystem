<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>
<c:if test="${welcomePage}">
    <h1>Welcome</h1>
    <a href="/registration">Create an account</a></p>
    <a href="/login">Login</a></p>
</c:if>
<c:if test="${myProfile}">
    <h1>My profile</h1>
    <a href="/location">My locations</a></p>
    <a href="/location/add">Add location</a></p>
    <a href="/location/share">Share</a></p>
    <a href="/logout">Logout</a></p>
</c:if>
</body>
</html>