<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>

<form:form method="post" modelAttribute="location">

    <form:input path="user" type="hidden" value="${user.id.toString()}"/>

    Name:
    <form:input path="name" type="text"/>

    Address:
    <form:input path="address" type="text"/>

    <button class="btn btn-primary w-100" type="submit">Add Location</button>

    <c:if test="${param.error != null}">
        <div id="error" class="alert alert-danger">
            <spring:message code="message.alreadyExists">
            </spring:message>
        </div>
    </c:if>

</form:form>

</body>
</html>
