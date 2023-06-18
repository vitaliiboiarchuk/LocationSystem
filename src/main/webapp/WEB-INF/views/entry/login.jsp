<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>

<form:form method="post" modelAttribute="user">

    Email:
    <form:input path="username" type="email"/>

    Password:
    <form:input path="password" type="password"/>

    <button class="btn btn-primary w-100" type="submit">Login</button>

    <c:if test="${param.error != null}">
        <div id="error" class="alert alert-danger">
            <spring:message code="message.badCredentials">
            </spring:message>
        </div>
    </c:if>

    <div class="col-12">
        <p class="small mb-0">Don't have an account? <a href="/registration">Create an account</a></p>
    </div>
</form:form>

</body>
</html>
