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

    Name:

    <form:input path="name" type="text"/>

    Password:

    <form:input path="password" type="password"/>

    Email:

    <form:input path="username" type="email"/>

    <input type="submit" value="Create Account"></input>

    <c:if test="${param.error != null}">
        <div id="error" class="alert alert-danger">
            <spring:message code="message.alreadyExists">
            </spring:message>
        </div>
    </c:if>

    <div class="col-12">
        <p class="small mb-0">Already have an account? <a href="/login">Login</a></p>
    </div>
</form:form>

</body>
</html>
