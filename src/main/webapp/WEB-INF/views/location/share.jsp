<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>

<c:if test="${available}">
    <form:form method="post" modelAttribute="userAccess" action="/location/share">

        <h3>Choose access</h3>
        <form:select path="title" items="${accessTitles}"/>

        <h3>Choose location</h3>
        <form:select path="location" items="${locations}" itemValue="id" itemLabel="name"/>
        <form:input path="user" type="hidden" value="${user.id.toString()}"/>

        <input type="submit" value="Submit"></input>
    </form:form>
</c:if>

<c:if test="${notAvailable}">
    <h3>User already has access to all of your locations, or you have no location to share.</h3>
</c:if>

</body>
</html>
