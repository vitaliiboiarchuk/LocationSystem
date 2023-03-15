<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>

<form:form method="post" modelAttribute="user">

    Name:

    <form:input path="name" type="text"/>
    <form:errors path="name" class="alert alert-danger"/>

    Password:

    <form:input path="password" type="password"/>
    <form:errors path="password" class="alert alert-danger"/>

    Email:

    <form:input path="username" type="email"/>
    <form:errors path="username" class="alert alert-danger"/>

    <input type="submit" value="Create Account"></input>

    <div class="col-12">
        <p class="small mb-0">Already have an account? <a href="/login">Login</a></p>
    </div>
</form:form>


</body>
</html>
